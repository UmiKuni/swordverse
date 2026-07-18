package com.swordverse.server.auth.application;

import com.swordverse.server.auth.persistence.entity.RefreshToken;
import com.swordverse.server.auth.persistence.entity.Session;
import com.swordverse.server.auth.persistence.repository.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {
    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.secureRandom = new SecureRandom();
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String generateRawToken() {
        byte[] randomBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public String hash(String rawToken) {
        Objects.requireNonNull(rawToken, "Raw refresh token must not be null");

        if (rawToken.isBlank()) {
            throw new IllegalArgumentException("Raw refresh token must not be blank");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashedBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public String issue(Session session) {
        String rawToken = generateRawToken();
        String tokenHash = hash(rawToken);

        RefreshToken entity =
                new RefreshToken(session.getId(), tokenHash, session.getRefreshTokenExpiresAt());

        refreshTokenRepository.save(entity);

        return rawToken;
    }

    public Optional<RefreshToken> findForUpdate(String rawToken) {
        return refreshTokenRepository.findByTokenHashForUpdate(hash(rawToken));
    }

    public Optional<RefreshToken> find(String rawToken) {
        return refreshTokenRepository.findByTokenHash(hash(rawToken));
    }

    public void revokeAllActive(UUID sessionId, Instant revokedAt) {
        List<RefreshToken> activeTokens =
                refreshTokenRepository.findAllBySessionIdAndRevokedAtIsNull(sessionId);

        activeTokens.forEach(token -> token.revoke(revokedAt));
    }
}
