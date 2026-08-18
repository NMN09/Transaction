# Digital Wallet — Version 2.0 Architectural & Feature Roadmap

> **Document Purpose**: This document captures advanced engineering optimizations, concurrency patterns, and future architectural enhancements planned for **Version 2.0** after the deterministic MVP (v1.0) is completed.

---

## 📌 1. Advanced Concurrency & Deadlock Prevention

### 1.1 Deterministic Lock Ordering (Eliminate Circular Wait)
* **Problem in v1.0**: If User 1 (Wallet 1) sends to User 2 (Wallet 2) while User 2 sends to User 1 at the exact same millisecond, MySQL acquires row locks in reverse order, triggering an InnoDB Deadlock (Wait-For-Graph cycle) where MySQL aborts one transaction.
* **V2.0 Solution**:
  - Implement **Deterministic Resource Ordering** across all dual-wallet operations.
  - Regardless of who is the sender or receiver, always acquire locks and perform balance updates in ascending order of primary key (`wallet.id`):
    ```java
    Wallet firstLock = senderWallet.getId() < receiverWallet.getId() ? senderWallet : receiverWallet;
    Wallet secondLock = senderWallet.getId() < receiverWallet.getId() ? receiverWallet : senderWallet;
    ```
  - **Result**: Eliminates circular wait completely; concurrent opposing transfers queue sequentially and both succeed without failure.

### 1.2 Optimistic Locking with `@Version`
* Add a `version` column to the `wallets` table.
* Enables non-blocking high-throughput reads while guaranteeing that concurrent write conflicts throw `OptimisticLockException` and trigger an automated retry policy.

---

## 📌 2. Distributed Systems & Reliability Enhancements

### 2.1 Idempotency Keys (Prevent Duplicate Payments)
* Require client to send an `Idempotency-Key` header with financial requests (Transfer, Add Money, Withdraw).
* Store key with Redis / MySQL unique constraint to prevent duplicate balance deductions from network retries or fast double-clicks.

### 2.2 Double-Entry Ledger System
* Transition from single-row transactions to formal double-entry accounting (every transaction generates balanced Debit and Credit ledger entries).

## 📌 3. Security, Roles & UX Enhancements

### 3.1 Superadmin Tier & Dynamic Role Promotion
* **Context**: In real-world enterprise systems, admins are not hardcoded or manually seeded in SQL. A bootstrap `SUPER_ADMIN` provisions and manages lower-tier `ADMIN`s.
* **V2.0 Solution**:
  - Add `SUPER_ADMIN` enum value to `Role.java`.
  - Create endpoint: `PUT /api/admin/users/{id}/promote` (restricted strictly to `ROLE_SUPER_ADMIN`).
  - Audit logging for role changes and permission grants.
  - Dedicated admin dashboard/UI for role and user management.

### 3.2 Counterparty Information in Transaction History
* **Context**: Per PRD §15, `TransactionResponse` strictly returns `referenceId`, `type`, `amount`, `status`, `remarks`, `createdAt` without revealing counterparty identity.
* **V2.0 Solution**:
  - Walk the `Transaction` ➡️ `Wallet` ➡️ `User` JPA relationship chain.
  - Enrich `TransactionResponse` with counterparty metadata:
    - `counterpartyName` (e.g., "Rahul Sharma")
    - `counterpartyEmail` (e.g., "rahul@gmail.com")
    - `direction` (e.g., `DEBIT` / `CREDIT` from current user's perspective).

---

## 📌 4. Additional Future Features & Roadmap

- [ ] Scheduled / Recurring Auto-Transfers
- [ ] Wallet QR Code Generation & Scanning
- [ ] Beneficiary / Contact Book Management
- [ ] Daily/Monthly Transaction Limits & Velocity Checks
- [ ] Real-time Webhook / WebSocket Push Notifications

---

