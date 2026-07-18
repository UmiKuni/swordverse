package com.swordverse.server.auth.application.model;

import java.util.UUID;

public record AuthSessionResult(
        TokenPair tokens, UUID sessionId, UUID userId, String username, String displayName) {}
