# Realtime Connections and Gameplay Lease

## 1. Purpose

This document defines how SwordVerse supports multiple authenticated tabs and devices while ensuring that one account can control only one gameplay activity at a time.

## 2. Definitions

### Authentication session

A revocable login represented by `sessionId`. A user may own multiple active sessions.

### Realtime connection

One established WebSocket connection represented by `connectionId`. A user and session may own multiple connections.

### Gameplay lease

The exclusive right for one connection to issue commands associated with matchmaking, a room, or a match. A user may own at most one active gameplay lease.

## 3. Required Invariants

1. A user may maintain multiple authentication sessions.
2. A user may maintain multiple general realtime connections.
3. A user may own at most one gameplay lease.
4. Only the connection identified by the lease may issue gameplay commands.
5. Lease validation does not replace room membership, match membership, phase, or payload validation.
6. Disconnect does not revoke authentication.
7. Logout or session revocation closes connections belonging to the revoked session.

## 4. Command Classification

| Operation | Transport | Gameplay lease required |
|---|---|---:|
| Read rules or static game data | REST | No |
| Read profile or match history | REST | No |
| Receive general notifications | WebSocket | No |
| Chat or social presence | REST/WebSocket | No |
| Enter matchmaking | WebSocket | Yes; acquisition may occur atomically with the command |
| Create or join a room | REST or WebSocket | Yes; acquisition must be atomic with the operation |
| Change room readiness or start a match | WebSocket | Yes |
| Submit loadout, Ascension, or Action Queue commands | WebSocket | Yes |
| Surrender | WebSocket | Yes |

Read operations must not acquire a gameplay lease.

## 5. Lease Model

A lease contains, at minimum:

```text
userId
sessionId
connectionId
activityType
activityId
acquiredAt
lastSeenAt
```

`activityType` identifies `MATCHMAKING`, `ROOM`, or `MATCH`. `activityId` may be absent while matchmaking and must identify the room or match after one exists. `connectionId` may be temporarily absent when an HTTP operation has reserved a room activity for the authenticated session but the realtime connection has not yet claimed control.

Lease acquisition must be atomic. A check followed by a separate insertion is insufficient because concurrent connections could both observe that no lease exists.

## 6. Acquisition and Release

The server acquires a lease when a user enters matchmaking, creates a room, joins a room, or reconnects to an existing room or match. Merely opening a page or establishing a general WebSocket connection does not acquire a lease.

When room creation or joining is performed over HTTP, the operation atomically reserves the lease for the authenticated user and session. After establishing and subscribing through WebSocket, that client sends the gameplay claim command. The server binds the reserved lease to the claiming `connectionId` only if the authenticated user, session, and activity are consistent with the reservation.

The server releases the lease when the user leaves matchmaking, leaves a room according to room rules, completes the match, logs out the owning session, or exceeds the applicable reconnect deadline.

An unexpected disconnect retains the lease during the reconnect grace period. This prevents a second device from taking control while the original gameplay client is temporarily reconnecting.

## 7. Reconnection and Takeover

A connection may reclaim an existing lease when:

- It authenticates as the same user.
- Its session remains active.
- The previous lease connection is no longer healthy.
- The user remains a member of the referenced room or match.

A healthy gameplay connection is not replaced implicitly. If explicit takeover is introduced, the server must first invalidate and close the previous gameplay connection, then transfer the lease atomically. The external contract must clearly inform both clients.

## 8. Command Authorization

For every gameplay command, the server must validate:

```text
authenticated userId
authenticated sessionId
current connectionId
active gameplay lease
resource membership
current gameplay phase
command-specific rules
```

A connection that is authenticated but does not own the lease receives a gameplay conflict error, not an authentication error. The client must not log the user out in response.

## 9. Initial and Distributed Implementations

For a single server instance, a `ConcurrentHashMap<UUID, GameplayLease>` behind a dedicated registry interface is acceptable. Atomic operations such as `compute` must be used for acquisition and transfer.

For multiple server instances, the lease must move to a shared store with atomic compare-and-set semantics and an expiry or heartbeat mechanism. General connection delivery also requires cross-instance routing.

## 10. Observability

Connection and lease logs may include `userId`, `sessionId`, `connectionId`, activity type, and activity identifier. They must not contain access tokens, refresh tokens, passwords, or complete authorization headers.
