package com.snaplink.controller;

import com.snaplink.dto.response.analytics.AnalyticsResponse;
import com.snaplink.dto.response.analytics.StatItem;
import com.snaplink.dto.response.analytics.TimeSeriesPoint;
import com.snaplink.exception.GlobalExceptionHandler;
import com.snaplink.exception.ResourceNotFoundException;
import com.snaplink.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private AnalyticsController analyticsController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(analyticsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/urls/{id}/analytics: Trả về HTTP 200 OK với đầy đủ dữ liệu thống kê")
    void getAnalytics_Success() throws Exception {
        AnalyticsResponse response = AnalyticsResponse.builder()
                .urlId(5L)
                .shortCode("short5")
                .originalUrl("https://example.com")
                .totalClicks(42L)
                .uniqueVisitors(35L)
                .clicksOverTime(List.of(new TimeSeriesPoint("2026-09-28", 42L)))
                .devices(List.of(new StatItem("DESKTOP", 30L, 71.4)))
                .browsers(List.of(new StatItem("Chrome", 28L, 66.7)))
                .countries(List.of(new StatItem("VN", 35L, 83.3)))
                .referrers(List.of(new StatItem("https://google.com", 20L, 47.6)))
                .build();

        when(analyticsService.getAnalytics(eq(5L), any())).thenReturn(response);

        mockMvc.perform(get("/api/urls/5/analytics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urlId").value(5L))
                .andExpect(jsonPath("$.shortCode").value("short5"))
                .andExpect(jsonPath("$.totalClicks").value(42))
                .andExpect(jsonPath("$.uniqueVisitors").value(35))
                .andExpect(jsonPath("$.devices[0].name").value("DESKTOP"))
                .andExpect(jsonPath("$.browsers[0].name").value("Chrome"))
                .andExpect(jsonPath("$.countries[0].name").value("VN"));
    }

    @Test
    @DisplayName("GET /api/urls/{id}/analytics: Trả về HTTP 404 khi link không tồn tại hoặc không có quyền")
    void getAnalytics_NotFound() throws Exception {
        when(analyticsService.getAnalytics(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("URL not found with id: 99"));

        mockMvc.perform(get("/api/urls/99/analytics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
