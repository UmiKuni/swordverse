package com.swordverse.server.common.api;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.swordverse.server.common.error.BaseException;
import com.swordverse.server.common.error.ErrorDefinition;

@Component
public class ApiErrorResponseFactory {

    public ResponseEntity<ApiErrorResponse> create(BaseException exception) {
        return create(
                exception.getErrorDefinition(),
                exception.getMessage(),
                exception.getDetails());
    }

    public ResponseEntity<ApiErrorResponse> create(
            ErrorDefinition errorDefinition, String message, Map<String, ?> details) {
        String resolvedMessage =
                message != null ? message : errorDefinition.defaultMessage();

        return ResponseEntity.status(errorDefinition.httpStatus())
                .body(ApiErrorResponse.of(errorDefinition, resolvedMessage, details));
    }
}
