package com.swordverse.server.auth.persistence.entity;

import com.swordverse.server.auth.domain.SessionStatus;
import com.swordverse.server.common.persistence.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Session extends AuditedEntity {
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status;

    @Column(name = "access_token_expires_at", nullable = false)
    private Instant accessTokenExpiresAt;

    @Column(name = "refresh_token_expires_at", nullable = false)
    private Instant refreshTokenExpiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public Session(UUID userId, Instant accessTokenExpiresAt, Instant refreshTokenExpiresAt) {

        Instant validAccessTokenExpirationAt =
                Objects.requireNonNull(
                        accessTokenExpiresAt, "Access token expiration time cannot be null");
        Instant validRefreshTokenExpirationAt =
                Objects.requireNonNull(
                        refreshTokenExpiresAt, "Refresh token expiration time cannot be null");

        if (validAccessTokenExpirationAt.isAfter(validRefreshTokenExpirationAt)) {
            throw new IllegalArgumentException("Access token cannot expire after refresh token");
        }

        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.status = SessionStatus.ACTIVE;
        this.accessTokenExpiresAt = validAccessTokenExpirationAt;
        this.refreshTokenExpiresAt = validRefreshTokenExpirationAt;
    }

    public void revoke(Instant revokedAt) {
        if (this.status != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Session is already revoked or expired");
        }
        Instant validRevokedAt = Objects.requireNonNull(revokedAt, "Revoked at cannot be null");

        this.status = SessionStatus.REVOKED;
        this.revokedAt = validRevokedAt;
    }

    public void renewAccessToken(Instant newExpiration) {
        if (status != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Only active session can be renewed");
        }

        Instant validExpiration = Objects.requireNonNull(newExpiration);

        if (validExpiration.isAfter(refreshTokenExpiresAt)) {
            throw new IllegalArgumentException("Access token cannot outlive session");
        }

        this.accessTokenExpiresAt = validExpiration;
    }

    public boolean isActiveAt(Instant instant) {
        Objects.requireNonNull(instant, "Comparison time must not be null");
        return status == SessionStatus.ACTIVE
                && revokedAt == null
                && refreshTokenExpiresAt.isAfter(instant);
    }
}
