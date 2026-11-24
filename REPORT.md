# Round 111 Deliverables Report — Folio v11.1 KFX (Tonight's Pick Pool Switcher & 1-Tap Queue)
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v11.1 KFX (Enhanced Typesetting)` installed on device at `2026-11-24 03:04:09`.

---

## 1. Task A: Source Integration & Release Staging
1. **Drop-In Source Integration (`FolioMoreView.java`)**:
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`:
     - **Interactive Pool Scope Switcher**: Added 3 live-counted scope pills (`Unread (N)`, `📌 Queue (N)`, `All (N)`) beneath the `🎲 Tonight's Pick` header, enabling rolling strictly from unread books, from the `📌 Up Next` queue, or from the whole library (displaying reading progress like `25% read` or `Finished`).
     - **1-Tap `📌 Queue` / `📌 Queued` Toggle**: Added third action button inline with `🎲 Another` and `ⓘ Details`. Bookmarks candidate books directly into `📌 Up Next` queue while rolling, instantly updating button style/label to warm amber `📌 Queued` and dynamically incrementing the `📌 Queue (N)` scope pill counter.
     - **Version Bump**: Updated Control Center header badge and `About Folio` dialog string to `Folio v11.1 KFX (Enhanced Typesetting)`.
     - Invariants: `MetadataEnricher.java` and `OpenCatalogView.java` strictly preserved and untouched.

2. **Compilation, Packaging & Deployment**:
   - Recompiled `classes9.dex` (374,876 bytes) via `d8`.
   - Rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, and signed with `apksigner`.
   - Streamed APK install to physical Samsung Galaxy M32 (`lastUpdateTime=2026-11-24 03:04:09`).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v11.1_kfx/base.apk` (132 MB).

---

## 2. Task B: Hardware Verification on Physical Galaxy M32
Verified on physical Samsung Galaxy M32 hardware:

1. **`verify_r111_tonights_pick_pools.png` (282,846 bytes)**:
   - Captured **🎲 Tonight's Pick** modal launched from header `🎲` icon pill.
   - Shows all 3 scope pills: `Unread (26)`, `📌 Queue (2)`, `All (32)`.
   - Shows picked book (*Choke* by Chuck Palahniuk · Bloomsbury Publishing Plc) with genre tags.
   - Shows all 3 action buttons: `🎲 Another`, `📌 Queued` (toggled to amber `#F59E0B`), and `ⓘ Details`.
   - Verifies the `📌 Queue (N)` scope counter incremented dynamically from 1 to 2 upon tapping.
2. **`verify_r111_about_v111.png` (353,732 bytes)**:
   - About Folio dialog confirming:
     * `Version: Folio v11.1 KFX (Enhanced Typesetting)`
     * `Hardware: TrebleDroid with GApps · Android 13 (arm64-v8a)`
     * `Up Next & Goal: 2 queued · 1/5 annual goal`
     * Pure-Rust Engine (`libkfxconv.so`) telemetry and live library stats.
3. **Stock Kindle Package Invariant**:
   - `adb shell dumpsys package com.amazon.kindle | grep -E "versionName|lastUpdateTime"`
   - `versionName=8.160.1.0(2.0.124621.0)`
   - `lastUpdateTime=2026-11-19 21:59:23` (**100% UNTOUCHED and READ-ONLY**).

---

## 3. Project Lead Architectural Directive on Cover Architecture
Reiterating the project lead directive verbatim:

> *"The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."*
