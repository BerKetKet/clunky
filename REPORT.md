# Round 167 Final Closeout Report — Azath v18.7 KFX (`master` / `v18.7-kfx`)

## 1. Hardware & Safety Invariants (Round 167 Final Closeout)
- **Target Hardware**: Physical **Samsung Galaxy M30** (`32001776e8e82623`), TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Git Merge, Revert & Tag**:
  - **F4 Undone (`classes4.dex` restored)**: `REVERT_F4_SHA=dde820c0068a37108ef0aebba6b9e403067363cc` (`Revert F4: restore classes4.dex tracking and skip provenance items per Project Lead`) on `round-166-qc-closeout` (`e5e9004..dde820c`), with `git ls-files 07_APK_EPUB_INGESTION_MOD/dex/classes4.dex` verified returning `07_APK_EPUB_INGESTION_MOD/dex/classes4.dex`.
  - **Clean `--no-ff` Merge into `master`**: `MERGE_SHA=9a0973c93b9737aa304e2b1a42d32646801d6316` (`Merge QC Remediation Rounds 163-166 (Azath v18.7 KFX) into master`), conflict-free, zero history rewrite, zero force-push.
  - **Annotated Tag**: `v18.7-kfx` (`TAG_SHA=270efcc7fbb2a92c7dfb3d09abed6880d84b95da`, `Azath v18.7 KFX — QC Remediation Closeout (Rounds 163-166)`).
  - **Unit Tests (`master`)**: `./scripts/run_unit_tests.sh` -> `AzathUnitTests: passed=84 failed=0 total=84`.
  - **M30 Verification (`32001776e8e82623`)**: `com.amazon.kindle` untouched (`versionName=8.160.1.0(2.0.124621.0)`, `lastUpdateTime=2026-11-19 21:59:23`); `com.azath.reader` (`versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-26 11:18:08`, `pkgFlags=[ HAS_CODE ALLOW_CLEAR_USER_DATA LARGE_HEAP ]`); `pm list packages | grep -i ct.reader` -> `NONE`; `adb logcat -d -b crash,main` -> `0` matches for `FATAL EXCEPTION|SIGSYS|SQLiteConstraintException|ct\.reader`.
- **Round 166 Origin Branch**: `round-166-qc-closeout` (branched cleanly from `round-165-qc-fixes` at `b0ff924`; pushed to `origin/round-166-qc-closeout` with zero history rewrite or force-push).
- **Active Package (`com.azath.reader`)**: `versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-26 11:18:08`, `pkgFlags=[ HAS_CODE ALLOW_CLEAR_USER_DATA LARGE_HEAP ]` (non-debuggable), UI badge **`v18.7 KFX`**.
- **Staged Release**: `07_APK_EPUB_INGESTION_MOD/release/azath_v18.7_kfx/base.apk` (`138,922,087` bytes, SHA256 `7d04b3afb5c67ed8cf5e5708f47ee95930f634741104ea12dadcdfbd3b9930d8`).
- **Compiled Mod DEX**: `07_APK_EPUB_INGESTION_MOD/dex/classes9.dex` (`562,824` bytes, built purely from `07_APK_EPUB_INGESTION_MOD/mod_src/real/`).
- **Stock Kindle Safety (`com.amazon.kindle`)**: `versionName=8.160.1.0(2.0.124621.0)`, **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).
- **Crash, Seccomp & Constraint Audit (`logcat -d -b crash,main`)**: Zero `FATAL EXCEPTION`, zero `SIGSYS`, zero `SQLiteConstraintException`, zero `FileUriExposedException`, zero `no such column` warnings on `v18.7 KFX` (`PID=4764`).
- **Play Store Folio (`ct.reader`)**: Zero launches, zero inspections, zero references.

---

