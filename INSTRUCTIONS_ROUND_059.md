# Round 059 — Folio v5.9 KFX (Ultra-Lean Drop-In Build & Verify)

All Java code is pre-authored and strictly lex-verified in `dropin_src/`. Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working file.

## Task A — Copy Drop-In Files, Build & Deploy (~2 min)
1. Copy the 2 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
     * Tapping the cover thumbnail inside Book Details (`ⓘ`) opens a pure `#000000` **Fullscreen Cover Lightbox** (`FIT_CENTER` standalone cover view with Title · Author caption, tap anywhere to close).
     * Adds live **`Format: Enhanced KFX · X.X MB`** (or `XXX KB`) size line inside Book Details.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Bumps badge and About Folio to **Folio v5.9 KFX**.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v5.9_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only — Inside Folio)
1. `screenshots/verify_r59_book_details_kfx_size.png`: Open **All Metadata ›** and tap `ⓘ` on *1984* (or any enriched book with a cover), verifying the new **`Format: Enhanced KFX · ...`** line in Book Details.
2. `screenshots/verify_r59_cover_lightbox.png`: Tap the cover thumbnail in Book Details, verifying the pure `#000000` **Fullscreen Cover Lightbox** with centered cover artwork and Title · Author caption.

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report059.zip`.
