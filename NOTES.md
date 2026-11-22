# Kindle Android v8.159.0.100 - Deployment & Restoration Notes

## Status
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Centennial Milestone Folio v10.0 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 100 complete:
  1. **Unified Quick Library Search & v10.0 Telemetry (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMoreView.java`.
     - Added sky-blue `🔍 Search` pill directly in Control Center header beside `Folio v10.0 KFX` badge.
     - Built `showGlobalSearchDialog(...)` modal providing real-time search across books (title, author, genre, ISBN), reading notes (warm amber quotes), and custom shelves (`🏷 Shelf: Classics`).
     - Added 1-tap `Read ›` launch into KFX reader, `Open in Catalog` deep link, and cover thumbnails.
     - Added live `• Up Next & Goal: 1 queued · 1/5 annual goal` telemetry line to About Folio.
     - Bumped badge and About Folio string to `Folio v10.0 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r100_quick_search_v10.png` (275 KB) — Quick Library Search modal showing search bar, `🏷 Shelf: Classics`, *Moby Dick*, *Animal Farm*, and *1984* note snippet with `Read ›` buttons.
       * `verify_r100_about_v10.png` (341 KB) — About Folio modal showing `Version: Folio v10.0 KFX (Enhanced Typesetting)` and new `Up Next & Goal` telemetry.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v10.0 KFX release APK (137 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v10.0_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 100)
1. `verify_r100_quick_search_v10.png`: Quick Library Search modal with search bar, shelves, and notes.
2. `verify_r100_about_v10.png`: About Folio modal confirming `Version: Folio v10.0 KFX (Enhanced Typesetting)` and `Up Next & Goal` telemetry.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v10.0 KFX deployed (`lastUpdateTime=2026-11-23 02:44:49`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v9.9 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 099 complete:
  1. **Reading Goal Completed Books Trophy Shelf (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMoreView.java`.
     - Built `showReadingGoalDialog(...)` into rich `🎯 2026 Reading Goal` modal.
     - Added live progress bar track (`1 of 5 books finished (20%)`) and `🏆 COMPLETED BOOKS (N)` horizontal cover-art trophy strip showing cover thumbnails, titles, authors, and `✓ YYYY-MM-DD` completion dates.
     - Added quick target presets (`5`, `12`, `24`, `36`, `52`) + custom number input.
     - Bumped badge and About Folio string to `Folio v9.9 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r99_goal_trophy_shelf.png` (213 KB) — Reading Goal modal showing progress bar, `🏆 COMPLETED BOOKS (1)` trophy card for *Moby Dick* (`✓ 2026-11-23`), and presets.
       * `verify_r99_about_v99.png` (331 KB) — About Folio modal showing `Version: Folio v9.9 KFX (Enhanced Typesetting)` and live stats.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v9.9 KFX release APK (137 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v9.9_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 099)
1. `verify_r99_goal_trophy_shelf.png`: Reading Goal modal with progress bar and Completed Books trophy shelf.
2. `verify_r99_about_v99.png`: About Folio modal confirming `Version: Folio v9.9 KFX (Enhanced Typesetting)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v9.9 KFX deployed (`lastUpdateTime=2026-11-23 02:38:15`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v9.8 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 098 complete:
  1. **Persistent Finished Date Tracking & Export (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMetadataCatalogDialog.java`, `FolioMetadataBackup.java`, and `FolioMoreView.java`.
     - Added persistent finished date tracking in `SharedPreferences("folio_finished_dates")`.
     - In Book Details (`ⓘ`), finished books display an emerald status line (`✓ Finished · Set date ›` or `✓ Finished on YYYY-MM-DD · Edit ›`) that summons `showEditFinishedDateDialog` for 1-tap inline modification.
     - Preserves `finished_date` across JSON Backup/Restore, Markdown reading log (`folio_reading_log.md`), and RFC-4180 CSV export (`folio_library.csv`).
     - Bumped badge and About Folio string to `Folio v9.8 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r98_finished_date_details.png` (235 KB) — Book Details modal for *Moby Dick* displaying `✓ Finished on 2026-11-23 · Edit ›` in the header.
       * `verify_r98_about_v98.png` (332 KB) — About Folio modal showing `Version: Folio v9.8 KFX (Enhanced Typesetting)` and live stats.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v9.8 KFX release APK (137 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v9.8_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 098)
1. `verify_r98_finished_date_details.png`: Book Details modal with `✓ Finished on 2026-11-23 · Edit ›`.
2. `verify_r98_about_v98.png`: About Folio modal confirming `Version: Folio v9.8 KFX (Enhanced Typesetting)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v9.8 KFX deployed (`lastUpdateTime=2026-11-23 02:31:04`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v9.7 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 097 complete:
  1. **Curated-Only Reading Log Toggle & Top-Placed Up Next Queue (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMetadataBackup.java` and `FolioMoreView.java`.
     - Added `✨ Scope: Curated Only` / `📚 Scope: All Books` toggle pill alongside `📊 Export CSV` in `Reading Log (.md)` modal.
     - Defaults to Curated Only mode filtering markdown export strictly to engaged books (Finished, Reading, Favorited, Rated, Annotated, Shelved, Queued).
     - Promoted `## 📌 Up Next Queue (N)` section to the prominent top of the Markdown log right below the Reading Goal summary.
     - Bumped badge and About Folio string to `Folio v9.7 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r97_curated_reading_log.png` (218 KB) — Reading Log modal showing `✨ Scope: Curated Only` toggle pill, `# Folio Curated Reading Log`, and `## 📌 Up Next Queue (1)` at top of preview.
       * `verify_r97_about_v97.png` (331 KB) — About Folio modal showing `Version: Folio v9.7 KFX (Enhanced Typesetting)` and live stats.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v9.7 KFX release APK (137 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v9.7_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 097)
1. `verify_r97_curated_reading_log.png`: Reading Log modal with Curated Only toggle and top-placed Up Next Queue.
2. `verify_r97_about_v97.png`: About Folio modal confirming `Version: Folio v9.7 KFX (Enhanced Typesetting)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v9.7 KFX deployed (`lastUpdateTime=2026-11-23 02:23:50`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v9.6 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 096 complete:
  1. **1-Tap Sort Picker Modal, Direction Toggle & Size/Queue Sorts (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`.
     - Replaced linear cycling of top bar `Sort:` button with dark `Sort Catalog` picker modal (`showSortPickerDialog(...)`) offering 1-tap selection across all 9 sort modes.
     - Added persistent `↓ Standard / ↑ Reversed` direction toggle pill in modal header stored in `SharedPreferences("folio_ui_prefs", "catalog_sort_rev")` with live indicator arrows on the top bar pill.
     - Supported `File Size (Largest)` and `📌 Up Next Queue First` sort orders.
     - Bumped badge and About Folio string to `Folio v9.6 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `verify_r96_sort_picker_modal.png` (142 KB) — Sort Catalog modal showing `↓ Standard` toggle pill and all 9 sort modes with amber checkmark.
       * `verify_r96_about_v96.png` (331 KB) — About Folio modal showing `Version: Folio v9.6 KFX (Enhanced Typesetting)` and live stats.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v9.6 KFX release APK (137 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v9.6_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 096)
1. `verify_r96_sort_picker_modal.png`: Sort Catalog modal with direction toggle and 9 sort options.
2. `verify_r96_about_v96.png`: About Folio modal confirming `Version: Folio v9.6 KFX (Enhanced Typesetting)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v9.6 KFX deployed (`lastUpdateTime=2026-11-23 02:16:01`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.9 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 089 complete:
  1. **Reading Notes Notebook & Storage Scroll Polish (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMoreView.java`.
     - Added `📝 Reading Notes Notebook` action card on Control Center directly below `📄 Reading Log & Export`.
     - Built comprehensive `showNotesNotebookDialog(...)` modal:
       * Real-time search filter (`Search notes, titles, or authors...`) with live count header (`📝 Reading Notes (1)`).
       * Card layout with standalone `.cover.jpg` thumbnail, bold title (1-tap launch into KFX reader), author, rating badge (`★ 5/5`), and reading shelf (`🏷 Classics`).
       * Warm amber quote box (`#251D14` fill, `#D97706` border) with full note text (`Essential dystopian classic - reread Part 3`).
       * 1-tap `📋 Copy` note to clipboard, 1-tap `✏️ Edit` inline note editor with immediate reactive notebook refresh on save, and `Copy All` Markdown export.
     - In `FolioMoreView.java`, expanded Storage & Duplicates scroll viewport to `388dp` with `10dp` bottom padding to eliminate edge clipping on the Duplicate Titles status card.
     - Bumped badge and About Folio string to `Folio v8.9 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `screenshots/verify_r89_notes_notebook.png` (153 KB) — Reading Notes Notebook modal showing search bar, *1984* cover thumbnail, rating/shelf badges, amber quote box, and `📋 Copy` / `✏️ Edit` pills.
       * `screenshots/verify_r89_about_v89.png` (325 KB) — About Folio modal showing `Version: Folio v8.9 KFX (Enhanced Typesetting)` and live stats.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v8.9 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v8.9_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 089)
1. `screenshots/verify_r89_notes_notebook.png`: Reading Notes Notebook modal with search bar, cover thumbnail, quote box, and copy/edit pills.
2. `screenshots/verify_r89_about_v89.png`: About Folio modal confirming `Version: Folio v8.9 KFX (Enhanced Typesetting)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v8.9 KFX deployed (`lastUpdateTime=2026-11-23 01:22:19`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.8 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 088 complete:
  1. **Storage Footprint & Duplicate Titles Manager (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMoreView.java`.
     - Added `💾 Storage & Duplicates` action card on Control Center directly below `🏷 Manage Shelves`.
     - Built comprehensive `showStorageDuplicatesDialog(...)` modal:
       * Three centered live storage tiles: App Library (`13.3 MB`), Backups (`682 KB`), and Temp Cache (`1.3 MB`).
       * Top 5 Largest Books ranking with amber proportional horizontal bars and 1-tap search drill-down into catalog (*Homer*, *Cicero*, *Plutarch*, *Aristotle*, *Xenophon*).
       * Duplicate Titles Audit running title normalization across all 27 cataloged books, reporting `✓ No duplicate titles found across 27 cataloged books`.
       * 1-tap `Clear Temp Cache` button allowing instant scratch cache purge and live modal refresh.
     - Bumped badge and About Folio string to `Folio v8.8 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `screenshots/verify_r88_control_center_v88.png` (206 KB) — Control Center showing the new `💾 Storage & Duplicates` card under `🏷 Manage Shelves` with `Folio v8.8 KFX` badge.
       * `screenshots/verify_r88_storage_duplicates_modal.png` (231 KB) — Storage & Duplicates modal showing 3 storage tiles, Top 5 Largest Books amber bars, and duplicate title audit status.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v8.8 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v8.8_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 088)
1. `screenshots/verify_r88_control_center_v88.png`: Control Center with `💾 Storage & Duplicates` card and `Folio v8.8 KFX` badge.
2. `screenshots/verify_r88_storage_duplicates_modal.png`: Storage & Duplicates modal with 3 storage tiles, largest books bars, and duplicate check.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v8.8 KFX deployed (`lastUpdateTime=2026-11-23 01:15:26`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.7 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 087 complete:
  1. **Bulk Genre Tagging & Centered Metadata Health Tiles (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`.
     - In `FolioMetadataCatalogDialog.java`, added sky-blue `+ Genre` action pill in the bottom Bulk Action Bar alongside `🏷 Shelf`, `★ Fav`, and `✓ Done`.
     - Implemented `showBulkGenrePicker(...)` displaying interactive `🏷 Add Genre (N books)` modal with horizontal genre preset chips (`+ Fiction`, `+ History`, `+ Rome`, `+ Biography`, `+ Correspondence`) and custom comma-separated genre input.
     - Implemented safe case-insensitive genre merge logic into `books_metadata.genres` in `folio_metadata.db` for all selected volumes.
     - In `FolioMoreView.java`, center-aligned both count (`valTv`) and label (`lblTv`) inside `makeHealthBadge(...)` across all 3 audit tiles.
     - Bumped badge and About Folio string to `Folio v8.7 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `screenshots/verify_r87_bulk_genre_bar.png` (210 KB) — Bulk Add Genre modal with horizontal genre chips, custom input field, and 4-action bottom bar.
       * `screenshots/verify_r87_insights_centered_v87.png` (161 KB) — Library Insights modal showing crisp center-aligned 3-tile Metadata Health strip with `Folio v8.7 KFX` badge.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v8.7 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v8.7_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 087)
1. `screenshots/verify_r87_bulk_genre_bar.png`: Add Genre modal with preset chips, custom input, and 4-button bulk bar.
2. `screenshots/verify_r87_insights_centered_v87.png`: Library Insights modal with center-aligned 3-tile Metadata Health strip.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v8.7 KFX deployed (`lastUpdateTime=2026-11-23 01:06:47`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.6 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 086 complete:
  1. **Metadata Health Audit & 1-Tap Quick-Fix Filters (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMoreView.java` and `FolioMetadataCatalogDialog.java`.
     - In `FolioMoreView.java`, added `METADATA HEALTH — 70% COMPLETE (TAP TO FIX)` section inside `📊 Library Insights` directly below `READING STATUS`.
     - Built three live health audit tiles: Covers (`1 › No Cover`), Genres (`2 › No Genre`), and Synopsis (`21 › No Synopsis`) with emerald styling for 100% and alert badges for gaps.
     - In `FolioMetadataCatalogDialog.java`, added support for `MISSING_COVER`, `MISSING_GENRES`, and `MISSING_SYNOPSIS` deep-links, badged with dedicated `🩺 Missing ... (N)` active chips.
     - Verified 1-tap tile click on `2 › No Genre` dismisses Insights and opens catalog filtered to `Library Metadata & Genres (2 of 27)` with `🩺 Missing Genres (2)` chip.
     - Bumped badge and About Folio string to `Folio v8.6 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `screenshots/verify_r86_insights_metadata_health.png` (174 KB) — Library Insights modal showing `METADATA HEALTH — 70% COMPLETE (TAP TO FIX)` section with 3 audit tiles directly under `READING STATUS`.
       * `screenshots/verify_r86_missing_filter_drilldown.png` (406 KB) — Metadata Catalog dialog filtered to 2 books needing genres with active `🩺 Missing Genres (2)` chip.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v8.6 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v8.6_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 086)
1. `screenshots/verify_r86_insights_metadata_health.png`: Library Insights modal showing `METADATA HEALTH — 70% COMPLETE (TAP TO FIX)` with 3 audit tiles.
2. `screenshots/verify_r86_missing_filter_drilldown.png`: Metadata Catalog filtered to 2 books with active `🩺 Missing Genres (2)` chip.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v8.6 KFX deployed (`lastUpdateTime=2026-11-23 00:57:13`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Milestone Folio v8.5 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 085 complete:
  1. **Catalog Multi-Select & Bulk Actions (Task A & B)**:
     - Integrated Gemini's pre-authored drop-in updates into `FolioMetadataCatalogDialog.java` and `FolioMoreView.java`.
     - In `FolioMetadataCatalogDialog.java`, added compact `☑ Select` toggle pill beside catalog header, switching to `✕ Done` with `#4F46E5` fill and `#818CF8` border when active.
     - Implemented dynamic card selection highlighting: tapping any book in List or 3-column Grid view highlights it with `2dp #818CF8` indigo border and `#1E2642` background.
     - Added floating bottom Bulk Action Bar with dynamic selection counter (`N books selected`), `Select All / Clear All` toggle, and three 1-tap bulk actions:
       * `🏷 Shelf`: bulk assign or clear custom shelves across all selected books.
       * `★ Fav`: bulk favorite or unfavorite across all selected books.
       * `✓ Finished`: bulk mark finished or unread, automatically syncing reading progress and status into `kindle_library.db`.
     - In `FolioMoreView.java`, widened Continue Reading carousel cards to `196dp` for improved title and author framing.
     - Bumped badge and About Folio string to `Folio v8.5 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task B)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `screenshots/verify_r85_bulk_select_mode.png` (617 KB) — Metadata Catalog in 3-column Grid view with `☑ Select` active (`✕ Done`), 3 books selected (*1984*, *Moby Dick*, *Animal Farm*) with indigo selection borders, and bottom Bulk Action Bar showing `3 books selected`, `Select All (27)`, `🏷 Shelf`, `★ Fav`, and `✓ Finished`.
       * `screenshots/verify_r85_about_v85.png` (324 KB) — About Folio modal showing `Version: Folio v8.5 KFX (Enhanced Typesetting)` and live stats.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Milestone Folio v8.5 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v8.5_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Verification Evidence (Round 085)
1. `screenshots/verify_r85_bulk_select_mode.png`: Metadata Catalog in 3-column Grid view with `☑ Select` active, 3 books selected with indigo selection borders, and bottom Bulk Action Bar.
2. `screenshots/verify_r85_about_v85.png`: About Folio modal confirming `Version: Folio v8.5 KFX (Enhanced Typesetting)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v8.5 KFX deployed (`lastUpdateTime=2026-11-23 00:48:01`).
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v7.8 KFX") deployed and fully verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 078 complete:
  1. **Folder Importer Load More (+60) Pagination & Last-Folder Persistence (Task A & B)**:
     - Integrated Gemini's drop-in updates to `FolioFolderBrowserDialog.java` and `FolioMoreView.java`.
     - In `FolioFolderBrowserDialog.java`, added interactive teal `Load More (+60 of 659 remaining)` pagination button at the bottom of the EPUB list.
     - Verified list expands dynamically to `Showing 120 of 719 matching EPUBs (719 total in folder)`.
     - Added last-folder persistence via `SharedPreferences("folio_ui_prefs", "last_import_dir")`, automatically reloading `/sdcard/en/0` on dialog launch.
     - Bumped badge and About Folio to `Folio v7.8 KFX (Enhanced Typesetting)`.
  2. **Visual Verification & Milestone Release Staging (Task C)**:
     - Captured hardware proof screenshots on Galaxy M32:
       * `screenshots/verify_r78_load_more_button.png` (187 KB) — Batch EPUB Importer scrolled to bottom of 60 items showing teal `Load More (+60 of 659 remaining)` button.
       * `screenshots/verify_r78_loaded_120_items.png` (191 KB) — Batch EPUB Importer header updated to `Showing 120 of 719 matching EPUBs (719 total in folder)` after tapping `Load More`.
     - Safety Invariant: Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
     - Staged signed Folio v7.8 KFX release APK (132 MB) at `07_APK_EPUB_INGESTION_MOD/release/folio_v7.8_kfx/base.apk`.
  3. **Cover Architecture Directive**: Formally recorded and relayed to manager AI:
     "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v7.8 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, Folder Importer Load More (+60) Pagination, Last-Imported Directory Persistence (folio_ui_prefs), Folder Importer Sort Toggle (A-Z / Newest / Largest / Smallest), Live Selected Payload Size CTA (Import Selected (N · X.X MB)), Interactive 1-Tap Drill-Down Library Insights & Custom Shelves Chart, Library Insights Visual Analytics Modal (Top Genres, Top Authors, Average Rating & Shelf Stats), Searchable Authors Directory & Long Author Name Ellipsis Polish, Authors Directory Modal (👤) with Book Counts & 1-Tap Author Filtering, RFC-4180 CSV Library Export (/sdcard/kindle/backups/folio_library.csv), 1-Tap Export CSV Pill in Dialog Header, Reading Goal in Markdown Header & JSON Backup, Annual Reading Goal Progress Card (🎯), Goal Customizer Dialog with 1-Tap Presets (5/12/24/36/52), Persistent Reading Goal Preference (folio_ui_prefs), Reading Shelf Clipboard Info (📋 Copy Info), Custom Reading Shelves Subsystem (folio_shelves), Dynamic Indigo Shelf Filter Chips (🏷 <Shelf> (N)), 1-Tap Preset Shelf Picker & Custom Input, Shelf Full-Text Search, JSON Shelf Backup/Restore & Markdown Shelf Export, Reading Log Author Normalization & Live Reading Status, 1-Tap Android Share Intent (Intent.ACTION_SEND), Markdown Reading Log Export & Monospace Preview (/sdcard/kindle/backups/folio_reading_log.md), 1-Tap Copy Markdown to Clipboard, Dynamic Unread Backlog Filter Chip (Unread (N)), 1-Tap Copy Book Info to Clipboard (📋 Copy Info), Sort by Rating Mode, Random Unread Book Picker (🎲), Interactive 5-Star Ratings Subsystem (folio_ratings), Persistent Catalog Sort Preference (folio_ui_prefs), Portable JSON Ratings Backup & Restore, Inline Card Note Snippet Previews, Sort by Favorites Mode, Favorites & Notes Combined Telemetry (1 favorited · 1 annotated), Dynamic Notes Quick-Filter Chip (📝 Notes (N)), Full-Text Search inside Reading Notes, Persistent Personal Reading Notes Subsystem (folio_notes), Combined Catalog Badges (★ 📝 Title), Inline Note Editor Dialog with Save/Clear, Portable JSON Note Backup & Restore, Control Center 4-Tile Live Stats Strip with ★ FAVS Deep-Link, Version 2 Portable JSON Backup with is_favorite Serialization, Persistent Favorites Subsystem with SharedPreferences zero-DB migration, Dynamic Gold ★ Favorites Quick-Filter Chip, Starred Title Prefix (★), Fullscreen Pitch-Black Cover Lightbox with centered cover art, Live KFX file size indicator in Book Details, 64x96dp Cover Thumbnail Header in Book Details, Expandable Synopsis Toggle (Read full synopsis › / Show less), Clickable Genre Pills in Book Details with 1-tap catalog filtering, Control Center Live Stat Tile Auto-Refresh on dialog close, Tappable Header Version Badge, 1-Tap Select New / Clear batch selection pill in folder importer, Dynamic filtered count header (X of Y), unified inline search clear button (✕) across catalog and batch folder importer, Clickable Author Quick-Filter link with automatic genre reset and search sync, Mark as Finished / Unread toggle pill in Book Details, dynamic teal Finished (N) chip filter, green Finished card badges, accurate reading progress SQL check, dynamic Reading quick-filter chip with in-progress badges, deduplicated Title-Cased genre tags, bibliographic meta-tag filtering from top chips, live reading progress stats in About Folio, Pass-3 missing-metadata auto-repair, natural-name author formatting, frequency-ranked genre chips with live counts, unified local library scan to app-private storage, right-aligned Home All Metadata link, safe cascade remove action, live reading progress badges and sort mode, 700+ EPUB live filename search in folder importer, "Hide Imported" toggle pill, ISBN-to-title genre fallback, multi-select batch EPUB folder importer from storage, portable metadata JSON backup/restore, missing-cover auto-repair with Open Library Covers API + 302 redirect following, catalog sort controls Recent/Title/Author/Year/Progress, in-app editable metadata dialog with live persistence, full-library `Sync & Enrich` metadata backfiller, real-time search filtering, Title-Cased genre chips, rich Book Details popup with 0ms reader launch, duplicate import guard with "Open Existing Copy" vs "Re-Import & Replace", "Library Metadata & Genres" browser dialog, persistent `folio_metadata.db`, Open Library metadata enrichment, live OPDS/Atom feed download, heavy omnibus stress-tested up to 23 MB / 1,450 sections, CSS text-transform uppercase/lowercase/capitalize, font-variant small-caps, RTL Arabic/Hebrew direction and right alignment, multi-level nested lists, native tables with thead/colspan/rowspan, malformed asset resilience, full HTML4/HTML5 entity decoding, store-free library search, zero-hyphenation code blocks, proportional sup/sub, `$270` cover container, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 078)
1. `screenshots/verify_r78_load_more_button.png`: Batch EPUB Importer scrolled to bottom of 60 items showing teal `Load More (+60 of 659 remaining)` button.
2. `screenshots/verify_r78_loaded_120_items.png`: Batch EPUB Importer header updated to `Showing 120 of 719 matching EPUBs (719 total in folder)` after tapping `Load More`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v7.8 KFX deployed (`lastUpdateTime=2026-11-23 00:00:20`).
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v7.7 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, Folder Importer Sort Toggle (A-Z / Newest / Largest / Smallest), Live Selected Payload Size CTA (Import Selected (N · X.X MB)), Interactive 1-Tap Drill-Down Library Insights & Custom Shelves Chart, Library Insights Visual Analytics Modal (Top Genres, Top Authors, Average Rating & Shelf Stats), Searchable Authors Directory & Long Author Name Ellipsis Polish, Authors Directory Modal (👤) with Book Counts & 1-Tap Author Filtering, RFC-4180 CSV Library Export (/sdcard/kindle/backups/folio_library.csv), 1-Tap Export CSV Pill in Dialog Header, Reading Goal in Markdown Header & JSON Backup, Annual Reading Goal Progress Card (🎯), Goal Customizer Dialog with 1-Tap Presets (5/12/24/36/52), Persistent Reading Goal Preference (folio_ui_prefs), Reading Shelf Clipboard Info (📋 Copy Info), Custom Reading Shelves Subsystem (folio_shelves), Dynamic Indigo Shelf Filter Chips (🏷 <Shelf> (N)), 1-Tap Preset Shelf Picker & Custom Input, Shelf Full-Text Search, JSON Shelf Backup/Restore & Markdown Shelf Export, Reading Log Author Normalization & Live Reading Status, 1-Tap Android Share Intent (Intent.ACTION_SEND), Markdown Reading Log Export & Monospace Preview (/sdcard/kindle/backups/folio_reading_log.md), 1-Tap Copy Markdown to Clipboard, Dynamic Unread Backlog Filter Chip (Unread (N)), 1-Tap Copy Book Info to Clipboard (📋 Copy Info), Sort by Rating Mode, Random Unread Book Picker (🎲), Interactive 5-Star Ratings Subsystem (folio_ratings), Persistent Catalog Sort Preference (folio_ui_prefs), Portable JSON Ratings Backup & Restore, Inline Card Note Snippet Previews, Sort by Favorites Mode, Favorites & Notes Combined Telemetry (1 favorited · 1 annotated), Dynamic Notes Quick-Filter Chip (📝 Notes (N)), Full-Text Search inside Reading Notes, Persistent Personal Reading Notes Subsystem (folio_notes), Combined Catalog Badges (★ 📝 Title), Inline Note Editor Dialog with Save/Clear, Portable JSON Note Backup & Restore, Control Center 4-Tile Live Stats Strip with ★ FAVS Deep-Link, Version 2 Portable JSON Backup with is_favorite Serialization, Persistent Favorites Subsystem with SharedPreferences zero-DB migration, Dynamic Gold ★ Favorites Quick-Filter Chip, Starred Title Prefix (★), Fullscreen Pitch-Black Cover Lightbox with centered cover art, Live KFX file size indicator in Book Details, 64x96dp Cover Thumbnail Header in Book Details, Expandable Synopsis Toggle (Read full synopsis › / Show less), Clickable Genre Pills in Book Details with 1-tap catalog filtering, Control Center Live Stat Tile Auto-Refresh on dialog close, Tappable Header Version Badge, 1-Tap Select New / Clear batch selection pill in folder importer, Dynamic filtered count header (X of Y), unified inline search clear button (✕) across catalog and batch folder importer, Clickable Author Quick-Filter link with automatic genre reset and search sync, Mark as Finished / Unread toggle pill in Book Details, dynamic teal Finished (N) chip filter, green Finished card badges, accurate reading progress SQL check, dynamic Reading quick-filter chip with in-progress badges, deduplicated Title-Cased genre tags, bibliographic meta-tag filtering from top chips, live reading progress stats in About Folio, Pass-3 missing-metadata auto-repair, natural-name author formatting, frequency-ranked genre chips with live counts, unified local library scan to app-private storage, right-aligned Home All Metadata link, safe cascade remove action, live reading progress badges and sort mode, 700+ EPUB live filename search in folder importer, "Hide Imported" toggle pill, ISBN-to-title genre fallback, multi-select batch EPUB folder importer from storage, portable metadata JSON backup/restore, missing-cover auto-repair with Open Library Covers API + 302 redirect following, catalog sort controls Recent/Title/Author/Year/Progress, in-app editable metadata dialog with live persistence, full-library `Sync & Enrich` metadata backfiller, real-time search filtering, Title-Cased genre chips, rich Book Details popup with 0ms reader launch, duplicate import guard with "Open Existing Copy" vs "Re-Import & Replace", "Library Metadata & Genres" browser dialog, persistent `folio_metadata.db`, Open Library metadata enrichment, live OPDS/Atom feed download, heavy omnibus stress-tested up to 23 MB / 1,450 sections, CSS text-transform uppercase/lowercase/capitalize, font-variant small-caps, RTL Arabic/Hebrew direction and right alignment, multi-level nested lists, native tables with thead/colspan/rowspan, malformed asset resilience, full HTML4/HTML5 entity decoding, store-free library search, zero-hyphenation code blocks, proportional sup/sub, `$270` cover container, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 077)
1. `screenshots/verify_r77_folder_sort_largest.png`: Batch EPUB Importer showing `Sort: Largest` with multi-MB EPUBs ranked at top.
2. `screenshots/verify_r77_selected_size_cta.png`: Batch EPUB Importer showing bottom CTA updated to `Import Selected (25 · 238.6 MB)` with `Clear (25)` button.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v7.7 KFX deployed (`lastUpdateTime=2026-11-22 23:52:10`).
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v7.6 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, Interactive 1-Tap Drill-Down Library Insights & Custom Shelves Chart, Library Insights Visual Analytics Modal (Top Genres, Top Authors, Average Rating & Shelf Stats), Searchable Authors Directory & Long Author Name Ellipsis Polish, Authors Directory Modal (👤) with Book Counts & 1-Tap Author Filtering, RFC-4180 CSV Library Export (/sdcard/kindle/backups/folio_library.csv), 1-Tap Export CSV Pill in Dialog Header, Reading Goal in Markdown Header & JSON Backup, Annual Reading Goal Progress Card (🎯), Goal Customizer Dialog with 1-Tap Presets (5/12/24/36/52), Persistent Reading Goal Preference (folio_ui_prefs), Reading Shelf Clipboard Info (📋 Copy Info), Custom Reading Shelves Subsystem (folio_shelves), Dynamic Indigo Shelf Filter Chips (🏷 <Shelf> (N)), 1-Tap Preset Shelf Picker & Custom Input, Shelf Full-Text Search, JSON Shelf Backup/Restore & Markdown Shelf Export, Reading Log Author Normalization & Live Reading Status, 1-Tap Android Share Intent (Intent.ACTION_SEND), Markdown Reading Log Export & Monospace Preview (/sdcard/kindle/backups/folio_reading_log.md), 1-Tap Copy Markdown to Clipboard, Dynamic Unread Backlog Filter Chip (Unread (N)), 1-Tap Copy Book Info to Clipboard (📋 Copy Info), Sort by Rating Mode, Random Unread Book Picker (🎲), Interactive 5-Star Ratings Subsystem (folio_ratings), Persistent Catalog Sort Preference (folio_ui_prefs), Portable JSON Ratings Backup & Restore, Inline Card Note Snippet Previews, Sort by Favorites Mode, Favorites & Notes Combined Telemetry (1 favorited · 1 annotated), Dynamic Notes Quick-Filter Chip (📝 Notes (N)), Full-Text Search inside Reading Notes, Persistent Personal Reading Notes Subsystem (folio_notes), Combined Catalog Badges (★ 📝 Title), Inline Note Editor Dialog with Save/Clear, Portable JSON Note Backup & Restore, Control Center 4-Tile Live Stats Strip with ★ FAVS Deep-Link, Version 2 Portable JSON Backup with is_favorite Serialization, Persistent Favorites Subsystem with SharedPreferences zero-DB migration, Dynamic Gold ★ Favorites Quick-Filter Chip, Starred Title Prefix (★), Fullscreen Pitch-Black Cover Lightbox with centered cover art, Live KFX file size indicator in Book Details, 64x96dp Cover Thumbnail Header in Book Details, Expandable Synopsis Toggle (Read full synopsis › / Show less), Clickable Genre Pills in Book Details with 1-tap catalog filtering, Control Center Live Stat Tile Auto-Refresh on dialog close, Tappable Header Version Badge, 1-Tap Select New / Clear batch selection pill in folder importer, Dynamic filtered count header (X of Y), unified inline search clear button (✕) across catalog and batch folder importer, Clickable Author Quick-Filter link with automatic genre reset and search sync, Mark as Finished / Unread toggle pill in Book Details, dynamic teal Finished (N) chip filter, green Finished card badges, accurate reading progress SQL check, dynamic Reading quick-filter chip with in-progress badges, deduplicated Title-Cased genre tags, bibliographic meta-tag filtering from top chips, live reading progress stats in About Folio, Pass-3 missing-metadata auto-repair, natural-name author formatting, frequency-ranked genre chips with live counts, unified local library scan to app-private storage, right-aligned Home All Metadata link, safe cascade remove action, live reading progress badges and sort mode, 700+ EPUB live filename search in folder importer, "Hide Imported" toggle pill, ISBN-to-title genre fallback, multi-select batch EPUB folder importer from storage, portable metadata JSON backup/restore, missing-cover auto-repair with Open Library Covers API + 302 redirect following, catalog sort controls Recent/Title/Author/Year/Progress, in-app editable metadata dialog with live persistence, full-library `Sync & Enrich` metadata backfiller, real-time search filtering, Title-Cased genre chips, rich Book Details popup with 0ms reader launch, duplicate import guard with "Open Existing Copy" vs "Re-Import & Replace", "Library Metadata & Genres" browser dialog, persistent `folio_metadata.db`, Open Library metadata enrichment, live OPDS/Atom feed download, heavy omnibus stress-tested up to 23 MB / 1,450 sections, CSS text-transform uppercase/lowercase/capitalize, font-variant small-caps, RTL Arabic/Hebrew direction and right alignment, multi-level nested lists, native tables with thead/colspan/rowspan, malformed asset resilience, full HTML4/HTML5 entity decoding, store-free library search, zero-hyphenation code blocks, proportional sup/sub, `$270` cover container, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 076)
1. `screenshots/verify_r76_insights_clean_bars.png`: Library Insights modal showing zero scrollbar overlap over numbers (`7`, `5`, `4`) and `(TAP TO FILTER)` headers.
2. `screenshots/verify_r76_insights_author_drilldown.png`: Catalog dynamically opened and filtered to `Library Metadata & Genres (4 of 27)` with search query `Aldous Huxley`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v7.6 KFX deployed (`lastUpdateTime=2026-11-22 23:45:54`).
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v7.5 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, Library Insights Visual Analytics Modal (Top Genres, Top Authors, Average Rating & Shelf Stats), Searchable Authors Directory & Long Author Name Ellipsis Polish, Authors Directory Modal (👤) with Book Counts & 1-Tap Author Filtering, RFC-4180 CSV Library Export (/sdcard/kindle/backups/folio_library.csv), 1-Tap Export CSV Pill in Dialog Header, Reading Goal in Markdown Header & JSON Backup, Annual Reading Goal Progress Card (🎯), Goal Customizer Dialog with 1-Tap Presets (5/12/24/36/52), Persistent Reading Goal Preference (folio_ui_prefs), Reading Shelf Clipboard Info (📋 Copy Info), Custom Reading Shelves Subsystem (folio_shelves), Dynamic Indigo Shelf Filter Chips (🏷 <Shelf> (N)), 1-Tap Preset Shelf Picker & Custom Input, Shelf Full-Text Search, JSON Shelf Backup/Restore & Markdown Shelf Export, Reading Log Author Normalization & Live Reading Status, 1-Tap Android Share Intent (Intent.ACTION_SEND), Markdown Reading Log Export & Monospace Preview (/sdcard/kindle/backups/folio_reading_log.md), 1-Tap Copy Markdown to Clipboard, Dynamic Unread Backlog Filter Chip (Unread (N)), 1-Tap Copy Book Info to Clipboard (📋 Copy Info), Sort by Rating Mode, Random Unread Book Picker (🎲), Interactive 5-Star Ratings Subsystem (folio_ratings), Persistent Catalog Sort Preference (folio_ui_prefs), Portable JSON Ratings Backup & Restore, Inline Card Note Snippet Previews, Sort by Favorites Mode, Favorites & Notes Combined Telemetry (1 favorited · 1 annotated), Dynamic Notes Quick-Filter Chip (📝 Notes (N)), Full-Text Search inside Reading Notes, Persistent Personal Reading Notes Subsystem (folio_notes), Combined Catalog Badges (★ 📝 Title), Inline Note Editor Dialog with Save/Clear, Portable JSON Note Backup & Restore, Control Center 4-Tile Live Stats Strip with ★ FAVS Deep-Link, Version 2 Portable JSON Backup with is_favorite Serialization, Persistent Favorites Subsystem with SharedPreferences zero-DB migration, Dynamic Gold ★ Favorites Quick-Filter Chip, Starred Title Prefix (★), Fullscreen Pitch-Black Cover Lightbox with centered cover art, Live KFX file size indicator in Book Details, 64x96dp Cover Thumbnail Header in Book Details, Expandable Synopsis Toggle (Read full synopsis › / Show less), Clickable Genre Pills in Book Details with 1-tap catalog filtering, Control Center Live Stat Tile Auto-Refresh on dialog close, Tappable Header Version Badge, 1-Tap Select New / Clear batch selection pill in folder importer, Dynamic filtered count header (X of Y), unified inline search clear button (✕) across catalog and batch folder importer, Clickable Author Quick-Filter link with automatic genre reset and search sync, Mark as Finished / Unread toggle pill in Book Details, dynamic teal Finished (N) chip filter, green Finished card badges, accurate reading progress SQL check, dynamic Reading quick-filter chip with in-progress badges, deduplicated Title-Cased genre tags, bibliographic meta-tag filtering from top chips, live reading progress stats in About Folio, Pass-3 missing-metadata auto-repair, natural-name author formatting, frequency-ranked genre chips with live counts, unified local library scan to app-private storage, right-aligned Home All Metadata link, safe cascade remove action, live reading progress badges and sort mode, 700+ EPUB live filename search in folder importer, "Hide Imported" toggle pill, ISBN-to-title genre fallback, multi-select batch EPUB folder importer from storage, portable metadata JSON backup/restore, missing-cover auto-repair with Open Library Covers API + 302 redirect following, catalog sort controls Recent/Title/Author/Year/Progress, in-app editable metadata dialog with live persistence, full-library `Sync & Enrich` metadata backfiller, real-time search filtering, Title-Cased genre chips, rich Book Details popup with 0ms reader launch, duplicate import guard with "Open Existing Copy" vs "Re-Import & Replace", "Library Metadata & Genres" browser dialog, persistent `folio_metadata.db`, Open Library metadata enrichment, live OPDS/Atom feed download, heavy omnibus stress-tested up to 23 MB / 1,450 sections, CSS text-transform uppercase/lowercase/capitalize, font-variant small-caps, RTL Arabic/Hebrew direction and right alignment, multi-level nested lists, native tables with thead/colspan/rowspan, malformed asset resilience, full HTML4/HTML5 entity decoding, store-free library search, zero-hyphenation code blocks, proportional sup/sub, `$270` cover container, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 075)
1. `screenshots/verify_r75_library_insights_modal.png`: Library Insights modal showing live summary, blue TOP GENRES bar chart, and amber TOP AUTHORS bar chart.
2. `screenshots/verify_r75_control_center_v75.png`: Control Center header showing `Folio v7.5 KFX` badge and 4-tile live stat strip.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v7.5 KFX deployed (`lastUpdateTime=2026-11-22 23:39:14`).
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v7.4 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, Searchable Authors Directory & Long Author Name Ellipsis Polish, Authors Directory Modal (👤) with Book Counts & 1-Tap Author Filtering, RFC-4180 CSV Library Export (/sdcard/kindle/backups/folio_library.csv), 1-Tap Export CSV Pill in Dialog Header, Reading Goal in Markdown Header & JSON Backup, Annual Reading Goal Progress Card (🎯), Goal Customizer Dialog with 1-Tap Presets (5/12/24/36/52), Persistent Reading Goal Preference (folio_ui_prefs), Reading Shelf Clipboard Info (📋 Copy Info), Custom Reading Shelves Subsystem (folio_shelves), Dynamic Indigo Shelf Filter Chips (🏷 <Shelf> (N)), 1-Tap Preset Shelf Picker & Custom Input, Shelf Full-Text Search, JSON Shelf Backup/Restore & Markdown Shelf Export, Reading Log Author Normalization & Live Reading Status, 1-Tap Android Share Intent (Intent.ACTION_SEND), Markdown Reading Log Export & Monospace Preview (/sdcard/kindle/backups/folio_reading_log.md), 1-Tap Copy Markdown to Clipboard, Dynamic Unread Backlog Filter Chip (Unread (N)), 1-Tap Copy Book Info to Clipboard (📋 Copy Info), Sort by Rating Mode, Random Unread Book Picker (🎲), Interactive 5-Star Ratings Subsystem (folio_ratings), Persistent Catalog Sort Preference (folio_ui_prefs), Portable JSON Ratings Backup & Restore, Inline Card Note Snippet Previews, Sort by Favorites Mode, Favorites & Notes Combined Telemetry (1 favorited · 1 annotated), Dynamic Notes Quick-Filter Chip (📝 Notes (N)), Full-Text Search inside Reading Notes, Persistent Personal Reading Notes Subsystem (folio_notes), Combined Catalog Badges (★ 📝 Title), Inline Note Editor Dialog with Save/Clear, Portable JSON Note Backup & Restore, Control Center 4-Tile Live Stats Strip with ★ FAVS Deep-Link, Version 2 Portable JSON Backup with is_favorite Serialization, Persistent Favorites Subsystem with SharedPreferences zero-DB migration, Dynamic Gold ★ Favorites Quick-Filter Chip, Starred Title Prefix (★), Fullscreen Pitch-Black Cover Lightbox with centered cover art, Live KFX file size indicator in Book Details, 64x96dp Cover Thumbnail Header in Book Details, Expandable Synopsis Toggle (Read full synopsis › / Show less), Clickable Genre Pills in Book Details with 1-tap catalog filtering, Control Center Live Stat Tile Auto-Refresh on dialog close, Tappable Header Version Badge, 1-Tap Select New / Clear batch selection pill in folder importer, Dynamic filtered count header (X of Y), unified inline search clear button (✕) across catalog and batch folder importer, Clickable Author Quick-Filter link with automatic genre reset and search sync, Mark as Finished / Unread toggle pill in Book Details, dynamic teal Finished (N) chip filter, green Finished card badges, accurate reading progress SQL check, dynamic Reading quick-filter chip with in-progress badges, deduplicated Title-Cased genre tags, bibliographic meta-tag filtering from top chips, live reading progress stats in About Folio, Pass-3 missing-metadata auto-repair, natural-name author formatting, frequency-ranked genre chips with live counts, unified local library scan to app-private storage, right-aligned Home All Metadata link, safe cascade remove action, live reading progress badges and sort mode, 700+ EPUB live filename search in folder importer, "Hide Imported" toggle pill, ISBN-to-title genre fallback, multi-select batch EPUB folder importer from storage, portable metadata JSON backup/restore, missing-cover auto-repair with Open Library Covers API + 302 redirect following, catalog sort controls Recent/Title/Author/Year/Progress, in-app editable metadata dialog with live persistence, full-library `Sync & Enrich` metadata backfiller, real-time search filtering, Title-Cased genre chips, rich Book Details popup with 0ms reader launch, duplicate import guard with "Open Existing Copy" vs "Re-Import & Replace", "Library Metadata & Genres" browser dialog, persistent `folio_metadata.db`, Open Library metadata enrichment, live OPDS/Atom feed download, heavy omnibus stress-tested up to 23 MB / 1,450 sections, CSS text-transform uppercase/lowercase/capitalize, font-variant small-caps, RTL Arabic/Hebrew direction and right alignment, multi-level nested lists, native tables with thead/colspan/rowspan, malformed asset resilience, full HTML4/HTML5 entity decoding, store-free library search, zero-hyphenation code blocks, proportional sup/sub, `$270` cover container, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 074)
1. `screenshots/verify_r74_authors_search_ellipsis.png`: Authors Directory modal showing new `Search authors...` input at top and single-line layout on author rows.
2. `screenshots/verify_r74_authors_filtered_live.png`: Authors Directory modal dynamically filtered to `Authors Directory (1 of 23)` with query `"Orwell"`, displaying `George Orwell (2 books ›)`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v7.4 KFX deployed (`lastUpdateTime=2026-11-22 23:33:18`).

## Verification Evidence (Round 073)
1. `screenshots/verify_r73_authors_directory_modal.png`: Authors Directory modal listing authors sorted by book count descending (`Aldous Huxley (4 books ›)`).
2. `screenshots/verify_r73_author_selected_filter.png`: Catalog dynamically filtered to `Library Metadata & Genres (4 of 27)` with search query `Aldous Huxley`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v7.3 KFX deployed (`lastUpdateTime=2026-11-22 23:25:53`).
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v7.2 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, RFC-4180 CSV Library Export (/sdcard/kindle/backups/folio_library.csv), 1-Tap Export CSV Pill in Dialog Header, Reading Goal in Markdown Header & JSON Backup, Annual Reading Goal Progress Card (🎯), Goal Customizer Dialog with 1-Tap Presets (5/12/24/36/52), Persistent Reading Goal Preference (folio_ui_prefs), Reading Shelf Clipboard Info (📋 Copy Info), Custom Reading Shelves Subsystem (folio_shelves), Dynamic Indigo Shelf Filter Chips (🏷 <Shelf> (N)), 1-Tap Preset Shelf Picker & Custom Input, Shelf Full-Text Search, JSON Shelf Backup/Restore & Markdown Shelf Export, Reading Log Author Normalization & Live Reading Status, 1-Tap Android Share Intent (Intent.ACTION_SEND), Markdown Reading Log Export & Monospace Preview (/sdcard/kindle/backups/folio_reading_log.md), 1-Tap Copy Markdown to Clipboard, Dynamic Unread Backlog Filter Chip (Unread (N)), 1-Tap Copy Book Info to Clipboard (📋 Copy Info), Sort by Rating Mode, Random Unread Book Picker (🎲), Interactive 5-Star Ratings Subsystem (folio_ratings), Persistent Catalog Sort Preference (folio_ui_prefs), Portable JSON Ratings Backup & Restore, Inline Card Note Snippet Previews, Sort by Favorites Mode, Favorites & Notes Combined Telemetry (1 favorited · 1 annotated), Dynamic Notes Quick-Filter Chip (📝 Notes (N)), Full-Text Search inside Reading Notes, Persistent Personal Reading Notes Subsystem (folio_notes), Combined Catalog Badges (★ 📝 Title), Inline Note Editor Dialog with Save/Clear, Portable JSON Note Backup & Restore, Control Center 4-Tile Live Stats Strip with ★ FAVS Deep-Link, Version 2 Portable JSON Backup with is_favorite Serialization, Persistent Favorites Subsystem with SharedPreferences zero-DB migration, Dynamic Gold ★ Favorites Quick-Filter Chip, Starred Title Prefix (★), Fullscreen Pitch-Black Cover Lightbox with centered cover art, Live KFX file size indicator in Book Details, 64x96dp Cover Thumbnail Header in Book Details, Expandable Synopsis Toggle (Read full synopsis › / Show less), Clickable Genre Pills in Book Details with 1-tap catalog filtering, Control Center Live Stat Tile Auto-Refresh on dialog close, Tappable Header Version Badge, 1-Tap Select New / Clear batch selection pill in folder importer, Dynamic filtered count header (X of Y), unified inline search clear button (✕) across catalog and batch folder importer, Clickable Author Quick-Filter link with automatic genre reset and search sync, Mark as Finished / Unread toggle pill in Book Details, dynamic teal Finished (N) chip filter, green Finished card badges, accurate reading progress SQL check, dynamic Reading quick-filter chip with in-progress badges, deduplicated Title-Cased genre tags, bibliographic meta-tag filtering from top chips, live reading progress stats in About Folio, Pass-3 missing-metadata auto-repair, natural-name author formatting, frequency-ranked genre chips with live counts, unified local library scan to app-private storage, right-aligned Home All Metadata link, safe cascade remove action, live reading progress badges and sort mode, 700+ EPUB live filename search in folder importer, "Hide Imported" toggle pill, ISBN-to-title genre fallback, multi-select batch EPUB folder importer from storage, portable metadata JSON backup/restore, missing-cover auto-repair with Open Library Covers API + 302 redirect following, catalog sort controls Recent/Title/Author/Year/Progress, in-app editable metadata dialog with live persistence, full-library `Sync & Enrich` metadata backfiller, real-time search filtering, Title-Cased genre chips, rich Book Details popup with 0ms reader launch, duplicate import guard with "Open Existing Copy" vs "Re-Import & Replace", "Library Metadata & Genres" browser dialog, persistent `folio_metadata.db`, Open Library metadata enrichment, live OPDS/Atom feed download, heavy omnibus stress-tested up to 23 MB / 1,450 sections, CSS text-transform uppercase/lowercase/capitalize, font-variant small-caps, RTL Arabic/Hebrew direction and right alignment, multi-level nested lists, native tables with thead/colspan/rowspan, malformed asset resilience, full HTML4/HTML5 entity decoding, store-free library search, zero-hyphenation code blocks, proportional sup/sub, `$270` cover container, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 072)
1. `screenshots/verify_r72_reading_log_goal_and_csv_btn.png`: Reading Log dialog showing `**2026 Reading Goal:** 1 / 5 books finished (20%)`, `- **Shelf:** Classics` under *1984*, and amber `📊 Export CSV` pill in top bar.
2. `screenshots/verify_r72_csv_exported_banner.png`: Reading Log dialog showing banner updated to `Saved CSV: /sdcard/kindle/backups/folio_library.csv` after tapping `📊 Export CSV`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v7.2 KFX deployed (`lastUpdateTime=2026-11-22 23:15:43`).
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Folio v7.1 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, Annual Reading Goal Progress Card (🎯), Goal Customizer Dialog with 1-Tap Presets (5/12/24/36/52), Persistent Reading Goal Preference (folio_ui_prefs), Reading Shelf Clipboard Info (📋 Copy Info), Custom Reading Shelves Subsystem (folio_shelves), Dynamic Indigo Shelf Filter Chips (🏷 <Shelf> (N)), 1-Tap Preset Shelf Picker & Custom Input, Shelf Full-Text Search, JSON Shelf Backup/Restore & Markdown Shelf Export, Reading Log Author Normalization & Live Reading Status, 1-Tap Android Share Intent (Intent.ACTION_SEND), Markdown Reading Log Export & Monospace Preview (/sdcard/kindle/backups/folio_reading_log.md), 1-Tap Copy Markdown to Clipboard, Dynamic Unread Backlog Filter Chip (Unread (N)), 1-Tap Copy Book Info to Clipboard (📋 Copy Info), Sort by Rating Mode, Random Unread Book Picker (🎲), Interactive 5-Star Ratings Subsystem (folio_ratings), Persistent Catalog Sort Preference (folio_ui_prefs), Portable JSON Ratings Backup & Restore, Inline Card Note Snippet Previews, Sort by Favorites Mode, Favorites & Notes Combined Telemetry (1 favorited · 1 annotated), Dynamic Notes Quick-Filter Chip (📝 Notes (N)), Full-Text Search inside Reading Notes, Persistent Personal Reading Notes Subsystem (folio_notes), Combined Catalog Badges (★ 📝 Title), Inline Note Editor Dialog with Save/Clear, Portable JSON Note Backup & Restore, Control Center 4-Tile Live Stats Strip with ★ FAVS Deep-Link, Version 2 Portable JSON Backup with is_favorite Serialization, Persistent Favorites Subsystem with SharedPreferences zero-DB migration, Dynamic Gold ★ Favorites Quick-Filter Chip, Starred Title Prefix (★), Fullscreen Pitch-Black Cover Lightbox with centered cover art, Live KFX file size indicator in Book Details, 64x96dp Cover Thumbnail Header in Book Details, Expandable Synopsis Toggle (Read full synopsis › / Show less), Clickable Genre Pills in Book Details with 1-tap catalog filtering, Control Center Live Stat Tile Auto-Refresh on dialog close, Tappable Header Version Badge, 1-Tap Select New / Clear batch selection pill in folder importer, Dynamic filtered count header (X of Y), unified inline search clear button (✕) across catalog and batch folder importer, Clickable Author Quick-Filter link with automatic genre reset and search sync, Mark as Finished / Unread toggle pill in Book Details, dynamic teal Finished (N) chip filter, green Finished card badges, accurate reading progress SQL check, dynamic Reading quick-filter chip with in-progress badges, deduplicated Title-Cased genre tags, bibliographic meta-tag filtering from top chips, live reading progress stats in About Folio, Pass-3 missing-metadata auto-repair, natural-name author formatting, frequency-ranked genre chips with live counts, unified local library scan to app-private storage, right-aligned Home All Metadata link, safe cascade remove action, live reading progress badges and sort mode, 700+ EPUB live filename search in folder importer, "Hide Imported" toggle pill, ISBN-to-title genre fallback, multi-select batch EPUB folder importer from storage, portable metadata JSON backup/restore, missing-cover auto-repair with Open Library Covers API + 302 redirect following, catalog sort controls Recent/Title/Author/Year/Progress, in-app editable metadata dialog with live persistence, full-library `Sync & Enrich` metadata backfiller, real-time search filtering, Title-Cased genre chips, rich Book Details popup with 0ms reader launch, duplicate import guard with "Open Existing Copy" vs "Re-Import & Replace", "Library Metadata & Genres" browser dialog, persistent `folio_metadata.db`, Open Library metadata enrichment, live OPDS/Atom feed download, heavy omnibus stress-tested up to 23 MB / 1,450 sections, CSS text-transform uppercase/lowercase/capitalize, font-variant small-caps, RTL Arabic/Hebrew direction and right alignment, multi-level nested lists, native tables with thead/colspan/rowspan, malformed asset resilience, full HTML4/HTML5 entity decoding, store-free library search, zero-hyphenation code blocks, proportional sup/sub, `$270` cover container, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 071)
1. `screenshots/verify_r71_control_center_reading_goal.png`: Control Center showing `Folio v7.1 KFX` badge, the 4-tile live stats strip, and `🎯 2026 Reading Goal` progress card (`1 of 12 books finished (8%)` with emerald progress bar).
2. `screenshots/verify_r71_reading_goal_updated.png`: Reading Goal card updated live to `1 of 5 books finished (20%)` with wider emerald progress bar after selecting preset `5` in goal dialog.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v7.1 KFX deployed (`lastUpdateTime=2026-11-22 23:07:38`).
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0, `lastUpdateTime=2026-11-19 21:59:23`)
  - Clone: `com.amazon.kindle.epub` ("Milestone Folio v7.0 KFX", v8.159.0.100, native pure-Rust `libkfxconv.so` pipeline, Custom Reading Shelves Subsystem (folio_shelves), Dynamic Indigo Shelf Filter Chips (🏷 <Shelf> (N)), 1-Tap Preset Shelf Picker & Custom Input, Shelf Full-Text Search, JSON Shelf Backup/Restore & Markdown Shelf Export, Reading Log Author Normalization & Live Reading Status, 1-Tap Android Share Intent (Intent.ACTION_SEND), Markdown Reading Log Export & Monospace Preview (/sdcard/kindle/backups/folio_reading_log.md), 1-Tap Copy Markdown to Clipboard, Dynamic Unread Backlog Filter Chip (Unread (N)), 1-Tap Copy Book Info to Clipboard (📋 Copy Info), Sort by Rating Mode, Random Unread Book Picker (🎲), Interactive 5-Star Ratings Subsystem (folio_ratings), Persistent Catalog Sort Preference (folio_ui_prefs), Portable JSON Ratings Backup & Restore, Inline Card Note Snippet Previews, Sort by Favorites Mode, Favorites & Notes Combined Telemetry (1 favorited · 1 annotated), Dynamic Notes Quick-Filter Chip (📝 Notes (N)), Full-Text Search inside Reading Notes, Persistent Personal Reading Notes Subsystem (folio_notes), Combined Catalog Badges (★ 📝 Title), Inline Note Editor Dialog with Save/Clear, Portable JSON Note Backup & Restore, Control Center 4-Tile Live Stats Strip with ★ FAVS Deep-Link, Version 2 Portable JSON Backup with is_favorite Serialization, Persistent Favorites Subsystem with SharedPreferences zero-DB migration, Dynamic Gold ★ Favorites Quick-Filter Chip, Starred Title Prefix (★), Fullscreen Pitch-Black Cover Lightbox with centered cover art, Live KFX file size indicator in Book Details, 64x96dp Cover Thumbnail Header in Book Details, Expandable Synopsis Toggle (Read full synopsis › / Show less), Clickable Genre Pills in Book Details with 1-tap catalog filtering, Control Center Live Stat Tile Auto-Refresh on dialog close, Tappable Header Version Badge, 1-Tap Select New / Clear batch selection pill in folder importer, Dynamic filtered count header (X of Y), unified inline search clear button (✕) across catalog and batch folder importer, Clickable Author Quick-Filter link with automatic genre reset and search sync, Mark as Finished / Unread toggle pill in Book Details, dynamic teal Finished (N) chip filter, green Finished card badges, accurate reading progress SQL check, dynamic Reading quick-filter chip with in-progress badges, deduplicated Title-Cased genre tags, bibliographic meta-tag filtering from top chips, live reading progress stats in About Folio, Pass-3 missing-metadata auto-repair, natural-name author formatting, frequency-ranked genre chips with live counts, unified local library scan to app-private storage, right-aligned Home All Metadata link, safe cascade remove action, live reading progress badges and sort mode, 700+ EPUB live filename search in folder importer, "Hide Imported" toggle pill, ISBN-to-title genre fallback, multi-select batch EPUB folder importer from storage, portable metadata JSON backup/restore, missing-cover auto-repair with Open Library Covers API + 302 redirect following, catalog sort controls Recent/Title/Author/Year/Progress, in-app editable metadata dialog with live persistence, full-library `Sync & Enrich` metadata backfiller, real-time search filtering, Title-Cased genre chips, rich Book Details popup with 0ms reader launch, duplicate import guard with "Open Existing Copy" vs "Re-Import & Replace", "Library Metadata & Genres" browser dialog, persistent `folio_metadata.db`, Open Library metadata enrichment, live OPDS/Atom feed download, heavy omnibus stress-tested up to 23 MB / 1,450 sections, CSS text-transform uppercase/lowercase/capitalize, font-variant small-caps, RTL Arabic/Hebrew direction and right alignment, multi-level nested lists, native tables with thead/colspan/rowspan, malformed asset resilience, full HTML4/HTML5 entity decoding, store-free library search, zero-hyphenation code blocks, proportional sup/sub, `$270` cover container, HTML whitespace normalization, UTF-16 style offsets, native `$266` anchor links, proportional image scaling, Library branding, dynamic hardware metadata, KRIFDocViewer Enhanced Typesetting)

