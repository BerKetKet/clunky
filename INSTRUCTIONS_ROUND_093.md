# Round 093 — Folio v9.3 KFX (Publication Eras Timeline Chart & 1-Tap Era Filtering)

## Task A: Drop-In Build & Deploy
1. Copy both drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
2. Do **NOT** overwrite `MetadataEnricher.java` or `OpenCatalogView.java`.
3. Run `compile_mod_dex.sh`, `rebuild_fast.sh`, align/sign, install on Galaxy M32 (`32001776e8e82623`), and stage the release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v9.3_kfx/base.apk`.

## What's New in Folio v9.3 KFX
- **📅 Publication Eras Chart in Library Insights (`FolioMoreView.java`)**:
  - Parses valid publication years across all cataloged books, computes the live library span (`PUBLICATION ERAS · YYYY–YYYY (TAP TO FILTER)`), and plots proportional violet horizontal bars across chronological eras (`Pre-1900`, `1900–1949`, `1950–1999`, `2000–2009`, `2010–2019`, `2020+`).
  - Tapping any era bar dismisses Insights and jumps straight into the catalog filtered to that exact publication era.
- **Smart `ERA:` Filter Chip (`FolioMetadataCatalogDialog.java`)**:
  - Supports `ERA:<bucket>` deep-links with a dedicated active `📅 <Era> (N)` filter chip and exact year-range filtering.
- Version badge and About Folio bumped to **Folio v9.3 KFX (Enhanced Typesetting)**.

## Task B: Minimal Hardware Verification (2 Screenshots)
1. `verify_r93_insights_pub_eras.png` — Open `📊 Library Insights` and scroll down to capture the new **`PUBLICATION ERAS · YYYY–YYYY (TAP TO FILTER)`** violet bar chart.
2. `verify_r93_era_filter_drilldown.png` — Tap any era bar (e.g. `📅 Pre-1900` or `📅 2000–2009`) to verify the catalog opens filtered with the active **`📅 <Era> (N)`** chip (and confirm `Folio v9.3 KFX` badge/About in `REPORT.md`).
3. Return `antig_to_gemini_neon_clunky_report093.zip` with `REPORT.md` and both screenshots, plus stock `com.amazon.kindle` untouched check.
