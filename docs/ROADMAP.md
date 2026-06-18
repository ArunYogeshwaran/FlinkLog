# 🗺️ FlinkLog — Future Features & Engineering Roadmap

This document outlines planned features, user-experience enhancements, and technical milestones for **FlinkLog**. Any future developer or AI agent should consult this roadmap before starting major feature development to ensure alignment with our design principles.

---

## 🎯 Core Engineering Constraints
Any new feature implementation **MUST** respect the following guidelines:
1.  **Lightweight Footprint (<3MB):** Do not add external libraries that bloat the APK size (e.g., avoid large chart or network libraries). Prefer custom, lightweight Compose `Canvas` implementations.
2.  **Privacy-First & Offline-First:** No remote databases, user accounts, analytics tracking, or cloud APIs. All data must reside strictly in the local SQLite database.
3.  **Encrypted Sync via OS:** Rely strictly on **Google Auto Backup** for cloud transfers/backups.

---

## 📋 1. Roadmap Tracks & Tasks

### Track A: UI & Quick-Logging Enhancements
*   **[ ] A1: Routine Templates (Quick-Log)**
    *   *Description:* Allow users to define group templates (e.g., "Leg Day", "Push Day") and log them simultaneously with one tap instead of selecting individual exercises.
    *   *Implementation Tip:* Add a new `WorkoutTemplate` entity in Room and implement a custom multi-select selector on the log board.
*   **[ ] A2: Structured Sets/Reps Inputs**
    *   *Description:* Add a structured input for sets, reps, and weights (e.g., `3 sets x 10 reps @ 60kg`) inside the logging sheet, supplementing the existing free-form notes.
    *   *Implementation Tip:* Leverage simple regex or structured DB columns in `WorkoutEntry`.

### Track B: Insights & Canvas Analytics
*   **[ ] B1: Canvas-Drawn Progression Charts**
    *   *Description:* Build lightweight progression graphs (e.g., weight over time or workout frequency charts) drawn entirely on Jetpack Compose `Canvas`.
    *   *Implementation Tip:* Do NOT pull in MPAndroidChart. Draw custom coordinate axes, grids, and paths programmatically.
*   **[ ] B2: Session Streaks & Targets**
    *   *Description:* Add a weekly target tracker (e.g., "Goal: 3 workouts/week") and calculate user streaks fully on-device.

### Track C: Data Freedom & Portability
*   **[ ] C1: CSV / JSON Backup Export & Import**
    *   *Description:* Since there are no cloud accounts, provide a button to manually export the database as a CSV or JSON file to the user's Local Storage, and allow restoring from it.
    *   *Implementation Tip:* Use Android Document Provider (`Intent.ACTION_CREATE_DOCUMENT`) for storage framework compliance.

### Track D: System AI & AppFunctions Extensions
*   **[ ] D1: Expose Statistics to Google Assistant**
    *   *Description:* Add new `@AppFunction` entry points enabling assistant agents to query summary statistics.
    *   *Examples:* `getWorkoutCountForRange(startTime: Long, endTime: Long): Int` or `getMostFrequentWorkout(daysAgo: Int): String`.
*   **[ ] D2: Voice Shortcuts Integration**
    *   *Description:* Hook AppFunctions into Google Assistant routines so users can say: *"Hey Google, start my Chest Day template on FlinkLog"*.

---

## 🛠️ 2. Verification Protocol for Roadmap Tasks
Before mark-completing any task from this roadmap:
1.  **Create a Spec File:** Draft a specification file in `docs/features/` mapping the new functionality and automated verify steps.
2.  **Ensure Zero Bloat:** Check the release build APK size to guarantee the app remains under 3MB:
    ```bash
    ./gradlew assembleRelease
    ```
3.  **Run Quality Suite:**
    ```bash
    ./gradlew compileDebugKotlin lintDebug testDebugUnitTest
    ```
