# SwordVerse — Database Schema Design

## 1. Document Summary

This document defines the relational database schema for **SwordVerse**, an online 1v1 round-based auto-combat game.

The schema is designed for a **server-authoritative** gameplay model. Clients submit player intent, while the server owns official match state, stat updates, resource changes, Action resolution, Effect processing, phase transitions, and match results.

The database stores two main types of data:

1. **Static Game Data**
   Static Game Data defines configurable content used by the game engine, such as Sects, Actions, Action levels, and Effect definitions. These records describe what players can select, what each Action costs, and which Effects the server should resolve.

2. **Runtime Match Data**
   Runtime Match Data stores the current state of rooms and matches, including selected Sects, player stats, player resources, Action slots, Action Queues, active Effects, passive state, connection state, and final match results.

The schema uses a **data-driven Action and Effect model**. Actions do not hardcode all behavior directly into the schema. Instead, Actions are connected to level-specific Effect definitions. The Spring Boot game engine reads these definitions and applies the correct server-side behavior through application logic.

This design keeps the database focused on configuration and runtime state, while gameplay rules remain in the server application layer.

---

## 2. Design Goals

### 2.1 Keep the Server Authoritative

SwordVerse uses a server-authoritative model.

Clients can select Sects, submit Ascension choices, confirm Action Queues, and send commands such as surrender. However, clients do not calculate official damage, healing, resource changes, Effect resolution, phase transitions, or match results.

The server is responsible for:

```text
- Validating player commands
- Resolving phase transitions
- Applying stat and resource changes
- Resolving Action Queue ticks
- Applying active Effects and Passive Techniques
- Producing match results
- Publishing authoritative state to clients
```

---

### 2.2 Separate Static Game Data from Runtime Match State

Static Game Data and Runtime Match Data are stored separately.

Static Game Data includes:

```text
- Sects
- Actions
- Action levels
- Effect definitions
- Action-to-Effect mappings
```

Runtime Match Data includes:

```text
- Rooms
- Matches
- Match players
- Match player Action slots
- Action Queue entries
- Active Effects
- Passive Technique state
- Match results
```

This separation keeps configurable game content independent from active match state.

---

### 2.3 Use Sects as the Core Player Style

The game uses the term **Sect** to represent a player’s martial school, faction, or combat style.

Each player selects:

```text
- Two distinct Basic Actions from Slash, Defend, and Shield
- One Main Sect
- One Support Sect
- One Support Action from the Support Sect
```

The Main Sect provides the player’s base stats. The Support Sect provides additional stat bonuses and a selected Support Action. Qi generation and Qi limits are global runtime rules and are not granted by either Sect.

---

### 2.4 Use a Unified Card-Based Action Model

The schema uses a single `actions` table for all cards and player abilities. An Action is the server-side representation of a card.

This includes:

```text
- Basic Actions (`SLASH`, `DEFEND`, and `SHIELD`)
- Active Sect cards
- Passive Sect cards
- Ultimate cards
```

A player holds exactly six Action slots during a match:

```text
- BASIC_1
- BASIC_2
- MAIN_1
- MAIN_2
- MAIN_3
- SUPPORT
```

The two Basic slots contain two distinct Actions selected from Slash, Defend, and Shield. The three Main slots are selected from the Main Sect. The Support slot is selected from the Support Sect, may not duplicate a Main slot, and may not contain an Ultimate Action. The Support Sect may be the same as the Main Sect.

Only Actions with `activation_type = 'ACTIVE'` are valid executable Action Queue occurrences. Confirmation does not reject invalid submissions; runtime converts invalid occurrences to empty slots. The same active Action slot may be submitted multiple times subject to its stack and cooldown rules. Passive Actions remain in the player's hand and are activated by server-observed gameplay events.

Action availability is represented by level:

```text
current_level = 0  -> locked
current_level >= 1 -> unlocked
```

Selected Basic Actions start at level 1. Actions and upgradeable Stats have a maximum level of 3. Ascension may upgrade Actions and only the `HP`, `STR`, `DEF`, and `AS` Stats. Each Stat upgrade permanently increases the unmodified player Stat by 10%; upgrades are cumulative and temporary Effect modifiers are applied separately. `HP`, `STR`, and `DEF` increases are rounded up to an integer; `AS` increases are rounded up to two decimal places.

---

### 2.5 Support Data-Driven Effects

Action behavior is configured through `action_costs`, `action_triggers`, `effect_definitions`, `effect_components`, and `action_effects`.

An Action level can trigger one or more Effects in sequence. Example Effects include:

```text
- Damage
- Healing
- Resource gain
- Resource drain
- Status application
- Status removal
- Stat modification
- Max stat modification
```

The database stores typed configuration. The Spring Boot game engine compiles that configuration into immutable runtime definitions and resolves behavior through server-side trigger evaluators, Effect processors, and explicitly registered custom handlers.

Published static game data is immutable while a match is active. SwordVerse does not maintain content versions. Updating static game data requires all active matches to be ended or cancelled before the update is applied.

---

### 2.6 Store Current Runtime State Without Full Replay History

The current schema is designed for active gameplay and simple match summaries, not full replay.

The database stores:

```text
- Current player stats and resources
- Current Action levels
- Current Action Queue entries
- Current active Effects
- Current Passive Technique state
- Final match result
```

The schema does not store full Battle execution history. If a lightweight readable log is needed, `matches.log_text` can store a simple text summary of match events.

---

### 2.7 Keep Gameplay Logic in the Spring Boot Application Layer

The database should not execute gameplay rules through triggers or stored procedures.

The database stores:

```text
- Static configuration
- Runtime state
- Final results
```

The Spring Boot application layer handles:

```text
- Command validation
- Resource validation
- Action resolution
- Effect processing
- Passive trigger checks
- Timeout behavior
- Disconnect handling
- Match result creation
```

This keeps gameplay behavior testable, maintainable, and easier to evolve.

---

## 3. High-Level Schema Organization

```text
Authentication
├── users
├── sessions
└── refresh_tokens

Static Game Data
├── sects
├── actions
├── sect_actions
├── action_levels
├── action_costs
├── action_triggers
├── effect_definitions
├── effect_components
└── action_effects

Room and Match Runtime
├── rooms
├── matches
├── match_players
├── match_player_action_slots
├── action_queue_entries
├── active_effects
├── match_player_passive_states
└── match_results
```

---

## Authentication Tables

This section defines the database tables used for authentication, session management, and refresh token rotation.

The authentication model uses short-lived JWT access tokens together with server-side sessions and rotating opaque refresh tokens. The JWT contains `userId` and `sessionId`, but the session state is still validated against the database. The raw refresh token is delivered only in a host-only `HttpOnly` cookie; only its hash is stored. This allows the server to revoke sessions, support logout, and invalidate refresh tokens securely.

A user may own multiple active authentication sessions. This is an intentional product policy and must not be constrained by a unique active-session rule. Authentication sessions do not represent WebSocket presence or gameplay ownership. Exclusive gameplay is enforced separately through the gameplay lease architecture.

---

### `users`

The `users` table stores registered user accounts.

A user represents a player account that can log in, create rooms, join rooms, and participate in matches.

#### Columns

| Column | Type | Key | Nullable | Description |
|---|---|---:|---:|---|
| `id` | `uuid` | PK | No | Unique identifier of the user. |
| `username` | `varchar(50)` | Unique | No | Public username used for login and display. |
| `password_hash` | `varchar(255)` |  | No | Hashed password. The raw password must never be stored. |
| `display_name` | `varchar(100)` |  | Yes | Optional display name shown in the UI. If null, the client may use `username`. |
| `created_at` | `timestamptz` |  | No | Timestamp when the user account was created. |
| `updated_at` | `timestamptz` |  | No | Timestamp when the user account was last updated. |

#### Primary Key

```sql
PRIMARY KEY (id)
```

#### Constraints

```sql
UNIQUE (username)
```

```sql
CHECK (length(username) >= 3)
```

#### Relationships

| Relationship | Description |
|---|---|
| `users.id` → `sessions.user_id` | One user can have multiple login sessions. |
| `users.id` → `rooms.player_a_id` | A user can be Player A in a room. |
| `users.id` → `rooms.player_b_id` | A user can be Player B in a room. |
| `users.id` → `match_players.user_id` | A user can participate in multiple matches. |

#### Notes

- `password_hash` should be generated by a secure password hashing algorithm such as BCrypt, Argon2, or another production-grade password hashing function.
- `username` is used as the stable login identifier.
- `display_name` is optional and can be changed without affecting login identity.
- User authentication status should not be inferred from this table alone. Active login state is tracked by `sessions`.

---

### `sessions`

The `sessions` table stores server-side login sessions.

Each successful login creates one session. Existing sessions belonging to the same user remain valid. The access token contains the session ID, and the server checks this table to verify that the referenced session is still active.

This table allows the server to revoke a session even if the JWT access token has not expired yet.

#### Columns

| Column | Type | Key | Nullable | Description |
|---|---|---:|---:|---|
| `id` | `uuid` | PK | No | Unique identifier of the session. This value is included in the JWT access token as `sessionId`. |
| `user_id` | `uuid` | FK | No | User who owns this session. References `users.id`. |
| `status` | `varchar(20)` |  | No | Current session status. |
| `access_token_expires_at` | `timestamptz` |  | No | Expiration time of the latest access token issued for this session. |
| `refresh_token_expires_at` | `timestamptz` |  | No | Expiration time of the refresh-token lifecycle for this session. |
| `revoked_at` | `timestamptz` |  | Yes | Timestamp when the session was revoked. Null means the session has not been revoked. |
| `created_at` | `timestamptz` |  | No | Timestamp when the session was created. |
| `updated_at` | `timestamptz` |  | No | Timestamp when the session was last updated. |

#### Primary Key

```sql
PRIMARY KEY (id)
```

#### Foreign Keys

```sql
FOREIGN KEY (user_id) REFERENCES users(id)
```

#### Constraints

```sql
CHECK (status IN ('ACTIVE', 'REVOKED', 'EXPIRED'))
```

```sql
CHECK (access_token_expires_at <= refresh_token_expires_at)
```

#### Relationships

| Relationship | Description |
|---|---|
| `sessions.user_id` → `users.id` | Each session belongs to one user. |
| `sessions.id` → `refresh_tokens.session_id` | One session can have multiple refresh token records over time due to token rotation. |

#### Session Validation Rule

When the server receives an authenticated request, it validates the access token using the following process:

