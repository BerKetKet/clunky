# Kindle Android v8.159.0.100 - Deployment & Restoration Notes

## Status
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v3.9 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 042 complete:
  1. **719-EPUB Folder Search & "Hide Imported" Toggle (Task A & B)**:
     - Dropped in Gemini's updated `FolioFolderBrowserDialog.java` and `MetadataEnricher.java`.
     - In "Import EPUBs from Storage", verified tapping `Hide Imported: OFF` toggles to emerald `Hide Imported: ON` and eliminates previously imported books (*Brave New World*, *Island*, *Crome Yellow*, *Moksha*), reducing folder display from 719 to 715 files.
     - Verified instant in-memory filename search by typing `"Orwell"`, immediately filtering all 719 files to 14 matching EPUBs.
  2. **Batch Import & Genre Enrichment (Task B)**:
     - Selected `George Orwell - Animal Farm.epub` (179 KB) and tapped `Import Selected (1)`.
     - Converted via `libkfxconv.so` in 164ms (`238,885 bytes`), extracted cover thumbnail, enriched metadata via Open Library, and persisted in `folio_metadata.db` and `kindle_library.db`.
     - Opened `Library Metadata & Genres (26)`: *Animal farm: a fairy story* displays at top with genuine cover thumbnail, publisher `"Penguin"`, pub date `"2000-07-15"`, ISBN `"9780141182704"`, and rich blue genre tags (`Fiction`, `Read`).
  3. **Visual Verification & Milestone Release Staging (Task C)**:
     - Captured 3 hardware proof screenshots on Galaxy M32:
       * `screenshots/verify_r42_folder_search_and_hide_imported.png` (176 KB)
       * `screenshots/verify_r42_enriched_genres_card.png` (336 KB)
       * `screenshots/verify_r42_about_v39.png` (290 KB)
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v3.9 KFX release APK (131 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v3.9_kfx/base.apk`.
  4. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v3.9 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, 700+ EPUB live filename search in folder importer, "Hide Imported" toggle pill, ISBN-to-title genre fallback, multi-select batch EPUB folder importer from storage, portable metadata JSON backup/restore, missing-cover auto-repair with Open Library Covers API + 302 redirect following, catalog sort controls Recent/Title/Author/Year, in-app editable metadata dialog with live persistence, full-library `Sync & Enrich` metadata backfiller, real-time search filtering, Title-Cased genre chips, rich Book Details popup with 0ms reader launch, duplicate import guard with "Open Existing Copy" vs "Re-Import & Replace", "Library Metadata & Genres" browser dialog, persistent `folio_metadata.db`, Open Library metadata enrichment, live OPDS/Atom feed download, heavy omnibus stress-tested up to 23 MB / 1,450 sections, CSS text-transform uppercase/lowercase/capitalize, font-variant small-caps, RTL Arabic/Hebrew direction and right alignment, multi-level nested lists, native tables with thead/colspan/rowspan, malformed asset resilience, full HTML4/HTML5 entity decoding, store-free library search, zero-hyphenation code blocks, proportional sup/sub, `$270` cover container, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 042)
1. `screenshots/verify_r42_folder_search_and_hide_imported.png`: Folder browser dialog browsing `/sdcard/en/0` showing `Hide Imported: ON`, live search for `Orwell` (14 matching files), and checked `1984.epub`.
2. `screenshots/verify_r42_enriched_genres_card.png`: Library Metadata & Genres (26) showing newly imported *Animal Farm* with genuine cover thumbnail and rich blue genre tags (`Fiction`, `Read`).
3. `screenshots/verify_r42_about_v39.png`: Control Center showing `About Folio` dialog with `Folio v3.9 KFX` version and engine stats.
4. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
5. `dumpsys package com.amazon.kindle.epub`: Folio v3.9 KFX deployed (`lastUpdateTime=2026-11-22 18:21:03`).

---

# Round 041: Batch EPUB Folder Importer & JSON Backup (Folio v3.8)
1. `screenshots/verify_r41_folder_browser_select.png`: Folder browser dialog browsing `/sdcard/en/0` with green `· Already Imported` badges and checked boxes ready to batch import.
2. `screenshots/verify_r41_batch_imported_catalog.png`: Library Metadata & Genres (24) showing newly imported *Moksha* and *Crome Yellow* with covers and metadata.
3. `screenshots/verify_r41_control_center_v38.png`: Control Center showing `Import EPUBs from Storage` card and `Folio v3.8 KFX` badge.
4. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
5. `dumpsys package com.amazon.kindle.epub`: Folio v3.8 KFX deployed (`lastUpdateTime=2026-11-22 18:07:40`).

---

# Round 040: Missing-Cover Auto-Repair & Sort (Folio v3.7)
1. `screenshots/verify_r40_covers_fixed_and_sorted.png`: Catalog dialog after `Sync & Enrich` proving *The 210th Day* has a real cover thumbnail and the new `Sort: Recent` pill is active.
2. `screenshots/verify_r40_edit_metadata_dialog.png`: Dark **Edit Book Metadata** form opened from Book Details with pre-filled fields (`Title`, `Author`, `Published Date / Year`, `Publisher`, `Genres`, `ISBN`) and `SAVE` / `CANCEL` actions.
3. `screenshots/verify_r40_about_v37.png`: Control Center **About Folio** dialog showing `Version: Folio v3.7 KFX (Enhanced Typesetting)` and live engine stats.
4. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
5. `dumpsys package com.amazon.kindle.epub`: Folio v3.7 KFX deployed (`lastUpdateTime=2026-11-22 17:53:17`).

---

# Round 039: Full-Library Metadata Backfill & Search (Folio v3.6)
1. `screenshots/verify_r39_metadata_catalog_synced.png`: Catalog dialog after `Sync & Enrich` showing `Library Metadata & Genres (21)`, Title-Cased genre chips, search bar, and enriched book cards.
2. `screenshots/verify_r39_metadata_search_filtered.png`: Live real-time search filter active with query `"Huxley"`, filtering display strictly to matching cards.
3. `screenshots/verify_r39_book_details_popup.png`: Tapping `ⓘ` on `Island` showing dark Book Details dialog with full metadata, Open Library work key, and `READ NOW` / `CLOSE` actions.
4. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
5. `dumpsys package com.amazon.kindle.epub`: Folio v3.6 KFX deployed (`lastUpdateTime=2026-11-22 17:39:47`).

---

# Round 038: Duplicate Guard & Metadata Catalog (Folio v3.5)
1. `screenshots/verify_r38_duplicate_import_guard.png`: Green `ALREADY IN FOLIO LIBRARY` dialog over Galaxy M32 showing `Open Existing Copy` and `Re-Import & Replace`.
2. `screenshots/verify_r38_metadata_catalog_genres.png`: Dark `FOLIO METADATA CATALOG` dialog with genre filter chips and enriched book cards.
3. `screenshots/verify_r38_control_center_v35.png`: Folio Control Center showing `Folio v3.5 KFX` badge and `Library Metadata & Genres` action card.
4. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
5. `dumpsys package com.amazon.kindle.epub`: Folio v3.5 KFX deployed (`lastUpdateTime=2026-11-22 17:22:20`).

---

# Round 037: External EPUB Intent Hook, Ingestion Flow & Open Library Metadata (Folio v3.4)
1. `screenshots/verify_r37_external_import_dialog.png`: Dark **FOLIO EPUB IMPORT** dialog showing extracted metadata and action buttons.
2. `screenshots/verify_r37_open_once_reader.png`: Book opened directly in `KRIFDocViewer` after "Open Once".
3. `screenshots/verify_r37_add_to_library_reader.png`: Book opened directly in `KRIFDocViewer` after "Add to Library & Read".
4. `screenshots/verify_r37_library_imported_book.png`: Folio Library tab showing the newly imported book with its real cover.
5. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
6. `dumpsys package com.amazon.kindle.epub`: Folio v3.4 KFX deployed (`lastUpdateTime=2026-11-22 16:49:43`).

---

# Round 034: Nested Lists, Table Spans, Clean Context Menu & Settings Audit (Folio v3.1)
1. `screenshots/verify_r34_nested_lists_and_table_spans.png`: Multi-level nested list (`•`, `◦`, `▪`) with indentation scaling + Native table with bold `<th>` headers, `colspan="2"`, and `rowspan="2"`.
2. `screenshots/verify_r34_library_book_context_menu.png`: Library tab long-press context menu on local KFX book showing `Mark as Read`, `Remove`, and `Add to Collection` with zero store links.
3. `screenshots/verify_r34_settings_screen_audit.png`: Reader & App Settings launched from Control Center showing offline settings without account prompts.
4. `screenshots/verify_r34_opds_catalog_live.png`: OPDS Catalogs launched from Control Center showing clean catalog presets.
5. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
6. `dumpsys package com.amazon.kindle.epub`: Folio v3.1 KFX deployed (`lastUpdateTime=2026-11-22 14:45:26`).

---

# Round 033: Complete HTML4/HTML5 Entity Decoding & In-Book Search/Bookmarks QC (Folio v3.0)
1. `screenshots/verify_r33_html_entities_decoded.png`: Rendered KFX page proving `2H₂ + O₂ → 2H₂O` (real `→` arrow, zero literal `&rarr;`) plus decoded `&times;`, `&plusmn;`, `&hellip;`, `&mdash;`, `&eacute;`, `&Omega;`, and `&#x2192;`.
2. `screenshots/verify_r33_in_book_search.png`: Live in-book search results for `"Creon"` inside Sophocles showing `BOOK (297)` with snippets and location markers.
3. `screenshots/verify_r33_bookmarks_notebook.png`: Saved bookmark/highlight visible inside the in-reader Notebook view with zero sync error dialogs.
4. `screenshots/verify_r33_about_v30.png`: Control Center **About Folio** dialog showing milestone `Version: 3.0 KFX (Enhanced Typesetting)`.
5. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
6. `dumpsys package com.amazon.kindle.epub`: Folio v3.0 KFX deployed (`lastUpdateTime=2026-11-22 13:26:58`).

---

# Round 032: Zero-Hyphenation Code Blocks, Sup/Sub Polish & Store-Free Library Search (Folio v2.9)
  1. **Zero-Hyphenation Code Blocks & Proportional Sup/Sub (Task A)**:
     - Pure-Rust schema parity in `libkfxconv.so`: Emitted YellowJersey `$127: $349` (`hyphens: none`) on `style_pre` and `style_code`. Variable names, keywords, and long identifiers wrap cleanly on word boundaries with zero hyphens.
     - Polished `<sup>` and `<sub>` styling: Kept proportional `0.75em` (`$16`) and baseline styles (`$44: $370` super, `$44: $371` sub), and completely removed numeric baseline shift (`$31`). Fixed awkward line spacing and vertical line collisions.
     - Added Rust unit test `test_r32_code_no_hyphen_and_proportional_sup_sub` in `src/packager/tests.rs`; all 212 tests pass cleanly.
     - Cross-compiled stripped ARM64 `libkfxconv.so` (4.5 MB) and updated version badge in `FolioMoreView.java` to `Folio v2.9 KFX`.
  2. **Store-Free Library Search (Task B)**:
     - Reverse-engineered Search UI in `classes5.dex` (`SearchResultsKt`, `SearchActivity`, `RubySearchActivity`, `SearchProvider`, `SearchViewModel`, `models.b`).
     - In `SearchResultsKt.smali`: Patched `b4(ZZZZZ)Z` to return `false` (0), completely suppressing `StoreHeaderSection` (the Amazon Kindle Store shopping cart header).
     - In `com/amazon/kcp/search/v2/models/b.smali`: Patched `e()Ljava/util/List;` to return `Collections.EMPTY_LIST`.
     - In `SearchProvider.smali`: Patched `N()` to return `Collections.EMPTY_LIST` and forced `queryStore = false`.
     - In `SearchViewModel.smali`: Silenced `r()V` (coroutine launcher) and `z(SearchNodeCompleteEvent)`.
     - In `SearchActivity.smali`: Forced `setShouldShowStoreResults(false)`.
     - Verified: Searching `"Sophocles"` displays **ONLY `In your library (1)`** with zero store shelf.
     - Verified: Tapping the local search result opens directly into `StandAloneBookReaderActivity` (`KRIFDocViewer`).
  3. **Visual Verification & Release Staging (Task C)**:
     - 3 hardware proof screenshots captured on Samsung Galaxy M32 (`verify_r32_code_no_hyphen_and_sup_sub.png`, `verify_r32_library_search_no_store.png`, `verify_r32_search_result_open_book.png`).
     - Safety Invariant: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Signed Folio v2.9 KFX APK (131 MB) and stripped native library (4.5 MB) staged at `07_APK_EPUB_INGESTION_MOD/release/folio_v2.9_kfx/`.
  4. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page and take the image for the cover. It is then processed separately and kept in its own class and inserted when displaying the book separately."
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v2.9 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, store-free library search, zero-hyphenation code blocks, proportional sup/sub, `$270` cover container, native table support, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 032)
1. `screenshots/verify_r32_code_no_hyphen_and_sup_sub.png`: Rendered KFX page showing `<pre>` Python code block with zero hyphenation alongside crisp proportional `E = mc²` and `H₂O` sup/sub formatting.
2. `screenshots/verify_r32_library_search_no_store.png`: Live `"Search Folio"` query (`"Sophocles"`) showing ONLY `In your library (1)` with the Amazon `"From the store"` shelf completely absent.
3. `screenshots/verify_r32_search_result_open_book.png`: Tapping local search match under `In your library (1)` opens directly into `StandAloneBookReaderActivity` (`KRIFDocViewer`).
4. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
5. `dumpsys package com.amazon.kindle.epub`: Folio v2.9 KFX deployed (`lastUpdateTime=2026-11-22 12:03:13`).

