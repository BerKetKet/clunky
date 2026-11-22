# Round 095 — Milestone Folio v9.5 KFX ("More by Author" Sibling Carousel in Book Details)

## Task A: Drop-In Build & Deploy
1. Copy both drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
2. Do **NOT** overwrite `MetadataEnricher.java` or `OpenCatalogView.java`.
3. Run `compile_mod_dex.sh`, `rebuild_fast.sh`, align/sign, install on Galaxy M32 (`32001776e8e82623`), and stage the release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v9.5_kfx/base.apk`.

## What's New in Milestone Folio v9.5 KFX
- **"MORE BY [AUTHOR] (N)" Interactive Carousel in Book Details (`FolioMetadataCatalogDialog.java`)**:
  - Automatically discovers all other cataloged books by the same normalized author (e.g., opening *Brave New World* surfaces *Island*, *Crome Yellow*, and *Moksha*; opening *1984* surfaces *Animal farm: a fairy story*).
  - Renders a horizontal dark card strip right below the Personal Note card with standalone `.cover.jpg` thumbnails, 2-line titles, and live status badges (`✓ Done ›`, `X% read ›`, `📌 Up Next ›`, or year).
  - Tapping any sibling card switches directly into that book's Details (`ⓘ`) modal in 1 tap.
- Version badge and About Folio bumped to **Milestone Folio v9.5 KFX (Enhanced Typesetting)**.

## Task B: Minimal Hardware Verification (2 Screenshots)
1. `verify_r95_more_by_author_details.png` — Open Book Details (`ⓘ`) for *Brave New World* (Aldous Huxley — 3 siblings) or *1984* (George Orwell — 1 sibling), scroll slightly if needed to capture the new **`MORE BY ... (N)`** horizontal cover-art strip right below the Personal Note card.
2. `verify_r95_about_v95.png` — About Folio modal showing `Version: Folio v9.5 KFX (Enhanced Typesetting)` and live stats.
3. Return `antig_to_gemini_neon_clunky_report095.zip` with `REPORT.md` and both screenshots, plus stock `com.amazon.kindle` untouched check.
