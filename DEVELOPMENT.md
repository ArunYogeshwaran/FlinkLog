# Developer Guidelines

All developers (human and AI agents) contributing to FlinkLog must follow these standards to preserve compilation, localization (i18n), and code quality integrity.

---

## 🏗️ 1. Core Architecture

FlinkLog is designed as an offline-first, lightweight (<3MB footprint), privacy-first Android application. It implements clean architecture with MVVM:
*   **Presentation Layer:** Jetpack Compose using Material You dynamic color themes. Lightweight graphics (e.g. empty states, brand logo) are drawn programmatically using Compose `Canvas` to minimize package size.
*   **Domain Layer:** Decoupled interfaces, use cases, and models representing workouts and history.
*   **Data Layer:** Room SQLite storage (configured with robust system Auto Backup guidelines covering DB, WAL, and SHM files).
*   **AppFunctions Layer:** Exposed capabilities for on-device assistant integration.

---

## 💡 2. AI Coding Agent Rules & Common Pitfalls

### 2.1 Adaptive Launcher Icons
*   **Issue:** Loading adaptive XML icons (`R.mipmap.ic_launcher`) via Compose `painterResource()` will trigger an immediate runtime crash (`IllegalArgumentException`).
*   **Rule:** Always wrap launcher/adaptive icons in an `AndroidView`/`ImageView` wrapper:
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

### 2.2 XML String Formatting (Percent Escaping)
*   **Issue:** A raw `%` inside `strings.xml` (e.g. `100% open-source`) is interpreted as a formatter tag, which triggers compilation warnings (`PluralsCandidate`) and can crash `String.format` calls at runtime.
*   **Rule:** Always escape raw `%` inside `strings.xml` as `%%` (e.g., `100%% open-source`).

### 2.3 Compose Resource Lookups (Lint Compatibility)
*   **Issue:** Inside composables or async collectors (like `LaunchedEffect`), retrieving resources via `context.getString(...)` on context resolved via `LocalContext.current` triggers `LocalContextGetResourceValueCall` lint errors.
*   **Rule:** Standardize lookups:
    *   In Composables: Use `stringResource(...)`.
    *   In callbacks, coroutines, or flow collectors: Reference the configuration-aware `LocalResources.current` instead:
        ```kotlin
        val resources = LocalResources.current
        LaunchedEffect(Unit) {
            viewModel.events.collect { event ->
                val msg = resources.getString(R.string.workout_deleted_msg)
            }
        }
        ```

---

## 🌎 3. Localization & Formatting Standards

*   **No Hardcoded UI Strings:** All user-visible strings must exist in `strings.xml`. Use `stringResource(R.string.id)` or `pluralStringResource(R.plurals.id, count)` in Composable scopes.
*   **Date & Time Layouts:** Never hardcode time/date formats. Query system regional formatting settings:
    ```kotlin
    // Time format
    val timeFormat = android.text.format.DateFormat.getTimeFormat(context)
    // Date format
    val formattedDate = android.text.format.DateUtils.formatDateTime(context, millis, flags)
    ```

---

## 🛡️ 4. Obfuscation & AppFunctions Reflection Keep Rules

Android 16 AppFunctions rely heavily on reflection during compilation. R8 optimization will strip these bindings in release builds. Ensure `app/proguard-rules.pro` contains the required keep rules:
```proguard
-keep class com.ayogeshwaran.workoutlogger.appfunctions.** { *; }
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
```

---

## 🧪 5. Verification Commands

Before staging or committing any code changes, always verify compilation and lint metrics locally:
```bash
./gradlew compileDebugKotlin lintDebug
```
All commits must pass both compilation and Android Lint checks without errors.
