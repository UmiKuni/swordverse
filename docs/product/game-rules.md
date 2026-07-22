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

### 2.1 Concurrent access and gameplay ownership

- A player account may remain authenticated in multiple browser tabs or devices.
- Non-gameplay activities, including reading rules, viewing a profile, and reviewing match history, may be used concurrently.
- A player account may participate in only one active gameplay activity at a time.
- Gameplay activity begins when the player enters matchmaking, creates or joins a room, or reconnects to an existing room or match.
- Only one connection may control that gameplay activity at a time.
- A second authenticated connection does not log the player out. It is prohibited only from issuing gameplay commands while another connection owns gameplay control.
- A temporary disconnect retains the player's gameplay ownership until the applicable reconnect deadline expires.

## 3. Sects and Actions

### 3.1 Sects

Each Sect defines:

- Main Sect base Stats.
- Support Sect bonus Stats.
- A set of Sect Techniques, normally around five.

Support Sect and Main Sect have the same list of attribute.

Qi generation, Qi limits, and Qi spending are global round rules. They are not defined by a Sect.

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

An **Ultimate Action** is always `ACTIVE`, can only be selected as a Main Technique, and cannot be selected as the Support Action.

### 3.3 Active Action resolution types

Every Active Action defines a non-empty set of one to three resolution types. An Action may use any combination of these types, with no duplicates:

```text
RESOLVE_ON_START
RESOLVE_DURING_EXECUTION
RESOLVE_ON_END
```

Each configured Effect is assigned to one declared resolution type. Passive Actions have no resolution types.

`RESOLVE_ON_START` applies its configured Effects at the first timeline tick after the occurrence starts: `startTick + 1`. For an Action occupying `(0.0, 1.0]`, it resolves at `0.1` seconds.

`RESOLVE_DURING_EXECUTION` keeps its configured Effects active throughout the complete `(start, end]` execution interval. For example, an Action occupying `(0.0, 1.0]` can increase DEF throughout that interval, including at `1.0`.

`RESOLVE_ON_END` applies its configured Effects at the endpoint. For example, an Action occupying `(0.0, 1.0]` resolves at `1.0`.

### 3.4 Action components

Each Action level defines:

- **Duration** — execution time.
- **Cooldown** — required waiting time after the Action or its consecutive stack chain ends.
- **Stack** — maximum number of immediately consecutive uses before cooldown applies.
- **Costs** — QI, HP, or other configured resources paid for each use.

Time is represented by integer ticks. One tick is `0.1` second.

AS modifies an Active Action's execution duration. `baseDurationTicks` is the configured duration at `AS = 1`. When an occurrence reaches its runtime start position, the server snapshots the performer's strictly positive current AS as `asSnapshot`, then calculates:

```text
effectiveDurationTicks = max(1, ceil(baseDurationTicks / asSnapshot))
```

The server schedules that occurrence's `(start, end]` interval and all of its resolution timings from `effectiveDurationTicks`. It then performs runtime validation and atomically checks costs. A valid occurrence executes across the calculated interval; an invalid occurrence becomes `EMPTY_SLOT` but retains that interval. The next occurrence on the same player's timeline begins after that interval ends.

Each player's queue is scheduled incrementally during Battle. Both players still share one deterministic timeline, but AS gained during Battle can affect the duration of later occurrences that have not started. It never reschedules an occurrence that has started. Rounding up preserves the integer `0.1`-second tick timeline, and an Action can never be shorter than one tick.

An Action with a documented controlled custom duration calculates that duration at runtime instead of using the default formula and must not apply the default formula a second time.

`SLASH`, `DEFEND`, and `SHIELD` are Basic Actions with no cooldown. Main and Support Sect Techniques may define cooldowns, measured in `0.1`-second ticks. AS does not modify cooldowns.

### 3.5 Stack and cooldown

`stack = N` permits at most `N` immediately consecutive occurrences of the same Action. Consecutive stacked occurrences must have no gap between them. Each occurrence pays its own costs.

Basic Actions have unlimited consecutive uses and no cooldown. For an Action with a configured cooldown, cooldown begins at the end of the final occurrence in the consecutive stack chain. The next occurrence must satisfy:

```text
nextStart >= stackChainEnd + cooldown
```

For `stack = 1`, cooldown begins after every occurrence.

### 3.6 Persistent Effects created by Actions

An Action may create an Effect whose lifetime differs from the Action's execution duration. Such an Effect remains active until its lifetime expires or its removal condition is met.

Shield does not create a persistent shield Effect. It has a configured base duration of 1 second and, through `RESOLVE_DURING_EXECUTION`, Boosts the performer's DEF by 100% throughout its effective `(start, end]` interval. The DEF Boost is removed when the Action ends.

Defend is also execution-bound. It has a configured base duration of 1 second and, through `RESOLVE_DURING_EXECUTION`, ignores incoming Slash damage only throughout its effective `(start, end]` interval. Its protection ends when the Action ends.

Actions or Effects that dynamically change another Action's duration or cooldown are reserved for a future version and are not implemented in the next version.

