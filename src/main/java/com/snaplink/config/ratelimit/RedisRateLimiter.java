package com.snaplink.config.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/** Fixed window trên Redis. Lua gộp INCR và EXPIRE thành một thao tác atomic để tránh race condition. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRateLimiter {

    private final StringRedisTemplate stringRedisTemplate;

    // INCR và EXPIRE chạy cùng một Lua script nên không bị request khác chen giữa.
    // Script chỉ đếm + trả TTL; Java bên dưới so sánh count với quota.
    private static final String RATE_LIMIT_LUA_SCRIPT = """
            local key = KEYS[1]
            local window = tonumber(ARGV[1])
            local current = redis.call('INCR', key)
            if current == 1 then
                redis.call('EXPIRE', key, window)
            end
            local ttl = redis.call('TTL', key)
            if ttl < 0 then
                redis.call('EXPIRE', key, window)
                ttl = window
            end
            return { current, ttl }
            """;

    private final RedisScript<List> script = new DefaultRedisScript<>(RATE_LIMIT_LUA_SCRIPT, List.class);

    /** Thực thi Lua rồi tính quota còn lại. Chính sách hiện tại fail-open khi Redis không khả dụng. */
    @SuppressWarnings("unchecked")
    public RateLimitResult checkRateLimit(String key, long limit, long windowSeconds) {
        try {
            List<?> result = stringRedisTemplate.execute(
                    script,
                    Collections.singletonList(key),
                    String.valueOf(windowSeconds)
            );

            if (result != null && result.size() >= 2) {
                long currentCount = ((Number) result.get(0)).longValue();
                long ttl = ((Number) result.get(1)).longValue();
                boolean allowed = currentCount <= limit;
                long remaining = Math.max(0, limit - currentCount);

                return RateLimitResult.builder()
                        .allowed(allowed)
                        .currentCount(currentCount)
                        .limit(limit)
                        .remaining(remaining)
                        .resetSeconds(Math.max(1, ttl))
                        .build();
            }
        } catch (Exception ex) {
            log.warn("Redis rate limiter error for key {}: {}. Fail-open active.", key, ex.getMessage());
        }

        // Fail-open: Redis lỗi thì cho request đi tiếp để giữ API khả dụng.
        return RateLimitResult.builder()
                .allowed(true)
                .currentCount(1)
                .limit(limit)
                .remaining(Math.max(0, limit - 1))
                .resetSeconds(windowSeconds)
                .build();
    }
}
