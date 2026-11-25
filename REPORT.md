# Round 151 Report — Azath v17.1 KFX (Uniform 26sp Headers, Limestone Heading/Footer Fix & Round 152 Data Dump)

## 1. Target Hardware & Policy Compliance
- **Hardware**: Physical **Samsung Galaxy M30** (`32001776e8e82623`), running TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Active Package**: `com.azath.reader` (`Azath v17.1 KFX`, `versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-25 03:00:15`).
- **Safety Invariant Verified**: Stock `com.amazon.kindle` (`versionName=8.160.1.0(2.0.124621.0)`) remains **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).
- **Strict Policy Respected**: Zero Play Store Folio (`ct.reader`) launches, inspections, or references.

## 2. Task A: Verbatim Drop-In Build, Deploy & Stage (`Azath v17.1 KFX`)
- Copied all 4 Round 151 files (`FolioMoreView.java`, `FolioTheme.java`, `OpenCatalogView.java`, `FolioMetadataCatalogDialog.java`) verbatim into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/`.
- Updated `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` to stage `07_APK_EPUB_INGESTION_MOD/release/azath_v17.1_kfx/base.apk`.
- Recompiled DEX cleanly (`classes9.dex`: `474,212` bytes), built/signed APK, deployed to Galaxy M30 (`32001776e8e82623`), and staged release:
  - **Path**: `07_APK_EPUB_INGESTION_MOD/release/azath_v17.1_kfx/base.apk`
  - **Size**: `138,831,975` bytes
  - **SHA256**: `eed25c1e152126c288185163127802d8502386db88b53d997a9daf0b668655fc`

## 3. Task B: Hardware Verification (`verify_r151_azath_catalog_limestone.png`)
- **`verify_r151_azath_catalog_limestone.png` (`368,135` bytes)**: Tapped `Granite -> Limestone` in Control Center (`v17.1 KFX`) and opened **Library Metadata & Genres (24)**.
  - Verified `"Library Metadata & Genres (24)"` heading now renders in crisp `#1A1B1E` deep granite EB Garamond on `#EFEAE0`.
  - Verified the `AlertDialog` footer panel (`CLOSE` bar) now paints `#EFEAE0` warm Limestone with high-contrast dark Granite `CLOSE` text.
  - Restored cleanly back to `Granite` (`[876,221][1027,285]` text `Granite`).
- **Logcat Crash Check**: `ZERO_CRASHES_AND_NO_SIGSYS` in `AndroidRuntime:E`, `FATAL:E`, and `DEBUG:*`.

## 4. Raw Data Collection for Round 152

### A. `uiautomator` Bounds of `"Library"` Header Node (`/sdcard/ui_r151_lib.xml`)
```xml
<node index="0" text="Library" resource-id="" class="android.widget.TextView" package="com.azath.reader" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[43,135][178,198]" />
<node index="1" text="Search Azath" resource-id="com.azath.reader:id/search_box_text_view" class="android.widget.TextView" package="com.azath.reader" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[158,251][378,356]" />
<node index="0" text="Library" resource-id="" class="android.widget.TextView" package="com.azath.reader" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[630,2193][719,2246]" />
```
*(Compare: `"Library"` header height is `198 - 135 = 63 px` starting at `x=43, y=135`, whereas `"Control Center"` is `bounds="[53,115][630,205]"` = `90 px` height.)*

