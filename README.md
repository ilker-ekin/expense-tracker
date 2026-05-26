# Vault - Expense Tracker

A full-stack personal finance app built with **Spring Boot** and **React**. Track expenses, manage recurring payments, and monitor spending across multiple currencies.

## Features

- **Authentication** — Register, login, email verification, password reset (JWT via HttpOnly cookies)
- **Expense Tracking** — Add, edit, delete expenses with category tagging
- **Recurring Transactions** — Set up daily/weekly/monthly/yearly recurring items with active/inactive toggle
- **Multi-Currency** — Live exchange rates (TRY, EUR, USD) via Frankfurter API
- **Dashboard** — Spending breakdown by category, income vs expenses, budget progress
- **Reports** — Monthly trends and category-level analytics
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

- Java 21+
- PostgreSQL 15+
- Maven (or use the included `./mvnw` wrapper)

## Getting Started

### 1. Start PostgreSQL

```bash
# Option A: Docker Compose (recommended)
docker compose up -d

# Option B: Standalone Docker
docker run -d --name expense-db \
  -e POSTGRES_DB=expense_tracker \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 postgres:17-alpine
```

### 2. Configure the app

Create `src/main/resources/application.properties`:

```properties
spring.application.name=tracker

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/expense_tracker
spring.datasource.username=postgres
spring.datasource.password=postgres

# JWT
jwt.secret=<your-base64-encoded-secret>
jwt.expiration-ms=900000

# Mail (optional — for email verification)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

app.base-url=http://localhost:8080
```

### 3. Run the app

```bash
./mvnw spring-boot:run
```

The app starts at **http://localhost:8080**. Flyway automatically creates all database tables on first run.

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
- Push to `dev` branch
- Pull requests targeting `master`

Tests use H2 in-memory database in PostgreSQL-compatibility mode -- no external services required.

## Contributing

1. Create a feature branch from `dev`
2. Make your changes and add tests
3. Run `./mvnw verify` to ensure all tests pass
4. Open a pull request targeting `master`

## License

MIT -- see [LICENSE](LICENSE) for details.
