# Kindle Android v8.159.0.100 - Deployment & Restoration Notes

## Status
- **Current State**: Cloned package `com.amazon.kindle.epub` rebranded to **Folio** with custom midnight-navy/amber launcher icon (`folio_launcher_icon.png`), deployed and fully verified alongside unmodified stock `com.amazon.kindle`. Round 012 complete: "Folio" launcher icon & label verified on device launcher, 4-tier Gutenberg mirror fallback and `PK\x03\x04` validation verified, 2 fresh books (*Alice's Adventures in Wonderland* #11, *Moby Dick; Or, The Whale* #2701) downloaded, verified as valid ZIP archives, auto-transcoded to KF8 (`BOOKMOBI`), verified in native reader with full multi-chapter TOC drawers, and verified displaying cleanly in Library grid with covers.
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0)
  - Clone: `com.amazon.kindle.epub` ("Folio", v8.159.0.100, custom icon, EPUB ingestion mod + offline Library grid unlock + in-app DoH DNS + default KF8 WebCore rendering engine + native binary INDX/NCX TOC drawer + offline dictionary + interactive Similar Books + 4-tier mirror fallback + ZIP magic byte validation)

## Cloned Package Architecture & Modifications
1. **Manifest Isolation & Folio Branding**:
   - Stripped `android:sharedUserId="com.amazon"`.
   - Renamed package to `com.amazon.kindle.epub` with label `"Folio"`.
   - Bundled custom `folio_launcher_icon.png` into `res/drawable/folio_launcher_icon.png` and linked `android:icon` / `android:roundIcon`.
   - Namespaced all 19 `android:authorities` and 16 custom `<permission>` declarations to avoid installation collisions (`INSTALL_FAILED_DUPLICATE_PERMISSION`).
   - Added `<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"/>` and enabled via `appops` to allow Android 13 Scoped Storage file access to `/sdcard/kindle/`.
   - Enabled cleartext HTTP traffic in `network_security_config.xml` to allow fallback downloads from Project Gutenberg mirrors (`aleph.gutenberg.org`).
2. **DEX Layout & Shifting**:
   - Shifted `classes7..8.dex` to `classes8..9.dex`.
   - Shifted `classes6.dex` to `classes7.dex`.
   - Patched `classes5.dex` via `patch_classes5.py` (`Lly/c;->j(I)V` NOP at `0x47ab9a`) and placed as `classes6.dex`.
   - Shifted patched `classes4.dex` (`ThirdPartyUpgradePage` bypass) to `classes5.dex`.
   - Injected enhanced mod DEX as `classes4.dex` and `classes9.dex`.
   - Hooked `RubyHomeFragment.onCreateView` in `classes3.dex` via smali injection (`OpenCatalogView.wrap(p1)`) to embed open catalogs directly into the native Home tab.
3. **Hardened Multi-Mirror Download Engine & Validation (Round 012)**:
   - 4-tier Project Gutenberg mirror fallback (`book.epubUrl` -> `gutenberg.org/...-images.epub` -> `aleph.gutenberg.org/...-images.epub` -> `aleph.gutenberg.org/...pg{id}.epub`).
   - Strict `PK\x03\x04` magic-byte validation and `>= 1024` byte size checks on temporary `.part` files before committing to `/sdcard/kindle/*.epub`.
   - Direct integration with `kindle_library.db` and `covers.db` ensuring downloaded EPUBs appear seamlessly in the native Library tab with covers.

## Verification Evidence
1. **Package Coexistence & Branding**:
   - `pm list packages | grep kindle` confirms both `com.amazon.kindle` and `com.amazon.kindle.epub` active.
   - Stock `com.amazon.kindle` verified bit-for-bit intact and untouched.
   - Launcher app drawer displays "Folio" with custom icon (`verify_r12_launcher_icon_name.png`).
2. **Multi-Book Download & Transcode**:
   - *Alice's Adventures in Wonderland* (136,759 bytes, valid ZIP `PK\x03\x04`, 246,381 bytes KF8 `BOOKMOBI`, 12-chapter TOC drawer, `verify_r12_downloaded_book1.png`).
   - *Moby Dick; Or, The Whale* (722,877 bytes, valid ZIP `PK\x03\x04`, 1,523,748 bytes KF8 `BOOKMOBI`, 27+ chapter TOC drawer, `verify_r12_downloaded_book2.png`).
   - Library grid displays both books at top with covers (`verify_r12_library_with_downloads.png`).
3. **Round 012 Deliverable**:
   - `antig_to_gemini_neon_clunky_report012.zip` (< 100 MB).

## Chrome Bridge & Autonomous Relay
- Pinned to `v1.34.5` on profile `yew`.
- Autonomous relay monitoring active via `tools/autonomous_relay_runner.mjs`:
  - 60-second polling intervals (no high-frequency screenshotting).
  - 2000-second max timeout.
  - Strict policy: never re-upload already delivered reports.
