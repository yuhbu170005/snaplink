package com.snaplink.controller;

import com.snaplink.dto.internal.UrlRedirectDto;
import com.snaplink.event.ClickTrackEvent;
import com.snaplink.exception.GlobalExceptionHandler;
import com.snaplink.exception.ResourceNotFoundException;
import com.snaplink.service.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RedirectControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UrlService urlService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RedirectController redirectController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(redirectController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /{code}: chuyển hướng thành công với HTTP 302 Found và phát ClickTrackEvent")
    void redirect_Success_Returns302AndPublishesEvent() throws Exception {
        UrlRedirectDto dto = UrlRedirectDto.builder()
                .id(1L)
                .shortCode("xyz123")
                .originalUrl("https://destination-url.com/landing")
                .isActive(true)
                .build();

        when(urlService.getRedirectInfo("xyz123")).thenReturn(dto);

        mockMvc.perform(get("/xyz123")
                        .header("User-Agent", "Mozilla/5.0")
                        .header("Referer", "https://google.com"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://destination-url.com/landing"));

        verify(eventPublisher).publishEvent(any(ClickTrackEvent.class));
    }

    @Test
    @DisplayName("GET /{code}: trả về HTTP 404 khi mã short code không tồn tại hoặc đã hết hạn")
    void redirect_NotFound_Returns404() throws Exception {
        when(urlService.getRedirectInfo("notfound")).thenThrow(new ResourceNotFoundException("Short URL not found: notfound"));

        mockMvc.perform(get("/notfound"))
                .andExpect(status().isNotFound());
    }
}
