package com.swordverse.server.realtime.transport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketSession;

class InMemoryWebSocketTransportRegistryTest {

    private InMemoryWebSocketTransportRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new InMemoryWebSocketTransportRegistry();
    }

    @Test
    void registersTransportByConnectionId() {
        WebSocketSession session = session("connection-1");

        registry.register(session);

        assertThat(registry.find("connection-1")).containsSame(session);

        assertThat(registry.transportCount()).isEqualTo(1);
    }

    @Test
    void unregisterRemovesTransport() {
        WebSocketSession session = session("connection-1");

        registry.register(session);
        registry.unregister("connection-1");

        assertThat(registry.find("connection-1")).isEmpty();

        assertThat(registry.transportCount()).isZero();
    }

    @Test
    void unregisterIsIdempotent() {
        registry.unregister("missing-connection");
        registry.unregister("missing-connection");

        assertThat(registry.transportCount()).isZero();
    }

    @Test
    void registeringSameTransportTwiceIsIdempotent() {
        WebSocketSession session = session("connection-1");

        registry.register(session);
        registry.register(session);

        assertThat(registry.transportCount()).isEqualTo(1);
    }

    private static WebSocketSession session(String connectionId) {

        WebSocketSession session = mock(WebSocketSession.class);

        when(session.getId()).thenReturn(connectionId);

        return session;
    }
}
