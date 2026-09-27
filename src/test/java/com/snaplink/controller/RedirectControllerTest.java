package com.snaplink.controller;

import com.snaplink.config.security.CustomUserDetailsService;
import com.snaplink.config.security.JwtAuthenticationEntryPoint;
import com.snaplink.config.security.JwtAuthenticationFilter;
import com.snaplink.config.security.JwtTokenProvider;
import com.snaplink.exception.ResourceNotFoundException;
import com.snaplink.service.UrlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RedirectController.class)
@AutoConfigureMockMvc(addFilters = false)
class RedirectControllerTest {

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
    @DisplayName("GET /{code}: chuyển hướng thành công với HTTP 302 Found và header Location")
    void redirect_Success_Returns302() throws Exception {
        when(urlService.getOriginalUrl("xyz123")).thenReturn("https://destination-url.com/landing");

        mockMvc.perform(get("/xyz123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://destination-url.com/landing"));
    }

    @Test
    @DisplayName("GET /{code}: trả về HTTP 404 khi mã short code không tồn tại hoặc đã hết hạn")
    void redirect_NotFound_Returns404() throws Exception {
        when(urlService.getOriginalUrl("notfound")).thenThrow(new ResourceNotFoundException("Short URL not found: notfound"));

        mockMvc.perform(get("/notfound"))
                .andExpect(status().isNotFound());
    }
}
