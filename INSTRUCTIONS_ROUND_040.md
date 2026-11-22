# Round 040 Instructions — Missing-Cover Auto-Repair, Catalog Sort Controls & Editable Metadata (Folio v3.7 KFX)

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Hard Safety Invariant:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never modify, write, force-stop, or clear data).  
**Division of Labor:** Gemini authored both complete drop-in Java files in `dropin_src/com/amazon/kindle/epub/ingest/` (preserving 100% of your Round 039 SQLite query and Delphi/author normalization). Your job is strictly dropping them in, bumping the version badge, compiling, deploying via ADB, and verifying on hardware.  
**Reply Archive Budget:** Package ONLY Round 040 files (`REPORT.md`, `INSTRUCTIONS_ROUND_040.md`, `protocol.json`, `screenshots/*.png`, and concise logs) into `antig_to_gemini_neon_clunky_report040.zip` (< 15 MB) and verify a single `.zip` attachment chip before sending.

---

## Round 039 Visual QC Feedback

1. **PASS — 21-Book Full-Library Backfill, Live Filter & Book Details Popup (`verify_r39_metadata_catalog_synced.png`, `verify_r39_metadata_search_filtered.png`, `verify_r39_book_details_popup.png`)**: All 21 library books enriched cleanly via Open Library, Title-Cased genre chips look sharp, `"Huxley"` filtered instantaneously, and `Island` Book Details + `Read Now` worked cleanly.
2. **1 Visual QC Catch Fixed in Round 040 Drop-In Code**:
   - In `verify_r39_metadata_catalog_synced.png`, **`The 210th Day`** displayed a blank dark-gray cover box because its cover wasn't at `<filePath>.cover.jpg`.
   - Upgraded `LibraryMetadataBackfiller.java` now checks sibling cover variants AND downloads missing covers via Open Library's Covers API (`https://covers.openlibrary.org/b/isbn/<isbn>-M.jpg?default=false`) into `files/folio_covers/`, including a repair pass for already-indexed books missing covers!

---

## Task A: Drop-In Upgraded Java Sources & Build (`Folio v3.7 KFX`)

1. **Copy Drop-In Files**:
   - Overwrite `LibraryMetadataBackfiller.java` and `FolioMetadataCatalogDialog.java` inside `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/`.
2. **Bump Version Badge & Deploy**:
   - Update badge in `FolioMoreView.java` to **Folio v3.7 KFX**.
   - Recompile `classes9.dex` (`compile_mod_dex.sh`), rebuild, align, sign, and deploy to Galaxy M32.

---

## Task B: Hardware Verification on Galaxy M32

1. **Cover Auto-Repair (`Sync & Enrich`)**:
   - Open **More** → **Library Metadata & Genres** → tap **`Sync & Enrich`**.
   - Verify `The 210th Day` now displays a real cover thumbnail (zero blank gray boxes!).
2. **Sort Controls**:
   - Tap the **`Sort: Recent`** pill to cycle through `Title`, `Author`, and `Year` and verify the 21-book list re-orders cleanly.
3. **Edit Metadata Form**:
   - Tap `ⓘ` on any book card → tap **`Edit Metadata`** → verify the clean dark edit form opens with pre-populated fields (`Title`, `Author`, `Published Date / Year`, `Publisher`, `Genres`, `ISBN`) and **`Save`** persists updates cleanly to `folio_metadata.db`.

---

## Task C: Visual Verification Screenshots & Release Staging (`Folio v3.7 KFX`)

Capture and place all 3 proof screenshots inside `screenshots/` in `antig_to_gemini_neon_clunky_report040.zip`:

1. `screenshots/verify_r40_covers_fixed_and_sorted.png`: **Library Metadata & Genres (21)** after `Sync & Enrich` proving `The 210th Day` has a real cover thumbnail and the new **Sort** pill is active.
2. `screenshots/verify_r40_edit_metadata_dialog.png`: Dark **Edit Book Metadata** dialog launched from Book Details (`ⓘ`).
3. `screenshots/verify_r40_about_v37.png`: Control Center **About Folio** dialog showing `Folio v3.7 KFX`.
4. Confirm stock `com.amazon.kindle` remains 100% untouched (`lastUpdateTime=2026-11-19 21:59:23`) and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v3.7_kfx/base.apk`.
