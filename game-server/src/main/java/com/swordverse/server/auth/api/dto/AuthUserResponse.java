package com.swordverse.server.auth.api.dto;

import java.util.UUID;

public record AuthUserResponse(
        UUID userId,
        String username,
        String displayName
) {
}
