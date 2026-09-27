package com.snaplink.config.ratelimit;

import com.snaplink.config.security.UserPrincipal;
import com.snaplink.exception.RateLimitExceededException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

    @Mock
    private RedisRateLimiter redisRateLimiter;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private RateLimit rateLimit;

    @InjectMocks
    private RateLimitAspect rateLimitAspect;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

        when(rateLimit.keyPrefix()).thenReturn("create_url");
        when(rateLimit.windowSeconds()).thenReturn(60);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Guest Request: Rate limit theo IP cho phép proceed khi trong hạn mức")
    void enforceRateLimit_Guest_Allowed() throws Throwable {
        request.setRemoteAddr("192.168.1.100");
        when(rateLimit.guestLimit()).thenReturn(10);

        RateLimitResult result = RateLimitResult.builder()
                .allowed(true)
                .currentCount(1)
                .limit(10)
                .remaining(9)
                .resetSeconds(60)
                .build();

        when(redisRateLimiter.checkRateLimit(eq("ratelimit:create_url:ip:192.168.1.100"), eq(10L), eq(60L)))
                .thenReturn(result);
        when(joinPoint.proceed()).thenReturn("success");

        Object proceedResult = rateLimitAspect.enforceRateLimit(joinPoint, rateLimit);

        assertThat(proceedResult).isEqualTo("success");
        assertThat(response.getHeader("X-RateLimit-Limit")).isEqualTo("10");
        assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo("9");
        assertThat(response.getHeader("X-RateLimit-Reset")).isEqualTo("60");
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    @DisplayName("Guest Request: Ném RateLimitExceededException khi vượt hạn ngạch IP")
    void enforceRateLimit_Guest_Exceeded() throws Throwable {
        request.setRemoteAddr("192.168.1.100");
        when(rateLimit.guestLimit()).thenReturn(10);

        RateLimitResult result = RateLimitResult.builder()
                .allowed(false)
                .currentCount(11)
                .limit(10)
                .remaining(0)
                .resetSeconds(45)
                .build();

        when(redisRateLimiter.checkRateLimit(eq("ratelimit:create_url:ip:192.168.1.100"), eq(10L), eq(60L)))
                .thenReturn(result);

        assertThatThrownBy(() -> rateLimitAspect.enforceRateLimit(joinPoint, rateLimit))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessageContaining("Rate limit exceeded. Try again in 45 seconds.");

        assertThat(response.getHeader("X-RateLimit-Limit")).isEqualTo("10");
        assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo("0");
        assertThat(response.getHeader("X-RateLimit-Reset")).isEqualTo("45");
        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("Authenticated User: Rate limit theo User ID cho phép proceed với hạn mức cao hơn")
    void enforceRateLimit_AuthUser_Allowed() throws Throwable {
        UserPrincipal principal = new UserPrincipal(42L, "user42@snaplink.com", "pwd", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        when(rateLimit.authLimit()).thenReturn(30);

        RateLimitResult result = RateLimitResult.builder()
                .allowed(true)
                .currentCount(5)
                .limit(30)
                .remaining(25)
                .resetSeconds(55)
                .build();

        when(redisRateLimiter.checkRateLimit(eq("ratelimit:create_url:user:42"), eq(30L), eq(60L)))
                .thenReturn(result);
        when(joinPoint.proceed()).thenReturn("auth_success");

        Object proceedResult = rateLimitAspect.enforceRateLimit(joinPoint, rateLimit);

        assertThat(proceedResult).isEqualTo("auth_success");
        assertThat(response.getHeader("X-RateLimit-Limit")).isEqualTo("30");
        assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo("25");
        assertThat(response.getHeader("X-RateLimit-Reset")).isEqualTo("55");
        verify(joinPoint, times(1)).proceed();
    }
}
