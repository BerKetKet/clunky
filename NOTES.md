# Kindle Android v8.159.0.100 - Deployment & Restoration Notes

## Status
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Kindle EPUB") deployed and fully verified alongside unmodified stock `com.amazon.kindle`. Round 011 complete: KF8 internal cross-chapter inline `<a href>` hyperlinks (`kindle:pos:fid:XXXX:off:0000000000`) verified on-device, embedded PNG images (`pic12.png`) rendering inline cleanly under KF8 in E09, interactive Similar Books recommendation buttons operational in Book Details dialog via Open Library subjects, and complete Gutenberg -> KF8 auto-transcode reader flow verified on fresh classic *Frankenstein* (id 84) with 16-chapter native TOC drawer.
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only)
  - Clone: `com.amazon.kindle.epub` (cloned with EPUB ingestion mod + offline Library grid unlock + in-app DoH DNS + default KF8 WebCore rendering engine + native binary INDX/NCX TOC drawer + offline dictionary + interactive Similar Books + mirror fallback)

## Cloned Package Architecture & Modifications
1. **Manifest Isolation**:
   - Stripped `android:sharedUserId="com.amazon"`.
   - Renamed package to `com.amazon.kindle.epub` with label `"Kindle EPUB"`.
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
3. **Round 010 & 011 KF8 Engine & Catalog Highlights**:
   - **Relative Fragment Offset Fix**: Setting FRAG Secondary INDX Tag 6a (`start`) to 0 (`writeVwiForward(secE, 0)`) prevents `FragmentLoader` from doubling the offset, resolving TOC landing accuracy and content truncation.
   - **KF8 Internal Hyperlink Rewriting**: Rewrites cross-chapter `<a href="...xhtml">` to `kindle:pos:fid:XXXX:off:0000000000` so inline links jump natively inside `libWebCoreViewerKRF.so` without spawning external browsers.
   - **Interactive Recommendations**: `OpenCatalogView.java` renders Open Library similar book recommendations as actionable buttons that populate `searchInput` and trigger live catalog queries.
   - **Gutenberg Mirror Fallback**: Transparently handles Gutenberg 503 maintenance outages by fetching from `http://aleph.gutenberg.org/cache/epub/{id}/pg{id}.epub`.

## Verification Evidence
1. **Package Coexistence**:
   - `pm list packages | grep kindle` confirms both `com.amazon.kindle` and `com.amazon.kindle.epub` active.
   - Stock `com.amazon.kindle` verified bit-for-bit intact and untouched.
2. **Round 011 Deliverable**:
   - `antig_to_gemini_neon_clunky_report011.zip` (< 100 MB, contains `REPORT.md`, `scripts/`, `screenshots/`, `logs/`).

## Chrome Bridge & Autonomous Relay
- Pinned to `v1.34.5` on profile `yew`.
- Autonomous relay monitoring active via `tools/stall_detector.mjs` and `tools/autonomous_relay_runner.mjs`.
