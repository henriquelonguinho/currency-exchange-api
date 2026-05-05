# Currency Exchange API

REST API that stores purchase transactions in US dollars and retrieves them with currency conversion using exchange rates from the [U.S. Treasury Reporting Rates of Exchange](https://fiscaldata.treasury.gov/datasets/treasury-reporting-rates-exchange/treasury-reporting-rates-of-exchange).

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Data JPA + H2 Database
- Spring Validation
- Spring Actuator
- SpringDoc OpenAPI (Swagger UI)
- Flyway (database migrations)
- Maven
- Docker

## Prerequisites

- Java 21
- Maven 3.9+ (or use the included `mvnw` wrapper)
- Docker (optional, for containerized deployment)

## Running Locally

```bash
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080` with an in-memory H2 database.

The H2 console is available at `http://localhost:8080/h2-console`. Use the following JDBC URL to connect:

```
jdbc:h2:mem:currency_exchange
```

## API Documentation (Swagger)

With the application running, the Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

The OpenAPI spec (JSON) is available at:

```
http://localhost:8080/v3/api-docs
```

Swagger is disabled in the `prod` profile.

## Running Tests

```bash
./mvnw test
```

## API Endpoints

### Create a Purchase Transaction

```
POST /purchase-transaction
```

**Request body:**

```json
{
  "description": "Office supplies",
  "transaction_date": "2026-04-15",
  "amount": 49.99
}
```

| Field              | Type    | Rules                              |
|--------------------|---------|------------------------------------|
| `description`      | string  | Required. Max 50 characters.       |
| `transaction_date` | string  | Required. Format `yyyy-MM-dd`.     |
| `amount`           | number  | Required. Must be positive.        |

**Response (201 Created):**

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "description": "Office supplies",
  "transaction_date": "2026-04-15",
  "amount": 49.99
}
```

### Retrieve a Transaction with Currency Conversion

```
GET /purchase-transaction/{id}?currency={currency}
```

| Parameter  | Type   | Description                                                         |
|------------|--------|---------------------------------------------------------------------|
| `id`       | path   | UUID of the purchase transaction.                                   |
| `currency` | query  | Required. Target currency as listed by the Treasury API (e.g. `Brazil-Real`). Must not be blank.|

The API looks for an exchange rate within 6 months prior to the transaction date. If no rate is found, it returns an error.

**Response (200 OK):**

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "description": "Office supplies",
  "transaction_date": "2026-04-15",
  "usd_amount": 49.99,
  "exchange_rate": 5.254,
  "converted_amount": 262.65
}
```

### Error Responses

All errors follow a consistent format:

```json
{
  "timestamp": "2026-04-15T10:30:00.000000",
  "status": 400,
  "error": "Bad Request",
  "path": "/purchase-transaction",
  "details": ["Description is required"]
}
```

| Status | Scenario                                              |
|--------|-------------------------------------------------------|
| 400    | Validation errors, invalid input format, invalid UUID.|
| 404    | Transaction not found.                                |
| 422    | Currency conversion not possible (no rate available). |
| 502    | Treasury API rejected the request (4xx from upstream).|
| 503    | Treasury API is unavailable (timeout, server error).  |

## Docker

### Prerequisites

- Docker and Docker Compose installed

### Build and Run

```bash
./mvnw clean package -DskipTests
docker compose up --build
```

The application will be available at `http://localhost:8080`.

### Stop

```bash
docker compose down
```

To stop and remove the persisted data:

```bash
docker compose down -v
```

### Production Profile

The container runs with the `prod` profile, which:

- Uses H2 in file mode so data persists across container restarts
- Stores the database file in a Docker volume (`h2-data`)
- Disables the H2 console and Swagger UI
- Exposes only `health`, `info`, and `metrics` Actuator endpoints
- Outputs logs in structured JSON format (ECS) for log aggregation tools
- Sets log levels to INFO for application code and WARN for Spring internals

### Health Check

The container includes a health check via Spring Actuator:

```
GET /actuator/health
```

## Database Migrations (Flyway)

Schema changes are managed by Flyway. Migration files live in `src/main/resources/db/migration/` and follow the naming convention:

```
V1__create_purchase_transaction.sql
V2__add_some_column.sql
```

Flyway runs automatically on application startup. Hibernate is set to `validate` only — it checks that entities match the schema but never modifies it.

## CI/CD

A GitHub Actions workflow (`.github/workflows/ci.yml`) runs on every push and pull request to `main`:

1. Builds the project and runs all tests (`./mvnw verify`)
2. Builds the Docker image to validate the Dockerfile

For CD (Continuous Deployment), the application is container-ready. To complete the pipeline, choose a hosting provider (e.g. AWS ECS, Railway, Fly.io) and add a deploy job that pushes the image to a registry and triggers a deployment.

## Project Structure

```
src/main/java/com/currency/exchange/
├── client/treasury/          # Treasury API integration (RestClient, config, DTOs, query builder)
├── controller/               # REST controllers
├── dto/                      # Request and response DTOs
├── exception/                # Global exception handler and custom exceptions
├── model/                    # JPA entities
├── repository/               # Spring Data repositories
└── service/                  # Business logic

src/main/resources/
├── db/migration/             # Flyway SQL migrations
├── application.yaml          # Default configuration
└── application-prod.yaml     # Production profile
```
