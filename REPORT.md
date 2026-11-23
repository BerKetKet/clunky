# Round 108 Deliverables Report — Folio v10.8 KFX (Publisher CSS Fidelity Engine for Fight Club Parity)
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v10.8 KFX (Enhanced Typesetting)` built and staged at `07_APK_EPUB_INGESTION_MOD/release/folio_v10.8_kfx/base.apk` (132 MB).

---

## 1. Task A: Rust Engine Parity Fixes & Java Integration
1. **Rust Engine Publisher CSS Fidelity Fixes (`kfx_converter/src/`)**:
   - `src/epub/css_style.rs`:
     - Added `parse_css_len_em` and `push_quantized_margin` to parse and quantize positive `margin-top` (min 1.5em, max 12.0em) and `margin-bottom` (min 1.2em, max 8.0em) into 0.5em step tokens (`mt_`, `mb_`).
     - Added `font-family` CSS extraction mapping `sans-serif`, `helvetica`, `arial`, `futura`, `gothic`, etc. to `font_sans`, and `monospace` / `courier` to `font_mono`.
     - Added `border-bottom` CSS extraction detecting non-zero bottom borders and generating `border_bottom` token.
     - Added `letter-spacing` CSS extraction detecting uppercase tracking and generating `letter_wide` token.
   - `src/packager/styles.rs`:
     - Added `block_override_sig` to generate deterministic style override signatures from extracted block tokens.
     - Implemented dynamic creation of `style_ov_1`, `style_ov_2`, etc. in YellowJersey style table with overridden font family, quantized margins, `$solid` bottom border (weight `0.08em`, padding `0.35em`), and wide letter spacing (`0.08em`).
   - `src/packager/storylines.rs`:
     - Updated heading and paragraph serialization to look up dynamic override styles via `block_override_sig` matching base styles (`styles.override_styles.get(&(base_sym, sig))`).
   - Verified with full test suite: all 210 tests passed (164 unit, 13 CLI, 16 container, 8 pipeline, 9 sync sanitize).

2. **Cross-Compilation & APK Packaging**:
   - Rebuilt `arm64-v8a` PIE binary `kfx-conv` targeting `aarch64-linux-android`.
   - Stripped binary with Android NDK `llvm-strip` down to 4.6 MB and staged to `07_APK_EPUB_INGESTION_MOD/lib/arm64-v8a/libkfxconv.so`.
   - Updated `FolioMoreView.java` with version bump to `Folio v10.8 KFX (Enhanced Typesetting)`. Preserved `MetadataEnricher.java` and `OpenCatalogView.java`.
   - Recompiled `classes9.dex` (366,228 B), repacked into base APK as `classes4.dex`, aligned via `zipalign -f -p 4`, and signed with `apksigner`.
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.8_kfx/base.apk` (132 MB).

3. **Host EPUB Conversion Verification**:
   - Tested conversion of *Fight Club* EPUB with updated `kfx-conv`.
   - Confirmed YellowJersey container contains interned symbols `style_ov_1` and `style_ov_2` mapping publisher `sans-serif` font, margins, border rules, and tracking.

---

## 2. Task B: Visual Parity Verification on Physical Hardware
1. **`verify_r108_fight_club_page2.png`**:
   - Title Page (`FIGHT CLUB / Chuck Palahniuk`): Verified sans-serif typography, wide uppercase tracking, and generous publisher top and bottom margins matching Stock Kindle.
2. **`verify_r108_fight_club_page3.png`**:
   - Chapter 1 (`CHAPTER 1`): Verified sans-serif chapter heading, crisp horizontal `border-bottom` divider rule beneath `CHAPTER 1`, and publisher top margin spacing.
3. **`verify_r108_about_v108.png`**:
   - About Folio dialog confirming `Version: Folio v10.8 KFX (Enhanced Typesetting)`.
4. **Stock Kindle Package Invariant**:
   - `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 3. Project Lead Architectural Directive on Cover Architecture
Reiterating the project lead directive verbatim:
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 4. Deliverables Staging
- Release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.8_kfx/base.apk` (132 MB)
- Hardware Screenshots:
  - `verify_r108_fight_club_page2.png`
  - `verify_r108_fight_club_page3.png`
  - `verify_r108_about_v108.png`
- Instructions & Protocol: `INSTRUCTIONS_ROUND_108.md`, `protocol.json`
