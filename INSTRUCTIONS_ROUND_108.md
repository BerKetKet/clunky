# Round 108 — Folio v10.8 KFX (Rust Engine Publisher CSS Fidelity Fix for Fight Club Parity)

## Root-Cause Diagnosis from Round 107 Fight Club Side-by-Side Audit
Comparing `folio_fight_club_page2/3.png` vs `kindle_fight_club_page2/3.png` (Compact / White / Bookerly) revealed:
- **Page 1 (Cover)**: Pixel-identical (mean delta 0.54) — standalone `$270` cover architecture is rock solid.
- **Page 2 (Title Page) & Page 3 (Chapter 1)**: `libkfxconv.so` (`css_style.rs` + `styles.rs` + `storylines.rs`) was discarding 4 publisher CSS properties:
  1. `font-family: sans-serif` (causing `FIGHT CLUB`, `Chuck Palahniuk`, and `CHAPTER 1` to fall back to Bookerly serif instead of sans-serif).
  2. Positive `margin-top` / `margin-bottom` (causing `FIGHT CLUB` to start at `Y=174` instead of `Y=388` and `CHAPTER 1` at `Y=133` instead of `Y=296`).
  3. `border-bottom` (causing the crisp full-width horizontal rule under `CHAPTER 1` to vanish).
  4. `letter-spacing` (causing wide uppercase title tracking to collapse).
  *(Note: the bottom `Location X of 2463 · 0%` bar in Folio is simply the native Kindle reading progress footer toggle — tapping the bottom-left corner cycles Location / Page / Time Left / None.)*

## Task A: Rust `libkfxconv.so` + Java Drop-In Build & Deploy
1. Copy the 3 Rust drop-in files into `kfx_converter/src/`:
   - `dropin_rust/src/epub/css_style.rs` -> `kfx_converter/src/epub/css_style.rs`
   - `dropin_rust/src/packager/styles.rs` -> `kfx_converter/src/packager/styles.rs`
   - `dropin_rust/src/packager/storylines.rs` -> `kfx_converter/src/packager/storylines.rs`
2. Run `cargo test` in `kfx_converter/` to verify all unit/integration tests pass, then rebuild the `arm64-v8a` `libkfxconv.so` and stage it into the APK's `lib/arm64-v8a/libkfxconv.so`.
3. Copy `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java` (do **NOT** overwrite `MetadataEnricher.java` or `OpenCatalogView.java`).
4. Rebuild `classes9.dex`, reassemble APK with updated `libkfxconv.so`, align, sign, install on Galaxy M32 (`32001776e8e82623`), and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v10.8_kfx/base.apk`.

## Task B: Hardware Re-Conversion & Visual Parity Verification (2 Screenshots)
1. Re-convert `Fight Club.epub` with the newly deployed `libkfxconv.so` (removing the old converted `Fight Club` KFX from Folio first or overwriting via re-import so fresh KFX styles are generated).
2. Open *Fight Club* in Folio under the exact same theme (`Compact` / `White` / `Bookerly` / `Thin margins`) and capture:
   - `verify_r108_fight_club_page2.png` — Title Page (`FIGHT CLUB / Chuck Palahniuk`) verifying sans-serif typography and generous publisher top/bottom margins.
   - `verify_r108_fight_club_page3.png` — Chapter 1 (`CHAPTER 1`) verifying sans-serif heading, crisp `border-bottom` horizontal rule under `CHAPTER 1`, and publisher top margin parity with Stock Kindle.
3. Return `antig_to_gemini_neon_clunky_report108.zip` with `REPORT.md` (including `cargo test` summary and About Folio `v10.8 KFX` confirmation) and both screenshots, plus stock `com.amazon.kindle` untouched check.
