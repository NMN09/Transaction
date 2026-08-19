# Digital Wallet Backend — Project Context & Handover Document

> **Purpose of this file**: This document preserves 100% of the project context, architectural decisions, completed work, testing history, and the next steps. When starting a new session, reading this file enables immediate continuation without any lost context.

---

## 📌 1. Project Overview & Rules
* **Project Name**: Digital Wallet Backend
* **Specification Source**: `Digital_Wallet_CORE_DETERMINISTIC_PRD(1).pdf` (in root directory).
* **Tech Stack**: Java 17/21, Spring Boot 3.x, Maven, MySQL 8.x, Spring Data JPA / Hibernate, Spring Security + JWT, Jakarta Validation, BCrypt.
* **Core Philosophy**: Deterministic MVP implementation. No out-of-scope features (no Redis, Kafka, real money, bank APIs, microservices, frontend). Simulated money only.
* **Development Approach**: 
  - **File-by-File Interactive Learning**: Explain architectural rationale, annotations, security considerations, and senior-level interview talking points for every single file before creation.
  - **Strict Validation & Security**: Whitelist DTOs, prevent Mass Assignment and IDOR vulnerabilities, stateless JWT auth, comprehensive `@RestControllerAdvice` error responses matching PRD Section 16.

---

## 📊 2. Overall Progress Status: 100% / 100% (Core Deterministic MVP Fully Completed)

### Phase Status Breakdown:
* [x] **Phase 1: Project Setup & Configuration (100% / 8%)** — Maven, JPA/MySQL, `.env` / `application.properties`.
* [x] **Phase 2: Database Entities & Repositories (100% / 12%)** — `User`, `Wallet`, `Transaction`, Enums (`Role`, `WalletStatus`, `TransactionType`, `TransactionStatus`), Repositories.
* [x] **Phase 3: Registration & Automatic Wallet Creation (100% / 10%)** — `POST /api/auth/register`, password hashing, duplicate 409 checks, auto wallet creation with 0.00 balance.
* [x] **Phase 4: Spring Security, BCrypt & JWT Login (100% / 12%)** — `POST /api/auth/login`, `JwtService`, `JwtAuthenticationFilter`, `SecurityConfig`.
* [x] **Phase 5: User Profile & Wallet Inspection Endpoints (100% / 8%)** — `GET /api/users/me`, `PUT /api/users/me`, `GET /api/wallet`.
* [x] **Phase 6: Add Money & Withdraw (100% / 10%)** — `POST /api/wallet/add-money`, `POST /api/wallet/withdraw`, balance mutation, transaction ledger creation, `@Transactional`.
* [x] **Phase 7: Atomic Money Transfer (100% / 12%)** — `POST /api/transactions/transfer` with `@Transactional`, IDOR safety, freeze & balance checks.
* [x] **Phase 8: Transaction History (100% / 6%)** — `GET /api/transactions` (Sorted newest first, IDOR safe, `@Transactional(readOnly = true)`).
* [x] **Phase 9: Admin Operations & Wallet Freeze/Activate (100% / 8%)** — `GET /api/admin/users`, `GET /api/admin/transactions`, `PUT /api/admin/wallets/{id}/freeze`, `PUT /api/admin/wallets/{id}/activate`, `ROLE_ADMIN` protection.
* [x] **Phase 10: Exception Handling & Swagger/OpenAPI (100% / 6%)** — Springdoc OpenAPI 3.0 UI, JWT Bearer Scheme, global CORS enabled.
* [x] **Phase 11: Unit/Integration Tests & README (100% / 8%)** — 36 automated tests covering all PRD §18 test cases + PRD §21 End-to-End flow + comprehensive `README.md`.


---

## 📁 3. Current Codebase File Map

```
src/main/java/com/wallet/
├── App.java                               # Spring Boot Application Entry Point
├── config/
│   └── SecurityConfig.java                # Spring Security filter chain (Stateless, JWT Filter, BCrypt bean, Admin RBAC)
├── controller/
│   ├── AdminController.java               # GET /api/admin/users, GET /api/admin/transactions, PUT /api/admin/wallets/{id}/freeze & activate
│   ├── AuthController.java                # POST /api/auth/register, POST /api/auth/login
│   ├── TransactionController.java         # POST /api/transactions/transfer, GET /api/transactions
│   ├── UserController.java                # GET /api/users/me, PUT /api/users/me
│   └── WalletController.java              # GET /api/wallet, POST /api/wallet/add-money, POST /api/wallet/withdraw
├── dto/
│   ├── request/
│   │   ├── AmountRequest.java             # amount (NotNull, DecimalMin, Digits)
│   │   ├── LoginRequest.java              # email, password
│   │   ├── RegisterRequest.java           # name, email, phone, password
│   │   ├── TransferRequest.java           # receiverEmail, amount, remarks
│   │   └── UpdateProfileRequest.java      # name, phone (whitelisted to prevent mass assignment)
│   └── response/
│       ├── ErrorResponse.java             # timestamp, status, code, message, path
│       ├── LoginResponse.java             # token, expiresIn
│       ├── RegisterResponse.java          # message
│       ├── TransactionResponse.java       # referenceId, type, amount, status, remarks, createdAt
│       ├── UserProfileResponse.java       # id, name, email, phone, role, createdAt (no passwordHash!)
│       └── WalletResponse.java            # walletId, balance, status
├── entity/
│   ├── Role.java                          # Enum: USER, ADMIN
│   ├── Transaction.java                   # senderWallet, receiverWallet, amount, type, status, referenceId, remarks
│   ├── TransactionStatus.java             # Enum: SUCCESS, FAILED
│   ├── TransactionType.java               # Enum: ADD_MONEY, WITHDRAW, TRANSFER
│   ├── User.java                          # id, name, email, phone, passwordHash, role, createdAt
│   ├── Wallet.java                        # id, user (OneToOne), balance (BigDecimal 19,2), status, createdAt
│   └── WalletStatus.java                  # Enum: ACTIVE, FROZEN
├── exception/
│   ├── EmailAlreadyExistsException.java   # 409 CONFLICT
│   ├── GlobalExceptionHandler.java        # @RestControllerAdvice handling 400, 401, 403, 404, 409, 500
│   ├── InsufficientBalanceException.java  # 400 BAD_REQUEST
│   ├── InvalidCredentialsException.java   # 401 UNAUTHORIZED
│   ├── PhoneAlreadyExistsException.java   # 409 CONFLICT
│   ├── ResourceNotFoundException.java     # 404 NOT_FOUND
│   ├── SelfTransferException.java         # 400 BAD_REQUEST
│   └── WalletFrozenException.java         # 403 FORBIDDEN
├── repository/
│   ├── TransactionRepository.java         # findBySenderWalletIdOrReceiverWalletIdOrderByCreatedAtDesc, findAllByOrderByCreatedAtDesc
│   ├── UserRepository.java                # findByEmail, existsByEmail, existsByPhone, existsByPhoneAndIdNot
│   └── WalletRepository.java              # findByUserId
├── security/
│   ├── JwtAuthenticationFilter.java       # OncePerRequestFilter parsing Bearer token -> SecurityContext
│   └── JwtService.java                    # HMAC-SHA token creation, parsing, validation, extraction
└── service/
    ├── AdminService.java                  # getAllUsers, getAllTransactions, freezeWallet, activateWallet
    ├── AuthService.java                   # register, login
    ├── TransactionService.java            # transfer, getTransactionHistory
    ├── UserService.java                   # getUserProfile, updateUserProfile
    └── WalletService.java                 # getWalletByUserId, addMoney, withdraw
```

