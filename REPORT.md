# Round 157 Report — Azath v17.7 KFX (`dp.g.ingestKfxLive` + `syncLibrarySearchBand` Hardware Test)

## 1. Target Hardware & Policy Compliance
- **Hardware**: Physical **Samsung Galaxy M30** (`32001776e8e82623`), running TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Active Package**: `com.azath.reader` (`Azath v17.7 KFX`, `versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-25 13:49:15`).
- **Safety Invariant Verified**: Stock `com.amazon.kindle` (`versionName=8.160.1.0(2.0.124621.0)`) remains **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).
- **Strict Policy Respected**: Zero Play Store Folio (`ct.reader`) launches, inspections, or references.

## 2. Task A: Build, Deploy & Stage (`Azath v17.7 KFX`)
- Copied Round 157 `dp/g.java`, `FolioMoreView.java`, `ExternalEpubActivity.java`, `FolioFolderBrowserDialog.java`, and `FolioWebBrowserDialog.java`, and merged `syncLibrarySearchBand(Activity)` into `FolioTheme.java` while preserving Round 154 `onTabIdSelected`, `composeSurfaceLong`, `composeOnSurfaceLong`, `wrapLegacySearchBar`, and `wrapLibraryScreenlet`.
- Built `classes9.dex` (`489,656` bytes) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v17.7_kfx/base.apk` (`138,848,359` bytes, SHA256 `510b923f2484d6dd8f6e99b7fdd33cad8303f065cd9e7f2318a904470d9229d8`).

## 3. Task B: Live Library Refresh & Limestone Search Band Results (`verify_r157_library_live_limestone.png`)
- **EPUB Conversion**: `Aldous Huxley - Island.epub` converted in `931ms` to `/sdcard/kindle/Island_FOLIO_8CE251A8A07B.kfx` (`1,223,991` B) + `.kfx.cover.jpg` (`66,733` B), with **ZERO `SQLiteConstraintException`** and **ZERO crashes / ZERO `SIGSYS`**.
- **Honest Findings Requiring Round 158**:
  1. **Live Library Refresh did NOT trigger**: Switching straight to `Library` without relaunching (`verify_r157_library_live_limestone.png`) did **not** show `Island`, and **`AzathLiveIngest` never appeared in `logcat`** (no `dp.g.ingestKfxLive` log line emitted).
  2. **Duplicate `Crome Yellow` rows**: `verify_r157_library_live_limestone.png` shows `Crome Yellow` twice — one row without `NEW` (`FolioMetadataDb` DB row) and one row with `NEW` (`/sdcard/kindle/` sideload scanner row).
  3. **Limestone Library Search Outer Band**: The thin `#101112` dark band behind `"Search Azath"` (`y=230..375`) is **still dark** in `verify_r157_library_live_limestone.png` (likely drawn by a Compose container rather than an Android `View` ancestor of `search_box_text_view`, or `ph == 0` at `syncStoneBottomBar` call time).
  4. **Bottom-Left Cutout**: When no floating book cover is present on `Library`, the left cutout of `StoneBottomBarView` exposes the dark native `Home` tab (`Hom`) underneath.

## 4. Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
