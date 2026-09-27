package com.snaplink.config.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Maximum number of requests allowed for guest users (identified by Client IP) within the window.
     */
    int guestLimit() default 10;

    /**
     * Maximum number of requests allowed for authenticated users (identified by User ID) within the window.
     */
    int authLimit() default 30;

    /**
     * Time window in seconds. Default is 60 seconds (1 minute).
     */
    int windowSeconds() default 60;

    /**
     * Unique key prefix for this rate limit bucket.
     */
    String keyPrefix() default "create_url";
}
