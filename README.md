# Digital Wallet Backend

A simulated digital-wallet REST API built as a Java and Spring Boot learning project. The application supports user accounts, JWT authentication, wallet operations, peer-to-peer transfers, transaction history, and administrator wallet controls.

This project handles simulated balances only. It is not connected to banks, cards, UPI, or real payment systems.

## Project status

The core backend MVP is implemented. The repository currently contains:

- The Spring Boot backend
- MySQL persistence
- Swagger/OpenAPI configuration
- Unit, security, and service-level acceptance tests
- A separate frontend design document

There is no frontend application in this repository yet. `FRONTEND_DESIGN.md` is a plan for possible future work.

## Technology stack

| Area | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.5.16 |
| API | Spring Web, REST, JSON |
| Persistence | Spring Data JPA, Hibernate |
| Database | MySQL 8.x |
| Security | Spring Security, JWT, BCrypt |
| Validation | Jakarta Bean Validation |
| API documentation | Springdoc OpenAPI / Swagger UI |
| Testing | JUnit 5, Mockito, MockMvc, Spring Security Test |
| Build tool | Maven Wrapper |

## Implemented features

### Authentication

- Register a user with name, email, phone number, and password
- Normalize email addresses before storing and looking them up
- Hash passwords with BCrypt
- Automatically create one active wallet with a zero balance
- Log in with email and password
- Return a signed JWT containing the user ID and role

### User profile

- View the authenticated user's profile
- Update the authenticated user's name and phone number
- Prevent duplicate phone numbers
- Avoid exposing password hashes in API responses

### Wallet operations

- View the authenticated user's wallet
- Add simulated money
- Withdraw simulated money
- Reject withdrawals that exceed the available balance
- Reject financial operations when a wallet is frozen
- Store monetary values with `BigDecimal`

### Transfers and transaction history

- Transfer simulated money to another registered user by email
- Reject transfers to the sender's own account
- Reject transfers involving a frozen sender or receiver wallet
- Update both wallet balances and create the transaction record inside one Spring transaction
- Return transaction history for the authenticated user's wallet only
- Show transaction direction as `CREDIT` or `DEBIT`
- Include counterparty details for transfers
- Sort transaction history by creation time, newest first

### Administration

- List users and their wallet status
- View the platform transaction list
- Freeze a wallet
- Activate a frozen wallet
- Restrict `/api/admin/**` endpoints to users with the `ADMIN` role

## Architecture

The application is a single Spring Boot application organized into conventional layers:

```text
HTTP request
    |
    v
Spring Security + JWT filter
    |
    v
Controller
    |
    v
Service / business rules / transaction boundary
    |
    v
Spring Data JPA repository
    |
    v
MySQL
```

```text
src/main/java/com/wallet/
|-- App.java
|-- config/
|   |-- OpenApiConfig.java
|   `-- SecurityConfig.java
|-- controller/
|   |-- AdminController.java
|   |-- AuthController.java
|   |-- TransactionController.java
|   |-- UserController.java
|   `-- WalletController.java
|-- dto/
|   |-- request/
|   `-- response/
|-- entity/
|   |-- User.java
|   |-- Wallet.java
|   |-- Transaction.java
|   `-- supporting enums
|-- exception/
|-- repository/
|-- security/
`-- service/
```

Controllers use request and response DTOs rather than returning JPA entities directly. Protected user endpoints obtain the authenticated user ID from Spring Security instead of accepting a user ID from the request.

## Data model

### User

- Unique email and phone number
- BCrypt password hash
- `USER` or `ADMIN` role
- UTC creation timestamp

### Wallet

- One-to-one relationship with a user
- `BigDecimal(19,2)` balance
- `ACTIVE` or `FROZEN` status

### Transaction

- Optional sender wallet
- Optional receiver wallet
- `ADD_MONEY`, `WITHDRAW`, or `TRANSFER` type
- `SUCCESS` or `FAILED` status
- Unique reference ID
- Amount, optional remarks, and UTC creation timestamp

The wallet references represent each operation as follows:

| Operation | Sender wallet | Receiver wallet |
| --- | --- | --- |
| Add money | `null` | User wallet |
| Withdraw | User wallet | `null` |
| Transfer | Sender wallet | Receiver wallet |

## API endpoints

### Authentication

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | Public | Register a user and create a wallet |
| `POST` | `/api/auth/login` | Public | Authenticate and receive a JWT |

### User

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/users/me` | Authenticated | Get the current user's profile |
| `PUT` | `/api/users/me` | Authenticated | Update the current user's name and phone |

### Wallet

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/wallet` | Authenticated | Get the current user's wallet |
| `POST` | `/api/wallet/add-money` | Authenticated | Add simulated money |
| `POST` | `/api/wallet/withdraw` | Authenticated | Withdraw simulated money |

### Transactions

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `POST` | `/api/transactions/transfer` | Authenticated | Transfer money to another registered user |
| `GET` | `/api/transactions` | Authenticated | Get the current user's transaction history |

### Administration

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/admin/users` | `ADMIN` | List users and wallet information |
| `GET` | `/api/admin/transactions` | `ADMIN` | List all transactions |
| `PUT` | `/api/admin/wallets/{walletId}/freeze` | `ADMIN` | Freeze a wallet |
| `PUT` | `/api/admin/wallets/{walletId}/activate` | `ADMIN` | Activate a wallet |

## Example requests

### Register

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "name": "Naman",
  "email": "naman@example.com",
  "phone": "9876543210",
  "password": "Password123!"
}
```

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "naman@example.com",
  "password": "Password123!"
}
```

