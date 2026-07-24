package com.swordverse.server.realtime.connection;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryConnectionRegistryTest {

    private InMemoryConnectionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new InMemoryConnectionRegistry();
    }

    @Test
    void registersConnectionBySessionAndUser() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        RealtimeConnection connection = connection("connection-1", userId, sessionId);

        registry.register(connection);

        assertThat(registry.findBySessionId(sessionId)).containsExactly(connection);

        assertThat(registry.findByUserId(userId)).containsExactly(connection);

        assertThat(registry.connectionCount()).isEqualTo(1);
    }

    @Test
    void supportsMultipleConnectionsForOneSession() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        RealtimeConnection first = connection("connection-1", userId, sessionId);

        RealtimeConnection second = connection("connection-2", userId, sessionId);

        registry.register(first);
        registry.register(second);

        assertThat(registry.findBySessionId(sessionId)).containsExactlyInAnyOrder(first, second);

        assertThat(registry.connectionCount()).isEqualTo(2);
    }

    @Test
    void unregisterRemovesConnectionFromAllIndexes() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        RealtimeConnection connection = connection("connection-1", userId, sessionId);

        registry.register(connection);
        registry.unregister(connection.connectionId());

        assertThat(registry.findBySessionId(sessionId)).isEmpty();

        assertThat(registry.findByUserId(userId)).isEmpty();

        assertThat(registry.connectionCount()).isZero();
    }

    @Test
    void unregisterIsIdempotent() {
        registry.unregister("missing-connection");
        registry.unregister("missing-connection");

        assertThat(registry.connectionCount()).isZero();
    }

    private static RealtimeConnection connection(String connectionId, UUID userId, UUID sessionId) {

        return new RealtimeConnection(
                connectionId, userId, sessionId, Instant.parse("2026-07-22T00:00:00Z"));
    }
}
