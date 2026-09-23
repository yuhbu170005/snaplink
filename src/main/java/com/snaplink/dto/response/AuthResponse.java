package com.snaplink.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthResponse {

    String accessToken;

    @Builder.Default
    String tokenType = "Bearer";

    long expiresIn;

    Long userId;

    String email;
}
