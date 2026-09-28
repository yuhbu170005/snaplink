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
import java.time.Instant;

@RestController
@RequiredArgsConstructor
@Tag(name = "Redirect", description = "Endpoint chuyển hướng tốc độ cao (Two-Level Cache L1/L2) và ghi nhận telemetry bất đồng bộ")
public class RedirectController {

    private final UrlService urlService;
    private final ApplicationEventPublisher eventPublisher;

    @GetMapping("/{code:[a-zA-Z0-9_-]+}")
    @Operation(
            summary = "Chuyển hướng đến URL gốc (HTTP 302 Found)",
            description = "Truy xuất nhanh qua L1 In-Memory Caffeine / L2 Redis Cloud. Phát sự kiện click tracking bất đồng bộ không gây nghẽn luồng.",
            responses = {
                    @ApiResponse(responseCode = "302", description = "Chuyển hướng thành công tới Location header"),
                    @ApiResponse(responseCode = "404", description = "Link không tồn tại, bị vô hiệu hoặc đã hết hạn")
            }
    )
    public ResponseEntity<Void> redirect(
            @PathVariable String code,
            HttpServletRequest request
    ) {
        UrlRedirectDto redirectDto = urlService.getRedirectInfo(code);

        // Phát sự kiện ghi nhận click bất đồng bộ (Non-blocking)
        publishClickEvent(redirectDto, request);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectDto.getOriginalUrl()))
                .build();
    }

    private void publishClickEvent(UrlRedirectDto redirectDto, HttpServletRequest request) {
        String ipAddress = extractClientIp(request);
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

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.trim().isEmpty()) {
            return xRealIp.trim();
        }

        String remoteAddr = request.getRemoteAddr();
        return (remoteAddr != null && !remoteAddr.trim().isEmpty()) ? remoteAddr : "unknown";
    }
}
