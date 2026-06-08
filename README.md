## 📊 Current Project Status: Beta 4 (v0.9.50)

URL Checker is currently in its **Beta 4** phase. 

This build enforces a strict **Minimum SDK of 31 (Android 12+)**, allowing us to drop bloated legacy compatibility libraries. The current architecture features:
* Jetpack Compose UI with native `window.setBackgroundBlurRadius()` liquid glassmorphism.
* Infinite-Loop intent resolution (explicitly bypassing native Android browser loops).
* Hyper-optimized battery management via `Expedited WorkManager`.
* Bundled Baseline Profiles for instant UI rendering.
* Aggressive R8 code shrinking for an ultra-lightweight footprint.

*We are currently stabilizing this build in preparation for our official 1.0 Stable Release.*
