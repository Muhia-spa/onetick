# OneTick

Enterprise task and ticket management backend built with Spring Boot 3, PostgreSQL, JWT auth, Flyway migrations, and browser-based API tooling.

## Runtime Stack

- Java 17
- Maven 3.9+
- PostgreSQL 17 (or 16+)
- Spring Boot 3.3.x

## Local Run

1. Create database and user:
   - DB: `onetick`
   - User: `onetick`
   - Password: `onetick`
2. Start the app:
   - `mvn spring-boot:run`
3. Open:
   - UI console: `http://localhost:8080/`
   - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
   - Health: `http://localhost:8080/actuator/health`

## Docker Run (Production-like)

1. Copy env template:
   - `copy .env.example .env`
2. Set secure values in `.env` (especially `APP_JWT_SECRET`).
3. Start stack:
   - `docker compose up -d --build`
4. Open:
   - UI console: `http://localhost:8080/`
   - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
   - Health: `http://localhost:8080/actuator/health`

## Default Bootstrap Admin (for local/dev)

On first startup, if missing, the app seeds:

- Email: `admin@onetick.local`
- Password: `admin12345`
- Role: `ADMIN`

Override via `application.yml` or environment-backed properties under:

- `app.bootstrap.admin.enabled`
- `app.bootstrap.admin.email`
- `app.bootstrap.admin.name`
- `app.bootstrap.admin.password`

## API Security

- Login endpoint: `POST /api/v1/auth/login`
- Protected endpoints require `Authorization: Bearer <token>`
- Swagger/OpenAPI includes Bearer auth scheme for interactive testing

## Phase 2 Foundations

- Workspace and Project domain model introduced:
  - `GET/POST /api/v1/workspaces`
  - `GET/POST /api/v1/projects`
- Department and task APIs now support pagination/filtering:
  - `GET /api/v1/departments?page=0&size=20&search=ops&workspaceId=1`
  - `GET /api/v1/tasks?page=0&size=20&status=IN_PROGRESS&assignedToUserId=1&projectId=1`
- Flyway migration `V2` seeds a default workspace and upgrades schema.

## Profiles

- `dev` (default): bootstrap admin enabled for local onboarding.
- `prod`: bootstrap admin disabled by default.
- Override with `SPRING_PROFILES_ACTIVE`.

## Roadmap

- Enterprise transformation plan: [`docs/PLATFORM_ROADMAP.md`](docs/PLATFORM_ROADMAP.md)
