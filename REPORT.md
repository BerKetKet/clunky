# Round 109 Deliverables Report — Folio v10.9 KFX (YellowJersey Border Color & Chapter Rule Parity)
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v10.9 KFX (Enhanced Typesetting)` installed on device at `2026-11-24 02:45:02`.

---

## 1. Task A: Rust Engine Parity Fixes & Java Integration
1. **Rust Engine YellowJersey Border & Spacing Refinements (`kfx_converter/src/`)**:
   - `src/epub/css_style.rs`:
     - Lowered quantized margin thresholds to `margin-top` min `1.0em` (down from 1.5em) and `margin-bottom` min `0.8em` (down from 1.2em), allowing accurate extraction and quantization of publisher vertical chapter breathing room.
   - `src/packager/styles.rs`:
     - Emits explicit `border_color_bottom` symbol (`$86 = 0xFF808080`, neutral dark gray visible across both White and Dark themes) for YellowJersey `$solid` bottom border overrides.
     - Automatically attaches generous `1.2em` bottom margin under `border_bottom` blocks when publisher CSS does not define a larger bottom margin (`if !sig.contains("mb_") { set_field(sym_margin_bottom, make_em("1.2")); }`).
     - Enhanced `<hr/>` horizontal rule (`hr_val`) with explicit `border_style_bottom` (`$91`), `border_weight_bottom` (`$96`, 0.06em), and `border_color_bottom` (`$86`).
   - Verified with full test suite: all 210 unit and integration tests passed (164 unit, 13 CLI, 16 container, 8 pipeline, 9 sync sanitize).

2. **Cross-Compilation & Release Packaging**:
   - Rebuilt `aarch64-linux-android` binary `kfx-conv` (target `arm64-v8a`), stripped with Android NDK `llvm-strip` down to 4.6 MB, and staged to `07_APK_EPUB_INGESTION_MOD/lib/arm64-v8a/libkfxconv.so`.
   - Updated `FolioMoreView.java` with version bump to `Folio v10.9 KFX (Enhanced Typesetting)`. Preserved `MetadataEnricher.java` and `OpenCatalogView.java`.
   - Recompiled `classes9.dex` (366,228 B), repacked base APK as `classes4.dex`, aligned via `zipalign -f -p 4`, and signed with `apksigner`.
   - Installed on Samsung Galaxy M32 (`lastUpdateTime=2026-11-24 02:45:02`) and staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.9_kfx/base.apk` (132 MB).

3. **On-Device EPUB Re-Conversion**:
   - Re-converted `/sdcard/en/0/Chuck Palahniuk - Fight Club.epub` directly on device with newly deployed `libkfxconv.so` in **210.67ms**.
   - Output KFX: `/data/user/0/com.amazon.kindle.epub/files/folio_library/Palahniuk_Chuck_FOLIO_7313DDB45B1B.kfx` (507,541 bytes).

---

## 2. Task B: Hardware Verification on Physical Galaxy M32
Both apps were verified on physical hardware under identical theme settings (`Compact` theme, `White` background, `Bookerly` font, `Thin margins`):

1. **`verify_r109_fight_club_page3_border.png` (315,025 bytes)**:
   - Chapter 1 (`CHAPTER 1`):
     * Heading starts at `Y = 218` with clean sans-serif typography.
     * `1.2em` bottom breathing room under `CHAPTER 1` pushes the opening body line (`"Tyler gets me a job..."`) down to `Y = 358`, matching Stock Kindle's generous heading spacing.
     * Body text canvas ends cleanly at `Y = 2109`, giving a total vertical text height of **1,891 px**, which matches Stock Kindle's canvas height (**1,877 px**) within **14 pixels**.
2. **`verify_r109_about_v109.png` (355,143 bytes)**:
   - About Folio dialog confirming:
     * `Version: Folio v10.9 KFX (Enhanced Typesetting)`
     * `Hardware: TrebleDroid with GApps - Android 13 (arm64-v8a)`
     * Pure-Rust Engine (`libkfxconv.so`) telemetry and live library stats.
3. **Stock Kindle Package Invariant**:
   - `adb shell dumpsys package com.amazon.kindle | grep -E "versionName|lastUpdateTime"`
   - `versionName=8.160.1.0(2.0.124621.0)`
   - `lastUpdateTime=2026-11-19 21:59:23` (**100% UNTOUCHED and READ-ONLY**).

---

## 3. Project Lead Architectural Directive on Cover Architecture
Reiterating the project lead directive verbatim:
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 4. Deliverables Staging
- Release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v10.9_kfx/base.apk` (132 MB)
- Hardware Screenshots:
  - `verify_r109_fight_club_page3_border.png`
  - `verify_r109_about_v109.png`
- Instructions & Protocol: `INSTRUCTIONS_ROUND_109.md`, `protocol.json`
