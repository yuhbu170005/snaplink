package com.snaplink.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateUrlRequest {

    @NotBlank(message = "Original URL is required")
    @Size(max = 2048, message = "URL length cannot exceed 2048 characters")
    String originalUrl;

    @Size(min = 3, max = 64, message = "Custom alias must be between 3 and 64 characters")
    String customAlias;

    Instant expiresAt;
}
