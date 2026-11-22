# Round 053 — Folio v5.3 KFX (Ultra-Lean Drop-In Build & Verify)

All Java code is pre-authored and strictly lex-verified in `dropin_src/` (including the fixed Toast quote escaping from Round 052). Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working file.

> **QC Note from Round 052**: `verify_r52_book_details_mark_finished.png` passed cleanly, but `verify_r52_finished_chip_live.png` accidentally captured the Android launcher app drawer (`via`) instead of Folio. Please re-capture the live Folio `Finished (1)` chip screen in Task B below.

## Task A — Copy Drop-In Files, Build & Deploy (~2 min)
1. Copy the 2 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
     * Makes the amber author name inside Book Details (`ⓘ`) clickable (`Author Name ›`): tapping it dismisses details, resets genre to `All`, and filters the catalog to all books by that author (matching both `"First Last"` and `"Last, First"` DB records).
     * Adds an inline **`✕`** Clear button inside the catalog search bar whenever search text is non-empty.
     * Includes the fixed Toast quote escaping for `✓ Mark as Finished` / `↺ Mark as Unread`.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Bumps badge and About Folio to **Folio v5.3 KFX**.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v5.3_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only — Inside Folio)
1. `screenshots/verify_r53_finished_chip_live.png`: Inside Folio → **All Metadata ›**, tap the teal **Finished (1)** chip so *Moby Dick* (or any finished book) is visible with its green `✓ Finished` badge.
2. `screenshots/verify_r53_author_filter_and_clear.png`: Tap `ⓘ` on any multi-book author (e.g., *Crome Yellow* / *Aldous Huxley*) and tap the amber **`Aldous Huxley  ›`** author link, verifying the catalog filters to Aldous Huxley's books and displays the inline **`✕`** clear button in the search bar.

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report053.zip`.
