package com.swordverse.server.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must contain between 3 and 50 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 128, message = "Password must contain between 8 and 128 characters")
        String password,

        @Size(max = 100, message = "Display name must not exceed 100 characters")
        String displayName) {

}
