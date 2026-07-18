# SwordVerse System Architecture

## 1. Purpose

This document defines the principal system boundaries and ownership rules for SwordVerse. It describes stable architectural policy rather than framework-level implementation details.

## 2. System Context

```text
Browser client
    ├── HTTPS REST requests
    └── STOMP over secure WebSocket
             ↓
Spring Boot game server
    ├── Authentication and authorization
    ├── Realtime connection management
    ├── Room and matchmaking services
    ├── Authoritative match engine
    └── Persistence services
             ↓
PostgreSQL
```

The browser submits player intent. The server is authoritative for identity, authorization, gameplay validation, calculations, state transitions, effects, and final results.

## 3. Principal Runtime Identities

The architecture distinguishes the following identities:

| Identity | Scope | Lifetime |
|---|---|---|
| `userId` | Account | Permanent |
| `sessionId` | One authenticated login | Until expiration or revocation |
| `connectionId` | One WebSocket connection | Until disconnect |
| Gameplay lease | Exclusive gameplay ownership for one user | Matchmaking, room, or match activity |

These identities must not be treated as interchangeable. A user may own multiple authentication sessions and each session may establish multiple realtime connections. The user may nevertheless own no more than one gameplay lease.

## 4. Communication Responsibilities

REST is preferred for request-response operations and durable reads, including authentication, profile retrieval, static rules, and match history.

WebSocket is preferred when the server must push timely state changes, including invitations, room updates, matchmaking results, match phases, and battle events. The existence of a WebSocket connection is not evidence that an authentication session should remain valid.

## 5. Ownership Boundaries

- Authentication determines who the caller is.
- Authorization determines whether the authenticated caller may access a resource.
- Connection management records where realtime events may be delivered.
- The gameplay lease determines which connection may issue exclusive gameplay commands.
- Room and match services remain authoritative for membership and phase-specific rules even after a lease has been validated.

## 6. Deployment Assumptions

The initial deployment may use an in-memory connection registry and gameplay lease registry because the service is expected to run as a single game-server instance for a limited audience. Registry interfaces must not expose this implementation detail to application services.

A multi-instance deployment requires shared atomic ownership, such as Redis or a database-backed lease, and cross-instance connection revocation. The external protocol and gameplay ownership rules must remain unchanged when the registry implementation changes.

## 7. Related Documents

- [Authentication architecture](authentication.md)
- [Realtime connections and gameplay lease](realtime-connections.md)
- [ADR 0001](decisions/0001-multi-session-single-gameplay-lease.md)
- [API and WebSocket contract](../contracts/api-websocket-contract.md)
