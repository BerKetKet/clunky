# Round 153 Report — Azath v17.3 KFX (Limestone Touch-Passthrough Bottom Navigation Bar)

## 1. Target Hardware & Policy Compliance
- **Hardware**: Physical **Samsung Galaxy M30** (`32001776e8e82623`), running TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Active Package**: `com.azath.reader` (`Azath v17.3 KFX`, `versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-25 03:13:56`).
- **Safety Invariant Verified**: Stock `com.amazon.kindle` (`versionName=8.160.1.0(2.0.124621.0)`) remains **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).
- **Strict Policy Respected**: Zero Play Store Folio (`ct.reader`) launches, inspections, or references.

## 2. Task A: Verbatim Drop-In Build, Deploy & Stage (`Azath v17.3 KFX`)
- Copied Round 153 `FolioTheme.java` (`StoneBottomBarView` non-clickable touch-passthrough overlay + `setActiveBottomTab` / `syncStoneBottomBar`), `FolioMoreView.java` (`v17.3 KFX`), and `OpenCatalogView.java` verbatim into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/`.
- Updated `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` to stage `07_APK_EPUB_INGESTION_MOD/release/azath_v17.3_kfx/base.apk`.
- Recompiled DEX cleanly (`classes9.dex`: `478,760` bytes), built/signed APK, deployed to Galaxy M30 (`32001776e8e82623`), and staged release:
  - **Path**: `07_APK_EPUB_INGESTION_MOD/release/azath_v17.3_kfx/base.apk`
  - **Size**: `138,836,071` bytes
  - **SHA256**: `737e130b42e5b51c67be31e31301f633b99cfb5ef8af703f3fc5316c0674d216`

## 3. Task B: Hardware Verification (`verify_r153_azath_home_limestone_complete.png`)
- **`verify_r153_azath_home_limestone_complete.png` (`883,730` bytes)**: Toggled `Granite -> Limestone` inside Control Center (`v17.3 KFX`) with zero force-stop, tapped **Home** straight through `StoneBottomBarView`, and verified:
  - Full-width warm **Limestone `#EFEAE0`** bottom navigation bar with `#CBC3B3` top hairline seamlessly connecting to the Limestone gesture bar.
  - Crisp `#2D3138` active **Home** icon + Inter label, `#73757B` **Library** & **More** icons + labels, and clean left cutout preserving the floating *Aristotle* mini book cover.
  - `Azath` 22sp header at `bounds="[42,127][527,202]"`.
- **Passthrough Return & Granite Restore**: Tapped **More** (`[944, 2180]`) back through `StoneBottomBarView` and toggled `Limestone -> Granite` cleanly (`v17.3 KFX` badge `[853,129][1038,197]`, `Control Center` `[42,125][645,200]`, `Granite` pill `[887,222][1038,286]`).
- **Logcat Crash Check**: `ZERO_CRASHES_AND_NO_SIGSYS` in `AndroidRuntime:E`, `FATAL:E`, and `DEBUG:*`.

## 4. Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
