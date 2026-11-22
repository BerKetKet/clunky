# Round 058 — Folio v5.8 KFX (Ultra-Lean Drop-In Build & Verify)

All Java code is pre-authored and strictly lex-verified in `dropin_src/`. Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working file.

## Task A — Copy Drop-In Files, Build & Deploy (~2 min)
1. Copy the 2 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
     * Upgrades the Book Details (`ⓘ`) header with the book's crisp `64×96dp` Cover Thumbnail beside the Title, clickable amber `Author ›`, and live reading status (`✓ Finished` or `X% read`).
     * Makes long book descriptions tap-to-expand / collapse with a teal **`Read full synopsis ›`** / **`Show less`** toggle.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Bumps badge and About Folio to **Folio v5.8 KFX**.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v5.8_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only — Inside Folio)
1. `screenshots/verify_r58_book_details_cover_header.png`: Open **All Metadata ›** and tap `ⓘ` on *1984* (or another enriched book with a cover & synopsis), verifying the new cover thumbnail header, clickable genre pills, and teal **`Read full synopsis ›`** toggle.
2. `screenshots/verify_r58_synopsis_expanded.png`: Tap **`Read full synopsis ›`**, verifying the full description expands cleanly and the toggle switches to **`Show less`**.

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report058.zip`.
