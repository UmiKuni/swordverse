package com.swordverse.server.auth.application.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Describes a committed authentication-session revocation that other application modules may react
 * to. The event deliberately carries no credentials or transport-specific state.
 *
 * @param sessionId identifier of the revoked authentication session
 * @param reason reason the session was revoked
 * @param occurredAt time at which the revocation was initiated
 */
public record AuthenticationSessionRevokedEvent(
        UUID sessionId, SessionRevocationReason reason, Instant occurredAt) {

    /** Validates the required event attributes. */
    public AuthenticationSessionRevokedEvent {
        Objects.requireNonNull(
                sessionId, "[AuthenticationSessionRevokedEvent] Session ID must not be null");

        Objects.requireNonNull(
                reason, "[AuthenticationSessionRevokedEvent] Revocation reason must not be null");

        Objects.requireNonNull(
                occurredAt, "[AuthenticationSessionRevokedEvent] Event time must not be null");
    }
}
