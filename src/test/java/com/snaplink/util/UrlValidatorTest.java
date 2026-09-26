package com.snaplink.util;

import com.snaplink.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrlValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "https://google.com",
            "http://example.com/path/to/page?query=123",
            "https://sub.domain.org/index.html#hash"
    })
    @DisplayName("validateOriginalUrl: hợp lệ với URL chuẩn http/https")
    void validateOriginalUrl_Valid(String url) {
        assertThatCode(() -> UrlValidator.validateOriginalUrl(url))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ftp://ftp.example.com",
            "javascript:alert(1)",
            "data:text/html,<html>test</html>",
            "file:///etc/passwd",
            "just-text-not-url",
            ""
    })
    @DisplayName("validateOriginalUrl: ném BadRequestException với URL không hợp lệ hoặc scheme nguy hiểm")
    void validateOriginalUrl_Invalid(String url) {
        assertThatThrownBy(() -> UrlValidator.validateOriginalUrl(url))
                .isInstanceOf(BadRequestException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "my-custom-link",
            "promo_2026",
            "abc123"
    })
    @DisplayName("validateCustomAlias: hợp lệ với custom alias chuẩn")
    void validateCustomAlias_Valid(String alias) {
        assertThatCode(() -> UrlValidator.validateCustomAlias(alias))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "api",
            "auth",
            "swagger",
            "admin",
            "login",
            "urls"
    })
    @DisplayName("validateCustomAlias: ném BadRequestException với từ khoá bảo lưu (Reserved Keywords)")
    void validateCustomAlias_ReservedKeywords(String alias) {
        assertThatThrownBy(() -> UrlValidator.validateCustomAlias(alias))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("reserved keyword");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ab", // Quá ngắn (<3)
            "link with space",
            "link@special!",
            "invalid.dot"
    })
    @DisplayName("validateCustomAlias: ném BadRequestException với định dạng alias sai")
    void validateCustomAlias_InvalidFormat(String alias) {
        assertThatThrownBy(() -> UrlValidator.validateCustomAlias(alias))
                .isInstanceOf(BadRequestException.class);
    }
}
