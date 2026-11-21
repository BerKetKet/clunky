# Round 020 Instructions — Systematic UI/UX Quality Control Pass (Zero Duplicate HTML Cover, TOC "Cover" Hook, Real Covers in Continue Reading, Reliable Search Bar Hide & Omnibus Dedup)

**Target Package:** `com.amazon.kindle.epub` ("Folio")  
**HARD SAFETY INVARIANT:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never write, force-stop, clear data, or modify stock `com.amazon.kindle`).  
**Autonomous Relay Mode:** Execute all tasks end-to-end and upload `antig_to_gemini_neon_clunky_report020.zip` (`< 100 MB`) directly to this chat via Chrome Dev Bridge without pausing.

---

## Round 019 QC Visual Audit — 4 General Bugs Fixed in Round 020

Inspecting Round 019's screenshots caught 4 concrete UX issues across the app:
1. **Duplicate white-margined HTML cover page inside the spine**: When dismissing `CoverOverlayView` or swiping back, the book's raw `cover.xhtml` still existed as Spine 0 with white left/right margins. `EpubTranscoder.java` now omits the redundant cover-only HTML spine item whenever `hasCover == true`, so the cover is rendered **exclusively** via native fullscreen `#000000` `CoverOverlayView` (matching stock Kindle's `DocumentPage::clear()` architecture!).
2. **Tapping "Cover" in the native TOC drawer**: Added `CoverOverlayView.showForCurrentBookForced(Activity)` — hook `MobiDocViewer.navigateToCover()` (or the TOC Cover tap handler) so tapping **"Cover"** in the TOC drawer at any reading position opens the exact same fullscreen `#000000` native cover!
3. **Generic gray placeholder covers in Continue Reading (`Metro 2034`, `Rashomon`)**: `LocalLibraryReader.java` now checks `epub_cache/<name>.azw3.cover.jpg` and `<filePath>.cover.jpg` BEFORE Kindle's `coverslarge`/`coverssmall` directories so real book covers are never eclipsed by Kindle's gray placeholder! (Also verify `CoverOverlayView` checks `.cover.jpg` sidecar BEFORE `coverslarge`!)
4. **Duplicate `"Search Kindle"` bar reappearing on Home + Omnibus recommendation dedup**:
   - `OpenCatalogView.wrap()` now uses `OnAttachStateChangeListener` + delayed `postDelayed` passes to hide the native `"Search Kindle"` bar reliably 100% of the time.
   - `OpenLibraryClient.java` now performs general substring deduplication so omnibus editions containing the seed book's title aren't recommended back as "similar" books.

---

## Tasks

### Task A — Deploy All 5 Updated Java Modules + Hook `navigateToCover()`
1. Copy from this package into `<MOD_ROOT>/mod_src/real/com/amazon/kindle/epub/`:
   - `EpubTranscoder.java`
   - `CoverOverlayView.java`
   - `catalog/LocalLibraryReader.java`
   - `catalog/OpenLibraryClient.java`
   - `catalog/OpenCatalogView.java`
2. In `CoverOverlayView.java`, ensure the real `.cover.jpg` sidecar is checked BEFORE `coverslarge`/`coverssmall` (preventing gray placeholders when opening books from Continue Reading!).
3. Hook `MobiDocViewer.navigateToCover()` (in `classes2.dex` offset `0x2e2208`, or the TOC "Cover" click handler) to invoke `CoverOverlayView.showForCurrentBookForced(activity)` so tapping **"Cover"** in the native slide-out TOC drawer at ANY reading position displays the fullscreen `#000000` black cover cleanly!
4. Compile, rebuild, and deploy **Folio** (`com.amazon.kindle.epub`). Confirm stock `com.amazon.kindle` is 100% untouched.

### Task B — Multi-Book UI/UX Verification Sweep
1. Clear `epub_cache/` so books re-transcode cleanly without redundant HTML cover spine items.
2. **Home Tab QC**: Open Folio's **Home** tab, wait ~6s for all covers and Open Library recommendations to finish loading, and capture **`screenshots/verify_r20_home_clean.png`** confirming:
   - ZERO duplicate `"Search Kindle"` bar!
   - Real color covers on **Continue Reading** cards (no gray placeholder on EPUBs with covers)!
   - Clean, non-duplicate, on-genre **Because You Read ...** recommendations with loaded covers!
3. **Rich Open Library Info Dialog QC**: Tap a recommended book, wait ~4s until Open Library ratings, year, topics, and full synopsis finish loading (NOT the `"Fetching..."` state!), and capture **`screenshots/verify_r20_rec_dialog_loaded.png`**.
4. **Native Cover + TOC "Cover" Jump QC (2 distinct books)**:
   - Open a book, dismiss the fullscreen `#000000` cover, verify Page 1 lands directly on the title/contents/Chapter 1 (NO second white-margined HTML cover page!), navigate to Chapter 2, then open the native TOC drawer and tap **"Cover"** — verify the fullscreen `#000000` black cover appears immediately!
   - Capture **`screenshots/verify_r20_toc_cover_jump.png`** and **`screenshots/verify_r20_page1_no_dup_cover.png`**.
5. Stage updated signed APKs into `07_APK_EPUB_INGESTION_MOD/release/folio_v1.2/`.

### Task C — Package & Autonomous Upload
Create `antig_to_gemini_neon_clunky_report020.zip` (`< 100 MB`) containing `REPORT.md`, `scripts/`, `screenshots/`, and `logs/`, and upload directly to this chat via Chrome Dev Bridge.
