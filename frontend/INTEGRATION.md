# Spring Boot integration

The frontend is connected to the Spring Boot API through `HttpWalletApi` in `src/api/httpWalletApi.ts`. The older `MockWalletApi` remains in the repository only as a local reference and is not used by the running app.

## Integration boundary

All pages and application state use the `WalletApi` interface in `src/api/walletApi.ts`. The active implementation is `HttpWalletApi`.

Configuration is in `.env.development`:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Copy `.env.example` when configuring another environment. Never put database credentials or `JWT_SECRET` in a Vite environment file.

## Endpoint mapping

| `WalletApi` method | Spring endpoint |
| --- | --- |
| `register` | `POST /api/auth/register` |
| `login` | `POST /api/auth/login` |
| `getProfile` | `GET /api/users/me` |
| `updateProfile` | `PUT /api/users/me` |
| `getWallet` | `GET /api/wallet` |
| `addMoney` | `POST /api/wallet/add-money` |
| `withdraw` | `POST /api/wallet/withdraw` |
| `transfer` | `POST /api/transactions/transfer` |
| `getTransactions` | `GET /api/transactions` |
| `getAdminUsers` | `GET /api/admin/users` |
| `getAdminTransactions` | `GET /api/admin/transactions` |
| `freezeWallet` | `PUT /api/admin/wallets/{walletId}/freeze` |
| `activateWallet` | `PUT /api/admin/wallets/{walletId}/activate` |

## Contract details already represented

- Request and response interfaces mirror the Java DTO field names.
- Roles use `USER` and `ADMIN`; the frontend does not expect `ROLE_ADMIN` in the JWT claim.
- JWT expiry is stored as an absolute `expiresAt` timestamp.
- Transaction direction and counterparty fields are optional for admin ledger entries.
- Error handling understands `ErrorResponse` fields: `timestamp`, `status`, `code`, `message`, and `path`.
- UI behavior is ready for `VALIDATION_ERROR`, `UNAUTHORIZED`, `FORBIDDEN`, `WALLET_FROZEN`, `NOT_FOUND`, `CONFLICT`, and `INSUFFICIENT_BALANCE`.
- A `SELF_TRANSFER` UI path is included. The backend currently maps this situation to `VALIDATION_ERROR`; align that code before integration if the more specific code is desired.

## HTTP adapter behavior

- Parse JSON only when a response has content.
- Convert non-2xx responses into `ApiError`.
- Session restoration validates the token through `GET /api/users/me`; an invalid or expired token is cleared and the user returns to login.
- Do not retry financial mutations automatically.
- Disable mutation buttons while requests are in progress.
- Re-fetch wallet and transaction history after successful financial mutations.
- Re-fetch the admin user list after freeze/activate.
- Do not trust decoded JWT claims for server authorization; they are only used to shape navigation. The backend remains authoritative.

## Backend configuration

- `SecurityConfig` accepts local Vite origins on ports 5173 and 5174 by default.
- Set `CORS_ALLOWED_ORIGINS` to a comma-separated list of real frontend origins when deploying.
- The frontend sends `Authorization: Bearer <JWT>` on protected requests.
- Swagger's BearerAuth scheme expects the raw JWT in its authorization dialog; Swagger adds the `Bearer` prefix.
- Use a dedicated test database before running end-to-end financial-flow tests.
