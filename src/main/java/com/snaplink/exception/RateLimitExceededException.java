package com.snaplink.exception;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends RuntimeException {

    private final long retryAfterSeconds;
    private final long limit;
    private final long remaining;

    public RateLimitExceededException(String message, long retryAfterSeconds, long limit, long remaining) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
        this.limit = limit;
        this.remaining = remaining;
    }

    public RateLimitExceededException(String message, long retryAfterSeconds) {
        this(message, retryAfterSeconds, 0, 0);
    }
}
