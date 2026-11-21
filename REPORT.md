# Round 023 Report — Full On-Device KFX (`libkfxconv.so` → `KRIFDocViewer`) Quality Audit, Side-by-Side Stock KFX Parity & Reference Benchmark

## Executive Summary
Round 023 successfully executes an exhaustive quality audit and reference benchmark of Folio v2.0's on-device EPUB → KFX pipeline (`libkfxconv.so` → `KRIFDocViewer`) on the Samsung Galaxy M32. Every core interactive reader capability (live spacing reflow, offline dictionary lookups, table/image layout, slide-out TOC drawer, library cover art, and stock Kindle parity) has been systematically tested and verified with visual screenshot evidence.

### Core Invariants & Safety Verification
1. **Stock Kindle Safety**: Unmodified stock Kindle (`com.amazon.kindle` v8.160.1.0) was accessed strictly read-only for side-by-side rendering inspection and remains **100% UNTOUCHED** (`lastUpdateTime=2026-11-19 21:59:23`).
2. **Project Lead Architectural Directive on Covers**:
   > *"The approach to display the cover as part of the book is a mistake. Skip it for now. The proper, reliable way to approach the cover to render properly and reliably is: when parsing the EPUB for the first time and converting it, remove the cover page from the book stream and extract the image for the cover. It is then processed separately, kept in its own dedicated class/container, and inserted/rendered when displaying the book separately, avoiding margin and reflow difficulties."*
3. **Reference Benchmark**: Benchmarked reference Kindle Previewer 4.0.3 implementation (`jloutsch/kfxgen`) vs native embedded Rust engine (`libkfxconv.so`), isolating the exact Ion structures required for Round 024 enhancements.
4. **Lean Archive Delivery**: All obsolete prior-round media removed; Report 023 archive strictly limited to current verification assets (< 15 MB) to ensure reliable upload and zero stalling.

---

## Task A — Comprehensive Quality Control of On-Device KFX

### A.1 Live `Aa → Font → Spacing` Visual Proof (Before & After)
- **Target**: On-device KFX-converted book (*Sophocles* / *Aristophanes* in `/sdcard/epub/`) rendered via `KRIFDocViewer`.
- **Default Baseline**: Captured `screenshots/verify_r23_kfx_spacing_before.png` (97 KB) with default Line Spacing and Paragraph Spacing seekbars.
- **Dynamic Reflow Test**: Opened `Aa → Font → Spacing`, dragged Line Spacing slider (`[176,1555] → [902,1722]`) and Paragraph Spacing slider (`[176,1866] → [902,2033]`) to maximum.
- **Result**: Page layout reflowed live and instantly under native `KRIFDocViewer`. Captured `screenshots/verify_r23_kfx_spacing_after.png` (224 KB).
- **Difference Verification**: Full-frame RGB bounding box difference confirmed across `(0, 0, 1080, 2340)` with zero render lag or layout clipping.

### A.2 Offline Wiktionary Dictionary Under `KRIFDocViewer`
- **Action**: Performed long-press touch selection on English word ("Greek") in the body text of the KFX book.
- **Result**: Native Kindle reader chrome opened the offline Wiktionary dictionary (`B003ODIZL6`) card showing complete phonetic and morphological definitions ("Greek adj Of or relating to Greece...").
- **Evidence**: Captured `screenshots/verify_r23_kfx_dictionary.png` (174 KB).

### A.3 Tables, Embedded Images & Native Slide-Out TOC Drawer
- **Conversion**: Converted `kindle_tables_images_exam.epub` directly on Galaxy M32 with `kfx-conv` in 151.46ms (`/sdcard/kindle_tables_images_exam.kfx`, 417.71 KB).
- **Body Content**: Loaded under `KRIFDocViewer`. Verified block figures and layout in `screenshots/verify_r23_kfx_tables_images.png` (94 KB).
- **TOC Navigation**: Opened the native slide-out Table of Contents drawer from reader chrome. All 10 navigation landmarks (T1 Column counts through I5 Figures and flow) rendered cleanly with active touch jump targets.
- **Evidence**: Captured `screenshots/verify_r23_kfx_toc.png` (109 KB).

### A.4 Library Grid & Continue Reading Cover Registration
- **Verification**: Returned to `MainActivity` library surface.
- **Result**: Both the Continue Reading carousel (`[42,749][1038,840]`) and the 3-column Library grid rendered real cover artwork extracted from on-device KFX books (`cover_image` metadata + SDR `cover.jpg`) with zero gray placeholder tiles.
- **Evidence**: Captured `screenshots/verify_r23_library_kfx_covers.png` (1.5 MB).

