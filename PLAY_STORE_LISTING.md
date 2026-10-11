# FlinkLog — Play Store Listing

---

## App Name (30 chars max)

FlinkLog: Tiny Workout Logger

---

## Short Description (80 chars max)

Tiny, lightweight workout log. Simple, fast, offline, no ads, no tracking.

---

## Full Description

FlinkLog is a privacy-first, ultra-lightweight workout logger. No sign-ups, no subscriptions, and no tracking.

◉ Log in one tap: Select workout type, hit "Log Workout," and move on.
◉ Quick History: View your logging history anytime via the monthly calendar or continuous timeline.

Features:
• Under 3.5MB download size — fast, lightweight, and efficient
• Smart suggestions — auto-fill note descriptions from past entries
• Offline-first — 100% offline, code is fully open-source
• Auto Cloud Backups — securely restore your data via Google Auto Backup
• Material You — dynamic themes matching your device colors
• AppFunctions — Android 16 on-device system assistant hooks

What it does NOT do:
✗ No ads, account creation, or subscriptions
✗ No internet connection required
✗ No data collection or selling of any kind

---

## What's New (500 chars max)

• Continuous Agenda Timeline: Browse your complete workout journey in a seamless, smooth-scrolling timeline feed.
• Bulk Workout Sharing: Long-press any workout card to select multiple workouts across different days and share them grouped by date with workout partners or trainers.
• Polished Navigation: Seamlessly toggle between your month overview calendar and continuous chronological timeline.
• Stability & Performance: Streamlined database loading for faster app start and responsive scrolling.

---

## Subsequent Release Checklist

Ensure the following before publishing any update:

1. **Increment Version Code:** Increase `versionCode` in `app/build.gradle.kts` for each release.
2. **Verify AppFunctions & Obfuscation:** Run `./gradlew assembleRelease` to confirm R8 optimizations don't break AppFunctions reflection.
3. **Data Safety Declaration:** Ensure the Data Safety page in Play Console remains declared as collecting **"No data"**.
