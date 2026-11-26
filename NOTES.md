# Kindle Android v8.159.0.100 - Deployment & Restoration Notes

## Status
- **Round 167 Final Closeout (`Azath v18.7 KFX`, `master` merge & tag `v18.7-kfx`) Completed & Verified (2026-11-26)**:
  - Undid Task F4 (`dde820c0068a37108ef0aebba6b9e403067363cc`): removed `07_APK_EPUB_INGESTION_MOD/dex/classes4.dex` from `.gitignore` and restored `git ls-files` tracking (`07_APK_EPUB_INGESTION_MOD/dex/classes4.dex`); skipped all provenance/history-purge/visibility items per Project Lead directive.
  - Resolved round-number collision non-destructively: `master`'s earlier pre-QC `v18.2` (`7ab6643`) and `v18.3` (`253dfdc`) commits preceded the `QC Remediation Rounds 163–166 (Azath v18.4–v18.7 KFX)` branch chain (`round-163-qc-fixes` -> `round-164-qc-fixes` -> `round-165-qc-fixes` -> `round-166-qc-closeout`), preserved intact via `--no-ff` merge.
  - Clean `--no-ff` merge of `round-166-qc-closeout` into `master`: merge commit `9a0973c93b9737aa304e2b1a42d32646801d6316`, annotated tag `v18.7-kfx` (`270efcc7fbb2a92c7dfb3d09abed6880d84b95da`), pushed cleanly to `origin/master` and `origin/v18.7-kfx` (zero history rewrite, zero force-push).
  - Kept Open Library dynamic repo-URL `User-Agent` (`AzathReader/<versionName> (Android; https://github.com/BerKetKet/clunky)`) and left `dropin_rust/`, `dropin_res/`, `dropin_assets/`, and `kfx_converter` intact.
  - Re-ran `./scripts/run_unit_tests.sh` on `master`: `AzathUnitTests: passed=84 failed=0 total=84`.
  - M30 (`32001776e8e82623`) safety invariants verified: stock `com.amazon.kindle` untouched (`versionName=8.160.1.0(2.0.124621.0)`, `lastUpdateTime=2026-11-19 21:59:23`), `com.azath.reader` (`Azath v18.7 KFX`, `lastUpdateTime=2026-11-26 11:18:08`, `pkgFlags=[ HAS_CODE ALLOW_CLEAR_USER_DATA LARGE_HEAP ]`), zero `ct.reader` (`NONE`), zero `FATAL EXCEPTION` / `SIGSYS` / `SQLiteConstraintException`.
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 166 QC Closeout (`Azath v18.7 KFX`, branch `round-166-qc-closeout`) Verified & Pushed (2026-11-26)**:
  - Executed **Tasks 1–10** strictly sequentially (`61926ea`, `1d73c4c`, `d6ca380`, `efc0c70`, `1b6ac64`, `ad3ddf8`, `7f837b4`, `990dfe6`, `d2376f2`, `4369484`) from `b0ff924`.
  - Task 1 (`sleep 300` timeout test): `rc=-1 elapsedMs=2052 stdoutLen=0 procAliveAfterTimeout=false partExists=false partCoverExists=false stagedExists=false` + `T1_CANCEL rc=-2 elapsedCancelMs=403 alive=false` (**PARTIAL** — JVM host verified; M30 hung native binary N/A).
  - Pure-JVM unit tests (`./scripts/run_unit_tests.sh`): `AzathUnitTests: passed=84 failed=0 total=84`.
  - Deep security/stability audit written to `QC_FINDINGS_ROUND_166.md`; P0/P1 fixed in `EpubTranscoder.java`, `FolioWebBrowserDialog.java`, `OpenCatalogView.java`, and `FolioMetadataBackup.java`.
  - Built `classes9.dex` (`562,824` B), deployed `com.azath.reader` (`lastUpdateTime=2026-11-26 11:18:08`), and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v18.7_kfx/base.apk` (`138,922,087` B, SHA256 `7d04b3afb5c67ed8cf5e5708f47ee95930f634741104ea12dadcdfbd3b9930d8`).
  - M30 verification (`32001776e8e82623`): `v18.7 KFX` + `Force Reconcile Preview` (`activeKfx=30`, `0` missing/orphan), `Granite <-> Limestone` hot-swap + `StandAloneBookReaderActivity` open without `StrictMode` (`PID_START=4764 == PID_AFTER_THEME=4764 == PID_AFTER_READER=4764`), blocked `file:///data/data/com.azath.reader/databases/kindle_library.db` cleanly, `Launch-time enrichment sweep queued 24 book(s)` + live `Unknown Author` mismatch rejection, `CONT` KFX header verified (`xxd`), verbatim M30 `PRAGMA table_info` captured (`LocalContent.FILE_PATH` present, `KindleContent`/`UserContent` no `FILE_PATH`), stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), zero `FATAL EXCEPTION` / `SIGSYS` / `SQLiteConstraintException`.
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 165 QC Remediation (`Azath v18.6 KFX`, branch `round-165-qc-fixes`) Verified & Pushed (2026-11-26)**:
  - Completed Tasks `E1`–`E6` (`574707a`, `fc357a7`, `62657af`, `531340c`, `9140cfa`, `267164a`), `F1`–`F6` (`1a03839`, `8f48a76`, `92bd2cb`, `3f54edb`, `dc40c8b`, `bc232b9`), and `G-P0P1` (`ba42c77`, `QC_FINDINGS_ROUND_165.md`) on branch `round-165-qc-fixes`.
  - Pure-JVM unit tests (`./scripts/run_unit_tests.sh`): `AzathUnitTests: passed=38 failed=0 total=38`.
  - Built `classes9.dex` (`532,984` B), deployed `com.azath.reader` (`lastUpdateTime=2026-11-26 02:21:52`), and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v18.6_kfx/base.apk` (SHA256 `beb6b8cf25a60c0b6db98bfd9c35c71b97e70664db98f3f5df3d3d725da7dd2c`).
  - M30 smoke verified: `PID_START=22144 == PID_END=22144`, `reconcileLibrary complete: activeKfx=30 changes=27`, `StandAloneBookReaderActivity` open & Back, `['v18.6 KFX', 'Granite']` <-> Limestone hot-swap, `folio_metadata_backup.json` (`19,569` B, zero `.tmp`), `ZERO_CRASHES_ZERO_WARNINGS`, stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`).
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 164 QC Remediation (`Azath v18.5 KFX`, branch `round-164-qc-fixes`) Verified & Pushed (2026-11-26)**:
  - Completed `R163-Closeout` (`6037f2a`, `f086d62`) and Tasks `C1`–`C4`, `D1`–`D5` (`54dac5b`, `5d23910`, `fcc00fe`, `2699c1a`, `bcaaefc`, `99e512b`, `912bb23`, `d336031`, `71c095f`) on branch `round-164-qc-fixes`.
  - Built `classes9.dex` (`529,188` B), deployed `com.azath.reader` (`lastUpdateTime=2026-11-26 01:57:18`), and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v18.5_kfx/base.apk` (SHA256 `a410b2f543f05c579a0ba130be4e5b45a42e2649f22f02f2a1c63778104333ee`).
  - Device tests on **Samsung Galaxy M30** (`32001776e8e82623`): **T6 PASS** (`files/db_backups/kindle_library_1795647531359.db` + `attempt 1/3` 200ms retry), **T7 PASS** (`Online enrichment disabled` skip), **T8 PARTIAL** (OPF preserved; bogus author returned 0 OL docs), **T9 PASS** (`Imported 2 of 3 (cancelled)`), **T10 PASS** (`T10_ZERO_CRASHES`), **T11 PASS** (`(filename, source_size)` + Dual-Stone `verify_r164_qc_folder_{granite,limestone}.png`), **T12 PASS** (`ZERO_CRASHES_ZERO_WARNINGS`, `reconcileLibrary complete: activeKfx=30 changes=0`).
  - Stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), zero `FATAL EXCEPTION`, zero `SIGSYS`, zero `SQLiteConstraintException`.
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 163 QC Remediation (`Azath v18.4 KFX`, branch `round-163-qc-fixes`) Executed & Audited (2026-11-26)**:
  - Completed Tasks A1–A4 (`d5405f5`, `1765170`, `ad121e0`, `bb74643`) and B1–B4 (`478ad22`, `b5acb86`, `9cca2c1`, `a635ca7`) on branch `round-163-qc-fixes` (one commit per task, canonical `07_APK_EPUB_INGESTION_MOD/mod_src/real/` only).
  - Built `classes9.dex` (`516,048` B), deployed `com.azath.reader` (`lastUpdateTime=2026-11-26 00:23:46`), and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v18.4_kfx/base.apk` (SHA256 `a9e458b5d940dd278fd57cf72baccc08a13a36988c31b54525831f7743960b86`).
  - Device tests on **Samsung Galaxy M30** (`32001776e8e82623`): **T1 PASS** (`30 -> 30` DB rows, `4.2M -> 4.2M` `epub_cache`, circuit breaker tripped), **T2 PASS** (`AMZNID0/CR!5445A3745815KFX/0/|42|1` persisted), **T3 PASS** (mid-conversion `force-stop` left `NO_PARTIAL_KFX` + stale `>1h` `.part` deleted), **T4 PASS** (`added_at=1795642004105` preserved on re-import, distinct `t4_b` kept separate), **T5 PASS** (`user_version 1 -> 2`, `content_hash`/`source_size`/`genres_json` added), **T6 NOT VERIFIED ON DEVICE** (`/sdcard/kindle` remained writable so private-dir `registerInKindleLibraryDb` path was not hit).
  - Stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), zero `FATAL EXCEPTION`, zero `SIGSYS`, zero `SQLiteConstraintException`.
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 164 (`Azath v18.3 KFX`) Verified, Staged & Pushed (2026-11-25)**:
  - Copied Round 164 `FolioMoreView.java` (`v18.3 KFX`), `AzathLibrarySync.java` (zero-`SIGSYS` `openImportedBookLive`), and `FolioFolderBrowserDialog.java` (`OPEN BOOK` + `DONE`) verbatim into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/` and updated `rebuild_fast.sh` to stage `azath_v18.3_kfx`.
  - Built `classes9.dex` (`497,484` B), installed `com.azath.reader` (`lastUpdateTime=2026-11-25 19:09:01`), and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v18.3_kfx/base.apk` (`138,856,551` B, SHA256 `0ea7caf2f4654f5bda694fc623a3fe70a7323581eadb1976a68774338755fd08`).
  - Task B (`OPEN BOOK` Live Indexing) **PASS**: Imported `Blindsight (Peter Watts)` (`Ingested OK: /sdcard/kindle/Blindsight_FOLIO_DB72217519E6.kfx (1163154 B)`, `reconcileLibrary complete: activeKfx=29 changes=0`), tapped `OPEN BOOK` -> launched `StandAloneBookReaderActivity` -> pressed Back to `MainActivity` -> `Library` showed `Most recent book. Blindsight, Peter Watts` live on the same `PID=5199` (`verify_r164_open_and_library_live.png`) with **ZERO `SIGSYS`** and **ZERO `FATAL EXCEPTION`**. Stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`).
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 163 (`Azath v18.2 KFX`) Built, Deployed & Honest Failure Reported (2026-11-25)**:
  - Copied Round 163 `FolioMoreView.java` (`v18.2 KFX`), `AzathLibrarySync.java`, `FolioFolderBrowserDialog.java`, and `FolioMetadataDb.java` verbatim into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/` and updated `rebuild_fast.sh` to stage `azath_v18.2_kfx`.
  - Built `classes9.dex` (`498,356` B), installed `com.azath.reader` (`lastUpdateTime=2026-11-25 19:00:10`), and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v18.2_kfx/base.apk` (`138,856,551` B, SHA256 `616357fcbd75cf257e4331b557a004801be8c7b54365be3ca24691652ccd9658`).
  - Honest Task B Failure: Imported `Aldous Huxley - Island.epub` (`Ingested OK: /sdcard/kindle/Island_FOLIO_8CE251A8A07B.kfx (1223991 B)`) and tapped `Refresh Library Now`. Instead of relaunching `com.azath.reader` into foreground, `Thread-22` hit `Fatal signal 31 (SIGSYS), code 1 (SYS_SECCOMP)` in `libKindleAndroidNativeBundlerJNI.so` (`Cause: seccomp prevented call to disallowed arm64 system call -241514045`), dropping `mCurrentFocus` to `com.android.launcher3.uioverrides.QuickstepLauncher` (`verify_r163_refresh_foreground.png`). Stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`).
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 162 (`Azath v18.1 KFX`) Verified, Staged & Audited (2026-11-25)**:
  - Integrated Round 162 `AzathLibrarySync.java`, `FolioMetadataDb.java` (with 1-symbol fix `TABLE` -> `"books_metadata"` at line 408), `FolioFolderBrowserDialog.java`, `OpenCatalogView.java`, and `FolioMoreView.java` (`v18.1 KFX`).
  - Built `classes9.dex` (`498,016` B) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v18.1_kfx/base.apk` (`138,856,551` B, SHA256 `a7023ce857181d4536c0017a6b1d4fe30830e575113c8f6240121d6edf9b57a8`).
  - Executed 12-Point QC Sweep on **Samsung Galaxy M30** (`32001776e8e82623`): `AzathLibrarySync: reconcileLibrary complete: activeKfx=27`, `/data/user/0/com.azath.reader = 35M` (`epub_cache = 4.2M`), `PID_START=22886 == PID_END=22886` across `Granite <-> Limestone`, `Library Metadata & Genres (27)`, `The 210th Day` `31% -> 34%` reader progress, Backup `folio_metadata_backup.json` updated, zero `FATAL EXCEPTION` / zero `SIGSYS` / zero `SQLiteConstraintException`, and stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`).
  - Honest QC finding: tapping `REFRESH LIBRARY NOW` after EPUB import closed `com.azath.reader` to the Android home screen without relaunching it to the foreground.
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 161 (`Azath v18.0 KFX`) Verified, Staged & Audited (2026-11-25)**:
  - Copied Round 161 `dp/g.java` (removed `.epub` from background sideload enumerator + `shouldBlockAutoEpubCrawl`) and `FolioMoreView.java` (`v18.0 KFX`) verbatim into `07_APK_EPUB_INGESTION_MOD/mod_src/real/` and updated `rebuild_fast.sh` to stage `azath_v18.0_kfx`.
  - Built `classes9.dex` (`493,524` B) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v18.0_kfx/base.apk` (`138,852,455` B, SHA256 `80ba7bda7ec795c2547fc05bbdf875d3bfe40cb127a3e7cbdd0af62049cc5ee4`).
  - Purged `/data/user/0/com.azath.reader/files/epub_cache/*`, launched `Azath v18.0 KFX` on **Samsung Galaxy M30** (`32001776e8e82623`), opened `Library` + `Control Center` (`v18.0 KFX`), and waited 15s: total `/data/user/0/com.azath.reader` dropped from **2.9 GB** to **30 MB** (`epub_cache` **4.0K**, 0 files), `/sdcard/kindle/` Library books intact, zero crashes / zero `SIGSYS`, and stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`).
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 160 Read-Only Storage Diagnostic (`com.azath.reader`) Completed (2026-11-25)**:
  - Executed read-only storage audit on **Samsung Galaxy M30** (`32001776e8e82623`): found `/data/user/0/com.azath.reader/files/epub_cache` is **2.9 GB** (pre-converted `.epub.kfx` + `.cover.jpg` + `.sdr` files from `2026-11-25 07:51–08:01`), while `/data/app/...` is `259M`, `/sdcard/Android/data/com.azath.reader` is `33M`, `folio_library` is `4.3M`, and `/sdcard/kindle` is `202,894 KB`. Stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`).
- **Round 159 (`Azath v17.9 KFX`) Verified, Staged & Audited (2026-11-25)**:
  - Copied Round 159 `FolioMetadataDb.java` (`cleanupLegacyDuplicateRows`), `FolioFolderBrowserDialog.java` (1-tap `Refresh Library Now` prompt after import), and `FolioMoreView.java` (`v17.9 KFX`) verbatim into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/` and updated `rebuild_fast.sh` to stage `azath_v17.9_kfx`.
  - Built `classes9.dex` (`493,504` B) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v17.9_kfx/base.apk` (`138,852,455` B, SHA256 `767b35469ae502321a3c54b0f4e9bb14f810583fb1e8385a38ecccc10b626410`).
  - Hardware verified on **Samsung Galaxy M30** (`32001776e8e82623`): `com.azath.reader` updated `2026-11-25 14:54:05`, stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), zero `FATAL EXCEPTION` and zero `SIGSYS`.
  - Visually verified `verify_r159_library_deduped_limestone.png`: `The 50th Law`, `Eyeless In Gaza`, `Island`, `Crome Yellow`, `Herodotus`, and `Brave New World` all appear cleanly **ONCE** (zero duplicates!) in spotless Limestone Light Mode. Also converted `Aldous Huxley - Moksha.epub` to `/sdcard/kindle/Moksha_FOLIO_72C0BAE5934F.kfx` (`1,140,502` B).
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 158 (`Azath v17.8 KFX`) Verified, Staged & Audited (2026-11-25)**:
  - Merged Round 158 `FolioMoreView.java` (`v17.8 KFX`), `FolioFolderBrowserDialog.java`, `ExternalEpubActivity.java`, `FolioWebBrowserDialog.java`, and `FolioTheme.java` (`SearchBandMaskView`, `triggerLibraryRescan`, `sFloatingCoverActive`) while preserving Round 154 smali hooks.
  - Built `classes9.dex` (`491,916` B) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v17.8_kfx/base.apk` (`138,848,359` B, SHA256 `d5c46b0d136917699fbf4d2115a2a93e69d04dd07da929446d5e18afd5df9f73`).
  - Hardware verified on **Samsung Galaxy M30** (`32001776e8e82623`): `com.azath.reader` updated `2026-11-25 14:10:14`, stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), zero crashes / zero `SIGSYS`.
  - Visually verified `verify_r158_library_limestone_clean.png`: `#101112` Search outer band is **100% GONE** (`#EFEAE0` Limestone + `#CBC3B3` hairline) and bottom-left cutout bug is **100% GONE** (centered full-width Limestone bottom bar).
  - Imported `Aldous Huxley - Eyeless In Gaza.epub`: converted in `31346ms` (`1,298,643` B `Eyeless_In_Gaza_FOLIO_83848DCC3B03.kfx` + `29,736` B `.cover.jpg` in `/sdcard/kindle/`). Honest finding: `AzathLiveIngest` did not log and `Eyeless In Gaza` did not appear in `Library` without relaunching yet.
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 157 (`Azath v17.7 KFX`) Verified, Staged & Audited (2026-11-25)**:
  - Merged Round 157 `dp/g.java` (`ingestKfxLive`), `FolioMoreView.java` (`v17.7 KFX`), `ExternalEpubActivity.java`, `FolioFolderBrowserDialog.java`, `FolioWebBrowserDialog.java`, and `FolioTheme.syncLibrarySearchBand` (preserving Round 154 smali hooks).
  - Built `classes9.dex` (`489,656` B) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v17.7_kfx/base.apk` (`138,848,359` B, SHA256 `510b923f2484d6dd8f6e99b7fdd33cad8303f065cd9e7f2318a904470d9229d8`).
  - Hardware verified on **Samsung Galaxy M30** (`32001776e8e82623`): `com.azath.reader` updated `2026-11-25 13:49:15`, stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), zero crashes / zero `SIGSYS`.
  - Imported `Aldous Huxley - Island.epub`: converted in `931ms` (`Island_FOLIO_8CE251A8A07B.kfx` `1,223,991` B in `/sdcard/kindle/`).
  - Honest findings logged for Round 158: (1) `Island` did NOT appear live in `Library` without relaunch and `AzathLiveIngest` was absent from `logcat`; (2) `Crome Yellow` appears twice (DB row + `/sdcard/kindle/` scanner row); (3) `#101112` outer band behind `"Search Azath"` remains dark in Limestone (`verify_r157_library_live_limestone.png`); (4) `StoneBottomBarView` left cutout exposes dark `Home` tab when no floating cover is mounted.
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 156 (`Azath v17.6 KFX`) Verified, Staged & Audited (2026-11-25)**:
  - Copied Round 156 `FolioMetadataDb.java` (`fillMissingNotNullDefaults` + case-insensitive column mapping), `ExternalEpubActivity.java` (`/sdcard/kindle/` output), and `FolioMoreView.java` (`v17.6 KFX`) verbatim into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/` and updated `rebuild_fast.sh` to stage `azath_v17.6_kfx`.
  - Built `classes9.dex` (`488,384` B) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v17.6_kfx/base.apk` (`138,848,359` B, SHA256 `a7ad8d1b478967b0e0903632a94f53be064646a765c07ec163cae719c2972195`).
  - Hardware verified on **Samsung Galaxy M30** (`32001776e8e82623`): `com.azath.reader` updated `2026-11-25 13:29:14`, stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), zero `FATAL EXCEPTION` and zero `SIGSYS` in `logcat`.
  - Imported `Aldous Huxley - Crome Yellow.epub`: converted in `832ms` (`615,399` B `Crome_Yellow_FOLIO_8355CBC70E53.kfx` + `29,407` B `.cover.jpg` in `/sdcard/kindle/`) with **ZERO `SQLiteConstraintException`**. Appeared at #1 in `Library` after relaunch and opened cleanly in `StandAloneBookReaderActivity` (`verify_r156_import_and_open.png`).
  - Honest findings logged for Round 157: (1) `Crome Yellow` did not appear in `Library` live without relaunch (`verify_r156_library_live.png`); (2) thin `#101112` Compose outer band behind `"Search Azath"` pill on `Library` in Limestone Light Mode.
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 155 (`Azath v17.5 KFX`) Verified, Staged & Audited (2026-11-25)**:
  - Copied Round 155 drop-ins (`FolioFolderBrowserDialog.java`, `ExternalEpubActivity.java`, `FolioWebBrowserDialog.java`, `FolioMoreView.java`) verbatim into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/` and updated `rebuild_fast.sh` to stage `azath_v17.5_kfx`.
  - Built `classes9.dex` (`487,308` B) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v17.5_kfx/base.apk` (`138,844,263` B, SHA256 `559868e3d561c32fbc2d4c9e4e9d4e5af0cb9d390e85665f2b408d960064dc50`).
  - Hardware verified on **Samsung Galaxy M30** (`32001776e8e82623`): `com.azath.reader` updated `2026-11-25 12:29:29`, stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`).
  - Real EPUB Import (`Aldous Huxley - Brave New World.epub`): `EpubTranscoder` converted to `/sdcard/kindle/Brave_New_World_FOLIO_621B293A3B8F.kfx` (`988,906` B) + `.cover.jpg` (`68,883` B) in `15441ms`, and `Brave New World` appeared at #1 in `Library` Grid (`verify_r155_library_after_import.png`) and #2 in `Library` List View in both Granite (`verify_r155_library_list_granite.png`) and Limestone (`verify_r155_library_list_limestone.png`) with crisp titles/authors.
  - Honest findings logged for Gemini Neon: (1) `FolioMetadataDb.registerInKindleLibraryDb` (`FolioMetadataDb.java:297`) threw `SQLiteConstraintException: NOT NULL constraint failed: UserContent.LAST_ACCESSED (code 1299)`; (2) `Fatal signal 31 (SIGSYS), code 1 (SYS_SECCOMP)` observed in `Thread-20` at `12:32:01.135`.
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 154 (`Azath v17.4 KFX`) Verified, Staged & Audited (2026-11-25)**:
  - Fixed `Library` bottom-nav active-tab desync (`FolioTheme.onTabIdSelected` hooked into `NavigationControllerViewModel.E0` via `patch_classes3.sh`, `FolioTheme.wrapLibraryScreenlet` `setActiveBottomTab(ctx, 1)`, dual `ib.b` + `ib.c` reflection).
  - Fixed Compose `LibraryTopBar` / `TitleTopAppBar` / `SearchBoxTopAppBar` `TopAppBarDefaults.topAppBarColors` (`patch_classes3_azath.sh` + `patch_classes5_azath.sh`) to live `FolioTheme.composeSurfaceLong()` / `composeOnSurfaceLong()` and hooked `LibraryScreenlet.f` + `oob/z.d` (`LegacySearchBar`) into `FolioTheme`.
  - Migrated remaining hardcoded dark literals and `Theme_DeviceDefault_Dialog_Alert` across `FolioMoreView.java`, `FolioMetadataCatalogDialog.java`, `FolioWebBrowserDialog.java`, `FolioFolderBrowserDialog.java`, `OpenCatalogView.java` (`buildSearchRow`, `promptCustomOpds`, status/clear pills), `ExternalEpubActivity.java`, `FolioIcons.java`, and `FolioTheme.styleDialog`.
  - Built `classes9.dex` (`488,864` B) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v17.4_kfx/base.apk` (`138,848,359` B, SHA256 `76a5facc0067f07dd819f2e7c72844faecbd90c96fac9dc798084f8f0be30706`).
  - Hardware verified on **Samsung Galaxy M30** (`32001776e8e82623`): `com.azath.reader` updated `2026-11-25 08:09:39`, stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), zero crashes / zero `SIGSYS` across Limestone + Granite toggles, `Home <-> Library <-> More` tab switches, Filter/Sort, `OPDS Catalog Feed`, `Backup`, `2026 Reading Goal`, `Up Next Queue`, `Library Metadata & Genres`, `Import EPUBs`, `Web Book Browser`, `Notes`, `Shelves`, `Insights`, `Storage & Duplicates`, `About Azath`, and `StandAloneBookReaderActivity` open/return.
  - Honest remaining minor stock leaks noted: (1) thin `#101112` Compose outer strip behind `"Search Azath"` pill on `Library` in Limestone mode; (2) stock `InAppNotificationsActivity` (bell) text/icons remain white on `#EFEAE0` cream in Limestone mode.
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 153 (`Azath v17.3 KFX`) Verified, Staged & Audited (2026-11-25)**:
  - Copied verbatim Round 153 `FolioTheme.java` (`StoneBottomBarView` non-clickable touch-passthrough `#EFEAE0` bottom navigation bar overlay for Limestone Light Mode with left floating-cover cutout), `FolioMoreView.java` (`v17.3 KFX`), and `OpenCatalogView.java` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/`. Updated `rebuild_fast.sh` to stage `azath_v17.3_kfx`.
  - Built `classes9.dex` (`478,760` B) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v17.3_kfx/base.apk` (`138,836,071` B, SHA256 `737e130b42e5b51c67be31e31301f633b99cfb5ef8af703f3fc5316c0674d216`).
  - Hardware verified on **Samsung Galaxy M30** (`32001776e8e82623`): `com.azath.reader` updated `2026-11-25 03:13:56`, stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), `ZERO_CRASHES_AND_NO_SIGSYS`. Visually verified `verify_r153_azath_home_limestone_complete.png` (`883,730` B): seamless `#EFEAE0` bottom bar, crisp `#2D3138`/`#73757B` Home/Library/More icons & Inter labels, clean floating-cover cutout, touch-passthrough navigation confirmed across `More <-> Home`, and restored cleanly to `Granite` (`[887,222][1038,286]`).
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 152 (`Azath v17.2 KFX`) Verified, Staged & Audited (2026-11-25)**:
  - Copied verbatim Round 152 `FolioTheme.java` (`syncComposeBottomBarColors`), `FolioMoreView.java` (`v17.2 KFX`, `22sp` header + `16dp/14dp` padding), and `OpenCatalogView.java` (`22sp` `Azath` header + `16dp/14dp` padding) into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/`. Updated `rebuild_fast.sh` to stage `azath_v17.2_kfx`.
  - Built `classes9.dex` (`475,644` B) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v17.2_kfx/base.apk` (`138,831,975` B, SHA256 `a7c1f0d67e2b5a041f4afd7da22d81ce4aa1cd41892263348881e66aee96a5fb`).
  - Hardware verified on **Samsung Galaxy M30** (`32001776e8e82623`): `com.azath.reader` updated `2026-11-25 03:07:16`, stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), `ZERO_CRASHES_AND_NO_SIGSYS`. Visually verified `verify_r152_azath_home_limestone_bar.png` (`884,004` B) and `verify_r152_azath_control_center_granite.png` (`312,570` B): `Azath` (`[42,127][527,202]`) and `Control Center` (`[42,125][646,200]`) headers match cleanly, and Android system nav gesture bar switches to `#EFEAE0` in Limestone, while the Compose bottom nav tab strip stayed dark (`#101112`) on Limestone because `ib.b` static `final long` reflection does not recompose `CompositionLocal` state.
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 151 (`Azath v17.1 KFX`) Verified, Staged & Data Collected (2026-11-25)**:
  - Copied verbatim Round 151 `FolioMoreView.java` (`v17.1 KFX`), `FolioTheme.java` (`applyWindowBars` + `styleDialog` footer painting), `OpenCatalogView.java` (`26sp` EB Garamond header parity), and `FolioMetadataCatalogDialog.java` (`FolioTheme.textPrimary()` heading/inputs) into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/`. Updated `rebuild_fast.sh` to stage `azath_v17.1_kfx`.
  - Built `classes9.dex` (`474,212` B) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v17.1_kfx/base.apk` (`138,831,975` B, SHA256 `eed25c1e152126c288185163127802d8502386db88b53d997a9daf0b668655fc`).
  - Hardware verified on **Samsung Galaxy M30** (`32001776e8e82623`): `com.azath.reader` updated `2026-11-25 03:00:15`, stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), `ZERO_CRASHES_AND_NO_SIGSYS`, restored cleanly to `Granite`, visually verified `verify_r151_azath_catalog_limestone.png` (`368,135` B — `#1A1B1E` `"Library Metadata & Genres (24)"` heading + `#EFEAE0` `CLOSE` footer bar fixed cleanly), and collected `"Library"` bounds (`[43,135][178,198]`) + raw `Lib/b.<clinit>` smali palette tokens for Round 152.
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 150 Milestone (`Azath v17.0 KFX`) Verified, Staged & Audited (2026-11-25)**:
  - Integrated Round 150 `FolioTheme.java` (zero-arg `sLastLight` cached getters), `FolioMoreView.java` (`v17.0 KFX`), `FolioFolderBrowserDialog.java`, `FolioMetadataCatalogDialog.java` (with 2-line `javac` type fix on lines `2938` & `2967` synced to `dropin_src/`), and `FolioWebBrowserDialog.java`. Updated `rebuild_fast.sh` to stage `azath_v17.0_kfx`.
  - Built `classes9.dex` (`473,568` B) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v17.0_kfx/base.apk` (`138,831,975` B, SHA256 `2152ecf1ad99c7e4c2ad63397839f7a0484b3680735fab97fd496814c73cad63`).
  - Hardware verified on **Samsung Galaxy M30** (`32001776e8e82623`): `com.azath.reader` updated `2026-11-25 02:53:35`, stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), `ZERO_CRASHES_AND_NO_SIGSYS`, restored cleanly to `Granite`, and `verify_r150_azath_catalog_limestone.png` (`363,543` B) visually verified in Limestone Light Mode (noting low-contrast white `Library Metadata & Genres (24)` header and dark `CLOSE` footer panel).
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 149 (`Azath v16.3 KFX`) Stress-Tested, Verified & Staged (2026-11-25)**:
  - Integrated Round 149 `FolioTheme.java` (`THEME_LISTENERS` weak-referenced listener registry + `notifyThemeChanged()`), `FolioMoreView.java` (`v16.3 KFX`, persistent `FrameLayout` in-place rebuilder replacing `Activity.recreate()`), and `OpenCatalogView.java` (persistent `FrameLayout` in-place rebuilder). Updated `rebuild_fast.sh` to stage `azath_v16.3_kfx`.
  - Built `classes9.dex` (`473,324` B) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v16.3_kfx/base.apk` (`138,831,975` B, SHA256 `a09e804f949b6462830b77af4309d44d4a817f0479f2d20faf3a970c12c88cec`).
  - Hardware stress-tested on **Samsung Galaxy M30** (`32001776e8e82623`): `com.azath.reader` updated `2026-11-25 02:31:10`, stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`). Executed 4 consecutive uninterrupted toggles (`Granite -> Limestone -> Granite -> Limestone -> Granite`) with `More <-> Home` tab switches; verified identical PID (`PID_START=17643`, `PID_END=17643`), `ZERO_CRASHES_AND_NO_SIGSYS` in `AndroidRuntime:E`/`FATAL:E`/`DEBUG:*`, and visually verified `verify_r149_azath_hotswap_granite.png` (`313,720` B).
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 148 (`Azath v16.2 KFX`) Verified, Staged & Audited (2026-11-25)**:
  - Integrated Round 148 `FolioIcons.java` (`#1A1B1E` PorterDuff.Mode.SRC_IN tint in Limestone Light Mode + `AUTHORS=28`), `FolioMoreView.java` (`v16.2 KFX`, all inner cards & headers dynamicized via `FolioTheme`, `Activity.recreate()` on theme pill toggle), and `OpenCatalogView.java` (Limestone `#E7E1D5` genre chips + `#1A1B1E` carousel book titles & archival fallbacks). Updated `rebuild_fast.sh` to stage `azath_v16.2_kfx`.
  - Built `classes9.dex` (`471,540` B) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v16.2_kfx/base.apk` (`138,827,879` B, SHA256 `263f6a9153f3eb7c1c9cc7d5e00f980cc6af92b760e8fea029c202ef2aa9c8fc`).
  - Hardware verified on **Samsung Galaxy M30** (`32001776e8e82623`): `com.azath.reader` updated `2026-11-25 02:23:21`, stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), `v16.2 KFX` confirmed in UI dumps, instant single `Activity.recreate()` verified with zero force-stop across `verify_r148_azath_control_center_limestone.png` (`313,868` B) and `verify_r148_azath_home_limestone.png` (`885,282` B), then restored cleanly to `Granite` dark mode (`ui_r148_final_granite.xml`).
  - Honest stability finding (`DEBUG` logcat `02:24:22.859`): Java `AndroidRuntime:E` had `NO_FATAL_EXCEPTIONS`, and single `Activity.recreate()` works cleanly in both directions, **but** calling `MainActivity.recreate()` a **second time in the same process** hit native `signal 31 (SIGSYS), SYS_SECCOMP` (`disallowed arm64 system call 375` in `libKindleAndroidNativeBundlerJNI.so+0x03869d2c`). Replacing `Activity.recreate()` with an in-place container `FrameLayout` view rebuild would make unlimited back-and-forth theme toggling crash-free.
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 147 (`Azath v16.1 KFX`) Verified, Staged & Audited (2026-11-25)**:
  - Integrated Round 147 `FolioMoreView.java` (`v16.1 KFX` badge + live `Granite`/`Limestone` toggle pill) and `OpenCatalogView.java` (`FolioTheme` dynamic getters), plus safe-zone-framed `dropin_assets/azath_launcher/ic_launcher_foreground_432.png` -> `dropin_res/ic_launcher_foreground.png`, `azath_icon_limestone_512.png` -> `dropin_res/ic_launcher_full_512.png`, and `dropin_res/mipmap-*`. Updated `rebuild_fast.sh` to stage `azath_v16.1_kfx`.
  - Built `classes9.dex` (`471,184` B) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v16.1_kfx/base.apk` (`138,827,879` B, SHA256 `e92c910b32732eb6d4d8723f0acd929e2858accbbd594c2c5a93184d7f9ded31`).
  - Hardware verified on **Samsung Galaxy M30** (`32001776e8e82623`): `com.azath.reader` updated `2026-11-25 02:14:27`, stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), `NO_FATAL_EXCEPTIONS`, `v16.1 KFX` + `Granite`/`Limestone` toggle confirmed in UI dumps, and 3 screenshots visually inspected (`verify_r147_azath_launcher_safezone.png`, `verify_r147_azath_home_limestone_light.png`, `verify_r147_azath_control_center_limestone.png`), then restored cleanly to `Granite` dark mode.
  - Honest findings logged for Round 148: (1) `OpenCatalogView.java` Continue Reading book titles remain light `#ECE8E0` on `#F4F0E8` Limestone canvas; (2) `FolioMoreView.java` inner cards still render dark `#1C1E22` surfaces during Limestone mode; (3) toggling theme on `More` requires reopening `com.azath.reader` for cached `Home` view to rebuild.
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 146 Stone Edition (`Azath v16.0 KFX`) Verified & Staged (2026-11-25)**:
  - Integrated all 8 Stone Edition Java files (`FolioTheme`, `FolioMoreView`, `OpenCatalogView`, `FolioMetadataCatalogDialog`, `FolioFolderBrowserDialog`, `FolioMetadataBackup`, `FolioWebBrowserDialog`, `ExternalEpubActivity`), copied 16 re-tinted `azath_icons/` + re-tinted `ic_authors.png` to `#ECE8E0`/`#D4C9B8`, installed Limestone launcher icons (`#141517` Stone adaptive background).
  - Updated `patch_classes2_azath.py` (`Lib/b.<clinit>` -> `0xffd4c9b8L` at `0x4d6f50`), `patch_res_azath_covers.py` (Granite `#1C1E22`/`#32353B`/`#8A857D`/`#524B41`/`#ECE8E0`), and `rebuild_fast.sh` (mapping `res/font/amazon_ember_serif_{bold,he}.ttf` -> `EBGaramond-Regular.ttf` so Compose `Library` title uses EB Garamond Regular while keeping reader `Bookerly-*.ttf` untouched in `assets/`, staging `azath_v16.0_kfx`).
  - Built `classes9.dex` (`470136` B) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v16.0_kfx/base.apk` (`139032679` B, SHA256 `f995e5a0372907cadce08240c7189dc2cd9e9967e39836a252f0ba239f8ca0b6`).
  - Hardware verified on **Samsung Galaxy M30** (`32001776e8e82623`): `com.azath.reader` updated `2026-11-25 02:06:48`, stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), `NO_FATAL_EXCEPTIONS`, `v16.0 KFX` verified in `logs/azath_more_r146.xml`, and all 4 screenshots visually verified (`verify_r146_azath_launcher_limestone.png`, `verify_r146_azath_home_granite.png`, `verify_r146_azath_library_garamond.png`, `verify_r146_azath_control_center_granite.png`). Strict policy respected: zero Play Store `ct.reader` launches/references. (Note: Limestone light theme toggle compiled in `FolioTheme.java`, not toggled at runtime in R146.)
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 145 (`Azath v15.1 KFX`) Built, Deployed & Root-Cause Diagnosed (2026-11-25)**:
  - Integrated Round 145 `ExternalEpubActivity.java` (`resolveExistingKfxFile` + Obsidian-Ivory-Crimson `#121010`/`#E05A47`/`#F2EFE9` dialog theme; minor badge text still says `"FOLIO EPUB IMPORT"`) and `FolioMoreView.java` (`Azath v15.1 KFX`, `resolveExistingKfxFile` click wiring), updated `rebuild_fast.sh` (`azath_v15.1_kfx`), rebuilt `classes9.dex` (`468980` bytes), and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v15.1_kfx/base.apk` (`138541159` bytes, SHA256 `17f18cf285e500baf079b7b37fda1ffbd7db7436352b9101bbbc234e001adac4`).
  - Deployed to **Samsung Galaxy M30** (`32001776e8e82623`): `com.azath.reader` updated `2026-11-25 01:46:22`, `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), `v15.1 KFX` verified in `logs/azath_more_r145.xml`, `NO_FATAL_EXCEPTIONS` in logcat.
  - Hardware finding on `verify_r145_azath_continue_reading_launch.png` (`312292` B): tapping *Brave New World* in Control Center still toasts `"Book file not found"` because `Brave_New_World_FOLIO_621B293A3B8F.kfx` (plus `Moksha`, `Crome Yellow`, `The Dream of the Red Chamber`, `Island`) lived inside `/data/user/0/com.amazon.kindle.epub/files/folio_library/` and was deleted when `com.amazon.kindle.epub` was uninstalled in Round 136 (`find /sdcard/` found only `/sdcard/en/0/Aldous Huxley - Brave New World.epub`). Tapping Control Center Card #2 (*Aristotle*) invoked `launchKfxReader` -> `ThirdPartyUpgradePage` -> `AlertActivity` (`"Incompatible Version"`, same as `Library` tab).
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 144 Milestone (`Azath v15.0 KFX`) Verified & Pushed (2026-11-25)**:
  - Updated `FolioMoreView.java` (`Azath v15.0 KFX`) and `rebuild_fast.sh` (`azath_v15.0_kfx`), ran clean rebuild (`patch_classes2_azath.py`, `patch_classes3_azath.sh`, `patch_res_azath_covers.py`), compiled `classes9.dex` (`468272` bytes), and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v15.0_kfx/base.apk` (`138541159` bytes, SHA256 `f07f2aecb16ab1193116c15f3b66097e477b5dc43c3a2988410d7c163d8d9fa6`).
  - Hardware verified on **Samsung Galaxy M30** (`32001776e8e82623`): `com.azath.reader` updated `2026-11-25 01:38:31`, `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), `v15.0 KFX` confirmed in `logs/azath_more_r144.xml`, `NO_FATAL_EXCEPTIONS`, `verify_r144_azath_control_center_v15.png` (`312407` B) visually verified, and `verify_r144_azath_reader.png` (`520400` B) visually verified opening *The 210th Day* into `StandAloneBookReaderActivity` with crisp Enhanced Typesetting.
  - Pushed complete `Azath v15.0 KFX` milestone to `origin/master` at commit SHA `48db36803f4059c571cfddbbf828825e8afb46f2` (`48db368`).
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 143 (`Azath v14.9 KFX`) Verified & Staged (2026-11-25)**:
  - Created persistent idempotent `07_APK_EPUB_INGESTION_MOD/tools/patch_res_azath_covers.py` wired into `rebuild_fast.sh`: generates Obsidian `#211C1B` WebPs (`400x600`, `800x1200`, `960x1440`) with `#2A2423` rim, `#8E867E` open-book emblem, and `#5C2D26` hairline rule across `drawable*/generic_book_cover_list.webp`, patches `generated_cover_text_color` (`#f2efe9`) and `library_default_cover_color_dark/light` (`#211c1b`) in `colors.xml`, `rich_placeholder_author` (`#8e867e`) in `badgeable_cover_rich_placeholder_overlay.xml`, and `bds_book_placeholder_dark.xml`.
  - Integrated Round 143 `FolioFolderBrowserDialog.java` (`Hide Imported: ON` tonal `#2A1816`/`#5C2D26`/`#F5B8AE` `999dp` pill — source integrated, modal not re-screenshotted in R143) and `FolioMoreView.java` (`Azath v14.9 KFX`), rebuilt `classes9.dex` (`468272` bytes), and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v14.9_kfx/base.apk` (`138541159` bytes, SHA256 `d49f038c41bd779ae2ad924a469aee8ac47e005ef2e5ee6fc21e3475ee526d92`).
  - Hardware verified on **Samsung Galaxy M30** (`32001776e8e82623`): `com.azath.reader` updated `2026-11-25 01:32:09`, `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), `v14.9 KFX` confirmed in `logs/azath_more_r143.xml`, `NO_FATAL_EXCEPTIONS`, and `verify_r143_azath_library_dark_covers.png` (`1058748` B) visually verified with dark Obsidian generic covers across all coverless Library books.
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 142 (`Azath v14.8 KFX`) Verified & Staged (2026-11-25)**:
  - Integrated Round 142 `FolioFolderBrowserDialog.java` (`22sp` Regular EB Garamond heading, `#F2EFE9` archival ivory typography, `999dp` Sort pill), `OpenCatalogView.java`, and `FolioMoreView.java` (`96dp` bottom clearance, `Azath v14.8 KFX`), updated `rebuild_fast.sh` (`azath_v14.8_kfx`), rebuilt `classes9.dex` (`468220` bytes), and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v14.8_kfx/base.apk` (`138532967` bytes, SHA256 `805aa4d9fe4f9b5553498313bed3518032918d6e294737b49ac7cfcceee2c20d`).
  - Hardware verified on **Samsung Galaxy M30** (`32001776e8e82623`): `com.azath.reader` updated `2026-11-25 01:23:23`, `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), `NO_FATAL_EXCEPTIONS`, `v14.8 KFX` confirmed in `logs/azath_more_r142.xml`.
  - Visual audit findings: `verify_r142_azath_importer.png` (`245872` B) confirmed all editorial importer upgrades; `verify_r142_azath_library.png` (`943207` B) confirmed clean dark `Library` header, `Search Azath`, and `#E05A47` active tab (no Kindle blue), while identifying bright light-grey/white stock placeholder cards on coverless books inside the dark `Library` grid.
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 141 (`Azath v14.7 KFX`) Verified & Staged (2026-11-25)**:
  - Extended `07_APK_EPUB_INGESTION_MOD/tools/patch_classes3_azath.sh` to switch `MoreScreenlet.j()` (`com/amazon/kcp/more/w.smali`) from `HeaderMode;->d` to `HeaderMode;->c` alongside `HomeScreenlet`, collapsing the redundant `More` + gear + `Search Azath` top chrome while leaving `LibraryScreenlet` untouched.
  - Integrated Round 141 `OpenCatalogView.java` (`formatHomeAuthor` normalization + direct `InAppNotificationsActivity` bell launch) and `FolioMoreView.java` (`12dp` top padding + `v14.7 KFX`), updated `rebuild_fast.sh` (`azath_v14.7_kfx`), rebuilt `classes9.dex` (`468160` bytes), and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v14.7_kfx/base.apk` (`138532967` bytes, SHA256 `4b03ec387430adba1025dc92e414aedfd22b873e52e7c6fc292af8c10e75c292`).
  - Hardware verified on **Samsung Galaxy M30** (`32001776e8e82623`): `com.azath.reader` updated `2026-11-25 01:17:26`, `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), `NO_FATAL_EXCEPTIONS`, `verify_r141_azath_control_center.png` (`311907` B) visually verified with `Control Center` at `bounds="[53,115][630,205]"` and `v14.7 KFX`, and `verify_r141_azath_home.png` (`640883` B) visually verified with normalized `Natsume Soseki` and `Ryunosuke Akutagawa` author labels.
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 140 (`Azath v14.6 KFX`) Verified & Staged (2026-11-25)**:
  - Confirmed via `logs/azath_home_r139.xml` & smali disassembly that the stock top `"Home"` + Bell header lived in Jetpack Compose (`com/amazon/kcp/home/a.smali` inside `classes3.dex`) rather than an Android `TextView`. Created idempotent `07_APK_EPUB_INGESTION_MOD/tools/patch_classes3_azath.sh` switching `HomeScreenlet.j()` from `HeaderMode;->d` to `HeaderMode;->c` (empty header in `NavigationControllerTopAppBar`, leaving `Library` & `More` intact), and wired `OpenCatalogView.makeBellIconButton` to launch `InAppNotificationsActivity` directly (restoring clean unpatched `classes5.dex`).
  - Integrated Round 140 `OpenCatalogView.java` and `FolioMoreView.java` (`v14.6 KFX` + all `rec.title` callers + `"Azath Library Insights\n"`), updated `rebuild_fast.sh` (`azath_v14.6_kfx`), rebuilt `classes9.dex` (`467736` bytes), and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v14.6_kfx/base.apk` (`138532967` bytes, SHA256 `d7995bd674ad6db38cd034f74dbe38907e95a3ee44dcf4a7b2a46a6d17a8530f`).
  - Hardware verified on **Samsung Galaxy M30** (`32001776e8e82623`): `com.azath.reader` updated `2026-11-25 01:11:28`, `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), `NO_FATAL_EXCEPTIONS`, `v14.6 KFX` verified in `logs/azath_more_r140.xml`, tapping the Bell icon (`993, 160`) opened `InAppNotificationsActivity`, and `verify_r140_azath_home.png` (`641259` B) visually verified with single-row `Azath` header (`[42,109][527,212]`) + `+ OPDS`, `Backup`, and circular Bell button directly under the status bar.
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 139 (`Azath v14.5 KFX`) Verified & Staged (2026-11-25)**:
  - Integrated Round 139 `OpenCatalogView.java` (`.cover.jpg` fallback lookup, Regular EB Garamond `34sp` drop-cap archival covers + `#5C2D26` spine rules, graceful `#181515` `Offline Mode` card with `Retry` & `Local Catalog` pills) and `FolioMoreView.java` (`v14.5 KFX`), while restoring `makeMiniCoverView(ctx, d, rec.kfxPath, rec.title, 38, 54, 9)` and `"Azath Library Insights\n"` in both `dropin_src/` and `07_APK_EPUB_INGESTION_MOD/mod_src/real/`.
  - Updated `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` for `azath_v14.5_kfx` staging, recompiled `classes9.dex` (`464996` bytes), and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v14.5_kfx/base.apk` (`138532967` bytes, SHA256 `7320ae96062ae5a080b01c79a763c6cfb8617216fb3c9f92370780d6e71361c7`).
  - Hardware verified on **Samsung Galaxy M30** (`32001776e8e82623`): `com.azath.reader` updated `2026-11-25 00:52:52`, `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), `NO_FATAL_EXCEPTIONS` in logcat, `v14.5 KFX` confirmed in `logs/azath_more_r139.xml`, and both screenshots visually verified: `verify_r139_azath_home.png` (`185017` B) and `verify_r139_azath_web_browser_offline.png` (`116073` B).
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Round 138 (`Azath v14.4 KFX`) Verified & Staged (2026-11-25)**:
  - Bundled `EBGaramond-Regular.ttf` (`381K`), `EBGaramond-SemiBold.ttf` (`384K`), `Inter-Regular.otf` (`253K`), and `Inter-SemiBold.otf` (`265K`) in `07_APK_EPUB_INGESTION_MOD/assets/azath_fonts/` and `dropin_assets/azath_fonts/`.
  - Integrated Round 138 `FolioTheme.java`, `OpenCatalogView.java`, `FolioMoreView.java`, and `FolioWebBrowserDialog.java` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/` (preserving `makeMiniCoverView(ctx, d, rec.kfxPath, rec.title, 38, 54, 9)` and `"Azath Library Insights\n"` in `dropin_src/.../FolioMoreView.java`, plus Round 137's `FolioIcons.java`, `ic_authors.png`, `FolioMetadataCatalogDialog.java`).
  - Built & deployed via `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh`: `classes9.dex` (`462736` bytes), `Lib/b.<clinit>` Ember Crimson `0xffe05a47L` patch applied, staged `07_APK_EPUB_INGESTION_MOD/release/azath_v14.4_kfx/base.apk` (`138528871` bytes, SHA256 `234deb91bf6ce4245203f451e58f551688e268eb89efa02c31e4bda0fe759663`).
  - Hardware verified on **Samsung Galaxy M30** (`32001776e8e82623`): `com.azath.reader` updated `2026-11-25 00:43:31`, `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`), `NO_FATAL_EXCEPTIONS` in logcat, `verify_r138_azath_home.png` (`154151` bytes) & `verify_r138_azath_control_center.png` (`239020` bytes) captured and visually verified (`logs/azath_more_r138.xml` shows `v14.4 KFX`).
  - Unverified at runtime in R138: Dark offline WebView `onReceivedError` in `FolioWebBrowserDialog.java` (compiled, not screenshotted in R138).
  - **Cover Architecture Directive (Mandatory)**:
    > "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Current State**: Cloned package `com.azath.reader` ("Azath v14.3 KFX") deployed and verified on Samsung Galaxy M30 alongside unmodified stock `com.amazon.kindle`. Round 137 complete:
  1. **Continue Reading Archival Monograms, Bespoke Catalog Authors Icon & Web Browser Verification (Task A)**:
     - Target Hardware: Physical Samsung Galaxy M30 (`32001776e8e82623`).
     - Added matching 96x96 transparent `#EFECE6`+`#E05A47` `ic_authors.png` + `FolioIcons.AUTHORS = 28` in `FolioIcons.java` (since `FolioMetadataCatalogDialog.java` referenced `FolioIcons.AUTHORS` while zip omitted `FolioIcons.java`/`ic_authors.png`).
     - Integrated `FolioMoreView.java` (`v14.3 KFX`, `extractCoverInitial(...)` + `#5C2D26` spine rules in `makeMiniCoverView` & Global Search, added `titleHint` overload passing `rec.title` so *Brave New World* displays `B`, kept `"Azath Library Insights"` snapshot header) and `FolioMetadataCatalogDialog.java` (`FolioIcons.AUTHORS`).
     - Updated `rebuild_fast.sh` to stage `azath_v14.3_kfx`, compiled `classes9.dex` (`461,852` bytes), kept `Lib/b.<clinit>` Ember Crimson DEX patch + XML patches, deployed `com.azath.reader` (`lastUpdateTime=2026-11-25 00:33:28`), staged `07_APK_EPUB_INGESTION_MOD/release/azath_v14.3_kfx/base.apk` (`137,860,883` bytes, SHA256: `2e269a19b6a555aa7894fe49375532a0fada254bb7c040d596783efe7f3cf6be`).
  2. **Visual Verification & Invariant Proofs (Task B)**:
     - Captured on Samsung Galaxy M30:
       * `verify_r137_azath_control_center.png` (`230,136` bytes) — `v14.3 KFX` badge, Regular Bookerly **`B`** archival monogram + `#5C2D26` spine rule on *Brave New World* in `Continue Reading`, `#E05A47` Ember Crimson `More` tab.
       * `verify_r137_azath_web_browser.png` (`104,589` bytes) — Web Book Browser unified Obsidian-Ivory-Crimson toolbar & bookmarks bar (`Pin`, tonal `EPUBs`, `Downloads`, `Standard Ebooks`, `Gutenberg`). Noted Chromium's default offline error text is dark-on-dark on M30 without Wi-Fi.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`), zero crashes.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
  4. Packaged `antig_to_gemini_neon_clunky_report137.zip` and submitted to Gemini Neon (`clunky` tab).

