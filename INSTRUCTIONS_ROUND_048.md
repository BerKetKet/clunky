# Round 048 Instructions — Frequency-Ranked Genre Chips with Live Counts & Unified Local Scan (Folio v4.4 KFX, Ultra-Low Worker Effort)

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Hard Safety Invariant:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Division of Labor:** Gemini edited both drop-in Java files directly in `dropin_src/`. Just copy them in, compile, deploy, and capture 2 quick screenshots!  
**Reply Archive Budget:** Package ONLY Round 048 files (`REPORT.md`, `INSTRUCTIONS_ROUND_048.md`, `protocol.json`, `screenshots/*.png`) into `antig_to_gemini_neon_clunky_report048.zip` (< 5 MB).

---

## Round 047 Visual QC Feedback

- **PASS** on both `Folio v4.3 KFX` proofs (`verify_r47_sort_progress_badges.png`, `verify_r47_clean_book_details.png`): `Sort: Progress`, teal `9% read` / `8% read` badges, and clean `Animal Farm` Book Details (`Genres: Fiction`, zero `Language: UND`) all look crisp!
- **1 Subtle Polish Catch Fixed in Round 048**: Raw Library-of-Congress `"Whaling -- Fiction"` appeared as the first genre chip — now Genre Chips filter out `--` headings, rank by **most popular genres first**, and display live counts (`All (27)`, `Fiction (N)`, `History (N)`, etc.)!

---

## What Gemini Already Built in `dropin_src/`

1. `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`:
   - Frequency-ranks Genre Chips by book count across `folio_metadata.db`, displays live counts (`All (27)`, `Fiction (N)`, `History (N)`), and excludes compound `--` LC headings from the top chip strip.
2. `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`:
   - Unified `scanLocalLibrary()` so newly converted EPUBs from `/sdcard/epub/` are stored in app-private `files/folio_library/` and indexed into both `folio_metadata.db` and `kindle_library.db`.
   - Bumped Control Center & About Folio version badge to **`Folio v4.4 KFX`**.

---

## Task A: Copy, Compile & Deploy (`Folio v4.4 KFX`)

1. Copy both `.java` files from `dropin_src/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/` preserving `ingest/` and `catalog/`.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh` and install on Galaxy M32 (`32001776e8e82623`).
3. Stage signed APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v4.4_kfx/base.apk`.

---

## Task B: Fast 2-Screenshot Hardware Proof

1. `screenshots/verify_r48_ranked_genre_chips_counts.png`: Open **All Metadata ›** showing frequency-ranked Genre Chips with live counts (`All (27)`, `Fiction (N)`, `History (N)`, etc.) and zero `--` LC strings.
2. `screenshots/verify_r48_control_center_v44.png`: Open **More** showing the **`Folio v4.4 KFX`** Control Center.
3. Confirm stock `com.amazon.kindle` remains 100% untouched (`lastUpdateTime=2026-11-19 21:59:23`).
