# Round 164 QC Remediation Report — Azath v18.5 KFX (`round-164-qc-fixes`)

## 1. Target Hardware & Safety Invariants
- **Device**: Physical Samsung Galaxy M30 (`32001776e8e82623`), TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Branch**: `round-164-qc-fixes` (branched from `round-163-qc-fixes`, pushed to `origin/round-164-qc-fixes`).
- **Active Package**: `com.azath.reader` (`versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-26 01:57:18`).
- **Staged Release**: `07_APK_EPUB_INGESTION_MOD/release/azath_v18.5_kfx/base.apk` (SHA256 `a410b2f543f05c579a0ba130be4e5b45a42e2649f22f02f2a1c63778104333ee`).
- **Stock Kindle Safety**: `com.amazon.kindle` (`versionName=8.160.1.0(2.0.124621.0)`) verified **100% UNTOUCHED** (`lastUpdateTime=2026-11-19 21:59:23`).
- **Crash & Constraint Audit (`logcat -d -b crash,main`)**: Zero `FATAL EXCEPTION`, zero `SIGSYS`, zero `SQLiteConstraintException`, zero `BadTokenException`, zero `no such column` warnings.
- **Play Store Folio (`ct.reader`)**: Zero launches, zero inspections, zero references.

---

## 2. Per-Task Status Table (Branch `round-164-qc-fixes`)
| Task | Status | Commit | Summary |
| :--- | :--- | :--- | :--- |
| **R163-Closeout** | **DONE** | `6037f2a`, `f086d62` | Fixed `cleanupLegacyDuplicateRows` to query `LocalContent.FILE_PATH` (not `KindleContent`), scoped chunked `IN (...)` deletes on `KindleContent`/`LocalContent`/`UserContent`; required `sdKindle.isDirectory() && sdKindle.canWrite()` in `ExternalEpubActivity`. |
| **C1** | **DONE** | `54dac5b` | Deferred Open Library enrichment onto a bounded 2-thread daemon `ExecutorService` (`MetadataEnricher.enqueueAsync`) deduplicated per `book_key`, persisting updates non-destructively via `FolioMetadataDb.upsertBook`. |
| **C2** | **DONE** | `5d23910` | Added pure Android-free `MetadataEnricher.isAcceptableMatch(...)` (normalized ISBN or title Jaccard `>= 0.75` + author overlap) and preserved non-empty OPF metadata fields. |
| **C3** | **DONE** | `fcc00fe` | Enforced `connectTimeout=8000`, `readTimeout=10000`, `256 KB` JSON response cap, `AzathReader/18.5 (Android; https://github.com/BerKetKet/clunky)` `User-Agent`, and `"online_enrichment_enabled"` toggle in `FolioMoreView`. |
| **C4** | **DONE** | `2699c1a` | Added `AtomicBoolean cancelled` + `Cancel Import` button in `FolioFolderBrowserDialog` checking cleanly between books with honest `Imported X of Y (cancelled)` status. |
| **D1** | **DONE** | `bcaaefc` | Moved `listFiles()`, stat/sort, and `FolioMetadataDb` imported-source queries off UI thread onto single-thread `scanExecutor` with `AtomicInteger scanGeneration` cancellation. |
| **D2** | **DONE** | `99e512b` | Added `FolioTheme.isContextDead(ctx)`, guarded `mainHandler.post` callbacks in `FolioFolderBrowserDialog` & `ExternalEpubActivity`, and shut down `scanExecutor` in `OnDismissListener`. |
| **D3** | **DONE** | `912bb23` | Replaced filename-only `Already Imported` check with `ImportedIndex` matching `(lowercaseFilename, sourceSize)` when `source_size > 0` (legacy fallback for `<= 0`). |
| **D4** | **DONE** | `d336031` | Replaced hardcoded hex color literals in `FolioFolderBrowserDialog` and `ExternalEpubActivity` with `FolioTheme` dynamic getters. |
| **D5** | **DONE** | `71c095f` | Added `content://`/`file://` scheme & `..` traversal checks, `250 MB` size cap, `cache/epub_staging/` cleanup (`finally` + `>1h` sweep in `reconcileLibrary`), `120s` `libkfxconv.so` timeout, and `Azath v18.5 KFX` version bump. |

---