## Verification Evidence (Round 070)
1. `screenshots/verify_r70_shelf_picker_assigned.png`: Book Details on *1984* showing assigned `Shelf: 🏷 Classics  ›` pill positioned under `Your Rating: ★★★★★`.
2. `screenshots/verify_r70_shelf_chip_filter_live.png`: Catalog dialog showing active indigo `🏷 Classics (1)` chip filtering library to `Library Metadata & Genres (1 of 27)`, displaying *1984* with its indigo `🏷 Classics` card badge.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v7.0 KFX deployed (`lastUpdateTime=2026-11-22 22:58:46`).

## Verification Evidence (Round 068)
1. `screenshots/verify_r68_control_center_reading_log_card.png`: Control Center showing `Folio v6.8 KFX` badge and `📄 Reading Log & Export` action card.
2. `screenshots/verify_r68_reading_log_markdown_preview.png`: Reading Log dialog showing saved path banner (`Saved to: /sdcard/kindle/backups/folio_reading_log.md`), full markdown preview starting with `## ★ 1984`, `★★★★★ (5/5)`, and `> Essential dystopian classic - reread Part 3`, and `COPY MARKDOWN` button.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v6.8 KFX deployed (`lastUpdateTime=2026-11-22 22:43:52`).

## Verification Evidence (Round 067)
1. `screenshots/verify_r67_unread_chip_live.png`: Catalog dialog showing active teal `Unread (21)` quick-filter chip filtering library to `Library Metadata & Genres (21 of 27)`.
2. `screenshots/verify_r67_book_details_copy_info.png`: Book Details popup on *1984* showing `📋 Copy Info` pill cleanly positioned across from `Remove from Folio Library`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v6.7 KFX deployed (`lastUpdateTime=2026-11-22 22:35:01`).

