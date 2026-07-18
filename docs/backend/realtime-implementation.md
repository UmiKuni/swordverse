# Backend Realtime Implementation

## 1. Purpose

This document translates the accepted realtime and gameplay-lease architecture into backend implementation responsibilities. It does not prescribe a permanent storage technology and does not replace the external WebSocket contract.

## 2. Required Components

### Connection registry

The connection registry tracks active transport connections by `connectionId` and maintains indexes by `userId` and `sessionId`. It supports multiple connections for both a user and a session.

Required operations include:

```java
register(userId, sessionId, connectionId)
unregister(connectionId)
findByUserId(userId)
findBySessionId(sessionId)
closeBySessionId(sessionId, reason)
```

Registration and removal must be idempotent because transport callbacks may be repeated or arrive after application state has already changed.

### Gameplay lease registry

The gameplay lease registry owns atomic reservation, claim, validation, transition, and release operations. Application services must depend on an interface rather than directly accessing an in-memory map.

Conceptual operations include:

```java
reserve(userId, sessionId, activityType, activityId)
claim(userId, sessionId, connectionId, activityType, activityId)
requireOwner(userId, sessionId, connectionId)
transition(userId, expectedActivity, nextActivity)
markDisconnected(connectionId, disconnectedAt)
release(userId, expectedActivity, reason)
```

Every mutation must compare the expected current state. Blind replacement is prohibited.

## 3. HTTP Reservation and WebSocket Claim

Room creation and joining are currently HTTP operations. Their application transaction must reserve gameplay ownership atomically with the room mutation:

```text
validate authenticated user and session
  → verify no conflicting gameplay activity
  → create or join room
  → reserve gameplay lease for user, session, and room
  → commit
```

The subsequent WebSocket claim binds the reservation to `connectionId`. A failed claim does not remove room membership. The client may retry the claim or leave the room through the documented operation.

If the initial lease registry is in memory while room state is persisted, a server restart may lose the reservation. On startup or reconnect, room and match membership is authoritative for rebuilding an eligible lease. This recovery rule must be deterministic and must never permit one user to control two activities.

## 4. Command Interception

Gameplay lease validation should be applied through a common interceptor, authorization service, or command-dispatch boundary rather than repeated inconsistently in every handler.

Validation order is:

1. Authenticate the WebSocket principal.
2. Resolve trusted `userId`, `sessionId`, and `connectionId`.
3. Require gameplay lease ownership for mutation commands.
4. Authorize room or match membership.
5. Validate activity state and phase.
6. Apply command-specific validation.

Snapshot requests used during reconnect may be authorized before final connection claim, but only for the authenticated user who is a member of the referenced activity. Snapshot access never transfers gameplay control implicitly.

## 5. Disconnect Lifecycle

Transport disconnect performs two independent actions:

- Remove the connection from the general connection registry.
- Mark the gameplay lease disconnected when the connection was its controller.

The lease remains reserved until reconnection, explicit leave, match completion, logout, session revocation, or reconnect deadline expiration. Scheduled expiry must compare the lease version or expected disconnect timestamp so that an obsolete timer cannot release a successfully reclaimed lease.

## 6. Session Revocation

Logout and security-driven session revocation must publish an internal session-revoked event. Realtime handling then:

1. Closes all connections associated with that session.
2. Releases a lease owned by that session when product rules permit immediate release.
3. Otherwise marks the player disconnected and applies the room or match disconnect policy.

Whether logout during a match is treated as surrender or disconnect must remain an explicit product rule. It must not be determined accidentally by connection cleanup order.

## 7. Concurrency Requirements

For a single server instance, registry operations may use `ConcurrentHashMap.compute` or an equivalent atomic primitive. The following pattern is prohibited:

```text
if no lease exists
    insert lease
```

because two connections may pass the check concurrently.

Database transactions and in-memory mutations do not form one atomic transaction. Operations that persist room or match state and mutate an in-memory lease require compensation or deterministic recovery. This limitation must be tested explicitly.

## 8. Testing Requirements

Automated tests must cover:

- Multiple general connections for one user.
- Concurrent lease acquisition attempts.
- Two sessions attempting to create or join gameplay simultaneously.
- Two tabs in the same session attempting to claim control.
- Refresh of the controlling tab and successful reclaim.
- Command rejection from an authenticated non-owner connection.
- Disconnect followed by reconnect before the deadline.
- Obsolete disconnect timers after successful reconnect.
- Logout or revocation of the controlling session.
- Server restart recovery when room or match state remains persisted.

## 9. Related Documents

- [Realtime connections and gameplay lease](../architecture/realtime-connections.md)
- [ADR 0001](../architecture/decisions/0001-multi-session-single-gameplay-lease.md)
- [API and WebSocket contract](../contracts/api-websocket-contract.md)
