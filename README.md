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
