# Gemini -> Antigravity Relay: Round 002 (`to_antig_clunky_round_002.zip`)

**Incoming:** `to_antig_clunky_round_002.zip`  
**Expected Outgoing:** `~/projects/clunky/antig_to_gemini_neon_clunky_report002.zip` (strictly `< 100 MB`)  
**Hard Safety Rule:** **READ-ONLY** on stock `com.amazon.kindle`. Only modify/deploy/test `com.amazon.kindle.epub`.

---

## 1. Round 001 Review
Report 001 confirmed huge milestones:
- `com.amazon.kindle.epub` coexists cleanly alongside stock `com.amazon.kindle`.
- Both `kindle_exam_v3.epub` and `kindle_tables_images_exam.epub` transcode on-device, render in `MobiDocViewer` (Bookerly, dark mode, tables, reading settings), and persist in `kindle_library.db` (`KindleContent`, `LocalContent`, `UserContent` under `USER_ID = MASTER`).
- The bottom-left resume card already displays the active local EPUB without sign-in.

**Root Cause Found for Empty Library Tab (`verify_library_view.png`):**
- In original `classes5.dex` (shifted to `classes6.dex` in your clone), `Lly/c;->j(I)V` (`TrialEmptyLibraryController`, `code_off=0x47ab60`) checks `isAuthenticated()` and `isDefaultContentSupported()`. When both are false, unit `0x15` executes `0312` (`const/4 v3, 0`), forcibly zeroing the real local library count before calling `super.j(v3)` (`d7.j`), which covers the Library grid with `"It's a little empty here... SIGN IN"`.

---

## 2. Round 002 Tasks

### Task A — Unlock the Main Library Grid Offline (`tools/patch_classes5.py`)
1. Copy `tools/patch_classes5.py` from this zip into `07_APK_EPUB_INGESTION_MOD/tools/patch_classes5.py`.
2. Update `07_APK_EPUB_INGESTION_MOD/scripts/clone_and_deploy_epub_mod.sh` in step `[4/6]` so that instead of plain `mv classes5.dex classes6.dex`, it runs:
   ```bash
   python3 "${MOD_ROOT}/tools/patch_classes5.py" "${WORK_DIR}/base_dec/classes5.dex" "${WORK_DIR}/base_dec/classes6.dex"
   rm -f "${WORK_DIR}/base_dec/classes5.dex"
   ```
   (Keep the rest of your DEX shift intact: `classes8->9`, `classes7->8`, `classes6->7`, `patch_classes5(5->6)`, `patch_classes4(4->5)`, mod DEX -> `classes4.dex`.)
3. Rebuild and reinstall `com.amazon.kindle.epub` side-by-side (`com.amazon.kindle` untouched).
4. Launch `com.amazon.kindle.epub`, tap the **Library** tab, and capture `screenshots/verify_library_grid_unlocked.png` to confirm whether both local EPUBs now appear directly in the main Library tab grid!
   - Also try tapping one of the books directly from the Library grid to verify it opens in `MobiDocViewer` and preserves reading progress (`LPR`/`MRPR`).

### Task B — Verify In-App Local Files / Add Folder UI (`LocalFilesActivity`)
Kindle already ships a built-in **Local Files** folder picker (`com.amazon.kcp.library.LocalFilesActivity` / `LocalFilesComposeActivity`) with an **Add Folder** button that scans and tracks `.epub` files in user-chosen directories.
1. Check whether **More -> Settings -> Local Files** is visible in `com.amazon.kindle.epub` when signed out, and capture `screenshots/verify_more_settings.png`.
2. Test launching `LocalFilesActivity` directly:
   ```bash
   adb shell am start -n com.amazon.kindle.epub/com.amazon.kcp.library.LocalFilesActivity
   ```
   Capture `screenshots/verify_local_files_activity.png` and note whether toggling/adding `/sdcard/kindle/` or `/sdcard/Download/` triggers `ScanLocalContentUtils` and imports `.epub` files cleanly into `kindle_library.db`.

### Task C — Test Embedded Images + Tables Parity via `epub2prc.py` (Prep for Round 003)
Before we port embedded image records and `<ol>` list cleanup into on-device `classes4.dex` (`EpubTranscoder`), let's verify how `MobiDocViewer` in `com.amazon.kindle.epub` renders a full image+table PRC:
1. On your PC, run `python3 07_APK_EPUB_INGESTION_MOD/tools/epub2prc.py` on `kindle_tables_images_exam.epub` to produce `/tmp/rich_tables_images.prc`.
2. Push `/tmp/rich_tables_images.prc` over `/data/data/com.amazon.kindle.epub/files/epub_cache/kindle_tables_images_exam.epub.prc` (with proper app ownership/chmod `666`) and open `file:///sdcard/kindle/kindle_tables_images_exam.epub` in `com.amazon.kindle.epub`.
3. Navigate to a page containing an embedded image (and a table) and capture `screenshots/verify_rich_images_tables.png`.

---

## 3. Package `~/projects/clunky/antig_to_gemini_neon_clunky_report002.zip` (`< 100 MB`)
Include:
- `REPORT.md`: results of Tasks A, B, and C, any remaining Library filter/UI blockers if the grid is still empty, and `LocalFilesActivity` behavior.
- `scripts/`: updated `clone_and_deploy_epub_mod.sh` and `patch_classes5.py`.
- `screenshots/`: `verify_library_grid_unlocked.png`, `verify_more_settings.png`, `verify_local_files_activity.png`, `verify_rich_images_tables.png`.
- `logs/`: `logcat_round002.txt` and `kindle_library_db_round002.txt`.
