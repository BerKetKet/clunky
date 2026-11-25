# Round 158 Report — Azath v17.8 KFX (`SearchBandMaskView`, Full-Width `StoneBottomBarView` & `triggerLibraryRescan`)

## 1. Target Hardware & Policy Compliance
- **Hardware**: Physical **Samsung Galaxy M30** (`32001776e8e82623`), running TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Active Package**: `com.azath.reader` (`Azath v17.8 KFX`, `versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-25 14:10:14`).
- **Safety Invariant Verified**: Stock `com.amazon.kindle` (`versionName=8.160.1.0(2.0.124621.0)`) remains **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).
- **Strict Policy Respected**: Zero Play Store Folio (`ct.reader`) launches, inspections, or references.

## 2. Task A: Build, Deploy & Stage (`Azath v17.8 KFX`)
- Copied Round 158 `FolioMoreView.java`, `FolioFolderBrowserDialog.java`, `ExternalEpubActivity.java`, `FolioWebBrowserDialog.java` verbatim, and merged `SearchBandMaskView`, `triggerLibraryRescan`, and `sFloatingCoverActive` into `FolioTheme.java` while keeping Round 154 `onTabIdSelected`, `composeSurfaceLong`, `composeOnSurfaceLong`, `wrapLegacySearchBar`, and `wrapLibraryScreenlet` hooks intact.
- Built `classes9.dex` (`491,916` bytes) and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v17.8_kfx/base.apk` (`138,848,359` bytes, SHA256 `d5c46b0d136917699fbf4d2115a2a93e69d04dd07da929446d5e18afd5df9f73`).

## 3. Task B: Hardware Verification (`verify_r158_library_limestone_clean.png`)
- **Limestone Library Search Outer Band — FIXED**: `verify_r158_library_limestone_clean.png` shows warm `#EFEAE0` Limestone seamlessly surrounding the `"Search Azath"` pill with a `#CBC3B3` hairline — the `#101112` dark outer band is **100% GONE**.
- **Limestone Bottom Bar Cutout — FIXED**: When `sFloatingCoverActive == false`, `StoneBottomBarView` renders a full-width `#EFEAE0` bottom bar with centered `Home`, `Library` (active), and `More` — zero dark cutout leak.
- **EPUB Import (`Aldous Huxley - Eyeless In Gaza.epub`)**:
  - `11-25 14:15:25.986  5958  6734 I EpubTranscoder: Successfully converted Aldous Huxley - Eyeless In Gaza.epub to KFX on-device in 31346ms (1298643 bytes)`
  - Created in `/sdcard/kindle/`: `Eyeless_In_Gaza_FOLIO_83848DCC3B03.kfx` (`1,298,643` B), `.kfx.cover.jpg` (`29,736` B), `.sdr/`.
  - **Honest Live-Rescan Finding**: `AzathLiveIngest` did not appear in `logcat`, and `Eyeless In Gaza` did not appear in `Library` without relaunching yet (note `Utils.getFactory().getLibraryService()` may not expose a zero-arg `scan`/`sideload`/`refresh`/`sync` method on this obfuscated `ILibraryService` build).
- **Crash Audit**: Zero `FATAL EXCEPTION` and zero `SIGSYS` in `logcat`.

## 4. Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