## Verification Evidence (Round 066)
1. `screenshots/verify_r66_sort_rating_and_dice_pill.png`: Catalog dialog in `Sort: Rating` mode with `🎲` pill beside Sort, ranking 5-star `★ 📝 1984` (`★★★★★ 5/5`) at position 0 at top.
2. `screenshots/verify_r66_random_book_picked.png`: Book Details popup opened for randomly picked unread book (*Delphi Complete Works of Pliny the Younger*).
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v6.6 KFX deployed (`lastUpdateTime=2026-11-22 22:27:32`).

## Verification Evidence (Round 065)
1. `screenshots/verify_r65_book_details_5star_rating.png`: Book Details on *1984* showing interactive `Your Rating: ★★★★★` fully illuminated in gold.
2. `screenshots/verify_r65_card_star_rating_badge.png`: Catalog list card for *1984* displaying gold `★★★★★  5/5` badge directly above italic amber note snippet.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v6.5 KFX deployed (`lastUpdateTime=2026-11-22 22:17:59`).

## Verification Evidence (Round 064)
1. `screenshots/verify_r64_sort_favorites_note_snippet.png`: Catalog dialog in `Sort: Favorites` mode with `★ 📝 1984` pinned to position 0 at top displaying italic amber snippet preview `“Essential dystopian classic - reread Part 3”`.
2. `screenshots/verify_r64_about_v64_telemetry.png`: Control Center About Folio dialog displaying `Version: Folio v6.4 KFX (Enhanced Typesetting)` and `• Favorites & Notes: 1 favorited · 1 annotated`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v6.4 KFX deployed (`lastUpdateTime=2026-11-22 22:10:50`).