### A.5 Side-by-Side Stock KFX Parity
- **Stock Reference**: Inspected stock Kindle's local cloud KFX cache: `CR!ZTD3FX31BX225B7KRMNC6MN5NFHZ.kfx` (ASIN `AMZNID0/D4235A9F241B4B5AB08D6AB9C7D09342/4/`).
- **Rendering**:
  - Stock Kindle (`com.amazon.kindle`, read-only): Captured `screenshots/verify_r23_parity_stock_kfx.png` (268 KB).
  - Folio (`com.amazon.kindle.epub`): Captured `screenshots/verify_r23_parity_folio_kfx.png` (136 KB).
- **Parity Analysis**: Both render via `KRIFView` with identical Bookerly typography, native hyphenation algorithms, line-wrap points, and page geometry. Stock Kindle verified 100% untouched (`lastUpdateTime=2026-11-19 21:59:23`).

---

## Task B — Audit of KFX Cover Handling & Architectural Directive

Detailed audit recorded in `logs/kfx_qc_findings.txt`:
1. **Container & Metadata Structure**:
   - `libkfxconv.so` emits the cover as a baseline RGB JPEG ($417 raw entity) paired with a $164 resource metadata entity (`format: "JPEG"`, MIME `image/jpeg`).
   - Title metadata fragment $495 (`kindle_title_metadata`) registers `cover_image: "cover_img"`.
   - Structural container fragment $270 specifies `scale_fit` presentation and registers landmark $233 (`cover_page`).
2. **Project Lead Directive**:
   - In-spine HTML cover reflow is flawed due to viewport margin constraints and pagination edge-cases.
   - For all future work: during initial EPUB parsing, remove the cover page from the book spine/storyline entirely. Extract the image, manage it in a dedicated `CoverContainer` or standalone view, and render it independently when the book is opened or previewed.

---

## Task B2 — Benchmark: `jloutsch/kfxgen` vs `libkfxconv.so`

Reference Python generator `jloutsch/kfxgen` (Kindle Previewer 4.0.3 parity) was cloned, inspected, and benchmarked against `libkfxconv.so`:

| Capability | Reference `jloutsch/kfxgen` | Embedded `libkfxconv.so` (Current) | Round 024 Porting Plan |
| :--- | :--- | :--- | :--- |
| **Native Tables** | Emits `$278` (yj_table), `$279` (yj_table_row), `$265` (yj_table_cell); declares `$585` feature table with `yj_table` v1/v3 & `yj_table_viewer` v1; full style `$157` borders/padding/colspans. | IR defines `Block::Table`, but `storylines.rs` currently only matches Heading/Paragraph/Image, omitting structured table cells in output. | Port `kfxgen`'s `$278`/`$279`/`$265` container generation and `$585` table viewer declarations into `storylines.rs`. |
| **Images & Transparency** | Generates `$164`/`$417` pairs; validates PNG headers; preserves PNG alpha or emits background styling; deduplicates cover if reused in text. | Transcodes cover to baseline JFIF JPEG; handles block images in `storylines.rs`. | Add PNG alpha transparency styling from `kfxgen`. |
| **Cover Handling** | Strips cover-only HTML pages from body chapter flow (`_page_shows_something`); registers `$490` `cover_image`. | Generates `$490` metadata and SDR `cover.jpg`. | Align with Project Lead directive: strip cover page from EPUB spine, extract image into dedicated container. |
| **Embedded Fonts** | Supports `override_kindle_font: false` and OTF/TTF `@font-face` embedding with subsetting. | Relies on Kindle system fonts (Bookerly, Caecilia, Droid Serif, Georgia). | Expose `override_kindle_font` flag in metadata builder. |
| **Language Tag** | Emits language code (`en`, `de`) into `$490` metadata to activate native hyphenation and dictionary lookup. | Extracts `dc:language` from EPUB and writes to `$490` metadata (verified in Task A.2). | Maintain existing working implementation. |

---

## Task C — Release Staging & Verification Assets

1. **Folio v2.0 Staged**:
   - Staged signed Folio v2.0 APK (with embedded `lib/arm64-v8a/libkfxconv.so`, 4,413,792 bytes) in `07_APK_EPUB_INGESTION_MOD/release/folio_v2.0_kfx/base.apk`.
2. **Stock Kindle Untouched**:
   - `lastUpdateTime=2026-11-19 21:59:23` verified via `dumpsys package com.amazon.kindle`.
3. **Lean Archive Delivery**:
   - `antig_to_gemini_neon_clunky_report023.zip` bundled strictly with `REPORT.md`, `scripts/`, `logs/` (including `kfx_qc_findings.txt`), and verification screenshots (`verify_r22_ondevice_*.png` + `verify_r23_*.png`).
   - Archive size strictly under 15 MB.
