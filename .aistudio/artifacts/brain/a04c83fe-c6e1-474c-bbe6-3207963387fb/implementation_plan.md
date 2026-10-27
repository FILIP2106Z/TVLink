# Remote ADB Terminal & TV Manager for Android TV

A high-performance Android TV wireless debugging tool, terminal emulator, remote controller, and application manager powered by a pure Kotlin ADB engine (`dadb`), designed according to Android 12+ Material 3 guidelines.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following user preferences were confirmed during clarification and incorporated into the design:

- **Confirmed Decision 1 (Virtual Remote Layout)**: A floating TV remote control bottom sheet will be accessible across all screens, allowing instantaneous TV D-pad navigation while running shell commands or managing apps.
- **Confirmed Decision 2 (Device Discovery)**: Automatic device discovery will combine dual Network Service Discovery (`NsdManager` for `_adb._tcp`) with high-speed local Wi-Fi subnet sweeping on port 5555.
- **Confirmed Decision 3 (Visual Theme)**: Material You dynamic theming (`dynamicLightColorScheme` / `dynamicDarkColorScheme`) with adaptive high-contrast styling and terminal syntax highlighting.
- **Backend Architecture**: Pure Kotlin `dadb` (`dev.mobile:dadb:2.0.0`) with automatic RSA ADB key-pair generation saved in app-private storage (`context.filesDir/adbkey`). No native binaries, root, or ADB server required.

---

## 1. Overview & Core Concept

- **What It Does**: Connects wirelessly to any Android TV, Google TV, or Android device over local Wi-Fi on ADB port 5555. Features an interactive terminal emulator with live command history, a remote TV package manager (launch, force stop, clear data, uninstall), and an ergonomic TV D-pad remote control overlay.
- **Target Audience / Persona**: Android TV power users, cord-cutters, developers, and enthusiasts wanting to manage apps, debloat smart TVs, test APKs, and control TV navigation wirelessly from their phone or tablet without a PC.
- **Key Value**: Zero-dependency Kotlin implementation—works out-of-the-box directly from an Android phone to Android TV without needing a laptop or USB cables.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Connect & Discover**: User opens the app; auto-discovery runs in the background to detect Android TV devices on the network. User can tap any detected TV or select from saved recent connections, or type an IP address. On the TV, the standard "Allow USB debugging?" dialog appears, user clicks Allow, and the app indicates a green Connected state with live latency.
2. **Interactive Terminal**: User navigates to the Terminal tab, selects quick TV commands (e.g., `dumpsys window`, `pm list packages`, `getprop`) or types custom shell commands. Output is rendered with color-coded timestamps, command prompts, and exit codes.
3. **App Manager**: User browses installed apps (filterable by User vs System). For any app, user can remotely Force Stop, Clear Data, Launch onto the TV screen, or Uninstall.
4. **Anywhere TV Remote**: Tapping the floating remote FAB opens a sleek bottom sheet containing a directional D-pad, OK, Home, Back, Menu, Power, and Volume controls sending real-time keyevents to the TV.

### Visual Identity & Theme
- **Design System**: Material 3 (Material You) with `DynamicColor` support on Android 12+, falling back to rich charcoal/slate surface colors with vibrant indigo/cyan accents.
- **Terminal Styling**: Monospace typography for shell outputs, status badges (success/failure), line wrapping, and syntax color coding.
- **Component Styling**: Bottom navigation bar with active pill indicators, elevated cards with rounded corners (16dp–24dp), and tactile ripple feedback on all controls.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Native Kotlin ADB (`dadb`) vs Shell Subprocess**:
  - *Chosen Approach*: `dev.mobile:dadb` over direct TCP sockets.
  - *Why*: Standard Android OS does not permit non-root apps to execute desktop adb client binaries. `dadb` implements the entire ADB protocol and RSA handshake in pure Kotlin.
- **Decision 2: App-Private ADB Key Generation**:
  - *Chosen Approach*: Generate and persist `adbkey` and `adbkey.pub` in `context.filesDir` via `AdbKeyPair.generate`.
  - *Why*: Eliminates file permission issues in sandboxed Android environments and ensures persistent authentication across sessions.