## 2. Per-Task Status Table (Tasks `1`–`10`, Strictly Sequential)
| Task | Status | Commit | Summary & Honest Verification Basis |
| :--- | :--- | :--- | :--- |
| **Task 1** | **PARTIAL** | `61926ea` | `ExternalEpubActivity.runProcessWithTimeout` drains output on daemon thread before `waitFor`, adds `Future.get(250ms)` timeout around `EpubTranscoder.transcodeToKfx`, and makes progress dialog cancelable via Back/Cancel button cleaning `.part` & staging files. **Verified**: JVM `T1TimeoutTest` fake silent `sleep 300` timed out cleanly in `2052ms` (`rc=-1`, killed, cleanup ok) and cancelled in `403ms` (`rc=-2`); interactive M30 dialog Cancel & hung native `libkfxconv.so` not triggered on-device. |
| **Task 2** | **PASS** | `1d73c4c` | Blocked app-private `file://` URIs (`getDataDir`, `getFilesDir().getParentFile()`, `getCacheDir().getParentFile()`, `/data/data/<pkg>`, `/data/user/0/<pkg>`) with trailing separator boundary check; gated `/data/local/tmp/kfx-conv` behind `ApplicationInfo.FLAG_DEBUGGABLE` in `ExternalEpubActivity` and `EpubTranscoder`; removed `StrictMode.disableDeathOnFileUriExposure()` reflection from `ExternalEpubActivity`. **Verified on M30 `v18.7 KFX`**: blocked `file:///data/data/com.azath.reader/databases/kindle_library.db`, removed `StrictMode` reflection opened `StandAloneBookReaderActivity` cleanly (`PID=4764`, zero `FileUriExposedException`). |
| **Task 3** | **PASS** | `d6ca380` | Treated `!privDir.exists()` (`folio_library` absent) as healthy-empty via `AzathLibrarySync.isPrivateLibraryDirHealthy`; added `force` parameter to `planReconcile` & `reconcileLibrary`; added `Force Reconcile` preview/confirm UI in `FolioMoreView.java`; added Task 3 JVM unit tests. **Verified**: unit tests pass + M30 `v18.7 KFX` opened `Force Reconcile Preview` (`Active KFX on disk: 30`, `Missing DB rows to delete: 0`, `Orphan epub_cache files to delete: 0`); non-zero forced delete path verified by JVM tests only because live M30 library was clean (`0` orphan rows). |
| **Task 4** | **PASS** | `efc0c70` | Hardened `EpubMetadataExtractor`: regex ISO-only `normalizePubDate` with 4-digit year extraction (`1000..2099`), case-insensitive exact-entry `; ` author deduplication (`appendDistinctAuthor`), XML auto-encoding `setInput(..., null)` with UTF-8 fallback, `2 MB` OPF cap (`readEntryBounded`), and `4000`-char `description` cap. **Verified**: pure-JVM unit tests pass. |
| **Task 5** | **PASS** | `1b6ac64` | Enforced strict unknown/blank author matching (ISBN match OR `limit=3` agreeing normalized first author), updated Odyssey tests, added `ConnectivityManager` online check, `1 req/s` process-wide rate limiter, HTTP `429`/`503` `Retry-After`/backoff (max 2 retries), dynamic `User-Agent` from `PackageManager` `versionName`, private-dir post-enrichment `registerInKindleLibraryDb`, and launch-time enrichment sweep. **Verified**: JVM Odyssey tests pass + M30 live launch logged `Launch-time enrichment sweep queued 24 book(s)` and `Rejected mismatched Open Library candidates` (`count=2`, `count=3`); HTTP `429`/`503` retry not induced with a mock 429 server. |
| **Task 6** | **PARTIAL** | `ad3ddf8` | `FolioMetadataDb.backupKindleLibraryDbOnce` now uses `VACUUM INTO '<dest>.part'` (with atomic DB+`-wal`+`-shm` fallback), sets `sBackedUpThisProcess=true` only after verified non-empty backup, and aborts `registerInKindleLibraryDb` cleanly (returns `false`) on backup failure; `deleteBook` logs errors & returns `boolean` consumed by `reconcileLibrary`; `getCanonicalLibraryDir` uses `sdKindle.isDirectory()`. **Verified**: compiled into `classes9.dex`; not exercised on M30 because no private-dir registration ran during `v18.7` smoke (`/sdcard/kindle` writable). |
| **Task 7** | **PASS** | `7f837b4` | Added `>1h` `cache/folio_once` sweep in `reconcileLibrary`, guarded `dp.g.ingestKfxLive` in `ExternalEpubActivity` with `try/catch`, synchronized `processAndOpen` with `AzathLibrarySync.SYNC_LOCK`, verified M30 KFX `"CONT"` magic via `xxd`, and enforced `'C','O','N','T'` magic + `>= 128 B` in `ExternalEpubActivity.isValidKfxPart`. **Verified**: M30 `xxd` header dump + JVM `isValidKfxPart` tests + `classes9.dex`. |
| **Task 8** | **PASS** | `990dfe6` | Deep line-by-line security/stability audit documented in `QC_FINDINGS_ROUND_166.md`; fixed all P0/P1 findings (`EpubTranscoder` zip-slip `isSafeZipEntryName` + `10000` entries / `32 MB` entry / `150 MB` total caps + `inSampleSize`; `FolioWebBrowserDialog` `onReceivedSslError` cancel + `setAllowFileAccess(false)`/`setAllowContentAccess(false)` + 0 `addJavascriptInterface` + `sanitizeDownloadFilename`; `OpenCatalogView` `250 MB` OPDS cap; `FolioMetadataBackup` `16 MB` / path / key / goal / rating restore bounds). **Verified**: `AzathUnitTests` + `classes9.dex`. |
| **Task 9** | **PARTIAL** | `d2376f2` | Pulled M30 `kindle_library.db` and extracted verbatim `PRAGMA table_info` for `KindleContent`, `LocalContent`, and `UserContent`, confirming `cleanupLegacyDuplicateRows` matches M30 schema with zero `no such column` warnings (`PASS`). **Pre-upgrade `v1 -> v2` `31 -> 30` diff (`PARTIAL`)**: ephemeral Round 163 `folio_metadata_v1.db` is no longer present on host `/tmp` or M30, so the single deleted row cannot be reconstructed directly from a `v1` vs `v2` snapshot. |
| **Task 10** | **PASS** | `4369484` | Expanded `AzathUnitTests.java` (`84/84` passing), validated `FOLIO_[0-9A-F]{6,32}` hex suffix in `FolioMetadataDb.extractBookKeyFromFilename`, bumped `FolioMoreView.java` + `rebuild_fast.sh` to **`Azath v18.7 KFX`**, staged `azath_v18.7_kfx/base.apk`, deployed and verified on M30. |

