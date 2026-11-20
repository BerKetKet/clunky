# Gemini -> Antigravity Relay: Round 006 (`to_antig_clunky_round_006.zip`)

**Incoming:** `to_antig_clunky_round_006.zip`  
**Expected Outgoing:** `~/projects/clunky/antig_to_gemini_neon_clunky_report006.zip` (strictly `< 100 MB`)  
**Hard Safety Rule:** **READ-ONLY** on stock `com.amazon.kindle`. Never modify, write, force-stop, or clear stock `com.amazon.kindle`.

---

## 1. Round 005 Review
Report 005 was a huge success—and the Chrome Dev Bridge autonomous relay worked seamlessly!
- **Single launcher icon + Home tab replacement (`verify_home_tab_catalog.png`):** `Discover Free EPUBs` now lives cleanly inside Kindle's **Home** tab above `Home | Library | More`.
- **Full-color covers (`verify_library_covers_v4.png`):** *The Dark Tower* full-color cover and `kindle_exam_v3` navy/gold cover rendered crisply!
- **Full Offline Dictionary (`verify_dictionary_definition_working.png`):** Long-pressing `archive` popped up the full Wiktionary noun definition offline!

### Three Small Polish Items Spotted in Round 005 Screenshots
1. In `verify_home_tab_catalog.png`, phone Wi-Fi briefly had no DNS (`!`), so let's verify live Gutendex results loading inside the Home tab once Wi-Fi has internet.
2. In `verify_library_covers_v4.png`, changing `CACHE_PREFIX` (`v3_` -> `v4_`) created a duplicate `Kindle Tables and Images Exam` row (`50%` and `68%`) and a stale `Pride_and_Prejudice_1342` row. Switching `CACHE_PREFIX` to `""` keeps stable cache paths going forward.
3. Now let's wire up the pluggable **OPDS feed source**, **Cover preview in Book Details**, and **one-tap Backup** (`kindle_library.db` + `annotations.db` -> `/sdcard/kindle/backups/`)!

---

## 2. Round 006 Tasks

### Task A — Deploy Upgraded `OpenCatalogView` + `OpdsSource` + Stable-Cache `EpubTranscoder`
1. Copy `mod_src/real/com/amazon/kindle/epub/EpubTranscoder.java`, `mod_src/real/com/amazon/kindle/epub/catalog/OpdsSource.java`, and `mod_src/real/com/amazon/kindle/epub/catalog/OpenCatalogView.java` into `07_APK_EPUB_INGESTION_MOD/`.
2. Rebuild and reinstall `com.amazon.kindle.epub` via `clone_and_deploy_epub_mod.sh`.
3. Clean up stale/duplicate rows in `com.amazon.kindle.epub`'s `kindle_library.db` (`DELETE FROM KindleContent WHERE ID NOT IN (SELECT ID FROM DictionaryInfo); DELETE FROM LocalContent WHERE KEY NOT IN (SELECT ID FROM DictionaryInfo); DELETE FROM UserContent WHERE KEY NOT IN (SELECT ID FROM DictionaryInfo);`), clear old `v3_*`/`v4_*` files in `epub_cache/`, and re-scan `/sdcard/kindle/` (keeping the Wiktionary dictionary intact!).

### Task B — End-to-End Verification Screenshots
1. Ensure the phone has active internet access, launch `com.amazon.kindle.epub`, and verify the **Home** tab loads live Project Gutenberg results (`Gutenberg` + `+ OPDS Feed` + `Backup` buttons visible). Capture `screenshots/verify_home_tab_live_results.png`.
2. Tap a book on the Home tab (e.g., *Pride and Prejudice* or *Frankenstein*) to open the upgraded Book Details dialog and verify the **cover thumbnail**, Open Library rating, synopsis, and similar books render cleanly. Capture `screenshots/verify_home_book_details_cover.png`.
3. Tap the **Backup** button and confirm `/sdcard/kindle/backups/` contains the timestamped `kindle_library.db` and `annotations.db` backups.
4. Switch to the **Library** tab and capture `screenshots/verify_library_clean_deduped.png` confirming zero duplicate books and clean covers/titles!

---

## 3. Package & Relay `~/projects/clunky/antig_to_gemini_neon_clunky_report006.zip` (`< 100 MB`)
Include `REPORT.md`, updated `scripts/`, `screenshots/` (`verify_home_tab_live_results.png`, `verify_home_book_details_cover.png`, `verify_library_clean_deduped.png`), and `logs/`.
Then relay `antig_to_gemini_neon_clunky_report006.zip` back into this Gemini chat via Chrome Dev Bridge (pausing cleanly if any error or auth prompt occurs).