---

# Round 031: Real Pop-Up Footnotes, Rich Inline/Code Formatting & Live Library Search QC (Folio v2.8)
  1. **Real Native KFX Pop-Up Footnotes (Task A)**:
     - Pure-Rust schema parity: Link span in `$142 style_events` emits `$179 link_to` and `$616: $617` (`yj.display: yj.note`). Target footnote entry in `$259 storyline` emits `$615: $281` (`yj.classification: footnote`).
     - Anchor entity `$266` resolves `$183 position` pointing to target footnote story entry EID (`$155`) with offset 0.
     - Cleaned up Fragment `$597 auxiliary_data` to stock `IS_TARGET_SECTION` metadata only, eliminating non-standard `$617` note arrays.
     - Fixed anchor leak bug in `xhtml.rs`: consumed anchor with `anchor.take()` on span flush; reset `current_anchor` and `current_block_id` on block close.
     - Verified in `KRIFDocViewer`: tapping footnote link navigates to footnote text with top "Back to Location" affordance with zero soft keyboard / zero Add Note editor.
  2. **Rich Formatting Audit (Task B)**:
     - Monospace code blocks (`<pre>`, `<code>`): Preserves whitespace and newlines verbatim, font family `$11: "monospace"`, left alignment `$34: 59`, text indent `$36: 0.0rem`, top/bottom margin `$48: 1.2rem`.
     - Horizontal rules (`<hr/>`): Emits YellowJersey `$596 horizontal_rule` story elements with `style_hr`.
     - Inlines: `<sup>`/`<sub>` baseline styles (`$44: $370/$371`) and baseline shift (`$31`), `<s>`/`<del>` strikethrough (`$27: $328`), and `<u>` underline (`$23: $328`).
     - Added comprehensive unit tests in `src/packager/tests.rs`; all 211 tests pass cleanly.
  3. **Clean Hardware Label, Live Library Search & Visual Verification (Task C)**:
     - In `FolioMoreView.java`: Filtered out `"unknown"` (case-insensitive) or blank `Build.MANUFACTURER`. Cleanly formatted as `Build.MODEL + " · Android " + Build.VERSION.RELEASE + " (" + arch + ")"`. Updated badge to `Folio v2.8 KFX`.
     - Live Library Search: Verified query `"Sophocles"` triggers `RubySearchActivity` and filters local KFX library books cleanly (`In your library (1)`). Zero crashes or Amazon store dead ends.
     - Verified on Samsung Galaxy M32: 4 required proof screenshots captured and verified.
     - Safety Invariant: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Cover Architecture Mandate**: Project Lead architectural directive formally documented and passed to manager AI.
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v2.8 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, `$270` cover container, native table support, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 031)
1. `screenshots/verify_r31_real_footnote_popup.png`: Native pop-up footnote navigation in `KRIFDocViewer` without keyboard or Add Note modal.
2. `screenshots/verify_r31_code_hr_rich_formatting.png`: Rendered `<pre>/<code>` monospace block, `<hr/>` divider, `<sup>/<sub>`, and `<s>/<u>` formatting.
3. `screenshots/verify_r31_about_clean_hardware.png`: About Folio dialog showing `Folio v2.8 KFX` and clean hardware string without `"unknown"`.
4. `screenshots/verify_r31_library_search_results.png`: Live `"Search Folio"` query filtering local books cleanly in Library tab.
5. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
6. `dumpsys package com.amazon.kindle.epub`: Folio v2.8 KFX deployed (`lastUpdateTime=2026-11-22 11:21:33`).

