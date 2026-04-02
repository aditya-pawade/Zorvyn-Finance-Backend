# Zorvyn Finance Backend

Spring Boot backend for financial record management with role-based access control, analytics endpoints, and JWT support.

## Features

- User and financial record CRUD APIs
- Role-based authorization (`ADMIN`, `ANALYST`, `VIEWER`)
- Record filtering by date, date range, category, and type
- Dashboard APIs:
  - total income
  - total expense
  - net balance
  - category totals
  - recent activity
  - monthly and weekly trends
- Analyst insight APIs
- Authentication options:
  - HTTP Basic (for local/testing)
  - JWT token issuance and Bearer-token authorization
- Integration tests for security and endpoint behavior

## Tech Stack

- Java 8
- Spring Boot 2.7.14
- Spring Web, Spring Data JPA, Spring Security
- MySQL (runtime)
- H2 (test profile)
- Maven Wrapper (`mvnw`, `mvnw.cmd`)

## Project Structure

- `src/main/java/com/zorvyn/financebackend`
  - `controller` - REST endpoints
  - `service` - business logic
  - `repository` - JPA data access
  - `model` - entities
  - `dto` - request/response payloads
  - `security` - auth and security config
  - `exception` - global exception handling
- `src/main/resources` - application config
- `src/test` - integration and context tests

## Prerequisites

- JDK 8
- Maven (optional, wrapper included)
- MySQL running locally (default profile)

## Configuration

Default config is in `src/main/resources/application.properties`.

Important properties to update before production:

- `spring.datasource.*`
- `app.jwt.secret`
- `app.jwt.expiration-ms`
- `app.security.seed-test-users`

## Run Locally

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

## Run Tests

```powershell
.\mvnw.cmd test
```

## Free Deployment (Easy)

Recommended free setup:

- Render (free web service)
- Neon (free PostgreSQL)

### 1) Create free PostgreSQL DB (Neon)

1. Create a Neon account and project.
2. Create a database.
3. Copy:
  - Host
  - Database name
  - Username
  - Password
  - Port

Build JDBC URL:

`jdbc:postgresql://<HOST>:5432/<DB_NAME>?sslmode=require`

### 2) Create web service (Render)

1. In Render, click New -> Web Service.
2. Connect this GitHub repository.
3. Render can auto-detect settings from `render.yaml` in this repo.
3. Runtime: Docker not required (native Java build).
4. Build command:

```bash
./mvnw -DskipTests package
```

5. Start command:

```bash
java -jar target/finance-backend-0.0.1-SNAPSHOT.jar
```

### 3) Add environment variables in Render

You can copy keys from `.env.render.example`.

- `SPRING_DATASOURCE_URL` = `jdbc:postgresql://<HOST>:5432/<DB_NAME>?sslmode=require`
- `SPRING_DATASOURCE_USERNAME` = Neon username
- `SPRING_DATASOURCE_PASSWORD` = Neon password
- `APP_DATASOURCE_DRIVER_CLASS_NAME` = `org.postgresql.Driver`
- `SPRING_JPA_HIBERNATE_DDL_AUTO` = `update`
- `APP_SECURITY_SEED_TEST_USERS` = `false`
- `APP_JWT_SECRET` = long random secret (at least 32 chars)
- `APP_JWT_EXPIRATION_MS` = `3600000`

Render provides `PORT` automatically.

### 4) Deploy and verify

After deploy, test:

- `GET /api/auth/me` (with valid auth)
- `POST /api/auth/token`
- `GET /api/dashboard/summary`

If needed, seed users temporarily by setting `APP_SECURITY_SEED_TEST_USERS=true`, verify, then set it back to `false` and redeploy.

## Live Smoke Test Script

After deployment, run:

```powershell
.\scripts\live-smoke-test.ps1 -BaseUrl "https://your-service.onrender.com"
```

## Authentication

### 1) HTTP Basic
Use email/password credentials in Basic auth header.

### 2) JWT
Request token:

`POST /api/auth/token`

Example body:

```json
{
  "email": "analyst@zorvyn.local",
  "password": "analyst123"
}
```

Use returned token:

`Authorization: Bearer <token>`

## Main API Groups

- `/api/auth`
- `/api/users`
- `/api/records`
- `/api/dashboard`
- `/api/analyst`
- `/api/admin`

## Test Users (when seeding is enabled)

- `admin@zorvyn.local / admin123`
- `analyst@zorvyn.local / analyst123`
- `viewer@zorvyn.local / viewer123`

## License

This project is licensed under the MIT License. See `LICENSE`.
