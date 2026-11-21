# Round 017 Instructions — Native Fullscreen `#000000` Cover Overlay (Outside Reflow Spine) + Live `Aa → Font → Spacing` Verification + Polished Home Screen

**Target Package:** `com.amazon.kindle.epub` ("Folio")  
**HARD SAFETY INVARIANT:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never write, force-stop, clear data, or modify stock `com.amazon.kindle`).  
**Autonomous Relay Mode:** Execute all tasks end-to-end and upload `antig_to_gemini_neon_clunky_report017.zip` (`< 100 MB`) directly to this chat via Chrome Dev Bridge without pausing.

---

## Course Correction Applied (Per Project Lead)

Instead of injecting synthetic cover HTML into the KF8 reflow spine and fighting `z8.i` / `MobiDocViewer` margins:
1. **`EpubTranscoder.java`** now extracts and writes a standalone high-res cover JPEG sidecar (`<outFile>.cover.jpg`, up to `1080×1600`) alongside the `.azw3` file, and removes the synthetic in-spine black cover HTML hack.
2. **`CoverOverlayView.java`** (new class!) renders the extracted cover image natively on a pure `#000000` black edge-to-edge fullscreen Android `FrameLayout` (`decorView`, immersive fullscreen, zero margins, zero `"0%"` footer, centered full-width `FIT_CENTER` `ImageView`). Tap or swipe left smoothly fades straight into Page 1 of the book!

---

## Tasks

### Task A — Wire Native Fullscreen `CoverOverlayView` & Deploy Round 017
1. Copy into `<MOD_ROOT>/mod_src/real/com/amazon/kindle/epub/`:
   - `CoverOverlayView.java` (new!)
   - `EpubTranscoder.java` (updated: writes `<outFile>.cover.jpg`, removes in-spine black cover HTML)
2. Keep your Round 016 work already in place:
   - `OpenCatalogView.java` (hides redundant `"Search Kindle"` bar) & `OpenLibraryClient.java` (filters generic subjects for on-genre recommendations)
   - `Aa → Font → Spacing` bytecode patches (`TextSpacingModel` in `classes6.dex` + `KindleDocViewerImpl` in `classes2.dex`)
3. Wire `CoverOverlayView.showForBookFile(activity, bookPath)` into `StandAloneBookReaderActivity` (or via `dp.g` / `ThirdPartyUpgradePage` hook when opening a book or when the user taps **"Cover"** in the native TOC drawer!).
   - Specifically: whenever a book is opened cleanly (or when Cover is selected), show `CoverOverlayView` directly over the reader's `decorView` so it displays edge-to-edge on pure `#000000` black with zero white frame and zero `"0%"` footer!
   - Swiping left or tapping dismisses `CoverOverlayView` immediately into the book's reflowable reader with normal margins intact!
4. Compile, rebuild, and deploy **Folio** (`com.amazon.kindle.epub`). Confirm stock `com.amazon.kindle` is 100% untouched.

### Task B — Make `Aa → Font → Spacing` Line Spacing Slider Actually Update Live (If Possible)
1. Check `KindleDocViewerImpl.setLineSpacingV2(float)` (or `MobiDocViewer.setLineSpacing(...)`) in `classes2.dex`:
   - If `MobiDocViewer` has a native `setLineSpacing(KindleDocLineSettings$LineSpacing)` method, bridge `setLineSpacingV2(float)` to map `< 1.25f -> SMALL`, `1.25f..1.65f -> MEDIUM`, `> 1.65f -> LARGE` so dragging the Line Spacing slider in `Aa → Font → Spacing` visibly updates the page live!
   - Log exact setter wiring and slider test results in `logs/r17_spacing_live_test.txt`.

### Task C — Hardware Verification & Screenshots
Capture and verify ALL FIVE screenshots on-device:
1. **`screenshots/verify_r17_cover_fullscreen_native.png`**: Opening *Metro 2034* (or *Moby Dick*) showing the **native fullscreen `#000000` black `CoverOverlayView`** — full-width cover, edge-to-edge pure black, ZERO white borders, ZERO `"0%"` footer!
2. **`screenshots/verify_r17_after_cover_dismiss.png`**: Immediately after swiping left / tapping the cover overlay, showing Page 1 / Chapter 1 prose with clean normal margins!
3. **`screenshots/verify_r17_aa_font_spacing.png`**: `Aa → Font` menu showing **"Spacing — Adjust text spacing of the page."** unlocked and visible!
4. **`screenshots/verify_r17_spacing_sliders.png`**: Tapping **Spacing** to show the native Spacing slider sub-panel open and responsive without crashes!
5. **`screenshots/verify_r17_home_polished.png`**: Folio **Home** tab showing NO duplicate `"Search Kindle"` bar and on-genre **"Because You Read Metro 2034"** cover recommendations!

### Task D — Package & Autonomous Upload
Create `antig_to_gemini_neon_clunky_report017.zip` (`< 100 MB`) containing `REPORT.md`, `scripts/`, `screenshots/`, and `logs/`, and upload directly to this chat via Chrome Dev Bridge.
