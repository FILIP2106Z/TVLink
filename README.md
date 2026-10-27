# AdbTV Remote — Wireless ADB Terminal & Controller for Android TV

[![Build Android APKs](https://github.com/your-username/adbtv-remote/actions/workflows/build-apk.yml/badge.svg)](https://github.com/your-username/adbtv-remote/actions/workflows/build-apk.yml)
[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84.svg?logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![dadb](https://img.shields.io/badge/ADB-dadb%20(Pure%20Kotlin)-orange.svg)](https://github.com/mobile-dev-inc/dadb)

**AdbTV Remote** is a native Android application built with Kotlin and Jetpack Compose that acts as an interactive Remote ADB (Android Debug Bridge) terminal, remote control, and package manager tailored specifically for **Android TV** and **Google TV** devices over Wireless ADB (TCP/IP).

Unlike traditional tools, **AdbTV Remote does NOT require desktop adb binaries, terminal scripts, root access, or an external computer**. It establishes direct, encrypted ADB TCP connections directly from your Android phone or tablet to your Android TV using the pure Kotlin `dadb` backend.

---

## 🌟 Key Features

### 1. ⚡ Pure Kotlin Wireless ADB Backend
- Connects directly to Android TV devices over TCP/IP (default port `5555`).
- Automatically generates and securely stores RSA ADB authentication keypairs (`adbkey` / `adbkey.pub`) in internal app storage.
- Handles authorization handshakes, connection timeouts, latency metrics, and broken pipe recovery.

### 2. 🔍 Automatic TV Discovery & Connection Manager
- **Dual-Engine Auto Discovery**:
  - **mDNS (Network Service Discovery)**: Listens for `_adb._tcp.` services broadcasted by Android TV.
  - **Parallel Subnet Sweep**: Concurrently scans the local Wi-Fi subnet on port `5555` to detect devices that do not broadcast mDNS.
- **Recent & Saved Connections**: Saves connection history with custom aliases (e.g., "Living Room Sony TV", "Bedroom Shield TV") and last-seen timestamps for instant 1-tap reconnect.
- **Connection Health**: Real-time ping latency indicator, device model detection, and quick disconnect.

### 3. 💻 Interactive Terminal & Shell
- Interactive terminal console with dark theme styling, execution durations, and exit code status badges (`exit: 0`).
- **Quick TV Action Chips**: 1-tap shortcuts for common TV diagnostic commands:
  - Active Window (`dumpsys window | grep -E 'mCurrentFocus'`)
  - Power State (`dumpsys power | grep mWakefulness`)
  - Memory Summary (`dumpsys meminfo -s`)
  - Storage Usage (`df -h /data`)
  - IP Routes (`ip route`)
  - Keyevents (Home, Back, Select, Wake/Sleep)
- Command history dropdown for rapid command recall.
- Copy entire session output to clipboard or clear output with a single tap.

### 4. 📦 Remote TV App Manager
- Live inventory of installed TV applications (user 3rd-party vs system apps).
- Instant search filter by package name or label.
- **Remote Actions**:
  - 🚀 **Launch**: Remotely start any app on your TV display (`monkey -p <pkg> -c android.intent.category.LAUNCHER 1`).
  - 🛑 **Force Stop**: Terminate runaway or background background apps (`am force-stop <pkg>`).
  - 🧹 **Clear Data**: Wipe app cache and temporary data (`pm clear <pkg>`).
  - 🗑️ **Uninstall**: Remotely remove unneeded or bloatware apps (`pm uninstall <pkg>`).

### 5. 🎮 Floating TV Remote Control Overlay
- Accessible from **any screen** via the floating action button.
- Full TV navigation D-pad: Up, Down, Left, Right, OK / Select.
- TV power, volume up, volume down, and mute buttons.
- Android TV navigation buttons: Back (`4`), Home (`3`), Menu (`82`), Play/Pause (`85`).
- **Remote Keyboard / Text Injection**: Type on your phone's keyboard and send raw text strings directly to your TV's active input field (`input text "..."`).

---

## 📺 Android TV Setup (Prerequisites)

To allow AdbTV Remote to connect to your Android TV or Google TV:

1. **Connect to Same Wi-Fi**: Ensure both your phone running AdbTV Remote and your Android TV are connected to the same local Wi-Fi network.
2. **Enable Developer Options**:
   - On your TV, navigate to **Settings** → **Device Preferences** (or **System**) → **About**.
   - Scroll down to **Android TV OS Build** (or **Build**) and click it **7 times** repeatedly until a toast says *"You are now a developer!"*.
3. **Enable USB Debugging & Wireless Debugging**:
   - Go back to **Settings** → **Device Preferences** → **Developer Options**.
   - Toggle **USB Debugging** to **ON**.
   - If available on Android 11+, also toggle **Wireless Debugging** to **ON**.
4. **Authorize the Connection**:
   - When you first tap **Connect** in AdbTV Remote, look at your TV screen.
   - A dialog titled **"Allow USB debugging?"** will appear displaying the RSA key fingerprint.
   - Check the box **"Always allow from this computer"** and select **Allow** or **OK**.

---

## 🛠️ Building & Compilation

### Requirements
- **JDK 17**
- **Android SDK Platform 36** (minSdk 24, targetSdk 36)
- **Gradle 8.x+**

### Build Commands
```bash
# Clone the repository
git clone https://github.com/your-username/adbtv-remote.git
cd adbtv-remote

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease
```
The resulting APKs will be located in:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release-unsigned.apk`

---

## 🚀 GitHub Actions CI/CD

A production-ready GitHub Actions workflow is included at `.github/workflows/build-apk.yml`. On every `push` or `pull_request` to `main` or `master`:
- Checks out the project and sets up JDK 17 with Gradle caching.
- Compiles both Debug and Release APKs.
- Automatically uploads the APKs as downloadable artifacts under the GitHub Actions run summary.

---

## 👏 Credits & Acknowledgments

- **[dadb](https://github.com/mobile-dev-inc/dadb)** by Mobile Dev Inc. — Pure Kotlin ADB implementation that enables zero-dependency Android-to-Android debugging over TCP/IP.
- **[Clean Your TV](https://github.com/udhaykumarbala/cleanyourtv-app)** by Udhay Kumar Bala — Inspiration for pure Kotlin TV management, debloating strategies, and direct TCP socket authorization without root.
- **Jetpack Compose & Material 3** by Google — Modern declarative UI and dynamic color theming.

---

## 📄 License
This project is open-source under the Apache License 2.0.
