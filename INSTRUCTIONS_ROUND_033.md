# Round 033 Instructions — Full HTML Entity Decoding, In-Book Search & Notebook QC (Folio v3.0 KFX)

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Hard Safety Invariant:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never modify, write, force-stop, or clear data).  
**Zero Overfitting Rule:** All converter and UI logic must be 100% general across all books and queries.  
**Reply Archive Budget:** Package ONLY Round 033 files (`REPORT.md`, `INSTRUCTIONS_ROUND_033.md`, `protocol.json`, `screenshots/*.png`, and concise logs) into `antig_to_gemini_neon_clunky_report033.zip` (< 15 MB) and verify a single `.zip` attachment chip before sending.

---

## Round 032 Visual QC Feedback

1. **PASS — Store-Free Library Search (`verify_r32_library_search_no_store.png`, `verify_r32_search_result_open_book.png`)**: Searching `"sophocles"` shows ONLY `In your library (1)` with the Amazon `"From the store"` shelf completely gone, and tapping opens straight into `KRIFDocViewer`.
2. **PASS — Zero-Hyphenation Code Blocks & Sup/Sub (`verify_r32_code_no_hyphen_and_sup_sub.png`)**: Long Python identifiers (`calculate_engine_execution_duration`, `total_duration_seconds`, `processing_throughput_rate`) wrap cleanly with zero hyphens, and `E = mc²`, `a² + b² = c²`, `[1]` sit cleanly on the baseline.
3. **REAL PARSER BUG CAUGHT BY VISUAL QC — Literal `&rarr;` Entity Leak**:
   - In `verify_r32_code_no_hyphen_and_sup_sub.png`, line 2 reads:
     `Chemical reaction equation: 2H₂ + O₂ &rarr; 2H₂O and atmospheric CO₂.`
   - Named HTML entities beyond basic XML entities (`&rarr;`, `&times;`, `&plusmn;`, `&hellip;`, `&mdash;`, `&eacute;`, `&Omega;`, etc.) are leaking as raw `&name;` text instead of being decoded to Unicode!

---

## Task A: Complete HTML4/HTML5 Named & Numeric Entity Decoding in `libkfxconv.so`

1. **Full Entity Decoder (`src/epub/xhtml.rs`)**:
   - Audit how `kfx_converter` decodes XML/HTML character entities in text nodes and attributes.
   - Implement complete HTML4/HTML5 named entity decoding (via an appropriate offline/cached Rust crate like `html-escape` if already vendored, or a clean comprehensive lookup table in `xhtml.rs` covering all standard HTML4/HTML5 named entities: arrows `&rarr;`/`&larr;`/`&harr;`, math `&times;`/`&divide;`/`&plusmn;`/`&le;`/`&ge;`/`&ne;`/`&infin;`/`&deg;`, typography `&mdash;`/`&ndash;`/`&hellip;`/`&lsquo;`/`&rsquo;`/`&ldquo;`/`&rdquo;`/`&nbsp;`/`&thinsp;`/`&copy;`/`&reg;`/`&trade;`/`&sect;`/`&para;`/`&dagger;`/`&Dagger;`/`&bull;`, Latin-1 accented letters `&eacute;`/`&uuml;`/`&ntilde;`/`&ccedil;`/`&aelig;`/`&oelig;`, Greek `&alpha;`–`&Omega;`, fractions `&frac12;`/`&frac14;`/`&frac34;`) alongside decimal (`&#8594;`) and hexadecimal (`&#x2192;`) numeric character references.
2. **Unit Tests & ARM64 Build**:
   - Add Rust unit tests in `src/packager/tests.rs` / `src/epub/xhtml.rs` asserting `&rarr;` → `→`, `&times;` → `×`, `&plusmn;` → `±`, `&hellip;` → `…`, `&eacute;` → `é`, `&Omega;` → `Ω`, `&#8594;` → `→`, and `&#x2192;` → `→` with zero raw `&...;` leaks.
   - Run `cargo test --all-targets`, cross-compile stripped ARM64 `libkfxconv.so`, update version badge to **Folio v3.0 KFX** in `FolioMoreView.java`, build, sign, and deploy to Galaxy M32.

---

## Task B: In-Book Full-Text Search & Bookmarks/Notebook Audit (`KRIFDocViewer`)

1. **In-Book Full-Text Search**:
   - Open an on-device converted KFX book (e.g., Sophocles) in `KRIFDocViewer`, tap the in-book Search icon, and search for a real term (e.g., `"Creon"`).
   - Verify whether native KFX search returns matching locations/snippets and jumps cleanly on tap (note any KFX indexing requirement if search returns empty).
2. **Bookmarks & Highlights Notebook**:
   - Add a bookmark and/or highlight in the open KFX book, open the in-reader **Notebook** view, and verify saved bookmarks/highlights persist locally without Amazon Whispersync error popups or crashes.

---

## Task C: On-Device Visual Verification & Milestone `Folio v3.0 KFX` Staging

Capture and include in `antig_to_gemini_neon_clunky_report033.zip`:

1. `screenshots/verify_r33_html_entities_decoded.png`: Rendered KFX page proving `2H₂ + O₂ → 2H₂O` (real `→` arrow, zero literal `&rarr;`) plus decoded `&times;`, `&plusmn;`, `&hellip;`, `&mdash;`, `&eacute;`, `&Omega;`, and `&#x2192;`.
2. `screenshots/verify_r33_in_book_search.png`: Live in-book search results inside an on-device KFX book in `KRIFDocViewer`.
3. `screenshots/verify_r33_bookmarks_notebook.png`: Saved bookmark/highlight visible inside the in-reader Notebook view with zero sync error dialogs.
4. `screenshots/verify_r33_about_v30.png`: Control Center **About Folio** dialog showing milestone `Version: 3.0 KFX (Enhanced Typesetting)`.
5. Confirm stock `com.amazon.kindle` remains 100% untouched (`lastUpdateTime=2026-11-19 21:59:23`) and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v3.0_kfx/base.apk`.
