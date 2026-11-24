# Round 126 Deliverables Report — Folio v12.6 KFX (Strict Bookerly/Ember Hierarchy & Modal-Wide Theme Unification)
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: `Folio v12.6 KFX (Enhanced Typesetting)` installed on device at `2026-11-24 05:33:49`.

---

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 1. Task A: Source Integration & Release Staging
1. **Drop-In Source Integration**:
   - **`FolioTheme.java` (Strict Typography Routing & Dialog Theming)**:
     * Refined font routing logic so **Bookerly** applies strictly to display headings (`>= 17sp`), while all subheadings, eyebrows, pills, chips, buttons, and body text (`< 17sp`) render in crisp **Amazon Ember** sans-serif matching native Kindle typography hierarchy.
     * Implemented `FolioTheme.applyToDialog(ctx, dlg)` to recursively style modal view hierarchies with Kindle fonts and style AlertDialog action buttons (`#4DA6FF` Ember bold Positive, `#8E959C` Ember regular Negative, `#C4CBD2` Neutral) in a single unified call.
   - **`FolioFolderBrowserDialog.java` (Active Pin Button Color Polish)**:
     * Replaced the last leftover amber/brown border (`#2D2312` fill, `#B45309` border) on the active pinned-folder `★` button (`btnPinDir`) with Kindle blue-slate (`#162330` fill, `#2C5278` border).
   - **`FolioMoreView.java` & `FolioMetadataCatalogDialog.java` (Modal-Wide Theming)**:
     * Wired `FolioTheme.applyToDialog(...)` across Control Center sub-modals (Reading Goal, Tonight's Pick, Queue Reorder, Reading Notes, Manage Shelves, Storage & Duplicates, Quick Search) and Catalog sub-modals (Edit Metadata, Authors Directory).
     * Bumped header badge and `About Folio` dialog to **`Folio v12.6 KFX (Enhanced Typesetting)`**.

2. **Compilation, Packaging & Deployment**:
   - Recompiled `classes9.dex` (450,364 bytes) via `javac` and Android `d8`.
   - Rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, signed with debug keystore.
   - Streamed APK install to physical Samsung Galaxy M32 (`lastUpdateTime=2026-11-24 05:33:49`).
   - Verified zero fatal crashes in `logcat` (`FATAL EXCEPTION` check clean).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v12.6_kfx/base.apk` (132 MB).

---

## 2. Task B: Hardware Verification on Physical Galaxy M32
Verified on physical Samsung Galaxy M32 hardware:

1. **`verify_r126_importer_ember_and_pin.png` (200,897 bytes)**:
   - Captured the **Import EPUBs from Storage** modal showing:
     * Bookerly heading (`Import EPUBs from Storage`).
     * Amazon Ember sans on eyebrows and pills (`BATCH EPUB IMPORTER`, `Sort: A–Z`, `Hide Imported: OFF`).
     * Active `★` pinned folder button now styled in Kindle blue-slate `#162330` fill and `#2C5278` border with `#4DA6FF` star (verified 0 brown/amber pixels).
2. **`verify_r126_modal_bookerly_ember.png` (200,543 bytes)**:
   - Captured the **2026 Reading Goal** modal showing:
     * Unified Bookerly heading and Amazon Ember body typography.
     * Styled AlertDialog footer buttons: `Save Goal` in calm Kindle blue `#4DA6FF` Ember bold (1,200 verified `#4DA6FF` pixels) and `Cancel` in `#8E959C` muted text (641 verified pixels).
3. **Safety Invariants**:
   - Stock `com.amazon.kindle` (v8.160.1.0) remains 100% UNTOUCHED and READ-ONLY (`lastUpdateTime=2026-11-19 21:59:23`).
   - Zero crashes logged for `com.amazon.kindle.epub`.

---

## 3. GitHub Repository Synchronization
- **Repository Remote**: `https://github.com/BerKetKet/clunky.git`
- **Branch**: `master` (tracking `origin/master`)
- **Status**: Commits synced and pushed to remote.
