package com.swordverse.server.realtime.connection;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable identity metadata for one authenticated STOMP connection.
 *
 * @param connectionId Spring WebSocket/STOMP session identifier
 * @param userId authenticated user that owns the connection
 * @param sessionId authentication session used to establish the connection
 * @param connectedAt time at which STOMP authentication completed
 */
public record RealtimeConnection(
        String connectionId, UUID userId, UUID sessionId, Instant connectedAt) {

    /** Validates the identity and lifecycle metadata required by the connection registries. */
    public RealtimeConnection {
        if (connectionId == null || connectionId.isBlank()) {
            throw new IllegalArgumentException(
                    "[RealtimeConnection] Connection ID must not be blank");
        }

        Objects.requireNonNull(userId, "[RealtimeConnection] User ID must not be null");

        Objects.requireNonNull(sessionId, "[RealtimeConnection] Session ID must not be null");

        Objects.requireNonNull(connectedAt, "[RealtimeConnection] Connected time must not be null");
    }
}
