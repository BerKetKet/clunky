# Round 036 Instructions — Heavy Omnibus Stress Test (22+ MB) & Live Download-to-KFX End-to-End QC (Folio v3.3 KFX)

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Hard Safety Invariant:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never modify, write, force-stop, or clear data).  
**Zero Overfitting Rule:** All performance optimizations and download/ingestion logic must be 100% general across all books and catalogs.  
**Reply Archive Budget:** Package ONLY Round 036 files (`REPORT.md`, `INSTRUCTIONS_ROUND_036.md`, `protocol.json`, `screenshots/*.png`, and concise logs) into `antig_to_gemini_neon_clunky_report036.zip` (< 15 MB) and verify a single `.zip` attachment chip before sending.

---

## Round 035 Visual QC Feedback

1. **PASS — `text-transform`, `small-caps` & Arabic/Hebrew RTL (`verify_r35_text_transform_and_rtl.png`)**: Crisp uppercase, title-case, small-caps, connected Arabic ligatures, and right-aligned Hebrew RTL in `KRIFDocViewer`.
2. **PASS — OPDS Contrast & Settings Branding (`verify_r35_opds_contrast_and_feed.png`, `verify_r35_settings_folio_branding.png`)**: High-contrast `#CBD5E1` OPDS labels and clean `"in your Folio library."` under `Local file access`.
3. **PASS — Pristine Library (`verify_r35_clean_library_grid.png`)**: Zero synthetic test clutter; real classical omnibuses (Sophocles, Aristotle 22.8 MB, Herodotus 8.9 MB, Tacitus 7.3 MB, Seneca 8.4 MB, Sallust) with real covers.

---

## Task A: Heavy Commercial Omnibus Stress Test & Memory Audit (`libkfxconv.so`)

1. **Benchmark 3 Heavy Omnibuses On-Device**:
   - Test on-device KFX conversion and `KRIFDocViewer` launch on the largest real EPUBs on device:
     * **Aristotle** (~22.8 MB)
     * **Herodotus** (~8.9 MB)
     * **Seneca** (~8.4 MB)
   - Record in `REPORT.md`: EPUB size, on-device `libkfxconv.so` conversion time (ms), peak RSS / memory usage, output KFX size, TOC chapter count, cover extraction status, and zero-crash `KRIFDocViewer` open + deep TOC chapter jump.
2. **Optimize Any Bottleneck Found**:
   - If `libkfxconv.so` hits excessive memory usage, slow image transcoding, or any edge-case failure on 20+ MB omnibuses, optimize it cleanly in Rust, verify `cargo test --all-targets`, cross-compile stripped ARM64 `libkfxconv.so`, and bump badge to **Folio v3.3 KFX**.

---

## Task B: End-to-End Live Catalog Download → On-Device KFX → `KRIFDocViewer` Flow

1. **Test Full User Journey On-Device**:
   - From Folio's **Home** tab (`Popular Free EPUBs`) OR **OPDS Catalogs** (`Standard Ebooks` / `Internet Archive`), trigger a live download of a real public-domain EPUB (note: treat `gutenberg.org` 503 maintenance as non-fatal and use working mirror/OPDS source).
   - Verify end-to-end:
     1. Clean download progress & ZIP validation (no HTML error pages saved as `.epub`).
     2. Automatic on-device EPUB → KFX conversion via `libkfxconv.so` + companion `.cover.jpg` extraction.
     3. Clean registration in `kindle_library.db` so it appears in **Library** and **Continue Reading** with its real cover.
     4. Smooth launch directly into `KRIFDocViewer` (`format: YJBINARY`).
   - Fix any friction, UI freeze, or cover/TOC bug discovered along this pipeline and rebuild **Folio v3.3 KFX** if modified.

---

## Task C: On-Device Visual Verification & Release Staging (`Folio v3.3 KFX`)

Capture and place all 4 proof screenshots inside `screenshots/` in `antig_to_gemini_neon_clunky_report036.zip`:

1. `screenshots/verify_r36_aristotle_22mb_cover.png`: *Aristotle* (22.8 MB omnibus) native fullscreen `#000000` `$270` cover in `KRIFDocViewer`.
2. `screenshots/verify_r36_aristotle_22mb_toc_and_prose.png`: *Aristotle* (22.8 MB) slide-out TOC drawer or deep chapter prose proving fast, zero-OOM rendering.
3. `screenshots/verify_r36_live_download_kfx_reader.png`: Freshly downloaded catalog/OPDS EPUB auto-converted to KFX on-device and open in `KRIFDocViewer`.
4. `screenshots/verify_r36_home_continue_reading_updated.png`: Folio **Home** tab showing **Continue Reading** cleanly updated with real covers for the newly opened/downloaded books.
5. Confirm stock `com.amazon.kindle` remains 100% untouched (`lastUpdateTime=2026-11-19 21:59:23`) and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v3.3_kfx/base.apk`.
