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

## 📊 2. Overall Progress Status: 55% / 100%

### Phase Status Breakdown:
* [x] **Phase 1: Project Setup & Configuration (100% / 8%)** — Maven, JPA/MySQL, `.env` / `application.properties`.
* [x] **Phase 2: Database Entities & Repositories (100% / 12%)** — `User`, `Wallet`, `Transaction`, Enums (`Role`, `WalletStatus`, `TransactionType`, `TransactionStatus`), Repositories.
* [x] **Phase 3: Registration & Automatic Wallet Creation (100% / 10%)** — `POST /api/auth/register`, password hashing, duplicate 409 checks, auto wallet creation with 0.00 balance.
* [x] **Phase 4: Spring Security, BCrypt & JWT Login (100% / 12%)** — `POST /api/auth/login`, `JwtService`, `JwtAuthenticationFilter`, `SecurityConfig`.
* [x] **Phase 5: User Profile & Wallet Inspection Endpoints (100% / 8%)** — `GET /api/users/me`, `PUT /api/users/me`, `GET /api/wallet`.
* [ ] **Phase 6: Add Money & Withdraw (0% / 10%)** — *NEXT UP*.
* [ ] **Phase 7: Atomic Money Transfer (0% / 12%)** — `POST /api/transactions/transfer` with `@Transactional`.
* [ ] **Phase 8: Transaction History (25% / 6%)** — `GET /api/transactions` (Repository query done, Service/Controller pending).
* [ ] **Phase 9: Admin Operations & Wallet Freeze/Activate (0% / 8%)** — `GET /api/admin/users`, `GET /api/admin/transactions`, freeze/activate endpoints, `ROLE_ADMIN`.
* [ ] **Phase 10: Exception Handling & Swagger/OpenAPI (50% / 6%)** — Springdoc OpenAPI setup + custom financial exceptions.
* [ ] **Phase 11: Unit/Integration Tests & README (0% / 8%)** — All 15 PRD Section 18 test cases + `README.md`.

---

## 📁 3. Current Codebase File Map

```
src/main/java/com/wallet/
├── App.java                               # Spring Boot Application Entry Point
├── config/
│   └── SecurityConfig.java                # Spring Security filter chain (Stateless, JWT Filter, BCrypt bean)
├── controller/
│   ├── AuthController.java                # POST /api/auth/register, POST /api/auth/login
│   ├── UserController.java                # GET /api/users/me, PUT /api/users/me
│   └── WalletController.java              # GET /api/wallet
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java              # email, password
│   │   ├── RegisterRequest.java           # name, email, phone, password
│   │   └── UpdateProfileRequest.java      # name, phone (whitelisted to prevent mass assignment)
│   └── response/
│       ├── ErrorResponse.java             # timestamp, status, code, message, path
│       ├── LoginResponse.java             # token, expiresIn
│       ├── RegisterResponse.java          # message
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
│   ├── GlobalExceptionHandler.java        # @RestControllerAdvice handling 400, 401, 404, 409, 500
│   ├── InvalidCredentialsException.java   # 401 UNAUTHORIZED
│   ├── PhoneAlreadyExistsException.java   # 409 CONFLICT
│   └── ResourceNotFoundException.java     # 404 NOT_FOUND
├── repository/
│   ├── TransactionRepository.java         # findBySenderWalletIdOrReceiverWalletIdOrderByCreatedAtDesc
│   ├── UserRepository.java                # findByEmail, existsByEmail, existsByPhone, existsByPhoneAndIdNot
│   └── WalletRepository.java              # findByUserId
├── security/
│   ├── JwtAuthenticationFilter.java       # OncePerRequestFilter parsing Bearer token -> SecurityContext
│   └── JwtService.java                    # HMAC-SHA token creation, parsing, validation, extraction
└── service/
    ├── AuthService.java                   # register, login
    ├── UserService.java                   # getUserProfile, updateUserProfile
    └── WalletService.java                 # getWalletByUserId
```

---

## 🧪 4. Verified Working APIs (Tested in Postman)

1. **`POST /api/auth/register`** ➡️ Creates `User` + initial `Wallet` (`balance: 0.00`, `status: ACTIVE`). Returns `201 Created`.
2. **`POST /api/auth/login`** ➡️ Validates credentials via BCrypt, returns signed JWT token (`expiresIn: 3600`).
3. **`GET /api/users/me`** ➡️ Extracts `userId` via `@AuthenticationPrincipal` from JWT, returns safe `UserProfileResponse` (200 OK). Blocks unauthenticated requests (403).
4. **`PUT /api/users/me`** ➡️ Updates `name`/`phone`. Checks `existsByPhoneAndIdNot` (returns 409 if taken by someone else; allows if it's the user's own phone). Validates lengths (400).
5. **`GET /api/wallet`** ➡️ Returns user's `walletId`, `balance` (`0.00`), `status` (`ACTIVE`).

---

## 🚀 5. Next Immediate Steps: Phase 6 (Add Money & Withdraw)

When resuming in the next session, begin with **Phase 6** using the same file-by-file teaching method:

### Files to build in Phase 6:
1. **`AmountRequest.java`** (`src/main/java/com/wallet/dto/request/AmountRequest.java`):
   - `@NotNull`, `@DecimalMin(value = "0.01")`, `@Digits(integer = 15, fraction = 2)`
2. **`TransactionResponse.java`** (`src/main/java/com/wallet/dto/response/TransactionResponse.java`):
   - Fields matching PRD Section 15: `referenceId`, `type`, `amount`, `status`, `remarks`, `createdAt`
3. **`InsufficientBalanceException.java`** (`src/main/java/com/wallet/exception/`):
   - HTTP 400 with code `INSUFFICIENT_BALANCE`
4. **`WalletFrozenException.java`** (`src/main/java/com/wallet/exception/`):
   - HTTP 403 with code `WALLET_FROZEN`
5. **Update `GlobalExceptionHandler.java`**:
   - Add handlers for `InsufficientBalanceException` and `WalletFrozenException`.
6. **Update `WalletService.java`**:
   - Implement `addMoney(Long userId, BigDecimal amount)`:
     - Verify wallet is `ACTIVE`.
     - `wallet.setBalance(wallet.getBalance().add(amount))`.
     - Create `Transaction` record (`ADD_MONEY`, `receiverWallet = wallet`, `senderWallet = null`, `SUCCESS`, unique UUID/`TX-...` referenceId).
     - Run under `@Transactional`.
   - Implement `withdraw(Long userId, BigDecimal amount)`:
     - Verify wallet is `ACTIVE`.
     - Check `wallet.getBalance().compareTo(amount) >= 0`. Throw `InsufficientBalanceException` if insufficient.
     - `wallet.setBalance(wallet.getBalance().subtract(amount))`.
     - Create `Transaction` record (`WITHDRAW`, `senderWallet = wallet`, `receiverWallet = null`, `SUCCESS`, unique referenceId).
     - Run under `@Transactional`.
7. **Update `WalletController.java`**:
   - `POST /api/wallet/add-money` -> `@Valid @RequestBody AmountRequest`
   - `POST /api/wallet/withdraw` -> `@Valid @RequestBody AmountRequest`

---

## 💡 Quick Instruction for New Chat
> **Prompt to resume next time**:
> *"Read `context.md` and `Digital_Wallet_CORE_DETERMINISTIC_PRD(1).pdf`. We have completed Phase 5 (55%). Let's start Phase 6 file-by-file with the same teaching and interview explanation approach."*
