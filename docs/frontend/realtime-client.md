# Frontend Realtime Client

## 1. Connection Model

Each tab that requires realtime updates may establish its own authenticated WebSocket connection. Read-only pages that do not require server-pushed updates should use REST and should not establish a socket solely to preserve login state.

The client distinguishes:

```text
authentication state
connection state
gameplay ownership state
```

No state may be derived solely from another state.

## 2. Recommended State Machine

```text
DISCONNECTED
    ↓ connect
CONNECTING
    ↓ authenticated
GENERAL_CONNECTED
    ↓ acquire or reclaim lease
GAMEPLAY_ACTIVE
    ↓ transport lost
RECONNECTING
    ├── success → GAMEPLAY_ACTIVE
    └── deadline/conflict → GENERAL_CONNECTED or DISCONNECTED
```

Authentication failure transitions the affected session toward logout. Gameplay ownership conflict returns to a non-gameplay state while preserving authentication.

## 3. Subscription Order

After connection or reconnection, the client must:

1. Establish the authenticated connection.
2. Subscribe to required private destinations.
3. Request the authoritative room or match snapshot.
4. Reconcile local state from the snapshot.
5. Resume permitted interaction.

Confirmation and mutation commands must not be replayed automatically after reconnect.

## 4. Gameplay Lease UX

The lease is acquired when entering matchmaking, creating or joining a room, or reconnecting to an existing gameplay activity. Opening a game-related route alone does not establish ownership.

If another connection owns gameplay, the current tab remains usable for non-gameplay features and presents a formal conflict message. An explicit takeover control must not be displayed until the server contract defines takeover behavior.

## 5. Disconnect Behavior

Unexpected socket loss does not log the user out. During a match, the UI displays reconnecting state until reconnection succeeds or the server-provided disconnect deadline expires.

Closing a read-only tab has no gameplay effect. Closing the gameplay tab retains server-side gameplay ownership for the reconnect grace period.

## 6. Security

- The client never sends a caller-selected `userId` or `playerId` as proof of identity.
- Access tokens and session identifiers must not be included in analytics events or user-visible logs.
- Incoming events must be treated as untrusted transport data until validated against their DTO schema.
