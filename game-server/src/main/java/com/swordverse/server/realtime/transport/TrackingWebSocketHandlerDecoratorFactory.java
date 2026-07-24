package com.swordverse.server.realtime.transport;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

/** Creates transport-tracking decorators for the WebSocket handlers managed by Spring. */
@Component
public class TrackingWebSocketHandlerDecoratorFactory implements WebSocketHandlerDecoratorFactory {

    private final WebSocketTransportRegistry transportRegistry;

    /**
     * Creates the decorator factory.
     *
     * @param transportRegistry registry shared by every decorated WebSocket handler
     */
    public TrackingWebSocketHandlerDecoratorFactory(WebSocketTransportRegistry transportRegistry) {
        this.transportRegistry = transportRegistry;
    }

    /**
     * Wraps a Spring handler with physical transport bookkeeping.
     *
     * @param handler Spring-managed transport handler
     * @return tracking decorator around the supplied handler
     */
    @Override
    public WebSocketHandler decorate(WebSocketHandler handler) {

        return new TrackingWebSocketHandlerDecorator(handler, transportRegistry);
    }
}
