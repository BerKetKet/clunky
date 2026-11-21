# Round 015 Instructions — Flagship Home Screen Carousels + Fullscreen Black Cover Page + Spacing/Theme Parity Investigation

**Target Package:** `com.amazon.kindle.epub` ("Folio")  
**HARD SAFETY INVARIANT:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never write, force-stop, clear data, or modify stock `com.amazon.kindle`).  
**Autonomous Relay Mode:** Execute all tasks end-to-end and upload `antig_to_gemini_neon_clunky_report015.zip` (`< 100 MB`) directly to this chat via Chrome Dev Bridge without pausing.

---

## User Feedback Addressed in Round 015

1. **Home Page Redesign (`OpenCatalogView.java` + `LocalLibraryReader.java` + `OpenLibraryClient.java`)**:
   - Replaced the plain dark text list with a modern, Kindle-caliber dark editorial Home screen featuring:
     - Rounded search bar + pill buttons (`+ OPDS`, `Backup`) + Genre quick chips (`Sci-Fi`, `Fantasy`, `Mystery`, `Horror`, `Classic`, `History`, `Philosophy`, `Adventure`).
     - **"Continue Reading"** horizontal cover carousel reading directly from Folio's `kindle_library.db` + local covers.
     - **"Because You Read [Book]"** personalized recommendations horizontal cover carousel seeded from your actual local Library via **Open Library API** (`subjects` + `covers.openlibrary.org`).
     - **"Popular Free EPUBs"** horizontal cover carousel with async-loaded real book covers.
     - Cover-illustrated search result rows (no bare text lists).
2. **Fullscreen Black Cover Page (`EpubTranscoder.java`)**:
   - Kept high-resolution covers (up to `1080×1600`, `<= 350 KB`) instead of crushing them to `450×600`.
   - Styled the Cover spine page with a full-bleed `#000000` black background (`margin: 0; padding: 0; background-color: #000000`) and full-width vertically centered cover image.
3. **Condensed Page / Missing `Aa → Font → Spacing` Fix & Deep Investigation**:
   - Added `sanitizeEpubCssForKindle()` in `EpubTranscoder.java` to strip rigid EPUB `line-height`, `letter-spacing`, `word-spacing`, and fixed `px`/`pt` `font-size` rules that prevented Kindle's native `Aa` theme & spacing controls from breathing.
   - **AND (Task C)**: Disassemble/inspect the `Aa` menu's **"Spacing / Adjust text spacing of the page"** gate in `classes*.dex` so we know the exact smali condition that enables `Font → Spacing` on KFX books vs hides it on KF8 (`MOBI8`) books — and check how stock Kindle renders full-bleed black cover pages (whether `MobiDocViewer` has a native cover-viewer mode or CSS viewport margin override)!

---

## Tasks

### Task A — Build & Deploy Round 015
1. Copy all 4 Java files into `<MOD_ROOT>/mod_src/real/com/amazon/kindle/epub/`:
   - `EpubTranscoder.java`
   - `catalog/LocalLibraryReader.java` (new file!)
   - `catalog/OpenLibraryClient.java`
   - `catalog/OpenCatalogView.java`
2. Run `clone_and_deploy_epub_mod.sh` to compile `classes4.dex` and deploy **Folio** (`com.amazon.kindle.epub`).
3. Confirm stock `com.amazon.kindle` is 100% untouched.

### Task B — Verify Redesigned Home Screen, Fullscreen Black Cover & Spaced Prose
1. Launch **Folio** on the **Home** tab, wait ~4 seconds for covers and Open Library recommendations to load, and capture **`screenshots/verify_r15_home_carousels.png`** (showing Continue Reading + Because You Read ... + Popular Free EPUBs cover carousels!).
2. Clear cached `.azw3` files in `/data/data/com.amazon.kindle.epub/files/epub_cache/` and open a book with a cover (e.g. *Metro 2034* if in `/sdcard/kindle/`, or *Moby Dick* / *Alice*):
   - Navigate to the **Cover** page and capture **`screenshots/verify_r15_cover_black.png`**.
   - Navigate to **Chapter 1** prose and capture **`screenshots/verify_r15_spaced_prose.png`**.
   - Open the **`Aa`** reader menu and capture BOTH **`screenshots/verify_r15_aa_font.png`** and **`screenshots/verify_r15_aa_layout.png`**.

### Task C — Reverse-Engineer `Font → Spacing` Gate & Native Fullscreen Cover Mode
1. Search the decoded `classes*.dex` (from `v8.159.0.100` base APK) for:
   - `"Adjust text spacing of the page"` / `Spacing` panel visibility gate (find the exact class/method and boolean check that hides `Spacing` for `MOBI8`/KF8 vs KFX!).
   - Native cover page fullscreen black rendering (`cover` / `isCover` / `getCover` / `MobiDocViewer` margins on cover page).
2. Save your exact findings (class names, method signatures, smali snippets) to **`logs/aa_spacing_and_cover_analysis.txt`** so we can unlock native `Aa → Font → Spacing` and pixel-perfect fullscreen black cover rendering in Round 016!

### Task D — Package & Autonomous Upload
Create `antig_to_gemini_neon_clunky_report015.zip` (`< 100 MB`) containing `REPORT.md`, `scripts/`, `screenshots/`, and `logs/`, and upload directly to this chat via Chrome Dev Bridge.
