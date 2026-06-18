# Changelog

All notable changes to the FlinkLog project are documented in this file. The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-06-18

### Added
*   **Unique Branding & Name:** Renamed the app from the generic "Workout Logger" to **FlinkLog** (inspired by the German word *flink*, meaning quick, nimble, or agile).
*   **Play Store Listing Assets:** Configured the 30-character App Title (`FlinkLog: Tiny Workout (<3MB)`) and the 80-character Short Description (`Tiny <3MB workout log. Simple, fast, offline, no cloud, no ads, no tracking.`).
*   **Android 16 AppFunctions Integration:** Exposed 4 key app capabilities (`logWorkout`, `getWorkoutsForRange`, `getCustomWorkoutTypes`, and `suggestWorkout`) locally on-device for assistant integrations (like Google Gemini).
*   **Google Auto Backup:** Configured robust system backup policies (including SQLite database journals, WAL, and SHM files) for seamless, encrypted data transfer when switching devices.
*   **Custom Workout Categories:** Added capability to create new permanent exercises dynamically via the `+` chip and delete them via the `x` chip on category lists.
*   **Multi-Activity Logging:** Enabled selecting multiple exercises at once, entering custom notes per activity in a bottom sheet, and copying notes to all selection rows instantly using "Copy to All".
*   **History Screen:** Introduced monthly calendar view toggles with dot indicators for active days and a collapsible 7-day rolling weekly view.
*   **Inline Editing & Swipe-to-Delete:** Added capability to edit notes post-logging and delete entries with a swipe-to-left gesture, accompanied by an instantaneous "Undo" action.
*   **Swipe Tutorial Onboarding:** Added a dismissible guidance card for first-time users explaining how to swipe to delete.
*   **Material You Theme support:** Fully dynamic light and dark theme adaptation matching system colors.
*   **Programmatic Brand Visuals:** Replaced heavy raster assets with clean, lightweight Canvas-drawn graphics:
    *   `EmptyHomeIllustration` (Kettlebell)
    *   `EmptyHistoryIllustration` (Calendar moon & stars)
    *   `FlinkLogBrandLogo` (Refined non-overlapping dumbbell and log checklist on the About page)

### Changed
*   **Privacy & Support Email Obfuscation:** Replaced the developer email in documentation and strings with an obfuscated layout (`h.arunbuilds+workoutlogger [at] gmail [dot] com`) to prevent spam scraping.
*   **Unified UI Design:** Unified all three action cards on the About screen (Share, Rate, and Feedback) to use the primary container color theme for visual cohesion.
*   **Adaptive App Icon Wrapper:** Switched the launcher logo rendering on the About screen from Compose's `Image` to an `AndroidView`/`ImageView` wrapper to natively support adaptive icon layouts.

### Fixed
*   **Compose LocalContext Lint Errors:** Fixed all 10 `LocalContextGetResourceValueCall` and `LocalContextResourcesRead` errors by switching to Compose's configuration-aware `LocalResources.current` API for async and callback-based resource fetches.
*   **String Resource Percent Parsing Crash:** Fixed a formatting warning/potential crash by escaping raw `%` characters to `%%` in `strings.xml`.
*   **Clean Git History:** Rewrote local and remote Git commit history to clean up raw personal emails in previous commits, updating the author email mapping to the GitHub no-reply address.
