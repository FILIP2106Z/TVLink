# TVLink 📺

> The ultimate open-source wireless Android TV remote, 1-tap APK sideload manager, virtual touchpad, and TV speed booster. No root, no computer, and no USB cables required.

<div align="center">

[![Latest Release](https://img.shields.io/github/v/release/FILIP2106Z/AdbTv-?style=for-the-badge&color=00C853&label=LATEST%20RELEASE)](https://github.com/FILIP2106Z/AdbTv-/releases/latest)
[![Download APK](https://img.shields.io/badge/DOWNLOAD-APK-blue?style=for-the-badge&logo=android&logoColor=white)](https://github.com/FILIP2106Z/AdbTv-/releases/latest/download/TVLink-latest.apk)
[![Obtainium](https://img.shields.io/badge/OBTAINIUM-1--CLICK-8E24AA?style=for-the-badge&logo=android)](https://github.com/ImranR98/Obtainium)
[![License](https://img.shields.io/badge/LICENSE-APACHE%202.0-orange?style=for-the-badge)](LICENSE)

<br/>

[![Platform](https://img.shields.io/badge/Platform-Android%2012%20–%2016+-3DDC84?logo=android&logoColor=white)](#-device-compatibility)
[![Target](https://img.shields.io/badge/Target-Android%20TV%20%7C%20Google%20TV%20%7C%20Fire%20OS-4285F4?logo=google)](#-device-compatibility)
[![Zero Root](https://img.shields.io/badge/Root-NOT%20REQUIRED-success)](#)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20Local%20P2P-brightgreen)](#)

</div>

---

## 📥 Instant Downloads

Choose the easiest way to install TVLink on your phone or tablet:

| Method | Link | Recommended For |
| :--- | :--- | :--- |
| **🚀 Direct Download (Latest APK)** | **[Download TVLink-latest.apk](https://github.com/FILIP2106Z/AdbTv-/releases/latest/download/TVLink-latest.apk)** | **Everyone (1-click direct file)** |
| **📱 Obtainium (Auto-Updates)** | **[Add to Obtainium](https://github.com/ImranR98/Obtainium)** (`https://github.com/FILIP2106Z/AdbTv-`) | Automatic background updates |
| **📦 GitHub Releases Page** | **[Browse All Versions & Assets](https://github.com/FILIP2106Z/AdbTv-/releases)** | Specific versions & release notes |
| **📺 Android TV Downloader App** | URL: `https://github.com/FILIP2106Z/AdbTv-/releases/latest/download/TVLink-latest.apk` | Sideloading directly onto TV |

### SHA-256 Checksum Verification
Every release includes cryptographic SHA-256 hashes inside `checksums.txt` on the [Releases page](https://github.com/FILIP2106Z/AdbTv-/releases/latest) to guarantee binary integrity.

---

## ✨ Key Features & Capabilities

```
┌────────────────────────────────────────────────────────────────────────┐
│                                TVLink                                  │
│  ┌───────────────────────┬──────────────────────┬───────────────────┐  │
│  │   🎮 Virtual Remote   │  🖱️ Virtual Touchpad │  🚀 1-Tap Sideload│  │
│  │   D-Pad / Volume /    │  Mouse cursor &      │  Multi-stage ADB  │  │
│  │   Power / Text Input  │  scroll for phone    │  with auto-grant  │  │
│  │   over local Wi-Fi    │  apps on Android TV  │  permissions (-g) │  │
│  ├───────────────────────┼──────────────────────┼───────────────────┤  │
│  │   🛡️ Zero-Root Debloat│  ⚡ TV Speed Booster │  ⚙️ Hidden Settings│  │
│  │   Disable bloatware   │  2x UI animation     │  DPI scaler &     │  │
│  │   & telemetry safely  │  speed & RAM cleanup │  Encrypted DNS    │  │
│  └───────────────────────┴──────────────────────┴───────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

### 🎮 1. Ergonomic TV Remote Control
- **Full Navigation**: Fluid D-pad, OK, Back, Home, Menu, and Play/Pause controls.
- **Hardware Keys**: Volume Up, Volume Down, Mute, and Display Wake/Sleep toggle.
- **Remote Keyboard**: Type search queries, passwords, and long URLs on your phone's keyboard and send them directly into Android TV input fields.

### 🖱️ 2. Virtual Mouse & Touchpad (Run Mobile Apps on TV)
- **Solve Mobile App Limitations**: Many smartphone apps installed on Android TV do not respond to remote directional buttons.
- **Touch Gesture Control**: Real-time cursor taps (`input tap X Y`) and directional swipe scrolling (`input swipe X1 Y1 X2 Y2`).
- **Dedicated Shortcuts**: 1-tap **Scroll ▲**, **Scroll ▼**, and **Click (OK)** buttons.

### 🚀 3. Bulletproof Multi-Stage APK Sideloading
- **Two-Stage Engine**: Primary direct socket streaming with automatic fallback to `/data/local/tmp/` push and package manager installation if Wi-Fi sockets hit transfer limits.
- **Smart Compatibility Flags**:
  - `-r`: Replace existing app without requiring manual uninstallation.
  - `-d`: Allow version downgrades.
  - `-t`: Allow test and development packages.
  - **`-g` (Vital)**: **Automatically grants all runtime permissions** upon install, preventing smartphone apps from freezing on TV permission prompts that cannot be clicked with a standard remote!
- **Error Diagnostics**: Automatically translates cryptic Android errors (`INSTALL_FAILED_NO_MATCHING_ABIS`, `INSTALL_FAILED_INSUFFICIENT_STORAGE`) into human-readable advice.
- **1-Tap Popular TV Presets**: Built-in 1-tap download and installation for SmartTube, CleanYourTV, and Nova Video Player.

### 📺 4. Leanback Bypass TV Launcher
- Standard smartphone apps lack Android TV Leanback banners and are hidden by the default TV home screen.
- TVLink detects them with a **`[Mobile App]`** badge and launches them directly via `monkey -p <pkg> -c android.intent.category.LAUNCHER 1`.

### 🛡️ 5. Zero-Root Debloater & App Manager
- Freeze or disable factory-installed TV bloatware, pre-bundled streaming apps, and telemetry without root (`pm disable-user --user 0`).
- Re-enable any debloated app at any time with a single tap.
- Clear app caches and app data remotely to recover storage.

### ⚡ 6. TV Speed Booster
- Make sluggish, low-spec smart TVs instantly snappier by setting window, transition, and animator scales to **0.5x (2x Speed)** or **0.0x (Animations OFF)**.
- Kill cached background processes with one tap to free up RAM.

### ⚙️ 7. Hidden Display & Private DNS Settings
- **DPI Scaler**: Override display density from 240 DPI to 400 DPI (`wm density`) for larger or more compact TV interfaces.
- **Ad-Blocking Private DNS**: Enforce encrypted system-wide ad-blocking on Android TV via AdGuard DNS or Cloudflare 1.1.1.1.
- **Stay Awake**: Keep TV screen active while streaming or monitoring.

### 💻 8. Wireless ADB Terminal
- Complete interactive command-line terminal with real-time command output, execution time metrics, and saved command history.
- Built-in shortcuts for `dumpsys`, `pm list`, `top`, `ip addr`, and `reboot`.

---

## ⚡ 60-Second Setup Guide

### Step 1: Connect to Same Wi-Fi
Ensure your Android phone and Android TV / Google TV are connected to the same local Wi-Fi network.

### Step 2: Enable Wireless Debugging on TV
1. On your TV, navigate to **Settings** ➔ **Device Preferences** (or **System**) ➔ **About**.
2. Scroll to **Build** (or **Android TV OS build**) and click it **7 times** until you see *"You are now a developer!"*.
3. Go back one menu to **Developer Options**.
4. Turn ON **USB Debugging** (and **Wireless Debugging** if visible).

### Step 3: Connect TVLink
1. Open **TVLink** on your phone.
2. Tap your TV from the **Auto-Discovery** list (or enter your TV's IP address displayed in TV Network Settings).
3. Look at your TV screen and check **"Always allow from this computer"** ➔ click **Allow / OK**.
4. You are connected! Enjoy complete control over your TV.

---

## 📲 Obtainium Setup (Recommended for Automatic Updates)

Get seamless background updates without Google Play Store:

1. Install **[Obtainium](https://github.com/ImranR98/Obtainium)** on your phone or Android TV.
2. In Obtainium, tap **Add App (+)**.
3. In the **App Source URL** field, paste:
   ```text
   https://github.com/FILIP2106Z/AdbTv-
   ```
4. Tap **Add**. Obtainium will automatically track releases and notify you whenever a new version of TVLink is available!

---

## 📺 Device Compatibility

TVLink is tested and verified across modern Android and TV platforms:

| Device Category | Supported Devices |
| :--- | :--- |
| **Google TV** | Chromecast with Google TV (4K & HD), Sony Bravia Google TV, TCL Google TV, Hisense Google TV, Philips Google TV |
| **Android TV** | Nvidia Shield TV / Pro, Xiaomi Mi Box S, Mi TV Stick, Nokia Streaming Box, Mecool, dynalink |
| **Fire OS** | Amazon Fire TV Stick (4K Max, 4K, Lite), Fire TV Cube, Fire TV Smart TVs |
| **Mobile Platforms** | Android 12, 13, 14, 15, 16+, OriginOS, One UI, ColorOS, OxygenOS, LineageOS, Termux |

---

## ❓ Troubleshooting & FAQs

<details>
<summary><b>1. TVLink says "Connection refused" or times out</b></summary>

- Confirm that your phone and Android TV are on the **same Wi-Fi network** (ensure your router does not have "AP Isolation" or "Guest Mode" enabled).
- Verify that **USB Debugging** is toggled ON under Developer Options on your TV.
- Try restarting the ADB daemon on the TV by toggling USB Debugging OFF and ON again.
</details>

<details>
<summary><b>2. App says "Unauthorized"</b></summary>

- Look at your TV screen. A dialog will appear asking to authorize USB debugging.
- Check the box **"Always allow from this computer"** and select **OK**.
</details>

<details>
<summary><b>3. Why did an APK fail with `INSTALL_FAILED_NO_MATCHING_ABIS`?</b></summary>

- Many budget smart TVs (such as Chromecast HD or Xiaomi Mi Box) have 32-bit CPU architectures (`armeabi-v7a`).
- If you attempt to install a 64-bit-only (`arm64-v8a`) APK, Android TV will reject it.
- **Solution**: Download the **32-bit (`armeabi-v7a`)** or **Universal** APK variant from the app developer's GitHub or website. TVLink displays your TV's exact architecture at the top of the App Manager tab!
</details>

<details>
<summary><b>4. How do I navigate mobile phone apps on my TV?</b></summary>

- Open the floating **TV Remote** in TVLink.
- Switch the toggle at the top from **D-Pad** to **Mouse / Touchpad**.
- Drag your finger across the virtual touchpad to scroll or swipe, and tap anywhere to click on the TV screen.
</details>

---

## 🛠️ Building From Source

```bash
# Clone the repository
git clone https://github.com/FILIP2106Z/AdbTv-.git
cd AdbTv-

# Build Debug and Release APKs
./gradlew assembleDebug assembleRelease
```
The compiled binaries will be located at:
- `app/build/outputs/apk/release/app-release.apk`
- `app/build/outputs/apk/debug/app-debug.apk`

---

## 📜 License

TVLink is free and open-source software distributed under the **[Apache License 2.0](LICENSE)**.

---

<div align="center">
Made with ❤️ for the Android TV and Open-Source Community.
</div>
