# 🎨 Digital Wallet — Frontend Architecture & UI/UX Design Plan

> **Document Purpose**: Complete technical and functional specification for the Digital Wallet frontend client application connecting directly to the Spring Boot REST API (`http://localhost:8080`).

---

## 🏛️ 1. Global Navigation Architecture

The global top navigation bar persists across all authenticated views.

```
+-------------------------------------------------------------------------------------------------------+
|  ⚡ VaultPay        🟢 ACTIVE       [ Dashboard ]       [ Admin Console * ]       👤 Naman (User) ▼    |
+-------------------------------------------------------------------------------------------------------+
```
*\* Admin Console link is rendered conditionally based on client-side JWT claims.*

### Navigation Elements & Client Logic:
1. **Brand Identity**: Logo + App name (`VaultPay`).
2. **Action-Refreshed Wallet Status Badge**:
   - `🟢 ACTIVE` — Normal operating state.
   - `🔴 FROZEN` — Account restricted by administrator.
   - *Note on State Updates*: Status and balance are re-fetched from `GET /api/wallet` on view mount and immediately after every financial mutation (Add Money, Withdraw, Transfer, Admin Freeze/Activate). No WebSockets/server-push required per PRD scope.
3. **Role-Based View Switcher**:
   - **Dashboard**: Default view for all users.
   - **Admin Console**: Visible strictly when `role === 'ROLE_ADMIN'`.
4. **User Profile Dropdown Pill**:
   - Displays avatar initials + user's first name.
   - **Dropdown Items**:
     - 👤 **View & Edit Profile** (Triggers Profile Drawer/Modal).
     - 🛡️ **Role Badge** (`USER` / `ADMIN`).
     - 🚪 **Logout** (Clears token from `localStorage` & redirects to `/login`).

---

## 🔑 2. Client-Side Authentication & Session Lifecycle

### 2.1 JWT Payload Decoding (Role Extraction)
The backend `LoginResponse` strictly conforms to PRD §15 and returns:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 3600
}
```
* **Client-Side Role Extraction**: The client decodes the base64 JWT payload directly in the browser without extra network overhead:
  ```javascript
  function parseJwt(token) {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
  }

  const userRole = parseJwt(token).role; // 'USER' or 'ADMIN'
  ```

### 2.2 Proactive Session Expiry Timer & Global 401 Interceptor
Per PRD §8, JWT expiration is fixed at 1 hour (3600 seconds) with no refresh-token mechanism.
* **Proactive Expiry Timer**: Upon login, the client computes an absolute expiry timestamp (`Date.now() + expiresIn * 1000`) and starts a client-side timer:
  - **At 55 minutes**: Displays a non-intrusive warning toast: *"Your session will expire in 5 minutes. Please save any pending transfers."*
  - **At 60 minutes**: Automatically clears stored credentials and redirects to `/login` with an informational notice.
* **Unified API Client / Fetch Wrapper**: All outgoing API requests pass through a centralized HTTP client:
  ```javascript
  async function apiFetch(endpoint, options = {}) {
    const token = localStorage.getItem('wallet_jwt');
    const headers = { 'Content-Type': 'application/json', ...options.headers };
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const response = await fetch(endpoint, { ...options, headers });

    // Handle 401 Unauthorized (Expired or Invalid JWT)
    if (response.status === 401) {
      localStorage.removeItem('wallet_jwt');
      localStorage.removeItem('wallet_user');
      window.dispatchEvent(new CustomEvent('session-expired', { 
        detail: { message: 'Your session has expired (1 hour limit). Please sign in again.' } 
      }));
      return null;
    }

    return response;
  }
  ```

---

## 📱 3. Page Specifications & User Flows

```
                                  ┌────────────────────────┐
                                  │      1. Auth Page      │
                                  │   (Login / Register)   │
                                  └───────────┬────────────┘
                                              │ JWT Token Saved
                                              ▼
                             ┌──────────────────────────────────┐
                             │       Global Top Navigation      │
                             │  [Logo]   [Live Status] [Profile]│
                             └────────────────┬─────────────────┘
                                              │
              ┌───────────────────────────────┴───────────────────────────────┐
              ▼                                                               ▼
