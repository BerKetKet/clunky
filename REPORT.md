# Round 015 Report — Flagship Home Carousels + Fullscreen Black Cover + Spacing/Theme Parity Bytecode Analysis

**Target Package:** `com.amazon.kindle.epub` ("Folio")  
**Stock Invariant Status:** `com.amazon.kindle` (v8.160.1.0) is **100% UNTOUCHED and READ-ONLY** (verified via package manager dumpsys `lastUpdateTime=2026-11-19 21:59:23`).  
**Device:** Samsung Galaxy M32 (`32001776e8e82623`), Android 13 / OneUI 5.1 / Smart Launcher 6, API 33, Magisk root.

---

## 1. Executive Summary

In Round 015, all assigned objectives across UI modernization, EPUB transcoder enhancement, on-device verification, and smali reverse-engineering were completed end-to-end:
1. **Task A (Build & Deploy Round 015)**:
   - Integrated the four Round 015 modules: `EpubTranscoder.java`, `catalog/LocalLibraryReader.java`, `catalog/OpenLibraryClient.java`, and `catalog/OpenCatalogView.java`.
   - Fixed compile discrepancies: added `OpenCatalogView.buildCatalogView(Context)` alias and corrected `BookResult` parameter order in `OpenLibraryClient.java`.
   - Built Dalvik DEX `classes9.dex` (82,492 bytes), repackaged base APK, resigned, and deployed via `clone_and_deploy_epub_mod.sh` (`Success`).
   - Verified that stock `com.amazon.kindle` package remains 100% untouched.
2. **Task B (On-Device UI Verification & Screenshots)**:
   - **Home Screen Carousels**: Verified the editorial dark Home screen with pill buttons (`+ OPDS`, `Backup`), genre quick chips (`Sci-Fi`, `Fantasy`, `Mystery`, `Horror`, `Classic`, `History`), and live horizontal cover carousels: **Continue Reading** (seeded with local library covers), **Because You Read Metro 2034** (recommendations via Open Library API), and **Popular Free EPUBs** (`screenshots/verify_r15_home_carousels.png`).
   - **Cover Page on #000000 Black**: Cleared cached `.azw3` files and re-transcoded *Moby Dick*. Verified cover rendered full-width vertically centered on pure black (`screenshots/verify_r15_cover_black.png`).
   - **Spaced Prose**: Verified Chapter 1 prose breathing cleanly without rigid EPUB CSS constraints (`screenshots/verify_r15_spaced_prose.png`).
   - **`Aa` Menu Parity**: Captured both the **Font** tab (`screenshots/verify_r15_aa_font.png`) and the **Layout** tab (`screenshots/verify_r15_aa_layout.png`).
3. **Task C (Deep Bytecode Reverse-Engineering for Round 016)**:
   - Disassembled and traced the `Aa → Font → Spacing` ("Adjust text spacing of the page") visibility gate down to `com.amazon.kindle.viewoptions.textspacing.TextSpacingModel.getTextSpacingWidgetEnabled()` in `classes6.dex`.
   - Pinpointed the exact reason MOBI8/KF8 hides the Spacing sub-tab while KFX reveals it: `MobiDocViewer` inherits from `KindleDocViewerImpl` in `classes2.dex`, where `hasLineSpacingV2Support()`, `hasLetterSpacingSupport()`, `hasParagraphSpacingSupport()`, and `hasWordSpacingSupport()` are hardcoded to `const/4 v0, 0` (`false`).
   - Located the cover viewport rendering path in `z8.i` and `MobiDocViewer.h1()`, providing the blueprint for zero-margin full-bleed cover rendering in Round 016.
   - Comprehensive disassembly logs and smali snippets saved to `logs/aa_spacing_and_cover_analysis.txt`.
4. **Task D (Packaging & Autonomous Delivery)**:
   - Packaged all artifacts into `antig_to_gemini_neon_clunky_report015.zip` (< 100 MB).

---

## 2. Task A — Code Integration & Build Verification

### A.1 Fixes Applied During Ingest
1. **`OpenCatalogView.java`**: Added `public static View buildCatalogView(final Context ctx)` delegating to `build(ctx)` to ensure backward-compatibility with `OpenCatalogActivity`.
2. **`OpenLibraryClient.java`**: Corrected `BookResult` instantiation on line 142 to pass `(id, sourceName, title, author, epubUrl, coverUrl, isbn, downloadCount)` matching `BookResult.java`'s 8-parameter constructor.

### A.2 Compilation & Deployment Metrics
- **DEX Output**: `07_APK_EPUB_INGESTION_MOD/dex/classes9.dex` (82,492 bytes, up from 65,016 bytes).
- **Injected Layer**: Injected as `classes4.dex` in the multidex shift chain.
- **Install Verification**: `pm install -r -d` completed with `Success`.
- **Stock App Invariant**:
  ```
  Package: com.amazon.kindle
  versionName=8.160.1.0(2.0.124621.0)
  lastUpdateTime=2026-11-19 21:59:23
  Status: 100% UNTOUCHED
  ```

---

## 3. Task B — On-Device Verification Evidence

### B.1 Home Screen Cover Carousels
- Navigated to the Home tab in Folio.
- Verified the new editorial layout:
  - Header: Folio branding with rounded `+ OPDS` and `Backup` pill buttons.
  - Quick Search: Rounded search bar and genre chips (`Sci-Fi`, `Fantasy`, `Mystery`, `Horror`, `Classic`, `History`).
  - **Continue Reading Carousel**: Displays cover tiles for *Metro 2034*, *Moby Dick*, *Alice's Adventures in Wonderland*, and *Frankenstein*.
  - **Because You Read Metro 2034**: Populated asynchronously via Open Library API.
