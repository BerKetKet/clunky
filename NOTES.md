# Kindle Android v8.159.0.100 EPUB Ingestion Mod - Deployment & Verification Notes

## Status
- **Current State**: Deployed & Verified. Local EPUB ingestion mod is operational on-device under package `com.amazon.kindle`.
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux).
- **Target Package**: `com.amazon.kindle` (versionCode=1286541811, versionName=8.159.0.100).
- **Original Backup**: Saved untouched at `/tmp/kindle_mod/base_orig.apk`.

## Root Cause Fixes in Mod Bytecode & Packaging
1. **DEX Verifier Failure in `classes4.dex`**:
   - *Problem*: Stock `classes4.dex` had `class_def #8367` (`Ldp/g;`) removed via shifting `class_defs` in `patch_classes4.py`. However, ART's verifier (`dex_file_verifier.cc::CheckInterClassDataItem`) requires that every non-empty `class_data_item` in `TYPE_CLASS_DATA_ITEM` (offset `0x7cd781`) have a declaring `class_def`. Because it was orphaned, ART rejected `classes4.dex` with `Could not find declaring class for non-empty class data item`.
   - *Solution*: Re-applied patches 1–7 to original `classes4.dex` while keeping `class_defs` intact. Inserted `classes9.dex` (containing enhanced `Ldp/g;`, `EpubBookBuilder`, and `EpubTranscoder`) as `classes4.dex`, shifting original `classes4.dex` to `classes5.dex` (and existing 5..8 to 6..9). MultiDex `PathClassLoader` resolves `Ldp/g;` from the new `classes4.dex` first, while all DEX structures maintain 100% valid schema and intra-item integrity.
2. **Dalvik Verifier Rejection in `build_classes9.py`**:
   - *Opcode Bug*: In `EpubTranscoder.putIntBE` and `putShortBE`, opcode `0x4c` (`aput-wide`) was used instead of `0x4f` (`aput-byte`), triggering `VerifyError: [0x3] put insn has type 'Long (Low Half)' but expected type 'Byte'`.
   - *outsSize Bug*: In `EpubTranscoder.toFixed32Bytes`, `make_code_item` configured `outs_size = 4`, but the method invoked `System.arraycopy` which requires 5 arguments, triggering `VerifyError: [0x1C] invalid argument count (5) exceeds outsSize (4)`.
   - *Solution*: Replaced `0x4c` with `0x4f` and increased `outs_size` to 5 in `build_classes9.py`. Recompiled `classes9.dex`.
3. **Zipalign & Runtime Optimization**:
   - Zipaligned APK with 4-byte / page alignment (`zipalign -f -p 4`).
   - Recompiled cleanly on device via `cmd package compile -m speed-profile -f com.amazon.kindle` (exited `Success`, generated `base.art`, `base.odex`, `base.vdex`).
4. **Android 13 Storage Permission**:
   - Granted `READ_EXTERNAL_STORAGE` and `MANAGE_EXTERNAL_STORAGE` appop to allow `com.amazon.kindle` to read local EPUB files under `/sdcard/Download/` and `/sdcard/kindle/`.

## Verified Against Real State
- **Compilation**: `cmd package compile -m speed-profile -f com.amazon.kindle` returned `Success`.
- **Cache Pre-population**: Verified `/data/data/com.amazon.kindle/files/epub_cache/` containing pre-transcoded `.prc` files (`kindle_exam_v3.epub.prc` and `kindle_tables_images_exam.epub.prc`) with owner `u0_a420:u0_a420` and mode `0666`.
- **View Intent Execution**:
  - `file:///sdcard/Download/kindle_tables_images_exam.epub`: Launched via `ThirdPartyUpgradePage` VIEW intent -> successfully opened in `com.amazon.kindle/com.amazon.kcp.reader.StandAloneBookReaderActivity` via `MobiDocViewer`.
  - `file:///sdcard/Download/kindle_exam_v3.epub`: Launched via `ThirdPartyUpgradePage` VIEW intent -> successfully displayed and focused `StandAloneBookReaderActivity`.
- **Focus Verification**: `dumpsys window | grep mCurrentFocus` verified `Window{... com.amazon.kindle/com.amazon.kcp.reader.StandAloneBookReaderActivity}` as current foreground window.
- **Visual Capture**: Screenshot captured at `~/projects/clunky/verify_epub_kindle.png`.

## Next Steps
- Verify in-app Library scanning for sideloaded books under `/sdcard/kindle/`.
- Verify ACTION_SEND "Share to Kindle" intent from third-party file managers.