- **Decision 3: Dual-Mode Network Discovery**:
  - *Chosen Approach*: Combine Android `NsdManager` (DNS-SD/mDNS) with concurrent coroutine port scanning on the active Wi-Fi subnet.
  - *Why*: Some Android TVs broadcast `_adb._tcp` mDNS services, while others simply listen on port 5555 without advertising. Running both ensures 100% discovery rate.
- **Decision 4: Local Storage for Recent Devices**:
  - *Chosen Approach*: Persistent JSON / DataStore key-value list of recently connected devices with IP, port, alias, and last connected timestamp.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                   Jetpack Compose UI                   │
│  ┌─────────────────┐ ┌───────────────┐ ┌────────────┐  │
│  │ ConnectionScreen│ │ TerminalScreen│ │ AppManager │  │
│  └────────┬────────┘ └───────┬───────┘ └─────┬──────┘  │
│           │                  │               │         │
│           └──────────┬───────┴───────────────┘         │
│                      ▼                                 │
│         Floating TV Remote BottomSheet                 │
│         (D-pad, Home, Back, Volume, Power)             │
└──────────────────────┬─────────────────────────────────┘
                       │
                       ▼
┌────────────────────────────────────────────────────────┐
│                    AdbViewModel                        │
│  - Connection State (Disconnected/Connecting/Connected)│
│  - Terminal logs & Command history                     │
│  - Discovered & Recent Devices list                    │
│  - Target Installed Apps state                         │
└──────────────────────┬─────────────────────────────────┘
                       │
                       ▼
┌────────────────────────────────────────────────────────┐
│                 AdbManager Repository                  │
│  ┌───────────────────────┐  ┌───────────────────────┐  │
│  │     dadb Engine       │  │ DeviceDiscoveryService│  │
│  │ (AdbKeyPair + Socket) │  │  (mDNS + Subnet Scan) │  │
│  └───────────────────────┘  └───────────────────────┘  │
└──────────────────────┬─────────────────────────────────┘
                       │ TCP / IP Port 5555
                       ▼
              Target Android TV / Box
```

### Module & Component Plan
1. **Gradle Dependencies**: Add `dev.mobile:dadb:2.0.0` and configure repositories; comment out unused template dependencies.
2. **Data & Storage Layer**:
   - `DeviceConnection`: Data model for target IP, port, device name, and connection date.
   - `DevicePreferences`: Lightweight storage for recent connections and terminal command history.
3. **ADB Service Layer (`AdbManager.kt`)**:
   - Manages connection lifecycle (`connect`, `disconnect`, `testLatency`).
   - Executes shell commands asynchronously with timeout and error handling.
   - Keyevent sender helper (`sendKeyevent(keyCode)`).
   - Package manager parser (`getInstalledPackages`, `forceStop`, `clearData`, `launchPackage`).
4. **Discovery Layer (`DeviceDiscoveryService.kt`)**:
   - `NsdManager` service listener for `_adb._tcp.`.
   - Local subnet detector via `WifiManager` / network interfaces, performing parallel socket pings on port 5555.
5. **UI Layer**:
   - `MainActivity.kt`: Scaffold with dynamic M3 NavigationBar, top status bar, and floating remote control sheet.
   - `ConnectionScreen.kt`: Quick connect card, discovery radar, recent devices list with custom labels.
   - `TerminalScreen.kt`: Interactive terminal output, quick command buttons, input bar with history.
   - `AppManagerScreen.kt`: List of apps, search filter, system/user filter, batch and individual app controls.
   - `TvRemoteSheet.kt`: Virtual D-pad, Home, Back, Menu, Power, Volume up/down/mute.
6. **CI/CD & Documentation**:
   - `.github/workflows/build-apk.yml`: Compiles debug & release APKs on push.
   - `README.md`: Comprehensive usage guide, Android TV setup, and credits.

---

## 5. Verification Plan

1. **Compilation Check**: Run `compile_applet` to ensure Gradle resolves `dadb`, dependencies build cleanly, and Kotlin code compiles with zero errors.
2. **Unit / Architecture Validation**: Verify `AdbManager`, `DeviceDiscoveryService`, and UI state flows in local tests.
3. **Safety & Policy**: Ensure no prohibited permissions, properly declared `<uses-permission android:name="android.permission.INTERNET" />`, and edge-to-edge layout compliance.
