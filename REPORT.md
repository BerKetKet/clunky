# Round 135 Deliverables Report — Azath v14.1 KFX (Editorial Serif/Sans Typography & Unified Obsidian-Ivory-Crimson Design System)
**Environment**: Samsung Galaxy M30 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Azath Version**: `Azath v14.1 KFX` (`com.azath.reader`) installed on device at `2026-11-25 00:08:51`.

---

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 1. Task A: Source Integration, Build, Deploy & Staging (`com.azath.reader`)
1. **Editorial Typography & Design System Unification (Inspired by Play Store Folio `ct.reader`)**:
   - **`FolioTheme.java`**: Updated display headings (`>= 17sp`) to airy Regular-weight `Bookerly` (`bookerlyReg`) with `-0.018f` letter spacing (`>= 19.5sp`) paired with crisp `Amazon Ember` UI controls (`< 17sp`). Added unified Obsidian-Ivory-Crimson modal frames (`#121010` fill, 18dp corner radius, `#2A2423` warm hairline border, `#E05A47` / `#8E867E` / `#C4BDB5` buttons).
   - **All 6 Modules Unified** (`FolioTheme.java`, `FolioMoreView.java`, `FolioMetadataCatalogDialog.java`, `FolioFolderBrowserDialog.java`, `FolioWebBrowserDialog.java`, `FolioMetadataBackup.java`): Swept stray cold-slate/navy/amber/emerald/purple hexes into Canvas `#0A0909`, Modal `#121010`, Surface `#181515`, Control `#211C1B`, Hairline `#2A2423`, Text `#F2EFE9` / `#C4BDB5` / `#8E867E`, Accent `#E05A47`, Soft Pill `#2A1816` / `#5C2D26` / `#F5B8AE`.
   - **Carryover Fix Preserved**: Restored the Round 132/133 `Reading Time (WPM)` snapshot summary line (`snapshotText`) and `Azath Library Insights` header in `FolioMoreView.java`.
   - **Control Center Polish**: Regular `26sp` Bookerly `Control Center` heading (`-0.02f` tracking), 12dp button corners, and ember-crimson pill badge `v14.1 KFX`.
2. **Compilation, Deployment & Staging**:
   - Copied all 6 drop-in Java files to `07_APK_EPUB_INGESTION_MOD/mod_src/real/`.
   - Updated `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` staging path to `azath_v14.1_kfx`.
   - Recompiled `classes9.dex` (`460,456` bytes), rebuilt APK, aligned via `zipalign`, signed with apksigner, deployed to Samsung Galaxy M30 (`com.azath.reader`, `lastUpdateTime=2026-11-25 00:08:51`).
   - Verified zero crashes or fatal errors in `logcat`.
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/azath_v14.1_kfx/base.apk` (`137,856,708` bytes ~132 MB, SHA256: `e20d524c13e2c97b975317ab462765f843123838ef73f74b149782172d2fbaa0`).

---

## 2. Task B: Hardware Verification Screenshots (Samsung Galaxy M30)
1. **`verify_r135_azath_control_center.png` (229,415 bytes)**:
   - Captured **Azath** More tab displaying the Regular `Bookerly` `Control Center` header (`-0.02f` tracking), ember-crimson `v14.1 KFX` pill, `Edit` / `See All (5) ›` / `9% read · Resume ›` / `+ Pick ›` accents, restored progress metrics (`26 BOOKS`, `5 READING`), `#181515` warm obsidian cards, and bespoke ivory+crimson icons.
2. **`verify_r135_azath_modal_uniformity.png` (305,157 bytes)**:
   - Captured **Library Metadata & Genres (24)** modal displaying the 18dp rounded Obsidian `#121010` dialog frame with `#2A2423` border, muted `AZATH` eyebrow, Regular Bookerly modal heading, ember-crimson `#E05A47` active filter chips (`All (24)`, `Sync & Enrich`), crisp Amazon Ember cards, and `#E05A47` `CLOSE` button.
3. **Safety Invariant**:
   - Stock `com.amazon.kindle` (`8.160.1.0`) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).
   - Zero `FATAL EXCEPTION` entries logged in `logcat`.
