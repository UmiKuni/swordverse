package com.swordverse.server.common.error;

import java.util.Map;
import java.util.Objects;

public abstract class BaseException extends RuntimeException {

    private final ErrorDefinition errorDefinition;
    private final Map<String, ?> details;

    protected BaseException(ErrorDefinition errorDefinition) {
        this(errorDefinition, errorDefinition.defaultMessage(), null);
    }

    protected BaseException(ErrorDefinition errorDefinition, Map<String, ?> details) {
        this(errorDefinition, errorDefinition.defaultMessage(), details);
    }

    protected BaseException(ErrorDefinition errorDefinition, String message) {
        this(errorDefinition, message, null);
    }

    protected BaseException(
            ErrorDefinition errorDefinition, String message, Map<String, ?> details) {
        super(getErrorMessage(errorDefinition, message));
        this.errorDefinition = errorDefinition;
        this.details = details != null ? Map.copyOf(details) : Map.of();
    }

    private static String getErrorMessage(ErrorDefinition errorDefinition, String message) {
        Objects.requireNonNull(errorDefinition, "Error Code must not be null");
        return message != null ? message : errorDefinition.defaultMessage();
    }

    public ErrorDefinition getErrorDefinition() {
        return errorDefinition;
    }

    public Map<String, ?> getDetails() {
        return details;
    }
}
