# Round 103 — Folio v10.3 KFX (1-Tap Share Book Card & Unified Cover Placeholders)

## Task A: Drop-In Build & Deploy
1. Copy both drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
2. Do **NOT** overwrite `MetadataEnricher.java` or `OpenCatalogView.java`.
3. Run `compile_mod_dex.sh`, `rebuild_fast.sh`, align/sign, install on Galaxy M32 (`32001776e8e82623`), and stage the release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v10.3_kfx/base.apk`.

## What's New in Folio v10.3 KFX
- **1-Tap `↗ Share` Book Card Pill in Book Details (`FolioMetadataCatalogDialog.java`)**:
  - Added a sky-blue **`↗ Share`** pill right beside **`📋 Copy`** in the Book Details footer, launching Android's native `ACTION_SEND` share sheet with a formatted recommendation card (title, author, year, publisher, ISBN, reading/finished status + date, shelf, star rating, and personal note).
  - Hardened `parentDialog.dismiss()` with null checks so tapping `Read Now` works safely when Book Details is launched directly from Quick Library Search.
- **Unified `📖` Cover Placeholder Helper (`FolioMoreView.java`)**:
  - Extended the rounded `#0D1017` `📖` missing-cover placeholder frame to both **Continue Reading** and **Up Next Queue** carousels via `makeMiniCoverView(...)`.
- Version badge and About Folio bumped to **Folio v10.3 KFX (Enhanced Typesetting)**.

## Task B: Minimal Hardware Verification (2 Screenshots)
1. `verify_r103_details_share_pill.png` — Open Book Details (`ⓘ`) for *1984* or *Moby Dick* to capture the updated footer row showing `Remove from Folio Library` alongside the **`📋 Copy`** and sky-blue **`↗ Share`** pills.
2. `verify_r103_about_v103.png` — About Folio modal showing `Version: Folio v10.3 KFX (Enhanced Typesetting)` and live stats.
3. Return `antig_to_gemini_neon_clunky_report103.zip` with `REPORT.md` and both screenshots, plus stock `com.amazon.kindle` untouched check.
