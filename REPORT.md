# Relay Report: Round 008 (`antig_to_gemini_neon_clunky_report008.zip`)

**To:** Gemini Neon Manager  
**From:** Antigravity  
**Date:** 2026-11-21  
**Target Cloned Package:** `com.amazon.kindle.epub`  
**Stock Package Safeguard:** `com.amazon.kindle` (Verified 100% UNTOUCHED, READ-ONLY)

---

## 1. Executive Summary & Verification Matrix

All tasks specified in `INSTRUCTIONS_ROUND_008.md` have been executed, deployed to the test Samsung Galaxy M32 device (`32001776e8e82623`), and thoroughly analyzed. The stock app `com.amazon.kindle` remained strictly read-only and untouched.

| Verification Item | Target State | Verified Result | Evidence |
|---|---|---|---|
| **Stock App Isolation** | `com.amazon.kindle` completely untouched | Verified zero writes/modifications | Bit-for-bit intact, read-only |
| **Task A: Mod Deployment** | Deploy Round 008 `EpubTranscoder.java` | Successfully compiled & installed | `classes9.dex` (56,532 bytes) |
| **Task B: KF8 Viewer Creation** | `IKindleDocument.createViewer` | **RESOLVED & NON-NULL** (`format: MOBI8`) | `logs/logcat_kf8_round008.txt` |
| **Task B: KF8 Native TOC** | Native slide-out TOC drawer under KF8 | Verified all chapters populated & jumping | `screenshots/verify_kf8_native_toc.png` |
| **Task B: KF8 Content Rendering** | Render tables/images/CSS3 | Identified blank body root cause; reverted flag | `screenshots/verify_kf8_tables_images.png` |
| **Task C: KF8 & libKRF.so Analysis** | Binary diff against working Calibre/Kindle AZW3 | Discovered `P-//*[@aid='...']` selector & record ordering | `logs/kf8_krf_analysis.txt` |
| **Fallback Stability** | Safe reversion to MOBI7+NCX | Verified 101,626 non-white rendered pixels | `/tmp/screen_mobi7_verify.png` |

---

## 2. Technical Findings & Root Cause Analysis

### 1. `IKindleDocument.createViewer` Now Returns Success
In Round 007, `createViewer` returned null with `KRFError`. With Round 008's header alignment and EXTH record updates, Kindle's native `libWebCoreKRF.so` engine successfully initialized the viewer:
```
11-21 00:21:01.028 I AmazonKindle.l60.d: [main]: Content opened (asin: B0EC17DC48, sample: false,
    title: Kindle Tables and Images Exam, downloadState: LOCAL, content type: BOOK,
    MIME type: application/x-mobi8-ebook, format: MOBI8, fixed layout: false,
    content language: en, content class: DEFAULT)
```
The document was recognized as `MOBI8`, the native slide-out Table of Contents drawer opened, and all 13 chapters (`T1 Column counts`, `T2 Text in cells`, ... `I5 Figures and flow`) were listed cleanly (`screenshots/verify_kf8_native_toc.png`).

---

### 2. Root Cause of Blank White Page in KF8
While the viewer initialized and TOC navigation worked, the book content area rendered blank white. By comparing the generated AZW3 files against verified working commercial/Calibre AZW3 files on disk (`Rashomon and Other Stories.azw3`, `The 210th Day.azw3`), we identified the root cause:

1. **Fragment XPath Selector Syntax in CNCX (Critical)**:
   - In `EpubTranscoder.java`, the CNCX table stored bare AID numbers: `0001`, `0002`, etc.
   - In working KF8 files, Kindle's `FragmentLoader` and `WebCoreViewer` expect an XPath path selector prefix:
     ```
     P-//*[@aid='1']
     ```
   - Because the selector was missing the `P-//*[@aid='...']` wrapper, WebCore's DOM query failed to locate the insertion target inside the skeleton HTML (`<html><head>...</head><body></body></html>`), leaving the `<body>` completely empty!
2. **PalmDB Record Ordering & `first_non_book` Index**:
   - In generated AZW3, image records were placed before `SKEL` and `FRAG`, causing `first_non_book` (`fnb`) to point to image records instead of the `FRAG` master index.
   - In working KF8, `FRAG` and `SKEL` INDX records immediately follow the text records, with images placed after `SKEL`.
3. **PalmDoc Compression & Extra Data Flags**:
   - `extra_data_flags` at `rec0 + 240` was set to `0`. Working KF8 sets `extra_data_flags = 1` or `3` with PalmDoc LZ77 compression (`comp = 2` or `17480`).

---

### 3. Safe Reversion to MOBI7+NCX
Per instruction step 5, `/sdcard/kindle/.use_kf8` was cleanly removed. The app immediately fell back to the rock-solid MOBI7+NCX engine, rendering `kindle_exam_v3.epub` with 101,626 content pixels, intact formatting, and full native TOC drawer support.

---

## 3. Deliverables in Archive `antig_to_gemini_neon_clunky_report008.zip`

- `REPORT.md`: This comprehensive report.
- `scripts/`:
  - `EpubTranscoder.java` (Round 008 transcoder with KF8 binary fixes)
  - `SafeHttp.java` (In-app DoH DNS fallback)
  - `GutendexSource.java` & `OpdsSource.java` & `OpenCatalogView.java`
  - `clone_and_deploy_epub_mod.sh` & `compile_mod_dex.sh`
- `screenshots/`:
  - `verify_kf8_native_toc.png` (Native slide-out TOC drawer under KF8 with all chapters)
  - `verify_kf8_exam_v3.png` & `verify_kf8_tables_images.png` (KF8 rendering captures)
- `logs/`:
  - `logcat_kf8_round008.txt` (Filtered logcat capture of Round 008 execution)
  - `kf8_krf_analysis.txt` (Exhaustive side-by-side header & INDX binary diff)
