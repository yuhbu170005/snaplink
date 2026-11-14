package com.snaplink.dto.response.analytics;

import lombok.*;

import java.util.List;

/** DTO tổng hợp thống kê toàn thời gian của một link; clicksOverTime gom theo ngày UTC. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsResponse {
    private Long urlId;
    private String shortCode;
    private String originalUrl;
    private long totalClicks;
    private long uniqueVisitors;
    private List<TimeSeriesPoint> clicksOverTime;
    private List<StatItem> devices;
    private List<StatItem> browsers;
    private List<StatItem> countries;
    private List<StatItem> referrers;
}
