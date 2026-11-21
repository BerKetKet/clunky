# Kindle Android v8.159.0.100 - Deployment & Restoration Notes

## Status
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v1.1") deployed and fully verified alongside unmodified stock `com.amazon.kindle`. Round 018 complete: Final polish delivered with guaranteed on-genre cover recommendations (3-tier fallback engine in `OpenLibraryClient.java`), per-book cover resolution and byte-offset beginning-of-book calibration in `CoverOverlayView.java`, verified on-device with all 3 Home carousels (Continue Reading, Because You Read Metro 2034, Popular Free EPUBs) and native edge-to-edge fullscreen `#000000` black cover overlay. Signed Folio v1.1 release APK staged in `07_APK_EPUB_INGESTION_MOD/release/folio_v1.1/` (134.8 MB).
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0)
  - Clone: `com.amazon.kindle.epub` ("Folio v1.1", v8.159.0.100, adaptive icon, EPUB ingestion mod + offline Library grid unlock + in-app DoH DNS + default KF8 WebCore rendering engine + native binary INDX/NCX TOC drawer with multi-chapter `#anchor` offset resolution + 4-digit INDX keys + EPUB3 nav fallback + intra-file link rewriting + OPDS presets + SQLite backup/restore + offline dictionary + interactive Similar Books + 4-tier mirror fallback + ZIP magic byte validation + Home cover carousels with 3-tier on-genre fallback + sanitized CSS + native fullscreen cover overlay + unlocked text spacing seekbars)

## Cloned Package Architecture & Modifications
1. **Manifest Isolation & Adaptive Folio Branding**:
   - Stripped `android:sharedUserId="com.amazon"`.
   - Renamed package to `com.amazon.kindle.epub` with label `"Folio"`.
   - Bundled Android Adaptive Icon at `res/mipmap-anydpi-v26/folio_launcher_icon.xml` referencing `@drawable/folio_icon_bg` and `@drawable/folio_icon_fg`.
   - Namespaced all 19 `android:authorities` and 16 custom `<permission>` declarations.
   - Enabled Scoped Storage via `MANAGE_EXTERNAL_STORAGE` and cleartext HTTP fallback.
2. **Native Fullscreen `#000000` Cover Overlay (`CoverOverlayView.java`)**:
   - Native Android `FrameLayout` overlay attached directly to `StandAloneBookReaderActivity.getWindow().getDecorView()`.
   - Sidecar extraction (`<outFile>.cover.jpg`) during EPUB ingestion.
   - Immersive sticky fullscreen mode flags hide status bar and navigation bar.
   - Zero white borders, zero `"0%"` footer. Dismisses smoothly (`alpha -> 0` in 180ms) on tap or swipe left into Page 1 prose with normal reader margins intact.
   - Injected into `StandAloneBookReaderActivity.onResume()` in `classes3.dex`.
3. **Guaranteed On-Genre Recommendations (`OpenLibraryClient.java`, Round 018)**:
   - 3-tier fallback engine: Specific non-generic subjects -> Same-author books -> Genre shelf fallback (sci-fi, fantasy, horror, adventure).
   - "Because You Read [Book]" carousel guaranteed to populate with on-genre covers.
4. **Live `Aa → Font → Spacing` Slider Unlocking**:
   - Patched `TextSpacingModel.getTextSpacingWidgetEnabled` in `classes6.dex` to return true.
   - Patched `KindleDocViewerImpl` spacing gates in `classes2.dex` to return true.
   - NOPed empty library bypass at `Lly/c;->j(I)V`.
   - In `UserSettingsController`: `R2(int)` maps line spacing seekbar values `[0..4]` to `Q2(Math.min(i, 2))` controlling `Setting.LINE_SPACING` (SMALL=0, MEDIUM=1, LARGE=2) live without crashing.

## Verification Evidence
1. **Folio v1.1 Home Carousels**:
   - Verified on-device (`verify_r18_home_all_carousels.png`): Continue Reading, Because You Read Metro 2034, and Popular Free EPUBs all visible.
2. **Native Fullscreen Cover Overlay**:
   - Verified on *Moby Dick* (`verify_r18_cover_verified.png`): 1080×2340 edge-to-edge pure black background (30.2% black letterbox pixels, zero white frame, zero footer).
3. **Staged Release**:
   - `07_APK_EPUB_INGESTION_MOD/release/folio_v1.1/base.apk` (134,839,933 bytes).
4. **Deliverables**:
   - `antig_to_gemini_neon_clunky_report018.zip` (< 100 MB).

## Chrome Bridge & Autonomous Relay
- Pinned to `v1.34.5` on profile `yew`.
- Autonomous relay monitoring active via `tools/autonomous_relay_runner.mjs`:
  - 60-second polling intervals (no high-frequency screenshotting).
  - 2000-second max timeout.
  - Never reupload an already sent report.
