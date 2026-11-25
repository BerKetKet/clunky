# Round 152 Report — Azath v17.2 KFX (Unified 22sp EB Garamond Headers & Compose Bottom Bar Audit)

## 1. Target Hardware & Policy Compliance
- **Hardware**: Physical **Samsung Galaxy M30** (`32001776e8e82623`), running TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Active Package**: `com.azath.reader` (`Azath v17.2 KFX`, `versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-25 03:07:16`).
- **Safety Invariant Verified**: Stock `com.amazon.kindle` (`versionName=8.160.1.0(2.0.124621.0)`) remains **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).
- **Strict Policy Respected**: Zero Play Store Folio (`ct.reader`) launches, inspections, or references.

## 2. Task A: Verbatim Drop-In Build, Deploy & Stage (`Azath v17.2 KFX`)
- Copied all 3 Round 152 files (`FolioTheme.java`, `FolioMoreView.java`, `OpenCatalogView.java`) verbatim into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/`.
- Updated `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` to stage `07_APK_EPUB_INGESTION_MOD/release/azath_v17.2_kfx/base.apk`.
- Recompiled DEX cleanly (`classes9.dex`: `475,644` bytes), built/signed APK, deployed to Galaxy M30 (`32001776e8e82623`), and staged release:
  - **Path**: `07_APK_EPUB_INGESTION_MOD/release/azath_v17.2_kfx/base.apk`
  - **Size**: `138,831,975` bytes
  - **SHA256**: `a7c1f0d67e2b5a041f4afd7da22d81ce4aa1cd41892263348881e66aee96a5fb`

## 3. Task B: Hardware Verification Screenshots & Honest Audit (Samsung Galaxy M30)
- **`verify_r152_azath_home_limestone_bar.png` (`884,004` bytes)**: Toggled `Granite -> Limestone` inside Control Center, then tapped `Home`.
  - **Unified Header Verified**: `Azath` renders at `bounds="[42,127][527,202]"` (`x=42px`, `height=75px`), cleanly matching `22sp` Regular EB Garamond weight and baseline across tabs.
  - **System Nav Gesture Bar**: The Android gesture bar strip at the bottom switched cleanly to warm Limestone `#EFEAE0`.
  - **Honest Compose Bottom-Bar Observation (Per Stop-and-Report Protocol)**: The Compose bottom navigation tab strip (`Home`, `Library`, `More` directly above the system gesture bar) **did not recompose to Limestone** — it remained dark (`#101112` container with `#D4C9B8` active icon) even though `syncComposeBottomBarColors(ctx)` reflects onto `ib.b`'s `private static final long` fields. Likely causes: `ib.b`'s static color longs are read once when Kindle's Compose `CompositionLocalProvider` / theme object initializes (non-`MutableState`), or `setAccessible(true)` reflection onto `private static final long` fields doesn't trigger Compose snapshot invalidation on tab switch.
- **`verify_r152_azath_control_center_granite.png` (`312,570` bytes)**: Toggled back to **Granite** (`bounds="[887,222][1038,286]"` text `Granite`) and captured Control Center (`v17.2 KFX` badge `[854,129][1038,197]`, `Control Center` header `bounds="[42,125][646,200]"` matching `Azath`'s exact `x=42px, height=75px` geometry) with dark Granite bottom bar.
- **Logcat Crash Check**: `ZERO_CRASHES_AND_NO_SIGSYS` in `AndroidRuntime:E`, `FATAL:E`, and `DEBUG:*`.

## 4. Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
