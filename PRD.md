# Workout Logger — Product Requirements Document

**Version:** 1.1
**Date:** March 31, 2026
**Package:** `com.ayogeshwaran.workoutlogger`
**Status:** Released

---

## 1. Overview

Workout Logger is a lightweight, offline-first Android app that helps users log their workouts every single day. The app makes it effortless to record what you did, when you did it, and look back at your workout history on any past date. No accounts, no cloud, no bloat — just a fast daily workout log that works out of the box.

---

## 2. Problem Statement

People want to stay consistent with their fitness routine but often lose track of what they did, when they did it, or how consistent they've been over weeks and months. Most fitness apps on the market are overloaded with features — social feeds, meal plans, subscription paywalls — when all the user really wants is a quick way to say "I worked out today" and check what they did last Tuesday or a year ago. There is a clear need for a minimal, fast, offline-first workout logger that does one thing and does it well.

---

## 3. Target Users

- Anyone who works out regularly and wants a no-friction daily log.
- Users who prefer a minimal, no-account-required app that works entirely offline.
- Fitness enthusiasts who want to look back at their workout history over weeks, months, or years.

---

## 4. Goals

| Goal | Measure |
|------|---------|
| Log a workout with minimal effort | Under 5 seconds: tap a type → confirm → done |
| Browse full workout history | Calendar view with indicators; tap any date to see details |
| Work offline by default | All data in local Room database; no network required |
| Support future extensibility | Clean architecture with separated layers; backend can be added without touching UI or business logic |

---

## 5. Tech Stack

| Area | Technology |
|------|------------|
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Design System | Material 3 (Material You) with dynamic color |
| Local Database | Room (SQLite) |
| Architecture | Clean Architecture — Data → Domain → Presentation |
| Dependency Injection | Manual DI container via `AppContainer` (replaceable with Hilt later) |
| Navigation | Jetpack Navigation Compose |
| State Management | ViewModel + Kotlin StateFlow |
| Async | Kotlin Coroutines + Flow |
| Min SDK | 24 |
| Target SDK | 36 |
| Build System | Gradle with Kotlin DSL + Version Catalog (`libs.versions.toml`) |

---

## 6. Architecture

The project follows **clean architecture** with three clearly separated layers. Each layer has a single direction of dependency. This ensures any layer can be modified, tested, or extended independently — and is critical for plugging in a backend later without rewriting the app.

### 6.1 Layer Diagram

```
┌──────────────────────────────────────────────────┐
│               PRESENTATION LAYER                 │
│    Screens (Composables), ViewModels, Theme,     │
│    Navigation                                    │
│                                                  │
│    Depends on: Domain layer only                 │
├──────────────────────────────────────────────────┤
│                 DOMAIN LAYER                     │
│    Models, Repository Interfaces, Use Cases      │
│                                                  │
│    Depends on: Nothing (pure Kotlin, no Android) │
├──────────────────────────────────────────────────┤
│                  DATA LAYER                      │
│    Room Database, Entities, DAOs,                │
│    Repository Implementation                     │
│                                                  │
│    Depends on: Domain layer only                 │
└──────────────────────────────────────────────────┘
```

### 6.2 Package Structure

