package com.snaplink.controller;

import com.snaplink.dto.internal.UrlRedirectDto;
import com.snaplink.event.ClickTrackEvent;
import com.snaplink.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.concurrent.RejectedExecutionException;
import lombok.extern.slf4j.Slf4j;
import com.snaplink.util.ClientIpResolver;
import org.springframework.beans.factory.annotation.Value;
import java.time.Instant;

/** Nhận short code, tìm link gốc rồi trả HTTP 302; click tracking chạy riêng ở background. */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Redirect", description = "Endpoint chuyển hướng tốc độ cao (Two-Level Cache L1/L2) và ghi nhận telemetry bất đồng bộ")
public class RedirectController {

    private final UrlService urlService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.trust-proxy-headers:false}")
    private boolean trustProxyHeaders;

    @GetMapping("/{code:[a-zA-Z0-9_-]+}")
    @Operation(
            summary = "Chuyển hướng đến URL gốc (HTTP 302 Found)",
            description = "Truy xuất nhanh qua L1 In-Memory Caffeine / L2 Redis Cloud. Phát sự kiện click tracking bất đồng bộ không gây nghẽn luồng.",
            responses = {
                    @ApiResponse(responseCode = "302", description = "Chuyển hướng thành công tới Location header"),
                    @ApiResponse(responseCode = "404", description = "Link không tồn tại, bị vô hiệu hoặc đã hết hạn")
            }
    )
    /** Trả Location + HTTP 302 sau khi tìm link; hàng đợi tracking đầy vẫn trả redirect. */
    public ResponseEntity<Void> redirect(
            @PathVariable String code,
            HttpServletRequest request
    ) {
        UrlRedirectDto redirectDto = urlService.getRedirectInfo(code);

        // Phát sự kiện ghi nhận click bất đồng bộ (Non-blocking)
        try {
            publishClickEvent(redirectDto, request);
        } catch (RejectedExecutionException ex) {
            // Tracking là best-effort: đầy hàng đợi không được làm mất redirect.
            log.warn("Click tracking queue full; skipped click for urlId={}", redirectDto.getId());
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectDto.getOriginalUrl()))
                .build();
    }

    /** Chụp IP, user-agent, referrer và thời điểm click trước khi rời luồng HTTP. */
    private void publishClickEvent(UrlRedirectDto redirectDto, HttpServletRequest request) {
        String ipAddress = ClientIpResolver.resolve(request, trustProxyHeaders);
        String userAgent = request.getHeader("User-Agent");
        String referrer = request.getHeader("Referer");

        ClickTrackEvent event = ClickTrackEvent.builder()
                .urlId(redirectDto.getId())
                .shortCode(redirectDto.getShortCode())
                .originalUrl(redirectDto.getOriginalUrl())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .referrer(referrer)
                .clickedAt(Instant.now())
                .build();

        eventPublisher.publishEvent(event);
    }

}
