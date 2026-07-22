package com.swordverse.server.auth.application.event;

/** Identifies the security or lifecycle condition that revoked an authentication session. */
public enum SessionRevocationReason {
    /** The authenticated client explicitly logged out. */
    LOGOUT,

    /** A previously rotated refresh token was presented again. */
    REFRESH_TOKEN_REUSE,

    /** The authentication session reached the end of its configured lifetime. */
    EXPIRED
}
