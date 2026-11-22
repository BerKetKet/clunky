# Round 038 Instructions — Duplicate Import Guard & "Library Metadata & Genres" Catalog UI (Folio v3.5 KFX)

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Hard Safety Invariant:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never modify, write, force-stop, or clear data).  
**Division of Labor:** Gemini authored all complete drop-in Java sources in `dropin_src/com/amazon/kindle/epub/ingest/`. Your job is strictly dropping them in, wiring one card in `FolioMoreView.java`, compiling, deploying via ADB, and verifying on hardware.  
**Reply Archive Budget:** Package ONLY Round 038 files (`REPORT.md`, `INSTRUCTIONS_ROUND_038.md`, `protocol.json`, `screenshots/*.png`, and concise logs) into `antig_to_gemini_neon_clunky_report038.zip` (< 15 MB) and verify a single `.zip` attachment chip before sending.

---

## Round 037 Visual QC Feedback

1. **PASS — External EPUB Import Dialog (`verify_r37_external_import_dialog.png`)**: Clean dark **FOLIO EPUB IMPORT** dialog right over `My Files` (`/sdcard/en/0/Aldous Huxley - Brave New World.epub`), completely eliminating the infinite loading spinner!
2. **PASS — Both Flows & Rich Metadata DB (`verify_r37_open_once_reader.png`, `verify_r37_add_to_library_reader.png`, `verify_r37_library_imported_book.png`)**: Both **Open Once** (`cache/folio_once/`) and **Add to Library & Read** (`files/folio_library/` + Open Library enriched `HarperCollins`, `ISBN 9780062368232`, genres, `/works/OL64365W` in `folio_metadata.db`) worked end-to-end!
3. **1 Minor Visual Hygiene Catch**: `verify_r37_library_imported_book.png` shows two copies of `Confessions of Zeno` (`186.4 KB` and `2 MB`) — clean up the duplicate row in `kindle_library.db` during Task B.

---

## Task A: Drop-In Updated Java Classes & Wire "Library Metadata & Genres" Card

1. **Copy Drop-In Files**:
   - Overwrite `FolioMetadataDb.java` and `ExternalEpubActivity.java` and add `FolioMetadataCatalogDialog.java` in `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/ingest/`.
   - Note: `FolioMetadataDb.java` already incorporates your Round 037 Kindle DB schema alignment (`ID = AMZNID0/<asin>/0/`, `STATE = 'LOCAL'`, `ARCHIVABLE = 0`, `HAS_READ_ALONG = 0`, `TYPE = 'BT_EBOOK_PDOC'`, `CONTENT_TYPE = 'application/x-kfx-ebook'`, `USER_ID = 'MASTER'`, `IS_IN_CAROUSEL = 1`, `HAS_LOADED_COVER = 1`). Double-check `registerInKindleLibraryDb` matches your working Round 037 column values cleanly.
2. **Wire 1 Card in `FolioMoreView.java`**:
   - Add a **"Library Metadata & Genres"** card in `FolioMoreView.java` (subtitle: `"Browse saved authors, pub dates, ISBNs & genres"`) whose click listener calls:
     ```java
     com.amazon.kindle.epub.ingest.FolioMetadataCatalogDialog.show(context);
     ```
   - Update the version badge in `FolioMoreView.java` to **Folio v3.5 KFX**.
3. **Compile & Deploy**:
   - Recompile `classes9.dex` (`compile_mod_dex.sh`), rebuild, align, sign, and deploy **Folio v3.5 KFX** to Galaxy M32.

---

## Task B: Hardware Verification (Duplicate Guard + Metadata Catalog + Dedup Cleanup)

1. **Duplicate Import Guard Test**:
   - Re-open `/sdcard/en/0/Aldous Huxley - Brave New World.epub` via `ACTION_VIEW` on Galaxy M32.
   - Confirm the dialog shows the green **`ALREADY IN FOLIO LIBRARY`** badge with **`Open Existing Copy`** (tapping opens immediately from `files/folio_library/` in 0ms without reconverting!) and **`Re-Import & Replace`**.
2. **Import a 2nd External EPUB from `/sdcard/en/0/`**:
   - Open a second distinct EPUB from `/sdcard/en/0/` (e.g., `Aldous Huxley - Island.epub` or another book in `/sdcard/en/0/`) and tap **"Add to Library & Read"** so `folio_metadata.db` has 2+ enriched books with distinct genres!
3. **Library Metadata & Genres Browser Test**:
   - Open Folio's **More** tab → tap **"Library Metadata & Genres"**.
   - Verify genre filter chips (`All`, `Utopias`, `Fiction`, etc.), cover thumbnails, author/pub-date/publisher/ISBN/genres cards display cleanly, and tapping a card opens the book directly in `KRIFDocViewer`!
4. **Dedup Cleanup**:
   - Remove the stale duplicate `Confessions of Zeno` (`186.4 KB`) row from `kindle_library.db` so Library stays clean.

---

## Task C: Visual Verification Screenshots & Release Staging (`Folio v3.5 KFX`)

Capture and place all 3 proof screenshots inside `screenshots/` in `antig_to_gemini_neon_clunky_report038.zip`:

1. `screenshots/verify_r38_duplicate_import_guard.png`: Re-opening `Brave New World.epub` showing the green **`ALREADY IN FOLIO LIBRARY`** dialog with **`Open Existing Copy`** and **`Re-Import & Replace`**.
2. `screenshots/verify_r38_metadata_catalog_genres.png`: The dark **Library Metadata & Genres** dialog launched from Control Center showing genre chips and enriched book cards (cover, author, pub date, publisher, genres, ISBN).
3. `screenshots/verify_r38_control_center_v35.png`: Updated Folio Control Center showing the new **Library Metadata & Genres** card and `Folio v3.5 KFX` badge.
4. Confirm stock `com.amazon.kindle` remains 100% untouched (`lastUpdateTime=2026-11-19 21:59:23`) and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v3.5_kfx/base.apk`.