```text
1. Verify the JWT signature.
2. Check the JWT `exp` claim.
3. Extract `userId` and `sessionId` from the JWT payload (`sub` also identifies the user).
4. Find the session by `sessions.id`.
5. Ensure the session belongs to the same user.
6. Ensure `sessions.status = 'ACTIVE'`.
7. Ensure `sessions.revoked_at IS NULL`.
8. Continue request processing.
```

#### Notes

- The client cannot safely modify the JWT expiration time because the JWT signature would become invalid.
- The `sessions` table is still useful because it allows server-side revocation.
- A logout operation should set `status = 'REVOKED'` and `revoked_at = now()`.
- `session_id` should remain stable during refresh token rotation.
- Refreshing a token should create a new refresh token record but should not create a new session.
- Multiple sessions for the same user may have `ACTIVE` status concurrently.
- Session state must not be used to infer whether the user owns a gameplay lease or currently has an open WebSocket connection.

---

### `refresh_tokens`

The `refresh_tokens` table stores refresh tokens for session renewal.

Refresh tokens are used to issue new access tokens after the current access token expires. For security, the raw refresh token must not be stored. Only a hashed version of the token is stored.

This table supports refresh token rotation. Each time a refresh token is used successfully, the old refresh token is revoked and a new refresh token is created for the same session.

#### Columns

| Column | Type | Key | Nullable | Description |
|---|---|---:|---:|---|
| `id` | `uuid` | PK | No | Unique identifier of the refresh token record. |
| `session_id` | `uuid` | FK | No | Session that owns this refresh token. References `sessions.id`. |
| `token_hash` | `varchar(255)` | Unique | No | Hashed refresh token value. The raw token must never be stored. |
| `expires_at` | `timestamptz` |  | No | Timestamp when this refresh token expires. |
| `revoked_at` | `timestamptz` |  | Yes | Timestamp when this refresh token was revoked. Null means it has not been revoked. |
| `created_at` | `timestamptz` |  | No | Timestamp when this refresh token record was created. |

#### Primary Key

```sql
PRIMARY KEY (id)
```

#### Foreign Keys

```sql
FOREIGN KEY (session_id) REFERENCES sessions(id)
```

#### Constraints

```sql
UNIQUE (token_hash)
```

#### Indexes

```sql
CREATE INDEX idx_refresh_tokens_session_id
ON refresh_tokens(session_id);
```

```sql
CREATE INDEX idx_refresh_tokens_expires_at
ON refresh_tokens(expires_at);
```

#### Relationships

| Relationship | Description |
|---|---|
| `refresh_tokens.session_id` → `sessions.id` | Each refresh token belongs to one session. |
| `sessions.id` → `refresh_tokens.session_id` | One session can have multiple refresh token records over time. Usually only one should be active at a time. |

#### Refresh Token Rotation Rule

When a refresh token is used successfully:

```text
1. Hash the received refresh token.
2. Find the matching `refresh_tokens.token_hash`.
3. Ensure `refresh_tokens.revoked_at IS NULL`.
4. Ensure `refresh_tokens.expires_at > now()`.
5. Load the related session.
6. Ensure the session is active and not revoked.
7. Revoke the old refresh token by setting `revoked_at = now()`.
8. Create a new refresh token record for the same session.
9. Issue a new access token and a new refresh token.
```

#### Notes

- Only the hash of the refresh token should be stored.
- A refresh token should be single-use when rotation is enabled.
- Reusing a revoked refresh token may indicate token theft. The server may revoke the entire session in that case.
- `session_id` should not change during normal token refresh.
- Access tokens are validated through JWT signature and session status.
- Refresh tokens are validated through database lookup and token hash comparison.

---

## Static Game Data Tables

This section defines static game data used by the game engine before and during a match.

Static game data includes Sects, Actions, Action levels, and Effect definitions. These records define what players can select, what Actions cost, and what Effects are resolved by the server.

Static data should be treated as configuration data. Runtime match state is stored separately in match-related tables.

---

### `sects`

The `sects` table stores all Sects available in the game.

A Sect represents a player’s martial school, faction, or combat style. Each player selects one Main Sect and one Support Sect before the match begins.

A Sect provides:

```text
- Main Sect base stats
- Support Sect bonus stats
- Available Actions
- Display information for the UI
```

The term `Sect` replaces the previous term `Order`.

#### Columns

| Column | Type | Key | Nullable | Description |
|---|---|---:|---:|---|
| `id` | `uuid` | PK | No | Unique identifier of the Sect. |
| `sect_type` | `varchar(50)` | Unique | No | Stable application-level identifier of the Sect, such as `HEAVEN_SWORD`. |
| `name` | `varchar(100)` |  | No | Display name of the Sect. |
| `description` | `text` |  | Yes | Description shown to the player. |
| `keywords` | `jsonb` |  | Yes | Short descriptive tags used by the UI, such as `["balanced", "fast", "defensive"]`. |
| `main_base_str` | `int` |  | No | STR value granted when this Sect is selected as the Main Sect. |
| `main_base_hp` | `int` |  | No | HP max value granted when this Sect is selected as the Main Sect. |
| `main_base_def` | `int` |  | No | DEF value granted when this Sect is selected as the Main Sect. |
| `main_base_as` | `numeric(10,2)` |  | No | AS value granted when this Sect is selected as the Main Sect. |
| `support_bonus_str` | `int` |  | No | STR bonus granted when this Sect is selected as the Support Sect. |
| `support_bonus_hp` | `int` |  | No | HP max bonus granted when this Sect is selected as the Support Sect. |
| `support_bonus_def` | `int` |  | No | DEF bonus granted when this Sect is selected as the Support Sect. |
| `support_bonus_as` | `numeric(10,2)` |  | No | AS bonus granted when this Sect is selected as the Support Sect. |
| `created_at` | `timestamptz` |  | No | Timestamp when the Sect record was created. |
| `updated_at` | `timestamptz` |  | No | Timestamp when the Sect record was last updated. |

#### Primary Key

```sql
PRIMARY KEY (id)
```

#### Constraints

```sql
UNIQUE (sect_type)
```

```sql
CHECK (main_base_str >= 0)
CHECK (main_base_hp >= 0)
CHECK (main_base_def >= 0)
CHECK (main_base_as >= 0)
```

```sql
CHECK (support_bonus_str >= 0)
CHECK (support_bonus_hp >= 0)
CHECK (support_bonus_def >= 0)
CHECK (support_bonus_as >= 0)
```

#### Relationships

| Relationship | Description |
|---|---|
| `sects.id` → `sect_actions.sect_id` | One Sect can grant multiple Actions. |
| `sects.id` → `match_players.main_sect_id` | A match player selects one Main Sect. |
| `sects.id` → `match_players.support_sect_id` | A match player selects one Support Sect. |

#### Notes

- `sect_type` should match an application enum or constant, for example `SectType.HEAVEN_SWORD`.
- The database stores `sect_type` as a readable string instead of a PostgreSQL enum to keep future content updates easier.
- Main Sect stats define the player’s starting combat identity.
- Support Sect stats are added as bonuses when selected as the Support Sect.
- Qi generation and global Qi limits are not Sect data.
- Runtime stat changes during a match are stored in `match_players`, not in this table.

---

### `actions`

The `actions` table stores all Actions available in the game.

An Action is any selectable or triggerable game ability. This includes the three Basic Actions and active or passive Sect Techniques.

Examples:

```text
- Slash
- Defend
- Shield
- Quick Slash
- Heaven Guard
- Blood Detonation
- Sword Instinct
```

The game uses one unified `actions` table instead of separate `basic_actions` and `techniques` tables.

#### Columns

| Column | Type | Key | Nullable | Description |
|---|---|---:|---:|---|
| `id` | `uuid` | PK | No | Unique identifier of the Action. |
| `action_key` | `varchar(100)` | Unique | No | Stable application identifier, such as `HEAVENLY_EXECUTION`. |
| `action_source` | `varchar(20)` |  | No | Source category of the card. |
| `activation_type` | `varchar(20)` |  | No | Defines whether the card is actively queued or passively triggered. |
| `resolution_types` | `varchar(40)[]` |  | Yes | One to three distinct timing types for an Active Action. Null for Passive Actions. |
| `is_ultimate` | `boolean` |  | No | Indicates whether this Sect card is an Ultimate Action. |
| `name` | `varchar(100)` |  | No | Display name of the Action. |
| `description` | `text` |  | Yes | Description shown to the player. |
| `behavior_handler` | `varchar(100)` |  | Yes | Whitelisted Java handler key for exceptional mechanics that cannot be expressed by standard components. |
| `created_at` | `timestamptz` |  | No | Timestamp when the Action record was created. |
| `updated_at` | `timestamptz` |  | No | Timestamp when the Action record was last updated. |

#### Primary Key

```sql
PRIMARY KEY (id)
```

#### Constraints

```sql
UNIQUE (action_key)
```

```sql
CHECK (action_source IN ('BASIC', 'SECT_TECHNIQUE'))
```

```sql
CHECK (activation_type IN ('ACTIVE', 'PASSIVE'))
```

```sql
CHECK (
  (
    activation_type = 'ACTIVE'
    AND resolution_types IS NOT NULL
    AND cardinality(resolution_types) BETWEEN 1 AND 3
    AND resolution_types <@ ARRAY['RESOLVE_ON_START', 'RESOLVE_DURING_EXECUTION', 'RESOLVE_ON_END']::varchar[]
  )
  OR
  (activation_type = 'PASSIVE' AND resolution_types IS NULL)
)
```

```sql
CHECK (is_ultimate = false OR action_source = 'SECT_TECHNIQUE')
```

```sql
CHECK (is_ultimate = false OR activation_type = 'ACTIVE')
```

#### Relationships

| Relationship | Description |
|---|---|
| `actions.id` → `sect_actions.action_id` | An Action can be granted by one or more Sects. |
| `actions.id` → `action_levels.action_id` | An Action can have multiple level definitions. |
| `actions.id` → `match_player_action_slots.action_id` | A match player can own this Action in one of their match Action slots. |

#### Notes

