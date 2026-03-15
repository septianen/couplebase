# Couplebase — Product Requirements Document (PRD)

**Version**: 1.0
**Date**: March 10, 2026
**Product**: Couplebase — Wedding Planning & Marriage Life App
**Platforms**: Android, iOS, Web
**Design Reference**: [Figma — Marriage Life Planner UI](https://www.figma.com/make/rBPqFJgEB7v5CIKQTKyPzr/Marriage-Life-Planner-UI?t=38Ne08G2CESZUWff-0)

---

## 1. Product Overview

### 1.1 Vision

Couplebase is the all-in-one companion for couples — from wedding planning to building a life together. It syncs in real-time between partners, works offline, and runs natively on Android, iOS, and Web from a single codebase.

### 1.2 Problem Statement

Couples today juggle multiple tools for wedding planning (spreadsheets, Pinterest, apps) and marriage life management (budget trackers, note apps, calendars). There is no single app that:

- Covers both **wedding planning** and **ongoing marriage life**
- **Syncs in real-time** between two partners
- Works **offline** and across all platforms
- Transitions seamlessly from pre-wedding to post-wedding use

### 1.3 Target Users

- **Primary**: Engaged couples planning their wedding (ages 24-38)
- **Secondary**: Married couples managing finances, goals, and communication
- **Tertiary**: Couples in long-term relationships preparing for engagement

### 1.4 Key Value Propositions

- **One app for the whole journey** — wedding → marriage → life goals
- **Real-time partner sync** — both see changes instantly
- **Offline-first** — works without internet, syncs when connected
- **Cross-platform** — Android, iOS, and Web from one codebase
- **Privacy-first** — couple data isolated via row-level security

---

## 2. Tech Stack

### 2.1 Architecture Overview

```
Compose UI → Decompose Component (MVI) → Use Case → Repository

                                                      ├── SQLDelight (local, source of truth)

                                                      └── Supabase (remote, sync)
```

### 2.2 Stack Table

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **UI** | Compose Multiplatform 1.7+ | Declarative cross-platform UI |
| **Design** | Material 3 | Theming, adaptive layouts |
| **Navigation** | Decompose 3.x + Essenty | Lifecycle-aware multiplatform navigation |
| **Local DB** | SQLDelight 2.x | Typesafe offline-first database |
| **Backend** | Supabase Kotlin SDK | Auth, PostgreSQL, Realtime, Storage |
| **Networking** | Ktor Client 3.x | HTTP client (OkHttp/Darwin/Js engines) |
| **DI** | Koin 4.x | Lightweight multiplatform DI |
| **Async** | Kotlinx Coroutines + Flow | Reactive async programming |
| **Serialization** | kotlinx-serialization | JSON encoding/decoding |
| **Date/Time** | kotlinx-datetime | Multiplatform date handling |
| **Images** | Coil 3 | Cross-platform image loading & caching |
| **Testing** | kotlin.test + Turbine + Mokkery | Unit, Flow, and mock testing |
| **Build** | Gradle + Convention Plugins + Version Catalog | DRY build configuration |

### 2.3 Design Patterns

| Pattern | Where | How |
|---------|-------|-----|
| **MVI** (Model-View-Intent) | Every screen | Immutable `UiState` + sealed `Intent` + `Component` |
| **Repository** (Offline-First) | Data layer | Reads from SQLDelight, writes local-first then queues sync |
| **Use Case** | Domain layer | Single-responsibility business operations |
| **Decompose Components** | Navigation/State | Lifecycle-aware state holders (replaces ViewModel) |
| **Queue-Based Sync** | Core sync | Pending changes queue → push to Supabase when online |
| **expect/actual** | Platform code | Camera, notifications, connectivity, deep links |

### 2.4 Code Examples

**MVI Pattern:**

```kotlin
@Immutable
data class ChecklistUiState(
    val items: List<ChecklistItem> = emptyList(),
    val isLoading: Boolean = false,
    val filter: ChecklistFilter = ChecklistFilter.ALL
)

sealed interface ChecklistIntent {
    data class ToggleItem(val id: String) : ChecklistIntent
    data class DeleteItem(val id: String) : ChecklistIntent
    data class ChangeFilter(val filter: ChecklistFilter) : ChecklistIntent
}

class ChecklistComponent(
    componentContext: ComponentContext,
    private val toggleChecklistItemUseCase: ToggleChecklistItemUseCase,
) : ComponentContext by componentContext {
    private val _state = MutableStateFlow(ChecklistUiState())
    val state: StateFlow<ChecklistUiState> = _state.asStateFlow()

    fun onIntent(intent: ChecklistIntent) { /* ... */ }
}
```

**Offline-First Repository:**

```kotlin
class ChecklistRepositoryImpl(
    private val local: ChecklistLocalDataSource,
    private val remote: ChecklistRemoteDataSource,
    private val syncManager: SyncManager
) : ChecklistRepository {
    override fun getItems(coupleId: String): Flow<List<ChecklistItem>> =
        local.getItems(coupleId).map { it.toDomainList() }

    override suspend fun toggleItem(id: String, completed: Boolean) {
        local.updateItem(id, completed, SyncStatus.PENDING)
        syncManager.enqueueSync(SyncTask.Checklist(id))
    }
}
```

**Sync Engine:**

```kotlin
class SyncManager(
    private val connectivity: ConnectivityMonitor,
    private val syncQueue: SyncQueue,
    private val supabase: SupabaseClient
) {
    fun startSync() {
        connectivity.isOnline.filter { it }.collect { processQueue() }
    }

    fun subscribeToPartnerChanges(coupleId: String) {
        supabase.realtime.channel("couple:$coupleId")
            .on<PostgresAction.Insert>("checklist_item") { /* upsert local */ }
    }
}
```

---

## 3. Project Structure

### 3.1 Module Map

```
couplebase/
├── build-logic/convention/              # Convention plugins
├── core/
│   ├── :core:common                     # Extensions, Result wrapper, UUID
│   ├── :core:model                      # Domain data classes (pure Kotlin)
│   ├── :core:database                   # SQLDelight schemas, DAOs, migrations
│   ├── :core:network                    # Supabase client, API models
│   ├── :core:sync                       # SyncManager, queue, connectivity
│   ├── :core:domain                     # Base UseCase, common use cases
│   ├── :core:datastore                  # Preferences (DataStore multiplatform)
│   └── :core:ui                         # Design system, theme, shared components
├── feature/
│   ├── :feature:auth                    # Login, signup, couple pairing
│   ├── :feature:wedding-checklist       # Wedding task checklist
│   ├── :feature:wedding-budget          # Wedding budget tracker
│   ├── :feature:wedding-guests          # Guest list & RSVP
│   ├── :feature:wedding-vendors         # Vendor management
│   ├── :feature:wedding-timeline        # Wedding day schedule
│   ├── :feature:couple-profile          # Couple profile & milestones
│   ├── :feature:couple-goals            # Life goals & shared calendar
│   ├── :feature:finance-budget          # Monthly budgets
│   ├── :feature:finance-expenses        # Expense tracking
│   ├── :feature:finance-savings         # Savings goals
│   ├── :feature:comm-notes              # Shared notes
│   ├── :feature:comm-journal            # Love journal / diary
│   ├── :feature:comm-checkin            # Daily check-ins & mood
│   └── :feature:settings                # App settings
├── composeApp/
│   ├── commonMain/                      # Root navigation, App.kt, root DI
│   ├── androidMain/                     # MainActivity, platform specifics
│   ├── iosMain/                         # MainViewController
│   └── wasmJsMain/                      # Browser entry point
├── gradle/libs.versions.toml
└── settings.gradle.kts
```

### 3.2 Module Dependency Graph

```
composeApp → feature:* → core:domain → core:model
                        → core:ui    → core:common

core:domain → core:database → core:model + core:common
            → core:network  → core:model + core:common
            → core:sync     → core:database + core:network + core:common
```

### 3.3 Feature Module Internal Structure

```
feature/<name>/src/commonMain/kotlin/com/couplebase/feature/<name>/
├── di/              # Koin module
├── domain/
│   ├── usecase/     # Feature-specific use cases
│   └── repository/  # Repository interface
├── data/
│   ├── repository/  # Repository implementation (offline-first)
│   ├── local/       # SQLDelight data source
│   └── remote/      # Supabase data source
├── presentation/
│   ├── <Name>Component.kt    # Decompose component (MVI)
│   ├── <Name>UiState.kt      # Immutable state
│   ├── <Name>Intent.kt       # User actions
│   └── <Name>Screen.kt       # Compose UI
└── navigation/
    └── <Name>Navigation.kt   # Entry point for parent nav
```

---

## 4. Feature Specifications

### 4.1 Auth & Couple Pairing (:feature:auth)

| Capability | Description |
|-----------|-------------|
| Email auth | Sign up & login with email/password via Supabase GoTrue |
| Social login | Google (all platforms) + Apple (iOS/Web) via expect/actual |
| Create couple space | Generate 6-char invite code + shareable deep link |
| Join via invite code | Enter 6-character alphanumeric code |
| Join via QR code | Scan partner's QR (CameraX/AVFoundation/jsQR) |
| Session management | Auto token refresh, persistent login |
| RLS | Supabase Row-Level Security scopes all data to couple_id |

### 4.2 Wedding Planning

**Checklist (:feature:wedding-checklist)**

| Capability | Description |
|-----------|-------------|
| Templates | Pre-populated tasks: 12mo, 6mo, 3mo, 1mo, final week |
| Custom tasks | User-created tasks with title, category, due date |
| Assignment | Assign each task: Me / Partner / Both |
| Filtering | All, Mine, Partner's, Completed |
| Reordering | Drag-to-reorder within category groups |
| Completion | Toggle complete/incomplete with sync |

**Budget (:feature:wedding-budget)**

| Capability | Description |
|-----------|-------------|
| Total budget | Set overall wedding budget amount |
| Categories | Predefined + custom (venue, catering, photo, attire, flowers, music, etc.) |
| Allocation | Set budget per category |
| Expense tracking | Log expenses against categories |
| Visualization | Donut chart (total) + progress bars (per category) |
| Over-budget alerts | Warning when category exceeds allocation |

**Guest List (:feature:wedding-guests)**

| Capability | Description |
|-----------|-------------|
| Guest management | Add/edit/delete guests |
| Import | CSV import + device contacts |
| RSVP tracking | Pending, Accepted, Declined statuses |
| Meal preferences | Free-text dietary/meal selection |
| Seating | Table number assignment |
| Plus-ones | Track plus-one per guest |
| Dashboard | Summary stats (total, accepted, declined, pending) |
| Export | Export to CSV |

**Vendors (:feature:wedding-vendors)**

| Capability | Description |
|-----------|-------------|
| Vendor cards | Name, category, phone, email, website |
| Categories | Photographer, florist, DJ, caterer, venue, bakery, etc. |
| Contracts | Upload files → Supabase Storage |
| Payment schedule | Track deposits, installments, final payments with due dates |
| Notes & ratings | Free-text notes per vendor |

**Timeline (:feature:wedding-timeline)**

| Capability | Description |
|-----------|-------------|
| Schedule builder | Add time blocks for wedding day events |
| Time blocks | Start time, duration, location, description |
| People assignment | Assign couple, vendors, wedding party to blocks |
| Sharing | Export as shareable format (PDF/image) |

### 4.3 Couple Profile & Goals

**Profile (:feature:couple-profile)**

| Capability | Description |
|-----------|-------------|
| Couple profile | Shared photo, couple name, together-since date |
| Milestones | Timeline of relationship milestones (first date, engaged, etc.) |
| Status | Relationship stage (dating, engaged, married) |

**Goals (:feature:couple-goals)**

| Capability | Description |
|-----------|-------------|
| Life goals | Shared goals with title, description, target date |
| Milestones | Sub-milestones per goal with completion tracking |
| Progress | Visual percentage progress |
| Calendar | Shared calendar for important dates/reminders |

### 4.4 Finance Management

**Budget (:feature:finance-budget)**

| Capability | Description |
|-----------|-------------|
| Monthly budgets | Set income and spending categories per month |
| Category limits | Set spending limit per category |
| Progress tracking | Visual progress bars per category |
| Comparison | Month-over-month view |

**Expenses (:feature:finance-expenses)**

| Capability | Description |
|-----------|-------------|
| Quick entry | Amount, description, category, date |
| Assignment | Me / Partner / Split |
| Receipt capture | Photo → Supabase Storage |
| Search & filter | By category, date range, partner |

**Savings (:feature:finance-savings)**

| Capability | Description |
|-----------|-------------|
| Goals | Title, target amount, target date |
| Contributions | Log contributions with date and amount |
| Visualization | Ring chart / thermometer progress |
| History | Contribution history timeline |

### 4.5 Communication

**Notes (:feature:comm-notes)**

| Capability | Description |
|-----------|-------------|
| Shared notes | Title + body (basic markdown) |
| Pin | Pin important notes to top |
| Real-time | Both partners edit, synced via Realtime |

**Journal (:feature:comm-journal)**

| Capability | Description |
|-----------|-------------|
| Entries | Date, text, photo attachments |
| Privacy | Private (only you) vs Shared (both see) |
| Timeline | Chronological date-based view |
| Memories | "On this day" feature from past entries |

**Check-in (:feature:comm-checkin)**

| Capability | Description |
|-----------|-------------|
| Daily mood | Select emoji mood |
| Reflection | Optional one-line text |
| Partner view | See partner's check-in after both submit |
| History | Weekly mood chart for both partners |

### 4.6 Settings (:feature:settings)

| Capability | Description |
|-----------|-------------|
| Account | View/edit name, email, change password |
| Theme | Light / Dark / System |
| Notifications | Toggle per notification type |
| Currency | Select display currency |
| Data export | Export all data |
| Sync status | View current sync state |
| Couple space | View invite code, edit wedding date, leave/delete space |
| Logout | Sign out |

---

## 5. UI Design

### 5.1 Design Language

**Style**: Modern & Minimal

| Token | Value |
|-------|-------|
| Background | #FAFAFA (light), #121212 (dark) |
| Surface | #FFFFFF (light), #1E1E1E (dark) |
| Border | #E0E0E0 (light), #333333 (dark) |
| Primary accent | Deep Rose #C2185B |
| Primary tint | Light Rose #FCE4EC |
| Secondary accent | Soft Teal #00897B (success, finance) |
| Error | #D32F2F |
| Warning | #F57C00 |
| Typography | System sans-serif (Roboto/SF Pro) |
| Corner radius | 12dp (cards), 8dp (buttons), 24dp (chips) |
| Base spacing | 16dp grid |
| Elevation | 2dp subtle shadows on cards |
| Icons | Outlined style (Material Symbols) |

### 5.2 Screen Wireframes

#### Navigation Structure

```
App
├── Splash → Onboarding (first launch) → Auth
└── Main Shell (Bottom Navigation)
    ├── Tab 1: Home (Dashboard)
    ├── Tab 2: Wedding
    ├── Tab 3: Finance
    ├── Tab 4: Us (Communication)
    └── Tab 5: Me (Profile)
```

---

#### 01. Splash

```
┌──────────────────────────────────┐
│                                  │
│                                  │
│                                  │
│           ♡ couplebase           │
│                                  │
│        Plan your forever.        │
│                                  │
│                                  │
│                                  │
└──────────────────────────────────┘
```

#### 02. Onboarding (3-page horizontal pager)

```
┌──────────────────────────────────┐
│                                  │
│       ┌──────────────────┐       │
│       │  [Illustration]  │       │
│       └──────────────────┘       │
│                                  │
│     Plan Your Dream Wedding      │
│   Checklists, budgets, guests    │
│   — synced with your partner.    │
│                                  │
│          ● ○ ○                   │
│                                  │
│   ┌──────────────────────────┐   │
│   │         Next →           │   │
│   └──────────────────────────┘   │
│          Skip                    │
└──────────────────────────────────┘

Page 2: "Build Your Life Together" (goals, finances, calendar)
Page 3: "Stay Connected" (journal, check-ins) → "Get Started" button
```

#### 03. Login

```
┌──────────────────────────────────┐
│           ♡ couplebase           │
│        Welcome back              │
│                                  │
│   ┌──────────────────────────┐   │
│   │  Email                   │   │
│   └──────────────────────────┘   │
│   ┌──────────────────────────┐   │
│   │  Password            👁  │   │
│   └──────────────────────────┘   │
│        Forgot password?          │
│                                  │
│   ┌──────────────────────────┐   │
│   │        Log In            │   │ ← Filled rose
│   └──────────────────────────┘   │
│   ──────── or ────────           │
│   ┌──────────────────────────┐   │
│   │  G  Continue with Google │   │ ← Outlined
│   └──────────────────────────┘   │
│   ┌──────────────────────────┐   │
│   │    Continue with Apple   │   │
│   └──────────────────────────┘   │
│                                  │
│   Don't have an account? Sign up │
└──────────────────────────────────┘
```

#### 04. Sign Up

```
┌──────────────────────────────────┐
│  ←                               │
│        Create Account            │
│                                  │
│   [Full Name]                    │
│   [Email]                        │
│   [Password 👁]                  │
│   [Confirm Password 👁]         │
│                                  │
│   [  Create Account  ]          │
│   ──────── or ────────           │
│   [G Continue with Google]       │
│                                  │
│   Already have an account? Login │
└──────────────────────────────────┘
```

#### 05. Couple Pairing — Choose

```
┌──────────────────────────────────┐
│  ←                               │
│     Connect with your partner    │
│                                  │
│   ┌──────────────────────────┐   │
│   │ ♡ Create a new           │   │
│   │   couple space           │   │
│   │   Start fresh & invite   │   │
│   │   your partner       →   │   │
│   └──────────────────────────┘   │
│                                  │
│   ┌──────────────────────────┐   │
│   │ 🔗 Join your partner's   │   │
│   │    space                 │   │
│   │    Enter code or scan QR │   │
│   │                      →   │   │
│   └──────────────────────────┘   │
│                                  │
│         Skip for now             │
└──────────────────────────────────┘
```

#### 06. Couple Pairing — Invite

```
┌──────────────────────────────────┐
│  ←                               │
│     Invite your partner          │
│                                  │
│   ┌──────────────────────────┐   │
│   │      A 7 X 9 K 2        │   │ ← Large monospace
│   │         📋 Copy          │   │
│   └──────────────────────────┘   │
│         ── or ──                 │
│   ┌──────────────────────────┐   │
│   │      ┌──────────┐       │   │
│   │      │ QR CODE  │       │   │
│   │      └──────────┘       │   │
│   └──────────────────────────┘   │
│                                  │
│   [    Share Invite Link    ]    │
│                                  │
│   Waiting for partner to join... │
└──────────────────────────────────┘
```

#### 07. Couple Pairing — Join

```
┌──────────────────────────────────┐
│  ←                               │
│     Join your partner            │
│   Enter the 6-digit invite code  │
│                                  │
│   ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐│
│   │A │ │7 │ │X │ │9 │ │K │ │_ ││
│   └──┘ └──┘ └──┘ └──┘ └──┘ └──┘│
│                                  │
│   [       Join       ]           │
│         ── or ──                 │
│   [  📷 Scan QR Code  ]         │
└──────────────────────────────────┘
```

#### 08. Home Dashboard

```
┌──────────────────────────────────┐
│  couplebase              🔔  ⚙️  │
│                                  │
│  ┌──────────────────────────┐    │
│  │  Sarah & Mike       ♡    │    │
│  │  Wedding: Jun 15, 2026   │    │
│  │  ━━━━━━━━━━━━━━━░░ 73%   │    │
│  │  97 days to go           │    │
│  └──────────────────────────┘    │
│                                  │
│  Quick Actions                   │
│  ┌──────┐ ┌──────┐ ┌──────┐     │
│  │ 📋   │ │ 💰   │ │ 👥   │     │
│  │Check │ │Budget│ │Guest │     │
│  └──────┘ └──────┘ └──────┘     │
│                                  │
│  Today's Tasks              →    │
│  ┌──────────────────────────┐    │
│  │ ○ Book photographer      │    │
│  │   Due: Mar 15  · You     │    │
│  │ ○ Finalize guest list    │    │
│  │   Due: Mar 20  · Both    │    │
│  │ ● Send save-the-dates ✓  │    │
│  │   Completed · Partner    │    │
│  └──────────────────────────┘    │
│                                  │
│  Daily Check-in             →    │
│  ┌──────────────────────────┐    │
│  │ How are you feeling?     │    │
│  │ 😊 😐 😔 😍 😤         │    │
│  └──────────────────────────┘    │
│                                  │
│  Budget Snapshot            →    │
│  ┌──────────────────────────┐    │
│  │ $24,500 / $35,000  70%   │    │
│  │ ━━━━━━━━━━━━━░░░░         │    │
│  │ $10,500 remaining        │    │
│  └──────────────────────────┘    │
│                                  │
├──────────────────────────────────┤
│ 🏠    📋    💰    💬    👤      │
│ Home Wedding Finance Us   Me     │
└──────────────────────────────────┘
```

#### 09. Wedding Tab (Hub)

```
┌──────────────────────────────────┐
│  Wedding                    🔍   │
│                                  │
│  ┌──────────────────────────┐    │
│  │ 97 days to go            │    │
│  │ Jun 15, 2026             │    │
│  │ ━━━━━━━━━━━━━━━░░ 73%    │    │
│  └──────────────────────────┘    │
│                                  │
│  ┌────────────┐ ┌────────────┐   │
│  │ 📋 Check   │ │ 💰 Budget  │   │
│  │   12/48    │ │   $24.5k   │   │
│  └────────────┘ └────────────┘   │
│  ┌────────────┐ ┌────────────┐   │
│  │ 👥 Guests  │ │ 🏪 Vendors │   │
│  │   142      │ │   8 booked │   │
│  └────────────┘ └────────────┘   │
│  ┌────────────┐                  │
│  │ 📅 Timeline│                  │
│  │   Day-of   │                  │
│  └────────────┘                  │
│                                  │
├──────────────────────────────────┤
│ 🏠    📋    💰    💬    👤      │
└──────────────────────────────────┘
```

#### 10. Wedding Checklist

```
┌──────────────────────────────────┐
│  ← Wedding                      │
│  Checklist               + Add   │
│                                  │
│  [All] [Mine] [Partner] [Done]   │
│                                  │
│  12+ Months Before          ▾    │
│  ┌──────────────────────────┐    │
│  │ ● Set a budget        ✓  │    │
│  │ ● Choose wedding date ✓  │    │
│  └──────────────────────────┘    │
│  6-12 Months Before         ▾    │
│  ┌──────────────────────────┐    │
│  │ ○ Book venue              │    │
│  │   Both · Due Apr 1        │    │
│  │ ○ Book photographer       │    │
│  │   You · Due Mar 15        │    │
│  └──────────────────────────┘    │
│  3-6 Months Before          ▸    │
│  1-3 Months Before          ▸    │
│  Final Month                ▸    │
│                                  │
├──────────────────────────────────┤
│ 🏠    📋    💰    💬    👤      │
└──────────────────────────────────┘
```

#### 11. Add Checklist Item (Bottom Sheet)

```
┌──────────────────────────────────┐
│  ━━━                             │
│  New Task                        │
│                                  │
│  [Task name                    ] │
│  Category  [6-12 Months Before ▾]│
│  Assigned  [Me] [Partner] [Both] │
│  Due date  [📅 Mar 15, 2026    ] │
│                                  │
│  [       Save Task       ]       │
└──────────────────────────────────┘
```

#### 12. Wedding Budget

```
┌──────────────────────────────────┐
│  ← Wedding                      │
│  Budget                    ✏️    │
│                                  │
│  ┌──────────────────────────┐    │
│  │    Total: $35,000        │    │
│  │      ┌─────────┐        │    │
│  │     / 70% spent \       │    │ ← Donut chart
│  │    │  $24,500    │       │    │
│  │     \           /       │    │
│  │      └─────────┘        │    │
│  │  Spent $24,500  Rem $10,500│  │
│  └──────────────────────────┘    │
│                                  │
│  Categories              + Add   │
│  ┌──────────────────────────┐    │
│  │ Venue     ━━━━━━━━━━━░ $8k│   │
│  │ $7,200 / $8,000           │    │
│  │ Catering  ━━━━━━━░░░ $6k │    │
│  │ $4,800 / $6,000           │    │
│  │ Photo     ━━━━━━━░░ $4k  │    │
│  │ $3,500 / $4,000           │    │
│  │ Attire    ━━━━━━━━━━ $3k ⚠️│  │ ← Over budget
│  │ $3,200 / $3,000           │    │
│  └──────────────────────────┘    │
│        (+) Add Expense           │
│                                  │
├──────────────────────────────────┤
│ 🏠    📋    💰    💬    👤      │
└──────────────────────────────────┘
```

#### 13. Guest List

```
┌──────────────────────────────────┐
│  ← Wedding                      │
│  Guest List               + Add  │
│                                  │
│  ┌──────┐ ┌──────┐ ┌──────┐     │
│  │ 142  │ │  98  │ │  12  │     │
│  │Total │ │Accept│ │Decl. │     │
│  └──────┘ └──────┘ └──────┘     │
│  [🔍 Search guests...]          │
│  [All] [Accepted] [Pending] [Dec]│
│                                  │
│  ┌──────────────────────────┐    │
│  │ 👤 Emma Johnson          │    │
│  │   Accepted · Table 5     │    │
│  │   🍽 Vegetarian  +1      │    │
│  │ 👤 James Wilson          │    │
│  │   Pending · No table     │    │
│  │ 👤 Sofia Garcia          │    │
│  │   Accepted · Table 3  +1 │    │
│  └──────────────────────────┘    │
│  Export CSV                      │
├──────────────────────────────────┤
│ 🏠    📋    💰    💬    👤      │
└──────────────────────────────────┘
```

#### 14. Vendor List

```
┌──────────────────────────────────┐
│  ← Wedding                      │
│  Vendors                  + Add  │
│                                  │
│  ┌──────────────────────────┐    │
│  │ 📷 Stellar Photography   │    │
│  │   Photography             │    │
│  │   $2,000 / $3,500 paid   │    │
│  │   Next: $750 due Apr 1   │    │
│  ├──────────────────────────┤    │
│  │ 🌸 Bloom & Petal         │    │
│  │   Florist · Paid in full ✓│   │
│  ├──────────────────────────┤    │
│  │ 🎵 DJ Marcus             │    │
│  │   Music · $500/$1,800    │    │
│  │   Next: $650 due May 1   │    │
│  └──────────────────────────┘    │
├──────────────────────────────────┤
│ 🏠    📋    💰    💬    👤      │
└──────────────────────────────────┘
```

#### 15. Vendor Detail

```
┌──────────────────────────────────┐
│  ← Vendors                 ✏️   │
│  Stellar Photography             │
│  Photography                     │
│                                  │
│  Contact                         │
│  📞 (555) 123-4567              │
│  📧 info@stellar.com            │
│  🌐 stellar-photo.com           │
│                                  │
│  Contract                        │
│  📄 contract.pdf  📎             │
│                                  │
│  Payment Schedule                │
│  ● $1,000  Deposit     ✓        │
│  ● $1,000  2nd payment ✓        │
│  ○ $750   3rd (Apr 1)           │
│  ○ $750   Final (Jun 1)         │
│                                  │
│  Notes                           │
│  "Great at outdoor shoots."      │
├──────────────────────────────────┤
│ 🏠    📋    💰    💬    👤      │
└──────────────────────────────────┘
```

#### 16. Wedding Day Timeline

```
┌──────────────────────────────────┐
│  ← Wedding                      │
│  Wedding Day Timeline     + Add  │
│  June 15, 2026                   │
│                                  │
│  10:00 AM  Hair & Makeup         │
│  │         Bride's suite · 2h    │
│  │                               │
│  12:00 PM  First Look Photos     │
│  │         Garden · 45m          │
│  │                               │
│   1:00 PM  Ceremony              │
│  │         Main Chapel · 30m     │
│  │                               │
│   1:45 PM  Cocktail Hour         │
│  │         Terrace · 1h          │
│  │                               │
│   3:00 PM  Reception             │
│            Grand Hall · 4h       │
│                                  │
│  [  📤 Share Timeline  ]         │
├──────────────────────────────────┤
│ 🏠    📋    💰    💬    👤      │
└──────────────────────────────────┘
```

#### 17. Finance Tab (Hub)

```
┌──────────────────────────────────┐
│  Finance                         │
│  March 2026              < >     │
│                                  │
│  ┌──────────────────────────┐    │
│  │ Income     $8,500        │    │
│  │ Expenses   $5,200        │    │
│  │ ─────────────────        │    │
│  │ Remaining  $3,300 (green)│    │
│  └──────────────────────────┘    │
│                                  │
│  ┌──────────┐ ┌──────────┐      │
│  │📊 Budget │ │💳 Expenses│      │
│  └──────────┘ └──────────┘      │
│  ┌──────────┐                    │
│  │🎯 Savings│                    │
│  └──────────┘                    │
│                                  │
│  Recent Expenses                 │
│  Groceries      -$142.50 Today   │
│  Electric bill   -$89.00 Yest.   │
│  Date night      -$75.00 Mar 8   │
│                                  │
│        (+) Add Expense           │
├──────────────────────────────────┤
│ 🏠    📋    💰    💬    👤      │
└──────────────────────────────────┘
```

#### 18. Monthly Budget Detail

```
┌──────────────────────────────────┐
│  ← Finance                      │
│  Monthly Budget       March < >  │
│                                  │
│  Housing     $1,800              │
│  ━━━━━━━━━━━━━━━░ 90%            │
│  Food        $800                │
│  ━━━━━━━━━━░░░░ 65%              │
│  Transport   $400                │
│  ━━━━━━░░░░░░ 45%                │
│  Entertainment $300   ⚠️         │
│  ━━━━━━━━━━━━━━━━ 110%           │
│  Savings     $1,000   ✓         │
│  ━━━━━━━━━━━━━━━━ 100%           │
│                                  │
│       + Add Category             │
├──────────────────────────────────┤
│ 🏠    📋    💰    💬    👤      │
└──────────────────────────────────┘
```

#### 19. Add Expense (Bottom Sheet)

```
┌──────────────────────────────────┐
│  ━━━                             │
│  Add Expense                     │
│         $ 0.00                   │ ← Large input
│  [Description                  ] │
│  Category [Food & Dining      ▾] │
│  Paid by  [Me] [Partner] [Split] │
│  Date     [📅 Today, Mar 10   ] │
│  [📷 Add Receipt Photo]         │
│  [     Save Expense      ]      │
└──────────────────────────────────┘
```

#### 20. Savings Goals

```
┌──────────────────────────────────┐
│  ← Finance                      │
│  Savings Goals            + Add  │
│                                  │
│  ┌──────────────────────────┐    │
│  │ 🏠 House Down Payment    │    │
│  │    ┌───────┐             │    │
│  │   / 35%    \            │    │ ← Ring chart
│  │  │ $17,500  │            │    │
│  │   \ of $50k /            │    │
│  │    └───────┘             │    │
│  │  Target: Dec 2027        │    │
│  │  + Add Contribution      │    │
│  └──────────────────────────┘    │
│  ┌──────────────────────────┐    │
│  │ ✈️  Honeymoon Fund       │    │
│  │   80% · $4,000 / $5,000  │    │
│  │  Target: May 2026        │    │
│  │  + Add Contribution      │    │
│  └──────────────────────────┘    │
├──────────────────────────────────┤
│ 🏠    📋    💰    💬    👤      │
└──────────────────────────────────┘
```

#### 21. Us Tab (Communication Hub)

```
┌──────────────────────────────────┐
│  Us                              │
│                                  │
│  Daily Check-in                  │
│  ┌──────────────────────────┐    │
│  │ How are you feeling?     │    │
│  │ 😊  😐  😔  😍  😤     │    │
│  │ [One thought today...   ]│    │
│  │ [    Submit Check-in    ]│    │
│  └──────────────────────────┘    │
│                                  │
│  ┌──────────┐ ┌──────────┐      │
│  │📝 Notes  │ │📖 Journal│      │
│  │  12 notes│ │ 28 entries│      │
│  └──────────┘ └──────────┘      │
│                                  │
│  Partner's Mood Today            │
│  ┌──────────────────────────┐    │
│  │ 😍 "Excited about cake   │    │
│  │    tasting tomorrow!"    │    │
│  └──────────────────────────┘    │
│                                  │
│  This Week's Moods               │
│  M  T  W  T  F  S  S            │
│  😊 😍 😐 😊 😍 ·  · You       │
│  😊 😊 😍 😐 😊 ·  · Partner   │
├──────────────────────────────────┤
│ 🏠    📋    💰    💬    👤      │
└──────────────────────────────────┘
```

#### 22. Shared Notes

```
┌──────────────────────────────────┐
│  ← Us                           │
│  Notes                    + New  │
│                                  │
│  📌 Grocery List                 │
│     Updated 2h ago               │
│  📌 House Hunting Criteria       │
│     Updated yesterday            │
│  📝 Restaurant Ideas             │
│     Updated Mar 5                │
│  📝 Gift Ideas - Parents         │
│     Updated Feb 28               │
├──────────────────────────────────┤
│ 🏠    📋    💰    💬    👤      │
└──────────────────────────────────┘
```

#### 23. Love Journal

```
┌──────────────────────────────────┐
│  ← Us                           │
│  Journal                  + New  │
│  [  All  ] [  Shared  ]         │
│                                  │
│  March 2026                      │
│  ┌──────────────────────────┐    │
│  │ Mar 10 — "Planning seating│   │
│  │  charts and laughing..."  │    │
│  │  🔒 Private       📷 2   │    │
│  │ Mar 8 — "Date night at   │    │
│  │  the Thai place!"         │    │
│  │  💑 Shared        📷 1   │    │
│  └──────────────────────────┘    │
│                                  │
│  💡 On this day last year...     │
│  "First time visiting the venue" │
├──────────────────────────────────┤
│ 🏠    📋    💰    💬    👤      │
└──────────────────────────────────┘
```

#### 24. Me / Profile

```
┌──────────────────────────────────┐
│  Profile                    ⚙️   │
│                                  │
│       ┌────────┐                │
│       │ 👫     │                │
│       └────────┘                │
│     Sarah & Mike                 │
│     Together since 2022 · ♡      │
│                                  │
│  Milestones                      │
│  ●── First Date · Jun 2022      │
│  │                               │
│  ●── First Trip · Sep 2022      │
│  │                               │
│  ●── Moved In · Mar 2023        │
│  │                               │
│  ●── Engaged! · Mar 5, 2025     │
│  │                               │
│  ○── Wedding · Jun 15, 2026     │
│      97 days to go               │
│                                  │
│  Life Goals                 →    │
│  🏠 Buy a house     35%         │
│  ✈️  Honeymoon       80%         │
│  🐕 Get a dog        0%         │
├──────────────────────────────────┤
│ 🏠    📋    💰    💬    👤      │
└──────────────────────────────────┘
```

#### 25. Life Goals

```
┌──────────────────────────────────┐
│  ← Profile                      │
│  Life Goals               + Add  │
│                                  │
│  ┌──────────────────────────┐    │
│  │ 🏠 Buy a House    35%    │    │
│  │ ━━━━━━░░░░░░░░░░          │    │
│  │ ● Save for down payment  │    │
│  │ ● Get pre-approved       │    │
│  │ ○ Find a realtor         │    │
│  │ ○ House hunting          │    │
│  │ ○ Close on house         │    │
│  └──────────────────────────┘    │
│  ┌──────────────────────────┐    │
│  │ ✈️  Honeymoon Italy  80%  │    │
│  │ ━━━━━━━━━━━━━━░░░         │    │
│  │ ● Book flights           │    │
│  │ ● Book hotels            │    │
│  │ ○ Plan itinerary         │    │
│  └──────────────────────────┘    │
├──────────────────────────────────┤
│ 🏠    📋    💰    💬    👤      │
└──────────────────────────────────┘
```

#### 26. Settings

```
┌──────────────────────────────────┐
│  ← Profile                      │
│  Settings                        │
│                                  │
│  Account                         │
│  👤 Sarah Miller · sarah@...  →  │
│  🔒 Change Password           →  │
│                                  │
│  Couple Space                    │
│  🔗 Invite Code: A7X9K2      📋 │
│  📅 Wedding Date: Jun 15     →  │
│                                  │
│  Preferences                     │
│  🎨 Theme: System             →  │
│  🔔 Notifications: Enabled    →  │
│  💱 Currency: USD ($)          →  │
│                                  │
│  Data                            │
│  📤 Export Data               →  │
│  🔄 Sync: All synced ✓        →  │
│                                  │
│  [      Log Out      ]  ← Red   │
│  [  Leave Couple Space]  ← Red   │
│                                  │
│  couplebase v1.0.0               │
├──────────────────────────────────┤
│ 🏠    📋    💰    💬    👤      │
└──────────────────────────────────┘
```

#### 27. Notifications

```
┌──────────────────────────────────┐
│  ← Home                         │
│  Notifications                   │
│                                  │
│  Today                           │
│  😍 Mike checked in · 2h ago    │
│  ✓ "Save-the-dates" done · 4h   │
│                                  │
│  Yesterday                       │
│  💰 Mike added expense $89      │
│  ⏰ Payment reminder: DJ $650   │
│                                  │
│  This Week                       │
│  📝 "Grocery List" updated      │
│  👥 3 guests RSVP'd             │
├──────────────────────────────────┤
│ 🏠    📋    💰    💬    👤      │
└──────────────────────────────────┘
```

#### 28. Empty State (Generic Pattern)

```
┌──────────────────────────────────┐
│  ← Section                      │
│  Feature Name                    │
│                                  │
│         ┌──────────┐            │
│         │ [icon]   │            │
│         └──────────┘            │
│     No [items] yet               │
│   [Helpful description of        │
│    what this feature does]       │
│                                  │
│   [  + Add First [Item]  ]      │
├──────────────────────────────────┤
│ 🏠    📋    💰    💬    👤      │
└──────────────────────────────────┘
```

### 5.3 Navigation Flow

```
Splash
├── (first launch) → Onboarding [3 pages] → Login/Signup
└── (returning user) → Home

Auth: Login ↔ Signup → Couple Pairing (Create/Join) → Home

Main Tabs:
Home ─── Dashboard
│        └── Notifications
Wedding ─ Hub
│        ├── Checklist ── Add/Edit (sheet)
│        ├── Budget ───── Add Expense (sheet)
│        ├── Guests ───── Add/Edit Guest (sheet)
│        ├── Vendors ──── Detail ── Add Vendor (sheet)
│        └── Timeline ─── Add Block (sheet)
Finance ─ Hub
│        ├── Budget ───── Edit Categories
│        ├── Expenses ─── Add Expense (sheet)
│        └── Savings ──── Detail ── Add Contribution
Us ────── Hub
│        ├── Check-in (inline)
│        ├── Notes ────── Note Detail
│        └── Journal ──── Entry Detail
Me ────── Profile
         ├── Milestones
         ├── Life Goals ── Goal Detail
         └── Settings ──── Sub-settings pages
```

---

## 6. Database Schema

### Core Tables

```sql
CREATE TABLE couple (
  id TEXT PRIMARY KEY,
  invite_code TEXT UNIQUE NOT NULL,
  partner1_id TEXT NOT NULL,
  partner2_id TEXT,
  couple_name TEXT,
  wedding_date TEXT,
  photo_url TEXT,
  together_since TEXT,
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,
  sync_status TEXT NOT NULL DEFAULT 'SYNCED'
);

CREATE TABLE user_profile (
  id TEXT PRIMARY KEY,
  couple_id TEXT,
  full_name TEXT NOT NULL,
  email TEXT NOT NULL,
  avatar_url TEXT,
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,
  sync_status TEXT NOT NULL DEFAULT 'SYNCED'
);
```

### Wedding Tables

```sql
CREATE TABLE checklist_item (
  id TEXT PRIMARY KEY, couple_id TEXT NOT NULL,
  title TEXT NOT NULL, category TEXT, due_date TEXT,
  assigned_to TEXT, is_completed INTEGER DEFAULT 0,
  sort_order INTEGER DEFAULT 0,
  updated_at TEXT NOT NULL, sync_status TEXT DEFAULT 'PENDING',
  is_deleted INTEGER DEFAULT 0
);

CREATE TABLE budget_category (
  id TEXT PRIMARY KEY, couple_id TEXT NOT NULL,
  name TEXT NOT NULL, allocated_amount REAL DEFAULT 0,
  icon TEXT, sort_order INTEGER DEFAULT 0,
  updated_at TEXT NOT NULL, sync_status TEXT DEFAULT 'PENDING',
  is_deleted INTEGER DEFAULT 0
);

CREATE TABLE expense (
  id TEXT PRIMARY KEY, couple_id TEXT NOT NULL,
  category_id TEXT, description TEXT NOT NULL,
  amount REAL NOT NULL, paid_by TEXT,
  receipt_url TEXT, date TEXT NOT NULL,
  is_wedding_expense INTEGER DEFAULT 0,
  updated_at TEXT NOT NULL, sync_status TEXT DEFAULT 'PENDING',
  is_deleted INTEGER DEFAULT 0
);

CREATE TABLE guest (
  id TEXT PRIMARY KEY, couple_id TEXT NOT NULL,
  name TEXT NOT NULL, email TEXT, phone TEXT,
  rsvp_status TEXT DEFAULT 'PENDING', meal_preference TEXT,
  table_number INTEGER, has_plus_one INTEGER DEFAULT 0,
  notes TEXT,
  updated_at TEXT NOT NULL, sync_status TEXT DEFAULT 'PENDING',
  is_deleted INTEGER DEFAULT 0
);

CREATE TABLE vendor (
  id TEXT PRIMARY KEY, couple_id TEXT NOT NULL,
  name TEXT NOT NULL, category TEXT NOT NULL,
  phone TEXT, email TEXT, website TEXT,
  total_cost REAL DEFAULT 0, notes TEXT,
  updated_at TEXT NOT NULL, sync_status TEXT DEFAULT 'PENDING',
  is_deleted INTEGER DEFAULT 0
);

CREATE TABLE vendor_payment (
  id TEXT PRIMARY KEY, vendor_id TEXT NOT NULL,
  couple_id TEXT NOT NULL, description TEXT NOT NULL,
  amount REAL NOT NULL, due_date TEXT NOT NULL,
  is_paid INTEGER DEFAULT 0,
  updated_at TEXT NOT NULL, sync_status TEXT DEFAULT 'PENDING',
  is_deleted INTEGER DEFAULT 0
);

CREATE TABLE timeline_block (
  id TEXT PRIMARY KEY, couple_id TEXT NOT NULL,
  title TEXT NOT NULL, start_time TEXT NOT NULL,
  duration_minutes INTEGER NOT NULL,
  location TEXT, description TEXT,
  assigned_people TEXT, sort_order INTEGER DEFAULT 0,
  updated_at TEXT NOT NULL, sync_status TEXT DEFAULT 'PENDING',
  is_deleted INTEGER DEFAULT 0
);
```

### Life & Finance Tables

```sql
CREATE TABLE milestone (
  id TEXT PRIMARY KEY, couple_id TEXT NOT NULL,
  title TEXT NOT NULL, date TEXT NOT NULL,
  description TEXT, icon TEXT,
  sort_order INTEGER DEFAULT 0,
  updated_at TEXT NOT NULL, sync_status TEXT DEFAULT 'PENDING',
  is_deleted INTEGER DEFAULT 0
);

CREATE TABLE life_goal (
  id TEXT PRIMARY KEY, couple_id TEXT NOT NULL,
  title TEXT NOT NULL, description TEXT,
  target_date TEXT, progress INTEGER DEFAULT 0,
  updated_at TEXT NOT NULL, sync_status TEXT DEFAULT 'PENDING',
  is_deleted INTEGER DEFAULT 0
);

CREATE TABLE goal_milestone (
  id TEXT PRIMARY KEY, goal_id TEXT NOT NULL,
  couple_id TEXT NOT NULL, title TEXT NOT NULL,
  is_completed INTEGER DEFAULT 0, sort_order INTEGER DEFAULT 0,
  updated_at TEXT NOT NULL, sync_status TEXT DEFAULT 'PENDING',
  is_deleted INTEGER DEFAULT 0
);

CREATE TABLE monthly_budget (
  id TEXT PRIMARY KEY, couple_id TEXT NOT NULL,
  year_month TEXT NOT NULL, category TEXT NOT NULL,
  limit_amount REAL NOT NULL, income_amount REAL DEFAULT 0,
  updated_at TEXT NOT NULL, sync_status TEXT DEFAULT 'PENDING',
  is_deleted INTEGER DEFAULT 0
);

CREATE TABLE savings_goal (
  id TEXT PRIMARY KEY, couple_id TEXT NOT NULL,
  title TEXT NOT NULL, target_amount REAL NOT NULL,
  current_amount REAL DEFAULT 0, target_date TEXT,
  icon TEXT,
  updated_at TEXT NOT NULL, sync_status TEXT DEFAULT 'PENDING',
  is_deleted INTEGER DEFAULT 0
);

CREATE TABLE savings_contribution (
  id TEXT PRIMARY KEY, goal_id TEXT NOT NULL,
  couple_id TEXT NOT NULL, amount REAL NOT NULL,
  date TEXT NOT NULL, note TEXT,
  updated_at TEXT NOT NULL, sync_status TEXT DEFAULT 'PENDING',
  is_deleted INTEGER DEFAULT 0
);
```

### Communication Tables

```sql
CREATE TABLE shared_note (
  id TEXT PRIMARY KEY, couple_id TEXT NOT NULL,
  title TEXT NOT NULL, body TEXT DEFAULT '',
  is_pinned INTEGER DEFAULT 0,
  updated_at TEXT NOT NULL, sync_status TEXT DEFAULT 'PENDING',
  is_deleted INTEGER DEFAULT 0
);

CREATE TABLE journal_entry (
  id TEXT PRIMARY KEY, couple_id TEXT NOT NULL,
  author_id TEXT NOT NULL, body TEXT NOT NULL,
  is_shared INTEGER DEFAULT 0, date TEXT NOT NULL,
  updated_at TEXT NOT NULL, sync_status TEXT DEFAULT 'PENDING',
  is_deleted INTEGER DEFAULT 0
);

CREATE TABLE journal_photo (
  id TEXT PRIMARY KEY, entry_id TEXT NOT NULL,
  couple_id TEXT NOT NULL, photo_url TEXT NOT NULL,
  sort_order INTEGER DEFAULT 0,
  updated_at TEXT NOT NULL, sync_status TEXT DEFAULT 'PENDING',
  is_deleted INTEGER DEFAULT 0
);

CREATE TABLE daily_checkin (
  id TEXT PRIMARY KEY, couple_id TEXT NOT NULL,
  user_id TEXT NOT NULL, date TEXT NOT NULL,
  mood TEXT NOT NULL, reflection TEXT,
  updated_at TEXT NOT NULL, sync_status TEXT DEFAULT 'PENDING'
);

CREATE TABLE sync_queue (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  table_name TEXT NOT NULL,
  record_id TEXT NOT NULL,
  operation TEXT NOT NULL,
  created_at TEXT NOT NULL,
  retry_count INTEGER DEFAULT 0
);
```

---

## 7. Development Tasks

### Epic 0: Git & Repository Setup

**TASK-000: Initialize Git repository and remote origin**

- `git init` in project root
- Create `.gitignore` (Kotlin, Gradle, Android, iOS, Node, IDE files)
- Create initial `README.md` with project name and description
- Create `main` branch with initial commit: `init: initialize couplebase repository`
- Create GitHub repository via `gh repo create couplebase --private`
- Set remote origin: `git remote add origin <repo-url>`
- Push `main` to remote
- Create `develop` branch off `main`, push to remote
- Set `develop` as default working branch
- Configure branch protection on `main` (no direct push, require PR)
- Deliverable: GitHub repo with `main` and `develop` branches, ready for feature branches

---

### Epic 1: Project Scaffold & Build System

**TASK-001: Initialize KMP project**

- Create root `build.gradle.kts` with KMP plugins
- Create `settings.gradle.kts` with all module includes
- Create `gradle/libs.versions.toml` version catalog with all dependencies
- Configure Kotlin 2.1+, Compose Multiplatform 1.7+, Android SDK targets
- Deliverable: Empty project that configures and syncs in IDE

**TASK-002: Create build-logic convention plugins**

- `KmpLibraryConventionPlugin` — base KMP library module setup
- `KmpComposeConventionPlugin` — KMP + Compose Multiplatform
- `KmpFeatureConventionPlugin` — feature module (compose + koin + decompose)
- Apply common Kotlin options (JVM target, opt-ins, etc.)
- Deliverable: Feature modules apply `id("couplebase.kmp.feature")` and get full setup

**TASK-003: Create all module directories with build.gradle.kts**

- Create 8 core modules + 15 feature modules + composeApp
- Each module has `src/commonMain`, `src/androidMain`, `src/iosMain`, `src/wasmJsMain`
- Each applies the appropriate convention plugin
- Deliverable: `./gradlew build` succeeds (empty modules)

**TASK-004: Setup composeApp platform entry points**

- `androidMain`: `MainActivity.kt`, `AndroidManifest.xml`
- `iosMain`: `MainViewController.kt`
- `wasmJsMain`: `main.kt` with browser entry
- Minimal "Hello Couplebase" screen on all 3 platforms
- Deliverable: App launches on Android emulator, iOS simulator, and browser

---

### Epic 2: Core Infrastructure

**TASK-005: :core:common module**

- `Result<T>` wrapper (Success/Error) for use case returns
- `UuidGenerator` (expect/actual: `java.util.UUID` / `platform.Foundation.NSUUID` / `crypto.randomUUID`)
- `DateTimeExt.kt` — kotlinx-datetime extension functions
- `FlowExt.kt` — common Flow operators
- Deliverable: Utility functions available to all modules

**TASK-006: :core:model module**

- All domain data classes (pure Kotlin, no framework deps):
- `User`, `Couple`, `ChecklistItem`, `BudgetCategory`, `Expense`
- `Guest`, `Vendor`, `VendorPayment`, `TimelineBlock`
- `Milestone`, `LifeGoal`, `GoalMilestone`
- `MonthlyBudget`, `SavingsGoal`, `SavingsContribution`
- `SharedNote`, `JournalEntry`, `DailyCheckin`
- `SyncStatus` enum, `ChecklistFilter` enum, `RsvpStatus` enum, etc.
- Deliverable: All domain types defined and importable

**TASK-007: :core:database module — SQLDelight setup**

- Configure SQLDelight for all 3 platform drivers
- Create all `.sq` files from schema (Section 6)
- Platform-specific driver factories (expect/actual)
- Database singleton with `CouplebaseDatabase` interface
- Deliverable: Database creates on all platforms, generated DAO code compiles

**TASK-008: :core:database module — Queries & DAOs**

- Write SQLDelight queries for each table:
- CRUD operations
- `Flow`-returning queries for reactive reads
- Filter/search queries (checklist by filter, guests by status, etc.)
- `getByCouple(coupleId)` on all couple-scoped tables
- Pending sync queries: `getBySyncStatus('PENDING')`
- Deliverable: All queries compile and generate correct Kotlin

**TASK-009: :core:network module**

- Supabase client initialization (expect/actual for platform config)
- `SupabaseProvider` singleton with auth, postgrest, realtime, storage modules
- API response DTOs with kotlinx-serialization `@Serializable`
- DTO <-> Domain model mappers
- Deliverable: Supabase client initializes on all platforms

**TASK-010: :core:datastore module**

- Multiplatform DataStore preferences
- `UserPreferences`: theme, onboarding completed, last sync time
- `CouplePreferences`: couple_id, wedding_date (cached locally)
- Deliverable: Preferences read/write works on all platforms

**TASK-011: :core:domain module**

- Base `UseCase` interface / conventions
- Common use cases: `GetCurrentCoupleUseCase`, `IsLoggedInUseCase`
- Deliverable: Domain layer foundation ready

**TASK-012: :core:ui module — Design system**

- `CouplebaseTheme.kt` — Material 3 theme (light + dark)
- Color tokens: rose primary, teal secondary, surfaces, errors
- Typography scale: headline, title, body, label sizes
- Spacing system: 4/8/12/16/24/32dp tokens
- Shape system: 8dp buttons, 12dp cards, 24dp chips
- Deliverable: Theme applies consistently across platforms

**TASK-013: :core:ui module — Shared components**

- `CbButton` — primary, outlined, text variants
- `CbTextField` — with label, error state, password toggle
- `CbCard` — elevated card with consistent styling
- `CbTopBar` — with back button, title, trailing actions
- `CbBottomSheet` — modal bottom sheet wrapper
- `CbEmptyState` — icon + message + CTA pattern
- `CbProgressBar` — linear progress with percentage label
- `CbChip` — filter chip, toggle chip
- `CbLoadingIndicator` — centered circular spinner
- Deliverable: Reusable component library ready

---

### Epic 3: Navigation & App Shell

**TASK-014: Root Decompose navigation**

- `RootComponent` with `childStack` (Auth <-> Main)
- Serializable `Config` sealed interface
- Child factory creating auth/main components
- Deliverable: App navigates between auth and main shells

**TASK-015: Main shell with bottom navigation**

- `MainComponent` with 5 tabs: Home, Wedding, Finance, Us, Me
- `ChildPages` or `ChildStack` per tab with independent back stacks
- Bottom navigation bar composable with Material 3 `NavigationBar`
- Deliverable: 5-tab navigation with placeholder screens

**TASK-016: Deep link infrastructure**

- Platform-specific deep link handling (expect/actual)
- Android: intent filter for `couplebase://join/{code}`
- iOS: Universal Links config
- Web: URL routing
- Route deep links to appropriate Decompose configs
- Deliverable: Deep links resolve to correct screens

---

### Epic 4: Auth & Couple Pairing

**TASK-017: Auth — Login screen**

- `LoginComponent` (MVI): state, intents, Supabase auth calls
- `LoginScreen` composable matching wireframe 03
- Email + password fields with validation
- "Forgot password" flow (Supabase reset email)
- Navigation to signup
- Deliverable: Users can log in with email/password

**TASK-018: Auth — Signup screen**

- `SignupComponent` with form validation
- `SignupScreen` composable matching wireframe 04
- Name, email, password, confirm password
- Supabase `signUpWith(Email)` integration
- Deliverable: Users can create accounts

**TASK-019: Auth — Social login**

- Google Sign-In (expect/actual per platform)
- Apple Sign-In (iOS + Web only)
- `SocialAuthProvider` interface with platform implementations
- Deliverable: Social login works on all supported platforms

**TASK-020: Couple pairing — Create space**

- `CreateCoupleUseCase` — generates couple record + 6-char invite code
- Supabase insert + RLS policy for couple-scoped data
- `PairingScreen` matching wireframe 05 (choose create/join)
- Deliverable: Partner 1 can create a couple space

**TASK-021: Couple pairing — Invite screen**

- `InviteScreen` matching wireframe 06
- QR code generation (shared KMP library: `qrcode-kotlin`)
- Copy invite code to clipboard
- System share sheet for invite link
- Realtime listener: detect when partner 2 joins
- Deliverable: Invite code + QR + share link all work

**TASK-022: Couple pairing — Join screen**

- `JoinScreen` matching wireframe 07
- 6-digit code input with individual character fields
- QR scanner (expect/actual: CameraX / AVFoundation / jsQR)
- `JoinCoupleUseCase` — validates code, links user to couple
- Deliverable: Partner 2 can join via code or QR

**TASK-023: Auth — Session management**

- Auto token refresh via Supabase SDK
- Persistent login check on app launch
- Logout flow (clear local data + navigate to login)
- Deliverable: Users stay logged in across app restarts

**TASK-024: Supabase RLS policies**

- Write PostgreSQL RLS policies for ALL tables
- All SELECT/INSERT/UPDATE/DELETE scoped to `couple_id`
- Users can only access rows where `couple_id` matches their couple
- Test with 2 different couple accounts
- Deliverable: Data isolation verified between couples

---

### Epic 5: Home Dashboard

**TASK-025: Home dashboard screen**

- `HomeComponent` composing data from multiple features
- `HomeScreen` matching wireframe 08
- Wedding countdown card (days to wedding, progress bar)
- Quick action grid (checklist, budget, guests)
- Today's tasks section (from checklist, filtered by due date)
- Inline daily check-in widget
- Budget snapshot card
- Deliverable: Dashboard shows aggregated data from all features

**TASK-026: Notifications screen**

- `NotificationComponent` + `NotificationScreen` matching wireframe 27
- Local notification model (activity feed from sync events)
- Grouped by: Today, Yesterday, This Week
- Types: partner check-in, task completed, expense added, RSVP, payment reminder
- Deliverable: Notification feed displays recent activity

---

### Epic 6: Wedding Checklist

**TASK-027: Checklist — Data layer**

- `ChecklistLocalDataSource` — SQLDelight queries wrapper
- `ChecklistRemoteDataSource` — Supabase postgrest calls
- `ChecklistRepositoryImpl` — offline-first implementation
- Koin DI module registration
- Deliverable: Checklist CRUD works offline with sync queueing

**TASK-028: Checklist — Use cases**

- `GetChecklistItemsUseCase(filter)` — returns Flow grouped by category
- `ToggleChecklistItemUseCase` — toggle completion
- `AddChecklistItemUseCase` — create new item
- `DeleteChecklistItemUseCase` — soft delete
- `LoadChecklistTemplateUseCase` — pre-populate template tasks
- Deliverable: All business logic encapsulated in use cases

**TASK-029: Checklist — UI**

- `ChecklistComponent` (MVI) — state with grouped items, filter
- `ChecklistScreen` matching wireframe 10
- Filter chips (All, Mine, Partner, Done)
- Collapsible category sections
- Swipe-to-complete, swipe-to-delete
- `AddChecklistItemSheet` matching wireframe 11
- Deliverable: Full checklist UI with all interactions

**TASK-030: Checklist — Templates**

- JSON/resource file with pre-populated wedding tasks
- Categories: 12+ months, 6-12 months, 3-6 months, 1-3 months, final month
- 40-50 curated tasks with suggested due dates (relative to wedding date)
- Template loads on first couple creation
- Deliverable: New couples get a populated checklist

---

### Epic 7: Wedding Budget

**TASK-031: Budget — Data layer**

- `BudgetLocalDataSource`, `BudgetRemoteDataSource`, `BudgetRepositoryImpl`
- Handles both `budget_category` and `expense` tables
- Query: total spent per category, overall totals
- Deliverable: Budget data accessible offline

**TASK-032: Budget — Use cases**

- `GetBudgetOverviewUseCase` — total budget, spent, remaining, per-category
- `AddBudgetCategoryUseCase`, `UpdateBudgetCategoryUseCase`
- `AddExpenseUseCase` (shared with finance expenses, `is_wedding_expense` flag)
- `GetCategoryExpensesUseCase`
- Deliverable: Budget logic encapsulated

**TASK-033: Budget — UI**

- `BudgetComponent` + `BudgetScreen` matching wireframe 12
- Donut chart (Compose Canvas) for total spent vs budget
- Category list with progress bars
- Over-budget warning indicator
- Add category bottom sheet
- Add expense bottom sheet (reused from finance)
- Deliverable: Budget tracking with visual charts

---

### Epic 8: Guest List

**TASK-034: Guests — Data layer & use cases**

- `GuestLocalDataSource`, `GuestRemoteDataSource`, `GuestRepositoryImpl`
- `GetGuestsUseCase(filter, search)` — Flow with stats (total, accepted, etc.)
- `AddGuestUseCase`, `UpdateGuestUseCase`, `DeleteGuestUseCase`
- `ExportGuestListUseCase` — generate CSV string
- `ImportGuestsUseCase` — parse CSV
- Deliverable: Guest CRUD + import/export

**TASK-035: Guests — UI**

- `GuestListComponent` + `GuestListScreen` matching wireframe 13
- Stats row (total, accepted, declined)
- Search bar + filter chips
- Guest cards with RSVP status, table, meal, plus-one badges
- Add/edit guest bottom sheet
- Export CSV button
- Deliverable: Full guest management UI

---

### Epic 9: Vendors

**TASK-036: Vendors — Data layer & use cases**

- `VendorLocalDataSource`, `VendorRemoteDataSource`, `VendorRepositoryImpl`
- Handles `vendor` + `vendor_payment` tables
- `GetVendorsUseCase`, `GetVendorDetailUseCase`
- `AddVendorUseCase`, `UpdateVendorUseCase`, `DeleteVendorUseCase`
- `AddVendorPaymentUseCase`, `MarkPaymentPaidUseCase`
- Deliverable: Vendor + payment CRUD

**TASK-037: Vendors — UI**

- `VendorListComponent` + `VendorListScreen` matching wireframe 14
- Vendor cards with payment progress
- `VendorDetailComponent` + `VendorDetailScreen` matching wireframe 15
- Contact info, contract upload, payment schedule, notes
- File upload to Supabase Storage (expect/actual for file picker)
- Deliverable: Full vendor management with file uploads

---

### Epic 10: Wedding Timeline

**TASK-038: Timeline — Data layer & use cases**

- `TimelineLocalDataSource`, `TimelineRemoteDataSource`, `TimelineRepositoryImpl`
- `GetTimelineUseCase` — sorted by start_time
- `AddTimelineBlockUseCase`, `UpdateTimelineBlockUseCase`, `DeleteTimelineBlockUseCase`
- Deliverable: Timeline CRUD

**TASK-039: Timeline — UI**

- `TimelineComponent` + `TimelineScreen` matching wireframe 16
- Vertical timeline with time markers
- Time block cards with title, location, duration, assigned people
- Add/edit block bottom sheet
- Share/export timeline button
- Deliverable: Wedding day schedule builder

---

### Epic 11: Sync Engine

**TASK-040: Core sync — SyncQueue & SyncManager**

- `sync_queue` SQLDelight table + queries
- `SyncManager` class:
- `enqueueSync(tableName, recordId, operation)` — add to queue
- `processQueue()` — iterate pending items, push to Supabase
- On success: update `sync_status` to `SYNCED`, remove from queue
- On failure: increment `retry_count`, exponential backoff
- Deliverable: Queued sync mechanism works

**TASK-041: Core sync — Connectivity monitor**

- `ConnectivityMonitor` interface with `isOnline: StateFlow<Boolean>`
- expect/actual implementations:
- Android: `ConnectivityManager` + `NetworkCallback`
- iOS: `NWPathMonitor`
- Web: `navigator.onLine` + events
- Trigger `processQueue()` on connectivity restore
- Deliverable: App detects online/offline state on all platforms

**TASK-042: Core sync — Supabase Realtime subscriptions**

- Subscribe to Supabase Realtime channels per couple
- Listen for INSERT, UPDATE, DELETE on all couple-scoped tables
- On receive: upsert into local SQLDelight, mark as SYNCED
- Handle subscription lifecycle (connect, reconnect, disconnect)
- Deliverable: Partner changes appear in real-time

**TASK-043: Core sync — Conflict resolution**

- Detect conflicts: local PENDING + incoming remote change for same record
- Strategy: last-write-wins using `updated_at` timestamps
- For critical data (budget amounts): mark as CONFLICT, prompt user
- UI: conflict resolution dialog showing both versions
- Deliverable: Conflicts handled gracefully

**TASK-044: Core sync — Periodic sync**

- Background periodic sync (every 5 minutes when app is active)
- Full sync on app foreground (pull all changes since last sync)
- `last_sync_timestamp` stored in DataStore
- Deliverable: Data stays fresh even without Realtime connection

---

### Epic 12: Couple Profile & Goals

**TASK-045: Profile — Data layer & use cases**

- `ProfileRepositoryImpl` handling `couple`, `user_profile`, `milestone` tables
- `GetCoupleProfileUseCase`, `UpdateCoupleProfileUseCase`
- `GetMilestonesUseCase`, `AddMilestoneUseCase`, `DeleteMilestoneUseCase`
- Photo upload to Supabase Storage
- Deliverable: Profile & milestone CRUD

**TASK-046: Profile — UI**

- `ProfileComponent` + `ProfileScreen` matching wireframe 24
- Couple photo + name + status card
- Milestone timeline (vertical with dots and lines)
- Life goals summary with navigation
- Settings gear navigation
- Deliverable: Profile screen with milestones

**TASK-047: Goals — Data layer, use cases & UI**

- `GoalRepositoryImpl` handling `life_goal` + `goal_milestone` tables
- `GetGoalsUseCase`, `AddGoalUseCase`, `UpdateGoalProgressUseCase`
- `GoalListComponent` + `GoalListScreen` matching wireframe 25
- Goal cards with progress bars and sub-milestone checklist
- Add/edit goal bottom sheet
- Deliverable: Life goals feature complete

---

### Epic 13: Finance

**TASK-048: Finance hub — UI**

- `FinanceComponent` + `FinanceScreen` matching wireframe 17
- Monthly income/expense/remaining summary card
- Navigation grid (Budget, Expenses, Savings)
- Recent expenses list
- FAB for quick expense entry
- Deliverable: Finance hub screen

**TASK-049: Monthly budget — Data layer, use cases & UI**

- `MonthlyBudgetRepositoryImpl` handling `monthly_budget` table
- `GetMonthlyBudgetUseCase(yearMonth)`, `SetCategoryLimitUseCase`
- `MonthlyBudgetComponent` + `MonthlyBudgetScreen` matching wireframe 18
- Category progress bars with over-budget warnings
- Month navigation (< > arrows)
- Deliverable: Monthly budget tracking

**TASK-050: Expenses — Data layer, use cases & UI**

- Reuses `expense` table (shared with wedding budget, `is_wedding_expense` flag)
- `GetExpensesUseCase(month, category, search)`, `AddExpenseUseCase`
- Expense list with filters
- `AddExpenseSheet` matching wireframe 19
- Receipt photo capture + upload (expect/actual camera/gallery)
- Deliverable: Expense tracking with receipts

**TASK-051: Savings — Data layer, use cases & UI**

- `SavingsRepositoryImpl` handling `savings_goal` + `savings_contribution`
- `GetSavingsGoalsUseCase`, `AddContributionUseCase`
- `SavingsComponent` + `SavingsScreen` matching wireframe 20
- Ring chart progress (Compose Canvas)
- Contribution history
- Deliverable: Savings goals feature complete

---

### Epic 14: Communication

**TASK-052: Daily check-in — Data layer, use cases & UI**

- `CheckinRepositoryImpl` handling `daily_checkin` table
- `SubmitCheckinUseCase`, `GetPartnerCheckinUseCase`, `GetWeeklyMoodsUseCase`
- Check-in widget on Home (wireframe 08) + Us hub (wireframe 21)
- Mood emoji selector + text input
- Partner mood reveal (only after both submit)
- Weekly mood chart (grid of emojis)
- Deliverable: Daily check-in feature complete

**TASK-053: Shared notes — Data layer, use cases & UI**

- `NotesRepositoryImpl` handling `shared_note` table
- `GetNotesUseCase`, `CreateNoteUseCase`, `UpdateNoteUseCase`, `PinNoteUseCase`
- `NotesListComponent` + `NotesListScreen` matching wireframe 22
- Note cards with pin indicator, title, preview, last updated
- Note detail screen with editing
- Deliverable: Shared notes feature complete

**TASK-054: Love journal — Data layer, use cases & UI**

- `JournalRepositoryImpl` handling `journal_entry` + `journal_photo`
- `GetEntriesUseCase(filter: all|shared)`, `CreateEntryUseCase`
- `GetOnThisDayUseCase` — entries from same date in previous years
- `JournalComponent` + `JournalScreen` matching wireframe 23
- Entry cards with privacy badge, photo count
- New entry screen with photo picker + privacy toggle
- "On this day" memories section
- Deliverable: Journal feature complete

---

### Epic 15: Settings

**TASK-055: Settings screen**

- `SettingsComponent` + `SettingsScreen` matching wireframe 26
- Sections: Account, Couple Space, Preferences, Data
- Theme picker (light/dark/system) → DataStore
- Notification preferences
- Currency selector
- Sync status display
- Deliverable: Settings screen

**TASK-056: Account management**

- Edit name/email screen
- Change password flow (Supabase)
- Logout with local data cleanup
- Deliverable: Account management works

**TASK-057: Couple space management**

- View/share invite code
- Edit wedding date
- Leave couple space (with confirmation dialog)
- Delete couple space (with double confirmation, destructive)
- Deliverable: Couple space admin functions

**TASK-058: Data export**

- `ExportDataUseCase` — compile all couple data into JSON/CSV
- Platform share sheet to save/send export file
- Deliverable: Users can export their data

---

### Epic 16: Onboarding & Empty States

**TASK-059: Onboarding flow**

- 3-page horizontal pager matching wireframe 02
- Page indicators, Next/Skip, Get Started
- Mark onboarding complete in DataStore
- Skip on subsequent launches
- Deliverable: First-time users see onboarding

**TASK-060: Empty states for all features**

- Generic `CbEmptyState` component (icon + message + CTA)
- Unique empty state per feature:
- Checklist: "No tasks yet — load a template?"
- Guests: "No guests yet — add your first guest"
- Budget: "Set your wedding budget to get started"
- Vendors: "No vendors yet"
- Timeline: "Build your wedding day schedule"
- Notes: "Create your first shared note"
- Journal: "Start your love journal"
- Savings: "Set your first savings goal"
- Deliverable: Every list screen has a helpful empty state

---

### Epic 17: Polish & Platform Integration

**TASK-061: Push notifications**

- Android: FCM integration
- iOS: APNs integration
- Web: Web Push API
- Notification types: partner check-in, task due, payment reminder, RSVP update
- Notification preferences (per type toggle)
- Deliverable: Push notifications on all platforms

**TASK-062: Performance optimization**

- Lazy loading for long lists (checklist, guest list, expenses)
- Image caching configuration (Coil)
- Database query optimization (indices on `couple_id`, `sync_status`)
- Compose performance: minimize recompositions, use `@Stable`/`@Immutable`
- Deliverable: App feels snappy on all platforms

**TASK-063: Error handling & edge cases**

- Global error handler for network failures
- Retry UI for failed syncs
- Graceful degradation when offline
- Input validation on all forms
- Deliverable: App handles errors gracefully

**TASK-064: Testing**

- Unit tests: all use cases, all repositories (with mock data sources)
- Flow tests: reactive queries with Turbine
- Integration tests: sync engine (local <-> remote)
- UI tests: auth flow, checklist CRUD, expense entry
- Deliverable: Core flows have test coverage

**TASK-065: App Store preparation**

- Android: signing config, ProGuard rules, Play Store listing
- iOS: Xcode project config, App Store Connect
- Web: hosting setup (Vercel/Netlify), PWA manifest
- App icons, splash screen assets
- Deliverable: Ready to submit to stores

---

## 8. Conventional Commit Rules

### 8.1 Commit Message Format

```
<type>(<scope>): <subject>

[optional body]

[optional footer(s)]
```

**Example:**

```
feat(wedding-checklist): add checklist template loader

Load pre-populated wedding tasks from JSON resource file
grouped by timeline category (12mo, 6mo, 3mo, 1mo, final).

Refs: TASK-030
Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
```

### 8.2 Types

| Type | When to use | Example |
|------|------------|---------|
| `feat` | New feature or capability | `feat(auth): add Google sign-in` |
| `fix` | Bug fix | `fix(sync): resolve duplicate entries on reconnect` |
| `refactor` | Code change that neither fixes nor adds | `refactor(database): extract base DAO interface` |
| `chore` | Build, config, tooling, dependencies | `chore(build): add KmpFeature convention plugin` |
| `style` | UI/visual changes only (no logic change) | `style(ui): update rose accent to #C2185B` |
| `docs` | Documentation only | `docs: add API key setup instructions` |
| `test` | Adding or fixing tests | `test(checklist): add repository unit tests` |
| `perf` | Performance improvement | `perf(guests): add index on couple_id column` |
| `ci` | CI/CD pipeline changes | `ci: add iOS simulator build step` |
| `init` | Initial project/module setup | `init: scaffold KMP project with Compose Multiplatform` |

### 8.3 Scopes

Scopes map to our module names:

**Core modules:**

| Scope | Module |
|-------|--------|
| `common` | `:core:common` |
| `model` | `:core:model` |
| `database` | `:core:database` |
| `network` | `:core:network` |
| `sync` | `:core:sync` |
| `domain` | `:core:domain` |
| `datastore` | `:core:datastore` |
| `ui` | `:core:ui` |

**Feature modules:**

| Scope | Module |
|-------|--------|
| `auth` | `:feature:auth` |
| `wedding-checklist` | `:feature:wedding-checklist` |
| `wedding-budget` | `:feature:wedding-budget` |
| `wedding-guests` | `:feature:wedding-guests` |
| `wedding-vendors` | `:feature:wedding-vendors` |
| `wedding-timeline` | `:feature:wedding-timeline` |
| `couple-profile` | `:feature:couple-profile` |
| `couple-goals` | `:feature:couple-goals` |
| `finance-budget` | `:feature:finance-budget` |
| `finance-expenses` | `:feature:finance-expenses` |
| `finance-savings` | `:feature:finance-savings` |
| `comm-notes` | `:feature:comm-notes` |
| `comm-journal` | `:feature:comm-journal` |
| `comm-checkin` | `:feature:comm-checkin` |
| `settings` | `:feature:settings` |

**Cross-cutting scopes:**

| Scope | When |
|-------|------|
| `build` | Gradle, convention plugins, version catalog |
| `app` | `composeApp` module (root nav, entry points) |
| `deps` | Dependency updates only |
| `nav` | Navigation/routing changes |
| *(omit scope)* | Truly project-wide changes |

### 8.4 Rules

- **Subject line**: imperative mood, lowercase, no period, max 72 chars
  - Good: `feat(auth): add email validation on signup form`
  - Bad: `feat(auth): Added email validation on signup form.`

- **Body** (optional): explain *what* and *why*, not *how*. Wrap at 80 chars.

- **Footer**:
  - Reference task: `Refs: TASK-029`
  - Breaking change: `BREAKING CHANGE: renamed CoupleId to CoupleSpaceId`
  - Co-author: `Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>`

- **One logical change per commit** — don't mix unrelated changes.

- **Scope is required** for `feat`, `fix`, `refactor`, `style`, `test`, `perf`.
  Scope is optional for `chore`, `docs`, `ci`, `init`.

- **Breaking changes** must include `BREAKING CHANGE:` in footer and `!` after scope:

```
refactor(model)!: rename Expense.paid_by to Expense.paidByUserId

BREAKING CHANGE: Expense.paid_by field renamed for clarity.
All existing references must be updated.
```

### 8.5 Commit Strategy per Task

Each task from Section 7 should produce commits following this pattern:

| Task Phase | Commit Pattern |
|-----------|---------------|
| Module scaffold | `init(<scope>): scaffold <module> module structure` |
| Data layer (models, DB, network) | `feat(<scope>): add <entity> data layer` |
| Use cases | `feat(<scope>): add <action> use case` |
| UI screen | `feat(<scope>): implement <screen> screen` |
| UI refinement | `style(<scope>): polish <screen> layout and spacing` |
| Tests | `test(<scope>): add <layer> tests for <feature>` |
| Bug found during dev | `fix(<scope>): <describe the fix>` |

**Example commit sequence for TASK-027 to TASK-030 (Wedding Checklist):**

```
init(wedding-checklist): scaffold feature module structure
feat(wedding-checklist): add checklist data layer with offline-first repo
feat(wedding-checklist): add checklist use cases
feat(wedding-checklist): implement checklist screen with filter chips
feat(wedding-checklist): add add/edit checklist item bottom sheet
feat(wedding-checklist): add checklist template loader
test(wedding-checklist): add repository and use case unit tests
```

### 8.6 Branch Naming

```
<type>/<scope>/<short-description>
```

| Branch | Example |
|--------|---------|
| Feature | `feat/auth/social-login` |
| Fix | `fix/sync/duplicate-entries` |
| Chore | `chore/build/convention-plugins` |
| Epic branch | `epic/wedding-checklist` |

**Flow:**

- `main` — stable, deployable
- `develop` — integration branch
- `feat/*`, `fix/*`, `chore/*` — short-lived branches off `develop`
- PRs merge into `develop`, release cuts merge `develop` → `main`

### 8.7 Pull Request Workflow

**A PR is created after every completed task (or group of closely related tasks).**

#### PR Title Format

```
<type>(<scope>): <short description>
```

Same convention as commits. The PR title matches the primary commit type.

#### PR Body Template

```markdown
## Summary
- Bullet points describing what was done and why

## Task Reference
- TASK-XXX: <task title>

## Changes
- List of key files/modules added or modified

## Screenshots / Recordings
(if UI changes — attach screen captures)

## Test Plan
- [ ] How to verify this PR works
- [ ] Edge cases tested
- [ ] Platforms verified (Android / iOS / Web)

🤖 Generated with [Claude Code](https://claude.com/claude-code)
```

#### PR Rules

- **One PR per task** (or per tightly coupled task group, e.g. TASK-027 + TASK-028 + TASK-029 for the full checklist feature)
- **PR branch** follows branch naming convention: `feat/wedding-checklist/data-and-ui`
- **Target branch**: `develop`
- **PR must build** — `./gradlew build` passes before creating PR
- **Self-review diff** before submitting — no leftover debug code, TODOs, or commented-out blocks
- **Labels** (applied via `gh pr create --label`):

| Label | When |
|-------|------|
| `feature` | New feature PRs |
| `fix` | Bug fix PRs |
| `infra` | Build, CI, tooling PRs |
| `core` | Core module changes |
| `wedding` | Wedding feature modules |
| `finance` | Finance feature modules |
| `communication` | Communication feature modules |

#### PR Sequence (maps to Epics)

| PR# | Branch | Tasks | Description |
|-----|--------|-------|-------------|
| PR-00 | `main` (direct) | TASK-000 | Git init, .gitignore, README, remote origin, develop branch |
| PR-01 | `chore/build/project-scaffold` | TASK-001 to TASK-004 | Project init, convention plugins, module dirs, platform entry points |
| PR-02 | `feat/core/common-and-model` | TASK-005, TASK-006 | Core utilities and domain models |
| PR-03 | `feat/core/database` | TASK-007, TASK-008 | SQLDelight schema, queries, platform drivers |
| PR-04 | `feat/core/network-and-datastore` | TASK-009, TASK-010 | Supabase client, preferences |
| PR-05 | `feat/core/domain` | TASK-011 | Base use cases |
| PR-06 | `feat/core/ui-design-system` | TASK-012, TASK-013 | Theme, colors, typography, shared components |
| PR-07 | `feat/app/navigation-shell` | TASK-014, TASK-015, TASK-016 | Root nav, bottom tabs, deep links |
| PR-08 | `feat/auth/login-and-signup` | TASK-017, TASK-018, TASK-019 | Login, signup, social auth |
| PR-09 | `feat/auth/couple-pairing` | TASK-020, TASK-021, TASK-022 | Create/join couple space, QR, invite |
| PR-10 | `feat/auth/session-and-rls` | TASK-023, TASK-024 | Session management, RLS policies |
| PR-11 | `feat/app/home-dashboard` | TASK-025, TASK-026 | Home dashboard, notifications |
| PR-12 | `feat/wedding-checklist/full` | TASK-027 to TASK-030 | Checklist data + use cases + UI + templates |
| PR-13 | `feat/wedding-budget/full` | TASK-031 to TASK-033 | Budget data + use cases + UI + charts |
| PR-14 | `feat/wedding-guests/full` | TASK-034, TASK-035 | Guest list data + UI |
| PR-15 | `feat/wedding-vendors/full` | TASK-036, TASK-037 | Vendors data + UI + file upload |
| PR-16 | `feat/wedding-timeline/full` | TASK-038, TASK-039 | Timeline data + UI |
| PR-17 | `feat/core/sync-engine` | TASK-040 to TASK-044 | Sync queue, connectivity, realtime, conflicts |
| PR-18 | `feat/couple-profile/full` | TASK-045, TASK-046 | Profile + milestones |
| PR-19 | `feat/couple-goals/full` | TASK-047 | Life goals |
| PR-20 | `feat/finance/hub-and-budget` | TASK-048, TASK-049 | Finance hub + monthly budget |
| PR-21 | `feat/finance/expenses-and-savings` | TASK-050, TASK-051 | Expenses + savings goals |
| PR-22 | `feat/comm/checkin` | TASK-052 | Daily check-in |
| PR-23 | `feat/comm/notes-and-journal` | TASK-053, TASK-054 | Shared notes + love journal |
| PR-24 | `feat/settings/full` | TASK-055 to TASK-058 | Settings, account, couple mgmt, export |
| PR-25 | `feat/app/onboarding` | TASK-059, TASK-060 | Onboarding flow + empty states |
| PR-26 | `feat/app/notifications-push` | TASK-061 | Push notifications |
| PR-27 | `chore/app/polish` | TASK-062 to TASK-065 | Performance, error handling, tests, store prep |

---

## 9. Verification Plan

| What | How |
|------|-----|
| **Build** | `./gradlew build` — all modules, all targets |
| **Android** | Run on emulator/device via Android Studio |
| **iOS** | Run on simulator via Xcode |
| **Web** | `./gradlew wasmJsBrowserRun` in Chrome |
| **Offline** | Airplane mode → create data → reconnect → verify sync |
| **Partner sync** | 2 devices, same couple → verify real-time changes |
| **Auth** | Sign up → pair → login on another device → see shared data |
| **Unit tests** | `./gradlew allTests` |
| **Edge cases** | Slow network, large guest lists (500+), concurrent edits |

---

*Couplebase PRD v1.0 — Generated March 10, 2026*
