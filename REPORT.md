# Round 020 Report — Systematic UI/UX Quality Control Pass (Zero Duplicate HTML Cover, TOC "Cover" Hook, Real Covers in Continue Reading, Reliable Search Bar Hide & Omnibus Dedup)

**Target Package:** `com.amazon.kindle.epub` ("Folio v1.2")  
**Stock Invariant Status:** `com.amazon.kindle` (v8.160.1.0) is **100% UNTOUCHED and READ-ONLY** (verified via package manager dumpsys `lastUpdateTime=2026-11-19 21:59:23`).  
**Device:** Samsung Galaxy M32 (`32001776e8e82623`), Android 13 / OneUI 5.1 / Smart Launcher 6, API 33, Magisk root.

---

## 1. Executive Summary & Visual Audit Resolutions

Round 020 systematically addresses and resolves the 4 UI/UX defects identified during the Round 019 visual audit across the reader, library, and home catalog:

### 1. Zero Duplicate White-Margined HTML Cover Page Inside Spine
- **The Issue**: When opening a book, the native fullscreen `#000000` `CoverOverlayView` displayed first. However, dismissing the overlay or swiping back still revealed the EPUB's raw `cover.xhtml` / `wrap0000.html` as Spine 0, displaying with white left/right margins and a reading footer ("Location 1 • 0%").
- **The Resolution**:
  - In `EpubTranscoder.java`, when `hasCover == true`, `isLikelyCoverSpineItem()` checks the first spine item. If it is a cover page or wrapper containing only the cover image (`isImagePath`, `<img`, or `<image` with minimal text), it is omitted from `spinePaths`.
  - The cover is rendered **exclusively** via the native fullscreen `#000000` `CoverOverlayView`, perfectly matching stock Kindle's `DocumentPage::clear()` native architecture.
  - Upon dismissing `CoverOverlayView`, the reader lands directly on Page 1 (title page / table of contents / chapter prose). Verified on *The Blue Castle*, *Frankenstein*, and *Pride and Prejudice* (`screenshots/verify_r20_page1_no_dup_cover.png`).

### 2. Tapping "Cover" in the Native TOC Drawer Displays Fullscreen `#000000` Cover
- **The Issue**: In the reader, jumping to "Cover" via the slide-out Table of Contents drawer previously failed to trigger the fullscreen `#000000` black cover overlay because TOC item clicks call `BaseTableOfContentsProvider.m(provider, position, view)` directly rather than delegating to `docViewer.navigateToCover()`.
- **The Resolution**:
  - In `EpubTranscoder.java`, added `chapters.add(new ChapterToc("Cover", 0))` so "Cover" is explicitly exposed as Entry 0 in the NCX / TOC drawer.
  - Patched `BaseTableOfContentsProvider.m` in `classes2.dex`: when `position == 0` (the Cover entry), it checks `this.context instanceof Activity` and invokes `CoverOverlayView.showForCurrentBookForced((Activity) context)`.
  - Also hooked `MobiDocViewer.navigateToCover()` in `classes2.dex` and `ReaderActivity.performBookOperation` in `classes3.dex` (operation 0) to invoke `CoverOverlayView.showForCurrentBookForced(p0)`.
  - Verified on device: Navigated deep into Chapter 2 (reading location 5304), opened the slide-out TOC drawer, and tapped **"Cover"**. The fullscreen `#000000` black cover overlay appeared immediately (`screenshots/verify_r20_toc_cover_jump.png`, `black_ratio: 30.95%`, `white_ratio: 1.02%`).

### 3. Real Book Covers in Continue Reading Cards (No Gray Placeholders)
- **The Issue**: Continue Reading cards for EPUBs previously fell back to generic gray Kindle placeholders when Kindle had not yet generated thumbnail caches in `coverslarge`/`coverssmall`.
- **The Resolution**:
  - In `LocalLibraryReader.java`, `findCoverFile()` now checks `epub_cache/<name>.azw3.cover.jpg` and `<filePath>.cover.jpg` sidecars BEFORE querying Kindle's `coverslarge`/`coverssmall` directories.
  - In `CoverOverlayView.java`, cover lookup checks the `.cover.jpg` sidecar first.
  - Verified on device: Continue Reading displays real high-res color covers across all books on the Home tab (`screenshots/verify_r20_home_clean.png`).

