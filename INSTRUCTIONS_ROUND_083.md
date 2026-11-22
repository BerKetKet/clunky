# Round 083 — Folio v8.3 KFX (Custom Shelves Manager)

## Task A: Drop-In Build & Deploy
1. Copy the single drop-in file into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` -> `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`
2. Do **NOT** overwrite `MetadataEnricher.java` or `OpenCatalogView.java`.
3. Run `compile_mod_dex.sh`, `rebuild_fast.sh`, align/sign, install on Galaxy M32 (`32001776e8e82623`), and stage the release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v8.3_kfx/base.apk`.

## What's New in Folio v8.3 KFX
- **🏷 Manage Shelves** action card added to Control Center right under `📊 Library Insights`.
- Tapping opens the dark **Manage Shelves** modal listing all custom shelves from `folio_shelves` sorted by book count descending:
  - Tap any shelf name/count (`🏷 Classics · 1 book ›`) to jump straight into the filtered Metadata Catalog (`SHELF:Classics`).
  - Tap **Rename** to rename (or merge) a shelf across all assigned books in place.
  - Tap **Remove** to clear a shelf across all assigned books (with confirmation; books/metadata stay untouched).
- Version badge and About Folio bumped to **Folio v8.3 KFX (Enhanced Typesetting)**.

## Task B: Minimal Hardware Verification (2 Screenshots)
1. `verify_r83_control_center_v83.png` — Control Center showing the **Folio v8.3 KFX** badge and the new **🏷 Manage Shelves** card.
2. `verify_r83_manage_shelves.png` — Tap **🏷 Manage Shelves** to capture the modal showing `🏷 Classics` (`1 book ›`) with **Rename** and **Remove** pills (also confirm tapping `🏷 Classics` opens the filtered catalog, without needing an extra screenshot).
3. Return `antig_to_gemini_neon_clunky_report083.zip` with `REPORT.md` and both screenshots, plus stock `com.amazon.kindle` untouched check.
