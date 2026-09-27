package com.snaplink.config.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRateLimiter {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String RATE_LIMIT_LUA_SCRIPT =
            "local key = KEYS[1]\n" +
            "local limit = tonumber(ARGV[1])\n" +
            "local window = tonumber(ARGV[2])\n" +
            "\n" +
            "local current = redis.call('INCR', key)\n" +
            "if current == 1 then\n" +
            "    redis.call('EXPIRE', key, window)\n" +
            "end\n" +
            "\n" +
            "local ttl = redis.call('TTL', key)\n" +
            "if ttl < 0 then\n" +
            "    redis.call('EXPIRE', key, window)\n" +
            "    ttl = window\n" +
            "end\n" +
            "\n" +
            "return { current, ttl }";

    private final RedisScript<List> script = new DefaultRedisScript<>(RATE_LIMIT_LUA_SCRIPT, List.class);

    @SuppressWarnings("unchecked")
    public RateLimitResult checkRateLimit(String key, long limit, long windowSeconds) {
        try {
            List<?> result = stringRedisTemplate.execute(
                    script,
                    Collections.singletonList(key),
                    String.valueOf(limit),
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

        // Fail-open fallback: allow request if Redis encounters error
        return RateLimitResult.builder()
                .allowed(true)
                .currentCount(1)
                .limit(limit)
                .remaining(Math.max(0, limit - 1))
                .resetSeconds(windowSeconds)
                .build();
    }
}
