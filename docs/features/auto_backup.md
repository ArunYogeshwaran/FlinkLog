# Feature Specification: Google Auto Backup

This document details the specifications and verification test cases for Android Auto Backup and device recovery support.

---

## 🏗️ 1. Technical Reference
*   **Configuration XMLs:**
    *   [backup_rules.xml](../../app/src/main/res/xml/backup_rules.xml) (Defines file inclusion/exclusion)
    *   [data_extraction_rules.xml](../../app/src/main/res/xml/data_extraction_rules.xml) (Defines Android 12+ cloud extraction filters)
*   **Database Manifests:**
    *   [AndroidManifest.xml](../../app/src/main/AndroidManifest.xml) (Enables rules and configures permissions)

---

## 📋 2. Functional Requirements
1.  **Scope of Backup:** The backup system must copy all local SQLite Room database streams, including:
    *   Primary database file (`workout_database`)
    *   Write-Ahead Log database helper file (`workout_database-wal`)
    *   Shared memory index mapping file (`workout_database-shm`)
2.  **Encryption:** Backups are sent to the private Google Drive account of the user's Android session. Backups are encrypted end-to-end and exempt from storage quotas.
3.  **Frictionless Restore:** Upon reinstalling the app on the same device or switching to a new Android phone under the same Google account, all user data (history, logs, categories) must restore automatically before the first launch.

---

## 🧪 3. Verification Test Suite

### Test Case 4.1: Manual ADB backup & Restore (On-Device simulation)
1.  Log at least 3 workouts on the Home screen and add a custom exercise category.
2.  Connect your phone/emulator with USB debugging active.
3.  Force immediate backup via terminal:
    ```bash
    adb shell bmgr backupnow com.ayogeshwaran.workoutlogger
    ```
4.  *Verify:* Terminal returns: `Package com.ayogeshwaran.workoutlogger backup success`.
5.  Uninstall the application:
    ```bash
    adb uninstall com.ayogeshwaran.workoutlogger
    ```
6.  Reinstall the application from scratch.
7.  Launch the app and open the Home and History tabs.
8.  *Verify:* All previously logged workouts and your custom category are present.
9.  Run a query on the Room DB to verify there is no corruption in WAL/SHM streams.

---

## 🤖 4. Verification Guidelines for AI
Ensure that database definitions do not bypass Auto Backup rules. The database helper files must be saved under the default app storage directories:
`/data/data/com.ayogeshwaran.workoutlogger/databases/`
