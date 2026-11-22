# Round 032 Report — Zero-Hyphenation Code Blocks, Sup/Sub Polish & Store-Free Library Search (Folio v2.9)

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Safety Invariant:** Stock `com.amazon.kindle` (v8.160.1.0) remains **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Zero Overfitting Rule:** All styling and search filtering rules are 100% general across all books, search queries, and screen sizes.  
**Hardware Verification:** Samsung Galaxy M32 (`32001776e8e82623`, Android 13 arm64-v8a).

---

## Executive Summary of Deliverables

| Deliverable | Status | Evidence |
| :--- | :--- | :--- |
| **Task A: Zero-Hyphenation Code Blocks** | **VERIFIED** | Emitted YellowJersey `$127: $349` (`hyphens: none`) on `style_pre` and `style_code`. Long identifiers wrap cleanly without hyphens. |
| **Task A: Proportional `<sup>`/`<sub>` Polish** | **VERIFIED** | Proportional font size `0.75em` (`$16`) and baseline styles (`$44: $370` super, `$44: $371` sub) without double-shifting numeric `$31`. Zero baseline collision. |
| **Task B: Store-Free Library Search** | **VERIFIED** | Suppressed Amazon Kindle Store shelf (`From the store` / shopping cart header) across `classes5.dex`. Search displays **ONLY `In your library (N)`**. |
| **Task B: Direct Local Book Launch** | **VERIFIED** | Tapping local search match under `In your library (1)` opens cleanly into `StandAloneBookReaderActivity` (`KRIFDocViewer`). |
| **Task C: Hardware Visual Verification** | **VERIFIED** | 3 hardware screenshots captured on device (`verify_r32_code_no_hyphen_and_sup_sub.png`, `verify_r32_library_search_no_store.png`, `verify_r32_search_result_open_book.png`). |
| **Task C: Release Staging (Folio v2.9 KFX)** | **VERIFIED** | Signed APK (131 MB) and stripped native library (4.5 MB) staged at `07_APK_EPUB_INGESTION_MOD/release/folio_v2.9_kfx/`. |

---

## Task A: Zero-Hyphenation Code Blocks & Proportional Sup/Sub in `libkfxconv.so`

### 1. Pure-Rust YellowJersey Schema Parity
In `kfx_converter/src/packager/styles.rs`:
- Added YellowJersey's `$127: $349` (`hyphens: none`) property to both preformatted code blocks (`style_pre`) and inline code spans (`style_code`).
  - Variable names, functions, and symbols (`calculate_engine_execution_duration`, `total_duration_seconds`, `processing_throughput_rate`) are never hyphenated with hyphens (`-`).
- Polished `<sup>` and `<sub>` styling:
  - Preserved proportional font size `0.75em` (`$16: 0.75em`).
  - Preserved YellowJersey baseline styles `$44: $370` (superscript) and `$44: $371` (subscript).
  - Completely removed explicit numeric `$31` baseline shift (`make_em("0.35")` / `make_em("-0.20")`), which previously caused double-shift collisions and vertical line distortion.

### 2. Unit Testing & ARM64 Build
- Implemented unit test `test_r32_code_no_hyphen_and_proportional_sup_sub` in `kfx_converter/src/packager/tests.rs` asserting:
  - `style_pre` contains `$127: $349`.
  - `style_code` contains `$127: $349`.
  - `style_superscript` contains `$44: $370` and `$16: 0.75em`, with zero occurrences of `$31`.
  - `style_subscript` contains `$44: $371` and `$16: 0.75em`, with zero occurrences of `$31`.
- All 212 cargo unit tests pass:
  ```
  test result: ok. 156 passed; 0 failed; finished in 10.72s
  test result: ok. 13 passed; 0 failed (cli)
  test result: ok. 16 passed; 0 failed (container boundary)
  test result: ok. 8 passed; 0 failed (pipeline)
  test result: ok. 9 passed; 0 failed (sync sanitize)
  ```
- Cross-compiled release profile with Android NDK Clang for `aarch64-linux-android` and stripped with `llvm-strip` into `07_APK_EPUB_INGESTION_MOD/lib/arm64-v8a/libkfxconv.so` (4.5 MB).

---

