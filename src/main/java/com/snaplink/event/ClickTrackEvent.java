package com.snaplink.event;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Builder
@ToString
public class ClickTrackEvent {
    private final Long urlId;
    private final String shortCode;
    private final String originalUrl;
    private final String ipAddress;
    private final String userAgent;
    private final String referrer;
    private final Instant clickedAt;
}
