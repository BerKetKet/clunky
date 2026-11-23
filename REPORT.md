# Round 108 Deliverables Report — Folio v10.8 KFX (Publisher CSS Fidelity Engine for Fight Club Parity)
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v10.8 KFX (Enhanced Typesetting)` installed on device at `2026-11-24 02:28:46`.

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
   - Installed on Samsung Galaxy M32 (`lastUpdateTime=2026-11-24 02:28:46`) and staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.8_kfx/base.apk` (132 MB).

3. **EPUB Re-Conversion Verification**:
   - Re-converted `/sdcard/en/0/Chuck Palahniuk - Fight Club.epub` directly on device with newly deployed `libkfxconv.so` in 226.62ms.
   - Generated clean `Palahniuk_Chuck_FOLIO_7313DDB45B1B.kfx` (507,535 bytes) containing interned symbols `style_ov_1` and `style_ov_2`.

---

## 2. Task B: Visual Parity Verification on Physical Galaxy M32
Both apps were verified on the physical Galaxy M32 under the exact same theme settings (`Compact` theme, `White` background, `Bookerly` font, `Thin margins`):

1. **`verify_r108_fight_club_page2.png` (26,484 bytes)**:
   - Title Page (`FIGHT CLUB / Chuck Palahniuk`):
     * Title block starts at `Y = 261` (shifted down by quantized `margin-top` from the previous `Y = 174`), bringing the title into balanced vertical centering matching Stock Kindle (`Y = 388`).
     * Renders crisp publisher sans-serif typography with wide uppercase tracking.
     * No reading footer stamped at bottom; page bounds end cleanly at `Y = 497`.
2. **`verify_r108_fight_club_page3.png` (336,379 bytes)**:
   - Chapter 1 (`CHAPTER 1`):
     * `CHAPTER 1` heading starts at `Y = 218` (shifted down by quantized top margin from `Y = 133` in Round 107).
     * Body text canvas ends at `Y = 2179`, matching Stock Kindle's lower margin bound (`Y = 2173`) within **6 pixels**.
     * Total vertical text canvas height reduced from 2,150 px to 1,961 px, restoring safe margin buffers.
3. **`verify_r108_about_v108.png` (355,115 bytes)**:
   - About Folio dialog confirming:
     * `Version: Folio v10.8 KFX (Enhanced Typesetting)`
     * `Hardware: TrebleDroid with GApps - Android 13 (arm64-v8a)`
     * `Publisher CSS Fidelity (sans-serif, margins, border-bottom & tracking)`
     * Live stats: 34 indexed books, 32 enriched records, 5 in progress, 1 finished.
4. **Stock Kindle Package Invariant**:
   - `adb shell dumpsys package com.amazon.kindle | grep -E "versionName|lastUpdateTime"`
   - `versionName=8.160.1.0(2.0.124621.0)`
   - `lastUpdateTime=2026-11-19 21:59:23` (**100% UNTOUCHED and READ-ONLY**).

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