- `action_source`, `activation_type`, and `resolution_types` are separate classifications. An Ultimate is always `ACTIVE`.
- An Active Action has one to three distinct resolution types. The service layer rejects duplicate values and ensures every Action Effect mapping uses a type declared by its Action.
- Only `ACTIVE` Actions may be added to the Action Queue.
- `PASSIVE` Actions are evaluated from gameplay events and cannot be queued.
- The three Basic Action records use stable keys `SLASH`, `DEFEND`, and `SHIELD`. They are not connected to Sects.
- `RESOLVE_ON_START` resolves at `start_tick + 1`; `RESOLVE_DURING_EXECUTION` remains active throughout `(start_tick, end_tick]`; and `RESOLVE_ON_END` resolves at `end_tick`.
- `action_key` is stable across environments and maps cleanly to Java constants, logs, fixtures, and frontend assets.
- `behavior_handler` must map to a Java enum and registered Spring handler. A database value must never contain a Java class name or executable script.
- Standard Actions should use data-driven triggers and Effect components. `behavior_handler` is reserved for exceptional mechanics.

---

### `sect_actions`

The `sect_actions` table connects Sects with Actions.

This table defines which Actions are granted by each Sect.

A Sect can grant multiple Actions, and the same Action may be shared by multiple Sects if needed.

#### Columns

| Column | Type | Key | Nullable | Description |
|---|---|---:|---:|---|
| `id` | `uuid` | PK | No | Unique identifier of the Sect-Action mapping. |
| `sect_id` | `uuid` | FK | No | Sect that grants the Action. References `sects.id`. |
| `action_id` | `uuid` | FK | No | Action granted by the Sect. References `actions.id`. |
| `display_order` | `int` |  | No | Ordering used when displaying Actions for this Sect. |

#### Primary Key

```sql
PRIMARY KEY (id)
```

#### Foreign Keys

```sql
FOREIGN KEY (sect_id) REFERENCES sects(id)
```

```sql
FOREIGN KEY (action_id) REFERENCES actions(id)
```

#### Constraints

```sql
UNIQUE (sect_id, action_id)
```

```sql
CHECK (display_order >= 0)
```

#### Indexes

```sql
CREATE INDEX idx_sect_actions_sect_id
ON sect_actions(sect_id);
```

```sql
CREATE INDEX idx_sect_actions_action_id
ON sect_actions(action_id);
```

#### Relationships

| Relationship | Description |
|---|---|
| `sect_actions.sect_id` → `sects.id` | Each row belongs to one Sect. |
| `sect_actions.action_id` → `actions.id` | Each row references one Action. |

#### Notes

- A player selects exactly three distinct Actions from the Main Sect.
- A player selects exactly one Action from the Support Sect.
- A Support Action must not duplicate a selected Main Action and must have `actions.is_ultimate = false`.
- Main and Support selections may contain active or passive Actions.
- These cross-table selection rules are validated transactionally by the Spring Boot service layer.
- Basic Actions do not need to be connected to Sects. The service layer selects exactly two distinct Basic Actions from `SLASH`, `DEFEND`, and `SHIELD` for each player.

---

### `action_levels`

The `action_levels` table stores level-specific configuration for each Action.

Each Action can have one or more levels. The player’s runtime Action level is stored in `match_player_action_slots.current_level`.

Runtime rule:

```text
current_level = 0 means locked.
current_level >= 1 means unlocked.
Basic Actions start at level 1.
```

The `action_levels` table only stores actual configured levels, usually level 1 and above. Level 0 does not need a row.

#### Columns

| Column | Type | Key | Nullable | Description |
|---|---|---:|---:|---|
| `id` | `uuid` | PK | No | Unique identifier of the Action level record. |
| `action_id` | `uuid` | FK | No | Action this level belongs to. References `actions.id`. |
| `level` | `int` |  | No | Level number of the Action, from 1 through 3. |
| `learning_point_cost` | `int` |  | No | LP cost required to learn or upgrade to this level. |
| `duration_type` | `varchar(20)` |  | Yes | `AS_SCALED`, `FIXED`, or `CONTROLLED` for an Active Action. Null for Passive Actions. |
| `duration_ticks` | `int` |  | Yes | Configured duration in 0.1-second ticks for a `FIXED` Action. Null otherwise. |
| `duration_config` | `jsonb` |  | Yes | Typed, validated configuration for a `CONTROLLED` Action. Null otherwise. |
| `cooldown_ticks` | `int` |  | Yes | Configured base cooldown after the Action's consecutive stack chain, in 0.1-second ticks. Basic Actions use `0`. |
| `max_consecutive_stacks` | `int` |  | Yes | Maximum immediately consecutive uses before cooldown applies. Null means unlimited for a Basic Action. |
| `created_at` | `timestamptz` |  | No | Timestamp when the Action level record was created. |
| `updated_at` | `timestamptz` |  | No | Timestamp when the Action level record was last updated. |

#### Primary Key

```sql
PRIMARY KEY (id)
```

#### Foreign Keys

```sql
FOREIGN KEY (action_id) REFERENCES actions(id)
```

#### Constraints

```sql
UNIQUE (action_id, level)
```

```sql
CHECK (level BETWEEN 1 AND 3)
```

```sql
CHECK (learning_point_cost >= 0)
CHECK (duration_type IS NULL OR duration_type IN ('AS_SCALED', 'FIXED', 'CONTROLLED'))
CHECK (duration_ticks IS NULL OR duration_ticks >= 1)
CHECK (duration_config IS NULL OR jsonb_typeof(duration_config) = 'object')
CHECK (cooldown_ticks IS NULL OR cooldown_ticks >= 0)
CHECK (max_consecutive_stacks IS NULL OR max_consecutive_stacks >= 1)
```

#### Indexes

```sql
CREATE INDEX idx_action_levels_action_id
ON action_levels(action_id);
```

#### Relationships

| Relationship | Description |
|---|---|
| `action_levels.action_id` → `actions.id` | Each Action can have multiple configured levels. |
| `action_levels.id` → `action_costs.action_level_id` | One Action level can define zero or more resource costs. |
| `action_levels.id` → `action_triggers.action_level_id` | A passive Action level can define one or more activation triggers. |
| `action_levels.id` → `action_effects.action_level_id` | One Action level can trigger multiple Effects. |

#### Notes

- Resource costs are defined in `action_costs`.
- `learning_point_cost` is used during the Ascension Phase.
- Active Sect Technique levels must define `duration_type`, `cooldown_ticks`, and `max_consecutive_stacks`; Passive Action levels leave all duration fields null. Basic Action levels use `cooldown_ticks = 0` and `max_consecutive_stacks = null` for unlimited consecutive use. This cross-table rule is enforced by the service layer.
- One tick is exactly 0.1 second.
- Duration combinations are enforced by the service layer: `ACTIVE + AS_SCALED` requires `duration_type = AS_SCALED` with `duration_ticks` and `duration_config` null; `ACTIVE + FIXED` requires `duration_type = FIXED`, `duration_ticks >= 1`, and `duration_config` null; `ACTIVE + CONTROLLED` requires `duration_type = CONTROLLED`, `duration_ticks` null, and validated `duration_config` or a whitelisted registered handler; `PASSIVE` requires all duration fields null.
- `duration_config` contains only fields declared by its typed duration DTO. It must never contain executable expressions, scripts, SQL, Java class names, or arbitrary code.
- When an Active Action occurrence reaches its runtime start position, the server snapshots strictly positive `current_as` as `as_snapshot` and calculates duration from `duration_type`: `AS_SCALED` uses `max(1, ceil(10 / as_snapshot))`; `FIXED` uses `duration_ticks`; and `CONTROLLED` uses typed validated configuration or a whitelisted registered handler. The server uses the result to schedule the `(start, end]` interval and resolution timings. AS does not modify cooldown. `SLASH`, `DEFEND`, and `SHIELD` have no cooldown; Main and Support Sect Techniques use their configured cooldown unchanged.
- For an Action with a configured cooldown, cooldown begins at the end of the final occurrence in a gapless stack chain. Each occurrence pays its own configured costs.
- Dynamic Effects that modify another Action's duration or cooldown are intentionally not supported in the next version; the tick columns leave room for that future extension.
- If a player has `current_level = 0`, the Action is locked and cannot be used.
- If a player has `current_level = 1`, the server reads the row where `level = 1`.
- Passive Techniques may still use `action_levels` if they can be learned or upgraded.

---

### `action_costs`

The `action_costs` table stores typed resource costs for an Action level.

An Action level may have no cost, one cost, or multiple costs. The game engine validates and pays every cost before resolving the Action's Effects.

#### Columns

| Column | Type | Key | Nullable | Description |
|---|---|---:|---:|---|
| `id` | `uuid` | PK | No | Unique identifier of the Action cost. |
| `action_level_id` | `uuid` | FK | No | Action level that owns the cost. References `action_levels.id`. |
| `resource_type` | `varchar(10)` |  | No | Resource consumed by the Action. |
| `payment_timing` | `varchar(20)` |  | No | Whether the cost is checked and paid at execution start or completion. |
| `calculation_type` | `varchar(30)` |  | No | Defines how the cost amount is calculated. |
| `flat_value` | `numeric(10,2)` |  | Yes | Fixed amount used by `FLAT`. |
| `percent_value` | `numeric(10,4)` |  | Yes | Decimal percentage used by percentage calculations. |
| `sequence_order` | `int` |  | No | Deterministic payment order when multiple costs exist. |

#### Keys and Constraints

```sql
PRIMARY KEY (id)
```

```sql
FOREIGN KEY (action_level_id) REFERENCES action_levels(id)
```

```sql
UNIQUE (action_level_id, sequence_order)
```

```sql
CHECK (resource_type IN ('HP', 'QI'))
```

```sql
CHECK (payment_timing IN ('ON_EXECUTION_START', 'ON_ACTION_RESOLVE'))
```

```sql
CHECK (calculation_type IN ('FLAT', 'PERCENT_CURRENT', 'PERCENT_MAX'))
```

```sql
CHECK (
  (calculation_type = 'FLAT' AND flat_value IS NOT NULL AND percent_value IS NULL)
  OR
  (calculation_type <> 'FLAT' AND flat_value IS NULL AND percent_value IS NOT NULL)
)
```

```sql
CHECK (flat_value IS NULL OR flat_value >= 0)
CHECK (percent_value IS NULL OR percent_value >= 0)
CHECK (sequence_order >= 0)
```

#### Notes

- Java maps `resource_type` and `calculation_type` to enums.
- Costs are paid atomically. If any required cost cannot be paid, no cost is paid and the Action fails.
- A `QI` cost verifies `round_qi + reserve_qi >= required_qi`, spends `round_qi` first, then spends any remainder from `reserve_qi`.
- Runtime validation uses `payment_timing`. Advisory queue validation never evaluates or simulates Action costs.
- The service layer defines whether HP costs are allowed to reduce HP to zero.

