package com.swordverse.server.realtime.connection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Single-server {@link ConnectionRegistry} backed by synchronized in-memory indexes. All index
 * mutations share the instance monitor so the connection, session, and user views remain
 * consistent.
 */
@Component
public class InMemoryConnectionRegistry implements ConnectionRegistry {

    private static final Logger log = LoggerFactory.getLogger(InMemoryConnectionRegistry.class);

    private final Map<String, RealtimeConnection> connectionsById = new HashMap<>();

    private final Map<UUID, Set<String>> connectionIdsBySession = new HashMap<>();

    private final Map<UUID, Set<String>> connectionIdsByUser = new HashMap<>();

    /** {@inheritDoc} */
    @Override
    public synchronized void register(RealtimeConnection connection) {

        Objects.requireNonNull(
                connection, "[InMemoryConnectionRegistry.register] Connection must not be null");

        RealtimeConnection existing = connectionsById.get(connection.connectionId());

        if (existing != null) {
            if (existing.equals(connection)) {
                return;
            }

            throw new IllegalStateException(
                    "[InMemoryConnectionRegistry.register] Connection ID is already registered");
        }

        connectionsById.put(connection.connectionId(), connection);

        connectionIdsBySession
                .computeIfAbsent(connection.sessionId(), ignored -> new HashSet<>())
                .add(connection.connectionId());

        connectionIdsByUser
                .computeIfAbsent(connection.userId(), ignored -> new HashSet<>())
                .add(connection.connectionId());

        log.info(
                "[InMemoryConnectionRegistry.register] Realtime connection registered: "
                        + "connectionId={}, userId={}, "
                        + "sessionId={}, "
                        + "sessionConnectionCount={}, "
                        + "totalConnectionCount={}",
                connection.connectionId(),
                connection.userId(),
                connection.sessionId(),
                connectionIdsBySession.get(connection.sessionId()).size(),
                connectionsById.size());
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void unregister(String connectionId) {

        if (connectionId == null || connectionId.isBlank()) {
            return;
        }

        RealtimeConnection removed = connectionsById.remove(connectionId);

        if (removed == null) {
            return;
        }

        removeFromIndex(connectionIdsBySession, removed.sessionId(), connectionId);

        removeFromIndex(connectionIdsByUser, removed.userId(), connectionId);

        int remainingSessionConnections =
                connectionIdsBySession.getOrDefault(removed.sessionId(), Set.of()).size();

        log.info(
                "[InMemoryConnectionRegistry.unregister] Realtime connection unregistered: "
                        + "connectionId={}, userId={}, "
                        + "sessionId={}, "
                        + "sessionConnectionCount={}, "
                        + "totalConnectionCount={}",
                connectionId,
                removed.userId(),
                removed.sessionId(),
                remainingSessionConnections,
                connectionsById.size());
    }

    /** {@inheritDoc} */
    @Override
    public synchronized Set<RealtimeConnection> findBySessionId(UUID sessionId) {

        return resolveConnections(connectionIdsBySession.get(sessionId));
    }

    /** {@inheritDoc} */
    @Override
    public synchronized Set<RealtimeConnection> findByUserId(UUID userId) {

        return resolveConnections(connectionIdsByUser.get(userId));
    }

    /** {@inheritDoc} */
    @Override
    public synchronized int connectionCount() {
        return connectionsById.size();
    }

    /** Resolves an index snapshot to immutable connection metadata while ignoring stale IDs. */
    private Set<RealtimeConnection> resolveConnections(Set<String> connectionIds) {

        if (connectionIds == null || connectionIds.isEmpty()) {
            return Set.of();
        }

        Set<RealtimeConnection> connections = new HashSet<>();

        for (String connectionId : connectionIds) {
            RealtimeConnection connection = connectionsById.get(connectionId);

            if (connection != null) {
                connections.add(connection);
            }
        }

        return Set.copyOf(connections);
    }

    /** Removes a connection ID and discards the index bucket when it becomes empty. */
    private static <K> void removeFromIndex(Map<K, Set<String>> index, K key, String connectionId) {

        Set<String> connectionIds = index.get(key);

        if (connectionIds == null) {
            return;
        }

        connectionIds.remove(connectionId);

        if (connectionIds.isEmpty()) {
            index.remove(key);
        }
    }
}
