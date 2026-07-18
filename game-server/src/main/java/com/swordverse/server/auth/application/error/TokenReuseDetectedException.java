package com.swordverse.server.auth.application.error;

public final class TokenReuseDetectedException extends AuthException {
    public TokenReuseDetectedException() {
        super(AuthError.TOKEN_REUSE_DETECTED);
    }
}
