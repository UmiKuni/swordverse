package com.swordverse.server.auth.persistence.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.swordverse.server.common.persistence.CreatedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshTokenEntity extends CreatedEntity {

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "token_hash", unique = true, nullable = false)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public RefreshTokenEntity(UUID sessionId, String tokenHash, Instant expiresAt) {
        this.sessionId = Objects.requireNonNull(sessionId, "Session ID must not be null");
        this.tokenHash = checkTokenHash(tokenHash);
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expiration time must not be null");
    }

    public void revoke(Instant revokedAt) {
        this.revokedAt = Objects.requireNonNull(revokedAt, "Revoked at must not be null");
    }

    private String checkTokenHash(String tokenHash) {
        Objects.requireNonNull(tokenHash, "Token hash must not be null");

        if (tokenHash.isBlank()) {
            throw new IllegalArgumentException("Token hash must not be blank");
        }

        return tokenHash;
    }
}
