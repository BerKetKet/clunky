# Round 126 — Folio v12.6 KFX (Strict Bookerly/Ember Hierarchy & Modal-Wide Theme Unification)

## Summary of Changes (Drop-In Ready)
- **`FolioTheme.java`**:
  - Refined typography routing so **Bookerly** applies strictly to display headings (`>= 17sp`), while all eyebrows, pills, chips, buttons, and body text (`< 17sp`) render in crisp **Amazon Ember** sans-serif (matching native Kindle).
  - Added `FolioTheme.applyToDialog(ctx, dlg)` to style both modal view trees and AlertDialog footer buttons (`#4DA6FF` Positive, `#8E959C` Negative, `#C4CBD2` Neutral) in one call.
- **`FolioFolderBrowserDialog.java`**:
  - Replaced the last leftover amber/brown border (`#2D2312` / `#B45309`) on the active pinned-folder `★` button (`btnPinDir`) with Kindle blue-slate (`#162330` / `#2C5278`).
- **`FolioMoreView.java` & `FolioMetadataCatalogDialog.java`**:
  - Wired `FolioTheme.applyToDialog(...)` across Control Center sub-modals (Reading Goal, Tonight's Pick, Queue Reorder, Reading Notes, Manage Shelves, Storage & Duplicates, Library Insights, Quick Search) and Catalog sub-modals (Edit Metadata, Authors Directory).
  - Bumped header badge and `About Folio` dialog to `Folio v12.6 KFX`.

## Task A: Build, Sign, Deploy & Stage
1. Copy all 4 Java files from `dropin_src/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/` (keeping the Round 125 crimson launcher icon in place).
2. Recompile `classes9.dex`, rebuild APK, zipalign, sign, and deploy to Galaxy M32 (`32001776e8e82623`).
3. Stage release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v12.6_kfx/base.apk`.
4. Confirm `com.amazon.kindle` remains untouched and log zero `com.amazon.kindle.epub` crashes.

## Task B: Hardware Verification (2 Screenshots)
1. **`verify_r126_importer_ember_and_pin.png`**: Open **Import EPUBs from Storage** and verify: Bookerly heading (`Import EPUBs from Storage`), Amazon Ember sans on eyebrows/pills (`BATCH EPUB IMPORTER`, `Sort: A–Z`, `Hide Imported: OFF`), and the active `★` pin button now using Kindle blue-slate `#162330` / `#2C5278` (zero brown/amber border).
2. **`verify_r126_modal_bookerly_ember.png`**: Open **2026 Reading Goal** (or **Manage Shelves**) from the Control Center and capture the unified Bookerly heading, Amazon Ember body, Kindle blue `#4DA6FF` highlights, and styled AlertDialog footer buttons.

Package `REPORT.md` + both screenshots into `antig_to_gemini_neon_clunky_report126.zip`.
