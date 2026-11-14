package com.snaplink.service;

import com.snaplink.dto.response.analytics.AnalyticsResponse;
import com.snaplink.dto.response.analytics.StatItem;
import com.snaplink.dto.response.analytics.TimeSeriesPoint;
import com.snaplink.entity.Url;
import com.snaplink.exception.ResourceNotFoundException;
import com.snaplink.repository.ClickEventRepository;
import com.snaplink.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/** Đọc dữ liệu aggregate từ repository và chuyển thành DTO cho dashboard. */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UrlRepository urlRepository;
    private final ClickEventRepository clickEventRepository;

    /** Kiểm tra chủ sở hữu trước, sau đó mới truy vấn thống kê để tránh lộ dữ liệu. */
    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics(Long urlId, Long currentUserId) {
        Url url = urlRepository.findById(urlId)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found with id: " + urlId));

        // Guest link không có chủ sở hữu nên không có dashboard analytics riêng.
        if (currentUserId == null || url.getUser() == null || !url.getUser().getId().equals(currentUserId)) {
            throw new ResourceNotFoundException("URL not found with id: " + urlId);
        }

        long totalClicks = clickEventRepository.countByUrlId(urlId);
        long uniqueVisitors = clickEventRepository.countDistinctIpByUrlId(urlId);

        List<StatItem> devices = mapGroupedStats(clickEventRepository.countGroupedByDeviceType(urlId), totalClicks);
        List<StatItem> browsers = mapGroupedStats(clickEventRepository.countGroupedByBrowser(urlId), totalClicks);
        List<StatItem> countries = mapGroupedStats(clickEventRepository.countGroupedByCountry(urlId), totalClicks);
        List<StatItem> referrers = mapGroupedStats(clickEventRepository.countGroupedByReferrer(urlId), totalClicks);

        List<TimeSeriesPoint> clicksOverTime = buildTimeSeries(clickEventRepository.countGroupedByDay(urlId));

        return AnalyticsResponse.builder()
                .urlId(url.getId())
                .shortCode(url.getCustomAlias() != null ? url.getCustomAlias() : url.getShortCode())
                .originalUrl(url.getOriginalUrl())
                .totalClicks(totalClicks)
                .uniqueVisitors(uniqueVisitors)
                .clicksOverTime(clicksOverTime)
                .devices(devices)
                .browsers(browsers)
                .countries(countries)
                .referrers(referrers)
                .build();
    }

    /** Chuyển từng nhóm [tên, số lượng] thành DTO và tính phần trăm, xử lý tổng bằng 0. */
    private List<StatItem> mapGroupedStats(List<Object[]> rawList, long totalClicks) {
        if (rawList == null || rawList.isEmpty() || totalClicks == 0) {
            return Collections.emptyList();
        }

        return rawList.stream().map(row -> {
            String name = row[0] != null ? row[0].toString() : "UNKNOWN";
            long count = ((Number) row[1]).longValue();
            double percentage = Math.round(((double) count / totalClicks * 100.0) * 10.0) / 10.0;
            return StatItem.builder()
                    .name(name)
                    .count(count)
                    .percentage(percentage)
                    .build();
        }).toList();
    }

    /** Mỗi dòng SQL trả [ngày UTC, số click]; chuyển sang DTO dùng cho biểu đồ. */
    private List<TimeSeriesPoint> buildTimeSeries(List<Object[]> rows) {
        return rows.stream()
                .map(row -> TimeSeriesPoint.builder()
                        .timestamp(row[0].toString())
                        .clicks(((Number) row[1]).longValue())
                        .build())
                .toList();
    }
}
