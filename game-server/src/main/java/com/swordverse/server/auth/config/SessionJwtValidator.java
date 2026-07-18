package com.swordverse.server.auth.config;

import com.swordverse.server.auth.persistence.entity.Session;
import com.swordverse.server.auth.persistence.repository.SessionRepository;
import java.time.Clock;
import java.util.UUID;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

final class SessionJwtValidator implements OAuth2TokenValidator<Jwt> {
    private static final OAuth2Error INVALID_TOKEN =
            new OAuth2Error("INVALID_TOKEN", "The access token session claims are invalid.", null);
    private static final OAuth2Error SESSION_REVOKED =
            new OAuth2Error(
                    "SESSION_REVOKED", "The access token session is no longer active.", null);

    private final SessionRepository sessionRepository;
    private final Clock clock;

    SessionJwtValidator(SessionRepository sessionRepository, Clock clock) {
        this.sessionRepository = sessionRepository;
        this.clock = clock;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        UUID userId;
        UUID sessionId;
        try {
            userId = UUID.fromString(jwt.getSubject());
            sessionId = UUID.fromString(jwt.getClaimAsString("sessionId"));
        } catch (RuntimeException exception) {
            return OAuth2TokenValidatorResult.failure(INVALID_TOKEN);
        }

        Session session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null
                || !session.getUserId().equals(userId)
                || !session.isActiveAt(clock.instant())) {
            return OAuth2TokenValidatorResult.failure(SESSION_REVOKED);
        }

        return OAuth2TokenValidatorResult.success();
    }
}
