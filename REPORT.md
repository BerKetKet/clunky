# Round 129 Deliverables Report — Folio v12.9 KFX (Kindle-Native Book Details, Reading-Time Estimate & About Folio Theme)
**Environment**: Samsung Galaxy M30 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v12.9 KFX` installed on device at `2026-11-24 06:19:06`.

---

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 1. Task A: Source Integration & Release Staging
1. **Drop-In Source Integration**:
   - **`FolioMetadataCatalogDialog.java` (Reading-Time Estimate & Kindle Dialog Styling)**:
     * Computed offline reading-time estimate from KFX payload size: `int estMins = Math.max(15, Math.min(2400, (int) Math.round((bytes / 1024.0) * 0.34)))` and appended inline to the `Format:` line (e.g. `Format: Enhanced KFX · 1.2 MB · ~7h 8m read`).
     * Wired `FolioTheme.applyToDialog(ctx, detailDlgRef[0])` post-`.show()` on `showBookDetailDialog` so dialog view hierarchies load Bookerly headings and Amazon Ember controls, and action buttons (`Read Now` in `#4DA6FF`, `Edit Metadata` in `#C4CBD2`, `Close` in `#8E959C`) match native Kindle design.
   - **`FolioMoreView.java` (About Folio Theming & Version Bump)**:
     * Wired `FolioTheme.applyToDialog(ctx, aboutDlg)` post-`.show()` on the `About Folio` dialog.
     * Preserved post-`.show()` placement for `FolioTheme.applyToDialog` across all Control Center dialogs (`goalDlgRef[0]`, `dlgRef[0]`, `storDlgRef[0]`).
     * Bumped badge to **`Folio v12.9 KFX`** and updated `About Folio` version string to `Folio v12.9 KFX (Enhanced Typesetting)`.

2. **Compilation, Packaging & Deployment**:
   - Recompiled `classes9.dex` (454,336 bytes) via `javac` and Android `d8`.
   - Rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, signed with debug keystore.
   - Streamed APK install to physical Samsung Galaxy M30 (`lastUpdateTime=2026-11-24 06:19:06`).
   - Verified zero fatal crashes in `logcat` (`FATAL EXCEPTION` check clean).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v12.9_kfx/base.apk` (132 MB, SHA256: `5cae24952cadd7e703085bc3334260de8fa5f8a0ec8990750ee0b983409bb3bc`).

---

## 2. Task B: Hardware Verification on Physical Samsung Galaxy M30
Verified on physical Samsung Galaxy M30 hardware (`32001776e8e82623`):

1. **`verify_r129_book_details_kindle.png` (268,709 bytes)**:
   - Captured **Book Details (`ⓘ`)** for *1984* showing:
     * Bookerly serif title (`1984`).
     * Amazon Ember metadata including the new reading-time estimate: `Format: Enhanced KFX · 1.2 MB · ~7h 8m read`.
     * Clean genre tags, synopsis, rating, shelf assignment (`Classics`), and notes preview.
     * Styled Kindle footer action buttons: `READ NOW` in `#4DA6FF` Ember bold, `EDIT METADATA` in `#C4CBD2`, and `CLOSE` in `#8E959C`. Zero emojis.
2. **`verify_r129_about_v129.png` (338,632 bytes)**:
   - Captured **About Folio** modal showing:
     * Bookerly serif heading (`About Folio`).
     * Version line: `Version: Folio v12.9 KFX (Enhanced Typesetting)`.
     * Hardware info: `TrebleDroid with GApps · Android 13 (arm64-v8a)`.
     * Pure-Rust Engine feature inventory and live library & storage stats.
     * Styled footer action buttons (`CLOSE`, `CLEAR TEMP CACHE`).
3. **Safety Invariants**:
   - Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED and READ-ONLY (`lastUpdateTime=2026-11-19 21:59:23`).
   - Zero crashes logged for `com.amazon.kindle.epub`.

---

## 3. GitHub Repository Synchronization
- **Repository Remote**: `https://github.com/BerKetKet/clunky.git`
- **Branch**: `master` (tracking `origin/master`)
- **Commit**: Recorded upon push in Task C.
