# Feature Specification: On-Device AI (AppFunctions)

This document details the functional specifications and verification test cases for the Android 16 AppFunctions integration.

---

## 🏗️ 1. Technical Reference
*   **Logical Components:**
    *   `WorkoutAppFunctions` in [WorkoutAppFunctions.kt](../../app/src/main/java/com/ayogeshwaran/workoutlogger/appfunctions/WorkoutAppFunctions.kt) (Exposes local helper methods)
*   **JVM Unit Tests:**
    *   [WorkoutAppFunctionsTest.kt](../../app/src/test/java/com/ayogeshwaran/workoutlogger/appfunctions/WorkoutAppFunctionsTest.kt)

---

## 📋 2. Functional Requirements
FlinkLog integrates with Android 16 AppFunctions to expose system-level triggers for on-device AI assistants.
1.  **Exposed APIs:**
    *   `logWorkout(workoutType: String, notes: String, timestamp: Long)`: Inserts a workout log record directly to local storage.
    *   `getWorkoutsForRange(startTimeMillis: Long, endTimeMillis: Long)`: Queries and returns sorted list of workouts in the range.
    *   `getCustomWorkoutTypes()`: Returns user-defined custom categories list.
    *   `suggestWorkout(preferenceType: String)`: Recommends exercises based on:
        *   `"LEAST_RECENT"`: Suggests the exercise neglected for the longest time over the last 30 days.
        *   `"WEEKDAY_PATTERN"`: Learns weekly routine patterns and suggests what the user typically logs on this day of the week.
2.  **Private Execution:** Processing is done 100% on-device, bypassing remote internet servers.

---

## 🧪 3. Verification Test Suite

### Test Case 5.1: ADB verification scripts (Local execution)
Connect your device/emulator with USB debugging active, and execute commands:

#### 1. Verify Registry Status
```bash
adb shell cmd app_function list-app-functions | grep com.ayogeshwaran.workoutlogger
```
*   *Expected Result:* Displays registered functions list: `logWorkout`, `getWorkoutsForRange`, `getCustomWorkoutTypes`, `suggestWorkout`.

#### 2. Execute `logWorkout`
```bash
adb shell cmd app_function execute-app-function \
  --package com.ayogeshwaran.workoutlogger \
  --function logWorkout \
  --parameters '{"workoutType":"Gym","notes":"Chest day","timestamp":1781683200000}'
```
*   *Expected Result:* Returns success code and saves a Chest workout inside the database.

#### 3. Execute `getWorkoutsForRange`
```bash
adb shell cmd app_function execute-app-function \
  --package com.ayogeshwaran.workoutlogger \
  --function getWorkoutsForRange \
  --parameters '{"startTimeMillis":0,"endTimeMillis":1900000000000}'
```
*   *Expected Result:* Returns a JSON array listing all logged workouts.

#### 4. Execute `suggestWorkout` (LEAST_RECENT mode)
```bash
adb shell cmd app_function execute-app-function \
  --package com.ayogeshwaran.workoutlogger \
  --function suggestWorkout \
  --parameters '{"preferenceType":"LEAST_RECENT"}'
```
*   *Expected Result:* Returns the neglected workout type and rationale.

---

## 🤖 4. Executing Automated Tests
AI agents can run unit tests verifying AppFunctions logic:
```bash
./gradlew testDebugUnitTest --tests "com.ayogeshwaran.workoutlogger.appfunctions.WorkoutAppFunctionsTest"
```
