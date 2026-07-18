# SwordVerse

SwordVerse is a server-authoritative online 1v1 auto-combat game. Players assemble a six-Action loadout, develop it during a match, prepare an Action Queue, and observe both players' plans resolve simultaneously on a shared timeline.

The project is currently under active design and implementation. The documentation in `docs/` is the authoritative source for product rules, architecture, external contracts, and implementation guidance.

## Repository Structure

```text
swordverse/
├── docs/          Product, architecture, contract, and implementation documentation
├── game-client/   React and TypeScript web client
└── game-server/   Spring Boot game server
```

## Core Product Principles

- The server is authoritative for validation, calculations, phase transitions, effects, and match results.
- A user may maintain multiple authenticated sessions and may use non-gameplay features from multiple tabs or devices.
- A user may own only one active gameplay lease and therefore may control only one matchmaking, room, or match activity at a time.
- Authentication state, realtime connection state, and gameplay ownership are separate concerns.
- Socket disconnection does not constitute logout.

## Documentation

Begin with the [documentation index](docs/README.md).

### Product

- [Product concept](docs/product/concept.md)
- [Game rules](docs/product/game-rules.md)

### Architecture

- [System overview](docs/architecture/overview.md)
- [Authentication architecture](docs/architecture/authentication.md)
- [Realtime connections and gameplay lease](docs/architecture/realtime-connections.md)
- [Architecture decisions](docs/architecture/decisions/README.md)

### Contracts

- [API and WebSocket contract](docs/contracts/api-websocket-contract.md)
- [Error code registry](docs/contracts/error-codes.md)

### Implementation

- [Backend authentication implementation](docs/backend/auth-implementation.md)
- [Backend realtime implementation](docs/backend/realtime-implementation.md)
- [Database schema](docs/backend/database-schema.md)
- [Frontend authentication lifecycle](docs/frontend/authentication-flow.md)
- [Frontend realtime client](docs/frontend/realtime-client.md)

## Technology

### Client

- React and TypeScript
- Redux Toolkit and RTK Query
- React Router
- STOMP over WebSocket

### Server

- Spring Boot and Spring Security
- Spring WebSocket
- PostgreSQL
- Short-lived JWT access tokens
- Server-side authentication sessions
- Rotating opaque refresh tokens delivered through secure `HttpOnly` cookies

## Local Development

Runtime instructions and environment requirements will be documented when the client and server startup workflows are finalized. Required environment variables are listed in `.env.example`.
