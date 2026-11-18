# SpendSmart

**Smart Student Expense & Budget Tracker** — an Android app that helps students track income and expenses, stay on budget, save for goals, and manage shared costs.

**Package:** `com.example.spendsmart`  
**Min SDK:** 26 · **Target SDK:** 37 · **Language:** Java

---

## Features

### Core
- Add, edit, and delete transactions (income & expense)
- Fields: amount, category, date, note, payment method (Cash, Card, JazzCash, Easypaisa, Other)
- Student-focused categories (Food, Transport, Books, Fees, Rent, Internet/Mobile Load, Entertainment, Other)
- Income types (Pocket Money, Allowance, Scholarship, Part-time, Other)
- Custom categories
- Monthly budget + per-category budget limits
- Dashboard with balance, spent vs budget, recent transactions, and daily spend limit
- History with search and filters (type, month, category, min/max amount)
- Reports: pie chart by category, weekly bar chart, month comparison
- Dark / light / system theme
- Currency: PKR or USD
- Export CSV and PDF monthly report

### Smart
- Budget alerts at ~80% and 100% (overall + category)
- Daily spending limit recalculated from remaining budget and days left in the month
- Named savings goals with progress (e.g. “Save for a laptop”)
- Recurring transactions (weekly / monthly)
- Split bills (track money friends owe you or you owe them)
- Spending insights (e.g. spending vs last month, top category, budget pace)
- Receipt photos (camera or gallery)
- Semester mode (fees vs living-cost focus)
- App lock with PIN
- Home-screen quick-add widget

---

## Screens

| Screen | Description |
|--------|-------------|
| Splash | Brand launch screen |
| Onboarding | First-run intro (3 pages) |
| Lock | PIN unlock when app lock is enabled |
| Login / Sign up | Local account auth |
| Dashboard | Home overview + shortcuts |
| History | Full transaction list + filters |
| Budget setup | Monthly & category budgets, daily limit, semester mode |
| Stats | Insights, weekly bars, category pie chart |
| Savings goals | Create goals and add progress |
| Split bills | Shared expenses with friends |
| Add / Edit transaction | Log expense or income + receipt |
| Settings | Theme, currency, lock, recurring, export, profile |

**Bottom navigation:** Dashboard · History · Budget · Stats

---

## Tech stack

- Java 11, AndroidX, Material 3
- Architecture: Fragments + ViewModels + LiveData + manual DI (`AppContainer`)
- Local database: Room (SQLite)
- Navigation Component
- View Binding
- FileProvider (CSV/PDF/receipts)
- App widgets (quick add)

No cloud backend — all data stays on the device.

---

## Getting started

### Requirements
- Android Studio (recent stable)
- JDK 11+
- Android emulator or device (API 26+)

### Run
1. Open the project folder in Android Studio.
2. Sync Gradle.
3. Select a device/emulator.
4. Run **app**.

### First launch
1. Splash → onboarding → create an account (Sign up).
2. Set a monthly budget under **Budget**.
3. Add transactions from the Dashboard **+** button.
4. Optional: Settings → Set PIN for app lock; add a home-screen widget for quick add.

---

## Project structure

```
app/src/main/java/com/example/spendsmart/
├── data/           # Room entities, DAOs, repositories, preferences, session
├── di/             # AppContainer (dependency wiring)
├── domain/         # Auth models, categories
├── ui/             # Fragments, ViewModels, adapters, charts
├── util/           # Formatters, PDF/CSV export, receipts, notifications
├── widget/         # Quick-add home widget
├── MainActivity.java
└── SpendSmartApp.java
```

---

## Data model (high level)

- **User** — local account (hashed password)
- **Transaction** — income/expense with category, payment method, optional receipt
- **Budget** — monthly limit + legacy savings field
- **CategoryBudget** — per-category monthly limits
- **RecurringTransaction** — auto-created due entries
- **SavingsGoal** — named goals with target / saved amounts
- **SplitBill** — shared expense ledger with friends

Database: `spendsmart.db` (Room; migrations included; destructive fallback for older versions in development).

---

## Demo tips (semester presentation)

1. Sign up → set monthly + Food category budget.
2. Add a few expenses/income with different payment methods.
3. Show Dashboard daily limit and Stats insights/charts.
4. Create a savings goal and add money to it.
5. Add a split bill with a friend.
6. Attach a receipt photo.
7. Export PDF report from Settings.
8. Enable PIN lock and relaunch the app.

---

## Notes

- Notifications need POST_NOTIFICATIONS permission on Android 13+.
- Camera permission is requested when taking a receipt photo.
- Recurring rules are processed when the app resumes while logged in.

---

## License

Student / academic project — for coursework use.
