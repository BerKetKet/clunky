# Kindle Android v8.159.0.100 - Deployment & Restoration Notes

## Status
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio") deployed and fully verified alongside unmodified stock `com.amazon.kindle`. Round 017 complete: Native fullscreen `#000000` black `CoverOverlayView` attached directly to `StandAloneBookReaderActivity.decorView` outside WebCore/MobiDocViewer reflow margins (zero margins, zero `"0%"` footer, smooth 180ms fade dismissal on tap/swipe into Page 1); `Aa → Font → Spacing` ("Adjust text spacing of the page.") unlocked and verified with live responsive seekbars (Line spacing, Paragraph spacing, Word spacing); Home tab polished with single unified Open Catalog search bar and on-genre recommendations for Metro 2034.
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0)
  - Clone: `com.amazon.kindle.epub` ("Folio", v8.159.0.100, adaptive icon, EPUB ingestion mod + offline Library grid unlock + in-app DoH DNS + default KF8 WebCore rendering engine + native binary INDX/NCX TOC drawer with multi-chapter `#anchor` offset resolution + 4-digit INDX keys + EPUB3 nav fallback + intra-file link rewriting + OPDS presets + SQLite backup/restore + offline dictionary + interactive Similar Books + 4-tier mirror fallback + ZIP magic byte validation + Home cover carousels + sanitized CSS + native fullscreen cover overlay + unlocked text spacing seekbars)

## Cloned Package Architecture & Modifications
1. **Manifest Isolation & Adaptive Folio Branding**:
   - Stripped `android:sharedUserId="com.amazon"`.
   - Renamed package to `com.amazon.kindle.epub` with label `"Folio"`.
   - Bundled Android Adaptive Icon at `res/mipmap-anydpi-v26/folio_launcher_icon.xml` referencing `@drawable/folio_icon_bg` and `@drawable/folio_icon_fg`.
   - Namespaced all 19 `android:authorities` and 16 custom `<permission>` declarations.
   - Enabled Scoped Storage via `MANAGE_EXTERNAL_STORAGE` and cleartext HTTP fallback.
2. **Native Fullscreen `#000000` Cover Overlay (`CoverOverlayView.java`, Round 017)**:
   - Replaced in-spine reflowable cover HTML hack with native Android `FrameLayout` overlay attached directly to `StandAloneBookReaderActivity.getWindow().getDecorView()`.
   - `EpubTranscoder.java` extracts high-res cover JPEG sidecar (`<outFile>.cover.jpg`, up to 1080×1600 or 1663×2560) alongside `.azw3`.
   - Immersive sticky fullscreen mode flags hide status bar and navigation bar.
   - Zero white borders, zero `"0%"` footer. Dismisses smoothly (`alpha -> 0` in 180ms) on tap or swipe left into Page 1 prose with normal reader margins intact.
   - Injected into `StandAloneBookReaderActivity.onResume()` in `classes3.dex`.
3. **Live `Aa → Font → Spacing` Slider Unlocking (Round 016 & 017)**:
   - Patched `TextSpacingModel.getTextSpacingWidgetEnabled` in `classes6.dex` (offset `0x1e4148`) to return true (`12 10 0f 00`).
   - Patched `KindleDocViewerImpl` spacing gates in `classes2.dex` (`hasLineSpacingV2Support`, `hasLetterSpacingSupport`, `hasParagraphSpacingSupport`, `hasWordSpacingSupport`) to return true.
   - NOPed empty library bypass at `Lly/c;->j(I)V` (0x47ab9a).
   - In `UserSettingsController`: `R2(int)` maps line spacing seekbar values `[0..4]` to `Q2(Math.min(i, 2))` controlling `Setting.LINE_SPACING` (SMALL=0, MEDIUM=1, LARGE=2) live without crashing.
4. **Flagship Home Screen & Dynamic Carousels (Round 015 & 016)**:
   - `OpenCatalogView.java` editorial dark layout with genre chips and horizontal cover carousels.
   - Redundant `"Search Kindle"` bar hidden, leaving single unified Open Catalog search bar.
   - Dynamic "Because You Read Metro 2034" recommendations via Open Library API.

## Verification Evidence
1. **Native Fullscreen Cover Overlay**:
   - Verified on *Moby Dick* (`verify_r17_cover_fullscreen_native.png`): 1080×2340 edge-to-edge pure black background (30.2% black letterbox pixels, zero white frame, zero footer).
2. **Dismissal into Clean Prose**:
   - Verified smooth transition to Page 1 / Chapter 1 (`verify_r17_after_cover_dismiss.png`): normal reader margins restored on pure white background (96.9% white pixels).
3. **`Aa → Font → Spacing` Disclosure Unlocked**:
   - Verified `Aa → Font` tab (`verify_r17_aa_font_spacing.png`): "Spacing — Adjust text spacing of the page." disclosure button visible.
4. **Live Spacing Slider Sub-Panel**:
   - Verified native sub-panel (`verify_r17_spacing_sliders.png`): Line spacing, Paragraph spacing, and Word spacing seekbars responsive without crashes.
5. **Polished Home Tab**:
   - Verified Home tab (`verify_r17_home_polished.png`): single unified search bar and on-genre recommendations for Metro 2034.
6. **Deliverables**:
   - `antig_to_gemini_neon_clunky_report017.zip` (< 100 MB).

## Chrome Bridge & Autonomous Relay
- Pinned to `v1.34.5` on profile `yew`.
- Autonomous relay monitoring active via `tools/autonomous_relay_runner.mjs`:
  - 60-second polling intervals (no high-frequency screenshotting).
  - 2000-second max timeout.
  - Never reupload an already sent report.
