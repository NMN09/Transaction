# Digital Wallet Backend — Project Context & Handover Document

> **Purpose of this file**: This document preserves 100% of the project context, architectural decisions, completed work, testing history, and the next steps. When starting a new session, reading this file enables immediate continuation without any lost context.

---

## 📌 1. Project Overview & Rules
* **Project Name**: Digital Wallet Backend & Full-Stack Platform
* **Specification Source**: `Digital_Wallet_CORE_DETERMINISTIC_PRD(1).pdf` (in root directory).
* **Tech Stack**: Java 21, Spring Boot 3.5.x, Maven, MySQL 8.x, Spring Data JPA / Hibernate, Spring Security 6.x + JJWT, Jakarta Validation, BCrypt.
* **Core Philosophy**: Deterministic implementation with zero financial inconsistencies. Atomic multi-wallet balance mutations, stateless JWT authentication, IDOR and Mass Assignment defenses, and RFC 7807 uniform JSON error contracts.

---

## 📊 2. Overall Progress Status: 100% / 100% (Core Deterministic MVP + Phase 11 Completed)

### Phase Status Breakdown:
* [x] **Phase 1: Project Setup & Configuration (100%)** — Maven, JPA/MySQL, `.env` / `application.properties`.
* [x] **Phase 2: Database Entities & Repositories (100%)** — `User`, `Wallet`, `Transaction`, Enums (`Role`, `WalletStatus`, `TransactionType`, `TransactionStatus`), Repositories.
* [x] **Phase 3: Registration & Automatic Wallet Creation (100%)** — `POST /api/auth/register`, password hashing, duplicate 409 checks, auto wallet creation with 0.00 balance.
* [x] **Phase 4: Spring Security, BCrypt & JWT Login (100%)** — `POST /api/auth/login`, `JwtService`, `JwtAuthenticationFilter`, `SecurityConfig`.
* [x] **Phase 5: User Profile & Wallet Inspection Endpoints (100%)** — `GET /api/users/me`, `PUT /api/users/me`, `GET /api/wallet`.
* [x] **Phase 6: Add Money & Withdraw (100%)** — `POST /api/wallet/add-money`, `POST /api/wallet/withdraw`, balance mutation, transaction ledger creation, `@Transactional`.
* [x] **Phase 7: Atomic Money Transfer (100%)** — `POST /api/transactions/transfer` with `@Transactional`, IDOR safety, freeze & balance checks.
* [x] **Phase 8: Transaction History (100%)** — `GET /api/transactions` (Sorted newest first, IDOR safe, `@Transactional(readOnly = true)`).
* [x] **Phase 9: Admin Operations & Wallet Freeze/Activate (100%)** — `GET /api/admin/users`, `GET /api/admin/transactions`, `PUT /api/admin/wallets/{id}/freeze`, `PUT /api/admin/wallets/{id}/activate`, `ROLE_ADMIN` protection.
* [x] **Phase 10: Exception Handling & Swagger/OpenAPI (100%)** — Springdoc OpenAPI 3.0 UI, JWT Bearer Scheme, global CORS enabled.
* [x] **Phase 11: Unit/Integration Tests & Documentation (100%)** — 36 automated tests passing with 0 failures, comprehensive `README.md`, and complete `FRONTEND_DESIGN.md`.

---

## 📁 3. Current Codebase File Map

