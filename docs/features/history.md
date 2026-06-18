# Feature Specification: History Calendar

This document details the functional specifications and verification test cases for the workout history calendar and timeline feeds.

---

## 🏗️ 1. Technical Reference
*   **Key Composables:**
    *   `HistoryScreen` in [HistoryScreen.kt](../../app/src/main/java/com/ayogeshwaran/workoutlogger/presentation/history/HistoryScreen.kt) (Calendar views and lists)
    *   `CalendarGrid` and custom date cells in [HistoryScreen.kt](../../app/src/main/java/com/ayogeshwaran/workoutlogger/presentation/history/HistoryScreen.kt)
*   **Logical Components:**
    *   `HistoryViewModel` in [HistoryViewModel.kt](../../app/src/main/java/com/ayogeshwaran/workoutlogger/presentation/history/HistoryViewModel.kt) (State retrieval)
*   **JVM Unit Tests:**
    *   Verification runs cover calendar date calculation and selection ranges.

---

## 📋 2. Functional Requirements
1.  **Grid Toggle:** Users can switch between a full **Monthly View** grid and a collapsed **Weekly View** row. The weekly view shows a rolling 7-day strip based on the selected date.
2.  **Date Indicators:**
    *   **Workout Day:** Days with saved workouts display a small dot indicator below the day number.
    *   **Selected Day:** The currently selected date is highlighted with a primary-colored border ring.
3.  **Feed Details:** Tapping any date in the calendar queries and displays the logged workouts for that day directly below the calendar in chronological order.
4.  **Inline Modifications:** Logged items displayed below the calendar support swipe-to-delete, logging undo, and inline note editing dialogs.
5.  **Empty State:** If the selected day has no logged workouts, a custom programmatic Canvas vector illustration (`EmptyHistoryIllustration` representing a crescent moon and stars) is displayed.

---

## 🧪 3. Verification Test Suite

### Test Case 2.1: Calendar dot rendering
1.  Go to the Home ("Today") tab and log a workout. Note today's date.
2.  Navigate to the **History** tab.
3.  *Verify:* Today's calendar cell displays a workout indicator dot, and is highlighted with a blue border ring.
4.  Verify the list below the calendar shows today's logged workout details.

### Test Case 2.2: Weekly / Monthly Collapse Toggling
1.  On the History tab, locate the **Weekly/Monthly** segmented controls at the top.
2.  Tap the **Weekly** toggle.
3.  *Verify:* The monthly grid collapses into a single row of 7 days containing the selected date.
4.  Tap the **Monthly** toggle.
5.  *Verify:* The view expands back to the full month grid.

### Test Case 2.3: Date Navigation & Log Retrieval
1.  Use the calendar navigation arrows (`<` and `>`) to move to a previous month.
2.  Tap a date that has a workout indicator dot.
3.  *Verify:* The list below updates to show workouts logged on that date.
4.  Tap a date without a dot.
5.  *Verify:* The list displays the moon illustration empty state.

---

## 🤖 4. Executing Automated Tests
AI agents can run unit tests verifying the history data bindings and database calls:
```bash
./gradlew testDebugUnitTest --tests "com.ayogeshwaran.workoutlogger.presentation.history.*"
```
