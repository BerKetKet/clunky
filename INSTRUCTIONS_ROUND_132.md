# Round 132 — Folio v13.2 KFX (Insights WPM Tap-to-Calibrate, Snapshot Telemetry & Clean Dialog Theming Sync)

## Target Device
- Physical **Samsung Galaxy M30** (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).

## Summary of Changes (Drop-In Ready)
- **`FolioMoreView.java`**:
  1. **Tap-to-Calibrate WPM in Library Insights**: Tapping the `READING TIME PORTFOLIO (250 WPM · TAP TO CALIBRATE)` 3-tile strip jumps directly to the Reading Goal / WPM calibrator and re-opens Insights with updated hours on save.
  2. **Reading Time in Shareable Insights Snapshot**: Added `Reading Time (250 WPM): Xh total · Yh read · Zh remaining` to the `COPY SUMMARY` / `SHARE` plaintext snapshot.
  3. **Permanent Post-`.show()` Dialog Theming Sync**: Permanently fixed all 5 `FolioTheme.applyToDialog(...)` placements (`goalDlgRef[0]`, `dlgRef[0]`, `storDlgRef[0]`, `insightDlgRef[0]`, `aboutDlg`) so `FolioMoreView.java` compiles and themes cleanly out of the box with zero manual patching.
  4. **Version Bump**: Updated Control Center badge and `About Folio` dialog to `Folio v13.2 KFX`.

## Task A: Build, Sign, Deploy & Stage
1. Copy `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`.
2. Recompile `classes9.dex`, rebuild APK, zipalign, sign, and deploy to physical **Samsung Galaxy M30** (`32001776e8e82623`).
3. Stage release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v13.2_kfx/base.apk`.
4. Confirm `com.amazon.kindle` remains untouched and log zero `com.amazon.kindle.epub` crashes.

## Task B: Hardware Verification on Samsung Galaxy M30 (2 Screenshots)
1. **`verify_r132_insights_calibrate.png`**: Open **Library Insights** (`v13.2 KFX`) and capture the modal showing `READING TIME PORTFOLIO (250 WPM · TAP TO CALIBRATE)` and the 3-tile strip.
2. **`verify_r132_about_v132.png`**: Open **About Folio** and capture `Version: Folio v13.2 KFX (Enhanced Typesetting)`.

Package `REPORT.md` + both screenshots into `antig_to_gemini_neon_clunky_report132.zip`.
