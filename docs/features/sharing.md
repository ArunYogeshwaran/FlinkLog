# Feature Specification: Workout Sharing

This document details the specifications and verification test cases for the workout sharing
feature.

---

## 🏗️ 1. Technical Reference

* **Share Engine:
  ** [WorkoutComponents.kt](../../app/src/main/java/com/ayogeshwaran/workoutlogger/presentation/components/WorkoutComponents.kt) (
  `shareWorkouts`)
* **UI Components:**
    * [HomeScreen.kt](../../app/src/main/java/com/ayogeshwaran/workoutlogger/presentation/home/HomeScreen.kt) (
      Today's workouts section header)
    * [HistoryScreen.kt](../../app/src/main/java/com/ayogeshwaran/workoutlogger/presentation/history/HistoryScreen.kt) (
      Monthly View selected date header & Timeline View multi-select action in TopAppBar)

---

## 📋 2. Functional Requirements

1. **Contextual Gathering:** Gathers all logged workouts for a specific date (e.g. all activities
   completed on *July 7, 2026*) or multiple selected workouts across distinct dates in the timeline.
2. **Date Grouping & Formatting:**
    * Groups workouts by their normalized date in descending order.
    * Formats an individual date header for each date group (e.g. `📅 Tuesday, Jul 7, 2026`).
    * Inserts a clean blank line separator between different dates.
    * A bulleted list containing the exercise category/type with its respective emoji.
    * Notes/descriptions nested in brackets next to the exercise title.
    * A closing tagline:
      `Log your workouts offline, ad-free, and privately with FlinkLog! Download it here:` followed
      by the Play Store installation link.
3. **Android Share Intent:** Invokes the standard system-level share chooser, allowing users to
   share the formatted text to any external communication app (e.g. WhatsApp, Messages, Slack,
   Email).
4. **Contextual Activation:** 
    * Home/Monthly screens: The share icon is displayed next to date headers when at least one workout is logged for that day.
    * Timeline screen: The share icon is displayed in the TopAppBar when selection mode is active and at least one workout is selected.

---

## 🧪 3. Verification Test Suite

### Test Case 7.1: Today's Workouts Sharing (Home Screen)

1. Select **Cycling** and type `13 km commute` in the notes field.
2. Tap **Log Workout**.
3. *Verify:* A Share icon appears next to the "Logged Workouts Today" header.
4. Tap the **Share** button.
5. *Verify:* Android's system share sheet opens.
6. Choose to copy to clipboard or share to a text app.
7. *Verify:* The shared text reads:
   ```text
   📅 <Today's Date>
   • 🚴 Cycling (13 km commute)

   Log your workouts offline, ad-free, and privately with FlinkLog! Download it here:
   https://play.google.com/store/apps/details?id=com.ayogeshwaran.workoutlogger
   ```

### Test Case 7.2: Past Day Sharing (Monthly Calendar)

1. Navigate to the **History** tab.
2. Select a date on the monthly calendar that contains logged workouts.
3. *Verify:* A Share icon appears next to the selected date header text.
4. Tap the **Share** button.
5. *Verify:* The correct list of workouts for that day is gathered and formatted with their
   respective emojis and a date header on the first line.

### Test Case 7.3: Multi-Select Bulk Sharing (Timeline View)

1. Navigate to the **History** tab and switch to **Timeline** view.
2. Long-press a workout card from Date A to activate selection mode.
3. Tap another workout card from Date B to select it as well.
4. Tap the Share icon in the TopAppBar.
5. *Verify:* The shared text groups workouts under their respective dates with line breaks:
   ```text
   📅 <Date A>
   • 🏃 Running

   📅 <Date B>
   • 💪 Bench Press (3 sets)

   Log your workouts offline, ad-free, and privately with FlinkLog! Download it here:
   https://play.google.com/store/apps/details?id=com.ayogeshwaran.workoutlogger
   ```
