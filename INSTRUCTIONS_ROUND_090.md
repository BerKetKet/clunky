# Round 090 — Milestone Folio v9.0 KFX (Persistent Up Next Reading Queue)

## Task A: Drop-In Build & Deploy
1. Copy both drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`
2. Do **NOT** overwrite `MetadataEnricher.java` or `OpenCatalogView.java`.
3. Run `compile_mod_dex.sh`, `rebuild_fast.sh`, align/sign, install on Galaxy M32 (`32001776e8e82623`), and stage the release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v9.0_kfx/base.apk`.

## What's New in Milestone Folio v9.0 KFX
- **📌 Up Next Reading Queue (`FolioMetadataCatalogDialog.java` + `FolioMoreView.java`)**:
  - Persistent FIFO queue backed by `SharedPreferences("folio_queue")` (storing insertion timestamps).
  - **Book Details (`ⓘ`)**: New violet **`📌 Up Next` / `📌 Queued`** toggle pill right beside the Custom Shelf pill.
  - **Catalog Filter Strip**: Dynamic violet **`📌 Up Next (N)`** chip whenever 1+ books are queued.
  - **Control Center `📌 Up Next Queue` Carousel**: Sits directly below `📖 Continue Reading` — shows a compact `+ Pick ›` prompt when empty, and expands into a horizontal cover-art queue carousel (`Start Reading ›` 1-tap reader launch + inline `✕` unqueue + `See All (N) ›`) as soon as books are queued!
- Version badge and About Folio bumped to **Milestone Folio v9.0 KFX (Enhanced Typesetting)**.

## Task B: Minimal Hardware Verification (2 Screenshots)
1. `verify_r90_book_details_queue_v90.png` — Open any unread book's Details (`ⓘ`) (e.g. *Animal Farm* or *The Republic*), tap **`📌 Up Next`** to queue it, then capture Book Details showing the active violet **`📌 Queued`** pill (or the catalog showing the violet `📌 Up Next (1)` chip).
2. `verify_r90_up_next_control_center.png` — Return to Control Center to capture the **Folio v9.0 KFX** badge, `📖 Continue Reading`, and the live **`📌 Up Next Queue`** carousel card with cover thumbnail and `Start Reading ›`!
3. Return `antig_to_gemini_neon_clunky_report090.zip` with `REPORT.md` and both screenshots, plus stock `com.amazon.kindle` untouched check.
