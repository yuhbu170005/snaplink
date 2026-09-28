package com.snaplink.listener;

import com.snaplink.entity.ClickEvent;
import com.snaplink.entity.Url;
import com.snaplink.event.ClickTrackEvent;
import com.snaplink.repository.ClickEventRepository;
import com.snaplink.repository.UrlRepository;
import com.snaplink.util.GeoLocationUtil;
import com.snaplink.util.UserAgentParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClickTrackingListenerTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ClickEventRepository clickEventRepository;

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private UserAgentParser userAgentParser;

    @Mock
    private GeoLocationUtil geoLocationUtil;

    @InjectMocks
    private ClickTrackingListener clickTrackingListener;

    @Test
    @DisplayName("handleClickTrackEvent: Tăng counter Redis và lưu ClickEvent vào DB thành công")
    void handleClickTrackEvent_Success() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("url:clicks:100")).thenReturn(1L);

        when(userAgentParser.parse(anyString()))
                .thenReturn(new UserAgentParser.UserAgentDetails("DESKTOP", "Chrome", "macOS"));
        when(geoLocationUtil.resolveCountry(anyString())).thenReturn("VN");
        when(urlRepository.getReferenceById(100L)).thenReturn(Url.builder().id(100L).build());

        ClickTrackEvent event = ClickTrackEvent.builder()
                .urlId(100L)
                .shortCode("code100")
                .originalUrl("https://example.com")
                .ipAddress("127.0.0.1")
                .userAgent("Mozilla/5.0")
                .referrer("https://google.com")
                .clickedAt(Instant.now())
                .build();

        clickTrackingListener.handleClickTrackEvent(event);

        verify(valueOperations, times(1)).increment("url:clicks:100");
        verify(clickEventRepository, times(1)).save(any(ClickEvent.class));
    }

    @Test
    @DisplayName("handleClickTrackEvent: Xử lý ngoại lệ an toàn khi Redis gặp sự cố vẫn lưu DB")
    void handleClickTrackEvent_RedisError_HandledGracefully() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("url:clicks:101")).thenThrow(new RuntimeException("Redis connection error"));

        when(userAgentParser.parse(any())).thenReturn(new UserAgentParser.UserAgentDetails("MOBILE", "Safari", "iOS"));
        when(geoLocationUtil.resolveCountry(any())).thenReturn("US");
        when(urlRepository.getReferenceById(101L)).thenReturn(Url.builder().id(101L).build());

        ClickTrackEvent event = ClickTrackEvent.builder()
                .urlId(101L)
                .shortCode("code101")
                .originalUrl("https://example.com")
                .clickedAt(Instant.now())
                .build();

        clickTrackingListener.handleClickTrackEvent(event);

        verify(valueOperations, times(1)).increment("url:clicks:101");
        verify(clickEventRepository, times(1)).save(any(ClickEvent.class));
    }

    @Test
    @DisplayName("handleClickTrackEvent: Bỏ qua khi urlId rỗng")
    void handleClickTrackEvent_NullUrlId_Ignored() {
        ClickTrackEvent event = ClickTrackEvent.builder().urlId(null).build();
        clickTrackingListener.handleClickTrackEvent(event);

        verifyNoInteractions(stringRedisTemplate);
        verifyNoInteractions(clickEventRepository);
    }
}
