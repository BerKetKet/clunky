# Round 147 Report — Azath v16.1 KFX (Adaptive Launcher Icon Safe-Zone Framing & Live Granite/Limestone Theme Switcher)

## 1. Target Hardware & Policy Compliance
- **Hardware**: Physical **Samsung Galaxy M30** (`32001776e8e82623`), running TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Active Package**: `com.azath.reader` (`Azath v16.1 KFX`, `versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-25 02:14:27`).
- **Safety Invariant Verified**: Stock `com.amazon.kindle` (`versionName=8.160.1.0(2.0.124621.0)`) remains **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).
- **Strict Policy Respected**: Zero Play Store Folio (`ct.reader`) launches, inspections, or references.

## 2. Task A: Adaptive Launcher Icon & Dynamic Theme Build / Deploy / Stage
- Copied Round 147 `FolioMoreView.java` and `OpenCatalogView.java` from `dropin_src/com/amazon/kindle/epub/catalog/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/`.
- Synced safe-zone-framed `dropin_assets/azath_launcher/ic_launcher_foreground_432.png` -> `dropin_res/ic_launcher_foreground.png` and `dropin_assets/azath_launcher/azath_icon_limestone_512.png` -> `dropin_res/ic_launcher_full_512.png`, alongside `dropin_res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/`.
- Updated `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` to stage `07_APK_EPUB_INGESTION_MOD/release/azath_v16.1_kfx/base.apk`.
- Recompiled DEX cleanly (`classes9.dex`: `471,184` bytes), built and signed APK, deployed to Galaxy M30 (`32001776e8e82623`), and staged release:
  - **Path**: `07_APK_EPUB_INGESTION_MOD/release/azath_v16.1_kfx/base.apk`
  - **Size**: `138,827,879` bytes
  - **SHA256**: `e92c910b32732eb6d4d8723f0acd929e2858accbbd594c2c5a93184d7f9ded31`

## 3. Task B: Hardware Verification & Visual Audit (Samsung Galaxy M30)
- **`verify_r147_azath_launcher_safezone.png` (`325,542` bytes)**: Verified in the Android app drawer that the feathered 69% safe-zone crop cleanly frames the **crescent moon, hanging tree foliage, and reader silhouette with open book** inside the circular `Azath` adaptive launcher icon with zero edge clipping.
- **`verify_r147_azath_home_limestone_light.png` (`874,452` bytes)**: Toggled `Granite` -> `Limestone` in Control Center (`v16.1 KFX` badge verified in UI XML) and verified Home in **Limestone Light Mode** (`#F4F0E8` warm limestone canvas, `#E7E1D5` Search bar & `Offline Mode` card, `#2D3138` dark Granite CTA/genre pills, EB Garamond `#1A1B1E` `Azath` wordmark & section headings).
- **`verify_r147_azath_control_center_limestone.png` (`314,002` bytes)**: Captured Control Center while Limestone mode was active, then toggled `Limestone` -> `Granite` (confirmed in UI XML `bounds="[876,221][1027,285]" text="Granite"`) and reopened so device returned cleanly to dark Granite mode.
- **Logcat Crash Check**: `NO_FATAL_EXCEPTIONS` in `AndroidRuntime:E` / `FATAL:E`.

### Honest Observations / Visual & UX Polish Opportunities for Round 148
1. **Home Carousel Book Titles in Limestone Light Mode (`OpenCatalogView.java`)**: Author names rendered cleanly in muted Granite ink, but **Continue Reading book titles** (e.g., *Delphi Complete Works of Aristotle*, *The 210th Day*, *Rashomon and Other Stories*) still render in light `#ECE8E0` on the `#F4F0E8` Limestone background (nearly invisible contrast) and need `FolioTheme.textPrimary(ctx)`.
2. **Control Center Card Surfaces in Limestone Light Mode (`FolioMoreView.java`)**: While Control Center's canvas, `Control Center` heading, subtitle, and `Limestone` toggle pill switched to Light Mode, the inner cards (stats tiles, `2026 Reading Goal`, `Continue Reading`, `Up Next Queue`, `LIBRARY & IMPORT` card) still render with static `BG_CARD` / `BG_SURFACE` (`#1C1E22` / `#25282D`) and light text instead of `FolioTheme.bgSurface(ctx)` / `bgControl(ctx)` / `textPrimary(ctx)`.
3. **In-App Tab Switch vs Reopen**: Tapping `Granite` -> `Limestone` updates `SharedPreferences`, but switching tabs from `More` -> `Home` in-process leaves the cached `RubyHomeFragment` in Granite until Azath is reopened (app restart triggered Limestone on Home). Automatically refreshing on tab re-resume/re-selection or calling `recreate()` on toggle would make switching instantaneous without a manual restart.

## 4. Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
