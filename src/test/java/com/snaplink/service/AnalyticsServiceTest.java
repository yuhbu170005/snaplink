package com.snaplink.service;

import com.snaplink.dto.response.analytics.AnalyticsResponse;
import com.snaplink.entity.ClickEvent;
import com.snaplink.entity.Url;
import com.snaplink.entity.User;
import com.snaplink.exception.ResourceNotFoundException;
import com.snaplink.repository.ClickEventRepository;
import com.snaplink.repository.UrlRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private ClickEventRepository clickEventRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    @DisplayName("getAnalytics: Trả về đầy đủ số liệu thống kê khi URL hợp lệ")
    void getAnalytics_Success() {
        User user = User.builder().id(1L).email("user@example.com").build();
        Url url = Url.builder()
                .id(10L)
                .shortCode("abc12345")
                .originalUrl("https://example.com/target")
                .user(user)
                .build();

        when(urlRepository.findById(10L)).thenReturn(Optional.of(url));
        when(clickEventRepository.countByUrlId(10L)).thenReturn(100L);
        when(clickEventRepository.countDistinctIpByUrlId(10L)).thenReturn(85L);

        List<Object[]> deviceData = new ArrayList<>();
        deviceData.add(new Object[]{"DESKTOP", 70L});
        deviceData.add(new Object[]{"MOBILE", 30L});
        when(clickEventRepository.countGroupedByDeviceType(10L)).thenReturn(deviceData);

        List<Object[]> browserData = new ArrayList<>();
        browserData.add(new Object[]{"Chrome", 60L});
        browserData.add(new Object[]{"Safari", 40L});
        when(clickEventRepository.countGroupedByBrowser(10L)).thenReturn(browserData);

        List<Object[]> countryData = new ArrayList<>();
        countryData.add(new Object[]{"VN", 80L});
        countryData.add(new Object[]{"US", 20L});
        when(clickEventRepository.countGroupedByCountry(10L)).thenReturn(countryData);

        List<Object[]> referrerData = new ArrayList<>();
        referrerData.add(new Object[]{"https://google.com", 50L});
        when(clickEventRepository.countGroupedByReferrer(10L)).thenReturn(referrerData);

        List<ClickEvent> clickEvents = List.of(
                ClickEvent.builder().clickedAt(Instant.parse("2026-09-28T10:00:00Z")).build(),
                ClickEvent.builder().clickedAt(Instant.parse("2026-09-28T12:00:00Z")).build()
        );
        when(clickEventRepository.findByUrlIdOrderByClickedAtAsc(10L)).thenReturn(clickEvents);

        AnalyticsResponse response = analyticsService.getAnalytics(10L, 1L);

        assertNotNull(response);
        assertEquals(10L, response.getUrlId());
        assertEquals("abc12345", response.getShortCode());
        assertEquals(100L, response.getTotalClicks());
        assertEquals(85L, response.getUniqueVisitors());
        assertEquals(2, response.getDevices().size());
        assertEquals("DESKTOP", response.getDevices().get(0).getName());
        assertEquals(70.0, response.getDevices().get(0).getPercentage());
        assertEquals(1, response.getClicksOverTime().size());
        assertEquals(2L, response.getClicksOverTime().get(0).getClicks());
    }

    @Test
    @DisplayName("getAnalytics: Ném ResourceNotFoundException khi URL không tồn tại")
    void getAnalytics_NotFound() {
        when(urlRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> analyticsService.getAnalytics(999L, 1L));
    }

    @Test
    @DisplayName("getAnalytics: Ném ResourceNotFoundException khi truy cập link của người khác")
    void getAnalytics_OtherUserLink_Forbidden() {
        User owner = User.builder().id(2L).build();
        Url url = Url.builder().id(10L).user(owner).build();
        when(urlRepository.findById(10L)).thenReturn(Optional.of(url));

        assertThrows(ResourceNotFoundException.class, () -> analyticsService.getAnalytics(10L, 1L));
    }

    @Test
    @DisplayName("getAnalytics: Xử lý an toàn khi URL chưa có lượt click nào")
    void getAnalytics_ZeroClicks() {
        Url url = Url.builder().id(20L).shortCode("empty20").originalUrl("https://example.com").build();
        when(urlRepository.findById(20L)).thenReturn(Optional.of(url));
        when(clickEventRepository.countByUrlId(20L)).thenReturn(0L);
        when(clickEventRepository.countDistinctIpByUrlId(20L)).thenReturn(0L);
        when(clickEventRepository.countGroupedByDeviceType(20L)).thenReturn(List.of());
        when(clickEventRepository.countGroupedByBrowser(20L)).thenReturn(List.of());
        when(clickEventRepository.countGroupedByCountry(20L)).thenReturn(List.of());
        when(clickEventRepository.countGroupedByReferrer(20L)).thenReturn(List.of());
        when(clickEventRepository.findByUrlIdOrderByClickedAtAsc(20L)).thenReturn(List.of());

        AnalyticsResponse response = analyticsService.getAnalytics(20L, null);

        assertNotNull(response);
        assertEquals(0L, response.getTotalClicks());
        assertTrue(response.getDevices().isEmpty());
        assertTrue(response.getClicksOverTime().isEmpty());
    }
}
