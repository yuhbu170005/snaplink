package com.snaplink.dto.response.analytics;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimeSeriesPoint {
    String timestamp; // ISO Date (e.g. "2026-09-28" or "2026-09-28 18:00")
    long clicks;
}
