# Agnostik Backend

## Agnostik: An Experimental Social Network of Live Written Presence

Agnostik is an experimental social platform built around live written presence. Its goal is to make identity dynamic, allowing it to emerge through spontaneous expression rather than through a fixed, curated social image. There are no publicly visible usernames or other identifying markers inside the experience. Presence unfolds in real time through writing, whether that writing is conversational, reflective, improvisational, or simply a way of being present.

### Core Idea 📦

When a user enters Agnostik, they are placed inside a shared digital corridor. **Each user sees their own writing space in the center and only two immediate neighbors, one on the left and one on the right.**

**There is no send button.** **Text appears live, character by character, as it is being typed.** This allows nearby users to witness writing in real time.

**Users initially enter based on join order, but the corridor changes as people move**, making it a shifting social space rather than a static one.

### Navigation 🧭

**Users can move left or right through the corridor, exploring neighboring presences and discovering interaction through movement.**

### Friendship and Recognition 🤝

When two users feel a connection, they can exchange a friend request. **If it is accepted, the friendship is marked only by a color indicator that appears when they meet again.**

**There are no friend lists and no strong public identity markers inside the corridor.** Because of this, a user may encounter a friend again without immediately knowing who they are, recognizing the connection only through the visual signal. This gives friendship a more mysterious and unpredictable quality, since connection can disappear into the space of the corridor and later return by chance.

### Position Locking 🔒

Agnostik includes a locking mechanism that allows users to freeze their position in the corridor. **Any user can lock or unlock at any time.**

**When two adjacent users are both locked, no third user can move between them.** Others pass around them instead, helping preserve the continuity of an encounter when both sides want to remain together.

**When two friends meet side by side, the system automatically locks them as well.** They may unlock if they choose, but the friendship remains. Locking is not limited to friends and can also be used between strangers.

## Demo Video 🎥

