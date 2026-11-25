# Round 163 QC Remediation Report — Azath v18.4 KFX (`round-163-qc-fixes`)

## 1. Target Hardware & Safety Invariants
- **Device**: Physical Samsung Galaxy M30 (`32001776e8e82623`), TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Branch**: `round-163-qc-fixes` (branched from `master` at `253dfdc`).
- **Active Package**: `com.azath.reader` (`versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-26 00:23:46`).
- **Staged Release**: `07_APK_EPUB_INGESTION_MOD/release/azath_v18.4_kfx/base.apk` (SHA256 `a9e458b5d940dd278fd57cf72baccc08a13a36988c31b54525831f7743960b86`).
- **Stock Kindle Safety**: `com.amazon.kindle` (`versionName=8.160.1.0(2.0.124621.0)`) verified **100% UNTOUCHED** (`lastUpdateTime=2026-11-19 21:59:23`).
- **Crash & Constraint Audit (`logcat -d -b crash,main`)**: Zero `FATAL EXCEPTION`, zero `SIGSYS`, zero `SQLiteConstraintException`.
- **Play Store Folio (`ct.reader`)**: Zero launches, zero inspections, zero references.

---

## 2. Per-Task Status Table (Branch `round-163-qc-fixes`)
| Task | Status | Commit | Summary |
| :--- | :--- | :--- | :--- |
| **A1** | **DONE** | `d5405f5` | Added scan-health tracking (`isDirectory && canRead && listFiles != null`), `/sdcard/kindle` unhealthy-scan destructive skip, circuit breaker (`>10` rows OR `>50%` of DB), exact base-name `epub_cache` boundary matching (`isCacheEntryReferenced`), `INFO` deletion logs, and pure Android-free `planReconcile(...)` helper in `AzathLibrarySync.java`. |
| **A2** | **DONE** | `1765170` | Updated `FolioMetadataDb.cleanupLegacyDuplicateRows` to query & collect exact `ID`/`KEY` values of `/sdcard/kindle/%` rows in `KindleContent`/`LocalContent`, delete only those exact keys from `UserContent`, log SQLite warnings instead of swallowing, and return accurate removed count. |
| **A3** | **DONE** | `ad121e0` | Atomic `.kfx.part` + `.part.cover.jpg` write & `renameTo` in `ExternalEpubActivity.convertEpubToKfx` (with cleanup on failure/fallback failure), `ReentrantLock SYNC_LOCK` in `AzathLibrarySync`, 30s recent-file skip, and `>1h` stale `.part` cleanup on reconcile. |
| **A4** | **DONE** | `bb74643` | Moved `epubFile.delete()` in `AzathLibrarySync.ingestEpub` to run strictly after `db.upsertBook(...)` and required private-dir `registerInKindleLibraryDb(...)` succeed; preserves source EPUB and returns `IngestResult(ok=false, ...)` on failure. |
| **B1** | **DONE** | `478ad22` | Added streamed SHA-256 `computeContentHash` + `FOLIO_<12-hex>` `computeBookKeyFromHash`, restricted `findExisting` to `content_hash` or `book_key` ONLY, added advisory `findPossibleDuplicate(title, author)` (skipping blank/`Unknown Author`), and recovered `book_key` from `_FOLIO_<hex>.kfx` in `reconcileLibrary` backfill. |
| **B2** | **DONE** | `b5acb86` | Replaced `CONFLICT_REPLACE` in `FolioMetadataDb.upsertBook` with explicit transactional lookup by `book_key` / `kfx_path`, preserving `added_at` and non-empty metadata fields (`cover_path`, `genres`, `genres_json`, `publisher`, `pub_date`, `isbn`, `open_library_key`, `description`, `language`). |
| **B3** | **DONE** | `9cca2c1` | Bumped `FolioMetadataDb.DB_VERSION = 2` with idempotent `onUpgrade` (`content_hash`, `source_size`, `genres_json` + legacy CSV->JSON migration), updated `BookRecord.hasGenre` & `getDistinctGenres` to read `genres_json`, and removed the 12-genre filter cap in `FolioMetadataDb` and `FolioMetadataCatalogDialog`. |
| **B4** | **DONE** | `a635ca7` | Added `files/db_backups/kindle_library_<ts>.db` (keep last 3, skip if `<5m`), 3-attempt `200ms` retry on `SQLiteDatabaseLockedException`/busy, `fillMissingNotNullDefaults` schema-drift abort (`>3` forged columns), `registered`/`liveIndexed` on `IngestResult`, honest `ExternalEpubActivity` toast, `dp.g.ingestKfxLive` exception logging, and bumped `FolioMoreView` to `Azath v18.4 KFX`. |

---

