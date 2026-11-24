# Round 136 Deliverables Report — Azath v14.2 KFX (Bottom Nav Ember-Crimson Alignment, Archival Monogram Covers & Tonal Catalog Polish)
**Environment**: Samsung Galaxy M30 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Azath Version**: `Azath v14.2 KFX` (`com.azath.reader`) installed on device at `2026-11-25 00:24:20`.

---

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 1. Task A: Drop-In Integration, Bottom Nav Ember-Crimson Resource + DEX Patch, Build & Stage
1. **Drop-In Integration**:
   - Copied `FolioMoreView.java` (`v14.2 KFX`, preserved `Reading Time (WPM)` snapshot summary + updated title to `Azath Library Insights` + shared `calibrateClick`) and `FolioMetadataCatalogDialog.java` (Archival Spine Monogram covers + Tonal `#2A1816`/`#5C2D26`/`#F5B8AE` active filter chips + `#8E867E` muted genre hierarchy + `#F2EFE9` ivory titles) into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`.
2. **Bottom Navigation Bar Active Tint (`#2491FF` -> `#E05A47`)**:
   - Pixel inspection of `More` tab initially identified stock `#2491ff`.
   - Patched XML tokens (`#ff2491ff -> #ffe05a47` across `values-night/colors.xml`, `values/colors.xml`, `values/styles.xml`, and `drawable/bottom_bar_label_color_dark.xml`).
   - Hardware pixel sampling revealed Jetpack Compose (`compose_content`) reads dark palette color `functionalCorePrimary` directly from compiled bytecode constant `0xff2491ffL` in `classes2.dex` (`Lib/b.<clinit>`).
   - Created idempotent binary DEX patcher `07_APK_EPUB_INGESTION_MOD/tools/patch_classes2_azath.py` (`0xff2491ffL -> 0xffe05a47L` at `0x4d6f50` with SHA1/Adler32 update) and persisted both DEX + XML patches into `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh`.
3. **Device Storage Cleanup & Deployment**:
   - Uninstalled superseded mod package `com.amazon.kindle.epub` and cleared old scratch files in `/data/local/tmp/` with explicit user approval (`1.6G` free on `/data`).
   - Recompiled `classes9.dex` (`460,968` bytes), patched `classes2.dex` (`8,413,156` bytes), rebuilt APK, aligned via `zipalign`, signed with apksigner, and deployed `com.azath.reader` (`lastUpdateTime=2026-11-25 00:24:20`) with zero `FATAL EXCEPTION` entries in `logcat`.
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/azath_v14.2_kfx/base.apk` (`137,856,708` bytes ~132 MB, SHA256: `8cb1e59a65dec11292b5efed8a289d31150cf2b4e4ff92e16e9d93cc13e11a9a`).

---

## 2. Task B: Hardware Verification Screenshots (Samsung Galaxy M30)
1. **`verify_r136_azath_control_center.png` (229,874 bytes)**:
   - Captured **Azath** Control Center showing **`v14.2 KFX`** badge, Regular Bookerly heading, restored library metrics (`26 BOOKS`, `5 READING`), bespoke icons, and the bottom navigation **`More` tab** verified via pixel sampling at exact **`#e05a47` Ember Crimson** (zero remaining stock `#2491ff` blue).
2. **`verify_r136_azath_catalog_polish.png` (309,600 bytes)**:
   - Captured **Library Metadata & Genres (24)** showing tonal `#2A1816`/`#5C2D26`/`#F5B8AE` active `All (24)` chip beside single primary `#E05A47` `Sync & Enrich` CTA, rounded `#211C1B` Archival Bookerly Monogram covers (`B` for *Brave New World*, `I` for *Island*) with `#5C2D26` spine rules, warm `#F2EFE9` ivory titles, and `#8E867E` muted genre tags.
3. **Safety Invariant**:
   - Stock `com.amazon.kindle` (`8.160.1.0`) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).
   - Zero crashes logged in `logcat`.
