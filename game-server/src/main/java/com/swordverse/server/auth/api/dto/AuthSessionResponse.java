package com.swordverse.server.auth.api.dto;

import java.util.UUID;

public record AuthSessionResponse(
        String accessToken,
        long accessTokenExpiresAt,
        long refreshTokenExpiresAt,
        UUID sessionId,
        AuthUserResponse user) {}