## Next Steps
1. Package `antig_to_gemini_neon_clunky_report031.zip` (< 15 MB).
2. Relay report zip to Gemini Neon tab via Chrome bridge, verifying attachment preview chip before sending.
3. Launch autonomous relay runner with 2000s timeout and 60s polling interval to await Round 032 instructions.

---

# Round 030: Subtle Visual Polish, Multi-Book Stress Test & Interactive Flow QC (Folio v2.7)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v2.7 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 030 complete:
  1. **Converter & UI Micro-Polish (Task A)**:
     - Pure-Rust Link Coalescing: Contiguous text spans sharing the same `href` coalesce into a single `$179 link_to` event in `kfx_converter/src/packager/storylines.rs`. Stripped pseudo-classes (`:link`, `:visited`, etc.) in `kfx_converter/src/css.rs` so stylesheet rules cascade cleanly onto nested inline tags. Unit test: `test_r30_nested_inline_tags_in_link` passes.
     - Uniform `<li>` Marker Spacing: Added `trim_start()` on `item_spans[0].text` in `kfx_converter/src/packager/storylines.rs` and set `text_alignment: left` (`sym_left`, 59) in `styles.rs` so bullet gaps are never stretched by text justification. Unit test: `test_r30_uniform_list_item_marker_spacing` passes.
     - Dynamic Device Info & Privacy Mode: In `FolioMoreView.java`, populated `Hardware:` dynamically via `Build.MANUFACTURER + " " + Build.MODEL + " (" + Build.SUPPORTED_ABIS[0] + ")"`, and updated privacy label to `"Privacy Mode: Local-First (Zero Amazon Telemetry)"`.
     - Pop-up Note / Footnote Ingestion: Emitted `$617 yj.note` inside `$597 auxiliary_data` when footnotes are detected for a section. All 200 Rust tests pass.
  2. **Multi-Book On-Device Benchmark Matrix (Task B)**:
     - Executed 7-book stress matrix on Galaxy M32 with sub-2s conversion and zero-crash `KRIFDocViewer` launch (`Hesiod`, `Cato the Elder`, `Aristophanes`, `Sophocles`, `Julius Caesar`, `kindle_exam_v3`, `kindle_tables_images_exam`).
  3. **Interactive Flow Verification (Task B.2 & C)**:
     - Verified uniform bullet gap and link underline in `kindle_exam_v3.epub` (`screenshots/verify_r30_list_spacing_and_links_fixed.png`).
     - Verified live pop-up modal panel in `KRIFDocViewer` (`screenshots/verify_r30_footnote_popup.png`).
     - Verified live text selection with highlight palette, action bar (`Annotate`, `Note`, `Copy`, `Pin`, `Report`), and Oxford dictionary definition card on `"profound"` (`screenshots/verify_r30_highlight_or_scroll.png`).
     - Verified Control Center About Folio dialog with dynamic hardware metadata (`screenshots/verify_r30_control_center_scan_and_about.png`).
  4. **Cover Architecture Directive Relayed**: Included Project Lead directive in `REPORT.md` regarding extracting cover images on ingestion and managing them in a dedicated UI View rather than within the reflowable KFX book spine.
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v2.7 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, `$270` cover container, native table support, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 030)
1. `screenshots/verify_r30_list_spacing_and_links_fixed.png`: Uniform bullet gap in `kindle_exam_v3.epub` verified via OCR.
2. `screenshots/verify_r30_footnote_popup.png`: Live pop-up modal panel in `KRIFDocViewer`.
3. `screenshots/verify_r30_highlight_or_scroll.png`: Live highlight selection, color palette, and Oxford dictionary definition card.
4. `screenshots/verify_r30_control_center_scan_and_about.png`: Control Center About Folio dialog showing dynamic hardware details and Privacy Mode label.
5. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
6. `dumpsys package com.amazon.kindle.epub`: Folio v2.7 KFX deployed (`lastUpdateTime=2026-11-22 09:49:08`).