┌───────────────────────────┐                                   ┌───────────────────────────┐
│  2. Customer Dashboard    │                                   │     3. Admin Console      │
│  (For All Active Users)   │                                   │  (Only if ROLE_ADMIN)     │
├───────────────────────────┤                                   ├───────────────────────────┤
│ • Wallet Balance Hero Card│                                   │ • Platform User Directory │
│ • Quick Action Modals:    │                                   │   (With Wallet ID/Status) │
│   - Add Money Modal       │                                   │ • 1-Click Freeze/Unfreeze │
│   - Withdraw Modal        │                                   │ • Global Transaction Feed │
│   - P2P Transfer Modal    │                                   │   (Client-Side Filtering) │
│ • Enriched Ledger Table   │                                   └───────────────────────────┘
│   (Direction, Counterparty│
│    Client-Side Filtering) │
└───────────────────────────┘
```

---

### 🔐 Page 1: Authentication Portal (`/login` & `/register`)

* **Visual Style**: Centered glassmorphic card with gradient background.
* **Component Tabs**:
  1. **Sign In Tab**:
     - Inputs: `Email`, `Password`.
     - Action: Calls `POST /api/auth/login`.
     - On Success: Saves `token` to `localStorage`, decodes `role`, fetches user profile (`GET /api/users/me`), starts expiry timer, and routes to `/dashboard`.
     - On Error: Inline alert displaying *"Invalid email or password"*.
  2. **Create Account Tab**:
     - Inputs: `Full Name`, `Email`, `Phone Number` (10 digits), `Password`.
     - Validation: Instant format checks for email and 10-digit phone.
     - Action: Calls `POST /api/auth/register`.
     - On Success: Auto-switches to Sign In tab with success alert.
     - On Error: Displays conflict alert if email (409) or phone (409) is already registered.

---

### 💳 Page 2: Customer Dashboard (`/dashboard`)

The main financial command center for authenticated users.

#### 1. Wallet Balance Hero Card (Top Area)
* **Balance Display**: Prominent formatted currency (e.g., `₹ 10,000.00`).
* **Metadata**: Wallet ID badge (`Wallet #101`) + Status badge (`ACTIVE` / `FROZEN`).
* **3 Quick Action Buttons**:
  - `[ + Add Money ]` (Green Accent Button) ➡️ Opens Add Money Modal.
  - `[ ↗ Send / Transfer ]` (Blue Accent Button) ➡️ Opens P2P Transfer Modal.
  - `[ ↙ Withdraw ]` (Outline Button) ➡️ Opens Withdraw Modal.

#### 2. Action Modals

##### A. Add Money Modal
* **Purpose**: Deposit simulated funds.
* **Input**: Numeric amount.
* **Quick Select Chips**: `+₹500`, `+₹1,000`, `+₹5,000`, `+₹10,000` (autofills input).
* **API Call**: `POST /api/wallet/add-money` with body `{ "amount": 5000.00 }`.
* **Behavior**: Closes modal, re-fetches wallet balance & transaction ledger.

##### B. Withdraw Modal
* **Purpose**: Simulated withdrawal to external account.
* **Input**: Amount to withdraw.
* **Validation**: Displays current balance; blocks submission if withdrawal amount exceeds balance.
* **API Call**: `POST /api/wallet/withdraw` with body `{ "amount": 2000.00 }`.
* **Behavior**: Closes modal, re-fetches wallet balance & transaction ledger.

##### C. P2P Money Transfer Modal
* **Purpose**: Direct transfer to another registered user.
* **Inputs**:
  - `Recipient Email`: Target user's registered email.
  - `Transfer Amount`: Numeric value with 2 decimal places.
  - `Remarks / Note`: Optional description (e.g., *"Dinner payment"*).