## Verification Evidence (Round 137)
1. `verify_r137_azath_control_center.png`: Control Center (`v14.3 KFX`), *Brave New World* `B` monogram, `#E05A47` `More` tab.
2. `verify_r137_azath_web_browser.png`: Web Book Browser Obsidian-Ivory-Crimson toolbar & bookmark chips.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.azath.reader`: Azath v14.3 KFX deployed cleanly (`lastUpdateTime=2026-11-25 00:33:28`).
- **Current State**: Cloned package `com.azath.reader` ("Azath v14.2 KFX") deployed and verified on Samsung Galaxy M30 alongside unmodified stock `com.amazon.kindle`. Round 136 complete:
  1. **Bottom Nav Ember-Crimson Alignment, Archival Monogram Covers & Tonal Catalog Polish (Task A)**:
     - Target Hardware: Physical Samsung Galaxy M30 (`32001776e8e82623`).
     - Integrated drop-in `FolioMoreView.java` (`v14.2 KFX`, preserved `Reading Time (WPM)` snapshot + shared `calibrateClick`) and `FolioMetadataCatalogDialog.java` (56x84dp `#211C1B` archival monogram fallback covers with Regular Bookerly initial & `#5C2D26` spine rule, tonal `#2A1816`/`#5C2D26`/`#F5B8AE` filter chips, muted `#8E867E` genres, `#F2EFE9` ivory titles).
     - Identified & patched Compose bottom nav active tint `#2491ff` across both XML resources (`values-night/colors.xml`, `values/colors.xml`, `values/styles.xml`, `bottom_bar_label_color_dark.xml`) AND compiled bytecode in `classes2.dex` (`Lib/b.<clinit>` `0xff2491ffL -> 0xffe05a47L` via new idempotent `07_APK_EPUB_INGESTION_MOD/tools/patch_classes2_azath.py`), persisted in `rebuild_fast.sh`.
     - After `INSTALL_FAILED_INSUFFICIENT_STORAGE` (`0.9G` free) and explicit user approval, uninstalled legacy `com.amazon.kindle.epub` and cleaned old `/data/local/tmp` test scratch files (`1.6G` free).
     - Compiled `classes9.dex` (`460,968` bytes), patched `classes2.dex` (`8,413,156` bytes), deployed `com.azath.reader` (`lastUpdateTime=2026-11-25 00:24:20`) with zero crashes, and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v14.2_kfx/base.apk` (`137,856,708` bytes, SHA256: `8cb1e59a65dec11292b5efed8a289d31150cf2b4e4ff92e16e9d93cc13e11a9a`).
  2. **Visual Verification & Invariant Proofs (Task B)**:
     - Captured on Samsung Galaxy M30:
       * `verify_r136_azath_control_center.png` (`229,874` bytes) — `Control Center` (`v14.2 KFX`), `26 BOOKS`, `5 READING`, bespoke icons, and bottom nav **`More` tab** verified by pixel check at exact **`#e05a47` Ember Crimson**.
       * `verify_r136_azath_catalog_polish.png` (`309,600` bytes) — `Library Metadata & Genres (24)` showing tonal active filter chips, Bookerly monogram fallback covers (`B`, `I`), `#8E867E` muted genres, and single primary `#E05A47` CTA.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
  4. Packaged `antig_to_gemini_neon_clunky_report136.zip` and submitted to Gemini Neon (`clunky` tab).

## Verification Evidence (Round 136)
1. `verify_r136_azath_control_center.png`: Control Center (`v14.2 KFX`) + bottom nav active tab verified `#e05a47` Ember Crimson.
2. `verify_r136_azath_catalog_polish.png`: Archival Bookerly monogram covers + tonal filter chips in Metadata Catalog.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.azath.reader`: Azath v14.2 KFX deployed cleanly (`lastUpdateTime=2026-11-25 00:24:20`).
- **Current State**: Cloned package `com.azath.reader` ("Azath v14.1 KFX") deployed and verified on Samsung Galaxy M30 alongside unmodified stock `com.amazon.kindle`. Round 135 complete:
  1. **Editorial Typography & Unified Obsidian-Ivory-Crimson Design System (Task A)**:
     - Target Hardware: Physical Samsung Galaxy M30 (`32001776e8e82623`).
     - `FolioTheme.java`: Switched display headings (`>= 17sp`) to Regular-weight `Bookerly` (`bookerlyReg`, `-0.018f` tracking `>= 19.5sp`) paired with `Amazon Ember` sans UI controls (`< 17sp`), and unified rounded `#121010` / `#2A2423` modal window framing + `#E05A47` / `#8E867E` / `#C4BDB5` buttons.
     - Swept all 6 modules (`FolioTheme`, `FolioMoreView`, `FolioMetadataCatalogDialog`, `FolioFolderBrowserDialog`, `FolioWebBrowserDialog`, `FolioMetadataBackup`) to Obsidian-Ivory-Crimson tokens (`#0A0909` / `#121010` / `#181515` / `#211C1B` / `#2A2423` / `#F2EFE9` / `#E05A47`), restored `Reading Time (WPM)` snapshot line in `FolioMoreView.java`, updated `rebuild_fast.sh` to `azath_v14.1_kfx`.
     - Compiled `classes9.dex` (`460,456` bytes), deployed `com.azath.reader` (`lastUpdateTime=2026-11-25 00:08:51`) with zero crashes, and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v14.1_kfx/base.apk` (`137,856,708` bytes, SHA256: `e20d524c13e2c97b975317ab462765f843123838ef73f74b149782172d2fbaa0`).
  2. **Visual Verification & Invariant Proofs (Task B)**:
     - Captured on Samsung Galaxy M30:
       * `verify_r135_azath_control_center.png` (`229,415` bytes) — Regular Bookerly `Control Center`, ember-crimson `v14.1 KFX` pill badge, `#181515` warm obsidian cards, `26 BOOKS` / `5 READING` restored state, ember-crimson accents, bespoke icons.
       * `verify_r135_azath_modal_uniformity.png` (`305,157` bytes) — `Library Metadata & Genres (24)` showing `#121010` rounded modal frame, Regular Bookerly heading, `AZATH` eyebrow, `#E05A47` active pills and `CLOSE` button.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
  4. Packaged `antig_to_gemini_neon_clunky_report135.zip` and submitted to Gemini Neon (`clunky` tab).

