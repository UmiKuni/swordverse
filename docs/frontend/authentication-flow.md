# Frontend Authentication Lifecycle

## 1. Purpose

This document defines browser behavior for authentication, renewal, logout, and coordination across tabs. It does not redefine server token validation or public payloads.

## 2. Credential Storage

- The refresh token is owned by the browser as an `HttpOnly` cookie and is unavailable to JavaScript.
- The access token should remain in application memory.
- Passwords must not be retained after an authentication request completes.
- Authentication state must not be inferred from WebSocket state.

## 3. Application Startup

```text
Application starts
  → no in-memory access token
  → coordinate one refresh attempt for the browser profile
  → refresh succeeds: publish authenticated state to participating tabs
  → refresh fails: publish anonymous state and present login when required
```

A protected page may remain in an indeterminate loading state while the initial refresh completes. It must not briefly render protected content or redirect repeatedly between login and application routes.

## 4. Multi-Tab Refresh Coordination

Tabs in the same browser profile share the rotating refresh cookie. They must not independently refresh at the same time.

A browser-level coordinator should provide:

- A single in-flight refresh request.
- Broadcast of successful authentication state and access-token metadata.
- Broadcast of logout and session-revocation events.
- Recovery when the coordinating tab closes.

`BroadcastChannel` is the preferred initial mechanism. A Web Lock may additionally serialize refresh requests where supported. The implementation must have a bounded timeout and recovery path rather than relying permanently on one tab.

## 5. HTTP Failure Handling

- A request that fails because the access token expired may trigger one coordinated refresh and one retry when the operation is safe to retry.
- Refresh failure with `INVALID_TOKEN`, `TOKEN_EXPIRED`, `SESSION_REVOKED`, or `TOKEN_REUSE_DETECTED` ends only the affected browser session.
- Gameplay ownership errors do not clear authentication state.
- Mutation commands must not be replayed automatically unless their idempotency contract explicitly permits it.

## 6. Login and Logout

Successful login creates a new authentication session even if the same account has sessions on other devices. The frontend must not attempt to discover other sessions through socket presence.

Logout revokes the current session, clears local access-token state, broadcasts logout to tabs sharing that browser session, disconnects their sockets, and navigates protected views to an anonymous state.

## 7. Relationship to Gameplay

Authenticated tabs may read profiles, history, rules, and other permitted resources while another connection controls gameplay. Only the realtime client that owns the gameplay lease may submit gameplay commands.

Receiving `GAMEPLAY_ACTIVE_ON_ANOTHER_CONNECTION` must display an ownership conflict without logging the user out.
