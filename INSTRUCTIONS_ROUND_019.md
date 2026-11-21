# Round 019 Instructions — Deep Cover Page Reverse-Engineering (Zero Hacks/Overfitting) + Fix Continue Reading Launch + Fine-Grained Open Library Recommendations

**Target Package:** `com.amazon.kindle.epub` ("Folio")  
**HARD SAFETY INVARIANT:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never write, force-stop, clear data, or modify stock `com.amazon.kindle`).  
**Autonomous Relay Mode:** Execute all tasks end-to-end and upload `antig_to_gemini_neon_clunky_report019.zip` (`< 100 MB`) directly to this chat via Chrome Dev Bridge without pausing.

---

## Project Lead Feedback & Non-Negotiable Quality Standards

1. **ZERO OVERFITTING**: Never hardcode or special-case any specific book title (e.g. *Metro 2034*, *Moby Dick*, etc.). Every feature must be 100% general and verified across **multiple distinct books**.
2. **Cover Page Rendering**: Our temporary `CoverOverlayView` overlay only fired on initial launch and still left the book's actual cover page rendering with left/right white margins. We must **investigate how stock Kindle actually renders cover pages natively** (separate native cover element / full-bleed black canvas / zero margins / swipe back-and-forth to Cover) and reproduce THAT properly for ALL books!
3. **"Continue Reading" on Home Tab**: Tapping a book in Continue Reading currently shows `"Open ... from your Library tab"` instead of actually opening the book! Fix it so tapping ANY book in Continue Reading opens that exact book directly in the reader!
4. **Fine-Grained Open Library Recommendations**: Must recommend genuinely similar books via Open Library API (using curated work subjects, multi-subject intersection, era proximity, and same-author works) **even when no free download exists**, displaying rich book info (cover, author, publish year, rating, topics, synopsis) cleanly on tap!

---

## Tasks

### Task A — Deep Reverse-Engineering of Stock Kindle's Native Cover Page Rendering
1. Inspect `classes*.dex` (from `v8.159.0.100` base APK) and `libKRF.so` / `libWebCoreKRF.so` to find how **stock Kindle** renders a book's Cover page fullscreen on `#000000` black with zero left/right margins and zero `0%` footer:
   - Search for `CoverPage`, `isCover`, `hasCoverPage`, `isOnCoverPage`, `CoverImage`, `BlackBackground`, `MobiDocViewer`, `KRIFDocViewer`, `BookReaderActivity`, `ReaderFragment`, `BaseAnimatedPageFlipper`, and `z8.i` margin / background logic.
   - Check how stock Kindle handles swiping left from Page 1 onto Cover, and swiping right from Cover onto Page 1!
   - Check whether stock Kindle uses:
     - (a) A native Android `ImageView` / `CoverPageFragment` / `ViewPager` page 0 outside WebCore, OR
     - (b) A `KRF` / `MobiDocViewer` cover-page flag (`EXTH 201` + `guide` INDX `type="cover"` / `isCoverPage()`) that switches the reader view's background to `#000000`, zeroes margins, and hides the footer!
2. Document the exact classes, methods, and bytecode in **`logs/stock_kindle_cover_architecture.txt`**.
3. If the native mechanism is a clean fix we can apply immediately (or if you need to remove the old `CoverOverlayView` `onResume` hack and replace it with the true native cover page handler), implement and test it on **at least 2 completely different books** (e.g. *Alice's Adventures in Wonderland* AND *Frankenstein* or *Moby Dick*), verifying:
   - Full-width centered cover on `#000000` black with **zero left/right white margins** on BOTH books!
   - Smooth navigation between Cover and Page 1!
   - Capture **`screenshots/verify_r19_cover_book1.png`** and **`screenshots/verify_r19_cover_book2.png`**.

### Task B — Fix "Continue Reading" on Home to Directly Open ANY Library Book
1. Inspect the exact schema and sample rows of Folio's `kindle_library.db` (`PRAGMA table_info(KindleContent);`, `PRAGMA table_info(UserContent);`, plus 5 sample rows including `ID`, `TITLE`, file path / URI columns).
2. Update `LocalLibraryReader.java` and `OpenCatalogView.java` so tapping **ANY** book in **Continue Reading** launches that exact book directly in Folio's reader (using Kindle's own internal `ReaderController.openBook(...)` / `StandAloneBookReaderActivity` intent with `ASIN`/`ID` OR the exact resolved file path from `kindle_library.db` + `epub_cache` + local folders — NEVER falling back to `"Open from Library tab"`!).
3. Verify on-device by tapping **2 different books** in **Continue Reading** on the Home tab (including *Metro 2034* and *Alice's Adventures in Wonderland* or *Moby Dick*), confirming BOTH open immediately into the reader! Capture **`screenshots/verify_r19_continue_reading_opened.png`**.

### Task C — Deploy Fine-Grained, Non-Overfitted Open Library Recommendations
1. Copy `OpenLibraryClient.java` and `OpenCatalogView.java` from this package into `<MOD_ROOT>/mod_src/real/com/amazon/kindle/epub/catalog/` (merging your Task B `LocalLibraryReader` / Continue Reading open fix cleanly!).
2. Rebuild & deploy **Folio** (`com.amazon.kindle.epub`) and confirm stock `com.amazon.kindle` is 100% untouched.
3. Open Folio's **Home** tab, wait ~6s for Open Library's multi-subject intersection & same-author queries to load, and capture **`screenshots/verify_r19_home_fine_grained_recs.png`** verifying genuinely relevant, on-genre recommendations (NO *Wizard of Oz* for a post-apocalyptic sci-fi novel!).
4. Tap a recommended book that is NOT in Project Gutenberg to verify the rich **Open Library Info Dialog** (cover, author, first published year, rating, topics, synopsis, "Search Catalog") opens cleanly! Capture **`screenshots/verify_r19_rec_info_dialog.png`**.

### Task D — Package & Autonomous Upload
Create `antig_to_gemini_neon_clunky_report019.zip` (`< 100 MB`) containing `REPORT.md`, `scripts/`, `screenshots/`, and `logs/`, and upload directly to this chat via Chrome Dev Bridge.
