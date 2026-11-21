# Round 025 Instructions — Visual Audit Fixes for Rust `libkfxconv.so`: True Native KFX Fullscreen Black Cover + Proper KFX Table Columns/Borders (Folio v2.2)

**Target Package:** `com.amazon.kindle.epub` ("Folio v2.2 KFX")  
**HARD SAFETY INVARIANT:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (you may read-only inspect its unencrypted `.kfx` files for Ion schema comparison, NEVER write, modify, clear data, force-stop, or uninstall stock `com.amazon.kindle`).  
**Autonomous Relay Mode:** Execute all tasks end-to-end and upload `antig_to_gemini_neon_clunky_report025.zip` (`< 15 MB`, verify `.zip` preview chip attached!) directly to this chat via Chrome Dev Bridge without pausing.

---

## Round 024 Visual Inspection Findings (Awesome Progress + 2 Exact Bugs Caught!)

First, awesome work fixing the Chrome Dev Bridge attachment—`report024.zip` (1.8 MB) attached cleanly! And `verify_r24_exam_v3_images_hyphen.png` + `verify_r24_kfx_images.png` look **razor-sharp** with real native Amazon hyphenation (`an- / wered`, `post- / poned`, `wil- / low`) and crisp embedded figures!

Visual inspection of the screenshots caught **2 exact bugs in `libkfxconv.so`** that we can fix cleanly in Round 025 by diffing directly against stock Kindle's unencrypted KFX files (read-only):

1. **Bug 1 — Table cells mashed together without columns (`verify_r24_kfx_native_tables.png`)**:
   - In `verify_r24_kfx_native_tables.png`, table rows rendered as concatenated strings (`Gannet992.1Indigo466.1Kestrel483.9`) instead of separate aligned columns with cell spacing/borders!
   - Cause: Missing `$280` (`table_body`) / `$281` (`table_header`) wrapper OR incorrect Ion symbol ID / cell `$157` style (`padding`, `border`, `column_format`) in `storylines.rs`.
2. **Bug 2 — Cover rendered as a small top-left image on a white page (`verify_r24_classic_cover.png`)**:
   - In `verify_r24_classic_cover.png`, Hesiod's cover rendered as a small thumbnail in the top-left corner of a **white** page (`Location 2 of 1551 • 0%`), NOT full-width centered on `#000000` black!
   - By contrast, in Round 021 (`kfx_poc_continuous_scrolling_enabled.png`), stock Kindle's unencrypted KFX opened in Folio's `KRIFDocViewer` rendered the cover **edge-to-edge on pure `#000000` black** natively!

---

## Tasks

### Task A — Fix Native KFX Table Columns & Borders in Rust `libkfxconv.so`
1. Inspect the exact Ion table structure from:
   - Stock Kindle's unencrypted `CR!6KYWZNKF153BK4YSK5MTFZ1XN6ZA.kfx` (`kindle_tables_images_exam`, read-only!) AND
   - `jloutsch/kfxgen/plugin/kfxgen/native_generator.py` (table emission section).
2. Log the exact Ion table hierarchy (`table` → `table_body`/`table_header` → `table_row` → `table_cell` symbol IDs + `$157` cell padding/border/width styles) in `logs/r25_kfx_table_and_cover_diff.txt`.
3. Update `kfx_converter/src/packager/storylines.rs` and `styles.rs` to match that exact Ion table hierarchy so cells render in distinct, properly padded/bordered columns with the native horizontal pan chevron on wide tables!

### Task B — Fix Native KFX Fullscreen `#000000` Black Cover in Rust `libkfxconv.so`
1. Inspect (read-only!) the exact Cover section Ion entities (`$260` section, `$259` storyline, `$157` style, `$270`/`$271` page_template with `fill_color` / `scale_fit`, `$495` `cover_image`, `$389` landmarks) inside stock Kindle's unencrypted `CR!ZTD3FX31BX225B7KRMNC6MN5NFHZ.kfx` or `CR!AE2V9HFM2X0SDF9K7NY5XFFST45T.kfx`!
2. Also check why Hesiod's cover HTML wasn't stripped cleanly (handle `<svg><image .../></svg>` and image-only cover wrappers across sections 0..1 without any title-specific hardcoding!).
3. Update `kfx_converter` so EVERY EPUB with a cover gets the exact stock-Kindle KFX cover section structure (`KRIFDocViewer` renders it full-width centered on pure `#000000` black natively, zero white page background, zero tiny top-left thumbnail!).

### Task C — Cross-Compile `libkfxconv.so`, Deploy Folio v2.2 & Capture Full-Screen Reading Screenshots
1. Cross-compile `kfx_converter` for `aarch64-linux-android` (`lib/arm64-v8a/libkfxconv.so`), rebuild & deploy **Folio v2.2** (`com.amazon.kindle.epub`), and confirm stock `com.amazon.kindle` is 100% untouched.
2. Clear `epub_cache/` so test books re-transcode with the fixed `libkfxconv.so`.
3. Capture **full-screen normal reading screenshots** (NOT zoomed-out Page Flip thumbnails!):
   - **`screenshots/verify_r25_table_columns.png`**: Full-screen `T1 Column counts` (including the 6-column table) in `kindle_tables_images_exam.epub` showing clean distinct columns, headers, and borders (NO mashed `Gannet992.1Indigo...`!).
   - **`screenshots/verify_r25_cover_book1.png`** (e.g. Hesiod) and **`screenshots/verify_r25_cover_book2.png`** (e.g. Sophocles or Alice or Moby Dick): BOTH showing full-width centered cover on pure `#000000` black natively in `KRIFDocViewer` (NO small top-left image on white!).
   - **`screenshots/verify_r25_prose_spacing_before.png`** and **`screenshots/verify_r25_prose_spacing_after.png`**: Full-screen Chapter 1 prose before & after moving `Aa → Font → Spacing` sliders!
4. Stage signed **Folio v2.2** APK in `07_APK_EPUB_INGESTION_MOD/release/folio_v2.2_kfx/`.

### Task D — Package Lean Report (`< 15 MB`) & Verify Attachment Chip Before Sending
Build `antig_to_gemini_neon_clunky_report025.zip` (`< 15 MB`, Round 025 files only), verify the `.zip` attachment chip in the composer, and upload via Chrome Dev Bridge!
