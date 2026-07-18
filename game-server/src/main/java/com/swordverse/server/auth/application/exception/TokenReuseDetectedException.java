package com.swordverse.server.auth.application.exception;

import org.springframework.http.HttpStatus;

public final class TokenReuseDetectedException extends AuthException {
    public TokenReuseDetectedException() {
        super(
                "TOKEN_REUSE_DETECTED",
                "The refresh token has already been used; the session was revoked.",
                HttpStatus.UNAUTHORIZED);
    }
}
