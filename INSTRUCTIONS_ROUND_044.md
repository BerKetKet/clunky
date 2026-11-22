# Round 044 Instructions — Milestone Folio v4.0 KFX Polish (Ultra-Low Worker Effort)

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Hard Safety Invariant:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Division of Labor:** Gemini edited all 3 Java files directly from your Round 043 snapshot and placed them in `dropin_src/`. **Do NOT rewrite or modify them** — just copy them in, compile, deploy, and capture 2 quick screenshots!  
**Reply Archive Budget:** Package ONLY Round 044 files (`REPORT.md`, `INSTRUCTIONS_ROUND_044.md`, `protocol.json`, `screenshots/*.png`) into `antig_to_gemini_neon_clunky_report044.zip` (< 5 MB).

---

## What Gemini Already Built in `dropin_src/`

1. `dropin_src/com/amazon/kindle/epub/ingest/EpubMetadataExtractor.java`:
   - Added `normalizePubDate()` stripping raw ISO timestamps (`2000-07-15 00:00:00+01:00` → `2000-07-15`).
   - Added `isNoiseTag()` filtering Calibre status/noise tags (`Read`, `Unread`, `To Read`, `Ebook`, `Calibre`, `General`).
2. `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`:
   - Applies `normalizePubDate()` on book cards and Book Details popups so existing records like *Animal Farm* immediately display crisp `2000-07-15`.
   - Filters noise tags (`Read`, etc.) out of genre filter chips.
3. `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`:
   - Bumped milestone badge and About dialog to **`Folio v4.0 KFX`**.
   - Upgraded **About Folio** with live breakdown of **Enriched Metadata Records**, **App Library Storage** (`files/folio_library/` + `files/folio_covers/`), **Legacy `/sdcard/kindle`**, and **Temp Open-Once Cache** (`cache/folio_once/` + `cache/folio_staging/`).
   - Added one-tap **`Clear Temp Cache`** button inside **About Folio** with freed-MB Toast.

---

## Task A: Copy, Compile & Deploy (`Folio v4.0 KFX`)

1. Copy all 3 files from `dropin_src/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/` preserving their subdirectories (`ingest/` and `catalog/`).
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh` and install on Galaxy M32 (`32001776e8e82623`).
3. Stage signed APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v4.0_kfx/base.apk`.

---

## Task B: Fast 2-Screenshot Hardware Proof

1. `screenshots/verify_r44_clean_dates_and_genres.png`: Open **More** → **Library Metadata & Genres** proving *Animal Farm* now shows clean `2000-07-15` (no `00:00:00+01:00`) and `"Read"` is gone from the genre chips.
2. `screenshots/verify_r44_about_v40_storage_and_clear_cache.png`: Open **More** → **About Folio** showing **`Folio v4.0 KFX`**, real **App Library Storage** & **Temp Open-Once Cache** stats, and the **`CLEAR TEMP CACHE`** button.
3. Confirm stock `com.amazon.kindle` remains 100% untouched (`lastUpdateTime=2026-11-19 21:59:23`).
