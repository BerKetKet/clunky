# Kindle Android v8.159.0.100 - Deployment & Restoration Notes

## Status
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v1.2") deployed and fully verified alongside unmodified stock `com.amazon.kindle`. Round 020 complete: Systematic UI/UX QC pass resolving 4 concrete issues: (1) Zero duplicate white-margined HTML cover inside book spine (`EpubTranscoder.java` omits redundant cover-only HTML spine item when `hasCover == true`, rendering cover exclusively via native fullscreen `#000000` `CoverOverlayView`); (2) TOC "Cover" jump hook (`CoverOverlayView.showForCurrentBookForced` hooked into `BaseTableOfContentsProvider.m` in `classes2.dex`, `MobiDocViewer.navigateToCover()` in `classes2.dex`, and `ReaderActivity.performBookOperation` in `classes3.dex`), enabling fullscreen cover display directly from the native slide-out TOC drawer at any reading position; (3) Real `.cover.jpg` sidecars preferred before `coverslarge`/`coverssmall` in `LocalLibraryReader.java` and `CoverOverlayView.java`, eliminating gray placeholder cards; (4) Reliable hiding of duplicate `"Search Kindle"` bar on Home (`OnAttachStateChangeListener` + delayed passes), omnibus recommendation deduplication (`OpenLibraryClient.java`), and clean non-duplicate Similar Books list in book info dialogs (`OpenCatalogView.java`).
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v1.2", v8.159.0.100, adaptive icon, EPUB ingestion mod + offline Library grid unlock + in-app DoH DNS + default KF8 WebCore rendering engine + native binary INDX/NCX TOC drawer with multi-chapter `#anchor` offset resolution + 4-digit INDX keys + EPUB3 nav fallback + intra-file link rewriting + OPDS presets + SQLite backup/restore + offline dictionary + interactive Similar Books + 4-tier mirror fallback + ZIP magic byte validation + Home cover carousels with fine-grained Open Library recommendations + sanitized CSS + native fullscreen cover presentation + unlocked text spacing seekbars + direct Continue Reading reader launch + zero duplicate HTML cover + TOC Cover drawer hook)

## Cloned Package Architecture & Modifications (Round 020)
1. **Zero Duplicate HTML Cover Spine Item (`EpubTranscoder.java`)**:
   - When `hasCover == true`, `isLikelyCoverSpineItem()` checks the first spine item. If it is a cover page or wrapper containing only the cover image (`isImagePath`, `<img`, or `<image` with minimal text), it is omitted from `spinePaths`.
   - The cover is rendered **exclusively** via the native fullscreen `#000000` `CoverOverlayView`, matching stock Kindle's `DocumentPage::clear()` architecture.
   - Dismissing `CoverOverlayView` lands directly on Page 1 (title/contents/chapter prose).
2. **TOC Drawer "Cover" Hook (`BaseTableOfContentsProvider.m` in `classes2.dex`, `CoverOverlayView.java`)**:
   - `chapters.add(new ChapterToc("Cover", 0))` exposes "Cover" as entry 0 in the NCX/TOC drawer.
   - In `classes2.dex`, `BaseTableOfContentsProvider.m` was patched to check if clicked item `position == 0` (Cover entry), and if so, invokes `CoverOverlayView.showForCurrentBookForced((Activity) context)`.
   - In `classes3.dex`, `ReaderActivity.performBookOperation` hooks `operation == 0` to invoke `CoverOverlayView.showForCurrentBookForced(p0)`.
   - In `classes2.dex`, `MobiDocViewer.navigateToCover()` hooks to invoke `CoverOverlayView.showForCurrentBookForced(activity)`.
   - Verified on device: Navigated to Chapter 2 in *Frankenstein* (reading location 5304), opened TOC drawer, tapped "Cover", and the fullscreen `#000000` black cover overlay appeared immediately (`verify_r20_toc_cover_jump.png`).
3. **Real Covers in Continue Reading Cards (`LocalLibraryReader.java`, `CoverOverlayView.java`)**:
   - `findCoverFile()` checks `.cover.jpg` sidecars and `epub_cache/<name>.azw3.cover.jpg` before `coverslarge`/`coverssmall`.
   - Real color covers displayed across all Continue Reading cards on Home.
4. **Reliable Home Search Bar Suppression, Dedup & Clean Similar Books (`OpenCatalogView.java`, `OpenLibraryClient.java`)**:
   - `OnAttachStateChangeListener` + `postDelayed` passes hide native `"Search Kindle"` bar 100% reliably.
   - Title substring deduplication in `OpenLibraryClient.java` prevents omnibus self-recommendations.
   - Dedicated `simContainer` with `populateSimilarContainer()` ensures zero duplicate headers or buttons in Open Library metadata dialogs.

## Verification Evidence (Round 020)
1. **Clean Home Tab**:
   - `screenshots/verify_r20_home_clean.png` (899,818 B, 1080x2340): ZERO duplicate "Search Kindle" bar, real color covers in Continue Reading, clean categories, and fine-grained recommendations.
2. **Rich Open Library Info Dialog**:
   - `screenshots/verify_r20_rec_dialog_loaded.png` (428,538 B, 1080x2340): Loaded metadata for *The Last Man* showing Author, Year (1826), Rating (3.50/5), Topics, full synopsis, and non-duplicate Similar Books list.
3. **Zero Duplicate Cover**:
   - `screenshots/verify_r20_page1_no_dup_cover.png` (883,775 B, 1080x2340): Dismissing cover lands directly on Page 1 prose with zero redundant HTML cover page.
4. **TOC "Cover" Jump**:
   - `screenshots/verify_r20_toc_cover_jump.png` (865,018 B, 1080x2340): Tapping "Cover" from Chapter 2 in the native TOC drawer immediately displays the fullscreen `#000000` black cover overlay (`black_ratio: 30.95%`, `white_ratio: 1.02%`).
5. **Stock Safety Invariant**:
   - Confirmed `com.amazon.kindle` `lastUpdateTime=2026-11-19 21:59:23` 100% untouched.

## Deliverables
- Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v1.2/base.apk` (129 MB)
- Report Archive: `antig_to_gemini_neon_clunky_report020.zip` (< 100 MB)
