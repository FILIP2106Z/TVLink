# Reliable APK Installer & Mobile App TV Compatibility (Completed)

A comprehensive architectural implementation that resolves APK installation failures, adds robust multi-stage ADB installation (`dadb.push` + `pm install -r -d -t -g`), detailed diagnostic feedback, and dedicated support for running and navigating standard mobile Android apps on Android TV.

## Completed Capabilities & Implementation Details

### 1. Two-Stage Installation Engine (`AdbManager.kt`)
- **Stage 1 (Direct Streaming with Flags)**: Executes `dadb.install(file, "-r", "-d", "-t", "-g")`:
  - `-r`: Replaces existing packages without requiring manual uninstalls.
  - `-d`: Allows version downgrades.
  - `-t`: Allows test-signed packages and debug builds.
  - `-g`: **Automatically grants all runtime permissions** at install time, fixing standard phone apps from hanging on Android TV permission dialogs that cannot be selected with standard D-pad remotes.
- **Stage 2 (Push & Shell Fallback)**: If streaming over Wi-Fi sockets fails or times out, TVLink pushes the APK to `/data/local/tmp/tvlink_install_<timestamp>.apk`, runs `pm install -r -d -t -g`, and ensures the temporary APK file is immediately removed.
- **Error Diagnostic Parser**: Translates raw Android Package Manager errors into friendly guidance:
  - `INSTALL_FAILED_NO_MATCHING_ABIS`: Informs the user of 32-bit (`armeabi-v7a`) vs 64-bit (`arm64-v8a`) mismatches and advises installing universal or 32-bit APKs.
  - `INSTALL_FAILED_INSUFFICIENT_STORAGE`: Prompts user to free TV storage.
  - `INSTALL_FAILED_VERSION_DOWNGRADE`: Advises uninstalling the newer version first.
  - `INSTALL_FAILED_UPDATE_INCOMPATIBLE`: Advises uninstalling previously installed app due to certificate signing mismatch.

### 2. TV Architecture Detection & Sideload Guidance
- Queries `ro.product.cpu.abi` and displays the TV CPU architecture chip in the App Manager screen and inside the GitHub/URL sideload dialog (e.g., `TV Architecture: armeabi-v7a (32-bit)`).
- Warns users on the difference between 32-bit and 64-bit packages before downloading.

### 3. TV Native vs Mobile App Tagging & Direct TV Launcher
- Queries `cmd package query-intent-activities -c android.intent.category.LEANBACK_LAUNCHER` to determine whether an app contains a native Android TV Leanback banner.
- Tags apps with clear badges: `[TV Native]` vs `[Mobile App]`.
- Launching utilizes `monkey -p <pkg> -c android.intent.category.LAUNCHER 1`, bypassing the Android TV launcher filter and allowing users to open standard smartphone apps on their TV with one tap.

### 4. Virtual Touchpad & Mouse Remote (`TvRemoteSheet.kt`)
- Added a segmented mode toggle between **D-Pad** and **Mouse / Touchpad**.
- **Virtual Touchpad Surface**: Full gesture detection via Compose `pointerInput`:
  - Tap on touchpad surface sends `input tap X Y` at corresponding display coordinates.
  - Drag on touchpad surface sends `input swipe X1 Y1 X2 Y2`.
- **Navigation Shortcuts**: Quick 1-tap **Scroll ▲**, **Scroll ▼**, and **Click (OK)** buttons to easily operate touch-oriented smartphone apps on the TV.

### 5. Verification
- All files build cleanly via `compile_applet`.
- Robolectric and unit tests pass with 31/31 successful Gradle tasks.
