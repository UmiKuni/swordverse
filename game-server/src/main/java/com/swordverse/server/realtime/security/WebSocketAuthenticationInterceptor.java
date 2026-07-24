package com.swordverse.server.realtime.security;

import java.util.UUID;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

/**
 * Authenticates inbound STOMP CONNECT frames with the same {@link JwtDecoder} used by protected
 * REST endpoints. Successful validation attaches a trusted {@link RealtimePrincipal} to the STOMP
 * session; no caller-controlled identity fields are accepted.
 */
@Component
public class WebSocketAuthenticationInterceptor implements ChannelInterceptor {
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtDecoder jwtDecoder;

    /**
     * Creates the interceptor.
     *
     * @param jwtDecoder configured decoder that validates JWT claims and server-side sessions
     */
    public WebSocketAuthenticationInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Authenticates CONNECT frames and leaves all other inbound STOMP frames unchanged.
     *
     * @param message inbound Spring message
     * @param channel client inbound channel
     * @return the original message after any authenticated principal has been attached
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticate(accessor);
        }

        return message;
    }

    /** Validates the bearer token and assigns the resulting user and session principal. */
    private void authenticate(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader(AUTHORIZATION);

        String rawAccessToken = extractBearerToken(authorization);

        Jwt jwt = decode(rawAccessToken);

        UUID userId = readUuid(jwt.getSubject(), "subject");
        UUID sessionId = readUuid(jwt.getClaimAsString("sessionId"), "sessionId");

        accessor.setUser(new RealtimePrincipal(userId, sessionId));
    }

    /** Extracts a non-blank token from the native STOMP Authorization header. */
    private String extractBearerToken(String authorization) {

        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new WebSocketAuthenticationException(
                    "[WebSocketAuthenticationInterceptor.extractBearerToken] Bearer access token is required");
        }

        String rawToken = authorization.substring(BEARER_PREFIX.length()).trim();

        if (rawToken.isBlank()) {
            throw new WebSocketAuthenticationException(
                    "[WebSocketAuthenticationInterceptor.extractBearerToken] Bearer access token is required");
        }

        return rawToken;
    }

    /** Decodes and fully validates an access token without exposing its raw value in errors. */
    private Jwt decode(String rawAccessToken) {
        try {
            return jwtDecoder.decode(rawAccessToken);
        } catch (RuntimeException exception) {
            throw new WebSocketAuthenticationException(
                    "[WebSocketAuthenticationInterceptor.decode] Access token is invalid",
                    exception);
        }
    }

    /** Converts a required JWT identity claim to its UUID representation. */
    private UUID readUuid(String value, String claimName) {
        try {
            return UUID.fromString(value);
        } catch (RuntimeException exception) {
            throw new WebSocketAuthenticationException(
                    "[WebSocketAuthenticationInterceptor.readUuid] Access token "
                            + claimName
                            + " claimName is invalid",
                    exception);
        }
    }
}