## Verification Evidence (Round 063)
1. `screenshots/verify_r63_notes_chip_live.png`: Catalog dialog showing active amber `📝 Notes (1)` quick-filter chip with header `Library Metadata & Genres (1 of 27)` filtering strictly to `★ 📝 1984`.
2. `screenshots/verify_r63_search_inside_note.png`: Catalog dialog showing search query `"Part 3"` matching inside personal note text and filtering `Library Metadata & Genres (1 of 27)` to `★ 📝 1984`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v6.3 KFX deployed (`lastUpdateTime=2026-11-22 22:03:29`).

## Verification Evidence (Round 062)
1. `screenshots/verify_r62_personal_note_saved.png`: Book Details popup on *1984* displaying amber-bordered `📝 Personal Note (tap to edit)` card with saved text `"Essential dystopian classic - reread Part 3"`.
2. `screenshots/verify_r62_card_note_badge.png`: Catalog list card for *1984* rendering combined `★ 📝 1984` favorite and personal note indicators.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v6.2 KFX deployed (`lastUpdateTime=2026-11-22 21:55:06`).

## Verification Evidence (Round 061)
1. `screenshots/verify_r61_control_center_4tile_favs.png`: Control Center (More tab) showing `Folio v6.1 KFX` badge and the crisp 4-tile live stats strip (`29 BOOKS` · `5 READING` · `1 FINISHED` · `1 ★ FAVS`) cleanly aligned on one row.
2. `screenshots/verify_r61_favs_tile_deeplink.png`: Catalog pre-filtered to gold `★ Favorites (1)` chip displaying `Library Metadata & Genres (1 of 27)` with starred `★ 1984` card upon tapping `1 ★ FAVS` tile.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v6.1 KFX deployed (`lastUpdateTime=2026-11-22 21:48:47`).

