# Round 063 — Folio v6.3 KFX (Ultra-Lean Drop-In Build & Verify)

All Java code is pre-authored and strictly lex-verified in `dropin_src/`. Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working file.

## Task A — Copy Drop-In Files, Build & Deploy (~2 min)
1. Copy the 2 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
     * Automatically surfaces a dynamic **`📝 Notes (N)`** quick-filter chip beside `★ Favorites (N)` whenever 1+ books have saved personal notes.
     * Extends live catalog search (`matchesQuery`) to search inside each book's personal reading note text as well.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Bumps badge and About Folio to **Folio v6.3 KFX**.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v6.3_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only — Inside Folio)
1. `screenshots/verify_r63_notes_chip_live.png`: Open **All Metadata ›** and tap the new **`📝 Notes (1)`** chip, verifying `Library Metadata & Genres (1 of 27)` filtered strictly to annotated book `★ 📝 1984`.
2. `screenshots/verify_r63_search_inside_note.png`: Tap `All (27)` and type **`Part 3`** (a phrase only inside your personal note!) in the search box, verifying live full-text note search finds `★ 📝 1984` (`1 of 27`).

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report063.zip`.