```
src/main/java/com/wallet/
├── App.java                               # Spring Boot Application Entry Point
├── config/
│   ├── OpenApiConfig.java                 # Swagger 3.0 UI & JWT Bearer Security Scheme
│   └── SecurityConfig.java                # Stateless Filter Chain, 401/403 Error Handlers, CORS, PasswordEncoder
├── controller/
│   ├── AdminController.java               # Admin operations (Users, Transactions, Wallet freeze/activate)
│   ├── AuthController.java                # Public endpoints (Register, Login)
│   ├── TransactionController.java         # Money transfer & transaction history
│   ├── UserController.java                # User profile inspection & update
│   └── WalletController.java              # Wallet balance, Add money, Withdraw money
├── dto/
│   ├── request/                           # Strictly validated request payloads
│   │   ├── AmountRequest.java             # amount (NotNull, DecimalMin, Digits)
│   │   ├── LoginRequest.java              # email, password
│   │   ├── RegisterRequest.java           # name, email, phone, password
│   │   ├── TransferRequest.java           # receiverEmail, amount, remarks
│   │   └── UpdateProfileRequest.java      # name, phone (whitelisted to prevent mass assignment)
│   └── response/                          # Safe response DTOs (never exposing password hashes)
│       ├── AdminUserResponse.java         # id, name, email, phone, role, walletId, walletStatus, createdAt
│       ├── ErrorResponse.java             # timestamp, status, code, message, path
│       ├── LoginResponse.java             # token, expiresIn (3600)
│       ├── RegisterResponse.java          # message
│       ├── TransactionResponse.java       # referenceId, type, amount, direction, counterparty, status, remarks, createdAt
│       ├── UserProfileResponse.java       # id, name, email, phone, role, createdAt
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
│   ├── GlobalExceptionHandler.java        # Centralized @RestControllerAdvice
│   ├── InsufficientBalanceException.java  # 400 BAD_REQUEST
│   ├── InvalidCredentialsException.java   # 401 UNAUTHORIZED
│   ├── PhoneAlreadyExistsException.java   # 409 CONFLICT
│   ├── ResourceNotFoundException.java     # 404 NOT_FOUND
│   ├── SelfTransferException.java         # 400 BAD_REQUEST
│   └── WalletFrozenException.java         # 403 WALLET_FROZEN
├── repository/
│   ├── TransactionRepository.java         # findBySenderWalletIdOrReceiverWalletIdOrderByCreatedAtDesc, findAllByOrderByCreatedAtDesc
│   ├── UserRepository.java                # findByEmail, existsByEmail, existsByPhone, existsByPhoneAndIdNot
│   └── WalletRepository.java              # findByUserId
├── security/
│   ├── JwtAuthenticationFilter.java       # OncePerRequestFilter parsing Bearer token -> SecurityContext
│   └── JwtService.java                    # HMAC-SHA token creation, parsing, validation, extraction
└── service/
    ├── AdminService.java                  # getAllUsers (with wallet metadata), getAllTransactions, freezeWallet, activateWallet
    ├── AuthService.java                   # register, login
    ├── TransactionService.java            # transfer, getTransactionHistory (with direction & counterparty)
    ├── UserService.java                   # getUserProfile, updateUserProfile
    └── WalletService.java                 # getWalletByUserId, addMoney, withdraw

src/test/java/com/wallet/
├── AppTests.java                          # Spring context boot test
├── EndToEndAcceptanceTest.java            # PRD §21 18-step acceptance test on MySQL
├── controller/
│   └── SecurityAndRoleIntegrationTest.java# 401 Unauthorized & 403 Forbidden RBAC tests
├── security/
│   └── JwtServiceTest.java                # Token generation & signature validation tests
└── service/
    ├── AdminServiceTest.java              # Admin queries & wallet moderation tests
    ├── AuthServiceTest.java               # Registration, duplicate conflict, login tests
    ├── TransactionServiceTest.java        # Atomic transfer, rollback, counterparty tests
    ├── UserServiceTest.java               # Profile retrieval & update tests
    └── WalletServiceTest.java             # Add money, withdraw, overdraft, freeze tests
```

---

## 🧪 4. Automated Test Suite (36 Tests Passing — 0 Failures)

Ran via `./mvnw.cmd test`:
```
[INFO] Results:
[INFO] Tests run: 36, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 📋 5. Preserved Action Items for Next Session

Based on our architectural review, here is the exact checklist to tackle in the next session:

### 🛠️ Backend Enhancements & Minor Fixes:
1. **Self-Transfer Error Code**: In `GlobalExceptionHandler.java`, ensure `SelfTransferException` maps to code `"SELF_TRANSFER"` (instead of generic `"VALIDATION_ERROR"`).
2. **Phone Number Regex Validation**: Add `@Pattern(regexp = "\\d{10}", message = "Phone must be exactly 10 digits")` to `RegisterRequest.java` and `UpdateProfileRequest.java`.
3. **Hide DevTools Stack Traces**: Add `server.error.include-stacktrace=never` in `src/main/resources/application.properties` to ensure zero stack trace leakage in all environments.
4. **CORS Deployment Policy**: Review allowed origins in `SecurityConfig.java` for production locking.

### 🎨 Frontend Implementation Items:
1. **Role Check Alignment**: Ensure frontend navigation checks `userRole === "ADMIN"` (since JWT claim payload contains `"ADMIN"`, not `"ROLE_ADMIN"`).
2. **Session Timer Persistence**: Store `expiresAt` (`Date.now() + 3600000`) in `localStorage` so refreshing the browser tab (F5) accurately preserves the remaining session time rather than resetting to 60 minutes.
3. **Build Frontend Web App**: Implement the complete modern, glassmorphic UI as specified in `FRONTEND_DESIGN.md` (Auth, Dashboard, Modals, History Table, Admin Panel).

---

## 💡 Quick Instruction for New Chat
> **Prompt to resume next session**:
> *"Read `context.md`, `FRONTEND_DESIGN.md`, and `README.md`. All 36 backend tests pass. Let's work through the checklist in Section 5 of `context.md` (minor backend validations & building the frontend web app)."*
