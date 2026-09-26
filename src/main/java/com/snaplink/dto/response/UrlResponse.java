package com.snaplink.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UrlResponse {

    Long id;
    String shortCode;
    String shortUrl;
    String originalUrl;
    String customAlias;
    Instant expiresAt;
    Instant createdAt;
    Boolean isActive;
    Long userId;
}
