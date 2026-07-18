# ADR 0001: Multiple Authentication Sessions with a Single Gameplay Lease

## Status

Accepted.

## Context

SwordVerse is delivered through a browser. A user may legitimately open several tabs or devices to read rules, inspect a profile, review match history, or use future social functionality. Restricting the account to one WebSocket connection would cause normal browser activity to terminate unrelated tabs or interrupt gameplay.

The game must nevertheless prevent the same account from entering multiple queues, rooms, or matches, or from controlling one match through several concurrent connections.

Authentication sessions, WebSocket connections, and gameplay control have different lifecycles. Treating any one of them as equivalent to the others would produce ambiguous logout, reconnect, and takeover behavior.

## Decision

- A user may own multiple active authentication sessions.
- A user may own multiple general realtime connections across sessions, tabs, and devices.
- A user may own at most one active gameplay lease.
- The gameplay lease is bound to a user, session, connection, and gameplay activity.
- Every gameplay command must validate lease ownership in addition to ordinary authentication and resource authorization.
- Socket disconnection does not revoke an authentication session.
- A disconnected gameplay connection retains its lease during the reconnect grace period.
- Non-gameplay connections remain authenticated when another connection owns the gameplay lease.

## Consequences

Users may access read-only and social features without interrupting an active game. Frontend code must distinguish authentication errors from gameplay ownership conflicts. Backend code must perform atomic lease acquisition and validate lease ownership for every gameplay command.

The initial single-instance implementation may store connection and lease state in memory. A multi-instance deployment will require shared atomic lease storage and cross-instance connection coordination.

## Rejected Alternatives

### One authentication session and one socket per user

This model resembles a dedicated native game client but conflicts with normal browser behavior. Opening a second tab, restoring browser tabs, or refreshing a page could displace an unrelated connection.

### Socket presence as authentication state

WebSocket lifecycle is not sufficiently stable to represent login state. Network interruption, browser suspension, server restart, and deployment may terminate a socket without invalidating the user's credentials.

### Multiple unrestricted gameplay connections

This model permits conflicting commands, duplicate matchmaking, information leakage between tabs, and race conditions in room and match state.
