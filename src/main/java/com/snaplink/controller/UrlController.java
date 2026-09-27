package com.snaplink.controller;

import com.snaplink.config.ratelimit.RateLimit;
import com.snaplink.config.security.UserPrincipal;
import com.snaplink.dto.request.CreateUrlRequest;
import com.snaplink.dto.response.UrlResponse;
import com.snaplink.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping
    @RateLimit(guestLimit = 10, authLimit = 30, windowSeconds = 60, keyPrefix = "create_url")
    public ResponseEntity<UrlResponse> createShortUrl(
            @Valid @RequestBody CreateUrlRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        UrlResponse response = urlService.createShortUrl(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<com.snaplink.dto.response.PageResponse<UrlResponse>> getMyUrls(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @org.springframework.data.web.PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            org.springframework.data.domain.Pageable pageable
    ) {
        com.snaplink.dto.response.PageResponse<UrlResponse> response = urlService.getMyUrls(currentUser, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UrlResponse> getUrlById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        UrlResponse response = urlService.getUrlById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUrl(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        urlService.deleteUrl(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UrlResponse> toggleUrlStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        UrlResponse response = urlService.toggleUrlStatus(id, currentUser);
        return ResponseEntity.ok(response);
    }
}