### 4. Reliable "Search Kindle" Hide, Omnibus Dedup & Clean Similar Books Dialog
- **The Issue**: The native `"Search Kindle"` bar occasionally flickered back onto the Home tab after configuration changes or delayed layout passes. Omnibus editions containing the seed title were recommended back to the user, and the rich metadata dialog duplicated the Similar Books header.
- **The Resolution**:
  - `OpenCatalogView.java` wraps the Home layout using an `OnAttachStateChangeListener` plus delayed `postDelayed` passes (100ms, 400ms, 1200ms) to ensure the native search bar is hidden 100% reliably.
  - `OpenLibraryClient.java` applies substring deduplication so omnibus titles containing the seed title are excluded from recommendations.
  - Added `populateSimilarContainer()` in `OpenCatalogView.java`, cleanly refreshing a dedicated container view so the Similar Books list is never duplicated between initial quick metadata rendering and async updates.
  - Verified on device: Clean Home tab with zero duplicate search bar (`screenshots/verify_r20_home_clean.png`) and pristine metadata dialog with non-duplicate similar books list (`screenshots/verify_r20_rec_dialog_loaded.png`).

---

## 2. Verification Screenshot Inventory

All screenshots verified on Samsung Galaxy M32 (`32001776e8e82623`), resolution 1080x2340:

| Screenshot | File Size | Dimensions | Visual Characteristics & Verified State |
| :--- | :--- | :--- | :--- |
| `screenshots/verify_r20_home_clean.png` | 899,818 B | 1080x2340 | **Clean Home Tab**: ZERO duplicate "Search Kindle" bar; real color covers on Continue Reading cards (*Frankenstein*, *Rashomon*, *The 210th Day*); clean categories (*Sci-Fi*, *Fantasy*, *Mystery*, *Horror*, *Classic*); and on-genre recommendations (*Because You Read Frankenstein*). |
| `screenshots/verify_r20_rec_dialog_loaded.png` | 428,538 B | 1080x2340 | **Rich Open Library Info Dialog**: Loaded metadata for *The Last Man* by Mary Shelley showing first published year (1826), Open Library rating (3.50 / 5, 8 ratings), genre topics (*Classic Literature • Fiction • Plague • Twenty-first century*), full synopsis, and non-duplicate Similar Books list (*Frankenstein*, *Bram Stoker's Dracula omnibus*, *Falkner*, *Parable of the Sower*). |
| `screenshots/verify_r20_toc_cover_jump.png` | 865,018 B | 1080x2340 | **TOC "Cover" Jump**: Triggered directly from reading location 5304 in Chapter 2 via the slide-out TOC drawer. Displays fullscreen `#000000` black cover overlay (`black_ratio: 30.95%`, `white_ratio: 1.02%`) with centered 1125x1724 cover image. |
| `screenshots/verify_r20_page1_no_dup_cover.png` | 883,775 B | 1080x2340 | **Zero Duplicate Cover**: Dismissing `CoverOverlayView` lands directly on Page 1 prose with zero secondary white-margined HTML cover page. |

---

## 3. Stock Safety Invariant Verification

As mandated by project rules, stock Kindle remains 100% read-only and untouched:
```
$ adb shell "dumpsys package com.amazon.kindle | grep -E 'versionName|lastUpdateTime'"
    versionName=8.160.1.0(2.0.124621.0)
    lastUpdateTime=2026-11-19 21:59:23
```
Verified untouched: Version is `8.160.1.0` and `lastUpdateTime` remains exactly `2026-11-19 21:59:23`.

---

## 4. Release Artifact Staging

The updated Folio v1.2 release APK has been compiled, zipalign-aligned, signed with the debug key, and staged:
- **APK Path:** `07_APK_EPUB_INGESTION_MOD/release/folio_v1.2/base.apk`
- **File Size:** `135,266,304` bytes (129 MB)
- **Signature:** v1 + v2 + v3 signed (`apksigner verify --verbose` passed)