[![Agnostik Demo](https://img.youtube.com/vi/HN0AaEQVmHw/hqdefault.jpg)](https://youtu.be/HN0AaEQVmHw)

---

This repository contains the backend implementation of that system. It is a Spring Boot 3.5 application that provides JWT-based authentication, in-memory corridor presence state, friendship persistence in MySQL, REST endpoints for presence and requests, and STOMP-over-WebSocket delivery for live snapshots.

Related repositories:

- Frontend: https://github.com/dimell94/agnostik-app-ui
- Bot runner: https://github.com/dimell94/agnostik-bot-runner

## Table of Contents

- [Architecture](#architecture)
- [Core Behavior](#core-behavior)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Running Locally with Docker](#running-locally-with-docker)
- [Running Locally without Docker](#running-locally-without-docker)
- [Authentication](#authentication)
- [REST API Overview](#rest-api-overview)
- [WebSocket and Snapshot Flow](#websocket-and-snapshot-flow)
- [Documentation](#documentation)
- [Project Structure](#project-structure)
- [Troubleshooting](#troubleshooting)

## Architecture 🏗️

The backend has three main responsibilities:

1. Authentication and user persistence
   - Users are stored in MySQL.
   - Passwords are hashed with BCrypt.
   - JWTs identify users by numeric user id in the token subject.
2. Corridor presence and live interaction
   - Current corridor order, lock state, and text are held in server memory.
   - Presence actions are issued through REST and text updates through STOMP.
3. Live synchronization
   - Clients fetch an initial snapshot through REST.
   - The server pushes updated snapshots to each affected user on `/user/queue/snapshot`.

Important design detail:

- Corridor presence state is in-memory, not persisted to MySQL.
- Friendships are persisted in MySQL.
- User text content is also stored in-memory.

## Core Behavior 🎯

- A user joins the corridor automatically after successful register or login.
- Users can move left or right unless blocked by corridor boundaries or lock constraints.
- Locked users cannot be displaced directly.
- Movement can skip across runs of locked users if there is an available slot beyond them.
- A friend request can only be sent when both adjacent users are locked.
- Accepting a request creates a friendship and locks both users.
- When two friends are adjacent, the backend auto-locks them.
- If adjacent friends unlock, the backend allows movement before auto-locking them again after two moves involving that friendship pair.
- Text updates are capped at 6000 characters.

## Tech Stack 🧰

- Java 17
- Spring Boot 3.5.5
- Spring Web
- Spring Security
- Spring Data JPA
- Spring WebSocket + Spring Messaging
- MySQL 8
- JWT via `jjwt`
- OpenAPI/Swagger via `springdoc-openapi`
- Lombok

## Prerequisites ✅

Choose one of the two local run modes:

- Docker and Docker Compose
- Or Java 17 + a reachable MySQL instance

## Configuration ⚙️

Base app config:

- `server.port=8080`
- active profile: `dev`

The `dev` profile reads these properties:

### Database

- `MYSQL_HOST`
- `MYSQL_PORT`
- `MYSQL_DB`
- `MYSQL_USER`
- `MYSQL_PASSWORD`

The JDBC URL enables:

- `createDatabaseIfNotExist=true`
- `useSSL=false`
- `allowPublicKeyRetrieval=true`

JPA settings:

- `spring.jpa.hibernate.ddl-auto=update`
- dialect: `org.hibernate.dialect.MySQLDialect`

### JWT

- `jwt.secret`
- `jwt.expMinutes` default in `application-dev.properties`: `60`

The application expects `jwt.secret` to be a Base64-encoded HMAC key.

## Running Locally with Docker 🐳

```bash
docker compose build
docker compose up -d
```

The provided `docker-compose.yml` starts:

- MySQL on host port `3307`
- backend app on host port `8080`

Container-level wiring:

- app connects to DB host `db`
- DB container exposes MySQL internally on `3306`

## Running Locally without Docker ▶️

1. Ensure MySQL is running.
2. Export the environment variables you want to override.
3. Start the Spring Boot app.

Example:

```bash
export MYSQL_HOST=127.0.0.1
export MYSQL_PORT=3307
export MYSQL_DB=agnostik_local
export MYSQL_USER=agnostik_user
export MYSQL_PASSWORD=agnostik_pass

./gradlew bootRun
```

If you prefer an IDE run configuration, make sure the active profile is `dev`.

## Authentication 🔐

Authentication is stateless and JWT-based.

1. `POST /api/auth/register` creates the user, returns a JWT, and joins the corridor.
2. `POST /api/auth/login` validates credentials, returns a JWT, and joins the corridor.
3. Clients send `Authorization: Bearer <token>` on protected REST calls.
4. The JWT subject is the user id.
5. The same bearer token must be sent in the STOMP `CONNECT` header for WebSocket use.

Public routes:

- `/api/auth/register`
- `/api/auth/login`
- `/ws/**`
- `/v3/api-docs/**`
- `/swagger-ui.html`
- `/swagger-ui/**`
- `/docs/**`

All other routes require authentication.

## REST API Overview 🔌

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

### Presence

- `GET /api/presence/snapshot`
- `POST /api/presence/moveLeft`
- `POST /api/presence/moveRight`
- `POST /api/presence/lock`
- `POST /api/presence/unlock`
- `POST /api/presence/leave`

### Friend requests

- `POST /api/requests/send/{left|right}`
- `POST /api/requests/cancel/{left|right}`
- `POST /api/requests/accept/{left|right}`
- `POST /api/requests/reject/{left|right}`

Behavior note:

- REST endpoints trigger state changes.
- Clients should treat WebSocket snapshots as authoritative for resulting UI state.

## WebSocket and Snapshot Flow 📡

### STOMP endpoint

- WebSocket endpoint: `/ws`
- Application destination prefix: `/app`
- User destination prefix: `/user`
- Simple broker prefix: `/queue`

### Current messaging flow

- Clients connect to `/ws`
- Clients send JWT in `Authorization: Bearer <token>` during STOMP `CONNECT`
- Clients subscribe to `/user/queue/snapshot`
- Clients publish text updates to `/app/text`

The backend currently sends full per-user snapshots through:

- `convertAndSendToUser(id, "/queue/snapshot", snapshot)`

The snapshot includes:

- current user info
- left and right neighbor info
- corridor size
- lock state
- text
- friend-request flags
- friendship flags

## Documentation 📚

When the app is running locally:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- WebSocket AsyncAPI HTML: `http://localhost:8080/docs/websocket.html`

The repository also contains:

- `src/main/resources/static/docs/websocket-asyncapi.yaml`

## Project Structure 🗂️

Main packages:

- `api`: REST controllers and STOMP controller
- `authentication`: JWT generation and OpenAPI config
- `security`: JWT filter and HTTP security configuration
- `service`: corridor presence, snapshots, friendships, requests, text
- `model`: JPA entities
- `repository`: JPA repositories
- `core`: exception handling and WebSocket config

## Troubleshooting 🛠️

| Issue | Likely Cause | What to Check |
| --- | --- | --- |
| App fails to start with DB errors | MySQL not reachable or wrong credentials | Verify `MYSQL_*` values and database availability |
| Frontend can log in but not receive live updates | Missing STOMP auth header or WS connectivity issue | Check `/ws`, CONNECT headers, and subscription to `/user/queue/snapshot` |
| REST works but WebSocket connect fails | JWT missing in STOMP `CONNECT` headers | The interceptor requires `Authorization: Bearer <token>` |
| `409` on movement | User is at a boundary or blocked by lock constraints | Check corridor state and adjacent lock status |
| Friend request fails with validation error | Users are not adjacent, already friends, or not both locked | Verify adjacency and both lock states before sending |
| Corridor state disappears after restart | Presence state is intentionally in-memory | This is current design, not a persistence bug |
| Existing users remain but live state resets | MySQL persists users/friendships, not live presence | Expected behavior after app restart |
