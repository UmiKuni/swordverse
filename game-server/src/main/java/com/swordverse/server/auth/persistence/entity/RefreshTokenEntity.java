package com.swordverse.server.auth.persistence.entity;

import java.time.Instant;
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
        this.sessionId = sessionId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public void revoke(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }
}
