# SwordVerse

SwordVerse is an online 1v1 auto-combat game about building a sword-fighting style, preparing an Action Queue on a shared timeline, and watching both players resolve their plans simultaneously.

The game is designed for short private matches between friends.

## Quick Start

### 1. Create or join a room

Sign in, then:

- Create a room and share its room code; or
- Enter a room code sent by your friend.

Both players must be present, connected, and ready before the host can start the match.

### 2. Build your six-Action loadout

Each player has exactly six Actions:

```text
2 Basic Actions
3 Main Sect Techniques
1 Support Sect Technique
```

First, select two distinct **Basic Actions** from **Slash**, **Defend**, and **Shield**. Then select a **Main Sect** and three Techniques from that Sect.

Then, select a **Support Sect** and one Technique from it. Your Support Sect may be the same as your Main Sect, but the Support Technique cannot duplicate a Main Technique and cannot be an **Ultimate**.

Actions may be:

- **Active** — learned Actions that can be added to the Action Queue.
- **Passive** — Actions that activate automatically when their conditions are met.
- **Ultimate** — a Sect's special Technique; it may only be selected as a Main Technique.

Active Actions also define how their Effects resolve:

- **Resolve on completion** — the Effect occurs at the end of the Action's duration, such as an attack dealing damage.
- **Active during execution** — the Effect remains active throughout the Action's `(start, end]` interval, such as Defend increasing DEF.

### 3. Play each round

Every round has four phases:

```text
RENEWAL → ASCENSION → ACTION STRATEGY → BATTLE
```

#### 3.1. Renewal

The server resolves ongoing Effects, converts remaining MP into QP, and restores MP.

#### 3.2. Ascension

Spend up to 2 Learning Points to:

- Upgrade `HP`, `STR`, `DEF`, or `AS`.
- Learn a locked Action.
- Level up a learned Action.

Stats and Actions have a maximum level of 3. An Action at level 0 is locked; an Action at level 1 or higher is learned.

#### 3.3. Action Strategy

Build your Action Queue using learned Active Actions from your six-Action loadout.

Queue capacity is a duration limit, not a number of Actions. Time is measured in `0.1` second ticks. A queue is valid only when:

- Its total duration does not exceed the round's duration limit, including the Action's duration and empty space between if have.
- Every Action satisfies its cooldown and stack rules.
- After round 1, it is derived from the previous queue by removing zero or one occurrence, preserving retained order, and inserting new Actions anywhere.

AS does not change any Action's duration. It reduces Slash cooldown only: Slash's configured cooldown is divided by `AS` and rounded down to whole `0.1`-second ticks. Defend, Shield, and all Main or Support Sect Technique cooldowns are not changed by `AS`.

You may ask the server to check whether the current queue is valid before confirming it. This check is advisory: confirming does not validate or block the submitted queue.

During Battle, the server checks each Action occurrence against the current runtime state. An invalid occurrence is removed from execution and resolved as an `EMPTY_SLOT`; it produces no Action Effects, but its scheduled timeline interval remains reserved so later Actions do not shift. The opponent's timeline continues normally.

#### 3.4. Battle

Both queues start at time `0` and resolve on the same `0.1` second timeline. There is no initiative or alternating turn order.

An Action occupies `(start, end]`. An attack that resolves on completion applies its damage at `end`; a continuous defensive Action remains active at that same endpoint and can defend against the attack.

Some Actions create Effects that outlive their execution. For example, the Basic Action Shield may create a one-charge Shield Effect that lasts two seconds, negates the next incoming damage instance, and is then removed.

### 4. Win the match

The match ends when:

- A player's HP reaches 0.
- A player surrenders.
- A player remains disconnected for more than five minutes.

If both players reach 0 HP at the same timeline point, the match is a draw.

## Core Stats and Resources

| Value | Meaning |
|---|---|
| `STR` | Offensive power. Upgradeable during Ascension. |
| `HP` | Health. Reaching 0 ends the match. Upgradeable during Ascension. |
| `DEF` | Damage reduction. Upgradeable during Ascension. |
| `AS` | Reduces Slash cooldown only. Upgradeable during Ascension. |
| `MP` | Resource used to activate Actions; not upgraded during Ascension. |
| `QP` | Special resource used by selected Actions; not upgraded during Ascension. |

HP, MP, and QP have current and maximum values. Restoration cannot exceed the current maximum.

## Detailed Rules

For complete rules, timing, selection restrictions, queue validation, cooldowns, stacks, Passive Actions, Effects, disconnect handling, and match-end resolution, see [RULES.md](RULES.md).

## Technical Documentation

- [API_WEBSOCKET_CONTRACT.md](API_WEBSOCKET_CONTRACT.md) — REST and WebSocket contract.
- [DATABASE_SCHEMA.md](game-server/DATABASE_SCHEMA.md) — PostgreSQL schema design.

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
- JWT access tokens with server-side sessions and rotating opaque refresh tokens stored in host-only `HttpOnly`, `Secure`, `SameSite=Strict` cookies

## Project Status

SwordVerse is currently in the documentation and design phase. Implementation instructions will be added after the client and server projects are ready to run.