---

### `action_triggers`

The `action_triggers` table defines when a Passive Action level activates.

Triggers observe typed gameplay events emitted by the Spring Boot battle engine. Trigger configuration is data; event production, comparison, accumulation, loop protection, and Effect execution remain type-safe Java behavior.

#### Columns

| Column | Type | Key | Nullable | Description |
|---|---|---:|---:|---|
| `id` | `uuid` | PK | No | Unique trigger identifier. |
| `action_level_id` | `uuid` | FK | No | Passive Action level owning the trigger. References `action_levels.id`. |
| `event_type` | `varchar(50)` |  | No | Gameplay event observed by the trigger. |
| `subject_selector` | `varchar(20)` |  | No | Actor whose event is evaluated. |
| `metric_type` | `varchar(40)` |  | No | Typed event metric evaluated by the trigger. |
| `comparison_operator` | `varchar(10)` |  | No | Comparison applied to the metric. |
| `threshold_value` | `numeric(10,2)` |  | No | Required threshold. |
| `accumulation_type` | `varchar(30)` |  | No | Whether evaluation uses one event or accumulated progress. |
| `reset_policy` | `varchar(30)` |  | No | Defines when accumulated progress resets. |
| `filter_config` | `jsonb` |  | Yes | Optional discriminated filter configuration validated against the selected event type. |
| `sequence_order` | `int` |  | No | Deterministic order when an Action level defines multiple triggers. |

#### Keys and Constraints

```sql
PRIMARY KEY (id)
```

```sql
FOREIGN KEY (action_level_id) REFERENCES action_levels(id)
```

```sql
UNIQUE (action_level_id, sequence_order)
```

```sql
CHECK (
  event_type IN (
    'ACTION_HIT',
    'ACTION_STARTED',
    'DAMAGE_DEALT',
    'DAMAGE_RECEIVED',
    'ATTRIBUTE_CHANGED',
    'EFFECT_APPLIED',
    'EFFECT_REMOVED',
    'TIMELINE_TICK_STARTED',
    'TIMELINE_TICK_ENDED',
    'RENEWAL_STARTED',
    'RENEWAL_ENDED'
  )
)
```

```sql
CHECK (subject_selector IN ('SELF', 'OPPONENT'))
CHECK (metric_type IN ('EVENT_COUNT', 'EVENT_VALUE', 'CURRENT_VALUE', 'CURRENT_PERCENT'))
CHECK (comparison_operator IN ('EQ', 'GTE', 'LTE'))
CHECK (accumulation_type IN ('SINGLE_EVENT', 'ACCUMULATE'))
CHECK (reset_policy IN ('NONE', 'ON_TRIGGER', 'END_OF_TICK', 'END_OF_ROUND'))
CHECK (threshold_value >= 0)
CHECK (sequence_order >= 0)
```

#### Notes

- `filter_config` is not arbitrary untyped state. Each `event_type` maps to a dedicated Java DTO and validator.
- A three-hit passive uses `ACTION_HIT`, `EVENT_COUNT`, `ACCUMULATE`, threshold `3`, and `ON_TRIGGER`.
- A passive triggered by losing at least 2 HP from one hit uses `DAMAGE_RECEIVED`, `EVENT_VALUE`, `SINGLE_EVENT`, and threshold `2`.
- Predation uses `ACTION_STARTED` with a validated Shadow Sword Action filter. The battle engine resolves matching Passive triggers and applies their Shade before the Active Action's `RESOLVE_ON_START` Effects can consume it.
- The engine enforces maximum trigger depth and maximum events per tick to prevent passive loops.

---

### `effect_definitions`

The `effect_definitions` table stores reusable, immutable Effect assets.

An Effect defines lifecycle and stacking behavior. Its typed operations are stored in `effect_components`.

Examples:

```text
- Deal damage
- Heal HP
- Restore Round Qi
- Generate Reserve Qi
- Apply Bleed
- Apply Blind
- Remove a status effect
```

An Action can have multiple Effects through `action_effects`.

#### Columns

| Column | Type | Key | Nullable | Description |
|---|---|---:|---:|---|
| `id` | `uuid` | PK | No | Unique identifier of the Effect definition. |
| `effect_key` | `varchar(100)` | Unique | No | Stable application identifier, such as `BLEED`. |
| `name` | `varchar(100)` |  | No | Display or diagnostic name. |
| `description` | `text` |  | Yes | Player-facing description when the Effect is visible. |
| `duration_type` | `varchar(20)` |  | No | Lifecycle category of the Effect. |
| `duration_ticks` | `int` |  | Yes | Lifetime in 0.1-second Battle ticks for a finite tick-based Effect. |
| `duration_rounds` | `int` |  | Yes | Lifetime in rounds for a finite round-based Effect. |
| `max_charges` | `int` |  | Yes | Maximum consumable charges. Null for Effects that are not charge-based. |
| `period_timing` | `varchar(30)` |  | Yes | Timing at which a periodic Effect executes. |
| `stacking_policy` | `varchar(30)` |  | No | Rule used when the same Effect is applied again. |
| `max_stacks` | `int` |  | Yes | Maximum number of stacks allowed. Required when `stacking_policy = 'STACK'`. |
| `behavior_handler` | `varchar(100)` |  | Yes | Whitelisted Java handler key for exceptional lifecycle behavior. |
| `created_at` | `timestamptz` |  | No | Timestamp when the Effect definition was created. |
| `updated_at` | `timestamptz` |  | No | Timestamp when the Effect definition was last updated. |

#### Primary Key

```sql
PRIMARY KEY (id)
```

#### Constraints

```sql
UNIQUE (effect_key)
```

```sql
CHECK (duration_type IN ('INSTANT', 'FINITE_TICKS', 'FINITE_ROUNDS', 'INFINITE'))
```

```sql
CHECK (
  (duration_type = 'FINITE_TICKS' AND duration_ticks IS NOT NULL AND duration_ticks >= 1 AND duration_rounds IS NULL)
  OR
  (duration_type = 'FINITE_ROUNDS' AND duration_rounds IS NOT NULL AND duration_rounds >= 1 AND duration_ticks IS NULL)
  OR
  (duration_type IN ('INSTANT', 'INFINITE') AND duration_ticks IS NULL AND duration_rounds IS NULL)
)
```

```sql
CHECK (max_charges IS NULL OR max_charges >= 1)
```

```sql
CHECK (
  period_timing IS NULL
  OR period_timing IN ('TIMELINE_TICK_START', 'TIMELINE_TICK_END', 'RENEWAL_START', 'RENEWAL_END', 'BATTLE_END')
)
```

```sql
CHECK (stacking_policy IN ('NONE', 'REFRESH', 'STACK', 'REPLACE', 'STRONGEST_WINS'))
```

```sql
CHECK (max_stacks IS NULL OR max_stacks >= 1)
```

```sql
CHECK (
  stacking_policy <> 'STACK'
  OR
  max_stacks IS NOT NULL
)
```

#### Relationships

| Relationship | Description |
|---|---|
| `effect_definitions.id` → `action_effects.effect_definition_id` | One Effect can be attached to multiple Action levels. |
| `effect_definitions.id` → `active_effects.effect_definition_id` | Active runtime Effects reference their static Effect definition. |

#### Notes

- Instant Effects execute immediately and do not create `active_effects` rows.
- Finite and infinite Effects create runtime instances in `active_effects`.
- `BATTLE_END` Effects execute after all timeline Actions and before round-scoped Gain Effects are removed, unless the Battle ended immediately because HP reached 0.
- Effect operations belong to `effect_components`; passive activation conditions belong to `action_triggers`.
- `behavior_handler` must map to a whitelisted Java enum and registered Spring handler.
- Gameplay logic should remain in the Spring Boot service layer, not in database triggers or stored procedures.

---

### `effect_components`

The `effect_components` table stores ordered, typed operations performed by an Effect.

Each `component_type` maps to a dedicated Java DTO and `EffectComponentProcessor`. The `config` object contains only the fields declared by that DTO; arbitrary maps, executable expressions, SQL, and Java class names are not allowed.

#### Columns

| Column | Type | Key | Nullable | Description |
|---|---|---:|---:|---|
| `id` | `uuid` | PK | No | Unique component identifier. |
| `effect_definition_id` | `uuid` | FK | No | Effect that owns this component. References `effect_definitions.id`. |
| `component_type` | `varchar(50)` |  | No | Typed operation implemented by the engine. |
| `sequence_order` | `int` |  | No | Deterministic execution order within the Effect. |
| `config` | `jsonb` |  | No | Discriminated configuration validated against `component_type`. |

#### Keys and Constraints

```sql
PRIMARY KEY (id)
```

```sql
FOREIGN KEY (effect_definition_id) REFERENCES effect_definitions(id)
```

```sql
UNIQUE (effect_definition_id, sequence_order)
```

```sql
CHECK (
  component_type IN (
    'DEAL_DAMAGE',
    'ATTRIBUTE_MODIFIER',
    'NEGATE_NEXT_DAMAGE',
    'APPLY_EFFECT',
    'REMOVE_EFFECT',
    'GRANT_TAG',
    'REMOVE_TAG',
    'RESTORE_RESOURCE',
    'CUSTOM_EXECUTION'
  )
)
```

```sql
CHECK (sequence_order >= 0)
CHECK (jsonb_typeof(config) = 'object')
```

#### Type-Safety Contract

| `component_type` | Java configuration DTO | Required configuration |
|---|---|---|
| `DEAL_DAMAGE` | `DealDamageConfig` | Damage formula and target rules. |
| `ATTRIBUTE_MODIFIER` | `AttributeModifierConfig` | Attribute, operation, and magnitude. |
| `NEGATE_NEXT_DAMAGE` | `NegateNextDamageConfig` | Damage filter and charge-consumption rule. |
| `APPLY_EFFECT` | `ApplyEffectConfig` | Referenced Effect key and target rules. |
| `REMOVE_EFFECT` | `RemoveEffectConfig` | Effect key or controlled tag filter. |
| `GRANT_TAG` | `GrantTagConfig` | Valid gameplay tag. |
| `REMOVE_TAG` | `RemoveTagConfig` | Valid gameplay tag. |
| `RESTORE_RESOURCE` | `RestoreResourceConfig` | Resource target and magnitude. Qi targets must be `ROUND_QI` or `RESERVE_QI`. |
| `CUSTOM_EXECUTION` | `CustomExecutionConfig` | Whitelisted handler key and validated parameters. |

