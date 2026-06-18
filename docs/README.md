# FlinkLog — Feature Specs & Test Documentation Index

Welcome to the FlinkLog documentation system. This repository uses a **modular, feature-based documentation strategy** designed to be highly scalable and token-efficient for AI agents and human developers.

---

## ⚡ Token-Efficient Documentation Strategy
Instead of keeping a single massive requirements or test suite document (which increases context token costs for AI agents), FlinkLog divides features into individual, granular markdown files in the `docs/features/` folder.

**Rule for future agents:** When modifying or reviewing a specific feature, **only read the corresponding feature file** listed below. Do not read the entire documentation directory unless explicitly requested.

---

## 📂 Feature Documentation Index

| Feature File | Target Capability | Verification Focus |
| :--- | :--- | :--- |
| [ARCHITECTURE.md](../docs/ARCHITECTURE.md) | MVVM Architecture, packages, and data flows | Gradle test suites execution and compiler verify |
| [1. Logging & Feed](../docs/features/logging.md) | Selecting exercises, notes entry, "Copy to All", swipe onboarding | Logging validation, undo actions, note edits, gestures |
| [2. History Calendar](../docs/features/history.md) | Month grid navigation, active days dot, weekly layout toggles | Calendar active states, scroll loads, and layout resizing |
| [3. Custom Exercises](../docs/features/custom_exercises.md) | Dynamic addition (+) and deletion (x) of categories | Persisting category lifecycle, duplicates & empty string handling |
| [4. Google Auto Backup](../docs/features/auto_backup.md) | SQLite DB, WAL/SHM streams configuration | End-to-end data restore on device transitions |
| [5. On-Device AI (AppFunctions)](../docs/features/appfunctions.md) | Local assistant hooks, log/query APIs, routines recommendations | ADB commands execution, on-device unit tests |

---

## 🔄 Lifecycle Workflow for Code Modifications
Whenever you are tasked with adding a feature or fixing a bug in FlinkLog:
1.  **Read the Feature File:** Open the specific markdown spec for the feature (e.g. `docs/features/logging.md`).
2.  **Make Code Changes:** Implement the code in the respective presentation, domain, or data package.
3.  **Run Tests:** Execute the corresponding JVM unit tests or automated scripts documented in that feature file.
4.  **Update Documentation:** If you altered requirements or layout properties, update **only** that feature file's specifications and test scenarios to match.
5.  **Run Quality Suite:** Run `./gradlew compileDebugKotlin lintDebug testDebugUnitTest` to ensure no regressions.
