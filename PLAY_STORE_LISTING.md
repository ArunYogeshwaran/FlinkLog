# FlinkLog — Play Store Listing

---

## App Name (30 chars max)

FlinkLog: Tiny Workout Logger (<3MB)

---

## Short Description (80 chars max)

Tiny <3MB workout log. Simple, fast, offline, no cloud, no ads, no tracking.

---

## Full Description

Everyone deserves simple, lightweight tools.

I built this app because I wanted to log my workout and move on with my day. No sign-ups. No subscriptions. No nonsense. Then I thought — maybe others want this too.

◉ Open the app.
◉ Tap your workout type.
◉ Hit "Log Workout."
◉ Done.

Want to see what you did last Tuesday? Open the calendar. Tap the date. There it is.

What this app does NOT do:

✗ No accounts or sign-ups
✗ No internet connection needed — ever
✗ No ads, in-app purchases, or subscriptions
✗ No data collection of any kind
✗ No remote AI processing or cloud-based data selling

Your data stays on your device. Period.

Features:

• Ultra-lightweight (under 3MB) — installs instantly, negligible battery use
• Log workouts with one tap — Cardio, Gym, or any preset type
• Pick a date and time, or just log "right now"
• Swipe to delete, with undo
• Monthly calendar view and rolling 7-day weekly view
• 100% open-source & offline — code is auditable on GitHub
• Worry-free device switching — securely backed up to your Google account
• Android 16 AppFunctions integration — let your on-device assistant log workouts and query history locally
• Material You theming with light and dark mode

This app does one thing and does it well.
If you're tired of bloated apps — this is for you.

---

## Google Play Console Pre-Launch & Store Asset Checklist

### 1. Visual Store Assets Specs & Checklist
Ensure you prepare the following graphics before publishing:
* **App Icon:**
  * Size: 512 x 512 pixels
  * Format: 32-bit PNG (with alpha channel)
  * File Size Limit: 1 MB
  * Design: Simple and recognizable, matching the Material You aesthetic.
* **Feature Graphic:**
  * Size: 1024 x 500 pixels
  * Format: PNG or JPEG
  * File Size Limit: 1 MB
  * Design: Placed on a vibrant background. Keep text minimal (app name only). Ensure key visual elements are near the center (avoiding the outer 15% borders, as they can get cropped).
* **Screenshots (Phone):**
  * Quantity: Minimum of 2, maximum of 8.
  * Size: Aspect ratio 16:9 or 9:16. Each side must be between 320 px and 3840 px.
  * Captions: Add short, high-contrast text overlays (e.g., "100% Offline", "Logged in 2 Seconds", "Android 16 Smart Suggestions").
* **Screenshots (Tablet - 7" & 10"):**
  * Quantity: Optional but recommended. Minimum of 2, maximum of 8 for each.
  * Size: 16:9 or 9:16 aspect ratio.

### 2. Play Store Policies & App Content Declarations
You must declare these answers inside the Play Console's **App Content** page:
* **Privacy Policy:**
  * Link to the hosted privacy policy (must point to a live URL, e.g. a GitHub Pages link hosting the contents of `PRIVACY_POLICY.md`).
* **Data Safety Questionnaire:**
  * **Does your app collect or share user data?** Select **"No"**.
  * Explanation: Since FlinkLog is 100% offline, requires no account, and doesn't transmit telemetry/analytics, it collects 0 user data. (Google Auto Backup is considered system-level backup and is exempt from data collection disclosure, but you can mention it in your public privacy policy).
* **Target Audience and Content:**
  * Age Group: Select **13 and older** or **Everyone (3+)**. Since it collects 0 data and is ad-free, it is highly compliant.
  * **Does your app intentionally appeal to children?** Select **"No"** (to avoid strict COPPA-related store review processes).
* **Ads Declaration:**
  * Select **"No, my app does not contain ads"**.
* **Financial Features:**
  * Select **"My app doesn't provide any financial features"** (since there are no in-app purchases, subscriptions, or wallet integrations).
* **Government Status:**
  * Select **"No, this app is not owned or run by a government entity"**.
* **Account Deletion:**
  * Select **"No, this app does not allow account creation"**.

### 3. Release Verification & Technical Checklist
* **Google Play App Signing:**
  * Select "Let Google manage and protect your app signing key" (recommended).
* **Key Generation & Keystore:**
  * Generate a Release keystore (`.jks` file) to sign your upload bundle. Keep this key safe and backed up.
* **Verify Versioning (`app/build.gradle.kts`):**
  * Make sure `versionCode` is incremented for subsequent updates.
* **Obfuscation & Shrinking Verification:**
  * Ensure R8 optimization is tested on a local release build (`./gradlew assembleRelease`).
  * Verify that `app/proguard-rules.pro` contains the custom keep rules for Android 16 AppFunctions to prevent runtime class resolution failures.

