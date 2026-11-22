# Round 034 Instructions — Nested Lists & Table Spans, Library Context Menu & Settings/OPDS Audit (Folio v3.1 KFX)

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Hard Safety Invariant:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never modify, write, force-stop, or clear data).  
**Zero Overfitting Rule:** All converter and UI logic must be 100% general across all books and catalogs.  
**Reply Archive Budget:** Package ONLY Round 034 files (`REPORT.md`, `INSTRUCTIONS_ROUND_034.md`, `protocol.json`, `screenshots/*.png`, and concise logs) into `antig_to_gemini_neon_clunky_report034.zip` (< 15 MB) and verify a single `.zip` attachment chip before sending.

---

## Round 033 Visual QC Feedback

1. **PASS — 2,125 W3C HTML5 Entity Decoder (`verify_r33_html_entities_decoded.png`)**: `2H₂ + O₂ → 2H₂O`, `5 × 10 ± 2 ≤ 100 ≠ ∞ …`, `—café au lait— in Ωhm realm © 2026`, Greek spans, fractions, currency, and decimal/hex arrows all render cleanly with zero raw entity leaks.
2. **PASS — In-Book Full-Text Search (`verify_r33_in_book_search.png`)**: Searching `"Creon"` in Sophocles returned `BOOK (297)` with rich highlighted snippets and instant jump to Location 509.
3. **PASS — Bookmarks & Notebook (`verify_r33_bookmarks_notebook.png`)**: Saved bookmark at Location 509 persisted locally in `Annotations` with zero Whispersync error popups.
4. **PASS — Milestone `Folio v3.0 KFX` (`verify_r33_about_v30.png`)**: Clean About dialog showing `Version: 3.0 KFX (Enhanced Typesetting)` and `29` indexed books.

---

## Task A: Pure-Rust `libkfxconv.so` Edge-Case Hardening

1. **Multi-Level Nested Lists (`<ul>`/`<ol>` inside `<li>`)**:
   - Track list nesting depth in `src/epub/xhtml.rs` and `src/packager/storylines.rs`.
   - Scale `margin_left` by depth (e.g., `1.5em`, `3.0em`, `4.5em`) and cycle unordered markers cleanly (`•`, `◦`, `▪`) so nested sub-lists indent visually instead of flattening to depth 1.
2. **Table `<thead>` / `<th>` Bold Styling & `colspan`/`rowspan`**:
   - Ensure `<th>` cells render bold (and centered/distinct header styling) inside `$278`/`$454`/`$279`/`$269` native KFX tables.
   - Support HTML `colspan` and `rowspan` attributes on `<td>`/`<th>` (verifying exact YellowJersey Ion symbols `$154` `table_column_span` / `$153` `table_row_span` against `jloutsch/kfxgen` and stock Kindle KFX read-only).
3. **Malformed-Asset Resilience**:
   - Verify `libkfxconv.so` gracefully skips broken/missing `<img src="...">` paths, empty spine documents, and invalid CSS rules without panicking.
4. **Unit Tests & ARM64 Build**:
   - Add Rust unit tests covering nested lists (depth 1–3), table `<th>` + `colspan`/`rowspan`, and missing-image resilience.
   - Run `cargo test --all-targets`, cross-compile stripped ARM64 `libkfxconv.so`, bump badge to **Folio v3.1 KFX** in `FolioMoreView.java`, build, sign, and deploy to Galaxy M32.

---

## Task B: Library Context Menu, Settings & OPDS Catalogs UI Audit

1. **Library Book Long-Press / Context Menu**:
   - Long-press a local KFX book in Folio's **Library** tab and inspect all menu items.
   - Suppress or hide dead Amazon-store/cloud items (such as `"View in Store"`, `"Add to Goodreads"`, `"Shop for..."`) if present, while preserving working local actions (e.g., open, mark as read, remove/delete local copy).
2. **Control Center → Reader & App Settings + OPDS Catalogs**:
   - Verify tapping **Reader & App Settings** from the More tab opens cleanly without Amazon sign-in prompts or crashes (document any dead Amazon-account sub-rows in `REPORT.md`).
   - Verify tapping **OPDS Catalogs** opens Folio's catalog presets/feed browser cleanly.

---

## Task C: On-Device Visual Verification & Release Staging (`Folio v3.1 KFX`)

Capture and place all 4 proof screenshots inside `screenshots/` in `antig_to_gemini_neon_clunky_report034.zip`:

1. `screenshots/verify_r34_nested_lists_and_table_spans.png`: Rendered KFX page showing 3-level nested lists with clear indentation AND a native KFX table with bold `<th>` headers + `colspan` cell.
2. `screenshots/verify_r34_library_book_context_menu.png`: Library tab long-press context menu on a local KFX book proving clean local options with zero Amazon Store / Goodreads dead ends.
3. `screenshots/verify_r34_settings_screen_audit.png`: **Reader & App Settings** launched from Control Center.
4. `screenshots/verify_r34_opds_catalog_live.png`: **OPDS Catalogs** launched from Control Center showing clean catalog presets/feed UI.
5. Confirm stock `com.amazon.kindle` remains 100% untouched (`lastUpdateTime=2026-11-19 21:59:23`) and stage `07_APK_EPUB_INGESTION_MOD/release/folio_v3.1_kfx/base.apk`.