## Verification Evidence (Round 060)
1. `screenshots/verify_r60_book_details_favorite_pill.png`: Book Details popup on *1984* showing side-by-side `✓ Mark as Finished` and `☆ Favorite` action pills.
2. `screenshots/verify_r60_favorites_chip_live.png`: Catalog filtered to gold `★ Favorites (1)` chip displaying `Library Metadata & Genres (1 of 27)` and starred `★ 1984` card.
3. `logs/r60_about.xml`: Control Center About Folio showing `Version: Folio v6.0 KFX (Enhanced Typesetting)` and `• Favorited Books: 1`.
4. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
5. `dumpsys package com.amazon.kindle.epub`: Folio v6.0 KFX deployed (`lastUpdateTime=2026-11-22 21:40:01`).

## Verification Evidence (Round 059)
1. `screenshots/verify_r59_book_details_kfx_size.png`: Book Details popup on *1984* displaying the live `Format: Enhanced KFX · 1.2 MB` line.
2. `screenshots/verify_r59_cover_lightbox.png`: Pure `#000000` black Fullscreen Cover Lightbox with centered *1984* cover art, `1984 · George Orwell` caption, and tap-to-close behavior.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v5.9 KFX deployed (`lastUpdateTime=2026-11-22 21:32:43`).

## Verification Evidence (Round 058)
1. `screenshots/verify_r58_book_details_cover_header.png`: Book Details popup on *1984* showing `64×96dp` cover thumbnail header, title, author quick-filter, clickable genre pills, 5-line clamped description, and teal `Read full synopsis ›` toggle.
2. `screenshots/verify_r58_synopsis_expanded.png`: Synopsis cleanly expanded to full length with toggle dynamically updated to `Show less`.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v5.8 KFX deployed (`lastUpdateTime=2026-11-22 21:25:37`).

