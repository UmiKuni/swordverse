package com.swordverse.server.auth.api;

import com.swordverse.server.auth.api.dto.AuthSessionResponse;
import com.swordverse.server.auth.api.dto.AuthUserResponse;
import com.swordverse.server.auth.api.dto.LoginRequestDto;
import com.swordverse.server.auth.api.dto.RegisterRequestDto;
import com.swordverse.server.auth.application.AuthService;
import com.swordverse.server.auth.application.model.AuthSessionResult;
import com.swordverse.server.auth.persistence.entity.User;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenCookieService refreshTokenCookieService;

    public AuthController(
            AuthService authService, RefreshTokenCookieService refreshTokenCookieService) {
        this.authService = authService;
        this.refreshTokenCookieService = refreshTokenCookieService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthSessionResponse> register(
            @Valid @RequestBody RegisterRequestDto request, HttpServletResponse response) {
        AuthSessionResult result = authService.register(request);
        writeRefreshCookie(response, result);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    @PostMapping("/login")
    public AuthSessionResponse login(
            @Valid @RequestBody LoginRequestDto request, HttpServletResponse response) {
        AuthSessionResult result = authService.login(request);
        writeRefreshCookie(response, result);
        return toResponse(result);
    }

    @PostMapping("/refresh")
    public AuthSessionResponse refresh(
            @CookieValue(name = RefreshTokenCookieService.COOKIE_NAME, required = false)
                    String rawRefreshToken,
            HttpServletResponse response) {
        AuthSessionResult result = authService.refresh(rawRefreshToken);
        writeRefreshCookie(response, result);
        return toResponse(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal Jwt jwt, HttpServletResponse response) {
        authService.logout(sessionId(jwt));
        refreshTokenCookieService.clear(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public AuthUserResponse currentUser(@AuthenticationPrincipal Jwt jwt) {
        User user = authService.currentUser(UUID.fromString(jwt.getSubject()));
        return new AuthUserResponse(
                user.getId(), user.getUsername(), user.getEffectiveDisplayName());
    }

    private void writeRefreshCookie(HttpServletResponse response, AuthSessionResult result) {
        refreshTokenCookieService.write(
                response, result.tokens().refreshToken(), result.tokens().refreshTokenExpiresAt());
    }

    private static UUID sessionId(Jwt jwt) {
        return UUID.fromString(jwt.getClaimAsString("sessionId"));
    }

    private static AuthSessionResponse toResponse(AuthSessionResult result) {
        return new AuthSessionResponse(
                result.tokens().accessToken(),
                result.tokens().accessTokenExpiresAt().toEpochMilli(),
                result.tokens().refreshTokenExpiresAt().toEpochMilli(),
                result.sessionId(),
                new AuthUserResponse(result.userId(), result.username(), result.displayName()));
    }
}
