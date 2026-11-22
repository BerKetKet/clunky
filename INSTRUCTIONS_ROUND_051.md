# Round 051 — Folio v5.1 KFX (Ultra-Lean Drop-In Build & Verify)

All Java code is pre-authored and verified in `dropin_src/`. Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working file.

## Task A — Copy Drop-In Files, Build & Deploy (~2 min)
1. Copy the 2 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Fixes `KindleContent.READ_STATE` SQL check (TEXT `'READ'` / `'UNREAD'` instead of integer `> 0`) so `About Folio` reports accurate live `Reading Progress: X in progress · Y finished`.
     * Bumps badge and About Folio to **Folio v5.1 KFX**.
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
     * Adds dynamic teal **Reading (N)** (and **Finished (N)** when present) quick-filter chip right beside `All (27)` in the Metadata Catalog strip.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v5.1_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only)
1. `screenshots/verify_r51_reading_chip_filter.png`: Open **All Metadata ›** from Home and tap the new teal **Reading (N)** chip (showing only in-progress books with their `X% read` badges).
2. `screenshots/verify_r51_about_v51_accurate_stats.png`: Open **More → About Folio**, confirming **Folio v5.1 KFX** and the corrected live **Reading Progress: X in progress · Y finished** count.

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report051.zip`.
