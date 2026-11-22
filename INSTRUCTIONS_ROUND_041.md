# Round 041 Instructions — Batch EPUB Folder Importer & Portable Metadata JSON Backup (Folio v3.8 KFX)

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Hard Safety Invariant:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never modify, write, force-stop, or clear data).  
**Division of Labor:** Gemini authored both complete drop-in Java classes (`FolioFolderBrowserDialog.java` and `FolioMetadataBackup.java`) in `dropin_src/com/amazon/kindle/epub/ingest/`. Your job is strictly dropping them in, wiring one Control Center card + backup hook, compiling, deploying via ADB, and verifying on hardware.  
**Reply Archive Budget:** Package ONLY Round 041 files (`REPORT.md`, `INSTRUCTIONS_ROUND_041.md`, `protocol.json`, `screenshots/*.png`, and concise logs) into `antig_to_gemini_neon_clunky_report041.zip` (< 15 MB) and verify a single `.zip` attachment chip before sending.

---

## Round 040 Visual QC Feedback

1. **PASS — Missing-Cover Auto-Repair (`verify_r40_covers_fixed_and_sorted.png`)**: *The 210th Day* resolved its cover cleanly via Open Library → Archive.org 302 redirect, eliminating the blank box, and `Sort: Recent` pill looks crisp next to `Sync & Enrich`.
2. **PASS — Editable Metadata Form (`verify_r40_edit_metadata_dialog.png`)**: Clean dark form with pre-populated fields and working SQLite persistence.
3. **PASS — Milestone `Folio v3.7 KFX` (`verify_r40_about_v37.png`)**: Accurate version and live library/cache stats.

---

## Task A: Drop-In Sources & Wire Batch Folder Importer + Metadata Backup (`Folio v3.8 KFX`)

1. **Copy Drop-In Files**:
   - Add `FolioFolderBrowserDialog.java` and `FolioMetadataBackup.java` to `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/`.
   - Do **NOT** overwrite your existing working Round 038–040 files (`FolioMetadataDb.java`, `MetadataEnricher.java`, `EpubMetadataExtractor.java`, `LibraryMetadataBackfiller.java`, `FolioMetadataCatalogDialog.java`, `ExternalEpubActivity.java`).
2. **Wire in `FolioMoreView.java`**:
   - Add an **"Import EPUBs from Storage"** card (subtitle: `"Batch-select EPUB folders into app library"`) calling:
     ```java
     com.amazon.kindle.epub.ingest.FolioFolderBrowserDialog.show(context);
     ```
   - Inside your existing **Backup & Restore** action (or on backup completion), also invoke `FolioMetadataBackup.exportToJson(context)` / `FolioMetadataBackup.restoreFromJson(context)` so `folio_metadata.db` is backed up alongside reading progress to `/sdcard/kindle/backups/folio_metadata_backup.json`!
   - Bump version badge & About dialog string to **Folio v3.8 KFX**.
3. **Compile & Deploy**:
   - Recompile `classes9.dex` (`compile_mod_dex.sh`), rebuild, align, sign, and deploy to Galaxy M32.

---

## Task B: Hardware Verification on Galaxy M32

1. **Batch Folder Import Test**:
   - Open **More** → tap **"Import EPUBs from Storage"**.
   - Browse `/sdcard/en/0` (verify already-imported `Brave New World` and `Island` display the green `Already Imported` badge!).
   - Check 2 new EPUBs (e.g., `Aldous Huxley - Moksha.epub` and `Aldous Huxley - Point Counter Point.epub` or any 2 distinct EPUBs in `/sdcard/en/0/`) and tap **`Import Selected (2)`**.
   - Verify both convert via `libkfxconv.so` into `/data/data/com.amazon.kindle.epub/files/folio_library/`, enrich via Open Library, persist in `folio_metadata.db` + `kindle_library.db`, and display with real covers in Library & `Library Metadata & Genres`!
2. **Portable Metadata JSON Backup Test**:
   - Trigger **Backup & Restore** → verify `/sdcard/kindle/backups/folio_metadata_backup.json` is written with valid JSON containing all indexed books (log file size & book count in `REPORT.md`).

---

## Task C: Visual Verification Screenshots & Release Staging (`Folio v3.8 KFX`)

Capture and place all 3 proof screenshots inside `screenshots/` in `antig_to_gemini_neon_clunky_report041.zip`:

1. `screenshots/verify_r41_folder_browser_select.png`: **Import EPUBs from Storage** dialog browsing `/sdcard/en/0` showing green `Already Imported` badges and checked EPUBs ready to batch import.
2. `screenshots/verify_r41_batch_imported_catalog.png`: **Library Metadata & Genres** after batch import showing the newly imported books with covers, enriched pub dates, publishers, and genres.
3. `screenshots/verify_r41_control_center_v38.png`: Updated **Control Center** showing the **Import EPUBs from Storage** card and `Folio v3.8 KFX` badge.
4. Confirm stock `com.amazon.kindle` remains 100% untouched (`lastUpdateTime=2026-11-19 21:59:23`) and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v3.8_kfx/base.apk`.
