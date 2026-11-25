# Round 155 Report — Azath v17.5 KFX (Real EPUB Import to `/sdcard/kindle/` & Library List View Diagnostic)

## 1. Target Hardware & Policy Compliance
- **Hardware**: Physical **Samsung Galaxy M30** (`32001776e8e82623`), running TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Active Package**: `com.azath.reader` (`Azath v17.5 KFX`, `versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-25 12:29:29`).
- **Safety Invariant Verified**: Stock `com.amazon.kindle` (`versionName=8.160.1.0(2.0.124621.0)`) remains **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).
- **Strict Policy Respected**: Zero Play Store Folio (`ct.reader`) launches, inspections, or references.

## 2. Task A: Verbatim Drop-In Build, Deploy & Stage (`Azath v17.5 KFX`)
- Copied Round 155 `FolioFolderBrowserDialog.java`, `ExternalEpubActivity.java`, `FolioWebBrowserDialog.java`, and `FolioMoreView.java` verbatim into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/`.
- Updated `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` to stage `azath_v17.5_kfx`.
- Built `classes9.dex` (`487,308` bytes) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v17.5_kfx/base.apk` (`138,844,263` bytes, SHA256 `559868e3d561c32fbc2d4c9e4e9d4e5af0cb9d390e85665f2b408d960064dc50`).

## 3. Task B: Real End-to-End EPUB Import & Library Titles Verification
1. **Real EPUB Import (`Aldous Huxley - Brave New World.epub`)**:
   - **Pre-import `/sdcard/kindle/` `.kfx` count**: `18`.
   - **Action**: Opened **Control Center -> Import EPUBs from Storage**, selected `Aldous Huxley - Brave New World.epub`, tapped `Import Selected`.
   - **Conversion & `/sdcard/kindle/` Output**:
     - `11-25 12:30:55.340 I EpubTranscoder: Successfully converted Aldous Huxley - Brave New World.epub to KFX on-device in 15441ms (988906 bytes)`
     - Created in `/sdcard/kindle/`: `Brave_New_World_FOLIO_621B293A3B8F.kfx` (`988,906` B), `Brave_New_World_FOLIO_621B293A3B8F.kfx.cover.jpg` (`68,883` B), `Brave_New_World_FOLIO_621B293A3B8F.sdr/`.
   - **Honest `FolioMetadataDb` Error Captured in `logcat`**:
     - `11-25 12:31:02.541 15332 16022 E FolioMetadataDb: Failed to register book in kindle_library.db`
     - `android.database.sqlite.SQLiteConstraintException: NOT NULL constraint failed: UserContent.LAST_ACCESSED (code 1299 SQLITE_CONSTRAINT_NOTNULL)` at `FolioMetadataDb.registerInKindleLibraryDb(FolioMetadataDb.java:297)`.
   - **Library Result**: Despite the `FolioMetadataDb` `LAST_ACCESSED` SQLite constraint error, Kindle's native `/sdcard/kindle/` scanner indexed `Brave_New_World_FOLIO_621B293A3B8F.kfx` on relaunch, and **`Brave New World` appeared at position #1 in Library Grid View (`verify_r155_library_after_import.png`)** with its cover.
2. **Library List View Diagnostic (Granite Dark & Limestone Light)**:
   - `verify_r155_library_list_granite.png` and `verify_r155_library_list_limestone.png` + `verify_r155_library_list_limestone.xml`:
   - All book titles, authors, `NEW` badges, and file sizes render crisply in BOTH Granite and Limestone (`Brave New World`, `ALDOUS HUXLEY`, `NEW`, `965.7 KB` verified in XML and screenshots).
3. **Logcat Crash Audit**:
   - Note: `11-25 12:32:01.135 16219 16466 F libc : Fatal signal 31 (SIGSYS), code 1 (SYS_SECCOMP)` observed in `Thread-20` during the earlier `Herodotus` open/force-stop sequence; zero Java `FATAL EXCEPTION`.

## 4. Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