### 3.7 Passive Actions

Passive Actions remain in the player's six-Action loadout and activate automatically when their configured conditions are met.

Example conditions include:

- Successfully hit the opponent three times.
- Receive at least 2 damage from one hit.
- Fall below a configured HP percentage.
- Gain or lose a specified Effect.

Passive progress is current runtime state, not Battle history. A three-hit passive needs only its current counter, not a log of every previous hit.

`ACTION_STARTED` is the canonical event emitted when an Active Action begins. For a Shadow Sword Action, the server resolves events in this order:

1. Emit `ACTION_STARTED`.
2. Resolve applicable Passive triggers, including Predation.
3. Apply any Shade gained by those triggers.
4. Resolve the Active Action's `RESOLVE_ON_START` Effects.
5. Allow the Action to consume Shade.

The internal authoritative `ACTION_STARTED` event may also have a public server-event representation. The public event reveals the occurrence, not hidden Passive trigger configuration.

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
| `AS` | Positive `numeric(10,2)` Attack Speed. It shortens Active Action duration using `ceil(baseDurationTicks / AS)` and is otherwise used by server combat calculations where configured. |

Every Stat has a maximum level of 3. During Ascension, only `HP`, `STR`, `DEF`, and `AS` may be upgraded.

HP has current and maximum values:

```text
0 <= current <= max
```

Rules:

- Healing cannot increase HP above `hp.max`.
- Effects may increase or decrease maximum values during a match.
- When a maximum is reduced below its current value, the current value is clamped to the new maximum.
- Values cannot fall below 0.

### 5.1 Qi runtime pools

Qi is the only energy resource used by Actions. It is runtime match state, not a character Stat; no Sect grants it, and Ascension cannot upgrade it.

Each player owns two Qi pools:

```text
roundQi
reserveQi
```

Their global limits are:

```text
0 <= roundQi <= 550
0 <= reserveQi <= 150
availableQi = roundQi + reserveQi
```

`availableQi` is derived runtime state and is not independently persisted.

During Renewal, the server grants Round Qi for the new round:

| Round | Round Qi granted |
|---:|---:|
| 1 | 150 |
| 2 | 250 |
| 3 | 300 |
| 4 | 350 |
| 5 | 400 |
| 6 | 450 |
| 7 | 500 |
| 8+ | 550 |

Effects may restore or generate Qi, and every Qi-changing Effect must explicitly target `ROUND_QI` or `RESERVE_QI`. A change is clamped to the target pool's global range; excess Qi is discarded and neither pool may become negative. Effects cannot modify a Qi maximum.

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
2. Transfer remaining Round Qi into Reserve Qi:

   ```text
   transferableQi = min(roundQi, 150 - reserveQi)
   reserveQi = reserveQi + transferableQi
   discardedQi = roundQi - transferableQi
   roundQi = 0
   ```

3. Grant Round Qi for the new round: `roundQi = roundQiGrantedFor(currentRound)`.
4. Resolve active Effects scheduled for `RENEWAL_END`.
5. Clamp `roundQi` and `reserveQi` to their valid ranges.
6. Remove expired Effects according to the existing Effect lifecycle.

Effects at `RENEWAL_START` may change Qi before the transfer. Effects at `RENEWAL_END` observe the newly granted Round Qi. Any Round Qi that cannot be transferred because Reserve Qi has reached 150 is discarded. An Effect's duration decreases according to its configured lifecycle after it executes at the relevant timing.

## 8. Ascension

At the beginning of every round, each player receives 2 Learning Points.

Learning Points may be used to:

- Upgrade `HP`, `STR`, `DEF`, or `AS`, up to level 3.
- Learn a selected Action by changing its level from 0 to 1.
- Upgrade a learned Action, up to level 3.

Qi cannot be upgraded during Ascension.

Each Stat upgrade permanently increases that Stat's unmodified player value by 10%. The increase is cumulative: each upgrade uses the value produced by the previous permanent upgrade, before temporary Effect modifiers are applied.

```text
increase = ceil(integerStatValue * 0.10)
integerStatValue = integerStatValue + increase
```

`HP`, `STR`, and `DEF` are integers, so their 10% increase is rounded up to the next integer. `AS` is a positive `numeric(10,2)` value; its permanent increase is rounded up to two decimal places:

```text
asIncrease = ceil(asValue * 0.10 * 100) / 100
asValue = asValue + asIncrease
```

Temporary AS modifiers also use decimal arithmetic and are rounded up to two decimal places before the server calculates Action durations.

For an HP upgrade, calculate the rounded-up increase from the previous `hp.max`, then apply it to both values:

```text
hp.max = hp.max + increase
hp.current = min(hp.current + increase, hp.max)
```

The `hp.current` increase is an immediate heal equal to the HP maximum increase.

Rules:

- A player may spend between 0 and 2 LP.
- Unspent LP is forfeited and does not carry into the next round.
- An allocation becomes final when confirmed.
- Confirmed allocations remain secret until both players confirm or the phase times out.
- Resolved allocations are revealed simultaneously.
- If a player does not confirm before timeout, that player receives an empty allocation for the round.

