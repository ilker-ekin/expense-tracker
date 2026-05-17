# Handoff: Vault – Expense Tracker

## Overview
Vault is a personal expense tracking web application. It allows users to log income and expenses, categorise spending, track budgets, view recurring transactions, and analyse spending patterns through charts. Designed for personal / demonstration use with Turkish Lira (₺) as the default currency.

---

## About the Design Files
The files bundled in this package (`Expense Tracker.html`, `recurring.jsx`) are **high-fidelity design references built in HTML + React**. They are prototypes showing the intended look, layout, and interactive behaviour — **not production code to ship directly**.

Your task is to **recreate these designs in your target codebase** (React, Next.js, Vue, etc.) using your project's established patterns, routing, state management, and component libraries. Treat the HTML files as a living spec — open them in a browser to interact with every screen.

---

## Fidelity
**High-fidelity.** Pixel-accurate colours, typography, spacing, animations, and interactions are all finalised. Implement these exactly as shown. Do not substitute the design system unless a production component provides equivalent visual output.

---

## Pages / Screens

### 1. Dashboard (`/` or `/dashboard`)
**Purpose:** Overview of current month's financial health.

**Layout:**
- Full-viewport flex column: top nav bar (54px) + scrollable page content (24px padding)
- **Row 1:** 4-column stat card grid (`repeat(4, 1fr)`, gap 14px)
- **Row 2:** Flex row — Spend Trend chart card (flex: 1) + Category Donut card (flex: 0 0 290px), gap 14px
- **Row 3:** Upcoming Recurring card (full width, 2-col inner grid)
- **Row 4:** Recent Transactions card (full width, 2-col inner grid)

**Stat Cards (×4):**
- Background: `--card` | Border: 1px solid `--border` | Border-radius: 12px | Padding: 20px
- Label: 11px, 600 weight, `--text2`, uppercase, letter-spacing 0.6px
- Value: 26px, 700 weight, letter-spacing -1px
- Sub-text: 12px, `--text2`
- Cards: "Monthly Spend", "Income" (accent color), "Net Savings" (green if positive, red if negative), "Budget Used"

**Spend Trend Card:**
- Period tabs: Daily / Weekly / Monthly / Yearly (pill tabs, 11px 600 weight)
- Daily/Weekly → Line chart; Monthly/Yearly → Bar chart
- Average spend shown below title as `txs t2` text
- Chart height: 155px, built with SVG + ResizeObserver

**By Category Card (290px wide):**
- Donut chart (140px, SVG) centered
- Legend below: colored 8px square dot + category name + amount, 5 items max

**Upcoming Recurring:**
- 2-column grid of transaction rows, sorted by next-due date
- Each row: category chip (34px) + name/meta + signed amount

**Recent Transactions:**
- 2-column grid, 8 most recent items
- Each row: chip (34px) + name + category·date + signed amount (green for income)

---

### 2. Transactions (`/transactions`)
**Purpose:** Full searchable, filterable transaction list.

**Layout:**
- Filter card (search box + type chips + category chips)
- Results card (count + total expenses + scrollable tx list)

**Filter bar:**
- Search input with left-aligned search icon (padding-left: 34px)
- Type chips: All / Expense / Income
- Category chips: All + 8 categories (wrap)
- Active chip: `--accent` background, black text

**Transaction row:**
- 36×36px category chip (border-radius 9px, category bg + color + 2-letter abbreviation)
- Name (13px 500) + meta line (11px `--text2`)
- Amount: 14px 600, tabular-nums; income = accent color, expense = `--text`
- Edit (pencil) + Delete (trash) icon buttons (26×26px, border-radius 6px)

---

### 3. Add / Edit Transaction (`/add`)
**Purpose:** Form to create or modify a transaction.

**Layout:** Centered column, max-width 580px

**Form fields (inside card):**
- Type selector: 2-button grid (Expense red tint / Income green tint)
- Amount (₺) + Date — 2-column grid
- Description — full width
- Category — select, only shown for expenses
- Submit button — full width, accent background

**Validation:**
- Description required
- Amount must be > 0 and numeric
- Inline error message in `--red` at 12px

---

### 4. Categories (`/categories`)
**Purpose:** View and manage spending categories with per-category budget progress.

**Layout:** 3-column card grid

**Category card:**
- Header: 40px chip + name/tx-count + edit icon button
- Progress row: "Spent" label + "₺X,XXX / ₺X,XXX" amount
- 5px progress bar (accent color, amber if >80%, red if over budget)
- Over-budget warning line in red