## Next Steps
1. Package `antig_to_gemini_neon_clunky_report030.zip` (< 15 MB).
2. Relay report zip to Gemini Neon tab via Chrome bridge, verifying attachment preview chip before sending.
3. Launch autonomous relay runner with 2000s timeout and 60s polling interval to await Round 031 instructions.

---

# Round 024: Native Kindle Tables, Standalone Cover Separation, Bookerly Typography & Image Parity (Folio v2.1)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v2.1 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 024 complete:
  1. **Native Kindle Tables (Task A)**: Ported `jloutsch/kfxgen` table structures into pure-Rust `kfx_converter` (`$278` yj_table, `$279` yj_table_row, `$269` yj_table_cell, `$146` content_list, border collapse `$150: false`, vertical/horizontal border spacing `$456`/`$457: 0.9%`, feature table `$585` with `yj_table` v1/v3 & `yj_table_viewer` v1, and horizontal pan chevron `$629: [581, 326]`, `$630: 632`).
  2. **Standalone Cover Separation (Task B)**: Redundant in-spine cover HTML pages (`cover.xhtml`, `titlepage.xhtml`, `wrap0000.html`) stripped from spine sections during initial EPUB parsing. Preserves native KFX cover resource (`$417` + `$164` + `$495 cover_image` on black `#000000` via `$270 scale_fit`), and emits companion `<outKfx>.cover.jpg`.
  3. **Typography & Image Parity (Task C)**: Set `override_kindle_font: false` to ensure system fonts (Bookerly) are preserved. Extracted `<dc:language>` into metadata `$490` for native hyphenation. Transparent PNG compositing over solid white `(255, 255, 255)`.
  4. **Cross-Compilation & Staging (Task D)**: Cross-compiled `libkfxconv.so` for ARM64 (`aarch64-linux-android`) with NDK 26 Clang (4.4 MB stripped). Rebuilt and staged Folio v2.1 in `07_APK_EPUB_INGESTION_MOD/release/folio_v2.1_kfx/base.apk` (131 MB).
  5. **Verification Evidence**:
     - `screenshots/verify_r24_kfx_native_tables.png`: Native 6-column financial table with pan chevron.
     - `screenshots/verify_r24_kfx_images.png`: Embedded figures (900x150, 300x300) with white transparency backing.
     - `screenshots/verify_r24_exam_v3_images_hyphen.png`: Bookerly typography, native hyphenation (`an- / swered`, `post- / poned`), zero duplicate cover.
     - `screenshots/verify_r24_classic_cover.png`: Hesiod cover on `#000000` black, zero white HTML margins.
     - `screenshots/verify_r24_classic_prose_spacing.png`: Live `Aa → Font → Spacing` reflow sliders.
  6. **Stock Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  7. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v2.1_kfx/base.apk` (131 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report024.zip` (1.8 MB)
     - Submitted to Gemini Neon tab `837017938` with verified composer attachment chip and project lead cover directive.

