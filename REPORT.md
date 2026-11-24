# Antigravity Report — Round 146 Stone Edition (`Azath v16.0 KFX`)

## Summary
Implemented, built, deployed, and hardware-verified Round 146 (**Major Stone Edition `Azath v16.0 KFX`**) on the physical **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI), migrating all 8 Java UI modules, bespoke icons, launcher icon, Compose bottom nav tint, generic Library cover tiles, and `Library` header typography into the unified **Granite & Limestone Stone** design system.

---

## Verification Matrix
| Check / Deliverable | Status | Verified Evidence |
| :--- | :--- | :--- |
| Stone Java Modules (All 8 Files) | **VERIFIED** | Integrated all 8 drop-in files (`FolioTheme`, `FolioMoreView`, `OpenCatalogView`, `FolioMetadataCatalogDialog`, `FolioFolderBrowserDialog`, `FolioMetadataBackup`, `FolioWebBrowserDialog`, `ExternalEpubActivity`) into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/`. Verified Granite dark mode across Home, Library, Control Center, and `"AZATH"`/`v16.0 KFX` branding. (Note: Limestone light theme toggle inside Settings was compiled in `FolioTheme.java` and not toggled in R146.) |
| Limestone Launcher & 17 UI Icons (`azath_icons/`, `dropin_res/`) | **VERIFIED** | Installed `dropin_assets/azath_launcher/ic_launcher_foreground_432.png` & `azath_icon_limestone_512.png` + `mipmap-*` with `#141517` Stone adaptive background (`rebuild_fast.sh`); synced all 16 re-tinted `azath_icons/` and re-tinted `ic_authors.png` to `#ECE8E0` / `#D4C9B8`. |
| DEX & Resource Patchers (`patch_classes2_azath.py`, `patch_res_azath_covers.py`, `rebuild_fast.sh`) | **VERIFIED** | Updated `patch_classes2_azath.py` to patch `Lib/b.<clinit>` (`0xff2491ffL`/`0xffe05a47L` -> `0xffd4c9b8L`, applied at `0x4d6f50`), updated `patch_res_azath_covers.py` to Granite `#1C1E22`/`#32353B`/`#8A857D`/`#524B41`/`#ECE8E0`, and replaced `res/font/amazon_ember_serif_{bold,he}.ttf` with `EBGaramond-Regular.ttf` in `rebuild_fast.sh` (keeping reader `Bookerly-*.ttf` in `assets/` completely untouched). |
| APK Build & Staging (`v16.0 KFX`) | **VERIFIED** | `bash 07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` exited `0` (`classes9.dex` `470136` bytes, `classes2.dex` `8,413,156` bytes). Staged APK: `07_APK_EPUB_INGESTION_MOD/release/azath_v16.0_kfx/base.apk` (`139032679` bytes, SHA256 `f995e5a0372907cadce08240c7189dc2cd9e9967e39836a252f0ba239f8ca0b6`). |
| Device Deployment & Invariants | **VERIFIED** | `com.azath.reader` updated `2026-11-25 02:06:48`; stock `com.amazon.kindle` untouched (`lastUpdateTime=2026-11-19 21:59:23`); `NO_FATAL_EXCEPTIONS` in `logcat -d`; `text="v16.0 KFX"` confirmed in `logs/azath_more_r146.xml`; zero `ct.reader` launches/references. |
| App Drawer Limestone Icon (`verify_r146_azath_launcher_limestone.png`) | **VERIFIED** | Captured (`320969` bytes) & visually inspected: circular Limestone moonlit sky + tree silhouette `Azath` icon in Android app drawer (zero crimson). |
| Home Screen (`verify_r146_azath_home_granite.png`) | **VERIFIED** | Captured (`642942` bytes) & visually inspected: Granite `#1C1E22`/`#32353B` cards, luminous Limestone `#D4C9B8` `Search` CTA & active `Home` tab, EB Garamond headings, `#1C1E22` *Moksha* monogram, and `Offline Mode` card. |
| Library Screen (`verify_r146_azath_library_garamond.png`) | **VERIFIED** | Captured (`1079394` bytes) & visually inspected: top-left **`Library`** title now renders in airy **Regular EB Garamond** (`amazon_ember_serif_{bold,he}.ttf` patched), dark Granite `#1C1E22` generic covers with `#8A857D` open-book emblems & `#ECE8E0` ivory text, and `#D4C9B8` active `Library` tab. |
| Control Center (`verify_r146_azath_control_center_granite.png`) | **VERIFIED** | Captured (`314365` bytes) & visually inspected: **`Control Center`** (`v16.0 KFX`), Limestone-tinted `azath_icons/` (`Library Metadata & Genres`, `Import EPUBs from Storage`, `Web Book Browser`, `OPDS Catalogs`), Granite stat cards, tonal `+ Pick ›`, and `#D4C9B8` active `More` tab. |

---

## Modified / Added Files
- `dropin_res/` & `dropin_assets/azath_launcher/` (Limestone launcher icons + `#141517` background)
- `dropin_assets/azath_icons/*.png` & `07_APK_EPUB_INGESTION_MOD/assets/azath_icons/*.png` (all 17 Stone icons including re-tinted `ic_authors.png`)
- `dropin_src/com/amazon/kindle/epub/{catalog,ingest}/*.java` & `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/{catalog,ingest}/*.java` (all 8 Java files)
- `07_APK_EPUB_INGESTION_MOD/tools/{patch_classes2_azath.py,patch_res_azath_covers.py,rebuild_fast.sh}`
- `07_APK_EPUB_INGESTION_MOD/dex/classes9.dex` (`470136` bytes)
- `verify_r146_azath_launcher_limestone.png`, `verify_r146_azath_home_granite.png`, `verify_r146_azath_library_garamond.png`, `verify_r146_azath_control_center_granite.png`

---

## Mandatory Project Lead Architectural Directive (Cover Architecture)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
