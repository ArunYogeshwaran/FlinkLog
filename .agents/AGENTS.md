# FlinkLog — Agent Guidelines & Memory File

This workspace contains project-specific rules, design patterns, and lessons learned during the development of FlinkLog. Future AI agents must read and strictly adhere to these instructions to prevent regressions.

---

## 💡 Lessons Learned & Critical Rules

### 1. Adaptive Icon Handling in Compose
*   **Problem:** Google Play's adaptive icons (`R.mipmap.ic_launcher`) use `<adaptive-icon>` XML structures containing foreground and background layers. Loading this directly in Compose using `painterResource()` will trigger a fatal `IllegalArgumentException` at runtime.
*   **Rule:** Always wrap adaptive icons in a standard platform `AndroidView` with an `ImageView`:
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
*   **Problem:** Android's `strings.xml` parser treats raw `%` symbols followed by letters (e.g., `100% open-source`) as format specifiers (interpreting `% o` as octal `%o`). This causes compilation failures, PluralsCandidate lint warnings, or runtime formatter crashes.
*   **Rule:** Always escape `%` characters in `strings.xml` as `%%` (e.g., `100%% open-source`).

### 3. Configuration-Aware Resource Fetching
*   **Problem:** Reading resources via `LocalContext.current.getString()` inside Composables or async contexts (like `LaunchedEffect` collectors or dialog buttons) triggers `LocalContextGetResourceValueCall` lint errors. These calls are not reactive to configuration changes (like language or orientation changes), resulting in stale UI text.
*   **Rule:** 
    *   For standard UI text, use `stringResource(...)`.
    *   For resource lookups inside callback lambdas, coroutines, or event collectors, query `LocalResources.current` instead:
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
*   **Problem:** AppFunctions rely heavily on compilation reflection. Code shrinkers (like R8) will strip out or rename metadata classes in release builds, causing runtime AppFunction execution failure.
*   **Rule:** Keep proguard rules in `app/proguard-rules.pro` to safeguard AppFunction compilation pipelines.

### 5. Release Preparation & GitHub Release Notes
*   **Rule:** Whenever preparing a release build or pushing a release tag, the agent must automatically generate a professional Release Title and Release Notes tailored for GitHub based on the changelog updates, and prompt the user to copy/paste them.

---

## 🔒 Security & Privacy Rules
*   **Personal Info Scrubbing:** Never write raw personal email addresses in code, strings, or Markdown files. If a contact email must be shown in documentation (e.g. `PRIVACY_POLICY.md`), use an obfuscated layout like: `h.arunbuilds+workoutlogger [at] gmail [dot] com`.
*   **Git Author Email:** All commits must use the GitHub no-reply address: `ArunYogeshwaran@users.noreply.github.com`.

---

## 🏗️ Core Architecture & Flow Rules
*   **Architecture:** Follow MVVM with clean database access:
    *   Presentation: Jetpack Compose (using custom Canvas graphics for light weight).
    *   Database: SQLite via Android Room (configured for Google Auto Backup with WAL/SHM file handling).
    *   AppFunctions: Keep `WorkoutAppFunctions` in sync with any changes made to `WorkoutRepository`.
*   **Compilation & Linting:** Always run `./gradlew compileDebugKotlin lintDebug` to verify changes before making any commit.
