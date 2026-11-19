# Gemini -> Antigravity Relay: Round 001 (`to_antig_clunky_round_001.zip`)

## 1. New Zip-Only Communication Workflow (Mandatory Going Forward)
To keep collaboration clean and effortless for the user, **all** future communication between Gemini (remote architect) and Antigravity (local PC + rooted ADB worker in `~/projects/clunky`) will happen strictly via sequentially numbered zip files:

- **Incoming from Gemini**: `to_antig_clunky_round_001.zip`, `to_antig_clunky_round_002.zip`, ...
- **Outgoing from Antigravity**: `antig_to_gemini_neon_clunky_report001.zip`, `antig_to_gemini_neon_clunky_report002.zip`, ...
- **Output Location**: Always place your report zip directly in `~/projects/clunky/` (e.g., `~/projects/clunky/antig_to_gemini_neon_clunky_report001.zip`).
- **Size Limit**: Every outgoing report zip MUST be strictly **under 100 MB** (do NOT include raw stock APKs or untouched multi-megabyte `classes*.dex` files; only include small modified files, scripts, text logs, small `.prc` outputs, and PNG screenshots). If an output would ever exceed 95 MB, split it into numbered parts.
- **Standard Report Structure**: Every `antig_to_gemini_neon_clunky_reportNNN.zip` should contain a top-level `REPORT.md` (status, exact actions taken, findings, errors/blockers, and next-step observations) plus clearly named subfolders (`scripts/`, `screenshots/`, `logs/`, `artifacts/`).

## 2. Hard Safety Rule (Read-Only on Stock Kindle)
- **NEVER** write to, modify, force-stop, clear data, or uninstall the stock `com.amazon.kindle` app or its data/APK directories.
- All modifications, deployments, `appops` grants, and database checks apply **exclusively** to the cloned package `com.amazon.kindle.epub`.

## 3. Round 001 Tasks (Sync Your Fixes & Send Baseline Report)
Great job getting `com.amazon.kindle.epub` running side-by-side, fixing the `MANAGE_EXTERNAL_STORAGE` grant, shifting DEX numbering (`classes4..8 -> classes5..9` + mod as `classes4.dex`), fixing `aput-byte` (`0x4f`) and `outs_size=5`, and wiring `ILibraryService.addContentMetadata(...)`!

For **Round 001**, please package your current verified state into `~/projects/clunky/antig_to_gemini_neon_clunky_report001.zip` containing:

1. **`REPORT.md`**:
   - Confirm current state of `com.amazon.kindle.epub` and `com.amazon.kindle` coexistence.
   - Note what is currently visible on screen when opening `kindle_exam_v3.epub` and `kindle_tables_images_exam.epub` (e.g., text rendering, missing images, table layout, font/theme controls).
   - Note whether the Kindle Library UI shows the two registered EPUBs (or if `com.amazon.kindle.epub` currently lands on a sign-in wall when launched from the launcher icon instead of an `ACTION_VIEW` intent).
   - List any remaining issues or rough edges you observed.

2. **`scripts/`** (your exact working copies so Gemini stays 100% synced with your fixes):
   - `clone_and_deploy_epub_mod.sh`
   - `patch_classes4.py`
   - `build_classes9.py` (your fixed version with `0x4f` and `addContentMetadata`)
   - Any other helper scripts you created or modified in `07_APK_EPUB_INGESTION_MOD/`.

3. **`artifacts/`**:
   - Your small compiled mod DEX (the one injected as `classes4.dex`, ~7-8 KB; do NOT include the 8.5 MB stock/patched `classes5.dex`).
   - The two transcoded `.prc` files pulled from `/data/data/com.amazon.kindle.epub/files/epub_cache/`:
     - `kindle_exam_v3.epub.prc`
     - `kindle_tables_images_exam.epub.prc`

4. **`screenshots/`**:
   - `verify_cloned_kindle_epub.png` (existing screenshot of `kindle_exam_v3.epub`).
   - A fresh screenshot `verify_tables_images_exam.png` showing `kindle_tables_images_exam.epub` open in `com.amazon.kindle.epub` (`MobiDocViewer`).
   - A screenshot `verify_launcher_or_library.png` showing what happens when launching `com.amazon.kindle.epub` normally from its main launcher activity (to see whether the Library view is accessible offline or blocked by sign-in).

5. **`logs/`**:
   - `kindle_library_db_dump.txt`: output of querying `KindleContent`, `LocalContent`, and `UserContent` in `/data/data/com.amazon.kindle.epub/databases/kindle_library.db`.
   - `logcat_filtered.txt`: filtered `adb logcat -d` covering `ThirdPartyUpgradePage`, `ContentScheme`, `EpubTranscoder`, `LoadContent`, `MobiDocViewer`, `StandAloneBookReaderActivity`, and any `AndroidRuntime`/`ART` warnings or exceptions.

## 4. Verification Before Finishing Round 001
- Confirm `ls -lh ~/projects/clunky/antig_to_gemini_neon_clunky_report001.zip` exists and is `< 100 MB`.
- Tell the user in one short line that `antig_to_gemini_neon_clunky_report001.zip` is ready to upload to Gemini.
