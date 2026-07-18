package com.swordverse.server.common.api;

import com.swordverse.server.auth.api.RefreshTokenCookieService;
import com.swordverse.server.auth.application.exception.AuthException;
import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final RefreshTokenCookieService refreshTokenCookieService;

    public GlobalExceptionHandler(RefreshTokenCookieService refreshTokenCookieService) {
        this.refreshTokenCookieService = refreshTokenCookieService;
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthException(
            AuthException exception, HttpServletResponse response) {
        if (shouldClearRefreshCookie(exception.getCode())) {
            refreshTokenCookieService.clear(response);
        }

        return error(exception.getStatus(), exception.getCode(), exception.getMessage(), Map.of());
    }

    private static boolean shouldClearRefreshCookie(String code) {
        return code.equals("INVALID_TOKEN")
                || code.equals("TOKEN_EXPIRED")
                || code.equals("SESSION_REVOKED")
                || code.equals("TOKEN_REUSE_DETECTED");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception
                .getBindingResult()
                .getFieldErrors()
                .forEach(
                        fieldError ->
                                fields.putIfAbsent(
                                        fieldError.getField(), fieldError.getDefaultMessage()));

        return error(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "The request contains invalid fields.",
                Map.of("fields", fields));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException exception) {
        return error(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "The request body is missing or malformed.",
                Map.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", exception.getMessage(), Map.of());
    }

    private static ResponseEntity<ApiErrorResponse> error(
            HttpStatus status, String code, String message, Map<String, ?> details) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(code, message, details, UUID.randomUUID().toString()));
    }
}
