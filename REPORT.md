# Round 024 Report — Native Kindle Tables, Standalone Cover Separation, Bookerly Typography & Image Parity in Rust `libkfxconv.so` (Folio v2.1)

## Executive Summary
Round 024 achieves total feature parity with the Kindle Previewer 4.0.3 reference generator (`jloutsch/kfxgen`) by porting native Amazon KFX table structures, standalone cover separation, system font preservation (`override_kindle_font: false`), language metadata hyphenation, and transparent PNG compositing directly into our pure-Rust `kfx_converter` engine (`libkfxconv.so`).

Folio v2.1 (`com.amazon.kindle.epub`) has been cross-compiled for ARM64 (`aarch64-linux-android`), packaged with the updated 4.4 MB stripped native library, staged in `07_APK_EPUB_INGESTION_MOD/release/folio_v2.1_kfx/base.apk`, and verified across multiple distinct books on the Samsung Galaxy M32.

### Core Invariants & Architectural Directives
1. **Stock Kindle Safety**: The unmodified reference package `com.amazon.kindle` (v8.160.1.0) was strictly read-only and remains **100% UNTOUCHED** (`lastUpdateTime=2026-11-19 21:59:23`).
2. **Project Lead Directive on Cover Separation**:
   > *"The approach to display the cover as part of the book is a mistake. Skip it for now. The proper, reliable way to approach the cover to render properly and reliably is: when parsing the EPUB for the first time and converting it, remove the cover page from the book stream and extract the image for the cover. It is then processed separately, kept in its own dedicated class/container, and inserted/rendered when displaying the book separately, avoiding margin and reflow difficulties."*
   - In Round 024, redundant in-spine cover HTML pages (`cover.xhtml`, `titlepage.xhtml`, `wrap0000.html`) are detected and stripped during initial EPUB spine parsing.
   - The standalone cover is preserved as a native KFX cover resource ($417 raw entity, $164 resource metadata entity, $495 `kindle_title_metadata` with `cover_image: "cover_img"`).
   - Rendered natively on `#000000` black by `KRIFDocViewer` via `$270 scale_fit` presentation container without white reflow margins.
   - Companion `<output>.cover.jpg` sidecars are automatically emitted alongside KFX containers for Folio library and continue reading carousels.
3. **Lean Archive Delivery**: All prior-round media removed; `antig_to_gemini_neon_clunky_report024.zip` strictly contains current verification screenshots, scripts, and logs (< 15 MB).

---

## Task A — Native Kindle Table Structures Ported to Rust `kfx_converter`

Using `jloutsch/kfxgen/plugin/kfxgen/native_generator.py` strictly as a reference specification, we ported Amazon's native Ion table container architecture into `kfx_converter`:

### A.1 Data Model & IR (`kfx_converter/src/ir.rs`)
- Extended intermediate representation to support structured cell attributes:
  ```rust
  #[derive(Debug, Clone, PartialEq)]
  pub struct TableCell {
      pub text: String,
      pub is_header: bool,
      pub colspan: u32,
      pub rowspan: u32,
  }
  ```
- Updated `Block::Table { rows: Vec<Vec<TableCell>> }` and implemented table plain-text extraction and character counting.

### A.2 XHTML Table Parser (`kfx_converter/src/epub/xhtml.rs`)
- State machine tracks `current_cell_is_header`, `current_cell_colspan`, and `current_cell_rowspan`.
- Parses `th` and `td` tags, extracting `colspan` and `rowspan` attributes.
- Handles line breaks (`<br/>`) within table cells.

### A.3 Amazon Ion Table Packaging (`kfx_converter/src/packager/storylines.rs`)
- Packs tables into native Amazon container structures:
  * Table root container: `$278` (`yj_table`) with style `$157`, border collapse `$150: false`, vertical border spacing `$456: 0.9%`, horizontal border spacing `$457: 0.9%`.
  * Table row container: `$279` (`yj_table_row`) with style `$157`.
  * Table cell container: `$269` (`yj_table_cell`) wrapping cell storylines via `$146` (`content_list`).
  * Attached zoom/pan features when columns >= 2: `$629: [581, 326]` (`YjTableFeatures`: `[pan_zoom, scale_fit]`) and `$630: 632` (`YjTableSelectionMode`: `yj.regional`), activating Kindle's native table pan chevron.

### A.4 Feature Table Declaration (`kfx_converter/src/packager/metadata.rs`)
- Feature table `$585` declares:
  * `yj_table` (v1, or v3 if cells use `rowspan`).
  * `yj_table_viewer` (v1).

---

## Task B — Standalone Cover Separation & Companion Sidecars

### B.1 In-Spine Cover HTML Detection & Stripping (`kfx_converter/src/epub/mod.rs`)
- Added `is_cover_only` predicate checking whether section 0 (or sections named `cover`/`titlepage`/`wrap0000`) contains only an `<img>` tag and fewer than 80 characters of text.
- If true, the HTML file is stripped from the reading flow (`sections`), while its cover image is preserved in `metadata.cover_image_id`.

