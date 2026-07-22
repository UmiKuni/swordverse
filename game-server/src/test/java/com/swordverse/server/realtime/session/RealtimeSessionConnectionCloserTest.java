package com.swordverse.server.realtime.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.swordverse.server.realtime.connection.ConnectionRegistry;
import com.swordverse.server.realtime.connection.RealtimeConnection;
import com.swordverse.server.realtime.transport.WebSocketTransportRegistry;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

class RealtimeSessionConnectionCloserTest {

    @Test
    void closesEveryOpenConnectionForSession() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        RealtimeConnection first = connection("connection-1", userId, sessionId);

        RealtimeConnection second = connection("connection-2", userId, sessionId);

        ConnectionRegistry connectionRegistry = mock(ConnectionRegistry.class);

        WebSocketTransportRegistry transportRegistry = mock(WebSocketTransportRegistry.class);

        WebSocketSession firstTransport = openTransport();

        WebSocketSession secondTransport = openTransport();

        when(connectionRegistry.findBySessionId(sessionId)).thenReturn(Set.of(first, second));

        when(transportRegistry.find("connection-1")).thenReturn(Optional.of(firstTransport));

        when(transportRegistry.find("connection-2")).thenReturn(Optional.of(secondTransport));

        RealtimeSessionConnectionCloser closer =
                new RealtimeSessionConnectionCloser(connectionRegistry, transportRegistry);

        int closed = closer.closeAllBySessionId(sessionId);

        assertThat(closed).isEqualTo(2);

        verify(firstTransport).close(any(CloseStatus.class));

        verify(secondTransport).close(any(CloseStatus.class));
    }

    @Test
    void ignoresConnectionWhoseTransportIsAbsent() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        RealtimeConnection connection = connection("connection-1", userId, sessionId);

        ConnectionRegistry connectionRegistry = mock(ConnectionRegistry.class);

        WebSocketTransportRegistry transportRegistry = mock(WebSocketTransportRegistry.class);

        when(connectionRegistry.findBySessionId(sessionId)).thenReturn(Set.of(connection));

        when(transportRegistry.find("connection-1")).thenReturn(Optional.empty());

        RealtimeSessionConnectionCloser closer =
                new RealtimeSessionConnectionCloser(connectionRegistry, transportRegistry);

        int closed = closer.closeAllBySessionId(sessionId);

        assertThat(closed).isZero();
    }

    private static WebSocketSession openTransport() {
        WebSocketSession transport = mock(WebSocketSession.class);

        when(transport.isOpen()).thenReturn(true);

        return transport;
    }

    private static RealtimeConnection connection(String connectionId, UUID userId, UUID sessionId) {

        return new RealtimeConnection(
                connectionId, userId, sessionId, Instant.parse("2026-07-22T00:00:00Z"));
    }
}
