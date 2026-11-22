# Round 029 Deliverables Report: Folio Control Center & libkfxconv.so Typography Polish

**Date**: 2026-11-22  
**Device**: Samsung Galaxy M32 (`32001776e8e82623`, Android 13, ARM64)  
**Target Package**: `com.amazon.kindle.epub` (Folio v2.6 KFX)  
**Stock Invariant**: `com.amazon.kindle` (v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`) remains **100% UNTOUCHED and READ-ONLY**. Verified intact.

---

## 1. Executive Summary

Round 029 achieves two major milestones in our offline reader evolution:
1. **Folio Control Center (Task A)**: Completely repurposed the More tab in `com.amazon.kindle.epub` by injecting a dark, native `FolioMoreView` into `MoreItemsFragment.onCreateView` (`classes3.dex`). The dead Amazon account rows, store stubs, and `"Sign In with Amazon"` prompt are 100% eliminated. In their place is a dedicated offline command center with OPDS Catalogs, Scan Local Library, Backup & Restore, Settings, and an About Folio dialog showing live library and disk cache statistics.
2. **Pure-Rust Typesetting Polish (Task B)**: Upgraded `kfx_converter` (`libkfxconv.so`, ARM64 v8a) with:
   - Unicode canonical NFC normalization (`unicode-normalization = "0.1"`) to guarantee Polytonic Greek and non-Latin scripts render without decomposed combining character artifacts or tofu glyphs.
   - Native YellowJersey list support (`Block::List`), emitting `$159: $277` (`listitem`), `$100: list_style` (`numeric: 343` / `disc: 340`), `$551: $552` (`list_style_position: inside`), hanging indent styles (`style_list_item`), and clean markers.
   - Enhanced blockquote styling with `in_blockquote` nesting state, applying `style_blockquote` (`margin_left/right: 1.5em`, italic font).
   - Expanded pop-up footnote detection (`#fn`, `#footnote`, `#note`, and `<sup>`).
3. **Hardware Visual QC (Task C)**: Captured 4 proof screenshots on physical Samsung Galaxy M32 hardware verifying clean Control Center, live About Folio stats dialog, Sophocles Greek script rendering, and Exam v3 list/blockquote typography.

---

## 2. Project Lead Cover Architecture Guidance

> [!IMPORTANT]
> **Directive from Lead Architect regarding book covers**:
> *"The approach to display the cover as part of the book is a mistake. The correct path is to just take the image and render it differently so you don't have the difficulty you are having with margins. Pass that along to the manager AI.*
> *The way to approach the cover to render properly reliably is the following:*
> *When parsing the epub for the first time and converting it, remove the cover page and take the image for the cover. It is then processed separately and kept in its own class or whatever and inserted when displaying the book separately. Skip it for now."*

This architectural direction is formally adopted and will guide all subsequent cover rendering tasks.

---

## 3. Task A: Folio Control Center Implementation

### A. View Component (`FolioMoreView.java`)
Implemented `com.amazon.kindle.epub.catalog.FolioMoreView`, a dark, elevation-styled Android `ScrollView` featuring:
- **Header**: "Control Center" title with a "Folio v2.6 KFX" pill badge and "Offline Reader & Library Tools" subtitle.
- **Card 1: OPDS Catalogs**: Quick-launch buttons for Project Gutenberg, Standard Ebooks, and Internet Archive, plus custom feed URL configuration.
- **Card 2: Scan Local Library**: Background scanner thread targeting `/sdcard/epub/` and `/sdcard/kindle/`. Automatically runs `libkfxconv.so` on untranscoded `.epub` files, indexes new KFX containers into SQLite `kindle_library.db`, and displays a live Toast summary.
- **Card 3: Backup & Restore**: Wires into `OpenCatalogView.showBackupRestoreDialog()` to create or restore JSON backups of reading progress, annotations, and bookmarks.
- **Card 4: Reader & App Settings**: Dispatches intent directly to `com.amazon.kcp.library.SettingsActivity`.
- **Card 5: About Folio**: Opens an elevation dialog detailing engine capabilities, active features checklist, indexed books count from `kindle_library.db`, and local cache size in MB.

### B. Smali Patch (`MoreItemsFragment.smali` in `classes3.dex`)
Added hook in `MoreItemsFragment.onCreateView` via `07_APK_EPUB_INGESTION_MOD/tools/patch_classes3.sh`:
```smali
invoke-static {v0}, Lcom/amazon/kindle/epub/catalog/FolioMoreView;->wrap(Landroid/view/View;)Landroid/view/View;
move-result-object v0
return-object v0
```
This intercepts the Amazon view hierarchy before layout attachment and replaces it entirely with `FolioMoreView`, cleanly removing Amazon authentication without breaking fragment lifecycles or backstack navigation.

---

## 4. Task B: Pure-Rust Converter Polish (`libkfxconv.so`)

### A. Unicode NFC Canonical Normalization
- Added `unicode-normalization = "0.1"` to `kfx_converter/Cargo.toml`.
- In `src/epub/xhtml.rs` (`flush_current_span`), passed all decoded text spans through `.nfc().collect::<String>()`.
- Precomposes Greek vowels with multiple diacritics (oxia, varia, psili, dasia, perispomeni, iota subscript) into single precomposed UTF-8 codepoints in the `U+1F00–U+1FFF` block, preventing glyph clipping or missing font glyph fallback in Android's text rasterizer.

### B. Native YellowJersey Lists
- Extended `StyleSymMap` and `styles.rs` with `list_item` (`$159: $277`, `margin_left: 1.5em`, `text_indent: -1.0em`) and `list_item_cont`.
- In `src/packager/storylines.rs`, unpacked `Block::List` into native YellowJersey list item structures:
  * Symbol `$159: $277` (`listitem`)
  * Symbol `$100: list_style` (`numeric: 343` for ordered, `disc: 340` for unordered)
  * Symbol `$551: $552` (`list_style_position: inside`)
  * Generated bullet markers (`"• "`) or numeric markers (`"1. "`) with negative text-indent for crisp hanging indentation.

### C. Blockquote Inheritance & Footnote Detection
- Added `in_blockquote` state tracker in `src/epub/xhtml.rs` ensuring `<p>` tags inside `<blockquote>` inherit `style_blockquote` (`margin_left: 1.5em`, `margin_right: 1.5em`, italic typography).
- Expanded footnote detection to recognize `#fn`, `#footnote`, `#note`, and `<sup>` anchors, emitting `$159: $266` noteref links that trigger Kindle's native pop-up footnote preview panel.
- All 152 unit tests pass cleanly (`152 passed; 0 failed; 0 ignored`).
- Cross-compiled release binary for `aarch64-linux-android` (stripped to 4.5 MB).

---

## 5. Task C: Hardware Visual Verification

Verified live on physical Samsung Galaxy M32 hardware:
1. `screenshots/verify_r29_more_tab_clean.png`: Dark Control Center with 5 feature cards and zero Amazon sign-in prompt.
2. `screenshots/verify_r29_about_folio_dialog.png`: Live dialog showing Folio v2.6 KFX engine checklist, 8 indexed books, and 16.6 MB cache.
3. `screenshots/verify_r29_greek_script_render.png`: Sophocles *The Greek Texts* (Ajax / ΑΙΑΣ) rendered in YellowJersey reader with crisp polytonic Greek glyphs (`Ἀεὶ μέν, ὦ παῖ Λαρτίου, δέδορκά σε`) and zero tofu.
4. `screenshots/verify_r29_footnotes_lists_blockquotes.png`: Clean hanging indentation and typography of lists and blockquotes in `kindle_exam_v3.epub`.

---

## 6. Stock App Safety Verification

Stock Kindle package status verified via ADB:
- Package: `com.amazon.kindle`
- Version: `8.160.1.0(2.0.124621.0)`
- Last Update Time: `2026-11-19 21:59:23`
- State: **100% UNTOUCHED and READ-ONLY**.
