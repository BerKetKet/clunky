# Round 130 (Major Milestone) — Folio v13.0 KFX (Reading Speed WPM Calibration, Pure-Kindle Book Details & GitHub Sync)

## Target Device
- Physical **Samsung Galaxy M30** (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).

## Summary of Changes (Drop-In Ready)
- **`FolioMoreView.java`**:
  1. **Reading Speed (`WPM`) Calibration**: Added a 4-pill `READING SPEED (WPM)` selector (`180 wpm`, `250 wpm`, `330 wpm`, `420 wpm`, persisted in `SharedPreferences("folio_ui_prefs", "reading_speed_wpm")`) right inside the **2026 Reading Goal** modal.
  2. **Milestone Version Bump**: Bumped Control Center badge and `About Folio` dialog to **Milestone `Folio v13.0 KFX`**.
- **`FolioMetadataCatalogDialog.java`**:
  1. **Personalized Reading-Time Estimate**: Scales the `~Xh Ym read` estimate on the `Format:` line dynamically by the user's configured `reading_speed_wpm`.
  2. **Pure-Kindle Book Details Border Sweep**: Unified the remaining alpha-prefixed brown/indigo borders (`Favorited`, `Shelf`, `Personal Note`, `Up Next`) to Kindle blue-slate `#2C5278` / `#162330`.

## Task A: Build, Sign, Deploy & Stage
1. Copy both files from `dropin_src/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`.
2. Recompile `classes9.dex`, rebuild APK, zipalign, sign, and deploy to physical **Samsung Galaxy M30** (`32001776e8e82623`).
3. Stage release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v13.0_kfx/base.apk`.
4. Confirm `com.amazon.kindle` remains untouched and log zero `com.amazon.kindle.epub` crashes.

## Task B: Hardware Verification on Samsung Galaxy M30 (2 Screenshots)
1. **`verify_r130_reading_goal_wpm.png`**: Open **2026 Reading Goal** (`v13.0 KFX`) and capture the modal showing the new **`READING SPEED (WPM)`** 4-pill selector (`180 wpm`, `250 wpm`, `330 wpm`, `420 wpm`).
2. **`verify_r130_book_details_pure_kindle.png`**: Open **Book Details (`ⓘ`)** for *1984* and capture the pure-Kindle modal (zero brown/indigo borders on `Favorited`, `Classics`, and `Personal Note`) with the calibrated `~Xh Ym read` estimate.

## Task C: Commit & Push Milestone v13.0 KFX to GitHub
1. Stage and commit the Milestone v13.0 KFX changes (`Folio v13.0 KFX: Reading Speed WPM Calibration & Pure-Kindle UI Milestone`) and push to `origin/master`.
2. Record the commit SHA and branch in `REPORT.md`.

Package `REPORT.md` + both screenshots into `antig_to_gemini_neon_clunky_report130.zip`.
