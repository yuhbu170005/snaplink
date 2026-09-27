package com.snaplink.config.ratelimit;

import com.snaplink.config.security.UserPrincipal;
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

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisRateLimiter redisRateLimiter;

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
            String clientIp = extractClientIp(request);
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

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.trim().isEmpty()) {
            return xRealIp.trim();
        }

        String remoteAddr = request.getRemoteAddr();
        return (remoteAddr != null && !remoteAddr.trim().isEmpty()) ? remoteAddr : "unknown";
    }
}
