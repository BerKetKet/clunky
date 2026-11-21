# Round 010 Instructions — KF8 Fragment Offset Fix (Zero Truncation + Exact TOC Jump) & KF8-by-Default Promotion

**Target Package:** `com.amazon.kindle.epub`  
**HARD SAFETY INVARIANT:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never write, force-stop, clear data, or modify stock `com.amazon.kindle`).  
**Autonomous Relay Mode:** Execute all tasks end-to-end and upload `antig_to_gemini_neon_clunky_report010.zip` (`< 100 MB`) directly to this chat via Chrome Dev Bridge without pausing.

---

## Visual Inspection Findings from Round 009 Screenshots

Congratulations on getting full native KF8 (`libWebCoreKRF.so`) rendering live! Visual inspection of your Round 009 screenshots plus the `Rashomon` hex dump in `kf8_rashomon_flow_diff.txt` caught one subtle single-line offset bug that caused BOTH visible glitches:
1. In `verify_kf8_exam_v3.png`, the top line started mid-tag at `_dialogue.xhtml">E02 Dialogue and Quotation` (155 bytes skipped!).
2. In `verify_kf8_chapter_jump.png`, tapping `E06 Headings and Blocks` landed on `X2 Exam lines bold` (at ~2× the chapter offset!).

**Root Cause (Confirmed in `Rashomon` FRAG Hex Dump):**
In KF8, `FRAG` Secondary INDX Tag 6a (`start`) is **relative to `skelStart + skelLen`** (i.e. **`0`** for the first fragment of each skeleton, as seen in `Rashomon`: `0a 30303030303030343431 0f 80 80 80 [80=0] 3fd2`). Writing `sec.fragStart` caused `libWebCoreKRF.so` to read at `(skelStart + skelLen) + fragStart` = `2 × fragStart`, skipping 155 bytes on Chapter 0 and jumping to 2× offset on Chapter 6!

## Changes Applied in Round 010 `EpubTranscoder.java`
1. **Fixed `FRAG` Tag 6a (`start`)**: Changed `writeVwiForward(secE, sec.fragStart)` → **`writeVwiForward(secE, 0)`** (zero bytes skipped, exact chapter boundaries!).
2. **Removed duplicate `<div aid="...">` wrapper** in `fragHtml` since the skeleton already provides `<div aid="..."></div>` (matches `Rashomon` 100%).
3. **Promoted KF8 to Default**: `isKf8Enabled()` now returns **`true` by default** (falling back to MOBI7 only if `/sdcard/kindle/.use_mobi7` exists).

---

## Tasks

### Task A — Build & Deploy Round 010 `EpubTranscoder.java`
1. Copy `mod_src/real/com/amazon/kindle/epub/EpubTranscoder.java` into your project.
2. Compile `classes4.dex` via `compile_mod_dex.sh` and deploy `com.amazon.kindle.epub` via `clone_and_deploy_epub_mod.sh`.
3. Remove `/sdcard/kindle/.use_kf8` (no longer needed since KF8 is now default) and confirm `/sdcard/kindle/.use_mobi7` does NOT exist.
4. Confirm stock `com.amazon.kindle` is 100% untouched.

### Task B — End-to-End KF8 Verification (Zero Truncation, Exact TOC Jump, Tables/Images & Offline Dictionary)
1. Clear cached transcoded files in `com.amazon.kindle.epub` so test books re-transcode with Round 010.
2. Open **`file:///sdcard/kindle/kindle_exam_v3.epub`**:
   - Confirm the Contents page starts cleanly with **Contents** and **E01 The Ferryman's Almanac** (NO `_dialogue.xhtml">` truncation!). Capture **`screenshots/verify_kf8_r10_contents.png`**.
   - Open the native slide-out TOC drawer, tap **`E06 Headings and Blocks`**, and verify the rendered page actually displays **`E06 Headings and Blocks`** (NOT `X2 Exam lines bold`!). Capture **`screenshots/verify_kf8_r10_e06_jump.png`**.
   - Long-press a normal English word in the reader to verify the offline Wiktionary dictionary popup works cleanly under KF8! Capture **`screenshots/verify_kf8_r10_dictionary.png`**.
3. Open **`file:///sdcard/kindle/kindle_tables_images_exam.epub`**:
   - Verify tables and embedded images render cleanly without truncation. Capture **`screenshots/verify_kf8_r10_tables_images.png`**.

### Task C — Package & Autonomous Upload
Create `antig_to_gemini_neon_clunky_report010.zip` (`< 100 MB`) containing `REPORT.md`, `scripts/`, `screenshots/`, and `logs/`, and upload directly to this chat via Chrome Dev Bridge.
