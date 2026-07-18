package com.swordverse.server.common.error;

import org.springframework.http.HttpStatus;

public interface ErrorDefinition {
    String code();

    String defaultMessage();

    HttpStatus httpStatus();
}
