# Workout Logger — Product Requirements Document

**Version:** 1.0  
**Date:** June 12, 2026  
**Status:** Active  

---

## 1. Overview

Workout Logger is a lightweight, offline-first Android application designed to help users log their
daily fitness activities effortlessly. The app focuses on speed, simplicity, and privacy—requiring 
no accounts, no cloud sync, and no internet connection to operate.

---

## 2. Problem Statement

People want to stay consistent with their fitness routine but often lose track of what they did,
when they did it, or how consistent they've been over weeks and months. Most fitness apps on the
market are overloaded with features — social feeds, meal plans, subscription paywalls — when all the
user really wants is a quick way to say "I worked out today" and check what they did last Tuesday or
a year ago. There is a clear need for a minimal, fast, offline-first workout logger that does one
thing and does it well.

---

## 3. Target Users

- Fitness enthusiasts who want a frictionless way to track their daily workouts.
- Users who value data privacy and prefer an app that runs entirely locally on their device.
- People looking for a minimal, ad-free, and distraction-free workout tracking tool.

---

## 4. Product Goals

- **Frictionless Logging:** Log daily workouts in under 5 seconds (select activities, add optional notes, and save).
- **Simple History Retrieval:** Allow users to quickly look back at any date in the past to see what they did.
- **Full Privacy & Offline Access:** Operate 100% offline, keeping all personal data on the user's device.
- **Focus on Simplicity:** Keep the interface clean and restricted to core workout logging, free of bloat or social elements.

---

## 5. Core Features & User Experience

The application is structured into two main views via a simple bottom navigation bar, with a secondary info screen.

### 5.1 Home ("Today") Screen
- **Automatic Date & Time:** All logged workouts default to the current date/time. Users can optionally customize the logging date and time using picker buttons inside the logging bottom sheet.
- **Activity Selector & Custom Workouts:** Preset exercises categorized under Cardio & General or Gym / Muscle Groups. Users can also add their own custom, permanent categories dynamically via a "+" button, and delete custom workout categories by tapping a clear/delete icon on the category chip. Custom workouts persist in the local database.
- **Multi-Activity Logging:** Users can select multiple activities at once.
- **Workout Logging Bottom Sheet:** Once the user selects all the workouts and taps "Log Workout", a bottom sheet is displayed listing all the selected workouts. The user can add notes/details to each selected workout directly in this bottom sheet.
- **Remove Selected Workouts:** In the bottom sheet, the user can remove a workout from their selection using a delete button next to each workout.
- **Final Log Confirmation:** Clicking the "Log Workout" button inside the bottom sheet saves all selected workouts and notes, clears all inputs, dismisses the bottom sheet, and displays a confirmation notification with an "Undo" option.
- **Daily Workout List:** A list at the bottom shows all logged workouts for today.
  - **Edit Notes:** Tapping on a card's note text or edit icon opens a dialog to update the notes post-logging.
  - **Swipe to Delete:** Users can swipe any logged card to the left to delete it, with a confirmation notification and "Undo" option.
  - **Swipe Tutorial:** First-time users see a dismissible tip teaching them how to swipe to delete.

### 5.2 History Screen
- **Monthly Calendar:** Displays a calendar grid representing the selected month.
- **Activity Indicators:** Dates with logged workouts show a dot indicator.
- **Date Details:** Tapping any date displays the logged workouts for that day below the calendar.
- **Inline Actions:** Users can edit workout notes or swipe to delete workouts directly from this list.
- **Navigation:** Navigation controls allow moving back and forth between months.

### 5.3 About & Feedback
- A secondary screen accessible via an info button on the Home screen.
- Displays app information, developer details, and rating links.
- **Feedback Email:** Tapping the feedback button opens the user's email client with pre-filled developer email and subject line.
- **Sharing Feature:** Tapping the "Share App" button triggers a standard Android share intent containing a description of the app and a Play Store link.
- **Licenses:** Lists all open-source libraries used.

### 5.4 Data Backup & Phone Recovery
- **Google Auto Backup**: The application fully supports Android's Auto Backup system. All local Room database files (including SQLite journals, WAL, and SHM files) are backed up securely to the user's private Google Drive storage associated with their Android Google Account.
- **Quota & Encryption**: Backups are encrypted end-to-end and do not count against the user's personal Google Drive storage quota.
- **Frictionless Recovery**: When a user changes phones or reinstalls the app on a new device, Android automatically restores their entire workout history, custom exercises, and settings during the initial setup.

---

## 6. Key User Flows

### 6.1 Log a Workout
1. Open the app to the Home screen.
2. Select one or more activity chips (e.g., "Running" and "Chest").
3. Tap "Log Workout" to open the workout details bottom sheet.
4. (Optional) Enter custom notes for each selected workout or remove any accidental selections.
5. Tap "Log Workout" inside the bottom sheet.
6. The workouts are saved, the bottom sheet is dismissed, and a confirmation with "Undo" appears.

### 6.2 View and Edit Past Logged Workouts
1. Navigate to the "History" tab.
2. Tap a date marked with a workout dot.
3. Tap on the note text or edit icon of a workout in the list.
4. Modify the notes in the dialog and save.

### 6.3 Delete a Workout
1. On either the Home or History screen, find the workout in the list.
2. Swipe the card to the left.
3. The workout is removed, and an "Undo" confirmation appears.

---

## 7. Future Roadmap

- **Structured Inputs:** Support logging structured sets, reps, weights, or distance instead of relying solely on free-form text.
- **Custom Activities Management:** Allow users to rename, delete, or re-organize custom workout categories and activities.
- **Statistics & Streaks:** Introduce a dashboard showing monthly workout trends, consistency streaks, and total workouts.
- **Data Portability:** Enable exporting and importing workout history via CSV/JSON files.
- **Reminders:** Optional local daily notifications to encourage logging consistency.

---

## 8. Acceptance Criteria

- [x] User can log one or multiple workouts for today or any custom date/time.
- [x] User can write and edit free-form notes for each logged activity.
- [x] All logged activities appear in chronological lists on the corresponding date.
- [x] User can delete any workout with a swipe-to-left gesture and undo the deletion.
- [x] History screen provides a calendar with dots indicating workout days.
- [x] The app works fully offline and preserves data across app restarts.
- [x] The UI automatically supports system-wide light and dark mode preferences.
- [x] Users can send pre-addressed feedback emails and access open-source licenses.
- [x] First-time users are presented with a dismissible swipe-to-delete tutorial.
- [x] All database transaction files (including WAL/SHM logs) are correctly configured for Android Auto Backup to ensure clean data recovery on phone transition without corruption.

---

## 9. Product Changelog

* **Version 1.0:** Initial launch featuring:
  - Daily workout logging with multi-activity selection, custom date/time logging support, and workout details/notes.
  - Custom workout category creation (persisted in local DB) and category chip deletion.
  - History screen supporting toggling between Monthly Calendar view and rolling 7-day Weekly view, and a grouped chronological workout feed.
  - Post-logging note edits and swipe-to-delete with undo (plus a dismissible onboarding tutorial).
  - Lightweight programmatic vector illustrations for empty screens (Home and History).
  - Configured secure, robust Google Auto Backup rules to ensure complete and corruption-free workout history recovery when transitioning devices.
  - About and Feedback screens, including a system-level app sharing action.
  - Cohesive Gemini-inspired design system with automatic system-wide dark/light theme support.

