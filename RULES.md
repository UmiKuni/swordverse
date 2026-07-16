# SwordVerse — Game Rules

## 1. Match Overview

SwordVerse is a server-authoritative online 1v1 auto-combat game.

Players select Sects and Actions, improve their loadouts during Ascension, prepare Action Queues, and resolve both queues simultaneously during Battle.

The client submits player decisions. The server owns all official validation, calculations, Effect processing, phase transitions, and match results.

## 2. Room Rules

- A room contains at most two players.
- Player A is the host.
- Player B joins using the room code.
- Both players must be present, connected, and ready before the host starts the match.
- The host may remove Player B before the match starts.
- Either player may leave before the match starts.
- If Player A leaves while Player B remains, Player B becomes Player A.
- Disconnecting does not immediately remove a player from the room. A disconnected player keeps their slot until they leave, are removed, or the room closes.

## 3. Sects and Actions

### 3.1 Sects

Each Sect defines:

- Main Sect base Stats.
- Support Sect bonus Stats.
- An MP-to-QP conversion ratio when used as the Main Sect.
- A set of Actions, normally around five.

The Support Sect may be the same as the Main Sect.

### 3.2 Action classifications

Every Action has a source:

```text
BASIC
SECT
```

Every Action has an activation type:

```text
ACTIVE
PASSIVE
```

A Sect Action may additionally be marked as an **Ultimate**.

These classifications are independent. For example, an Ultimate may be active or passive.

### 3.3 Active Actions

Active Actions:

- May be added to the Action Queue when learned.
- May appear more than once in the same queue.
- Resolve during Battle when their queue position is reached.
- May fail if their cost cannot be paid or an Effect disables them.

### 3.4 Passive Actions

Passive Actions:

- Remain in the player's five-Action loadout.
- Cannot be added to the Action Queue.
- Activate automatically when server-observed conditions are met.

Example conditions include:

- Successfully hit the opponent three times.
- Receive at least 2 damage from one hit.
- Fall below a configured HP percentage.
- Gain or lose a specified Effect.

Passive progress is current runtime state, not Battle history. A three-hit passive needs only its current counter, not a log of every previous hit.

## 4. Pre-Match Selection

### 4.1 Main loadout

Each player secretly selects:

- One Main Sect.
- Exactly three distinct Actions belonging to that Sect.

Main Actions may be active, passive, or Ultimate.

After both players confirm, both Main loadouts are revealed simultaneously.

### 4.2 Support loadout

Each player then secretly selects:

- One Support Sect.
- Exactly one Action belonging to that Sect.

The Support Action:

- Must not duplicate any selected Main Action.
- Must not be an Ultimate.
- May be active or passive.

After both players confirm, both Support loadouts are revealed simultaneously.

### 4.3 Final loadout

Each player has exactly five Action slots:

```text
BASIC
MAIN_1
MAIN_2
MAIN_3
SUPPORT
```

The Basic Action starts at level 1.

Other Actions may begin locked:

```text
currentLevel = 0  → locked
currentLevel >= 1 → learned
```

A selected but locked Action remains in the loadout and can be learned during Ascension.

## 5. Stats and Resources

| Stat | Meaning |
|---|---|
| `STR` | Offensive power used by server damage calculations. |
| `HP` | Health. Reaching 0 satisfies a match-end condition. |
| `DEF` | Damage reduction used by server calculations. |
| `AS` | Attack speed used by server Action-resolution rules. |
| `MP` | Resource consumed by Actions. |
| `QP` | Special resource consumed by selected Actions. |

HP, MP, and QP have current and maximum values:

```text
0 <= current <= max
```

Rules:

- Healing cannot increase HP above `hp.max`.
- MP restoration cannot increase MP above `mp.max`.
- MP-to-QP conversion cannot increase QP above `qp.max`.
- Effects may increase or decrease maximum values during a match.
- When a maximum is reduced below its current value, the current value is clamped to the new maximum.
- Values cannot fall below 0.

## 6. Round Structure

Each round contains:

```text
RENEWAL
→ ASCENSION
→ ACTION STRATEGY
→ BATTLE
```

If no match-end condition is satisfied after Battle, the next round begins with Renewal.

## 7. Renewal

Renewal is resolved entirely by the server.

Resolution order:

1. Resolve active Effects scheduled for `RENEWAL_START`.
2. Convert remaining MP into QP using the Main Sect's conversion ratio.
3. Clamp QP to `qp.max`.
4. Restore MP to `mp.max`.
5. Resolve active Effects scheduled for `RENEWAL_END`.
6. Remove expired Effects.

Different Effects may use different trigger timings. For example:

- Bleed may deal damage at `RENEWAL_START`.
- Regeneration may restore HP at `RENEWAL_END`.

An Effect's duration decreases according to its configured lifecycle after it executes at the relevant timing.

## 8. Ascension

At the beginning of every round, each player receives 2 Learning Points.

Learning Points may be used to:

- Upgrade `STR`, `HP`, `DEF`, `AS`, `MP`, or `QP`.
- Learn a selected Action by changing its level from 0 to 1.
- Upgrade a learned Action to a higher configured level.