```
com.ayogeshwaran.workoutlogger/
│
├── WorkoutLoggerApplication.kt            # Application class; initializes DI container
├── MainActivity.kt                        # Single Activity; setContent { Theme { NavHost } }
│
├── di/
│   └── AppContainer.kt                    # Manual DI wiring
│                                          #   Database → DAO → Repository → UseCases
│                                          #   Replaceable with Hilt modules later
│
├── data/
│   ├── local/
│   │   ├── WorkoutDatabase.kt             # Room @Database definition
│   │   ├── WorkoutDao.kt                  # DAO interface with all queries
│   │   └── entity/
│   │       └── WorkoutEntryEntity.kt      # Room @Entity (annotated data class)
│   │
│   ├── repository/
│   │   └── WorkoutRepositoryImpl.kt       # Implements domain's WorkoutRepository
│   │                                      #   Maps between Entity ↔ Domain Model
│   │                                      #   Today: talks to Room only
│   │                                      #   Future: coordinates local + remote
│   │
│   └── remote/                            # (empty for v1 — reserved for backend)
│       └── .gitkeep
│
├── domain/
│   ├── model/
│   │   ├── WorkoutEntry.kt               # Plain Kotlin data class (no annotations)
│   │   └── WorkoutCategory.kt            # Enum: CARDIO, GYM — groups workout types
│   │
│   ├── repository/
│   │   └── WorkoutRepository.kt          # Interface — the contract the data layer fulfills
│   │
│   └── usecase/
│       ├── AddWorkoutUseCase.kt           # Insert a workout entry
│       ├── DeleteWorkoutUseCase.kt        # Delete a workout entry
│       ├── GetWorkoutsForDateUseCase.kt   # Get all workouts for a specific day
│       └── GetDatesWithWorkoutsUseCase.kt # Get all dates that have at least one workout
│
├── presentation/
│   ├── theme/
│   │   ├── Color.kt                      # Seed color palette for Material You
│   │   ├── Theme.kt                      # MaterialTheme w/ dynamic color support
│   │   └── Type.kt                       # App typography scale
│   │
│   ├── navigation/
│   │   └── AppNavigation.kt              # NavHost, route definitions, bottom nav setup
│   │
│   ├── home/
│   │   ├── HomeScreen.kt                 # Today's workout logging screen
│   │   └── HomeViewModel.kt              # State: selected type, date/time, today's list
│   │
│   ├── history/
│   │   ├── HistoryScreen.kt              # Calendar + workout history screen
│   │   └── HistoryViewModel.kt           # State: selected month, selected date, workouts
│   │
│   └── about/
│       └── AboutScreen.kt                # App info, developer, feedback, open source licenses
```

### 6.3 Backend Readiness

The `domain/repository/WorkoutRepository` interface is the **boundary**. The presentation layer never knows where data comes from — it only talks to use cases, which talk to the repository interface.

When a backend is introduced in the future:

1. Add API service and remote data source classes in `data/remote/`.
2. Update `WorkoutRepositoryImpl` to coordinate between local (Room) and remote (API) — e.g., cache-first strategy, background sync.
3. **Zero changes** needed in the domain or presentation layers.

---

## 7. Data Model

### 7.1 Room Entity — `WorkoutEntryEntity`

| Field | Type | Description |
|-------|------|-------------|
| `id` | `Int` | Primary key, auto-generated |
| `workoutCategory` | `String` | Category the workout belongs to — `"cardio"` or `"gym"` |
| `workoutType` | `String` | The specific workout type (e.g., `"Running"`, `"Chest"`, `"Biceps"`) |
| `date` | `Long` | Epoch milliseconds normalized to midnight — used for grouping and querying by calendar day |
| `timestamp` | `Long` | Epoch milliseconds for the exact date + time the user selected |
| `createdAt` | `Long` | Epoch milliseconds when the record was actually inserted into the database |
| `notes` | `String` | Free-form details/notes (e.g., `"did three sets of chest workout"`) |

### 7.2 Domain Model — `WorkoutEntry`

Mirrors the entity fields but is a **plain Kotlin data class** with no Room annotations and no Android dependencies. Mapping between entity and domain model happens exclusively inside `WorkoutRepositoryImpl`. Includes `notes` as an optional `String` field.

A companion enum-like sealed class or string constants define the two categories:
- `WorkoutCategory.CARDIO` — Running, Cycling, Swimming, Walking, HIIT, Yoga, Stretching
- `WorkoutCategory.GYM` — Chest, Back, Shoulders, Biceps, Triceps, Legs, Abs, Full Body

### 7.3 DAO Operations — `WorkoutDao`

| Method | Return Type | Description |
|--------|-------------|-------------|
| `insertWorkout(entry)` | `Unit` | Insert a new workout entry |
| `deleteWorkout(entry)` | `Unit` | Delete a workout entry by ID |
| `getWorkoutsForDate(startOfDay, endOfDay)` | `Flow<List<WorkoutEntryEntity>>` | All workouts where `date` falls within the given day |
| `getDatesWithWorkouts()` | `Flow<List<Long>>` | All distinct `date` values that have at least one workout |
| `getWorkoutsInRange(start, end)` | `Flow<List<WorkoutEntryEntity>>` | All workouts within a date range (for loading a calendar month) |

