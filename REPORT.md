# Round 034 Final Verification & Engineering Report
**Project Folio: Enhanced Offline EPUB/KFX Reader**  
**Date:** November 22, 2026  
**Target Device:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13)  
**Release Badge:** Folio v3.1 KFX (Enhanced Typesetting & Hardened Layouts)  
**Safety Invariant:** Stock `com.amazon.kindle` (v8.160.1.0) remains **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 1. Executive Summary

Round 034 successfully completes all requirements specified in `INSTRUCTIONS_ROUND_034.md`:
1. **Task A (Pure-Rust `libkfxconv.so` Edge-Case Hardening)**:
   - Implemented depth tracking in `src/epub/xhtml.rs` and recursive emission in `src/packager/storylines.rs` for multi-level nested lists (`<ul>`/`<ol>`), scaling margins by depth (`list_item` 1.5em, `list_item_d2` 3.0em, `list_item_d3` 4.5em) and cycling bullet glyphs (`•`, `◦`, `▪`).
   - Implemented `<thead>` and `<th>` bold and center styling in native KFX tables ($278/$454/$279/$269) and verified HTML `colspan` and `rowspan` support using YellowJersey Ion symbols `$148` (`table_column_span`) and `$149` (`table_row_span`).
   - Hardened parser and packager against malformed assets: missing image references are gracefully skipped without panicking or creating invalid locator mandatory dependencies.
   - Added 3 unit tests in `src/packager/tests.rs`; all **207 cargo tests** pass cleanly.
   - Cross-compiled stripped ARM64 `libkfxconv.so` (4,773,560 bytes) and staged release APK for **Folio v3.1 KFX**.
2. **Task B (Library Context Menu, Settings & OPDS Catalogs UI Audit)**:
   - Audited Library long-press context menu on local KFX books: by classifying sideloaded content as `BT_EBOOK_PDOC`, dead Amazon-store and cloud items (`"View in store"`, `"Add to Goodreads"`) are suppressed, leaving clean local actions (`"Mark as Read"`, `"Remove"` / delete local download, `"Add to Collection"`).
   - Audited **Reader & App Settings**: resolved launch parameter by invoking `SettingsComposeActivity` / `EXTRA_TITLE`, opening cleanly without sign-in prompts, crashes, or dead account sub-rows.
   - Audited **OPDS Catalogs**: verified catalog presets (Project Gutenberg, Standard Ebooks, Internet Archive) and custom feed URL input from Control Center.
3. **Task C (Visual Verification & Hardware Proof)**:
   - Captured 4 physical hardware proof screenshots on Samsung Galaxy M32:
     - `screenshots/verify_r34_nested_lists_and_table_spans.png`: Multi-level nested lists with indentation scaling + native table with bold headers, `colspan="2"`, and `rowspan="2"`.
     - `screenshots/verify_r34_library_book_context_menu.png`: Clean local context menu with zero Amazon Store dead ends.
     - `screenshots/verify_r34_settings_screen_audit.png`: Offline settings screen without sign-in prompts.
     - `screenshots/verify_r34_opds_catalog_live.png`: OPDS Catalogs preset dialog.

---

## 2. Task A: Pure-Rust `libkfxconv.so` Hardening

### Multi-Level Nested Lists
- **Depth Tracking & Margin Scaling**: In `kfx_converter/src/packager/styles.rs`, styles `list_item_d2` (margin-left: 3.0em) and `list_item_d3` (margin-left: 4.5em) were added alongside base `list_item` (margin-left: 1.5em).
- **Marker Cycling**: In `kfx_converter/src/packager/storylines.rs`, recursive function `emit_list_items` assigns markers according to depth:
  - Depth 1: `•` (`\u{2022}`), style `list_item`
  - Depth 2: `◦` (`\u{25E6}`), style `list_item_d2`
  - Depth 3+: `▪` (`\u{25AA}`), style `list_item_d3`
- **Missing Tag Resilience**: In `src/epub/xhtml.rs`, consecutive `<li>` elements without intervening `</li>` close the open list item automatically before starting the next item.

### Native Table `<thead>`, `<th>` and Spans
- **Row Group & Header Propagation**: `xhtml.rs` tracks `in_thead: bool`. Any cell within `<thead>` (whether `<th>` or `<td>`) is marked as `is_header = true`.
- **Header Styling**: In `styles.rs`, `table_cell_header` (`s_th`) sets `font-weight: bold` and `text-align: center` (`sym_text_alignment: sym_center`).
- **Colspan & Rowspan (`$148` / `$149`)**:
  - YellowJersey symbols `$148` (`table_column_span`) and `$149` (`table_row_span`) are dynamically emitted in style dictionaries for cells where `colspan > 1 || rowspan > 1`.
  - Cells carry `$148: cell.colspan` and `$149: cell.rowspan` directly on their Ion entry struct.

