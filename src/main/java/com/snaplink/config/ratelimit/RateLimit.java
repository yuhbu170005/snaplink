package com.snaplink.config.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation mô tả quota trên controller. RateLimitAspect là nơi thực thi giới hạn này. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Quota cho guest theo IP trong một khoảng thời gian.
     */
    int guestLimit() default 10;

    /**
     * Quota cho user đã đăng nhập, nhận diện bằng user ID.
     */
    int authLimit() default 30;

    /**
     * Độ dài cửa sổ tính quota, đơn vị giây; mặc định 60 giây.
     */
    int windowSeconds() default 60;

    /**
     * Namespace của quota để login/register/create_url không dùng chung bộ đếm.
     */
    String keyPrefix() default "create_url";
}
