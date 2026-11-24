# Round 125 (Milestone) Deliverables Report — Folio v12.5 KFX (Crimson Launcher Icon, Kindle Bookerly/Ember Typography & Zero-Neon Sweep)
**Environment**: Samsung Galaxy M32 (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).  
**Stock Kindle Invariant**: Package `com.amazon.kindle` (v8.160.1.0) verified **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Folio Version**: Milestone `Folio v12.5 KFX (Enhanced Typesetting)` installed on device at `2026-11-24 05:13:45`.

---

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

---

## 1. Task A: Source Integration & Release Staging
1. **Drop-In Source Integration**:
   - **`FolioTheme.java` (Kindle-Native Typography & Dialog Styling)**:
     * Implemented dynamic runtime font loader loading Kindle's bundled **Bookerly** (display/titles) and **Amazon Ember** (body/buttons/chips) fonts directly from the APK's `assets/` directory with clean `Typeface.SERIF` / `Typeface.SANS_SERIF` fallbacks.
     * Implemented `FolioTheme.applyToTree`: Recursively applies Kindle typography across any view tree and dynamically tints every `CheckBox` with Kindle blue (`#4DA6FF` checked, `#6E7680` unchecked).
     * Implemented `FolioTheme.styleDialog`: Standardizes AlertDialog positive (`#4DA6FF` Ember bold), negative (`#8E959C`), and neutral (`#C4CBD2`) action buttons.
   - **New Crimson Launcher Icon (`dropin_res/`)**:
     * Integrated high-res crimson launcher icon assets across all Android densities (`mipmap-mdpi` through `mipmap-xxxhdpi`: `ic_launcher.png`, `ic_launcher_round.png`, `ic_launcher_foreground.png`).
     * Set adaptive background to solid `#1A0808` crimson (`folio_icon_bg.png` 512x512, `launcher_bg_adaptive.jpg` 432x432) and wired foreground to `ic_launcher_foreground.png`.
   - **Thorough Zero-Neon Highlight Sweep Across All 4 UI Files**:
     * Audited and removed all lingering neon hexes (green `#00FF*`, purple `#D946EF`, amber `#F59E0B`, cyan `#06B6D4`) across `FolioMoreView.java`, `FolioMetadataCatalogDialog.java`, `FolioFolderBrowserDialog.java`, and `FolioWebBrowserDialog.java`.
     * Replaced them with the unified Kindle palette: `#4DA6FF` (checked/active), `#162330` (subtle selected fill), `#2C5278` (active border), `#1C1F22` (neutral control surfaces), and `#262A2E` (hairlines).
   - **Version Badge**: Bumped Control Center badge and `About Folio` dialog to **Milestone `Folio v12.5 KFX (Enhanced Typesetting)`**.

2. **Compilation, Packaging & Deployment**:
   - Recompiled `classes9.dex` (450,180 bytes) via `javac` and Android `d8`.
   - Rebuilt APK with `apktool b`, aligned via `zipalign -f -p 4`, signed with `apksigner`.
   - Streamed APK install to physical Samsung Galaxy M32 (`lastUpdateTime=2026-11-24 05:13:45`).
   - Verified zero fatal crashes in `logcat` (`FATAL EXCEPTION` check clean).
   - Staged release APK: `07_APK_EPUB_INGESTION_MOD/release/folio_v12.5_kfx/base.apk` (132 MB).

---

## 2. Task B: Hardware Verification on Physical Galaxy M32
Verified on physical Samsung Galaxy M32 hardware:

1. **`verify_r125_launcher_icon.png` (229,758 bytes)**:
   - Captured the Android home screen on Galaxy M32 showing:
     * The new **Folio** crimson boy-under-tree launcher icon (`#230f11` crimson base, crisp boy silhouette).
     * Sits alongside stock Kindle icon (`#2c93bc` blue) with distinct, elegant crimson identity.
2. **`verify_r125_kindle_fonts_and_highlights.png` (208,619 bytes)**:
   - Captured the **Import EPUBs from Storage** modal showing:
     * Two EPUB checkboxes checked, displaying calm Kindle blue `#4DA6FF` checked tint (verified 1,418 pixels of exact `#4DA6FF` inside checkbox).
     * Native Amazon Ember and Bookerly typography across modal header and list items.
     * Zero-neon controls: `Select New` chip, `Already Imported` tags, and `★` folder pin button.
     * Exact 0 neon pixels detected across the entire modal screenshot.
3. **Safety Invariants**:
   - Stock `com.amazon.kindle` (v8.160.1.0) remains bit-for-bit intact (`lastUpdateTime=2026-11-19 21:59:23`).
   - Zero crashes logged for `com.amazon.kindle.epub`.

---

## 3. GitHub Repository Synchronization
- **Repository Remote**: `https://github.com/BerKetKet/clunky.git`
- **Branch**: `master` (tracking `origin/master`)
- **Status**: Commits synced and pushed to remote.
