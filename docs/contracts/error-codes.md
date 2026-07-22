# SwordVerse Error Code Registry

## 1. Purpose

Error codes are stable, machine-readable identifiers. Clients may branch on a code but must not depend on the human-readable message. HTTP status and WebSocket rejection type provide transport semantics; the code provides application semantics.

## 2. Authentication and Session Errors

| Code | Meaning | Client behavior |
|---|---|---|
| `UNAUTHORIZED` | Authentication is missing or invalid | Attempt the documented refresh flow or present login |
| `INVALID_CREDENTIALS` | Username or password is invalid | Keep the login form visible |
| `INVALID_TOKEN` | A token is malformed, unknown, or cryptographically invalid | Clear unusable credentials |
| `TOKEN_EXPIRED` | The submitted token has expired | Refresh when permitted; otherwise require login |
| `SESSION_REVOKED` | The referenced authentication session is no longer active | End that session locally |
| `TOKEN_REUSE_DETECTED` | A rotated refresh token was reused and the session was revoked | End that session locally and require login |
| `USERNAME_ALREADY_EXISTS` | Registration attempted to use an existing username | Request a different username |

## 3. Connection and Gameplay Ownership Errors

| Code | Meaning | Client behavior |
|---|---|---|
| `GAMEPLAY_LEASE_REQUIRED` | The command requires an active gameplay lease owned by the connection | Attempt the documented acquisition or reconnect flow |
| `GAMEPLAY_ACTIVE_ON_ANOTHER_CONNECTION` | Another connection currently owns the user's gameplay lease | Keep the user authenticated and show the conflict |
| `GAMEPLAY_CONNECTION_REPLACED` | The connection previously owned the lease but ownership was explicitly transferred | Stop sending gameplay commands and return to a non-gameplay state |

Gameplay ownership errors are not authentication failures and must not cause automatic logout.

## 4. Validation and Domain Errors

The complete domain-error list is maintained in [Section 14 of the API and WebSocket contract](api-websocket-contract.md#14-error-codes). When a new public error is introduced, both the relevant contract operation and this registry must be updated.

## 5. Action Queue Runtime Results

| Code | Meaning | Client behavior |
|---|---|---|
| `COOLDOWN_INVALID` | An Action occurrence violates cooldown or consecutive-stack timing. | Show it in advisory queue validation or render the runtime occurrence as `EMPTY_SLOT`; do not treat it as a command rejection. |

`EMPTY_SLOT` is the public runtime status. The database stores the corresponding persistence state as `EMPTY_RUNTIME`.
