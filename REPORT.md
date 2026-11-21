# Round 009 Verification Report — Full KF8 (`libWebCoreKRF.so`) Rendering & Native TOC

**To:** Gemini Neon Manager  
**From:** Antigravity  
**Date:** 2026-11-21  
**Target Cloned Package:** `com.amazon.kindle.epub`  
**Stock Package Safeguard:** `com.amazon.kindle` (Verified 100% UNTOUCHED, READ-ONLY)

---

## 1. Executive Summary & Verification Matrix

All tasks specified in `INSTRUCTIONS_ROUND_009.md` have been executed, deployed to the test Samsung Galaxy M32 device (`32001776e8e82623`), and verified on-device. The stock app `com.amazon.kindle` remained strictly read-only and untouched.

| Verification Item | Target State | Verified Result | Evidence |
|---|---|---|---|
| **Stock App Isolation** | `com.amazon.kindle` completely untouched | Verified zero writes/modifications | Intact, read-only (`/data/data/com.amazon.kindle`) |
| **Task A: Mod Deployment** | Deploy Round 009 `EpubTranscoder.java` | Successfully compiled & installed | `classes9.dex` (56,932 bytes) |
| **Task B: KF8 Viewer Creation** | `IKindleDocument.createViewer` | **RESOLVED & NON-NULL** (`format: MOBI8`) | `logs/logcat_kf8_round009.txt` |
| **Task B: KF8 Body & Table Rendering** | Render CSS3 tables, typography & embedded images | **RESOLVED**: 203,765 content pixels | `screenshots/verify_kf8_tables_images.png` |
| **Task B: KF8 Typography & Headings** | Render CSS3 prose, font styling & headings | **RESOLVED**: 75,808 content pixels | `screenshots/verify_kf8_exam_v3.png` |
| **Task B: KF8 Native TOC Drawer** | Slide-out native drawer populated with 16 chapters | **RESOLVED**: All chapters rendered | `screenshots/verify_kf8_native_toc.png` |
| **Task B: KF8 Native Chapter Jump** | Tap `E06 Headings and Blocks` jumps to chapter | **RESOLVED**: Jumped to Loc 252 (41%), 583,653 content pixels | `screenshots/verify_kf8_chapter_jump.png` |
| **Engine Retention** | Keep `/sdcard/kindle/.use_kf8` active | **ACTIVE & VERIFIED** | `/sdcard/kindle/.use_kf8` kept in place |

---

## 2. Technical Findings & Root Cause Resolution

### The Blank White Page Root Cause in KF8
In the initial Round 009 build, `IKindleDocument.createViewer` succeeded and recognized `format: MOBI8` and `MIME type: application/x-mobi8-ebook`. However, the book content area initially rendered 0 non-white pixels (blank white).

By generating an exhaustive binary and text diff against `Rashomon and Other Stories.azw3` (`logs/kf8_rashomon_flow_diff.txt`), we discovered the missing piece:
1. **Skeleton Anchor Element Requirement**:
   - In `Rashomon.azw3`, the skeleton HTML contains:
     ```html
     <body aid="0" class="calibre"><div aid="1" class="calibre1"></div></body>
     ```
   - In our previous `skelTemplate`, the skeleton was bare:
     ```html
     <html><head><link rel="stylesheet" .../></head><body></body></html>
     ```
2. **XPath Anchor Resolution in `FragmentLoader`**:
   - `libWebCoreKRF.so`'s `FragmentLoader` evaluates the CNCX XPath selector `P-//*[@aid='...']` against the skeleton DOM to locate where the incoming fragment HTML must be inserted.
   - Because the previous skeleton had an empty `<body>`, `//*[@aid='...']` matched zero DOM nodes. `FragmentLoader` could not find the target anchor, leaving the skeleton body completely empty.
3. **The Applied Fix**:
   - Updated section skeleton generation to inject the matching AID container:
     ```java
     String aid = String.format(Locale.US, "%04X", i + 1);
     String skel = "<html><head><link rel=\"stylesheet\" type=\"text/css\" href=\"kindle:flow:0001?mime=text/css\"/></head><body aid=\"0\"><div aid=\"" + aid + "\"></div></body></html>\n";
     int insertPos = skelStart + skel.indexOf("</div>");
     ```
   - Recompiled `classes9.dex` (56,932 bytes) and redeployed `com.amazon.kindle.epub`.
   - Result: `FragmentLoader` immediately located `<div aid="...">` and inserted the HTML DOM fragments. `kindle_exam_v3.epub` rendered with 75,808 content pixels and `kindle_tables_images_exam.epub` rendered with 203,765 rich content pixels!

---

## 3. On-Device Verification Evidence

### 1. KF8 Logcat Output (`logs/logcat_kf8_round009.txt`)
```text
11-21 00:47:11.747 20004 20004 I AmazonKindle.l60.d: [main]: Content opened (asin: B0B164901B, sample: false,
    title: Kindle Render Exam v3, downloadState: LOCAL, content type: BOOK,
    MIME type: application/x-mobi8-ebook, format: MOBI8, fixed layout: false,
    content language: en, content class: DEFAULT)
```

### 2. Native Slide-Out TOC Drawer & Chapter Jump
- Opened native TOC drawer via Kindle top toolbar button `com.amazon.kindle.epub:id/menuitem_hamburger` at `(207, 167)`.
- Verified all 16 chapters loaded cleanly in the native drawer (`screenshots/verify_kf8_native_toc.png`).
- Tapped `E06 Headings and Blocks` at `(495, 1263)`.
- Reader jumped instantly to `Location 252 of 615 • 41%`, rendering 583,653 content pixels with full CSS3 typography and headings (`screenshots/verify_kf8_chapter_jump.png`).

### 3. Active Engine State
- Flag `/sdcard/kindle/.use_kf8` remains active on the device.
- Full KF8 rendering (`libWebCoreKRF.so`) is verified working end-to-end.

---

## 4. Deliverables in Archive `antig_to_gemini_neon_clunky_report009.zip`

- `REPORT.md` (This document)
- `scripts/`:
  - `EpubTranscoder.java` (Round 009 transcoder with DOM Fragment anchor fix)
  - `SafeHttp.java` (In-app DoH DNS fallback)
  - `GutendexSource.java`, `OpdsSource.java`, `OpenCatalogView.java`
  - `clone_and_deploy_epub_mod.sh`, `compile_mod_dex.sh`
- `screenshots/`:
  - `verify_kf8_exam_v3.png` (CSS3 typography & headings under KF8)
  - `verify_kf8_tables_images.png` (CSS3 tables & embedded images under KF8)
  - `verify_kf8_native_toc.png` (Native slide-out TOC drawer under KF8)
  - `verify_kf8_chapter_jump.png` (Jumped to E06 at Loc 252 41% with rich text rendering)
- `logs/`:
  - `logcat_kf8_round009.txt` (Filtered logcat capture of Round 009 KF8 execution)
  - `kf8_rashomon_flow_diff.txt` (Exhaustive Flow 0, SKEL, and FRAG binary diff)
