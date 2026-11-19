# Kindle Android v8.159.0.100 - Deployment & Restoration Notes

## Status
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Kindle EPUB") deployed and fully verified alongside unmodified stock `com.amazon.kindle`.
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, read-only)
  - Clone: `com.amazon.kindle.epub` (cloned with EPUB ingestion mod)

## Cloned Package Architecture & Modifications
1. **Manifest Isolation**:
   - Stripped `android:sharedUserId="com.amazon"`.
   - Renamed package to `com.amazon.kindle.epub` with label `"Kindle EPUB"`.
   - Namespaced all 19 `android:authorities` and 16 custom `<permission>` declarations to avoid installation collisions (`INSTALL_FAILED_DUPLICATE_PERMISSION`).
   - Added `<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"/>` and enabled via `appops` to allow Android 13 Scoped Storage file access to `/sdcard/kindle/`.
2. **DEX Layout & Shifting**:
   - Preserved `class_defs` integrity in `classes4.dex` (avoiding ART verifier rejection from orphaned `class_data_item`s).
   - Shifted `classes5..8.dex` to `classes6..9.dex`, placed patched `classes4.dex` as `classes5.dex`, and injected the enhanced mod DEX as `classes4.dex` so ART resolves enhanced `dp.g` first.
3. **Bytecode Fixes in `classes9.dex` (`build_classes9.py`)**:
   - Corrected `putIntBE` and `putShortBE` opcodes from `0x4c` (`aput-wide`) to `0x4f` (`aput-byte`).
   - Fixed `outs_size` to 5 in `toFixed32Bytes` for `System.arraycopy`.
   - Enhanced `registerInLibrary`: calls `ILibraryService.addContentMetadata(singletonList(metadata))` to guarantee entries are written to `KindleContent` and `LocalContent` in `kindle_library.db` even without an Amazon account signed in, plus conditional `ILibraryService.W(userId, ...)` when `userId != null`.
   - Wired `registerInLibrary` into both `dp.g.a(str, file)` and `dp.g.e(str, file, state, id)`.

## Verification Evidence
1. **Package Coexistence**:
   - `pm list packages | grep kindle` confirms both `com.amazon.kindle` and `com.amazon.kindle.epub` installed simultaneously.
2. **Dynamic Transcoding**:
   - Verified in `/data/data/com.amazon.kindle.epub/files/epub_cache/`:
     - `kindle_exam_v3.epub.prc` (89,867 bytes)
     - `kindle_tables_images_exam.epub.prc` (36,303 bytes)
3. **Database Library Tracking**:
   - Queried `/data/data/com.amazon.kindle.epub/databases/kindle_library.db`:
     - `KindleContent`:
       - `AMZNID0/B0B164901B/0/` | `Kindle Render Exam v3` | `Exam` | `LOCAL`
       - `AMZNID0/B0EC17DC48/0/` | `Kindle Tables and Images Exam` | `Local EPUB Author` | `LOCAL`
     - `LocalContent`:
       - `AMZNID0/B0B164901B/0/` -> `/data/user/0/com.amazon.kindle.epub/files/epub_cache/kindle_exam_v3.epub.prc`
       - `AMZNID0/B0EC17DC48/0/` -> `/data/user/0/com.amazon.kindle.epub/files/epub_cache/kindle_tables_images_exam.epub.prc`
4. **Reader Rendering**:
   - Confirmed `StandAloneBookReaderActivity` with `MobiDocViewer` active.
   - Captured screenshot to `verify_cloned_kindle_epub.png`.
5. **Stock App Invariant**:
   - Stock `com.amazon.kindle` was never stopped, modified, or written to.

