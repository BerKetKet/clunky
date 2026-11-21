# Kindle Android v8.159.0.100 - Deployment & Restoration Notes

## Status
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Folio") deployed and fully verified alongside unmodified stock `com.amazon.kindle`. Round 013 complete: true Android `<adaptive-icon>` deployed with `@drawable/folio_icon_bg` and `@drawable/folio_icon_fg`, verified filling Smart Launcher 6 squircle edge-to-edge with zero white border; multi-chapter-per-file `#anchor` TOC support verified on *Moby Dick* with 135+ consecutive chapters indexed in native TOC drawer; direct TOC jumping to intra-file anchor Chapter 2 verified at Location 440.
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only, v8.160.1.0)
  - Clone: `com.amazon.kindle.epub` ("Folio", v8.159.0.100, adaptive icon, EPUB ingestion mod + offline Library grid unlock + in-app DoH DNS + default KF8 WebCore rendering engine + native binary INDX/NCX TOC drawer with multi-chapter `#anchor` offset resolution + 4-digit INDX keys + offline dictionary + interactive Similar Books + 4-tier mirror fallback + ZIP magic byte validation)

## Cloned Package Architecture & Modifications
1. **Manifest Isolation & Adaptive Folio Branding**:
   - Stripped `android:sharedUserId="com.amazon"`.
   - Renamed package to `com.amazon.kindle.epub` with label `"Folio"`.
   - Bundled Android Adaptive Icon at `res/mipmap-anydpi-v26/folio_launcher_icon.xml` referencing `@drawable/folio_icon_bg` and `@drawable/folio_icon_fg`, eliminating white borders on modern third-party and OEM launchers.
   - Namespaced all 19 `android:authorities` and 16 custom `<permission>` declarations to avoid installation collisions (`INSTALL_FAILED_DUPLICATE_PERMISSION`).
   - Enabled Scoped Storage via `MANAGE_EXTERNAL_STORAGE` and cleartext HTTP fallback.
2. **Multi-Chapter `#anchor` Navigation & INDX Indexing (Round 013)**:
   - `EpubTranscoder.java` parses all NCX `<navPoint>` entries, extracting both spine file paths and `#fragment` anchors.
   - Implemented `findAnchorByteOffset(html, fragId)` to match `id="..."` and `name="..."` tags and calculate precise UTF-8 byte offsets inside KF8 fragments.
   - Upgraded NCX Secondary INDX keys from 2-digit `%02d` to 4-digit `%04d` to support large books with hundreds of chapters.
   - Tested on *Moby Dick* (135+ chapters across 27 spine files); consecutive chapters 1, 2, 3, 4, 5... rendered in TOC drawer with flawless intra-file jump accuracy.

## Verification Evidence
1. **Adaptive Launcher Icon**:
   - Smart Launcher 6 app drawer renders Folio with full navy-teal background squircle without white frame (`verify_r13_adaptive_icon.png`).
2. **Consecutive Chapter TOC**:
   - Native TOC drawer on *Moby Dick* lists all chapters consecutively (`verify_r13_mobydick_full_toc.png`).
3. **Intra-File Anchor Jump**:
   - Tapping Chapter 2 jumps reader to Location 440 (`verify_r13_mobydick_ch2_jump.png`).
4. **Deliverables**:
   - `antig_to_gemini_neon_clunky_report013.zip` (< 100 MB).

## Chrome Bridge & Autonomous Relay
- Pinned to `v1.34.5` on profile `yew`.
- Autonomous relay monitoring active via `tools/autonomous_relay_runner.mjs`:
  - 60-second polling intervals (no high-frequency screenshotting).
  - 2000-second max timeout.
  - Strict policy: never re-upload already delivered reports.
