# Round 118 — Folio v11.8 KFX (Saved Backups & Exports File Browser)

## Summary of Changes (Drop-In Ready)
- **`FolioMoreView.java`**:
  1. **Interactive `Backups ›` Tile & Saved Backups Browser (`showSavedBackupsDialog`)**: Tapping the emerald `Backups ›` footprint tile inside `💾 Storage & Duplicates` now opens a dark file browser for `/sdcard/kindle/backups/`, sorted newest-first with color-coded type badges (`HTML` sky-blue, `MD` violet, `JSON` amber, `CSV` emerald), file sizes, timestamps, inline `👁` monospace file preview (`showBackupFilePreviewDialog` with `Copy`), and `✕` delete controls.
  2. **Synced `presetLp` Variable Cleanup**: Synced the Round 117 `presetLp` rename in `showReadingGoalDialog` cleanly into `FolioMoreView.java`.
  3. **Version Bump**: Updated Control Center badge and `About Folio` dialog to `Folio v11.8 KFX`.

## Task A: Build, Sign, Deploy & Stage
1. Copy `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`.
2. Recompile `classes9.dex`, rebuild APK, zipalign, sign, and deploy to Galaxy M32 (`32001776e8e82623`).
3. Stage release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v11.8_kfx/base.apk`.
4. Confirm `com.amazon.kindle` remains untouched and log zero `com.amazon.kindle.epub` crashes.

## Task B: Hardware Verification (2 Screenshots)
1. **`verify_r118_saved_backups_browser.png`**: Open `💾 Storage & Duplicates` from the Control Center, tap the emerald **`Backups ›`** tile, and capture the `📦 Saved Backups & Exports` modal listing the exported files (`folio_reading_notes.md`, `folio_library.html`, `folio_backup.json`, etc.) with color-coded extension badges, sizes, timestamps, and `👁` / `✕` buttons.
2. **`verify_r118_about_v118.png`**: Open `About Folio` and capture `Version: Folio v11.8 KFX (Enhanced Typesetting)`.

Package `REPORT.md` + both screenshots into `antig_to_gemini_neon_clunky_report118.zip`.
