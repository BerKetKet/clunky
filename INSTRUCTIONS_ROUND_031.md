# Round 031 Instructions — Real Pop-Up Footnotes, Rich Inline/Code Formatting & Live Library Search QC (Folio v2.8)

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Hard Safety Invariant:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never modify, write, force-stop, or clear data).  
**Zero Overfitting Rule:** All converter and UI logic must be 100% general across all books and devices.  
**Reply Archive Budget:** Package ONLY Round 031 files (`REPORT.md`, `INSTRUCTIONS_ROUND_031.md`, `protocol.json`, `screenshots/*.png`, and concise logs) into `antig_to_gemini_neon_clunky_report031.zip` (< 15 MB) and verify a single `.zip` attachment chip before sending.

---

## Round 030 Visual QC Feedback

1. **PASS — 7-Book Stress Matrix & List Bullet Alignment (`verify_r30_list_spacing_and_links_fixed.png`)**: 7/7 diverse EPUBs converted in < 2s on-device with zero crashes, and every `•` bullet gap in `kindle_exam_v3.epub` is now pixel-uniform.
2. **PASS — Highlight Selection & Action Bar (`verify_r30_highlight_or_scroll.png`)**: Crisp text selection handles, 4-color highlight action bar, and superscript `[1]`/`[2]` links rendered cleanly.
3. **2 REAL CATCHES FROM VISUAL QC**:
   - **Wrong Modal in `verify_r30_footnote_popup.png`**: That screenshot shows the **Add Note** text-input modal with the soft keyboard open over a book cover — NOT a pop-up footnote preview card!
   - **`unknown` Prefix in About Folio (`verify_r30_control_center_scan_and_about.png`)**: On TrebleDroid GSI, `Build.MANUFACTURER` returns `"unknown"`, producing `"Hardware: unknown TrebleDroid with GApps (arm64-v8a)"`.

---

## Task A: Real Native KFX Pop-Up Footnotes in `libkfxconv.so`

1. **Verify & Wire True YellowJersey Pop-Up Footnote Schema**:
   - Compare `kfx_converter`'s footnote emission against `jloutsch/kfxgen` (`plugin/kfxgen/native_generator.py`) and read-only stock Kindle KFX files for pop-up footnotes (e.g., `epub:type="noteref"` / `role="doc-noteref"` / `<sup><a href="#fn1">` pointing to `<aside epub:type="footnote">` or a back-linked footnote paragraph; KFX `$179` link + `$163` / footnote feature / anchor target).
   - Ensure tapping `[1]` on a footnote link in `KRIFDocViewer` opens Amazon's native **pop-up footnote preview card** at the bottom of the screen (displaying the footnote text with `"Go to Footnotes"` / dismiss) rather than opening the Add Note editor or silently failing.
   - Add a Rust unit test in `src/packager/tests.rs` verifying the exact pop-up footnote Ion structure.

---

## Task B: Rich Formatting Audit (`<pre>`/`<code>`, `<hr/>`, `<sup>`/`<sub>`/`<s>`) in `libkfxconv.so`

1. **Code Blocks & Monospace (`<pre>`, `<code>`)**:
   - Verify `<pre>` preserves newlines and indentation, uses monospace font family (`$11: monospace` / appropriate KFX font family symbol), and has clean left padding/margin so code listings don't collide with body prose.
2. **Horizontal Rules (`<hr/>`) & Rich Inline Formatting (`<sup>`, `<sub>`, `<s>`/`<del>`, `<u>`)**:
   - Verify `<hr/>` renders as a visible horizontal divider (not silently dropped or blank) and `<sup>`, `<sub>`, `<s>`/`<del>`, `<u>` emit valid YellowJersey style properties (`$18` baseline_shift, `$24` strikethrough, `$23` underline).
3. **Unit Tests & Build**:
   - Add Rust unit tests for `<pre>`/`<code>`, `<hr/>`, and `<s>`/`<sup>`/`<sub>`, run `cargo test --all-targets`, cross-compile stripped ARM64 `libkfxconv.so`, and build/deploy **Folio v2.8 KFX**.

---

## Task C: Clean Device Label, Live Library Search & Visual Verification (Folio v2.8)

1. **Fix `"unknown"` Manufacturer in `FolioMoreView.java`**:
   - Filter out `"unknown"` (case-insensitive) or blank `Build.MANUFACTURER`, and format cleanly as e.g. `Build.MODEL + " · Android " + Build.VERSION.RELEASE + " (" + abi + ")"` (plus manufacturer only if valid and not already in `Build.MODEL`). Update badge to `Folio v2.8 KFX`.
2. **Live Library `"Search Folio"` Verification**:
   - Tap the `"Search Folio"` bar on the Library tab, type a query matching local books (e.g., `"Sophocles"` or `"Hesiod"`), and verify local KFX books filter cleanly with zero Amazon-store dead ends or crashes.
3. **Capture 4 Hardware Proof Screenshots**:
   - `screenshots/verify_r31_real_footnote_popup.png`: Real native pop-up footnote preview card in `KRIFDocViewer` after tapping `[1]` (showing footnote body text; NO keyboard / NO Add Note editor).
   - `screenshots/verify_r31_code_hr_rich_formatting.png`: Rendered KFX page demonstrating `<pre>`/`<code>` monospace block, `<hr/>` divider, `<sup>`/`<sub>`, and `<s>`/`<u>` formatting.
   - `screenshots/verify_r31_library_search_results.png`: Live `"Search Folio"` query filtering local books cleanly in the Library tab.
   - `screenshots/verify_r31_about_clean_hardware.png`: Updated **About Folio** dialog showing `Folio v2.8 KFX` and clean hardware string without `"unknown"`.
4. Confirm stock `com.amazon.kindle` remains 100% untouched (`lastUpdateTime=2026-11-19 21:59:23`) and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v2.8_kfx/base.apk`.
