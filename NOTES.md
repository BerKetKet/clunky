# Kindle Android v8.159.0.100 - Deployment & Restoration Notes

## Status
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio") deployed and fully verified alongside unmodified stock `com.amazon.kindle`. Round 015 complete: Flagship editorial dark Home screen deployed with horizontal cover carousels ("Continue Reading", "Because You Read Metro 2034" seeded via Open Library API, "Popular Free EPUBs", and genre quick chips); Fullscreen black cover page rendering verified on *Moby Dick*; Prose spacing sanitized via `sanitizeEpubCssForKindle`; `Aa` reader menu (Font & Layout tabs) captured; reverse-engineered `Font → Spacing` gate (`TextSpacingModel.getTextSpacingWidgetEnabled` in `classes6.dex` querying `KindleDocViewerImpl` in `classes2.dex`) and cover zero-margin viewport path for Round 016.
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0)
  - Clone: `com.amazon.kindle.epub` ("Folio", v8.159.0.100, adaptive icon, EPUB ingestion mod + offline Library grid unlock + in-app DoH DNS + default KF8 WebCore rendering engine + native binary INDX/NCX TOC drawer with multi-chapter `#anchor` offset resolution + 4-digit INDX keys + EPUB3 nav fallback + intra-file link rewriting + OPDS presets + SQLite backup/restore + offline dictionary + interactive Similar Books + 4-tier mirror fallback + ZIP magic byte validation + Home cover carousels + sanitized CSS + black cover page)

## Cloned Package Architecture & Modifications
1. **Manifest Isolation & Adaptive Folio Branding**:
   - Stripped `android:sharedUserId="com.amazon"`.
   - Renamed package to `com.amazon.kindle.epub` with label `"Folio"`.
   - Bundled Android Adaptive Icon at `res/mipmap-anydpi-v26/folio_launcher_icon.xml` referencing `@drawable/folio_icon_bg` and `@drawable/folio_icon_fg`, eliminating white borders on modern third-party and OEM launchers.
   - Namespaced all 19 `android:authorities` and 16 custom `<permission>` declarations to avoid installation collisions (`INSTALL_FAILED_DUPLICATE_PERMISSION`).
   - Enabled Scoped Storage via `MANAGE_EXTERNAL_STORAGE` and cleartext HTTP fallback.
2. **Flagship Home Screen & Dynamic Carousels (Round 015)**:
   - `OpenCatalogView.java` rewritten with modern dark editorial aesthetics, genre chips, and live horizontal cover carousels.
   - `LocalLibraryReader.java` reads `kindle_library.db` directly to populate the "Continue Reading" carousel with real cover art and reading progress.
   - `OpenLibraryClient.java` dynamically queries Open Library subjects API using user's library books to seed personalized "Because You Read [Book]" recommendations.
3. **EPUB Transcoder & Prose Spacing Sanitizer (Round 015)**:
   - Preserves high-resolution covers up to 1080×1600.
   - Generates pure black full-bleed cover spine page (`background-color: #000000; margin: 0; padding: 0`).
   - `sanitizeEpubCssForKindle()` strips hardcoded line-height, letter-spacing, and fixed font-size rules so Kindle's native reader typography can breathe.
4. **Multi-Chapter `#anchor` Navigation & INDX Indexing (Round 013 & 014)**:
   - `EpubTranscoder.java` parses both NCX `<navPoint>` and EPUB3 `<nav>` / `nav.xhtml` structures.
   - Extracts spine files and `#fragment` anchors, computing precise UTF-8 byte offsets inside KF8 fragments using `findAnchorByteOffset(html, fragId)`.
   - Rewrites intra-file links (`<a href="#id">`, `<a href="file.xhtml#id">`) to `kindle:pos:fid:XXXX:off:YYYYYYYYYY` using 10-char Base-32 encoding (`toBase32Ten`).
   - Uses 4-digit Secondary INDX keys (`%04d`) supporting arbitrary chapter counts.

## Verification Evidence
1. **Adaptive Launcher Icon**:
   - Smart Launcher 6 app drawer renders Folio with full navy-teal background squircle without white frame (`verify_r13_adaptive_icon.png`).
2. **Consecutive Chapter TOC**:
   - Native TOC drawer on *Moby Dick* lists all chapters consecutively (`verify_r13_mobydick_full_toc.png`, `verify_r14_kf8_regression.png`).
3. **Home Carousels & Spaced Prose**:
   - Verified Home carousels with Continue Reading + Because You Read Metro 2034 (`verify_r15_home_carousels.png`).
   - Verified Fullscreen black cover page (`verify_r15_cover_black.png`).
   - Verified spaced prose (`verify_r15_spaced_prose.png`).
   - Verified Aa Font tab (`verify_r15_aa_font.png`) and Layout tab (`verify_r15_aa_layout.png`).
4. **Bytecode Reverse-Engineering**:
   - Logged exact smali gate for `Aa → Font → Spacing` and cover viewport mode (`logs/aa_spacing_and_cover_analysis.txt`).
5. **Deliverables**:
   - `antig_to_gemini_neon_clunky_report015.zip` (< 100 MB).

## Chrome Bridge & Autonomous Relay
- Pinned to `v1.34.5` on profile `yew`.
- Autonomous relay monitoring active via `tools/autonomous_relay_runner.mjs`:
  - 60-second polling intervals (no high-frequency screenshotting).
  - 2000-second max timeout.
  - Strict policy: never re-upload already delivered reports.
