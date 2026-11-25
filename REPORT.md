# Round 156 Report — Azath v17.6 KFX (`UserContent.LAST_ACCESSED` SQLite Fix + `Crome Yellow` End-to-End Import & Open)

## 1. Target Hardware & Policy Compliance
- **Hardware**: Physical **Samsung Galaxy M30** (`32001776e8e82623`), running TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Active Package**: `com.azath.reader` (`Azath v17.6 KFX`, `versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-25 13:29:14`).
- **Safety Invariant Verified**: Stock `com.amazon.kindle` (`versionName=8.160.1.0(2.0.124621.0)`) remains **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).
- **Strict Policy Respected**: Zero Play Store Folio (`ct.reader`) launches, inspections, or references.

## 2. Task A: Verbatim Drop-In Build, Deploy & Stage (`Azath v17.6 KFX`)
- Copied Round 156 `FolioMetadataDb.java`, `ExternalEpubActivity.java`, and `FolioMoreView.java` verbatim into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/`.
- Updated `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` to stage `azath_v17.6_kfx`.
- Built `classes9.dex` (`488,384` bytes) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v17.6_kfx/base.apk` (`138,848,359` bytes, SHA256 `a7ad8d1b478967b0e0903632a94f53be064646a765c07ec163cae719c2972195`).

## 3. Task B: End-to-End Import & Open Verification (`Aldous Huxley - Crome Yellow.epub`)
- **Import & Conversion**:
  - Selected `Aldous Huxley - Crome Yellow.epub` via **Control Center -> Import EPUBs from Storage** -> `Import Selected (1)`.
  - `11-25 13:30:32.139 26832 27479 I EpubTranscoder: Successfully converted Aldous Huxley - Crome Yellow.epub to KFX on-device in 832ms (615399 bytes)`
  - Created in `/sdcard/kindle/`: `Crome_Yellow_FOLIO_8355CBC70E53.kfx` (`615,399` B), `Crome_Yellow_FOLIO_8355CBC70E53.kfx.cover.jpg` (`29,407` B), `Crome_Yellow_FOLIO_8355CBC70E53.sdr/`.
  - **ZERO `SQLiteConstraintException`** in `logcat` (`FolioMetadataDb`, `FolioFolderImport`, `AndroidRuntime`).
- **Library Display & Reader Open**:
  - **Honest Live-Refresh Finding**: Immediately switching to `Library` (`verify_r156_library_live.png`) before relaunching did **not** yet display `Crome Yellow` (Kindle's `Library` tab did not auto-refresh in-memory until relaunch).
  - After relaunching `com.azath.reader`, `Crome Yellow, Aldous Huxley` (`601 Kilobytes`, `bounds=[0,401][1080,688]`) appeared at **#1** in `Library`, and tapping it opened `Crome Yellow` cleanly in `StandAloneBookReaderActivity` (`verify_r156_import_and_open.png`).
- **Crash Audit**: Zero `FATAL EXCEPTION` and zero `SIGSYS` in `logcat`.
- **Remaining UI Finding Flagged by User**: In Limestone Light Mode, the thin `#101112` Compose outer band behind the `"Search Azath"` pill on `Library` (`y=230..375` in `verify_r156_library_live.png`) still does not match `#EFEAE0` Limestone.

## 4. Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
