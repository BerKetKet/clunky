# Round 025 Engineering Report — Native KFX Table Columns/Borders Fixed + Cover Architecture Blueprint for Folio v2.2

**Package Under Test:** `com.amazon.kindle.epub` ("Folio v2.2 KFX")  
**Target Device:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 / API 33, Magisk Root)  
**Safety Invariant Verification:** Stock `com.amazon.kindle` verified **100% UNTOUCHED and READ-ONLY** (`versionName=8.160.1.0(2.0.124621.0)`, `lastUpdateTime=2026-11-19 21:59:23`).  

---

## 1. Executive Summary & Root Cause Resolutions

In Round 025, we addressed the two visual issues identified in Round 024:
1. **Task A (Native KFX Table Columns & Borders)**: Fixed cell concatenation and missing columns in Rust `libkfxconv.so`. Tables in `kindle_tables_images_exam.epub` now render in native `KRIFDocViewer` with distinct aligned columns, cell padding, borders, and pan/zoom chevrons (`verify_r25_table_columns.png`).
2. **Task B (Cover Handling & Architectural Direction)**: Rather than continuing fragile Ion container hacks inside the KFX spine to combat reader margins, we have adopted the user's architectural direction for covers:
   - **User Directive**: Extract the cover image during EPUB parsing, remove the cover page from the book spine entirely, and display the cover separately in its own dedicated View/Activity on `#000000` black upon opening the book.
   - Current cover screenshots captured as baseline: `verify_r25_cover_book1.png` (Hesiod) and `verify_r25_cover_book2.png` (Sophocles).
3. **Task C (Build & On-Device Verification)**:
   - Cross-compiled `kfx-conv` for `aarch64-linux-android` with NDK Clang into `07_APK_EPUB_INGESTION_MOD/lib/arm64-v8a/libkfxconv.so` (4.4 MB stripped).
   - Rebuilt, signed, and deployed Folio v2.2 APK; staged signed APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v2.2_kfx/base.apk` (131 MB).
   - Verified live `Aa → Font → Spacing` sliders in `StandAloneBookReaderActivity` (`verify_r25_prose_spacing_before.png` and `verify_r25_prose_spacing_after.png`).

---

## 2. Deep-Diff Analysis & Fixes for Native KFX Tables (Task A)

### Root Cause of Mashed Cells in Round 024
In Round 024 (`verify_r24_kfx_native_tables.png`), table cells rendered concatenated (e.g. `Gannet992.1Indigo466.1Kestrel483.9`). Deep-diffing against stock Kindle's unencrypted `CR!6KYWZNKF153BK4YSK5MTFZ1XN6ZA.kfx` revealed:
1. **Missing `$152` (`column_format`)**: Stock Amazon KFX tables (`$278`) require `$152` containing a list of structs per column (`[{$698: false}, ...]`, where `$698` is `is_empty`). Without `$152`, `KRIFDocViewer` ignores column boundaries.
2. **Missing `$454` (`body`) Container**: In stock KFX, table rows (`$279`) are not direct children of `$278` under `$146`; they must be wrapped inside a `$454` (`body`) container (`{$155: body_eid, $159: $454, $146: [rows...]}`).
3. **Table Border Collapse & Units**: `$150` (`table_border_collapse`) must be `true`. Border spacing (`$456`, `$457`) must use unit `$318` (`pt`) with decimal `0.9` (not percent).
4. **Cell Padding & Border Color**: In `styles.rs`, `$83` (`border_color: 4286611584`) was added to table styles, and cell padding was aligned to stock values (`$53` padding left, `$55` padding right).

### Verification Evidence (`verify_r25_table_columns.png`)
On-device verification of `T1 Column counts` (6-column table):
- **Before (R24)**: `Gannet992.1Indigo466.1Kestrel483.9` (mashed text)
- **After (R25)**:
  ```
  Amber    364.4    Cobalt    123.6    Ember      848.2
  Gannet   992.1    Indigo    466.1    Kestrel    483.9
  Marigold  86.7    Osprey    103.0    Quince     343.0
  Sorrel   265.2    Umber     828.2    Willow     162.1
  ```
All columns are cleanly separated, horizontally aligned, and readable.

---

## 3. Cover Rendering: Architectural Direction from User (Task B)

### Architectural Analysis & User Guidance
Attempting to force the cover to display edge-to-edge as part of the book's KFX spine creates persistent layout and margin friction with `KRIFDocViewer`'s reflow engine:
- If rendered reflowable, reader margins (left/right/top padding) prevent full bleed.
- In-spine cover wrappers (such as `<svg><image .../></svg>` in Delphi Classics) frequently produce duplicate title pages.

**The User's Prescribed Solution**:
> "When parsing the EPUB for the first time and converting it, remove the cover page and take the image for the cover. It is then processed separately and kept in its own class or whatever and inserted when displaying the book separately."

### Actionable Blueprint for Round 026
1. In `kfx_converter` (`src/epub/mod.rs` & `xhtml.rs`):
   - Identify the cover item from OPF metadata (`<meta name="cover" ...>` or properties `cover-image`).
   - Extract the raw cover image binary directly to `<book>.cover.jpg`.
   - Strip the cover HTML document from the EPUB spine so the converted KFX book begins cleanly at the first content chapter (Location 1).
2. In Folio Android app (`CoverOverlayView` / Reader Activity):
   - Intercept book opening in `StandAloneBookReaderActivity`.
   - If opening at Location 1 (or cover landmark), display the cached cover image full-screen edge-to-edge on pure `#000000` black before transitioning into the reader text.

Screenshots captured of current state:
- `screenshots/verify_r25_cover_book1.png` (Hesiod)
- `screenshots/verify_r25_cover_book2.png` (Sophocles)

---

## 4. On-Device Verification Artifacts (Task C)

| Screenshot | Description | Verification Status |
|---|---|---|
| `screenshots/verify_r25_table_columns.png` | 6-column table in `kindle_tables_images_exam.epub` showing distinct aligned columns | **PASSED** (no mashed text) |
| `screenshots/verify_r25_prose_spacing_before.png` | Chapter 1 prose before moving spacing sliders | **PASSED** |
| `screenshots/verify_r25_prose_spacing_after.png` | Chapter 1 prose after adjusting line/paragraph spacing sliders | **PASSED** |
| `screenshots/verify_r25_cover_book1.png` | Hesiod cover reading position | **CAPTURED** |
| `screenshots/verify_r25_cover_book2.png` | Sophocles cover reading position | **CAPTURED** |

**Staged APK**: `07_APK_EPUB_INGESTION_MOD/release/folio_v2.2_kfx/base.apk` (131 MB).  
**Stock Kindle Integrity**: Verified untouched (`8.160.1.0`, `lastUpdateTime=2026-11-19 21:59:23`).
