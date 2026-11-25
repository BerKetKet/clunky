# Round 159 Report — Azath v17.9 KFX (Duplicate Row Cleanup, 1-Tap `Refresh Library Now` & Spotless Limestone Library)

## 1. Target Hardware & Policy Compliance
- **Hardware**: Physical **Samsung Galaxy M30** (`32001776e8e82623`), running TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Active Package**: `com.azath.reader` (`Azath v17.9 KFX`, `versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-25 14:54:05`).
- **Safety Invariant Verified**: Stock `com.amazon.kindle` (`versionName=8.160.1.0(2.0.124621.0)`) remains **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).
- **Strict Policy Respected**: Zero Play Store Folio (`ct.reader`) launches, inspections, or references.

## 2. Task A: Verbatim Drop-In Build, Deploy, Stage & GitHub Push (`Azath v17.9 KFX`)
- Copied Round 159 `FolioMetadataDb.java`, `FolioFolderBrowserDialog.java`, and `FolioMoreView.java` verbatim into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/`.
- Updated `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` to stage `azath_v17.9_kfx`.
- Built `classes9.dex` (`493,504` bytes) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v17.9_kfx/base.apk` (`138,852,455` bytes, SHA256 `767b35469ae502321a3c54b0f4e9bb14f810583fb1e8385a38ecccc10b626410`).

## 3. Task B: Hardware Verification (`verify_r159_library_deduped_limestone.png`)
- **Zero Duplicate Library Rows**: Opened Control Center (`v17.9 KFX`) triggering `FolioMetadataDb.cleanupLegacyDuplicateRows`, relaunched once, and verified `verify_r159_library_deduped_limestone.png` + `/tmp/r159/library_deduped.xml`:
  - `The 50th Law`
  - `Eyeless In Gaza`
  - `Island`
  - `Crome Yellow`
  - `Complete Works of Herodotus (Illustrated) (Delphi Ancient Classics)`
  - `Brave New World`
  All appear **ONCE** (zero duplicates!) in spotless **Limestone Light Mode** (`#EFEAE0` header, `#EFEAE0` Search outer band, `#EFEAE0` full-width bottom bar).
- **Additional EPUB Import (`Aldous Huxley - Moksha.epub`)**: Converted cleanly into `/sdcard/kindle/Moksha_FOLIO_72C0BAE5934F.kfx` (`1,140,502` B) + `.kfx.cover.jpg` (`80,039` B).
- **Crash Audit**: Zero `FATAL EXCEPTION` and zero `SIGSYS` in `logcat`.

## 4. Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
