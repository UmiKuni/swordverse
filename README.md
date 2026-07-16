# SwordVerse

SwordVerse is an online 1v1 auto-combat game about building a sword-fighting style, preparing an Action Queue, and watching both players resolve their plans simultaneously.

The game is designed for short private matches between friends.

## Quick Start

### 1. Create or join a room

Sign in, then:

- Create a room and share its room code; or
- Enter a room code sent by your friend.

Both players must be present, connected, and ready before the host can start the match.

### 2. Build your five-Action loadout

Each player prepares exactly five Actions:

```text
1 Basic Action
3 Main Actions
1 Support Action
```

First, select a **Main Sect** and three Actions from that Sect.

Then, select a **Support Sect** and one Action from it. Your Support Sect may be the same as your Main Sect, but the Support Action cannot duplicate a Main Action and cannot be an **Ultimate**.

Actions may be:

- **Active** — can be added to your Action Queue.
- **Passive** — activates automatically when its condition is met.
- **Ultimate** — a Sect's special Action; it may only be selected as a Main Action.

### 3. Play each round

Every round has four phases:

```text
RENEWAL
→ ASCENSION
→ ACTION STRATEGY
→ BATTLE
```

#### Renewal

The server resolves ongoing Effects, converts remaining MP into QP, and restores MP.

#### Ascension

Spend up to 2 Learning Points to:

- Upgrade a Stat.
- Learn a locked Action.
- Level up a learned Action.

An Action at level 0 is locked. An Action at level 1 or higher is learned.

#### Action Strategy

Build your Action Queue using learned Active Actions from your five-Action loadout.

You may add the same Active Action more than once.

#### Battle

Both queues resolve together:

```text
Your first Action     ↔ Opponent's first Action
Your second Action    ↔ Opponent's second Action
...
```

There is no turn order or initiative. Each pair resolves as one Battle tick.

### 4. Win the match

The match ends when:

- A player's HP reaches 0.
- A player surrenders.
- A player remains disconnected for more than five minutes.

If both players reach 0 HP in the same tick, the match is a draw.

## Core Stats

| Stat | Meaning |
|---|---|
| `STR` | Offensive power. |
| `HP` | Health. Reaching 0 ends the match. |
| `DEF` | Damage reduction. |
| `AS` | Attack speed used by server-side Action resolution. |
| `MP` | Resource used to activate Actions. |
| `QP` | Special resource used by selected Actions. |

HP, MP, and QP have current and maximum values. Restoration cannot exceed the current maximum.

## Detailed Rules

For complete rules, timing, selection restrictions, queue behavior, Passive Actions, Effects, disconnect handling, and match-end resolution, see [RULES.md](RULES.md).

## Technical Documentation

- [API_WEBSOCKET_CONTRACT.md](API_WEBSOCKET_CONTRACT.md) — REST and WebSocket contract.
- [Database schema](game-server/DATABASE_SCHEMA.md) — PostgreSQL schema design.

## Technology

### Client

- React and TypeScript
- Redux Toolkit and RTK Query
- React Router
- STOMP over WebSocket

### Server

- Spring Boot
- Spring WebSocket
- PostgreSQL
- JWT access tokens with server-side sessions and rotating refresh tokens

## Project Status

SwordVerse is currently in the documentation and design phase. Implementation instructions will be added after the client and server projects are ready to run.