All query methods return `Flow` so the UI automatically reacts to database changes.

---

## 8. Screens & User Experience

The app has two primary screens connected by a Material 3 bottom navigation bar, and one secondary screen (About) accessible via an info icon.

### 8.1 Home Screen — "Today"

This is the default screen the user sees when they open the app. It is designed for speed — log a workout in a few taps.

#### Info Icon

- A subtle `ℹ️` icon button is displayed at the top-right of the screen.
- Tapping it navigates to the **About** screen (secondary navigation — not part of the bottom bar).

#### Date & Time Header

- Displays today's date and the current time prominently at the top.
- **Tapping the date** opens a Material 3 `DatePickerDialog`. It defaults to today but allows the user to select any date (past or future).
- **Tapping the time** opens a Material 3 `TimePickerDialog`. It defaults to the current time but allows the user to customize.
- The selected date and time are used when saving the workout entry. If the user doesn't touch them, the entry saves with "right now."

#### Workout Type Selector

- Workout types are organized into **two categories** displayed as labeled sections.
- Each category is a horizontally scrollable row (or a flow layout that wraps) of Material 3 `FilterChip` components.
- A small Material 3 `titleSmall` label above each row identifies the category.

**Category 1 — Cardio & General**

| Chip Label | Emoji |
|------------|-------|
| Running | 🏃 |
| Cycling | 🚴 |
| Swimming | 🏊 |
| Walking | 🚶 |
| HIIT | ⚡ |
| Yoga | 🧘 |
| Stretching | 🤸 |

**Category 2 — Gym / Muscle Groups**

| Chip Label | Emoji |
|------------|-------|
| Chest | 🫁 |
| Back | 🔙 |
| Shoulders | 💪 |
| Biceps | 💪 |
| Triceps | 💪 |
| Legs | 🦵 |
| Abs | 🧱 |
| Full Body | 🏋️ |

- Single selection — tapping one chip selects it and deselects the previous (across both categories).
- The selected chip uses Material 3's selected `FilterChip` style (filled tonal with checkmark).

#### Workout Notes / Details

- Independent, contextually labeled Material 3 `OutlinedTextField` inputs appear in a stable-sorted sequence under a "Workout Notes" header when one or more workout types are selected.
- Users can select multiple workout chips simultaneously.
- Users can type custom, distinct notes for each selected exercise (e.g. pace for running, weight/sets for chest).
- Tapping **"Log Workout"** creates separate workout entries for each selected type, with the same date/time and their respective notes.
- Each text field includes a trailing clear icon button to easily clear its text.
- All notes inputs are automatically cleared after logging.
- Logged workouts can be edited post-logging by clicking on the note text or the edit (pencil) icon on their respective cards, which pops up an "Edit Notes / Details" dialog.

#### Log Workout Button
- A prominent Material 3 `Button` (filled) labeled **"Log Workout"**.
- Enabled only when one or more workout types are selected.
- On tap:
  - Saves the selected workout types + chosen date/time + notes to Room.
  - Clears the chip selection and notes text.
  - Shows a Material 3 `Snackbar`: **"Workout logged!"** (or count if multiple) with an **"Undo"** action.
  - The new entries immediately appear in the list below (Room Flow triggers recomposition).

#### Today's Workout List

- A `LazyColumn` below the input area listing all workouts logged for the currently displayed date.
- Each item is a Material 3 `ElevatedCard` showing:
  - The workout type emoji and label (e.g., "🏃 Running").
  - The time it was logged (e.g., "2:30 PM").
- **Swipe-to-delete**: swiping a card to the left reveals a delete action. On completion:
  - The entry is deleted from Room.
  - A `Snackbar` shows **"Workout deleted"** with **"Undo"**.
- **Swipe-to-delete onboarding tooltip**: on first launch, a tinted tip card appears above the workout list when at least one workout exists: *"💡 Tip: Swipe left on a workout to delete it"* with a **"Got it"** dismiss button. Dismissal is persisted via `SharedPreferences` and never shown again.
- **Empty state**: when no workouts exist for the selected date, show a centered message:
  > "No workouts logged yet. Let's get moving! 💪"

