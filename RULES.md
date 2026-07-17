# SwordVerse — Game Rules

## 1. Match Overview

SwordVerse is a server-authoritative online 1v1 auto-combat game.

Players select Sects and Actions, improve their loadouts during Ascension, prepare Action Queues, and resolve both queues simultaneously on a shared Battle timeline.

The client submits player decisions. The server owns all official validation, calculations, Effect processing, phase transitions, and match results.

## 2. Room Rules

- A room contains at most two players.
- Player A is the host.
- Player B joins using the room code.
- Both players must be present, connected, and ready before the host starts the match.
- The host may remove Player B before the match starts.
- Either player may leave before the match starts.
- If Player A leaves while Player B remains, Player B becomes Player A (host).
- Disconnecting does not immediately remove a player from the room. A disconnected player keeps their slot until they leave, are removed, or the room closes.

## 3. Sects and Actions

### 3.1 Sects

Each Sect defines:

- Main Sect base Stats.
- Support Sect bonus Stats.
- An MP-to-QP conversion ratio when used as the Main Sect.
- A set of Sect Techniques, normally around five.

Support Sect and Main Sect have the same list of attribute.

### 3.2 Action source and activation type

Every Action has a source:

```text
BASIC
SECT_TECHNIQUE
```

Every Action has an activation type:

```text
ACTIVE
PASSIVE
```

Only learned Active Actions may be placed in the Action Queue. Passive Actions activate automatically when server-observed conditions are met and cannot be add into the Action Queue.

Note that an **Ultimate Action** can only be selected as a Main Technique.

### 3.3 Active Action resolution type

Every Active Action defines one resolution type:

```text
RESOLVE_ON_COMPLETION
ACTIVE_DURING_EXECUTION
```

`RESOLVE_ON_COMPLETION` means the Action prepares throughout its duration and applies its configured Effects at the endpoint. For example, the Basic Action Slash occupying `(0.0, 1.0]` deals damage at `1.0`.

`ACTIVE_DURING_EXECUTION` means the configured Effects remain active throughout the complete `(start, end]` execution interval. For example, the Basic Action Defend occupying `(0.0, 1.0]` increases DEF throughout that interval, including at `1.0`, and can defend against a Slash that resolves at `1.0`.

### 3.4 Action components

Each Action level defines:

- **Duration** — execution time.
- **Cooldown** — required waiting time after the Action or its consecutive stack chain ends.
- **Stack** — maximum number of immediately consecutive uses before cooldown applies.
- **Costs** — MP, QP, HP, or other configured resources paid for each use.

Time is represented by integer ticks. One tick is `0.1` second.

AS does not modify Action duration. Every Action uses its configured duration unchanged.

For Slash cooldown only:

```text
effectiveCooldownTicks = max(0, floor(baseCooldownTicks / AS))
```

Cooldown is measured in `0.1`-second ticks, so this rounds down to the nearest tick. Defend, Shield, and Main or Support Sect Technique cooldowns are not modified by `AS`.

### 3.5 Stack and cooldown

`stack = N` permits at most `N` immediately consecutive occurrences of the same Action. Consecutive stacked occurrences must have no gap between them. Each occurrence pays its own costs.

Cooldown begins at the end of the final occurrence in the consecutive stack chain. The next occurrence must satisfy:

```text
nextStart >= stackChainEnd + cooldown
```

For `stack = 1`, cooldown begins after every occurrence.

### 3.6 Persistent Effects created by Actions

An Action may create an Effect whose lifetime differs from the Action's execution duration. Such an Effect remains active until its lifetime expires or its removal condition is met.

Example: the Basic Action Shield may execute for 1 second and then create a Shield Effect with a 2-second lifetime and one charge. The Shield negates the next incoming damage instance and is removed immediately when its charge is consumed. A later attack deals damage normally.

Actions or Effects that dynamically change another Action's duration or cooldown are reserved for a future version and are not implemented in the next version.

### 3.7 Passive Actions

Passive Actions remain in the player's six-Action loadout and activate automatically when their configured conditions are met.

Example conditions include:

- Successfully hit the opponent three times.
- Receive at least 2 damage from one hit.
- Fall below a configured HP percentage.
- Gain or lose a specified Effect.

Passive progress is current runtime state, not Battle history. A three-hit passive needs only its current counter, not a log of every previous hit.

## 4. Pre-Match Selection