Rules:

- A player may spend between 0 and 2 LP.
- Unspent LP is forfeited and does not carry into the next round.
- An allocation becomes final when confirmed.
- Confirmed allocations remain secret until both players confirm or the phase times out.
- Resolved allocations are revealed simultaneously.
- If a player does not confirm before timeout, that player receives an empty allocation for the round.

## 9. Action Strategy

### 9.1 Queue size

| Round | Required Queue Size |
|---:|---:|
| 1 | 2 |
| 2 | 3 |
| 3 | 4 |
| 4 | 5 |
| 5 | 6 |
| 6+ | 7 |

### 9.2 Eligible Actions

An Action may be added to the queue only when:

- It belongs to one of the player's five Action slots.
- Its current level is at least 1.
- Its activation type is `ACTIVE`.

Passive Actions cannot be queued.

The same eligible Action slot may be added multiple times.

### 9.3 Queue changes after round 1

When preparing the next queue:

1. Remove at most one occurrence from the previous queue.
2. Preserve the relative order of retained occurrences.
3. Insert newly selected occurrences at the beginning, end, or between retained occurrences.

### 9.4 Confirmation and timeout

- A player may submit between zero Actions and the required queue size.
- Submitting confirms the queue and prevents further edits.
- The queue remains hidden from the opponent until each position is revealed during Battle.
- If the phase times out before confirmation, the server uses an empty submission for that player.
- Every missing queue position becomes an explicit `EMPTY_TIMEOUT` position.

An empty position performs no Action during its Battle tick.

## 10. Battle

### 10.1 Simultaneous ticks

Both queues resolve in lockstep:

```text
Tick 0: Player A slot 0 ↔ Player B slot 0
Tick 1: Player A slot 1 ↔ Player B slot 1
...
```

There is no initiative and no alternating turn order.

The outcome of a tick depends on both Actions and the current runtime state. Examples:

- Two attacking Actions may damage both players.
- An attack against a counter may damage only the attacker.
- An attack against a guard may deal no damage.

These are examples, not universal categories. The server resolves configured costs, triggers, Effects, and exceptional Action behavior.

### 10.2 Action costs

The server validates all costs when an Action resolves.

- All costs are paid atomically.
- If any required cost cannot be paid, no cost is paid.
- The Action fails with `INSUFFICIENT_RESOURCE`.
- The opponent's Action continues to resolve.

### 10.3 Disabled and empty Actions

An Action produces no Effects when:

- Its costs cannot be paid.
- It is disabled by an active Effect.
- The queue position is empty.

Failure reasons are:

```text
INSUFFICIENT_RESOURCE
DISABLED_BY_EFFECT
EMPTY_SLOT
```

### 10.4 Passive triggers

Battle resolution emits internal gameplay events such as:

```text
ACTION_HIT
DAMAGE_DEALT
DAMAGE_RECEIVED
ATTRIBUTE_CHANGED
EFFECT_APPLIED
EFFECT_REMOVED
```

The server evaluates learned Passive Actions against these events.

When a passive condition is satisfied:

1. The Passive Action activates.
2. Its configured Effects resolve.
3. Its runtime progress resets according to its trigger rule.
4. Any new gameplay events are evaluated in deterministic order.

The server limits passive trigger depth and the number of events per tick to prevent infinite trigger loops.

### 10.5 Active Effects

Effects may be:

- Instant.
- Finite-duration.
- Infinite until removed.

Effects may use these stacking policies:

```text
NONE
REFRESH
STACK
REPLACE
STRONGEST_WINS
```

The server applies each Effect's configured duration, periodic timing, stack limit, and removal rules.

## 11. Match End Conditions

The match ends immediately when:

- A player's HP reaches 0.
- A player surrenders.
- A player remains disconnected for more than five minutes.

### 11.1 Draw

If both players reach 0 HP during the same tick:

```text
reason = DRAW
winner = null
loser = null
```

### 11.2 Surrender

- Surrender is valid in every match phase except `GAME_OVER`.
- The surrendering player loses.
- Pending timers and Battle resolution are cancelled.

### 11.3 Disconnect

- The five-minute timer starts immediately when a match player disconnects.
- The rule applies during every match phase.
- Reconnecting as the same authenticated user before the deadline restores connection state.
- If the deadline expires, the disconnected player loses.

## 12. Battle Log and History

The frontend may display a lightweight Battle Log using live server events such as:

```text
[Round 1][Tick 0]
Player A used Quick Slash.
Player B used Guard.
Player B HP changed from 100 to 92.
Player B received Bleed.
```

SwordVerse does not store a structured replay or complete event history.

The database may store a simple human-readable match summary in `matches.log_text`. This text is for lightweight display and debugging, not deterministic replay.

## 13. Static Game Data Updates

SwordVerse does not maintain game-content versions.

Before updating Sect, Action, trigger, cost, or Effect definitions:

1. Prevent new matches from starting.
2. End or cancel all active matches.
3. Apply and validate the static-data update.
4. Rebuild the server's immutable in-memory content cache.
5. Allow new matches to start.

Static game data must not change while a match is active.
