package com.swordverse.server.auth.api;

import java.time.Duration;
import java.time.Clock;
import java.time.Instant;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.swordverse.server.common.config.properties.AuthProperties;

import jakarta.servlet.http.HttpServletResponse;

@Component
public class RefreshTokenCookieService {
    public static final String COOKIE_NAME = "swordverse_refresh";
    private static final String COOKIE_PATH = "/api/auth";

    private final AuthProperties properties;
    private final Clock clock;

    public RefreshTokenCookieService(AuthProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public void write(
            HttpServletResponse response,
            String rawRefreshToken,
            Instant expiresAt
    ) {
        Duration maxAge = Duration.between(clock.instant(), expiresAt);
        if (maxAge.isNegative() || maxAge.isZero()) {
            throw new IllegalArgumentException("Refresh token cookie must expire in the future");
        }

        ResponseCookie cookie = baseCookie(rawRefreshToken)
                .maxAge(maxAge)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clear(HttpServletResponse response) {
        ResponseCookie cookie = baseCookie("")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(properties.refreshCookieSecure())
                .sameSite(properties.refreshCookieSameSite())
                .path(COOKIE_PATH);
    }
}
