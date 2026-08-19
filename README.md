# 💳 Digital Wallet Backend

> A production-grade, deterministic digital wallet backend built with **Java 21**, **Spring Boot 3.x**, **Spring Security + JWT**, and **MySQL 8.x**. Engineered with strict financial ledger principles, atomic dual-wallet transactions, robust IDOR prevention, and role-based access control.

---

## 🌟 Key Highlights & Architectural Strengths

- **Atomic Financial Transactions**: Multi-wallet balance mutations execute inside isolated `@Transactional` service methods to guarantee zero balance inconsistency and automatic rollbacks on failure.
- **Stateless JWT Authentication & BCrypt**: Cryptographically signed HMAC-SHA JWT bearer authentication with configurable TTL and zero-trust stateless authorization.
- **Defensive API Design**: Whitelisted Request DTOs preventing Mass Assignment vulnerabilities, server-enforced authorization eliminating IDOR, and strict `@Digits(fraction = 2)` currency formatting.
- **Comprehensive Error Contract**: Centralized `@RestControllerAdvice` returning uniform, machine-readable JSON error payloads matching RFC 7807 standards.
- **100% Automated Test Suite**: 36 automated unit and integration test cases covering all edge cases (overdraft protection, self-transfers, wallet freeze states, RBAC enforcement, and PRD Section 21 End-to-End flows).
- **Interactive OpenAPI 3.0 / Swagger UI**: Full interactive API documentation with built-in JWT Bearer token authorization.

---

## 🛠️ Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Java 21 (LTS) |
| **Framework** | Spring Boot 3.5.x (Modular Monolith) |
| **Database** | MySQL 8.x |
| **ORM / Persistence** | Spring Data JPA / Hibernate ORM |
| **Security** | Spring Security 6.x + JJWT (HMAC-SHA-256) |
| **Validation** | Jakarta Bean Validation (Hibernate Validator) |
| **Documentation** | Springdoc OpenAPI 3.0 (Swagger UI) |
| **Build Tool** | Apache Maven 3.x |
| **Testing** | JUnit 5, Mockito, Spring Security Test, MockMvc |

---

## 🏛️ System Architecture & Package Layout

```
src/main/java/com/wallet/
├── App.java                               # Spring Boot Application Entry Point
├── config/
│   ├── OpenApiConfig.java                 # Swagger 3.0 UI & JWT Bearer Security Scheme
│   └── SecurityConfig.java                # Stateless Filter Chain, Global CORS, PasswordEncoder
├── controller/
│   ├── AdminController.java               # Admin operations (Users, Transactions, Wallet freeze/activate)
│   ├── AuthController.java                # Public endpoints (Register, Login)
│   ├── TransactionController.java         # Money transfer & transaction history
│   ├── UserController.java                # User profile inspection & update
│   └── WalletController.java              # Wallet balance, Add money, Withdraw money
├── dto/
│   ├── request/                           # Strictly validated request payloads
│   │   ├── AmountRequest.java
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── TransferRequest.java
│   │   └── UpdateProfileRequest.java
│   └── response/                          # Safe response DTOs (never exposing password hashes)
│       ├── ErrorResponse.java
│       ├── LoginResponse.java
│       ├── RegisterResponse.java
│       ├── TransactionResponse.java
│       ├── UserProfileResponse.java
│       └── WalletResponse.java
├── entity/                                # JPA database entities & Enums
│   ├── Role.java                          # USER, ADMIN
│   ├── Transaction.java                   # Ledger records with reference IDs & timestamps
│   ├── TransactionStatus.java             # SUCCESS, FAILED
│   ├── TransactionType.java               # ADD_MONEY, WITHDRAW, TRANSFER
│   ├── User.java                          # User entity
│   ├── Wallet.java                        # 1-to-1 User wallet with BigDecimal balance
│   └── WalletStatus.java                  # ACTIVE, FROZEN
├── exception/                             # Custom business exceptions & Global Handler
│   ├── EmailAlreadyExistsException.java   # 409 CONFLICT
│   ├── GlobalExceptionHandler.java        # Centralized @RestControllerAdvice
│   ├── InsufficientBalanceException.java  # 400 BAD_REQUEST
│   ├── InvalidCredentialsException.java   # 401 UNAUTHORIZED
│   ├── PhoneAlreadyExistsException.java   # 409 CONFLICT
│   ├── ResourceNotFoundException.java     # 404 NOT_FOUND
│   ├── SelfTransferException.java         # 400 BAD_REQUEST
│   └── WalletFrozenException.java         # 403 WALLET_FROZEN
├── repository/                            # Spring Data JPA repositories
│   ├── TransactionRepository.java
│   ├── UserRepository.java
│   └── WalletRepository.java
├── security/                              # Security filters and token management
│   ├── JwtAuthenticationFilter.java       # OncePerRequestFilter parsing Bearer tokens
│   └── JwtService.java                    # HMAC token signing, validation, claims extraction
└── service/                               # Core transactional business logic
    ├── AdminService.java
    ├── AuthService.java
    ├── TransactionService.java
    ├── UserService.java
    └── WalletService.java
```

