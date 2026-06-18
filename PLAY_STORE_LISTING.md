# FlinkLog — Play Store Listing

---

## App Name (30 chars max)

FlinkLog: Tiny Workout (<3MB)

---

## Short Description (80 chars max)

Tiny <3MB workout log. Simple, fast, offline, no cloud, no ads, no tracking.

---

## Full Description

Everyone deserves simple, lightweight tools.

We live in an era where every app wants your email, your location, your attention, and your money — bloated with megabytes of trackers and animations you don't need. 

FlinkLog is different. At **under 3MB**, it is one of the smallest and fastest fitness apps on the Play Store.

—

I built this app for myself.
I wanted to log my workout and move on with my day.
No sign-ups. No subscriptions. No analytics. No cloud. No nonsense.
Then I thought — maybe others want this too.

—

Here's what the app does:

◉ Open the app.
◉ Tap your workout type.
◉ Hit "Log Workout."
◉ Done.

That's the whole app.

—

Want to see what you did last Tuesday?
Open the calendar. Tap the date. There it is.

Want to see what you did three months ago?
Same thing. Scroll back. Tap. Done.

—

What this app does NOT do:

✗ No heavy storage use — under 3MB download size
✗ No accounts or sign-ups
✗ No internet connection needed — ever
✗ No ads
✗ No in-app purchases
✗ No data collection of any kind
✗ No social features
✗ No remote AI processing or cloud-based data selling
✗ No subscription walls

Your data stays on your device. Period. (With secure, automated cloud backup to your Google account for seamless device upgrades).

—

Features — the short list:

• **Ultra-Lightweight (under 3MB)** — highly optimized, installs instantly and uses negligible battery
• **Log workouts with one tap** — Cardio, Gym, or any preset type
• **Pick a date and time**, or just log "right now"
• **Swipe to delete**, with undo
• **Toggle between Monthly Calendar view** and rolling 7-day Weekly view
• **Grouped history feed** sorting workouts chronologically by date
• **100% Open-Source & Offline** — code is fully auditable on GitHub; zero internet access required
• **Worry-free device switching** — securely backed up to your Google account (encrypted, zero quota cost) to restore your history automatically on your next phone
• **Android 16 AppFunctions integration** — let your secure on-device assistant (like Gemini) log workouts, query history, or suggest today's workouts locally and privately
• **Material You theming** — adapts to your wallpaper colors
• **Light and dark mode** — follows your system setting

That's it. No feature bloat. No settings maze. No onboarding screens.

—

This app is intentionally small.
It does one thing and does it well.

If you're tired of bloated apps that do everything except the one thing you need — this is for you.

—

Have a feature request?
I'm listening. If it keeps things simple and the size low, I'm happy to build it.

—

Built with care. Kept simple and tiny on purpose.

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

