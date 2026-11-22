# Round 045 Instructions — Unified Home Import Pill & App-Private Download Pipeline (Folio v4.1 KFX, Ultra-Low Worker Effort)

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Hard Safety Invariant:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Division of Labor:** Gemini edited both drop-in Java files directly from your Round 043/044 codebase in `dropin_src/`. Just copy them in, compile, deploy, and capture 2 quick screenshots!  
**Reply Archive Budget:** Package ONLY Round 045 files (`REPORT.md`, `INSTRUCTIONS_ROUND_045.md`, `protocol.json`, `screenshots/*.png`) into `antig_to_gemini_neon_clunky_report045.zip` (< 5 MB).

---

## Round 044 Visual QC Feedback

- **PASS** on both Milestone `Folio v4.0 KFX` screenshots (`verify_r44_clean_dates_and_genres.png`, `verify_r44_about_v40_storage_and_clear_cache.png`): clean `2000-07-15` date, noise-free genre chips, live App Library/Temp Cache storage breakdown, and `CLEAR TEMP CACHE` button!

---

## What Gemini Already Built in `dropin_src/`

1. `dropin_src/com/amazon/kindle/epub/catalog/OpenCatalogView.java`:
   - Added a crisp emerald **`Import`** pill in the Home screen top bar (`Folio` | `Import` | `OPDS` | `Backup`) for 1-tap access to the Batch EPUB Importer right from Home.
   - Unified Home/OPDS downloads to store converted `.kfx` + `.cover.jpg` inside Folio's app-private `files/folio_library/`, automatically enrich metadata into `folio_metadata.db`, and register as `BT_EBOOK_PDOC` in `kindle_library.db`.
2. `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`:
   - Bumped Control Center & About Folio version badge to **`Folio v4.1 KFX`**.

---

## Task A: Copy, Compile & Deploy (`Folio v4.1 KFX`)

1. Copy both `.java` files from `dropin_src/com/amazon/kindle/epub/catalog/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/`.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh` and install on Galaxy M32 (`32001776e8e82623`).
3. Stage signed APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v4.1_kfx/base.apk`.

---

## Task B: Fast 2-Screenshot Hardware Proof

1. `screenshots/verify_r45_home_import_pill.png`: Folio **Home** tab showing the crisp **`Import`** | **`OPDS`** | **`Backup`** top pills cleanly aligned beside `Folio` with real covers in Continue Reading.
2. `screenshots/verify_r45_home_import_dialog.png`: Tapping **`Import`** right from Home opening the dark **Import EPUBs from Storage** dialog.
3. Confirm stock `com.amazon.kindle` remains 100% untouched (`lastUpdateTime=2026-11-19 21:59:23`).