---

### 5. Recurring (`/recurring`)
**Purpose:** Manage recurring income and expense templates.

**Layout:**
- 3-column stat row (Monthly Expenses in red, Monthly Income in accent, Net in conditional)
- Add/Edit form card (collapsible)
- Filter chips: All / Expense / Income
- 2-column card grid

**Recurring card:**
- 38px chip + description + frequency badge + monthly-equivalent note
- Amount (18px 700, signed, colored)
- Due date with amber highlight if ≤ 5 days
- Active/Paused toggle (40×22px pill toggle)
- Edit + Delete icon buttons

**Frequency badges:**
| Freq | Background | Color |
|------|-----------|-------|
| daily | `rgba(6,182,212,.14)` | `#06b6d4` |
| weekly | `rgba(168,85,247,.14)` | `#a855f7` |
| monthly | `rgba(34,197,94,.14)` | `#22c55e` |
| yearly | `rgba(245,158,11,.14)` | `#f59e0b` |

**Monthly equivalent multipliers:**
- daily × 30, weekly × 4.33, monthly × 1, yearly ÷ 12

---

### 6. Budgets & Goals (`/budgets`)
**Purpose:** Track monthly spending against per-category budgets.

**Layout:**
- 3 stat cards (Total Budget, Spent So Far, Remaining)
- Full-width overall budget progress card (10px bar height)
- Stacked per-category budget rows

**Per-category row (card, padding 14px 18px):**
- 30px chip + name + remaining/over text
- Amount right-aligned (spent + /budget)
- 5px progress bar

---

### 7. Reports (`/reports`)
**Purpose:** Visual spending analysis with period/month selectors.

**Layout:** 2-column grid (×2 rows)
- Top-left: Spend Trend card with Daily/Weekly/Monthly/Yearly tabs
- Top-right: Donut chart (190px) — Spending by Category
- Bottom-left: Weekly Breakdown line chart (170px)
- Bottom-right: Category Share progress bars

**Month selector:** `<input type="month">` top-right of page header, filters donut + share bars.

---

### 8. Settings (`/settings`)
**Purpose:** Profile, preferences, and data management.

**Layout:** Centered column, max-width 660px

**Sections:**
1. Profile card — 58px avatar circle + name/email inputs (2-col grid)
2. Preferences card — Dark Mode toggle, Currency display, Start of Week select, Budget Alerts toggle
3. Data card — Export CSV, Export JSON, Clear All Data buttons
4. Footer card — version string

---

## Design Tokens

### Colors
```
/* Accent (user-configurable via tweaks) */
--accent:     #22c55e   /* Green (default) */
--accent-dim: rgba(34,197,94,.12)

/* Status */
--red:   #ef4444
--amber: #f59e0b

/* Dark theme */
--bg:      #090909
--surface: #111111
--card:    #161616
--border:  #222222
--text:    #f0efed
--text2:   #8a8a8a
--muted:   #2e2e2e

/* Light theme */
--bg:      #f3f3f1
--surface: #eaeae8
--card:    #ffffff
--border:  #e0e0de
--text:    #0d0d0d
--text2:   #666666
--muted:   #d0d0ce
```

**Accent color variants (user-selectable):**
| Name | Hex |
|------|-----|
| Green (default) | `#22c55e` |
| Blue | `#3b82f6` |
| Purple | `#a855f7` |
| Amber | `#f59e0b` |

### Category Colors
| Category | Color | Background tint |
|----------|-------|-----------------|
| Food & Dining | `#22c55e` | `rgba(34,197,94,.13)` |
| Transport | `#3b82f6` | `rgba(59,130,246,.13)` |
| Entertainment | `#a855f7` | `rgba(168,85,247,.13)` |
| Shopping | `#f59e0b` | `rgba(245,158,11,.13)` |
| Health | `#ef4444` | `rgba(239,68,68,.13)` |
| Bills & Utilities | `#6b7280` | `rgba(107,114,128,.13)` |
| Education | `#06b6d4` | `rgba(6,182,212,.13)` |
| Other | `#f97316` | `rgba(249,115,22,.13)` |

### Typography
```
Font family: 'Space Grotesk', sans-serif
Weights used: 400, 500, 600, 700

Page title:     21px / 700 / letter-spacing -0.5px
Section title:  14px / 600
Body:           13px / 400–500
Small:          12px (class: tsm)
Extra small:    11px (class: txs)
Stat value:     26px / 700 / letter-spacing -1px
Nav label:      13px / 500
Amount large:   18px / 700 (recurring cards)
Amount normal:  14px / 600 / tabular-nums
```

