# Round 165 QC Remediation Report — Azath v18.6 KFX (`round-165-qc-fixes`)

## 1. Target Hardware & Safety Invariants
- **Device**: Physical Samsung Galaxy M30 (`32001776e8e82623`), TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Branch**: `round-165-qc-fixes` (branched from `round-164-qc-fixes`, pushed to `origin/round-165-qc-fixes`).
- **Active Package**: `com.azath.reader` (`versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-26 02:21:52`).
- **Staged Release**: `07_APK_EPUB_INGESTION_MOD/release/azath_v18.6_kfx/base.apk` (SHA256 `beb6b8cf25a60c0b6db98bfd9c35c71b97e70664db98f3f5df3d3d725da7dd2c`).
- **Stock Kindle Safety**: `com.amazon.kindle` (`versionName=8.160.1.0(2.0.124621.0)`) verified **100% UNTOUCHED** (`lastUpdateTime=2026-11-19 21:59:23`).
- **Crash & Constraint Audit (`logcat -d -b crash,main`)**: Zero `FATAL EXCEPTION`, zero `SIGSYS`, zero `SQLiteConstraintException`, zero `no such column` warnings (`ZERO_CRASHES_ZERO_WARNINGS`).
- **Play Store Folio (`ct.reader`)**: Zero launches, zero inspections, zero references.

---

## 2. Per-Task Status Table (Branch `round-165-qc-fixes`)
| Task | Status | Commit | Summary |
| :--- | :--- | :--- | :--- |
| **E1** | **DONE** | `574707a` | Added `decodeXmlEntities` (`&amp;`, `&apos;`, `&quot;`, `&lt;`, `&gt;`, `&#...;`, `&#x...;`), case-insensitive `META-INF/container.xml` fallback, namespace-agnostic attribute scan, and `finally` `InputStream` close in `EpubMetadataExtractor.java`. |
| **E2** | **DONE** | `fc357a7` | Added pure `EpubMetadataExtractor.normalizeIsbn(String)` with ISBN-13 (`978`/`979`) & ISBN-10 (`X`) checksum validation, UUID/Calibre/URL rejection, and ISBN-13 preference over ISBN-10. |
| **E3** | **DONE** | `62657af` | Added pure `EpubMetadataExtractor.normalizeAuthor(String)` (handles `Last, First`, `Jr.`/`Sr.`/`III`, `;` multi-author), ISO `normalizePubDate`, and case-insensitive genre deduplication before `genres_json`/CSV writes. |
| **E4** | **DONE** | `531340c` | Added pure `AzathLibrarySync.sanitizeFilenameBase(title, bookKey)` stripping separators/NUL/hostile chars, capping length `<= 80`, and falling back to `Book_<bookKey>`. |
| **E5** | **DONE** | `9140cfa` | Made `FolioTheme` font caches `volatile`, used `applicationContext` for static `AssetManager` reads in `FolioTheme` & `FolioIcons`, and closed icon `InputStream` in `finally`. |
| **E6** | **DONE** | `267164a` | Switched `FolioMetadataBackup` JSON/Markdown/CSV/HTML exports to atomic `.tmp -> renameTo`, fixed RFC-4180 CSV escaping, and validated `books` array & entries before restore. |
| **F1** | **DONE** | `1a03839` | Removed 13 strictly redundant files in `dropin_src/` (`git rm -r dropin_src`); preserved `dropin_rust/`, `dropin_res/`, and `dropin_assets/` per Escalation Guard. |
| **F2** | **DONE** | `8f48a76` | Rewrote root `README.md` for Azath (`com.azath.reader`), Dual Stone Edition, `mod_src/real/`, `rebuild_fast.sh`, `scripts/run_unit_tests.sh`, and hardware safety invariants. |
| **F3** | **DONE** | `92bd2cb` | Audited `kfx_converter` (`160000 commit 61f664c4207ba2eef9d58037f5f4c92dba52230e`, no `.gitmodules`) and documented its dangling gitlink state in `README.md`. |
| **F4** | **DONE** | `3f54edb` | Untracked `07_APK_EPUB_INGESTION_MOD/dex/classes4.dex` (`git rm --cached`) without history rewrite and added it to `.gitignore`. |
| **F5** | **DONE** | `dc40c8b` | Moved 158 tracked/untracked root `verify_*.png` & `ref_*.png` screenshots into `screenshots/`. |
| **F6** | **DONE** | `bc232b9` | Added `07_APK_EPUB_INGESTION_MOD/tests/AzathUnitTests.java` and `scripts/run_unit_tests.sh` covering `planReconcile`, `isCacheEntryReferenced`, `isAcceptableMatch` (R164 T8 closure), `normalizeIsbn`, `normalizeAuthor`, `normalizePubDate`, and `sanitizeFilenameBase`. |
| **G** | **DONE** | `ba42c77` | Audited all 7 unreviewed files in `QC_FINDINGS_ROUND_165.md`, fixed P0 (`FolioWebBrowserDialog` inline ingest bypassing `AzathLibrarySync.ingestEpub`) & P1 (unclosed `in`/`fos`/`conn` + `250 MB` cap), and bumped `Azath v18.6 KFX`. |

