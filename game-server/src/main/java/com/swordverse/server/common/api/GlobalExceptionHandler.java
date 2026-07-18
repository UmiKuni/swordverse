package com.swordverse.server.common.api;

import com.swordverse.server.auth.api.RefreshTokenCookieService;
import com.swordverse.server.auth.application.error.AuthError;
import com.swordverse.server.auth.application.error.AuthException;
import com.swordverse.server.common.error.BaseException;
import com.swordverse.server.common.error.CommonError;
import com.swordverse.server.common.error.ErrorDefinition;
import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;
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
        if (shouldClearRefreshCookie(exception.getErrorDefinition())) {
            refreshTokenCookieService.clear(response);
        }

        return toErrorResponse(exception);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiErrorResponse> hanldeBaseException(BaseException exception) {
        return toErrorResponse(exception);
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
                CommonError.VALIDATION_ERROR,
                "The request contains invalid fields.",
                Map.of("fields", fields));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException exception) {
        return error(
                CommonError.VALIDATION_ERROR,
                "The request body is missing or malformed.",
                Map.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception) {
        return error(
                CommonError.VALIDATION_ERROR,
                getMessage(exception, CommonError.VALIDATION_ERROR),
                Map.of());
    }

    private static boolean shouldClearRefreshCookie(AuthError error) {
        return switch (error) {
            case INVALID_TOKEN, TOKEN_EXPIRED, SESSION_REVOKED, TOKEN_REUSE_DETECTED -> true;
            default -> false;
        };
    }

    private ResponseEntity<ApiErrorResponse> toErrorResponse(BaseException exception) {
        ErrorDefinition errorDefinition = exception.getErrorDefinition();
        String message = exception.getMessage();
        Map<String, ?> details = exception.getDetails();

        return error(errorDefinition, message, details);
    }

    private static ResponseEntity<ApiErrorResponse> error(
            ErrorDefinition errorDefinition, String message, Map<String, ?> details) {
        return ResponseEntity.status(errorDefinition.httpStatus())
                .body(ApiErrorResponse.of(errorDefinition, message, details));
    }

    private static String getMessage(Exception exception, ErrorDefinition errorDefinition) {
        return exception.getMessage() != null
                ? exception.getMessage()
                : errorDefinition.defaultMessage();
    }
}
