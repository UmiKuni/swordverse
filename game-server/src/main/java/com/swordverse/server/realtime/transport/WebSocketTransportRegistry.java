package com.swordverse.server.realtime.transport;

import java.util.Optional;
import org.springframework.web.socket.WebSocketSession;

/**
 * Indexes live physical WebSocket sessions by the connection identifier shared with Spring STOMP.
 * Transport state is process-local and must never be persisted.
 */
public interface WebSocketTransportRegistry {

    /**
     * Registers a newly established physical WebSocket session.
     *
     * @param session live transport to register
     */
    void register(WebSocketSession session);

    /**
     * Removes a transport after closure. Unknown identifiers are treated as an idempotent no-op.
     *
     * @param connectionId physical WebSocket session identifier
     */
    void unregister(String connectionId);

    /**
     * Finds the physical transport associated with a connection.
     *
     * @param connectionId physical WebSocket session identifier
     * @return the live transport when it is still indexed
     */
    Optional<WebSocketSession> find(String connectionId);

    /**
     * Returns the number of physical WebSocket sessions currently indexed.
     *
     * @return current transport count
     */
    int transportCount();
}
