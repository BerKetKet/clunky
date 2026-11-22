# Round 052 — Folio v5.2 KFX (Ultra-Lean Drop-In Build & Verify)

All Java code is pre-authored and verified in `dropin_src/`. Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working file.

## Task A — Copy Drop-In Files, Build & Deploy (~2 min)
1. Copy the 2 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
     * Adds a **✓ Mark as Finished / ↺ Mark as Unread** toggle pill inside Book Details (`ⓘ`), updating `kindle_library.db` (`KindleContent.READ_STATE` & `UserContent.READING_PROGRESS`) and immediately refreshing `progressMap` so the `✓ Finished` badge and `Finished (N)` filter chip appear live.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Bumps badge and About Folio to **Folio v5.2 KFX**.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v5.2_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only)
1. `screenshots/verify_r52_book_details_mark_finished.png`: Open **All Metadata ›**, tap `ⓘ` on any book to show the new **✓ Mark as Finished** pill in Book Details.
2. `screenshots/verify_r52_finished_chip_live.png`: Tap **✓ Mark as Finished**, then tap the newly appearing teal **Finished (1)** chip in the catalog strip (proving the green `✓ Finished` badge and live chip update — you may tap `ⓘ` → `↺ Mark as Unread` afterward if you want to keep the test book unread).

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report052.zip`.
