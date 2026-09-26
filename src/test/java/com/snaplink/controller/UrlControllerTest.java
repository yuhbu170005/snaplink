package com.snaplink.controller;

import com.snaplink.config.security.CustomUserDetailsService;
import com.snaplink.config.security.JwtAuthenticationEntryPoint;
import com.snaplink.config.security.JwtAuthenticationFilter;
import com.snaplink.config.security.JwtTokenProvider;
import com.snaplink.dto.response.PageResponse;
import com.snaplink.dto.response.UrlResponse;
import com.snaplink.service.UrlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlController.class)
@AutoConfigureMockMvc(addFilters = false)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService urlService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/urls: tạo link thành công trả về 201 Created")
    void createShortUrl_Success() throws Exception {
        UrlResponse response = UrlResponse.builder()
                .id(1L)
                .shortCode("abc12")
                .shortUrl("http://localhost:8080/abc12")
                .originalUrl("https://spring.io")
                .isActive(true)
                .createdAt(Instant.now())
                .build();

        when(urlService.createShortUrl(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalUrl\":\"https://spring.io\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value("abc12"))
                .andExpect(jsonPath("$.originalUrl").value("https://spring.io"));
    }

    @Test
    @DisplayName("GET /api/urls: trả về danh sách phân trang 200 OK")
    void getMyUrls_Success() throws Exception {
        UrlResponse item = UrlResponse.builder()
                .id(1L)
                .shortCode("my-link")
                .shortUrl("http://localhost:8080/my-link")
                .originalUrl("https://example.com")
                .build();

        PageResponse<UrlResponse> pageResponse = PageResponse.<UrlResponse>builder()
                .content(List.of(item))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(urlService.getMyUrls(any(), any(Pageable.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/urls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].shortCode").value("my-link"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/urls/{id}: trả về chi tiết link 200 OK")
    void getUrlById_Success() throws Exception {
        UrlResponse response = UrlResponse.builder()
                .id(5L)
                .shortCode("code5")
                .shortUrl("http://localhost:8080/code5")
                .originalUrl("https://example.com/5")
                .build();

        when(urlService.getUrlById(eq(5L), any())).thenReturn(response);

        mockMvc.perform(get("/api/urls/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.shortCode").value("code5"));
    }

    @Test
    @DisplayName("DELETE /api/urls/{id}: xoá link thành công trả về 204 No Content")
    void deleteUrl_Success() throws Exception {
        doNothing().when(urlService).deleteUrl(eq(5L), any());

        mockMvc.perform(delete("/api/urls/5"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/urls/{id}/status: bật/tắt trạng thái thành công trả về 200 OK")
    void toggleUrlStatus_Success() throws Exception {
        UrlResponse response = UrlResponse.builder()
                .id(5L)
                .shortCode("code5")
                .isActive(false)
                .build();

        when(urlService.toggleUrlStatus(eq(5L), any())).thenReturn(response);

        mockMvc.perform(patch("/api/urls/5/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }
}
