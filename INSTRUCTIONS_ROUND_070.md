# Round 070 — Milestone Folio v7.0 KFX (Ultra-Lean Drop-In Build & Verify)

All Java code is pre-authored and strictly lex-verified in `dropin_src/`. Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working file.

## Task A — Copy Drop-In Files, Build & Deploy (~2 min)
1. Copy the 3 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
     * Adds persistent **Custom Reading Shelves** (`SharedPreferences("folio_shelves")`, zero DB migration risk): inside Book Details (`ⓘ`), adds a **`Shelf: + Assign Shelf ›`** pill launching a dark shelf picker with 1-tap presets (`Classics`, `Philosophy`, `Sci-Fi`, `History`, `To Read`, `Reference`) plus custom shelf input.
     * Dynamically surfaces indigo **`🏷 <Shelf> (N)`** filter chips in the catalog strip, displays `🏷 <Shelf>` badges on catalog cards, and makes shelf names searchable via catalog search.
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataBackup.java`
     * Exports and restores `"shelf"` in portable JSON backup and renders `- **Shelf:** <shelf>` in the Markdown Reading Log.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Bumps badge and About Folio to **Milestone Folio v7.0 KFX (Enhanced Typesetting)**.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v7.0_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only — Inside Folio)
1. `screenshots/verify_r70_shelf_picker_assigned.png`: Open **All Metadata ›**, tap `ⓘ` on *1984*, tap **`+ Assign Shelf ›`**, select **`🏷 Classics`**, then re-open `ⓘ` on *1984* showing the indigo **`🏷 Classics ›`** shelf pill under `Your Rating:`.
2. `screenshots/verify_r70_shelf_chip_filter_live.png`: Close Book Details and tap the newly appearing indigo **`🏷 Classics (1)`** chip in the catalog strip, verifying `Library Metadata & Genres (1 of 27)` filtered to `★ 📝 1984` displaying its indigo `🏷 Classics` card badge.

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report070.zip`.
