package com.snaplink.dto.response.analytics;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnalyticsResponse {
    Long urlId;
    String shortCode;
    String originalUrl;
    long totalClicks;
    long uniqueVisitors;
    List<TimeSeriesPoint> clicksOverTime;
    List<StatItem> devices;
    List<StatItem> browsers;
    List<StatItem> countries;
    List<StatItem> referrers;
}
