# Round 154 Report — Azath v17.4 KFX (Library Bottom-Tab Sync, Library Theme Parity & Full Theme Matrix)

## 1. Target Hardware & Policy Compliance
- **Hardware**: Physical **Samsung Galaxy M30** (`32001776e8e82623`), running TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Active Package**: `com.azath.reader` (`Azath v17.4 KFX`, `versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-25 08:09:39`).
- **Safety Invariant Verified**: Stock `com.amazon.kindle` (`versionName=8.160.1.0(2.0.124621.0)`) remains **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).
- **Strict Policy Respected**: Zero Play Store Folio (`ct.reader`) launches, inspections, or references.

## 2. Root Causes Fixed in `Azath v17.4 KFX`
1. **Bottom Navigation Active-Tab Desync on `Library`**:
   - `OpenCatalogView` (`Home` = `0`) and `FolioMoreView` (`More` = `2`) previously updated `setActiveBottomTab` only on `wrap`/`onViewAttachedToWindow`, while `Library` had no hook and `NavigationControllerViewModel.E0` (`switchToTab`) never notified `StoneBottomBarView`.
   - Fixed via `FolioTheme.onTabIdSelected(String)` hooked into `NavigationControllerViewModel.E0` (`patch_classes3.sh`), `FolioTheme.wrapLibraryScreenlet` (`setActiveBottomTab(ctx, 1)`), and dual `ib.b` + `ib.c` reflection in `syncComposeBottomBarColors`.
2. **Library Screen Limestone Theme Leaks (`LibraryTopBar`, `LegacySearchBar`, `LibraryScreenlet`)**:
   - `LibraryTopBar.r` (`l4.smali`, `m1.smali`, `q2.smali` in `classes5.dex`) and `TitleTopAppBar`/`SearchBoxTopAppBar` (`d0.smali` in `classes3.dex`) called `pf.h` (`TopAppBarDefaults.topAppBarColors`) with `titleContentColor = 0L`, causing Compose Material3 to read `MaterialTheme.colorScheme.onSurface` cached at `onCreate` (`0xFFFFFFFF` white) on `#EFEAE0` cream header. Patched via `patch_classes3_azath.sh` and `patch_classes5_azath.sh` to live `FolioTheme.composeSurfaceLong()` / `FolioTheme.composeOnSurfaceLong()`.
   - Hooked `oob/z.d` (`LegacySearchBar`) -> `FolioTheme.wrapLegacySearchBar` and `LibraryScreenlet.f` -> `FolioTheme.wrapLibraryScreenlet` (`patch_classes3.sh`) so `search_box`, `search_icon`, `search_box_text_view`, and `library_screenlet_root` / `recycler_view` / grid & list items dynamically re-theme without `Activity.recreate()` (avoiding native `SIGSYS` syscall `375`).
3. **Hardcoded Dark Constants & Dialog Theme Leaks Across Mod UI**:
   - Eliminated hardcoded dark literals and `Theme_DeviceDefault_Dialog_Alert` across `FolioMoreView.java`, `FolioMetadataCatalogDialog.java`, `FolioWebBrowserDialog.java`, `FolioFolderBrowserDialog.java`, `OpenCatalogView.java` (`buildSearchRow`, `promptCustomOpds`, status/clear pills), `ExternalEpubActivity.java`, `FolioIcons.java` (`ImageView` `SRC_IN` tinting), and `FolioTheme.styleDialog` (`alertTitle`, `android.R.id.message`, panels).

## 3. Build & Staged Release Artifact
- **DEX**: `07_APK_EPUB_INGESTION_MOD/dex/classes9.dex` (`488,864` bytes).
- **Release APK**: `07_APK_EPUB_INGESTION_MOD/release/azath_v17.4_kfx/base.apk` (`138,848,359` bytes, SHA256 `76a5facc0067f07dd819f2e7c72844faecbd90c96fac9dc798084f8f0be30706`).

## 4. Exhaustive Galaxy M30 (`32001776e8e82623`) Hardware Verification Matrix
| Surface / Interaction | Limestone (Light) | Granite (Dark) | Notes |
| --- | --- | --- | --- |
| **Bottom Nav Bar (`Home` / `Library` / `More`)** | PASS | PASS | `home_tab` / `library_tab` / `more_tab` `selected=true` verified in UI XML & visually; floating cover preserved. |
| **Home (`OpenCatalogView`)** | PASS | PASS | Header, search pill, genre chips, carousels, `+ OPDS` (`OPDS Catalog Feed` fixed), `Backup`, Genre Search (`Mystery`/`Sci-Fi`). |
| **Library (`LibraryScreenlet` + `LibraryTopBar`)** | PASS (with 1 minor leak) | PASS | `"Library"` title (`#1A1916` on `#EFEAE0`), Filter & Sort icons/sheets, `"Search Azath"` pill, grid covers, and book open (`StandAloneBookReaderActivity`) + return to Library (`library_tab` active). Minor leak: thin `#101112` outer Compose band behind `"Search Azath"` pill in Limestone. |
| **Control Center (`FolioMoreView`)** | PASS | PASS | `v17.4 KFX`, `Limestone`/`Granite` live toggle, stat tiles, `2026 Reading Goal`, `Up Next Queue`. |
| **Metadata Catalog (`FolioMetadataCatalogDialog`)** | PASS | PASS | Cards, genre chips, search input, `CLOSE` footer bar. |
| **Batch Importer (`FolioFolderBrowserDialog`)** | PASS | PASS | Path chips, EPUB list, `Import Selected`, `CLOSE` button. |
| **Web Book Browser (`FolioWebBrowserDialog`)** | PASS | PASS | Toolbar, URL bar, quick chips, WebView. |
| **Curation & System Modals** | PASS | PASS | `Reading Notes Notebook`, `Manage Shelves`, `Library Insights`, `Storage & Duplicates`, `About Azath` (`v17.4 KFX`). |
| **Stock `InAppNotificationsActivity` (Bell)** | PARTIAL | PASS | `#EFEAE0` background in Limestone, but stock Activity text/icons remain light (noted honestly for future smali hook if desired). |
| **Stability / Crash Audit (`logcat`)** | PASS | PASS | Zero `FATAL EXCEPTION`, zero `SIGSYS` (`SYS_SECCOMP`) across all theme toggles, tab switches, dialogs, and reader open/close. |

## 5. Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
