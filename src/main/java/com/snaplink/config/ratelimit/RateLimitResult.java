package com.snaplink.config.ratelimit;

import lombok.Builder;
import lombok.Getter;

/** Kết quả kiểm tra quota: có được phép tiếp tục, số request còn lại và thời gian chờ (giây). */
@Getter
@Builder
public class RateLimitResult {
    private final boolean allowed;
    private final long currentCount;
    private final long limit;
    private final long remaining;
    private final long resetSeconds;
}
