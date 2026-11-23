# Round 107 — Folio v10.7 KFX (Subfolder Navigation & ⬆ Up Parent Button in Batch Importer)

## Task A: Drop-In Build & Deploy
1. Copy both drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioFolderBrowserDialog.java`
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
2. Do **NOT** overwrite `MetadataEnricher.java` or `OpenCatalogView.java`.
3. Run `compile_mod_dex.sh`, `rebuild_fast.sh`, align/sign, install on Galaxy M32 (`32001776e8e82623`), and stage the release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v10.7_kfx/base.apk`.

## What's New in Folio v10.7 KFX
- **⬆ Parent Folder Button & Clickable `📁` Subfolder Rows (`FolioFolderBrowserDialog.java`)**:
  - Added a compact **`⬆`** parent-directory button directly left of the path input in the Batch EPUB Importer, allowing 1-tap upward directory traversal (`/sdcard/en/0` -> `/sdcard/en` -> `/sdcard`).
  - Automatically lists non-hidden subdirectories at the top of the folder view as clickable warm-amber **`📁 <FolderName>  Open ›`** rows, letting users drill into nested EPUB folders without typing paths manually.
- Version badge and About Folio bumped to **Folio v10.7 KFX (Enhanced Typesetting)**.

## Task B: Minimal Hardware Verification (2 Screenshots)
1. `verify_r107_importer_subfolders.png` — Open **Import EPUBs from Storage**, tap **`⬆`** once (stepping up from `/sdcard/en/0` to `/sdcard/en`), and capture the Batch EPUB Importer showing the **`⬆`** button and the clickable **`📁 0  Open ›`** subfolder row (then tap `📁 0` to restore `/sdcard/en/0` as `last_import_dir`).
2. `verify_r107_about_v107.png` — About Folio modal showing `Version: Folio v10.7 KFX (Enhanced Typesetting)` and live stats.
3. Return `antig_to_gemini_neon_clunky_report107.zip` with `REPORT.md` and both screenshots, plus stock `com.amazon.kindle` untouched check.