---

## 3. Verification Evidence Table

| Check | Result | Verbatim Evidence |
| :--- | :--- | :--- |
| **Task 1 Fake Silent Binary Timeout Test (`T1TimeoutTest`, `sleep 300`, `2s` timeout + `400ms` cancel)** | **PARTIAL** (JVM host pass; M30 hung binary N/A) | `W/FolioExternalEpub: Process timed out after 2s; destroyed forcibly`<br>`T1_RESULT rc=-1 elapsedMs=2052 stdoutLen=0 pid=488127 procAliveAfterTimeout=false partExists=false partCoverExists=false stagedExists=false`<br>`T1_CANCEL rc=-2 elapsedCancelMs=403 pid=488138 alive=false` |
| **Task 2 ADB Private `file://` URI Guard Test (`Azath v18.7 KFX` on M30)** | **PASS** | Command: `adb -s 32001776e8e82623 shell am start -W -a android.intent.action.VIEW -d "file:///data/data/com.azath.reader/databases/kindle_library.db" -t "application/epub+zip" com.azath.reader/com.amazon.kindle.epub.ingest.ExternalEpubActivity`<br>Output: `Status: ok ... Activity: com.azath.reader/com.amazon.kindle.epub.ingest.ExternalEpubActivity`, `logcat`: `11-26 11:19:26.270  4764  4764 W FolioExternalEpub: Blocked file:// URI targeting private app storage: file:///data/data/com.azath.reader/databases/kindle_library.db`, zero DB copies in `cache/epub_staging/`, zero crash.<br>*(Note: testing instruction's un-namespaced component `com.azath.reader/com.amazon.kindle.epub.ExternalEpubActivity` returned `Error type 3: Activity class {com.azath.reader/com.amazon.kindle.epub.ExternalEpubActivity} does not exist.` because `AndroidManifest.xml:939` declares `.ingest.ExternalEpubActivity`.)* |
| **Task 2 `StrictMode` Removal Check (`Azath v18.7 KFX` on M30)** | **PASS** | Removed `StrictMode.disableDeathOnFileUriExposure()` from `ExternalEpubActivity.launchKfxReader`. Tapped `Blindsight` in Catalog -> opened cleanly in `mCurrentFocus=Window{e229267 u0 com.azath.reader/com.amazon.kcp.reader.StandAloneBookReaderActivity}`, Back returned cleanly to same process (`PID_AFTER_READER=4764`), zero `FileUriExposedException`, zero `FATAL EXCEPTION`. |
| **Task 7 M30 KFX Container Magic (`xxd -l 32`)** | **PASS** | `adb -s 32001776e8e82623 shell "xxd -l 32 /sdcard/kindle/Blindsight_FOLIO_DB72217519E6.kfx"`:<br>`00000000: 434f 4e54 0200 7e55 0000 2c55 0000 5000  CONT..~U..,U..P.`<br>`00000010: 0000 5c01 0000 4902 0000 0000 0000 0000  ..\...I.........`<br>Exact 4-byte magic `'C','O','N','T'` (`0x43 0x4F 0x4E 0x54`) enforced in `ExternalEpubActivity.isValidKfxPart`. *(Spot-check of 10 `.kfx` files in `/sdcard/kindle/` confirmed all start with `43 4f 4e 54 CONT`.)* |
| **Task 10 Pure-JVM Unit Tests (`./scripts/run_unit_tests.sh`)** | **PASS** | `AzathUnitTests: passed=84 failed=0 total=84` |
| **Task 10 M30 Smoke (`Azath v18.7 KFX`)** | **PASS** | Launch: `reconcileLibrary complete: activeKfx=30 changes=0`, `MetadataEnricher: Launch-time enrichment sweep queued 24 book(s)`, `Rejected mismatched Open Library candidates` (`count=2`/`3`), UI badges `['v18.7 KFX', 'Granite']` -> `['v18.7 KFX', 'Limestone']` -> `Granite`, `Force Reconcile Preview` (`Active KFX on disk: 30`, healthy dirs, `0` deletes), `PID_START=4764 == PID_AFTER_THEME=4764 == PID_AFTER_READER=4764`, stock `com.amazon.kindle` untouched (`2026-11-19 21:59:23`), zero crashes/SIGSYS/SQLite errors. |

### Task 9.1 Verbatim `PRAGMA table_info` Output (`/data/user/0/com.azath.reader/databases/kindle_library.db`)

**`PRAGMA table_info(KindleContent);`** (51 columns, no `FILE_PATH` column):
```text
cid                 name                  type    notnull    dflt_value    pk
---  ----------------------------------  -------  -------  --------------  --
  0  ID                                  TEXT           0                   1
  1  STATE                               TEXT           1                   0
  2  ERROR                               INTEGER        0                   0
  3  ARCHIVABLE                          INTEGER        1                   0
  4  HAS_READ_ALONG                      INTEGER        1                   0
  5  TYPE                                TEXT           1                   0
  6  KEPT                                INTEGER        1  0                0
  7  CONTENT_TYPE                        TEXT           0                   0
  8  TITLE                               TEXT           1                   0
  9  AUTHOR                              TEXT           1                   0
 10  PUBLICATION_DATE                    INTEGER        0                   0
 11  PUBLISHER                           TEXT           0                   0
 12  ERROR_MESSAGE                       TEXT           0                   0
 13  ERROR_TITLE                         TEXT           0                   0
 14  USER_COUNT                          INTEGER        0  0                0
 15  DOWNLOADING_FROM_REMOTE_TODO        INTEGER        0                   0
 16  IS_MOST_RECENT_ISSUE                INTEGER        0  0                0
 17  HAS_XRAY                            INTEGER        1  -1               0
 18  LANGUAGE                            TEXT           0                   0
 19  TITLE_PRONUNCIATION                 TEXT           0                   0
 20  AUTHOR_PRONUNCIATION                TEXT           0                   0
 21  HAS_MULTIMEDIA_CONTENT              INTEGER        0  0                0
 22  SORTABLE_TITLE                      TEXT           0                   0
 23  SORTABLE_AUTHOR                     TEXT           0                   0
 24  DICTIONARY_TYPE                     TEXT           1  'NONE'           0
 25  ERROR_LINK                          TEXT           0                   0
 26  ERROR_LINK_TITLE                    TEXT           0                   0
 27  ERROR_CODE_CDE                      TEXT           0                   0
 28  ERROR_DISPLAY_STATUS_CDE            INTEGER        0  0                0
 29  PARENT_ASIN                         TEXT           0                   0
 30  ORIGIN_TYPE                         TEXT           0                   0
 31  SHARE_ORIGIN_ID                     TEXT           0                   0
 32  DELIVERY_DATE                       INTEGER        0                   0
 33  TITLE_ORDER                         INTEGER        1  -1               0
 34  AUTHOR_ORDER                        INTEGER        1  -1               0
 35  AUTHOR_ORDER_REVERSED               INTEGER        1  -1               0
 36  EXTENDED_METADATA                   TEXT           0                   0
 37  IS_COMIC                            INTEGER        0  0                0
 38  READ_STATE                          TEXT           0  'UNREAD'         0
 39  READ_STATE_ORIGIN                   TEXT           0  'OTHER'          0
 40  IS_FALKOR_EPISODE                   INTEGER        0  0                0
 41  CONTENT_TAGS                        TEXT           0  'CONTENT_TAGS'   0
 42  TITLE_DETAILS_JSON                  TEXT           0                   0
 43  HAS_LOADED_COVER                    INTEGER        0  0                0
 44  IS_HIDDEN                           INTEGER        0  0                0
 45  IS_HIDDEN_BY_USER                   INTEGER        0  0                0
 46  IS_WEBCOMIC                         INTEGER        0  0                0
 47  ANNOTATIONS_MANIFEST_GUID           TEXT           0  ''               0
 48  ADS                                 TEXT           0                   0
 49  TIME_DURATION_RESTRICTION_END_DATE  INTEGER        0                   0
 50  SOURCE_CONTENT_TYPE                 TEXT           0                   0
```

**`PRAGMA table_info(LocalContent);`** (8 columns; `FILE_PATH` IS present at `cid=1`):
```text
cid       name        type    notnull  dflt_value  pk
---  --------------  -------  -------  ----------  --
  0  KEY             TEXT           0               1
  1  FILE_PATH       TEXT           1               0
  2  GUID            TEXT           0               0
  3  WATERMARK       TEXT           0               0
  4  ENCRYPTED       INTEGER        0               0
  5  LAST_MODIFIED   INTEGER        1               0
  6  OWNERSHIP_TYPE  TEXT           0               0
  7  FILE_SIZE       INTEGER        1  -1           0
```

**`PRAGMA table_info(UserContent);`** (14 columns, no `FILE_PATH` column):
```text
cid             name              type    notnull  dflt_value  pk
---  --------------------------  -------  -------  ----------  --
  0  KEY                         TEXT           0               2
  1  USER_ID                     TEXT           0               1
  2  LAST_ACCESSED               INTEGER        1               0
  3  LPR                         TEXT           0               0
  4  MRPR                        TEXT           0               0
  5  CRP                         TEXT           0               0
  6  WAYPOINTS                   TEXT           0               0
  7  FPR                         TEXT           0               0
  8  MLT_LASTUPDATED             INTEGER        0               0
  9  READING_PROGRESS            INTEGER        0               0
 10  IS_IN_CAROUSEL              INTEGER        0  -1           0
 11  CLIPPING_BALANCE            INTEGER        1  '0'          0
 12  HAS_UNMIGRATED_ANNOTATION   INTEGER        0  0            0
 13  CLIPPING_BALANCE_ENCRYPTED  BLOB           0               0
```
- **Schema Confirmation**: `FolioMetadataDb.cleanupLegacyDuplicateRows` queries `SELECT KEY FROM LocalContent WHERE KEY LIKE 'AMZNID0/CR!%KFX/0/' AND FILE_PATH LIKE '/sdcard/kindle/%'` (matching `LocalContent.FILE_PATH`) and deletes by `KindleContent.ID`, `LocalContent.KEY`, and `UserContent.KEY`, producing zero `no such column` warnings. (An earlier Round 163 `041924d:REPORT.md` note had mistakenly swapped `KindleContent` and `LocalContent` when describing the old bug from `1765170`.)

### Task 9.2 Round 163 T5 (`31 -> 30`) Primary Evidence Check
- **Honest Status**: **PARTIAL** — The ephemeral `folio_metadata_v1.db` snapshot pulled during Round 163 (`2026-11-26 00:23`, `041924d:REPORT.md`) is no longer present on the host (`/tmp`) or the M30 (`/sdcard/folio_metadata*.db` files are earlier Nov 22 snapshots).
- **Observed Current State on M30**: Current `/data/user/0/com.azath.reader/databases/folio_metadata.db` contains `30` rows (`id` `1..34`, with gaps at `id` `3, 5, 6, 7`; `id=34` added at `2026-11-25 19:27:15`). On disk under `/data/user/0/com.azath.reader/files/folio_library/`, `Xenophon - Delphi Complete Works.kfx.cover.jpg` and `.sdr` (`2026-11-25 19:28`) remain without a corresponding `.kfx` file. Without the preserved Round 163 `v1` DB file, which exact single row changed `31 -> 30` during the Round 163 upgrade/reconcile launch cannot be definitively proven from primary DB snapshots.

---

## 4. Known Issues / `PARTIAL` Items (With Exact Reasons)
1. **Task 1 (M30 interactive hung-binary UI cancel)**: Timeout kill & file cleanup were verified via `T1TimeoutTest` (`sleep 300`, `rc=-1`, `2052ms`, `rc=-2` at `403ms`), NOT by substituting a hung native binary in `/data/app/.../lib/arm64/` on M30.
2. **Task 6 (`VACUUM INTO` / backup-failure abort on M30)**: Compiled cleanly into `classes9.dex`, NOT triggered dynamically on M30 during the `v18.7` smoke check because `/sdcard/kindle` remained writable and no private-dir `registerInKindleLibraryDb` write was triggered (and existing `files/db_backups/kindle_library_1795647531359.db` was from Round 164).
3. **Task 9.2 (Round 163 T5 `31 -> 30` row identity)**: Marked **PARTIAL** because the pre-upgrade Round 163 `folio_metadata_v1.db` snapshot no longer exists on `/tmp` or on the M30.
4. **Task 5 (`429`/`503` backoff & `OpenCatalogView` `StrictMode` P2)**: HTTP 429/503 Retry-After sleep path was not exercised against a live mock server; `OpenCatalogView.java:1482` still contains the legacy `StrictMode` reflection + `registerDownloadedBook` fallback (documented as P2 in `QC_FINDINGS_ROUND_166.md`).

---

## 5. Resolved Escalations (Round 167 Final Closeout)
1. **Branch Merge & Round-Number Collision — RESOLVED**: Merged `round-166-qc-closeout` cleanly into `master` via `--no-ff` (`MERGE_SHA=9a0973c93b9737aa304e2b1a42d32646801d6316`) and created annotated tag `v18.7-kfx` (`TAG_SHA=270efcc7fbb2a92c7dfb3d09abed6880d84b95da`). Documented in `NOTES.md` that `master`'s `v18.2` (`7ab6643`) and `v18.3` (`253dfdc`) were pre-QC release commits preceding `QC Remediation Rounds 163–166`.
2. **`classes4.dex` History Purge & Repo Visibility — UNDONE / SKIPPED**: Per Project Lead directive (*"undo and skip anything to do with provenance the rest just decide what is best on your own"*), F4 (`3f54edb`) was reverted via `REVERT_F4_SHA=dde820c0068a37108ef0aebba6b9e403067363cc` (removing `07_APK_EPUB_INGESTION_MOD/dex/classes4.dex` from `.gitignore` and restoring tracking). History rewrite, repo visibility changes, and `README.md` provenance/Amazon-binary warnings were skipped/absent.
3. **`classes9.dex` Provenance Check — SKIPPED**: Skipped per Project Lead directive above; `dropin_*` and `kfx_converter` kept intact as shipped in `v18.7 KFX`.
4. **Open Library `User-Agent` Contact — RESOLVED**: Kept dynamic `AzathReader/<versionName> (Android; https://github.com/BerKetKet/clunky)` using the repository URL without a personal contact email.

---

## 6. Mandatory Cover Architecture Directive (Verbatim — Preserved & Active)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
