# Round 008 Instructions — KF8 (`libWebCoreKRF.so`) Binary Fix & Verification

**Target Package:** `com.amazon.kindle.epub`  
**HARD SAFETY INVARIANT:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never write, force-stop, clear data, or modify stock `com.amazon.kindle`).  
**Autonomous Relay Mode:** Execute all tasks end-to-end and upload `antig_to_gemini_neon_clunky_report008.zip` (`< 100 MB`) directly to this chat via Chrome Dev Bridge without pausing.

---

## Why Round 007's KF8 Test Returned `IKindleDocument.createViewer returned null`

Audit of Round 007's `EpubTranscoder.java` revealed **five concrete binary bugs** in `buildPalmDbKf8()` / `buildSimpleKf8Skel()` / `buildSimpleKf8Frag()`:
1. **Header length & offsets shifted by +16 bytes**: `mobiHdrLen` was `264` instead of `248` (`16 + 248 = 264`), and `ncxIdx`, `fragIdx`, `skelIdx` were written at `rec0 + 260, 264, 268` instead of **`rec0 + 244, 248, 252`** — so `libKRF.so` read `0` for `ncx_index`, `frag_index`, and `skel_index`!
2. **Wrong `INDX` header offsets in `SKEL` and `FRAG`**: Master `INDX` had `offset 8 = 2, offset 12 = 1, offset 16 = 0` instead of **`offset 8 = 0, offset 12 = 0, offset 16 = 2`**, and Secondary `INDX` had `offset 12 = 0` instead of **`offset 12 = 1`** (the exact layout you verified in `libKRF.so` for NCX!).
3. **Wrong `FRAG` control byte**: Wrote `0x1F` instead of **`0x0F`** for the 4 TAGX fields (`0x01 | 0x02 | 0x04 | 0x08`).
4. **Overlapping `SKEL` and `FRAG` ranges**: Both pointed to `[0, htmlLen)` with insert position `0` instead of non-overlapping per-chapter Skeleton (`<html><head><link .../></head><body></body></html>`) + Fragment (`<div aid="0001">...</div>`) with `FRAG` key = exact `</body>` byte offset.
5. **Minor KF8 polish**: Added `EXTH 131 = 0` (`unidentified_count`) and Base32 4-char `toBase32Four()` for `kindle:embed:` image URIs.

All 5 fixes are applied in `mod_src/real/com/amazon/kindle/epub/EpubTranscoder.java` (MOBI7+NCX path is 100% untouched).

---

## Tasks

### Task A — Deploy Fixed `EpubTranscoder.java`
1. Copy `mod_src/real/com/amazon/kindle/epub/EpubTranscoder.java` into your project's `mod_src/real/com/amazon/kindle/epub/EpubTranscoder.java`.
2. Rebuild `classes4.dex` via `compile_mod_dex.sh` and redeploy `com.amazon.kindle.epub` via `clone_and_deploy_epub_mod.sh`.
3. Verify `com.amazon.kindle` (stock) is 100% untouched.

### Task B — End-to-End KF8 (`libWebCoreKRF.so`) Verification
1. Enable KF8 test mode on the device:
   ```bash
   adb shell "touch /sdcard/kindle/.use_kf8"
   ```
2. Clear the transcoded cache files in `com.amazon.kindle.epub`'s cache directory (and remove stale DB rows for the test books if needed so they re-transcode cleanly in KF8).
3. Open **`file:///sdcard/kindle/kindle_tables_images_exam.epub`** and **`file:///sdcard/kindle/kindle_exam_v3.epub`** in `com.amazon.kindle.epub` while capturing `logcat` (`logs/logcat_kf8_round008.txt`).
4. **If KF8 renders cleanly**:
   - Capture **`screenshots/verify_kf8_tables_images.png`** (showing CSS3 table borders/styling + embedded images).
   - Capture **`screenshots/verify_kf8_exam_v3.png`** (showing CSS3 typography/headings).
   - Open Kindle's native slide-out **"Go To -> Table of Contents"** drawer in `kindle_exam_v3.epub` under KF8 mode, verify all chapters appear, tap a chapter to confirm jumping works, and capture **`screenshots/verify_kf8_native_toc.png`**.
   - Keep `/sdcard/kindle/.use_kf8` in place if both books + native TOC drawer render without errors (otherwise remove `/sdcard/kindle/.use_kf8` so the phone stays on MOBI7+NCX).
5. **If `IKindleDocument.createViewer` STILL returns null**:
   - Cleanly remove `/sdcard/kindle/.use_kf8` so the phone stays on rock-solid MOBI7+NCX.
   - Inspect `KindleDocumentMobi::createViewer` / `IKindleDocument::createViewer` in `libKRF.so` (or `libWebCoreKRF.so`) and dump the generated KF8 file's Record 0 (`0..320`), `FDST`, `SKEL`, and `FRAG` headers into `logs/kf8_krf_analysis.txt` (if `ebook-convert` / Calibre is installed on the host PC, also diff against a 1-chapter Calibre `.azw3` header!).
   - If you spot the exact missing header/INDX field from `libKRF.so` disassembly, feel free to patch & verify it directly in this round and include the updated `EpubTranscoder.java` in `scripts/`!

### Task C — Package & Autonomous Upload
Create `antig_to_gemini_neon_clunky_report008.zip` (`< 100 MB`) containing:
- `REPORT.md`
- `scripts/` (current `EpubTranscoder.java` and any modified scripts)
- `screenshots/` (Round 008 KF8 verification screenshots)
- `logs/` (`logcat_kf8_round008.txt`, and `kf8_krf_analysis.txt` if applicable)

Upload `antig_to_gemini_neon_clunky_report008.zip` directly to this chat via Chrome Dev Bridge.
