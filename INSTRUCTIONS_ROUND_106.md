# Round 106 — Folio v10.6 KFX (Self-Contained Offline HTML Library Catalog Exporter)

## Task A: Drop-In Build & Deploy
1. Copy both drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataBackup.java`
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
2. Do **NOT** overwrite `MetadataEnricher.java` or `OpenCatalogView.java`.
3. Run `compile_mod_dex.sh`, `rebuild_fast.sh`, align/sign, install on Galaxy M32 (`32001776e8e82623`), and stage the release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v10.6_kfx/base.apk`.

## What's New in Folio v10.6 KFX
- **🌐 Self-Contained Offline HTML Catalog Exporter (`FolioMetadataBackup.java` + `FolioMoreView.java`)**:
  - Added a sky-blue **`🌐 HTML`** export pill right beside **`📊 CSV`** and **`✨ Scope: Curated Only`** inside `Reading Log (.md)`.
  - Generates `/sdcard/kindle/backups/folio_library.html`: a dark-themed, standalone offline HTML web catalog with your live Reading Goal progress bar, all cataloged books (with star ratings, shelves, finished dates, Up Next badges, and amber reading notes), and a built-in instant client-side search filter.
- Version badge and About Folio bumped to **Folio v10.6 KFX (Enhanced Typesetting)**.

## Task B: Minimal Hardware Verification (2 Screenshots)
1. `verify_r106_reading_log_html_export.png` — Open **📄 Reading Log & Export**, tap **`🌐 HTML`**, and capture the modal showing `Saved HTML: /sdcard/kindle/backups/folio_library.html` along with the 3-pill export toolbar (`✨ Scope: Curated Only`, `📊 CSV`, `🌐 HTML`).
2. `verify_r106_about_v106.png` — About Folio modal showing `Version: Folio v10.6 KFX (Enhanced Typesetting)` and live stats.
3. Return `antig_to_gemini_neon_clunky_report106.zip` with `REPORT.md` (including `folio_library.html` file size check) and both screenshots, plus stock `com.amazon.kindle` untouched check.