### 4.1 Basic Action selection

Each player secretly selects exactly two distinct Basic Actions from:

```text
SLASH
DEFEND
SHIELD
```

After both players confirm, both Basic loadouts are revealed simultaneously.

### 4.2 Main loadout

Each player secretly selects:

- One Main Sect.
- Exactly three distinct Techniques belonging to that Sect.

Main Techniques may be active, passive, or Ultimate.

After both players confirm, both Main loadouts are revealed simultaneously.

### 4.3 Support loadout

Each player then secretly selects:

- One Support Sect.
- Exactly one Technique belonging to that Sect.

The Support Technique:

- Must not duplicate any selected Main Technique.
- Must not be an Ultimate.
- May be active or passive.

After both players confirm, both Support loadouts are revealed simultaneously.

### 4.4 Final loadout

Each player has exactly six Action slots:

```text
BASIC_1
BASIC_2
MAIN_1
MAIN_2
MAIN_3
SUPPORT
```

Both Basic Actions start at level 1. Other Actions may begin locked:

```text
currentLevel = 0  → locked
currentLevel >= 1 → learned
```

A selected but locked Action remains in the loadout and can be learned during Ascension. Every Action has a maximum level of 3.

`MAIN` and `SUPPORT` describe loadout roles, not intrinsic Technique types.

## 5. Stats and Resources

| Value | Meaning |
|---|---|
| `STR` | Offensive power used by server damage calculations. |
| `HP` | Health. Reaching 0 satisfies a match-end condition. |
| `DEF` | Damage reduction used by server calculations. |
| `AS` | Attack speed that reduces Slash cooldown only. |
| `MP` | Resource consumed by Actions. |
| `QP` | Special resource consumed by selected Actions. |

Every Stat has a maximum level of 3. During Ascension, only `HP`, `STR`, `DEF`, and `AS` may be upgraded. `MP` and `QP` are resources and cannot be upgraded during Ascension.

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
RENEWAL → ASCENSION → ACTION STRATEGY → BATTLE
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

Different Effects may use different trigger timings. An Effect's duration decreases according to its configured lifecycle after it executes at the relevant timing.

## 8. Ascension

At the beginning of every round, each player receives 2 Learning Points.

Learning Points may be used to:

- Upgrade `HP`, `STR`, `DEF`, or `AS`, up to level 3.
- Learn a selected Action by changing its level from 0 to 1.
- Upgrade a learned Action, up to level 3.

`MP` and `QP` cannot be upgraded during Ascension.

Rules:

- A player may spend between 0 and 2 LP.
- Unspent LP is forfeited and does not carry into the next round.
- An allocation becomes final when confirmed.
- Confirmed allocations remain secret until both players confirm or the phase times out.
- Resolved allocations are revealed simultaneously.
- If a player does not confirm before timeout, that player receives an empty allocation for the round.

## 9. Action Strategy

### 9.1 Queue duration limit

Queue capacity is the maximum timeline duration that a sequence of Actions may occupy, not a required number of Action entries.

| Round | Duration Limit |
|---:|---:|
| 1 | 2 seconds |
| 2 | 3 seconds |
| 3 | 4 seconds |
| 4 | 5 seconds |
| 5 | 6 seconds |
| 6+ | 7 seconds |

A queue may use less than the available duration. It is invalid only when its total effective duration exceeds the limit.

### 9.2 Eligible Actions

An Action may be added to the queue only when:

- It belongs to one of the player's six Action slots.
- Its current level is at least 1.
- Its activation type is `ACTIVE`.

Passive Actions cannot be queued. The same eligible Action slot may be added multiple times when its stack and cooldown constraints are satisfied.

### 9.3 Queue changes after round 1

The next round's queue is derived from the previous round's confirmed queue:

```text
nextQueue = insertNewActions(removeZeroOrOne(previousConfirmedQueue))
```

Rules:

1. Remove zero or one occurrence from the previous confirmed queue.
2. Preserve the relative order of all retained occurrences.
3. Insert newly selected occurrences at the beginning, end, or between retained occurrences.
4. Check the complete resulting queue against the current round's duration, cooldown, stack, transition, and eligibility rules.

### 9.4 Queue validation

Before confirming, a player may request an authoritative preview validation of the current queue any number of times. Checking does not confirm or lock the queue, and its result is advisory because runtime state may change before an Action executes.

The server returns whether the queue is valid and all detected violations, including the relevant Action occurrence where possible:

