package com.swordverse.server.auth.application.model;

import java.time.Instant;

public record IssuedAccessToken(String value, Instant expiresAt) {}
