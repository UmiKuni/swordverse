# SwordVerse Documentation

This directory contains the authoritative documentation for the SwordVerse product and codebase. Documents are organized by responsibility rather than by repository package so that product rules, architectural decisions, external contracts, and implementation details remain distinct.

## Document Categories

| Category | Purpose | Audience |
|---|---|---|
| `product/` | Product terminology, intended experience, and game rules | Product, design, frontend, backend |
| `architecture/` | System boundaries, policies, ownership, and accepted technical decisions | Frontend and backend engineers |
| `contracts/` | HTTP payloads, WebSocket destinations, events, commands, and error codes | Frontend and backend engineers |
| `backend/` | Persistence and Spring implementation guidance | Backend engineers |
| `frontend/` | Browser authentication, multi-tab behavior, and realtime lifecycle | Frontend engineers |

## Recommended Reading Order

1. [Product concept](product/concept.md)
2. [Game components](product/game-components.md)
3. [Game rules](product/game-rules.md)
4. [System overview](architecture/overview.md)
5. [Authentication architecture](architecture/authentication.md)
6. [Realtime connections and gameplay lease](architecture/realtime-connections.md)
7. [API and WebSocket contract](contracts/api-websocket-contract.md)
8. The relevant frontend or backend implementation guide

Backend realtime ownership is described in [Backend Realtime Implementation](backend/realtime-implementation.md).

## Sources of Truth

| Subject | Authoritative document |
|---|---|
| Product terminology and intended experience | [Product concept](product/concept.md) |
| Sects, Stats, Basic Actions, Techniques, and gameplay keywords | [Game components](product/game-components.md) |
| Gameplay rules | [Game rules](product/game-rules.md) |
| Authentication and connection ownership | [Architecture](architecture/overview.md) and accepted ADRs |
| HTTP and WebSocket wire format | [API and WebSocket contract](contracts/api-websocket-contract.md) |
| Public error identifiers | [Error code registry](contracts/error-codes.md) |
| Database tables and constraints | [Database schema](backend/database-schema.md) |

Implementation must conform to the product rules, accepted architecture decisions, and external contracts. If documents conflict, the narrower source of truth listed above takes precedence and the conflicting document must be corrected.
