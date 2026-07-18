package com.swordverse.server.auth.application.error;

import com.swordverse.server.common.error.BaseException;
import java.util.Map;

public class AuthException extends BaseException {
    public AuthException(AuthError authError) {
        super(authError);
    }

    public AuthException(AuthError authError, String message) {
        super(authError, message);
    }

    public AuthException(AuthError authError, Map<String, ?> details) {
        super(authError, details);
    }

    public AuthException(AuthError authError, String message, Map<String, ?> details) {
        super(authError, message, details);
    }

    @Override
    public AuthError getErrorDefinition() {
        return (AuthError) super.getErrorDefinition();
    }
}
