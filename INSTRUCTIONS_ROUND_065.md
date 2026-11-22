# Round 065 — Folio v6.5 KFX (Ultra-Lean Drop-In Build & Verify)

All Java code is pre-authored and strictly lex-verified in `dropin_src/`. Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working file.

## Task A — Copy Drop-In Files, Build & Deploy (~2 min)
1. Copy the 3 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
     * Adds an interactive **Your Rating: ★ ★ ★ ★ ★** 5-star bar inside Book Details (`ⓘ`), stored cleanly in `SharedPreferences("folio_ratings")` (tapping a star updates in-place immediately; tapping the active star clears to 0) and renders compact gold stars (`★★★★★  5/5`) on rated catalog cards.
     * Persists the user's selected Sort Mode (`SharedPreferences("folio_ui_prefs")` → `catalog_sort_idx`) across dialog re-opens.
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataBackup.java`
     * Exports and restores `star_rating` alongside metadata, favorites, and personal notes.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Bumps badge and About Folio to **Folio v6.5 KFX**.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v6.5_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only — Inside Folio)
1. `screenshots/verify_r65_book_details_5star_rating.png`: Open **All Metadata ›**, tap `ⓘ` on *1984*, and tap the 5th star on **`Your Rating:`**, verifying all 5 gold stars (`★★★★★`) illuminate live inside Book Details.
2. `screenshots/verify_r65_card_star_rating_badge.png`: Close Book Details to show *1984*'s catalog card displaying `★★★★★  5/5` above its italic amber note snippet.

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report065.zip`.
