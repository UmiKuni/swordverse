package com.swordverse.server.auth.application.model;

import java.time.Instant;

public record TokenPair(
        String accessToken,
        String refreshToken,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt) {
}