## 9. Action Strategy

### 9.1 Queue length

An Action Queue has no maximum number of Action occurrences and no maximum timeline duration. Each occurrence's runtime AS snapshot and effective duration determine when it resolves, but the complete queue may use any number of ticks.

### 9.2 Eligible Actions

An Action may be added to the queue only when:

- It belongs to one of the player's six Action slots.
- Its current level is at least 1.
- Its activation type is `ACTIVE`.

Passive Actions cannot be queued. The same eligible Action slot may be added multiple times when its stack and cooldown constraints are satisfied.

### 9.3 Queue changes after round 1

The next round's queue is derived from the previous round's confirmed queue:

```text
nextQueue = insertNewActions(removeContiguousRange(previousConfirmedQueue))
```

Rules:

1. Remove zero or one contiguous range of occurrences from the previous confirmed queue.
2. The removed range's total effective duration must satisfy `removedDurationTicks * 3 <= previousResolvedQueueDurationTicks`.
3. Preserve the relative order of all retained occurrences.
4. Insert newly selected occurrences at the beginning, end, or between retained occurrences.
5. Check the complete resulting queue against cooldown, stack, transition, and eligibility rules.

`previousResolvedQueueDurationTicks` is the previous round's authoritative resolved duration, including the reserved duration of any `EMPTY_SLOT` occurrences. It is not a Queue capacity. In round 1, there is no previous queue and no removal rule.

### 9.4 Queue validation

Before confirming, a player may request an authoritative preview validation of the current queue any number of times. Checking does not confirm or lock the queue, and its result is advisory because runtime state may change before an Action executes. Preview durations are estimates calculated with the player's AS at the time of the check; Battle events provide the authoritative runtime durations.

The server returns whether the queue is valid and all detected violations, including the relevant Action occurrence where possible:

```text
COOLDOWN_INVALID
QUEUE_TRANSITION_INVALID
```

Validation checks:

- Cooldown and consecutive stack rules are satisfied.
- The queue satisfies the previous-round contiguous-removal, removal-duration, and retained-order rules.
- Every occurrence references an eligible Action.

The optional queue check never evaluates QI, HP, or any other Action cost. All resource requirements are checked only against actual state at runtime.

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

Actions within each player's queue execute sequentially and are scheduled incrementally. Because Actions may have different durations and AS may change during Battle, the two players' Action boundaries do not need to align. There is no initiative and no alternating turn order.

Every Action occupies the interval:

```text
(start, end]
```

The start boundary is excluded and the end boundary is included. Therefore, a `RESOLVE_DURING_EXECUTION` defense remains active when a `RESOLVE_ON_END` attack resolves at the same `end` time.

All events scheduled for the same timeline point are resolved by deterministic server rules. Defensive Effects that are active at that point participate in damage resolution.

### 10.2 Runtime Action validation

When an occurrence reaches its runtime start position, the server snapshots AS, calculates and reserves its interval, then validates it using the actual runtime state. Runtime validation includes Action eligibility, cooldown, stack, configured cost, and any other execution requirements.

If an occurrence is invalid:

- It is removed from execution and treated as an `EMPTY_SLOT`.
- It pays no costs and produces no Action Effects.
- Its scheduled timeline interval remains reserved, so later Actions do not shift earlier.
- The opponent's timeline continues normally.

The optional queue check does not inspect costs. Only the runtime check determines whether an occurrence can pay its costs and execute.

### 10.3 Action costs

The server checks costs when each Action begins or resolves according to its configured cost timing. A `QI` cost may use both pools, but the server always spends `roundQi` before `reserveQi`.

- All costs for one Action occurrence are paid atomically.
- Before paying a QI cost, the server verifies `roundQi + reserveQi >= requiredQi`.
- It deducts from `roundQi` first, then deducts any remainder from `reserveQi`.
- If any required cost cannot be paid, no Qi or other cost belonging to that occurrence is paid.
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
ACTION_STARTED
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

For an `RESOLVE_DURING_EXECUTION` Action-Effect mapping, `periodIntervalTicks` may define a repeating cadence. It is null for non-periodic mappings and at least 1 when present. The first periodic resolution occurs after one complete interval, and subsequent resolutions occur every interval; an endpoint-aligned resolution occurs at the inclusive endpoint. The server uses the Action's already calculated execution interval and never resolves a periodic Effect after that endpoint.

### 10.7 Battle-end Effects

After all timeline Actions finish and no player has reached 0 HP, the server resolves `BATTLE_END` Effects in this order:

1. Resolve Ending Effects in deterministic application order.
2. Resolve Bleed stacks.
3. Record relevant resolved Bleed stacks for follow-up Effects.
4. Resolve Ascendance follow-up damage.
5. Remove round-scoped Gain Effects.
6. Evaluate match-end conditions, then start the next-round transition when no terminal condition exists.

If HP reaches 0 during Battle, the existing immediate match-end policy applies: remaining timeline Actions and `BATTLE_END` Effects do not resolve.

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
