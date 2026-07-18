# Backend Authentication Implementation

## 1. Purpose

This document maps the authentication architecture to the current Spring Boot implementation. It is an onboarding and maintenance guide; public payloads remain defined by the external contract.

## 2. Package Responsibilities

| Package | Responsibility |
|---|---|
| `auth.api` | HTTP endpoints, request/response mapping, and refresh-cookie delivery |
| `auth.application` | Registration, login, refresh, logout, and token issuance workflows |
| `auth.config` | Spring Security filter chain, JWT codec, validators, and security failure responses |
| `auth.persistence` | User, session, refresh-token entities and repositories |
| `common.api` | Shared error response and controller exception translation |
| `common.config.properties` | Validated authentication configuration |

## 3. Framework-Invoked Components

`SecurityConfig` defines public and protected routes and configures the application as a stateless OAuth2 resource server. Stateless refers to Spring HTTP sessions; it does not remove the SwordVerse authentication-session entity.

`JwtConfig` provides the HMAC key, `JwtEncoder`, and `JwtDecoder`. Spring Security invokes the decoder for Bearer-token requests. The decoder applies issuer and time validation, `AudienceJwtValidator`, and `SessionJwtValidator`.

`RestAuthenticationEntryPoint` implements Spring Security's `AuthenticationEntryPoint`. It writes the standard JSON `401` response when authentication fails before a controller is invoked. Such failures do not pass through ordinary controller exception handling.

`GlobalExceptionHandler` translates exceptions raised after controller dispatch into the standard API error response.

## 4. Application-Invoked Components

`AuthController` maps HTTP input to DTOs, delegates to `AuthService`, writes refresh cookies, and maps internal results to public responses.

`AuthService` owns transaction boundaries and coordinates repositories, password verification, session lifecycle, access-token issuance, and refresh-token rotation.

`AccessTokenService` builds and signs JWT access tokens. `RefreshTokenService` generates cryptographically secure opaque values, hashes them for persistence, locates tokens under lock, and revokes token families when required.

`RefreshTokenCookieService` centralizes cookie name, path, `HttpOnly`, `Secure`, `SameSite`, lifetime, and deletion behavior.

## 5. Principal Flows

### Login

```text
AuthController
  → validate LoginRequestDto
  → AuthService.login
  → UserRepository.findByUsername
  → PasswordEncoder.matches
  → create Session
  → AccessTokenService.issue
  → RefreshTokenService.issue
  → write refresh cookie and return access-token response
```

Each successful login creates a new session. Existing sessions remain valid under the accepted multi-session policy.

### Authenticated request

```text
Bearer token
  → Spring Security filter
  → JwtDecoder verifies signature and standard claims
  → AudienceJwtValidator
  → SessionJwtValidator loads the referenced session
  → authenticated Jwt principal
  → controller
```

### Refresh

Refresh uses pessimistic locks for the session and refresh-token rows. This prevents two concurrent requests from successfully rotating the same token. Reuse of a revoked token persists session revocation even though the request terminates with an exception.

### Logout

Logout reads `sessionId` from the authenticated JWT, revokes that session, revokes its active refresh tokens, clears the cookie, and must notify realtime connection management to close every connection belonging to that session.

## 6. Implementation Requirements for Gameplay Lease

Authentication code must not determine gameplay ownership by counting sessions or sockets. A separate application service must atomically acquire, validate, transfer, and release gameplay leases.

WebSocket command handlers must derive `userId` and `sessionId` from the authenticated principal and `connectionId` from the transport context. Caller-controlled identifiers must not be accepted as proof of ownership.

## 7. Known Lifecycle Requirements

- Expired sessions must eventually transition to `EXPIRED` or be removed by a maintenance process.
- Refresh-token history must be retained long enough to detect relevant reuse and later cleaned according to retention policy.
- Raw credentials and authorization headers must not be logged.
- Multi-tab refresh coordination is a frontend responsibility, while the backend must remain safe under concurrent requests.
