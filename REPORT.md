# Antigravity Report — Round 139 Deliverables (`Azath v14.5 KFX`)

## Summary
Implemented, built, deployed, and hardware-verified Round 139 (`Azath v14.5 KFX`) on the physical **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI), adding `.cover.jpg` fallback lookup and literary archival **EB Garamond** drop-cap covers with `#5C2D26` spine rules on Home, the graceful **Offline Mode** card (`Retry` + `Local Catalog` pills), version bump to `Azath v14.5 KFX`, and runtime verification of the Round 138 dark offline WebView error screen.

---

## Verification Matrix
| Check / Deliverable | Status | Verified Evidence |
| :--- | :--- | :--- |
| Regression Fixes Preserved (`FolioMoreView.java`) | **VERIFIED** | Restored `makeMiniCoverView(ctx, d, rec.kfxPath, rec.title, 38, 54, 9)` (line 1326) and `"Azath Library Insights\n"` (line 5159) in `dropin_src/` before copying into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`. |
| APK Build & Staging (`v14.5 KFX`) | **VERIFIED** | `bash 07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` exited `0` (`classes9.dex` `464996` bytes, `Lib/b.<clinit>` Ember Crimson `0xffe05a47L` + XML patches). Staged APK: `07_APK_EPUB_INGESTION_MOD/release/azath_v14.5_kfx/base.apk` (`138532967` bytes, SHA256 `7320ae96062ae5a080b01c79a763c6cfb8617216fb3c9f92370780d6e71361c7`). |
| Device Deployment (`com.azath.reader`) | **VERIFIED** | `dumpsys package com.azath.reader` -> `versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-25 00:52:52`. Logcat (`FATAL EXCEPTION`) -> `NO_FATAL_EXCEPTIONS`. Control Center badge `v14.5 KFX` confirmed in `logs/azath_more_r139.xml`. |
| Stock Kindle Preservation (`com.amazon.kindle`) | **VERIFIED** | `dumpsys package com.amazon.kindle` -> `versionName=8.160.1.0(2.0.124621.0)`, `lastUpdateTime=2026-11-19 21:59:23` (100% untouched & read-only). |
| Home Archival Covers & Offline Card (`verify_r139_azath_home.png`) | **VERIFIED** | Captured on Galaxy M30 (`185017` bytes) & visually inspected: EB Garamond drop-cap initials (`M`, `C`, `T`) with `#5C2D26` crimson spine rules and EB Garamond titles on `Continue Reading` cards; clean **Offline Mode** card under `Popular Free EPUBs` with EB Garamond heading, Inter guidance copy, tonal ember-crimson `Retry` pill, and `Local Catalog` pill. |
| Offline Web Book Browser (`verify_r139_azath_web_browser_offline.png`) | **VERIFIED** | Captured on Galaxy M30 (`116073` bytes) & visually inspected: Web Book Browser opened offline to `https://standardebooks.org/ebooks`, rendering dark Obsidian-Ivory-Crimson **`Page unavailable offline`** card, `net::ERR_INTERNET_DISCONNECTED` pill, and `#E05A47` **`Try Again`** button. |

---

## Modified / Added Files
- `dropin_src/com/amazon/kindle/epub/catalog/{OpenCatalogView.java,FolioMoreView.java}` & `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/{OpenCatalogView.java,FolioMoreView.java}`
- `07_APK_EPUB_INGESTION_MOD/dex/classes9.dex` (`464996` bytes)
- `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` (`azath_v14.5_kfx` staging)
- `verify_r139_azath_home.png`, `verify_r139_azath_web_browser_offline.png`

---

## Mandatory Project Lead Architectural Directive (Cover Architecture)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
