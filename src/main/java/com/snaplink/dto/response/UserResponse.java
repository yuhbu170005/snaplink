package com.snaplink.dto.response;

import lombok.*;

import java.time.Instant;

/** Thông tin tài khoản được phép công khai cho chính user; không chứa password hash. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;

    private String email;

    private Instant createdAt;
}
