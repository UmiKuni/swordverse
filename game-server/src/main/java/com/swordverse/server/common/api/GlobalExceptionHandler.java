package com.swordverse.server.common.api;

import com.swordverse.server.common.error.BaseException;
import com.swordverse.server.common.error.CommonError;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final ApiErrorResponseFactory responseFactory;

    public GlobalExceptionHandler(ApiErrorResponseFactory responseFactory) {
        this.responseFactory = responseFactory;
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiErrorResponse> handleBaseException(BaseException exception) {
        return responseFactory.create(exception);
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

        return responseFactory.create(
                CommonError.VALIDATION_ERROR,
                "The request contains invalid fields.",
                Map.of("fields", fields));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException exception) {
        return responseFactory.create(
                CommonError.VALIDATION_ERROR,
                "The request body is missing or malformed.",
                Map.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception) {
        return responseFactory.create(
                CommonError.VALIDATION_ERROR, exception.getMessage(), Map.of());
    }
}
