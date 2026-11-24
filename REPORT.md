# Antigravity Report — Round 141 Deliverables (`Azath v14.7 KFX`)

## Summary
Implemented, built, deployed, and hardware-verified Round 141 (`Azath v14.7 KFX`) on the physical **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI), extending the Compose `HeaderMode;->c` collapse to `MoreScreenlet`, integrating normalized Home author formatting (`Natsume Soseki`, `Ryunosuke Akutagawa`), tightening Control Center top padding (`12dp`), and bumping to `Azath v14.7 KFX`.

---

## Verification Matrix
| Check / Deliverable | Status | Verified Evidence |
| :--- | :--- | :--- |
| Single-Header `More` Collapse (`patch_classes3_azath.sh`) | **VERIFIED** | Extended idempotent `07_APK_EPUB_INGESTION_MOD/tools/patch_classes3_azath.sh` to switch `MoreScreenlet.j()` (`com/amazon/kcp/more/w.smali`) from `HeaderMode;->d` to `HeaderMode;->c` alongside `HomeScreenlet` (`com/amazon/kcp/home/a.smali`), leaving `LibraryScreenlet` untouched. |
| Author Formatting & Top Padding (`OpenCatalogView.java`, `FolioMoreView.java`) | **VERIFIED** | Integrated `formatHomeAuthor(...)` + direct `InAppNotificationsActivity` bell launch (`OpenCatalogView.java`) and `12dp` top container padding + `Azath v14.7 KFX` bump (`FolioMoreView.java`). |
| APK Build & Staging (`v14.7 KFX`) | **VERIFIED** | `bash 07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` exited `0` (`classes9.dex` `468160` bytes, `Lib/b.<clinit>` Ember Crimson `0xffe05a47L`, `patch_classes3_azath.sh`, XML patches). Staged APK: `07_APK_EPUB_INGESTION_MOD/release/azath_v14.7_kfx/base.apk` (`138532967` bytes, SHA256 `4b03ec387430adba1025dc92e414aedfd22b873e52e7c6fc292af8c10e75c292`). |
| Device Deployment (`com.azath.reader`) | **VERIFIED** | `dumpsys package com.azath.reader` -> `versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-25 01:17:26`. Logcat (`FATAL EXCEPTION`) -> `NO_FATAL_EXCEPTIONS`. |
| Stock Kindle Preservation (`com.amazon.kindle`) | **VERIFIED** | `dumpsys package com.amazon.kindle` -> `versionName=8.160.1.0(2.0.124621.0)`, `lastUpdateTime=2026-11-19 21:59:23` (100% untouched & read-only). |
| Control Center (`verify_r141_azath_control_center.png`) | **VERIFIED** | Captured on Galaxy M30 (`311907` bytes) & visually inspected: **`Control Center`** (`bounds="[53,115][630,205]"`, `v14.7 KFX` badge, compass & search icons) extends directly under the status bar with zero redundant `More` or `Search Azath` bar, showing `26 BOOKS`, `6 READING`, `Continue Reading`, `Up Next Queue`, and `LIBRARY & IMPORT`. |
| Home Screen (`verify_r141_azath_home.png`) | **VERIFIED** | Captured on Galaxy M30 (`640883` bytes) & visually inspected + XML checked: single-row **`Azath`** header (`+ OPDS`, `Backup`, circular Bell button), normalized **`Natsume Soseki`** and **`Ryunosuke Akutagawa`** author labels under `Continue Reading`, and clean `Offline Mode` card. |

---

## Modified / Added Files
- `dropin_src/com/amazon/kindle/epub/catalog/{OpenCatalogView.java,FolioMoreView.java}` & `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/{OpenCatalogView.java,FolioMoreView.java}`
- `07_APK_EPUB_INGESTION_MOD/tools/patch_classes3_azath.sh` & `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh`
- `07_APK_EPUB_INGESTION_MOD/dex/classes9.dex` (`468160` bytes)
- `verify_r141_azath_control_center.png`, `verify_r141_azath_home.png`

---

## Mandatory Project Lead Architectural Directive (Cover Architecture)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