---

## 🧪 4. Verified Working APIs (Tested in Postman)

1. **`POST /api/auth/register`** ➡️ Creates `User` + initial `Wallet` (`balance: 0.00`, `status: ACTIVE`). Returns `201 Created`.
2. **`POST /api/auth/login`** ➡️ Validates credentials via BCrypt, returns signed JWT token (`expiresIn: 3600`).
3. **`GET /api/users/me`** ➡️ Extracts `userId` via `@AuthenticationPrincipal` from JWT, returns safe `UserProfileResponse` (200 OK). Blocks unauthenticated requests (403).
4. **`PUT /api/users/me`** ➡️ Updates `name`/`phone`. Checks `existsByPhoneAndIdNot` (returns 409 if taken by someone else; allows if it's the user's own phone). Validates lengths (400).
5. **`GET /api/wallet`** ➡️ Returns user's `walletId`, `balance` (`0.00`), `status` (`ACTIVE`).
6. **`POST /api/wallet/add-money`** ➡️ Increases balance, persists `ADD_MONEY` `Transaction`, returns `TransactionResponse`.
7. **`POST /api/wallet/withdraw`** ➡️ Validates sufficient balance, decreases balance, persists `WITHDRAW` `Transaction`, returns `TransactionResponse`.
8. **`POST /api/transactions/transfer`** ➡️ Debits sender, credits receiver, creates `TRANSFER` ledger transaction under `@Transactional`.
9. **`GET /api/transactions`** ➡️ Returns all transactions involving the user's wallet (both sent and received), newest first.
10. **`GET /api/admin/users`** ➡️ Admin-only endpoint returning all registered user profiles.
11. **`GET /api/admin/transactions`** ➡️ Admin-only endpoint returning all transactions across the platform.
12. **`PUT /api/admin/wallets/{id}/freeze`** ➡️ Admin-only endpoint to freeze a user's wallet.
13. **`PUT /api/admin/wallets/{id}/activate`** ➡️ Admin-only endpoint to unfreeze/activate a user's wallet.

---

## 🚀 5. Next Steps for Tomorrow's Session

### What is Completed Today (Phases 1 through 10 — 95%):
- **All 13 Backend APIs** implemented and verified (Auth, Profile, Wallet Mutations, Atomic Transfers, History, Admin Operations, Wallet Freeze/Activate).
- **Swagger / OpenAPI 3.0 UI** configured at `http://localhost:8080/swagger-ui/index.html` with interactive JWT Bearer authentication.
- **Global CORS** enabled to allow any frontend domain/port to connect.
- **`version_2_planning.md`** created with deadlock prevention (resource ordering), superadmin role promotion, idempotency keys, and counterparty history metadata.

---

### Two Options to Tackle Tomorrow:

#### Option A: Build the Full Frontend Web Application 🎨
- Implement modern, glassmorphic UI connecting to `http://localhost:8080`:
  - Login / Register tabs with JWT token management.
  - Customer Wallet Dashboard (Live balance card, Add Money modal, Withdraw modal, P2P Transfer modal).
  - Live Transaction History Table (color-coded badges for ADD_MONEY, WITHDRAW, TRANSFER).
  - Admin Console (User directory, System-wide transactions, 1-click Freeze/Unfreeze toggle).

#### Option B: Phase 11 — PRD Test Suite & README.md 🧪
- JUnit 5 / Mockito automated test suite covering all 15 required PRD Section 18 test cases.
- Comprehensive `README.md` documentation with architecture diagrams, setup instructions, and API docs.

---

## 💡 Quick Instruction for New Chat
> **Prompt to resume tomorrow**:
> *"Read `context.md`, `version_2_planning.md`, and `Digital_Wallet_CORE_DETERMINISTIC_PRD(1).pdf`. We have completed Phases 1–10 (95%). Let's continue with [Option A: Building the Frontend Web Application / Option B: Phase 11 Test Suite & README]."*





