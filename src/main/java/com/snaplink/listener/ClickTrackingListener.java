package com.snaplink.listener;

import com.snaplink.config.async.AsyncConfig;
import com.snaplink.entity.ClickEvent;
import com.snaplink.entity.Url;
import com.snaplink.event.ClickTrackEvent;
import com.snaplink.repository.ClickEventRepository;
import com.snaplink.repository.UrlRepository;
import com.snaplink.util.GeoLocationUtil;
import com.snaplink.util.UserAgentParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClickTrackingListener {

    private final StringRedisTemplate stringRedisTemplate;
    private final ClickEventRepository clickEventRepository;
    private final UrlRepository urlRepository;
    private final UserAgentParser userAgentParser;
    private final GeoLocationUtil geoLocationUtil;

    private static final String REDIS_CLICKS_KEY_PREFIX = "url:clicks:";

    @Async(AsyncConfig.CLICK_TRACKING_EXECUTOR)
    @EventListener
    public void handleClickTrackEvent(ClickTrackEvent event) {
        log.info("[Async Telemetry] Processing click event for urlId={}, shortCode='{}' on thread '{}'",
                event.getUrlId(), event.getShortCode(), Thread.currentThread().getName());

        if (event.getUrlId() == null) {
            return;
        }

        // 1. Increment Atomic Redis Counter for instant response / dashboard
        try {
            String redisKey = REDIS_CLICKS_KEY_PREFIX + event.getUrlId();
            Long totalClicks = stringRedisTemplate.opsForValue().increment(redisKey);
            log.debug("Incremented click counter for urlId {} to {}", event.getUrlId(), totalClicks);
        } catch (Exception ex) {
            log.warn("Failed to increment click count in Redis for urlId {}: {}", event.getUrlId(), ex.getMessage());
        }

        // 2. Parse User-Agent and Geolocation, then Persist ClickEvent entity to PostgreSQL
        try {
            UserAgentParser.UserAgentDetails ua = userAgentParser.parse(event.getUserAgent());
            String country = geoLocationUtil.resolveCountry(event.getIpAddress());

            Url urlProxy = urlRepository.getReferenceById(event.getUrlId());

            ClickEvent clickEvent = ClickEvent.builder()
                    .url(urlProxy)
                    .clickedAt(event.getClickedAt() != null ? event.getClickedAt() : Instant.now())
                    .ipAddress(event.getIpAddress())
                    .country(country)
                    .deviceType(ua.deviceType())
                    .browser(ua.browser())
                    .referrer(event.getReferrer())
                    .build();

            clickEventRepository.save(clickEvent);
            log.debug("Persisted click event entity for urlId {} (device: {}, browser: {}, country: {})",
                    event.getUrlId(), ua.deviceType(), ua.browser(), country);
        } catch (Exception ex) {
            log.error("Failed to persist ClickEvent in database for urlId {}: {}", event.getUrlId(), ex.getMessage(), ex);
        }
    }
}