### Add money or withdraw

```json
{
  "amount": 5000.00
}
```

### Transfer

```json
{
  "receiverEmail": "rahul@example.com",
  "amount": 2000.00,
  "remarks": "Dinner"
}
```

Protected endpoints require this header:

```http
Authorization: Bearer <jwt-token>
```

## Error responses

Application and Spring Security errors use a shared project-specific JSON structure:

```json
{
  "timestamp": "2026-08-19T16:30:00Z",
  "status": 400,
  "code": "INSUFFICIENT_BALANCE",
  "message": "Insufficient wallet balance",
  "path": "/api/wallet/withdraw"
}
```

Common codes include:

| Situation | HTTP status | Code |
| --- | --- | --- |
| Request validation failure | `400` | `VALIDATION_ERROR` |
| Insufficient balance | `400` | `INSUFFICIENT_BALANCE` |
| Invalid login or missing/invalid JWT | `401` | `UNAUTHORIZED` |
| Frozen wallet | `403` | `WALLET_FROZEN` |
| Insufficient role permissions | `403` | `FORBIDDEN` |
| User or wallet not found | `404` | `NOT_FOUND` |
| Duplicate email or phone | `409` | `CONFLICT` |
| Unexpected application error | `500` | `INTERNAL_ERROR` |

This is a custom error contract. It is inspired by structured API error responses but does not claim full RFC 7807 compliance.

## Local setup

### Prerequisites

- Java 21
- MySQL 8.x
- Git

Maven does not need to be installed separately because the repository includes the Maven Wrapper.

### 1. Clone the repository

```bash
git clone <repository-url>
cd Spring
```

### 2. Create the database

```sql
CREATE DATABASE digital_wallet;
```

### 3. Configure environment variables

Copy `.env.example` to `.env` and replace the placeholder values:

```properties
DB_URL=jdbc:mysql://localhost:3306/digital_wallet
DB_USERNAME=root
DB_PASSWORD=your_database_password
JWT_SECRET=replace_with_a_secret_of_at_least_32_characters
JWT_EXPIRATION_SECONDS=3600
```

Do not commit `.env`. It is excluded by `.gitignore`.

### 4. Run the application

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS or Linux:

```bash
./mvnw spring-boot:run
```

The API starts at `http://localhost:8080`.

### 5. Open Swagger UI

```text
http://localhost:8080/swagger-ui/index.html
```

Use the Swagger **Authorize** dialog to provide a JWT when testing protected endpoints.

## Creating a local administrator

Registration always creates a normal `USER`, as required by the current MVP. There is no public role-promotion endpoint.

For local development, register a user and then update that user's role directly in the development database:

```sql
UPDATE users
SET role = 'ADMIN'
WHERE email = 'admin@example.com';
```

Log in again after changing the role so the new JWT contains the `ADMIN` claim. Do not expose direct role updates as an unauthenticated production feature.

## Tests

Run the test suite with:

```powershell
.\mvnw.cmd test
```

The current suite contains 36 tests across:

- Authentication service behavior
- User profile behavior
- Wallet add-money and withdrawal rules
- Transfer validation and balance changes
- Transaction-history mapping
- Admin wallet controls
- JWT generation and tamper detection
- Security status and role responses
- The PRD acceptance flow

The service tests primarily use Mockito. The security tests use Spring Boot and MockMvc, while the acceptance test uses Spring services and the configured MySQL database.

Important: the acceptance test clears transaction, wallet, and user tables during setup. Run it only against a dedicated local/test database, never against a database containing data you need to preserve.

The repository contains previous Maven Surefire reports showing 36 passing tests. Test results should always be regenerated after code or configuration changes rather than assumed from old reports.

## Current limitations

This is an educational MVP, not a real payment system. Known limitations include:

- No real-money integration
- No frontend implementation yet
- No pagination or server-side transaction filtering
- No refresh tokens, token revocation, or account-session management
- No database migration tool such as Flyway or Liquibase
- Hibernate currently uses `ddl-auto=update`
- SQL logging is enabled for development
- CORS currently permits all origin patterns
- Wallet updates do not yet use optimistic or pessimistic locking
- Concurrent balance-changing requests have not been stress-tested
- Tests do not yet use an isolated Testcontainers database
- No idempotency keys for retry-safe financial requests
- No formal double-entry accounting ledger
- No deployment or CI/CD configuration

`@Transactional` keeps each wallet operation within one database transaction and provides rollback on exceptions. It does not, by itself, guarantee correctness for every possible concurrent-update scenario. Concurrency controls are planned as future improvements.

## Possible next improvements

- Add pessimistic locking or `@Version`-based optimistic locking for wallet updates
- Add concurrent-transfer tests
- Use Testcontainers with a dedicated MySQL test database
- Add Flyway migrations and separate development/test/production profiles
- Tighten CORS for the deployed frontend origin
- Improve validation for trimmed names and numeric phone numbers
- Expand request-parsing and exception-handler tests
- Add a CI workflow
- Add Docker support and deploy the API
- Implement the separately documented frontend if full-stack presentation is desired

## Project purpose

This project was built to practise and demonstrate:

- Spring Boot request flow and dependency injection
- REST API design
- Spring Data JPA relationships
- Spring Security and JWT authentication
- Server-side authorization
- BCrypt password hashing
- Bean Validation
- Transaction boundaries
- Monetary calculations with `BigDecimal`
- Unit and integration testing
- API documentation

It should be presented as a learning-focused backend project with a completed core MVP and a clearly documented improvement roadmap.
