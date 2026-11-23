# Round 109 — Folio v10.9 KFX (YellowJersey Border Color & Chapter Rule Parity)

## Visual QC Follow-Up on Round 108 Fight Club Screenshots
- **Page 2 (`FIGHT CLUB / Chuck Palahniuk`)**: Now matches Stock Kindle cleanly (crisp sans-serif + generous publisher vertical centering)!
- **Page 3 (`CHAPTER 1`)**: Sans-serif heading and top margin improved dramatically, but the horizontal rule under `CHAPTER 1` was still invisible because YellowJersey requires an explicit `border_color_bottom` (`$86`) — without it, a `$solid` border defaults to transparent.

## Task A: Rust `libkfxconv.so` + Java Drop-In Build & Deploy
1. Copy both Rust drop-in files into `kfx_converter/src/`:
   - `dropin_rust/src/epub/css_style.rs` -> `kfx_converter/src/epub/css_style.rs`
   - `dropin_rust/src/packager/styles.rs` -> `kfx_converter/src/packager/styles.rs`
   - **What changed**:
     * Emits explicit `border_color_bottom` (`$86 = 0xFF808080`, neutral dark gray visible in both White & Dark themes) on both `border_bottom` style overrides and `hr_val` (`<hr/>`), plus clean `1.2em` bottom breathing room under chapter rules.
     * Lowers `margin-top` / `margin-bottom` quantization thresholds to `1.0em` / `0.8em` so publisher chapter spacing is preserved accurately.
2. Run `cargo test` in `kfx_converter/`, cross-compile `arm64-v8a` `libkfxconv.so`, strip, and stage it into `07_APK_EPUB_INGESTION_MOD/lib/arm64-v8a/libkfxconv.so`.
3. Copy `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java` (do **NOT** overwrite `MetadataEnricher.java` or `OpenCatalogView.java`).
4. Rebuild `classes9.dex`, reassemble APK, align, sign, install on Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v10.9_kfx/base.apk`.

## Task B: Minimal Hardware Verification (2 Screenshots)
1. Re-convert `Fight Club.epub` on device with the new `libkfxconv.so`.
2. `verify_r109_fight_club_page3_border.png` — Open *Fight Club* Chapter 1 (Compact / White / Bookerly) confirming the visible horizontal rule under `CHAPTER 1` and clean spacing before the first paragraph.
3. `verify_r109_about_v109.png` — About Folio modal showing `Version: Folio v10.9 KFX (Enhanced Typesetting)` and live stats.
4. Return `antig_to_gemini_neon_clunky_report109.zip` with `REPORT.md` and both screenshots, plus stock `com.amazon.kindle` untouched check.
