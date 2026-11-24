# Round 128 Deliverables Report — Folio v12.8 KFX (Web Browser Recent Downloads History Drawer)
**Environment**: Samsung Galaxy M30 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v12.8 KFX` installed on device at `2026-11-24 06:08:36`.

---

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 1. Task A: Source Integration & Release Staging
1. **Drop-In Source Integration**:
   - **`FolioWebBrowserDialog.java` (Downloads Chip & History Drawer)**:
     * Added a persistent `Downloads` chip using `FolioIcons.IMPORT` directly beside `Pin` and `EPUBs` in the bookmarks bar (`makeChipWithIcon(ctx, d, FolioIcons.IMPORT, dlLabel, TEXT_PRIMARY, BG_CONTROL, BORDER)`).
     * Implemented `recordRecentImport(ctx, title, author, kfxPath)` to persist converted EPUB entries in `SharedPreferences("folio_ui_prefs", "web_recent_imports")` (up to 15 recent entries) with timestamp.
     * Implemented `showRecentDownloadsDialog(ctx, parentDlg, onRefresh)` featuring:
       - Bookerly serif title (`Web Downloads (N)`).
       - Amazon Ember sans subtitle and entry metadata (`Title`, `Author · Timestamp`).
       - 1-tap `Open ›` button launching `ExternalEpubActivity.launchKfxReader` directly.
       - Clean `Clear History` neutral button and `Close` positive button.
       - Invokes `FolioTheme.applyToDialog(ctx, dlDlgRef[0])` post-`.show()` for Kindle font and button styling.
   - **`FolioMoreView.java` & `FolioMetadataCatalogDialog.java`**:
     * Preserved clean zero-emoji editorial elements (`Edit` shelf pill, pure text search chips, `+Queue` bulk pill).
     * Guaranteed `FolioTheme.applyToDialog` runs post-`.show()` on all dialogs.
     * Bumped badge to **`Folio v12.8 KFX`** and updated `About Folio` version string.

2. **Compilation, Packaging & Deployment**:
   - Recompiled `classes9.dex` (454,068 bytes) via `javac` and Android `d8`.
   - Rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, signed with debug keystore.
   - Streamed APK install to physical Samsung Galaxy M30 (`lastUpdateTime=2026-11-24 06:08:36`).
   - Verified zero fatal crashes in `logcat` (`FATAL EXCEPTION` check clean).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v12.8_kfx/base.apk` (132 MB, SHA256: `3c1d427fc53b639a0a1bab2b5b96d53e9c46fec75e67d24c4e710f8da4cb7e83`).

---

## 2. Task B: Hardware Verification on Physical Samsung Galaxy M30
Verified on physical Samsung Galaxy M30 hardware (`32001776e8e82623`):

1. **`verify_r128_web_browser_downloads_bar.png` (251,966 bytes)**:
   - Captured the **Web Book Browser** (`Folio v12.8 KFX`) showing:
     * Full web browser interface with URL search header (`z-lib.by`).
     * Clean monochrome bookmarks bar displaying `Pin`, `EPUBs`, and the new **`Downloads`** chip alongside default bookmarks (`Standard Ebooks`, `Gutenberg`).
2. **`verify_r128_web_downloads_drawer.png` (205,678 bytes)**:
   - Captured the **Web Downloads** drawer showing:
     * Bookerly serif heading (`Web Downloads (0)`).
     * Amazon Ember sans subtitle: `Downloaded EPUBs are automatically converted to KFX and listed here.`
     * Empty state container: `No web downloads yet. Browse a catalog and tap an EPUB link or use the EPUBs scanner.`
     * Clean footer action button (`Close`). Zero emojis.
3. **Safety Invariants**:
   - Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED and READ-ONLY (`lastUpdateTime=2026-11-19 21:59:23`).
   - Zero crashes logged for `com.amazon.kindle.epub`.

---

## 3. GitHub Repository Synchronization
- **Repository Remote**: `https://github.com/BerKetKet/clunky.git`
- **Branch**: `master` (tracking `origin/master`)
- **Commit**: Recorded upon push in Task C.
