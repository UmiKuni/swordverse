package com.swordverse.server.auth.application.error;

import com.swordverse.server.common.error.ErrorDefinition;
import org.springframework.http.HttpStatus;

public enum AuthError implements ErrorDefinition {
    INVALID_TOKEN("The token is invalid.", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("The token has expired.", HttpStatus.UNAUTHORIZED),
    SESSION_REVOKED("The session is no longer active.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("Authentication is missing or invalid.", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS("Invalid username or password.", HttpStatus.UNAUTHORIZED),
    USERNAME_ALREADY_EXISTS("The username is already registered.", HttpStatus.CONFLICT),
    TOKEN_REUSE_DETECTED(
            "The refresh token has already been used; the session was revoked.",
            HttpStatus.UNAUTHORIZED);

    private final String defaultMessage;
    private final HttpStatus httpStatus;

    AuthError(String defaultMessage, HttpStatus httpStatus) {
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

    @Override
    public String code() {
        return name();
    }

    @Override
    public String defaultMessage() {
        return defaultMessage;
    }

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }
}
