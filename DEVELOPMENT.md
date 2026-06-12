# Developer Guidelines — Workout Logger

All developers and coding agents must follow these standards to preserve localization (i18n) and formatting integrity.

---

## 1. Localization Standards
* **No Hardcoded UI Strings:** Raw string literals must never be passed to user-visible views.
* **Compose UI:** Use `stringResource(R.string.id)` for static text and `pluralStringResource(R.plurals.id, count)` for plurals.
* **Event Handlers / Callbacks:** Resolve strings via Android `Context` in non-composable scopes:
  ```kotlin
  val msg = context.resources.getQuantityString(R.plurals.workouts_logged, count, count)
  val action = context.getString(R.string.undo)
  ```
* **Domain Models:** Store stable IDs in the database/model. Resolve localized display names in the presentation layer via `@param:StringRes` properties and Compose extension functions:
  ```kotlin
  @Composable
  fun WorkoutEntry.localizedType(): String {
      val preset = PresetWorkoutTypes.find { it.name.equals(workoutType, ignoreCase = true) }
      return preset?.let { stringResource(it.nameRes) } ?: workoutType
  }
  ```

---

## 2. Date & Time Formatting
* **No Hardcoded Patterns:** Do not use hardcoded formatting templates (e.g., `"h:mm a"` or `"EEEE, dd/MM"`).
* **Time Formatting:** Respect system 12h/24h clocks using:
  ```kotlin
  val timeFormat = remember(context) { android.text.format.DateFormat.getTimeFormat(context) }
  ```
* **Date Formatting:** Respect system regional date formatting:
  ```kotlin
  val formattedDate = android.text.format.DateUtils.formatDateTime(context, millis, flags)
  ```
* **Calendar Weekdays:** Dynamically layout weekdays using the system locale's first day of week:
  ```kotlin
  val firstDayOfWeekSetting = cal.firstDayOfWeek
  val offset = (firstDayOfMonth - firstDayOfWeekSetting + 7) % 7
  ```

---

## 3. Automated Verification
* Android Lint will fail the build on any hardcoded XML layout text.
* Run verification tasks locally before committing code:
  ```bash
  ./gradlew compileDebugKotlin lintDebug
  ```
