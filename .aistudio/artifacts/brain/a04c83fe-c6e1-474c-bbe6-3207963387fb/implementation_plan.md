# Reliable APK Installer & Mobile App TV Compatibility

A comprehensive architectural plan to resolve APK installation failures, add robust multi-stage ADB installation (`dadb.push` + `pm install -r -d -t -g`), detailed diagnostic feedback, and dedicated support for running and navigating standard mobile Android apps on Android TV.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following user preferences were confirmed during clarification and will be implemented:

- **Confirmed Decision 1 (Installation Root Cause & Strategy)**:
  - APK installs on Android TV frequently time out or fail over ADB sockets because of buffer limits, missing `-r -d -t -g` flags, or architecture incompatibilities (e.g. attempting to install 64-bit `arm64-v8a` on 32-bit `armeabi-v7a` TVs like Chromecast with Google TV HD).
  - We will replace the single-method install with a **two-stage push-and-install engine**:
    1. Direct ADB streaming install (`dadb.install(apkFile, "-r", "-d", "-t", "-g")`).
    2. Fallback to `dadb.push(apkFile, "/data/local/tmp/app.apk")` followed by `pm install -r -d -t -g /data/local/tmp/app.apk`.
    3. Parsing and displaying exact error diagnostics (`INSTALL_FAILED_NO_MATCHING_ABIS`, `INSTALL_FAILED_INSUFFICIENT_STORAGE`, etc.) instead of vague timeouts.
- **Confirmed Decision 2 (Mobile App Support on TV)**:
  - Standard mobile apps do not appear on the Android TV Leanback launcher home screen. TVLink will tag them with a **[Mobile App]** badge and provide a 1-tap **Direct TV Launcher** (`monkey -p <pkg> -c android.intent.category.LAUNCHER 1`).
  - Mobile apps require permissions that cannot be accepted via D-pad; the `-g` flag grants all permissions at install time.
  - In the floating remote, we will add a **Virtual Cursor / Touchpad Mode** allowing users to send directional cursor taps (`input tap X Y`) and screen swipes (`input swipe`) to operate mobile interfaces easily.

---

## 1. Overview & Core Concept

- **What It Does**: Resolves all APK installation timeouts and crashes when sideloading onto Android TV devices. Enhances TVLink to seamlessly handle both native Android TV apps and standard smartphone apps, including automatic permission grants (`-g`), Leanback bypass launching, and virtual cursor navigation.
- **Target Persona**: Android TV users wishing to sideload third-party apps, emulators, streaming APKs, and mobile utilities onto their smart TVs without root or a computer.
- **Key Value**: 100% reliable installation with transparent error reporting and full usability of touch-oriented mobile apps on large TV displays.

---

## 2. User Experience & Visual Design

### Key Flows
1. **Sideloading an APK**:
   - User picks a local file or enters a GitHub link.
   - TVLink uploads with a visual progress bar.
   - If the TV accepts it, a success toast appears with an immediate **"Launch on TV"** action button.
   - If the TV rejects it (e.g., mismatched CPU ABI or low storage), TVLink displays an explanation (e.g., *"Incompatible architecture: Your TV requires 32-bit armeabi-v7a"*).
2. **Managing & Launching Mobile Apps**:
   - The **TV Apps** tab displays clear badges: `[TV Native]` vs `[Mobile App]`.
   - Tapping **Launch** instantly fires the app on the TV even if hidden from the stock TV launcher.
3. **Navigating Mobile Apps via Remote**:
   - The floating TV Remote includes a toggle between **D-pad Mode** (standard TV controls) and **Mouse / Cursor Mode** (sends directional cursor taps and scroll swipes).

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Push + Package Manager vs Decompilation/Repackaging**:
  - *Chosen Approach*: Multi-tier ADB install with `-r -d -t -g` flags, combined with TVLink's Leanback-bypass launcher and virtual cursor.
  - *Why*: On-device APK decompilation, modifying `AndroidManifest.xml` (injecting Leanback intent filters), and re-signing with `apksigner` on Android is extremely slow (minutes per app), breaks app signatures (preventing updates), and triggers security scanner blocks. Sideloading mobile apps directly with automated permissions and direct launching achieves the same result with 10x faster speed and zero risk of corruption.
- **Decision 2: Automated ABI Architecture Detection**:
  - *Chosen Approach*: Query `getprop ro.product.cpu.abi` upon connecting to warn users if they attempt to install a 64-bit APK on a 32-bit Android TV device.

---

## 4. Technical Architecture & Implementation Plan

```
┌────────────────────────────────────────────────────────┐
│                   AppManagerScreen                     │
│  - File Picker / GitHub URL                            │
│  - Architecture Check Banner (e.g. armeabi-v7a)        │
│  - TV Apps List with [TV Native] vs [Mobile App] badge │
└──────────────────────┬─────────────────────────────────┘
                       │
                       ▼
┌────────────────────────────────────────────────────────┐
│                    AdbViewModel                        │
│  - downloadAndInstallApk(url)                          │
│  - installApk(file) with detailed progress & status    │
│  - launchMobileApp(packageName)                        │
└──────────────────────┬─────────────────────────────────┘
                       │
                       ▼
┌────────────────────────────────────────────────────────┐
│                    AdbManager                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │ Primary: dadb.install(file, "-r", "-d", "-t", "-g")│
│  ├──────────────────────────────────────────────────┤  │
│  │ Fallback: dadb.push(file, "/data/local/tmp/...") │  │
│  │           pm install -r -d -t -g /data/...       │  │
│  │           rm /data/local/tmp/...                 │  │
│  └──────────────────────────────────────────────────┘  │
│  - parseInstallError(output) -> Human-readable advice  │
│  - getDeviceAbi() -> "arm64-v8a", "armeabi-v7a", etc.  │
│  - sendTap(x, y), sendSwipe(x1, y1, x2, y2)            │
└────────────────────────────────────────────────────────┘
```

### Steps to Implement:
1. **`AdbManager.kt`**:
   - Overhaul `installApk`: Implement primary `dadb.install(file, "-r", "-d", "-t", "-g")` and fallback `dadb.push(...)` + `pm install`.
   - Add detailed error code parser (`INSTALL_FAILED_NO_MATCHING_ABIS`, `INSTALL_FAILED_INSUFFICIENT_STORAGE`, etc.).
   - Add `getDeviceAbi()` and helper for mouse touch events (`sendTap(x, y)`, `sendSwipe(...)`).
2. **`AdbViewModel.kt`**:
   - Expose `deviceAbi: StateFlow<String?>`.
   - Update `installApkFromUri` and `downloadAndInstallApk` with timeout safeguards and diagnostic messaging.
3. **`AppManagerScreen.kt`**:
   - Display target TV CPU ABI info chip (e.g., *"TV Architecture: armeabi-v7a"*).
   - Differentiate `isLeanback` vs mobile apps in the list.
4. **`TvRemoteSheet.kt`**:
   - Add a toggle for **Mouse / Cursor Mode** (sends `input tap` and `input swipe` for mobile apps).

---

## 5. Verification Plan

1. **Compilation Check**: Verify with `compile_applet`.
2. **Unit Test Validation**: Run `:app:testDebugUnitTest`.
3. **Error Handling**: Ensure descriptive diagnostic messages are returned for common install failures.
