package com.snaplink.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlRedirectDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String originalUrl;
    private String shortCode;
    private Instant expiresAt;
    private Boolean isActive;
}
