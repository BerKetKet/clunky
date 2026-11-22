# Round 050 — Milestone Folio v5.0 KFX (Ultra-Lean Drop-In Build & Verify)

All Java code is pre-authored and verified in `dropin_src/`. Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working Round 038–049 file.

## Task A — Copy Drop-In Files, Build & Deploy (~2 min)
1. Copy the 2 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
     * Deduplicates card & Book Details genres case-insensitively in clean Title Case (eliminating `fiction, Fiction, Fiction` and noisy `--` duplicates).
     * Filters bibliographic meta-tags (`early works`, `translations into`, `textbooks for`, etc.) from top Genre Chips so chips highlight real literary genres (`Fiction`, `History`, `Biography`, etc.).
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Bumps badge and About Folio to **Folio v5.0 KFX**.
     * Adds live **Reading Progress** summary (`X in progress · Y finished`) to `About Folio`.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v5.0_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only)
1. `screenshots/verify_r50_clean_genres_and_chips.png`: Open **All Metadata ›** from Home and scroll/search so *Crome Yellow* or *The Dream of the Red Chamber* is visible, verifying deduplicated Title-Cased genres and clean literary Genre Chips at the top.
2. `screenshots/verify_r50_about_v50_reading_stats.png`: Open **More → About Folio**, verifying **Folio v5.0 KFX** and the new live **Reading Progress: X in progress · Y finished** line alongside storage stats.

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report050.zip`.
