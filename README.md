# Agnostik Backend

- Real-time, corridor-style social network where users text live with their immediate neighbors.
- After register/login, each user enters a shared corridor; the text area sits center stage and can be focused/unfocused via click or Esc.
- Typed text streams instantly to adjacent neighbors—no submit button—so nearby users see updates as you type.
- Navigation via arrow icons or keyboard arrows lets users roam the corridor to meet and interact.
- Users can lock their current slot (lock icon or down arrow); locked users can’t be displaced and can’t move until they unlock (unlock icon or up arrow).
- When two adjacent users are both locked, no one can slip between them and an “add friend” icon appears for friend requests.
- Friends who meet get a yellow outline indicator and auto-lock until each unlocks.

## Related
- Frontend (React): https://github.com/dimell94/agnostik-app-ui#readme
- Bot runner for demo users: https://github.com/dimell94/agnostik-bot-runner#readme

## General Requirements
- Java 17 (tested with Amazon Corretto 17.0.15_6)
- Gradle 8.14.3

## Quick Start (Local)
- Clone: `git clone https://github.com/dimell94/agnostik-app.git && cd agnostik-app`
- Start MySQL (example Docker): `docker run -p 3306:3306 -e MYSQL_ROOT_PASSWORD=pass -e MYSQL_DATABASE=agnostik_db mysql:8`
- Configure enviroment variables
- Build: `./gradlew clean build`
- Run: `java -jar ./build/libs/agnostik-app-0.0.1-SNAPSHOT.jar` (or `./gradlew bootRun` for dev)
- App serves at `http://localhost:8080` 

## Environment Variables
- `MYSQL_HOST` 
- `MYSQL_PORT` 
- `MYSQL_DB` 
- `MYSQL_USER` 
- `MYSQL_PASSWORD` 

## API & WebSocket
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Profiles & Persistence
- Default profile `dev` loads `application-dev.properties` (uses the env placeholders above).
- Switch DB/port via env or custom `application-*.properties` (JDBC URL, credentials, `server.port`). If you change database vendor, add the matching JDBC driver (and Spring Data starter if needed) to `build.gradle`; only MySQL is included by default.
