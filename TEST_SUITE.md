# Test Suite & Verification Cases

This document describes the manual and automated verification procedures for FlinkLog. Use these test cases to validate regressions whenever making code modifications.

---

## 🏗️ Preconditions for Testing
*   **Android Device/Emulator:** Run Android 10+ (Android 16 required for AppFunctions).
*   **ADB Access:** Developer options enabled with USB debugging active.
*   **Keystore Setup:** Ensure debug or release keys are active for signing.

---

## 🧪 Test Case Catalog

### Feature 1: Core Workout Logging
*   **Goal:** Verify selecting, annotating, and logging workouts.
*   **Steps:**
    1.  Launch the app and open the Home ("Today") tab.
    2.  Select the **Cardio** and **Chest** activity chips.
    3.  Verify the bottom **Log Workout** button becomes active.
    4.  Tap **Log Workout** to open the details bottom sheet.
    5.  Under Cardio, enter the note `"Warmup 10m"`. Click **Copy to All** next to the input.
    6.  Verify that `"Warmup 10m"` is instantly populated into the **Chest** notes field.
    7.  Tap **Log Workout** inside the bottom sheet.
*   **Expected Results:**
    *   The bottom sheet dismisses.
    *   Both logged cards appear at the bottom list for today.
    *   A confirmation Snackbar is displayed showing `"2 workouts logged!"` with an active `"Undo"` action.

### Feature 2: Logging Undo
*   **Goal:** Verify that undoing a logging event successfully deletes records.
*   **Steps:**
    1.  Perform Feature 1 Logging steps to log 2 workouts.
    2.  Immediately tap the **Undo** action on the confirmation Snackbar.
*   **Expected Results:**
    *   Both logged cards are removed from today's list.
    *   The database records are deleted.

### Feature 3: Post-Logging Note Editing
*   **Goal:** Verify changing notes on already logged workouts.
*   **Steps:**
    1.  Tap on the note text of any card in today's list (e.g. the Chest card).
    2.  An **Edit Notes** dialog should appear.
    3.  Change the text to `"Benchpress 3x10 60kg"` and tap **Save**.
*   **Expected Results:**
    *   The dialog dismisses.
    *   The Chest card notes immediately update to `"Benchpress 3x10 60kg"`.
    *   Changes persist across app restarts.

### Feature 4: Swipe to Delete & Undo
*   **Goal:** Verify deleting workouts via gesture and restoring them.
*   **Steps:**
    1.  Locate any logged workout card on the Home or History lists.
    2.  Swipe the card to the left.
    3.  Verify that the card disappears from the list.
    4.  Verify a Snackbar showing `"Workout deleted"` with an `"Undo"` button is displayed.
    5.  Tap the **Undo** action.
*   **Expected Results:**
    *   The workout card is restored to its exact position with its original details.
    *   The database record is restored.

### Feature 5: Custom Category Lifecycle
*   **Goal:** Verify adding and deleting user-defined workout exercises.
*   **Steps:**
    1.  Under Gym or Cardio categories, tap the **`+`** chip.
    2.  Enter the name `"Pilates"` and tap **Save**.
    3.  Verify that the `"Pilates"` chip appears at the end of the category selection chips.
    4.  Tap the **`x`** close button on the newly created custom `"Pilates"` chip.
    5.  A deletion confirmation dialog should appear. Tap **Delete**.
*   **Expected Results:**
    *   The `"Pilates"` chip is removed from the chips list.
    *   Tapping delete does not affect previously logged workouts associated with `"Pilates"`.

### Feature 6: History Navigation & Calendar View
*   **Goal:** Verify calendar indicators, dates selection, and view toggles.
*   **Steps:**
    1.  Navigate to the **History** tab.
    2.  Verify today's date has a blue indicator ring.
    3.  Log a workout for yesterday.
    4.  Go to History and verify yesterday's calendar cell displays a workout indicator dot.
    5.  Tap the **Weekly** toggle. Verify the grid collapses into a single-line 7-day row.
    6.  Tap the **Monthly** toggle to expand the grid back to the month view.
*   **Expected Results:**
    *   Tapping any cell displays the list of logged workouts for that day below the calendar.
    *   Empty days display the restful moon illustration.

### Feature 7: Google Auto Backup
*   **Goal:** Verify that backup transaction scopes cover all DB files.
*   **Steps:**
    1.  Log 3 workouts and create a custom category.
    2.  Force a backup via ADB terminal:
        ```bash
        adb shell bmgr backupnow com.ayogeshwaran.workoutlogger
        ```
    3.  Uninstall the application:
        ```bash
        adb uninstall com.ayogeshwaran.workoutlogger
        ```
    4.  Reinstall the application.
    5.  Launch the app and open the Home/History lists.
*   **Expected Results:**
    *   All 3 logged workouts and custom categories are fully restored from the Google Account.

### Feature 8: Android 16 AppFunctions (Voice/AI Actions)
*   **Goal:** Verify local AppFunctions execute successfully without network bounds.
*   **Command Line Verification Steps:**
    *   **Check App Register:**
        ```bash
        adb shell cmd app_function list-app-functions | grep com.ayogeshwaran.workoutlogger
        ```
    *   **Verify `logWorkout`:**
        ```bash
        adb shell cmd app_function execute-app-function \
          --package com.ayogeshwaran.workoutlogger \
          --function logWorkout \
          --parameters '{"workoutType":"Gym","notes":"Chest day","timestamp":1781683200000}'
        ```
    *   **Verify `getWorkoutsForRange`:**
        ```bash
        adb shell cmd app_function execute-app-function \
          --package com.ayogeshwaran.workoutlogger \
          --function getWorkoutsForRange \
          --parameters '{"startTimeMillis":0,"endTimeMillis":1900000000000}'
        ```
    *   **Verify `getCustomWorkoutTypes`:**
        ```bash
        adb shell cmd app_function execute-app-function \
          --package com.ayogeshwaran.workoutlogger \
          --function getCustomWorkoutTypes
        ```
    *   **Verify `suggestWorkout`:**
        ```bash
        adb shell cmd app_function execute-app-function \
          --package com.ayogeshwaran.workoutlogger \
          --function suggestWorkout \
          --parameters '{"preferenceType":"LEAST_RECENT"}'
        ```
*   **Expected Results:**
    *   Commands execute with `SUCCESS` status and return correct JSON structures.
