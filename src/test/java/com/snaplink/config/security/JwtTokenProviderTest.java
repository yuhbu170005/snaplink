package com.snaplink.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "snaplink_super_secret_jwt_key_that_is_at_least_256_bits_long_for_hmac_sha";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secret, expirationMs);
    }

    @Test
    @DisplayName("generateToken & parseClaims: sinh token và đọc đúng userId, email")
    void generateAndParseToken_Success() {
        String token = jwtTokenProvider.generateToken(123L, "user@snaplink.com");

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(123L);
        assertThat(jwtTokenProvider.getEmailFromToken(token)).isEqualTo("user@snaplink.com");
    }

    @Test
    @DisplayName("validateToken: trả về false khi token không hợp lệ")
    void validateToken_InvalidToken_ReturnsFalse() {
        assertThat(jwtTokenProvider.validateToken("invalid.token.string")).isFalse();
    }
}