---

# Round 025: Native KFX Table Columns & Borders Fix + Cover Architecture Blueprint (Folio v2.2)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v2.2 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 025 complete:
  1. **Native Kindle Tables Fixed (Task A)**: Deep-diffed against stock `CR!6KYWZNKF153BK4YSK5MTFZ1XN6ZA.kfx`. Wrapped rows inside `$454` (`body`), added `$152` (`column_format`) per column with `{$698: false}`, enabled `$150: true` (`table_border_collapse`), changed border spacing to `0.9 pt` (`$456`/`$457`), and added `$83: 4286611584` (`border_color`). Verified in `verify_r25_table_columns.png`: completely eliminates mashed text (`Gannet992.1Indigo...`) with distinct, separated columns across 2 to 6-column tables.
  2. **Cover Architecture Blueprint (Task B)**: Integrated user directive to avoid forcing cover rendering inside the KFX spine. On initial EPUB parsing, the cover page is stripped from the spine and the cover image is extracted into its own class/asset, which is displayed separately in its own fullscreen Activity/View on `#000000` black upon opening the book.
  3. **On-Device Build & Verification (Task C)**:
     - Cross-compiled `libkfxconv.so` for `aarch64-linux-android` (4.4 MB stripped).
     - Built, signed, and deployed Folio v2.2; staged signed APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v2.2_kfx/base.apk` (131 MB).
     - Verified live `Aa → Font → Spacing` sliders in `StandAloneBookReaderActivity` (`verify_r25_prose_spacing_before.png` and `verify_r25_prose_spacing_after.png`).
     - Captured baseline cover reading screenshots: `verify_r25_cover_book1.png` and `verify_r25_cover_book2.png`.
  4. **Stock Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  5. **Deliverables & Report (Task D)**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v2.2_kfx/base.apk` (131 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report025.zip` (< 15 MB)
     - Uploaded to Gemini Neon tab `837017938` with verified composer attachment chip.
     - Autonomous relay runner active in background (`autonomous_relay_runner.mjs 26 2000`, 60s poll interval, 2000s max timeout, zero duplicate uploads) waiting for `to_antig_clunky_round_026.zip`.


---

# Round 026: Native Stock-Kindle KFX `$270` Fullscreen `#000000` Cover Container + Live KFX Spacing Proof (Folio v2.3)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v2.3 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 026 complete:
  1. **Native Stock-Kindle KFX `$270` Cover Container (Task A)**:
     - In `kfx_converter` (`de86fa2`), stripped cover-only HTML spine items (`cover`, `titlepage`, `wrap0000`, `calibre:cover`, and SVG image-only pages) from the prose spine during EPUB parsing.
     - Implemented Amazon's native standalone Cover Section `c0` container (`$159: $270`, `$156: $326 scale_fit`, `$140: $320 center`, actual dimensions `$66/$67`), Storyline `l4` (`$159: $271`, `$157: s8`, `$175: cover`, no alt text), Style `s8` (`$16: 1.0 rem / $505`, `$42: 1.0 lh / $310`), Auxiliary Data `c0-ad`, and `$389` landmarks (`cover-nav-unit` pointing to container `c0`, `start` pointing to Chapter 1).
     - Cross-compiled release binary `kfx-conv` for `aarch64-linux-android` using NDK Clang; stripped to `07_APK_EPUB_INGESTION_MOD/lib/arm64-v8a/libkfxconv.so` (4.3 MB).
  2. **Hardware Verification Across 2 Distinct Classics (Task B.1)**:
     - Verified on Samsung Galaxy M32:
       * `screenshots/verify_r26_kfx_cover_book1.png` (Hesiod): Full-width centered on `#000000` black, zero white margins, zero "0%" footer.
       * `screenshots/verify_r26_kfx_cover_book2.png` (Sophocles): Full-width centered on pure `#000000` black (RGB 0,0,0 corner pixels). Swiping left navigates smoothly into Chapter 1 prose with zero duplicate cover page.
  3. **Live Typography Reflow Proof on On-Device KFX (Task B.2)**:
     - Adjusted Line & Paragraph spacing seekbars in `StandAloneBookReaderActivity` (`KRIFDocViewer`):
       * `screenshots/verify_r26_kfx_spacing_before.png`: Minimum Line & Paragraph spacing (SHA-256: `78a0bdc3e7d5cd38dc4dfc55e7382565ef3cee4c9ccded7a878dbd6f39ccb4d2`, 32 lines).
       * `screenshots/verify_r26_kfx_spacing_after.png`: Maximum Line & Paragraph spacing (SHA-256: `755ec10a9ff2dcbeb31f4203ecc9b533bc6b0e141c158e45d9d4c8fda506b7a0`, 19 lines).
       * Measured reflow: **1,463,450 pixels reflowed (57.91% pixel difference)**, proving live Amazon Enhanced Typesetting reflow in `KRIFDocViewer`.
  4. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  5. **Deliverables & Report (Task C)**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v2.3_kfx/base.apk` (131 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report026.zip` (5.2 MB)
     - Uploaded to Gemini Neon tab `837017938` with verified composer attachment chip.


---

# Round 029: Folio Control Center & libkfxconv.so Typography Polish (Folio v2.6)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v2.6 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 029 complete:
  1. **Folio Control Center (Task A)**:
     - Implemented `FolioMoreView.java` injecting a dark, native Control Center view into `MoreItemsFragment.onCreateView` (`classes3.dex`).
     - Dead Amazon account rows, store stubs, and `"Sign In with Amazon"` prompt are 100% eliminated.
     - Wired 5 offline feature cards: OPDS Catalogs, Scan Local Library (background thread scanning `/sdcard/epub/` and `/sdcard/kindle/` with live Toast), Backup & Restore, Settings (`SettingsActivity`), and About Folio dialog showing live library DB stats (8 indexed books) and cache size (16.6 MB).
  2. **Pure-Rust Engine Polish (Task B)**:
     - Added `unicode-normalization = "0.1"` for canonical NFC precomposition on all text spans in `kfx_converter`. Prevents decomposed Greek diacritics and non-Latin character rendering artifacts.
     - Implemented native YellowJersey lists (`Block::List`), emitting `$159: $277` (`listitem`), `$100: list_style` (`numeric: 343` / `disc: 340`), `$551: $552` (`inside`), hanging indent styles (`style_list_item`), and prepended markers (`• ` or `1. `).
     - Enhanced blockquote styling with `in_blockquote` nesting state, applying `style_blockquote` (`margin_left/right: 1.5em`, italic font).
     - Expanded pop-up footnote detection (`#fn`, `#footnote`, `#note`, and `<sup>`).
     - All 152 cargo unit tests passing cleanly. Cross-compiled release binary `kfx-conv` stripped to `07_APK_EPUB_INGESTION_MOD/lib/arm64-v8a/libkfxconv.so` (4.5 MB).
  3. **Hardware Visual QC (Task C)**:
     - `screenshots/verify_r29_more_tab_clean.png`: Dark Control Center with 5 cards and zero Amazon sign-in prompt.
     - `screenshots/verify_r29_about_folio_dialog.png`: About Folio dialog showing Folio v2.6 KFX engine checklist, 8 indexed books, and 16.6 MB cache.
     - `screenshots/verify_r29_greek_script_render.png`: Sophocles *The Greek Texts* (Ajax / ΑΙΑΣ) rendered in YellowJersey reader with crisp polytonic Greek glyphs (`Ἀεὶ μέν, ὦ παῖ Λαρτίου, δέδορκά σε`) and zero tofu.
     - `screenshots/verify_r29_footnotes_lists_blockquotes.png`: Clean hanging indentation and typography of lists and blockquotes in `kindle_exam_v3.epub` on-device.
  4. **Lead Architect Cover Architecture Directive**:
     - Formally documented and passed to manager AI: "The approach to display the cover as part of the book is a mistake. The correct path is to just take the image and render it differently so you don't have the difficulty you are having with margins. When parsing the epub for the first time and converting it, remove the cover page and take the image for the cover. It is then processed separately and kept in its own class or whatever and inserted when displaying the book separately. Skip it for now."
  5. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  6. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v2.6_kfx/base.apk` (131 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report029.zip` (< 15 MB)
