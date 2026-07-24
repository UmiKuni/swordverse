package com.swordverse.server.realtime.transport;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * Thread-safe, single-server {@link WebSocketTransportRegistry} backed by a {@link
 * ConcurrentHashMap}. It retains live transport references only for the lifetime of the server
 * process.
 */
@Component
public class InMemoryWebSocketTransportRegistry implements WebSocketTransportRegistry {

    private static final Logger log =
            LoggerFactory.getLogger(InMemoryWebSocketTransportRegistry.class);

    private final ConcurrentHashMap<String, WebSocketSession> transports =
            new ConcurrentHashMap<>();

    /** {@inheritDoc} */
    @Override
    public void register(WebSocketSession session) {

        String connectionId = session.getId();

        WebSocketSession existing = transports.putIfAbsent(connectionId, session);

        if (existing == null) {
            log.info(
                    "WebSocket transport registered: " + "connectionId={}, " + "transportCount={}",
                    connectionId,
                    transports.size());

            return;
        }

        if (existing != session) {
            throw new IllegalStateException(
                    "Connection ID is already attached " + "to another WebSocket transport");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregister(String connectionId) {

        if (connectionId == null || connectionId.isBlank()) {
            return;
        }

        WebSocketSession removed = transports.remove(connectionId);

        if (removed != null) {
            log.info(
                    "WebSocket transport unregistered: "
                            + "connectionId={}, "
                            + "transportCount={}",
                    connectionId,
                    transports.size());
        }
    }

    /** {@inheritDoc} */
    @Override
    public Optional<WebSocketSession> find(String connectionId) {

        if (connectionId == null || connectionId.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(transports.get(connectionId));
    }

    /** {@inheritDoc} */
    @Override
    public int transportCount() {
        return transports.size();
    }
}
