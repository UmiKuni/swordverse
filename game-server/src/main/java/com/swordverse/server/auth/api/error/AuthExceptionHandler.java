package com.swordverse.server.auth.api.error;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.swordverse.server.auth.api.RefreshTokenCookieManager;
import com.swordverse.server.auth.application.error.AuthError;
import com.swordverse.server.auth.application.error.AuthException;
import com.swordverse.server.common.api.ApiErrorResponse;
import com.swordverse.server.common.api.ApiErrorResponseFactory;

import jakarta.servlet.http.HttpServletResponse;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class AuthExceptionHandler {
    private final RefreshTokenCookieManager refreshTokenCookieManager;
    private final ApiErrorResponseFactory responseFactory;

    public AuthExceptionHandler(
            RefreshTokenCookieManager refreshTokenCookieManager,
            ApiErrorResponseFactory responseFactory) {
        this.refreshTokenCookieManager = refreshTokenCookieManager;
        this.responseFactory = responseFactory;
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthException(
            AuthException exception, HttpServletResponse response) {
        if (shouldClearRefreshCookie(exception.getErrorDefinition())) {
            refreshTokenCookieManager.clear(response);
        }

        return responseFactory.create(exception);
    }

    private static boolean shouldClearRefreshCookie(AuthError error) {
        return switch (error) {
            case INVALID_TOKEN, TOKEN_EXPIRED, SESSION_REVOKED, TOKEN_REUSE_DETECTED -> true;
            default -> false;
        };
    }

}
