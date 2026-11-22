# Round 067 — Folio v6.7 KFX (Ultra-Lean Drop-In Build & Verify)

All Java code is pre-authored and strictly lex-verified in `dropin_src/`. Do **NOT** overwrite `MetadataEnricher.java` (preserve your Round 049 mobile HTTP fallback) or any other working file.

## Task A — Copy Drop-In Files, Build & Deploy (~2 min)
1. Copy the 2 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
     * Adds a dynamic **`Unread (N)`** backlog quick-filter chip beside `Finished (N)` to isolate all unstarted books (`progress == 0`).
     * Adds a **`📋 Copy Info`** pill right across from `Remove from Folio Library` inside Book Details (`ⓘ`), copying Title, Author, Year, Publisher, ISBN, Star Rating, and Personal Note cleanly to Android's clipboard.
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
     * Bumps badge and About Folio to **Folio v6.7 KFX**.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh`, sign, install onto Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v6.7_kfx/base.apk`.
3. Confirm stock `com.amazon.kindle` remains 100% untouched.

## Task B — Minimal Hardware Verification (2 Screenshots Only — Inside Folio)
1. `screenshots/verify_r67_unread_chip_live.png`: Open **All Metadata ›**, scroll the chip bar slightly right and tap the teal **`Unread (21)`** chip, verifying `Library Metadata & Genres (21 of 27)` filters strictly to unstarted books.
2. `screenshots/verify_r67_book_details_copy_info.png`: Tap `ⓘ` on *1984*, verifying the new **`📋 Copy Info`** pill sits cleanly across from `Remove from Folio Library` at the bottom of Book Details.

## Task C — Report Archive
Package `REPORT.md` + the 2 screenshots into `antig_to_gemini_neon_clunky_report067.zip`.
