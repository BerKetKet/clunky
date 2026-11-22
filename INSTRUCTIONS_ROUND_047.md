# Round 047 Instructions — Live Reading Progress Badges, "Sort: Progress" & Metadata Display Polish (Folio v4.3 KFX, Ultra-Low Worker Effort)

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Hard Safety Invariant:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Division of Labor:** Gemini edited both drop-in Java files directly from your live `kindle_library.db` schema and codebase in `dropin_src/`. Just copy them in, compile, deploy, and capture 2 quick screenshots!  
**Reply Archive Budget:** Package ONLY Round 047 files (`REPORT.md`, `INSTRUCTIONS_ROUND_047.md`, `protocol.json`, `screenshots/*.png`) into `antig_to_gemini_neon_clunky_report047.zip` (< 5 MB).

---

## Round 046 Visual QC Feedback

- **PASS** on both `Folio v4.2 KFX` proofs (`verify_r46_home_all_metadata_link.png`, `verify_r46_book_details_remove_action.png`): teal `All Metadata ›` link and red `Remove from Folio Library` action look crisp and clean!
- **2 Subtle Display Catches Fixed in Round 047 Drop-In Code**:
  - `Animal Farm` Book Details showed `Language: UND` and `Genres: Fiction, Read` — now `UND` is hidden and `Read` is stripped cleanly from both cards and Book Details!

---

## What Gemini Already Built in `dropin_src/`

1. `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`:
   - Reads live reading progress (`UserContent.READING_PROGRESS` & `KindleContent.READ_STATE`) from `kindle_library.db` and renders crisp teal **`X% read`** / emerald **`✓ Finished`** badges on book cards.
   - Added **`Sort: Progress`** mode (highest reading progress first) to the Sort pill cycle (`Recent` → `Title` → `Author` → `Year` → `Progress`).
   - Added `cleanGenresForDisplay()` stripping noise tags (`Read`, etc.) from both card lines and Book Details, and hides `Language: UND`.
2. `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`:
   - Bumped Control Center & About Folio version badge to **`Folio v4.3 KFX`**.

---

## Task A: Copy, Compile & Deploy (`Folio v4.3 KFX`)

1. Copy both `.java` files from `dropin_src/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/` preserving `ingest/` and `catalog/`.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh` and install on Galaxy M32 (`32001776e8e82623`).
3. Stage signed APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v4.3_kfx/base.apk`.

---

## Task B: Fast 2-Screenshot Hardware Proof

1. `screenshots/verify_r47_sort_progress_badges.png`: Open **All Metadata ›** → cycle Sort pill to **`Sort: Progress`** showing active reads with teal **`X% read`** badges floated to the top.
2. `screenshots/verify_r47_clean_book_details.png`: Tap `ⓘ` on *Animal Farm* proving `Genres: Fiction` (no `"Read"`) and zero `Language: UND` clutter.
3. Confirm stock `com.amazon.kindle` remains 100% untouched (`lastUpdateTime=2026-11-19 21:59:23`).
