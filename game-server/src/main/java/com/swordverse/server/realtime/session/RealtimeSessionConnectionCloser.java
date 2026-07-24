package com.swordverse.server.realtime.session;

import com.swordverse.server.realtime.connection.ConnectionRegistry;
import com.swordverse.server.realtime.connection.RealtimeConnection;
import com.swordverse.server.realtime.transport.WebSocketTransportRegistry;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

/**
 * Closes every live physical WebSocket connection established by one authentication session. The
 * service joins authenticated connection metadata with process-local transport references and
 * relies on normal WebSocket lifecycle callbacks for registry cleanup.
 */
@Service
public class RealtimeSessionConnectionCloser {

    private static final Logger log =
            LoggerFactory.getLogger(RealtimeSessionConnectionCloser.class);

    private static final CloseStatus SESSION_REVOKED_CLOSE_STATUS =
            new CloseStatus(1008, "Authentication session revoked");

    private final ConnectionRegistry connectionRegistry;

    private final WebSocketTransportRegistry transportRegistry;

    /**
     * Creates the session-scoped connection closer.
     *
     * @param connectionRegistry authenticated connection metadata
     * @param transportRegistry physical WebSocket transports
     */
    public RealtimeSessionConnectionCloser(
            ConnectionRegistry connectionRegistry, WebSocketTransportRegistry transportRegistry) {

        this.connectionRegistry = connectionRegistry;

        this.transportRegistry = transportRegistry;
    }

    /**
     * Requests policy-violation closure for every open connection belonging to a session.
     * Individual transport failures are logged and do not prevent remaining connections from
     * closing.
     *
     * @param sessionId revoked authentication session identifier
     * @return number of transports for which close was successfully requested
     */
    public int closeAllBySessionId(UUID sessionId) {

        Set<RealtimeConnection> connections = connectionRegistry.findBySessionId(sessionId);

        int closedCount = 0;

        for (RealtimeConnection connection : connections) {

            boolean closed = closeConnection(connection);

            if (closed) {
                closedCount++;
            }
        }

        log.info(
                "Realtime session connections closed: "
                        + "sessionId={}, requestedCount={}, "
                        + "closedCount={}",
                sessionId,
                connections.size(),
                closedCount);

        return closedCount;
    }

    /** Attempts to close one connection if its physical transport remains present and open. */
    private boolean closeConnection(RealtimeConnection connection) {

        WebSocketSession transport = transportRegistry.find(connection.connectionId()).orElse(null);

        if (transport == null) {
            log.debug(
                    "Realtime transport was already absent: " + "connectionId={}, sessionId={}",
                    connection.connectionId(),
                    connection.sessionId());

            return false;
        }

        if (!transport.isOpen()) {
            return false;
        }

        try {
            transport.close(SESSION_REVOKED_CLOSE_STATUS);

            return true;
        } catch (IOException exception) {
            log.warn(
                    "Could not close realtime connection: " + "connectionId={}, sessionId={}",
                    connection.connectionId(),
                    connection.sessionId(),
                    exception);

            return false;
        }
    }
}