---

## 3. JVM Unit Test & M30 Smoke Test Table
| Check | Result | Evidence |
| :--- | :--- | :--- |
| **`./scripts/run_unit_tests.sh`** | **PASS** | `AzathUnitTests: passed=38 failed=0 total=38` (pure JVM `javac -source 8 -target 8` + `java`, zero network dependencies). |
| **M30 Launch & Reconcile** | **PASS** | `PID_START=22144`, `reconcileLibrary complete: activeKfx=30 changes=27`, zero `no such column` warnings. |
| **M30 Reader Open & Return** | **PASS** | `Blindsight_FOLIO_DB72217519E6.kfx` opened into `StandAloneBookReaderActivity` (`mCurrentFocus=Window{ecf321 ...}`) and returned cleanly via Back. |
| **M30 Control Center & Hot-Swap** | **PASS** | UI dump showed `['v18.6 KFX', 'Granite']`; Granite <-> Limestone hot-swap kept `PID_END=22144` (`== PID_START`) with zero `SIGSYS`. |
| **M30 Atomic Backup & Crash Audit** | **PASS** | `/sdcard/kindle/backups/folio_metadata_backup.json` (`19,569` B), zero `.tmp` files, `ZERO_CRASHES_ZERO_WARNINGS`, stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`). |

---

## 4. Known Issues / Not Verified
- **P2/P3 recommendations** in `QC_FINDINGS_ROUND_165.md` (`FolioWebBrowserDialog` WebView `setAllowFileAccess(false)`, `CoverOverlayView` `inSampleSize` cap, `FolioMoreView.readQuickStats` off-UI-thread, `OpenCatalogView` `250 MB` download cap) were intentionally left unedited per Round 165 scope (`Fix ONLY clear P0/P1 findings`).

---

## 5. Open Questions & Escalations for Project Lead
1. **`classes4.dex` Git History Purge**: `07_APK_EPUB_INGESTION_MOD/dex/classes4.dex` is untracked (`F4`), but remains in prior git history. Approve a history rewrite (`git filter-repo` + force-push) or leave prior history intact?
2. **Non-Redundant `dropin_*` Directories**: `dropin_rust/`, `dropin_res/`, and `dropin_assets/` remain at the repo root (not duplicated under `mod_src/real/`). Should they be archived, moved into `07_APK_EPUB_INGESTION_MOD/`, or deleted?
3. **`kfx_converter` Dangling Gitlink**: `git ls-tree HEAD kfx_converter` points to commit `61f664c4207ba2eef9d58037f5f4c92dba52230e` with no `.gitmodules` or remote URL. Should this gitlink be removed or pointed to a specific repository URL?
4. **P2/P3 Audit Follow-Ups**: Approve scheduling the P2/P3 items in `QC_FINDINGS_ROUND_165.md` for a future polish round?

---

## 6. Mandatory Cover Architecture Directive (Project Lead)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
