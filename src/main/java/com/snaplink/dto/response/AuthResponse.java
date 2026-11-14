package com.snaplink.dto.response;

import lombok.*;

/** DTO token sau đăng ký/đăng nhập; expiresIn dùng millisecond để giữ tương thích API hiện tại. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private long expiresIn;

    private Long userId;

    private String email;
}
