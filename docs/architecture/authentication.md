# Authentication Architecture

## 1. Policy

SwordVerse permits a user to maintain multiple active authentication sessions. A session represents one authenticated login and does not imply that the user is online, connected through WebSocket, or permitted to control gameplay.

Concurrent gameplay is restricted independently through the gameplay lease described in [Realtime Connections and Gameplay Lease](realtime-connections.md).

## 2. Token Model

SwordVerse uses:

- A short-lived signed JWT access token for authenticated HTTP requests and WebSocket handshakes.
- A rotating opaque refresh token for session renewal.
- A server-side authentication session that permits explicit revocation before JWT expiration.

The access token contains the user identity, session identity, issuer, audience, issuance time, validity window, expiration, and token identifier. The server validates both cryptographic claims and the referenced server-side session.

The refresh token is delivered only through a host-only `HttpOnly` cookie. The raw value is not returned in JSON and is not stored in the database. The server stores a SHA-256 hash used for lookup and rotation.

## 3. Claim Semantics

| Claim | Meaning |
|---|---|
| `iss` | Authority that issued the token |
| `sub` | User represented by the token |
| `aud` | Service for which the token was issued |
| `iat` | Issuance time |
| `nbf` | Earliest valid time |
| `exp` | Expiration time |
| `jti` | Identifier of the individual JWT |
| `sessionId` | Server-side authentication session |

An identifier in a claim is serialized as a JSON string but is represented as `UUID` within Java code where its format is known.

## 4. Session Lifecycle

### 4.1 Registration and login

Successful registration or login creates a new authentication session. Existing sessions belonging to the same user remain valid. This behavior is intentional and supports access from multiple browsers or devices.

### 4.2 Refresh

A successful refresh:

1. Hashes and locates the submitted refresh token.
2. Locks the related session and token records.
3. Confirms that neither token nor session has expired or been revoked.
4. Revokes the submitted refresh token.
5. Issues a new access token and refresh token for the same session.
6. Replaces the refresh cookie.

Reuse of a revoked refresh token is treated as possible credential theft and revokes the associated session.

### 4.3 Logout

Logout revokes the current authentication session, revokes its active refresh tokens, clears the refresh cookie, and closes all realtime connections established by that session. Other sessions belonging to the same user remain active.

An optional future `logout-all` operation may revoke every session belonging to the user. It must be a distinct, explicit operation.

## 5. Browser and Multi-Tab Considerations

Tabs in the same browser profile share the refresh cookie. Concurrent refresh attempts can therefore submit the same rotating token and incorrectly resemble token reuse. The frontend must coordinate refresh operations so that only one refresh request is in flight per browser profile.

The preferred implementation uses a browser coordination mechanism such as `BroadcastChannel` to distribute successful token refresh and logout events. Access tokens should remain in memory unless a separate persistence policy is explicitly accepted.

## 6. Security Boundaries

- Authentication is derived only from a validated token and active server-side session.
- A valid authentication session does not grant ownership of matchmaking, a room, or a match.
- WebSocket disconnect does not revoke a session.
- Session revocation invalidates future HTTP requests and WebSocket handshakes and closes existing connections for that session.
- Passwords and raw refresh tokens must never be logged or persisted.

## 7. Related Documents

- [Backend authentication implementation](../backend/auth-implementation.md)
- [Frontend authentication lifecycle](../frontend/authentication-flow.md)
- [API and WebSocket contract](../contracts/api-websocket-contract.md)
