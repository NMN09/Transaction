# VaultPay frontend design system

## Design thesis

VaultPay is a simulated financial product for customers and administrators. Its interface should make balances and transaction movement easy to verify without pretending to be a real bank.

The visual concept is a **living ledger**: transaction rails, reference IDs, tabular figures, and explicit credit/debit language are structural elements. The signature element is the vertical ledger rail used in the authentication screen and wallet balance panel.

## Design decisions

The initial automated recommendations favored dark glassmorphism with bright green and a handwritten typeface. Those choices were rejected because they are common fintech-template defaults and the handwritten font weakens precision.

### Color tokens

| Role | Token | Value |
| --- | --- | --- |
| Ledger ink | `--ink` | `#102A43` |
| Navigation navy | `--navy` | `#12304A` |
| Deep navigation | `--navy-deep` | `#0B2235` |
| Verified teal | `--teal` | `#0F766E` |
| Cool paper | `--paper` | `#EDF3F6` |
| Surface | `--surface` | `#FFFFFF` |
| Debit | `--debit` | `#B33B2E` |
| Credit | `--credit` | `#08735F` |

Color is never the only state indicator. Credit/debit and active/frozen states always include text and an icon.

### Typography

- **IBM Plex Sans Variable**: interface, headings, forms, and navigation
- **IBM Plex Mono**: balances, currency values, transaction references, and wallet IDs

Both fonts are installed locally through npm packages, so rendering does not depend on Google Fonts.

### Layout

```text
Desktop
+-------------------+------------------------------------------------+
| Persistent nav    | Context header                                 |
|                   +------------------------------------------------+
| Overview          | Ledger balance / primary task                  |
| Transactions      |                                                |
| Profile           | Wallet actions                                 |
| Admin (role only) |                                                |
|                   | Recent transaction ledger                     |
+-------------------+------------------------------------------------+

Mobile
+----------------------------------------------------+
| Menu   Context                         Status      |
+----------------------------------------------------+
| Balance ledger                                   |
| Wallet actions                                   |
| Responsive transaction cards                    |
+----------------------------------------------------+
```

## Interaction rules

- All interactive controls have visible keyboard focus.
- Controls have a minimum 44px interaction target.
- Forms use visible labels and inline errors connected with `aria-describedby`.
- Modal focus is trapped and returned to the triggering control on close.
- Escape closes dialogs.
- Route changes move focus to the main region.
- Reduced-motion preferences disable non-essential animation.
- Tables become labeled record cards on narrow screens instead of requiring horizontal scrolling.
- Destructive admin actions are visually separated and explicitly labeled.

## Responsive targets

The CSS is designed around representative widths of 375px, 768px, 1024px, and 1440px. The application uses content-driven fluid sizing between those points.
