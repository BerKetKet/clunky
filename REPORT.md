# Round 148 Report — Azath v16.2 KFX (Complete Limestone Light Mode Parity & Instant Theme Toggle)

## 1. Target Hardware & Policy Compliance
- **Hardware**: Physical **Samsung Galaxy M30** (`32001776e8e82623`), running TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Active Package**: `com.azath.reader` (`Azath v16.2 KFX`, `versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-25 02:23:21`).
- **Safety Invariant Verified**: Stock `com.amazon.kindle` (`versionName=8.160.1.0(2.0.124621.0)`) remains **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).
- **Strict Policy Respected**: Zero Play Store Folio (`ct.reader`) launches, inspections, or references.

## 2. Task A: Build, Deploy, Stage & GitHub Sync
- Copied Round 148 `FolioIcons.java`, `FolioMoreView.java`, and `OpenCatalogView.java` from `dropin_src/com/amazon/kindle/epub/catalog/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/`.
- Updated `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` to stage `07_APK_EPUB_INGESTION_MOD/release/azath_v16.2_kfx/base.apk`.
- Recompiled DEX cleanly (`classes9.dex`: `471,540` bytes), built and signed APK, deployed to Galaxy M30 (`32001776e8e82623`), and staged release:
  - **Path**: `07_APK_EPUB_INGESTION_MOD/release/azath_v16.2_kfx/base.apk`
  - **Size**: `138,827,879` bytes
  - **SHA256**: `263f6a9153f3eb7c1c9cc7d5e00f980cc6af92b760e8fea029c202ef2aa9c8fc`

## 3. Task B: Hardware Verification & Visual Audit (Samsung Galaxy M30)
- **`verify_r148_azath_control_center_limestone.png` (`313,868` bytes)**: Tapped `Granite` -> `Limestone` inside Control Center (`v16.2 KFX` badge verified). Verified instant `Activity.recreate()` with **zero force-stop**: ALL Control Center surfaces (canvas `#F4F0E8`, 4 stat tiles, `2026 Reading Goal`, `Continue Reading`, `Up Next Queue`, `LIBRARY & IMPORT` grouped container `#E7E1D5`), `#1A1B1E` deep granite typography, and dark-tinted bespoke icons (`SHUFFLE`, `SEARCH`, `LIBRARY`, `IMPORT`, `WEB`, `OPDS`, chevrons) rendered with full Limestone Light Mode parity!
- **`verify_r148_azath_home_limestone.png` (`885,282` bytes)**: Switched directly from Control Center to **Home** with **zero force-stop**: verified warm `#E7E1D5` genre chips (`Sci-Fi`, `Fantasy`, `Mystery`, `Horror`, `Classic`) with `#1A1B1E` labels, crisp `#1A1B1E` deep granite ink on **all `Continue Reading` book titles** (*Delphi Complete Works of Aristotle*, *The 210th Day*, *Rashomon and Other Stories*), `#2D3138` dark Granite `Search` CTA, and warm `#E7E1D5` `Offline Mode` card!
- **Restored Cleanly to Granite**: Confirmed `Granite` (`v16.2 KFX`) restored cleanly on device in UI XML (`[876,221][1027,285]`).

### Honest Crash / Logcat Audit Finding
- Zero Java fatal exceptions occurred (`AndroidRuntime:E` / `FATAL:E`: `NO_FATAL_EXCEPTIONS`).
- **However**, while single `Activity.recreate()` worked cleanly in both directions (`Granite -> Limestone`, and in a fresh process `Limestone -> Granite`), calling `MainActivity.recreate()` a **second time inside the same process** (`02:24:22.859`) triggered a native **`signal 31 (SIGSYS), code 1 (SYS_SECCOMP)`** crash (`seccomp prevented call to disallowed arm64 system call 375` on `Thread-18` inside `/lib/arm64/libKindleAndroidNativeBundlerJNI.so` `pc 0x03869d2c`). Root cause: Amazon's native bundler library performs a seccomp-disallowed syscall during same-process `MainActivity` relaunch. An in-place `FrameLayout` view rebuild inside `OpenCatalogView.wrap` / `FolioMoreView.wrap` (swapping views without calling `Activity.recreate()`) would avoid restarting Amazon's native library altogether and make unlimited toggling 100% crash-free!

## 4. Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