### 8.2 History Screen — "History"

This screen lets users browse their entire workout history through a calendar interface.

#### Calendar View

- A custom-built month-grid calendar composable (7 columns × 5–6 rows).
- Shows the current month by default.
- **Dot indicators**: dates that have at least one logged workout display a small colored dot (primary color) below the date number.
- **Date selection**: tapping a date highlights it and loads that day's workouts in the list below.
- **Today indicator**: today's date always has a distinct outline/ring regardless of selection.
- **Month navigation**: left and right `IconButton` arrows in the header to navigate between months. The header shows the month and year (e.g., "February 2026").
- This allows users to navigate to any past date — even a year ago — and see exactly what they did.

#### Selected Date's Workout List

- Same `LazyColumn` card style as the home screen.
- A date header above the list shows the full selected date (e.g., **"Saturday, February 28, 2026"**).
- All workouts for the selected date are listed.
- Users can swipe-to-delete entries here as well.
- **Empty state** for a date with no workouts:
  > "No workouts on this day."

### 8.3 Bottom Navigation Bar

- Material 3 `NavigationBar` with two destinations:

| Tab | Label | Icon | Route |
|-----|-------|------|-------|
| 1 | Today | `Icons.Default.Home` | `home` |
| 2 | History | `Icons.Default.DateRange` | `history` |

- Follows Material 3 `NavigationBar` guidelines: icon + label, active indicator pill.
- Preserves scroll position and state when switching between tabs.
- The bottom bar is **hidden** when the user navigates to the About screen.

### 8.4 About Screen — Secondary Navigation

Accessible via the `ℹ️` info icon on the Home screen. Not part of the bottom navigation bar. Includes a `TopAppBar` with a back arrow to return.

#### Sections

1. **App header** — App icon emoji, app name (from `strings.xml`), version (read dynamically from `PackageManager`).
2. **Philosophy card** — *"Everyone deserves simple tools."* with a short description of the app's ethos.
3. **Developer card** — Developer name (from `strings.xml`), personal note.
4. **Actions:**
   - **Send Feedback** — `OutlinedButton` that opens the user's email app via `ACTION_SENDTO` with the developer email (from `strings.xml`) and subject pre-filled.
   - **Rate on Play Store** — `OutlinedButton` that opens the Play Store listing using `context.packageName` (dynamically resolved, no hardcoding).
5. **Open Source Licenses** — A list of all runtime libraries used by the app, each displayed as an `ElevatedCard` with name and license type. All current dependencies are Apache License 2.0.

#### Dynamic Values

All configurable values are centralized — **nothing is hardcoded in composables**:

| Value | Source |
|-------|--------|
| App name | `strings.xml` → `R.string.app_name` |
| Developer name | `strings.xml` → `R.string.developer_name` |
| Developer email | `strings.xml` → `R.string.developer_email` |
| Feedback subject | `strings.xml` → `R.string.feedback_subject` |
| App version | `PackageManager.getPackageInfo()` at runtime |
| Package ID (Play Store link) | `context.packageName` at runtime |

---

## 9. Theme & Visual Design

### 9.1 Material You / Dynamic Color

- **Android 12+ (API 31+):** Use `dynamicLightColorScheme(context)` and `dynamicDarkColorScheme(context)` to derive the entire color scheme from the user's wallpaper. This gives the app a personal, native feel on modern devices.
- **Android 11 and below:** Fall back to the custom seed color palette defined below.
- **Light/Dark mode:** Follows the system setting automatically. Both modes are fully supported.

### 9.2 Seed Color Palette (Fallback for API < 31)

A fitness-oriented palette — teal/green conveys health and vitality, amber provides warmth for accents and interactive elements.

| Color Role | Light Mode | Dark Mode |
|------------|------------|-----------|
| Primary | `#2E7D6F` (Teal Green) | `#80CBC4` (Light Teal) |
| On Primary | `#FFFFFF` | `#00332C` |
| Primary Container | `#B2DFDB` | `#00513D` |
| Secondary | `#FFA726` (Amber) | `#FFD54F` (Light Amber) |
| On Secondary | `#FFFFFF` | `#3E2C00` |
| Secondary Container | `#FFE0B2` | `#5C4200` |
| Background | `#FAFAFA` | `#1C1C1E` |
| Surface | `#FFFFFF` | `#1C1C1E` |
| On Surface | `#1C1C1E` | `#E6E1E5` |
| Error | `#D32F2F` | `#EF9A9A` |
| On Error | `#FFFFFF` | `#601410` |