### Malformed Asset Resilience
- Missing `<img src="...">` resources not present in `book.resources` are bypassed gracefully without panic, and alt text is emitted if present.
- In `locators.rs`, `mandatory_dependencies` ($419) only registers resources confirmed to exist in `book.resources`.

### Rust Unit Tests
- `test_r34_nested_lists_depth_indentation`: Verifies 3-level list emits correct markers and depth styles.
- `test_r34_table_thead_th_and_spans`: Verifies table emits `$148` and `$149` symbols, row group bodies, and bold header styles.
- `test_r34_missing_image_resilience`: Verifies missing image assets produce a valid KFX container without errors.
- Test Suite Result: **207 passed; 0 failed; 0 ignored**.

---

## 3. Task B: Library Context Menu, Settings & OPDS Catalogs UI Audit

### 1. Library Long-Press Context Menu
- When long-pressing a book card in Folio Library, the app enters multi-selection mode:
  - Top action bar displays: `Done`, `+` (Add to Collection), Trash can (`Remove` / delete local download), and `⋮` (More options).
  - Tapping `⋮` displays: `Mark as Read`.
  - By registering local books as `TYPE = 'BT_EBOOK_PDOC'`, dead Amazon cloud/store items (`"View in store"`, `"Add to Goodreads"`, `"Shop for..."`) are suppressed. Local actions remain functional.

### 2. Reader & App Settings Screen
- Opened via Control Center (`Reader & App Settings` card) or direct intent (`com.amazon.kcp.library.SettingsComposeActivity` / `SettingsActivity` with `EXTRA_TITLE`).
- Verified Sections / Sub-rows:
  1. `Push notifications` — "Notifications are on."
  2. `Download to SD card` — "Save new downloads to your external SD Card. 1.8 GB free" (Toggle checkbox).
  3. `Local file access` — "Enable or disable access to view files on your device in your Kindle library."
  4. `Manage additional fonts` — "Download or remove reading fonts with large file sizes."
- Zero Amazon account prompts, zero sign-in modals, and zero crashes.

### 3. OPDS Catalogs Feed Browser
- Opened via Control Center (`OPDS Catalogs` card) or Home tab (`+ OPDS` pill).
- Displays clean catalog presets:
  - `Project Gutenberg` (`https://m.gutenberg.org/ebooks.opds/`)
  - `Standard Ebooks` (`https://standardebooks.org/opds/all`)
  - `Internet Archive` (`https://bookserver.archive.org/catalog/`)
- Allows custom OPDS 1.2 feed entry with Save & Browse functionality.

---

## 4. Task C: Visual Verification on Samsung Galaxy M32

Hardware verification screenshots captured on Samsung Galaxy M32 (`32001776e8e82623`):

| Screenshot | Description | Verified Features |
|---|---|---|
| `screenshots/verify_r34_nested_lists_and_table_spans.png` | Rendered KFX page in KRIFDocViewer | 3-level nested list (`•`, `◦`, `▪`) with indentation scaling + Native table with bold `<th>` headers, `colspan="2"`, and `rowspan="2"` |
| `screenshots/verify_r34_library_book_context_menu.png` | Library tab long-press context menu | Clean local options (`Mark as Read`, `Remove`, `Add to Collection`) with zero store links |
| `screenshots/verify_r34_settings_screen_audit.png` | Reader & App Settings | 4 offline settings options, zero Amazon account prompts |
| `screenshots/verify_r34_opds_catalog_live.png` | OPDS Catalogs | Clean catalog presets & custom feed input dialog |

---

## 5. Architecture Note for Manager AI: Cover Rendering Architecture

> **USER DIRECTIVE CONVEYANCE**:
> Displaying the cover as an inline XHTML page within the reflowable book spine is structurally problematic due to engine-level margin and layout constraints.
> **Recommended Approach**:
> 1. When parsing and converting an EPUB for the first time, strip the cover page from the reflowable spine.
> 2. Extract and process the cover image separately as a standalone asset, caching it in its own data structure/class.
> 3. Render the cover image in a dedicated display component (e.g., `CoverOverlayView` or initial page overlay) rather than reflowing it through the standard spine storyline.

---

## 6. Safety Invariant & Artifact Sign-off

- **Stock Kindle Safety Invariant**:
  - Package: `com.amazon.kindle` (v8.160.1.0)
  - `lastUpdateTime`: `2026-11-19 21:59:23` (Verified **UNTOUCHED and READ-ONLY**).
- **Folio Mod Package**:
  - Package: `com.amazon.kindle.epub`
  - APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v3.1_kfx/base.apk` (131 MB)
  - Native Engine: `07_APK_EPUB_INGESTION_MOD/lib/arm64-v8a/libkfxconv.so` (4.6 MB)
- **Rust Unit Tests**: 207 passed, 0 failed.
