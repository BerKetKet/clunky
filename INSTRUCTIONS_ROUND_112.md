# Round 112 — Folio v11.2 KFX (Up Next Queue Reorder Manager & Live Sync)

## Summary of Changes (Drop-In Ready)
- **`FolioMoreView.java`**:
  1. **`⇅ Reorder` Queue Manager Modal (`showQueueManagerDialog`)**: Added a compact violet `⇅ Reorder` pill right beside `See All (N) ›` in the Control Center `📌 Up Next Queue` header. Opening it displays all queued books with numbered rank badges (`#1` highlighted in violet, `#2`, ...), standalone `.cover.jpg` thumbnails, `↑` / `↓` reorder controls that persist ordered timestamps to `SharedPreferences("folio_queue")`, inline `✕` remove, and a `Clear Queue` action.
  2. **Live Control Center Sync from `🎲 Tonight's Pick`**: Wired `refreshStats` into `showTonightsPickDialog(ctx, refreshStats)` so toggling `📌 Queue` / `📌 Queued` inside Tonight's Pick immediately updates the `📌 Up Next Queue` carousel behind the modal.
  3. **Version Bump**: Updated Control Center badge and `About Folio` dialog to `Folio v11.2 KFX`.

## Task A: Build, Sign, Deploy & Stage
1. Copy `dropin_src/com/amazon/kindle/epub/catalog/FolioMoreView.java` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/FolioMoreView.java`.
2. Recompile `classes9.dex`, rebuild APK, zipalign, sign, and deploy to Galaxy M32 (`32001776e8e82623`).
3. Stage release APK at `07_APK_EPUB_INGESTION_MOD/release/folio_v11.2_kfx/base.apk`.
4. Confirm `com.amazon.kindle` remains untouched and log zero `com.amazon.kindle.epub` crashes.

## Task B: Hardware Verification (2 Screenshots)
1. **`verify_r112_queue_reorder.png`**: On the Control Center (`Folio v11.2 KFX`), tap `⇅ Reorder` on the `📌 Up Next Queue` header, tap `↑` on `#2` to promote it to `#1`, and capture the modal showing the reordered `#1` / `#2` cards with `↑` / `↓` / `✕` buttons and `Clear Queue` / `Done` footer.
2. **`verify_r112_about_v112.png`**: Open `About Folio` and capture `Version: Folio v11.2 KFX (Enhanced Typesetting)`.

Package `REPORT.md` + both screenshots into `antig_to_gemini_neon_clunky_report112.zip`.
