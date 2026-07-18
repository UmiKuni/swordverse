# External Contracts

The [API and WebSocket contract](api-websocket-contract.md) is the canonical description of HTTP resources, STOMP destinations, payloads, commands, events, and security requirements. The [error code registry](error-codes.md) defines stable machine-readable error identifiers shared by transports.

The combined contract is retained as one document because room, match, and reconnect behavior spans both HTTP snapshots and WebSocket commands. It may be divided when independent versioning becomes necessary, provided that payload definitions retain a single source of truth.
