# Round 023 Instructions — Full On-Device KFX (`libkfxconv.so` → `KRIFDocViewer`) Quality Audit, Side-by-Side Stock KFX Parity & Lean Report Upload

**Target Package:** `com.amazon.kindle.epub` ("Folio v2.0 KFX")  
**HARD SAFETY INVARIANT:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (you may view/screenshot stock Kindle read-only for side-by-side comparison, NEVER write, modify, clear data, force-stop, or uninstall stock `com.amazon.kindle`).  
**Autonomous Relay Mode:** Execute all tasks end-to-end and upload `antig_to_gemini_neon_clunky_report023.zip` (`< 15 MB` — see Task C!) directly to this chat via Chrome Dev Bridge without pausing.

---

## Note on Round 022 Upload
Congratulations on cross-compiling `hwanjongyu/kfx_converter` for `arm64-v8a` (`libkfxconv.so`, 4.3 MB) and converting *Aristophanes* (5.2 MB in 1,070ms) and *Sophocles* (4.0 MB in 736ms) on-device directly into native `KRIFDocViewer` (`YJBINARY`)!  
Note: `antig_to_gemini_neon_clunky_report022.zip` did not attach to your Round 022 message (likely because re-bundling all 21 prior rounds' screenshots pushed the archive size up). Going forward, **only include the current round's screenshots and logs** so every report zip stays **`< 15 MB`** and attaches reliably! (Please also include your Round 022 `verify_r22_ondevice_epub_to_kfx*.png` screenshots in Report 023 so I can visually inspect them.)

---

## Tasks

### Task A — End-to-End Quality Control of On-Device EPUB → KFX (`libkfxconv.so` → `KRIFDocViewer`)
Test Folio's on-device `libkfxconv.so` → `KRIFDocViewer` pipeline across **every core reader interaction** (zero overfitting — test across multiple distinct books!):

1. **Live `Aa → Font → Spacing` Visual Proof (Before & After)**:
   - Open an on-device KFX-converted book (e.g. *Sophocles* or *Aristophanes* or *Hesiod* from `/sdcard/epub/`) in Folio.
   - Capture **`screenshots/verify_r23_kfx_spacing_before.png`** at default Line Spacing & Paragraph Spacing.
   - Open `Aa → Font → Spacing`, drag **Line spacing** and **Paragraph spacing** noticeably higher, dismiss `Aa`, and capture **`screenshots/verify_r23_kfx_spacing_after.png`** confirming the page layout VISIBLY changed live in `KRIFDocViewer`!
2. **Offline Wiktionary Dictionary Under `KRIFDocViewer` (KFX)**:
   - Long-press an English word inside the KFX book and confirm the offline Wiktionary dictionary (`B003ODIZL6`) popup renders definitions cleanly under `KRIFDocViewer`.
   - Capture **`screenshots/verify_r23_kfx_dictionary.png`**.
3. **Tables, Embedded Images & Native Slide-Out TOC Drawer in On-Device KFX**:
   - Open `kindle_tables_images_exam.epub` through on-device `libkfxconv.so` → `KRIFDocViewer`.
   - Verify tables, embedded images, and the native TOC drawer render cleanly (log any `libkfxconv.so` quirks or gaps in `logs/kfx_qc_findings.txt`).
   - Capture **`screenshots/verify_r23_kfx_tables_images.png`** and **`screenshots/verify_r23_kfx_toc.png`**.
4. **Library Grid & Continue Reading Cover Registration for On-Device KFX Books**:
   - Confirm that books transcoded on-device to `.kfx` register clean **real cover art** in Folio's Library grid AND Continue Reading carousel (no gray placeholders!).
   - Capture **`screenshots/verify_r23_library_kfx_covers.png`**.
5. **Side-by-Side Parity: Folio On-Device KFX vs Stock Kindle Cloud KFX**:
   - Pick a book present in BOTH `/sdcard/epub/` (or `kindle_exam_v3.epub`) and stock Kindle's local STK downloads (strictly read-only!), match font (`Bookerly`) and size, and capture:
     * **`screenshots/verify_r23_parity_folio_kfx.png`**
     * **`screenshots/verify_r23_parity_stock_kfx.png`**
   - Document any remaining pixel/layout/cover/hyphenation differences in `logs/kfx_qc_findings.txt` so we can refine `kfx_converter` in Round 024!

### Task B — Audit KFX Cover Handling (Per Project Lead Directive)
- Inspect how `libkfxconv.so`'s output `.kfx` handles the cover page (does `kfx_converter` emit a KFX cover resource rendered natively on `#000000` black by `KRIFDocViewer`, OR does it put the cover in the HTML/storyline flow with margins, OR omit it?).
- Document the exact cover behavior in `logs/kfx_qc_findings.txt` (do NOT force an in-spine HTML cover!).

### Task B2 — Benchmark `jloutsch/kfxgen` (Kindle Previewer 4.0.3 Parity Reference) vs `libkfxconv.so`
1. Clone **`https://github.com/jloutsch/kfxgen`** on your Linux host PC.
2. Run its standalone direct-Python converter (`python3 research/convert_epub_to_kfx.py`) on `kindle_tables_images_exam.epub` and `kindle_exam_v3.epub`, push the resulting `.kfx` files to `/sdcard/`, and open them in **Folio (`KRIFDocViewer`)**.
3. Compare `jloutsch/kfxgen` output vs our embedded Rust `libkfxconv.so` output across:
   - Native Kindle tables & multi-paragraph cells
   - Inline & block images (and transparent PNG handling)
   - Cover page & start landmark (`--kfxgen-open-at-cover`)
   - `override_kindle_font: false` & embedded `@font-face` fonts
   - Language tag (dictionary & hyphenation activation)
4. Record every concrete delta in `logs/kfx_qc_findings.txt` (referencing exact Ion/KFX structures from `plugin/kfxgen/native_generator.py`) so we can port the highest-value Previewer-parity fixes directly into our 4.3 MB Rust `libkfxconv.so` in Round 024!

### Task C — Stage Folio v2.0 KFX Release & Upload Lean Report Zip (`< 15 MB`)
1. Stage the signed Folio v2.0 APK (with embedded `lib/arm64-v8a/libkfxconv.so`) in `07_APK_EPUB_INGESTION_MOD/release/folio_v2.0_kfx/`.
2. Confirm stock `com.amazon.kindle` is 100% untouched.
3. Build `antig_to_gemini_neon_clunky_report023.zip` containing **ONLY**:
   - `REPORT.md`
   - `scripts/` (current `EpubTranscoder.java`, `CoverOverlayView.java`, `clone_and_deploy_epub_mod.sh`, and any modified scripts)
   - `screenshots/` (**ONLY** `verify_r22_ondevice_epub_to_kfx*.png` + `verify_r23_*.png` — do NOT include older Round 001–021 screenshots!)
   - `logs/` (Round 023 logs including `kfx_qc_findings.txt`)
4. Verify `antig_to_gemini_neon_clunky_report023.zip` is `< 15 MB` and **confirm the file attachment appears in the chat composer before hitting Send** via Chrome Dev Bridge!
