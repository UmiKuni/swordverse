# SwordVerse Product Concept

## 1. Product Definition

SwordVerse is an online 1v1 auto-combat game centered on swordsmanship, strategic loadout construction, and simultaneous timeline resolution. Players select a Sect, assemble a set of Actions, develop their capabilities during each round, and prepare an Action Queue that the authoritative server resolves against the opponent's plan.

The intended experience emphasizes strategic preparation rather than mechanical execution. Information is revealed according to explicit phase rules, and both players act under the same deterministic simulation model.

## 2. Setting

The SwordVerse setting presents five principal sword traditions. Product terminology currently refers to these traditions as **Sects**. Earlier drafts used **Orders**; new rules, contracts, and implementation must use **Sect** unless an accepted product decision changes the term.

### 2.1 True Sword Sect

The True Sword Sect represents disciplined mastery of conventional swordsmanship. Its intended style is balanced, sustainable, resilient, and adaptable.

### 2.2 Divine Sword Sect

The Divine Sword Sect channels righteous power that becomes increasingly effective as combat continues. Its intended style is scaling, forceful, and deliberately vulnerable during preparation.

### 2.3 Demonic Sword Sect

The Demonic Sword Sect exchanges stability and control for explosive destructive potential. Its intended style is aggressive, high-risk, and oriented toward burst damage.

### 2.4 Flying Sword Sect

The Flying Sword Sect emphasizes speed, deception, mobility, and extended combinations. Its intended style is swift, adaptive, and difficult to predict.

### 2.5 Shadow Sword Sect

The Shadow Sword Sect emphasizes precision, misdirection, and calculated evasion. Its intended style is technical and rewards deliberate sequencing.

These descriptions establish thematic direction only. Authoritative statistics, Action definitions, costs, and Effects are defined by static game data and the game rules.

## 3. Core Terminology

### Sect

A Sect defines a combat identity, base statistics when selected as the Main Sect, support bonuses when selected as the Support Sect, an MP-to-QP conversion ratio, and an associated set of Sect Techniques.

### Technique

A Technique is a Sect-associated combat capability. In the public API and implementation model, every Technique is represented as an Action with `actionSource = SECT_TECHNIQUE`.

### Action

An Action is the server-side gameplay entity represented as a selectable card in the client. Actions may be Basic or Sect-derived and may activate actively or passively.

### Action Queue

An Action Queue is the ordered sequence of learned Active Actions prepared by a player for Battle. The server resolves both players' queues simultaneously on a shared timeline.

## 4. Product Scope

The current scope includes authentication, private rooms, pre-match loadout selection, round progression, deterministic battle resolution, reconnection, surrender, and final match results.

Public matchmaking, ranking, spectators, social systems, chat, replay, shops, and content versioning are outside the current committed scope unless introduced by a later product decision.

## 5. Related Documents

- [Game rules](game-rules.md)
- [System architecture](../architecture/overview.md)
- [API and WebSocket contract](../contracts/api-websocket-contract.md)
