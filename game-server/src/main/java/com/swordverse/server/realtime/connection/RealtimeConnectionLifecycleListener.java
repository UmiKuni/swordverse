package com.swordverse.server.realtime.connection;

import com.swordverse.server.realtime.security.RealtimePrincipal;
import java.time.Clock;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Bridges Spring STOMP lifecycle events to the authenticated {@link ConnectionRegistry}. A
 * connection is registered only after STOMP authentication succeeds and is removed whenever the
 * STOMP session disconnects.
 */
@Component
public class RealtimeConnectionLifecycleListener {

    private final ConnectionRegistry connectionRegistry;
    private final Clock clock;

    /**
     * Creates the lifecycle bridge.
     *
     * @param connectionRegistry authenticated connection index
     * @param clock source used to timestamp successful connections
     */
    public RealtimeConnectionLifecycleListener(ConnectionRegistry connectionRegistry, Clock clock) {
        this.connectionRegistry = connectionRegistry;
        this.clock = clock;
    }

    /**
     * Registers a successfully authenticated STOMP session.
     *
     * @param event Spring event emitted after the broker accepts STOMP CONNECT
     */
    @EventListener
    public void onConnected(SessionConnectedEvent event) {

        if (!(event.getUser() instanceof RealtimePrincipal principal)) {
            return;
        }

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String connectionId = accessor.getSessionId();

        if (connectionId == null || connectionId.isBlank()) {
            throw new IllegalStateException("Connected STOMP session has no connection ID");
        }

        connectionRegistry.register(
                new RealtimeConnection(
                        connectionId, principal.userId(), principal.sessionId(), clock.instant()));
    }

    /**
     * Removes a STOMP session from the authenticated registry. Repeated disconnect notifications
     * are safe because registry removal is idempotent.
     *
     * @param event Spring STOMP disconnect event
     */
    @EventListener
    public void onDisconnected(SessionDisconnectEvent event) {

        connectionRegistry.unregister(event.getSessionId());
    }
}
