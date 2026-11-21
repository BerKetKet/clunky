# Round 012 Instructions — Custom App Name & Launcher Icon ("Folio") + Multi-Book Download Stress Test

**Target Package:** `com.amazon.kindle.epub`  
**HARD SAFETY INVARIANT:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never write, force-stop, clear data, or modify stock `com.amazon.kindle`).  
**Autonomous Relay Mode:** Execute all tasks end-to-end and upload `antig_to_gemini_neon_clunky_report012.zip` (`< 100 MB`) directly to this chat via Chrome Dev Bridge without pausing.

---

## Changes Applied in Round 012

1. **Custom App Name & Launcher Icon (`Folio`)**:
   - Bundled full-bleed 512×512 PNG launcher icon at `assets/folio_launcher_icon.png` (deep midnight navy-to-teal background with warm ivory open book + amber bookmark ribbon).
   - Updated `scripts/clone_and_deploy_epub_mod.sh` to set `android:label="Folio"` (configurable via `APP_LABEL`), copy `folio_launcher_icon.png` to `res/drawable/folio_launcher_icon.png`, and set `android:icon` & `android:roundIcon` on `<application>` and any `CATEGORY_LAUNCHER` activity/activity-alias.
2. **Hardened Multi-Mirror Download Engine (`OpenCatalogView.java`)**:
   - Added 4-tier Project Gutenberg mirror fallback (`book.epubUrl` → `gutenberg.org/...-images.epub` → `aleph.gutenberg.org/...-images.epub` → `aleph.gutenberg.org/...pg{id}.epub`).
   - Added strict **ZIP/EPUB magic-byte validation (`PK\x03\x04`, `>= 1024` bytes)** before committing the downloaded file so an HTML 503/404 page from any mirror can never overwrite or save a corrupt `.epub`.
   - Updated status toast to `"Opening in Folio..."`.

---

## Tasks

### Task A — Deploy "Folio" Branding & Hardened Download Engine
1. Copy:
   - `assets/folio_launcher_icon.png` → `<MOD_ROOT>/assets/folio_launcher_icon.png`
   - `mod_src/real/com/amazon/kindle/epub/catalog/OpenCatalogView.java` → `<MOD_ROOT>/mod_src/real/com/amazon/kindle/epub/catalog/OpenCatalogView.java`
   - `scripts/clone_and_deploy_epub_mod.sh` → `<MOD_ROOT>/tools/clone_and_deploy_epub_mod.sh`
2. Run `clone_and_deploy_epub_mod.sh` to rebuild & install `com.amazon.kindle.epub` with the new **Folio** name and custom icon.
3. Verify `com.amazon.kindle` (stock) is 100% untouched (still named "Kindle" with its original icon).
4. Open the Android app drawer (or home screen) showing the new **Folio** app icon and label clearly visible, and capture **`screenshots/verify_r12_launcher_icon_name.png`**.

### Task B — Multi-Book Real Download Stress Test (2 Fresh Books from Home Catalog)
1. Launch **Folio** (`com.amazon.kindle.epub`), go to the **Home** tab, and actually download **2 fresh books** via **DOWNLOAD & READ** (for example: *Alice's Adventures in Wonderland* and *The Picture of Dorian Gray* or *Moby Dick* — pick 2 books not already in `/sdcard/kindle/`).
2. For EACH downloaded book:
   - Verify the saved `.epub` file in `/sdcard/kindle/` is a valid ZIP archive (`unzip -t` / `PK\x03\x04` check) and log its byte size in `REPORT.md`.
   - Confirm it auto-transcodes to KF8 (`format: MOBI8`) and opens cleanly in the reader with readable text and working native TOC drawer.
   - Capture **`screenshots/verify_r12_downloaded_book1.png`** and **`screenshots/verify_r12_downloaded_book2.png`**.
3. Return to the **Library** tab and verify the newly downloaded books appear cleanly in the Library grid with covers. Capture **`screenshots/verify_r12_library_with_downloads.png`**.

### Task C — Package & Autonomous Upload
Create `antig_to_gemini_neon_clunky_report012.zip` (`< 100 MB`) containing `REPORT.md`, `scripts/`, `screenshots/`, and `logs/`, and upload directly to this chat via Chrome Dev Bridge.