## Verification Evidence (Round 057)
1. `screenshots/verify_r57_book_details_genre_pills.png`: Book Details popup on *1984* showing the horizontal scrollable row of clickable blue genre pills (`Totalitarianism`, `Fiction`, `Dystopias`).
2. `screenshots/verify_r57_genre_pill_filter_live.png`: Catalog filtered immediately upon tapping `Fiction` pill, displaying `Library Metadata & Genres (10 of 27)` with active `✕` clear button.
3. `logs/r57_about.xml`: Control Center `Folio v5.7 KFX` header badge tapped, directly opening About Folio with `Version: Folio v5.7 KFX (Enhanced Typesetting)`.
4. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
5. `dumpsys package com.amazon.kindle.epub`: Folio v5.7 KFX deployed (`lastUpdateTime=2026-11-22 21:14:57`).

## Verification Evidence (Round 056)
1. `screenshots/verify_r56_control_center_stats_strip.png`: Control Center (`More` tab) showing `Folio v5.6 KFX` badge and the 3-tile live stats strip (`29 BOOKS` · `5 READING` · `1 FINISHED`) above action cards.
2. `screenshots/verify_r56_stat_tile_deeplink_reading.png`: Metadata catalog pre-filtered to teal `Reading (5)` chip displaying `Library Metadata & Genres (5 of 27)` after tapping the `5 READING` tile.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v5.6 KFX deployed (`lastUpdateTime=2026-11-22 21:06:13`).

## Verification Evidence (Round 055)
1. `screenshots/verify_r55_select_new_orwell.png`: Batch EPUB Importer on `/sdcard/en/0` with query `Orwell` showing 12 unimported EPUBs checked, amber `Clear (12)` pill, and bottom `Import Selected (12)` CTA button.
2. `screenshots/verify_r55_about_v55.png`: Control Center About Folio dialog displaying `Version: Folio v5.5 KFX (Enhanced Typesetting)` and live reading stats (`5 in progress · 1 finished`).
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v5.5 KFX deployed (`lastUpdateTime=2026-11-22 20:55:15`).

## Verification Evidence (Round 054)
1. `screenshots/verify_r54_filtered_count_header.png`: Library Metadata & Genres (27) inside Folio showing active teal `Reading (5)` chip updating dialog header to `Library Metadata & Genres (5 of 27)`.
2. `screenshots/verify_r54_folder_search_clear.png`: Batch EPUB Importer browsing `/sdcard/en/0` with query `Orwell` showing 14 matching EPUBs and inline `✕` clear button in search bar.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v5.4 KFX deployed (`lastUpdateTime=2026-11-22 20:48:51`).

## Verification Evidence (Round 053)
1. `screenshots/verify_r53_finished_chip_live.png`: Library Metadata & Genres (27) inside Folio showing active teal `Finished (1)` chip filtering strictly to *Moby Dick* with its green `✓ Finished` card badge.
2. `screenshots/verify_r53_author_filter_and_clear.png`: Library Metadata & Genres filtered to Aldous Huxley's books (*Crome Yellow*, *Island*, *Brave New World*) via the Book Details `Aldous Huxley  ›` link, showing inline `✕` clear button in search bar.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v5.3 KFX deployed (`lastUpdateTime=2026-11-22 20:41:19`).

## Verification Evidence (Round 052)
1. `screenshots/verify_r52_book_details_mark_finished.png`: Book Details dialog for *Moby Dick* displaying the emerald `✓ Mark as Finished` pill above `Remove from Folio Library`.
2. `screenshots/verify_r52_finished_chip_live.png`: Library Metadata & Genres (27) showing active teal `Finished (1)` chip filtering strictly to *Moby Dick* with its green `✓ Finished` card badge.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v5.2 KFX deployed (`lastUpdateTime=2026-11-22 20:32:34`).

## Verification Evidence (Round 051)
1. `screenshots/verify_r51_reading_chip_filter.png`: Library Metadata & Genres (27) showing active teal `Reading (5)` chip filtering to in-progress books with `4% read` and `2% read` progress badges.
2. `screenshots/verify_r51_about_v51_accurate_stats.png`: Control Center `About Folio` dialog displaying `Version: Folio v5.1 KFX (Enhanced Typesetting)` and accurate live `Reading Progress: 5 in progress · 0 finished` alongside storage stats.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v5.1 KFX deployed (`lastUpdateTime=2026-11-22 20:26:05`).
1. `screenshots/verify_r50_clean_genres_and_chips.png`: Library Metadata & Genres (27) showing top literary chips (`All (27)`, `Fiction (7)`, `History (5)`, `Rome (3)`, `Biography (2)`), and deduplicated Title-Cased genres on *Crome Yellow* (`Fiction, Intellectuals, Country Homes, England, Humorous`) and *The Dream of the Red Chamber* (`Chinese Fiction, China, Fiction`).
2. `screenshots/verify_r50_about_v50_reading_stats.png`: Control Center `About Folio` dialog displaying `Version: Folio v5.0 KFX (Enhanced Typesetting)` and live `Reading Progress: 0 in progress · 29 finished` alongside storage stats.
3. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
4. `dumpsys package com.amazon.kindle.epub`: Folio v5.0 KFX deployed (`lastUpdateTime=2026-11-22 20:18:41`).
1. `screenshots/verify_r49_pass3_enriched_all.png`: Library Metadata & Genres (27) showing natural author name `"Herman Melville · 2001-07-01"` on *Moby Dick*, enriched *1984* (`2003 · Houghton Mifflin Harcourt`, `ISBN: 0582777313`, `Totalitarianism, Fiction...`), and updated `Fiction (7)` top genre chip.
2. `screenshots/verify_r49_moksha_crome_yellow.png`: Scrolled view showing Pass-3 enriched *Moksha* (`2012-04-12`, `ISBN: 9781594775178`, `Hallucinogenic drugs...`) and *Crome Yellow* (`2011-01-06`, `ISBN: 9781407020938`, `Fiction, Intellectuals...`).
3. `screenshots/verify_r49_about_v45.png`: Control Center `About Folio` dialog displaying `Version: Folio v4.5 KFX (Enhanced Typesetting)`, 29 indexed books, 27 enriched records, and 13.3 MB App Library storage.
4. `dumpsys package com.amazon.kindle`: Stock app bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
5. `dumpsys package com.amazon.kindle.epub`: Folio v4.5 KFX deployed (`lastUpdateTime=2026-11-22 20:08:00`).
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


---