### 9.3 Typography

- Use the Material 3 default type scale from `androidx.compose.material3.Typography`.
- Body text: system default (Roboto on most devices).
- Screen titles: `titleLarge` with medium weight.
- Card content: `bodyMedium` for workout type, `bodySmall` for time.
- No custom fonts in v1 — keeps the APK small and consistent with system style.

### 9.4 Consistency Rules

These rules must be followed across all screens to maintain a cohesive look and feel:

1. **All UI components are Material 3** — no mixing of Material 2 (`androidx.compose.material`) and Material 3 (`androidx.compose.material3`). Import only from `material3`.
2. **No hardcoded colors in composables** — every color must come from `MaterialTheme.colorScheme`. This ensures dynamic color and dark mode work correctly.
3. **Consistent spacing:**
   - Screen horizontal padding: `16.dp`
   - Vertical gap between sections: `16.dp`
   - Gap between list items: `8.dp`
   - Internal card padding: `16.dp`
4. **Consistent corner shapes:** Use Material 3 shape defaults. Do not override `RoundedCornerShape` manually unless absolutely necessary.
5. **Consistent elevation:** Use `ElevatedCard` for workout items (default M3 elevation). Use `Scaffold` for screen structure.

---

## 10. Dependencies

### 10.1 Current State

The project uses the following build tooling:
- AGP `9.1.0`
- Kotlin `2.3.10`
- KSP `2.3.2`
- Gradle with Version Catalog (`libs.versions.toml`)
- Compose BOM `2025.02.00`
- Room `2.7.0`
- All dependencies fully configured and working.

### 10.2 Plugins to Add

| Plugin | Purpose |
|--------|---------|
| `org.jetbrains.kotlin.android` | Kotlin language support |
| `org.jetbrains.kotlin.plugin.compose` | Compose compiler Kotlin plugin |
| `com.google.devtools.ksp` | Annotation processing for Room |

### 10.3 Libraries to Add

| Library | Purpose |
|---------|---------|
| `androidx.compose:compose-bom` | Compose Bill of Materials (version alignment) |
| `androidx.compose.ui:ui` | Core Compose UI |
| `androidx.compose.ui:ui-graphics` | Compose graphics |
| `androidx.compose.ui:ui-tooling-preview` | `@Preview` support |
| `androidx.compose.ui:ui-tooling` | Debug tooling (debugImplementation) |
| `androidx.compose.ui:ui-test-manifest` | Test manifest (debugImplementation) |
| `androidx.compose.material3:material3` | Material 3 components |
| `androidx.activity:activity-compose` | `setContent {}` in Activity |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | `viewModel()` in composables |
| `androidx.lifecycle:lifecycle-runtime-compose` | `collectAsStateWithLifecycle()` |
| `androidx.navigation:navigation-compose` | Navigation with Compose |
| `androidx.room:room-runtime` | Room database runtime |
| `androidx.room:room-ktx` | Room Kotlin extensions (coroutine support) |
| `androidx.room:room-compiler` | Room annotation processor (via KSP) |

### 10.4 Libraries to Remove

| Library | Reason |
|---------|--------|
| `androidx.appcompat:appcompat` | Not needed — Compose replaces the View system |
| `com.google.android.material:material` | Replaced by `androidx.compose.material3` |

### 10.5 Build Configuration Changes

- Enable `buildFeatures { compose = true }` in `app/build.gradle.kts`.
- Set `kotlinOptions { jvmTarget = "11" }`.
- Apply KSP plugin and wire `room-compiler` as `ksp` dependency.

---

## 11. User Flows

### 11.1 Log a Workout — Happy Path