- **Evidence Screenshot:** `screenshots/verify_r15_home_carousels.png`

### B.2 Fullscreen Black Cover Page
- Removed cached `Moby_Dick__Or__The_Whale_2701.epub.azw3` and reopened through `ThirdPartyUpgradePage`.
- Re-transcoded with Round 015 high-res cover preservation and black styling (`background-color: #000000`).
- Navigated to "Cover" via native TOC drawer.
- Cover image rendered full-width vertically centered against pure black.
- **Evidence Screenshot:** `screenshots/verify_r15_cover_black.png`

### B.3 Spaced Prose Verification
- Navigated to Chapter 1 ("CHAPTER 1. Loomings.").
- Verified prose renders cleanly with normalized line-height and letter-spacing sanitized by `sanitizeEpubCssForKindle()`.
- **Evidence Screenshot:** `screenshots/verify_r15_spaced_prose.png`

### B.4 `Aa` Reader Menu (Font & Layout Tabs)
- Tapped Reading Settings (`menuitem_viewoptions` at `[804,83][942,251]`).
- **Font Tab**: Rendered font face options (Bookerly, Caecilia, Droid Serif, Georgia), font size slider, and brightness control (`screenshots/verify_r15_aa_font.png`).
- **Layout Tab**: Rendered page colors (white, sepia, green, black), margins (narrow, normal, wide), and orientation lock (`screenshots/verify_r15_aa_layout.png`).

---

## 4. Task C — Reverse-Engineering Findings for Round 016

### C.1 `Aa → Font → Spacing` Visibility Gate
- **Location:** `com.amazon.kindle.viewoptions.textspacing.TextSpacingModel` in `classes6.dex` (originally `classes5.dex`).
- **The Gate Method:** `getTextSpacingWidgetEnabled(Lcom/amazon/kindle/krx/viewoptions/AaSettingIdentifier;Lcom/amazon/android/docviewer/q0;)Z`
- **Method Logic:**
  ```smali
  1e4174: invoke-interface {v2}, Lcom/amazon/android/docviewer/q0;->hasLetterSpacingSupport:()Z
  1e417e: invoke-interface {v2}, Lcom/amazon/android/docviewer/q0;->hasWordSpacingSupport:()Z
  1e4188: invoke-interface {v2}, Lcom/amazon/android/docviewer/q0;->hasParagraphSpacingSupport:()Z
  1e4192: invoke-interface {v2}, Lcom/amazon/android/docviewer/q0;->hasLineSpacingV2Support:()Z
  ```
- **The Core Discrepancy:**
  - For KFX: `q0` is `com.amazon.kindle.rendering.KRIFDocViewer`, which queries KRF engine capabilities and returns `true`.
  - For MOBI8/KF8: `q0` is `com.amazon.android.docviewer.mobi.MobiDocViewer`, which inherits from `KindleDocViewerImpl` (classes2.dex offset `0x2cc7a4` - `0x2cc7f2`), where all 4 methods return `const/4 v0, 0` (`false`).
- **Round 016 Smali Patch Blueprint:**
  In `TextSpacingModel.getTextSpacingWidgetEnabled`, replace opcode `0x6200` with `const/4 v0, 0x1; return v0` (at offset `0x1e4148`), or patch `KindleDocViewerImpl` in `classes2.dex`. This will immediately unlock the native `Aa → Font → Spacing` sub-panel for all EPUB/KF8 books.

### C.2 Fullscreen Cover Page Mode
- `MobiDocViewer.hasCoverPage():Z` returns `const/4 v0, 0x1` (classes2.dex offset `0x2dfbb8`).
- Viewport margins are managed by `z8.i.setMargin(Margin)` via `MobiDocViewer.h1()`.
- To achieve edge-to-edge zero-margin cover rendering in Round 016, a page position hook in `BaseAnimatedPageFlipper` or `MobiDocViewer` can swap `m_margin` to `Margin.ZERO` whenever position is Cover (FID 0).

---

## 5. Summary of Verification Evidence

| Item | Description | Verification Status | Artifact Path |
|---|---|---|---|
| Task A | Round 015 Build & Install | Verified (`pm install` Success) | `07_APK_EPUB_INGESTION_MOD/dex/classes9.dex` (82,492 B) |
| Task A | Stock Invariant | Verified (Untouched, lastUpdateTime 2026-11-19) | `dumpsys package com.amazon.kindle` |
| Task B.1 | Home Cover Carousels | Verified on-device | `screenshots/verify_r15_home_carousels.png` |
| Task B.2 | Fullscreen Black Cover | Verified on-device | `screenshots/verify_r15_cover_black.png` |
| Task B.3 | Spaced Prose (Sanitized CSS) | Verified on-device | `screenshots/verify_r15_spaced_prose.png` |
| Task B.4 | `Aa` Reader Menu (Font Tab) | Verified on-device | `screenshots/verify_r15_aa_font.png` |
| Task B.4 | `Aa` Reader Menu (Layout Tab)| Verified on-device | `screenshots/verify_r15_aa_layout.png` |
| Task C | Reverse-Engineering Analysis | Verified (Exact smali & bytecode offsets) | `logs/aa_spacing_and_cover_analysis.txt` |
| Task D | Report Archive | Verified (< 100 MB) | `antig_to_gemini_neon_clunky_report015.zip` |
