# Antigravity Report — Round 143 Deliverables (`Azath v14.9 KFX`)

## Summary
Implemented, built, deployed, and hardware-verified Round 143 (`Azath v14.9 KFX`) on the physical **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI), permanently replacing the stock light-grey Library generic cover placeholders with dark **Azath Obsidian `#211C1B`** cover tiles (Warm Ash `#8E867E` open-book emblem, `#5C2D26` crimson hairline rule, Archival Ivory `#F2EFE9` generated text), unifying `Hide Imported: ON` to tonal `#2A1816`/`#5C2D26`/`#F5B8AE`, and bumping to `Azath v14.9 KFX`.

---

## Verification Matrix
| Check / Deliverable | Status | Verified Evidence |
| :--- | :--- | :--- |
| Persistent Dark Obsidian Library Generic Cover Patch (`patch_res_azath_covers.py` + `rebuild_fast.sh`) | **VERIFIED** | Located stock cover assets (`drawable/generic_book_cover_list.webp` `400x600`, `drawable-large/generic_book_cover_list.webp` `800x1200`, `drawable-xlarge/generic_book_cover_list.webp` `960x1440`), `colors.xml` (`generated_cover_text_color` `#414042`, `library_default_cover_color_dark` `#555555`, `library_default_cover_color_light` `#e9e9e9`), `badgeable_cover_rich_placeholder_overlay.xml`, and `bds_book_placeholder_dark.xml`. Added idempotent `07_APK_EPUB_INGESTION_MOD/tools/patch_res_azath_covers.py` invoked on every `rebuild_fast.sh` run. |
| Round 143 Java Drop-Ins (`FolioFolderBrowserDialog.java`, `FolioMoreView.java`) | **VERIFIED** | Integrated `Hide Imported: ON` tonal `#2A1816` / `#5C2D26` / `#F5B8AE` (`999dp` pill) in `FolioFolderBrowserDialog.java` (note: active Importer modal itself was not re-screenshotted in Round 143 per Task B) and `Azath v14.9 KFX` bump in `FolioMoreView.java`. |
| APK Build & Staging (`v14.9 KFX`) | **VERIFIED** | `bash 07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` exited `0` (`classes9.dex` `468272` bytes, `Lib/b.<clinit>` Ember Crimson `0xffe05a47L`, `patch_classes3_azath.sh`, `patch_res_azath_covers.py`, XML patches). Staged APK: `07_APK_EPUB_INGESTION_MOD/release/azath_v14.9_kfx/base.apk` (`138541159` bytes, SHA256 `d49f038c41bd779ae2ad924a469aee8ac47e005ef2e5ee6fc21e3475ee526d92`). |
| Device Deployment (`com.azath.reader`) | **VERIFIED** | `dumpsys package com.azath.reader` -> `versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-25 01:32:09`. `v14.9 KFX` confirmed in `logs/azath_more_r143.xml`. Logcat (`FATAL EXCEPTION`) -> `NO_FATAL_EXCEPTIONS`. |
| Stock Kindle Preservation (`com.amazon.kindle`) | **VERIFIED** | `dumpsys package com.amazon.kindle` -> `versionName=8.160.1.0(2.0.124621.0)`, `lastUpdateTime=2026-11-19 21:59:23` (100% untouched & read-only). |
| Library Dark Obsidian Covers (`verify_r143_azath_library_dark_covers.png`) | **VERIFIED** | Captured on Galaxy M30 (`1058748` bytes) & visually inspected: coverless books (*Moksha*, *Crome Yellow*, *The Dream of the Red Chamber*, *Island*, *Brave New World*, *Confessions of Zeno*) now render cleanly on Obsidian `#211C1B` cards with `#2A2423` border, Warm Ash `#8E867E` open-book emblem, subtle `#5C2D26` spine hairline, and Archival Ivory `#F2EFE9` titles/authors (`generated_cover_text_color`). |

---

## Modified / Added Files
- `07_APK_EPUB_INGESTION_MOD/tools/patch_res_azath_covers.py` & `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh`
- `dropin_src/com/amazon/kindle/epub/{ingest/FolioFolderBrowserDialog.java,catalog/FolioMoreView.java}` & `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/{ingest/FolioFolderBrowserDialog.java,catalog/FolioMoreView.java}`
- `07_APK_EPUB_INGESTION_MOD/dex/classes9.dex` (`468272` bytes)
- `verify_r143_azath_library_dark_covers.png`

---

## Mandatory Project Lead Architectural Directive (Cover Architecture)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
