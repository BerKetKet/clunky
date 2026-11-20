# Kindle Android v8.159.0.100 - Deployment & Restoration Notes

## Status
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Kindle EPUB") deployed and fully verified alongside unmodified stock `com.amazon.kindle`. Round 002 report zip packaged.
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, read-only)
  - Clone: `com.amazon.kindle.epub` (cloned with EPUB ingestion mod + offline Library grid unlock)

## Cloned Package Architecture & Modifications
1. **Manifest Isolation**:
   - Stripped `android:sharedUserId="com.amazon"`.
   - Renamed package to `com.amazon.kindle.epub` with label `"Kindle EPUB"`.
   - Namespaced all 19 `android:authorities` and 16 custom `<permission>` declarations to avoid installation collisions (`INSTALL_FAILED_DUPLICATE_PERMISSION`).
   - Added `<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"/>` and enabled via `appops` to allow Android 13 Scoped Storage file access to `/sdcard/kindle/`.
2. **DEX Layout & Shifting**:
   - Shifted `classes7..8.dex` to `classes8..9.dex`.
   - Shifted `classes6.dex` to `classes7.dex`.
   - Patched `classes5.dex` via `patch_classes5.py` (`Lly/c;->j(I)V` NOP at `0x47ab9a`) and placed as `classes6.dex`.
   - Shifted patched `classes4.dex` (`ThirdPartyUpgradePage` bypass) to `classes5.dex`.
   - Injected enhanced mod DEX as `classes4.dex` so ART resolves enhanced `dp.g` first.
3. **Bytecode Fixes in `classes9.dex` (`build_classes9.py`)**:
   - Corrected `putIntBE` and `putShortBE` opcodes from `0x4c` (`aput-wide`) to `0x4f` (`aput-byte`).
   - Fixed `outs_size` to 5 in `toFixed32Bytes` for `System.arraycopy`.
   - Enhanced `registerInLibrary`: calls `ILibraryService.addContentMetadata(singletonList(metadata))` to guarantee entries are written to `KindleContent` and `LocalContent` in `kindle_library.db` even without an Amazon account signed in, plus conditional `ILibraryService.W(userId, ...)` when `userId != null`.
   - Wired `registerInLibrary` into both `dp.g.a(str, file)` and `dp.g.e(str, file, state, id)`.
4. **Offline Library Grid Unlock (`patch_classes5.py`)**:
   - NOPed unit `0x15` (`0x0312` -> `0x0000`) in `Lly/c;->j(I)V` (`TrialEmptyLibraryController.updateLibraryCount`).
   - Prevents `v3` from being zeroed when signed out (`!isAuthenticated() && !isDefaultContentSupported()`).
   - Real local item count from `kindle_library.db` passes to `super.j(count)` (`d7.j`), completely removing `EmptyLibraryView` and unlocking the full Library grid offline.

## Verification Evidence
1. **Package Coexistence**:
   - `pm list packages | grep kindle` confirms both `com.amazon.kindle` and `com.amazon.kindle.epub` installed simultaneously.
   - Stock `com.amazon.kindle` remains untouched.
2. **Round 002 Tasks Verified**:
   - **Task A (Library Grid Unlocked)**: Verified `verify_library_grid_unlocked.png`. All local EPUBs and samples appear directly in the Library tab grid. Tapping a book opens `StandAloneBookReaderActivity` and preserves reading position (`LPR`/`MRPR`).
   - **Task B (Local Files UI & Folder Scan)**: Verified `verify_more_settings.png` and `verify_local_files_activity.png`. "Local file access" is visible offline under More -> Settings. Toggling `kindle` initiates SAF document tree permission; once granted, `ScanLocalContentUtils` scans all EPUBs in `/sdcard/kindle/` and imports them into `kindle_library.db`.
   - **Task C (Rich Images & Tables)**: Verified `verify_rich_images_tables.png`. `rich_tables_images.prc` generated via `epub2prc.py` (9 text records, 15 image records, 412 KB). Inline image inside table cell (`OEBPS/t5_flow.xhtml`) renders cleanly in `MobiDocViewer` with 7,500 matching color pixels verified on screen.
3. **Round 002 Deliverable**:
   - Packaged to `antig_to_gemini_neon_clunky_report002.zip` (753 KB, contains `REPORT.md`, `scripts/`, `screenshots/`, `logs/`).

## Next Steps
- Receive Round 003 instructions from Gemini.
- Port embedded image parsing and `<img recindex="..."/>` rewriting from `epub2prc.py` directly into on-device `EpubTranscoder` (`classes4.dex`).
