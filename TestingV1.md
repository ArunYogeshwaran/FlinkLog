## Internal Testing Release Notes

### Full Features list for Testers
* **Frictionless Logging:** Tap activity chips, specify details/notes, copy notes across multi-selected items with "Copy to All", log with one tap, or undo within 5 seconds.
* **History Feed & Calendar:** Toggle calendar between Month view and 7-day Week view. Tap any day to view logs. Inline post-log note editing and swipe-to-delete.
* **Custom Workout Types:** Create permanent custom categories using the "+" chip and delete custom ones via the "x" chip.
* **Backup & Restore:** Robust end-to-end encrypted backup of local database files (DB, WAL, SHM) using Google Auto Backup.
* **AppFunctions (On-Device AI):** Exposes `logWorkout`, `getWorkoutsForRange`, `getCustomWorkoutTypes`, and `suggestWorkout` (with neglection and routine-learning intelligence) to local assistant agents.

---

## Testing Guide

### Test Case 1: Logging Flow & "Copy to All"
1. Select **Cardio** and **Chest** chips on the Home screen.
2. Tap **Log Workout**.
3. Under Cardio, type `"Warmup 10m"`. Click **Copy to All** next to the field.
4. Verify the note is instantly copied to the Chest field.
5. Tap **Log Workout** inside the bottom sheet.
6. Verify workouts appear in today's log list at the bottom.
7. Tap the delete icon or swipe left on the Chest card. Verify it vanishes, a toast appears with an **Undo** action, and clicking **Undo** restores the card.

### Test Case 2: Custom Chip Lifecycle
1. Under Gym or Cardio categories, tap the **`+`** chip.
2. Enter a custom name (e.g. `"Pilates"`) and tap save.
3. Verify `"Pilates"` chip appears.
4. Tap the **`x`** close button on the custom chip and confirm deletion.

### Test Case 3: History Calendar & Onboarding
1. Navigate to **History** tab.
2. Verify today's date has a blue dot indicator.
3. Tap **Weekly** toggle. Verify the calendar collapses into a 7-day view.
4. Tap a date in the past. Verify the empty state shows the placeholder illustration.
5. For new installs, verify the swipe-to-delete onboarding tutorial card is displayed and disappears when dismissed.

### Test Case 4: Google Auto Backup (Device Transition)
1. Add a custom category and log 3 workouts.
2. Force a backup via ADB:
   ```bash
   adb shell bmgr backupnow com.ayogeshwaran.workoutlogger
   ```
3. Uninstall the app:
   ```bash
   adb uninstall com.ayogeshwaran.workoutlogger
   ```
4. Reinstall the app. Launch it and verify your logged history and custom category are fully restored.

### Test Case 5: Android 16 AppFunctions (On-Device AI Assistant)
Verify that system-level AI tools can interact with the app via ADB:
* **Check Service Availability:**
  ```bash
  adb shell cmd app_function help
  ```
* **List AppFunctions:**
  ```bash
  adb shell cmd app_function list-app-functions | grep com.ayogeshwaran.workoutlogger
  ```
* **Log Workout:**
  ```bash
  adb shell cmd app_function execute-app-function \
    --package com.ayogeshwaran.workoutlogger \
    --function logWorkout \
    --parameters '{"workoutType":"Gym","notes":"Back day","timestamp":1781683200000}'
  ```
* **Query Workouts For Range:**
  ```bash
  adb shell cmd app_function execute-app-function \
    --package com.ayogeshwaran.workoutlogger \
    --function getWorkoutsForRange \
    --parameters '{"startTimeMillis":0,"endTimeMillis":1900000000000}'
  ```
* **Get Custom Workout Types:**
  ```bash
  adb shell cmd app_function execute-app-function \
    --package com.ayogeshwaran.workoutlogger \
    --function getCustomWorkoutTypes
  ```
* **Get Workout Suggestions:**
  ```bash
  adb shell cmd app_function execute-app-function \
    --package com.ayogeshwaran.workoutlogger \
    --function suggestWorkout \
    --parameters '{"preferenceType":"LEAST_RECENT"}'
  ```

### Play Console "What's New" (Under 500 chars limit)
```
v1.0 Internal Test. Verify:
1. Log workouts, add notes, use "Copy to All" in sheet.
2. Swipe left to delete (with Undo) on Home & History.
3. Custom category addition (+) & deletion (x).
4. Calendar vs. 7-day Weekly view toggle in History.
5. Google Auto Backup data recovery on reinstall.
6. Android 16 AppFunctions: Use Gemini/voice to log, query history, & request suggestions ("LEAST_RECENT"/"WEEKDAY_PATTERN" modes).
```
