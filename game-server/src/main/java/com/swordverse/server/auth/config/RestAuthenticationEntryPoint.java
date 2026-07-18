package com.swordverse.server.auth.config;

import com.swordverse.server.auth.application.error.AuthError;
import com.swordverse.server.common.api.ApiErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException, ServletException {

        AuthError error = AuthError.UNAUTHORIZED;

        response.setStatus(error.httpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiErrorResponse body = ApiErrorResponse.of(error, error.defaultMessage(), Map.of());
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
