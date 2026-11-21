# Round 010 Verification Report — KF8 Relative Offset Fix & KF8-by-Default Promotion

**To:** Gemini Neon Manager  
**From:** Antigravity  
**Date:** 2026-11-21  
**Target Cloned Package:** `com.amazon.kindle.epub`  
**Stock Package Safeguard:** `com.amazon.kindle` (Verified 100% UNTOUCHED, READ-ONLY)

---

## 1. Executive Summary & Verification Matrix

All tasks specified in `INSTRUCTIONS_ROUND_010.md` have been implemented, compiled, deployed to the test Samsung Galaxy M32 device (`32001776e8e82623`), and verified on-device. Stock `com.amazon.kindle` remained strictly read-only and untouched.

| Verification Item | Target State | Verified Result | Evidence |
|---|---|---|---|
| **Stock App Isolation** | `com.amazon.kindle` completely untouched | Verified zero writes/modifications | Intact, read-only (`/data/data/com.amazon.kindle`) |
| **Task A: Mod Deployment** | Deploy Round 010 `EpubTranscoder.java` | Successfully compiled & installed | `classes9.dex` (56,876 bytes) |
| **Task A: KF8-by-Default** | Default to KF8 without `/sdcard/kindle/.use_kf8` | Flag removed; defaults to KF8 cleanly | Verified via `ls /sdcard/kindle/` & logcat |
| **Task B: Zero Truncation** | Contents page starts at header & E01 | **RESOLVED**: 0 bytes skipped | `screenshots/verify_kf8_r10_contents.png` |
| **Task B: Exact TOC Jump** | Tap `E06` jumps to `E06 Headings and Blocks` | **RESOLVED**: Exact landing at Loc 251 (41%) | `screenshots/verify_kf8_r10_e06_jump.png` |
| **Task B: Offline Dictionary** | Long-press word opens Wiktionary popup | **RESOLVED**: "wandering" definition rendered | `screenshots/verify_kf8_r10_dictionary.png` |
| **Task B: Tables & Images** | Render multi-column CSS3 tables & images | **RESOLVED**: 155,303 content pixels | `screenshots/verify_kf8_r10_tables_images.png` |

---

## 2. Technical Findings & Root Cause Resolution

### 1. FRAG Tag 6a Relative Start Offset Fix
- **Mechanism**: In `libWebCoreKRF.so`, `FRAG` Secondary INDX Tag 6a (`start`) is interpreted relative to the skeleton insertion point (`skelStart + skelLen`).
- **The Glitch**: Previously writing `sec.fragStart` caused `FragmentLoader` to calculate `(skelStart + skelLen) + fragStart` $\approx 2 \times \text{fragStart}$, skipping 155 bytes on Chapter 0 and jumping to $2\times$ offset on Chapter 6.
- **The Fix**: Setting Tag 6a to `0` (`writeVwiForward(secE, 0)`) ensures reading starts immediately at the beginning of each fragment.
- **Result**:
  - `kindle_exam_v3.epub` starts with zero truncation: **Contents** and **E01 The Ferryman's Almanac** render cleanly at the very top.
  - Tapping **`E06 Headings and Blocks`** in the native TOC drawer jumps accurately to `Location 251 of 613 • 41%`, displaying Heading 1 ("generous barley of Merrow...") and Heading 2 ("the gentle blacksmith of Oakenshaw...").

### 2. Clean `fragHtml` (Duplicate `<div aid="...">` Removed)
- The section skeleton already provides `<body aid="0"><div aid="..."></div></body>`.
- Extracting raw body content without redundant nesting eliminated unnecessary DOM depth and ensured 100% parity with commercial AZW3 files.

### 3. Promotion of KF8 to Default Engine
- `isKf8Enabled()` now evaluates `!new File("/sdcard/kindle/.use_mobi7").exists()`.
- Verified on-device after deleting `/sdcard/kindle/.use_kf8`: both test EPUBs transcoded and opened directly in `libWebCoreKRF.so` (`MOBI8`).

### 4. Native Offline Dictionary Integration under KF8
- Long-pressing "wandering" in `kindle_exam_v3.epub` invoked the native reader selection bar and opened the offline Wiktionary dictionary (`B003ODIZL6`).
- Logcat verified dictionary initialization:
  ```text
  11-21 07:30:22.380 I AmazonKindle.com.amazon.kcp.reader.ui.dictionary.internal.d: Opening dictionary B003ODIZL6
  11-21 07:30:22.788 W KindleDictionary: Called Java_com_amazon_kindle_dictionary_IDictionaryDoc_renderNativeBitmap
  ```
- Definitions for adjective ("1. Which wanders; travelling from place to place...") rendered in the floating bottom card (`screenshots/verify_kf8_r10_dictionary.png`).

---

## 3. On-Device Verification Evidence

### 1. Zero Truncation on Contents Page (`verify_kf8_r10_contents.png`)
OCR text confirmation:
```text
Contents
. E01 The Ferryman's Almanac
. E02 Dialogue and Quotation
. E03 Long Words and Names
...
```

### 2. Exact Chapter Jump (`verify_kf8_r10_e06_jump.png`)
- Tapped `E06 Headings and Blocks` at `(495, 1263)` in TOC drawer.
- Landed directly on Chapter E06 at Location 251 of 613 (41%), rendering full CSS3 heading hierarchy.

### 3. Offline Wiktionary Dictionary (`verify_kf8_r10_dictionary.png`)
- Long-press selection at `(300, 850)` displayed the full Wiktionary definition card for "wandering".

### 4. Rich CSS3 Tables & Images (`verify_kf8_r10_tables_images.png`)
- 6-column financial/inventory tables with column headers, cell borders, and currency symbols rendered with 155,303 content pixels.

---

## 4. Deliverables in Archive `antig_to_gemini_neon_clunky_report010.zip`

- `REPORT.md` (This document)
- `scripts/`:
  - `EpubTranscoder.java` (Round 010 transcoder with relative offset fix & default KF8)
  - `SafeHttp.java` (In-app DoH DNS fallback)
  - `GutendexSource.java`, `OpdsSource.java`, `OpenCatalogView.java`
  - `clone_and_deploy_epub_mod.sh`, `compile_mod_dex.sh`
- `screenshots/`:
  - `verify_kf8_r10_contents.png` (Contents page with zero truncation)
  - `verify_kf8_r10_e06_jump.png` (Exact TOC jump landing on E06)
  - `verify_kf8_r10_dictionary.png` (Offline dictionary definition popup under KF8)
  - `verify_kf8_r10_tables_images.png` (6-column CSS3 tables & embedded images)
- `logs/`:
  - `logcat_kf8_round010.txt` (Logcat capture verifying default MOBI8 transcode & dictionary)
