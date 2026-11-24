# Round 114 — Folio v11.4 KFX (Authors Directory Sort Pills & Progress Telemetry)

## Summary of Changes (Drop-In Ready)
- **`FolioMetadataCatalogDialog.java`** (`showAuthorsDirectoryDialog`):
  1. **Interactive Sort Switcher**: Added 3 sort pills (`Most Books`, `A–Z`, `Most Read`) above the author search bar for instant re-ranking.
  2. **Monogram Avatars & Per-Author Reading Telemetry**: Each author card now displays a circular initials badge (teal for active/finished authors, amber for unread) and a live telemetry sub-line showing finished count, in-progress count, and average star rating (e.g., `✓ 1 finished`, `📖 1 reading · ★ 5.0`).
- **`FolioMoreView.java`**:
  - Bumped Control Center header badge and `About Folio` dialog to `Folio v11.4 KFX`.

## Task A: Build, Sign, Deploy & Stage
1. Copy both files from `dropin_src/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`.
2. Recompile `classes9.dex`, rebuild APK, zipalign, sign, and deploy to Galaxy M32 (`32001776e8e82623`).
3. Stage release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v11.4_kfx/base.apk`.
4. Confirm `com.amazon.kindle` remains untouched and log zero `com.amazon.kindle.epub` crashes.

## Task B: Hardware Verification (2 Screenshots)
1. **`verify_r114_authors_directory.png`**: Open `Library Metadata & Genres`, tap `👤` in the top toolbar to open `👤 Authors Directory`, tap **`Most Read`** so active/finished/rated authors bubble to the top, and capture the modal showing the 3 sort pills, monogram avatars, and per-author reading/rating telemetry.
2. **`verify_r114_about_v114.png`**: Open `About Folio` and capture `Version: Folio v11.4 KFX (Enhanced Typesetting)`.

Package `REPORT.md` + both screenshots into `antig_to_gemini_neon_clunky_report114.zip`.
