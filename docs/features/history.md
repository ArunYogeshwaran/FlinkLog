# Feature Specification: History Calendar & Agenda Timeline

This document details the functional specifications and verification test cases for the workout
history calendar and agenda timeline feeds.

---

## 🏗️ 1. Technical Reference

* **Key Composables:**
    * `HistoryScreen`
      in [HistoryScreen.kt](../../app/src/main/java/com/ayogeshwaran/workoutlogger/presentation/history/HistoryScreen.kt) (
      Container, TopAppBar with multi-select share, view mode switching)
    * `AgendaTimeline`
      in [AgendaTimeline.kt](../../app/src/main/java/com/ayogeshwaran/workoutlogger/presentation/history/AgendaTimeline.kt) (
      Lazy-loaded chronological timeline with reverse layout)
    * `CalendarGrid` and custom date cells
      in [HistoryScreen.kt](../../app/src/main/java/com/ayogeshwaran/workoutlogger/presentation/history/HistoryScreen.kt)
* **Logical Components:**
    * `HistoryViewModel`
      in [HistoryViewModel.kt](../../app/src/main/java/com/ayogeshwaran/workoutlogger/presentation/history/HistoryViewModel.kt) (
      Paging state, date selection, selection mode state)
    * `GetPagedWorkoutsUseCase`
      in [GetPagedWorkoutsUseCase.kt](../../app/src/main/java/com/ayogeshwaran/workoutlogger/domain/usecase/GetPagedWorkoutsUseCase.kt) (
      Room Paging 3 flow retrieval)
* **Data Layer:**
    * `WorkoutDao.getPagedWorkouts()` ordered by `date DESC, timestamp DESC`

---

## 📋 2. Functional Requirements

1. **View Toggle:** Users can switch between the **Timeline View** (agenda stream) and the **Monthly View** (full calendar grid) via segmented controls at the top.
2. **Agenda Timeline Feed:**
    * **Chat-Style Reverse Layout:** Starts anchored at the most recent workouts at the bottom (`reverseLayout = true`), scrolling down to navigate back through past history.
    * **Lazy Loading via Paging 3:** Loads workout entries incrementally using AndroidX Paging 3 to avoid loading the entire database at once.
    * **Date Grouping:** Workouts are grouped sequentially under date headers based on their normalized calendar day (`workout.date`).
    * **Layout Wrapping:** Date headers and workout cards within a date boundary are grouped in a vertical `Column` container to maintain strict top-to-bottom visual alignment within inverted list items.
3. **Multi-Select & Bulk Sharing:**
    * **Long-Press Activation:** Long-pressing any workout card enters selection mode and toggles selection on that card.
    * **Selection Controls:** Checkboxes appear on cards during selection mode; tapping cards toggles selection.
    * **Contextual TopAppBar:** Displays the selected count, a Close (`X`) button to exit selection mode, and a Share action icon.
4. **Monthly Calendar View:**
    * **Workout Indicators:** Days with saved workouts display a small dot indicator below the day number.
    * **Selected Day:** The currently selected date is highlighted with a primary-colored border ring.
    * **Date Selection:** Tapping any date queries and displays the logged workouts for that day below the calendar.
5. **Inline Modifications:** Logged items support swipe-to-delete, undo via Snackbar, and inline note editing dialogs.
6. **Empty State:** If a day has no logged workouts, `EmptyHistoryIllustration` is displayed.

---

## 🧪 3. Verification Test Suite

### Test Case 2.1: Timeline / Monthly Mode Toggling

1. Navigate to the **History** tab.
2. Observe the segmented buttons at the top: **Timeline** and **Monthly**.
3. *Verify:* Tapping **Timeline** displays the continuous chronological timeline stream.
4. *Verify:* Tapping **Monthly** displays the full monthly calendar grid.

### Test Case 2.2: Timeline Lazy Loading & Date Grouping

1. In Timeline view, observe the bottommost entry.
2. *Verify:* Today's or the most recent workouts appear at the bottom.
3. Scroll upward into older workouts.
4. *Verify:* Older workouts load smoothly via Paging 3.
5. *Verify:* Multiple workouts logged on the same date appear together below their corresponding date header.

### Test Case 2.3: Long-Press Multi-Select & Bulk Sharing

1. In Timeline view, long-press any workout card.
2. *Verify:* Selection mode activates, displaying checkboxes on workout cards and a TopAppBar with the count "1 selected".
3. Tap another workout card from a different date.
4. *Verify:* The count updates to "2 selected" and both checkboxes are checked.
5. Tap the Share icon in the TopAppBar.
6. *Verify:* The share sheet opens with text grouped by date, separated by blank lines.
7. Tap the Close (`X`) icon in the TopAppBar.
8. *Verify:* Selection mode exits and checkboxes disappear.

### Test Case 2.4: Monthly Calendar Navigation & Details

1. Switch to **Monthly** view.
2. Use navigation arrows (`<` and `>`) to move between months.
3. Tap a date that has a workout indicator dot.
4. *Verify:* The list below updates to show workouts logged on that date.
5. Tap a date without a dot.
6. *Verify:* The list displays the moon illustration empty state.

---

## 🤖 4. Executing Automated Tests

AI agents can run unit tests verifying the history data bindings and database calls:

```bash
./gradlew testDebugUnitTest --tests "com.ayogeshwaran.workoutlogger.presentation.history.*"
```
