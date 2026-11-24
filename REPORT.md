# Antigravity Report — Round 140 Deliverables (`Azath v14.6 KFX`)

## Summary
Implemented, built, deployed, and hardware-verified Round 140 (`Azath v14.6 KFX`) on the physical **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI), collapsing the redundant Jetpack Compose `Home` top bar cleanly at its Scaffold source, integrating the circular Notification Bell button into the **Azath** header row, carrying over permanent `rec.title` / `"Azath Library Insights\n"` fixes, and bumping to `Azath v14.6 KFX`.

---

## Verification Matrix
| Check / Deliverable | Status | Verified Evidence |
| :--- | :--- | :--- |
| Compose `Home` TopBar Collapse (`patch_classes3_azath.sh`) | **VERIFIED** | Verified via `logs/azath_home_r139.xml` that the top `"Home"` + Bell header was rendered inside Compose `com.azath.reader:id/compose_content` (`HomeScreenlet` -> `com/amazon/kcp/home/a.smali`), not an Android `TextView`. Added idempotent `07_APK_EPUB_INGESTION_MOD/tools/patch_classes3_azath.sh` patching `com/amazon/kcp/home/a;->j()` from `HeaderMode;->d` to `HeaderMode;->c` (`h$a`, zero-height empty header in `NavigationControllerTopAppBar`), leaving `Library` and `More` headers intact. |
| Notification Bell Integration (`OpenCatalogView.java`) | **VERIFIED** | Circular Notification Bell icon button (`makeBellIconButton`) sits directly beside `Backup` on the `Azath` row; wired click handler directly to `com.amazon.kindle.inapp.notifications.InAppNotificationsActivity` (`topResumedActivity` confirmed on `(993, 160)` tap). |
| APK Build & Staging (`v14.6 KFX`) | **VERIFIED** | `bash 07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` exited `0` (`classes9.dex` `467736` bytes, `Lib/b.<clinit>` Ember Crimson `0xffe05a47L`, `patch_classes3_azath.sh`, XML patches). Staged APK: `07_APK_EPUB_INGESTION_MOD/release/azath_v14.6_kfx/base.apk` (`138532967` bytes, SHA256 `d7995bd674ad6db38cd034f74dbe38907e95a3ee44dcf4a7b2a46a6d17a8530f`). |
| Device Deployment (`com.azath.reader`) | **VERIFIED** | `dumpsys package com.azath.reader` -> `versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-25 01:11:28`. Logcat (`FATAL EXCEPTION`) -> `NO_FATAL_EXCEPTIONS`. Control Center badge `v14.6 KFX` confirmed in `logs/azath_more_r140.xml`. |
| Stock Kindle Preservation (`com.amazon.kindle`) | **VERIFIED** | `dumpsys package com.amazon.kindle` -> `versionName=8.160.1.0(2.0.124621.0)`, `lastUpdateTime=2026-11-19 21:59:23` (100% untouched & read-only). |
| Single-Header Home UI (`verify_r140_azath_home.png`) | **VERIFIED** | Captured on Galaxy M30 (`641259` bytes) & visually inspected: redundant `Home` top bar eliminated (`Azath` at `bounds="[42,109][527,212]"` directly under status bar) with `+ OPDS`, `Backup`, and circular Bell button on one row; real `.cover.jpg` covers rendered for *The 210th Day* & *Rashomon and Other Stories*, EB Garamond archival monogram `M` on *Moksha*, and calm `Offline Mode` card below. |

---

## Modified / Added Files
- `dropin_src/com/amazon/kindle/epub/catalog/{OpenCatalogView.java,FolioMoreView.java}` & `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/{OpenCatalogView.java,FolioMoreView.java}`
- `07_APK_EPUB_INGESTION_MOD/tools/patch_classes3_azath.sh` & `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh`
- `07_APK_EPUB_INGESTION_MOD/dex/classes9.dex` (`467736` bytes)
- `verify_r140_azath_home.png`

---

## Mandatory Project Lead Architectural Directive (Cover Architecture)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