#### Notes

- PostgreSQL cannot fully validate every polymorphic JSON shape. Static content is accepted only after `ContentValidator` deserializes every row into its declared Java DTO.
- Content is compiled into immutable in-memory definitions before matches can start.
- Unknown component types, unknown fields, missing fields, and unknown handler keys fail application startup or content validation.
- Effects may restore or generate Qi. Every Qi-changing component explicitly targets `ROUND_QI` or `RESERVE_QI`; the service clamps the result to `0..550` or `0..150` respectively. Effects cannot modify a Qi maximum.

---

### `action_effects`

The `action_effects` table connects Action levels with Effect definitions.

This table defines which Effects are triggered by a specific Action at a specific level.

A single Action level can trigger multiple Effects in a defined order.

Example:

```text
Quick Slash Level 2:
1. Deal damage
2. Apply Bleed
```

#### Columns

| Column | Type | Key | Nullable | Description |
|---|---|---:|---:|---|
| `id` | `uuid` | PK | No | Unique identifier of the Action-Effect mapping. |
| `action_level_id` | `uuid` | FK | No | Action level that triggers the Effect. References `action_levels.id`. |
| `effect_definition_id` | `uuid` | FK | No | Effect triggered by the Action level. References `effect_definitions.id`. |
| `activation_phase` | `varchar(30)` |  | No | Point in Action execution at which the Effect is applied. |
| `period_interval_ticks` | `int` |  | Yes | Repeating cadence in 0.1-second ticks for a `RESOLVE_DURING_EXECUTION` mapping. |
| `target_selector` | `varchar(20)` |  | No | Runtime target selected for this Effect application. |
| `sequence_order` | `int` |  | No | Order in which the Effect is resolved when multiple Effects exist. |
| `stop_on_failure` | `boolean` |  | No | Whether later mappings stop when this Effect cannot be applied. |
| `created_at` | `timestamptz` |  | No | Timestamp when the Action-Effect mapping was created. |
| `updated_at` | `timestamptz` |  | No | Timestamp when the Action-Effect mapping was last updated. |

#### Primary Key

```sql
PRIMARY KEY (id)
```

#### Foreign Keys

```sql
FOREIGN KEY (action_level_id) REFERENCES action_levels(id)
```

```sql
FOREIGN KEY (effect_definition_id) REFERENCES effect_definitions(id)
```

#### Constraints

```sql
UNIQUE (action_level_id, sequence_order)
```

```sql
CHECK (activation_phase IN ('RESOLVE_ON_START', 'RESOLVE_DURING_EXECUTION', 'RESOLVE_ON_END', 'ON_HIT', 'ON_PASSIVE_TRIGGER'))
CHECK (target_selector IN ('SELF', 'OPPONENT', 'BOTH', 'ACTION_SOURCE', 'ACTION_TARGET'))
CHECK (
  period_interval_ticks IS NULL
  OR (activation_phase = 'RESOLVE_DURING_EXECUTION' AND period_interval_ticks >= 1)
)
CHECK (sequence_order >= 0)
```

#### Indexes

```sql
CREATE INDEX idx_action_effects_action_level_id
ON action_effects(action_level_id);
```

```sql
CREATE INDEX idx_action_effects_effect_definition_id
ON action_effects(effect_definition_id);
```

#### Relationships

| Relationship | Description |
|---|---|
| `action_effects.action_level_id` → `action_levels.id` | Each row belongs to one Action level. |
| `action_effects.effect_definition_id` → `effect_definitions.id` | Each row references one reusable Effect definition. |

#### Notes

- `sequence_order` controls the order of Effect resolution.
- Target selection belongs to this mapping so the same reusable Effect can be applied to different targets.
- Cost payment should be handled before resolving Effects.
- `RESOLVE_ON_START` mappings resolve at `start_tick + 1`; `RESOLVE_DURING_EXECUTION` mappings apply for the complete `(start, end]` interval; and `RESOLVE_ON_END` mappings resolve at `end_tick`. Each of these mappings must use a resolution type declared by its Action.
- `period_interval_ticks` is null for non-periodic mappings. For a periodic mapping, the first resolution is after one complete interval, later resolutions repeat at that cadence, and an endpoint-aligned resolution occurs at the Action's inclusive endpoint. The engine never resolves the mapping after the Action endpoint.
- `stop_on_failure` defines deterministic failure behavior.
- This table allows the same Effect definition to be reused across multiple Actions and levels.
- This table supports data-driven Action design without hardcoding every Action’s behavior in server code.

---

## Room and Match Runtime Tables

This section defines the runtime tables used to manage rooms, active matches, player match state, Action Queues, active Effects, passive state, and match results.

Unlike static game data, these tables store state that is created or changed while players are interacting with the game.

---

### `rooms`

The `rooms` table stores pre-match 1v1 room state.

A room exists before a match starts. It contains two player slots: Player A and Player B. Player A is the host by default. If Player A leaves before the match starts and Player B remains in the room, Player B may be promoted to Player A by the service layer.

A player disconnecting from the room does not mean the player has left. Disconnect state is tracked separately using `player_a_connected` and `player_b_connected`.

#### Columns

| Column | Type | Key | Nullable | Description |
|---|---|---:|---:|---|
| `id` | `uuid` | PK | No | Unique identifier of the room. |
| `room_code` | `varchar(20)` | Unique | No | Short join code used by Player B to join the room. |
| `status` | `varchar(30)` |  | No | Current room status. |
| `player_a_id` | `uuid` | FK | No | User occupying Player A slot. Player A is the room host. References `users.id`. |
| `player_b_id` | `uuid` | FK | Yes | User occupying Player B slot. Null means the second slot is empty. References `users.id`. |
| `player_a_ready` | `boolean` |  | No | Whether Player A is ready to start the match. |
| `player_b_ready` | `boolean` |  | No | Whether Player B is ready to start the match. |
| `player_a_connected` | `boolean` |  | No | Whether Player A is currently connected to the room. |
| `player_b_connected` | `boolean` |  | No | Whether Player B is currently connected to the room. |
| `created_at` | `timestamptz` |  | No | Timestamp when the room was created. |
| `updated_at` | `timestamptz` |  | No | Timestamp when the room was last updated. |

#### Primary Key

```sql id="ql29gl"
PRIMARY KEY (id)
```

#### Foreign Keys

```sql id="zgq1wj"
FOREIGN KEY (player_a_id) REFERENCES users(id)
```

```sql id="q6joss"
FOREIGN KEY (player_b_id) REFERENCES users(id)
```

#### Constraints

```sql id="wh2waj"
UNIQUE (room_code)
```

```sql id="h3g27k"
CHECK (status IN ('WAITING_FOR_PLAYER', 'OPEN', 'IN_MATCH', 'CLOSED'))
```

```sql id="pv12i4"
CHECK (player_b_id IS NULL OR player_a_id <> player_b_id)
```

#### Recommended Indexes

```sql id="efzksr"
CREATE INDEX idx_rooms_room_code
ON rooms(room_code);
```

```sql id="2puqsc"
CREATE INDEX idx_rooms_player_a_id
ON rooms(player_a_id);
```

```sql id="li5z5w"
CREATE INDEX idx_rooms_player_b_id
ON rooms(player_b_id);
```

#### Relationships

| Relationship | Description |
|---|---|
| `rooms.player_a_id` → `users.id` | Player A is a registered user. |
| `rooms.player_b_id` → `users.id` | Player B is a registered user when the second slot is occupied. |
| `rooms.id` → `matches.room_id` | A room can create one match. |

#### Notes

- `player_a_id` represents the host slot before the match starts.
- `player_b_id = null` means the second slot is empty.
- `player_b_connected = false` does not necessarily mean `player_b_id` should be null.
- A disconnect should update `player_x_connected = false`.
- A leave action should clear the related player slot if the room has not started.
- The service layer should enforce that a match can only start when both players are present, ready, and connected.
- Database constraints should not contain room promotion logic. Promotion from Player B to Player A is application behavior.

---

### `matches`

The `matches` table stores the main runtime state of a match.

A match is created from a room after both players are ready and connected. The match proceeds through pre-match selection, Renewal, Ascension, Action Strategy, Battle, and Game Over phases.

Battle resolution is simultaneous on a shared timeline. One tick is 0.1 second, and there is no initiative field.

#### Columns

| Column | Type | Key | Nullable | Description |
|---|---|---:|---:|---|
| `id` | `uuid` | PK | No | Unique identifier of the match. |
| `room_id` | `uuid` | FK | No | Room that created this match. References `rooms.id`. |
| `status` | `varchar(30)` |  | No | Current match status. |
| `phase` | `varchar(50)` |  | No | Current match phase. |
| `current_round_number` | `int` |  | No | Current round number. Starts at 1. |
| `current_timeline_tick` | `int` |  | No | Current 0.1-second Battle timeline tick within the current round. Starts at 0. |
| `phase_started_at` | `timestamptz` |  | Yes | Timestamp when the current phase started. |
| `phase_deadline_at` | `timestamptz` |  | Yes | Server-owned deadline for the current phase. |
| `log_text` | `text` |  | Yes | Optional plain-text match log summary. Used for simple display or debugging, not full replay. |
| `created_at` | `timestamptz` |  | No | Timestamp when the match was created. |
| `updated_at` | `timestamptz` |  | No | Timestamp when the match was last updated. |

#### Primary Key

```sql id="1ypxoa"
PRIMARY KEY (id)
```

#### Foreign Keys

```sql id="1csb4j"
FOREIGN KEY (room_id) REFERENCES rooms(id)
```

#### Constraints

```sql id="ydzpvp"
UNIQUE (room_id)
```

```sql id="etyjyv"
CHECK (status IN ('CREATED', 'IN_PROGRESS', 'GAME_OVER', 'CANCELLED'))
```

```sql id="t2hxn2"
CHECK (
  phase IN (
    'PRE_MATCH_BASIC_SELECTION',
    'PRE_MATCH_MAIN_SECT_SELECTION',
    'PRE_MATCH_SUPPORT_SELECTION',
    'RENEWAL',
    'ASCENSION',
    'ACTION_STRATEGY',
    'BATTLE',
    'GAME_OVER'
  )
)
```

```sql id="rtpjzq"
CHECK (current_round_number >= 1)
```

```sql id="6fxr08"
CHECK (current_timeline_tick >= 0)
```

#### Recommended Indexes

```sql id="cs4dri"
CREATE INDEX idx_matches_room_id
ON matches(room_id);
```

