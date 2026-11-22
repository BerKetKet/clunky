# Round 096 — Folio v9.6 KFX (1-Tap Sort Picker Modal, Direction Toggle & Size/Queue Sorts)

## Task A: Drop-In Build & Deploy
1. Copy both drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
2. Do **NOT** overwrite `MetadataEnricher.java` or `OpenCatalogView.java`.
3. Run `compile_mod_dex.sh`, `rebuild_fast.sh`, align/sign, install on Galaxy M32 (`32001776e8e82623`), and stage the release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v9.6_kfx/base.apk`.

## What's New in Folio v9.6 KFX
- **1-Tap Dark `Sort Catalog` Picker Modal (`FolioMetadataCatalogDialog.java`)**:
  - Tapping the `Sort: ... ↓` button now opens a clean dark **Sort Catalog** modal listing all **9 sort modes** (`Recently Added`, `Title (A–Z)`, `Author (A–Z)`, `Publication Year`, `Reading Progress`, `Favorites First`, `Star Rating`, **`File Size (Largest)`**, and **`📌 Up Next Queue First`**) with an amber `✓` on the active sort.
  - Added a persistent **`↓ Standard / ↑ Reversed`** direction toggle pill in the modal header (persisted in `SharedPreferences("folio_ui_prefs", "catalog_sort_rev")`) and live arrow indicator (`↓`/`↑`) on the top-bar `Sort:` pill.
- Version badge and About Folio bumped to **Folio v9.6 KFX (Enhanced Typesetting)**.

## Task B: Minimal Hardware Verification (2 Screenshots)
1. `verify_r96_sort_picker_modal.png` — Open Metadata Catalog and tap **`Sort: ... ↓`** to capture the new dark **Sort Catalog** picker modal showing the `↓ Standard` direction toggle and all 9 sort rows (including `File Size (Largest)` and `📌 Up Next Queue First`).
2. `verify_r96_about_v96.png` — About Folio modal showing `Version: Folio v9.6 KFX (Enhanced Typesetting)` and live stats.
3. Return `antig_to_gemini_neon_clunky_report096.zip` with `REPORT.md` and both screenshots, plus stock `com.amazon.kindle` untouched check.
