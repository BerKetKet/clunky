# Folio v3.8 KFX Hardware Verification & Release Report (Round 041)

**Date:** 2026-11-22  
**Target Package:** `com.amazon.kindle.epub` (Folio v3.8 KFX)  
**Target Hardware:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 OneUI 5.1 arm64-v8a)  
**Stock Invariant:** `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 1. Executive Summary

Round 041 delivers the **Batch EPUB Folder Importer and Portable Metadata JSON Backup & Restore** for Folio v3.8 KFX:
1. **Batch EPUB Folder Importer (`FolioFolderBrowserDialog.java`)**: Dropped in Gemini's dark-themed multi-select folder importer. Added an **"Import EPUBs from Storage"** action card in Control Center (`FolioMoreView.java`). Users can browse any storage directory (`/sdcard/en/0`, `/sdcard/epub`, `/sdcard/Download`, `/sdcard/Books`), see green `· Already Imported` status badges for books already indexed in `FolioMetadataDb`, multi-select new EPUBs, and batch convert them directly into `/data/user/0/com.amazon.kindle.epub/files/folio_library/` via `libkfxconv.so`.
2. **Open Library Enrichment & Double Database Persistence**: During batch import, each book extracts metadata, queries the Open Library Search API for publication date, publisher, and genres, upserts into `folio_metadata.db` (`books_metadata`), and registers in native `kindle_library.db` (`KindleContent` & `LocalContent`).
3. **Portable Metadata JSON Backup & Restore (`FolioMetadataBackup.java`)**: Dropped in Gemini's JSON export/restore engine and integrated it into the existing Control Center **Backup & Restore** action (`OpenCatalogView.java`). Successfully exported all 24 indexed books into `/sdcard/kindle/backups/folio_metadata_backup.json` (19.5 KB) alongside SQLite database backups.
4. **Milestone `Folio v3.8 KFX`**: Updated Control Center badge and About dialog version strings to **`Folio v3.8 KFX`**.

---

## 2. Drop-In Integration & Technical Details (Task A)

- **Drop-In Files**:
  - `FolioFolderBrowserDialog.java` and `FolioMetadataBackup.java` placed into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/`.
  - Preserved all Round 038–040 files (`FolioMetadataDb.java`, `MetadataEnricher.java`, `EpubMetadataExtractor.java`, `LibraryMetadataBackfiller.java`, `FolioMetadataCatalogDialog.java`, `ExternalEpubActivity.java`).
- **Wiring in `FolioMoreView.java`**:
  - Added action card **"Import EPUBs from Storage"** with icon `📥` and subtitle `"Batch-select EPUB folders into app library"`, invoking `FolioFolderBrowserDialog.show(ctx)`.
  - Bumped badge to `"Folio v3.8 KFX"` and About dialog version string to `"Folio v3.8 KFX (Enhanced Typesetting)"`.
- **Wiring in `OpenCatalogView.java`**:
  - Updated `backupLocalDatabases(ctx)` to invoke `FolioMetadataBackup.exportToJson(ctx)`.
  - Updated `restoreLatestDatabases(ctx)` to invoke `FolioMetadataBackup.restoreFromJson(ctx)`.
- **Build & Packaging**:
  - Recompiled `classes9.dex` (187,340 bytes) via Android SDK `d8`.
  - Packed with Apktool, zipaligned, and signed with debug keystore.
  - Deployed to Samsung Galaxy M32 (`32001776e8e82623`).
  - Staged signed release APK to `07_APK_EPUB_INGESTION_MOD/release/folio_v3.8_kfx/base.apk` (131 MB).

---

## 3. Hardware Verification Results (Task B)

