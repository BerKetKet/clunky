# Round 129 — Folio v12.9 KFX (Kindle-Native Book Details, Reading-Time Estimate & About Folio Theme)

## Target Device
- Physical **Samsung Galaxy M30** (`32001776e8e82623`), TrebleDroid Android 13 (arm64-v8a).

## Summary of Changes (Drop-In Ready)
- **`FolioMetadataCatalogDialog.java`** (`showBookDetailDialog`):
  1. **Reading-Time Estimate**: Computes an offline reading-time estimate from the KFX payload size and displays it inline on the `Format:` line (e.g., `Format: Enhanced KFX · 775 KB · ~4h 23m read`).
  2. **Kindle-Native Dialog Styling**: Wired `FolioTheme.applyToDialog(ctx, detailDlgRef[0])` onto Book Details so titles render in Bookerly, metadata/controls in Amazon Ember, and footer buttons (`Read Now` `#4DA6FF`, `Edit Metadata` `#C4CBD2`, `Close` `#8E959C`) match native Kindle.
- **`FolioMoreView.java`**:
  - Wired `FolioTheme.applyToDialog(ctx, aboutDlg)` onto `About Folio` and bumped version badge and dialog to `Folio v12.9 KFX`.

## Task A: Build, Sign, Deploy & Stage
1. Copy both files from `dropin_src/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`.
2. Recompile `classes9.dex`, rebuild APK, zipalign, sign, and deploy to physical **Samsung Galaxy M30** (`32001776e8e82623`).
3. Stage release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v12.9_kfx/base.apk`.
4. Confirm `com.amazon.kindle` remains untouched and log zero `com.amazon.kindle.epub` crashes.

## Task B: Hardware Verification on Samsung Galaxy M30 (2 Screenshots)
1. **`verify_r129_book_details_kindle.png`**: Open **Book Details (`ⓘ`)** for any book (e.g., *1984* or *Choke*) and capture the Kindle-native modal showing Bookerly title, Amazon Ember metadata with the new `~Xh Ym read` estimate, and styled `Read Now` / `Edit Metadata` / `Close` footer buttons.
2. **`verify_r129_about_v129.png`**: Open **About Folio** and capture `Version: Folio v12.9 KFX (Enhanced Typesetting)` styled with `FolioTheme`.

Package `REPORT.md` + both screenshots into `antig_to_gemini_neon_clunky_report129.zip`.
