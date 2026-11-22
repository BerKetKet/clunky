# Round 035 Final Verification & Engineering Report
**Project Folio: Enhanced Offline EPUB/KFX Reader**  
**Date:** November 22, 2026  
**Target Device:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13)  
**Release Badge:** Folio v3.2 KFX (CSS Text-Transform, Small-Caps & RTL Typesetting)  
**Safety Invariant:** Stock `com.amazon.kindle` (v8.160.1.0) remains **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).

---

## 1. Executive Summary

Round 035 successfully completes all engineering, UI polish, and visual verification objectives specified in `INSTRUCTIONS_ROUND_035.md`:

1. **Task A (Pure-Rust `libkfxconv.so` — `text-transform`, `small-caps` & RTL)**:
   - **CSS `text-transform`**: Parsed and supported `uppercase`, `lowercase`, and `capitalize` in `src/css.rs` and `src/epub/xhtml.rs` with full Unicode awareness. Emits YellowJersey Ion text-transform symbols `$41: $372` (`uppercase`), `$41: $373` (`lowercase`), and `$41: $374` (`titlecase` / `capitalize`).
   - **`font-variant: small-caps`**: Parsed and supported `small-caps` and `all-small-caps`, mapping to YellowJersey font-variant symbol `$14: $369` (`small-caps`).
   - **Right-to-Left (RTL) & Multilingual Text**: Full support for `dir="rtl"` (Arabic, Hebrew, Persian, Urdu, etc.) with NFC normalization, right text alignment (`$34: $61`), direction (`$192: $375` `rtl`), and document-level section direction metadata.
   - **Symbol Index Invariance**: All new interned symbols were appended after existing static symbols in `src/packager/styles.rs`, preserving exact numeric symbol ID stability across legacy test fixtures.
   - **Separation of Raw IR & Storyline Formatting**: `TextSpan.text` in `xhtml.rs` retains original casing for DOM structure consistency, while `extract_style_events` in `storylines.rs` applies the transformed casing and emits style offsets.
   - **Test Suite**: Added 3 unit tests in `src/packager/tests.rs`; all **207 cargo tests** pass (100% green across all targets).
   - **Cross-Compilation**: Compiled stripped ARM64 `libkfxconv.so` (4,635,392 bytes / 4.6 MB) via NDK 26 Clang.

2. **Task B (UI Polish Sweep & Library Hygiene)**:
   - **OPDS Dialog Contrast**: Brightened dim catalog labels (`"Quick OPDS Catalogs:"`, `"Or enter custom OPDS feed URL:"`) in `FolioMoreView.java` from dark charcoal `#333333` to crisp light slate `#CBD5E1`.
   - **Live Standard Ebooks Feed**: Verified live OPDS 1.2 catalog browsing and navigation.
   - **Settings Folio Branding**: Automated patching of `strings.xml` in `tools/rebuild_fast.sh` replacing `"in your Kindle library."` with `"in your Folio library."`, cleanly rebranding `Local file access`.
   - **Library Hygiene**: Pruned temporary synthetic test entries (`Folio v2.9/v3.0/v3.1/v3.2 ... Test`) from Folio's SQLite database (`kindle_library.db`), `LocalContent`, `UserContent`, and device storage `/sdcard/kindle/` / `/sdcard/epub/`, restoring a pristine library view showcasing real classical literature.

