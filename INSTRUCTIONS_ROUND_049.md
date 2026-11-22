# Round 049 Instructions — Pass-3 Missing-Metadata Auto-Repair & Clean Author Formatting (Folio v4.5 KFX, Ultra-Low Worker Effort)

**Target Package:** `com.amazon.kindle.epub` (Folio)  
**Hard Safety Invariant:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`).  
**Division of Labor:** Gemini edited all 3 drop-in Java files directly in `dropin_src/`. Just copy them in, compile, deploy, tap `Sync & Enrich`, and capture 2 quick screenshots!  
**Reply Archive Budget:** Package ONLY Round 049 files (`REPORT.md`, `INSTRUCTIONS_ROUND_049.md`, `protocol.json`, `screenshots/*.png`) into `antig_to_gemini_neon_clunky_report049.zip` (< 5 MB).

---

## Round 048 Visual QC Feedback

- **PASS** on both `Folio v4.4 KFX` proofs (`verify_r48_ranked_genre_chips_counts.png`, `verify_r48_control_center_v44.png`): `All (27)`, `History (5)`, `Fiction (4)`, `Biography (2)`, `Correspondence (2)` look crisp with zero `--` LC strings!
- **2 Subtle Catches Fixed in Round 049**:
  - `1984`, `Moksha`, and `Crome Yellow` were imported before Round 042's ISBN→title fallback and still lacked genres/pub-date — new **Pass 3** in `LibraryMetadataBackfiller.java` repairs missing genres/pub-date on already-indexed books when tapping `Sync & Enrich`!
  - `"Melville, Herman"` on *Moby Dick* now displays cleanly as `"Herman Melville"`!

---

## Task A: Copy, Compile & Deploy (`Folio v4.5 KFX`)

1. Copy all 3 `.java` files from `dropin_src/` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/` preserving `ingest/` and `catalog/`.
2. Run `compile_mod_dex.sh` + `rebuild_fast.sh` and install on Galaxy M32 (`32001776e8e82623`).
3. Stage signed APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v4.5_kfx/base.apk`.

---

## Task B: Fast 2-Screenshot Hardware Proof

1. Open **Home** → **All Metadata ›** → tap **`Sync & Enrich`** and wait for completion.
2. `screenshots/verify_r49_pass3_enriched_all.png`: Show **Library Metadata & Genres (27)** proving *Moby Dick* displays `"Herman Melville"` and *1984* / *Moksha* / *Crome Yellow* now have enriched genres/pub dates!
3. `screenshots/verify_r49_about_v45.png`: Control Center **About Folio** showing **`Folio v4.5 KFX`**.
4. Confirm stock `com.amazon.kindle` remains 100% untouched (`lastUpdateTime=2026-11-19 21:59:23`).
