# Round 105 — Milestone Folio v10.5 KFX (Exact Star-Rating Drill-Down & Rated Search Scope)

## Task A: Drop-In Build & Deploy
1. Copy both drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
2. Do **NOT** overwrite `MetadataEnricher.java` or `OpenCatalogView.java`.
3. Run `compile_mod_dex.sh`, `rebuild_fast.sh`, align/sign, install on Galaxy M32 (`32001776e8e82623`), and stage the release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v10.5_kfx/base.apk`.

## What's New in Milestone Folio v10.5 KFX
- **Exact `STAR:1..5` Rating Filter Drill-Down (`FolioMetadataCatalogDialog.java` + `FolioMoreView.java`)**:
  - Tapping any star bar in `📊 Library Insights` (e.g. `★★★★★ (5★)`) now dismisses Insights and launches the Metadata Catalog filtered strictly to that exact star rating (`STAR:5`), displaying a dedicated active gold **`★ 5-Star (1)`** chip.
- **`★ Rated` Scope Pill in Quick Library Search (`FolioMoreView.java`)**:
  - Added a 6th instant scope pill (**`★ Rated`**) to **🔍 Quick Library Search** (`All`, `📖 Reading`, `★ Rated`, `📝 Notes`, `📌 Queue`, `🏷 Shelves`).
- Version badge and About Folio bumped to **Milestone Folio v10.5 KFX (Enhanced Typesetting)**.

## Task B: Minimal Hardware Verification (2 Screenshots)
1. `verify_r105_star_filter_drilldown.png` — Open `📊 Library Insights`, scroll down and tap the **`★★★★★ (5★)`** bar to capture the Metadata Catalog opened directly to the active gold **`★ 5-Star (1)`** filter chip showing *1984*.
2. `verify_r105_about_v105.png` — About Folio modal showing `Version: Folio v10.5 KFX (Enhanced Typesetting)` and live stats.
3. Return `antig_to_gemini_neon_clunky_report105.zip` with `REPORT.md` and both screenshots, plus stock `com.amazon.kindle` untouched check.
