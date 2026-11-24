# Round 131 — Folio v13.1 KFX (Library Insights Reading Time Portfolio & Kindle Theme)

## Target Device
- Physical **Samsung Galaxy M30** (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).

## Summary of Changes (Drop-In Ready)
- **`FolioMoreView.java`** (`showLibraryInsightsDialog`):
  1. **`READING TIME PORTFOLIO` 3-Tile Strip**: Added a live WPM-calibrated reading-time dashboard at the top of **Library Insights** displaying **`TOTAL LIBRARY`** (total estimated hours across all local KFX books), **`TIME READ`** (weighted by finished + in-progress percentages, highlighted in Kindle blue-slate `#162330` / `#2C5278` / `#4DA6FF`), and **`REMAINING`** hours, scaled dynamically by `getReadingSpeedWpm(ctx)`.
  2. **Kindle-Native Insights Styling**: Wired `FolioTheme.applyToDialog(ctx, insightDlgRef[0])` onto `Library Insights` for Bookerly headings, Amazon Ember labels/charts, and styled `#4DA6FF` / `#C4CBD2` / `#8E959C` footer buttons.
  3. **Version Bump**: Updated Control Center badge and `About Folio` dialog to `Folio v13.1 KFX`.

## Task A: Build, Sign, Deploy & Stage
1. Copy `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`.
2. Recompile `classes9.dex`, rebuild APK, zipalign, sign, and deploy to physical **Samsung Galaxy M30** (`32001776e8e82623`).
3. Stage release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v13.1_kfx/base.apk`.
4. Confirm `com.amazon.kindle` remains untouched and log zero `com.amazon.kindle.epub` crashes.

## Task B: Hardware Verification on Samsung Galaxy M30 (2 Screenshots)
1. **`verify_r131_insights_reading_time.png`**: Open **Library Insights** from the Control Center (`v13.1 KFX`) and capture the modal showing the Bookerly `Library Insights` heading, the new **`READING TIME PORTFOLIO (250 WPM)`** 3-tile strip (`TOTAL LIBRARY`, `TIME READ`, `REMAINING`), and the reading status bar.
2. **`verify_r131_about_v131.png`**: Open **About Folio** and capture `Version: Folio v13.1 KFX (Enhanced Typesetting)`.

Package `REPORT.md` + both screenshots into `antig_to_gemini_neon_clunky_report131.zip`.
