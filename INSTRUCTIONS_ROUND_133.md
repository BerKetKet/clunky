# Round 133 — Folio v13.3 KFX (Kindle-Native Reading Log Modal & Editorial Offline HTML Export)

## Target Device
- Physical **Samsung Galaxy M30** (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).

## Summary of Changes (Drop-In Ready)
- **`FolioMetadataBackup.java`** (`exportLibraryHtml`):
  - Redesigned the exported `/sdcard/kindle/backups/folio_library.html` offline catalog CSS to match Folio's **Kindle-Native Editorial Dark Theme** (`#000000` canvas, `#141618` cards, `1px #262A2E` hairlines, Georgia serif display headings & book titles, `#4DA6FF` Kindle blue progress bar & badges, and `#162330` / `#2C5278` note callout — zero neon colors).
- **`FolioMoreView.java`**:
  - Wired `FolioTheme.applyToDialog(ctx, logDlg)` onto **Reading Log & Export** (`showReadingLogDialog`), synced `rtHdr` click-to-calibrate listener, and bumped Control Center badge and `About Folio` dialog to `Folio v13.3 KFX`.

## Task A: Build, Sign, Deploy & Stage
1. Copy both files from `dropin_src/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`.
2. Recompile `classes9.dex`, rebuild APK, zipalign, sign, and deploy to physical **Samsung Galaxy M30** (`32001776e8e82623`).
3. Stage release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v13.3_kfx/base.apk`.
4. Confirm `com.amazon.kindle` remains untouched and log zero `com.amazon.kindle.epub` crashes.

## Task B: Hardware Verification on Samsung Galaxy M30 (2 Screenshots)
1. **`verify_r133_reading_log_kindle.png`**: Open **Reading Log & Export** from the Control Center, tap **HTML** to regenerate `/sdcard/kindle/backups/folio_library.html` (report its updated byte size in `REPORT.md`), and capture the Kindle-native Reading Log modal.
2. **`verify_r133_about_v133.png`**: Open **About Folio** and capture `Version: Folio v13.3 KFX (Enhanced Typesetting)`.

Package `REPORT.md` + both screenshots into `antig_to_gemini_neon_clunky_report133.zip`.
