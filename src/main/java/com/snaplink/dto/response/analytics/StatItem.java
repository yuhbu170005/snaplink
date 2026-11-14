package com.snaplink.dto.response.analytics;

import lombok.*;

/** Một nhóm thống kê: tên nhóm, số click và phần trăm so với tổng click. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatItem {
    private String name;
    private long count;
    double percentage;
}
