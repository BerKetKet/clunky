# Round 098 — Folio v9.8 KFX (Persistent Finished Date Tracking & Export)

## Task A: Drop-In Build & Deploy
1. Copy all 3 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataBackup.java`
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
2. Do **NOT** overwrite `MetadataEnricher.java` or `OpenCatalogView.java`.
3. Run `compile_mod_dex.sh`, `rebuild_fast.sh`, align/sign, install on Galaxy M32 (`32001776e8e82623`), and stage the release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v9.8_kfx/base.apk`.

## What's New in Folio v9.8 KFX
- **📅 Persistent Finished Date Tracking (`FolioMetadataCatalogDialog.java`)**:
  - Automatically records today's `YYYY-MM-DD` date in `SharedPreferences("folio_finished_dates")` whenever any book is marked Finished (and clears it when reset to Unread).
  - In Book Details (`ⓘ`), Finished books display an interactive emerald status line (`✓ Finished · Set date ›` or `✓ Finished on 2026-11-23 · Edit ›`); tapping opens a dark **Finished Date** editor pre-filled with today's date.
- **Backup, Reading Log & CSV Export (`FolioMetadataBackup.java`)**:
  - Preserves `finished_date` in JSON Backup & Restore, appends `(YYYY-MM-DD)` next to `✓ Finished` in Markdown Reading Log, and adds a `Finished Date` column to RFC-4180 CSV export.
- Version badge and About Folio bumped to **Folio v9.8 KFX (Enhanced Typesetting)**.

## Task B: Minimal Hardware Verification (2 Screenshots)
1. `verify_r98_finished_date_details.png` — Open Book Details (`ⓘ`) for *Moby Dick; Or, The Whale* (already Finished), tap **`✓ Finished · Set date ›`**, save today's date (`2026-11-23`), then reopen Book Details to capture **`✓ Finished on 2026-11-23 · Edit ›`** clearly displayed in the header.
2. `verify_r98_about_v98.png` — About Folio modal showing `Version: Folio v9.8 KFX (Enhanced Typesetting)` and live stats.
3. Return `antig_to_gemini_neon_clunky_report098.zip` with `REPORT.md` and both screenshots, plus stock `com.amazon.kindle` untouched check.
