# VaultPay frontend

Standalone React and TypeScript frontend for the Digital Wallet project.

The application connects to the Spring Boot backend through a typed HTTP adapter. It sends the JWT returned by login as a Bearer token for protected wallet and admin requests.

## Included flows

- Login and registration
- Session restoration and expiry
- Wallet balance and status
- Add money, withdraw, and transfer dialogs
- Transaction search and filtering
- Profile viewing and editing
- Role-protected admin console
- User directory and wallet freeze/activate controls
- Platform transaction view
- Responsive layouts and accessible modal/form behavior

The frontend connects to Spring Boot at `http://localhost:8080` by default. Change `VITE_API_BASE_URL` in `.env.development` if your backend uses a different URL. JWT session data is stored in browser `localStorage` under `vaultpay_session_v1`.

## Run locally

```powershell
npm install
npm run dev
```

Then open the local URL printed by Vite.

## Quality checks

```powershell
npm run typecheck
npm run lint
npm test
npm run build
```

## Project structure

```text
src/
|-- api/          # HTTP adapter, API interface, and retained mock test adapter
|-- components/   # Reusable controls, modal, tables, wallet actions
|-- context/      # Session, wallet, admin, and mutation state
|-- data/         # Mock API seed data retained for adapter tests
|-- hooks/        # Typed context hook
|-- layouts/      # Authenticated navigation shell
|-- pages/        # Auth, dashboard, transactions, profile, admin
|-- styles/       # Product-wide tokens and responsive CSS
|-- test/         # Vitest setup
`-- types/        # DTO-aligned TypeScript contracts
```

## Documents

- `DESIGN_SYSTEM.md`: visual direction and accessibility decisions
- `INTEGRATION.md`: exact Spring endpoint mapping and deployment configuration
