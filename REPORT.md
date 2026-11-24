# Round 134 Deliverables Report — Major Milestone Azath v14.0 KFX (Malazan Rebrand, Bespoke Generated Icon Set & Play Store Folio Reference Study)
**Environment**: Samsung Galaxy M30 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Azath Version**: `Azath v14.0 KFX` (`com.azath.reader`, `userId=10503`) installed on device at `2026-11-24 23:55:15`.

---

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 1. Task A: Malazan Rebrand (`com.azath.reader`), Bespoke Icons, Build & Release Staging
1. **Drop-In Integration & Companion Adjustments**:
   - Integrated all 6 drop-in Java modules (`FolioIcons.java`, `FolioMoreView.java`, `FolioMetadataCatalogDialog.java`, `FolioFolderBrowserDialog.java`, `FolioWebBrowserDialog.java`, `FolioMetadataBackup.java`) into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`.
   - Preserved Round 132/133 fixes in `FolioMoreView.java` (`Reading Time (WPM)` line in `snapshotText` and shared `calibrateClick` on `rtHdr`/`rtRow`) and updated the non-curated fallback heading in `FolioMetadataBackup.java` to `# Azath Reading Log (`.
   - Updated `OpenCatalogView.java` (`hideSearchKindleRecursive` adds `"Search Azath"` alongside `"Search Folio"`/`"Search Kindle"`, and home brand header updated to `"Azath"`) and added `/data/data/com.azath.reader/lib/libkfxconv.so` primary fallback in `EpubTranscoder.java`.
   - Copied all 16 bespoke 96x96 transparent ivory+crimson PNGs from `dropin_assets/azath_icons/` into `07_APK_EPUB_INGESTION_MOD/assets/azath_icons/` and wired them into `rebuild_fast.sh` -> `/tmp/kindle_clone_build/base_dec/assets/azath_icons/`.
2. **Clean Package Rename (`com.amazon.kindle.epub` -> `com.azath.reader`)**:
   - Updated `AndroidManifest.xml` package name, permissions, `uses-permission`, and providers (`com.amazon.kindle.epub.*` -> `com.azath.reader.*`, `kindlereader.epub` -> `kindlereader.azath`, `MAPInformationProvider.epub` -> `MAPInformationProvider.azath`) while keeping Java `android:name` classes (`OpenCatalogActivity`, `ExternalEpubActivity`) intact.
   - Updated `android:label="Azath"` in manifest and patched `strings.xml` (`Search Azath`, `Azath library`).
   - Updated `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` to target `com.azath.reader` (including `MANAGE_EXTERNAL_STORAGE` grant) and stage `07_APK_EPUB_INGESTION_MOD/release/azath_v14.0_kfx/base.apk`.
3. **Compilation, Deployment & Smoke Test**:
   - Recompiled `classes9.dex` (`459,960` bytes), rebuilt APK with `apktool b`, aligned via `zipalign`, signed with debug keystore.
   - Streamed install succeeded cleanly as `com.azath.reader` (`lastUpdateTime=2026-11-24 23:55:15`) side-by-side with untouched stock `com.amazon.kindle` (`2026-11-19 21:59:23`) and `com.amazon.kindle.epub`.
   - Launched `com.azath.reader/com.amazon.kindle.UpgradePage` directly into **Azath** Home and More Control Center with **zero fatal crashes or authority/provider errors** in `logcat`.
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/azath_v14.0_kfx/base.apk` (`137,856,708` bytes ~132 MB, SHA256: `c5ff452232ba5fc38a5c7d0de91f8f03796ca9ea4ba5790dd4d30fdc8ecb0e67`).

---

## 2. Task B: Hardware Verification & Play Store Folio Reference Capture (Samsung Galaxy M30)
1. **`verify_r134_azath_control_center.png` (208,312 bytes)**:
   - Captured **Azath** More tab (`Search Azath`, `Control Center`, `v14.0 KFX` badge, bespoke ivory+crimson icons on compass/search/rows: `ic_library`, `ic_import`, `ic_globe`, `ic_feed`, `ic_compass`, `ic_search`).
   - Verified `RESTORE LATEST` in Backup & Restore ran against `/sdcard/kindle/backups/folio_metadata_backup.json` (19 local `/sdcard/kindle` KFX books indexed immediately on fresh `com.azath.reader` install; full backup prefs/DB sync applies cleanly on next restart).
2. **`verify_r134_azath_launcher.png` (339,650 bytes)**:
   - Captured Samsung Galaxy M30 app drawer displaying the crimson boy-under-tree launcher icon with label **`Azath`** clearly separated from legacy `Folio` and Play Store `Folio`.
3. **`ref_playstore_folio_1.png` (418,540 bytes) & `ref_playstore_folio_2.png` (155,997 bytes)**:
   - Launched Play Store **Folio** (`ct.reader/com.folio.reader.MainActivity`) and captured its Library screen (`Your library`, serif headings + geometric sans UI, `All books`/`Reading`/`Finished` chips, shelf grid) and Settings screen (`Make yourself at home`, serif card headlines + sans body, icon list) for Round 135 typography unification study.
4. **Safety Invariant**:
   - Stock `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).