---

## 📋 REST API Specification

### 1. Authentication (`/api/auth`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | Registers user and automatically provisions an `ACTIVE` wallet with `0.00` balance. |
| `POST` | `/api/auth/login` | Public | Validates credentials via BCrypt and returns signed JWT token with expiry. |

### 2. User Profile (`/api/users`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/users/me` | User / Admin | Returns authenticated user's profile information. |
| `PUT` | `/api/users/me` | User / Admin | Updates user's `name` and `phone` with uniqueness validation. |

### 3. Wallet Operations (`/api/wallet`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/wallet` | User / Admin | Retrieves authenticated user's wallet ID, balance, and status. |
| `POST` | `/api/wallet/add-money` | User / Admin | Deposits simulated funds and logs an `ADD_MONEY` transaction. |
| `POST` | `/api/wallet/withdraw` | User / Admin | Withdraws funds with overdraft protection and logs a `WITHDRAW` transaction. |

### 4. P2P Transfers & History (`/api/transactions`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/transactions/transfer` | User / Admin | Executes atomic dual-wallet transfer to receiver's email address. |
| `GET` | `/api/transactions` | User / Admin | Returns complete transaction history involving user's wallet (newest first). |

### 5. Administration (`/api/admin`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/admin/users` | `ROLE_ADMIN` | Lists all registered platform users. |
| `GET` | `/api/admin/transactions` | `ROLE_ADMIN` | Lists platform-wide transaction ledger. |
| `PUT` | `/api/admin/wallets/{id}/freeze` | `ROLE_ADMIN` | Freezes user wallet (blocks add, withdraw, transfer). |
| `PUT` | `/api/admin/wallets/{id}/activate` | `ROLE_ADMIN` | Activates frozen user wallet. |

---

## ⚙️ Environment Configuration

Create a `.env` file in the root directory (or use `.env.example` as a template):

```properties
DB_URL=jdbc:mysql://localhost:3306/digital_wallet?createDatabaseIfNotExist=true&useSSL=false
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
JWT_SECRET=your_super_secret_jwt_key_that_is_at_least_256_bits_long_for_hmac_sha!
JWT_EXPIRATION_SECONDS=3600
```

---

## 🚀 Running the Application

### 1. Build and Run via Maven Wrapper
```powershell
./mvnw.cmd spring-boot:run
```

The application starts on `http://localhost:8080`.

### 2. Interactive Swagger UI
Explore and test all endpoints directly in your browser:
```
http://localhost:8080/swagger-ui/index.html
```
Click **Authorize** (top right) and paste your Bearer token: `Bearer <your_token>`.

---

## 🧪 Running the Test Suite

Execute the entire test suite (36 unit, security, and integration test cases):

```powershell
./mvnw.cmd test
```

### Test Coverage Highlights:
- **`AuthServiceTest`**: Valid registration, auto-wallet provisioning, duplicate email rejection (409), duplicate phone rejection (409), login validation (401).
- **`WalletServiceTest`**: Add money balance increment, withdraw deduction, overdraft protection (400), frozen wallet lockouts (403).
- **`TransactionServiceTest`**: Atomic dual-wallet debit & credit, self-transfer rejection (400), non-existent receiver rejection (404), frozen counterparty handling, transaction history isolation.
- **`SecurityAndRoleIntegrationTest`**: Unauthenticated endpoint rejection (403/401), role-based authorization matrix (`ROLE_USER` vs `ROLE_ADMIN`).
- **`EndToEndAcceptanceTest`**: Complete 18-step verification per PRD Section 21.

---

## 📄 License
This project is licensed under the MIT License.
