# Round 164 Report — Azath v18.3 KFX (`com.azath.reader`)

## 1. Target Hardware & Safety Invariants
- **Device**: Physical Samsung Galaxy M30 (`32001776e8e82623`), TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Active Package**: `com.azath.reader` (`versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-25 19:09:01`).
- **Stock Kindle Safety**: `com.amazon.kindle` (`versionName=8.160.1.0(2.0.124621.0)`) verified **100% UNTOUCHED** (`lastUpdateTime=2026-11-19 21:59:23`).
- **Play Store Folio (`ct.reader`)**: Zero launches, zero inspections, zero references.

---

## 2. Task A — Verbatim Drop-In Build, Deploy, Stage & GitHub Push (`Azath v18.3 KFX`)
- Copied Round 164 `FolioMoreView.java` (`v18.3 KFX`), `AzathLibrarySync.java` (`openImportedBookLive`, zero `makeRestartActivityTask` / zero `Process.killProcess`), and `FolioFolderBrowserDialog.java` (`OPEN BOOK` / `DONE` dialog) verbatim into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/`.
- Updated `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` to stage `azath_v18.3_kfx`.
- Built `classes9.dex` (`497,484` bytes), installed `com.azath.reader` on Galaxy M30, and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v18.3_kfx/base.apk` (`138,856,551` bytes, SHA256 `0ea7caf2f4654f5bda694fc623a3fe70a7323581eadb1976a68774338755fd08`).

---

## 3. Task B — Zero-`SIGSYS` Post-Import `Open Book` -> Live Library Indexing (PASS)
- **Import**: Imported `Blindsight (Peter Watts) (z-library.sk, 1lib.sk, z-lib.sk).epub` (`468 KB`) via `Control Center -> Import EPUBs from Storage -> /sdcard/Books`.
  - `logcat`: `11-25 19:12:18.674 5199 6779 I AzathLibrarySync: Ingested OK: /sdcard/kindle/Blindsight_FOLIO_DB72217519E6.kfx (1163154 B)`
  - `logcat`: `11-25 19:12:18.739 5199 6779 I AzathLibrarySync: reconcileLibrary complete: activeKfx=29 changes=0`
- **Completion Dialog**: Displayed `Imported to Azath Library` with `OPEN BOOK` and `DONE`.
- **Tap `OPEN BOOK`**:
  - Opened straight into `com.azath.reader/com.amazon.kcp.reader.StandAloneBookReaderActivity` (`mCurrentFocus=Window{4c5b59a u0 com.azath.reader/com.amazon.kcp.reader.StandAloneBookReaderActivity}`).
- **Press Back & Switch to Library**:
  - Returned cleanly to `MainActivity`, kept the exact same process (`PID=5199`), and `Blindsight, Peter Watts` appeared **LIVE** as `Most recent book` in the bottom-left floating cover slot (`verify_r164_open_and_library_live.png`).
- **Crash Audit**: **ZERO `SIGSYS`**, **ZERO `FATAL EXCEPTION`**.

---

## 4. Mandatory Cover Architecture Directive (Project Lead)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