* **Client-Side Guardrails**: Blocks transferring to self immediately in UI.
* **API Call**: `POST /api/transactions/transfer` with body `{ "receiverEmail": "...", "amount": 2000.00, "remarks": "..." }`.
* **Error Handling & User Feedback**:
  - `404 NOT_FOUND` ➡️ *"Recipient email not found. Please verify the email address."*
  - `403 WALLET_FROZEN` (Sender) ➡️ *"Your wallet is currently frozen. Transfers are blocked."*
  - `403 WALLET_FROZEN` (Receiver) ➡️ *"Cannot transfer funds: The recipient's wallet is currently frozen."*
  - `400 INSUFFICIENT_BALANCE` ➡️ *"Insufficient wallet balance to complete this transfer."*
  - `400 SELF_TRANSFER` ➡️ *"You cannot transfer money to yourself."*
* **Success Feedback**: Displays transfer receipt modal with Reference ID, counterparty name, and timestamp.

#### 3. Enriched Transaction History Table (Bottom Area)
* **Data Source**: Fetched via `GET /api/transactions` (returns complete list newest first).
* **Client-Side Search / Filter**: In-browser filtering by Reference ID, Remarks, or Counterparty without server query params:
  ```javascript
  const filteredTx = allTransactions.filter(tx => 
    tx.referenceId.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (tx.remarks && tx.remarks.toLowerCase().includes(searchTerm.toLowerCase())) ||
    (tx.counterpartyEmail && tx.counterpartyEmail.toLowerCase().includes(searchTerm.toLowerCase()))
  );
  ```
* **Columns**:
  1. **Direction & Type Chip**:
     - `+ CREDIT` (Green badge for deposits and incoming transfers)
     - `- DEBIT` (Red/Orange badge for withdrawals and outgoing transfers)
  2. **Counterparty / Description**:
     - For Transfers: `"Rahul Sharma (rahul@gmail.com)"`
     - For Add Money: `"Deposit / Add Money"`
     - For Withdraw: `"Withdrawal"`
  3. **Reference ID**: Copyable chip (e.g., `TX-9A8B7C6D`).
  4. **Remarks / Purpose**: Description string.
  5. **Date & Time**: Formatted timestamp (`Aug 19, 2026, 09:30 PM`).
  6. **Amount**: Formatted with sign (`+₹2,000.00` green / `-₹2,000.00` red).
* **Empty State**: Clean placeholder *"No transactions yet. Add money to get started!"*

---

### 👤 Component 3: Profile Drawer / Modal (`/profile`)

Triggered from the top navigation profile dropdown:

* **Read-Only Information**:
  - `Email Address`: User's primary email.
  - `Account Role`: `USER` or `ADMIN`.
  - `Member Since`: Formatted creation date.
* **Editable Information**:
  - `Full Name`: Input field.
  - `Phone Number`: Input field (10 digits).
* **Actions**:
  - `[ Save Profile Changes ]` ➡️ Calls `PUT /api/users/me` with `{ "name": "...", "phone": "..." }`.
  - `[ Sign Out ]` ➡️ Clears authentication context and returns to login.

---

### 🛡️ Page 4: Admin Management Console (`/admin`)

*Strictly accessible to users with `ROLE_ADMIN`.*

#### Sub-Tab 1: Platform User Directory
* **Data Source**: `GET /api/admin/users` (Returns `List<AdminUserResponse>` containing `{ id, name, email, phone, role, walletId, walletStatus, createdAt }`).
* **User Management Table**:
  - **Columns**: User ID (`#1`), Name, Email, Phone, Role Badge, Wallet ID (`Wallet #1`), Wallet Status Pill (`🟢 ACTIVE` / `🔴 FROZEN`).
  - **1-Click Action Button**:
    - If status is `ACTIVE` ➡️ Red button: `[ 🔒 Freeze Wallet ]` (Calls `PUT /api/admin/wallets/{walletId}/freeze`).
    - If status is `FROZEN` ➡️ Green button: `[ 🔓 Activate Wallet ]` (Calls `PUT /api/admin/wallets/{walletId}/activate`).
  - *Post-Action Behavior*: Instantly flips the status pill and action button in the table row upon receiving the `200 OK` response.