3. **Task C (Visual Verification & Milestone Release Staging - Folio v3.2 KFX)**:
   - Captured 4 hardware proof screenshots on Samsung Galaxy M32:
     - `screenshots/verify_r35_text_transform_and_rtl.png`: Rendered KFX page proving `text-transform: uppercase/capitalize`, `small-caps`, and crisp RTL (Arabic/Hebrew) rendering in `KRIFDocViewer`.
     - `screenshots/verify_r35_opds_contrast_and_feed.png`: High-contrast `#CBD5E1` OPDS dialog labels.
     - `screenshots/verify_r35_settings_folio_branding.png`: Clean `"in your Folio library."` under Local file access.
     - `screenshots/verify_r35_clean_library_grid.png`: Clean Folio Library view after pruning temporary synthetic test entries, showcasing real classical books with real covers.
   - Confirmed Stock Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
   - Staged signed APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v3.2_kfx/base.apk` (131 MB) and stripped `libkfxconv.so` (4.6 MB).

4. **Special Directive for Manager AI**:
   - Transmitted the following architectural guidance regarding cover rendering:
     > *"The approach to display the cover as part of the book spine is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the book spine and extract the image for the cover. It is then processed separately, kept in its own class/structure, and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*

---

## 2. Task A: Pure-Rust `libkfxconv.so` Hardening

### CSS `text-transform` Implementation
- **Property Parsing (`src/epub/css_style.rs` & `src/css.rs`)**:
  - Interned `"direction"`.
  - Added support for `text-transform: capitalize`, `text-transform: uppercase`, and `text-transform: lowercase`.
- **Unicode Word Capitalization (`src/epub/xhtml.rs`)**:
  ```rust
  pub fn capitalize_words(s: &str) -> String {
      let mut res = String::with_capacity(s.len());
      let mut capitalize_next = true;
      for c in s.chars() {
          if c.is_alphanumeric() {
              if capitalize_next {
                  for uc in c.to_uppercase() {
                      res.push(uc);
                  }
                  capitalize_next = false;
              } else {
                  for lc in c.to_lowercase() {
                      res.push(lc);
                  }
              }
          } else {
              res.push(c);
              if c.is_whitespace() || c == '-' || c == '—' || c == '/' || c == '(' || c == '[' || c == '"' || c == '\'' {
                  capitalize_next = true;
              }
          }
      }
      res
  }
  ```
- **YellowJersey Ion Symbol Mapping (`src/packager/styles.rs`)**:
  - `$41: $372`: uppercase (`sym_text_transform: sym_uppercase`)
  - `$41: $373`: lowercase (`sym_text_transform: sym_lowercase`)
  - `$41: $374`: titlecase/capitalize (`sym_text_transform: sym_titlecase`)
  - Defined dedicated YellowJersey styles: `style_lowercase` and `style_capitalize`.
- **Storylines Casing Transformation (`src/packager/storylines.rs`)**:
  In `extract_style_events`, when tokens contain `"uppercase"`, `"lowercase"`, or `"capitalize"`, the string is transformed for the storyline payload and YellowJersey style events are attached for the text span offsets.

### Font-Variant Small-Caps
- Supported `font-variant: small-caps` and `font-variant: all-small-caps`.
- Mapped to YellowJersey Ion symbol `$14: $369` (`sym_font_variant: sym_small_caps`).

### Right-to-Left (RTL) Support
- **Detection & Normalization**:
  - Added `contains_rtl_char(s: &str) -> bool` checking Unicode Hebrew (`\u{0590}..\u{05FF}`), Arabic (`\u{0600}..\u{06FF}`, `\u{0750}..\u{077F}`, `\u{08A0}..\u{08FF}`), Syriac, and Thaana ranges.
  - Tracked `is_doc_rtl` on `<html>` and `<body>` tags and inherited `dir="rtl"` to child headings, paragraphs, and spans.
- **RTL YellowJersey Styles**:
  - `$192: $375`: direction rtl (`sym_direction: sym_rtl`)
  - `$34: $61`: text-align right (`sym_text_alignment: sym_right`)
  - Defined `style_rtl_p`, `style_rtl_span`, `style_h1_rtl`, `style_h2_rtl`, `style_h3_rtl`.
- **Document Metadata**:
  - `is_rtl_language` detects RTL language codes (`ar`, `he`, `iw`, `fa`, `ur`, `yi`, `ps`, `ku`, `sd`, `ug`).
  - Emits section metadata `$192: $375` (`direction: rtl`).

### Unit Test Parity
Added 3 comprehensive unit tests in `src/packager/tests.rs`:
- `test_r35_text_transform_cases`: Verifies uppercase, lowercase, and titlecase YellowJersey style maps and text transformations.
- `test_r35_small_caps_font_variant`: Verifies `$14: $369` small-caps emission on spans and paragraphs.
- `test_r35_rtl_direction_and_alignment`: Verifies `$192: $375` (rtl) and `$34: $61` (right alignment) across Hebrew and Arabic paragraphs.
- **Test Results**: All **207 tests** passing cleanly (`cargo test --all-targets`).

---

## 3. Task B: UI Polish Sweep & Library Hygiene

### OPDS Dialog Contrast Enhancement
- File: `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`
- Brightened dialog section labels:
  - `"Quick OPDS Catalogs:"`: `0xFF333333` -> `0xFFCBD5E1` (light slate)
  - `"Or enter custom OPDS feed URL:"`: `0xFF333333` -> `0xFFCBD5E1` (light slate)
- Tested on device: Crisp readability in dark mode.

### Settings Branding Rebrand
- File: `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh`
- Patched `res/values/strings.xml`:
  ```bash
  sed -i 's/in your Kindle library\./in your Folio library\./g' "$WORK_DIR/res/values/strings.xml"
  ```
- Subtitle under `Settings -> Local file access` now reads:
  > *"Allow Folio to view files on your device in your Folio library."*

### Library Hygiene & Pruning
- Sideloaded test entries (`Folio v2.9/v3.0/v3.1/v3.2 ... Test`) were purged from Folio SQLite database (`kindle_library.db`):
  - Purged `KindleContent` table entries matching test titles.
  - Purged dangling records in `LocalContent` and `UserContent`.
  - Cleaned filesystem cache in `/sdcard/kindle/` and `/sdcard/epub/`.
- Restored clean view showcasing Delphi Complete Works of Sophocles, Aristotle, Herodotus, Tacitus, Seneca, and Sallust.

---

## 4. Task C: Visual Verification & Release Staging

### Hardware Proof Screenshots (`screenshots/`)
1. **`screenshots/verify_r35_text_transform_and_rtl.png`**:
   - Device: Samsung Galaxy M32 (`32001776e8e82623`).
   - Content: `KRIFDocViewer` rendering uppercase (`UPPERCASE TITLE`), capitalize (`Capitalized Heading For Ancient Epics`), small-caps (`Small-Caps Heading`), Arabic (`نص عربي تجريبي من اليمين إلى اليسار`), and Hebrew (`טקסט עברי לדוגמה מימין לשמאל`).
2. **`screenshots/verify_r35_opds_contrast_and_feed.png`**:
   - Device: Samsung Galaxy M32.
   - Content: OPDS Catalogs dialog with bright `#CBD5E1` section headers.
