# Gemini -> Antigravity Relay: Round 003 (`to_antig_clunky_round_003.zip`)

**Incoming:** `to_antig_clunky_round_003.zip`  
**Expected Outgoing:** `~/projects/clunky/antig_to_gemini_neon_clunky_report003.zip` (strictly `< 100 MB`)  
**Hard Safety Rule:** **READ-ONLY** on stock `com.amazon.kindle`. Only modify/deploy/test `com.amazon.kindle.epub` (continuing to build from `handoff_assets/com.amazon.kindle_v8.159.0.100_base_original.apk`).

---

## 1. Round 002 Review
Report 002 was a massive breakthrough:
- `patch_classes5.py` unlocked the main **Library** tab grid offline—all 7 local EPUBs now appear directly in the Library tab with reading progress badges (`25%`) and resume cleanly!
- **More -> Settings -> Local file access** (`LocalFilesComposeActivity` / `LocalFilesActivity`) works completely offline and lets the user pick/add folders (`/sdcard/kindle/`, `/sdcard/Books/`, `ADD FOLDER`) that auto-scan and import `.epub` files into `kindle_library.db`.
- **Visual Bug Spotted in `verify_rich_images_tables.png`:**
  - The `"Image inside a cell"` table cell was actually blank white because `epub2prc.py` set `last_content_record = num_text` (`9`) instead of `num_text + num_images` (`24`), causing `libKRF.so` to treat image record lookups as out of bounds!
  - Also, `verify_cloned_kindle_epub.png` showed the MOBI7 `<ol>` `65535.` counter underflow bug, and `verify_library_grid_unlocked.png` showed generic icons (no embedded cover art) and clipped `"keleton Crew"` titles from fixed 32-byte space padding.

---

## 2. Round 003 Goal — Full On-Device Java `EpubTranscoder` (Compiled via `javac` + `d8`)
Instead of hand-assembling raw Dalvik opcodes in Python, this zip ships clean, full-featured Java sources (`mod_src/real/`) + compile-time stubs (`mod_src/stubs/`) + `tools/compile_mod_dex.sh` (`javac --release 8` + `d8 --min-api 26`), which upgrades on-device `EpubTranscoder` with:
1. **True OPF Spine Ordering & Exact Metadata**: Parses `META-INF/container.xml` -> `.opf` (`<manifest>`, `<spine>`, `<dc:title>`, `<dc:creator>`, `<dc:publisher>`, `<dc:language>`, `<meta name="cover">` / `properties="cover-image"`), fixing `"keleton Crew"` and out-of-order chapters.
2. **Embedded Images & Real Cover Art**:
   - Extracts all `.jpg`/`.jpeg`/`.png`/`.gif` entries, converts PNG/GIF to RGB JPEG on-device via Android's `BitmapFactory` + `Canvas` (white background) + `Bitmap.compress(JPEG, 90)`, embeds them as PalmDB image records with **`last_content_record = num_text + num_imgs`**, sets EXTH `201/202/203` (cover/thumbnail offsets), and rewrites `<img src="..."/>` to `<img recindex="00001"/>`.
3. **Clean HTML Normalization**:
   - Fixes the MOBI7 `<ol>` `65535.` counter bug by converting `<ol><li>` into numbered `<p>1. ...</p>` items.
   - Ensures `<table border="1">` on borderless tables and strips `<style>`/`<script>`.
4. **Automatic Cache Refresh**: Uses `v3_<name>.prc` cache filenames so all existing EPUBs re-transcode automatically with images, covers, and clean titles!

---

## 3. Round 003 Tasks
1. Copy `mod_src/` and `tools/compile_mod_dex.sh` from this zip into `07_APK_EPUB_INGESTION_MOD/`.
2. Run `bash 07_APK_EPUB_INGESTION_MOD/tools/compile_mod_dex.sh 07_APK_EPUB_INGESTION_MOD/dex/classes9.dex` to compile the new Java `EpubTranscoder` into `dex/classes9.dex` (which `clone_and_deploy_epub_mod.sh` injects as `classes4.dex`).
   - Note: If `javac` reports any missing JDK/SDK flag, check `javac -version` (e.g., use `-source 8 -target 8` if `--release 8` is unavailable) and verify `d8` succeeds cleanly.
3. Update `clone_and_deploy_epub_mod.sh` to invoke `compile_mod_dex.sh` automatically before step `[4/6]`, then rebuild and reinstall `com.amazon.kindle.epub` (keeping stock `com.amazon.kindle` 100% untouched).
4. Clear ONLY `com.amazon.kindle.epub`'s old `epub_cache` (or let `v3_` re-transcode) and re-open both:
   - `file:///sdcard/kindle/kindle_exam_v3.epub` -> verify TOC numbering is now `1. E01 ...`, `2. E02 ...` (NO `65535.`), and navigate to `E09 Pagination and Images` to confirm embedded images render! Capture `screenshots/verify_v3_toc_fixed.png` and `screenshots/verify_v3_images.png`.
   - `file:///sdcard/kindle/kindle_tables_images_exam.epub` -> navigate to `"Image inside a cell"` (`OEBPS/t5_flow.xhtml`) AND a standalone image page to confirm the 120x80 and full-width images **actually draw visibly** inside `MobiDocViewer`! Capture `screenshots/verify_tables_images_v3.png`.
   - Open the **Library** tab (and if any EPUB in `/sdcard/kindle/` has a cover image, re-scan via Local File Access) and capture `screenshots/verify_library_covers_v3.png` to verify clean titles (no clipped `"keleton Crew"`) and cover thumbnails!

---

## 4. Package `~/projects/clunky/antig_to_gemini_neon_clunky_report003.zip` (`< 100 MB`)
Include:
- `REPORT.md`: compile results (`javac`/`d8`), visual verification of embedded images, `<ol>` TOC fix, clean titles/covers in Library grid, and any remaining issues.
- `scripts/`: updated `compile_mod_dex.sh`, `clone_and_deploy_epub_mod.sh`, and any tweaks made to `EpubTranscoder.java`.
- `screenshots/`: `verify_v3_toc_fixed.png`, `verify_v3_images.png`, `verify_tables_images_v3.png`, `verify_library_covers_v3.png`.
- `logs/`: `logcat_round003.txt` and `kindle_library_db_round003.txt`.