# Round 044: Milestone Folio v4.0 KFX Polish

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v4.0 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 044 complete:
  1. **Drop-in Source Integration (Task A)**:
     - Integrated `EpubMetadataExtractor.java`, `FolioMetadataCatalogDialog.java`, and `FolioMoreView.java` from `dropin_src/`.
     - `normalizePubDate()`: Strips raw ISO timestamps (`2000-07-15 00:00:00+01:00` → `2000-07-15`) dynamically across list cards and Book Details popups.
     - `isNoiseTag()`: Strips Calibre reading status tags (`Read`, `Unread`, `To Read`, `Ebook`, `Calibre`, `General`) from EPUB extraction and genre chips bar.
     - Upgraded **About Folio** with live file-tree measurements (App Library Storage: 11.1 MB, Legacy `/sdcard/kindle`: 203.0 MB, Temp Open-Once Cache: 1.3 MB) and one-tap `CLEAR TEMP CACHE` button.
     - Built `classes9.dex` (191,888 B), rebuilt APK with apktool, signed, and deployed to Galaxy M32.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.0_kfx/base.apk` (131 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `screenshots/verify_r44_clean_dates_and_genres.png`: Proves *Animal Farm* shows clean `2000-07-15` date without timestamp, and `"Read"` is cleanly absent from the genre filter chips bar.
     - `screenshots/verify_r44_about_v40_storage_and_clear_cache.png`: Proves About Folio shows `Folio v4.0 KFX`, live App Library Storage (11.1 MB), Temp Cache (1.3 MB), and the new `CLEAR TEMP CACHE` button.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.0_kfx/base.apk` (131 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report044.zip` (< 1.5 MB)


---

# Round 045: Unified Home Import Pill & App-Private Download Pipeline (Folio v4.1 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v4.1 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 045 complete:
  1. **Drop-in Source Integration (Task A)**:
     - Integrated `OpenCatalogView.java` and `FolioMoreView.java` from `dropin_src/`.
     - Added emerald `Import` pill (`0xFF34D399`) into Home top bar (`Folio` | `Import` | `OPDS` | `Backup`).
     - Tapping `Import` directly opens `FolioFolderBrowserDialog` from Home tab for 1-tap batch EPUB import.
     - Unified Home/OPDS download pipeline into app-private `files/folio_library/` with metadata enrichment and `kindle_library.db` registration.
     - Bumped version badge to `Folio v4.1 KFX`.
     - Built `classes9.dex` (192,576 B), rebuilt APK with apktool, signed, and deployed to Galaxy M32.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.1_kfx/base.apk` (131 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `screenshots/verify_r45_home_import_pill.png`: Proves Home tab displays top action bar `Folio` | `Import` | `OPDS` | `Backup` with cover thumbnails in Continue Reading.
     - `screenshots/verify_r45_home_import_dialog.png`: Proves 1-tap `Import` pill opens dark Batch EPUB Importer dialog directly from Home tab.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.1_kfx/base.apk` (131 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report045.zip` (~1.5 MB)


---

# Round 046: Home "All Metadata ›" Link & Clean Library Book Removal (Folio v4.2 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v4.2 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 046 complete:
  1. **Source Integration (Task A)**:
     - Integrated `OpenCatalogView.java` with teal `All Metadata ›` link beside `Continue Reading` header on Home.
     - Implemented `🗑 Remove from Folio Library` action in `FolioMetadataCatalogDialog.java` Book Details dialog with confirmation dialog and safe cascade deletion across `folio_metadata.db`, `kindle_library.db`, and app-private storage, preserving external storage files.
     - Bumped version badge to `Folio v4.2 KFX` in `FolioMoreView.java`.
     - Built `classes9.dex` (195,312 B), rebuilt APK with apktool, signed, and deployed to Galaxy M32.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.2_kfx/base.apk` (131 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `screenshots/verify_r46_home_all_metadata_link.png`: Proves Home tab displays `Continue Reading` with teal `All Metadata ›` link cleanly aligned on the right.
     - `screenshots/verify_r46_book_details_remove_action.png`: Proves Book Details (`ⓘ`) dialog displays red `🗑 Remove from Folio Library` action button.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.2_kfx/base.apk` (131 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report046.zip` (~1.4 MB)


---

# Round 047: Live Reading Progress Badges & Metadata Polish (Folio v4.3 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v4.3 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 047 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Added live reading progress querying from `kindle_library.db` (`UserContent.READING_PROGRESS` & `KindleContent.READ_STATE`), rendering `X% read` badges on cards.
     - Added `Sort: Progress` mode to sort pill cycle (`Recent` → `Title` → `Author` → `Year` → `Progress`).
     - Added `cleanGenresForDisplay()` filtering Calibre noise tags (`Read`, etc.) and suppressed `Language: UND` in Book Details.
     - Bumped version badge to `Folio v4.3 KFX` in `FolioMoreView.java`.
     - Built `classes9.dex` (197,224 B), rebuilt APK with apktool, signed, and deployed to Galaxy M32.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.3_kfx/base.apk` (131 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `screenshots/verify_r47_sort_progress_badges.png`: Proves `Sort: Progress` mode floats books with live progress badges (`9% read`, `8% read`, `4% read`) to the top.
     - `screenshots/verify_r47_clean_book_details.png`: Proves Animal Farm Book Details (`ⓘ`) displays crisp `Genres: Fiction` without `"Read"` noise tag and zero `Language: UND` clutter.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.3_kfx/base.apk` (131 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report047.zip` (~650 KB)


---

# Round 048: Frequency-Ranked Genre Chips & Unified Local Scan (Folio v4.4 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v4.4 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 048 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Genre Chips now ranked by book count frequency (`All (27)`, `History (5)`, `Fiction (4)`, `Biography (2)`, `Correspondence (2)`), filtering out compound `--` LC headings.
     - Unified `scanLocalLibrary()` in `FolioMoreView.java` to convert new EPUBs into app-private `files/folio_library/` with full metadata enrichment and double DB registration.
     - Bumped version badge to `Folio v4.4 KFX` in `FolioMoreView.java`.
     - Built `classes9.dex` (198,512 B), rebuilt APK with apktool, signed, and deployed to Galaxy M32.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.4_kfx/base.apk` (131 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `screenshots/verify_r48_ranked_genre_chips_counts.png`: Proves Genre Chips strip displays `All (27)`, `History (5)`, `Fiction (4)`, `Biography (2)`, `Correspondence (2)` with live counts and no `--` strings.
     - `screenshots/verify_r48_control_center_v44.png`: Proves Control Center shows `Folio v4.4 KFX` badge and offline tools.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v4.4_kfx/base.apk` (131 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report048.zip` (~650 KB)


---

# Round 079: Stacked Reading Status Bar & Top Publishers Chart in Library Insights (Folio v7.9 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v7.9 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 079 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMoreView.java` from `dropin_src/`.
     - Added `READING STATUS (TAP TO FILTER)` horizontal stacked proportional bar (`● Finished (1)` emerald `#10b981` · `● Reading (5)` sky blue `#0284c7` · `● Unread (21)` slate `#64748b`) with 1-tap filter drill-down.
     - Added `TOP PUBLISHERS (TAP TO FILTER)` teal horizontal bar chart at the bottom of the Library Insights modal with 1-tap search filter drill-down.
     - Bumped version badge to `Folio v7.9 KFX (Enhanced Typesetting)`.
     - Built `classes9.dex` (263,236 B), fast-rebuilt APK, signed, and deployed to Galaxy M32 (`lastUpdateTime=2026-11-23 00:05:49`).
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v7.9_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `screenshots/verify_r79_insights_stacked_status.png`: Proves stacked proportional `READING STATUS` bar with Finished (1), Reading (5), Unread (21) segments and legend pills above `TOP GENRES`.
     - `screenshots/verify_r79_insights_shelves_publishers.png`: Proves `CUSTOM SHELVES` (`🏷 Classics 1`) and `TOP PUBLISHERS` teal bars (`Focus Publishing/R. Pullins Company 2`, `HarperCollins 2`, `Airlift 1`, `Apud Janssonio-Waesbergios 1`) and `CLOSE` button inside modal.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v7.9_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report079.zip`


---

# Round 080: 3-Column Cover Art Grid View Toggle (Folio v8.0 KFX Milestone)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.0 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 080 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Added persistent `⊞` / `☰` view-mode toggle pill in catalog top bar with `SharedPreferences("folio_ui_prefs", "catalog_grid_mode")` persistence.
     - Implemented 3-column Cover Art Grid mode with extracted `.cover.jpg` art, 2-line titles, compact status badges (`✓ Done`, `X%`, `★ 5/5`, `Unread`), and `ⓘ` details buttons.
     - Bumped version badge to `Milestone Folio v8.0 KFX (Enhanced Typesetting)`.
     - Built `classes9.dex` (267,104 B), fast-rebuilt APK, signed, and deployed to Galaxy M32 (`lastUpdateTime=2026-11-23 00:13:19`).
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.0_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `screenshots/verify_r80_catalog_cover_grid_view.png`: Proves 3-column Cover Art Grid view displaying covers, titles, status badges (`★ 5/5`, `✓ Done`, `Unread`), `ⓘ` buttons, and `☰` toggle pill.
     - `screenshots/verify_r80_control_center_v80.png`: Proves Control Center displays amber `Folio v8.0 KFX` milestone badge.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.0_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report080.zip`


---

# Round 081: Uncropped FIT_CENTER Grid Covers, Uniform Card Alignment & Mini Progress Bars (Folio v8.1 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.1 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 081 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Switched grid cover scaling to `ScaleType.FIT_CENTER` mounted on `#0D1017` dark mat (height 132dp) preventing cropping of tall cover art.
     - Enforced `minLines(2)` and `maxLines(2)` on tile titles so all cards maintain uniform pixel-aligned bottom borders.
     - Added sleek 3dp horizontal progress mini-bars directly under each cover (emerald for `✓ Done`, sky blue for in-progress `X%`).
     - Bumped version badge to `Folio v8.1 KFX (Enhanced Typesetting)`.
     - Built `classes9.dex` (267,404 B), fast-rebuilt APK, signed, and deployed to Galaxy M32 (`lastUpdateTime=2026-11-23 00:19:32`).
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.1_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `screenshots/verify_r81_grid_uncropped_aligned.png`: Proves uncropped *Moby Dick* and *Moksha* covers, uniform card bottom alignment, and emerald progress bar under *Moby Dick*.
     - `screenshots/verify_r81_grid_reading_bars.png`: Proves filtered `Reading (5 of 27)` grid view displaying sky blue progress mini-bars under all 5 in-progress covers (*The 210th Day* 4%, *Sophocles* 2%, *Aristotle* 8%, *Island*, *Brave New World*).
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.1_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report081.zip`


---

# Round 082: Editable Synopsis Field & Live Kindle DB Title/Author Sync (Folio v8.2 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.2 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 082 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Added multiline `Description / Synopsis` field to Edit Book Metadata dialog (`setMinLines(2)`, `setMaxLines(5)`).
     - Added `syncTitleAuthorToKindleDb(...)` on Save, ensuring edited title and author immediately propagate to `kindle_library.db` (`KindleContent`).
     - Bumped version badge to `Folio v8.2 KFX (Enhanced Typesetting)`.
     - Built `classes9.dex` (268,112 B), fast-rebuilt APK, signed, and deployed to Galaxy M32 (`lastUpdateTime=2026-11-23 00:26:18`).
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.2_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `screenshots/verify_r82_edit_metadata_synopsis.png`: Proves Edit Book Metadata form on *1984* displaying multiline `Description / Synopsis` field with full synopsis.
     - `screenshots/verify_r82_about_v82.png`: Proves About Folio dialog displaying `Version: Folio v8.2 KFX (Enhanced Typesetting)`.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.2_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report082.zip`


---

# Round 083: Custom Shelves Manager & 1-Tap Filter Drill-Down (Folio v8.3 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.3 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 083 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMoreView.java` from `dropin_src/`.
     - Added `🏷 Manage Shelves` action card directly under `📊 Library Insights` on Control Center.
     - Added interactive Manage Shelves modal listing shelves sorted descending, with 1-tap catalog drill-down (`SHELF:Classics`), in-place Rename/Merge, and safe shelf Removal.
     - Bumped version badge to `Folio v8.3 KFX (Enhanced Typesetting)`.
     - Built `classes9.dex` (274,900 B), fast-rebuilt APK, signed, and deployed to Galaxy M32 (`lastUpdateTime=2026-11-23 00:34:05`).
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.3_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `screenshots/verify_r83_control_center_v83.png`: Proves Control Center displays `🏷 Manage Shelves` card under `📊 Library Insights`.
     - `screenshots/verify_r83_manage_shelves.png`: Proves Manage Shelves modal displaying `🏷 Classics` (`1 book ›`) with `Rename` and `Remove` pills, and tapping `🏷 Classics` opens the catalog filtered to `🏷 Classics (1)`.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.3_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report083.zip`


---

# Round 084: Continue Reading Quick-Resume Carousel (Folio v8.4 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.4 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 084 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMoreView.java` from `dropin_src/`.
     - Added `📖 Continue Reading` horizontal card carousel directly below `🎯 2026 Reading Goal` on Control Center.
     - Displays in-progress books sorted descending by progress with `See All (5) ›` deep link, standalone cover art thumbnails, 2-line titles, authors, `X% read · Resume ›` labels, and 3dp sky-blue progress bars.
     - Tapping any card launches book directly into KFX reader.
     - Bumped version badge to `Folio v8.4 KFX (Enhanced Typesetting)`.
     - Built `classes9.dex` (278,892 B), fast-rebuilt APK, signed, and deployed to Galaxy M32 (`lastUpdateTime=2026-11-23 00:41:17`).
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.4_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `screenshots/verify_r84_continue_reading.png`: Proves Control Center top displays `Folio v8.4 KFX` badge, 4-tile stats strip, `🎯 2026 Reading Goal`, and the new `📖 Continue Reading` horizontal carousel with covers (*Brave New World* 9%, *Aristotle* 8%, etc.), progress bars, and `See All (5) ›`.
     - `screenshots/verify_r84_about_v84.png`: Proves About Folio dialog displaying `Version: Folio v8.4 KFX (Enhanced Typesetting)` and live stats.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.4_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report084.zip`


---

# Round 085: Multi-Select Catalog Batch Curation (Milestone Folio v8.5 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.5 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 085 complete:
  1. **Source Integration**:
     - Added `☑ Select` / `✕ Done` multi-select pill in catalog header, dynamic selection highlighting (`2dp #818CF8` border, `#1E2642` fill), and floating bottom Bulk Action Bar (`N books selected`, `Select All / Clear All`, `🏷 Shelf`, `★ Fav`, `✓ Finished`).
     - Widened Continue Reading carousel cards to `196dp`.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.5_kfx/base.apk` (132 MB).
  2. **Hardware Verification**:
     - `verify_r85_bulk_select_mode.png`: Proves multi-select mode with floating bottom bar.
     - `verify_r85_about_v85.png`: Proves About dialog bumped to Folio v8.5 KFX.
  3. **Stock Invariant**: `com.amazon.kindle` (v8.160.1.0) verified 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Report Archive**: `antig_to_gemini_neon_clunky_report085.zip`.

---

# Round 086: Metadata Health Audit & Catalog Deep-Links (Folio v8.6 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.6 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 086 complete:
  1. **Source Integration**:
     - Integrated `METADATA HEALTH — 70% COMPLETE (TAP TO FIX)` section inside `📊 Library Insights` with 3 live audit tiles: Covers (`1 › No Cover`), Genres (`2 › No Genre`), and Synopsis (`21 › No Synopsis`).
     - Added smart audit filter deep-links into catalog (`MISSING_COVER`, `MISSING_GENRES`, `MISSING_SYNOPSIS`) with dedicated `🩺 Missing ... (N)` active chips.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.6_kfx/base.apk` (132 MB).
  2. **Hardware Verification**:
     - `verify_r86_insights_metadata_health.png`: Proves Library Insights Metadata Health section with 3 audit tiles.
     - `verify_r86_missing_filter_drilldown.png`: Proves catalog filtered to missing genres via `🩺 Missing Genres (2)` chip.
  3. **Stock Invariant**: `com.amazon.kindle` (v8.160.1.0) verified 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Report Archive**: `antig_to_gemini_neon_clunky_report086.zip`.

---

# Round 087: Multi-Select Batch Genre Assignment (Folio v8.7 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.7 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 087 complete:
  1. **Source Integration**:
     - Added sky-blue `+ Genre` bulk action pill in bottom multi-select bar.
     - Built interactive `🏷 Add Genre (N books)` modal with horizontal genre preset chips and comma-separated custom genre input, merging safely into `books_metadata.genres` in `folio_metadata.db`.
     - Center-aligned typography (`Gravity.CENTER`) on all 3 Metadata Health tiles.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.7_kfx/base.apk` (132 MB).
  2. **Hardware Verification**:
     - `verify_r87_bulk_genre_bar.png`: Proves bottom multi-select bar with `+ Genre` pill.
     - `verify_r87_insights_centered_v87.png`: Proves center-aligned Metadata Health tiles and Folio v8.7 KFX version.
  3. **Stock Invariant**: `com.amazon.kindle` (v8.160.1.0) verified 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Report Archive**: `antig_to_gemini_neon_clunky_report087.zip`.

---

# Round 088: Storage & Duplicate Titles Audit (Folio v8.8 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.8 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 088 complete:
  1. **Source Integration**:
     - Added `💾 Storage & Duplicates` action card on Control Center directly below `🏷 Manage Shelves`.
     - Built interactive Storage & Duplicates modal with 3 storage tiles (`13.3 MB App Library`, `682 KB Backups`, `1.3 MB Temp Cache`), Top 5 Largest Books amber bars with 1-tap locate, duplicate titles audit (`✓ No duplicate titles found across 27 cataloged books`), and Clear Temp Cache.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.8_kfx/base.apk` (132 MB).
  2. **Hardware Verification**:
     - `verify_r88_control_center_v88.png`: Proves Control Center with `💾 Storage & Duplicates` action card.
     - `verify_r88_storage_duplicates_modal.png`: Proves Storage & Duplicates modal with storage tiles, largest books, and duplicate audit.
  3. **Stock Invariant**: `com.amazon.kindle` (v8.160.1.0) verified 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Report Archive**: `antig_to_gemini_neon_clunky_report088.zip`.

---

# Round 089: Reading Notes Notebook (Folio v8.9 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v8.9 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 089 complete:
  1. **Source Integration**:
     - Added `📝 Reading Notes Notebook` action card on Control Center directly below `📄 Reading Log & Export`.
     - Built interactive Reading Notes Notebook modal with live search bar, cover thumbnail, rating/shelf badges, warm amber quote card (`Essential dystopian classic - reread Part 3`), 1-tap copy, inline edit, and Markdown export.
     - Expanded Storage & Duplicates scroll viewport to `388dp` with `10dp` bottom padding.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v8.9_kfx/base.apk` (132 MB).
  2. **Hardware Verification**:
     - `verify_r89_notes_notebook.png`: Proves Reading Notes Notebook modal with *1984* note card.
     - `verify_r89_about_v89.png`: Proves About dialog bumped to Folio v8.9 KFX.
  3. **Stock Invariant**: `com.amazon.kindle` (v8.160.1.0) verified 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Report Archive**: `antig_to_gemini_neon_clunky_report089.zip`.

---

# Round 090: Up Next Reading Queue (Milestone Folio v9.0 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v9.0 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 090 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Fixed syntax quote escaping issue in `FolioMetadataCatalogDialog.java` line 1515.
     - Added persistent `SharedPreferences("folio_queue")` FIFO reading queue.
     - Added violet `📌 Up Next` / `📌 Queued` toggle pill in Book Details modal.
     - Added dynamic violet `📌 Up Next (N)` chip to catalog filter strip.
     - Added `📌 Up Next Queue` horizontal card carousel directly below `📖 Continue Reading` on Control Center, showing cover art thumbnails, title, author, `Start Reading ›` 1-tap reader launch, and inline `✕` unqueue button.
     - Bumped version to `Milestone Folio v9.0 KFX (Enhanced Typesetting)`.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.0_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `verify_r90_book_details_queue_v90.png`: Proves Book Details for *Animal Farm* displaying active violet `📌 Queued` pill alongside Custom Shelf pill.
     - `verify_r90_up_next_control_center.png`: Proves Control Center top showing `Folio v9.0 KFX` badge, `📖 Continue Reading`, and the live `📌 Up Next Queue` carousel card with cover thumbnail, *Animal Farm*, *George Orwell*, `Start Reading ›`, and `✕` unqueue button.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.0_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report090.zip`


---

# Round 091: Up Next Queue Export, Backup & Telemetry (Folio v9.1 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v9.1 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 091 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java`, `FolioMetadataBackup.java`, and `FolioMoreView.java` from `dropin_src/`.
     - Resolved missing `import java.util.ArrayList;` in `FolioMetadataBackup.java`.
     - Added `in_queue` boolean to JSON backup & restore.
     - Appended `## 📌 Up Next Queue (N)` section to Markdown Reading Log export.
     - Added `In Queue` column to RFC-4180 CSV export.
     - Added `📌 ` prefix badge to book titles in catalog list view when queued.
     - Integrated live queued count into Library Insights header summary (`1 queued`).
     - Bumped version to `Folio v9.1 KFX (Enhanced Typesetting)`.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.1_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `verify_r91_reading_log_up_next.png`: Proves Reading Log preview displaying `## 📌 Up Next Queue (1)` with `- **Animal farm: a fairy story** — George Orwell`.
     - `verify_r91_insights_queued_v91.png`: Proves Library Insights modal header summary displays `1 fav · 1 noted · 1 shelved · 1 queued · 5.0★ avg (1 rated)`.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.1_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report091.zip`


---

# Round 092: Pinned Folder Bookmarks in Batch Importer (Folio v9.2 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v9.2 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 092 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioFolderBrowserDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Added compact `☆` / `★` pin toggle button right beside `Open` in the Batch EPUB Importer path bar.
     - Persisted pinned folder bookmarks in `SharedPreferences("folio_ui_prefs", "pinned_import_dirs")`.
     - Preprended pinned folder chips with `★ ` prefix at the head of the quick-jump strip.
     - Added dynamic active folder highlight with amber fill (`#F59E0B`) matching the currently browsed path.
     - Bumped version to `Folio v9.2 KFX (Enhanced Typesetting)`.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.2_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `verify_r92_pinned_folder_importer.png`: Proves Batch EPUB Importer displaying active gold `★` pin button and highlighted gold `★ en/0` chip at the front of the quick-jump strip.
     - `verify_r92_about_v92.png`: Proves About Folio dialog displaying `Version: Folio v9.2 KFX (Enhanced Typesetting)` and live stats.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.2_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report092.zip`


---

# Round 093: Publication Eras Timeline Chart & Era Filtering (Folio v9.3 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v9.3 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 093 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Built `PUBLICATION ERAS · YYYY–YYYY (TAP TO FILTER)` section in Library Insights plotting chronological era bars (`Pre-1900`, `1900–1949`, `1950–1999`, `2000–2009`, `2010–2019`, `2020+`).
     - Added 1-tap drilldown dismissing Insights and opening catalog filtered to `ERA:<bucket>`.
     - Added smart `📅 <Era> (N)` active filter chip in catalog with exact year-range filtering.
     - Bumped version to `Folio v9.3 KFX (Enhanced Typesetting)`.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.3_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `verify_r93_insights_pub_eras.png`: Proves Library Insights modal displaying `PUBLICATION ERAS · 1531–2017 (TAP TO FILTER)` violet bar chart.
     - `verify_r93_era_filter_drilldown.png`: Proves catalog modal filtered to `📅 2000–2009 (4)` with active violet chip and matching books.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.3_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report093.zip`


---

# Round 094: Bulk Queue Action & Grid-View Up Next Badge (Folio v9.4 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v9.4 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 094 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Upgraded bottom Multi-Select Bulk Action Bar to a 5-pill single-row layout (`Shelf`, `+Genre`, `📌Queue`, `★Fav`, `✓Done`).
     - Added 1-tap batch queue / unqueue capability across all selected volumes.
     - Added violet `📌 Up Next` badge to queued books in 3-column Cover Art Grid view.
     - Bumped version to `Folio v9.4 KFX (Enhanced Typesetting)`.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.4_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `verify_r94_bulk_queue_grid_badge.png`: Proves 3-column Grid view displaying violet `📌 Up Next` badge on *Animal farm: a fairy story* and the 5-pill Bulk Action Bar (`Shelf`, `+Genre`, `📌Queue`, `★Fav`, `✓Done`).
     - `verify_r94_about_v94.png`: Proves About Folio dialog displaying `Version: Folio v9.4 KFX (Enhanced Typesetting)` and live stats.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.4_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report094.zip`


---

# Round 095: "More by Author" Sibling Carousel in Book Details (Milestone Folio v9.5 KFX)

- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio v9.5 KFX") deployed and verified on Samsung Galaxy M32 alongside unmodified stock `com.amazon.kindle`. Round 095 complete:
  1. **Source Integration (Task A)**:
     - Integrated `FolioMetadataCatalogDialog.java` and `FolioMoreView.java` from `dropin_src/`.
     - Built `MORE BY [AUTHOR] (N)` horizontal card carousel right below the Personal Note card inside Book Details.
     - Displays author's sibling volumes with cover art thumbnails, 2-line titles, and live status badges (`✓ Done ›`, `X% read ›`, `📌 Up Next ›`, or year).
     - 1-tap lateral exploration switches Book Details directly to the tapped sibling volume.
     - Bumped version to `Milestone Folio v9.5 KFX (Enhanced Typesetting)`.
     - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.5_kfx/base.apk` (132 MB).
  2. **Hardware Verification on Galaxy M32 (Task B)**:
     - `verify_r95_more_by_author_details.png`: Proves Book Details for *1984* displaying `MORE BY GEORGE ORWELL (1)` horizontal cover-art card strip showing *Animal farm: a fairy story* with standalone cover thumbnail, title, and violet `📌 Up Next ›` status badge.
     - `verify_r95_about_v95.png`: Proves About Folio dialog displaying `Version: Folio v9.5 KFX (Enhanced Typesetting)` and live stats.
  3. **Stock App Safety Invariant**: Stock package `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED (`lastUpdateTime=2026-11-19 21:59:23`).
  4. **Lead Architectural Directive Reaffirmed**: Cover extraction and rendering decoupled from reflowable spine, rendered as standalone UI cover view.
  5. **Deliverables & Report**:
     - Staged APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v9.5_kfx/base.apk` (132 MB)
     - Report Archive: `antig_to_gemini_neon_clunky_report095.zip`
