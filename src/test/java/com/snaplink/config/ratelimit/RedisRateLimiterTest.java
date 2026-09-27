package com.snaplink.config.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisRateLimiterTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @InjectMocks
    private RedisRateLimiter redisRateLimiter;

    @Test
    @DisplayName("checkRateLimit: Cho phép request khi count <= limit")
    @SuppressWarnings("unchecked")
    void checkRateLimit_Allowed() {
        when(stringRedisTemplate.execute(any(RedisScript.class), anyList(), any(), any()))
                .thenReturn(List.of(5L, 50L));

        RateLimitResult result = redisRateLimiter.checkRateLimit("test_key", 10, 60);

        assertThat(result.isAllowed()).isTrue();
        assertThat(result.getCurrentCount()).isEqualTo(5L);
        assertThat(result.getLimit()).isEqualTo(10L);
        assertThat(result.getRemaining()).isEqualTo(5L);
        assertThat(result.getResetSeconds()).isEqualTo(50L);
    }

    @Test
    @DisplayName("checkRateLimit: Từ chối request khi count > limit")
    @SuppressWarnings("unchecked")
    void checkRateLimit_Exceeded() {
        when(stringRedisTemplate.execute(any(RedisScript.class), anyList(), any(), any()))
                .thenReturn(List.of(11L, 35L));

        RateLimitResult result = redisRateLimiter.checkRateLimit("test_key", 10, 60);

        assertThat(result.isAllowed()).isFalse();
        assertThat(result.getCurrentCount()).isEqualTo(11L);
        assertThat(result.getLimit()).isEqualTo(10L);
        assertThat(result.getRemaining()).isEqualTo(0L);
        assertThat(result.getResetSeconds()).isEqualTo(35L);
    }

    @Test
    @DisplayName("checkRateLimit: Fail-open an toàn khi Redis throw Exception")
    @SuppressWarnings("unchecked")
    void checkRateLimit_RedisException_FailOpen() {
        when(stringRedisTemplate.execute(any(RedisScript.class), anyList(), any(), any()))
                .thenThrow(new RuntimeException("Redis connection refused"));

        RateLimitResult result = redisRateLimiter.checkRateLimit("test_key", 10, 60);

        assertThat(result.isAllowed()).isTrue();
        assertThat(result.getLimit()).isEqualTo(10L);
        assertThat(result.getRemaining()).isEqualTo(9L);
    }
}
