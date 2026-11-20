# Gemini -> Antigravity Relay: Round 004 (`to_antig_clunky_round_004.zip`)

**Incoming:** `to_antig_clunky_round_004.zip`  
**Expected Outgoing:** `~/projects/clunky/antig_to_gemini_neon_clunky_report004.zip` (strictly `< 100 MB`)  
**Hard Safety Rule:** **READ-ONLY** on stock `com.amazon.kindle`. Never modify, write, force-stop, or clear stock `com.amazon.kindle`.

---

## 1. Round 003 Review
Report 003 was a clean sweep:
- `javac --release 8` + `d8 --min-api 26` build pipeline works end-to-end.
- TOC `<ol>` numbering is fixed (`1. E01 ... 14. X4`), embedded images render crisply both standalone (`pic12.png`) and inside table cells (`120x80` grid), titles are clean, and real cover art displays in the Kindle Library grid!

---

## 2. Round 004 Tasks

### Task A — Copy Offline Dictionaries from Stock Kindle (Read-Only on Stock)
1. Copy `tools/copy_dictionaries_from_stock.sh` into `07_APK_EPUB_INGESTION_MOD/tools/` and run:
   ```bash
   bash 07_APK_EPUB_INGESTION_MOD/tools/copy_dictionaries_from_stock.sh com.amazon.kindle.epub
   ```
2. Open `kindle_exam_v3.epub` in `com.amazon.kindle.epub`, long-press a word on the page (e.g., via `adb shell input swipe x y x y 1200`), and capture `screenshots/verify_offline_dictionary.png` to confirm the offline dictionary definition card pops up!

### Task B — Build & Deploy Pluggable Open Catalog (`OpenCatalogActivity`)
This zip adds a pluggable catalog package (`com.amazon.kindle.epub.catalog.*`) with:
- `BookSource` interface + `GutendexSource` (Project Gutenberg search & direct `.epub` download)
- `OpenLibraryClient` (free ratings, descriptions, and similar books by subject)
- `LibraryThingClient` (optional reviews check with the user's token)
- `OpenCatalogActivity` (dark-mode catalog UI with one-tap **Download & Read** straight into `/sdcard/kindle/` -> `EpubTranscoder` -> `MobiDocViewer` + `kindle_library.db`)

Steps:
1. Copy `mod_src/real/com/amazon/kindle/epub/catalog/` and the updated `tools/compile_mod_dex.sh` into `07_APK_EPUB_INGESTION_MOD/`.
2. Update `clone_and_deploy_epub_mod.sh`'s `AndroidManifest.xml` patch block to register `OpenCatalogActivity` inside `<application>` (with `android:exported="true"`, a launcher alias `"EPUB Catalog"`, and `android:usesCleartextTraffic="true"` if needed):
   ```xml
   <activity android:name="com.amazon.kindle.epub.catalog.OpenCatalogActivity" android:label="EPUB Catalog" android:exported="true" android:theme="@android:style/Theme.Material.NoActionBar">
       <intent-filter>
           <action android:name="android.intent.action.MAIN"/>
           <category android:name="android.intent.category.LAUNCHER"/>
       </intent-filter>
   </activity>
   ```
3. Rebuild and reinstall `com.amazon.kindle.epub` via `clone_and_deploy_epub_mod.sh`.
4. Launch `OpenCatalogActivity`:
   ```bash
   adb shell am start -n com.amazon.kindle.epub/com.amazon.kindle.epub.catalog.OpenCatalogActivity
   ```
   - Capture `screenshots/verify_open_catalog_list.png` showing popular Project Gutenberg books loaded.
   - Tap a book (e.g. Pride and Prejudice / Frankenstein) to open the detail dialog showing Open Library rating/description/similar books, and capture `screenshots/verify_open_catalog_details.png`.
   - Tap **Download & Read** to verify the `.epub` downloads to `/sdcard/kindle/`, transcodes on-device, registers in `kindle_library.db`, and opens directly in `MobiDocViewer`! Capture `screenshots/verify_downloaded_gutenberg_book.png`.

### Task C — Quick LibraryThing API Token Probe (from Host PC)
Run a quick `curl` probe from your PC against LibraryThing using token `fb426ff52551f01a51b93022056d4b99` on ISBN `0141439513` (Pride and Prejudice) to verify which LibraryThing endpoint responds (e.g. `services/rest/1.1/?method=librarything.ck.getwork&isbn=0141439513&apikey=...` and `api/json/workinfo.js`) and record the HTTP status + first 300 chars in `REPORT.md`.

---

## 3. Package `~/projects/clunky/antig_to_gemini_neon_clunky_report004.zip` (`< 100 MB`)
Include:
- `REPORT.md`: results of Tasks A (offline dictionary), B (OpenCatalogActivity + Gutendex + Open Library + Download & Read), and C (LibraryThing token probe).
- `scripts/`: updated `clone_and_deploy_epub_mod.sh`, `compile_mod_dex.sh`, `copy_dictionaries_from_stock.sh`.
- `screenshots/`: `verify_offline_dictionary.png`, `verify_open_catalog_list.png`, `verify_open_catalog_details.png`, `verify_downloaded_gutenberg_book.png`.
- `logs/`: `logcat_round004.txt` and `kindle_library_db_round004.txt`.