- **Batch Folder Importer Verification**:
  - Launched Folio v3.8 KFX on Galaxy M32 → navigated to Control Center → tapped **"Import EPUBs from Storage"**.
  - Folder browser opened to `/sdcard/en/0` with 719 detected EPUBs.
  - Verified `Aldous Huxley - Brave New World.epub` and `Aldous Huxley - Island.epub` clearly displayed the green `· Already Imported` status badges.
  - Checked 2 new Huxley EPUBs:
    1. `Aldous Huxley - Crome Yellow.epub` (243 KB)
    2. `Aldous Huxley - Moksha.epub` (532 KB)
  - Selected count updated dynamically on button: `Import Selected (2)`.
  - Tapped `Import Selected (2)`. Background worker converted both files into KFX:
    - `Crome_Yellow_FOLIO_8355CBC70E53.kfx` (614,872 bytes) + `Crome_Yellow_FOLIO_8355CBC70E53.kfx.cover.jpg` (29,407 bytes)
    - `Moksha_FOLIO_72C0BAE5934F.kfx` (1,137,465 bytes) + `Moksha_FOLIO_72C0BAE5934F.kfx.cover.jpg` (80,039 bytes)
  - Open Library enriched publication dates & publishers:
    - *Crome Yellow*: Publisher `"Random House"`, Pub Date `"2011-01-06"`, ISBN `"9781407020938"`
    - *Moksha*: Publisher `"Inner Traditions / Bear & Company"`, Pub Date `"2012-04-12"`, ISBN `"9781594775178"`
  - Both records persisted into `folio_metadata.db` (`books_metadata`) and registered in `kindle_library.db` (`KindleContent` & `LocalContent`).
  - On dialog refresh, both `Crome Yellow` and `Moksha` updated to display the green `· Already Imported` badge alongside `Brave New World` and `Island`.
  - Opened **Library Metadata & Genres**: Total book count incremented from 22 to 24 books. Under `Sort: Recent`, both *Moksha* and *Crome Yellow* displayed at the top with extracted cover images and metadata.
- **Portable Metadata JSON Backup Verification**:
  - Tapped **Backup & Restore** in Control Center → tapped **BACKUP NOW**.
  - Verified backup output created on external storage:
    - Path: `/sdcard/kindle/backups/folio_metadata_backup.json`
    - Size: `19,496 bytes` (~19.5 KB)
    - Content: Valid JSON with `version: 1`, `count: 24`, and an array of 24 full book records with titles, authors, publishers, dates, genres, and cover paths.
    - Accompanying SQLite dumps: `20261122_181316_kindle_library.db` (200 KB) and `20261122_181316_annotations.db` (36 KB).

---

## 4. Visual Verification Proofs (Task C)

Three physical hardware screenshots captured on Samsung Galaxy M32:
1. `screenshots/verify_r41_folder_browser_select.png` (187 KB): **Import EPUBs from Storage** dialog browsing `/sdcard/en/0` showing green `· Already Imported` badges on *Brave New World* and *Island*, checked boxes for *Crome Yellow* and *Moksha*, and active button `Import Selected (2)`.
2. `screenshots/verify_r41_batch_imported_catalog.png` (343 KB): **Library Metadata & Genres (24)** after batch import showing newly imported *Moksha* and *Crome Yellow* at the top of `Sort: Recent` with covers, publishers, and ISBNs.
3. `screenshots/verify_r41_control_center_v38.png` (219 KB): Updated **Control Center** showing the **Import EPUBs from Storage** card alongside **Library Metadata & Genres** and the **`Folio v3.8 KFX`** amber milestone badge.

---

## 5. Architectural Directive on Cover Handling

As instructed by the Project Lead, we pass the following architectural directive to the Manager AI:
> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*

---

## 6. Staging & Package Deliverables

- **Staged APK:** `07_APK_EPUB_INGESTION_MOD/release/folio_v3.8_kfx/base.apk` (131 MB)
- **Stock App State:** `com.amazon.kindle` `lastUpdateTime=2026-11-19 21:59:23` (100% UNTOUCHED)
- **Reply Archive:** `antig_to_gemini_neon_clunky_report041.zip` (< 15 MB)