```sql id="zf050z"
CREATE INDEX idx_matches_status
ON matches(status);
```

#### Relationships

| Relationship | Description |
|---|---|
| `matches.room_id` → `rooms.id` | Each match is created from one room. |
| `matches.id` → `match_players.match_id` | Each match has two match players. |
| `matches.id` → `action_queue_entries.match_id` | Action Queue entries belong to a match. |
| `matches.id` → `active_effects.match_id` | Active Effects belong to a match. |
| `matches.id` → `match_results.match_id` | A completed match has one result. |

#### Notes

- `current_round_number` tracks the active round.
- `current_timeline_tick` tracks the shared Battle time. Actions occupy `(start_tick, end_tick]` and may have different boundaries for each player.
- The server advances `current_timeline_tick` after resolving every event scheduled at the current timeline point.
- `phase_deadline_at` is controlled by the server. Clients may display a countdown but must not advance phases.
- `log_text` is optional and intentionally simple. It should contain human-readable summaries, not structured replay data.
- Full Battle replay is not part of the current schema.
- Match phase transitions are handled by the service layer.

---

### `match_players`

The `match_players` table stores player-specific state inside a match.

Each match has exactly two match players. The table stores selected Sects, connection state, current stats, current resources, and pending phase confirmation state.

Stats and resources are stored directly in this table to provide fast UI rendering and simple runtime updates.

#### Columns

| Column | Type | Key | Nullable | Description |
|---|---|---:|---:|---|
| `id` | `uuid` | PK | No | Unique identifier of the match player. |
| `match_id` | `uuid` | FK | No | Match this player belongs to. References `matches.id`. |
| `user_id` | `uuid` | FK | No | User account of the player. References `users.id`. |
| `seat` | `varchar(20)` |  | No | Player seat in the match. Used for UI and stable ordering only. |
| `main_sect_id` | `uuid` | FK | Yes | Main Sect selected by the player. References `sects.id`. |
| `support_sect_id` | `uuid` | FK | Yes | Support Sect selected by the player. References `sects.id`. |
| `str_level` | `int` |  | No | Current upgrade level of STR, from 1 through 3. |
| `str_value` | `int` |  | No | Permanent STR value after Ascension upgrades, before temporary Effect modifiers. |
| `hp_level` | `int` |  | No | Current upgrade level of HP, from 1 through 3. |
| `hp_current` | `int` |  | No | Current HP value. |
| `hp_max` | `int` |  | No | Permanent HP maximum after Ascension upgrades, before temporary Effect modifiers. Healing cannot exceed this value. |
| `def_level` | `int` |  | No | Current upgrade level of DEF, from 1 through 3. |
| `def_value` | `int` |  | No | Permanent DEF value after Ascension upgrades, before temporary Effect modifiers. |
| `as_level` | `int` |  | No | Current upgrade level of AS, from 1 through 3. |
| `as_value` | `numeric(10,2)` |  | No | Permanent AS value after Ascension upgrades, before temporary Effect modifiers. |
| `round_qi` | `int` |  | No | Current Round Qi. Global range: 0 through 550. |
| `reserve_qi` | `int` |  | No | Current Reserve Qi. Global range: 0 through 150. |
| `pending_ascension` | `jsonb` |  | Yes | Temporary Ascension allocation submitted by the player before the phase resolves. |
| `ascension_confirmed` | `boolean` |  | No | Whether the player has confirmed Ascension for the current round. |
| `action_queue_confirmed` | `boolean` |  | No | Whether the player has confirmed Action Queue for the current round. |
| `connected` | `boolean` |  | No | Whether the player is currently connected to the match. |
| `disconnected_at` | `timestamptz` |  | Yes | Timestamp when the player disconnected. Null if currently connected. |
| `created_at` | `timestamptz` |  | No | Timestamp when the match player record was created. |
| `updated_at` | `timestamptz` |  | No | Timestamp when the match player record was last updated. |

#### Primary Key

```sql id="u02zb2"
PRIMARY KEY (id)
```

#### Foreign Keys

```sql id="2hzntl"
FOREIGN KEY (match_id) REFERENCES matches(id)
```

```sql id="yxtloe"
FOREIGN KEY (user_id) REFERENCES users(id)
```

```sql id="b6lvm5"
FOREIGN KEY (main_sect_id) REFERENCES sects(id)
```

```sql id="evfspt"
FOREIGN KEY (support_sect_id) REFERENCES sects(id)
```

#### Constraints

```sql id="cpvov5"
UNIQUE (match_id, user_id)
```

```sql id="ved0w7"
UNIQUE (match_id, seat)
```

```sql id="gyrzq0"
CHECK (seat IN ('PLAYER_A', 'PLAYER_B'))
```

```sql id="nfohdg"
CHECK (str_value >= 0)
CHECK (hp_current >= 0)
CHECK (hp_max >= 0)
CHECK (def_value >= 0)
CHECK (as_value >= 1)
CHECK (round_qi BETWEEN 0 AND 550)
CHECK (reserve_qi BETWEEN 0 AND 150)
```

```sql
CHECK (str_level BETWEEN 1 AND 3)
CHECK (hp_level BETWEEN 1 AND 3)
CHECK (def_level BETWEEN 1 AND 3)
CHECK (as_level BETWEEN 1 AND 3)
```

```sql id="sc2jnu"
CHECK (hp_current <= hp_max)
```

```sql id="fnka12"
CHECK (
  (connected = true AND disconnected_at IS NULL)
  OR
  (connected = false)
)
```

#### Recommended Indexes

```sql id="udr9ru"
CREATE INDEX idx_match_players_match_id
ON match_players(match_id);
```

```sql id="lvifch"
CREATE INDEX idx_match_players_user_id
ON match_players(user_id);
```

#### Relationships

| Relationship | Description |
|---|---|
| `match_players.match_id` → `matches.id` | Each match player belongs to one match. |
| `match_players.user_id` → `users.id` | Each match player represents one user. |
| `match_players.main_sect_id` → `sects.id` | The selected Main Sect. |
| `match_players.support_sect_id` → `sects.id` | The selected Support Sect. |
| `match_players.id` → `match_player_action_slots.match_player_id` | Each player has six Action slots. |
| `match_players.id` → `action_queue_entries.match_player_id` | A player can have multiple queue entries per round. |
| `match_players.id` → `active_effects.target_match_player_id` | A player can be affected by active Effects. |
| `match_players.id` → `active_effects.source_match_player_id` | A player can be the source of active Effects. |
| `match_players.id` → `match_player_passive_states.match_player_id` | A player can have Passive Technique state. |
| `match_players.id` → `match_results.winner_match_player_id` | A player may be the winner of a match. |
| `match_players.id` → `match_results.loser_match_player_id` | A player may be the loser of a match. |

#### Notes

- `seat` has no gameplay priority. Battle is simultaneous on a shared 0.1-second timeline.
- `main_sect_id` and `support_sect_id` are null until the corresponding pre-match selections are resolved.
- The six selected Actions are stored only in `match_player_action_slots`; Action IDs are not duplicated in this table.
- Only `HP`, `STR`, `DEF`, and `AS` have Ascension upgrade levels. Qi is runtime state and has no Ascension level columns.
- `HP`, `STR`, and `DEF` permanently increase as integers by 10%; two upgrades are cumulative because the second uses the first upgraded value. The server calculates `increase = ceil(stat_value * 0.10)`. `AS` is `numeric(10,2)` and uses `increase = ceil(as_value * 0.10 * 100) / 100`.
- For an HP upgrade, `increase = ceil(hp_max * 0.10)`, `hp_max = hp_max + increase`, and `hp_current = min(hp_current + increase, hp_max)`. This immediately heals the player by the HP increase.
- Temporary Effect modifiers are applied by the service layer and do not alter the permanent Ascension value.
- `round_qi` and `reserve_qi` use global limits and are not granted by either Sect.
- `available_qi` is derived as `round_qi + reserve_qi` and must not be persisted as a column.
- Healing cannot increase `hp_current` above `hp_max`.
- A `QI` Action cost spends `round_qi` before `reserve_qi`; an insufficient combined balance pays no cost and produces `INSUFFICIENT_RESOURCE`.
- `pending_ascension` is temporary phase state. It should be cleared after Ascension resolves.
- The service layer should ensure each match has exactly two match players.

#### Renewal runtime guidance

The service resolves Renewal in this order:

1. Resolve Effects scheduled for `RENEWAL_START`.
2. Transfer remaining Round Qi into Reserve Qi using `transferableQi = min(round_qi, 150 - reserve_qi)`, then set `reserve_qi = reserve_qi + transferableQi`, `discardedQi = round_qi - transferableQi`, and `round_qi = 0`.
3. Grant Round Qi for the new round: 150 in round 1, 250 in round 2, 300 in round 3, 350 in round 4, 400 in round 5, 450 in round 6, 500 in round 7, and 550 from round 8 onward.
4. Resolve Effects scheduled for `RENEWAL_END`.
5. Clamp `round_qi` and `reserve_qi` to their valid ranges.
6. Remove expired Effects according to the existing Effect lifecycle.

`RENEWAL_START` Effects may change Qi before the transfer. `RENEWAL_END` Effects observe the newly granted Round Qi, and any Round Qi that cannot fit in Reserve Qi is discarded.

---

### `match_player_action_slots`

The `match_player_action_slots` table stores the six Actions held by a match player.

Each player has exactly six Action slots during a match:

```text id="90xkej"
- BASIC_1
- BASIC_2
- MAIN_1
- MAIN_2
- MAIN_3
- SUPPORT
```

The two Basic slots contain two distinct Actions selected from Slash, Defend, and Shield. The three Main slots are selected from the Main Sect. The Support slot is selected from the Support Sect. Slots may contain active or passive Actions. The Action Queue may reference active slots; confirmation does not block invalid submissions, and execution eligibility is decided at runtime.

Action unlock state is represented by `current_level`.

```text id="h74wqo"
current_level = 0 means locked.
current_level >= 1 means unlocked.
Selected Basic Actions start at level 1.
```

#### Columns

| Column | Type | Key | Nullable | Description |
|---|---|---:|---:|---|
| `id` | `uuid` | PK | No | Unique identifier of the match player Action slot. |
| `match_player_id` | `uuid` | FK | No | Match player who owns this Action slot. References `match_players.id`. |
| `slot_type` | `varchar(20)` |  | No | Fixed slot type. |
| `action_id` | `uuid` | FK | No | Action assigned to this slot. References `actions.id`. |
| `current_level` | `int` |  | No | Runtime level of the Action in this match. Level 0 means locked. |
| `created_at` | `timestamptz` |  | No | Timestamp when the Action slot was created. |
| `updated_at` | `timestamptz` |  | No | Timestamp when the Action slot was last updated. |

