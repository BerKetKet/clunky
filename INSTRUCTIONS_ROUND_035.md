# Round 035 Instructions — Text-Transform/Small-Caps/RTL, OPDS Contrast & Live Feed, Settings Rebrand & Library Hygiene (Folio v3.2 KFX)

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Hard Safety Invariant:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never modify, write, force-stop, or clear data).  
**Zero Overfitting Rule:** All converter and UI logic must be 100% general across all books and feeds.  
**Reply Archive Budget:** Package ONLY Round 035 files (`REPORT.md`, `INSTRUCTIONS_ROUND_035.md`, `protocol.json`, `screenshots/*.png`, and concise logs) into `antig_to_gemini_neon_clunky_report035.zip` (< 15 MB) and verify a single `.zip` attachment chip before sending.

---

## Round 034 Visual QC Feedback

1. **PASS — 3-Level Nested Lists & Table `<thead>`/`colspan`/`rowspan` (`verify_r34_nested_lists_and_table_spans.png`)**: Crisp `•` → `◦` → `▪` depth indentation and textbook native KFX table spanning (`Tradition` | `Temporal Span (BCE)` `colspan="2"`, `Hellenic` `rowspan="2"`).
2. **PASS — Clean Library Context Menu & Offline Settings (`verify_r34_library_book_context_menu.png`, `verify_r34_settings_screen_audit.png`)**: Zero Amazon Store/Goodreads dead ends and zero sign-in prompts.
3. **3 REAL UX POLISH ITEMS CAUGHT BY VISUAL QC**:
   - **Low-contrast OPDS dialog labels (`verify_r34_opds_catalog_live.png`)**: `"Quick OPDS Catalogs:"` and `"Or enter custom OPDS feed URL:"` render very dim dark-gray on the dark dialog background.
   - **Leftover `"Kindle library"` string (`verify_r34_settings_screen_audit.png`)**: Under `Local file access`, the subtitle reads `"view files on your device in your Kindle library."`
   - **Duplicate synthetic test books cluttering Library (`verify_r34_library_book_context_menu.png`)**: Multiple `Folio v2.9/v3.0/v3.1 ... Test` entries remain in the Library list.

---

## Task A: Pure-Rust `libkfxconv.so` — `text-transform`, `small-caps` & RTL Support

1. **CSS `text-transform` & `font-variant: small-caps`**:
   - Parse and apply `text-transform: uppercase | lowercase | capitalize` in `src/css.rs` / `src/epub/xhtml.rs` (handling Unicode casing cleanly).
   - Support `font-variant: small-caps` (emitting YellowJersey small-caps style property or clean proportional small-caps presentation).
2. **Right-to-Left (`dir="rtl"`) & Multilingual Text**:
   - Preserve RTL Unicode text (Arabic, Hebrew) verbatim with NFC normalization and emit RTL direction/alignment (`text_alignment: right` / `$107` if document or block specifies `dir="rtl"` or RTL `<dc:language>`).
3. **Unit Tests & ARM64 Build**:
   - Add Rust unit tests covering `text-transform`, `small-caps`, and RTL blocks.
   - Run `cargo test --all-targets`, cross-compile stripped ARM64 `libkfxconv.so`, bump version badge to **Folio v3.2 KFX** in `FolioMoreView.java`, build, sign, and deploy to Galaxy M32.

---

## Task B: UI Polish Sweep & Library Hygiene

1. **OPDS Dialog Contrast + Live `Standard Ebooks` Feed (`OpenCatalogView.java`)**:
   - Brighten the dim labels (`"Quick OPDS Catalogs:"`, `"Or enter custom OPDS feed URL:"`) to high-contrast light slate (`#B0B8C4` or `#CBD5E1`) for crisp dark-mode legibility.
   - Tap **`Standard Ebooks`** in the OPDS dialog on-device to verify live OPDS 1.2 feed browsing renders real entries cleanly.
2. **Settings Branding (`clone_and_deploy_epub_mod.sh`)**:
   - Patch `Kindle library` → `Folio library` in `strings.xml` during APK packaging so `Local file access` reads cleanly as `"in your Folio library."`
3. **Library Hygiene**:
   - After capturing the Round 035 converter verification screenshot, clean up temporary synthetic `Folio v2.9/v3.0/v3.1/v3.2 ... Test` entries from Folio's SQLite DB and cache so the user's Library showcases real books cleanly without duplicate test clutter.

---

## Task C: On-Device Visual Verification & Release Staging (`Folio v3.2 KFX`)

Capture and place all 4 proof screenshots inside `screenshots/` in `antig_to_gemini_neon_clunky_report035.zip`:

1. `screenshots/verify_r35_text_transform_and_rtl.png`: Rendered KFX page proving `text-transform: uppercase/capitalize`, `small-caps`, and crisp RTL (Arabic/Hebrew) rendering in `KRIFDocViewer`.
2. `screenshots/verify_r35_opds_contrast_and_feed.png`: High-contrast OPDS Catalogs dialog or live `Standard Ebooks` OPDS feed listing inside Folio.
3. `screenshots/verify_r35_settings_folio_branding.png`: Settings screen showing `"in your Folio library"` cleanly rebranded.
4. `screenshots/verify_r35_clean_library_grid.png`: Clean Folio Library view after pruning temporary synthetic test entries, showcasing real books with real covers.
5. Confirm stock `com.amazon.kindle` remains 100% untouched (`lastUpdateTime=2026-11-19 21:59:23`) and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v3.2_kfx/base.apk`.
