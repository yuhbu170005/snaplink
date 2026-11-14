package com.snaplink.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

/** DTO nhận JSON tạo link. @Valid kiểm tra annotation trước khi controller gọi service. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUrlRequest {

    @NotBlank(message = "Original URL is required")
    @Size(max = 2048, message = "URL length cannot exceed 2048 characters")
    private String originalUrl;

    @Size(min = 3, max = 64, message = "Custom alias must be between 3 and 64 characters")
    private String customAlias;

    private Instant expiresAt;
}
