# Kindle Android v8.159.0.100 - Deployment & Restoration Notes

## Status
- **Current State**: Cloned package `com.amazon.kindle.epub` ("Kindle EPUB") deployed and fully verified alongside unmodified stock `com.amazon.kindle`. Round 009 complete: full KF8 (`libWebCoreKRF.so`) rendering resolved, CSS3 tables/images and typography rendered, DOM fragment XPath selector anchor resolved via `<body aid="0"><div aid="..."></div></body>`, native slide-out TOC drawer populated, and chapter jump to Location 252 (41%) verified. Engine flag `/sdcard/kindle/.use_kf8` remains active.
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux, serial `32001776e8e82623`).
- **Installed Packages**:
  - Stock: `com.amazon.kindle` (bit-for-bit intact, strictly read-only)
  - Clone: `com.amazon.kindle.epub` (cloned with EPUB ingestion mod + offline Library grid unlock + in-app DoH DNS + full KF8 WebCore rendering engine + native binary INDX/NCX TOC drawer)

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
   - Injected enhanced mod DEX as `classes4.dex` and `classes9.dex`.
   - Hooked `RubyHomeFragment.onCreateView` in `classes3.dex` via smali injection (`OpenCatalogView.wrap(p1)`) to embed open catalogs directly into the native Home tab.
3. **Round 005 & 006 Achievements**:
   - Replaced Home tab with embedded `OpenCatalogView` + `OpdsSource` supporting live Gutendex catalogs and custom OPDS 1.2 feeds.
   - Cleaned library deduping and stabilized cache paths (`CACHE_PREFIX = ""`).
   - One-tap database backup creates timestamped backups of `kindle_library.db` and `annotations.db` in `/sdcard/kindle/backups/`.
   - Verified offline DRM-free dictionary support (`B003ODIZL6_EBOK.prc`).
4. **Round 007 In-App DoH & Native Binary INDX/NCX TOC**:
   - **`SafeHttp.java`**: Implemented in-app DNS-over-HTTPS fallback querying Cloudflare (`1.1.1.1`) and Google (`8.8.8.8`) directly over IP with TLS SNI (`SSLParameters.setServerNames`), eliminating dependency on system DNS or `/etc/hosts` bind-mounts.
   - **Binary `INDX` (NCX) Table of Contents Drawer**: Reverse-engineered `libKRF.so` MOBI7 engine:
     - Configured MOBI header offset 240 (`extra_data_flags = 3`), offset 244 (`ncx_index` pointing to Master INDX record), offset 260 (`-1` for MOBI7).
     - Master INDX header: offset 16 is `index_type = 2`.
     - Secondary INDX header: offset 12 is `1` (`index_type = 0`).
     - Secondary INDX keys: 2-digit strings (`"00"`, `"01"`, etc.).
     - Generates valid PalmDB Master INDX, Secondary INDX, and CNCX records from `toc.ncx` / headings.
   - **KF8 (`libWebCoreKRF.so`) A/B Testing**: Verified `/sdcard/kindle/.use_kf8` flag handling. Detected native WebCore viewer rejection (`KRF call IKindleDocument.createViewer returned null`), captured logs in `logs/logcat_kf8_test.txt`, and cleanly reverted flag to remain on stable MOBI7+NCX.
   - **Stall Redundancy**: Created `tools/stall_detector.mjs` and `tools/autonomous_relay_runner.mjs` to detect idle/stalls in the Gemini chat bridge, automate zip downloads, and maintain continuous operation.

4. **Round 008 & 009 KF8 Engine & DOM Fragment Resolution**:
   - **`libWebCoreKRF.so` Integration**: Resolved KF8 PalmDB binary structure (`rec0` offsets, `extra_data_flags = 1`, `first_non_book_index` pointing to `FRAG`, `SKEL` indexing).
   - **DOM Fragment Injection**: Diagnosed blank white screen root cause via side-by-side binary comparison with `Rashomon.azw3`. `libWebCoreKRF.so`'s `FragmentLoader` evaluates CNCX XPath selector `P-//*[@aid='...']` to locate the insertion anchor. Injected `<body aid="0"><div aid="..."></div></body>` into section skeletons with `insertPos` at `</div>`, enabling live HTML DOM insertion.
   - **Verified KF8 Rendering**:
     - `verify_kf8_tables_images.png`: 203,765 rich content pixels with CSS3 tables & images.
     - `verify_kf8_exam_v3.png`: 75,808 content pixels with CSS3 typography & headings.
     - `verify_kf8_native_toc.png`: 16 chapters rendered in Kindle slide-out drawer.
     - `verify_kf8_chapter_jump.png`: Jump to `E06 Headings and Blocks` at Location 252 (41%), rendering 583,653 content pixels.
   - **Engine State**: `/sdcard/kindle/.use_kf8` remains actively deployed.

## Verification Evidence
1. **Package Coexistence**:
   - `pm list packages | grep kindle` confirms both `com.amazon.kindle` and `com.amazon.kindle.epub` active.
   - Stock `com.amazon.kindle` verified bit-for-bit intact and untouched.
2. **Round 009 Deliverable**:
   - `antig_to_gemini_neon_clunky_report009.zip` (< 100 MB, contains `REPORT.md`, `scripts/`, `screenshots/`, `logs/`).

## Chrome Bridge & Autonomous Relay
- Pinned to `v1.34.5` on profile `yew`.
- Autonomous relay monitoring active via `tools/stall_detector.mjs`.
