package com.snaplink.dto.response.analytics;

import lombok.*;

/** Một điểm biểu đồ gồm ngày UTC yyyy-MM-dd và số click ngày đó. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSeriesPoint {
    String timestamp; // ISO Date (e.g. "2026-09-28" or "2026-09-28 18:00")
    private long clicks;
}
