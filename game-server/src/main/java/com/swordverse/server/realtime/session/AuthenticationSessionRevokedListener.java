package com.swordverse.server.realtime.session;

import com.swordverse.server.auth.application.event.AuthenticationSessionRevokedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Realtime projection of authentication-session revocation. Events are handled only after the
 * authentication transaction commits, preventing a rolled-back revocation from closing valid
 * connections.
 */
@Component
public class AuthenticationSessionRevokedListener {

    private static final Logger log =
            LoggerFactory.getLogger(AuthenticationSessionRevokedListener.class);

    private final RealtimeSessionConnectionCloser connectionCloser;

    /**
     * Creates the revocation listener.
     *
     * @param connectionCloser service that closes every transport for the revoked session
     */
    public AuthenticationSessionRevokedListener(RealtimeSessionConnectionCloser connectionCloser) {
        this.connectionCloser = connectionCloser;
    }

    /**
     * Closes realtime connections after the session revocation has committed.
     *
     * @param event committed authentication-session revocation
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSessionRevoked(AuthenticationSessionRevokedEvent event) {

        log.info(
                "Authentication session revocation "
                        + "received by realtime: "
                        + "sessionId={}, reason={}",
                event.sessionId(),
                event.reason());

        connectionCloser.closeAllBySessionId(event.sessionId());
    }
}