## 3. Device Test Table (`Samsung Galaxy M30 — 32001776e8e82623`)
| Test | Result | Evidence |
| :--- | :--- | :--- |
| **T1** (Unreadable `/sdcard/kindle` Guard) | **PASS** | With storage permission revoked (`activeKfx=12`) and with `/sdcard/kindle` moved (`activeKfx=3`), `AzathLibrarySync` tripped the circuit breaker (`Circuit breaker tripped: would delete 26 of 30 DB rows; aborting all deletes`), keeping `folio_metadata.db` at `30` rows (`30 -> 30`) and `epub_cache` at `4.2M` (`4.2M -> 4.2M`). After restoring `/sdcard/kindle` & permissions: `reconcileLibrary complete: activeKfx=30 changes=0`. |
| **T2** (Private-Dir `UserContent` Persistence) | **PASS** | Inserted private-dir `UserContent` row `AMZNID0/CR!5445A3745815KFX/0/|42|1`, force-stopped and relaunched `com.azath.reader` (`reconcileLibrary` + `cleanupLegacyDuplicateRows` ran); verified row persisted intact: `AMZNID0/CR!5445A3745815KFX/0/|42|1`. |
| **T3** (Crash Mid-Conversion Atomic `.part`) | **PASS** | 1) Started converting `A Gentleman and a Thief - Dean Jobb.epub` via `ExternalEpubActivity`, ran `am force-stop com.azath.reader` mid-conversion: `NO_PARTIAL_KFX` and `0` rows in `books_metadata`. 2) Created backdated `>1h` `/sdcard/kindle/test_stale_r163.kfx.part` (`2026-11-25 18:00`), relaunched: `AzathLibrarySync: Deleted stale .part file: /sdcard/kindle/test_stale_r163.kfx.part`, `NO_PART_FILES_REMAIN`. |
| **T4** (Identity & `added_at` Preservation) | **PASS** | 1) Imported `t4_a.epub` (`T4 Identity Test`, `Unknown Author`): `36|FOLIO_5C24EE69F59E|1795642004105|5c24ee69f59e`. Reopened `t4_a.epub` -> dialog showed `ALREADY IN AZATH LIBRARY`; tapped `Re-Import & Replace` -> single row `id=36`, `added_at` preserved (`1795642004105`). 2) Imported distinct `t4_b.epub` (same title, `Unknown Author`) -> 2 separate rows (`FOLIO_5C24EE69F59E` + `FOLIO_777E85478D4B`). Cleaned test files afterward. |
| **T5** (`v1 -> v2` Migration) | **PASS** | Pulled pre-upgrade `folio_metadata_v1.db` (`user_version=1`, `COUNT(*)=31`). Upgraded on M30 launch (`folio_metadata_v2.db`): `user_version=2`, `COUNT(*)=30` (after 1 pre-existing stale path or reconcile; all active rows preserved), `PRAGMA table_info(books_metadata)` verified `15: content_hash TEXT`, `16: source_size INTEGER`, `17: genres_json TEXT`, with `8` rows populated with `genres_json`. |
| **T6** (Locked `kindle_library.db` Handling) | **NOT VERIFIED ON DEVICE** | `ExternalEpubActivity` checks `sdKindle.exists() && sdKindle.canWrite()` (not `isDirectory()`), so even with `/sdcard/kindle` replaced by a placeholder file during testing it did not fall back to private-dir `registerInKindleLibraryDb`. Backup (`files/db_backups/`) and 3-attempt lock retry logic are compiled in `FolioMetadataDb.java` but were not triggered at runtime on M30. |

---

## 4. Known Issues / Not Verified
1. **T6 runtime lock test**: Not verified at runtime because `ExternalEpubActivity` did not take the private-dir fallback path while `/sdcard/kindle` was writable.
2. **`LocalContent.FILE_PATH` in `cleanupLegacyDuplicateRows`**: Kindle v8.159 `LocalContent` does not have a `FILE_PATH` column (`KEY`/`CONTENT_ID` only), so `FolioMetadataDb.cleanupLegacyDuplicateRows` logs a `WARN` (`no such column: FILE_PATH`) on every `reconcileLibrary` pass while `KindleContent` query/delete succeeds.
3. **`T5` count note**: `folio_metadata_v1.db` had `31` rows before launch and `30` rows after first v18.4 launch (`onUpgrade` itself ran non-destructively; `reconcileLibrary` runs on launch).

---

## 5. Open Questions for Project Lead
1. Should `ExternalEpubActivity` check `sdKindle.isDirectory() && sdKindle.canWrite()` before choosing `/sdcard/kindle`, and should we add a debug/test trigger or accept T6 code-level verification?
2. Should `cleanupLegacyDuplicateRows` query `LocalContent` by `KEY IN (...)` collected from `KindleContent` instead of `WHERE FILE_PATH LIKE '/sdcard/kindle/%'` to eliminate the `no such column: FILE_PATH` `WARN` on every launch?

---

## 6. Mandatory Cover Architecture Directive (Project Lead)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
