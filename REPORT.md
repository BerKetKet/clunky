# Round 127 Deliverables Report — Folio v12.7 KFX (Zero-Emoji Editorial Modal Polish & GitHub Sync)
**Environment**: Samsung Galaxy M30 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Hardware Correction**: Physical test hardware is confirmed as **Samsung Galaxy M30** (NOT M32). All subsequent rounds and logs strictly reference Galaxy M30.  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v12.7 KFX` installed on device at `2026-11-24 05:51:25`.

---

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 1. Task A: Source Integration & Release Staging
1. **Drop-In Source Integration & Zero-Emoji Editorial Modal Polish**:
   - **`FolioMoreView.java`**:
     * Performed a complete zero-emoji editorial audit across all Control Center dialogs and modals (`Reading Goal`, `Tonight's Pick`, `Up Next Queue`, `Reading Notes`, `Manage Shelves`, `Storage & Duplicates`, `Saved Backups & Exports`, `Authors Directory`, `Book Details`).
     * Replaced shelf rename button `✏️` with a clean Kindle-native `Edit` pill (`#162330` fill, `#2C5278` stroke, `ACCENT_TEAL`).
     * Cleaned search history chips, removing leftover `🕒` prefix.
     * Guaranteed all headings render in Bookerly serif (`>= 17sp`) and all body/pills render in Amazon Ember sans (`< 17sp`).
     * Bumped Control Center badge and `About Folio` dialog to **`Folio v12.7 KFX`**.
   - **`FolioMetadataCatalogDialog.java`**:
     * Cleaned bulk action pill from `📌Queue` to clean `+Queue` (`#162330` fill, `#4DA6FF` text, `#2C5278` border).
     * Applied Bookerly/Ember typography and `#4DA6FF` Kindle blue accents across catalog views and edit metadata dialogs.
   - **`FolioTheme.java` Dialog Button Theming**:
     * Maintained post-`.show()` invocation order for `FolioTheme.applyToDialog` so `AlertDialog` positive (`#4DA6FF`), negative (`#8E959C`), and neutral action buttons are styled consistently.

2. **Compilation, Packaging & Deployment**:
   - Recompiled `classes9.dex` (449,732 bytes) via `javac` and Android `d8`.
   - Rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, signed with debug keystore.
   - Streamed APK install to physical Samsung Galaxy M30 (`lastUpdateTime=2026-11-24 05:51:25`).
   - Verified zero fatal crashes in `logcat` (`FATAL EXCEPTION` check clean).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v12.7_kfx/base.apk` (132 MB, SHA256: `cb9a79cd7abf19009c710881f2bc7366a8e7001541a442b72569b7444f64ebb7`).

---

## 2. Task B: Hardware Verification on Physical Samsung Galaxy M30
Verified on physical Samsung Galaxy M30 hardware (`32001776e8e82623`):

1. **`verify_r127_reading_goal_clean.png` (196,000 bytes)**:
   - Captured the **2026 Reading Goal** modal showing:
     * Bookerly serif heading (`2026 Reading Goal`).
     * Clean Amazon Ember sans on all metrics: `1 of 5 books finished (20%)`, `37 DAYS LEFT`, `4 TO GO`, `1 / 9d NEEDED PACE`, `COMPLETED BOOKS (1)`, `✓ 2026-11-23`.
     * Clean target selector buttons (`5`, `12`, `24`, `36`, `52`).
     * Editorial footer buttons: `Save Goal` in `#4DA6FF` Ember bold, `Cancel` in `#8E959C` muted sans. Zero emojis.
2. **`verify_r127_manage_shelves_clean.png` (207,504 bytes)**:
   - Captured the **Manage Shelves** modal showing:
     * Bookerly serif heading (`Manage Shelves`).
     * Clean Kindle controls: `+ New Shelf`, `+ Books`, `Edit` (replaced `✏️`), `✕`.
     * Shelves displayed (`Classics`, `Philosophy`) with book count indicators.
     * Clean footer action buttons: `Browse Catalog` and `Close`. Zero emojis.
3. **Safety Invariants**:
   - Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED and READ-ONLY (`lastUpdateTime=2026-11-19 21:59:23`).
   - Zero crashes logged for `com.amazon.kindle.epub`.

---

## 3. GitHub Repository Synchronization
- **Repository Remote**: `https://github.com/BerKetKet/clunky.git`
- **Branch**: `master` (tracking `origin/master`)
- **Commit**: `e58953e` ("Round 127: Zero-Emoji Editorial Modal Polish & GitHub Sync (Folio v12.7 KFX)") pushed to `origin/master`.
