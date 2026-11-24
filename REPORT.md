# Antigravity Report — Round 145 Deliverables (`Azath v15.1 KFX`)

## Summary
Built, deployed, and hardware-tested Round 145 (`Azath v15.1 KFX`) on the physical **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI), integrating `ExternalEpubActivity.resolveExistingKfxFile(...)`, Azath Obsidian-Ivory-Crimson unification in `ExternalEpubActivity.java`, and the `Azath v15.1 KFX` version bump. Hardware verification identified the exact root cause why **Brave New World** could not launch `StandAloneBookReaderActivity` from Control Center on this device.

---

## Verification Matrix
| Check / Deliverable | Status | Verified Evidence |
| :--- | :--- | :--- |
| Drop-In Sources (`ExternalEpubActivity.java`, `FolioMoreView.java`) | **VERIFIED** | Integrated `resolveExistingKfxFile(ctx, rawFile)` into `launchKfxReader` and `FolioMoreView` click handlers, plus Obsidian-Ivory-Crimson dialog theming (`#121010`, `#E05A47`, `#F2EFE9`) and `v15.1 KFX` bump. (Visual note from an accidental Home tap on *Rashomon*: `ExternalEpubActivity` renders the new `#121010`/`#E05A47`/`#F2EFE9` dialog cleanly, though the badge copy still reads `"FOLIO EPUB IMPORT"` / `"ALREADY IN FOLIO LIBRARY"`.) |
| APK Build & Staging (`v15.1 KFX`) | **VERIFIED** | `bash 07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` exited `0` (`classes9.dex` `468980` bytes, `Lib/b.<clinit>` Ember Crimson `0xffe05a47L`, `patch_classes3_azath.sh`, `patch_res_azath_covers.py`, XML patches). Staged APK: `07_APK_EPUB_INGESTION_MOD/release/azath_v15.1_kfx/base.apk` (`138541159` bytes, SHA256 `17f18cf285e500baf079b7b37fda1ffbd7db7436352b9101bbbc234e001adac4`). |
| Device Deployment (`com.azath.reader`) | **VERIFIED** | `dumpsys package com.azath.reader` -> `versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-25 01:46:22`. `v15.1 KFX` confirmed in `logs/azath_more_r145.xml`. Logcat (`FATAL EXCEPTION`) -> `NO_FATAL_EXCEPTIONS`. |
| Stock Kindle Preservation (`com.amazon.kindle`) | **VERIFIED** | `dumpsys package com.amazon.kindle` -> `versionName=8.160.1.0(2.0.124621.0)`, `lastUpdateTime=2026-11-19 21:59:23` (100% untouched & read-only). |
| Control Center `Brave New World` Launch (`verify_r145_azath_continue_reading_launch.png`) | **BLOCKED / DIAGNOSED** | Captured `verify_r145_azath_continue_reading_launch.png` (`312292` bytes) after tapping **Brave New World** `(380, 960)` in Control Center's `Continue Reading` row: remained on `MainActivity` after toasting `"Book file not found"`. **Root Cause**: `Brave_New_World_FOLIO_621B293A3B8F.kfx` (along with `Moksha`, `Crome Yellow`, `The Dream of the Red Chamber`, and `Island` `.kfx` files) resided only inside `/data/user/0/com.amazon.kindle.epub/files/folio_library/` and was permanently deleted by Android when `com.amazon.kindle.epub` was uninstalled in Round 136. `find /sdcard/ -iname '*brave*'` shows only the unimported source EPUB `/sdcard/en/0/Aldous Huxley - Brave New World.epub`. Tapping Control Center Card #2 (*Delphi Complete Works of Aristotle*, `/sdcard/kindle/Aristotle - Delphi Complete Works.kfx`) invokes `launchKfxReader` -> `ThirdPartyUpgradePage` -> `AlertActivity` (`"Incompatible Version"`), identical to tapping *Aristotle* inside the stock `Library` tab. |

---

## Actionable Findings for Round 146
1. **Fallback EPUB Re-Import / Stale Record Reconciliation**: When `resolveExistingKfxFile` finds that a legacy `/com.amazon.kindle.epub/files/folio_library/*_FOLIO_*.kfx` file no longer exists on disk, offer/trigger fallback import from matching source EPUBs in `/sdcard/en/0/` (e.g., `/sdcard/en/0/Aldous Huxley - Brave New World.epub`), or prune/hide orphaned entries from `Continue Reading`.
2. **`ExternalEpubActivity.java` Badge Copy**: Update `"ALREADY IN FOLIO LIBRARY"` / `"FOLIO EPUB IMPORT"` to `"ALREADY IN AZATH LIBRARY"` / `"AZATH EPUB IMPORT"`.

---

## Modified / Added Files
- `dropin_src/com/amazon/kindle/epub/{ingest/ExternalEpubActivity.java,catalog/FolioMoreView.java}` & `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/{ingest/ExternalEpubActivity.java,catalog/FolioMoreView.java}`
- `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` (`azath_v15.1_kfx` staging)
- `07_APK_EPUB_INGESTION_MOD/dex/classes9.dex` (`468980` bytes)
- `verify_r145_azath_continue_reading_launch.png`

---

## Mandatory Project Lead Architectural Directive (Cover Architecture)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
