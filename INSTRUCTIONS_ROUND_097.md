# Round 097 — Folio v9.7 KFX (Curated-Only Reading Log Toggle & Top-Placed Up Next Queue)

## Task A: Drop-In Build & Deploy
1. Copy both drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataBackup.java`
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
2. Do **NOT** overwrite `MetadataEnricher.java` or `OpenCatalogView.java`.
3. Run `compile_mod_dex.sh`, `rebuild_fast.sh`, align/sign, install on Galaxy M32 (`32001776e8e82623`), and stage the release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v9.7_kfx/base.apk`.

## What's New in Folio v9.7 KFX
- **Curated-Only vs All Books Reading Log Toggle (`FolioMetadataBackup.java` + `FolioMoreView.java`)**:
  - New **`✨ Scope: Curated Only` / `📚 Scope: All Books`** toggle pill right beside `📊 Export CSV` in `Reading Log (.md)`.
  - Defaults to **Curated Only** (books that are Finished, Reading, Favorited, Rated, Annotated, Shelved, or Queued) so users get a clean, concise journal ready to copy or share — and can flip to **All Books** in 1 tap.
  - Promoted **`## 📌 Up Next Queue (N)`** to the very top of the Markdown log right under the Reading Goal summary so it's visible immediately without scrolling.
- Version badge and About Folio bumped to **Folio v9.7 KFX (Enhanced Typesetting)**.

## Task B: Minimal Hardware Verification (2 Screenshots)
1. `verify_r97_curated_reading_log.png` — Open **📄 Reading Log & Export** to capture the new **`✨ Scope: Curated Only`** toggle pill alongside `📊 Export CSV`, with `# Folio Curated Reading Log` and **`## 📌 Up Next Queue (1)`** immediately visible at the top of the preview.
2. `verify_r97_about_v97.png` — About Folio modal showing `Version: Folio v9.7 KFX (Enhanced Typesetting)` and live stats.
3. Return `antig_to_gemini_neon_clunky_report097.zip` with `REPORT.md` and both screenshots, plus stock `com.amazon.kindle` untouched check.
