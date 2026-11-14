package com.snaplink.config.ratelimit;

import com.snaplink.config.security.UserPrincipal;
import com.snaplink.util.ClientIpResolver;
import org.springframework.beans.factory.annotation.Value;
import com.snaplink.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** AOP chặn lời gọi hàm có @RateLimit: chọn key theo user/IP, kiểm tra quota rồi mới cho hàm chạy. */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisRateLimiter redisRateLimiter;

    @Value("${app.trust-proxy-headers:false}")
    private boolean trustProxyHeaders;

    /** Được AOP gọi trước controller: vượt quota thì ném exception, còn quota mới proceed(). */
    @Around("@annotation(rateLimit)")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();

        String identifier;
        long limit;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserPrincipal principal) {
            identifier = "user:" + principal.getId();
            limit = rateLimit.authLimit();
        } else {
            String clientIp = ClientIpResolver.resolve(request, trustProxyHeaders);
            identifier = "ip:" + clientIp;
            limit = rateLimit.guestLimit();
        }

        String rateLimitKey = "ratelimit:" + rateLimit.keyPrefix() + ":" + identifier;
        RateLimitResult result = redisRateLimiter.checkRateLimit(rateLimitKey, limit, rateLimit.windowSeconds());

        if (response != null) {
            response.setHeader("X-RateLimit-Limit", String.valueOf(result.getLimit()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));
            response.setHeader("X-RateLimit-Reset", String.valueOf(result.getResetSeconds()));
        }

        if (!result.isAllowed()) {
            log.warn("Rate limit exceeded for {} on key {}. Current: {}, Limit: {}",
                    identifier, rateLimitKey, result.getCurrentCount(), result.getLimit());
            throw new RateLimitExceededException(
                    "Rate limit exceeded. Try again in " + result.getResetSeconds() + " seconds.",
                    result.getResetSeconds(),
                    result.getLimit(),
                    result.getRemaining()
            );
        }

        return joinPoint.proceed();
    }

}
