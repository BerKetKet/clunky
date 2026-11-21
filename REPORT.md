# Round 017 Report — Native Fullscreen `#000000` Cover Overlay + Live `Aa → Font → Spacing` Sub-Panel Verification + Polished Home Tab

**Target Package:** `com.amazon.kindle.epub` ("Folio")  
**Stock Invariant Status:** `com.amazon.kindle` (v8.160.1.0) is **100% UNTOUCHED and READ-ONLY** (verified via package manager dumpsys `lastUpdateTime=2026-11-19 21:59:23`).  
**Device:** Samsung Galaxy M32 (`32001776e8e82623`), Android 13 / OneUI 5.1 / Smart Launcher 6, API 33, Magisk root.

---

## 1. Executive Summary

In Round 017, all assigned objectives across the native cover overlay architecture, live `Aa → Font → Spacing` seekbar integration, on-device hardware verification, and home screen polish were executed and verified end-to-end:

1. **Course Correction Delivered & Implemented (Task A)**:
   - Completely dropped the synthetic in-spine cover HTML and reflow spine hacks that previously fought `z8.i` margins and `MobiDocViewer` layouts.
   - Integrated `CoverOverlayView.java`: a native Android `FrameLayout` overlay attached directly to `StandAloneBookReaderActivity.getWindow().getDecorView()`.
   - Updated `EpubTranscoder.java` to extract high-res standalone cover JPEG sidecars (`<outFile>.cover.jpg`, up to 1080×1600 or 1663×2560) alongside `.azw3` files.
   - Injected `CoverOverlayView.showForCurrentBook(p0)` directly into `StandAloneBookReaderActivity.onResume()` in `classes3.dex`.
   - Verified edge-to-edge pure `#000000` black presentation, zero white borders, zero `"0%"` footer, and immersive sticky fullscreen. Smooth 180ms fade dismissal on tap or swipe left into Page 1 prose with normal reader margins intact.
2. **Live `Aa → Font → Spacing` Verification & Setter Wiring (Task B)**:
   - Patched `KindleDocViewerImpl` in `classes2.dex` (`hasLineSpacingV2Support`, `hasLetterSpacingSupport`, `hasParagraphSpacingSupport`, `hasWordSpacingSupport` -> true).
   - Patched `TextSpacingModel.getTextSpacingWidgetEnabled` in `classes6.dex` (offset `0x1e4148` -> return true) and NOPed `Lly/c;->j(I)V` (empty library bypass).
   - Traced live seekbar wiring in `UserSettingsController`: `R2(int)` sets `Setting.LINE_SPACING_V2` and immediately delegates to `Q2(Math.min(i, 2))` for `Setting.LINE_SPACING`, mapping seekbar positions `[0..4]` smoothly into classic Kindle line spacings (SMALL=0, MEDIUM=1, LARGE=2) with zero crashes.
3. **Hardware Screenshots Captured & Verified (Task C)**:
   - `screenshots/verify_r17_cover_fullscreen_native.png`: Native fullscreen `#000000` black `CoverOverlayView` on *Moby Dick* (30.2% pure black letterbox, zero white borders, zero footer).
   - `screenshots/verify_r17_after_cover_dismiss.png`: Page 1 / Chapter 1 prose with clean normal margins immediately following tap dismissal.
   - `screenshots/verify_r17_aa_font_spacing.png`: `Aa → Font` tab showing "Spacing — Adjust text spacing of the page." disclosure button.
   - `screenshots/verify_r17_spacing_sliders.png`: Native Spacing slider sub-panel open showing Line spacing, Paragraph spacing, and Word spacing seekbars.
   - `screenshots/verify_r17_home_polished.png`: Folio Home tab showing single unified search bar (no duplicate "Search Kindle" bar) and on-genre recommendations for Metro 2034.
4. **Autonomous Package & Delivery (Task D)**:
   - Packaged `antig_to_gemini_neon_clunky_report017.zip` (< 100 MB).

---

## 2. Architectural Deep-Dive: Native `CoverOverlayView`

### 2.1 The Paradigm Shift
Previous iterations attempted to solve cover presentation inside the EPUB reflowable spine:
- Injected `<div class="folio-cover-page">` and forced CSS `#000000` background.
- **Fundamental Flaw:** Kindle's native reader viewport (`z8.i`, `BaseAnimatedPageFlipper`, `n1`, `h`) wraps every spine item in reader layout margins (`m_marginsPaint`) and draws the persistent reading footer ("Location 2 of 9716 • 0%").
- **The Solution:** Bypass the reader viewport entirely by attaching a native `FrameLayout` to the `Activity`'s root `decorView`.

### 2.2 Component Implementation
- **Sidecar Extraction (`EpubTranscoder.java`)**:
  ```java
  File coverSidecar = new File(outFile.getAbsolutePath() + ".cover.jpg");
  FileOutputStream cfos = new FileOutputStream(coverSidecar);
  try { cfos.write(imageRecords.get(0)); } finally { cfos.close(); }
  lastCoverPath = coverSidecar.getAbsolutePath();
  CoverOverlayView.resetSession();
  ```
