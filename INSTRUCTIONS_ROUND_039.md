# Round 039 Instructions — Full-Library Metadata Backfill, Live Filter & Book Details Popup (Folio v3.6 KFX)

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Hard Safety Invariant:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never modify, write, force-stop, or clear data).  
**Division of Labor:** Gemini authored both complete drop-in Java files (`LibraryMetadataBackfiller.java` and upgraded `FolioMetadataCatalogDialog.java`) in `dropin_src/com/amazon/kindle/epub/ingest/`. Your job is strictly dropping them in, compiling, deploying via ADB, and verifying on hardware.  
**Reply Archive Budget:** Package ONLY Round 039 files (`REPORT.md`, `INSTRUCTIONS_ROUND_039.md`, `protocol.json`, `screenshots/*.png`, and concise logs) into `antig_to_gemini_neon_clunky_report039.zip` (< 15 MB) and verify a single `.zip` attachment chip before sending.

---

## Round 038 Visual QC Feedback

1. **PASS — Duplicate Import Guard (`verify_r38_duplicate_import_guard.png`)**: Crisp emerald `ALREADY IN FOLIO LIBRARY` dialog with instant 0ms `Open Existing Copy` and `Re-Import & Replace`.
2. **PASS — Library Metadata & Genres Browser (`verify_r38_metadata_catalog_genres.png`, `verify_r38_control_center_v35.png`)**: Clean dark catalog UI with live genre chips, covers, author/pub-date/publisher/ISBN cards, and Control Center card.
3. **2 Polishing Opportunities Addressed in Round 039 Drop-In Code**:
   - Genre chips like `"shipwrecks"` were lowercase — upgraded `FolioMetadataCatalogDialog.java` normalizes all chips to clean **Title Case** and filters overly long subject strings (`> 24 chars`).
   - Only the 2 externally imported books appeared in `folio_metadata.db` — new `LibraryMetadataBackfiller.java` adds a one-tap **`Sync & Enrich`** button inside the catalog dialog to index and enrich ALL existing local books from `kindle_library.db`!

---

## Task A: Drop-In Sources & Build (`Folio v3.6 KFX`)

1. **Copy Drop-In Files (Preserve Round 038 Fixes!)**:
   - Add `LibraryMetadataBackfiller.java` and overwrite `FolioMetadataCatalogDialog.java` inside `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/`.
   - **Do NOT overwrite** your working Round 038 `FolioMetadataDb.java`, `MetadataEnricher.java`, or `EpubMetadataExtractor.java` (keep all Round 038 schema/timeout/blank-subject fixes intact!).
2. **Bump Version Badge & Deploy**:
   - Update badge in `FolioMoreView.java` to **Folio v3.6 KFX**.
   - Recompile `classes9.dex` (`compile_mod_dex.sh`), rebuild, align, sign, and deploy to Galaxy M32.

---

## Task B: Hardware Verification on Galaxy M32

1. **Full-Library Backfill (`Sync & Enrich`)**:
   - Open **More** → **Library Metadata & Genres**.
   - Tap the green **`Sync & Enrich`** pill at top-right and wait for completion.
   - Verify `folio_metadata.db` now indexes and enriches your existing library books (Aristotle, Herodotus, Livy, Sophocles, Seneca, Confessions of Zeno, Brave New World, Island, etc.) with Title-Cased genre chips!
2. **Live Search Filter & Genre Chip Filter**:
   - Type a query in the inline filter box (e.g., `"Huxley"` or `"Aristotle"` or `"Utopias"`) and verify instant real-time filtering.
3. **Book Details (`ⓘ`) Popup**:
   - Tap the `ⓘ` badge on an enriched book card and verify the dark **Book Details** popup shows title, author, pub date, publisher, ISBN, genres, Open Library key, description/synopsis, and **`Read Now`** button.

---

## Task C: Visual Verification Screenshots & Release Staging (`Folio v3.6 KFX`)

Capture and place all 3 proof screenshots inside `screenshots/` in `antig_to_gemini_neon_clunky_report039.zip`:

1. `screenshots/verify_r39_metadata_catalog_synced.png`: **Library Metadata & Genres** after `Sync & Enrich` showing the full enriched library count, Title-Cased genre chips, search bar, and multiple book cards.
2. `screenshots/verify_r39_metadata_search_filtered.png`: Live search filter or genre chip active inside the catalog dialog.
3. `screenshots/verify_r39_book_details_popup.png`: Tapping `ⓘ` on a book card showing the full **Book Details** dialog with synopsis, publisher, ISBN, genres, and `Read Now`.
4. Confirm stock `com.amazon.kindle` remains 100% untouched (`lastUpdateTime=2026-11-19 21:59:23`) and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v3.6_kfx/base.apk`.
