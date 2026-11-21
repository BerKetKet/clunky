# Round 016 Instructions — Unlock `Aa → Font → Spacing`, True Edge-to-Edge Fullscreen Black Cover, and Home Screen Polish

**Target Package:** `com.amazon.kindle.epub` ("Folio")  
**HARD SAFETY INVARIANT:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never write, force-stop, clear data, or modify stock `com.amazon.kindle`).  
**Autonomous Relay Mode:** Execute all tasks end-to-end and upload `antig_to_gemini_neon_clunky_report016.zip` (`< 100 MB`) directly to this chat via Chrome Dev Bridge without pausing.

---

## Round 015 Visual Review & Round 016 Goals

Round 015's redesigned Home screen (`verify_r15_home_carousels.png`) and spaced prose (`verify_r15_spaced_prose.png`) are a HUGE leap forward! Three targeted fixes will make Folio match or surpass stock Kindle:

1. **Unlock `Aa → Font → Spacing` ("Adjust text spacing of the page.")**:
   - Your Round 015 bytecode analysis pinpointed the exact gate:
     - `com.amazon.kindle.viewoptions.textspacing.TextSpacingModel.getTextSpacingWidgetEnabled` in `classes6.dex` (`0x1e4138`)
     - `com.amazon.android.docviewer.KindleDocViewerImpl.hasLineSpacingV2Support()`, `hasLetterSpacingSupport()`, `hasParagraphSpacingSupport()`, `hasWordSpacingSupport()` in `classes2.dex` (`0x2cc7a4..0x2cc7e2`).
2. **True Edge-to-Edge Fullscreen `#000000` Cover Page (Zero White Border)**:
   - In `verify_r15_cover_black.png`, the inner WebCore viewport was black with a crisp full-width cover, **but** Kindle's native reader margins (`z8.i` / `MobiDocViewer.h1()`) and footer (`Location 2 of 9716 • 0%`) still painted a white border around the black cover.
3. **Home Screen Polish (`OpenCatalogView.java` + `OpenLibraryClient.java`)**:
   - **No duplicate search bar**: Added `hideRedundantKindleSearchBar()` in `OpenCatalogView.wrap()` to collapse Kindle's native `"Search Kindle"` bar on the Home tab so only Folio's sleek search bar is shown and all carousels sit cleanly above the fold.
   - **On-genre Open Library Recommendations**: Filtered out overly generic Open Library subjects (`fiction`, `novel`, `literature`, `general`, `stories`, `juvenile_fiction`, etc.) and increased subject scan depth so **"Because You Read Metro 2034"** shows real sci-fi / post-apocalyptic / thriller recommendations instead of *The Wizard of Oz*!

---

## Tasks

### Task A — Unlock `Aa → Font → Spacing` in `classes2.dex` / `classes6.dex` & Test Live Sliders
1. Patch `TextSpacingModel.getTextSpacingWidgetEnabled` (in `classes6.dex` via `patch_classes5.py`) AND/OR `KindleDocViewerImpl.hasLineSpacingV2Support()` / `hasLetterSpacingSupport()` / `hasParagraphSpacingSupport()` / `hasWordSpacingSupport()` (in `classes2.dex`) so **`Aa → Font → Spacing`** is enabled for MOBI8/KF8 books!
2. Before finalizing the patch, inspect what `MobiDocViewer` does when the Spacing sliders are dragged (`setLineSpacingV2`, `setParagraphSpacing`, `setWordSpacing`, `setLetterSpacing`):
   - Make sure dragging any slider does NOT crash `MobiDocViewer` (if any setter throws `UnsupportedOperationException`, patch it cleanly or route `setLineSpacingV2` to `MobiDocViewer`'s native `setLineSpacing`!).
3. Verify on-device:
   - Open `Aa → Font` on *Metro 2034* (or *Moby Dick*) and capture **`screenshots/verify_r16_aa_font_spacing.png`** showing **"Spacing — Adjust text spacing of the page."** visible!
   - Tap **Spacing** to open the slider sub-panel, capture **`screenshots/verify_r16_spacing_sliders.png`**, adjust a slider, and log the observed behavior in `logs/spacing_hook_test.txt`.

### Task B — True Edge-to-Edge Fullscreen `#000000` Cover Page (Zero White Frame)
1. Hook the Cover page viewport in `MobiDocViewer` / `z8.i` (or `StandAloneBookReaderActivity`) so when displaying the Cover page (position 0 / FID 0 / Cover), the reader background is `#000000`, margins are zeroed (`0` padding / `Margin.ZERO`), and the white outer frame disappears completely—while restoring normal margins and page color immediately on page 2 / Chapter 1!
2. Verify on *Metro 2034* (or *Moby Dick*) Cover page and capture **`screenshots/verify_r16_cover_edge_to_edge.png`** (and **`screenshots/verify_r16_chapter1_margins_restored.png`** confirming normal margins return on Chapter 1!).

### Task C — Deploy Home Screen Polish & Verify
1. Copy `OpenCatalogView.java` and `OpenLibraryClient.java` from this package into `<MOD_ROOT>/mod_src/real/com/amazon/kindle/epub/catalog/`.
2. Rebuild & install **Folio** (`com.amazon.kindle.epub`) and confirm stock `com.amazon.kindle` is 100% untouched.
3. Open Folio's **Home** tab, wait ~4s for covers and recommendations to load, and capture **`screenshots/verify_r16_home_polished.png`** confirming:
   - No duplicate `"Search Kindle"` bar above Folio!
   - On-genre **"Because You Read Metro 2034"** recommendations with covers!

### Task D — Package & Autonomous Upload
Create `antig_to_gemini_neon_clunky_report016.zip` (`< 100 MB`) containing `REPORT.md`, `scripts/`, `screenshots/`, and `logs/`, and upload directly to this chat via Chrome Dev Bridge.
