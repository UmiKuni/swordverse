package com.swordverse.server.auth.application;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.swordverse.server.auth.api.dto.LoginRequestDto;
import com.swordverse.server.auth.api.dto.RegisterRequestDto;
import com.swordverse.server.auth.application.exception.AuthException;
import com.swordverse.server.auth.application.exception.TokenReuseDetectedException;
import com.swordverse.server.auth.application.model.AuthSessionResult;
import com.swordverse.server.auth.application.model.IssuedAccessToken;
import com.swordverse.server.auth.application.model.TokenPair;
import com.swordverse.server.auth.persistence.entity.RefreshToken;
import com.swordverse.server.auth.persistence.entity.Session;
import com.swordverse.server.auth.persistence.entity.SessionStatus;
import com.swordverse.server.auth.persistence.entity.User;
import com.swordverse.server.auth.persistence.repository.SessionRepository;
import com.swordverse.server.auth.persistence.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final Clock clock;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            SessionRepository sessionRepository,
            Clock clock,
            PasswordEncoder passwordEncoder,
            TokenService tokenService,
            AccessTokenService accessTokenService,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.clock = clock;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.accessTokenService = accessTokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthSessionResult register(RegisterRequestDto request) {
        if (userRepository.existsByUsername(request.username())) {
            throw usernameAlreadyExists();
        }

        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.displayName()
        );

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw usernameAlreadyExists();
        }

        return createAuthSession(user, clock.instant());
    }

    @Transactional
    public AuthSessionResult login(LoginRequestDto request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(this::invalidCredentials);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        return createAuthSession(user, clock.instant());
    }

    @Transactional(dontRollbackOn = TokenReuseDetectedException.class)
    public AuthSessionResult refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new AuthException(
                    "INVALID_TOKEN",
                    "The refresh token is missing.",
                    HttpStatus.UNAUTHORIZED
            );
        }

        Instant now = clock.instant();
        RefreshToken tokenSnapshot = refreshTokenService.find(rawRefreshToken)
                .orElseThrow(() -> new AuthException(
                        "INVALID_TOKEN",
                        "The refresh token is invalid.",
                        HttpStatus.UNAUTHORIZED
                ));

        Session session = sessionRepository.findByIdForUpdate(tokenSnapshot.getSessionId())
                .orElseThrow(() -> new AuthException(
                        "INVALID_TOKEN",
                        "The refresh token is not attached to a valid session.",
                        HttpStatus.UNAUTHORIZED
                ));

        RefreshToken oldToken = refreshTokenService.findForUpdate(rawRefreshToken)
                .orElseThrow(() -> new AuthException(
                        "INVALID_TOKEN",
                        "The refresh token is invalid.",
                        HttpStatus.UNAUTHORIZED
                ));

        if (oldToken.isRevoked()) {
            revokeCompromisedSession(session, now);
            throw new TokenReuseDetectedException();
        }

        if (oldToken.isExpiredAt(now)) {
            throw new AuthException(
                    "TOKEN_EXPIRED",
                    "The refresh token has expired.",
                    HttpStatus.UNAUTHORIZED
            );
        }

        ensureActiveSession(session, now);

        oldToken.revoke(now);

        IssuedAccessToken accessToken = accessTokenService.issue(
                session.getUserId(),
                session.getId(),
                now,
                session.getRefreshTokenExpiresAt()
        );
        session.renewAccessToken(accessToken.expiresAt());

        String newRefreshToken = refreshTokenService.issue(session);
        User user = findUser(session.getUserId());

        return toResult(user, session, accessToken, newRefreshToken);
    }

    @Transactional
    public void logout(UUID sessionId) {
        Instant now = clock.instant();
        Session session = sessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> new AuthException(
                        "SESSION_REVOKED",
                        "The session is no longer active.",
                        HttpStatus.UNAUTHORIZED
                ));

        if (session.getStatus() == SessionStatus.ACTIVE) {
            session.revoke(now);
        }
        refreshTokenService.revokeAllActive(sessionId, now);
    }

    @Transactional
    public User currentUser(UUID userId) {
        return findUser(userId);
    }

    private AuthSessionResult createAuthSession(User user, Instant issuedAt) {
        Instant accessExpiresAt = tokenService.calculateAccessTokenExpiration(issuedAt);
        Instant refreshExpiresAt = tokenService.calculateRefreshTokenExpiration(issuedAt);

        Session session = new Session(
                user.getId(),
                accessExpiresAt,
                refreshExpiresAt
        );
        sessionRepository.saveAndFlush(session);

        IssuedAccessToken accessToken = accessTokenService.issue(
                user.getId(),
                session.getId(),
                issuedAt,
                refreshExpiresAt
        );
        String refreshToken = refreshTokenService.issue(session);

        return toResult(user, session, accessToken, refreshToken);
    }

    private AuthSessionResult toResult(
            User user,
            Session session,
            IssuedAccessToken accessToken,
            String rawRefreshToken
    ) {
        TokenPair tokens = new TokenPair(
                accessToken.value(),
                rawRefreshToken,
                accessToken.expiresAt(),
                session.getRefreshTokenExpiresAt()
        );

        return new AuthSessionResult(
                tokens,
                session.getId(),
                user.getId(),
                user.getUsername(),
                user.getEffectiveDisplayName()
        );
    }

    private void ensureActiveSession(Session session, Instant now) {
        if (!session.isActiveAt(now)) {
            throw new AuthException(
                    "SESSION_REVOKED",
                    "The session is no longer active.",
                    HttpStatus.UNAUTHORIZED
            );
        }
    }

    private void revokeCompromisedSession(Session session, Instant now) {
        if (session.getStatus() == SessionStatus.ACTIVE) {
            session.revoke(now);
        }
        refreshTokenService.revokeAllActive(session.getId(), now);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(
                        "UNAUTHORIZED",
                        "The authenticated user no longer exists.",
                        HttpStatus.UNAUTHORIZED
                ));
    }

    private AuthException invalidCredentials() {
        return new AuthException(
                "INVALID_CREDENTIALS",
                "Invalid username or password.",
                HttpStatus.UNAUTHORIZED
        );
    }

    private static AuthException usernameAlreadyExists() {
        return new AuthException(
                "USERNAME_ALREADY_EXISTS",
                "The username is already registered.",
                HttpStatus.CONFLICT
        );
    }
}
