# Round 091 — Folio v9.1 KFX (Up Next Queue Export, Backup & Telemetry Integration)

## Task A: Drop-In Build & Deploy
1. Copy all 3 drop-in files into `07_APK_EPUB_INGESTION_MOD/mod_src/real/`:
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataCatalogDialog.java`
   - `dropin_src/com/amazon/kindle/epub/ingest/FolioMetadataBackup.java`
   - `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java`
2. Do **NOT** overwrite `MetadataEnricher.java` or `OpenCatalogView.java`.
3. Run `compile_mod_dex.sh`, `rebuild_fast.sh`, align/sign, install on Galaxy M32 (`32001776e8e82623`), and stage the release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v9.1_kfx/base.apk`.

## What's New in Folio v9.1 KFX
- **Full Queue Backup & Export (`FolioMetadataBackup.java`)**:
  - JSON Backup & Restore now preserves `in_queue` state across full library backups/restores.
  - Markdown Reading Log (`folio_reading_log.md`) now appends a dedicated **`## 📌 Up Next Queue (N)`** section listing all queued books.
  - RFC-4180 CSV Export (`folio_library.csv`) now includes an **`In Queue`** column (`Yes` / empty).
- **Queue Card Badge & Clean Quote Fix (`FolioMetadataCatalogDialog.java`)**:
  - Synced the clean quote-escaped Toast string in Book Details.
  - List view book titles now display a `📌` prefix badge when queued (alongside `★` and `📝`).
- **Library Insights Queue Telemetry & Version Bump (`FolioMoreView.java`)**:
  - Library Insights header summary now includes live queued count (`1 fav · 1 noted · 1 shelved · 1 queued · 5.0★ avg (1 rated)`).
  - Bumped Control Center badge and About Folio to **Folio v9.1 KFX (Enhanced Typesetting)**.

## Task B: Minimal Hardware Verification (2 Screenshots)
1. `verify_r91_reading_log_up_next.png` — Open **📄 Reading Log & Export** to verify the Markdown preview shows the new **`## 📌 Up Next Queue (1)`** section (*Animal farm: a fairy story — George Orwell*).
2. `verify_r91_insights_queued_v91.png` — Open **📊 Library Insights** to verify the updated summary line includes `1 queued` (and confirm `Folio v9.1 KFX` badge/About in `REPORT.md`).
3. Return `antig_to_gemini_neon_clunky_report091.zip` with `REPORT.md` and both screenshots, plus stock `com.amazon.kindle` untouched check.
