package com.swordverse.server.common.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "swordverse.auth")
public record AuthProperties(
        @NotBlank String issuer,
        @NotBlank String audience,
        @NotBlank String signingKeyBase64,
        @NotNull Duration accessTokenTtl,
        @NotNull Duration refreshTokenTtl,
        boolean refreshCookieSecure,
        @NotBlank String refreshCookieSameSite) {

    public AuthProperties {
        if (accessTokenTtl != null && (accessTokenTtl.isZero() || accessTokenTtl.isNegative())) {
            throw new IllegalArgumentException("Access token TTL must be positive");
        }

        if (refreshTokenTtl != null && (refreshTokenTtl.isZero() || refreshTokenTtl.isNegative())) {
            throw new IllegalArgumentException("Refresh token TTL must be positive");
        }

        if (accessTokenTtl != null
                && refreshTokenTtl != null
                && accessTokenTtl.compareTo(refreshTokenTtl) >= 0) {
            throw new IllegalArgumentException(
                    "Access token TTL must be shorter than refresh token TTL");
        }
    }
}
