# Luma TV

An ultra-clean, Apple TV-inspired custom launcher and performance-optimized TV home experience designed for Android TV (specifically optimized for **Xiaomi Mi TV 4A** with 1 GB RAM, Android 9 Pie, API 28).

---

## Highlights

- **Apple TV / tvOS Aesthetic**: Dynamic ambient radial spotlight glow that crossfades to match the focused app's branding (Netflix crimson, Prime Video electric blue, YouTube red, Hotstar violet).
- **Official 16:9 Leanback Banners**: High-contrast, hardware-clipped rounded cards with a glowing focus outline.
- **Continuous 4-Column Vertical Grid**: Sleek, distraction-free grid that scrolls vertically with the remote D-pad.
- **Top Quick Actions**: Frosted glass capsule pills for TV Inputs (HDMI 1, HDMI 2, HDMI 3, AV), current Wi-Fi status, and Settings.
- **Ultra-lightweight**: ~1.2 MB APK size, ~20 MB runtime memory, zero telemetry, and zero continuous background services.

---

## Target Device Profile

- **Model**: Xiaomi Mi TV 4A 32" (`MiTV-AXSO0` / `dangal`)
- **OS**: Android TV 9 (API Level 28)
- **RAM**: ~1 GiB
- **Package ID**: `com.mg.tv`
- **Application Name**: `Luma`
- **Navigation**: Standard D-pad Android TV remote control

---

## Build Instructions

```bash
# Build release APK
./gradlew assembleRelease

# Output location:
# app/build/outputs/apk/release/app-release.apk
```

---

## Recovery & Emergency Commands

If needed, Google TV Launcher can be restored as default via ADB:

```bash
# Re-enable and restore Google TV Launcher
adb shell pm enable com.google.android.tvlauncher
adb shell cmd package set-home-activity com.google.android.tvlauncher/.MainActivity

# Open TV Settings directly
adb shell am start -a android.settings.SETTINGS
```
