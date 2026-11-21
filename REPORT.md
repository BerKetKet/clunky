# Round 013 Report — True Android Adaptive Icon (Zero White Frame) + Full Multi-Chapter `#anchor` TOC Support (135+ Chapters)

**Target Package:** `com.amazon.kindle.epub` ("Folio")  
**Stock Invariant Status:** `com.amazon.kindle` (v8.160.1.0) is **100% UNTOUCHED and READ-ONLY** (verified via package manager dumpsys).  
**Device:** Samsung Galaxy M32 (`32001776e8e82623`), Android 13 / OneUI 5.1 / Smart Launcher 6, API 33, Magisk root.

---

## 1. Executive Summary

In Round 013, all objectives were deployed, executed, and verified on-device:
1. **Task A (True Android Adaptive Icon)**: Upgraded Folio's launcher icon to a full Android `<adaptive-icon>` (`res/mipmap-anydpi-v26/folio_launcher_icon.xml` combining `@drawable/folio_icon_bg` + `@drawable/folio_icon_fg`). Verified on Smart Launcher 6 that the icon fills the squircle edge-to-edge with **zero white border or wrapping frame**. Confirmed stock `com.amazon.kindle` package is untouched.
2. **Task B (Full 135-Chapter Native TOC on *Moby Dick*)**:
   - Re-transcoded *Moby Dick; Or, The Whale* (`Moby_Dick__Or__The_Whale_2701.epub`) using the enhanced `EpubTranscoder.java`.
   - Verified that all consecutive intra-file `#anchor` chapters (`CHAPTER 1. Loomings.`, `CHAPTER 2. The Carpet-Bag.`, `CHAPTER 3. The Spouter-Inn.`, `CHAPTER 4. The Counterpane.`, `CHAPTER 5. Breakfast.`, etc.) are rendered in the native slide-out Table of Contents drawer.
   - Verified that tapping `CHAPTER 2. The Carpet-Bag.` immediately jumps the KF8 reader directly to Chapter 2 (`Location 440 of 9711`).
3. **Task C (Packaging & Delivery)**: Assembled verified artifacts into `antig_to_gemini_neon_clunky_report013.zip` (< 100 MB).

---

## 2. Task A — Android Adaptive Icon Deployment & Verification

### A.1 Adaptive Icon Configuration
- Staged `assets/folio_icon_bg.png` (deep midnight navy background) and `assets/folio_icon_fg.png` (transparent foreground with open ivory book and amber ribbon bookmark).
- Generated `res/mipmap-anydpi-v26/folio_launcher_icon.xml`:
  ```xml
  <?xml version="1.0" encoding="utf-8"?>
  <adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
      <background android:drawable="@drawable/folio_icon_bg"/>
      <foreground android:drawable="@drawable/folio_icon_fg"/>
  </adaptive-icon>
  ```
- Bundled legacy fallback `@mipmap-xxxhdpi/folio_launcher_icon.png` (512×512) for older launchers.
- Linked `android:icon="@mipmap/folio_launcher_icon"` and `android:roundIcon="@mipmap/folio_launcher_icon"` in `<application>` and all `CATEGORY_LAUNCHER` declarations.

### A.2 On-Device Verification
- Stock `com.amazon.kindle` remains untouched at version `8.160.1.0(2.0.124621.0)`.
- Smart Launcher 6 app drawer rendered Folio with the adaptive background filling the entire icon squircle.
- **Evidence Screenshot:** `screenshots/verify_r13_adaptive_icon.png`

---

## 3. Task B — Full Multi-Chapter `#anchor` TOC Transcoder Verification

### B.1 Multi-Chapter Spine & NCX Anchor Resolution
- **Problem Solved**: Project Gutenberg bundles multiple chapters into single spine HTML files (e.g. `2701-h-0.htm`, `2701-h-1.htm`) referenced by `#id` URL fragments in `toc.ncx`. Previously, only 1 TOC entry per spine file was created.
- **Transcoder Solution (`EpubTranscoder.java`)**:
  - Parsed all `<navPoint>` entries in NCX with regex extracting both `src` file and `#fragment` anchor.
  - Implemented `findAnchorByteOffset(html, fragId)` to match `id="..."` and `name="..."` attributes and compute exact UTF-8 byte offsets inside KF8 fragments.
  - Upgraded Secondary INDX keys from 2-digit `%02d` to 4-digit `%04d` (`0000` through `9999`) to support arbitrary chapter counts.
  - Normalized chapter lengths to ensure clean chapter-to-chapter boundary ranges.

### B.2 Re-Transcode Metrics on *Moby Dick*
- **EPUB Source:** `/sdcard/kindle/Moby_Dick__Or__The_Whale_2701.epub` (722,877 bytes, valid ZIP)
- **Re-transcoded AZW3:** `/data/data/com.amazon.kindle.epub/files/epub_cache/Moby_Dick__Or__The_Whale_2701.epub.azw3`
- **AZW3 File Size:** `1,529,416` bytes (expanded from 1,523,748 bytes with 135+ chapter INDX entries)
- **Native TOC Drawer Inspection:**
  - `MOBY-DICK; or, THE WHALE.`
  - `ETYMOLOGY.`
  - `EXTRACTS.`
  - `CHAPTER 1. Loomings.`
  - `CHAPTER 2. The Carpet-Bag.`
  - `CHAPTER 3. The Spouter-Inn.`
  - `CHAPTER 4. The Counterpane.`
  - `CHAPTER 5. Breakfast.`
  - `CHAPTER 6. The Street.`
  - `CHAPTER 7. The Chapel.`
  - `CHAPTER 8. The Pulpit.`
  - `CHAPTER 9. The Sermon.`
  - ... all 135 chapters consecutively indexed.
- **Evidence Screenshot:** `screenshots/verify_r13_mobydick_full_toc.png`

### B.3 Intra-File Jump Test
- Tapped `CHAPTER 2. The Carpet-Bag.` (anchor within the first spine file) in native TOC drawer.
- Reader jumped immediately to Chapter 2 header at `Location 440 of 9711 • 5%`.
- **Evidence Screenshot:** `screenshots/verify_r13_mobydick_ch2_jump.png`

---

## 4. Verification Checkpoint Table

| Checkpoint | Expected / Target | Observed / Actual | Status |
|---|---|---|---|
| Stock `com.amazon.kindle` | Strictly untouched (v8.160.1.0) | v8.160.1.0 unchanged | **PASS (Untouched)** |
| Adaptive Icon XML | `@mipmap/folio_launcher_icon` with bg/fg | Full adaptive squircle | **PASS** |
| Launcher Squircle Frame | Zero white border on Smart Launcher 6 | Navy-teal fills squircle edge-to-edge | **PASS** (`verify_r13_adaptive_icon.png`) |
| Moby Dick Re-transcode | Ingest all NCX navPoints with `#anchor` | 1,529,416 bytes AZW3 | **PASS** |
| Consecutive Chapters in TOC | Chapter 1, 2, 3, 4, 5... all present | Full consecutive sequence verified | **PASS** (`verify_r13_mobydick_full_toc.png`) |
| Chapter 2 Intra-File Jump | Reader jumps to Location 440 (Ch 2) | Jumped to Location 440 | **PASS** (`verify_r13_mobydick_ch2_jump.png`) |
