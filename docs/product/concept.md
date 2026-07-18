# SwordVerse Product Concept

## 1. Product Definition

SwordVerse is an online 1v1 auto-combat game centered on swordsmanship, strategic loadout construction, and simultaneous timeline resolution. Players select a Sect, assemble a set of Actions, develop their capabilities during each round, and prepare an Action Queue that the authoritative server resolves against the opponent's plan.

The intended experience emphasizes strategic preparation rather than mechanical execution. Information is revealed according to explicit phase rules, and both players act under the same deterministic simulation model.

## 2. Setting

The SwordVerse setting presents five principal sword traditions. Product terminology currently refers to these traditions as **Sects**. Earlier drafts used **Orders**; new rules, contracts, and implementation must use **Sect** unless an accepted product decision changes the term.

See more in [Game Components](game-components.md).

## 3. Core Terminology

### Sect

A Sect defines a combat identity, base statistics when selected as the Main Sect, support bonuses when selected as the Support Sect, and an associated set of Sect Techniques. Qi generation and Qi limits are global round rules; they are not defined by a Sect.

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
- [Game components](game-components.md)
- [System architecture](../architecture/overview.md)
- [API and WebSocket contract](../contracts/api-websocket-contract.md)
