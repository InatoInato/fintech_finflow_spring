# Finflow — Backend README

> **Purpose:** a compact, secure fintech-ish backend (Spring Boot) with users, wallets, top-up and transactions — ready to be shown in portfolio.

---

## Quick overview

This repository contains a Spring Boot backend implementing:

* JWT-based authentication (register/login)
* Wallets (1:1 user -> wallet)
* Top-up and Transactions (deposit/withdraw/transfer)
* Flyway migrations (Postgres)
* Optional Redis for caching / rate-limiting
* OpenAPI / Swagger UI
* Global error handling and validation
* Swagger `http://localhost:8080/swagger-ui/index.html`

This README explains how to run the project locally, with Docker, how to call endpoints (curl examples), how to enable Redis, and troubleshooting notes.

---

## Architecture overview

The project follows a layered architecture:

Controller → Service → Repository

* Controllers: HTTP / REST only
* Services: business logic, transactions, validations
* Repositories: persistence layer (Spring Data JPA)

Cross-cutting concerns:
* Security (JWT filter)
* Global exception handling
* Flyway migrations
* Transactional boundaries at service layer


---

## Prerequisites

* Java 21+ / JDK 25 installed
* Maven
* Docker & Docker Compose (if running with containers)
* PostgreSQL (only if you run without Docker)
* Redis (only as rate limiter)

---

## Build locally (without Docker)

1. Build the jar:

```bash
mvn -DskipTests clean package
```

2. Create a `.env` or export env vars used by the app (see *Environment variables* below).

3. Run the jar:

```bash
java -jar target/finflow-0.0.1-SNAPSHOT.jar
```

By default the app uses `server.port=8080`.

---

## Run with Docker (recommended for demos)

**1.** Build the app and bring up services with Docker Compose (example `docker-compose.yml` included in repo):

```bash
mvn -DskipTests clean package
docker compose up --build
```

**2.** The compose file should include:

* `db` (postgres:17)
* `app` (built from Dockerfile)
* optionally `redis` (for rate limiting / caching)

**Common docker issue**: `bind: address already in use` on port 5432 — stop local Postgres or change compose `ports` (or run Postgres on a different host port).

---

## Environment variables

You can pass configuration either via `application.yml` or environment variables. Important vars:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/finflow
SPRING_DATASOURCE_USERNAME=finflow
SPRING_DATASOURCE_PASSWORD=finflow
SPRING_JPA_HIBERNATE_DDL_AUTO=update
APPLICATION_SECURITY_JWT_SECRET_KEY=<BASE64_OR_RAW_SECRET>
SPRING_FLYWAY_ENABLED=true
SPRING_FLYWAY_LOCATIONS=classpath:db/migration
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379
```

**JWT secret note:** use a sufficiently long secret for HMAC-SHA (>= 256 bits). If you see `Key byte array is 136 bits` error, replace the secret with a stronger one (for example use `io.jsonwebtoken.security.Keys.secretKeyFor(SignatureAlgorithm.HS256)` to generate a secure secret and store its Base64 form in env).

---

## Swagger / OpenAPI

* When the app runs, open: `http://localhost:8080/swagger-ui/index.html` (or `/swagger-ui.html`) — you will see all endpoints and can try requests from the UI.
* If Swagger UI returns `403` in the browser: make sure your security config allows unauthenticated access to `/v3/api-docs/**` and `/swagger-ui/**` paths.

---

## API quick-call examples (curl)

### Register

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com","password":"qwerty123"}'
```

Response contains `token`.

### Login

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com","password":"qwerty123"}'
```

### Top-up

```bash
curl -s -X POST http://localhost:8080/api/v1/wallet/topup \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"walletId":1,"amount":1000}'
```

### Get my wallet

```bash
curl -s -X GET http://localhost:8080/api/v1/wallet \
  -H "Authorization: Bearer <TOKEN>"
```

### Create transaction (transfer)

```bash
curl -s -X POST http://localhost:8080/api/v1/transaction \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"fromWalletId":1,"toWalletId":2,"amount":100}'
```

---

## Postman / Collection

You can import these curl examples as requests into Postman, create an `Auth` request to obtain JWT and then set `{{token}}` environment variable for other requests.

---

## Enabling Redis (for caching / rate limiter)

Add `redis` service to your `docker-compose.yml`:

```yaml
redis:
  image: redis:7
  container_name: finflow-redis
  ports:
    - "6379:6379"
```

Enable Redis properties in `application.yml` or env:

```text
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379
```

**Rate limiter idea (sliding window):**

* Use Redis INCR with TTL per key `rate:{userId}:{endpoint}`
* Key TTL = window (seconds)
* If INCR result > limit → reject with `429 Too Many Requests`

I can add a sample `RedisRateLimiter` bean implementation and filter if you want.

---

## Security checklist (fintech-ish)

* Use HTTPS in production (TLS termination via proxy / load balancer)
* Ensure JWT uses a secure key of at least 256 bits
* Protect endpoints with proper authorization (check ownership of wallet before transfer)
* Input validation (Jakarta Validation annotations already used)
* Use parameterized queries (Spring Data/JPA prevents SQL injection by default)
* Enable strong CORS policy
* Add security headers (CSP, X-Frame-Options, HSTS)
* Rate limiting per user/IP (Redis)
* Log suspicious events, but never log tokens or passwords

---

## Tests

Run unit/integration tests with Maven:

```bash
mvn test
```

If you prefer lightweight controller tests without spinning DB, add `@WebMvcTest` and mock services.

---

## Troubleshooting

* `500 Illegal base64 character: '-'` when parsing JWT secret: ensure your secret is correct Base64 or provide raw secret bytes properly (or set a simple raw string for development).
* `Key byte array is 136 bits` — use a longer secret (>= 256 bits) or generate programmatically.
* `Cannot serialize` on wallet response — prefer returning DTOs (e.g. `WalletResponse`) instead of entities to avoid lazy-loading or bidirectional serialization issues.
* Flyway migration errors: view SQL in `src/main/resources/db/migration` and adjust SQL dialect for Postgres (use `SERIAL` or `BIGSERIAL` or `GENERATED BY DEFAULT AS IDENTITY` instead of `AUTO_INCREMENT`).
* Docker port conflict: stop local database or change mapping in `docker-compose.yml`.

---

Tell me which to generate next and I will add it directly into the repo (Docker + Redis, RateLimiter, Postman, or README extras).

---

**Enjoy — show this in your portfolio!**
