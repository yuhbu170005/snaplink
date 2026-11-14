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

/** Nhận ClickTrackEvent qua @EventListener và xử lý trên executor riêng nhờ @Async. */
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

    /** Tăng counter Redis và lưu click độc lập; lỗi một bước không ngăn bước còn lại. */
    @Async(AsyncConfig.CLICK_TRACKING_EXECUTOR)
    @EventListener
    public void handleClickTrackEvent(ClickTrackEvent event) {
        if (event.getUrlId() == null) {
            return;
        }

        incrementCounter(event.getUrlId());
        persistClick(event);
    }

    /** Counter chỉ là số liệu phụ trợ; dashboard lấy click đã lưu trong DB làm nguồn chính. */
    private void incrementCounter(Long urlId) {
        try {
            stringRedisTemplate.opsForValue().increment(REDIS_CLICKS_KEY_PREFIX + urlId);
        } catch (Exception ex) {
            log.warn("Cannot increment click counter for urlId={}: {}", urlId, ex.getMessage());
        }
    }

    /** Lưu metadata đã giới hạn độ dài để header lạ không làm hỏng INSERT click. */
    private void persistClick(ClickTrackEvent event) {
        try {
            UserAgentParser.UserAgentDetails details = userAgentParser.parse(event.getUserAgent());
            String country = geoLocationUtil.resolveCountry(event.getIpAddress());
            Url url = urlRepository.getReferenceById(event.getUrlId());
            ClickEvent click = ClickEvent.builder()
                    .url(url)
                    .clickedAt(event.getClickedAt() != null ? event.getClickedAt() : Instant.now())
                    .ipAddress(limitLength(event.getIpAddress(), 45))
                    .country(limitLength(country, 100))
                    .deviceType(limitLength(details.deviceType(), 50))
                    .browser(limitLength(details.browser(), 50))
                    .referrer(limitLength(event.getReferrer(), 2048))
                    .build();
            clickEventRepository.save(click);
        } catch (Exception ex) {
            // Có thể link bị xoá trong khi click còn chờ ở queue. Redirect vẫn đã thành công.
            log.error("Cannot persist click for urlId={}", event.getUrlId(), ex);
        }
    }

    private String limitLength(String value, int maxLength) {
        return value == null || value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
