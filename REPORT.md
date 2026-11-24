# Round 115 Deliverables Report — Milestone Folio v11.5 KFX (Reading Notes Studio: + Add Note, Share & Export .md)
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Milestone Folio v11.5 KFX (Enhanced Typesetting)` installed on device at `2026-11-24 03:36:42`.

---

## 1. Task A: Source Integration & Milestone Release Staging
1. **Drop-In Source Integration (`FolioMoreView.java`)**:
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`:
     - **`+ Add Note` Header Action & Book Picker (`showAddNoteBookPicker`)**: Added a soft-violet `+ Add Note` pill right in the `📝 Reading Notes (N)` header. Tapping it opens a live-searchable picker across all cataloged books with `✏️ Edit` / `+ Note` status tags, allowing instant note addition/editing directly from the notebook.
     - **3-Pill Export & Share Toolbar**: Added compact toolbar above notes list:
       * **`📋 Copy All`** (amber `#F59E0B` on `#2B1F0A`): Copies formatted Markdown notes directly to the Android clipboard.
       * **`↗ Share`** (sky-blue `#38BDF8` on `#082F49`): Dispatches native Android `Intent.ACTION_SEND` with plain-text Markdown.
       * **`💾 Export .md`** (emerald `#10B981` on `#0A2B1D`): Atomically writes `/sdcard/kindle/backups/folio_reading_notes.md`.
     - **Markdown Builder (`buildNotesMarkdown`)**: Generates structured Markdown including book titles, authors, star ratings (`★★★★★`), and formatted quote blocks (`> ...`).
     - **Milestone Version Bump**: Updated Control Center header badge and `About Folio` dialog string to `Folio v11.5 KFX (Enhanced Typesetting)`.
     - Invariants: `MetadataEnricher.java` and `OpenCatalogView.java` strictly preserved and untouched.

2. **Compilation, Packaging & Deployment**:
   - Recompiled `classes9.dex` (395,068 bytes) via `d8`.
   - Rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, and signed with `apksigner`.
   - Streamed APK install to physical Samsung Galaxy M32 (`lastUpdateTime=2026-11-24 03:36:42`).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v11.5_kfx/base.apk` (132 MB).

---

## 2. Task B: Hardware Verification on Physical Galaxy M32
Verified on physical Samsung Galaxy M32 hardware:

1. **`verify_r115_notes_studio.png` (156,470 bytes)**:
   - Captured **📝 Reading Notes Notebook** opened from Control Center.
   - Shows the `+ Add Note` header pill alongside `📝 Reading Notes (1)`.
   - Shows the 3-pill export toolbar: `📋 Copy All`, `↗ Share`, `💾 Export .md`.
   - Shows saved note card (*1984* by George Orwell · ★ 5/5 · 🏷 Classics: `> Essential dystopian classic - reread Part 3`) with inline `📋 Copy` and `✏️ Edit` buttons.
2. **Exported Markdown File Verified**:
   - `/sdcard/kindle/backups/folio_reading_notes.md` verified on device storage:
     * Size: **114 bytes**.
     * Content:
       ```markdown
       # Folio Reading Notes

       ## 1984 — George Orwell (★★★★★)
       > Essential dystopian classic - reread Part 3
       ```
3. **`verify_r115_about_v115.png` (354,529 bytes)**:
   - About Folio dialog confirming:
     * `Version: Folio v11.5 KFX (Enhanced Typesetting)`
     * `Hardware: TrebleDroid with GApps · Android 13 (arm64-v8a)`
     * `Up Next & Goal: 2 queued · 1/5 annual goal`
     * Pure-Rust Engine (`libkfxconv.so`) telemetry and live library stats.
4. **Stock Kindle Package Invariant**:
   - `adb shell dumpsys package com.amazon.kindle | grep -E "versionName|lastUpdateTime"`
   - `versionName=8.160.1.0(2.0.124621.0)`
   - `lastUpdateTime=2026-11-19 21:59:23` (**100% UNTOUCHED and READ-ONLY**).

---

## 3. Project Lead Architectural Directive on Cover Architecture
Reiterating the project lead directive verbatim:

> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
