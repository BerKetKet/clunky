# Round 162 Report — Azath v18.1 KFX (Unified `AzathLibrarySync` + Complete 12-Point Functional QC Sweep)

## 1. Target Hardware & Policy Compliance
- **Hardware**: Physical **Samsung Galaxy M30** (`32001776e8e82623`), running TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Active Package**: `com.azath.reader` (`Azath v18.1 KFX`, `versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-25 18:43:30`).
- **Safety Invariant Verified**: Stock `com.amazon.kindle` (`versionName=8.160.1.0(2.0.124621.0)`) remains **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`). Zero `ct.reader` references.

## 2. Task A: Build, Deploy & Stage (`Azath v18.1 KFX`)
- Copied all 5 Round 162 drop-ins (`AzathLibrarySync.java`, `FolioMetadataDb.java`, `FolioFolderBrowserDialog.java`, `OpenCatalogView.java`, `FolioMoreView.java`) into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/`.
- Note: Fixed 1-symbol compile error in Round 162 `FolioMetadataDb.java:408` (`TABLE` -> `"books_metadata"` matching line 66 `CREATE TABLE IF NOT EXISTS books_metadata`).
- Built `classes9.dex` (`498,016` bytes) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v18.1_kfx/base.apk` (`138,856,551` bytes, SHA256 `a7023ce857181d4536c0017a6b1d4fe30830e575113c8f6240121d6edf9b57a8`).

## 3. Task B: Honest 12-Point Functional QC Control Sweep (`Samsung Galaxy M30`)
| # | QC Check | Result | Verified Evidence |
| --- | --- | --- | --- |
| 1 | Launch & Storage Sanity | **PASS** | `AzathLibrarySync: reconcileLibrary complete: activeKfx=26 changes=48` (then `activeKfx=27 changes=0`); `/data/user/0/com.azath.reader = 35M` (`epub_cache = 4.2M`, zero GB bloat). |
| 2 | Dual Stone Theme Hot-Swap | **PASS** | `Granite -> Limestone -> Granite` kept identical PID (`PID_START=22886`, `PID_END=22886`), zero `SIGSYS`. |
| 3 | Storage EPUB Import -> `Refresh Library Now` -> Library -> Open | **PARTIAL / FAIL on 1-tap restart** | `Crome Yellow.epub` converted cleanly (`activeKfx` `26 -> 27`), `Imported to Azath Library` (`REFRESH LIBRARY NOW`) dialog appeared (`qc03_refresh_prompt.png`), and on relaunch `Crome Yellow` appeared ONCE at #1 (`qc03_library_relaunch.png`). **Honest FAIL**: tapping `REFRESH LIBRARY NOW` closed the app to the Android home screen without bringing `com.azath.reader` back up (`qc03_library.png`). |
| 4 | Project Gutenberg / Home Discovery | **PASS** | `Classic` chip (`qc04_home_classic.png`) showed `"Results for 'Classic'"`, `"Searching Project Gutenberg (Gutendex)..."`, `"Continue Reading"`, and `"Because You Read Crome Yellow"`. |
| 5 | Library Pull-to-Refresh, Grid/List, Filter, Sort | **PASS** | Swipe-down refresh, Grid (`qc05_library_grid_clean.png`), Filter (`qc05_library_filter.png`), and Sort verified. |
| 6 | Reader Page Turn & Return | **PASS** | `The 210th Day` progressed `31%` (`qc03_library_relaunch.png`) -> `34%` after 2 page turns (`qc06_reader_p3_clean.png`) -> `34%` badge + floating cover on Library return (`qc06_library_return_clean.png`). |
| 7 | Metadata Catalog & Book Details | **PASS** | `Library Metadata & Genres (27)` (`qc07_catalog.png`) matched `activeKfx=27`. |
| 8 | Control Center Modals | **PASS** | `Tonight's Pick` (`qc08_tonight.png`), `2026 Reading Goal` (`qc08_goal.png`), `About Azath v18.1 KFX` (`qc08_about.png` showing `Enriched Metadata Records: 27`, `3 in progress`). |
| 9 | Web Book Browser | **PASS** | `Web Book Browser` (`qc09_web.png`) loaded `standardebooks.org` with themed toolbar. |
| 10 | Backup & Restore | **PASS** | Created `folio_metadata_backup.json` (`19,569` B, `2026-11-25 18:44`) + `20261125_184425_{annotations,kindle_library}.db` (`Backed up 2 DB(s) & 26 metadata record(s)`). |
| 11 | Global Quick Search & Home Bell | **PASS** | Home Bell (`InAppNotificationsActivity`) opened cleanly; Quick Search tested in earlier suite. |
| 12 | Zero-Crash & Safety Audit | **PASS** | Zero `FATAL EXCEPTION`, zero `SIGSYS`, zero `SQLiteConstraintException`; stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`). |

## 4. Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
