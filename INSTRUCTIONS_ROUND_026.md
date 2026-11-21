# Round 026 Instructions — Native Stock-Kindle KFX `$270` Fullscreen `#000000` Cover Container + Live KFX Spacing Proof (Folio v2.3)

**Target Package:** `com.amazon.kindle.epub` ("Folio v2.3 KFX")  
**HARD SAFETY INVARIANT:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never write, modify, clear data, force-stop, or uninstall stock `com.amazon.kindle`).  
**Autonomous Relay Mode:** Execute all tasks end-to-end and upload `antig_to_gemini_neon_clunky_report026.zip` (`< 15 MB`, verify `.zip` preview chip attached!) directly to this chat via Chrome Dev Bridge without pausing.

---

## Round 025 Visual Audit — Huge Win on Tables + Exact Fix for Native KFX Covers!

1. **`verify_r25_table_columns.png` is 100% STOCK KINDLE PARITY!** Clean 6-column alignment, bold headers, crisp dashed/solid rules, AND Amazon's native `[↙↗]` table expand/pan button!
2. **Exact Stock-Kindle Native Cover Schema Found in `r25_kfx_table_and_cover_diff.txt`**:
   Look at line 14 of your Round 025 diff of Amazon's own `YJConversionTools` KFX:
   ```
   Section c0: {$174: c0, $141: [{$155: 67, $176: l4, $66: <cover_w>, $67: <cover_h>, $156: $326, $140: $320, $159: $270}]}
   Storyline l4: {$155: 23, $157: s8, $159: $271, $175: <cover_resource_id>}
   ```
   - Normal prose sections use `$159: $269` (reflowable text).
   - **Stock Kindle's standalone Cover section `c0` uses `$159: $270` (non-reflowable fixed-aspect container) with `$66: <cover_w>, $67: <cover_h>, $156: $326 (scale_fit), $140: $320 (center)`**!
   - When `kfx_converter` strips the EPUB's HTML cover page from the prose spine AND emits this exact standalone `$270` Cover container `c0` (matching Amazon's `Style s8` and `$389` cover landmark), **`KRIFDocViewer` natively renders EVERY book's cover full-width centered on pure `#000000` black with ZERO white margins, ZERO `"0%"` footer, and native swipe into Chapter 1**!
3. **Note on `verify_r25_prose_spacing_before/after.png`**: Those two screenshots were identical because *Rashomon* was a legacy `.azw3` (`MobiDocViewer`) rather than an on-device `.kfx` (`KRIFDocViewer`). Let's verify live `Aa → Font → Spacing` reflow on an **on-device KFX book**!

---

## Tasks

### Task A — Implement Native Stock-Kindle `$270` Standalone Cover Container in Rust `libkfxconv.so`
1. Inspect `Style s8` and the `$389` cover landmark inside stock Kindle's unencrypted `CR!ZTD3FX31BX225B7KRMNC6MN5NFHZ.kfx` (read-only!) so we match every single field of `Section c0` + `Storyline l4` + `Style s8` + `$495 cover_image`.
2. In `kfx_converter`:
   - Ensure ANY cover-only HTML spine item (`cover`, `titlepage`, `wrap0000`, or image-only section 0/1 including `<svg><image .../></svg>`) is stripped from the prose spine (so no book ever renders a small cover image on a white page!).
   - Emit the standalone `$270` (`scale_fit` + `center` + actual cover width/height `$66`/`$67`) Cover Section `c0` referencing the extracted cover `$164`/`$417` resource, plus the `$389` `cover` landmark pointing to `c0` and `start` landmark pointing to Section 1!
3. Cross-compile `kfx-conv` for `aarch64-linux-android` (`lib/arm64-v8a/libkfxconv.so`), rebuild & deploy **Folio v2.3** (`com.amazon.kindle.epub`), and confirm stock `com.amazon.kindle` is 100% untouched.

### Task B — Hardware Verification Across Multiple Distinct Books (Zero Overfitting!)
Clear `epub_cache/` so books re-transcode with Round 026's `libkfxconv.so`, then verify:
1. **Native Fullscreen `#000000` KFX Cover (2 Distinct Books)**:
   - Open **Hesiod** AND **Sophocles** (from `/sdcard/epub/`), navigate to Cover, and confirm BOTH render **full-width centered on pure `#000000` black** natively in `KRIFDocViewer` (NO small top-left image on white!).
   - Capture **`screenshots/verify_r26_kfx_cover_book1.png`** (Hesiod) and **`screenshots/verify_r26_kfx_cover_book2.png`** (Sophocles).
   - Swipe left from Cover and confirm it lands cleanly on Chapter 1 / Title prose with zero duplicate white-margined cover page!
2. **Live `Aa → Font → Spacing` Visual Reflow Proof on On-Device KFX**:
   - On **Hesiod** (or **Sophocles**) Chapter 1 prose (on-device `.kfx` in `KRIFDocViewer`), capture **`screenshots/verify_r26_kfx_spacing_before.png`** at minimum Line & Paragraph spacing.
   - Open `Aa → Font → Spacing`, drag **Line spacing** and **Paragraph spacing** to maximum, dismiss `Aa`, and capture **`screenshots/verify_r26_kfx_spacing_after.png`** confirming visible live reflow (different pixel hash / fewer lines per screen!).
3. **Stage Signed Folio v2.3 Release**:
   - Stage signed APK in `07_APK_EPUB_INGESTION_MOD/release/folio_v2.3_kfx/base.apk`.

### Task C — Package Lean Report (`< 15 MB`) & Verify Attachment Chip Before Sending
Build `antig_to_gemini_neon_clunky_report026.zip` (`< 15 MB`, Round 026 files only), verify the `.zip` attachment chip in the composer, and upload via Chrome Dev Bridge!
