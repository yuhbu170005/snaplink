package com.snaplink.service;

import com.snaplink.dto.response.analytics.AnalyticsResponse;
import com.snaplink.dto.response.analytics.StatItem;
import com.snaplink.dto.response.analytics.TimeSeriesPoint;
import com.snaplink.entity.ClickEvent;
import com.snaplink.entity.Url;
import com.snaplink.exception.ResourceNotFoundException;
import com.snaplink.repository.ClickEventRepository;
import com.snaplink.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UrlRepository urlRepository;
    private final ClickEventRepository clickEventRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics(Long urlId, Long currentUserId) {
        Url url = urlRepository.findById(urlId)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found with id: " + urlId));

        // Check ownership if URL belongs to a registered user
        if (url.getUser() != null && !url.getUser().getId().equals(currentUserId)) {
            throw new ResourceNotFoundException("URL not found with id: " + urlId);
        }

        long totalClicks = clickEventRepository.countByUrlId(urlId);
        long uniqueVisitors = clickEventRepository.countDistinctIpByUrlId(urlId);

        List<StatItem> devices = mapGroupedStats(clickEventRepository.countGroupedByDeviceType(urlId), totalClicks);
        List<StatItem> browsers = mapGroupedStats(clickEventRepository.countGroupedByBrowser(urlId), totalClicks);
        List<StatItem> countries = mapGroupedStats(clickEventRepository.countGroupedByCountry(urlId), totalClicks);
        List<StatItem> referrers = mapGroupedStats(clickEventRepository.countGroupedByReferrer(urlId), totalClicks);

        // Time Series data (grouped by date)
        List<ClickEvent> allClicks = clickEventRepository.findByUrlIdOrderByClickedAtAsc(urlId);
        List<TimeSeriesPoint> clicksOverTime = buildTimeSeries(allClicks);

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
        }).collect(Collectors.toList());
    }

    private List<TimeSeriesPoint> buildTimeSeries(List<ClickEvent> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Long> dateCountMap = new LinkedHashMap<>();
        for (ClickEvent event : events) {
            if (event.getClickedAt() != null) {
                String dateStr = DATE_FORMATTER.format(event.getClickedAt());
                dateCountMap.put(dateStr, dateCountMap.getOrDefault(dateStr, 0L) + 1L);
            }
        }

        return dateCountMap.entrySet().stream()
                .map(entry -> TimeSeriesPoint.builder()
                        .timestamp(entry.getKey())
                        .clicks(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }
}
