# Round 046 Instructions — Home "All Metadata ›" Link & Clean Library Book Removal (Folio v4.2 KFX, Ultra-Low Worker Effort)

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Hard Safety Invariant:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Division of Labor:** Gemini edited all 3 drop-in Java files directly from your codebase in `dropin_src/`. Just copy them in, compile, deploy, and capture 2 quick screenshots!  
**Reply Archive Budget:** Package ONLY Round 046 files (`REPORT.md`, `INSTRUCTIONS_ROUND_046.md`, `protocol.json`, `screenshots/*.png`) into `antig_to_gemini_neon_clunky_report046.zip` (< 5 MB).

---

## Round 045 Visual QC Feedback

- **PASS** on both `Folio v4.1 KFX` proofs (`verify_r45_home_import_pill.png`, `verify_r45_home_import_dialog.png`): Home top bar (`Folio` | `Import` | `OPDS` | `Backup`) looks razor-sharp and 1-tap `Import` opens the Batch EPUB Importer cleanly!

---

## What Gemini Already Built in `dropin_src/`

1. `dropin_src/com/amazon/kindle/epub/catalog/OpenCatalogView.java`:
   - Added a right-aligned teal **`All Metadata ›`** link beside the **`Continue Reading`** heading on Home for 1-tap access to `FolioMetadataCatalogDialog` right from Home.
2. `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`:
   - Added a clean **`Remove from Folio Library`** action inside the scrollable Book Details (`ⓘ`) dialog with confirmation prompt that removes the book from `folio_metadata.db`, `kindle_library.db`, and app-private `files/folio_library/` (strictly preserving original EPUBs on external storage!).
3. `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`:
   - Bumped Control Center & About Folio version badge to **`Folio v4.2 KFX`**.

---

## Task A: Copy, Compile & Deploy (`Folio v4.2 KFX`)

1. Copy all 3 `.java` files from `dropin_src/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/` preserving `catalog/` and `ingest/`.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh` and install on Galaxy M32 (`32001776e8e82623`).
3. Stage signed APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v4.2_kfx/base.apk`.

---

## Task B: Fast 2-Screenshot Hardware Proof

1. `screenshots/verify_r46_home_all_metadata_link.png`: Folio **Home** tab showing **`Continue Reading`** with the new teal **`All Metadata ›`** link cleanly aligned on the right.
2. `screenshots/verify_r46_book_details_remove_action.png`: Tapping **`All Metadata ›`** → `ⓘ` on any book showing the dark **Book Details** dialog with the red **`Remove from Folio Library`** action cleanly visible (no need to actually delete the user's books).
3. Confirm stock `com.amazon.kindle` remains 100% untouched (`lastUpdateTime=2026-11-19 21:59:23`).
