# Round 066 — Folio v6.6 KFX (Ultra-Lean Drop-In Build & Verify)

All Java code is pre-authored and strictly lex-verified in `dropin_src/`. Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working file.

## Task A — Copy Drop-In Files, Build & Deploy (~2 min)
1. Copy the 2 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
     * Adds **`Sort: Rating`** to the Sort pill cycle (`Recent → Title → Author → Year → Progress → Favorites → Rating`) sorting highest star ratings (`★★★★★`) first.
     * Adds a compact **`🎲`** ("Pick a random unread book") pill right beside `Sort:` in the catalog top bar — tapping it selects a random unread book from the currently filtered view and opens its rich Book Details card directly.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Bumps badge and About Folio to **Folio v6.6 KFX**.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v6.6_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only — Inside Folio)
1. `screenshots/verify_r66_sort_rating_and_dice_pill.png`: Open **All Metadata ›** and cycle the Sort pill to **`Sort: Rating`**, verifying the new **`🎲`** pill sits cleanly beside `Sort: Rating` and 5-star `★ 📝 1984` (`★★★★★ 5/5`) ranks #1 at the top.
2. `screenshots/verify_r66_random_book_picked.png`: Tap the **`🎲`** pill, verifying a random unread book's rich Book Details card opens immediately.

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report066.zip`.