### B. Raw `Lib/b.<clinit>` Smali Lines Around Dark Surface & Accent Constants (`classes2.dex`)
```smali
    const-wide v0, 0xffd4c9b8L

    invoke-static {v0, v1}, Landroidx/compose/ui/graphics/m0;->d(J)J

    move-result-wide v2

    sput-wide v2, Lib/b;->functionalCorePrimary:J

    invoke-static {v0, v1}, Landroidx/compose/ui/graphics/m0;->d(J)J

    move-result-wide v2

    sput-wide v2, Lib/b;->functionalCorePrimaryInteractive:J
...
    const-wide v2, 0xffffffffL

    invoke-static {v2, v3}, Landroidx/compose/ui/graphics/m0;->d(J)J
...
    const-wide v4, 0xff000000L
...
    const-wide v6, 0xff101112L

    invoke-static {v6, v7}, Landroidx/compose/ui/graphics/m0;->d(J)J

    move-result-wide v8

    sput-wide v8, Lib/b;->functionalNeutralSurface:J

    invoke-static {v2, v3}, Landroidx/compose/ui/graphics/m0;->d(J)J

    move-result-wide v8

    sput-wide v8, Lib/b;->functionalNeutralOnSurface:J

    invoke-static {v4, v5}, Landroidx/compose/ui/graphics/m0;->d(J)J

    move-result-wide v8

    sput-wide v8, Lib/b;->functionalNeutralOnSurfaceFixed:J

    invoke-static {v4, v5}, Landroidx/compose/ui/graphics/m0;->d(J)J

    move-result-wide v8

    sput-wide v8, Lib/b;->functionalNeutralOnSurfaceInverse:J

    invoke-static {v2, v3}, Landroidx/compose/ui/graphics/m0;->d(J)J

    move-result-wide v8

    sput-wide v8, Lib/b;->functionalNeutralOnSurfaceInverseFixed:J

    const-wide v8, 0xffa8abadL

    invoke-static {v8, v9}, Landroidx/compose/ui/graphics/m0;->d(J)J

    move-result-wide v10

    sput-wide v10, Lib/b;->functionalNeutralOnSurfaceVariant:J

    const-wide v10, 0xff1a1b1cL

    invoke-static {v10, v11}, Landroidx/compose/ui/graphics/m0;->d(J)J

    move-result-wide v12

    sput-wide v12, Lib/b;->functionalNeutralSurfaceContainerLowest:J

    invoke-static {v10, v11}, Landroidx/compose/ui/graphics/m0;->d(J)J

    move-result-wide v12

    sput-wide v12, Lib/b;->functionalNeutralSurfaceContainerLow:J

    const-wide v12, 0xff242526L

    invoke-static {v12, v13}, Landroidx/compose/ui/graphics/m0;->d(J)J

    move-result-wide v12

    sput-wide v12, Lib/b;->functionalNeutralSurfaceContainer:J

    const-wide v12, 0xff2f3133L

    invoke-static {v12, v13}, Landroidx/compose/ui/graphics/m0;->d(J)J

    move-result-wide v12

    sput-wide v12, Lib/b;->functionalNeutralSurfaceContainerHigh:J

    invoke-static {v6, v7}, Landroidx/compose/ui/graphics/m0;->d(J)J

    move-result-wide v12

    sput-wide v12, Lib/b;->functionalNeutralSurfaceInverse:J

    const-wide v12, 0xff8e9194L

    invoke-static {v12, v13}, Landroidx/compose/ui/graphics/m0;->d(J)J

    move-result-wide v14

    sput-wide v14, Lib/b;->functionalNeutralOutline:J

    const-wide v14, 0xfff7f9faL

    invoke-static {v14, v15}, Landroidx/compose/ui/graphics/m0;->d(J)J

    move-result-wide v14

    sput-wide v14, Lib/b;->functionalNeutralOutlineBright:J

    const-wide v14, 0xff3b3c3dL

    invoke-static {v14, v15}, Landroidx/compose/ui/graphics/m0;->d(J)J

    move-result-wide v16

    sput-wide v16, Lib/b;->functionalNeutralOutlineVariant:J
```
*(Plus lines 238–276 later in `<clinit>`: `functionalSupplementalInteractionActive`, `functionalSupplementalInteractionSurfaceContainerSelected`, `functionalSupplementalInteractionOutlineSelected` (`0xffd4c9b8L`).)*

## 5. Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
