package com.swordverse.server.realtime.security;

import java.security.Principal;
import java.util.Objects;
import java.util.UUID;

/**
 * Trusted WebSocket principal derived exclusively from a validated SwordVerse access token.
 *
 * @param userId authenticated user identifier from the JWT subject
 * @param sessionId active server-side authentication session from the JWT claim
 */
public record RealtimePrincipal(UUID userId, UUID sessionId) implements Principal {
    /** Validates the required authenticated identifiers. */
    public RealtimePrincipal {
        Objects.requireNonNull(userId, "[RealtimePrincipal] User Id must not be null");
        Objects.requireNonNull(sessionId, "[RealtimePrincipal] Session Id must not be null");
    }

    /**
     * Returns the stable Spring principal name used by user-destination routing.
     *
     * @return authenticated user identifier as text
     */
    @Override
    public String getName() {
        return userId.toString();
    }
}
