package com.swordverse.server.realtime.transport;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

/**
 * Decorates Spring's WebSocket handler to register physical transports at establishment and remove
 * them after closure. Registration is rolled back if delegate initialization fails.
 */
public class TrackingWebSocketHandlerDecorator extends WebSocketHandlerDecorator {

    private final WebSocketTransportRegistry transportRegistry;

    /**
     * Creates a tracking wrapper around Spring's transport handler.
     *
     * @param delegate Spring WebSocket handler to invoke after registry bookkeeping
     * @param transportRegistry registry of live physical transports
     */
    public TrackingWebSocketHandlerDecorator(
            WebSocketHandler delegate, WebSocketTransportRegistry transportRegistry) {

        super(delegate);
        this.transportRegistry = transportRegistry;
    }

    /** Registers a transport before delegating the connection-established callback. */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        transportRegistry.register(session);

        try {
            super.afterConnectionEstablished(session);
        } catch (Exception exception) {
            transportRegistry.unregister(session.getId());

            throw exception;
        }
    }

    /** Removes a transport and always delegates the physical close callback to Spring. */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus)
            throws Exception {

        try {
            transportRegistry.unregister(session.getId());
        } finally {
            super.afterConnectionClosed(session, closeStatus);
        }
    }
}
