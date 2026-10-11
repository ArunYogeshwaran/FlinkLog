# FlinkLog — Agent Guidelines & Memory File

This workspace contains project-specific rules, design patterns, and lessons learned during the
development of FlinkLog. Future AI agents must read and strictly adhere to these instructions to
prevent regressions.

---

## 💡 Lessons Learned & Critical Rules

### 1. Adaptive Icon Handling in Compose

* **Problem:** Google Play's adaptive icons (`R.mipmap.ic_launcher`) use `<adaptive-icon>` XML
  structures containing foreground and background layers. Loading this directly in Compose using
  `painterResource()` will trigger a fatal `IllegalArgumentException` at runtime.
* **Rule:** Always wrap adaptive icons in a standard platform `AndroidView` with an `ImageView`:
  ```kotlin
  AndroidView(
      factory = { ctx ->
          ImageView(ctx).apply {
              setImageResource(R.mipmap.ic_launcher)
          }
      },
      modifier = Modifier.size(80.dp)
  )
  ```

### 2. String Resource Formatting & Percent Escaping

* **Problem:** Android's `strings.xml` parser treats raw `%` symbols followed by letters (e.g.,
  `100% open-source`) as format specifiers (interpreting `% o` as octal `%o`). This causes
  compilation failures, PluralsCandidate lint warnings, or runtime formatter crashes.
* **Rule:** Always escape `%` characters in `strings.xml` as `%%` (e.g., `100%% open-source`).

### 3. Configuration-Aware Resource Fetching

* **Problem:** Reading resources via `LocalContext.current.getString()` inside Composables or async
  contexts (like `LaunchedEffect` collectors or dialog buttons) triggers
  `LocalContextGetResourceValueCall` lint errors. These calls are not reactive to configuration
  changes (like language or orientation changes), resulting in stale UI text.
* **Rule:**
    * For standard UI text, use `stringResource(...)`.
    * For resource lookups inside callback lambdas, coroutines, or event collectors, query
      `LocalResources.current` instead:
      ```kotlin
      val resources = LocalResources.current
      LaunchedEffect(Unit) {
          viewModel.events.collect { event ->
              // Safe, configuration-aware string retrieval:
              val msg = resources.getString(R.string.workout_deleted_msg)
          }
      }
      ```

### 4. Code Minification & Reflection Safeguards (Android 16 AppFunctions)

* **Problem:** AppFunctions rely heavily on compilation reflection. Code shrinkers (like R8) will
  strip out or rename metadata classes in release builds, causing runtime AppFunction execution
  failure.
* **Rule:** Keep proguard rules in `app/proguard-rules.pro` to safeguard AppFunction compilation
  pipelines.

### 5. Release Preparation & App Store / GitHub Release Notes

* **Rule:** Whenever preparing a release build or pushing a release tag, the agent must
  automatically generate a professional Release Title and Release Notes tailored for GitHub based on
  the changelog updates, and prompt the user to copy/paste them.
* **Rule (Play Store Listing & Notes):** Future changes should **not** include anything technical in
  the Play Store listing or "What's new" release notes. Avoid technical jargon (e.g., ADB commands).
  Keep all public-facing text sleek, simple, and strictly user-focused.

### 6. Notes Auto-Suggestions in Bottom Sheet

* **Problem:** Displaying suggestions inline can clutter the screen or shift the keyboard layout,
  causing layout overlap or input focus issues.
* **Rule:** Suggestions must be loaded dynamically based on the current active workout type using
  `getRecentNotesForWorkoutType` and deduplicated against the user's active input. Suggestions must
  be hidden inside a trailing drop-down menu (`DropdownMenu`) on the text field, accessible only
  when suggestions are available and the user clicks the drop-down arrow icon.

### 7. Workout Sharing

* **Rule:** When adding or updating share features, do not duplicate text formatting logic in
  Composable files. Use the shared `shareWorkouts` helper in `WorkoutComponents.kt` to compose the
  shared content, ensuring consistent typography (bullet points, emojis, tagline, and app link).

### 8. Room Database Schema Migrations & Data Preservation