#### Sub-Tab 2: Platform-Wide Transaction Audit Feed
* **Data Source**: `GET /api/admin/transactions` (Fetches all transactions per PRD §4 without backend pagination/filtering).
* **Client-Side Search Bar**: Instant in-memory search by Reference ID:
  ```javascript
  const filteredAudit = allAdminTx.filter(tx => 
    tx.referenceId.toLowerCase().includes(adminSearchTerm.toLowerCase())
  );
  ```
* **Global Audit Table**:
  - Reference ID, Type, Amount, Status, Remarks, Created Timestamp.

---

## 🔗 4. REST API Integration Mapping

| Frontend View / Component | HTTP Method | Backend API Endpoint | Request Body / Params | Response / Error Handling |
| :--- | :--- | :--- | :--- | :--- |
| **Login Form** | `POST` | `/api/auth/login` | `{ "email", "password" }` | Returns `{ token, expiresIn }`. 401 on invalid credentials. |
| **Registration Form** | `POST` | `/api/auth/register` | `{ "name", "email", "phone", "password" }` | 409 on duplicate email/phone. |
| **Dashboard Load** | `GET` | `/api/wallet` | Bearer Token in Header | Returns `{ walletId, balance, status }`. |
| **Add Money Modal** | `POST` | `/api/wallet/add-money` | `{ "amount": 5000.00 }` | 403 if wallet is frozen. |
| **Withdraw Modal** | `POST` | `/api/wallet/withdraw` | `{ "amount": 2000.00 }` | 400 if insufficient balance, 403 if frozen. |
| **P2P Transfer Modal**| `POST` | `/api/transactions/transfer`| `{ "receiverEmail", "amount", "remarks" }`| 404 if receiver missing, 403 if sender/receiver frozen, 400 on self-transfer. |
| **Ledger History** | `GET` | `/api/transactions` | Bearer Token in Header | Returns `{ referenceId, type, amount, direction, counterpartyEmail, counterpartyName, status, remarks, createdAt }`. |
| **Profile Drawer** | `GET` | `/api/users/me` | Bearer Token in Header | Returns safe profile DTO. |
| **Profile Update** | `PUT` | `/api/users/me` | `{ "name", "phone" }` | 409 on phone collision. |
| **Admin User List** | `GET` | `/api/admin/users` | Bearer Token in Header (`ROLE_ADMIN`) | Returns `{ id, name, email, phone, role, walletId, walletStatus }`. |
| **Admin Transactions**| `GET` | `/api/admin/transactions` | Bearer Token in Header (`ROLE_ADMIN`) | Filtered client-side in memory. |
| **Freeze Wallet** | `PUT` | `/api/admin/wallets/{walletId}/freeze` | Bearer Token in Header (`ROLE_ADMIN`) | 1-click action updating row status. |
| **Activate Wallet** | `PUT` | `/api/admin/wallets/{walletId}/activate` | Bearer Token in Header (`ROLE_ADMIN`) | 1-click action updating row status. |

---

## 🎨 5. Design Tokens & UI Aesthetics

* **Color Palette**:
  - Background: Deep Dark Slate (`#0B0F19` / `#111827`)
  - Surface Cards: Translucent Glassmorphic panels with subtle borders (`rgba(255, 255, 255, 0.06)`)
  - Primary Accent: Indigo / Violet Gradient (`#6366F1` ➡️ `#8B5CF6`)
  - Success / Credit: Emerald Green (`#10B981`)
  - Warning / Debit: Amber Orange (`#F59E0B`)
  - Danger / Frozen: Crimson Red (`#EF4444`)
* **Typography**: Clean modern sans-serif (`Inter`, `system-ui`).
* **Micro-Interactions**: Smooth modal entrance fades, hover button elevations, and non-blocking toast alerts.
