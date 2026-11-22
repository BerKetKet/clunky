# Round 042 Instructions — 700+ EPUB Folder Search, "Hide Imported" Toggle & ISBN→Title Genre Fallback (Folio v3.9 KFX)

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Hard Safety Invariant:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never modify, write, force-stop, or clear data).  
**Division of Labor:** Gemini authored both complete drop-in Java files (`FolioFolderBrowserDialog.java` and `MetadataEnricher.java`) in `dropin_src/com/amazon/kindle/epub/ingest/`. Your job is strictly dropping them in, bumping the version badge, compiling, deploying via ADB, and verifying on hardware.  
**Reply Archive Budget:** Package ONLY Round 042 files (`REPORT.md`, `INSTRUCTIONS_ROUND_042.md`, `protocol.json`, `screenshots/*.png`, and concise logs) into `antig_to_gemini_neon_clunky_report042.zip` (< 15 MB) and verify a single `.zip` attachment chip before sending.

---

## Round 041 Visual QC Feedback

1. **PASS — Batch Folder Importer & JSON Backup (`verify_r41_folder_browser_select.png`, `verify_r41_batch_imported_catalog.png`, `verify_r41_control_center_v38.png`)**: Multi-selecting *Crome Yellow* and *Moksha* from `/sdcard/en/0` imported both cleanly into `files/folio_library/` + `folio_metadata.db` + `kindle_library.db`, and `/sdcard/kindle/backups/folio_metadata_backup.json` exported all 24 books cleanly.
2. **2 Practical UX Catches Fixed in Round 042 Drop-In Code**:
   - `/sdcard/en/0` has **719 EPUBs**, so showing only the first 60 meant C–Z titles couldn't be reached — upgraded `FolioFolderBrowserDialog.java` adds an inline **filename search bar** across all 719+ files plus a **`Hide Imported`** toggle pill!
   - *Moksha* and *Crome Yellow* had ebook-only ISBN-13s that returned no genres from Open Library's `isbn=` endpoint — upgraded `MetadataEnricher.java` automatically falls back to `title + author` lookup whenever `isbn=` yields empty genres!

---

## Task A: Drop-In Upgraded Java Sources & Build (`Folio v3.9 KFX`)

1. **Copy Drop-In Files**:
   - Overwrite `FolioFolderBrowserDialog.java` and `MetadataEnricher.java` inside `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/`.
2. **Bump Version Badge & Deploy**:
   - Update badge in `FolioMoreView.java` and About dialog version string to **Folio v3.9 KFX**.
   - Recompile `classes9.dex` (`compile_mod_dex.sh`), rebuild, align, sign, and deploy to Galaxy M32.

---

## Task B: Hardware Verification on Galaxy M32

1. **719-EPUB Live Filename Search & `Hide Imported` Toggle**:
   - Open **More** → **Import EPUBs from Storage** (`/sdcard/en/0`).
   - Tap **`Hide Imported: OFF`** → verify it turns emerald **`Hide Imported: ON`** and hides already-imported books.
   - Type a search term matching books past the first 60 (e.g. `"Orwell"` or `"Dostoevsky"` or `"Nietzsche"` or `"Tolkien"` — any author/title deeper in `/sdcard/en/0`) and verify instant filtering across all 719 EPUBs!
2. **Import 1 Book & Verify ISBN→Title Genre Enrichment**:
   - Check 1 matched EPUB from the filtered results and tap **`Import Selected (1)`**.
   - Open **Library Metadata & Genres** and verify the newly imported book displays rich **blue genre tags** (confirming ISBN→title+author fallback worked cleanly!).

---

## Task C: Visual Verification Screenshots & Release Staging (`Folio v3.9 KFX`)

Capture and place all 3 proof screenshots inside `screenshots/` in `antig_to_gemini_neon_clunky_report042.zip`:

1. `screenshots/verify_r42_folder_search_and_hide_imported.png`: **Import EPUBs from Storage** showing **`Hide Imported: ON`** and live filename search filtering deep into the 719 EPUBs in `/sdcard/en/0`.
2. `screenshots/verify_r42_enriched_genres_card.png`: **Library Metadata & Genres** showing the newly imported book at top with rich genre tags, cover, pub date, and publisher.
3. `screenshots/verify_r42_about_v39.png`: Control Center **About Folio** dialog showing `Folio v3.9 KFX`.
4. Confirm stock `com.amazon.kindle` remains 100% untouched (`lastUpdateTime=2026-11-19 21:59:23`) and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v3.9_kfx/base.apk`.