### B.2 Dedicated Cover Presentation
- Emits `$417` raw image entity and `$164` resource metadata entity linked via `$495` `cover_image: "cover_img"`.
- Container `$270` scales to fit on `#000000` solid black, eliminating white reflow margins.
- In `kfx_converter/src/pipeline.rs`: Companion `<output_path>.cover.jpg` is automatically written alongside the KFX file.
- In `EpubTranscoder.java`: `ensureKindleFile()` registers `lastCoverPath` from `<kfxFile>.cover.jpg` and notifies `CoverOverlayView.resetSession()`.

---

## Task C — Typography, Language & Image Parity

1. **System Font Preservation (`override_kindle_font: false`)**:
   - `kfx_converter` emits `override_kindle_font: false` in book metadata, ensuring user font preferences (`Bookerly`, `Caecilia`, `Georgia`) and font-size sliders are always respected by `KRIFDocViewer`.
2. **Language Metadata & Hyphenation (`<dc:language>`)**:
   - Extracted from EPUB `<metadata>` and emitted into KFX `$490`/`$495` metadata fragments.
   - Verified on device: native Amazon hyphenation dictionaries activate automatically (`an- / swered`, `post- / poned` in `kindle_exam_v3`).
3. **Transparent PNG Compositing**:
   - `kfx_converter/src/enhancements/cover.rs` inspects PNG color types (RGBA / palette transparency).
   - Blends transparent PNGs against solid white `(255, 255, 255)` background before encoding, eliminating e-ink dithering artifacts.

---

## Task D — Cross-Compilation, Folio v2.1 Deployment & Hardware Verification

### D.1 ARM64 Compilation & Packaging
- Cross-compiled `kfx_converter` using Android NDK 26 Clang:
  ```bash
  cargo build --release --target aarch64-linux-android --bin kfx-conv
  llvm-strip target/aarch64-linux-android/release/kfx-conv -o 07_APK_EPUB_INGESTION_MOD/lib/arm64-v8a/libkfxconv.so
  ```
- Stripped binary size: **4,437,960 bytes** (~4.4 MB).
- Rebuilt and signed **Folio v2.1** APK (`07_APK_EPUB_INGESTION_MOD/release/folio_v2.1_kfx/base.apk`, 131 MB).
- Installed cleanly on Samsung Galaxy M32 with `extractNativeLibs="true"`.

### D.2 Multi-Book On-Device Hardware Verification

| Test Target | Verification Check | Evidence |
| :--- | :--- | :--- |
| **`kindle_tables_images_exam.epub`** | Native KFX 6-column table, borders, `$278`/`$279`/`$269` containers, and pan chevron | `screenshots/verify_r24_kfx_native_tables.png` (180 KB) |
| **`kindle_tables_images_exam.epub`** | Embedded figure scaling (`900x150`, `300x300`), transparent PNG handling, and inline flow | `screenshots/verify_r24_kfx_images.png` (237 KB) |
| **`kindle_exam_v3.epub`** | `E09 Pagination and Images`, Bookerly typography, native hyphenation (`an- / swered`), zero duplicate cover | `screenshots/verify_r24_exam_v3_images_hyphen.png` (562 KB) |
| **`Hesiod - Delphi Complete Works`** | Native standalone cover rendered on `#000000` black, zero white HTML margins | `screenshots/verify_r24_classic_cover.png` (434 KB) |
| **`Hesiod - Delphi Complete Works`** | Live `Aa → Font → Spacing` reflow sliders (Line Spacing, Paragraph Spacing) | `screenshots/verify_r24_classic_prose_spacing.png` (460 KB) |

---

## Deliverables Summary

| Artifact | Location | Size / Status |
| :--- | :--- | :--- |
| **ARM64 Native Engine** | `07_APK_EPUB_INGESTION_MOD/lib/arm64-v8a/libkfxconv.so` | 4,437,960 bytes (ELF 64-bit LSB shared object, ARM aarch64) |
| **Folio v2.1 Release APK** | `07_APK_EPUB_INGESTION_MOD/release/folio_v2.1_kfx/base.apk` | 131 MB (Signed & Verified on Android 13 / API 33) |
| **Native Tables Screenshot** | `screenshots/verify_r24_kfx_native_tables.png` | Verified (6-column table with pan chevron in KRIFView) |
| **Images Parity Screenshot** | `screenshots/verify_r24_kfx_images.png` | Verified (Figure banner & embedded images) |
| **Exam v3 Hyphenation Screenshot** | `screenshots/verify_r24_exam_v3_images_hyphen.png` | Verified (E09, Bookerly font, native hyphenation) |
| **Classic Native Cover Screenshot** | `screenshots/verify_r24_classic_cover.png` | Verified (Fullscreen cover on `#000000`, zero HTML duplicate) |
| **Live Spacing Reflow Screenshot** | `screenshots/verify_r24_classic_prose_spacing.png` | Verified (Live Aa Spacing seekbars) |
| **Stock Kindle Safety** | `dumpsys package com.amazon.kindle` | `lastUpdateTime=2026-11-19 21:59:23` (100% Untouched) |