```
Open app
  → Home screen loads with today's date and current time
  → Two category rows visible: "Cardio & General" and "Gym / Muscle Groups"
  → Tap a workout type chip (e.g., "Chest" under Gym)
  → Chip becomes selected (filled + checkmark)
  → Tap "Log Workout" button
  → Entry saved to Room: { category: "gym", type: "Chest", date: today midnight, timestamp: now }
  → Workout appears in today's list below
  → Snackbar: "Workout logged!" [Undo]
  → Chip selection clears, ready for next entry
```

### 11.2 Log a Workout for a Different Date/Time

```
Open app → Home screen
  → Tap date display → DatePickerDialog opens (default: today)
  → Select a past date (e.g., February 15, 2026) → Confirm
  → Tap time display → TimePickerDialog opens (default: current time)
  → Select a custom time (e.g., 7:00 AM) → Confirm
  → Tap a workout type chip
  → Tap "Log Workout"
  → Entry saved with the custom date and time
  → Snackbar confirmation
```

### 11.3 Browse Workout History

```
Tap "History" in bottom navigation
  → Calendar view loads showing current month (February 2026)
  → Dates with workouts show dot indicators
  → Tap a date with a dot (e.g., Feb 15)
  → Date highlights; workouts for Feb 15 appear below
  → Tap left arrow to navigate to January 2026
  → Calendar updates; new dots visible
  → Tap a date in January to see those workouts
  → Keep navigating back to find workouts from a year ago
```

### 11.4 Delete a Workout

```
On Home or History screen
  → Swipe a workout card to the left
  → Card slides away; entry deleted from Room
  → Snackbar: "Workout deleted" [Undo]
  → Tap "Undo" → entry re-inserted, card reappears
```

### 11.5 First-Time Swipe-to-Delete Discovery

```
User logs their first workout ever
  → Workout appears in the list
  → A tinted tip card appears above the list:
      "💡 Tip: Swipe left on a workout to delete it" [Got it]
  → User taps "Got it" → tip disappears permanently
  → Tip never shown again (persisted in SharedPreferences)
```

### 11.6 View About / Send Feedback

```
On Home screen
  → Tap ℹ️ info icon (top-right)
  → About screen opens (bottom nav hidden, back arrow in top bar)
  → User reads app info, developer note, philosophy
  → Tap "Send Feedback" → email app opens with developer email + subject pre-filled
  → OR tap "Rate on Play Store" → Play Store listing opens
  → Scroll down to see open source licenses
  → Tap back arrow → returns to Home screen
```

---

## 12. Implementation Phases

| Phase | Task | Layer | Files |
|-------|------|-------|-------|
| **1** | Add all dependencies; configure Compose, Kotlin, KSP in build files | Build config | `libs.versions.toml`, `build.gradle.kts` (root + app) |
| **2** | Create Material 3 theme with dynamic color, seed palette, typography | Presentation | `Color.kt`, `Theme.kt`, `Type.kt` |
| **3** | Create Room entity, DAO, and database class | Data | `WorkoutEntryEntity.kt`, `WorkoutDao.kt`, `WorkoutDatabase.kt` |
| **4** | Create domain model, repository interface, and use cases | Domain | `WorkoutEntry.kt`, `WorkoutRepository.kt`, `*UseCase.kt` |
| **5** | Create repository implementation with entity ↔ domain mapping | Data | `WorkoutRepositoryImpl.kt` |
| **6** | Create DI container and Application class | DI | `AppContainer.kt`, `WorkoutLoggerApplication.kt` |
| **7** | Build Home screen composable and ViewModel | Presentation | `HomeScreen.kt`, `HomeViewModel.kt` |
| **8** | Build History screen composable and ViewModel | Presentation | `HistoryScreen.kt`, `HistoryViewModel.kt` |
| **9** | Set up Navigation graph, bottom navigation, and MainActivity | Presentation | `AppNavigation.kt`, `MainActivity.kt` |
| **10** | Add About screen (developer info, feedback, licenses), swipe-to-delete onboarding tooltip, info icon in Home screen | Presentation | `AboutScreen.kt`, `HomeScreen.kt`, `AppNavigation.kt`, `strings.xml` |

---

## 13. Out of Scope — v1

The following are explicitly **not** part of the v1 release:

- ❌ User accounts or authentication
- ❌ Cloud sync or backend integration (architecture supports it; not built yet)
- ❌ Custom / user-created workout types (presets only)
- ❌ Workout details — structured duration, sets, reps, distance (free-form notes are supported)
- ❌ Push notifications or daily reminders
- ❌ Data export / import (CSV, JSON)
- ❌ Home screen widget
- ❌ Statistics, charts, or streak tracking
- ❌ Multi-language / localization

---

## 14. Future Roadmap — v2 and Beyond

These features are out of scope for v1 but the architecture is designed to support them cleanly:

| Feature | How the Architecture Supports It |
|---------|----------------------------------|
| **Backend sync** | Add `data/remote/` with API service + remote data source. Update `WorkoutRepositoryImpl` to sync local ↔ remote. Zero changes to domain or presentation. |
| **Custom workout types** | Add a `WorkoutType` entity + DAO + domain model. Add a "Manage Types" screen in presentation. The existing `workoutType: String` field already stores the name. |
| **Workout details** (duration, notes, sets) | Add nullable fields to `WorkoutEntryEntity` and `WorkoutEntry`. Update the Home screen to show optional input fields. |
| **Statistics dashboard** | Add new use cases (`GetWeeklyStatsUseCase`, `GetStreakUseCase`). Add a new "Stats" tab in bottom navigation. |
| **Hilt migration** | Replace `AppContainer` with `@Module` + `@Provides`. Add `@HiltViewModel` to ViewModels. Package structure stays the same. |
| **Daily reminders** | Add `WorkManager` task in a new `data/worker/` package. Add notification permission handling. |
| **Data export** | Add export logic in a use case. Serialize `WorkoutEntry` list to JSON/CSV. Share via Android share sheet. |

---

## 15. Acceptance Criteria

The v1.1 release is considered complete when:

**Core (v1.0) — ✅ All complete:**

- [x] User can open the app and see the Home screen with today's date and time.
- [x] User can select a workout type from the preset chips.
- [x] User can tap "Log Workout" and the entry is saved to Room.
- [x] User can see all workouts logged for the selected date in a list.
- [x] User can change the date and time before logging.
- [x] User can swipe to delete a workout with undo support.
- [x] User can navigate to the History screen via bottom navigation.
- [x] History screen shows a month calendar with dot indicators on dates with workouts.
- [x] User can navigate between months in the calendar.
- [x] User can tap any date in the calendar to see that day's workouts.
- [x] The app follows Material 3 / Material You theming with dynamic color on API 31+.
- [x] The app supports both light and dark mode.
- [x] The app works fully offline with no network dependency.
- [x] All data persists across app restarts (Room database).

**About & Onboarding (v1.1) — ✅ All complete:**

- [x] User can tap the info icon on the Home screen to open the About screen.
- [x] About screen shows app name, version, developer info, and philosophy.
- [x] User can tap "Send Feedback" to open their email app with the developer email pre-filled.
- [x] User can tap "Rate on Play Store" to open the app's Play Store listing.
- [x] About screen lists all open source libraries used by the app.
- [x] About screen is **not** in the bottom navigation — it is secondary navigation with a back arrow.
- [x] First-time users see a dismissible tooltip explaining swipe-to-delete.
- [x] The tooltip persists its dismissed state across app restarts.
- [x] No hardcoded package IDs or developer info in composables — all dynamic or from `strings.xml`.

---

## 16. Changelog

| Version | Date | Changes |
|---------|------|---------|
| **1.0** | February 28, 2026 | Initial release — Home screen with workout logging, History screen with calendar view, swipe-to-delete with undo, Material 3 theming with dynamic color, Room database for offline persistence. |
| **1.1** | March 31, 2026 | Added About screen (developer info, send feedback via email, rate on Play Store, open source licenses). Moved About to secondary navigation (info icon on Home screen, not in bottom nav). Added swipe-to-delete onboarding tooltip for first-time users. Removed all hardcoded values — package ID read from `context.packageName`, developer info from `strings.xml`. Added privacy policy. Enabled resource shrinking and ProGuard line-number preservation for production builds. Configured backup rules for Room database. |
| **1.2** | June 7, 2026 | Added free-form notes/details option when logging a workout. Added support for multi-workout selection during logging, and post-logging edit note functionality. Maintained database version at 1 with destructive schema recreation. |

