package com.swordverse.server.common.api;

import com.swordverse.server.common.error.ErrorDefinition;
import java.util.Map;
import java.util.Objects;

public record ApiErrorResponse(ApiError error) {

    public record ApiError(String code, String message, Map<String, ?> details) {}

    public static ApiErrorResponse of(
            ErrorDefinition errorDefinition, String message, Map<String, ?> details) {
        Objects.requireNonNull(errorDefinition, "Error definition must not be null.");

        Map<String, ?> safeDetails = details == null ? Map.of() : Map.copyOf(details);

        return new ApiErrorResponse(new ApiError(errorDefinition.code(), message, safeDetails));
    }
}