```text
DURATION_LIMIT_EXCEEDED
COUNTDOWN_INVALID
```

Validation checks:

- Total effective duration does not exceed the round's duration limit.
- Cooldown and consecutive stack rules are satisfied.
- The queue satisfies the previous-round removal and retained-order rules.
- Every occurrence references an eligible Action.

The optional queue check never evaluates MP, QP, HP, or any other Action cost. All resource requirements are checked only against actual state at runtime.

### 9.5 Confirmation and timeout

- Confirming does not run queue validation again.
- Confirmation does not reject or block an invalid queue.
- A successful confirmation locks the submitted queue for the round.
- The complete queue remains hidden from the opponent and is revealed only as Battle progresses.
- If the phase times out before confirmation, the server uses an empty queue for that player.

Invalid Action occurrences are handled during Battle rather than during confirmation.

## 10. Battle

### 10.1 Shared timeline

Both queues start at time `0` and execute on the same timeline. One tick is `0.1` second.

Actions within each player's queue execute sequentially. Because Actions may have different durations, the two players' Action boundaries do not need to align. There is no initiative and no alternating turn order.

Every Action occupies the interval:

```text
(start, end]
```

The start boundary is excluded and the end boundary is included. Therefore, an `ACTIVE_DURING_EXECUTION` defense remains active when a `RESOLVE_ON_COMPLETION` attack resolves at the same `end` time.

All events scheduled for the same timeline point are resolved by deterministic server rules. Defensive Effects that are active at that point participate in damage resolution.

### 10.2 Runtime Action validation

The server validates each Action occurrence when Battle reaches it, using the actual runtime state. Runtime validation includes Action eligibility, cooldown, stack, configured cost, and any other execution requirements.

If an occurrence is invalid:

- It is removed from execution and treated as an `EMPTY_SLOT`.
- It pays no costs and produces no Action Effects.
- Its scheduled timeline interval remains reserved, so later Actions do not shift earlier.
- The opponent's timeline continues normally.

The optional queue check does not inspect costs. Only the runtime check determines whether an occurrence can pay its costs and execute.

### 10.3 Action costs

The server checks costs when each Action begins or resolves according to its configured cost timing.

- All costs for one Action occurrence are paid atomically.
- If any required cost cannot be paid, no cost is paid.
- The Action fails with `INSUFFICIENT_RESOURCE`.
- The invalid occurrence becomes an `EMPTY_SLOT`, and the opponent's timeline continues to resolve.

### 10.4 Disabled and failed Actions

An Action produces no configured Effects when:

- Its costs cannot be paid at runtime.
- It is disabled by an active Effect.

Failure reasons include:

```text
INSUFFICIENT_RESOURCE
DISABLED_BY_EFFECT
EMPTY_SLOT
```

### 10.5 Passive triggers

Battle resolution emits internal gameplay events such as:

```text
ACTION_HIT
DAMAGE_DEALT
DAMAGE_RECEIVED
ATTRIBUTE_CHANGED
EFFECT_APPLIED
EFFECT_REMOVED
```

The server evaluates learned Passive Actions against these events. Triggered Effects and any resulting gameplay events resolve in deterministic order. The server limits passive trigger depth and events per timeline point to prevent infinite trigger loops.

### 10.6 Active Effects

Effects may be:

- Instant.
- Finite-duration.
- Charge-based and removed when their charges are consumed.
- Infinite until removed.

Effects may use configured stacking, refresh, replacement, priority, lifetime, charge, trigger, and removal rules.

## 11. Match End Conditions

The match ends immediately when:

- A player's HP reaches 0.
- A player surrenders.
- A player remains disconnected for more than five minutes.

### 11.1 Draw

If both players reach 0 HP at the same timeline point:

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

The frontend may display a lightweight Battle Log using live server events with round number and timeline tick.

SwordVerse does not store a structured replay or complete event history. The database may store a simple human-readable match summary in `matches.log_text` for lightweight display and debugging, not deterministic replay.

## 13. Static Game Data Updates

SwordVerse does not maintain game-content versions.

Before updating Sect, Action, trigger, cost, or Effect definitions:

1. Prevent new matches from starting.
2. End or cancel all active matches.
3. Apply and validate the static-data update.
4. Rebuild the server's immutable in-memory content cache.
5. Allow new matches to start.

Static game data must not change while a match is active.
