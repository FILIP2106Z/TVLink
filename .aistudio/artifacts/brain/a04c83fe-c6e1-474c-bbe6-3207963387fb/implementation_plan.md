# Rebranding Plan: TVLink

A complete visual and identity rebrand of the application to **TVLink**, featuring clean 2024 Google Material You aesthetics, a simplified minimalist TV & remote icon, and concise, human-readable documentation.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following user preferences were confirmed during clarification and will be implemented:

- **Confirmed Decision 1 (Brand Name)**: The app is officially named **TVLink**. This replaces all occurrences of `AdbCommander` across `metadata.json`, `strings.xml`, `settings.gradle.kts`, `MainActivity.kt`, and test files.
- **Confirmed Decision 2 (2024 Google Icon Style)**: The launcher icon will be regenerated with a **minimalist rounded squircle TV silhouette with a clean remote pad motif**, adopting the flat, bold geometric styling of 2024 Google apps.
- **Confirmed Decision 3 (Color Theme)**: Full dynamic Material You color system throughout the application with **Emerald Green** (`#00C853` / `#00E676`) and deep slate (`#0B0F19`) as the primary fallback accents.
- **Confirmed Decision 4 (Concise Documentation)**: The project `README.md` and repository description will be streamlined into simple, crisp sections with clear instructions for Obtainium and quick setup.

---

## 1. Overview & Core Concept

- **What It Does**: **TVLink** wirelessly connects your phone to Android TV and Google TV devices over local Wi-Fi. It gives you a fast TV remote control, instant package manager (with APK sideloading and debloating), live ADB terminal, and hidden TV settings tweaker—with zero PC, root, or ADB server required.
- **Target Audience**: Anyone with an Android TV who wants an easy, powerful remote control and tool to manage TV apps and speed without complicated setup.
- **Key Value**: Simple, clean, fast, and fully automated via GitHub Releases and Obtainium.

---

## 2. User Experience & Visual Design

### Visual Identity & Theme
- **App Name**: **TVLink**
- **Short Tagline / Main Description**: *"Wireless remote control, app manager, and ADB terminal for Android TV."*
- **Icon Design**:
  - Clean 2024 Google Material You aesthetic.
  - A clean rounded squircle TV screen with Emerald Green wireless signal and subtle remote D-pad silhouette.
  - Consistent solid background (`#0F172A`) with adaptive layer-list and mipmap fallbacks across all resolutions.
- **UI Styling**:
  - Edge-to-edge Material 3 layout respecting system insets.
  - Emerald Green and Cyan accents for active connection status, status pills, and action buttons.
  - 4 clean navigation tabs: **Connection**, **Terminal**, **TV Apps**, and **Settings**.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Full Platform Sync**:
  - *Chosen Approach*: Update `metadata.json`, `app/src/main/res/values/strings.xml`, and `settings.gradle.kts` simultaneously to `TVLink`.
  - *Why*: Keeps the sidebar, Android launcher label, APK bundle name, and GitHub repository description 100% unified.
- **Decision 2: Simplified README & Obtainium Instructions**:
  - *Chosen Approach*: Replace lengthy developer documentation with clean, bulleted setup instructions, Obtainium 1-tap install links, and simple feature cards.
- **Decision 3: CI/CD Release Asset Naming**:
  - *Chosen Approach*: Update `.github/workflows/build-apk.yml` to publish `TVLink-release.apk` and `TVLink-debug.apk` directly to GitHub Releases for seamless Obtainium tracking.

---

## 4. Technical Architecture & File Sync Plan

```
┌────────────────────────────────────────────────────────┐
│                      TVLink App                        │
│                                                        │
│  ┌──────────────┐ ┌──────────────┐ ┌────────────────┐  │
│  │  Connection  │ │   Terminal   │ │    TV Apps     │  │
│  └───────┬──────┘ └──────┬───────┘ └────────┬───────┘  │
│          │               │                  │          │
│          └───────────────┼──────────────────┘          │
│                          ▼                             │
│                  ┌───────────────┐                     │
│                  │  TV Settings  │                     │
│                  └───────┬───────┘                     │
│                          ▼                             │
│             Floating Virtual TV Remote                 │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
                 dadb Pure Kotlin Engine
```

### Files to Update During Execution:
1. `metadata.json`: Name set to `TVLink`, concise description.
2. `app/src/main/res/values/strings.xml`: `app_name` set to `TVLink`.
3. `settings.gradle.kts`: `rootProject.name` set to `TVLink`.
4. `app/src/main/java/com/example/MainActivity.kt`: TopAppBar title updated to `TVLink`.
5. `app/src/test/java/com/example/ExampleRobolectricTest.kt`: Assertion updated to `TVLink`.
6. `app-icon-generation`: Generate new 2024 Google Material squircle icon and update `ic_launcher_foreground.xml`, `ic_launcher_background.xml`, and density mipmaps.
7. `.github/workflows/build-apk.yml`: Release asset names updated to `TVLink-release.apk` and `TVLink-debug.apk`.
8. `README.md`: Rewritten into a clean, simple, easy-to-read guide.

---

## 5. Verification Plan

1. **Compilation Check**: Run `compile_applet` to ensure all references compile cleanly.
2. **Unit Test Check**: Run `:app:testDebugUnitTest` to verify string resource assertions.
3. **Asset Verification**: Ensure all launcher icon densities are generated and `.webp` template files removed.
