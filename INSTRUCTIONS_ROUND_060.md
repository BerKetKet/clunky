# Round 060 — Milestone Folio v6.0 KFX (Ultra-Lean Drop-In Build & Verify)

All Java code is pre-authored and strictly lex-verified in `dropin_src/`. Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working file.

## Task A — Copy Drop-In Files, Build & Deploy (~2 min)
1. Copy the 2 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
     * Adds persistent **☆ Favorite / ★ Favorited** toggle pill side-by-side with `✓ Mark as Finished` inside Book Details (`ⓘ`), stored cleanly in `SharedPreferences("folio_favorites")` (zero DB migration risk).
     * Surfaces a gold **`★ Favorites (N)`** quick-filter chip right beside `All (27)` whenever 1+ books are favorited, and marks favorited cards with a `★` prefix.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Bumps badge and About Folio to **Milestone Folio v6.0 KFX (Enhanced Typesetting)** and displays live `• Favorited Books: N` in About Folio.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v6.0_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only — Inside Folio)
1. `screenshots/verify_r60_book_details_favorite_pill.png`: Open **All Metadata ›** and tap `ⓘ` on *1984* (or another book), showing the side-by-side **`✓ Mark as Finished`** and **`☆ Favorite`** pills.
2. `screenshots/verify_r60_favorites_chip_live.png`: Tap **`☆ Favorite`**, then tap the newly appearing gold **`★ Favorites (1)`** chip in the catalog strip, verifying `Library Metadata & Genres (1 of 27)` filtered to the starred book (`★ 1984`).

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report060.zip`.