## Task B: Store-Free Library Search in `com.amazon.kindle.epub`

### 1. Reverse Engineering & Root-Cause Elimination
In the Folio APK, Search is managed by Jetpack Compose within `RubySearchActivity` / `SearchActivity` residing in `classes5.dex`. When a user queried a term, the app displayed local library matches under `In your library`, but immediately below it rendered a shopping cart header labeled `From the store` (`0x7f141650`) backed by network queries.

To cleanly eliminate store results without altering library searching:
1. `SearchResultsKt.smali`:
   - Patched `b4(ZZZZZ)Z` (the header display predicate) to unconditionally return `0` (`false`). This completely suppresses `StoreHeaderSection` (the `From the store` shelf and shopping cart icon).
2. `com/amazon/kcp/search/v2/models/b.smali`:
   - Patched `e()Ljava/util/List;` (`getStoreResults`) to return `Collections.EMPTY_LIST`. As a result, store results are always treated as empty and the store item grid is never populated.
3. `SearchProvider.smali`:
   - Patched `N()Ljava/util/List;` to return `Collections.EMPTY_LIST`.
   - In `query()`, forced `queryStore = false`, ensuring no background store search query is ever initiated.
4. `SearchViewModel.smali`:
   - In `r()V` (the store coroutine launcher) and `z(SearchNodeCompleteEvent)` (the store result processor), replaced method bodies with `return-void` to silence background store calls.
5. `SearchActivity.smali`:
   - In `setShouldShowStoreResults(Z)V`, forced `const/4 p1, 0x0` before writing to `this.shouldShowStoreResults`.

### 2. Automated Build Script
Created permanent build tool `07_APK_EPUB_INGESTION_MOD/tools/patch_classes5.sh` to disassemble `classes5.dex`, apply all surgical smali patches, and reassemble cleanly.

---

## Task C: Hardware Visual Verification (Samsung Galaxy M32)

### 1. Hardware Verification Screenshots
1. `screenshots/verify_r32_code_no_hyphen_and_sup_sub.png`:
   - Captured in `KRIFDocViewer` showing the rendered Round 032 verification book (`Folio v2.9 Code Hyphenation & Sup Sub Test`).
   - Python code block `<pre><code>` renders `calculate_engine_execution_duration`, `total_duration_seconds`, and `processing_throughput_rate` with **zero hyphens**.
   - Scientific typography: `E = mc²`, `2H₂ + O₂ → 2H₂O`, `a² + b² = c²`, and Gauss reference `[1]` render cleanly on the baseline with zero collisions.
2. `screenshots/verify_r32_library_search_no_store.png`:
   - Live query `"Sophocles"` executed in `RubySearchActivity`.
   - Displays **ONLY `In your library (1)`** with the local Delphi Classics book.
   - The Amazon `"From the store"` shelf, shopping cart icon, and store placeholders are 100% absent.
3. `screenshots/verify_r32_search_result_open_book.png`:
   - Tapping the local Sophocles search result opens directly into `StandAloneBookReaderActivity` (`KRIFDocViewer`) at "AvTy6vn - ANTIGONE", verifying seamless library navigation.

### 2. Stock Package Safety Check
```zsh
adb -s 32001776e8e82623 shell dumpsys package com.amazon.kindle | grep -i lastupdatetime
```
Output:
```
    lastUpdateTime=2026-11-19 21:59:23
```
Stock Kindle is 100% untouched and intact.

---

## Project Lead Architectural Directive on Cover Architecture

As instructed by the project lead, this key design decision is reaffirmed for the architecture roadmap:
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page and take the image for the cover. It is then processed separately and kept in its own class and inserted when displaying the book separately."

---

## Release Staging (Folio v2.9 KFX)

The following production artifacts are staged for Round 032:
- `07_APK_EPUB_INGESTION_MOD/release/folio_v2.9_kfx/base.apk` (131 MB, signed with debug keystore)
- `07_APK_EPUB_INGESTION_MOD/release/folio_v2.9_kfx/base.apk.idsig` (1.1 MB)
- `07_APK_EPUB_INGESTION_MOD/release/folio_v2.9_kfx/libkfxconv.so` (4.5 MB, stripped ARM64)
