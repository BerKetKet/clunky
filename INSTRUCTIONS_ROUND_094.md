# Round 094 — Folio v9.4 KFX (Bulk 📌Queue Action & Grid-View Up Next Badge)

## Task A: Drop-In Build & Deploy
1. Copy both drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
2. Do **NOT** overwrite `MetadataEnricher.java` or `OpenCatalogView.java`.
3. Run `compile_mod_dex.sh`, `rebuild_fast.sh`, align/sign, install on Galaxy M32 (`32001776e8e82623`), and stage the release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v9.4_kfx/base.apk`.

## What's New in Folio v9.4 KFX
- **Bulk `📌Queue` Action Pill (`FolioMetadataCatalogDialog.java`)**:
  - Upgraded the bottom Multi-Select Bulk Action Bar to a sleek 5-pill single row (`Shelf`, `+Genre`, `📌Queue`, `★Fav`, `✓Done`), letting you batch-queue or batch-unqueue multiple books in 1 tap.
- **Grid-View Violet `📌 Up Next` Badge (`FolioMetadataCatalogDialog.java`)**:
  - Unread queued books in 3-column Cover Art Grid view now display a crisp violet **`📌 Up Next`** badge (e.g. *Animal farm: a fairy story*) instead of generic `Unread`.
- Version badge and About Folio bumped to **Folio v9.4 KFX (Enhanced Typesetting)**.

## Task B: Minimal Hardware Verification (2 Screenshots)
1. `verify_r94_bulk_queue_grid_badge.png` — Open Metadata Catalog in 3-column Grid view, tap **`☑ Select`** so both the violet **`📌 Up Next`** badge on *Animal farm: a fairy story* AND the new 5-pill Bulk Action Bar (`Shelf`, `+Genre`, `📌Queue`, `★Fav`, `✓Done`) are clearly visible in a single shot.
2. `verify_r94_about_v94.png` — About Folio modal showing `Version: Folio v9.4 KFX (Enhanced Typesetting)` and live stats.
3. Return `antig_to_gemini_neon_clunky_report094.zip` with `REPORT.md` and both screenshots, plus stock `com.amazon.kindle` untouched check.
