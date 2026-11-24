# Round 113 — Folio v11.3 KFX (Smart Metadata Quick-Fix Toolbar)

## Summary of Changes (Drop-In Ready)
- **`FolioMetadataCatalogDialog.java`** (`showEditMetadataDialog`):
  1. **Smart Quick-Fix Toolbar**: Added a 3-pill toolbar right beneath the Title & Author inputs in `Edit Book Metadata`:
     - **`⇄ Swap T/A`** (violet): Instantly swaps Title & Author fields (1-tap fix for EPUBs with inverted OPF metadata like *Palahniuk, Chuck / Choke*).
     - **`Aa Fix Author`** (sky-blue): Normalizes `Last, First` -> `First Last` via `formatAuthorForDisplay(...)`.
     - **`Aa Title Case`** (amber): Formats lowercase or inconsistent titles via `toSmartTitleCase(...)`.
  2. **Immediate In-Memory Record Sync**: Saving updates `rec` fields in memory alongside `books_metadata` SQLite + `kindle_library.db` sync.
- **`FolioMoreView.java`**:
  - Bumped Control Center header badge and `About Folio` dialog to `Folio v11.3 KFX`.

## Task A: Build, Sign, Deploy & Stage
1. Copy both files from `dropin_src/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`.
2. Recompile `classes9.dex`, rebuild APK, zipalign, sign, and deploy to Galaxy M32 (`32001776e8e82623`).
3. Stage release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v11.3_kfx/base.apk`.
4. Confirm `com.amazon.kindle` remains untouched and log zero `com.amazon.kindle.epub` crashes.

## Task B: Hardware Verification (2 Screenshots)
1. **`verify_r113_edit_metadata_quickfix.png`**: Open Book Details (`ⓘ`) for *Palahniuk, Chuck* (*Choke*), tap `Edit Metadata`, tap **`⇄ Swap T/A`** followed by **`Aa Fix Author`** so Title reads `Choke` and Author reads `Chuck Palahniuk`, and capture the `Edit Book Metadata` dialog showing the 3 quick-fix pills (`⇄ Swap T/A`, `Aa Fix Author`, `Aa Title Case`) and cleaned fields, then tap **Save** to persist the fix!
2. **`verify_r113_about_v113.png`**: Open `About Folio` and capture `Version: Folio v11.3 KFX (Enhanced Typesetting)`.

Package `REPORT.md` + both screenshots into `antig_to_gemini_neon_clunky_report113.zip`.