## 3. Device Test Table (`Samsung Galaxy M30 — 32001776e8e82623`)
| Test | Result | Evidence |
| :--- | :--- | :--- |
| **T6** (Locked `kindle_library.db` & Backup) | **PASS** | With `/sdcard/kindle` replaced by a regular file (`isDirectory() == false`) and `BEGIN EXCLUSIVE` held on `kindle_library.db`: `FolioMetadataDb: Backed up kindle_library.db -> /data/user/0/com.azath.reader/files/db_backups/kindle_library_1795647531359.db` (`180,224` B) and `FolioMetadataDb: kindle_library.db locked/busy (attempt 1/3), retrying in 200ms: database is locked (code 5 SQLITE_BUSY)`. Restored `/sdcard/kindle` cleanly. |
| **T7** (Deferred Enrichment & Online Toggle) | **PASS** | With `Online Metadata Enrichment` toggled OFF: imported `ZzzNonexistentBookR164` instantly and `logcat` logged `MetadataEnricher: Online enrichment disabled by user preference; skipping network lookup for FOLIO_1ECC3C998994`. |
| **T8** (Match Validation) | **PARTIAL** | Imported `t8_mismatch.epub` (`The Great Gatsby`, `Completely Wrong Author XYZ`, `publisher="OPF Keep Press"`): OPF metadata (`OPF Keep Press`) was preserved untouched in `books_metadata`, though `Rejected mismatched Open Library candidate` was not observed in `logcat` because the `search.json?title=...&author=...` query with a bogus author returns 0 docs from Open Library directly. Pure `isAcceptableMatch(...)` compiled for Round 165 Task F6 JVM tests. |
| **T9** (Cancelable Batch Import) | **PASS** | Selected 3 EPUBs (`t9_1`, `t9_2`, `t9_3`), tapped `Import Selected` -> `Cancel Import`: dialog displayed `Imported 2 of 3 (cancelled)` (`Converted 2 of 3 EPUBs (cancelled)`), `T9 Batch Book 1` & `T9 Batch Book 2` intact in `/sdcard/kindle/` & `folio_metadata.db`, `T9 Batch Book 3` skipped, zero `.part` files, zero crashes. |
| **T10** (Off-UI-Thread Scan & Lifecycle Safety) | **PASS** | Opened `FolioFolderBrowserDialog` on `/sdcard/en/0` (700+ EPUBs) and dismissed mid-scan with Back: `T10_ZERO_CRASHES` (zero `BadTokenException`, zero `IllegalArgumentException`, zero `FATAL EXCEPTION`). |
| **T11** (`(filename, size)` & Dual-Stone Theme) | **PASS** | Placed `same_name_test.epub` in `t11_a` (`1187` B) and `t11_b` (`1987` B); imported `t11_a`: `t11_a` showed `1 KB · Already Imported` while `t11_b` showed `1 KB` (not flagged). Visually verified both **Granite** ([verify_r164_qc_folder_granite.png](file:///home/bgg/projects/clunky/verify_r164_qc_folder_granite.png)) and **Limestone** ([verify_r164_qc_folder_limestone.png](file:///home/bgg/projects/clunky/verify_r164_qc_folder_limestone.png)), restored Granite. |
| **T12** (`ExternalEpubActivity` Hardening & Clean `A2` Launch Log) | **PASS** | 1) Launched `http://example.com/bad.epub` and `file:///sdcard/Download/does_not_exist_r164.epub`: rejected cleanly with toast + zero crash. 2) `t7_test` `content://`/`file://` staging file deleted immediately in `finally`. 3) Fresh launch audit: `ZERO_CRASHES_ZERO_WARNINGS` (zero `no such column: FILE_PATH` warnings, `reconcileLibrary complete: activeKfx=30 changes=0`). |

---

## 4. Known Issues / Not Verified
1. **T8 live mismatch rejection log**: Because Open Library's `search.json` returns 0 docs when a bogus author is included in the query string, `Rejected mismatched Open Library candidate` was not triggered live on-device; `isAcceptableMatch(...)` will be directly unit-tested in Round 165 Task F6.
2. **D5 250 MB cap & 120s native timeout**: Implemented and compiled cleanly; not exercised with a 251 MB EPUB or a 120s hung binary on M30.

---

## 5. Open Questions for Project Lead
- Ready to proceed to **Round 165** (`dropin_src` sync, JVM unit tests for `planReconcile` & `isAcceptableMatch`, or Cover Architecture refactor) — please deliver Round 165 instructions in a `.zip` archive.

---

## 6. Mandatory Cover Architecture Directive (Project Lead)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