* **Problem:** If you increment the database version in `WorkoutDatabase` without writing a
  migration path, Room's destructive fallback policy (`fallbackToDestructiveMigration(true)`) will
  silently drop all user tables and recreate them. This results in complete user data loss and
  eventual backup corruption (since the restored old cloud backup gets overwritten by the empty
  version on the next sync cycle).
* **Rule:** For any changes to entity classes or database schema, you MUST:
    1. Increment the database `version` in `WorkoutDatabase.kt`.
    2. Write a custom `Migration` class detailing the schema modification SQL queries (e.g.
       `ALTER TABLE`, `CREATE TABLE`).
    3. Register the migration object in the Room database builder using `.addMigrations()`.
    4. Never rely solely on destructive fallbacks for schema modifications in release updates.

### 9. Compose `reverseLayout` Sibling Ordering in LazyColumn

* **Problem:** When using `LazyColumn(reverseLayout = true)` for bottom-anchored feeds (e.g. chat or
  agenda timelines where the latest entries anchor at the bottom), emitting multiple sibling
  composables (such as a date header `Text` and a workout card) directly inside an `items` block causes
  Compose to stack the siblings bottom-to-top. This visually places the date header *below* the card or
  inverts their intended visual order.
* **Rule:** Always wrap sibling composables emitted within an inverted `LazyColumn` item inside a
  single `Column { ... }` container so that child components strictly render top-to-bottom.

### 10. Feed Date Grouping vs Creation Timestamps

* **Problem:** Grouping feed items by checking raw `workout.timestamp` values can split workouts from
  the same calendar day across different headers if workouts were logged late at night or backdated.
* **Rule:** Always group workouts by comparing their normalized midnight calendar epoch `workout.date`
  (`workout.date != prevWorkout.date`) rather than creation `timestamp`.

---

## ⚖️ Compliance & Rating Rules

* **IARC Re-rating:** Any change that introduces new content profiles (e.g., violence, UGC, social
  features, in-app purchases) requires re-doing the IARC questionnaire. When making such changes,
  you **must** flag a warning in your PR description or commit message. See `docs/COMPLIANCE.md` for
  the full list of triggers and the exact warning template to use.

---

## 🔒 Security & Privacy Rules

* **Personal Info Scrubbing:** Never write raw personal email addresses in code, strings, or
  Markdown files. If a contact email must be shown in documentation (e.g. `PRIVACY_POLICY.md`), use
  an obfuscated layout like: `h.arunbuilds+workoutlogger [at] gmail [dot] com`.
* **Git Author Email:** All commits must use the GitHub no-reply address:
  `ArunYogeshwaran@users.noreply.github.com`.

---

## 🏗️ Core Architecture & Flow Rules

* **Architecture:** Follow MVVM with clean database access:
    * Presentation: Jetpack Compose (using custom Canvas graphics for light weight).
    * Database: SQLite via Android Room (configured for Google Auto Backup with WAL/SHM file
      handling).
    * AppFunctions: Keep `WorkoutAppFunctions` in sync with any changes made to `WorkoutRepository`.
* **Compilation & Linting:** Always run `./gradlew compileDebugKotlin lintDebug` to verify changes
  before making any commit.

---

## 📝 Git Commit Guidelines

* **Commit Message Format:** All commits must follow the **Conventional Commits** specification (the
  standard format used in European Union/European Commission open source repositories).
* **Structure:** `<type>(<scope>): <subject>`
    * `type`: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `chore`, `build`, `ci`.
    * `scope` (optional): The package or module affected (e.g. `(ui)`, `(db)`, `(appfunctions)`).
    * `subject`: A concise, imperative mood summary (e.g., `feat: add templates`, not
      `Added templates`).
* **Length Constraints:** Keep commit messages short and concise. The subject line must be under 50
  characters, and no line of the commit message should exceed 72 characters.

---

## 🛑 Rule & Memory Maintenance

* **Mandatory Update:** Whenever you introduce a new feature, database schema change, AppFunction
  API, library dependency, or resolve a complex compilation/lint issue, you **must** update:
    * The corresponding feature file under docs/features/ and/or docs/README/ with updated
      specifications and verification test cases.
    * DEVELOPMENT.md with developer instructions.
    * .agents/AGENTS.md with agent guidelines.
      This must be done before completing the task to ensure product specifications remain the
      single source of truth.

