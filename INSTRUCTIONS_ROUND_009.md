# Round 009 Instructions — KF8 DOM Fragment Selector (`P-//*[@aid='...']`) & Record Ordering Fix

**Target Package:** `com.amazon.kindle.epub`  
**HARD SAFETY INVARIANT:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never write, force-stop, clear data, or modify stock `com.amazon.kindle`).  
**Autonomous Relay Mode:** Execute all tasks end-to-end and upload `antig_to_gemini_neon_clunky_report009.zip` (`< 100 MB`) directly to this chat via Chrome Dev Bridge without pausing.

---

## Changes Applied in Round 009 `EpubTranscoder.java`

Based directly on your Round 008 binary diff against `Rashomon and Other Stories.azw3`:
1. **FRAG CNCX XPath Selector**: Updated `buildKf8FragIndx()` from bare `0001` to **`P-//*[@aid='0001']`** (exact match to `Rashomon`'s `P-//*[@aid='1']`).
2. **PalmDB Record Ordering**: Reordered KF8 records to match `Rashomon`:
   `rec0` → `textRecords` → `FRAG` (`first_non_book_index = fragIdx`) → `SKEL` → `NCX` → `imageRecords` (`first_image_index`) → `FDST` → `FLIS` → `FCIS` → `EOF`.
3. **Multibyte Overlap Trailer (`extra_data_flags = 1`)**: Added `sliceUtf8RecordsWithOverlap()` appending the 1-byte `0x00` overlap trailer per text record and set `rec0 + 240 = 1`.
4. MOBI7+NCX path remains 100% untouched.

---

## Tasks

### Task A — Build & Deploy Round 009 `EpubTranscoder.java`
1. Copy `mod_src/real/com/amazon/kindle/epub/EpubTranscoder.java` into your project.
2. Compile `classes4.dex` via `compile_mod_dex.sh` and deploy `com.amazon.kindle.epub` via `clone_and_deploy_epub_mod.sh`.
3. Confirm stock `com.amazon.kindle` is 100% untouched.

### Task B — Verify Full KF8 Rendering (`libWebCoreKRF.so`)
1. Enable KF8 test mode:
   ```bash
   adb shell "touch /sdcard/kindle/.use_kf8"
   ```
2. Clear cached transcoded files in `com.amazon.kindle.epub` so test books re-transcode with Round 009's KF8 writer.
3. Open **`file:///sdcard/kindle/kindle_tables_images_exam.epub`** and **`file:///sdcard/kindle/kindle_exam_v3.epub`** while capturing `logs/logcat_kf8_round009.txt`.
4. **If KF8 renders body text, CSS3 tables, and images cleanly**:
   - Capture **`screenshots/verify_kf8_tables_images.png`** (CSS3 tables + embedded images).
   - Capture **`screenshots/verify_kf8_exam_v3.png`** (CSS3 typography + headings).
   - Open Kindle's native slide-out TOC drawer in `kindle_exam_v3.epub`, tap `E06 Headings and Blocks`, confirm it jumps to the rendered chapter, and capture **`screenshots/verify_kf8_chapter_jump.png`**.
   - Keep `/sdcard/kindle/.use_kf8` active if all checks pass cleanly!
5. **If the page STILL renders blank white**:
   - Inspect `Rashomon and Other Stories.azw3`'s exact Flow 0 first skeleton + first fragment HTML, `SKEL` secondary entry, and `FRAG` secondary entry (dumping the first 300 bytes of each into `logs/kf8_rashomon_flow_diff.txt`), compare against our generated AZW3, and if you spot a concrete 1–2 line fix (e.g. `<body aid="...">` on the skeleton or `insertPos` offset convention or `FRAG` CNCX format), patch & re-verify directly in this round!
   - If still blank at the end of the round, remove `/sdcard/kindle/.use_kf8` so the phone stays on MOBI7+NCX.

### Task C — Package & Autonomous Upload
Create `antig_to_gemini_neon_clunky_report009.zip` (`< 100 MB`) containing `REPORT.md`, `scripts/`, `screenshots/`, and `logs/`, and upload directly to this chat via Chrome Dev Bridge.
