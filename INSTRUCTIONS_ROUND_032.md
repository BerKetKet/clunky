# Round 032 Instructions — Zero-Hyphenation Code Blocks, Sup/Sub Polish & Store-Free Library Search (Folio v2.9)

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Hard Safety Invariant:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never modify, write, force-stop, or clear data).  
**Zero Overfitting Rule:** All converter and UI logic must be 100% general across all books and search queries.  
**Reply Archive Budget:** Package ONLY Round 032 files (`REPORT.md`, `INSTRUCTIONS_ROUND_032.md`, `protocol.json`, `screenshots/*.png`, and concise logs) into `antig_to_gemini_neon_clunky_report032.zip` (< 15 MB) and verify a single `.zip` chip before sending.

---

## Round 031 Visual QC Feedback

1. **PASS — Clean Device Label (`verify_r31_about_clean_hardware.png`)**: `"TrebleDroid with GApps · Android 13 (arm64-v8a)"` and `"Folio v2.8 KFX"` look crisp and accurate.
2. **PASS — `<hr/>` Dividers, `<u>`, `<s>`, & Clean Footnote Jump (`verify_r31_code_hr_rich_formatting.png`, `verify_r31_real_footnote_popup.png`)**: Horizontal rules (`$596`), strikethrough (`$27`), underline (`$23`), and clean footnote navigation (zero keyboard / Add Note modal) all verified.
3. **3 REAL POLISH BUGS CAUGHT BY VISUAL QC**:
   - **Code Identifiers Hyphenated Inside `<pre>`**: In `verify_r31_code_hr_rich_formatting.png` and `verify_r31_real_footnote_popup.png`, YellowJersey hyphenated Python code (`dur- / ation`, `ver- / batim`, `books/ / sec`). Code blocks (`<pre>` / `<code>`) must explicitly disable hyphenation!
   - **Cramped `<sup>`/`<sub>` Baseline Shift**: In `verify_r31_code_hr_rich_formatting.png`, long `<sup>`/`<sub>` spans caused awkward vertical collision and wide justification gaps (`This   paragraph   demonstrates`). Polish `<sup>`/`<sub>` sizing (`~0.75em`) and modest baseline shift.
   - **Amazon `"From the store"` Shelf in Library Search (`verify_r31_library_search_results.png`)**: Searching `"Sophocles"` in Library showed `In your library (1)` cleanly, but right underneath it displayed a shopping-cart **`From the store`** section with Amazon Kindle Store listings and gray placeholder covers!

---

## Task A: Disable Code-Block Hyphenation & Polish `<sup>`/`<sub>` in `libkfxconv.so`

1. **Disable Hyphenation on `<pre>` and `<code>` (`src/packager/styles.rs`)**:
   - Emit YellowJersey's `hyphens: none` property (verify exact KFX symbol ID in `kfx_converter` / `jloutsch/kfxgen`, e.g. `$127: $349` or `$46: false`) on `code_block` (`<pre>`) and inline `<code>` styles so variable names and keywords are NEVER hyphenated.
2. **Polish `<sup>`/`<sub>` Sizing & Baseline Shift (`src/packager/styles.rs`)**:
   - Ensure `<sup>` and `<sub>` use crisp proportional font size (`0.75em` / `75%`) and clean baseline shift so expressions like `E = mc²`, `H₂O`, and `[1]` sit cleanly without colliding with adjacent lines.
3. **Unit Tests & ARM64 Build**:
   - Add Rust unit tests verifying `hyphens: none` on `code_block` and proportional `sup`/`sub` styling.
   - Run `cargo test --all-targets`, cross-compile stripped ARM64 `libkfxconv.so`, and build **Folio v2.9 KFX** (update version badge in `FolioMoreView.java` to `Folio v2.9 KFX`).

---

## Task B: Remove Amazon `"From the store"` Shelf in Library Search (`RubySearchActivity`)

1. **Suppress Store Results in `com.amazon.kindle.epub`**:
   - Inspect `RubySearchActivity` / search result adapter in `com.amazon.kindle.epub` and hide/filter out the **`From the store`** Amazon Kindle Store section (including its shopping cart header and store result rows) so `"Search Folio"` displays ONLY local library matches (`In your library (N)`).
   - Verify tapping a matched book under `In your library` opens the local KFX book cleanly in `KRIFDocViewer`.

---

## Task C: On-Device Visual Verification & Release Staging (Folio v2.9)

Capture and include in `antig_to_gemini_neon_clunky_report032.zip`:

1. `screenshots/verify_r32_code_no_hyphen_and_sup_sub.png`: Rendered KFX page showing a `<pre>` Python code block with **zero hyphenation** on identifiers (`duration`, `verbatim`) alongside crisp `E = mc²` and `H₂O` superscript/subscript formatting.
2. `screenshots/verify_r32_library_search_no_store.png`: Live `"Search Folio"` query (`"Sophocles"`) in Library showing **ONLY `In your library (1)`** with the `"From the store"` Amazon shelf completely gone.
3. `screenshots/verify_r32_search_result_open_book.png`: Tapping the local search result and landing cleanly inside the book in `KRIFDocViewer`.
4. Confirm stock `com.amazon.kindle` remains 100% untouched (`lastUpdateTime=2026-11-19 21:59:23`) and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v2.9_kfx/base.apk`.
