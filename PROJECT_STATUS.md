# Luma TV — Project Status

## 1. Device Specifications
- **Device Model**: Xiaomi Mi TV 4A 32-inch (`MiTV-AXSO0`)
- **Device Codename / Board**: `dangal`
- **Android Version**: Android 9 (Pie)
- **API Level**: 28
- **Build ID**: `PI.6036 release-keys`
- **Memory**: ~1 GiB RAM (`1,014,724 kB` total)
- **Internal Storage**: `/data` total 4.4 GiB (2.0 GiB available / 47% free)
- **Display Resolution / Density**: 1366x768 (tvdpi / 540dp x 960dp landscape)
- **Package ID**: `com.mg.tv`
- **App Name**: `Luma`
- **Fallback Launcher**: `com.google.android.tvlauncher/.MainActivity` / `com.android.tv.settings/.system.FallbackHome`

---

## 2. Apple TV UI Overhaul (Implemented & Verified on TV)

### 2.1 Visual Architecture & Layout
- **Style**: Apple tvOS / Cinematic Dark (`#08090C` base with luminous ambient lighting).
- **Ambient Spotlight Glow**: Dynamic radial spotlight centered behind the header (`50% X, 26% Y`) that softly illuminates the top half of the screen.
  - Dynamically crossfades to match the focused app's brand colors (e.g. crimson red for Netflix, electric blue for Prime Video, pure red for YouTube, violet-magenta for JioHotstar).
- **Focused App Title**: Prominent white typography (`32sp sans-serif-medium`) positioned directly above the card row, updating synchronously with D-pad navigation.
- **Continuous 4-Column Vertical Grid**:
  - Exactly 4 cards per row spanning the full 16:9 width without horizontal scrollbars.
  - Row 1 (Top Streaming Favourites): Netflix, Prime Video, YouTube, JioHotstar.
  - Row 2 (Remaining Installed Apps): Peeking up from the bottom edge by ~45dp.
  - D-pad Down scrolls vertically to reveal Row 2, Row 3, etc.
  - D-pad Up scrolls back to top; Back button (`KEYCODE_BACK`) returns smoothly to top and focuses the first card.
- **TV Banners & Frosted Cards**:
  - Automatically loads the official 16:9 Leanback banners (`ri.activityInfo.loadBanner(pm)`).
  - Anti-aliased 18dp rounded corners with hardware outline clipping (`clipToOutline = true`).
  - Active focus state: `1.06x` scale animation, `12dp` elevation shadow, and bright glowing white focus outline (`stroke 3.5dp #FFFFFFFF`).
- **Header Pill Buttons**:
  - Left: "GOOD EVENING" greeting, large clock ("9:13 PM"), and date ("THURSDAY 29 OCTOBER").
  - Right: Sleek frosted-glass pill buttons with icons for **Inputs**, **WiFi** (displays active SSID), and **Settings**.

### 2.2 New Branding & Icon Assets
- **App Icon**: Modern minimalist Apple tvOS squircle badge with dark obsidian background and an ultra-thin TV screen emitting ambient crimson/violet aurora lighting.
- **TV Leanback Banner (320x180)**: 16:9 widescreen silhouette with glowing ambient backlight.
- Multi-density mipmap icons generated (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`).
- Native vector adaptive drawables (`ic_launcher_background.xml` and `ic_launcher_foreground.xml`).

---

## 3. Launcher Status & Recovery Commands

| Launcher | Role | Package Component | Priority | Status |
| :--- | :--- | :--- | :---: | :--- |
| **MG TV (Clean Launcher)** | **Primary System Default** | `com.miclean.launcher/.MainActivity` | `0` (active) | **ACTIVE DEFAULT** |
| **Google TV Launcher** | **System Fallback** | `com.google.android.tvlauncher/.MainActivity` | `2` | Disabled (`disabled-user`) |
| **Android Settings Fallback** | **Emergency Fallback** | `com.android.tv.settings/.system.FallbackHome` | `-1000` | Standby |

### Emergency Recovery Commands (via ADB)
- **Restore Google TV Launcher as Default:**
  ```bash
  adb shell pm enable com.google.android.tvlauncher
  adb shell cmd package set-home-activity com.google.android.tvlauncher/.MainActivity
  ```
- **Set MG TV as Default:**
  ```bash
  adb shell pm disable-user --user 0 com.google.android.tvlauncher
  adb shell cmd package set-home-activity com.miclean.launcher/.MainActivity
  ```
- **Launch Settings directly:**
  ```bash
  adb shell am start -a android.settings.SETTINGS
  ```
