# Vault - Expense Tracker

A full-stack personal finance app built with **Spring Boot** and **React**. Track expenses, manage recurring payments, and monitor spending across multiple currencies.

## Features

- **Authentication** — Register, login, email verification, password reset (JWT via HttpOnly cookies)
- **Expense Tracking** — Add, edit, delete expenses with category tagging
- **Recurring Transactions** — Set up daily/weekly/monthly/yearly recurring items with active/inactive toggle
- **Multi-Currency** — Live exchange rates (TRY, EUR, USD) via Frankfurter API
- **Dashboard** — Spending breakdown by category, income vs expenses, budget progress
- **Reports** — Monthly trends and category-level analytics
- **Data Export** — Export transactions as CSV or JSON
- **Dark/Light Theme** — Toggle between themes

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 3.5, Spring Security, Spring Data JPA |
| Database | PostgreSQL with Flyway migrations |
| Auth | JWT (HttpOnly cookies, SameSite=Strict) |
| Frontend | React 18 (CDN + Babel), single-page app |
| Email | Spring Mail (async verification & password reset) |
| API Docs | Swagger UI via springdoc-openapi |
| CI | GitHub Actions |
| Tests | JUnit 5, Mockito, Spring MockMvc (305 tests) |

## Prerequisites

- Docker (with Compose v2)
- Java 21+ (only if you run the app outside Docker; Maven comes with the `./mvnw` wrapper)

## Getting Started

### Option A: everything in Docker

```bash
echo "JWT_SECRET=$(openssl rand -base64 32)" > .env
docker compose --profile app up -d --build
```

This starts PostgreSQL, [Mailpit](https://mailpit.axllent.org/) (a local SMTP catcher) and the app. The app is built with a multi-stage `Dockerfile` and runs as a non-root user.

### Option B: app on the host, services in Docker

**1. Start PostgreSQL and Mailpit**

```bash
docker compose up -d
```

**2. Configure the app**

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

The database and mail defaults already match `docker-compose.yml`. You only need to replace `jwt.secret` with the output of:

```bash
openssl rand -base64 32
```

**3. Run the app**

```bash
./mvnw spring-boot:run
```

### Then

- App: **http://localhost:8080**. Flyway creates all tables on first start.
- Emails (verification, password reset): **http://localhost:8025** (Mailpit). New accounts must click the verification link before they can log in.
- Stop with `docker compose --profile app down`. Add `-v` to also delete the database volume.

### 4. Test account

A seeded admin user is available for testing:

| Field | Value |
|-------|-------|
| Email | `admin@admin.com` |
| Password | `12345678` |

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login (sets JWT cookie) |
| POST | `/api/auth/logout` | Logout (clears cookie) |
| GET | `/api/auth/me` | Get current user profile |
| PUT | `/api/auth/profile` | Update profile |
| GET | `/api/auth/verify?token=` | Verify email |
| POST | `/api/auth/resend-verification` | Resend verification email |
| POST | `/api/auth/forgot-password` | Request password reset |
| POST | `/api/auth/reset-password` | Reset password with token |

### Expenses
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/expenses` | List all expenses |
| GET | `/api/expenses/{id}` | Get single expense |
| POST | `/api/expenses` | Create expense |
| PUT | `/api/expenses/{id}` | Update expense |
| DELETE | `/api/expenses/{id}` | Delete expense |

### Recurring Transactions
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/recurring` | List all recurring |
| GET | `/api/recurring/{id}` | Get single recurring |
| POST | `/api/recurring` | Create recurring |
| PUT | `/api/recurring/{id}` | Update recurring |
| DELETE | `/api/recurring/{id}` | Delete recurring |

### Exchange Rates
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/rates?from=TRY` | Get exchange rates |

All endpoints except auth (register/login/verify) require authentication.

Full API documentation available at **http://localhost:8080/swagger-ui.html** when the app is running.

## Project Structure

```
src/main/java/com/expense/tracker/
  config/          # Security & app configuration
  controller/      # REST controllers
  dto/             # Request/response records
  entity/          # JPA entities (User, Expense, RecurringTransaction, AuthToken)
  exception/       # Global exception handler
  repository/      # Spring Data JPA repositories
  security/        # JWT authentication filter
  service/         # Business logic

src/main/resources/
  db/migration/    # Flyway SQL migrations (V1-V5)
  static/          # Frontend (index.html — React SPA)
```

## Running Tests

```bash
# All tests
./mvnw test

# Specific test class
./mvnw test -Dtest="ExpenseServiceTest"
```

Tests use H2 in-memory database — no PostgreSQL needed.

## CI/CD

GitHub Actions runs the full test suite on:
- Pushes to `master` and `DEV`
- Pull requests targeting `master`

Tests use H2 in-memory database in PostgreSQL-compatibility mode -- no external services required.

## Contributing

1. Create a feature branch from `DEV`
2. Make your changes and add tests
3. Run `./mvnw verify` to ensure all tests pass
4. Open a pull request targeting `master`

## License

MIT -- see [LICENSE](LICENSE) for details.
