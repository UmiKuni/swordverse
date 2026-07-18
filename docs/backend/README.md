# SwordVerse backend development guide

This document describes how to configure, start, and perform a basic verification of the SwordVerse backend.

## Project location

Run all commands in this document from the `game-server` directory unless stated otherwise.

```powershell
cd game-server
```

## Recommended approach: Docker Compose

Docker Compose starts two isolated services:

- `database`: PostgreSQL 17 with persistent local storage.
- `server`: the Spring Boot application built from the local source code.

### Prerequisites

- Docker Desktop, with Docker Compose enabled.
- Ports `8080` and Docker's required internal resources must be available.

Maven, Java, PostgreSQL, and Postman are not required when using this approach.

### 1. Create the local environment file

Copy the committed template. The resulting `.env` file is ignored by Git and must not be committed.

```powershell
Copy-Item .env.example .env
```

Replace the placeholder database password and JWT signing key in `.env`. A suitable Base64 signing key can be generated with PowerShell:

```powershell
$bytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes)
```

Assign the generated value to `SWORDVERSE_AUTH_SIGNING_KEY_BASE64`.

### 2. Validate the resolved configuration

```powershell
docker compose config --quiet
```

This command fails immediately when a required database or signing-key variable is missing.

### 3. Build and start the services

```powershell
docker compose up --build
```

The first run may take several minutes because Docker must download the base images and Maven dependencies. The server starts only after PostgreSQL passes its health check. Flyway then creates or updates the database schema.

The following URLs become available after startup:

- API base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI document: `http://localhost:8080/v3/api-docs`
- Health check: `http://localhost:8080/actuator/health`

Use `Ctrl+C` to stop attached containers. To stop containers started in the background, run:

```powershell
docker compose down
```

To start in the background and inspect application logs:

```powershell
docker compose up --build --detach
docker compose logs --follow server
```

Do not routinely add `--volumes` to `docker compose down`; deleting the volume also deletes the local PostgreSQL data.

## Alternative approach: run Spring Boot locally

This approach requires Java 17 and a reachable PostgreSQL instance. The values in `.env` are loaded by the default `dev` Spring profile when the application is started from `game-server`.

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS or Linux:

```shell
./mvnw spring-boot:run
```

For local execution, `SWORDVERSE_DB_URL` normally uses `localhost`. Inside Docker Compose, the server instead uses the Compose service hostname `database`.

## Basic authentication verification

Swagger UI is the recommended interface for manual development checks. Open `http://localhost:8080/swagger-ui/index.html` and execute the following flow.

### 1. Register a user

Call `POST /api/auth/register` with:

```json
{
  "username": "test_player",
  "password": "test-password-123",
  "displayName": "Test Player"
}
```

Expected result:

- HTTP status `201 Created`.
- The response body contains an `accessToken`, expiration timestamps, a `sessionId`, and user information.
- The response sets an HTTP-only refresh-token cookie.

Copy the returned `accessToken` for the next step.

### 2. Authenticate Swagger UI

Select **Authorize** in Swagger UI and enter the access token. Swagger UI applies the Bearer scheme automatically; enter only the token value, without the `Bearer` prefix.

```text
<accessToken>
```

### 3. Read the current user

Call `GET /api/auth/me`.

Expected result:

- HTTP status `200 OK`.
- The response identifies `test_player`.

Calling the same endpoint without an access token should return `401 Unauthorized` using the standard API error contract.

### 4. Rotate the refresh token

Call `POST /api/auth/refresh` without a request body. The browser sends the HTTP-only refresh-token cookie automatically because Swagger UI and the API share the same origin.

Expected result:

- HTTP status `200 OK`.
- A new access token is returned.
- The refresh-token cookie is replaced.

Authorize Swagger UI again with the new access token before continuing.

### 5. Log out

Call `POST /api/auth/logout` with the current access token.

Expected result:

- HTTP status `204 No Content`.
- The refresh-token cookie is cleared.
- A subsequent refresh attempt fails because the authenticated session has been revoked.

### 6. Verify login

Call `POST /api/auth/login` with:

```json
{
  "username": "test_player",
  "password": "test-password-123"
}
```

Expected result: HTTP status `200 OK` with a new authenticated session and refresh-token cookie.

## Development commands

### Clean and compile

```powershell
.\mvnw.cmd clean compile
```

### Run tests

```powershell
.\mvnw.cmd test
```

### Package the executable JAR

```powershell
.\mvnw.cmd package
```

### Check or apply formatting

```powershell
.\mvnw.cmd spotless:check
.\mvnw.cmd spotless:apply
```

### Run the complete verification lifecycle

```powershell
.\mvnw.cmd verify
```

## Troubleshooting

### Compose reports that a variable is not set

Confirm that `.env` exists beside `compose.yml`, then run `docker compose config --quiet`. Do not rely on `.env.example` directly; it is only a committed template.

### The server cannot connect to PostgreSQL

Inspect both service states and the server logs:

```powershell
docker compose ps
docker compose logs database
docker compose logs server
```

The Docker JDBC hostname must be `database`, not `localhost`.

### Port 8080 is already in use

Stop the process currently using port `8080`, or change the host side of the server port mapping in `compose.yml`.

### Database credentials were changed after the first startup

PostgreSQL initialization variables apply when the data directory is first created. For disposable local data only, recreate the volume with:

```powershell
docker compose down --volumes
docker compose up --build
```

This operation permanently deletes the local database contents.
