# Feature Specification: Logging & Feed

This document details the functional specifications and verification test cases for the workout logging and daily feed features.

---

## 🏗️ 1. Technical Reference
*   **Key Composables:**
    *   `HomeScreen` in [HomeScreen.kt](../../app/src/main/java/com/ayogeshwaran/workoutlogger/presentation/home/HomeScreen.kt) (Main viewport)
    *   `ActionsRow` and bottom sheet in [HomeScreen.kt](../../app/src/main/java/com/ayogeshwaran/workoutlogger/presentation/home/HomeScreen.kt)
    *   `SwipeToDeleteWorkoutCard` in [SwipeToDeleteWorkoutCard.kt](../../app/src/main/java/com/ayogeshwaran/workoutlogger/presentation/components/SwipeToDeleteWorkoutCard.kt)
*   **Logical Components:**
    *   `HomeViewModel` in [HomeViewModel.kt](../../app/src/main/java/com/ayogeshwaran/workoutlogger/presentation/home/HomeViewModel.kt) (MVVM lifecycle)
    *   `WorkoutRepository` in [WorkoutRepository.kt](../../app/src/main/java/com/ayogeshwaran/workoutlogger/domain/repository/WorkoutRepository.kt) (Database calls)
*   **JVM Unit Tests:**
    *   [HomeViewModelTest.kt](../../app/src/test/java/com/ayogeshwaran/workoutlogger/presentation/home/HomeViewModelTest.kt)

---

## 📋 2. Functional Requirements
1.  **Date Defaults:** All logs default to the current system date/time. The user can override logging time using picker options inside the logging sheet.
2.  **Multi-Activity Entry:** Tapping Cardio or Gym exercise chips activates multi-selection. Tapping "Log Workout" opens a bottom sheet with a text entry card for each selected exercise.
3.  **"Copy to All" Notes:** Tapping the copy icon next to any note text field instantly copies its content to all other text inputs inside the bottom sheet.
4.  **Swipe to Delete:** Users can swipe any logged card to the left to delete it. A confirmation Snackbar appears with an "Undo" action.
5.  **Swipe Tutorial:** On new installs, a guidance card shows how to swipe to delete. The card is permanently hidden once dismissed by the user.
6.  **Post-Log Note Editing:** Tapping the note text of a logged card opens an edit dialog to change notes.

---

## 🧪 3. Verification Test Suite

### Test Case 1.1: Multi-Exercise Log & Note Copying
1.  Launch FlinkLog.
2.  Select **Cardio** and **Chest** chips on the Home screen.
3.  Tap the bottom **Log Workout** button.
4.  Under the Cardio note field, type: `"Warmup 10 mins"`.
5.  Click the **Copy to All** icon next to this input.
6.  *Verify:* The Chest note field is instantly filled with `"Warmup 10 mins"`.
7.  Tap **Log Workout** in the bottom sheet.
8.  *Verify:* The bottom sheet dismisses, both cards appear in the today feed, and a confirmation Snackbar appears.

### Test Case 1.2: Undo Logging Action
1.  Directly after logging exercises (Test Case 1.1), tap **Undo** on the confirmation Snackbar.
2.  *Verify:* Both cards are instantly removed from the today list and deleted from the database.

### Test Case 1.3: Edit Notes Post-Log
1.  Tap on the note text of any card in the feed.
2.  An **Edit Notes** dialog appears. Type: `"Set 3: 10 reps @ 70kg"`.
3.  Tap **Save**.
4.  *Verify:* The dialog dismisses and the card notes immediately update to `"Set 3: 10 reps @ 70kg"`.

### Test Case 1.4: Swipe to Delete & Undo
1.  Find any card in the feed list.
2.  Swipe the card to the left.
3.  *Verify:* The card is removed, and a `"Workout deleted"` Snackbar appears with an `"Undo"` button.
4.  Tap **Undo**.
5.  *Verify:* The card is restored to its exact position with its original details.

---

## 🤖 4. Executing Automated Tests
AI agents can run unit tests specifically verifying the home view model flow logic:
```bash
./gradlew testDebugUnitTest --tests "com.ayogeshwaran.workoutlogger.presentation.home.*"
```
