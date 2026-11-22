# Round 092 — Folio v9.2 KFX (Pinned Folder Bookmarks & Active Folder Highlight in Batch Importer)

## Task A: Drop-In Build & Deploy
1. Copy both drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioFolderBrowserDialog.java`
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
2. Do **NOT** overwrite `MetadataEnricher.java` or `OpenCatalogView.java`.
3. Run `compile_mod_dex.sh`, `rebuild_fast.sh`, align/sign, install on Galaxy M32 (`32001776e8e82623`), and stage the release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v9.2_kfx/base.apk`.

## What's New in Folio v9.2 KFX
- **★ Pinned Folder Bookmarks & Active Highlight (`FolioFolderBrowserDialog.java`)**:
  - Compact **`☆` / `★`** pin toggle button right beside `Open` in the Batch EPUB Importer path bar.
  - Persists user-pinned folder bookmarks in `SharedPreferences("folio_ui_prefs", "pinned_import_dirs")` and renders them first in the quick-jump chip strip with a gold `★` prefix (`★ en/0`).
  - Highlights whichever quick-jump chip matches the currently open folder with an active amber fill (`#F59E0B`).
- Version badge and About Folio bumped to **Folio v9.2 KFX (Enhanced Typesetting)**.

## Task B: Minimal Hardware Verification (2 Screenshots)
1. `verify_r92_pinned_folder_importer.png` — Open **Import EPUBs from Storage** (which reopens `/sdcard/en/0`), tap **`☆`** to pin it, and capture the Batch EPUB Importer showing the active gold **`★`** button and the highlighted **`★ en/0`** chip at the front of the quick-jump strip.
2. `verify_r92_about_v92.png` — About Folio modal showing `Version: Folio v9.2 KFX (Enhanced Typesetting)` and live stats.
3. Return `antig_to_gemini_neon_clunky_report092.zip` with `REPORT.md` and both screenshots, plus stock `com.amazon.kindle` untouched check.