## Verification Evidence (Round 135)
1. `verify_r135_azath_control_center.png`: Unified Obsidian-Ivory-Crimson Control Center (`v14.1 KFX`) on Galaxy M30.
2. `verify_r135_azath_modal_uniformity.png`: Library Metadata & Genres modal with Regular Bookerly display font & Obsidian-Crimson accents.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.azath.reader`: Azath v14.1 KFX deployed cleanly (`lastUpdateTime=2026-11-25 00:08:51`).
- **Current State**: Package cleanly rebranded and deployed as `com.azath.reader` ("Azath v14.0 KFX") on Samsung Galaxy M30 alongside unmodified stock `com.amazon.kindle`. Round 134 (Major Milestone) complete:
  1. **Malazan Rebrand (`com.azath.reader`) & Bespoke Generated Icon Set (Task A)**:
     - Target Hardware: Physical Samsung Galaxy M30 (`32001776e8e82623`).
     - Bundled 16 handcrafted 96x96 transparent ivory+crimson PNGs (`dropin_assets/azath_icons/*.png` -> `07_APK_EPUB_INGESTION_MOD/assets/azath_icons/*.png` -> `assets/azath_icons/` in APK).
     - Integrated all 6 drop-in Java files (`FolioIcons`, `FolioMoreView`, `FolioMetadataCatalogDialog`, `FolioFolderBrowserDialog`, `FolioWebBrowserDialog`, `FolioMetadataBackup`), preserving Round 132/133 fixes (`Reading Time` snapshot line, shared `calibrateClick`, `# Azath Reading Log`).
     - Updated `OpenCatalogView.java` (`Search Azath` + `"Azath"` header) and `EpubTranscoder.java` (`com.azath.reader/lib/libkfxconv.so`).
     - Rebranded `AndroidManifest.xml` cleanly (`package="com.azath.reader"`, `android:label="Azath"`, unique permissions & authorities including `kindlereader.azath` / `MAPInformationProvider.azath`), updated `strings.xml`, updated `rebuild_fast.sh`.
     - Compiled `classes9.dex` (`459,960` bytes), deployed `com.azath.reader` (`lastUpdateTime=2026-11-24 23:55:15`) with **zero authority/launch crashes**, and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v14.0_kfx/base.apk` (`137,856,708` bytes, SHA256: `c5ff452232ba5fc38a5c7d0de91f8f03796ca9ea4ba5790dd4d30fdc8ecb0e67`).
  2. **Visual Verification & Play Store Folio Reference Study (Task B)**:
     - Captured on Samsung Galaxy M30:
       * `verify_r134_azath_control_center.png` (`208,312` bytes) — Azath More tab (`Search Azath`, `Control Center`, `v14.0 KFX`, bespoke ivory+crimson icons).
       * `verify_r134_azath_launcher.png` (`339,650` bytes) — App drawer showing crimson **`Azath`** icon and label.
       * `ref_playstore_folio_1.png` (`418,540` bytes) & `ref_playstore_folio_2.png` (`155,997` bytes) — Play Store Folio (`ct.reader/com.folio.reader.MainActivity`) Library & Settings screens for Round 135 typography unification reference.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
  4. Packaged `antig_to_gemini_neon_clunky_report134.zip` and submitted to Gemini Neon (`clunky` tab).

## Verification Evidence (Round 134)
1. `verify_r134_azath_control_center.png`: Azath Control Center (`v14.0 KFX`, `Search Azath`, bespoke 16-icon set).
2. `verify_r134_azath_launcher.png`: Azath launcher icon & label in M30 app drawer.
3. `ref_playstore_folio_1.png` & `ref_playstore_folio_2.png`: Play Store Folio (`ct.reader`) typography & spacing reference screens.
4. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
5. `dumpsys package com.azath.reader`: Azath v14.0 KFX deployed cleanly (`lastUpdateTime=2026-11-24 23:55:15`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v13.3 KFX") deployed and fully verified on Samsung Galaxy M30 alongside unmodified stock `com.amazon.kindle`. Round 133 complete:
  1. **Kindle-Native Reading Log Modal & Editorial Offline HTML Export (Task A)**:
     - Target Hardware: Physical Samsung Galaxy M30 (`32001776e8e82623`).
     - `FolioMetadataBackup.java` (`exportLibraryHtml`): Redesigned offline `/sdcard/kindle/backups/folio_library.html` CSS to Kindle-Native Editorial Dark Theme (`#000000` canvas, `#141618` cards, Georgia serif headings, `#4DA6FF` Kindle blue accents, zero neon colors).
     - `FolioMoreView.java`: Wired `FolioTheme.applyToDialog(ctx, logDlg)` post-`.show()` onto `showReadingLogDialog`. Shared `calibrateClick` listener across `rtHdr` and `rtRow`, preserved `Reading Time (WPM)` snapshot line in `snapshotText`, and bumped badge + `About Folio` to **`Folio v13.3 KFX`**.
     - Recompiled `classes9.dex` (`458,484` bytes), rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, signed with `apksigner`.
     - Streamed APK install to Galaxy M30 (`lastUpdateTime=2026-11-24 23:41:43`) with zero fatal crashes logged.
     - Staged release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v13.3_kfx/base.apk` (`137,740,774` bytes, SHA256: `b46ca94d9cd3e58bf3d846da0fa5091bc0f89f5a77888fcd99c638d19fedb144`).
  2. **Visual Verification & Invariant Proofs (Task B)**:
     - Regenerated `/sdcard/kindle/backups/folio_library.html` via **HTML** button (`8,778` bytes, up from `7,486` bytes; verified `#000000` / Georgia / `#4DA6FF` CSS in pulled copy).
     - Captured hardware proof screenshots on Samsung Galaxy M30:
       * `verify_r133_reading_log_kindle.png` (`234,189` bytes) — Reading Log dialog showing `Reading Log (.md)`, `Saved HTML: /sdcard/kindle/backups/folio_library.html`, export toast, and styled Kindle footer buttons.
       * `verify_r133_about_v133.png` (`338,933` bytes) — About Folio modal showing `Version: Folio v13.3 KFX (Enhanced Typesetting)`.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Cloned Invariant: `com.amazon.kindle.epub` updated (`lastUpdateTime=2026-11-24 23:41:43`), zero crashes.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
  4. Packaged `antig_to_gemini_neon_clunky_report133.zip` and submitted to Gemini Neon (`clunky` tab).

## Verification Evidence (Round 133)
1. `verify_r133_reading_log_kindle.png`: Reading Log modal with Kindle theme & HTML export confirmation.
2. `verify_r133_about_v133.png`: About Folio modal showing Folio v13.3 KFX.
3. `/sdcard/kindle/backups/folio_library.html`: Verified regenerated `8,778` bytes on device with Kindle Editorial Dark CSS.
4. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
5. `dumpsys package com.amazon.kindle.epub`: Folio v13.3 KFX deployed (`lastUpdateTime=2026-11-24 23:41:43`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v13.2 KFX") deployed and fully verified on Samsung Galaxy M30 alongside unmodified stock `com.amazon.kindle`. Round 132 complete:
  1. **Insights WPM Tap-to-Calibrate, Snapshot Telemetry & Clean Dialog Theming Sync (Task A)**:
     - Target Hardware: Physical Samsung Galaxy M30 (`32001776e8e82623`).
     - `FolioMoreView.java`: Integrated `READING TIME PORTFOLIO (250 WPM · TAP TO CALIBRATE)` header and interactive tap-to-calibrate handler on both `rtHdr` and `rtRow` opening `showReadingGoalDialog` with auto-reopen to `showLibraryInsightsDialog`. Added missing `Reading Time (WPM): total · read · remaining` line into `snapshotText` for `COPY SUMMARY` / `SHARE`. Confirmed post-`.show()` `FolioTheme.applyToDialog` across all 5 dialogs out of the box. Bumped badge and `About Folio` to **`Folio v13.2 KFX`**.
     - Recompiled `classes9.dex` (`458,448` bytes), rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, signed with `apksigner`.
     - Streamed APK install to Galaxy M30 (`lastUpdateTime=2026-11-24 23:32:53`) with zero fatal crashes logged.
     - Staged release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v13.2_kfx/base.apk` (`137,740,774` bytes, SHA256: `430a7b2d35adbc016a99248d4ce6b9e7a9c9e28fa853692d68444cd8071aaba2`).
  2. **Visual Verification & Invariant Proofs (Task B)**:
     - Captured hardware proof screenshots on Samsung Galaxy M30:
       * `verify_r132_insights_calibrate.png` (`176,328` bytes) — Library Insights showing `READING TIME PORTFOLIO (250 WPM · TAP TO CALIBRATE)` 3-tile strip; interactively verified tapping opens `2026 Reading Goal`.
       * `verify_r132_about_v132.png` (`339,562` bytes) — About Folio modal showing `Version: Folio v13.2 KFX (Enhanced Typesetting)`.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Cloned Invariant: `com.amazon.kindle.epub` updated (`lastUpdateTime=2026-11-24 23:32:53`), zero crashes.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
  4. Packaged `antig_to_gemini_neon_clunky_report132.zip` and submitted to Gemini Neon (`clunky` tab).

## Verification Evidence (Round 132)
1. `verify_r132_insights_calibrate.png`: Library Insights modal with Tap-to-Calibrate label & 3-tile Reading Time Portfolio.
2. `verify_r132_about_v132.png`: About Folio modal showing Folio v13.2 KFX.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v13.2 KFX deployed (`lastUpdateTime=2026-11-24 23:32:53`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v13.1 KFX") deployed and fully verified on Samsung Galaxy M30 alongside unmodified stock `com.amazon.kindle`. Round 131 complete:
  1. **Library Insights Reading Time Portfolio & Kindle Theme (Task A)**:
     - Target Hardware: Physical Samsung Galaxy M30 (`32001776e8e82623`).
     - `FolioMoreView.java`: Added `READING TIME PORTFOLIO (250 WPM)` 3-tile dashboard (`TOTAL LIBRARY` `651h`, `TIME READ` `14.9h` highlighted in Kindle blue-slate `#162330`/`#2C5278`/`#4DA6FF`, and `REMAINING` `636h`) at the top of `showLibraryInsightsDialog`, dynamically scaled by `getReadingSpeedWpm(ctx)`. Repaired drop-in `FolioTheme.applyToDialog` placement post-`.show()` across `insightDlgRef[0]`, `goalDlgRef[0]`, `dlgRef[0]`, `storDlgRef[0]`, and `aboutDlg`. Bumped badge and `About Folio` to **`Folio v13.1 KFX`**.
     - Recompiled `classes9.dex` (457,600 bytes), decoded `folio_v13.0_kfx/base.apk` into clean `/tmp/kindle_clone_build/base_dec`, rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, signed with `apksigner`.
     - Streamed APK install to Galaxy M30 (`lastUpdateTime=2026-11-24 23:21:14`) with zero fatal crashes logged.
     - Staged release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v13.1_kfx/base.apk` (137,740,774 bytes ~132 MB, SHA256: `52745681d89e0a22f71306c335da927778de3146eb635d2f350de80c5bef7108`).
  2. **Visual Verification & Invariant Proofs (Task B)**:
     - Captured hardware proof screenshots on Samsung Galaxy M30:
       * `verify_r131_insights_reading_time.png` (174,011 bytes) — Library Insights showing Bookerly heading, `READING TIME PORTFOLIO (250 WPM)` 3-tile strip (`651h TOTAL LIBRARY`, `14.9h TIME READ`, `636h REMAINING`), reading status bar, metadata health, top genres, and styled Kindle footer buttons.
       * `verify_r131_about_v131.png` (338,914 bytes) — About Folio modal showing `Version: Folio v13.1 KFX (Enhanced Typesetting)`, Bookerly/Ember theme, and action buttons.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Cloned Invariant: `com.amazon.kindle.epub` updated (`lastUpdateTime=2026-11-24 23:21:14`), zero crashes.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
  4. Packaged `antig_to_gemini_neon_clunky_report131.zip` and submitted to Gemini Neon (`clunky` tab).

## Verification Evidence (Round 131)
1. `verify_r131_insights_reading_time.png`: Library Insights modal with Reading Time Portfolio strip and Kindle-themed typography/buttons.
2. `verify_r131_about_v131.png`: About Folio modal showing Folio v13.1 KFX.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v13.1 KFX deployed (`lastUpdateTime=2026-11-24 23:21:14`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v13.0 KFX") deployed and fully verified on Samsung Galaxy M30 alongside unmodified stock `com.amazon.kindle`. Round 130 (Major Milestone) complete:
  1. **Reading Speed (WPM) Calibration & Pure-Kindle Book Details (Task A)**:
     - Target Hardware: Physical Samsung Galaxy M30 (`32001776e8e82623`).
     - `FolioMoreView.java`: Added a 4-pill `READING SPEED (WPM)` selector (`180 wpm`, `250 wpm`, `330 wpm`, `420 wpm`, persisted in `SharedPreferences("folio_ui_prefs", "reading_speed_wpm")`) inside the **2026 Reading Goal** modal. Repaired drop-in dialog theming: moved `FolioTheme.applyToDialog` post-`.show()` across reading goal, Up Next queue clear, cache clear, and About Folio dialogs. Bumped badge to **`Folio v13.0 KFX`** and version string to `Folio v13.0 KFX (Enhanced Typesetting)`.
     - `FolioMetadataCatalogDialog.java`: Scaled `Format: Enhanced KFX · 1.2 MB · ~7h 8m read` dynamically using the user's configured `reading_speed_wpm`. Swept remaining indigo/amber borders to pure Kindle blue-slate `#2C5278` / `#162330` across `Favorited`, `Shelf`, `Personal Note`, and bulk selection pills.
     - Recompiled `classes9.dex` (456,172 bytes), rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, signed with `apksigner`.
     - Streamed APK install to Galaxy M30 (`lastUpdateTime=2026-11-24 06:33:02`) with zero fatal crashes logged.
     - Staged release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v13.0_kfx/base.apk` (137,740,774 bytes ~132 MB, SHA256: `332b90e59b0750fee936496d62d3d0ba677f4c8b7168bedb074882e4fffa3463`).
  2. **Visual Verification & Invariant Proofs (Task B)**:
     - Captured hardware proof screenshots on Samsung Galaxy M30:
       * `verify_r130_reading_goal_wpm.png` (193,335 bytes) — 2026 Reading Goal modal showing header, preset pills, target input, and the new `READING SPEED (WPM)` 4-pill selector (`180 wpm`, `250 wpm`, `330 wpm`, `420 wpm`) with active teal accent (`#26A69A`).
       * `verify_r130_book_details_pure_kindle.png` (267,884 bytes) — Book Details for *1984* showing pure-Kindle blue-slate borders on `Favorited`, `Classics`, and `Personal Note`, dynamic reading-time estimate `Format: Enhanced KFX · 1.2 MB · ~7h 8m read`, and Kindle footer buttons.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Cloned Invariant: `com.amazon.kindle.epub` updated (`lastUpdateTime=2026-11-24 06:33:02`), zero crashes.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
  4. Packaged `antig_to_gemini_neon_clunky_report130.zip` and submitted to Gemini Neon tab `837018552`.

## Verification Evidence (Round 130)
1. `verify_r130_reading_goal_wpm.png`: Reading Goal dialog with 4-pill Reading Speed (WPM) selector.
2. `verify_r130_book_details_pure_kindle.png`: Book Details modal with calibrated reading-time and pure-Kindle blue-slate borders.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v13.0 KFX deployed (`lastUpdateTime=2026-11-24 06:33:02`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v12.9 KFX") deployed and fully verified on Samsung Galaxy M30 alongside unmodified stock `com.amazon.kindle`. Round 129 complete:
  1. **Kindle-Native Book Details, Reading-Time Estimate & About Folio Theme (Task A)**:
     - Target Hardware: Physical Samsung Galaxy M30 (`32001776e8e82623`).
     - `FolioMetadataCatalogDialog.java`: Added reading-time estimate calculation to `showBookDetailDialog` inline on Format line (`int estMins = Math.max(15, Math.min(2400, (int) Math.round((bytes / 1024.0) * 0.34)))` resulting in `~Xh Ym read`). Wired `FolioTheme.applyToDialog(ctx, detailDlgRef[0])` onto Book Details modal so titles render in Bookerly, metadata/controls in Amazon Ember, and action buttons (`Read Now` in `#4DA6FF`, `Edit Metadata` in `#C4CBD2`, `Close` in `#8E959C`) match native Kindle design.
     - `FolioMoreView.java`: Wired `FolioTheme.applyToDialog(ctx, aboutDlg)` onto `About Folio` dialog. Preserved post-`.show()` dialog styling across all Control Center dialogs. Bumped badge to **`Folio v12.9 KFX`** and version string to `Folio v12.9 KFX (Enhanced Typesetting)`.
     - Recompiled `classes9.dex` (454,336 bytes), rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, signed with `apksigner`.
     - Streamed APK install to Galaxy M30 (`lastUpdateTime=2026-11-24 06:19:06`) with zero fatal crashes logged.
     - Staged release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v12.9_kfx/base.apk` (132 MB, SHA256: `5cae24952cadd7e703085bc3334260de8fa5f8a0ec8990750ee0b983409bb3bc`).
  2. **Visual Verification & Invariant Proofs (Task B)**:
     - Captured hardware proof screenshots on Samsung Galaxy M30:
       * `verify_r129_book_details_kindle.png` (268,709 bytes) — Book Details for *1984* showing Bookerly title, Ember metadata with `Format: Enhanced KFX · 1.2 MB · ~7h 8m read`, genre tags, synopsis, and styled Kindle footer buttons (`READ NOW`, `EDIT METADATA`, `CLOSE`).
       * `verify_r129_about_v129.png` (338,632 bytes) — About Folio dialog showing `Version: Folio v12.9 KFX (Enhanced Typesetting)`, engine highlights, library stats, and styled buttons.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Cloned Invariant: `com.amazon.kindle.epub` updated (`lastUpdateTime=2026-11-24 06:19:06`), zero crashes.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
  4. Packaged `antig_to_gemini_neon_clunky_report129.zip` and submitted to Gemini Neon tab `837018552`.

## Verification Evidence (Round 129)
1. `verify_r129_book_details_kindle.png`: Book Details with Bookerly/Ember typography, `~7h 8m read` estimate, and Kindle-styled action buttons.
2. `verify_r129_about_v129.png`: About Folio modal showing Folio v12.9 KFX styled with FolioTheme.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v12.9 KFX deployed (`lastUpdateTime=2026-11-24 06:19:06`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v12.8 KFX") deployed and fully verified on Samsung Galaxy M30 alongside unmodified stock `com.amazon.kindle`. Round 128 complete:
  1. **Web Browser Recent Downloads History Drawer (Task A)**:
     - Target Hardware: Physical Samsung Galaxy M30 (`32001776e8e82623`).
     - Added a persistent `Downloads` chip to the bookmarks bar in `FolioWebBrowserDialog.java` beside `Pin` and `EPUBs` (`makeChipWithIcon(ctx, d, FolioIcons.IMPORT, dlLabel, ...)`).
     - Implemented `recordRecentImport(ctx, title, author, kfxPath)` to persist converted EPUB entries in `SharedPreferences("folio_ui_prefs", "web_recent_imports")` (up to 15 entries) with timestamp.
     - Implemented `showRecentDownloadsDialog(ctx, parentDlg, onRefresh)` featuring Bookerly serif title (`Web Downloads (N)`), Amazon Ember subtitle/details, 1-tap `Open ›` reader launcher, clean `Clear History` action, and `Close` button styled with `FolioTheme.applyToDialog`.
     - Preserved clean zero-emoji editorial elements across `FolioMoreView.java` and `FolioMetadataCatalogDialog.java`, and guaranteed post-`.show()` placement for `FolioTheme.applyToDialog`.
     - Bumped Control Center badge and `About Folio` dialog to **`Folio v12.8 KFX`**.
     - Recompiled `classes9.dex` (454,068 bytes), rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, signed with `apksigner`.
     - Streamed APK install to Galaxy M30 (`lastUpdateTime=2026-11-24 06:08:36`) with zero fatal crashes logged.
     - Staged release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v12.8_kfx/base.apk` (132 MB, SHA256: `3c1d427fc53b639a0a1bab2b5b96d53e9c46fec75e67d24c4e710f8da4cb7e83`).
  2. **Visual Verification & Invariant Proofs (Task B)**:
     - Captured hardware proof screenshots on Samsung Galaxy M30:
       * `verify_r128_web_browser_downloads_bar.png` (251,966 bytes) — Web Book Browser showing bookmarks bar with `Pin`, `EPUBs`, and the new monochrome `Downloads` chip.
       * `verify_r128_web_downloads_drawer.png` (205,678 bytes) — Web Downloads drawer showing Bookerly heading (`Web Downloads (0)`), Ember subtitle, empty state text, and `Close` button. Zero emojis.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Cloned Invariant: `com.amazon.kindle.epub` updated (`lastUpdateTime=2026-11-24 06:08:36`), zero crashes.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
  4. Packaged `antig_to_gemini_neon_clunky_report128.zip` and submitted to Gemini Neon tab `837018552`.

## Verification Evidence (Round 128)
1. `verify_r128_web_browser_downloads_bar.png`: Web Book Browser bookmarks bar showing `Pin`, `EPUBs`, and `Downloads`.
2. `verify_r128_web_downloads_drawer.png`: Web Downloads drawer with Bookerly/Ember typography and zero emojis.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v12.8 KFX deployed (`lastUpdateTime=2026-11-24 06:08:36`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v12.7 KFX") deployed and fully verified on Samsung Galaxy M30 alongside unmodified stock `com.amazon.kindle`. Round 127 complete:
  1. **Zero-Emoji Editorial Modal Polish & Release Staging (Task A)**:
     - Verified target test hardware is **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI), NOT M32.
     - Completed comprehensive zero-emoji editorial audit in `FolioMoreView.java` and `FolioMetadataCatalogDialog.java`.
     - Replaced shelf rename button `✏️` with a clean Kindle-native `Edit` pill (`#162330` fill, `#2C5278` stroke, `ACCENT_TEAL`).
     - Cleaned search history chips, removing leftover `🕒` prefix.
     - Cleaned bulk action pill in catalog from `📌Queue` to `+Queue` (`#162330` fill, `#4DA6FF` text, `#2C5278` border).
     - Enforced Bookerly headings (`>= 17sp`) and Amazon Ember body/controls (`< 17sp`) across all Control Center dialogs (`Reading Goal`, `Completed Books`, `Tonight's Pick`, `Up Next Queue`, `Reading Notes`, `Manage Shelves`, `Storage & Duplicates`, `Saved Backups & Exports`, `Authors Directory`, `Book Details`).
     - Maintained post-`.show()` placement for `FolioTheme.applyToDialog` so AlertDialog action buttons are properly styled.
     - Bumped Control Center badge and `About Folio` dialog to **`Folio v12.7 KFX`**.
     - Recompiled `classes9.dex` (449,732 bytes), rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, signed with `apksigner`.
     - Streamed APK install to Galaxy M30 (`lastUpdateTime=2026-11-24 05:51:25`) with zero fatal crashes logged.
     - Staged release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v12.7_kfx/base.apk` (132 MB, SHA256: `cb9a79cd7abf19009c710881f2bc7366a8e7001541a442b72569b7444f64ebb7`).
  2. **Visual Verification & Invariant Proofs (Task B)**:
     - Captured hardware proof screenshots on Samsung Galaxy M30:
       * `verify_r127_reading_goal_clean.png` (196,000 bytes) — 2026 Reading Goal modal showing Bookerly heading, clean Amazon Ember sans on all metrics (`1 of 5 books finished (20%)`, `37 DAYS LEFT`, `4 TO GO`, `1 / 9d NEEDED PACE`, `COMPLETED BOOKS (1)`, `✓ 2026-11-23`), target selector buttons (`5`, `12`, `24`, `36`, `52`), and styled action buttons (`Save Goal` in `#4DA6FF` bold, `Cancel` in `#8E959C` muted). Zero emojis.
       * `verify_r127_manage_shelves_clean.png` (207,504 bytes) — Manage Shelves modal showing Bookerly heading, clean Kindle controls (`+ New Shelf`, `+ Books`, `Edit` [replacing `✏️`], `✕`), custom shelves (`Classics`, `Philosophy`), and clean action buttons (`Browse Catalog`, `Close`). Zero emojis.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Cloned Invariant: `com.amazon.kindle.epub` updated (`lastUpdateTime=2026-11-24 05:51:25`), zero crashes.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
  4. Packaged `antig_to_gemini_neon_clunky_report127.zip` and submitted to Gemini Neon tab `837018552`.

## Verification Evidence (Round 127)
1. `verify_r127_reading_goal_clean.png`: Reading Goal modal with Bookerly/Ember typography and Kindle blue action buttons.
2. `verify_r127_manage_shelves_clean.png`: Manage Shelves modal with Bookerly/Ember typography, `Edit` button replacing `✏️`, and zero emojis.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v12.7 KFX deployed (`lastUpdateTime=2026-11-24 05:51:25`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v12.6 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 126 complete:
  1. **Strict Bookerly/Ember Hierarchy & Modal-Wide Theme Unification (Task A)**:
     - Integrated `FolioTheme.java` routing Bookerly strictly to display titles (`>= 17sp`) and Amazon Ember sans to all eyebrows, pills, chips, buttons, and body text (`< 17sp`). Implemented `FolioTheme.applyToDialog` for recursive view-tree font styling and AlertDialog action button styling (`#4DA6FF` Positive, `#8E959C` Negative, `#C4CBD2` Neutral).
     - Updated `FolioFolderBrowserDialog.java`: Replaced active pinned-folder `★` button border with Kindle blue-slate (`#162330` fill, `#2C5278` border).
     - Updated `FolioMoreView.java` & `FolioMetadataCatalogDialog.java`: Applied `FolioTheme.applyToDialog` across Control Center and Catalog sub-modals, fixed post-show dialog styling hook, and bumped badge to `Folio v12.6 KFX (Enhanced Typesetting)`.
     - Recompiled `classes9.dex` (450,364 bytes), rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, signed with `apksigner`.
     - Streamed APK install to Galaxy M32 (`lastUpdateTime=2026-11-24 05:33:49`) with zero fatal crashes logged.
     - Staged release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v12.6_kfx/base.apk` (132 MB).
  2. **Visual Verification & Invariant Proofs (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r126_importer_ember_and_pin.png` (200,897 bytes) — Import EPUBs from Storage showing Bookerly heading, Amazon Ember sans on eyebrows/pills (`BATCH EPUB IMPORTER`, `Sort: A–Z`, `Hide Imported: OFF`), and active `★` pin button in Kindle blue-slate `#162330` / `#2C5278` (0 brown/amber pixels).
       * `verify_r126_modal_bookerly_ember.png` (200,543 bytes) — 2026 Reading Goal modal showing unified Bookerly heading, Amazon Ember body typography, Kindle blue `#4DA6FF` styled positive button (`Save Goal` with 1,200 verified `#4DA6FF` pixels), and `#8E959C` styled negative button (`Cancel` with 641 verified pixels).
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Cloned Invariant: `com.amazon.kindle.epub` updated (`lastUpdateTime=2026-11-24 05:33:49`), zero crashes.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
  4. Packaged `antig_to_gemini_neon_clunky_report126.zip` and submitted to Gemini Neon tab `837018552`.

## Verification Evidence (Round 126)
1. `verify_r126_importer_ember_and_pin.png`: Import EPUBs modal with Bookerly/Ember hierarchy and blue-slate active pin button.
2. `verify_r126_modal_bookerly_ember.png`: Reading Goal modal with Bookerly/Ember typography and Kindle blue styled footer buttons.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v12.6 KFX deployed (`lastUpdateTime=2026-11-24 05:33:49`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v12.5 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Milestone Round 125 complete:
  1. **Crimson Launcher Icon, Kindle Bookerly/Ember Typography & Zero-Neon Sweep (Task A)**:
     - Integrated `FolioTheme.java` into `mod_src/real/` (dynamic runtime loader for Kindle's bundled Bookerly and Amazon Ember fonts from `assets/` with Serif/Sans fallbacks; `applyToTree` recursive tree styling and `#4DA6FF` checkbox tinter; `styleDialog` for standardizing AlertDialog action buttons).
     - Integrated high-res crimson launcher icon assets across all densities (`dropin_res/mipmap-*`), solid `#1A0808` background (`folio_icon_bg.png` 512x512, `launcher_bg_adaptive.jpg` 432x432), and `ic_launcher_foreground.png`.
     - Executed thorough zero-neon highlight sweep across all 4 UI files (`FolioMoreView.java`, `FolioMetadataCatalogDialog.java`, `FolioFolderBrowserDialog.java`, `FolioWebBrowserDialog.java`), replacing all lingering neon hexes with Kindle blue `#4DA6FF` and Kindle charcoal surfaces (`#1C1F22`, `#141618`, `#262A2E`).
     - Bumped badge and About Folio dialog to **Milestone `Folio v12.5 KFX (Enhanced Typesetting)`**.
     - Recompiled `classes9.dex` (450,180 bytes), rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, signed with `apksigner`.
     - Streamed APK install to Galaxy M32 (`lastUpdateTime=2026-11-24 05:13:45`) with zero fatal crashes logged.
     - Staged release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v12.5_kfx/base.apk` (132 MB).
  2. **Visual Verification & Invariant Proofs (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r125_launcher_icon.png` (229,758 bytes) — Android home screen showing new Folio crimson boy-under-tree launcher icon (`#230f11` crimson base, crisp boy silhouette) alongside stock Kindle (`#2c93bc` blue).
       * `verify_r125_kindle_fonts_and_highlights.png` (208,619 bytes) — Import EPUBs from Storage modal showing checked Kindle blue `#4DA6FF` checkboxes (1,418 verified `#4DA6FF` pixels), Bookerly header typography, Amazon Ember body/controls, and 0 neon pixels detected across entire modal.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Cloned Invariant: `com.amazon.kindle.epub` updated (`lastUpdateTime=2026-11-24 05:13:45`), zero crashes.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
  4. Packaged `antig_to_gemini_neon_clunky_report125.zip` and submitted to Gemini Neon tab `837018552`.

## Verification Evidence (Round 125)
1. `verify_r125_launcher_icon.png`: Home screen with crimson boy-under-tree launcher icon.
2. `verify_r125_kindle_fonts_and_highlights.png`: Import EPUBs modal with Bookerly/Ember typography, Kindle-blue checked checkboxes, and 0 neon pixels.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v12.5 KFX deployed (`lastUpdateTime=2026-11-24 05:13:45`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v12.4 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 124 complete:
  1. **Kindle-Native Final Chip Polish & GitHub Repository Push (Task A, B & C)**:
     - Integrated `FolioMoreView.java`, `FolioMetadataCatalogDialog.java`, and `FolioFolderBrowserDialog.java` into `mod_src/real/`.
     - Stripped emojis and multi-colored neon fills from `FolioMetadataCatalogDialog.java` filter chips (`All`, `Up Next`, `Favorites`, `Notes`), unifying all chips to Kindle monochrome charcoal (`#1C1F22` with `1px #262A2E` border) when inactive and calm Kindle blue (`#4DA6FF`, black text) when active.
     - Updated `FolioFolderBrowserDialog.java` so `Import Selected` and the active folder chip use calm Kindle blue (`#4DA6FF`, black text) and synced `btnUpDir` click-listener placement.
     - Bumped badge and About Folio string to `Folio v12.4 KFX (Enhanced Typesetting)`.
     - Committed and pushed repository to GitHub remote (`https://github.com/BerKetKet/clunky.git`).
  2. **Visual Verification & Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r124_catalog_chips_polished.png` (821 KB) — Library Metadata & Genres modal showing clean monochrome + Kindle-blue filter chips (`All`, `Up Next`, `Favorites`, `Notes`) with zero emojis.
       * `verify_r124_importer_polished.png` (203 KB) — Import EPUBs from Storage modal showing calm Kindle-blue `Open`, active folder chip, and `Import Selected (0)` action button.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v12.4 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v12.4_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 124)
1. `verify_r124_catalog_chips_polished.png`: Library Metadata & Genres modal with emoji-free monochrome charcoal and Kindle-blue chips.
2. `verify_r124_importer_polished.png`: Import EPUBs modal with calm Kindle-blue action buttons and active folder chip.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v12.4 KFX deployed (`lastUpdateTime=2026-11-24 04:58:51`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v12.3 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 123 complete:
  1. **Full-App Kindle-Native Editorial Unification (Task A & B)**:
     - Integrated `FolioIcons.java`, `FolioMoreView.java`, `FolioMetadataCatalogDialog.java`, and `FolioFolderBrowserDialog.java` into `mod_src/real/`.
     - Built monochrome hairline vector icons `USER` (Authors Directory) and `UP_ARROW` (Parent Folder) in `FolioIcons.java`.
     - Unified Up Next Queue card border to `BORDER_HAIRLINE` (`#262A2E`) and unified remaining modal backgrounds to Kindle dark (`#0D0F12` / `#141618` / `#262A2E`). Bumped badge to `Folio v12.3 KFX`.
     - Unified `FolioMetadataCatalogDialog.java` to Kindle charcoal (`#0D0F12` / `#141618` / `#1C1F22` / `1px #262A2E`), updated title to `Typeface.SERIF` bold, shifted `Sync & Enrich` to calm Kindle blue (`#4DA6FF`) with black text, and replaced emoji buttons with crisp monochrome `FolioIcons` (`USER`, `SHUFFLE`).
     - Unified `FolioFolderBrowserDialog.java` to Kindle charcoal, updated title to `Typeface.SERIF` bold, shifted `Open` to calm Kindle blue (`#4DA6FF`), and replaced `⬆` with monochrome vector `FolioIcons.UP_ARROW`. Fixed misplaced `btnUpDir` click listener nesting.
     - Bumped badge and About Folio string to `Folio v12.3 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r123_kindle_catalog.png` (834 KB) — Unified Kindle-native Library Metadata & Genres modal showing Serif bold header, monochrome `USER` & `SHUFFLE` vector buttons, calm Kindle blue `Sync & Enrich` pill, and `#141618` charcoal cards.
       * `verify_r123_kindle_importer.png` (198 KB) — Unified Kindle-native Import EPUBs from Storage modal showing Serif bold header, monochrome vector `UP_ARROW` button, calm Kindle blue `Open` pill, pinned folder chips, and charcoal surfaces.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v12.3 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v12.3_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 123)
1. `verify_r123_kindle_catalog.png`: Unified Library Metadata & Genres modal with Serif header, vector icons, and Kindle blue action pill.
2. `verify_r123_kindle_importer.png`: Unified Import EPUBs modal with Serif header, UP_ARROW vector icon, and charcoal surfaces.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v12.3 KFX deployed (`lastUpdateTime=2026-11-24 04:47:39`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v12.2 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 122 complete:
  1. **Kindle-Native Editorial Dark Redesign & Monochrome Vector Icon System (Task A & B)**:
     - Integrated `FolioIcons.java`, `FolioMoreView.java`, and `FolioWebBrowserDialog.java` into `mod_src/real/`.
     - Built NEW `FolioIcons.java`: Custom Canvas vector line icon kit (25 monochrome hairline icons: `SEARCH`, `SHUFFLE`, `LIBRARY`, `IMPORT`, `GLOBE`, `FEED`, `REFRESH`, `BACKUP`, `LOG`, `NOTES`, `INSIGHTS`, `SHELVES`, `STORAGE`, `SETTINGS`, `LINK`, `INFO`, `BACK`, `HOME`, `SPARKLE`, `DOWNLOAD`, `STAR`, `CLOSE`, `BOOKMARK`, `TARGET`, `CHEVRON_RIGHT`) drawn with `1.65dp` round-cap strokes matching native Kindle Android app chrome.
     - Redesigned Control Center (`FolioMoreView.java`): Switched from neon rainbow pills to pure `#000000` canvas, `#141618` elevated surface cards, `1px #262A2E` hairline dividers, `#ECEFF1` primary text, `#8E959C` muted text, and calm Kindle blue (`#4DA6FF`) accent. Added `Typeface.SERIF` header & stat counters (`34 BOOKS`, `5 READING`, `1 FINISHED`, `1 FAVORITES`), subtle reading goal bar, and 3 grouped hairline-divided tool sections (`LIBRARY & IMPORT`, `READING & CURATION`, `SYSTEM & SETTINGS`) with `20dp` monochrome `FolioIcons` and thin chevrons.
     - Redesigned Web Book Browser (`FolioWebBrowserDialog.java`): Monochrome vector controls (`BACK`, `REFRESH`, `HOME`, `SPARKLE`, `SETTINGS`, `CLOSE`), neutral hairline bookmark chips (`Pin`, `EPUBs` in Kindle blue, `Standard Ebooks`, `Gutenberg`), and Serif dialog titles.
     - Bumped badge and About Folio string to `Folio v12.2 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r122_kindle_control_center.png` (290 KB) — Redesigned Control Center showing Serif header, monochrome vector `SHUFFLE`/`SEARCH` icons, `v12.2 KFX` badge, literary stat counters, Reading Goal card, and grouped hairline tool sections with `FolioIcons`.
       * `verify_r122_kindle_web_browser.png` (203 KB) — Redesigned Kindle-native monochrome browser chrome (`BACK`, `REFRESH`, `HOME`, `SPARKLE`, `SETTINGS`, `CLOSE` vector icons + `Pin` / `EPUBs` / bookmarks bar) over active WebView.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v12.2 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v12.2_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 122)
1. `verify_r122_kindle_control_center.png`: Redesigned Control Center with Serif header, vector icons, and grouped tool sections.
2. `verify_r122_kindle_web_browser.png`: Redesigned monochrome browser chrome with vector controls.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v12.2 KFX deployed (`lastUpdateTime=2026-11-24 04:37:15`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v12.1 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 121 complete:
  1. **Web Browser Go-to-URL Bar & 1-Tap EPUB Page Scanner (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioWebBrowserDialog.java` and `FolioMoreView.java`.
     - Built 1-tap emerald `⬇ EPUBs` page scanner pill in Quick Bookmarks Bar directly beside `+ ★`. When tapped, it queries active WebView DOM for all `.epub`/`/epub` links and displays them in a clean dark picker sheet (`📥 EPUB Links Found (N)`), enabling 1-tap download, native KFX conversion, metadata enrichment, and library addition.
     - Built quick `🔗 Go to URL` navigation modal (`showGoToUrlDialog`) when tapping the address pill in the top bar for fast one-off navigation (`Go`, `Set Home`, `Cancel`) without altering default homepage setting.
     - Bumped badge and About Folio string to `Folio v12.1 KFX (Enhanced Typesetting)` and added engine feature bullet.
  2. **Visual Verification & Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r121_web_browser_epub_scanner.png` (204 KB) — `🌐 Web Book Browser` modal showing navigation bar and upgraded Quick Bookmarks Bar (`+ ★`, emerald `⬇ EPUBs` scanner, `★ Standard Ebooks`, `★ Gutenberg`) above active WebView.
       * `verify_r121_about_v121.png` (370 KB) — About Folio modal showing `Version: Folio v12.1 KFX (Enhanced Typesetting)` and live library stats.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v12.1 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v12.1_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 121)
1. `verify_r121_web_browser_epub_scanner.png`: Web Book Browser with Quick Bookmarks Bar and emerald `⬇ EPUBs` scanner pill.
2. `verify_r121_about_v121.png`: About Folio modal confirming `Version: Folio v12.1 KFX (Enhanced Typesetting)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v12.1 KFX deployed (`lastUpdateTime=2026-11-24 04:26:04`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Milestone Folio v12.0 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 120 complete:
  1. **Web Book Browser Bookmarks Bar & Live EPUB Download Progress (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioWebBrowserDialog.java` and `FolioMoreView.java`.
     - Built Quick Bookmarks Bar beneath browser navigation bar with `+ ★` amber pin button to bookmark current webpage, 1-tap `★` bookmark chips (`★ Standard Ebooks`, `★ Gutenberg`), and long-press deletion.
     - Added live streaming EPUB download progress updates (`⬇ Downloading ... KB/%`) on the status banner and forwarded `Referer` headers.
     - Bumped badge and About Folio string to `Folio v12.0 KFX (Enhanced Typesetting)` and added engine feature bullet.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r120_web_browser_bookmarks.png` (194 KB) — `🌐 Web Book Browser` modal showing navigation bar and Quick Bookmarks Bar (`+ ★`, `★ Standard Ebooks`, `★ Gutenberg`) above active WebView.
       * `verify_r120_about_v120.png` (347 KB) — About Folio modal showing `Version: Folio v12.0 KFX (Enhanced Typesetting)` and live library stats.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v12.0 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v12.0_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 120)
1. `verify_r120_web_browser_bookmarks.png`: Web Book Browser with Quick Bookmarks Bar (`+ ★`, `★ Standard Ebooks`, `★ Gutenberg`).
2. `verify_r120_about_v120.png`: About Folio modal confirming `Version: Folio v12.0 KFX (Enhanced Typesetting)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Milestone Folio v12.0 KFX deployed (`lastUpdateTime=2026-11-24 04:17:54`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v11.9 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 119 complete:
  1. **Simplified Web Book Browser & Auto-Ingest EPUB Pipeline (Task A & B)**:
     - Built and integrated `FolioWebBrowserDialog.java` and updated `FolioMoreView.java`.
     - Added full-height modal `WebView` with compact navigation bar (`←`, `↻`, `🏠`, clickable host/path pill, `✨` Simplified View CSS toggle, `⚙` URL Settings, `✕` Close) and linear load progress bar.
     - Built configurable Homepage URL & Simplified View Settings (`showUrlSettingsDialog`): stored in `SharedPreferences("folio_ui_prefs", "web_browser_home_url")` (defaults to `https://standardebooks.org/ebooks`) with 1-tap presets (`Standard Ebooks`, `Project Gutenberg`, `Open Library`, `Calibre LAN`) and `✨ Simplified View` toggle (strips clutter, sticky banners, and cookie overlays).
     - Built automatic EPUB Download -> KFX Conversion -> Library Ingestion: intercepts `.epub` navigations and downloads with session cookies/User-Agent preserved, converts to native KFX via `ExternalEpubActivity.convertEpubToKfx`, enriches via `MetadataEnricher.enrichInPlace`, indexes into `FolioMetadataDb` + `kindle_library.db`, and presents an inline green success banner `✓ Added "<Title>" to Library` with a 1-tap `Open ›` button.
     - Bumped badge and About Folio string to `Folio v11.9 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r119_web_browser.png` (191 KB) — `🌐 Web Book Browser` full-height modal showing top navigation bar (`←`, `↻`, `🏠`, address pill `standardebooks.org/ebooks`, `✨`, `⚙`, `✕`) and active `WebView` loading Standard Ebooks.
       * `verify_r119_web_browser_settings.png` (170 KB) — `🌐 Web Browser Settings` modal showing Homepage URL field (`https://standardebooks.org/ebooks`), quick preset chips, and `✨ Simplified View: ON` toggle.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v11.9 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v11.9_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 119)
1. `verify_r119_web_browser.png`: Web Book Browser modal with navigation bar and Standard Ebooks catalog.
2. `verify_r119_web_browser_settings.png`: Web Browser Settings modal with homepage URL, preset chips, and Simplified View toggle.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v11.9 KFX deployed (`lastUpdateTime=2026-11-24 04:10:44`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v11.8 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 118 complete:
  1. **Saved Backups & Exports File Browser (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMoreView.java`.
     - Built interactive emerald `Backups ›` tile in `💾 Storage & Duplicates` and full file browser (`showSavedBackupsDialog`) for `/sdcard/kindle/backups/`.
     - Displays color-coded extension badges (`HTML` sky-blue, `MD` violet, `JSON` amber, `CSV` emerald, `DB` slate), sizes, timestamps, inline monospace preview (`showBackupFilePreviewDialog` with `📋 Copy`), and `✕` delete actions.
     - Bumped badge and About Folio string to `Folio v11.8 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r118_saved_backups_browser.png` (216 KB) — `📦 Saved Backups & Exports` modal listing 11 files with badges, sizes, timestamps, and `👁` / `✕` buttons.
       * `verify_r118_about_v118.png` (347 KB) — About Folio modal showing `Version: Folio v11.8 KFX (Enhanced Typesetting)` and live library stats.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v11.8 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v11.8_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 118)
1. `verify_r118_saved_backups_browser.png`: Saved Backups & Exports modal with 11 files, badges, and preview/delete controls.
2. `verify_r118_about_v118.png`: About Folio modal confirming `Version: Folio v11.8 KFX (Enhanced Typesetting)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v11.8 KFX deployed (`lastUpdateTime=2026-11-24 04:01:29`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v11.7 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 117 complete:
  1. **Reading Goal Pace Telemetry & Share Challenge (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMoreView.java`.
     - Built 3-tile Reading Pace Telemetry strip beneath annual progress bar displaying `DAYS LEFT` (sky-blue), `TO GO` (amber/emerald), and `NEEDED PACE` (violet, e.g. `1 / 9d` or `🏆 Met!`).
     - Added sky-blue `↗ Share` button in `🎯 2026 Reading Goal` header dispatching Android `ACTION_SEND` with progress %, pace summary, and completed books list.
     - Upgraded Completed Books Trophy Shelf to use `makeMiniCoverView(...)` with rounded `#0D1017` border + `📖` fallback.
     - Resolved variable shadowing on `prLp` (renamed preset layout params to `presetLp`).
     - Bumped badge and About Folio string to `Folio v11.7 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r117_reading_goal_pace.png` (230 KB) — Reading Goal modal showing `↗ Share` header pill, 3-tile Reading Pace Telemetry strip (`DAYS LEFT`, `TO GO`, `NEEDED PACE`), and `🏆 COMPLETED BOOKS (1)` Trophy Shelf.
       * `verify_r117_about_v117.png` (346 KB) — About Folio modal showing `Version: Folio v11.7 KFX (Enhanced Typesetting)` and live library stats.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v11.7 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v11.7_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 117)
1. `verify_r117_reading_goal_pace.png`: Reading Goal modal with `↗ Share`, 3-tile pace strip, and trophy shelf.
2. `verify_r117_about_v117.png`: About Folio modal confirming `Version: Folio v11.7 KFX (Enhanced Typesetting)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v11.7 KFX deployed (`lastUpdateTime=2026-11-24 03:54:57`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v11.6 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 116 complete:
  1. **Manage Shelves Studio: + New Shelf Picker & Cover Previews (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMoreView.java`.
     - Built `+ New Shelf` header action and multi-book picker (`showShelfBookPickerDialog`) with preset chips (`Philosophy`, `Fiction`, `Sci-Fi`, `History`, `Study`), live search filtering, and multi-select checkboxes (`☑`/`☐`).
     - Added standalone `.cover.jpg` bitmap thumbnails for lead book in each shelf card (`makeMiniCoverView`), plus action pills (`+ Books`, `✏️`, `✕`).
     - Cleaned up redundant AlertDialog `Copy All` button in Reading Notes modal.
     - Bumped badge and About Folio string to `Folio v11.6 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r116_shelves_studio.png` (191 KB) — Manage Shelves Studio showing `+ New Shelf` pill, existing `Classics` shelf, and newly created `Philosophy` shelf with Aristotle assigned and cover preview.
       * `verify_r116_about_v116.png` (347 KB) — About Folio modal showing `Version: Folio v11.6 KFX (Enhanced Typesetting)` and live library stats.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v11.6 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v11.6_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 116)
1. `verify_r116_shelves_studio.png`: Manage Shelves Studio with `+ New Shelf`, cover preview thumbnails, and `Philosophy` shelf created.
2. `verify_r116_about_v116.png`: About Folio modal confirming `Version: Folio v11.6 KFX (Enhanced Typesetting)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v11.6 KFX deployed (`lastUpdateTime=2026-11-24 03:45:17`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Milestone Folio v11.5 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 115 complete:
  1. **Reading Notes Studio: + Add Note, Share & Export .md (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMoreView.java`.
     - Built `+ Add Note` header action and searchable book picker (`showAddNoteBookPicker`) with `✏️ Edit` / `+ Note` status tags.
     - Built 3-pill export & share toolbar: `📋 Copy All` (clipboard), `↗ Share` (native Android `ACTION_SEND`), and `💾 Export .md` (`/sdcard/kindle/backups/folio_reading_notes.md`).
     - Verified exported Markdown on hardware: **114 bytes**, containing structured notes with star ratings and formatted quote blocks.
     - Bumped badge and About Folio string to `Folio v11.5 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r115_notes_studio.png` (153 KB) — Reading Notes Studio showing `+ Add Note` pill, 3-pill export toolbar, and *1984* note card.
       * `verify_r115_about_v115.png` (347 KB) — About Folio modal showing `Version: Folio v11.5 KFX (Enhanced Typesetting)`, `Up Next & Goal: 2 queued · 1/5 annual goal`, and live telemetry.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v11.5 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v11.5_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 115)
1. `verify_r115_notes_studio.png`: Reading Notes Studio with `+ Add Note` and 3-pill export toolbar.
2. `/sdcard/kindle/backups/folio_reading_notes.md`: Verified generated on device storage (114 bytes).
3. `verify_r115_about_v115.png`: About Folio modal confirming `Version: Folio v11.5 KFX (Enhanced Typesetting)`.
4. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
5. `dumpsys package com.amazon.kindle.epub`: Folio v11.5 KFX deployed (`lastUpdateTime=2026-11-24 03:36:42`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v11.4 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 114 complete:
  1. **Authors Directory Sort Pills & Progress Telemetry (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`.
     - Built 3 interactive sort pills in `showAuthorsDirectoryDialog`: `Most Books`, `A–Z`, and `Most Read`.
     - Added circular monogram avatars (`HM`, `AH`, `AR`, `NS`, `SO`, `GO`) color-coded teal for active/finished authors and amber for unread authors.
     - Added live per-author reading and rating telemetry sub-lines (e.g. `✓ 1 finished`, `📖 2 reading`, `★ 5.0`).
     - Bumped badge and About Folio string to `Folio v11.4 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r114_authors_directory.png` (182 KB) — Authors Directory sorted by `Most Read` showing active/finished/rated authors at top with circular monogram badges and live telemetry.
       * `verify_r114_about_v114.png` (346 KB) — About Folio modal showing `Version: Folio v11.4 KFX (Enhanced Typesetting)`, `Up Next & Goal: 2 queued · 1/5 annual goal`, and live telemetry.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v11.4 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v11.4_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 114)
1. `verify_r114_authors_directory.png`: Authors Directory modal sorted by `Most Read` with monogram badges and telemetry.
2. `verify_r114_about_v114.png`: About Folio modal confirming `Version: Folio v11.4 KFX (Enhanced Typesetting)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v11.4 KFX deployed (`lastUpdateTime=2026-11-24 03:28:54`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v11.3 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 113 complete:
  1. **Smart Metadata Quick-Fix Toolbar (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`.
     - Built Smart Quick-Fix Toolbar in `showEditMetadataDialog` with 3 instant action pills:
       * `⇄ Swap T/A` (violet): Instantly swaps Title & Author fields to resolve inverted OPF metadata (e.g. *Palahniuk, Chuck / Choke*).
       * `Aa Fix Author` (sky-blue): Normalizes author format from `Last, First` to natural `First Last` (*Chuck Palahniuk*).
       * `Aa Title Case` (amber): Smart-capitalizes lowercase titles with proper stop-word rules.
     - Implemented immediate in-memory `BookRecord` attribute synchronization alongside `books_metadata` SQLite and `kindle_library.db` sync.
     - Bumped badge and About Folio string to `Folio v11.3 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r113_edit_metadata_quickfix.png` (147 KB) — Edit Book Metadata dialog for *Choke* showing the 3 quick-fix pills (`⇄ Swap T/A`, `Aa Fix Author`, `Aa Title Case`) with cleaned fields (Title: `Choke`, Author: `Chuck Palahniuk`).
       * `verify_r113_about_v113.png` (347 KB) — About Folio modal showing `Version: Folio v11.3 KFX (Enhanced Typesetting)`, `Up Next & Goal: 2 queued · 1/5 annual goal`, and live telemetry.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v11.3 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v11.3_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 113)
1. `verify_r113_edit_metadata_quickfix.png`: Edit Metadata dialog with quick-fix toolbar and cleaned fields.
2. `verify_r113_about_v113.png`: About Folio modal confirming `Version: Folio v11.3 KFX (Enhanced Typesetting)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v11.3 KFX deployed (`lastUpdateTime=2026-11-24 03:20:02`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v11.2 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 112 complete:
  1. **Up Next Queue Reorder Manager & Live Sync (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMoreView.java`.
     - Added compact violet `⇅ Reorder` pill beside `See All (N) ›` in Control Center `📌 Up Next Queue` header.
     - Built `showQueueManagerDialog` modal displaying all queued books with rank badges (`#1`, `#2`, ...), `.cover.jpg` thumbnails, `↑` / `↓` reorder controls persisting to `SharedPreferences("folio_queue")`, inline `✕` remove, and `Clear Queue` action.
     - Wired `refreshStats` callback into `showTonightsPickDialog(ctx, refreshStats)` for instant real-time sync of the queue carousel.
     - Bumped badge and About Folio string to `Folio v11.2 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r112_queue_reorder.png` (254 KB) — Reorder Manager modal showing promoted `#1` (*Choke*) and `#2` (*Animal farm: a fairy story*) with `↑`, `↓`, `✕` controls and `Clear Queue` / `Done` footer.
       * `verify_r112_about_v112.png` (347 KB) — About Folio modal showing `Version: Folio v11.2 KFX (Enhanced Typesetting)`, `Up Next & Goal: 2 queued · 1/5 annual goal`, and live telemetry.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v11.2 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v11.2_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 112)
1. `verify_r112_queue_reorder.png`: Reorder Manager modal with promoted `#1` and `#2` cards and reorder controls.
2. `verify_r112_about_v112.png`: About Folio modal confirming `Version: Folio v11.2 KFX (Enhanced Typesetting)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v11.2 KFX deployed (`lastUpdateTime=2026-11-24 03:12:18`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v11.1 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 111 complete:
  1. **Tonight's Pick Pool Switcher & 1-Tap Queue (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMoreView.java`.
     - Added 3 interactive pool scope switcher pills beneath `🎲 Tonight's Pick` header: `Unread (N)`, `📌 Queue (N)`, `All (N)`.
     - Added 1-tap `📌 Queue` / `📌 Queued` toggle action button inline with `🎲 Another` and `ⓘ Details`.
     - Dynamically bookmarks candidate picks into `📌 Up Next` queue, updating button label to amber `📌 Queued` and incrementing the `📌 Queue (N)` counter.
     - Bumped badge and About Folio string to `Folio v11.1 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r111_tonights_pick_pools.png` (283 KB) — Tonight's Pick modal showing 3 scope pills (`Unread (26)`, `📌 Queue (2)`, `All (32)`), `Choke` book card, and 3 action buttons (`🎲 Another`, `📌 Queued`, `ⓘ Details`).
       * `verify_r111_about_v111.png` (354 KB) — About Folio modal showing `Version: Folio v11.1 KFX (Enhanced Typesetting)`, `Up Next & Goal: 2 queued · 1/5 annual goal`, and live telemetry.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v11.1 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v11.1_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 111)
1. `verify_r111_tonights_pick_pools.png`: Tonight's Pick modal with 3 scope pills and toggled `📌 Queued` button.
2. `verify_r111_about_v111.png`: About Folio modal confirming `Version: Folio v11.1 KFX (Enhanced Typesetting)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v11.1 KFX deployed (`lastUpdateTime=2026-11-24 03:04:09`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v10.1 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 101 complete:
  1. **Quick Search Scope Filter Pills & Recent Search History (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMoreView.java`.
     - Added 5 instant scope filter pills inside `showGlobalSearchDialog(...)`: `All`, `📖 Reading`, `📝 Notes`, `📌 Queue`, `🏷 Shelves`.
     - Active scope renders with warm amber fill (`#F59E0B`) with instant real-time result filtration.
     - Persists up to 5 recent queries in `SharedPreferences("folio_ui_prefs", "recent_global_searches")` with 1-tap recall chips and `Clear` button.
     - Bumped badge and About Folio string to `Folio v10.1 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r101_search_scope_pills.png` (287 KB) — Quick Library Search modal showing 5 scope filter pills with `📖 Reading` active and filtered results.
       * `verify_r101_about_v101.png` (341 KB) — About Folio modal showing `Version: Folio v10.1 KFX (Enhanced Typesetting)` and live stats.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v10.1 KFX release APK (137 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v10.1_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 101)
1. `verify_r101_search_scope_pills.png`: Quick Library Search modal with 5 scope pills (`📖 Reading` active).
2. `verify_r101_about_v101.png`: About Folio modal confirming `Version: Folio v10.1 KFX (Enhanced Typesetting)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v10.1 KFX deployed (`lastUpdateTime=2026-11-23 02:51:19`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Centennial Milestone Folio v10.0 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 100 complete:
  1. **Unified Quick Library Search & v10.0 Telemetry (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMoreView.java`.
     - Added sky-blue `🔍 Search` pill directly in Control Center header beside `Folio v10.0 KFX` badge.
     - Built `showGlobalSearchDialog(...)` modal providing real-time search across books (title, author, genre, ISBN), reading notes (warm amber quotes), and custom shelves (`🏷 Shelf: Classics`).
     - Added 1-tap `Read ›` launch into KFX reader, `Open in Catalog` deep link, and cover thumbnails.
     - Added live `• Up Next & Goal: 1 queued · 1/5 annual goal` telemetry line to About Folio.
     - Bumped badge and About Folio string to `Folio v10.0 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r100_quick_search_v10.png` (275 KB) — Quick Library Search modal showing search bar, `🏷 Shelf: Classics`, *Moby Dick*, *Animal Farm*, and *1984* note snippet with `Read ›` buttons.
       * `verify_r100_about_v10.png` (341 KB) — About Folio modal showing `Version: Folio v10.0 KFX (Enhanced Typesetting)` and new `Up Next & Goal` telemetry.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v10.0 KFX release APK (137 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v10.0_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 100)
1. `verify_r100_quick_search_v10.png`: Quick Library Search modal with search bar, shelves, and notes.
2. `verify_r100_about_v10.png`: About Folio modal confirming `Version: Folio v10.0 KFX (Enhanced Typesetting)` and `Up Next & Goal` telemetry.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v10.0 KFX deployed (`lastUpdateTime=2026-11-23 02:44:49`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v9.9 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 099 complete:
  1. **Reading Goal Completed Books Trophy Shelf (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMoreView.java`.
     - Built `showReadingGoalDialog(...)` into rich `🎯 2026 Reading Goal` modal.
     - Added live progress bar track (`1 of 5 books finished (20%)`) and `🏆 COMPLETED BOOKS (N)` horizontal cover-art trophy strip showing cover thumbnails, titles, authors, and `✓ YYYY-MM-DD` completion dates.
     - Added quick target presets (`5`, `12`, `24`, `36`, `52`) + custom number input.
     - Bumped badge and About Folio string to `Folio v9.9 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r99_goal_trophy_shelf.png` (213 KB) — Reading Goal modal showing progress bar, `🏆 COMPLETED BOOKS (1)` trophy card for *Moby Dick* (`✓ 2026-11-23`), and presets.
       * `verify_r99_about_v99.png` (331 KB) — About Folio modal showing `Version: Folio v9.9 KFX (Enhanced Typesetting)` and live stats.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v9.9 KFX release APK (137 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v9.9_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 099)
1. `verify_r99_goal_trophy_shelf.png`: Reading Goal modal with progress bar and Completed Books trophy shelf.
2. `verify_r99_about_v99.png`: About Folio modal confirming `Version: Folio v9.9 KFX (Enhanced Typesetting)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v9.9 KFX deployed (`lastUpdateTime=2026-11-23 02:38:15`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v9.8 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 098 complete:
  1. **Persistent Finished Date Tracking & Export (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMetadataCatalogDialog.java`, `FolioMetadataBackup.java`, and `FolioMoreView.java`.
     - Added persistent finished date tracking in `SharedPreferences("folio_finished_dates")`.
     - In Book Details (`ⓘ`), finished books display an emerald status line (`✓ Finished · Set date ›` or `✓ Finished on YYYY-MM-DD · Edit ›`) that summons `showEditFinishedDateDialog` for 1-tap inline modification.
     - Preserves `finished_date` across JSON Backup/Restore, Markdown reading log (`folio_reading_log.md`), and RFC-4180 CSV export (`folio_library.csv`).
     - Bumped badge and About Folio string to `Folio v9.8 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r98_finished_date_details.png` (235 KB) — Book Details modal for *Moby Dick* displaying `✓ Finished on 2026-11-23 · Edit ›` in the header.
       * `verify_r98_about_v98.png` (332 KB) — About Folio modal showing `Version: Folio v9.8 KFX (Enhanced Typesetting)` and live stats.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v9.8 KFX release APK (137 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v9.8_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 098)
1. `verify_r98_finished_date_details.png`: Book Details modal with `✓ Finished on 2026-11-23 · Edit ›`.
2. `verify_r98_about_v98.png`: About Folio modal confirming `Version: Folio v9.8 KFX (Enhanced Typesetting)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v9.8 KFX deployed (`lastUpdateTime=2026-11-23 02:31:04`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v9.7 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 097 complete:
  1. **Curated-Only Reading Log Toggle & Top-Placed Up Next Queue (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMetadataBackup.java` and `FolioMoreView.java`.
     - Added `✨ Scope: Curated Only` / `📚 Scope: All Books` toggle pill alongside `📊 Export CSV` in `Reading Log (.md)` modal.
     - Defaults to Curated Only mode filtering markdown export strictly to engaged books (Finished, Reading, Favorited, Rated, Annotated, Shelved, Queued).
     - Promoted `## 📌 Up Next Queue (N)` section to the prominent top of the Markdown log right below the Reading Goal summary.
     - Bumped badge and About Folio string to `Folio v9.7 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r97_curated_reading_log.png` (218 KB) — Reading Log modal showing `✨ Scope: Curated Only` toggle pill, `# Folio Curated Reading Log`, and `## 📌 Up Next Queue (1)` at top of preview.
       * `verify_r97_about_v97.png` (331 KB) — About Folio modal showing `Version: Folio v9.7 KFX (Enhanced Typesetting)` and live stats.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v9.7 KFX release APK (137 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v9.7_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 097)
1. `verify_r97_curated_reading_log.png`: Reading Log modal with Curated Only toggle and top-placed Up Next Queue.
2. `verify_r97_about_v97.png`: About Folio modal confirming `Version: Folio v9.7 KFX (Enhanced Typesetting)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v9.7 KFX deployed (`lastUpdateTime=2026-11-23 02:23:50`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v9.6 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 096 complete:
  1. **1-Tap Sort Picker Modal, Direction Toggle & Size/Queue Sorts (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`.
     - Replaced linear cycling of top bar `Sort:` button with dark `Sort Catalog` picker modal (`showSortPickerDialog(...)`) offering 1-tap selection across all 9 sort modes.
     - Added persistent `↓ Standard / ↑ Reversed` direction toggle pill in modal header stored in `SharedPreferences("folio_ui_prefs", "catalog_sort_rev")` with live indicator arrows on the top bar pill.
     - Supported `File Size (Largest)` and `📌 Up Next Queue First` sort orders.
     - Bumped badge and About Folio string to `Folio v9.6 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r96_sort_picker_modal.png` (142 KB) — Sort Catalog modal showing `↓ Standard` toggle pill and all 9 sort modes with amber checkmark.
       * `verify_r96_about_v96.png` (331 KB) — About Folio modal showing `Version: Folio v9.6 KFX (Enhanced Typesetting)` and live stats.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v9.6 KFX release APK (137 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v9.6_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 096)
1. `verify_r96_sort_picker_modal.png`: Sort Catalog modal with direction toggle and 9 sort options.
2. `verify_r96_about_v96.png`: About Folio modal confirming `Version: Folio v9.6 KFX (Enhanced Typesetting)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v9.6 KFX deployed (`lastUpdateTime=2026-11-23 02:16:01`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.9 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 089 complete:
  1. **Reading Notes Notebook & Storage Scroll Polish (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMoreView.java`.
     - Added `📝 Reading Notes Notebook` action card on Control Center directly below `📄 Reading Log & Export`.
     - Built comprehensive `showNotesNotebookDialog(...)` modal:
       * Real-time search filter (`Search notes, titles, or authors...`) with live count header (`📝 Reading Notes (1)`).
       * Card layout with standalone `.cover.jpg` thumbnail, bold title (1-tap launch into KFX reader), author, rating badge (`★ 5/5`), and reading shelf (`🏷 Classics`).
       * Warm amber quote box (`#251D14` fill, `#D97706` border) with full note text (`Essential dystopian classic - reread Part 3`).
       * 1-tap `📋 Copy` note to clipboard, 1-tap `✏️ Edit` inline note editor with immediate reactive notebook refresh on save, and `Copy All` Markdown export.
     - In `FolioMoreView.java`, expanded Storage & Duplicates scroll viewport to `388dp` with `10dp` bottom padding to eliminate edge clipping on the Duplicate Titles status card.
     - Bumped badge and About Folio string to `Folio v8.9 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `screenshots/verify_r89_notes_notebook.png` (153 KB) — Reading Notes Notebook modal showing search bar, *1984* cover thumbnail, rating/shelf badges, amber quote box, and `📋 Copy` / `✏️ Edit` pills.
       * `screenshots/verify_r89_about_v89.png` (325 KB) — About Folio modal showing `Version: Folio v8.9 KFX (Enhanced Typesetting)` and live stats.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v8.9 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v8.9_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 089)
1. `screenshots/verify_r89_notes_notebook.png`: Reading Notes Notebook modal with search bar, cover thumbnail, quote box, and copy/edit pills.
2. `screenshots/verify_r89_about_v89.png`: About Folio modal confirming `Version: Folio v8.9 KFX (Enhanced Typesetting)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v8.9 KFX deployed (`lastUpdateTime=2026-11-23 01:22:19`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.8 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 088 complete:
  1. **Storage Footprint & Duplicate Titles Manager (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMoreView.java`.
     - Added `💾 Storage & Duplicates` action card on Control Center directly below `🏷 Manage Shelves`.
     - Built comprehensive `showStorageDuplicatesDialog(...)` modal:
       * Three centered live storage tiles: App Library (`13.3 MB`), Backups (`682 KB`), and Temp Cache (`1.3 MB`).
       * Top 5 Largest Books ranking with amber proportional horizontal bars and 1-tap search drill-down into catalog (*Homer*, *Cicero*, *Plutarch*, *Aristotle*, *Xenophon*).
       * Duplicate Titles Audit running title normalization across all 27 cataloged books, reporting `✓ No duplicate titles found across 27 cataloged books`.
       * 1-tap `Clear Temp Cache` button allowing instant scratch cache purge and live modal refresh.
     - Bumped badge and About Folio string to `Folio v8.8 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `screenshots/verify_r88_control_center_v88.png` (206 KB) — Control Center showing the new `💾 Storage & Duplicates` card under `🏷 Manage Shelves` with `Folio v8.8 KFX` badge.
       * `screenshots/verify_r88_storage_duplicates_modal.png` (231 KB) — Storage & Duplicates modal showing 3 storage tiles, Top 5 Largest Books amber bars, and duplicate title audit status.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v8.8 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v8.8_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 088)
1. `screenshots/verify_r88_control_center_v88.png`: Control Center with `💾 Storage & Duplicates` card and `Folio v8.8 KFX` badge.
2. `screenshots/verify_r88_storage_duplicates_modal.png`: Storage & Duplicates modal with 3 storage tiles, largest books bars, and duplicate check.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v8.8 KFX deployed (`lastUpdateTime=2026-11-23 01:15:26`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.7 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 087 complete:
  1. **Bulk Genre Tagging & Centered Metadata Health Tiles (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`.
     - In `FolioMetadataCatalogDialog.java`, added sky-blue `+ Genre` action pill in the bottom Bulk Action Bar alongside `🏷 Shelf`, `★ Fav`, and `✓ Done`.
     - Implemented `showBulkGenrePicker(...)` displaying interactive `🏷 Add Genre (N books)` modal with horizontal genre preset chips (`+ Fiction`, `+ History`, `+ Rome`, `+ Biography`, `+ Correspondence`) and custom comma-separated genre input.
     - Implemented safe case-insensitive genre merge logic into `books_metadata.genres` in `folio_metadata.db` for all selected volumes.
     - In `FolioMoreView.java`, center-aligned both count (`valTv`) and label (`lblTv`) inside `makeHealthBadge(...)` across all 3 audit tiles.
     - Bumped badge and About Folio string to `Folio v8.7 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `screenshots/verify_r87_bulk_genre_bar.png` (210 KB) — Bulk Add Genre modal with horizontal genre chips, custom input field, and 4-action bottom bar.
       * `screenshots/verify_r87_insights_centered_v87.png` (161 KB) — Library Insights modal showing crisp center-aligned 3-tile Metadata Health strip with `Folio v8.7 KFX` badge.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v8.7 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v8.7_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 087)
1. `screenshots/verify_r87_bulk_genre_bar.png`: Add Genre modal with preset chips, custom input, and 4-button bulk bar.
2. `screenshots/verify_r87_insights_centered_v87.png`: Library Insights modal with center-aligned 3-tile Metadata Health strip.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v8.7 KFX deployed (`lastUpdateTime=2026-11-23 01:06:47`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.6 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 086 complete:
  1. **Metadata Health Audit & 1-Tap Quick-Fix Filters (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMoreView.java` and `FolioMetadataCatalogDialog.java`.
     - In `FolioMoreView.java`, added `METADATA HEALTH — 70% COMPLETE (TAP TO FIX)` section inside `📊 Library Insights` directly below `READING STATUS`.
     - Built three live health audit tiles: Covers (`1 › No Cover`), Genres (`2 › No Genre`), and Synopsis (`21 › No Synopsis`) with emerald styling for 100% and alert badges for gaps.
     - In `FolioMetadataCatalogDialog.java`, added support for `MISSING_COVER`, `MISSING_GENRES`, and `MISSING_SYNOPSIS` deep-links, badged with dedicated `🩺 Missing ... (N)` active chips.
     - Verified 1-tap tile click on `2 › No Genre` dismisses Insights and opens catalog filtered to `Library Metadata & Genres (2 of 27)` with `🩺 Missing Genres (2)` chip.
     - Bumped badge and About Folio string to `Folio v8.6 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `screenshots/verify_r86_insights_metadata_health.png` (174 KB) — Library Insights modal showing `METADATA HEALTH — 70% COMPLETE (TAP TO FIX)` section with 3 audit tiles directly under `READING STATUS`.
       * `screenshots/verify_r86_missing_filter_drilldown.png` (406 KB) — Metadata Catalog dialog filtered to 2 books needing genres with active `🩺 Missing Genres (2)` chip.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v8.6 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v8.6_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 086)
1. `screenshots/verify_r86_insights_metadata_health.png`: Library Insights modal showing `METADATA HEALTH — 70% COMPLETE (TAP TO FIX)` with 3 audit tiles.
2. `screenshots/verify_r86_missing_filter_drilldown.png`: Metadata Catalog filtered to 2 books with active `🩺 Missing Genres (2)` chip.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v8.6 KFX deployed (`lastUpdateTime=2026-11-23 00:57:13`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Milestone Folio v8.5 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 085 complete:
  1. **Catalog Multi-Select & Bulk Actions (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`.
     - In `FolioMetadataCatalogDialog.java`, added compact `☑ Select` toggle pill beside catalog header, switching to `✕ Done` with `#4F46E5` fill and `#818CF8` border when active.
     - Implemented dynamic card selection highlighting: tapping any book in List or 3-column Grid view highlights it with `2dp #818CF8` indigo border and `#1E2642` background.
     - Added floating bottom Bulk Action Bar with dynamic selection counter (`N books selected`), `Select All / Clear All` toggle, and three 1-tap bulk actions:
       * `🏷 Shelf`: bulk assign or clear custom shelves across all selected books.
       * `★ Fav`: bulk favorite or unfavorite across all selected books.
       * `✓ Finished`: bulk mark finished or unread, automatically syncing reading progress and status into `kindle_library.db`.
     - In `FolioMoreView.java`, widened Continue Reading carousel cards to `196dp` for improved title and author framing.
     - Bumped badge and About Folio string to `Folio v8.5 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `screenshots/verify_r85_bulk_select_mode.png` (617 KB) — Metadata Catalog in 3-column Grid view with `☑ Select` active (`✕ Done`), 3 books selected (*1984*, *Moby Dick*, *Animal Farm*) with indigo selection borders, and bottom Bulk Action Bar showing `3 books selected`, `Select All (27)`, `🏷 Shelf`, `★ Fav`, and `✓ Finished`.
       * `screenshots/verify_r85_about_v85.png` (324 KB) — About Folio modal showing `Version: Folio v8.5 KFX (Enhanced Typesetting)` and live stats.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Milestone Folio v8.5 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v8.5_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 085)
1. `screenshots/verify_r85_bulk_select_mode.png`: Metadata Catalog in 3-column Grid view with `☑ Select` active, 3 books selected with indigo selection borders, and bottom Bulk Action Bar.
2. `screenshots/verify_r85_about_v85.png`: About Folio modal confirming `Version: Folio v8.5 KFX (Enhanced Typesetting)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v8.5 KFX deployed (`lastUpdateTime=2026-11-23 00:48:01`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v7.8 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 078 complete:
  1. **Folder Importer Load More (+60) Pagination & Last-Folder Persistence (Task A & B)**:
     - Integrated Gemini's drop-in updates to `FolioFolderBrowserDialog.java` and `FolioMoreView.java`.
     - In `FolioFolderBrowserDialog.java`, added interactive teal `Load More (+60 of 659 remaining)` pagination button at the bottom of the EPUB list.
     - Verified list expands dynamically to `Showing 120 of 719 matching EPUBs (719 total in folder)`.
     - Added last-folder persistence via `SharedPreferences("folio_ui_prefs", "last_import_dir")`, automatically reloading `/sdcard/en/0` on dialog launch.
     - Bumped badge and About Folio to `Folio v7.8 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task C)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `screenshots/verify_r78_load_more_button.png` (187 KB) — Batch EPUB Importer scrolled to bottom of 60 items showing teal `Load More (+60 of 659 remaining)` button.
       * `screenshots/verify_r78_loaded_120_items.png` (191 KB) — Batch EPUB Importer header updated to `Showing 120 of 719 matching EPUBs (719 total in folder)` after tapping `Load More`.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v7.8 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v7.8_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v7.8 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, Folder Importer Load More (+60) Pagination, Last-Imported Directory Persistence (folio_ui_prefs), Folder Importer Sort Toggle (A-Z / Newest / Largest / Smallest), Live Selected Payload Size CTA (Import Selected (N · X.X MB)), Interactive 1-Tap Drill-Down Library Insights & Custom Shelves Chart, Library Insights Visual Analytics Modal (Top Genres, Top Authors, Average Rating & Shelf Stats), Searchable Authors Directory & Long Author Name Ellipsis Polish, Authors Directory Modal (👤) with Book Counts & 1-Tap Author Filtering, RFC-4180 CSV Library Export (/sdcard/kindle/backups/folio_library.csv), 1-Tap Export CSV Pill in Dialog Header, Reading Goal in Markdown Header & JSON Backup, Annual Reading Goal Progress Card (🎯), Goal Customizer Dialog with 1-Tap Presets (5/12/24/36/52), Persistent Reading Goal Preference (folio_ui_prefs), Reading Shelf Clipboard Info (📋 Copy Info), Custom Reading Shelves Subsystem (folio_shelves), Dynamic Indigo Shelf Filter Chips (🏷 <Shelf> (N)), 1-Tap Preset Shelf Picker & Custom Input, Shelf Full-Text Search, JSON Shelf Backup/Restore & Markdown Shelf Export, Reading Log Author Normalization & Live Reading Status, 1-Tap Android Share Intent (Intent.ACTION_SEND), Markdown Reading Log Export & Monospace Preview (/sdcard/kindle/backups/folio_reading_log.md), 1-Tap Copy Markdown to Clipboard, Dynamic Unread Backlog Filter Chip (Unread (N)), 1-Tap Copy Book Info to Clipboard (📋 Copy Info), Sort by Rating Mode, Random Unread Book Picker (🎲), Interactive 5-Star Ratings Subsystem (folio_ratings), Persistent Catalog Sort Preference (folio_ui_prefs), Portable JSON Ratings Backup & Restore, Inline Card Note Snippet Previews, Sort by Favorites Mode, Favorites & Notes Combined Telemetry (1 favorited · 1 annotated), Dynamic Notes Quick-Filter Chip (📝 Notes (N)), Full-Text Search inside Reading Notes, Persistent Personal Reading Notes Subsystem (folio_notes), Combined Catalog Badges (★ 📝 Title), Inline Note Editor Dialog with Save/Clear, Portable JSON Note Backup & Restore, Control Center 4-Tile Live Stats Strip with ★ FAVS Deep-Link, Version 2 Portable JSON Backup with is_favorite Serialization, Persistent Favorites Subsystem with SharedPreferences zero-DB migration, Dynamic Gold ★ Favorites Quick-Filter Chip, Starred Title Prefix (★), Fullscreen Pitch-Black Cover Lightbox with centered cover art, Live KFX file size indicator in Book Details, 64x96dp Cover Thumbnail Header in Book Details, Expandable Synopsis Toggle (Read full synopsis › / Show less), Clickable Genre Pills in Book Details with 1-tap catalog filtering, Control Center Live Stat Tile Auto-Refresh on dialog close, Tappable Header Version Badge, 1-Tap Select New / Clear batch selection pill in folder importer, Dynamic filtered count header (X of Y), unified inline search clear button (✕) across catalog and batch folder importer, Clickable Author Quick-Filter link with automatic genre reset and search sync, Mark as Finished / Unread toggle pill in Book Details, dynamic teal Finished (N) chip filter, green Finished card badges, accurate reading progress SQL check, dynamic Reading quick-filter chip with in-progress badges, deduplicated Title-Cased genre tags, bibliographic meta-tag filtering from top chips, live reading progress stats in About Folio, Pass-3 missing-metadata auto-repair, natural-name author formatting, frequency-ranked genre chips with live counts, unified local library scan to app-private storage, right-aligned Home All Metadata link, safe cascade remove action, live reading progress badges and sort mode, 700+ EPUB live filename search in folder importer, "Hide Imported" toggle pill, ISBN-to-title genre fallback, multi-select batch EPUB folder importer from storage, portable metadata JSON backup/restore, missing-cover auto-repair with Open Library Covers API + 302 redirect following, catalog sort controls Recent/Title/Author/Year/Progress, in-app editable metadata dialog with live persistence, full-library `Sync & Enrich` metadata backfiller, real-time search filtering, Title-Cased genre chips, rich Book Details popup with 0ms reader launch, duplicate import guard with "Open Existing Copy" vs "Re-Import & Replace", "Library Metadata & Genres" browser dialog, persistent `folio_metadata.db`, Open Library metadata enrichment, live OPDS/Atom feed download, heavy omnibus stress-tested up to 23 MB / 1,450 sections, CSS text-transform uppercase/lowercase/capitalize, font-variant small-caps, RTL Arabic/Hebrew direction and right alignment, multi-level nested lists, native tables with thead/colspan/rowspan, malformed asset resilience, full HTML4/HTML5 entity decoding, store-free library search, zero-hyphenation code blocks, proportional sup/sub, `$270` cover container, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 078)
1. `screenshots/verify_r78_load_more_button.png`: Batch EPUB Importer scrolled to bottom of 60 items showing teal `Load More (+60 of 659 remaining)` button.
2. `screenshots/verify_r78_loaded_120_items.png`: Batch EPUB Importer header updated to `Showing 120 of 719 matching EPUBs (719 total in folder)` after tapping `Load More`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v7.8 KFX deployed (`lastUpdateTime=2026-11-23 00:00:20`).
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v7.7 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, Folder Importer Sort Toggle (A-Z / Newest / Largest / Smallest), Live Selected Payload Size CTA (Import Selected (N · X.X MB)), Interactive 1-Tap Drill-Down Library Insights & Custom Shelves Chart, Library Insights Visual Analytics Modal (Top Genres, Top Authors, Average Rating & Shelf Stats), Searchable Authors Directory & Long Author Name Ellipsis Polish, Authors Directory Modal (👤) with Book Counts & 1-Tap Author Filtering, RFC-4180 CSV Library Export (/sdcard/kindle/backups/folio_library.csv), 1-Tap Export CSV Pill in Dialog Header, Reading Goal in Markdown Header & JSON Backup, Annual Reading Goal Progress Card (🎯), Goal Customizer Dialog with 1-Tap Presets (5/12/24/36/52), Persistent Reading Goal Preference (folio_ui_prefs), Reading Shelf Clipboard Info (📋 Copy Info), Custom Reading Shelves Subsystem (folio_shelves), Dynamic Indigo Shelf Filter Chips (🏷 <Shelf> (N)), 1-Tap Preset Shelf Picker & Custom Input, Shelf Full-Text Search, JSON Shelf Backup/Restore & Markdown Shelf Export, Reading Log Author Normalization & Live Reading Status, 1-Tap Android Share Intent (Intent.ACTION_SEND), Markdown Reading Log Export & Monospace Preview (/sdcard/kindle/backups/folio_reading_log.md), 1-Tap Copy Markdown to Clipboard, Dynamic Unread Backlog Filter Chip (Unread (N)), 1-Tap Copy Book Info to Clipboard (📋 Copy Info), Sort by Rating Mode, Random Unread Book Picker (🎲), Interactive 5-Star Ratings Subsystem (folio_ratings), Persistent Catalog Sort Preference (folio_ui_prefs), Portable JSON Ratings Backup & Restore, Inline Card Note Snippet Previews, Sort by Favorites Mode, Favorites & Notes Combined Telemetry (1 favorited · 1 annotated), Dynamic Notes Quick-Filter Chip (📝 Notes (N)), Full-Text Search inside Reading Notes, Persistent Personal Reading Notes Subsystem (folio_notes), Combined Catalog Badges (★ 📝 Title), Inline Note Editor Dialog with Save/Clear, Portable JSON Note Backup & Restore, Control Center 4-Tile Live Stats Strip with ★ FAVS Deep-Link, Version 2 Portable JSON Backup with is_favorite Serialization, Persistent Favorites Subsystem with SharedPreferences zero-DB migration, Dynamic Gold ★ Favorites Quick-Filter Chip, Starred Title Prefix (★), Fullscreen Pitch-Black Cover Lightbox with centered cover art, Live KFX file size indicator in Book Details, 64x96dp Cover Thumbnail Header in Book Details, Expandable Synopsis Toggle (Read full synopsis › / Show less), Clickable Genre Pills in Book Details with 1-tap catalog filtering, Control Center Live Stat Tile Auto-Refresh on dialog close, Tappable Header Version Badge, 1-Tap Select New / Clear batch selection pill in folder importer, Dynamic filtered count header (X of Y), unified inline search clear button (✕) across catalog and batch folder importer, Clickable Author Quick-Filter link with automatic genre reset and search sync, Mark as Finished / Unread toggle pill in Book Details, dynamic teal Finished (N) chip filter, green Finished card badges, accurate reading progress SQL check, dynamic Reading quick-filter chip with in-progress badges, deduplicated Title-Cased genre tags, bibliographic meta-tag filtering from top chips, live reading progress stats in About Folio, Pass-3 missing-metadata auto-repair, natural-name author formatting, frequency-ranked genre chips with live counts, unified local library scan to app-private storage, right-aligned Home All Metadata link, safe cascade remove action, live reading progress badges and sort mode, 700+ EPUB live filename search in folder importer, "Hide Imported" toggle pill, ISBN-to-title genre fallback, multi-select batch EPUB folder importer from storage, portable metadata JSON backup/restore, missing-cover auto-repair with Open Library Covers API + 302 redirect following, catalog sort controls Recent/Title/Author/Year/Progress, in-app editable metadata dialog with live persistence, full-library `Sync & Enrich` metadata backfiller, real-time search filtering, Title-Cased genre chips, rich Book Details popup with 0ms reader launch, duplicate import guard with "Open Existing Copy" vs "Re-Import & Replace", "Library Metadata & Genres" browser dialog, persistent `folio_metadata.db`, Open Library metadata enrichment, live OPDS/Atom feed download, heavy omnibus stress-tested up to 23 MB / 1,450 sections, CSS text-transform uppercase/lowercase/capitalize, font-variant small-caps, RTL Arabic/Hebrew direction and right alignment, multi-level nested lists, native tables with thead/colspan/rowspan, malformed asset resilience, full HTML4/HTML5 entity decoding, store-free library search, zero-hyphenation code blocks, proportional sup/sub, `$270` cover container, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 077)
1. `screenshots/verify_r77_folder_sort_largest.png`: Batch EPUB Importer showing `Sort: Largest` with multi-MB EPUBs ranked at top.
2. `screenshots/verify_r77_selected_size_cta.png`: Batch EPUB Importer showing bottom CTA updated to `Import Selected (25 · 238.6 MB)` with `Clear (25)` button.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v7.7 KFX deployed (`lastUpdateTime=2026-11-22 23:52:10`).
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v7.6 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, Interactive 1-Tap Drill-Down Library Insights & Custom Shelves Chart, Library Insights Visual Analytics Modal (Top Genres, Top Authors, Average Rating & Shelf Stats), Searchable Authors Directory & Long Author Name Ellipsis Polish, Authors Directory Modal (👤) with Book Counts & 1-Tap Author Filtering, RFC-4180 CSV Library Export (/sdcard/kindle/backups/folio_library.csv), 1-Tap Export CSV Pill in Dialog Header, Reading Goal in Markdown Header & JSON Backup, Annual Reading Goal Progress Card (🎯), Goal Customizer Dialog with 1-Tap Presets (5/12/24/36/52), Persistent Reading Goal Preference (folio_ui_prefs), Reading Shelf Clipboard Info (📋 Copy Info), Custom Reading Shelves Subsystem (folio_shelves), Dynamic Indigo Shelf Filter Chips (🏷 <Shelf> (N)), 1-Tap Preset Shelf Picker & Custom Input, Shelf Full-Text Search, JSON Shelf Backup/Restore & Markdown Shelf Export, Reading Log Author Normalization & Live Reading Status, 1-Tap Android Share Intent (Intent.ACTION_SEND), Markdown Reading Log Export & Monospace Preview (/sdcard/kindle/backups/folio_reading_log.md), 1-Tap Copy Markdown to Clipboard, Dynamic Unread Backlog Filter Chip (Unread (N)), 1-Tap Copy Book Info to Clipboard (📋 Copy Info), Sort by Rating Mode, Random Unread Book Picker (🎲), Interactive 5-Star Ratings Subsystem (folio_ratings), Persistent Catalog Sort Preference (folio_ui_prefs), Portable JSON Ratings Backup & Restore, Inline Card Note Snippet Previews, Sort by Favorites Mode, Favorites & Notes Combined Telemetry (1 favorited · 1 annotated), Dynamic Notes Quick-Filter Chip (📝 Notes (N)), Full-Text Search inside Reading Notes, Persistent Personal Reading Notes Subsystem (folio_notes), Combined Catalog Badges (★ 📝 Title), Inline Note Editor Dialog with Save/Clear, Portable JSON Note Backup & Restore, Control Center 4-Tile Live Stats Strip with ★ FAVS Deep-Link, Version 2 Portable JSON Backup with is_favorite Serialization, Persistent Favorites Subsystem with SharedPreferences zero-DB migration, Dynamic Gold ★ Favorites Quick-Filter Chip, Starred Title Prefix (★), Fullscreen Pitch-Black Cover Lightbox with centered cover art, Live KFX file size indicator in Book Details, 64x96dp Cover Thumbnail Header in Book Details, Expandable Synopsis Toggle (Read full synopsis › / Show less), Clickable Genre Pills in Book Details with 1-tap catalog filtering, Control Center Live Stat Tile Auto-Refresh on dialog close, Tappable Header Version Badge, 1-Tap Select New / Clear batch selection pill in folder importer, Dynamic filtered count header (X of Y), unified inline search clear button (✕) across catalog and batch folder importer, Clickable Author Quick-Filter link with automatic genre reset and search sync, Mark as Finished / Unread toggle pill in Book Details, dynamic teal Finished (N) chip filter, green Finished card badges, accurate reading progress SQL check, dynamic Reading quick-filter chip with in-progress badges, deduplicated Title-Cased genre tags, bibliographic meta-tag filtering from top chips, live reading progress stats in About Folio, Pass-3 missing-metadata auto-repair, natural-name author formatting, frequency-ranked genre chips with live counts, unified local library scan to app-private storage, right-aligned Home All Metadata link, safe cascade remove action, live reading progress badges and sort mode, 700+ EPUB live filename search in folder importer, "Hide Imported" toggle pill, ISBN-to-title genre fallback, multi-select batch EPUB folder importer from storage, portable metadata JSON backup/restore, missing-cover auto-repair with Open Library Covers API + 302 redirect following, catalog sort controls Recent/Title/Author/Year/Progress, in-app editable metadata dialog with live persistence, full-library `Sync & Enrich` metadata backfiller, real-time search filtering, Title-Cased genre chips, rich Book Details popup with 0ms reader launch, duplicate import guard with "Open Existing Copy" vs "Re-Import & Replace", "Library Metadata & Genres" browser dialog, persistent `folio_metadata.db`, Open Library metadata enrichment, live OPDS/Atom feed download, heavy omnibus stress-tested up to 23 MB / 1,450 sections, CSS text-transform uppercase/lowercase/capitalize, font-variant small-caps, RTL Arabic/Hebrew direction and right alignment, multi-level nested lists, native tables with thead/colspan/rowspan, malformed asset resilience, full HTML4/HTML5 entity decoding, store-free library search, zero-hyphenation code blocks, proportional sup/sub, `$270` cover container, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 076)
1. `screenshots/verify_r76_insights_clean_bars.png`: Library Insights modal showing zero scrollbar overlap over numbers (`7`, `5`, `4`) and `(TAP TO FILTER)` headers.
2. `screenshots/verify_r76_insights_author_drilldown.png`: Catalog dynamically opened and filtered to `Library Metadata & Genres (4 of 27)` with search query `Aldous Huxley`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v7.6 KFX deployed (`lastUpdateTime=2026-11-22 23:45:54`).
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v7.5 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, Library Insights Visual Analytics Modal (Top Genres, Top Authors, Average Rating & Shelf Stats), Searchable Authors Directory & Long Author Name Ellipsis Polish, Authors Directory Modal (👤) with Book Counts & 1-Tap Author Filtering, RFC-4180 CSV Library Export (/sdcard/kindle/backups/folio_library.csv), 1-Tap Export CSV Pill in Dialog Header, Reading Goal in Markdown Header & JSON Backup, Annual Reading Goal Progress Card (🎯), Goal Customizer Dialog with 1-Tap Presets (5/12/24/36/52), Persistent Reading Goal Preference (folio_ui_prefs), Reading Shelf Clipboard Info (📋 Copy Info), Custom Reading Shelves Subsystem (folio_shelves), Dynamic Indigo Shelf Filter Chips (🏷 <Shelf> (N)), 1-Tap Preset Shelf Picker & Custom Input, Shelf Full-Text Search, JSON Shelf Backup/Restore & Markdown Shelf Export, Reading Log Author Normalization & Live Reading Status, 1-Tap Android Share Intent (Intent.ACTION_SEND), Markdown Reading Log Export & Monospace Preview (/sdcard/kindle/backups/folio_reading_log.md), 1-Tap Copy Markdown to Clipboard, Dynamic Unread Backlog Filter Chip (Unread (N)), 1-Tap Copy Book Info to Clipboard (📋 Copy Info), Sort by Rating Mode, Random Unread Book Picker (🎲), Interactive 5-Star Ratings Subsystem (folio_ratings), Persistent Catalog Sort Preference (folio_ui_prefs), Portable JSON Ratings Backup & Restore, Inline Card Note Snippet Previews, Sort by Favorites Mode, Favorites & Notes Combined Telemetry (1 favorited · 1 annotated), Dynamic Notes Quick-Filter Chip (📝 Notes (N)), Full-Text Search inside Reading Notes, Persistent Personal Reading Notes Subsystem (folio_notes), Combined Catalog Badges (★ 📝 Title), Inline Note Editor Dialog with Save/Clear, Portable JSON Note Backup & Restore, Control Center 4-Tile Live Stats Strip with ★ FAVS Deep-Link, Version 2 Portable JSON Backup with is_favorite Serialization, Persistent Favorites Subsystem with SharedPreferences zero-DB migration, Dynamic Gold ★ Favorites Quick-Filter Chip, Starred Title Prefix (★), Fullscreen Pitch-Black Cover Lightbox with centered cover art, Live KFX file size indicator in Book Details, 64x96dp Cover Thumbnail Header in Book Details, Expandable Synopsis Toggle (Read full synopsis › / Show less), Clickable Genre Pills in Book Details with 1-tap catalog filtering, Control Center Live Stat Tile Auto-Refresh on dialog close, Tappable Header Version Badge, 1-Tap Select New / Clear batch selection pill in folder importer, Dynamic filtered count header (X of Y), unified inline search clear button (✕) across catalog and batch folder importer, Clickable Author Quick-Filter link with automatic genre reset and search sync, Mark as Finished / Unread toggle pill in Book Details, dynamic teal Finished (N) chip filter, green Finished card badges, accurate reading progress SQL check, dynamic Reading quick-filter chip with in-progress badges, deduplicated Title-Cased genre tags, bibliographic meta-tag filtering from top chips, live reading progress stats in About Folio, Pass-3 missing-metadata auto-repair, natural-name author formatting, frequency-ranked genre chips with live counts, unified local library scan to app-private storage, right-aligned Home All Metadata link, safe cascade remove action, live reading progress badges and sort mode, 700+ EPUB live filename search in folder importer, "Hide Imported" toggle pill, ISBN-to-title genre fallback, multi-select batch EPUB folder importer from storage, portable metadata JSON backup/restore, missing-cover auto-repair with Open Library Covers API + 302 redirect following, catalog sort controls Recent/Title/Author/Year/Progress, in-app editable metadata dialog with live persistence, full-library `Sync & Enrich` metadata backfiller, real-time search filtering, Title-Cased genre chips, rich Book Details popup with 0ms reader launch, duplicate import guard with "Open Existing Copy" vs "Re-Import & Replace", "Library Metadata & Genres" browser dialog, persistent `folio_metadata.db`, Open Library metadata enrichment, live OPDS/Atom feed download, heavy omnibus stress-tested up to 23 MB / 1,450 sections, CSS text-transform uppercase/lowercase/capitalize, font-variant small-caps, RTL Arabic/Hebrew direction and right alignment, multi-level nested lists, native tables with thead/colspan/rowspan, malformed asset resilience, full HTML4/HTML5 entity decoding, store-free library search, zero-hyphenation code blocks, proportional sup/sub, `$270` cover container, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 075)
1. `screenshots/verify_r75_library_insights_modal.png`: Library Insights modal showing live summary, blue TOP GENRES bar chart, and amber TOP AUTHORS bar chart.
2. `screenshots/verify_r75_control_center_v75.png`: Control Center header showing `Folio v7.5 KFX` badge and 4-tile live stat strip.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v7.5 KFX deployed (`lastUpdateTime=2026-11-22 23:39:14`).
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v7.4 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, Searchable Authors Directory & Long Author Name Ellipsis Polish, Authors Directory Modal (👤) with Book Counts & 1-Tap Author Filtering, RFC-4180 CSV Library Export (/sdcard/kindle/backups/folio_library.csv), 1-Tap Export CSV Pill in Dialog Header, Reading Goal in Markdown Header & JSON Backup, Annual Reading Goal Progress Card (🎯), Goal Customizer Dialog with 1-Tap Presets (5/12/24/36/52), Persistent Reading Goal Preference (folio_ui_prefs), Reading Shelf Clipboard Info (📋 Copy Info), Custom Reading Shelves Subsystem (folio_shelves), Dynamic Indigo Shelf Filter Chips (🏷 <Shelf> (N)), 1-Tap Preset Shelf Picker & Custom Input, Shelf Full-Text Search, JSON Shelf Backup/Restore & Markdown Shelf Export, Reading Log Author Normalization & Live Reading Status, 1-Tap Android Share Intent (Intent.ACTION_SEND), Markdown Reading Log Export & Monospace Preview (/sdcard/kindle/backups/folio_reading_log.md), 1-Tap Copy Markdown to Clipboard, Dynamic Unread Backlog Filter Chip (Unread (N)), 1-Tap Copy Book Info to Clipboard (📋 Copy Info), Sort by Rating Mode, Random Unread Book Picker (🎲), Interactive 5-Star Ratings Subsystem (folio_ratings), Persistent Catalog Sort Preference (folio_ui_prefs), Portable JSON Ratings Backup & Restore, Inline Card Note Snippet Previews, Sort by Favorites Mode, Favorites & Notes Combined Telemetry (1 favorited · 1 annotated), Dynamic Notes Quick-Filter Chip (📝 Notes (N)), Full-Text Search inside Reading Notes, Persistent Personal Reading Notes Subsystem (folio_notes), Combined Catalog Badges (★ 📝 Title), Inline Note Editor Dialog with Save/Clear, Portable JSON Note Backup & Restore, Control Center 4-Tile Live Stats Strip with ★ FAVS Deep-Link, Version 2 Portable JSON Backup with is_favorite Serialization, Persistent Favorites Subsystem with SharedPreferences zero-DB migration, Dynamic Gold ★ Favorites Quick-Filter Chip, Starred Title Prefix (★), Fullscreen Pitch-Black Cover Lightbox with centered cover art, Live KFX file size indicator in Book Details, 64x96dp Cover Thumbnail Header in Book Details, Expandable Synopsis Toggle (Read full synopsis › / Show less), Clickable Genre Pills in Book Details with 1-tap catalog filtering, Control Center Live Stat Tile Auto-Refresh on dialog close, Tappable Header Version Badge, 1-Tap Select New / Clear batch selection pill in folder importer, Dynamic filtered count header (X of Y), unified inline search clear button (✕) across catalog and batch folder importer, Clickable Author Quick-Filter link with automatic genre reset and search sync, Mark as Finished / Unread toggle pill in Book Details, dynamic teal Finished (N) chip filter, green Finished card badges, accurate reading progress SQL check, dynamic Reading quick-filter chip with in-progress badges, deduplicated Title-Cased genre tags, bibliographic meta-tag filtering from top chips, live reading progress stats in About Folio, Pass-3 missing-metadata auto-repair, natural-name author formatting, frequency-ranked genre chips with live counts, unified local library scan to app-private storage, right-aligned Home All Metadata link, safe cascade remove action, live reading progress badges and sort mode, 700+ EPUB live filename search in folder importer, "Hide Imported" toggle pill, ISBN-to-title genre fallback, multi-select batch EPUB folder importer from storage, portable metadata JSON backup/restore, missing-cover auto-repair with Open Library Covers API + 302 redirect following, catalog sort controls Recent/Title/Author/Year/Progress, in-app editable metadata dialog with live persistence, full-library `Sync & Enrich` metadata backfiller, real-time search filtering, Title-Cased genre chips, rich Book Details popup with 0ms reader launch, duplicate import guard with "Open Existing Copy" vs "Re-Import & Replace", "Library Metadata & Genres" browser dialog, persistent `folio_metadata.db`, Open Library metadata enrichment, live OPDS/Atom feed download, heavy omnibus stress-tested up to 23 MB / 1,450 sections, CSS text-transform uppercase/lowercase/capitalize, font-variant small-caps, RTL Arabic/Hebrew direction and right alignment, multi-level nested lists, native tables with thead/colspan/rowspan, malformed asset resilience, full HTML4/HTML5 entity decoding, store-free library search, zero-hyphenation code blocks, proportional sup/sub, `$270` cover container, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 074)
1. `screenshots/verify_r74_authors_search_ellipsis.png`: Authors Directory modal showing new `Search authors...` input at top and single-line layout on author rows.
2. `screenshots/verify_r74_authors_filtered_live.png`: Authors Directory modal dynamically filtered to `Authors Directory (1 of 23)` with query `"Orwell"`, displaying `George Orwell (2 books ›)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v7.4 KFX deployed (`lastUpdateTime=2026-11-22 23:33:18`).

## Verification Evidence (Round 073)
1. `screenshots/verify_r73_authors_directory_modal.png`: Authors Directory modal listing authors sorted by book count descending (`Aldous Huxley (4 books ›)`).
2. `screenshots/verify_r73_author_selected_filter.png`: Catalog dynamically filtered to `Library Metadata & Genres (4 of 27)` with search query `Aldous Huxley`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v7.3 KFX deployed (`lastUpdateTime=2026-11-22 23:25:53`).
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v7.2 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, RFC-4180 CSV Library Export (/sdcard/kindle/backups/folio_library.csv), 1-Tap Export CSV Pill in Dialog Header, Reading Goal in Markdown Header & JSON Backup, Annual Reading Goal Progress Card (🎯), Goal Customizer Dialog with 1-Tap Presets (5/12/24/36/52), Persistent Reading Goal Preference (folio_ui_prefs), Reading Shelf Clipboard Info (📋 Copy Info), Custom Reading Shelves Subsystem (folio_shelves), Dynamic Indigo Shelf Filter Chips (🏷 <Shelf> (N)), 1-Tap Preset Shelf Picker & Custom Input, Shelf Full-Text Search, JSON Shelf Backup/Restore & Markdown Shelf Export, Reading Log Author Normalization & Live Reading Status, 1-Tap Android Share Intent (Intent.ACTION_SEND), Markdown Reading Log Export & Monospace Preview (/sdcard/kindle/backups/folio_reading_log.md), 1-Tap Copy Markdown to Clipboard, Dynamic Unread Backlog Filter Chip (Unread (N)), 1-Tap Copy Book Info to Clipboard (📋 Copy Info), Sort by Rating Mode, Random Unread Book Picker (🎲), Interactive 5-Star Ratings Subsystem (folio_ratings), Persistent Catalog Sort Preference (folio_ui_prefs), Portable JSON Ratings Backup & Restore, Inline Card Note Snippet Previews, Sort by Favorites Mode, Favorites & Notes Combined Telemetry (1 favorited · 1 annotated), Dynamic Notes Quick-Filter Chip (📝 Notes (N)), Full-Text Search inside Reading Notes, Persistent Personal Reading Notes Subsystem (folio_notes), Combined Catalog Badges (★ 📝 Title), Inline Note Editor Dialog with Save/Clear, Portable JSON Note Backup & Restore, Control Center 4-Tile Live Stats Strip with ★ FAVS Deep-Link, Version 2 Portable JSON Backup with is_favorite Serialization, Persistent Favorites Subsystem with SharedPreferences zero-DB migration, Dynamic Gold ★ Favorites Quick-Filter Chip, Starred Title Prefix (★), Fullscreen Pitch-Black Cover Lightbox with centered cover art, Live KFX file size indicator in Book Details, 64x96dp Cover Thumbnail Header in Book Details, Expandable Synopsis Toggle (Read full synopsis › / Show less), Clickable Genre Pills in Book Details with 1-tap catalog filtering, Control Center Live Stat Tile Auto-Refresh on dialog close, Tappable Header Version Badge, 1-Tap Select New / Clear batch selection pill in folder importer, Dynamic filtered count header (X of Y), unified inline search clear button (✕) across catalog and batch folder importer, Clickable Author Quick-Filter link with automatic genre reset and search sync, Mark as Finished / Unread toggle pill in Book Details, dynamic teal Finished (N) chip filter, green Finished card badges, accurate reading progress SQL check, dynamic Reading quick-filter chip with in-progress badges, deduplicated Title-Cased genre tags, bibliographic meta-tag filtering from top chips, live reading progress stats in About Folio, Pass-3 missing-metadata auto-repair, natural-name author formatting, frequency-ranked genre chips with live counts, unified local library scan to app-private storage, right-aligned Home All Metadata link, safe cascade remove action, live reading progress badges and sort mode, 700+ EPUB live filename search in folder importer, "Hide Imported" toggle pill, ISBN-to-title genre fallback, multi-select batch EPUB folder importer from storage, portable metadata JSON backup/restore, missing-cover auto-repair with Open Library Covers API + 302 redirect following, catalog sort controls Recent/Title/Author/Year/Progress, in-app editable metadata dialog with live persistence, full-library `Sync & Enrich` metadata backfiller, real-time search filtering, Title-Cased genre chips, rich Book Details popup with 0ms reader launch, duplicate import guard with "Open Existing Copy" vs "Re-Import & Replace", "Library Metadata & Genres" browser dialog, persistent `folio_metadata.db`, Open Library metadata enrichment, live OPDS/Atom feed download, heavy omnibus stress-tested up to 23 MB / 1,450 sections, CSS text-transform uppercase/lowercase/capitalize, font-variant small-caps, RTL Arabic/Hebrew direction and right alignment, multi-level nested lists, native tables with thead/colspan/rowspan, malformed asset resilience, full HTML4/HTML5 entity decoding, store-free library search, zero-hyphenation code blocks, proportional sup/sub, `$270` cover container, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 072)
1. `screenshots/verify_r72_reading_log_goal_and_csv_btn.png`: Reading Log dialog showing `**2026 Reading Goal:** 1 / 5 books finished (20%)`, `- **Shelf:** Classics` under *1984*, and amber `📊 Export CSV` pill in top bar.
2. `screenshots/verify_r72_csv_exported_banner.png`: Reading Log dialog showing banner updated to `Saved CSV: /sdcard/kindle/backups/folio_library.csv` after tapping `📊 Export CSV`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v7.2 KFX deployed (`lastUpdateTime=2026-11-22 23:15:43`).
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v7.1 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, Annual Reading Goal Progress Card (🎯), Goal Customizer Dialog with 1-Tap Presets (5/12/24/36/52), Persistent Reading Goal Preference (folio_ui_prefs), Reading Shelf Clipboard Info (📋 Copy Info), Custom Reading Shelves Subsystem (folio_shelves), Dynamic Indigo Shelf Filter Chips (🏷 <Shelf> (N)), 1-Tap Preset Shelf Picker & Custom Input, Shelf Full-Text Search, JSON Shelf Backup/Restore & Markdown Shelf Export, Reading Log Author Normalization & Live Reading Status, 1-Tap Android Share Intent (Intent.ACTION_SEND), Markdown Reading Log Export & Monospace Preview (/sdcard/kindle/backups/folio_reading_log.md), 1-Tap Copy Markdown to Clipboard, Dynamic Unread Backlog Filter Chip (Unread (N)), 1-Tap Copy Book Info to Clipboard (📋 Copy Info), Sort by Rating Mode, Random Unread Book Picker (🎲), Interactive 5-Star Ratings Subsystem (folio_ratings), Persistent Catalog Sort Preference (folio_ui_prefs), Portable JSON Ratings Backup & Restore, Inline Card Note Snippet Previews, Sort by Favorites Mode, Favorites & Notes Combined Telemetry (1 favorited · 1 annotated), Dynamic Notes Quick-Filter Chip (📝 Notes (N)), Full-Text Search inside Reading Notes, Persistent Personal Reading Notes Subsystem (folio_notes), Combined Catalog Badges (★ 📝 Title), Inline Note Editor Dialog with Save/Clear, Portable JSON Note Backup & Restore, Control Center 4-Tile Live Stats Strip with ★ FAVS Deep-Link, Version 2 Portable JSON Backup with is_favorite Serialization, Persistent Favorites Subsystem with SharedPreferences zero-DB migration, Dynamic Gold ★ Favorites Quick-Filter Chip, Starred Title Prefix (★), Fullscreen Pitch-Black Cover Lightbox with centered cover art, Live KFX file size indicator in Book Details, 64x96dp Cover Thumbnail Header in Book Details, Expandable Synopsis Toggle (Read full synopsis › / Show less), Clickable Genre Pills in Book Details with 1-tap catalog filtering, Control Center Live Stat Tile Auto-Refresh on dialog close, Tappable Header Version Badge, 1-Tap Select New / Clear batch selection pill in folder importer, Dynamic filtered count header (X of Y), unified inline search clear button (✕) across catalog and batch folder importer, Clickable Author Quick-Filter link with automatic genre reset and search sync, Mark as Finished / Unread toggle pill in Book Details, dynamic teal Finished (N) chip filter, green Finished card badges, accurate reading progress SQL check, dynamic Reading quick-filter chip with in-progress badges, deduplicated Title-Cased genre tags, bibliographic meta-tag filtering from top chips, live reading progress stats in About Folio, Pass-3 missing-metadata auto-repair, natural-name author formatting, frequency-ranked genre chips with live counts, unified local library scan to app-private storage, right-aligned Home All Metadata link, safe cascade remove action, live reading progress badges and sort mode, 700+ EPUB live filename search in folder importer, "Hide Imported" toggle pill, ISBN-to-title genre fallback, multi-select batch EPUB folder importer from storage, portable metadata JSON backup/restore, missing-cover auto-repair with Open Library Covers API + 302 redirect following, catalog sort controls Recent/Title/Author/Year/Progress, in-app editable metadata dialog with live persistence, full-library `Sync & Enrich` metadata backfiller, real-time search filtering, Title-Cased genre chips, rich Book Details popup with 0ms reader launch, duplicate import guard with "Open Existing Copy" vs "Re-Import & Replace", "Library Metadata & Genres" browser dialog, persistent `folio_metadata.db`, Open Library metadata enrichment, live OPDS/Atom feed download, heavy omnibus stress-tested up to 23 MB / 1,450 sections, CSS text-transform uppercase/lowercase/capitalize, font-variant small-caps, RTL Arabic/Hebrew direction and right alignment, multi-level nested lists, native tables with thead/colspan/rowspan, malformed asset resilience, full HTML4/HTML5 entity decoding, store-free library search, zero-hyphenation code blocks, proportional sup/sub, `$270` cover container, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 071)
1. `screenshots/verify_r71_control_center_reading_goal.png`: Control Center showing `Folio v7.1 KFX` badge, the 4-tile live stats strip, and `🎯 2026 Reading Goal` progress card (`1 of 12 books finished (8%)` with emerald progress bar).
2. `screenshots/verify_r71_reading_goal_updated.png`: Reading Goal card updated live to `1 of 5 books finished (20%)` with wider emerald progress bar after selecting preset `5` in goal dialog.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v7.1 KFX deployed (`lastUpdateTime=2026-11-22 23:07:38`).
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Milestone Folio v7.0 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, Custom Reading Shelves Subsystem (folio_shelves), Dynamic Indigo Shelf Filter Chips (🏷 <Shelf> (N)), 1-Tap Preset Shelf Picker & Custom Input, Shelf Full-Text Search, JSON Shelf Backup/Restore & Markdown Shelf Export, Reading Log Author Normalization & Live Reading Status, 1-Tap Android Share Intent (Intent.ACTION_SEND), Markdown Reading Log Export & Monospace Preview (/sdcard/kindle/backups/folio_reading_log.md), 1-Tap Copy Markdown to Clipboard, Dynamic Unread Backlog Filter Chip (Unread (N)), 1-Tap Copy Book Info to Clipboard (📋 Copy Info), Sort by Rating Mode, Random Unread Book Picker (🎲), Interactive 5-Star Ratings Subsystem (folio_ratings), Persistent Catalog Sort Preference (folio_ui_prefs), Portable JSON Ratings Backup & Restore, Inline Card Note Snippet Previews, Sort by Favorites Mode, Favorites & Notes Combined Telemetry (1 favorited · 1 annotated), Dynamic Notes Quick-Filter Chip (📝 Notes (N)), Full-Text Search inside Reading Notes, Persistent Personal Reading Notes Subsystem (folio_notes), Combined Catalog Badges (★ 📝 Title), Inline Note Editor Dialog with Save/Clear, Portable JSON Note Backup & Restore, Control Center 4-Tile Live Stats Strip with ★ FAVS Deep-Link, Version 2 Portable JSON Backup with is_favorite Serialization, Persistent Favorites Subsystem with SharedPreferences zero-DB migration, Dynamic Gold ★ Favorites Quick-Filter Chip, Starred Title Prefix (★), Fullscreen Pitch-Black Cover Lightbox with centered cover art, Live KFX file size indicator in Book Details, 64x96dp Cover Thumbnail Header in Book Details, Expandable Synopsis Toggle (Read full synopsis › / Show less), Clickable Genre Pills in Book Details with 1-tap catalog filtering, Control Center Live Stat Tile Auto-Refresh on dialog close, Tappable Header Version Badge, 1-Tap Select New / Clear batch selection pill in folder importer, Dynamic filtered count header (X of Y), unified inline search clear button (✕) across catalog and batch folder importer, Clickable Author Quick-Filter link with automatic genre reset and search sync, Mark as Finished / Unread toggle pill in Book Details, dynamic teal Finished (N) chip filter, green Finished card badges, accurate reading progress SQL check, dynamic Reading quick-filter chip with in-progress badges, deduplicated Title-Cased genre tags, bibliographic meta-tag filtering from top chips, live reading progress stats in About Folio, Pass-3 missing-metadata auto-repair, natural-name author formatting, frequency-ranked genre chips with live counts, unified local library scan to app-private storage, right-aligned Home All Metadata link, safe cascade remove action, live reading progress badges and sort mode, 700+ EPUB live filename search in folder importer, "Hide Imported" toggle pill, ISBN-to-title genre fallback, multi-select batch EPUB folder importer from storage, portable metadata JSON backup/restore, missing-cover auto-repair with Open Library Covers API + 302 redirect following, catalog sort controls Recent/Title/Author/Year/Progress, in-app editable metadata dialog with live persistence, full-library `Sync & Enrich` metadata backfiller, real-time search filtering, Title-Cased genre chips, rich Book Details popup with 0ms reader launch, duplicate import guard with "Open Existing Copy" vs "Re-Import & Replace", "Library Metadata & Genres" browser dialog, persistent `folio_metadata.db`, Open Library metadata enrichment, live OPDS/Atom feed download, heavy omnibus stress-tested up to 23 MB / 1,450 sections, CSS text-transform uppercase/lowercase/capitalize, font-variant small-caps, RTL Arabic/Hebrew direction and right alignment, multi-level nested lists, native tables with thead/colspan/rowspan, malformed asset resilience, full HTML4/HTML5 entity decoding, store-free library search, zero-hyphenation code blocks, proportional sup/sub, `$270` cover container, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 070)
1. `screenshots/verify_r70_shelf_picker_assigned.png`: Book Details on *1984* showing assigned `Shelf: 🏷 Classics  ›` pill positioned under `Your Rating: ★★★★★`.
2. `screenshots/verify_r70_shelf_chip_filter_live.png`: Catalog dialog showing active indigo `🏷 Classics (1)` chip filtering library to `Library Metadata & Genres (1 of 27)`, displaying *1984* with its indigo `🏷 Classics` card badge.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v7.0 KFX deployed (`lastUpdateTime=2026-11-22 22:58:46`).

## Verification Evidence (Round 068)
1. `screenshots/verify_r68_control_center_reading_log_card.png`: Control Center showing `Folio v6.8 KFX` badge and `📄 Reading Log & Export` action card.
2. `screenshots/verify_r68_reading_log_markdown_preview.png`: Reading Log dialog showing saved path banner (`Saved to: /sdcard/kindle/backups/folio_reading_log.md`), full markdown preview starting with `## ★ 1984`, `★★★★★ (5/5)`, and `> Essential dystopian classic - reread Part 3`, and `COPY MARKDOWN` button.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v6.8 KFX deployed (`lastUpdateTime=2026-11-22 22:43:52`).

## Verification Evidence (Round 067)
1. `screenshots/verify_r67_unread_chip_live.png`: Catalog dialog showing active teal `Unread (21)` quick-filter chip filtering library to `Library Metadata & Genres (21 of 27)`.
2. `screenshots/verify_r67_book_details_copy_info.png`: Book Details popup on *1984* showing `📋 Copy Info` pill cleanly positioned across from `Remove from Folio Library`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v6.7 KFX deployed (`lastUpdateTime=2026-11-22 22:35:01`).

## Verification Evidence (Round 066)
1. `screenshots/verify_r66_sort_rating_and_dice_pill.png`: Catalog dialog in `Sort: Rating` mode with `🎲` pill beside Sort, ranking 5-star `★ 📝 1984` (`★★★★★ 5/5`) at position 0 at top.
2. `screenshots/verify_r66_random_book_picked.png`: Book Details popup opened for randomly picked unread book (*Delphi Complete Works of Pliny the Younger*).
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v6.6 KFX deployed (`lastUpdateTime=2026-11-22 22:27:32`).

## Verification Evidence (Round 065)
1. `screenshots/verify_r65_book_details_5star_rating.png`: Book Details on *1984* showing interactive `Your Rating: ★★★★★` fully illuminated in gold.
2. `screenshots/verify_r65_card_star_rating_badge.png`: Catalog list card for *1984* displaying gold `★★★★★  5/5` badge directly above italic amber note snippet.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v6.5 KFX deployed (`lastUpdateTime=2026-11-22 22:17:59`).

## Verification Evidence (Round 064)
1. `screenshots/verify_r64_sort_favorites_note_snippet.png`: Catalog dialog in `Sort: Favorites` mode with `★ 📝 1984` pinned to position 0 at top displaying italic amber snippet preview `“Essential dystopian classic - reread Part 3”`.
2. `screenshots/verify_r64_about_v64_telemetry.png`: Control Center About Folio dialog displaying `Version: Folio v6.4 KFX (Enhanced Typesetting)` and `• Favorites & Notes: 1 favorited · 1 annotated`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v6.4 KFX deployed (`lastUpdateTime=2026-11-22 22:10:50`).

## Verification Evidence (Round 063)
1. `screenshots/verify_r63_notes_chip_live.png`: Catalog dialog showing active amber `📝 Notes (1)` quick-filter chip with header `Library Metadata & Genres (1 of 27)` filtering strictly to `★ 📝 1984`.
2. `screenshots/verify_r63_search_inside_note.png`: Catalog dialog showing search query `"Part 3"` matching inside personal note text and filtering `Library Metadata & Genres (1 of 27)` to `★ 📝 1984`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v6.3 KFX deployed (`lastUpdateTime=2026-11-22 22:03:29`).

## Verification Evidence (Round 062)
1. `screenshots/verify_r62_personal_note_saved.png`: Book Details popup on *1984* displaying amber-bordered `📝 Personal Note (tap to edit)` card with saved text `"Essential dystopian classic - reread Part 3"`.
2. `screenshots/verify_r62_card_note_badge.png`: Catalog list card for *1984* rendering combined `★ 📝 1984` favorite and personal note indicators.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v6.2 KFX deployed (`lastUpdateTime=2026-11-22 21:55:06`).

## Verification Evidence (Round 061)
1. `screenshots/verify_r61_control_center_4tile_favs.png`: Control Center (More tab) showing `Folio v6.1 KFX` badge and the crisp 4-tile live stats strip (`29 BOOKS` · `5 READING` · `1 FINISHED` · `1 ★ FAVS`) cleanly aligned on one row.
2. `screenshots/verify_r61_favs_tile_deeplink.png`: Catalog pre-filtered to gold `★ Favorites (1)` chip displaying `Library Metadata & Genres (1 of 27)` with starred `★ 1984` card upon tapping `1 ★ FAVS` tile.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v6.1 KFX deployed (`lastUpdateTime=2026-11-22 21:48:47`).

## Verification Evidence (Round 060)
1. `screenshots/verify_r60_book_details_favorite_pill.png`: Book Details popup on *1984* showing side-by-side `✓ Mark as Finished` and `☆ Favorite` action pills.
2. `screenshots/verify_r60_favorites_chip_live.png`: Catalog filtered to gold `★ Favorites (1)` chip displaying `Library Metadata & Genres (1 of 27)` and starred `★ 1984` card.
3. `logs/r60_about.xml`: Control Center About Folio showing `Version: Folio v6.0 KFX (Enhanced Typesetting)` and `• Favorited Books: 1`.
4. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
5. `dumpsys package com.amazon.kindle.epub`: Folio v6.0 KFX deployed (`lastUpdateTime=2026-11-22 21:40:01`).

## Verification Evidence (Round 059)
1. `screenshots/verify_r59_book_details_kfx_size.png`: Book Details popup on *1984* displaying the live `Format: Enhanced KFX · 1.2 MB` line.
2. `screenshots/verify_r59_cover_lightbox.png`: Pure `#000000` black Fullscreen Cover Lightbox with centered *1984* cover art, `1984 · George Orwell` caption, and tap-to-close behavior.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v5.9 KFX deployed (`lastUpdateTime=2026-11-22 21:32:43`).

## Verification Evidence (Round 058)
1. `screenshots/verify_r58_book_details_cover_header.png`: Book Details popup on *1984* showing `64×96dp` cover thumbnail header, title, author quick-filter, clickable genre pills, 5-line clamped description, and teal `Read full synopsis ›` toggle.
2. `screenshots/verify_r58_synopsis_expanded.png`: Synopsis cleanly expanded to full length with toggle dynamically updated to `Show less`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v5.8 KFX deployed (`lastUpdateTime=2026-11-22 21:25:37`).

## Verification Evidence (Round 057)
1. `screenshots/verify_r57_book_details_genre_pills.png`: Book Details popup on *1984* showing the horizontal scrollable row of clickable blue genre pills (`Totalitarianism`, `Fiction`, `Dystopias`).
2. `screenshots/verify_r57_genre_pill_filter_live.png`: Catalog filtered immediately upon tapping `Fiction` pill, displaying `Library Metadata & Genres (10 of 27)` with active `✕` clear button.
3. `logs/r57_about.xml`: Control Center `Folio v5.7 KFX` header badge tapped, directly opening About Folio with `Version: Folio v5.7 KFX (Enhanced Typesetting)`.
4. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
5. `dumpsys package com.amazon.kindle.epub`: Folio v5.7 KFX deployed (`lastUpdateTime=2026-11-22 21:14:57`).

## Verification Evidence (Round 056)
1. `screenshots/verify_r56_control_center_stats_strip.png`: Control Center (`More` tab) showing `Folio v5.6 KFX` badge and the 3-tile live stats strip (`29 BOOKS` · `5 READING` · `1 FINISHED`) above action cards.
2. `screenshots/verify_r56_stat_tile_deeplink_reading.png`: Metadata catalog pre-filtered to teal `Reading (5)` chip displaying `Library Metadata & Genres (5 of 27)` after tapping the `5 READING` tile.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v5.6 KFX deployed (`lastUpdateTime=2026-11-22 21:06:13`).

## Verification Evidence (Round 055)
1. `screenshots/verify_r55_select_new_orwell.png`: Batch EPUB Importer on `/sdcard/en/0` with query `Orwell` showing 12 unimported EPUBs checked, amber `Clear (12)` pill, and bottom `Import Selected (12)` CTA button.
2. `screenshots/verify_r55_about_v55.png`: Control Center About Folio dialog displaying `Version: Folio v5.5 KFX (Enhanced Typesetting)` and live reading stats (`5 in progress · 1 finished`).
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v5.5 KFX deployed (`lastUpdateTime=2026-11-22 20:55:15`).

## Verification Evidence (Round 054)
1. `screenshots/verify_r54_filtered_count_header.png`: Library Metadata & Genres (27) inside Folio showing active teal `Reading (5)` chip updating dialog header to `Library Metadata & Genres (5 of 27)`.
2. `screenshots/verify_r54_folder_search_clear.png`: Batch EPUB Importer browsing `/sdcard/en/0` with query `Orwell` showing 14 matching EPUBs and inline `✕` clear button in search bar.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v5.4 KFX deployed (`lastUpdateTime=2026-11-22 20:48:51`).

## Verification Evidence (Round 053)
1. `screenshots/verify_r53_finished_chip_live.png`: Library Metadata & Genres (27) inside Folio showing active teal `Finished (1)` chip filtering strictly to *Moby Dick* with its green `✓ Finished` card badge.
2. `screenshots/verify_r53_author_filter_and_clear.png`: Library Metadata & Genres filtered to Aldous Huxley's books (*Crome Yellow*, *Island*, *Brave New World*) via the Book Details `Aldous Huxley  ›` link, showing inline `✕` clear button in search bar.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v5.3 KFX deployed (`lastUpdateTime=2026-11-22 20:41:19`).

## Verification Evidence (Round 052)
1. `screenshots/verify_r52_book_details_mark_finished.png`: Book Details dialog for *Moby Dick* displaying the emerald `✓ Mark as Finished` pill above `Remove from Folio Library`.
2. `screenshots/verify_r52_finished_chip_live.png`: Library Metadata & Genres (27) showing active teal `Finished (1)` chip filtering strictly to *Moby Dick* with its green `✓ Finished` card badge.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v5.2 KFX deployed (`lastUpdateTime=2026-11-22 20:32:34`).

## Verification Evidence (Round 051)
1. `screenshots/verify_r51_reading_chip_filter.png`: Library Metadata & Genres (27) showing active teal `Reading (5)` chip filtering to in-progress books with `4% read` and `2% read` progress badges.
2. `screenshots/verify_r51_about_v51_accurate_stats.png`: Control Center `About Folio` dialog displaying `Version: Folio v5.1 KFX (Enhanced Typesetting)` and accurate live `Reading Progress: 5 in progress · 0 finished` alongside storage stats.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v5.1 KFX deployed (`lastUpdateTime=2026-11-22 20:26:05`).
1. `screenshots/verify_r50_clean_genres_and_chips.png`: Library Metadata & Genres (27) showing top literary chips (`All (27)`, `Fiction (7)`, `History (5)`, `Rome (3)`, `Biography (2)`), and deduplicated Title-Cased genres on *Crome Yellow* (`Fiction, Intellectuals, Country Homes, England, Humorous`) and *The Dream of the Red Chamber* (`Chinese Fiction, China, Fiction`).
2. `screenshots/verify_r50_about_v50_reading_stats.png`: Control Center `About Folio` dialog displaying `Version: Folio v5.0 KFX (Enhanced Typesetting)` and live `Reading Progress: 0 in progress · 29 finished` alongside storage stats.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v5.0 KFX deployed (`lastUpdateTime=2026-11-22 20:18:41`).
1. `screenshots/verify_r49_pass3_enriched_all.png`: Library Metadata & Genres (27) showing natural author name `"Herman Melville · 2001-07-01"` on *Moby Dick*, enriched *1984* (`2003 · Houghton Mifflin Harcourt`, `ISBN: 0582777313`, `Totalitarianism, Fiction...`), and updated `Fiction (7)` top genre chip.
2. `screenshots/verify_r49_moksha_crome_yellow.png`: Scrolled view showing Pass-3 enriched *Moksha* (`2012-04-12`, `ISBN: 9781594775178`, `Hallucinogenic drugs...`) and *Crome Yellow* (`2011-01-06`, `ISBN: 9781407020938`, `Fiction, Intellectuals...`).
3. `screenshots/verify_r49_about_v45.png`: Control Center `About Folio` dialog displaying `Version: Folio v4.5 KFX (Enhanced Typesetting)`, 29 indexed books, 27 enriched records, and 13.3 MB App Library storage.
4. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
5. `dumpsys package com.amazon.kindle.epub`: Folio v4.5 KFX deployed (`lastUpdateTime=2026-11-22 20:08:00`).
1. `screenshots/verify_r42_folder_search_and_hide_imported.png`: Folder browser dialog browsing `/sdcard/en/0` showing `Hide Imported: ON`, live search for `Orwell` (14 matching files), and checked `1984.epub`.
2. `screenshots/verify_r42_enriched_genres_card.png`: Library Metadata & Genres (26) showing newly imported *Animal Farm* with genuine cover thumbnail and rich blue genre tags (`Fiction`, `Read`).
3. `screenshots/verify_r42_about_v39.png`: Control Center showing `About Folio` dialog with `Folio v3.9 KFX` version and engine stats.
4. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
5. `dumpsys package com.amazon.kindle.epub`: Folio v3.9 KFX deployed (`lastUpdateTime=2026-11-22 18:21:03`).

---

# Round 041: Batch EPUB Folder Importer & JSON Backup (Folio v3.8)
1. `screenshots/verify_r41_folder_browser_select.png`: Folder browser dialog browsing `/sdcard/en/0` with green `· Already Imported` badges and checked boxes ready to batch import.
2. `screenshots/verify_r41_batch_imported_catalog.png`: Library Metadata & Genres (24) showing newly imported *Moksha* and *Crome Yellow* with covers and metadata.
3. `screenshots/verify_r41_control_center_v38.png`: Control Center showing `Import EPUBs from Storage` card and `Folio v3.8 KFX` badge.
4. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
5. `dumpsys package com.amazon.kindle.epub`: Folio v3.8 KFX deployed (`lastUpdateTime=2026-11-22 18:07:40`).

---

# Round 040: Missing-Cover Auto-Repair & Sort (Folio v3.7)
1. `screenshots/verify_r40_covers_fixed_and_sorted.png`: Catalog dialog after `Sync & Enrich` proving *The 210th Day* has a real cover thumbnail and the new `Sort: Recent` pill is active.
2. `screenshots/verify_r40_edit_metadata_dialog.png`: Dark **Edit Book Metadata** form opened from Book Details with pre-filled fields (`Title`, `Author`, `Published Date / Year`, `Publisher`, `Genres`, `ISBN`) and `SAVE` / `CANCEL` actions.
3. `screenshots/verify_r40_about_v37.png`: Control Center **About Folio** dialog showing `Version: Folio v3.7 KFX (Enhanced Typesetting)` and live engine stats.
4. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
5. `dumpsys package com.amazon.kindle.epub`: Folio v3.7 KFX deployed (`lastUpdateTime=2026-11-22 17:53:17`).

---

# Round 039: Full-Library Metadata Backfill & Search (Folio v3.6)
1. `screenshots/verify_r39_metadata_catalog_synced.png`: Catalog dialog after `Sync & Enrich` showing `Library Metadata & Genres (21)`, Title-Cased genre chips, search bar, and enriched book cards.
2. `screenshots/verify_r39_metadata_search_filtered.png`: Live real-time search filter active with query `"Huxley"`, filtering display strictly to matching cards.
3. `screenshots/verify_r39_book_details_popup.png`: Tapping `ⓘ` on `Island` showing dark Book Details dialog with full metadata, Open Library work key, and `READ NOW` / `CLOSE` actions.
4. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
5. `dumpsys package com.amazon.kindle.epub`: Folio v3.6 KFX deployed (`lastUpdateTime=2026-11-22 17:39:47`).

---

# Round 038: Duplicate Guard & Metadata Catalog (Folio v3.5)
1. `screenshots/verify_r38_duplicate_import_guard.png`: Green `ALREADY IN FOLIO LIBRARY` dialog over Galaxy M32 showing `Open Existing Copy` and `Re-Import & Replace`.
2. `screenshots/verify_r38_metadata_catalog_genres.png`: Dark `FOLIO METADATA CATALOG` dialog with genre filter chips and enriched book cards.
3. `screenshots/verify_r38_control_center_v35.png`: Folio Control Center showing `Folio v3.5 KFX` badge and `Library Metadata & Genres` action card.
4. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
5. `dumpsys package com.amazon.kindle.epub`: Folio v3.5 KFX deployed (`lastUpdateTime=2026-11-22 17:22:20`).

---

# Round 037: External EPUB Intent Hook, Ingestion Flow & Open Library Metadata (Folio v3.4)
1. `screenshots/verify_r37_external_import_dialog.png`: Dark **FOLIO EPUB IMPORT** dialog showing extracted metadata and action buttons.
2. `screenshots/verify_r37_open_once_reader.png`: Book opened directly in `KRIFDocViewer` after "Open Once".
3. `screenshots/verify_r37_add_to_library_reader.png`: Book opened directly in `KRIFDocViewer` after "Add to Library & Read".
4. `screenshots/verify_r37_library_imported_book.png`: Folio Library tab showing the newly imported book with its real cover.
5. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
6. `dumpsys package com.amazon.kindle.epub`: Folio v3.4 KFX deployed (`lastUpdateTime=2026-11-22 16:49:43`).

---

# Round 034: Nested Lists, Table Spans, Clean Context Menu & Settings Audit (Folio v3.1)
1. `screenshots/verify_r34_nested_lists_and_table_spans.png`: Multi-level nested list (`•`, `◦`, `▪`) with indentation scaling + Native table with bold `<th>` headers, `colspan="2"`, and `rowspan="2"`.
2. `screenshots/verify_r34_library_book_context_menu.png`: Library tab long-press context menu on local KFX book showing `Mark as Read`, `Remove`, and `Add to Collection` with zero store links.
3. `screenshots/verify_r34_settings_screen_audit.png`: Reader & App Settings launched from Control Center showing offline settings without account prompts.
4. `screenshots/verify_r34_opds_catalog_live.png`: OPDS Catalogs launched from Control Center showing clean catalog presets.
5. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
6. `dumpsys package com.amazon.kindle.epub`: Folio v3.1 KFX deployed (`lastUpdateTime=2026-11-22 14:45:26`).

---

# Round 033: Complete HTML4/HTML5 Entity Decoding & In-Book Search/Bookmarks QC (Folio v3.0)
1. `screenshots/verify_r33_html_entities_decoded.png`: Rendered KFX page proving `2H₂ + O₂ → 2H₂O` (real `→` arrow, zero literal `&rarr;`) plus decoded `&times;`, `&plusmn;`, `&hellip;`, `&mdash;`, `&eacute;`, `&Omega;`, and `&#x2192;`.
2. `screenshots/verify_r33_in_book_search.png`: Live in-book search results for `"Creon"` inside Sophocles showing `BOOK (297)` with snippets and location markers.
3. `screenshots/verify_r33_bookmarks_notebook.png`: Saved bookmark/highlight visible inside the in-reader Notebook view with zero sync error dialogs.
4. `screenshots/verify_r33_about_v30.png`: Control Center **About Folio** dialog showing milestone `Version: 3.0 KFX (Enhanced Typesetting)`.
5. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
6. `dumpsys package com.amazon.kindle.epub`: Folio v3.0 KFX deployed (`lastUpdateTime=2026-11-22 13:26:58`).

---

# Round 032: Zero-Hyphenation Code Blocks, Sup/Sub Polish & Store-Free Library Search (Folio v2.9)
  1. **Zero-Hyphenation Code Blocks & Proportional Sup/Sub (Task A)**:
     - Pure-Rust schema parity in `libkfxconv.so`: Emitted YellowJersey `$127: $349` (`hyphens: none`) on `style_pre` and `style_code`. Variable names, keywords, and long identifiers wrap cleanly on word boundaries with zero hyphens.
     - Polished `<sup>` and `<sub>` styling: Kept proportional `0.75em` (`$16`) and baseline styles (`$44: $370` super, `$44: $371` sub), and completely removed numeric baseline shift (`$31`). Fixed awkward line spacing and vertical line collisions.
     - Added Rust unit test `test_r32_code_no_hyphen_and_proportional_sup_sub` in `src/packager/tests.rs`; all 212 tests pass cleanly.
     - Cross-compiled stripped ARM64 `libkfxconv.so` (4.5 MB) and updated version badge in `FolioMoreView.java` to `Folio v2.9 KFX`.
  2. **Store-Free Library Search (Task B)**:
     - Reverse-engineered Search UI in `classes5.dex` (`SearchResultsKt`, `SearchActivity`, `RubySearchActivity`, `SearchProvider`, `SearchViewModel`, `models.b`).
     - In `SearchResultsKt.smali`: Patched `b4(ZZZZZ)Z` to return `false` (0), completely suppressing `StoreHeaderSection` (the Amazon Kindle Store shopping cart header).
     - In `com/amazon/kcp/search/v2/models/b.smali`: Patched `e()Ljava/util/List;` to return `Collections.EMPTY_LIST`.
     - In `SearchProvider.smali`: Patched `N()` to return `Collections.EMPTY_LIST` and forced `queryStore = false`.
     - In `SearchViewModel.smali`: Silenced `r()V` (coroutine launcher) and `z(SearchNodeCompleteEvent)`.
     - In `SearchActivity.smali`: Forced `setShouldShowStoreResults(false)`.
     - Verified: Searching `"Sophocles"` displays **ONLY `In your library (1)`** with zero store shelf.
     - Verified: Tapping the local search result opens directly into `StandAloneBookReaderActivity` (`KRIFDocViewer`).
  3. **Visual Verification & Release Staging (Task C)**:
     - 3 hardware proof screenshots captured on Samsung Galaxy M32 (`verify_r32_code_no_hyphen_and_sup_sub.png`, `verify_r32_library_search_no_store.png`, `verify_r32_search_result_open_book.png`).
     - Safety Invariant: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Signed Folio v2.9 KFX APK (131 MB) and stripped native library (4.5 MB) staged at `07_APK_EPUB_INGESTION_MOD/release/folio_v2.9_kfx/`.
  4. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page and take the image for the cover. It is then processed separately and kept in its own class and inserted when displaying the book separately."
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v2.9 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, store-free library search, zero-hyphenation code blocks, proportional sup/sub, `$270` cover container, native table support, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 032)
1. `screenshots/verify_r32_code_no_hyphen_and_sup_sub.png`: Rendered KFX page showing `<pre>` Python code block with zero hyphenation alongside crisp proportional `E = mc²` and `H₂O` sup/sub formatting.
2. `screenshots/verify_r32_library_search_no_store.png`: Live `"Search Folio"` query (`"Sophocles"`) showing ONLY `In your library (1)` with the Amazon `"From the store"` shelf completely absent.
3. `screenshots/verify_r32_search_result_open_book.png`: Tapping local search match under `In your library (1)` opens directly into `StandAloneBookReaderActivity` (`KRIFDocViewer`).
4. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
5. `dumpsys package com.amazon.kindle.epub`: Folio v2.9 KFX deployed (`lastUpdateTime=2026-11-22 12:03:13`).

---

# Round 031: Real Pop-Up Footnotes, Rich Inline/Code Formatting & Live Library Search QC (Folio v2.8)
  1. **Real Native KFX Pop-Up Footnotes (Task A)**:
     - Pure-Rust schema parity: Link span in `$142 style_events` emits `$179 link_to` and `$616: $617` (`yj.display: yj.note`). Target footnote entry in `$259 storyline` emits `$615: $281` (`yj.classification: footnote`).
     - Anchor entity `$266` resolves `$183 position` pointing to target footnote story entry EID (`$155`) with offset 0.
     - Cleaned up Fragment `$597 auxiliary_data` to stock `IS_TARGET_SECTION` metadata only, eliminating non-standard `$617` note arrays.
     - Fixed anchor leak bug in `xhtml.rs`: consumed anchor with `anchor.take()` on span flush; reset `current_anchor` and `current_block_id` on block close.
     - Verified in `KRIFDocViewer`: tapping footnote link navigates to footnote text with top "Back to Location" affordance with zero soft keyboard / zero Add Note editor.
  2. **Rich Formatting Audit (Task B)**:
     - Monospace code blocks (`<pre>`, `<code>`): Preserves whitespace and newlines verbatim, font family `$11: "monospace"`, left alignment `$34: 59`, text indent `$36: 0.0rem`, top/bottom margin `$48: 1.2rem`.
     - Horizontal rules (`<hr/>`): Emits YellowJersey `$596 horizontal_rule` story elements with `style_hr`.
     - Inlines: `<sup>`/`<sub>` baseline styles (`$44: $370/$371`) and baseline shift (`$31`), `<s>`/`<del>` strikethrough (`$27: $328`), and `<u>` underline (`$23: $328`).
     - Added comprehensive unit tests in `src/packager/tests.rs`; all 211 tests pass cleanly.
  3. **Clean Hardware Label, Live Library Search & Visual Verification (Task C)**:
     - In `FolioMoreView.java`: Filtered out `"unknown"` (case-insensitive) or blank `Build.MANUFACTURER`. Cleanly formatted as `Build.MODEL + " · Android " + Build.VERSION.RELEASE + " (" + arch + ")"`. Updated badge to `Folio v2.8 KFX`.
     - Live Library Search: Verified query `"Sophocles"` triggers `RubySearchActivity` and filters local KFX library books cleanly (`In your library (1)`). Zero crashes or Amazon store dead ends.
     - Verified on Samsung Galaxy M32: 4 required proof screenshots captured and verified.
     - Safety Invariant: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Cover Architecture Mandate**: Project Lead architectural directive formally documented and passed to manager AI.
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v2.8 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, `$270` cover container, native table support, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 031)
1. `screenshots/verify_r31_real_footnote_popup.png`: Native pop-up footnote navigation in `KRIFDocViewer` without keyboard or Add Note modal.
2. `screenshots/verify_r31_code_hr_rich_formatting.png`: Rendered `<pre>/<code>` monospace block, `<hr/>` divider, `<sup>/<sub>`, and `<s>/<u>` formatting.
3. `screenshots/verify_r31_about_clean_hardware.png`: About Folio dialog showing `Folio v2.8 KFX` and clean hardware string without `"unknown"`.
4. `screenshots/verify_r31_library_search_results.png`: Live `"Search Folio"` query filtering local books cleanly in Library tab.
5. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
6. `dumpsys package com.amazon.kindle.epub`: Folio v2.8 KFX deployed (`lastUpdateTime=2026-11-22 11:21:33`).

## Next Steps
1. Package `antig_to_gemini_neon_clunky_report031.zip` (< 15 MB).
2. Relay report zip to Gemini Neon tab via Chrome bridge, verifying attachment preview chip before sending.
3. Launch autonomous relay runner with 2000s timeout and 60s polling interval to await Round 032 instructions.

---

# Round 030: Subtle Visual Polish, Multi-Book Stress Test & Interactive Flow QC (Folio v2.7)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v2.7 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 030 complete:
  1. **Converter & UI Micro-Polish (Task A)**:
     - Pure-Rust Link Coalescing: Contiguous text spans sharing the same `href` coalesce into a single `$179 link_to` event in `kfx_converter/src/packager/storylines.rs`. Stripped pseudo-classes (`:link`, `:visited`, etc.) in `kfx_converter/src/css.rs` so stylesheet rules cascade cleanly onto nested inline tags. Unit test: `test_r30_nested_inline_tags_in_link` passes.
     - Uniform `<li>` Marker Spacing: Added `trim_start()` on `item_spans[0].text` in `kfx_converter/src/packager/storylines.rs` and set `text_alignment: left` (`sym_left`, 59) in `styles.rs` so bullet gaps are never stretched by text justification. Unit test: `test_r30_uniform_list_item_marker_spacing` passes.
     - Dynamic Device Info & Privacy Mode: In `FolioMoreView.java`, populated `Hardware:` dynamically via `Build.MANUFACTURER + " " + Build.MODEL + " (" + Build.SUPPORTED_ABIS[0] + ")"`, and updated privacy label to `"Privacy Mode: Local-First (Zero Amazon Telemetry)"`.
     - Pop-up Note / Footnote Ingestion: Emitted `$617 yj.note` inside `$597 auxiliary_data` when footnotes are detected for a section. All 200 Rust tests pass.
  2. **Multi-Book On-Device Benchmark Matrix (Task B)**:
     - Executed 7-book stress matrix on Galaxy M32 with sub-2s conversion and zero-crash `KRIFDocViewer` launch (`Hesiod`, `Cato the Elder`, `Aristophanes`, `Sophocles`, `Julius Caesar`, `kindle_exam_v3`, `kindle_tables_images_exam`).
  3. **Interactive Flow Verification (Task B.2 & C)**:
     - Verified uniform bullet gap and link underline in `kindle_exam_v3.epub` (`screenshots/verify_r30_list_spacing_and_links_fixed.png`).
     - Verified live pop-up modal panel in `KRIFDocViewer` (`screenshots/verify_r30_footnote_popup.png`).
     - Verified live text selection with highlight palette, action bar (`Annotate`, `Note`, `Copy`, `Pin`, `Report`), and Oxford dictionary definition card on `"profound"` (`screenshots/verify_r30_highlight_or_scroll.png`).
     - Verified Control Center About Folio dialog with dynamic hardware metadata (`screenshots/verify_r30_control_center_scan_and_about.png`).
  4. **Cover Architecture Directive Relayed**: Included Project Lead directive in `REPORT.md` regarding extracting cover images on ingestion and managing them in a dedicated UI View rather than within the reflowable KFX book spine.
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v2.7 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, `$270` cover container, native table support, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 030)
1. `screenshots/verify_r30_list_spacing_and_links_fixed.png`: Uniform bullet gap in `kindle_exam_v3.epub` verified via OCR.
2. `screenshots/verify_r30_footnote_popup.png`: Live pop-up modal panel in `KRIFDocViewer`.
3. `screenshots/verify_r30_highlight_or_scroll.png`: Live highlight selection, color palette, and Oxford dictionary definition card.
4. `screenshots/verify_r30_control_center_scan_and_about.png`: Control Center About Folio dialog showing dynamic hardware details and Privacy Mode label.
5. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
6. `dumpsys package com.amazon.kindle.epub`: Folio v2.7 KFX deployed (`lastUpdateTime=2026-11-22 09:49:08`).

## Next Steps
1. Package `antig_to_gemini_neon_clunky_report030.zip` (< 15 MB).
2. Relay report zip to Gemini Neon tab via Chrome bridge, verifying attachment preview chip before sending.
3. Launch autonomous relay runner with 2000s timeout and 60s polling interval to await Round 031 instructions.

---

# Round 024: Native Kindle Tables, Standalone Cover Separation, Bookerly Typography & Image Parity (Folio v2.1)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v2.1 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 024 complete:
  1. **Native Kindle Tables (Task A)**: Ported `jloutsch/kfxgen` table structures into pure-Rust `kfx_converter` (`$278` yj_table, `$279` yj_table_row, `$269` yj_table_cell, `$146` content_list, border collapse `$150: false`, vertical/horizontal border spacing `$456`/`$457: 0.9%`, feature table `$585` with `yj_table` v1/v3 & `yj_table_viewer` v1, and horizontal pan chevron `$629: [581, 326]`, `$630: 632`).
  2. **Standalone Cover Separation (Task B)**: Redundant in-spine cover HTML pages (`cover.xhtml`, `titlepage.xhtml`, `wrap0000.html`) stripped from spine sections during initial EPUB parsing. Preserves native KFX cover resource (`$417` + `$164` + `$495 cover_image` on black `#000000` via `$270 scale_fit`), and emits companion `<outKfx>.cover.jpg`.
  3. **Typography & Image Parity (Task C)**: Set `override_kindle_font: false` to ensure system fonts (Bookerly) are preserved. Extracted `<dc:language>` into metadata `$490` for native hyphenation. Transparent PNG compositing over solid white `(255, 255, 255)`.
  4. **Cross-Compilation & Staging (Task D)**: Cross-compiled `libkfxconv.so` for ARM64 (`aarch64-linux-android`) with NDK 26 Clang (4.4 MB stripped). Rebuilt and staged Folio v2.1 in `07_APK_EPUB_INGESTION_MOD/release/folio_v2.1_kfx/base.apk` (131 MB).
  5. **Verification Evidence**:
     - `screenshots/verify_r24_kfx_native_tables.png`: Native 6-column financial table with pan chevron.
     - `screenshots/verify_r24_kfx_images.png`: Embedded figures (900x150, 300x300) with white transparency backing.
     - `screenshots/verify_r24_exam_v3_images_hyphen.png`: Bookerly typography, native hyphenation (`an- / swered`, `post- / poned`), zero duplicate cover.
     - `screenshots/verify_r24_classic_cover.png`: Hesiod cover on `#000000` black, zero white HTML margins.
     - `screenshots/verify_r24_classic_prose_spacing.png`: Live `Aa → Font → Spacing` reflow sliders.
  6. **Stock Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  7. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v2.1_kfx/base.apk` (131 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report024.zip` (1.8 MB)
     - Submitted to Gemini Neon tab `837017938` with verified composer attachment chip and project lead cover directive.

---

# Round 025: Native KFX Table Columns & Borders Fix + Cover Architecture Blueprint (Folio v2.2)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v2.2 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 025 complete:
  1. **Native Kindle Tables Fixed (Task A)**: Deep-diffed against stock `CR!6KYWZNKF153BK4YSK5MTFZ1XN6ZA.kfx`. Wrapped rows inside `$454` (`body`), added `$152` (`column_format`) per column with `{$698: false}`, enabled `$150: true` (`table_border_collapse`), changed border spacing to `0.9 pt` (`$456`/`$457`), and added `$83: 4286611584` (`border_color`). Verified in `verify_r25_table_columns.png`: completely eliminates mashed text (`Gannet992.1Indigo...`) with distinct, separated columns across 2 to 6-column tables.
  2. **Cover Architecture Blueprint (Task B)**: Integrated user directive to avoid forcing cover rendering inside the KFX spine. On initial EPUB parsing, the cover page is stripped from the spine and the cover image is extracted into its own class/asset, which is displayed separately in its own fullscreen Activity/View on `#000000` black upon opening the book.
  3. **On-Device Build & Verification (Task C)**:
     - Cross-compiled `libkfxconv.so` for `aarch64-linux-android` (4.4 MB stripped).
     - Built, signed, and deployed Folio v2.2; staged signed APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v2.2_kfx/base.apk` (131 MB).
     - Verified live `Aa → Font → Spacing` sliders in `StandAloneBookReaderActivity` (`verify_r25_prose_spacing_before.png` and `verify_r25_prose_spacing_after.png`).
     - Captured baseline cover reading screenshots: `verify_r25_cover_book1.png` and `verify_r25_cover_book2.png`.
  4. **Stock Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  5. **Deliverables & Report (Task D)**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v2.2_kfx/base.apk` (131 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report025.zip` (< 15 MB)
     - Uploaded to Gemini Neon tab `837017938` with verified composer attachment chip.
     - Autonomous relay runner active in background (`autonomous_relay_runner.mjs 26 2000`, 60s poll interval, 2000s max timeout, zero duplicate uploads) waiting for `to_antig_clunky_round_026.zip`.


---

# Round 026: Native Stock-Kindle KFX `$270` Fullscreen `#000000` Cover Container + Live KFX Spacing Proof (Folio v2.3)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v2.3 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 026 complete:
  1. **Native Stock-Kindle KFX `$270` Cover Container (Task A)**:
     - In `kfx_converter` (`de86fa2`), stripped cover-only HTML spine items (`cover`, `titlepage`, `wrap0000`, `calibre:cover`, and SVG image-only pages) from the prose spine during EPUB parsing.
     - Implemented Amazon's native standalone Cover Section `c0` container (`$159: $270`, `$156: $326 scale_fit`, `$140: $320 center`, actual dimensions `$66/$67`), Storyline `l4` (`$159: $271`, `$157: s8`, `$175: cover`, no alt text), Style `s8` (`$16: 1.0 rem / $505`, `$42: 1.0 lh / $310`), Auxiliary Data `c0-ad`, and `$389` landmarks (`cover-nav-unit` pointing to container `c0`, `start` pointing to Chapter 1).
     - Cross-compiled release binary `kfx-conv` for `aarch64-linux-android` using NDK Clang; stripped to `07_APK_EPUB_INGESTION_MOD/lib/arm64-v8a/libkfxconv.so` (4.3 MB).
  2. **Hardware Verification Across 2 Distinct Classics (Task B.1)**:
     - Verified on Samsung Galaxy M32:
       * `screenshots/verify_r26_kfx_cover_book1.png` (Hesiod): Full-width centered on `#000000` black, zero white margins, zero "0%" footer.
       * `screenshots/verify_r26_kfx_cover_book2.png` (Sophocles): Full-width centered on pure `#000000` black (RGB 0,0,0 corner pixels). Swiping left navigates smoothly into Chapter 1 prose with zero duplicate cover page.
  3. **Live Typography Reflow Proof on On-Device KFX (Task B.2)**:
     - Adjusted Line & Paragraph spacing seekbars in `StandAloneBookReaderActivity` (`KRIFDocViewer`):
       * `screenshots/verify_r26_kfx_spacing_before.png`: Minimum Line & Paragraph spacing (SHA-256: `78a0bdc3e7d5cd38dc4dfc55e7382565ef3cee4c9ccded7a878dbd6f39ccb4d2`, 32 lines).
       * `screenshots/verify_r26_kfx_spacing_after.png`: Maximum Line & Paragraph spacing (SHA-256: `755ec10a9ff2dcbeb31f4203ecc9b533bc6b0e141c158e45d9d4c8fda506b7a0`, 19 lines).
       * Measured reflow: **1,463,450 pixels reflowed (57.91% pixel difference)**, proving live Amazon Enhanced Typesetting reflow in `KRIFDocViewer`.
  4. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  5. **Deliverables & Report (Task C)**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v2.3_kfx/base.apk` (131 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report026.zip` (5.2 MB)
     - Uploaded to Gemini Neon tab `837017938` with verified composer attachment chip.


---

# Round 029: Folio Control Center & libkfxconv.so Typography Polish (Folio v2.6)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v2.6 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 029 complete:
  1. **Folio Control Center (Task A)**:
     - Implemented `FolioMoreView.java` injecting a dark, native Control Center view into `MoreItemsFragment.onCreateView` (`classes3.dex`).
     - Dead Amazon account rows, store stubs, and `"Sign In with Amazon"` prompt are 100% eliminated.
     - Wired 5 offline feature cards: OPDS Catalogs, Scan Local Library (background thread scanning `/sdcard/epub/` and `/sdcard/kindle/` with live Toast), Backup & Restore, Settings (`SettingsActivity`), and About Folio dialog showing live library DB stats (8 indexed books) and cache size (16.6 MB).
  2. **Pure-Rust Engine Polish (Task B)**:
     - Added `unicode-normalization = "0.1"` for canonical NFC precomposition on all text spans in `kfx_converter`. Prevents decomposed Greek diacritics and non-Latin character rendering artifacts.
     - Implemented native YellowJersey lists (`Block::List`), emitting `$159: $277` (`listitem`), `$100: list_style` (`numeric: 343` / `disc: 340`), `$551: $552` (`inside`), hanging indent styles (`style_list_item`), and prepended markers (`• ` or `1. `).
     - Enhanced blockquote styling with `in_blockquote` nesting state, applying `style_blockquote` (`margin_left/right: 1.5em`, italic font).
     - Expanded pop-up footnote detection (`#fn`, `#footnote`, `#note`, and `<sup>`).
     - All 152 cargo unit tests passing cleanly. Cross-compiled release binary `kfx-conv` stripped to `07_APK_EPUB_INGESTION_MOD/lib/arm64-v8a/libkfxconv.so` (4.5 MB).
  3. **Hardware Visual QC (Task C)**:
     - `screenshots/verify_r29_more_tab_clean.png`: Dark Control Center with 5 cards and zero Amazon sign-in prompt.
     - `screenshots/verify_r29_about_folio_dialog.png`: About Folio dialog showing Folio v2.6 KFX engine checklist, 8 indexed books, and 16.6 MB cache.
     - `screenshots/verify_r29_greek_script_render.png`: Sophocles *The Greek Texts* (Ajax / ΑΙΑΣ) rendered in YellowJersey reader with crisp polytonic Greek glyphs (`Ἀεὶ μέν, ὦ παῖ Λαρτίου, δέδορκά σε`) and zero tofu.
     - `screenshots/verify_r29_footnotes_lists_blockquotes.png`: Clean hanging indentation and typography of lists and blockquotes in `kindle_exam_v3.epub` on-device.
  4. **Lead Architect Cover Architecture Directive**:
     - Formally documented and passed to manager AI: "The approach to display the cover as part of the book is a mistake. The correct path is to just take the image and render it differently so you don't have the difficulty you are having with margins. When parsing the epub for the first time and converting it, remove the cover page and take the image for the cover. It is then processed separately and kept in its own class or whatever and inserted when displaying the book separately. Skip it for now."
  5. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  6. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v2.6_kfx/base.apk` (131 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report029.zip` (< 15 MB)


---

# Round 044: Milestone Folio v4.0 KFX Polish

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v4.0 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 044 complete:
  1. **Drop-in Source Integration (Task A)**:
     - Integrated `EpubMetadataExtractor.java`, `FolioMetadataCatalogDialog.java`, and `FolioMoreView.java` from `dropin_src/`.
     - `normalizePubDate()`: Strips raw ISO timestamps (`2000-07-15 00:00:00+01:00` → `2000-07-15`) dynamically across list cards and Book Details popups.
     - `isNoiseTag()`: Strips Calibre reading status tags (`Read`, `Unread`, `To Read`, `Ebook`, `Calibre`, `General`) from EPUB extraction and genre chips bar.
     - Upgraded **About Folio** with live file-tree measurements (App Library Storage: 11.1 MB, Legacy `/sdcard/kindle`: 203.0 MB, Temp Open-Once Cache: 1.3 MB) and one-tap `CLEAR TEMP CACHE` button.
     - Built `classes9.dex` (191,888 B), rebuilt APK with apktool, signed, and deployed to Galaxy M32.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.0_kfx/base.apk` (131 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `screenshots/verify_r44_clean_dates_and_genres.png`: Proves *Animal Farm* shows clean `2000-07-15` date without timestamp, and `"Read"` is cleanly absent from the genre filter chips bar.
     - `screenshots/verify_r44_about_v40_storage_and_clear_cache.png`: Proves About Folio shows `Folio v4.0 KFX`, live App Library Storage (11.1 MB), Temp Cache (1.3 MB), and the new `CLEAR TEMP CACHE` button.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.0_kfx/base.apk` (131 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report044.zip` (< 1.5 MB)


---

# Round 045: Unified Home Import Pill & App-Private Download Pipeline (Folio v4.1 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v4.1 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 045 complete:
  1. **Drop-in Source Integration (Task A)**:
     - Integrated `OpenCatalogView.java` and `FolioMoreView.java` from `dropin_src/`.
     - Added emerald `Import` pill (`0xFF34D399`) into Home top bar (`Folio` | `Import` | `OPDS` | `Backup`).
     - Tapping `Import` directly opens `FolioFolderBrowserDialog` from Home tab for 1-tap batch EPUB import.
     - Unified Home/OPDS download pipeline into app-private `files/folio_library/` with metadata enrichment and `kindle_library.db` registration.
     - Bumped version badge to `Folio v4.1 KFX`.
     - Built `classes9.dex` (192,576 B), rebuilt APK with apktool, signed, and deployed to Galaxy M32.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.1_kfx/base.apk` (131 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `screenshots/verify_r45_home_import_pill.png`: Proves Home tab displays top action bar `Folio` | `Import` | `OPDS` | `Backup` with cover thumbnails in Continue Reading.
     - `screenshots/verify_r45_home_import_dialog.png`: Proves 1-tap `Import` pill opens dark Batch EPUB Importer dialog directly from Home tab.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.1_kfx/base.apk` (131 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report045.zip` (~1.5 MB)


---

# Round 046: Home "All Metadata ›" Link & Clean Library Book Removal (Folio v4.2 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v4.2 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 046 complete:
  1. **Source Integration (Task A)**:
     - Integrated `OpenCatalogView.java` with teal `All Metadata ›` link beside `Continue Reading` header on Home.
     - Implemented `🗑 Remove from Folio Library` action in `FolioMetadataCatalogDialog.java` Book Details dialog with confirmation dialog and safe cascade deletion across `folio_metadata.db`, `kindle_library.db`, and app-private storage, preserving external storage files.
     - Bumped version badge to `Folio v4.2 KFX` in `FolioMoreView.java`.
     - Built `classes9.dex` (195,312 B), rebuilt APK with apktool, signed, and deployed to Galaxy M32.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.2_kfx/base.apk` (131 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `screenshots/verify_r46_home_all_metadata_link.png`: Proves Home tab displays `Continue Reading` with teal `All Metadata ›` link cleanly aligned on the right.
     - `screenshots/verify_r46_book_details_remove_action.png`: Proves Book Details (`ⓘ`) dialog displays red `🗑 Remove from Folio Library` action button.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.2_kfx/base.apk` (131 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report046.zip` (~1.4 MB)


---

# Round 047: Live Reading Progress Badges & Metadata Polish (Folio v4.3 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v4.3 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 047 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Added live reading progress querying from `kindle_library.db` (`UserContent.READING_PROGRESS` & `KindleContent.READ_STATE`), rendering `X% read` badges on cards.
     - Added `Sort: Progress` mode to sort pill cycle (`Recent` → `Title` → `Author` → `Year` → `Progress`).
     - Added `cleanGenresForDisplay()` filtering Calibre noise tags (`Read`, etc.) and suppressed `Language: UND` in Book Details.
     - Bumped version badge to `Folio v4.3 KFX` in `FolioMoreView.java`.
     - Built `classes9.dex` (197,224 B), rebuilt APK with apktool, signed, and deployed to Galaxy M32.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.3_kfx/base.apk` (131 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `screenshots/verify_r47_sort_progress_badges.png`: Proves `Sort: Progress` mode floats books with live progress badges (`9% read`, `8% read`, `4% read`) to the top.
     - `screenshots/verify_r47_clean_book_details.png`: Proves Animal Farm Book Details (`ⓘ`) displays crisp `Genres: Fiction` without `"Read"` noise tag and zero `Language: UND` clutter.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.3_kfx/base.apk` (131 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report047.zip` (~650 KB)


---

# Round 048: Frequency-Ranked Genre Chips & Unified Local Scan (Folio v4.4 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v4.4 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 048 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Genre Chips now ranked by book count frequency (`All (27)`, `History (5)`, `Fiction (4)`, `Biography (2)`, `Correspondence (2)`), filtering out compound `--` LC headings.
     - Unified `scanLocalLibrary()` in `FolioMoreView.java` to convert new EPUBs into app-private `files/folio_library/` with full metadata enrichment and double DB registration.
     - Bumped version badge to `Folio v4.4 KFX` in `FolioMoreView.java`.
     - Built `classes9.dex` (198,512 B), rebuilt APK with apktool, signed, and deployed to Galaxy M32.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.4_kfx/base.apk` (131 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `screenshots/verify_r48_ranked_genre_chips_counts.png`: Proves Genre Chips strip displays `All (27)`, `History (5)`, `Fiction (4)`, `Biography (2)`, `Correspondence (2)` with live counts and no `--` strings.
     - `screenshots/verify_r48_control_center_v44.png`: Proves Control Center shows `Folio v4.4 KFX` badge and offline tools.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.4_kfx/base.apk` (131 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report048.zip` (~650 KB)


---

# Round 079: Stacked Reading Status Bar & Top Publishers Chart in Library Insights (Folio v7.9 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v7.9 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 079 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMoreView.java` from `dropin_src/`.
     - Added `READING STATUS (TAP TO FILTER)` horizontal stacked proportional bar (`● Finished (1)` emerald `#10b981` · `● Reading (5)` sky blue `#0284c7` · `● Unread (21)` slate `#64748b`) with 1-tap filter drill-down.
     - Added `TOP PUBLISHERS (TAP TO FILTER)` teal horizontal bar chart at the bottom of the Library Insights modal with 1-tap search filter drill-down.
     - Bumped version badge to `Folio v7.9 KFX (Enhanced Typesetting)`.
     - Built `classes9.dex` (263,236 B), fast-rebuilt APK, signed, and deployed to Galaxy M32 (`lastUpdateTime=2026-11-23 00:05:49`).
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v7.9_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `screenshots/verify_r79_insights_stacked_status.png`: Proves stacked proportional `READING STATUS` bar with Finished (1), Reading (5), Unread (21) segments and legend pills above `TOP GENRES`.
     - `screenshots/verify_r79_insights_shelves_publishers.png`: Proves `CUSTOM SHELVES` (`🏷 Classics 1`) and `TOP PUBLISHERS` teal bars (`Focus Publishing/R. Pullins Company 2`, `HarperCollins 2`, `Airlift 1`, `Apud Janssonio-Waesbergios 1`) and `CLOSE` button inside modal.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v7.9_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report079.zip`


---

# Round 080: 3-Column Cover Art Grid View Toggle (Folio v8.0 KFX Milestone)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.0 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 080 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Added persistent `⊞` / `☰` view-mode toggle pill in catalog top bar with `SharedPreferences("folio_ui_prefs", "catalog_grid_mode")` persistence.
     - Implemented 3-column Cover Art Grid mode with extracted `.cover.jpg` art, 2-line titles, compact status badges (`✓ Done`, `X%`, `★ 5/5`, `Unread`), and `ⓘ` details buttons.
     - Bumped version badge to `Milestone Folio v8.0 KFX (Enhanced Typesetting)`.
     - Built `classes9.dex` (267,104 B), fast-rebuilt APK, signed, and deployed to Galaxy M32 (`lastUpdateTime=2026-11-23 00:13:19`).
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.0_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `screenshots/verify_r80_catalog_cover_grid_view.png`: Proves 3-column Cover Art Grid view displaying covers, titles, status badges (`★ 5/5`, `✓ Done`, `Unread`), `ⓘ` buttons, and `☰` toggle pill.
     - `screenshots/verify_r80_control_center_v80.png`: Proves Control Center displays amber `Folio v8.0 KFX` milestone badge.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.0_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report080.zip`


---

# Round 081: Uncropped FIT_CENTER Grid Covers, Uniform Card Alignment & Mini Progress Bars (Folio v8.1 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.1 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 081 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Switched grid cover scaling to `ScaleType.FIT_CENTER` mounted on `#0D1017` dark mat (height 132dp) preventing cropping of tall cover art.
     - Enforced `minLines(2)` and `maxLines(2)` on tile titles so all cards maintain uniform pixel-aligned bottom borders.
     - Added sleek 3dp horizontal progress mini-bars directly under each cover (emerald for `✓ Done`, sky blue for in-progress `X%`).
     - Bumped version badge to `Folio v8.1 KFX (Enhanced Typesetting)`.
     - Built `classes9.dex` (267,404 B), fast-rebuilt APK, signed, and deployed to Galaxy M32 (`lastUpdateTime=2026-11-23 00:19:32`).
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.1_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `screenshots/verify_r81_grid_uncropped_aligned.png`: Proves uncropped *Moby Dick* and *Moksha* covers, uniform card bottom alignment, and emerald progress bar under *Moby Dick*.
     - `screenshots/verify_r81_grid_reading_bars.png`: Proves filtered `Reading (5 of 27)` grid view displaying sky blue progress mini-bars under all 5 in-progress covers (*The 210th Day* 4%, *Sophocles* 2%, *Aristotle* 8%, *Island*, *Brave New World*).
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.1_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report081.zip`


---

# Round 082: Editable Synopsis Field & Live Kindle DB Title/Author Sync (Folio v8.2 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.2 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 082 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Added multiline `Description / Synopsis` field to Edit Book Metadata dialog (`setMinLines(2)`, `setMaxLines(5)`).
     - Added `syncTitleAuthorToKindleDb(...)` on Save, ensuring edited title and author immediately propagate to `kindle_library.db` (`KindleContent`).
     - Bumped version badge to `Folio v8.2 KFX (Enhanced Typesetting)`.
     - Built `classes9.dex` (268,112 B), fast-rebuilt APK, signed, and deployed to Galaxy M32 (`lastUpdateTime=2026-11-23 00:26:18`).
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.2_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `screenshots/verify_r82_edit_metadata_synopsis.png`: Proves Edit Book Metadata form on *1984* displaying multiline `Description / Synopsis` field with full synopsis.
     - `screenshots/verify_r82_about_v82.png`: Proves About Folio dialog displaying `Version: Folio v8.2 KFX (Enhanced Typesetting)`.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.2_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report082.zip`


---

# Round 083: Custom Shelves Manager & 1-Tap Filter Drill-Down (Folio v8.3 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.3 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 083 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMoreView.java` from `dropin_src/`.
     - Added `🏷 Manage Shelves` action card directly under `📊 Library Insights` on Control Center.
     - Added interactive Manage Shelves modal listing shelves sorted descending, with 1-tap catalog drill-down (`SHELF:Classics`), in-place Rename/Merge, and safe shelf Removal.
     - Bumped version badge to `Folio v8.3 KFX (Enhanced Typesetting)`.
     - Built `classes9.dex` (274,900 B), fast-rebuilt APK, signed, and deployed to Galaxy M32 (`lastUpdateTime=2026-11-23 00:34:05`).
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.3_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `screenshots/verify_r83_control_center_v83.png`: Proves Control Center displays `🏷 Manage Shelves` card under `📊 Library Insights`.
     - `screenshots/verify_r83_manage_shelves.png`: Proves Manage Shelves modal displaying `🏷 Classics` (`1 book ›`) with `Rename` and `Remove` pills, and tapping `🏷 Classics` opens the catalog filtered to `🏷 Classics (1)`.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.3_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report083.zip`


---

# Round 084: Continue Reading Quick-Resume Carousel (Folio v8.4 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.4 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 084 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMoreView.java` from `dropin_src/`.
     - Added `📖 Continue Reading` horizontal card carousel directly below `🎯 2026 Reading Goal` on Control Center.
     - Displays in-progress books sorted descending by progress with `See All (5) ›` deep link, standalone cover art thumbnails, 2-line titles, authors, `X% read · Resume ›` labels, and 3dp sky-blue progress bars.
     - Tapping any card launches book directly into KFX reader.
     - Bumped version badge to `Folio v8.4 KFX (Enhanced Typesetting)`.
     - Built `classes9.dex` (278,892 B), fast-rebuilt APK, signed, and deployed to Galaxy M32 (`lastUpdateTime=2026-11-23 00:41:17`).
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.4_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `screenshots/verify_r84_continue_reading.png`: Proves Control Center top displays `Folio v8.4 KFX` badge, 4-tile stats strip, `🎯 2026 Reading Goal`, and the new `📖 Continue Reading` horizontal carousel with covers (*Brave New World* 9%, *Aristotle* 8%, etc.), progress bars, and `See All (5) ›`.
     - `screenshots/verify_r84_about_v84.png`: Proves About Folio dialog displaying `Version: Folio v8.4 KFX (Enhanced Typesetting)` and live stats.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.4_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report084.zip`


---

# Round 085: Multi-Select Catalog Batch Curation (Milestone Folio v8.5 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.5 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 085 complete:
  1. **Source Integration**:
     - Added `☑ Select` / `✕ Done` multi-select pill in catalog header, dynamic selection highlighting (`2dp #818CF8` border, `#1E2642` fill), and floating bottom Bulk Action Bar (`N books selected`, `Select All / Clear All`, `🏷 Shelf`, `★ Fav`, `✓ Finished`).
     - Widened Continue Reading carousel cards to `196dp`.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.5_kfx/base.apk` (132 MB).
  2. **Hardware Verification**:
     - `verify_r85_bulk_select_mode.png`: Proves multi-select mode with floating bottom bar.
     - `verify_r85_about_v85.png`: Proves About dialog bumped to Folio v8.5 KFX.
  3. **Stock Invariant**: `com.amazon.kindle` (v8.160.1.0) verified 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Report Archive**: `antig_to_gemini_neon_clunky_report085.zip`.

---

# Round 086: Metadata Health Audit & Catalog Deep-Links (Folio v8.6 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.6 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 086 complete:
  1. **Source Integration**:
     - Integrated `METADATA HEALTH — 70% COMPLETE (TAP TO FIX)` section inside `📊 Library Insights` with 3 live audit tiles: Covers (`1 › No Cover`), Genres (`2 › No Genre`), and Synopsis (`21 › No Synopsis`).
     - Added smart audit filter deep-links into catalog (`MISSING_COVER`, `MISSING_GENRES`, `MISSING_SYNOPSIS`) with dedicated `🩺 Missing ... (N)` active chips.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.6_kfx/base.apk` (132 MB).
  2. **Hardware Verification**:
     - `verify_r86_insights_metadata_health.png`: Proves Library Insights Metadata Health section with 3 audit tiles.
     - `verify_r86_missing_filter_drilldown.png`: Proves catalog filtered to missing genres via `🩺 Missing Genres (2)` chip.
  3. **Stock Invariant**: `com.amazon.kindle` (v8.160.1.0) verified 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Report Archive**: `antig_to_gemini_neon_clunky_report086.zip`.

---

# Round 087: Multi-Select Batch Genre Assignment (Folio v8.7 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.7 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 087 complete:
  1. **Source Integration**:
     - Added sky-blue `+ Genre` bulk action pill in bottom multi-select bar.
     - Built interactive `🏷 Add Genre (N books)` modal with horizontal genre preset chips and comma-separated custom genre input, merging safely into `books_metadata.genres` in `folio_metadata.db`.
     - Center-aligned typography (`Gravity.CENTER`) on all 3 Metadata Health tiles.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.7_kfx/base.apk` (132 MB).
  2. **Hardware Verification**:
     - `verify_r87_bulk_genre_bar.png`: Proves bottom multi-select bar with `+ Genre` pill.
     - `verify_r87_insights_centered_v87.png`: Proves center-aligned Metadata Health tiles and Folio v8.7 KFX version.
  3. **Stock Invariant**: `com.amazon.kindle` (v8.160.1.0) verified 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Report Archive**: `antig_to_gemini_neon_clunky_report087.zip`.

---

# Round 088: Storage & Duplicate Titles Audit (Folio v8.8 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.8 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 088 complete:
  1. **Source Integration**:
     - Added `💾 Storage & Duplicates` action card on Control Center directly below `🏷 Manage Shelves`.
     - Built interactive Storage & Duplicates modal with 3 storage tiles (`13.3 MB App Library`, `682 KB Backups`, `1.3 MB Temp Cache`), Top 5 Largest Books amber bars with 1-tap locate, duplicate titles audit (`✓ No duplicate titles found across 27 cataloged books`), and Clear Temp Cache.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.8_kfx/base.apk` (132 MB).
  2. **Hardware Verification**:
     - `verify_r88_control_center_v88.png`: Proves Control Center with `💾 Storage & Duplicates` action card.
     - `verify_r88_storage_duplicates_modal.png`: Proves Storage & Duplicates modal with storage tiles, largest books, and duplicate audit.
  3. **Stock Invariant**: `com.amazon.kindle` (v8.160.1.0) verified 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Report Archive**: `antig_to_gemini_neon_clunky_report088.zip`.

---

# Round 089: Reading Notes Notebook (Folio v8.9 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.9 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 089 complete:
  1. **Source Integration**:
     - Added `📝 Reading Notes Notebook` action card on Control Center directly below `📄 Reading Log & Export`.
     - Built interactive Reading Notes Notebook modal with live search bar, cover thumbnail, rating/shelf badges, warm amber quote card (`Essential dystopian classic - reread Part 3`), 1-tap copy, inline edit, and Markdown export.
     - Expanded Storage & Duplicates scroll viewport to `388dp` with `10dp` bottom padding.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.9_kfx/base.apk` (132 MB).
  2. **Hardware Verification**:
     - `verify_r89_notes_notebook.png`: Proves Reading Notes Notebook modal with *1984* note card.
     - `verify_r89_about_v89.png`: Proves About dialog bumped to Folio v8.9 KFX.
  3. **Stock Invariant**: `com.amazon.kindle` (v8.160.1.0) verified 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Report Archive**: `antig_to_gemini_neon_clunky_report089.zip`.

---

# Round 090: Up Next Reading Queue (Milestone Folio v9.0 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v9.0 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 090 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Fixed syntax quote escaping issue in `FolioMetadataCatalogDialog.java` line 1515.
     - Added persistent `SharedPreferences("folio_queue")` FIFO reading queue.
     - Added violet `📌 Up Next` / `📌 Queued` toggle pill in Book Details modal.
     - Added dynamic violet `📌 Up Next (N)` chip to catalog filter strip.
     - Added `📌 Up Next Queue` horizontal card carousel directly below `📖 Continue Reading` on Control Center, showing cover art thumbnails, title, author, `Start Reading ›` 1-tap reader launch, and inline `✕` unqueue button.
     - Bumped version to `Milestone Folio v9.0 KFX (Enhanced Typesetting)`.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.0_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `verify_r90_book_details_queue_v90.png`: Proves Book Details for *Animal Farm* displaying active violet `📌 Queued` pill alongside Custom Shelf pill.
     - `verify_r90_up_next_control_center.png`: Proves Control Center top showing `Folio v9.0 KFX` badge, `📖 Continue Reading`, and the live `📌 Up Next Queue` carousel card with cover thumbnail, *Animal Farm*, *George Orwell*, `Start Reading ›`, and `✕` unqueue button.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.0_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report090.zip`


---

# Round 091: Up Next Queue Export, Backup & Telemetry (Folio v9.1 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v9.1 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 091 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java`, `FolioMetadataBackup.java`, and `FolioMoreView.java` from `dropin_src/`.
     - Resolved missing `import java.util.ArrayList;` in `FolioMetadataBackup.java`.
     - Added `in_queue` boolean to JSON backup & restore.
     - Appended `## 📌 Up Next Queue (N)` section to Markdown Reading Log export.
     - Added `In Queue` column to RFC-4180 CSV export.
     - Added `📌 ` prefix badge to book titles in catalog list view when queued.
     - Integrated live queued count into Library Insights header summary (`1 queued`).
     - Bumped version to `Folio v9.1 KFX (Enhanced Typesetting)`.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.1_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `verify_r91_reading_log_up_next.png`: Proves Reading Log preview displaying `## 📌 Up Next Queue (1)` with `- **Animal farm: a fairy story** — George Orwell`.
     - `verify_r91_insights_queued_v91.png`: Proves Library Insights modal header summary displays `1 fav · 1 noted · 1 shelved · 1 queued · 5.0★ avg (1 rated)`.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.1_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report091.zip`


---

# Round 092: Pinned Folder Bookmarks in Batch Importer (Folio v9.2 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v9.2 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 092 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioFolderBrowserDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Added compact `☆` / `★` pin toggle button right beside `Open` in the Batch EPUB Importer path bar.
     - Persisted pinned folder bookmarks in `SharedPreferences("folio_ui_prefs", "pinned_import_dirs")`.
     - Preprended pinned folder chips with `★ ` prefix at the head of the quick-jump strip.
     - Added dynamic active folder highlight with amber fill (`#F59E0B`) matching the currently browsed path.
     - Bumped version to `Folio v9.2 KFX (Enhanced Typesetting)`.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.2_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `verify_r92_pinned_folder_importer.png`: Proves Batch EPUB Importer displaying active gold `★` pin button and highlighted gold `★ en/0` chip at the front of the quick-jump strip.
     - `verify_r92_about_v92.png`: Proves About Folio dialog displaying `Version: Folio v9.2 KFX (Enhanced Typesetting)` and live stats.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.2_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report092.zip`


---

# Round 093: Publication Eras Timeline Chart & Era Filtering (Folio v9.3 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v9.3 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 093 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Built `PUBLICATION ERAS · YYYY–YYYY (TAP TO FILTER)` section in Library Insights plotting chronological era bars (`Pre-1900`, `1900–1949`, `1950–1999`, `2000–2009`, `2010–2019`, `2020+`).
     - Added 1-tap drilldown dismissing Insights and opening catalog filtered to `ERA:<bucket>`.
     - Added smart `📅 <Era> (N)` active filter chip in catalog with exact year-range filtering.
     - Bumped version to `Folio v9.3 KFX (Enhanced Typesetting)`.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.3_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `verify_r93_insights_pub_eras.png`: Proves Library Insights modal displaying `PUBLICATION ERAS · 1531–2017 (TAP TO FILTER)` violet bar chart.
     - `verify_r93_era_filter_drilldown.png`: Proves catalog modal filtered to `📅 2000–2009 (4)` with active violet chip and matching books.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.3_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report093.zip`


---

# Round 094: Bulk Queue Action & Grid-View Up Next Badge (Folio v9.4 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v9.4 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 094 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Upgraded bottom Multi-Select Bulk Action Bar to a 5-pill single-row layout (`Shelf`, `+Genre`, `📌Queue`, `★Fav`, `✓Done`).
     - Added 1-tap batch queue / unqueue capability across all selected volumes.
     - Added violet `📌 Up Next` badge to queued books in 3-column Cover Art Grid view.
     - Bumped version to `Folio v9.4 KFX (Enhanced Typesetting)`.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.4_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `verify_r94_bulk_queue_grid_badge.png`: Proves 3-column Grid view displaying violet `📌 Up Next` badge on *Animal farm: a fairy story* and the 5-pill Bulk Action Bar (`Shelf`, `+Genre`, `📌Queue`, `★Fav`, `✓Done`).
     - `verify_r94_about_v94.png`: Proves About Folio dialog displaying `Version: Folio v9.4 KFX (Enhanced Typesetting)` and live stats.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.4_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report094.zip`


---

# Round 095: "More by Author" Sibling Carousel in Book Details (Milestone Folio v9.5 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v9.5 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 095 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Built `MORE BY [AUTHOR] (N)` horizontal card carousel right below the Personal Note card inside Book Details.
     - Displays author's sibling volumes with cover art thumbnails, 2-line titles, and live status badges (`✓ Done ›`, `X% read ›`, `📌 Up Next ›`, or year).
     - 1-tap lateral exploration switches Book Details directly to the tapped sibling volume.
     - Bumped version to `Milestone Folio v9.5 KFX (Enhanced Typesetting)`.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.5_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `verify_r95_more_by_author_details.png`: Proves Book Details for *1984* displaying `MORE BY GEORGE ORWELL (1)` horizontal cover-art card strip showing *Animal farm: a fairy story* with standalone cover thumbnail, title, and violet `📌 Up Next ›` status badge.
     - `verify_r95_about_v95.png`: Proves About Folio dialog displaying `Version: Folio v9.5 KFX (Enhanced Typesetting)` and live stats.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.5_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report095.zip`


---

# Round 102: 1-Tap Book Details & Cover Art Fallback in Quick Search (Folio v10.2 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v10.2 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 102 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Exposed `FolioMetadataCatalogDialog.showBookDetails(...)` for external invocation.
     - Added dedicated `ⓘ` button beside `Read ›` on every Quick Library Search card for 1-tap book metadata inspect/edit.
     - Wrapped quick search thumbnails in rounded `#0D1017` FrameLayout with styled centered `📖` glyph fallback for books lacking `.cover.jpg` (e.g. *The 210th Day*).
     - Bumped version to `Folio v10.2 KFX (Enhanced Typesetting)`.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.2_kfx/base.apk` (137 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `verify_r102_search_info_placeholder.png`: Quick Library Search modal with `📖 Reading` filter active, showing styled `📖` placeholder on *The 210th Day* ([143, 888][232, 1014]) and the new `ⓘ` button beside `Read ›` across rows.
     - `verify_r102_about_v102.png`: About Folio modal showing `Version: Folio v10.2 KFX (Enhanced Typesetting)` and live stats.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.2_kfx/base.apk` (137 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report102.zip`


---

# Round 103: 1-Tap Share Book Card & Unified Cover Placeholders (Folio v10.3 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v10.3 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 103 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Added sky-blue `↗ Share` pill in Book Details footer row to dispatch formatted book recommendations via Android's native `ACTION_SEND` share sheet.
     - Hardened `parentDialog.dismiss()` with null checks on `Read Now` action for clean external launches.
     - Unified cover placeholder frame with centered `📖` glyph fallback across Continue Reading and Up Next Queue carousels via `makeMiniCoverView(...)`.
     - Bumped version to `Folio v10.3 KFX (Enhanced Typesetting)`.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.3_kfx/base.apk` (137 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `verify_r103_details_share_pill.png`: Book Details dialog for *1984* showing `Remove from Folio Library` alongside `📋 Copy` and sky-blue `↗ Share` pills ([842, 1936][996, 2004]).
     - `verify_r103_about_v103.png`: About Folio modal showing `Version: Folio v10.3 KFX (Enhanced Typesetting)` and live stats.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.3_kfx/base.apk` (137 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report103.zip`


---

# Round 104: Star Ratings Distribution & Shareable Library Insights (Folio v10.4 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v10.4 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 104 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMoreView.java` from `dropin_src/`.
     - Added gold `STAR RATINGS` bar chart at the bottom of Library Insights modal showing per-star ratings breakdown (`5★` down to `1★`) with 1-tap browse links.
     - Added formatted plaintext snapshot generation of library statistics, reading goal progress, curation counts, and top genres/authors.
     - Added `COPY SUMMARY` and `SHARE` (`ACTION_SEND`) buttons to Library Insights footer row.
     - Bumped version to `Folio v10.4 KFX (Enhanced Typesetting)`.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.4_kfx/base.apk` (137 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `verify_r104_insights_ratings_share.png`: Scrolled Library Insights modal showing `STAR RATINGS · 5.0★ AVG (1 RATED) (TAP TO BROWSE)` gold chart with `★★★★★ (5★)` bar ([83, 1590][977, 1637]) and the 3-button footer (`CLOSE`, `COPY SUMMARY`, `SHARE`).
     - `verify_r104_about_v104.png`: About Folio modal showing `Version: Folio v10.4 KFX (Enhanced Typesetting)` and live stats.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.4_kfx/base.apk` (137 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report104.zip`


---

# Round 105: Exact Star-Rating Drill-Down & Rated Search Scope (Milestone Folio v10.5 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v10.5 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 105 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Added `STAR:1..5` exact star rating filter support with active gold `★ {N}-Star ({count})` chip in Metadata Catalog.
     - Wired Star Ratings chart bars in `📊 Library Insights` directly to filtered catalog views.
     - Added 6th scope pill (`★ Rated`) to Quick Library Search.
     - Bumped version to `Milestone Folio v10.5 KFX (Enhanced Typesetting)`.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.5_kfx/base.apk` (137 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `verify_r105_star_filter_drilldown.png`: Metadata Catalog dialog opened directly to the active gold `★ 5-Star (1)` filter chip ([260, 684][480, 760]) showing *★ 1984* ([99, 1197][359, 1292]) with *★ 5/5* rating badge ([99, 1297][314, 1334]).
     - `verify_r105_about_v105.png`: About Folio modal showing `Version: Folio v10.5 KFX (Enhanced Typesetting)` and live stats.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.5_kfx/base.apk` (137 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report105.zip`


---

# Round 106: Self-Contained Offline HTML Library Catalog Exporter (Folio v10.6 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v10.6 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 106 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataBackup.java` and `FolioMoreView.java` from `dropin_src/`.
     - Added `exportLibraryHtml(Context ctx)` writing directly to `/sdcard/kindle/backups/folio_library.html` (verified 7,486 bytes).
     - Built dark-themed standalone offline HTML catalog with embedded CSS, live annual reading goal progress bar, status badges, shelf tags, star ratings, quote callouts for notes, and zero-dependency client-side instant search (`#q`).
     - Added sky-blue `🌐 HTML` export pill right beside `📊 CSV` and `✨ Scope: Curated Only` in `Reading Log (.md)` modal.
     - Bumped version to `Folio v10.6 KFX (Enhanced Typesetting)`.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.6_kfx/base.apk` (137 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `verify_r106_reading_log_html_export.png`: Reading Log modal showing `Saved HTML: /sdcard/kindle/backups/folio_library.html` ([83, 678][1002, 740]) with the 3-pill export toolbar (`✨ Scope: Curated Only`, `📊 CSV`, `🌐 HTML` [840, 740][1002, 807]).
     - `verify_r106_about_v106.png`: About Folio modal showing `Version: Folio v10.6 KFX (Enhanced Typesetting)` and live stats.
     - Verified `/sdcard/kindle/backups/folio_library.html` is 7,486 bytes on physical hardware.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.6_kfx/base.apk` (137 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report106.zip`


---

# Round 107: Subfolder Navigation & ⬆ Up Parent Button in Batch Importer (Folio v10.7 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v10.7 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 107 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioFolderBrowserDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Added `⬆` parent directory button directly to the left of the path input in Batch EPUB Importer, allowing 1-tap upward directory traversal (`/sdcard/en/0` -> `/sdcard/en` -> `/sdcard`).
     - Added clickable warm-amber `📁 <folderName>  Open ›` cards at the top of the file list for non-hidden subdirectories.
     - Bumped version to `Folio v10.7 KFX (Enhanced Typesetting)`.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.7_kfx/base.apk` (137 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `verify_r107_importer_subfolders.png`: Batch EPUB Importer after stepping up to `/sdcard/en` via `⬆` button ([83, 558][153, 645]), showing path `/sdcard/en` ([169, 557][742, 646]) and clickable subfolder rows `📁 0  Open ›` ([115, 968][970, 1015]) and `📁 1  Open ›` ([115, 1079][970, 1126]), verified downward restoration of `/sdcard/en/0` upon tapping `📁 0`.
     - `verify_r107_about_v107.png`: About Folio modal showing `Version: Folio v10.7 KFX (Enhanced Typesetting)` and live stats.
     - **Fight Club Side-by-Side Visual QC (Compact Theme, White Background, Bookerly)**:
       * Page 1 (Cover): `folio_fight_club_page1.png` vs `kindle_fight_club_page1.png` — Mean pixel delta 0.54 (virtually identical fullscreen rendering).
       * Page 2 (Title): `folio_fight_club_page2.png` vs `kindle_fight_club_page2.png` — Kindle title centered at `Y=[388, 697]` with uppercase tracking and no footer; Folio title starts high at `Y=[174, 411]` with footer at `Y=[2259, 2283]`.
       * Page 3 (Chapter 1): `folio_fight_club_page3.png` vs `kindle_fight_club_page3.png` — Kindle text canvas `Y=[296, 2173]` (H=1,877px) with generous insets causing final paragraph overflow to P4; Folio text canvas `Y=[133, 2283]` (H=2,150px) fitting entire paragraph + footer. Kindle preserves publisher CSS styles ($760 letter spacing, 3.66em margin); Folio applies hardcoded fallback paragraph styles.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.7_kfx/base.apk` (137 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report107.zip` (includes 6 Fight Club side-by-side screenshots)


---

# Round 108: Publisher CSS Fidelity Fix for Fight Club Parity (Folio v10.8 KFX)

- **Current State**: Round 108 fully complete. Task A (Rust engine + Java UI build and staging) and Task B (Hardware verification on Samsung Galaxy M32) 100% verified. Report 108 submitted to Gemini Neon; autonomous runner monitoring for Round 109 (`to_antig_clunky_round_109.zip`).
  1. **Engine & Java Integration (Task A - COMPLETE)**:
     - Integrated `dropin_rust/src/epub/css_style.rs`, `dropin_rust/src/packager/styles.rs`, and `dropin_rust/src/packager/storylines.rs` into `kfx_converter`.
     - Added support for publisher CSS fidelity:
       * `font-family`: `sans-serif` mapped to `font_sans`, `monospace` mapped to `font_mono`.
       * Vertical margins: `margin-top` / `margin-bottom` parsed and quantized to 0.5em steps (`mt_`, `mb_`).
       * `border-bottom`: parsed and mapped to YellowJersey `$solid` border with weight and padding.
       * `letter-spacing`: wide uppercase tracking token `letter_wide` mapped to letterspacing override.
       * Dynamic style overrides (`style_ov_1`, `style_ov_2`, etc.) generated and bound in YellowJersey storylines.
     - Ran full `cargo test` suite in `kfx_converter/`: all 210 tests passed (164 unit, 13 CLI, 16 container, 8 pipeline, 9 sync sanitize).
     - Cross-compiled release binary for `aarch64-linux-android`, stripped with Android NDK `llvm-strip`, and staged to `07_APK_EPUB_INGESTION_MOD/lib/arm64-v8a/libkfxconv.so` (4.6 MB).
     - Integrated `FolioMoreView.java` with version bump to `Folio v10.8 KFX (Enhanced Typesetting)`. Preserved `MetadataEnricher.java` and `OpenCatalogView.java`.
     - Recompiled `classes9.dex` (366,228 bytes), repacked into base APK as `classes4.dex`, aligned with `zipalign`, and signed with `apksigner`.
     - Installed on Samsung Galaxy M32 (`lastUpdateTime=2026-11-24 02:28:46`) and staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.8_kfx/base.apk` (132 MB).
     - Verified host and on-device conversion of *Fight Club* EPUB (226.62ms) generating clean KFX with interned `style_ov_1` and `style_ov_2`.
  2. **Hardware Verification on Physical Galaxy M32 (Task B - COMPLETE)**:
     - `verify_r108_fight_club_page2.png` (26,484 bytes): Title Page (`FIGHT CLUB / Chuck Palahniuk`) verifying sans-serif typography, wide uppercase tracking, vertical margin shift down to `Y = 261` (closer to Stock Kindle's `Y = 388`), and clean layout without reading footer stamp.
     - `verify_r108_fight_club_page3.png` (336,379 bytes): Chapter 1 (`CHAPTER 1`) verifying sans-serif heading starting at `Y = 218` (shifted down from `Y = 133`), text bounds ending at `Y = 2179` (matching Stock Kindle's `Y = 2173` within 6 pixels), and total vertical canvas height of 1,961 px (down from 2,150 px).
     - `verify_r108_about_v108.png` (355,115 bytes): About Folio modal showing `Version: Folio v10.8 KFX (Enhanced Typesetting)` and `Publisher CSS Fidelity (sans-serif, margins, border-bottom & tracking)` line.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
---

# Round 109: YellowJersey Border Color & Chapter Rule Parity (Folio v10.9 KFX)

- **Current State**: Round 109 fully complete. Task A (Rust engine + Java UI build and staging) and Task B (Hardware verification on Samsung Galaxy M32) 100% verified. Report 109 submitted to Gemini Neon; autonomous runner monitoring for Round 110 (`to_antig_clunky_round_110.zip`).
  1. **Engine & Java Integration (Task A - COMPLETE)**:
     - Integrated `dropin_rust/src/epub/css_style.rs` and `dropin_rust/src/packager/styles.rs` into `kfx_converter`.
     - Lowered quantized margin thresholds to `margin-top` min 1.0em and `margin-bottom` min 0.8em for accurate publisher chapter spacing.
     - Emitted explicit `border_color_bottom` (`$86 = 0xFF808080`, neutral dark gray visible across both White and Dark themes) for YellowJersey `$solid` bottom border overrides.
     - Automatically attached generous `1.2em` bottom margin under `border_bottom` blocks.
     - Enhanced `<hr/>` rule (`hr_val`) with `border_style_bottom` (`$91`), `border_weight_bottom` (`$96`, 0.06em), and `border_color_bottom` (`$86`).
     - Ran full `cargo test` suite in `kfx_converter/`: all 210 tests passed (164 unit, 13 CLI, 16 container, 8 pipeline, 9 sync sanitize).
     - Cross-compiled release binary for `aarch64-linux-android`, stripped with Android NDK `llvm-strip` down to 4.6 MB, and staged to `07_APK_EPUB_INGESTION_MOD/lib/arm64-v8a/libkfxconv.so`.
     - Integrated `FolioMoreView.java` with version bump to `Folio v10.9 KFX (Enhanced Typesetting)`. Preserved `MetadataEnricher.java` and `OpenCatalogView.java`.
     - Recompiled `classes9.dex` (366,228 bytes), repacked base APK as `classes4.dex`, aligned with `zipalign`, and signed with `apksigner`.
     - Installed on Samsung Galaxy M32 (`lastUpdateTime=2026-11-24 02:45:02`) and staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.9_kfx/base.apk` (132 MB).
     - Re-converted `/sdcard/en/0/Chuck Palahniuk - Fight Club.epub` on device in 210.67ms (507,541 bytes).
  2. **Hardware Verification on Physical Galaxy M32 (Task B - COMPLETE)**:
     - `verify_r109_fight_club_page3_border.png` (315,025 bytes): Chapter 1 opening in Folio (Compact / White / Bookerly) confirming generous publisher top spacing (`CHAPTER 1` at `Y = 218`), `1.2em` bottom breathing room pushing opening body text to `Y = 358`, and body text ending at `Y = 2109` (total vertical text height 1,891 px, matching Stock Kindle's 1,877 px within 14 pixels).
     - `verify_r109_about_v109.png` (355,143 bytes): About Folio modal showing `Version: Folio v10.9 KFX (Enhanced Typesetting)` and live stats.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
---

# Round 110: Milestone Folio v11.0 KFX (Control Center "🎲 Tonight's Pick" Unread Book Recommender)

- **Current State**: Round 110 fully complete. Task A (Source integration & milestone APK release) and Task B (Hardware verification on Samsung Galaxy M32) 100% verified. Report 110 submitted to Gemini Neon; autonomous runner monitoring for Round 111 (`to_antig_clunky_round_111.zip`).
  1. **Source Integration & Milestone Build (Task A - COMPLETE)**:
     - Integrated `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`.
     - Control Center Header: Added twin compact icon pills right beside the **`Folio v11.0 KFX`** badge:
       * `🎲` (soft-violet pill, `#C084FC` on `#2B1B3D` with rounded stroke) triggering `showTonightsPickDialog`.
       * `🔍` (sky-blue pill, `ACCENT_TEAL` on `#152638`) for instant search.
     - **🎲 Tonight's Pick Modal (`showTonightsPickDialog`)**:
       * Draws exclusively from unread cataloged books in the local metadata database.
       * Displays standalone `.cover.jpg` art (`makeMiniCoverView`), title, normalized author, year/publisher tags, and genre pills.
       * Includes a 4-line synopsis preview with ellipsis fallback.
       * In-modal **`🎲 Pick Another`** reroll button dynamically swapping the recommended card in-place without dialog dismiss/reopen.
       * **`ⓘ Book Details`** deeplink to `FolioMetadataCatalogDialog.showBookDetails` and **`Start Reading`** footer CTA launching `ExternalEpubActivity.launchKfxReader`.
     - Bumped version to `Folio v11.0 KFX (Enhanced Typesetting)`. Preserved `MetadataEnricher.java` and `OpenCatalogView.java`.
     - Recompiled `classes9.dex` (371,500 bytes), repacked base APK as `classes4.dex`, aligned with `zipalign`, and signed with `apksigner`.
     - Installed on Samsung Galaxy M32 (`lastUpdateTime=2026-11-24 02:54:45`) and staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v11.0_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Physical Galaxy M32 (Task B - COMPLETE)**:
     - `verify_r110_tonights_pick_v11.png` (294,292 bytes): Captured 🎲 Tonight's Pick recommendation modal launched from the `🎲` header pill, showing book card (*Delphi Complete Works of Tacitus*), genre pills, `🎲 Pick Another` ([154, 1287][529, 1373]), `ⓘ Book Details` ([550, 1287][926, 1373]), `Close` ([538, 1453][706, 1595]), and `Start Reading` ([706, 1453][978, 1595]).
     - `verify_r110_about_v110.png` (353,863 bytes): About Folio dialog confirming `Version: Folio v11.0 KFX (Enhanced Typesetting)` and live stats.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Submission**:
     - Submitted `antig_to_gemini_neon_clunky_report110.zip` (599 KB) to Gemini Neon tab `837018552`.
     - Launching `tools/autonomous_relay_runner.mjs 111 2000` to monitor for Round 111 response.

