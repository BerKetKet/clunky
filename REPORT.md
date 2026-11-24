# Round 150 Report — Milestone Azath v17.0 KFX (Full Granite & Limestone Dual-Theme Modal Parity)

## 1. Target Hardware & Policy Compliance
- **Hardware**: Physical **Samsung Galaxy M30** (`32001776e8e82623`), running TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Active Package**: `com.azath.reader` (`Azath v17.0 KFX`, `versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-25 02:53:35`).
- **Safety Invariant Verified**: Stock `com.amazon.kindle` (`versionName=8.160.1.0(2.0.124621.0)`) remains **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).
- **Strict Policy Respected**: Zero Play Store Folio (`ct.reader`) launches, inspections, or references.

## 2. Task A: Build, Deploy, Stage & GitHub Milestone Push (`Azath v17.0 KFX`)
- Copied all 5 Round 150 files (`FolioTheme.java`, `FolioMoreView.java`, `FolioFolderBrowserDialog.java`, `FolioMetadataCatalogDialog.java`, `FolioWebBrowserDialog.java`) into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/`.
- Fixed 2 `javac` type errors in `FolioMetadataCatalogDialog.java` (both `mod_src/real/` and `dropin_src/`):
  - Line `2938`: `tv.setTextColor((textHex));` -> `tv.setTextColor(Color.parseColor(textHex));`
  - Line `2967`: `selBg.setColor(Color.parseColor(isSel ? FolioTheme.bgControl() : FolioTheme.bgSurface()));` -> `selBg.setColor(isSel ? FolioTheme.bgControl() : FolioTheme.bgSurface());`
- Updated `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` to stage `07_APK_EPUB_INGESTION_MOD/release/azath_v17.0_kfx/base.apk`.
- Recompiled DEX (`classes9.dex`: `473,568` bytes), built/signed APK, deployed to Galaxy M30 (`32001776e8e82623`), and staged release:
  - **Path**: `07_APK_EPUB_INGESTION_MOD/release/azath_v17.0_kfx/base.apk`
  - **Size**: `138,831,975` bytes
  - **SHA256**: `2152ecf1ad99c7e4c2ad63397839f7a0484b3680735fab97fd496814c73cad63`

## 3. Task B: Hardware Verification & Visual Audit (Samsung Galaxy M30)
- **`verify_r150_azath_catalog_limestone.png` (`363,543` bytes)**: Toggled `Granite -> Limestone` via zero-restart hot-swap inside Control Center (`v17.0 KFX`), then opened **Library Metadata & Genres (24)**.
  - Verified clean Limestone Light Mode rendering across the modal surface, `AZATH` eyebrow, dark-tinted `Authors`/`Compass`/`Grid` icons, `Sort: Recent`, `Sync & Enrich` CTA, search box, filter chips (`All (24)`, `Reading (6)`, `Unread (18)`, `History (5)`), archival monogram covers (`B`, `I`), book cards (*Brave New World*, *Island*, *Delphi Complete Works of Aeschylus*), and `(i)` icons.
  - Restored `Granite` cleanly afterward (`[876,221][1027,285]` text `Granite`).
- **Logcat Audit**: `ZERO_CRASHES_AND_NO_SIGSYS` in `AndroidRuntime:E`, `FATAL:E`, and `DEBUG:*`.

### Honest Visual Polish Observations in `verify_r150_azath_catalog_limestone.png`
1. **Modal Heading Contrast**: `Library Metadata & Genres (24)` still renders in white/ivory (`#ECE8E0`) against the light `#EFEAE0` modal background (low contrast).
2. **`AlertDialog` Bottom Button Bar (`CLOSE`)**: The Android `AlertDialog` footer panel stays dark `#15171A`/`#0E0F11` under the system dark theme while the `CLOSE` button text is dark `#1A1B1E`, making `CLOSE` hard to read in Limestone Light Mode.

## 4. Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
