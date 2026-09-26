package com.snaplink.util;

import com.snaplink.exception.BadRequestException;

import java.net.URI;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class UrlValidator {

    private static final int MAX_URL_LENGTH = 2048;
    private static final Pattern ALIAS_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,64}$");

    private static final Set<String> RESERVED_KEYWORDS = Set.of(
            "api",
            "auth",
            "swagger",
            "swagger-ui",
            "v3",
            "error",
            "actuator",
            "urls",
            "login",
            "register",
            "admin",
            "health",
            "docs",
            "null",
            "undefined",
            "favicon.ico"
    );

    private static final Set<String> DANGEROUS_SCHEMES = Set.of(
            "javascript:",
            "data:",
            "file:",
            "vbscript:",
            "blob:",
            "about:"
    );

    private UrlValidator() {
        // Private constructor
    }

    public static void validateOriginalUrl(String originalUrl) {
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            throw new BadRequestException("Original URL cannot be empty");
        }

        String trimmedUrl = originalUrl.trim();

        if (trimmedUrl.length() > MAX_URL_LENGTH) {
            throw new BadRequestException("URL length exceeds maximum limit of " + MAX_URL_LENGTH + " characters");
        }

        String lowerCaseUrl = trimmedUrl.toLowerCase(Locale.ROOT);
        for (String dangerousScheme : DANGEROUS_SCHEMES) {
            if (lowerCaseUrl.startsWith(dangerousScheme)) {
                throw new BadRequestException("Dangerous URL scheme is not allowed: " + dangerousScheme);
            }
        }

        try {
            URI uri = URI.create(trimmedUrl);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new BadRequestException("URL must start with http:// or https://");
            }

            if (uri.getHost() == null || uri.getHost().trim().isEmpty()) {
                throw new BadRequestException("Invalid URL host");
            }
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid URL format: " + e.getMessage());
        }
    }

    public static void validateCustomAlias(String customAlias) {
        if (customAlias == null || customAlias.trim().isEmpty()) {
            return;
        }

        String trimmedAlias = customAlias.trim();

        if (!ALIAS_PATTERN.matcher(trimmedAlias).matches()) {
            throw new BadRequestException("Custom alias must be 3-64 characters long and contain only letters, digits, underscores, or hyphens");
        }

        if (RESERVED_KEYWORDS.contains(trimmedAlias.toLowerCase(Locale.ROOT))) {
            throw new BadRequestException("Custom alias '" + trimmedAlias + "' is a reserved keyword and cannot be used");
        }
    }
}
