# TVLink 📺

> Wireless remote control, app manager, and ADB terminal for Android TV.

[![Build Status](https://img.shields.io/badge/Build-Passing-00C853.svg)](https://github.com/FILIP2106Z/AdbTv-/actions)
[![Platform](https://img.shields.io/badge/Platform-Android_TV_%26_Mobile-4285F4.svg?logo=android)](https://developer.android.com)
[![Obtainium](https://img.shields.io/badge/Obtainium-Compatible-8E24AA.svg)](https://github.com/ImranR98/Obtainium)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

**TVLink** connects your Android phone directly to your Android TV or Google TV over local Wi-Fi. No computers, no root, and no USB cables required.

---

## 📲 Install via Obtainium (Recommended)

1. Open **[Obtainium](https://github.com/ImranR98/Obtainium)** on your phone or TV.
2. Tap **Add App** and paste:
   ```text
   https://github.com/FILIP2106Z/AdbTv-
   ```
3. Tap **Add** — Obtainium will automatically download `TVLink-release.apk` and notify you when new updates are released!

Or download directly from [GitHub Releases](https://github.com/FILIP2106Z/AdbTv-/releases/latest).

---

## ✨ Features

- **🎮 Virtual TV Remote**: Floating D-pad, Home, Back, Menu, Volume, Power, and phone keyboard text injection.
- **🚀 1-Tap APK Sideload**: Install downloaded APKs from your phone or directly from GitHub links (with presets for SmartTube, CleanYourTV, and Nova Player).
- **🛡️ Debloat & Disable Apps**: Safely disable pre-installed TV bloatware and telemetry without root, and re-enable anytime.
- **⚡ TV Speed Booster**: Make sluggish Android TVs 2x faster by tuning animation scales to 0.5x or OFF.
- **⚙️ Hidden TV Settings**: Change screen resolution, display density (DPI scaler), and enable system-wide ad-blocking via Encrypted Private DNS (AdGuard / Cloudflare).
- **💻 ADB Terminal**: Interactive shell with live TV diagnostic presets (Active window, power state, package list, memory usage).
- **🔍 Auto Discovery**: Detects Android TVs automatically on your Wi-Fi network via mDNS and subnet scans.

---

## 📺 Quick TV Setup (1 Minute)

1. **Connect to Same Wi-Fi**: Make sure your phone and TV are on the same Wi-Fi network.
2. **Enable Wireless Debugging on TV**:
   - On TV: **Settings** → **About** → Click **Build** 7 times to unlock Developer options.
   - Go to **Developer options** → Turn on **USB debugging** (and **Wireless debugging** if available).
3. **Connect**:
   - Open TVLink on your phone and tap your TV in the list (or enter its IP address).
   - Check your TV screen and select **"Always allow from this computer"** → **OK**.

---

## 🛠️ Building from Source

```bash
git clone https://github.com/FILIP2106Z/AdbTv-.git
cd AdbTv-
./gradlew assembleRelease
```
The APK will be generated at `app/build/outputs/apk/release/app-release-unsigned.apk`.

---

## 👏 Credits

- Pure Kotlin ADB engine powered by **[dadb](https://github.com/mobile-dev-inc/dadb)** by Mobile Dev Inc.
- Debloating concepts inspired by **[Clean Your TV](https://github.com/udhaykumarbala/cleanyourtv-app)** by Udhay Kumar Bala.
- Material 3 design system by Google.
