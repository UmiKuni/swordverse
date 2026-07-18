package com.swordverse.server.common.api;

import java.util.Map;

public record ApiErrorResponse(ApiError error) {

    public record ApiError(String code, String message, Map<String, ?> details, String traceId) {}

    public static ApiErrorResponse of(
            String code, String message, Map<String, ?> details, String traceId) {
        return new ApiErrorResponse(new ApiError(code, message, details, traceId));
    }
}
