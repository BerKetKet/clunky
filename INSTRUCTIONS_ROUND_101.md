# Round 101 — Folio v10.1 KFX (Quick Search Scope Filter Pills & Recent Search History)

## Task A: Drop-In Build & Deploy
1. Copy the single drop-in file into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
2. Do **NOT** overwrite `MetadataEnricher.java` or `OpenCatalogView.java`.
3. Run `compile_mod_dex.sh`, `rebuild_fast.sh`, align/sign, install on Galaxy M32 (`32001776e8e82623`), and stage the release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v10.1_kfx/base.apk`.

## What's New in Folio v10.1 KFX
- **Quick Search Scope Pills (`FolioMoreView.java`)**:
  - Added 5 instant scope filter pills directly under the search input inside **🔍 Quick Library Search**: `All`, `📖 Reading`, `📝 Notes`, `📌 Queue`, and `🏷 Shelves`.
  - Tapping any scope pill immediately narrows results to that category (and combines live with any search text).
- **Persistent Recent Search History (`FolioMoreView.java`)**:
  - Persists up to 5 recent queries in `SharedPreferences("folio_ui_prefs", "recent_global_searches")` whenever a user opens a book or taps `Open in Catalog`, rendering tappable `🕒 <query>` chips + `Clear` when the search bar is empty.
- Version badge and About Folio bumped to **Folio v10.1 KFX (Enhanced Typesetting)**.

## Task B: Minimal Hardware Verification (2 Screenshots)
1. `verify_r101_search_scope_pills.png` — Tap **`🔍 Search`** in the Control Center header and tap **`📖 Reading`** (or **`📝 Notes`**) to capture the Quick Library Search modal showing the new 5 scope filter pills with active amber highlight and filtered results.
2. `verify_r101_about_v101.png` — About Folio modal showing `Version: Folio v10.1 KFX (Enhanced Typesetting)` and live stats.
3. Return `antig_to_gemini_neon_clunky_report101.zip` with `REPORT.md` and both screenshots, plus stock `com.amazon.kindle` untouched check.