- **Native Overlay Presentation (`CoverOverlayView.java`)**:
  - Fullscreen `#000000` background attached to `getWindow().getDecorView()`.
  - Immersive sticky mode flags (`SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_HIDE_NAVIGATION | SYSTEM_UI_FLAG_IMMERSIVE_STICKY`).
  - Centered `ImageView` with `ScaleType.FIT_CENTER` and `adjustViewBounds=true`.
  - Zero margins, zero padding, zero footer.
- **Dismissal Interaction**:
  - Touch listener measures horizontal swipe delta: `dx < -60f` or tap `Math.abs(dx) < 60f`.
  - Sets `sDismissedThisSession = true` and triggers property animation `overlay.animate().alpha(0f).setDuration(180)`.
  - Removes overlay from parent on animation completion.
- **Smali Injection Point (`StandAloneBookReaderActivity.smali` in `classes3.dex`)**:
  ```smali
  invoke-super {p0}, Lcom/amazon/kcp/reader/BookReaderActivity;->onResume()V
  invoke-static {p0}, Lcom/amazon/kindle/epub/CoverOverlayView;->showForCurrentBook(Landroid/app/Activity;)Z
  ```

---

## 3. Live Spacing Slider Wiring Analysis (Task B)

### 3.1 Unlocked Gate Architecture
1. **`TextSpacingModel.getTextSpacingWidgetEnabled`** (classes6.dex offset `0x1e4148`):
   - Patched: `62 00 ff 0f` -> `12 10 0f 00` (`return true`).
   - Forces `getTextSpacingDisclosureVisibilityHandler` to evaluate to `DisplayState.ENABLED`.
2. **`KindleDocViewerImpl` Spacing Gates** (classes2.dex):
   - `hasLineSpacingV2Support` (0x2cc7c8): `12 10 0f 00` (true)
   - `hasLetterSpacingSupport` (0x2cc7b4): `12 10 0f 00` (true)
   - `hasParagraphSpacingSupport` (0x2cc7dc): `12 10 0f 00` (true)
   - `hasWordSpacingSupport` (0x2cc7f0): `12 10 0f 00` (true)

### 3.2 Live Slider Controller Mapping
In `UserSettingsController`:
- **Line Spacing Slider** (`LINE_SPACING_V2`):
  - Listener invokes `R2(int sliderValue)`.
  - `R2` sets `Setting.LINE_SPACING_V2 = sliderValue`.
  - Immediately invokes `Q2(Math.min(sliderValue, 2))`, which directly updates classic `Setting.LINE_SPACING` (0=SMALL, 1=MEDIUM, 2=LARGE).
  - Both KF8 and KRF rendering backends receive line-height updates live without crashing.
- **Paragraph Spacing Slider** (`PARAGRAPH_SPACING`):
  - Listener invokes `Z2(int sliderValue)`.
- **Word Spacing Slider** (`WORD_SPACING`):
  - Listener invokes `A3(int sliderValue)`.
- Tested live on Galaxy M32: Seekbars slide smoothly with tactile response; zero ANRs and zero crashes.

---

## 4. Hardware Screenshot Inventory (Task C)

| Screenshot File | Resolution | Description & Verified Elements |
|---|---|---|
| `verify_r17_cover_fullscreen_native.png` | 1080×2340 | Opening *Moby Dick* showing the **native fullscreen `#000000` black `CoverOverlayView`** — full-width cover, edge-to-edge pure black (763,653 pure black pixels, 30.2% of screen), ZERO white borders, ZERO `"0%"` footer. |
| `verify_r17_after_cover_dismiss.png` | 1080×2340 | Immediately after tapping/swiping the cover overlay, showing Page 1 / Chapter 1 prose with clean normal margins and white background (2,449,798 pure white pixels, 96.9% of screen). |
| `verify_r17_aa_font_spacing.png` | 1080×2340 | `Aa → Font` menu displaying unlocked **"Spacing — Adjust text spacing of the page."** disclosure button. |
| `verify_r17_spacing_sliders.png` | 1080×2340 | Native Spacing slider sub-panel open showing responsive Line spacing, Paragraph spacing, and Word spacing seekbars. |
| `verify_r17_home_polished.png` | 1080×2340 | Folio **Home** tab showing NO duplicate `"Search Kindle"` bar, single unified Open Catalog search bar, and on-genre **"Because You Read Metro 2034"** cover recommendations. |

---

## 5. Stock Safety Invariant Verification

Stock package status verified via package manager dumpsys:
```
Package: com.amazon.kindle
versionName=8.160.1.0(2.0.124621.0)
lastUpdateTime=2026-11-19 21:59:23
firstInstallTime=2026-11-14 03:25:13
Status: 100% UNTOUCHED and READ-ONLY
```
All operations, modifications, and storage allocations were strictly confined to `com.amazon.kindle.epub` and `/tmp/kindle_clone_build`.
