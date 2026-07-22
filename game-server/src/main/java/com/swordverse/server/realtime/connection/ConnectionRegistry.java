package com.swordverse.server.realtime.connection;

import java.util.Set;
import java.util.UUID;

/**
 * Indexes authenticated realtime connections by connection, authentication session, and user.
 * Implementations must support multiple connections for both a session and a user.
 */
public interface ConnectionRegistry {

    /**
     * Registers authenticated connection metadata.
     *
     * @param connection connection to register
     */
    void register(RealtimeConnection connection);

    /**
     * Removes a connection from every index. Implementations must treat an unknown identifier as an
     * idempotent no-op.
     *
     * @param connectionId connection to remove
     */
    void unregister(String connectionId);

    /**
     * Returns an immutable snapshot of connections established by an authentication session.
     *
     * @param sessionId authentication session identifier
     * @return current connections for the session
     */
    Set<RealtimeConnection> findBySessionId(UUID sessionId);

    /**
     * Returns an immutable snapshot of connections owned by a user across all sessions.
     *
     * @param userId authenticated user identifier
     * @return current connections for the user
     */
    Set<RealtimeConnection> findByUserId(UUID userId);

    /**
     * Returns the number of authenticated connections currently registered.
     *
     * @return total authenticated connection count
     */
    int connectionCount();
}