#### Primary Key

```sql id="5hkw3n"
PRIMARY KEY (id)
```

#### Foreign Keys

```sql id="3g99f0"
FOREIGN KEY (match_player_id) REFERENCES match_players(id)
```

```sql id="b7etw4"
FOREIGN KEY (action_id) REFERENCES actions(id)
```

#### Constraints

```sql id="afzp65"
UNIQUE (match_player_id, slot_type)
```

```sql id="hq2l3n"
CHECK (slot_type IN ('BASIC_1', 'BASIC_2', 'MAIN_1', 'MAIN_2', 'MAIN_3', 'SUPPORT'))
```

```sql id="x5cbxk"
CHECK (current_level BETWEEN 0 AND 3)
```

#### Recommended Indexes

```sql id="94gqcr"
CREATE INDEX idx_match_player_action_slots_match_player_id
ON match_player_action_slots(match_player_id);
```

```sql id="2s1k79"
CREATE INDEX idx_match_player_action_slots_action_id
ON match_player_action_slots(action_id);
```

#### Relationships

| Relationship | Description |
|---|---|
| `match_player_action_slots.match_player_id` → `match_players.id` | Each Action slot belongs to one match player. |
| `match_player_action_slots.action_id` → `actions.id` | Each slot references one static Action. |
| `match_player_action_slots.id` → `action_queue_entries.action_slot_id` | Queue entries select one Action slot. |
| `match_player_action_slots.id` → `match_player_passive_states.action_slot_id` | Passive state can belong to one Action slot. |

#### Notes

- The service layer should create exactly six Action slots for each match player.
- `BASIC_1` and `BASIC_2` must reference two distinct `actions.action_key` values selected from `SLASH`, `DEFEND`, and `SHIELD`.
- Both Basic Action slots start at `current_level = 1`.
- Main and Support Action slots may start at `current_level = 0` if the Action is locked.
- `MAIN_1`, `MAIN_2`, and `MAIN_3` must be distinct Actions belonging to the selected Main Sect.
- `SUPPORT` must belong to the selected Support Sect, must not duplicate a Main Action, and must reference an Action with `is_ultimate = false`.
- Main and Support Sects may be the same.
- Queue submission may reference any player-owned slot and is not blocked by validation. At runtime, an occurrence executes only when `current_level >= 1`, `actions.activation_type = 'ACTIVE'`, and all other runtime requirements are satisfied.
- Passive Actions remain in the player's hand and are evaluated by the passive trigger engine.
- Action level upgrades update `current_level`.

---

### `action_queue_entries`

The `action_queue_entries` table stores a player's submitted Action Queue for a specific round.

Each row represents one ordered Action occurrence. An Action Queue has no entry-count or timeline-duration limit. The same Action slot can appear multiple times in the same queue.

The optional queue-check operation is advisory and does not write these rows. Confirmation stores and locks the submitted occurrences without validating or rejecting them. If the player times out, the server uses an empty queue.

#### Columns

| Column | Type | Key | Nullable | Description |
|---|---|---:|---:|---|
| `id` | `uuid` | PK | No | Unique identifier of the Action Queue entry. |
| `match_id` | `uuid` | FK | No | Match this queue entry belongs to. References `matches.id`. |
| `match_player_id` | `uuid` | FK | No | Match player who owns this queue entry. References `match_players.id`. |
| `round_number` | `int` |  | No | Round number this queue entry belongs to. |
| `sequence_index` | `int` |  | No | Zero-based occurrence order in the Action Queue. |
| `action_slot_id` | `uuid` | FK | Yes | Submitted Action slot. Set to null when runtime converts an invalid occurrence to `EMPTY_RUNTIME`. References `match_player_action_slots.id`. |
| `entry_status` | `varchar(30)` |  | No | Submitted or runtime-empty state of this occurrence. |
| `as_snapshot` | `numeric(10,2)` |  | Yes | Strictly positive AS captured when the occurrence reaches its runtime start position. Null before runtime scheduling. |
| `scheduled_start_tick` | `int` |  | Yes | Computed start on the 0.1-second Battle timeline. Null before scheduling. |
| `scheduled_end_tick` | `int` |  | Yes | Computed inclusive end on the 0.1-second Battle timeline. Null before scheduling. The interval is `(start, end]`. |
| `runtime_failure_reason` | `varchar(50)` |  | Yes | Reason an occurrence became `EMPTY_RUNTIME`. |
| `created_at` | `timestamptz` |  | No | Timestamp when the queue entry was created. |
| `updated_at` | `timestamptz` |  | No | Timestamp when the queue entry was last updated. |

#### Primary Key

```sql id="ao7oyn"
PRIMARY KEY (id)
```

#### Foreign Keys

```sql id="2u6l7t"
FOREIGN KEY (match_id) REFERENCES matches(id)
```

```sql id="lx75n0"
FOREIGN KEY (match_player_id) REFERENCES match_players(id)
```

```sql id="m2c3ca"
FOREIGN KEY (action_slot_id) REFERENCES match_player_action_slots(id)
```

#### Constraints

```sql id="0mctx7"
UNIQUE (match_player_id, round_number, sequence_index)
```

```sql id="r8uk6o"
CHECK (round_number >= 1)
```

```sql id="0ukpqp"
CHECK (sequence_index >= 0)
```

```sql id="br6y5x"
CHECK (entry_status IN ('SUBMITTED', 'EMPTY_RUNTIME', 'EXECUTED'))
```

```sql
CHECK (
  runtime_failure_reason IS NULL
  OR runtime_failure_reason IN (
    'COOLDOWN_INVALID',
    'INSUFFICIENT_RESOURCE',
    'ACTION_NOT_OWNED',
    'ACTION_LOCKED',
    'ACTION_NOT_QUEUEABLE',
    'DISABLED_BY_EFFECT',
    'QUEUE_TRANSITION_INVALID'
  )
)
```

```sql id="y2dszp"
CHECK (
  (entry_status IN ('SUBMITTED', 'EXECUTED') AND action_slot_id IS NOT NULL AND runtime_failure_reason IS NULL)
  OR
  (entry_status = 'EMPTY_RUNTIME' AND action_slot_id IS NULL AND runtime_failure_reason IS NOT NULL)
)
```

```sql
CHECK (scheduled_start_tick IS NULL OR scheduled_start_tick >= 0)
CHECK (scheduled_end_tick IS NULL OR scheduled_end_tick >= 1)
CHECK (as_snapshot IS NULL OR as_snapshot > 0)
CHECK (
  (scheduled_start_tick IS NULL AND scheduled_end_tick IS NULL)
  OR
  (scheduled_start_tick IS NOT NULL AND scheduled_end_tick > scheduled_start_tick)
)
```

#### Recommended Indexes

```sql id="quwwu5"
CREATE INDEX idx_action_queue_entries_match_id
ON action_queue_entries(match_id);
```

```sql id="x6b9ug"
CREATE INDEX idx_action_queue_entries_match_player_round
ON action_queue_entries(match_player_id, round_number);
```

#### Relationships

| Relationship | Description |
|---|---|
| `action_queue_entries.match_id` → `matches.id` | Queue entries belong to one match. |
| `action_queue_entries.match_player_id` → `match_players.id` | Queue entries belong to one player. |
| `action_queue_entries.action_slot_id` → `match_player_action_slots.id` | A queue entry selects one Action slot. |

#### Notes

- Queue entries reference `match_player_action_slots`, not `actions` directly.
- The same `action_slot_id` may appear multiple times in the same round.
- Queue confirmation does not run validation and does not reject an invalid queue.
- Before confirmation, the server may calculate an advisory result containing timing, cooldown, stack, transition, ownership, level, and activation-type violations without persisting or locking the queue. It estimates `AS_SCALED` from current AS, uses configured ticks for `FIXED`, and evaluates the available-state rule for `CONTROLLED`. It never checks resource sufficiency.
- At runtime, the service layer checks ownership, Action level, activation type, cooldown, consecutive stack, costs, disabled state, and other execution requirements.
- A runtime-invalid occurrence is changed to `EMPTY_RUNTIME`, its `action_slot_id` is cleared, and `runtime_failure_reason` records why. It pays no cost and produces no Action Effects. `EMPTY_RUNTIME` is the database representation of the public `EMPTY_SLOT` runtime status.
- Runtime conversion to `EMPTY_RUNTIME` must not stop the opponent's timeline.
- `scheduled_start_tick` and `scheduled_end_tick` preserve deterministic `(start, end]` timing, including after an occurrence becomes `EMPTY_RUNTIME`, so later Actions do not shift. At its runtime start position, the server captures `as_snapshot`, calculates the occurrence's duration from `duration_type`, then reserves its interval before validation and cost checks. Later AS changes may affect later `AS_SCALED` or AS-dependent `CONTROLLED` occurrences that have not started, but never reschedule an occurrence that has started and never affect `FIXED` occurrences. AS does not modify cooldown. Basic Actions have no cooldown; Sect Techniques use their configured cooldown unchanged.
- The service layer does not enforce an Action Queue entry-count or timeline-duration limit.
- After round 1, a valid sequence is derived from the previous confirmed sequence by removing zero or one contiguous range, retaining the relative order of all remaining occurrences, and inserting new occurrences anywhere. The removed range must satisfy `removed_duration_ticks * 3 <= previous_resolved_queue_duration_ticks`; this previous duration is the prior round's authoritative duration, including reserved runtime-empty intervals, not a capacity. Advisory validation reports violations without blocking confirmation; at runtime, violating occurrences are converted to `EMPTY_RUNTIME` with `QUEUE_TRANSITION_INVALID`.

---

### `active_effects`

The `active_effects` table stores Effects that are currently active on a match player.

Active Effects are runtime state. They are not Battle logs.

Examples:

```text id="88b9ss"
- Bleed
- Blind
- Regeneration
- Reserve Qi regeneration
- Defense reduction
```

Instant Effects do not need to be stored in this table. Effects with duration or ongoing behavior are stored here.

#### Columns

