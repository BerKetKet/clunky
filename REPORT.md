# Antigravity Report — Round 138 Deliverables (`Azath v14.4 KFX`)

## Summary
Implemented, built, deployed, and hardware-verified Round 138 (`Azath v14.4 KFX`) on the physical **Samsung Galaxy M30** (`32001776e8e82623`, Android 13 GSI `arm64-v8a`), delivering bundled **EB Garamond** + **Inter** UI typography, Home Screen UI polish, dark offline WebView error handling in source, and updated release branding.

---

## Verification Matrix
| Check / Deliverable | Status | Verified Evidence |
| :--- | :--- | :--- |
| Bundled Fonts (`EB Garamond` + `Inter`) | **VERIFIED** | `07_APK_EPUB_INGESTION_MOD/assets/azath_fonts/` and `dropin_assets/azath_fonts/`: `EBGaramond-Regular.ttf` (`381K`, TrueType), `EBGaramond-SemiBold.ttf` (`384K`, TrueType), `Inter-Regular.otf` (`253K`, OpenType), `Inter-SemiBold.otf` (`265K`, OpenType). |
| APK Build & Staging (`v14.4 KFX`) | **VERIFIED** | `bash 07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` exited `0` (`classes9.dex` `462736` bytes, `Lib/b.<clinit>` Ember Crimson `0xffe05a47L` DEX patch + XML patches applied). Staged release: `07_APK_EPUB_INGESTION_MOD/release/azath_v14.4_kfx/base.apk` (`138528871` bytes, SHA256 `234deb91bf6ce4245203f451e58f551688e268eb89efa02c31e4bda0fe759663`). |
| Device Deployment (`com.azath.reader`) | **VERIFIED** | `dumpsys package com.azath.reader` -> `lastUpdateTime=2026-11-25 00:43:31`. Logcat check -> `NO_FATAL_EXCEPTIONS`. |
| Stock Kindle Preservation (`com.amazon.kindle`) | **VERIFIED** | `dumpsys package com.amazon.kindle` -> `lastUpdateTime=2026-11-19 21:59:23` (100% untouched & read-only). |
| Home Screen UI (`verify_r138_azath_home.png`) | **VERIFIED** | Captured on Galaxy M30 (`154151` bytes) & visually inspected: **`Azath`** in Regular EB Garamond (`30sp`), `+ OPDS`, tonal `Backup`, `#181515` search bar with `#E05A47` `Search` CTA, genre pills, `Continue Reading` (`Moksha`, `Crome Yellow`, `The Dream of the Red Chamber`) & `Popular Free EPUBs` in Regular EB Garamond, `#E05A47` active `Home` tab. |
| Control Center (`verify_r138_azath_control_center.png`) | **VERIFIED** | Captured on Galaxy M30 (`239020` bytes) & visually inspected: `Control Center` & stat numbers (`26 BOOKS`, `5 READING`) in Regular EB Garamond, `v14.4 KFX` ember-crimson pill (`logs/azath_more_r138.xml` verified), Inter labels, preserved `B` monogram on *Brave New World* + `"Azath Library Insights"`, `#E05A47` `More` tab. |
| Dark Offline WebView Error Page (`FolioWebBrowserDialog.java`) | **COMPILED / NOT SCREENSHOTTED** | `FolioWebBrowserDialog.java` updated with dark offline `onReceivedError` handling and compiled into `classes9.dex`; not runtime-screenshotted in Round 138 (only Home & Control Center were captured per Task B). |

---

## Modified / Added Files
- `07_APK_EPUB_INGESTION_MOD/assets/azath_fonts/` & `dropin_assets/azath_fonts/` (`EBGaramond-Regular.ttf`, `EBGaramond-SemiBold.ttf`, `Inter-Regular.otf`, `Inter-SemiBold.otf`)
- `07_APK_EPUB_INGESTION_MOD/mod_src/real/{FolioTheme.java,OpenCatalogView.java,FolioMoreView.java,FolioWebBrowserDialog.java}` (and preserved `rec.title` + `"Azath Library Insights"` in `dropin_src/.../FolioMoreView.java`)
- `07_APK_EPUB_INGESTION_MOD/dex/classes9.dex` (`462736` bytes)
- `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh`
- `verify_r138_azath_home.png`, `verify_r138_azath_control_center.png`

---

## Mandatory Project Lead Architectural Directive (Cover Architecture)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
