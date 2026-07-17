package com.swordverse.server.auth.application;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.swordverse.server.common.config.properties.AuthProperties;

@Service
public class TokenService {

    private final AuthProperties authProperties;

    public TokenService(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public Instant calculateAccessTokenExpiration(
            Instant issuedAt
    ) {
        return issuedAt.plus(
            authProperties.accessTokenTtl()
        );
    }

    public Instant calculateRefreshTokenExpiration(
            Instant issuedAt
    ) {
        return issuedAt.plus(
            authProperties.refreshTokenTtl()
        );
    }
}