# Round 030 Instructions — Subtle Visual Polish, Multi-Book Stress Test & Interactive Flow QC (Folio v2.7)

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Hard Safety Invariant:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never modify, write, force-stop, or clear data).  
**Zero Overfitting Rule:** All converter and UI fixes must be 100% general — never hardcode book titles, filenames, or device models.  
**Reply Archive Budget:** Package ONLY Round 030 files (`REPORT.md`, `INSTRUCTIONS_ROUND_030.md`, `protocol.json`, `screenshots/*.png`, and concise logs) into `antig_to_gemini_neon_clunky_report030.zip` (< 15 MB) and verify only ONE clean `.zip` chip is attached before sending.

---

## Round 029 Visual QC Feedback

1. **PASS — Folio Control Center (`verify_r29_more_tab_clean.png`, `verify_r29_about_folio_dialog.png`)**: Outstanding dark-mode UI with zero Amazon sign-in prompts, clean 5-card layout, and live SQLite book/cache stats.
2. **PASS — Polytonic Greek NFC Normalization (`verify_r29_greek_script_render.png`)**: Every polytonic Greek diacritic (`Ἀεὶ μέν, ὦ παῖ Λαρτίου, δέδορκά σε`) renders razor-sharp with zero tofu boxes.
3. **PASS — Native Lists & Blockquotes (`verify_r29_footnotes_lists_blockquotes.png`)**: Crisp hanging indents and numbered/bulleted lists.
4. **3 SUBTLE POLISH ITEMS CAUGHT BY VISUAL QC**:
   - **Partial underline on nested tags inside `<a>`**: In `verify_r29_greek_script_render.png`, links like `Αίας - AJAX` and `Αντιγόνη - ANTIGONE` underline only the Greek prefix (`Αίας - `) while leaving `AJAX` un-underlined. Check `src/epub/xhtml.rs` to ensure nested inline tags (`<a><i>Text 1</i> Text 2</a>`) preserve the active `href` across all child spans inside `<a>` (note if the source EPUB itself only wraps the Greek prefix in `<a>`).
   - **Uneven gap after `•` bullet marker**: In `verify_r29_footnotes_lists_blockquotes.png`, the first bullet (`•No plateau...`) has a tighter gap than bullets 2–3 (`•  The canyon...`). Trim leading whitespace on `<li>` text before prepending `"• "` / `"1. "` and verify consistent marker spacing.
   - **About Folio metadata strings**: In `verify_r29_about_folio_dialog.png`, make sure `Hardware:` reads dynamically from `android.os.Build.MANUFACTURER` + `Build.MODEL` (not hardcoded) and change `"Network Mode: 100% Offline"` to `"Privacy Mode: Local-First (Zero Amazon Telemetry)"` (since OPDS/Open Library work online when used).

---

## Task A: Converter & UI Micro-Polish (Folio v2.7)

1. **Nested `<a>` Span Preservation (`src/epub/xhtml.rs`)**:
   - Verify active `href` link state persists across nested `<i>`/`<em>`/`<b>`/`<strong>`/`<span>` closing tags until `</a>` closes. Add a Rust unit test for `<a href="#target"><i>Greek - </i>English</a>`.
2. **Uniform `<li>` Marker Spacing (`src/packager/storylines.rs`)**:
   - Trim leading ASCII whitespace from the first text span of each `<li>` before prepending `"• "` or `"1. "` (and ensure non-justified or non-stretched marker spacing) so every list item has identical bullet-to-text alignment. Add a Rust unit test.
3. **Dynamic Device Info in `FolioMoreView.java`**:
   - Populate `Hardware:` dynamically via `Build.MANUFACTURER` / `Build.MODEL` + `Build.SUPPORTED_ABIS[0]`, and update the privacy label to `"Privacy Mode: Local-First (Zero Amazon Telemetry)"`.
4. **Build & Test**:
   - Run `cargo test --all-targets`, cross-compile stripped `libkfxconv.so` (`aarch64-linux-android`), bundle into **Folio v2.7 KFX**, sign, and deploy to Galaxy M32.

---

## Task B: Multi-Book On-Device Stress Test & Interactive Flow Verification

1. **Multi-Book On-Device Conversion & Launch Matrix**:
   - Run an automated on-device test across **6–8 diverse EPUBs** in `/sdcard/epub/` (including large Delphi Classics omnibuses, illustrated/table exams, and non-Latin/verse books) using Folio `v2.7`'s embedded `libkfxconv.so`.
   - Record a clean summary table in `REPORT.md`: Book, EPUB size, on-device conversion time (ms), KFX size, TOC chapter count, cover status, and zero-crash `KRIFDocViewer` (`YJBINARY`) open check.
2. **Interactive Reader & Control Center Flows**:
   - **Pop-up Footnote Preview**: Tap a real footnote reference (`<sup>` / `#fn`) in a KFX book and verify Amazon's native footnote preview panel opens cleanly.
   - **Highlight or Continuous Scrolling**: Verify live text highlighting (long-press + highlight color) OR Continuous Scrolling (`Aa → Layout → Continuous Scrolling`) on an on-device KFX book.
   - **Control Center Scan Local Library**: Tap **Scan Local Library** on the More tab and verify the background scan runs cleanly and displays the live Toast summary.

---

## Task C: On-Device Visual Verification & Release Staging

Capture and include in `antig_to_gemini_neon_clunky_report030.zip`:

1. `screenshots/verify_r30_list_spacing_and_links_fixed.png`: Proof of uniform bullet spacing in `kindle_exam_v3.epub` (and/or complete nested-tag link underline).
2. `screenshots/verify_r30_footnote_popup.png`: Live pop-up footnote preview panel in `KRIFDocViewer`.
3. `screenshots/verify_r30_highlight_or_scroll.png`: Live highlight selection or Continuous Scrolling view on an on-device KFX book.
4. `screenshots/verify_r30_control_center_scan_and_about.png`: Control Center **About Folio** (showing dynamic `Build.MODEL` + updated Privacy Mode label) or live **Scan Local Library** Toast.
5. Confirm stock `com.amazon.kindle` remains 100% untouched (`lastUpdateTime=2026-11-19 21:59:23`) and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v2.7_kfx/base.apk`.