### Spacing
```
Page padding:    24px
Card padding:    20px
Card gap:        14px
Border-radius:   12px (cards), 9px (chips), 8px (inputs/buttons), 7px (nav buttons)
Nav height:      54px
```

### Shadows
- Tweaks panel: `0 16px 60px rgba(0,0,0,.5)`
- Toggle knob: `0 1px 4px rgba(0,0,0,.3)`
- Period tab (active): `0 1px 3px rgba(0,0,0,.2)`

### Transitions
```
Theme switch:       background 0.25s, color 0.25s
Button hover:       all 0.15s
Toggle:             background 0.2s, transform 0.2s
Progress bar fill:  width 0.4s
Page enter:         translateY(8px → 0) 0.18s ease
Opacity (paused):   opacity 0.2s (recurring paused = 0.5)
```

---

## Interactions & Behaviour

### Navigation
- Single-page app; all navigation is client-side state (`page` string)
- Active nav item: accent-dim background + accent text color
- "Add" nav item always resets the edit form to blank

### Charts
All charts are SVG, built with `ResizeObserver` on a wrapper div. They recompute on container resize. Use a charting library (Recharts, Chart.js, Victory) as a production replacement.

| Chart type | Pages used |
|------------|-----------|
| Bar | Spend Trend (monthly/yearly), Reports |
| Line | Spend Trend (daily/weekly), Reports weekly breakdown |
| Donut | Dashboard category, Reports |
| Progress bar | Budgets, Categories, Reports category share |

### Forms
- Add/Edit transaction: validates on submit; shows inline red error
- Recurring form: toggled by "New" button; collapses on save/cancel
- All inputs use `--surface` background, `--border` border, accent focus ring

### Dark/Light Mode
- Toggled via sun/moon icon button in top-right nav
- Also togglable in Settings preferences
- Persisted via `data-theme` attribute on `<body>`

### Recurring active toggle
- Toggling active/paused dims card to `opacity: 0.5`
- Paused items excluded from dashboard Upcoming section and summary totals

---

## State Management

```
txs:          Transaction[]     — all transactions (expense + income)
recurring:    Recurring[]       — all recurring templates
page:         string            — current active page
dark:         boolean           — dark mode on/off
editing:      Transaction|null  — transaction being edited (null = new)
accent:       string            — hex color for --accent CSS var
```

### Transaction shape
```ts
{
  id:       number | string
  date:     string            // YYYY-MM-DD
  category: string            // category id or 'income'
  desc:     string
  amount:   number            // always positive
  type:     'expense' | 'income'
}
```

### Recurring shape
```ts
{
  id:       string
  desc:     string
  amount:   number
  category: string
  type:     'expense' | 'income'
  freq:     'daily' | 'weekly' | 'monthly' | 'yearly'
  nextDate: string            // YYYY-MM-DD
  active:   boolean
}
```

---

## Assets & Icons
All icons are inline SVG drawn with `stroke="currentColor"` at 15×15px (13×13px for action icons). No external icon library required — or substitute with Lucide React / Heroicons using the same names:

`grid, list, plus, tag, target, bar-chart-2, settings, moon, sun, x, edit-2, trash-2, search, download, repeat`

No images used. Avatar is a single letter in an accent-colored circle.

---

## Files
| File | Description |
|------|-------------|
| `Expense Tracker.html` | Main app — all pages except Recurring |
| `recurring.jsx` | Recurring page + UpcomingSection dashboard widget |

Open both files in a browser to interact with every screen before implementing.

---

## Currency
- Symbol: `₺` (Turkish Lira)
- Locale formatting: `tr-TR` with `minimumFractionDigits: 2`
- Example: `₺1.249,99`

---

## Notes for Developer
1. Replace mock data arrays (`INIT_TX`, `INIT_RECURRING`) with real API calls.
2. Charts are custom SVG — replace with Recharts or Chart.js in production.
3. Accent color is user-configurable; store the selected hex in user preferences.
4. Dark mode preference should be persisted to localStorage.
5. The `fmt()` function uses `Intl.NumberFormat` with `tr-TR` locale — use the same in production for consistent number formatting.
6. All monetary calculations use plain JS numbers; consider using a library like `dinero.js` for precision in production.
