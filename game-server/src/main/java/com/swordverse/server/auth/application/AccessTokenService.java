package com.swordverse.server.auth.application;

import com.swordverse.server.auth.application.model.IssuedAccessToken;
import com.swordverse.server.common.config.properties.AuthProperties;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class AccessTokenService {
    private final JwtEncoder jwtEncoder;
    private final AuthProperties authProperties;

    public AccessTokenService(JwtEncoder jwtEncoder, AuthProperties authProperties) {
        this.jwtEncoder = jwtEncoder;
        this.authProperties = authProperties;
    }

    public IssuedAccessToken issue(
            UUID userId, UUID sessionId, Instant issuedAt, Instant maximumExpiresAt) {
        Instant requestedExpiresAt = issuedAt.plus(authProperties.accessTokenTtl());
        Instant expiresAt =
                requestedExpiresAt.isAfter(maximumExpiresAt)
                        ? maximumExpiresAt
                        : requestedExpiresAt;

        if (!expiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException("Access token expiration must be after issuance");
        }

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();

        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer(authProperties.issuer())
                        .subject(userId.toString())
                        .audience(List.of(authProperties.audience()))
                        .issuedAt(issuedAt)
                        .notBefore(issuedAt)
                        .expiresAt(expiresAt)
                        .id(UUID.randomUUID().toString())
                        .claim("userId", userId.toString())
                        .claim("sessionId", sessionId.toString())
                        .build();

        String tokenValue =
                jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new IssuedAccessToken(tokenValue, expiresAt);
    }
}
