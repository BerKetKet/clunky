# Kindle Android v8.159.0.100 - Deployment & Restoration Notes

## Status
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio") deployed and fully verified alongside unmodified stock `com.amazon.kindle`. Round 014 complete: EPUB3 `nav.xhtml` fallback parsing, intra-file `#anchor` link rewriting (`kindle:pos:fid:XXXX:off:YYYYYYYYYY` via `toBase32Ten`), OPDS Quick Presets modal, SQLite Library/Notes backup dialog & on-device backup execution verified; native TOC drawer multi-chapter regression test passed on *Moby Dick*; clean Folio v1.0 signed APK release bundle saved into `07_APK_EPUB_INGESTION_MOD/release/folio_v1.0/`.
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0)
  - Clone: `com.amazon.kindle.epub` ("Folio", v8.159.0.100, adaptive icon, EPUB ingestion mod + offline Library grid unlock + in-app DoH DNS + default KF8 WebCore rendering engine + native binary INDX/NCX TOC drawer with multi-chapter `#anchor` offset resolution + 4-digit INDX keys + EPUB3 nav fallback + intra-file link rewriting + OPDS presets + SQLite backup/restore + offline dictionary + interactive Similar Books + 4-tier mirror fallback + ZIP magic byte validation)

## Cloned Package Architecture & Modifications
1. **Manifest Isolation & Adaptive Folio Branding**:
   - Stripped `android:sharedUserId="com.amazon"`.
   - Renamed package to `com.amazon.kindle.epub` with label `"Folio"`.
   - Bundled Android Adaptive Icon at `res/mipmap-anydpi-v26/folio_launcher_icon.xml` referencing `@drawable/folio_icon_bg` and `@drawable/folio_icon_fg`, eliminating white borders on modern third-party and OEM launchers.
   - Namespaced all 19 `android:authorities` and 16 custom `<permission>` declarations to avoid installation collisions (`INSTALL_FAILED_DUPLICATE_PERMISSION`).
   - Enabled Scoped Storage via `MANAGE_EXTERNAL_STORAGE` and cleartext HTTP fallback.
2. **Multi-Chapter `#anchor` Navigation & INDX Indexing (Round 013 & 014)**:
   - `EpubTranscoder.java` parses both NCX `<navPoint>` and EPUB3 `<nav>` / `nav.xhtml` structures.
   - Extracts spine files and `#fragment` anchors, computing precise UTF-8 byte offsets inside KF8 fragments using `findAnchorByteOffset(html, fragId)`.
   - Rewrites intra-file links (`<a href="#id">`, `<a href="file.xhtml#id">`) to `kindle:pos:fid:XXXX:off:YYYYYYYYYY` using 10-char Base-32 encoding (`toBase32Ten`).
   - Uses 4-digit Secondary INDX keys (`%04d`) supporting arbitrary chapter counts.
3. **OPDS Presets & Backup/Restore Dialog (Round 014)**:
   - Upgraded `+ OPDS Feed` modal with Quick Preset buttons for Feedbooks, Project Gutenberg, and Internet Archive.
   - Added Backup/Restore modal to trigger SQLite database copies to `/sdcard/kindle/backups/`.

## Verification Evidence
1. **Adaptive Launcher Icon**:
   - Smart Launcher 6 app drawer renders Folio with full navy-teal background squircle without white frame (`verify_r13_adaptive_icon.png`).
2. **Consecutive Chapter TOC**:
   - Native TOC drawer on *Moby Dick* lists all chapters consecutively (`verify_r13_mobydick_full_toc.png`, `verify_r14_kf8_regression.png`).
3. **OPDS Quick Presets & Backup Dialog**:
   - Verified Quick Presets modal (`verify_r14_opds_presets.png`).
   - Verified Backup & Restore modal (`verify_r14_backup_restore.png`).
   - Verified on-device backup execution (`/sdcard/kindle/backups/20261121_090505_*.db`).
4. **Release Bundle**:
   - `07_APK_EPUB_INGESTION_MOD/release/folio_v1.0/base.apk` (134,811,261 B)
   - `07_APK_EPUB_INGESTION_MOD/release/folio_v1.0/base.apk.idsig` (1,074,563 B)
5. **Deliverables**:
   - `antig_to_gemini_neon_clunky_report014.zip` (< 100 MB).

## Chrome Bridge & Autonomous Relay
- Pinned to `v1.34.5` on profile `yew`.
- Autonomous relay monitoring active via `tools/autonomous_relay_runner.mjs`:
  - 60-second polling intervals (no high-frequency screenshotting).
  - 2000-second max timeout.
  - Strict policy: never re-upload already delivered reports.
