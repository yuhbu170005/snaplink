package com.snaplink.dto.response;

import lombok.*;

import java.time.Instant;

/** DTO trả link. isActive là boolean; expiresAt độc lập với trạng thái bật/tắt. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlResponse {

    private Long id;
    private String shortCode;
    private String shortUrl;
    private String originalUrl;
    private String customAlias;
    private Instant expiresAt;
    private Instant createdAt;
    private Boolean isActive;
    private Long userId;
}