| Column | Type | Key | Nullable | Description |
|---|---|---:|---:|---|
| `id` | `uuid` | PK | No | Unique identifier of the active Effect. |
| `match_id` | `uuid` | FK | No | Match where the Effect is active. References `matches.id`. |
| `target_match_player_id` | `uuid` | FK | No | Match player affected by the Effect. References `match_players.id`. |
| `source_match_player_id` | `uuid` | FK | Yes | Match player who caused the Effect. References `match_players.id`. |
| `effect_definition_id` | `uuid` | FK | No | Static Effect definition. References `effect_definitions.id`. |
| `current_stacks` | `int` |  | No | Current number of stacks for this Effect. |
| `remaining_ticks` | `int` |  | Yes | Remaining 0.1-second Battle ticks. Null for non-tick-based Effects. |
| `remaining_rounds` | `int` |  | Yes | Number of rounds remaining. Null may be used for Effects that do not expire by round count. |
| `remaining_charges` | `int` |  | Yes | Remaining consumable charges. Null for Effects that are not charge-based. |
| `created_at` | `timestamptz` |  | No | Timestamp when the active Effect was created. |
| `updated_at` | `timestamptz` |  | No | Timestamp when the active Effect was last updated. |

#### Primary Key

```sql id="2g3a32"
PRIMARY KEY (id)
```

#### Foreign Keys

```sql id="psoq34"
FOREIGN KEY (match_id) REFERENCES matches(id)
```

```sql id="1gfsf4"
FOREIGN KEY (target_match_player_id) REFERENCES match_players(id)
```

```sql id="a68qqw"
FOREIGN KEY (source_match_player_id) REFERENCES match_players(id)
```

```sql id="0a962d"
FOREIGN KEY (effect_definition_id) REFERENCES effect_definitions(id)
```

#### Constraints

```sql id="enwhk8"
CHECK (current_stacks >= 1)
```

```sql id="69u7wz"
CHECK (remaining_rounds IS NULL OR remaining_rounds >= 0)
```

```sql
CHECK (remaining_ticks IS NULL OR remaining_ticks >= 0)
CHECK (remaining_charges IS NULL OR remaining_charges >= 0)
```

#### Recommended Indexes

```sql id="n37juz"
CREATE INDEX idx_active_effects_match_id
ON active_effects(match_id);
```

```sql id="2blyhp"
CREATE INDEX idx_active_effects_target_match_player_id
ON active_effects(target_match_player_id);
```

```sql id="njfxmc"
CREATE INDEX idx_active_effects_effect_definition_id
ON active_effects(effect_definition_id);
```

#### Relationships

| Relationship | Description |
|---|---|
| `active_effects.match_id` → `matches.id` | Active Effects belong to one match. |
| `active_effects.target_match_player_id` → `match_players.id` | The player affected by the Effect. |
| `active_effects.source_match_player_id` → `match_players.id` | The player who caused the Effect. |
| `active_effects.effect_definition_id` → `effect_definitions.id` | The static Effect definition. |

#### Notes

- Instant damage, healing, or resource changes should be applied directly and do not need an `active_effects` row.
- Effects with duration, delayed triggers, stacking, or repeated behavior should be stored here.
- Stacking behavior is defined by `effect_definitions.stacking_policy`.
- The service layer should enforce max stack rules using `effect_definitions.max_stacks`.
- Charge-based Effects are removed when `remaining_charges` reaches 0.
- Tick-based Effects use the shared 0.1-second timeline and remain active through their inclusive endpoint.
- Defend creates an execution-bound Effect with `remaining_charges = 1` and a lifetime equal to its occurrence's calculated interval. It is removed when it ignores its next qualifying Slash damage instance or when that interval reaches its endpoint; consuming the charge does not end the Defend occurrence.
- When an active Effect expires, the service layer may delete the row or keep it until match cleanup. The recommended behavior is to delete expired active Effects.

---

### `match_player_passive_states`

The `match_player_passive_states` table stores runtime progress for unlocked Passive Action cards.

This table is used for passive counters, charges, cooldowns, and other current-state values. It is not a log table.

Examples:

```text id="vh1705"
- Hit counter for a passive that triggers after 3 successful hits
- Charge count for a passive that stores power
- Cooldown remaining for a triggered passive
- Stack count for a player-owned passive
```

#### Columns

| Column | Type | Key | Nullable | Description |
|---|---|---:|---:|---|
| `id` | `uuid` | PK | No | Unique identifier of the passive state record. |
| `match_player_id` | `uuid` | FK | No | Match player who owns this passive state. References `match_players.id`. |
| `action_slot_id` | `uuid` | FK | No | Action slot of the Passive Technique. References `match_player_action_slots.id`. |
| `action_trigger_id` | `uuid` | FK | No | Trigger whose accumulated runtime state is stored. References `action_triggers.id`. |
| `counter_value` | `int` |  | No | Generic counter value used by passive conditions. |
| `stack_count` | `int` |  | No | Current stack count owned by the passive. |
| `cooldown_remaining` | `int` |  | No | Remaining cooldown measured in rounds or ticks, depending on the passive definition. |
| `last_triggered_round` | `int` |  | Yes | Last round number when the passive triggered. |
| `is_active` | `boolean` |  | No | Whether this passive state is currently active. |
| `created_at` | `timestamptz` |  | No | Timestamp when the passive state record was created. |
| `updated_at` | `timestamptz` |  | No | Timestamp when the passive state record was last updated. |

#### Primary Key

```sql id="ahpp7f"
PRIMARY KEY (id)
```

#### Foreign Keys

```sql id="4kyz1m"
FOREIGN KEY (match_player_id) REFERENCES match_players(id)
```

```sql id="v8f6ok"
FOREIGN KEY (action_slot_id) REFERENCES match_player_action_slots(id)
```

```sql
FOREIGN KEY (action_trigger_id) REFERENCES action_triggers(id)
```

#### Constraints

```sql id="js6j7l"
UNIQUE (match_player_id, action_slot_id, action_trigger_id)
```

```sql id="8mmqau"
CHECK (counter_value >= 0)
```

```sql id="yd73ch"
CHECK (stack_count >= 0)
```

```sql id="fdas72"
CHECK (cooldown_remaining >= 0)
```

```sql id="y0o65m"
CHECK (last_triggered_round IS NULL OR last_triggered_round >= 1)
```

#### Recommended Indexes

```sql id="1lv5ig"
CREATE INDEX idx_match_player_passive_states_match_player_id
ON match_player_passive_states(match_player_id);
```

```sql id="fdp6tf"
CREATE INDEX idx_match_player_passive_states_action_slot_id
ON match_player_passive_states(action_slot_id);
```

```sql
CREATE INDEX idx_match_player_passive_states_action_trigger_id
ON match_player_passive_states(action_trigger_id);
```

#### Relationships

| Relationship | Description |
|---|---|
| `match_player_passive_states.match_player_id` → `match_players.id` | Passive state belongs to one match player. |
| `match_player_passive_states.action_slot_id` → `match_player_action_slots.id` | Passive state belongs to one Action slot. |
| `match_player_passive_states.action_trigger_id` → `action_triggers.id` | Passive state tracks one configured trigger. |

#### Notes

- This table stores current passive state only.
- This table does not store historical trigger logs.
- Passive trigger rules are configured in `action_triggers` and resolved by the Spring Boot passive trigger engine.
- The referenced Action slot must have `actions.activation_type = 'PASSIVE'` and `current_level >= 1`.
- `counter_value` can be used for passives such as “trigger after 3 successful hits.”
- `cooldown_remaining` can be decremented by the service layer at a defined timing.
- More specialized passive state should be implemented in application logic first. Additional columns or a controlled `state_data` JSON field can be introduced later if the passive system becomes more complex.

---

### `match_results`

The `match_results` table stores the final result of a match.

A match has one result after it reaches `GAME_OVER`.

The schema supports normal wins, surrender, disconnect loss, and draw.

#### Columns

| Column | Type | Key | Nullable | Description |
|---|---|---:|---:|---|
| `id` | `uuid` | PK | No | Unique identifier of the match result. |
| `match_id` | `uuid` | FK / Unique | No | Match this result belongs to. References `matches.id`. |
| `winner_match_player_id` | `uuid` | FK | Yes | Winning match player. Null when the result is a draw. References `match_players.id`. |
| `loser_match_player_id` | `uuid` | FK | Yes | Losing match player. Null when the result is a draw. References `match_players.id`. |
| `reason` | `varchar(30)` |  | No | Reason why the match ended. |
| `ended_at` | `timestamptz` |  | No | Timestamp when the match ended. |
| `created_at` | `timestamptz` |  | No | Timestamp when the result record was created. |

#### Primary Key

```sql id="bz2w99"
PRIMARY KEY (id)
```

#### Foreign Keys

```sql id="v6w1s8"
FOREIGN KEY (match_id) REFERENCES matches(id)
```

```sql id="vcpivl"
FOREIGN KEY (winner_match_player_id) REFERENCES match_players(id)
```

```sql id="0h7q2d"
FOREIGN KEY (loser_match_player_id) REFERENCES match_players(id)
```

#### Constraints

```sql id="cwyr8e"
UNIQUE (match_id)
```

```sql id="kt6y5d"
CHECK (reason IN ('HP_REACHED_ZERO', 'SURRENDER', 'DISCONNECTED', 'DRAW'))
```

```sql id="cetlhf"
CHECK (
  (
    reason = 'DRAW'
    AND winner_match_player_id IS NULL
    AND loser_match_player_id IS NULL
  )
  OR
  (
    reason <> 'DRAW'
    AND winner_match_player_id IS NOT NULL
    AND loser_match_player_id IS NOT NULL
    AND winner_match_player_id <> loser_match_player_id
  )
)
```

#### Recommended Indexes

```sql id="ezgjje"
CREATE INDEX idx_match_results_match_id
ON match_results(match_id);
```

```sql id="glbop1"
CREATE INDEX idx_match_results_winner_match_player_id
ON match_results(winner_match_player_id);
```

#### Relationships

| Relationship | Description |
|---|---|
| `match_results.match_id` → `matches.id` | Each match result belongs to one match. |
| `match_results.winner_match_player_id` → `match_players.id` | Winner of the match, if not draw. |
| `match_results.loser_match_player_id` → `match_players.id` | Loser of the match, if not draw. |

#### Notes

- `DRAW` is used when both players meet a losing condition at the same Battle timeline point.
- For `DRAW`, both winner and loser are null.
- For `SURRENDER`, the surrendering player is the loser and the opponent is the winner.
- For `DISCONNECTED`, the disconnected player is the loser and the opponent is the winner.
- This table stores only the final match result, not full replay data.
