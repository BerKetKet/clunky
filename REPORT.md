# Round 149 Report — Azath v16.3 KFX (Crash-Free In-Place Stone Theme Hot-Swap & Final Stone Edition Milestone)

## 1. Target Hardware & Policy Compliance
- **Hardware**: Physical **Samsung Galaxy M30** (`32001776e8e82623`), running TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Active Package**: `com.azath.reader` (`Azath v16.3 KFX`, `versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-25 02:31:10`).
- **Safety Invariant Verified**: Stock `com.amazon.kindle` (`versionName=8.160.1.0(2.0.124621.0)`) remains **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).
- **Strict Policy Respected**: Zero Play Store Folio (`ct.reader`) launches, inspections, or references.

## 2. Task A: Build, Deploy, Stage & GitHub Sync
- Copied Round 149 `FolioTheme.java` (`THEME_LISTENERS` + `registerThemeListener` + `notifyThemeChanged`), `FolioMoreView.java` (`v16.3 KFX` + `FrameLayout` host rebuilder), and `OpenCatalogView.java` (`FrameLayout` host rebuilder) from `dropin_src/com/amazon/kindle/epub/catalog/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/`.
- Updated `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` to stage `07_APK_EPUB_INGESTION_MOD/release/azath_v16.3_kfx/base.apk`.
- Recompiled DEX cleanly (`classes9.dex`: `473,324` bytes), built and signed APK, deployed to Galaxy M30 (`32001776e8e82623`), and staged release:
  - **Path**: `07_APK_EPUB_INGESTION_MOD/release/azath_v16.3_kfx/base.apk`
  - **Size**: `138,831,975` bytes
  - **SHA256**: `a09e804f949b6462830b77af4309d44d4a817f0479f2d20faf3a970c12c88cec`

## 3. Task B: Rapid 4-Toggle Stress Test & Hardware Verification (Samsung Galaxy M30)
- **Single-Process Continuity Verified**: Launched `com.azath.reader` (`PID_START=17643`) cleanly and executed **4 consecutive toggles** (`Granite -> Limestone -> Granite -> Limestone -> Granite`) inside Control Center with zero force-stop, switching between Control Center (`More`) and `Home` after toggling to Limestone and again after returning to Granite.
- **Process Survived Uninterrupted**: Confirmed `PID_END=17643` (identical process ID before and after all 4 toggles and tab switches — zero `Activity.recreate()` teardown!).
- **Logcat Crash & Seccomp Audit**: Checked `AndroidRuntime:E`, `FATAL:E`, and `DEBUG:*` -> **`ZERO_CRASHES_AND_NO_SIGSYS`** (zero Java exceptions, zero `SIGSYS` / `SYS_SECCOMP` native tombstones).
- **`verify_r149_azath_hotswap_granite.png` (`313,720` bytes)**: Captured Control Center `v16.3 KFX` cleanly back in Granite (`[876,221][1027,285]`) immediately after the 4-toggle stress test; all surfaces, borders, EB Garamond headings, and untinted `azath_icons` rendered sharply.

## 4. Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
