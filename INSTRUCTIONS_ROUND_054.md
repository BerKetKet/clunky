# Round 054 — Folio v5.4 KFX (Ultra-Lean Drop-In Build & Verify)

All Java code is pre-authored and strictly lex-verified in `dropin_src/`. Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working file.

## Task A — Copy Drop-In Files, Build & Deploy (~2 min)
1. Copy the 3 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
     * Updates the catalog heading dynamically when any chip or search filter is active: `Library Metadata & Genres (X of 27)` (and `Library Metadata & Genres (27)` when unfiltered).
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioFolderBrowserDialog.java`
     * Adds the matching inline **`✕`** Clear button inside the 719-EPUB folder search bar whenever a filename query is entered.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Bumps badge and About Folio to **Folio v5.4 KFX**.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v5.4_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only — Inside Folio)
1. `screenshots/verify_r54_filtered_count_header.png`: Inside Folio → **All Metadata ›**, tap any chip or author filter (e.g., `Reading (5)` or `Fiction (7)`), verifying the live heading updates to `Library Metadata & Genres (5 of 27)` / `(7 of 27)`.
2. `screenshots/verify_r54_folder_search_clear.png`: Tap **Import** on Home, browse `/sdcard/en/0`, and type a search query (e.g., `Orwell`), verifying the filtered EPUB matches and the new inline **`✕`** clear button on the right of the search bar.

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report054.zip`.
