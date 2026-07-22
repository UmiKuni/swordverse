package com.swordverse.server.realtime.security;

/** Indicates that a STOMP CONNECT frame could not establish an authenticated principal. */
public final class WebSocketAuthenticationException extends RuntimeException {

    /**
     * Creates an authentication failure with a safe diagnostic message.
     *
     * @param message failure description that must not contain credentials
     */
    public WebSocketAuthenticationException(String message) {
        super(message);
    }

    /**
     * Creates an authentication failure that preserves the validator cause for server diagnostics.
     *
     * @param message failure description that must not contain credentials
     * @param cause underlying JWT or claim-validation failure
     */
    public WebSocketAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