3. **`screenshots/verify_r35_settings_folio_branding.png`**:
   - Device: Samsung Galaxy M32.
   - Content: Settings screen showing `"in your Folio library."` under Local file access.
4. **`screenshots/verify_r35_clean_library_grid.png`**:
   - Device: Samsung Galaxy M32.
   - Content: Clean Folio Library view with real Delphi classical titles and zero test clutter.

### Stock Safety Invariant Verification
```
$ adb shell dumpsys package com.amazon.kindle | grep -E "versionName|firstInstallTime|lastUpdateTime"
    versionName=8.160.1.0(2.0.124621.0)
    lastUpdateTime=2026-11-19 21:59:23
    firstInstallTime=2026-11-14 03:25:13
```
Stock `com.amazon.kindle` remains 100% untouched and intact.

### Milestone Release Staging
- Signed APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v3.2_kfx/base.apk` (131 MB)
- Native Library: `07_APK_EPUB_INGESTION_MOD/lib/arm64-v8a/libkfxconv.so` (4.6 MB)
- Badge: **Folio v3.2 KFX**

---

## 5. Summary Table

| Requirement | Implementation Detail | Status |
| :--- | :--- | :--- |
| **CSS `text-transform`** | `uppercase`, `lowercase`, `capitalize` with Unicode casing & YellowJersey symbols `$41: $372/$373/$374` | **PASS** |
| **`font-variant: small-caps`** | Parsed and emitted `$14: $369` (`small-caps`) | **PASS** |
| **RTL Support** | `dir="rtl"`, Hebrew/Arabic detection, right alignment `$34: $61`, direction `$192: $375` | **PASS** |
| **Unit Tests** | 3 new packager tests; 207 cargo tests passing (100% green) | **PASS** |
| **OPDS Dialog Contrast** | Brightened labels to `#CBD5E1` in `FolioMoreView.java` | **PASS** |
| **Settings Branding** | Rebranded `"in your Kindle library."` -> `"in your Folio library."` in `strings.xml` | **PASS** |
| **Library Hygiene** | Sideloaded synthetic test books pruned from DB and storage | **PASS** |
| **Visual QC Proof** | 4 hardware screenshots on Samsung Galaxy M32 in `screenshots/` | **PASS** |
| **Stock Invariant** | Stock `com.amazon.kindle` `lastUpdateTime=2026-11-19 21:59:23` untouched | **PASS** |
| **Release Staging** | Signed APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v3.2_kfx/base.apk` | **PASS** |
| **Cover Architecture** | User directive documented and relayed to Manager AI | **PASS** |
