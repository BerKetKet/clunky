# Round 117 — Folio v11.7 KFX (Reading Goal Pace Telemetry & Share Challenge)

## Summary of Changes (Drop-In Ready)
- **`FolioMoreView.java`** (`showReadingGoalDialog`):
  1. **3-Tile Reading Pace Telemetry Strip**: Added a live pace dashboard right beneath the annual progress bar displaying **`DAYS LEFT`** (sky-blue), **`TO GO`** (amber / emerald when met), and **`NEEDED PACE`** (violet, e.g. `1 / 9d` or `🏆 Met!`), computed dynamically from the device calendar.
  2. **`↗ Share` Reading Challenge Pill**: Added a sky-blue `↗ Share` button right beside `🎯 2026 Reading Goal` that dispatches Android's `ACTION_SEND` share sheet with progress %, pace summary, and completed books list.
  3. **Unified Trophy Shelf Covers**: Upgraded the Completed Books Trophy Shelf to use `makeMiniCoverView(...)` with rounded `#0D1017` border + `📖` fallback.
  4. **Version Bump**: Updated Control Center badge and `About Folio` dialog to `Folio v11.7 KFX`.

## Task A: Build, Sign, Deploy & Stage
1. Copy `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`.
2. Recompile `classes9.dex`, rebuild APK, zipalign, sign, and deploy to Galaxy M32 (`32001776e8e82623`).
3. Stage release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v11.7_kfx/base.apk`.
4. Confirm `com.amazon.kindle` remains untouched and log zero `com.amazon.kindle.epub` crashes.

## Task B: Hardware Verification (2 Screenshots)
1. **`verify_r117_reading_goal_pace.png`**: Tap the `🎯 2026 Reading Goal` progress bar on the Control Center (`Folio v11.7 KFX`) and capture the modal showing the `↗ Share` header pill, the 3-tile Reading Pace Telemetry strip (`DAYS LEFT`, `TO GO`, `NEEDED PACE`), and the `🏆 COMPLETED BOOKS` Trophy Shelf.
2. **`verify_r117_about_v117.png`**: Open `About Folio` and capture `Version: Folio v11.7 KFX (Enhanced Typesetting)`.

Package `REPORT.md` + both screenshots into `antig_to_gemini_neon_clunky_report117.zip`.
