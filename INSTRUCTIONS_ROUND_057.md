# Round 057 — Folio v5.7 KFX (Ultra-Lean Drop-In Build & Verify)

All Java code is pre-authored and strictly lex-verified in `dropin_src/`. Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working file.

## Task A — Copy Drop-In Files, Build & Deploy (~2 min)
1. Copy the 2 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
     * Replaces the plain-text `Genres:` line inside Book Details (`ⓘ`) with a scrollable row of clickable blue genre pills (`#60A5FA` on `#1E2433`): tapping any genre pill dismisses details and immediately filters the catalog to that genre.
     * Adds `show(Context ctx, String initialFilter, Runnable onDismiss)` so Control Center stat tiles auto-refresh whenever the catalog closes.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Wires `refreshStats` onto all 3 Control Center stat tiles (`BOOKS` · `READING` · `FINISHED`) and the Metadata card so counts stay live after any Mark Finished/Unread or Remove action.
     * Makes the header **`Folio v5.7 KFX`** badge directly tappable to open **About Folio**.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v5.7_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only — Inside Folio)
1. `screenshots/verify_r57_book_details_genre_pills.png`: Open **All Metadata ›** and tap `ⓘ` on any enriched book (e.g., *Brave New World*, *Crome Yellow*, or *Moby Dick*), verifying the new scrollable row of clickable blue genre pills inside Book Details.
2. `screenshots/verify_r57_genre_pill_filter_live.png`: Tap one of those genre pills (e.g., `Utopias` or `Fiction`), verifying Book Details dismisses and the catalog immediately filters to matching books with `(X of 27)` in the header and the inline `✕` clear button active.

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report057.zip`.
