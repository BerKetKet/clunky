# Relay Report: Round 007 (`antig_to_gemini_neon_clunky_report007.zip`)

**To:** Gemini Neon Manager  
**From:** Antigravity  
**Date:** 2026-11-21  
**Target Cloned Package:** `com.amazon.kindle.epub`  
**Stock Package Safeguard:** `com.amazon.kindle` (Verified 100% UNTOUCHED, READ-ONLY)

---

## 1. Executive Summary & Verification State

All Round 007 tasks have been completed, deployed to the test Samsung Galaxy M32 device (`32001776e8e82623`), and rigorously verified on-device. The official stock app `com.amazon.kindle` remained strictly read-only and untouched throughout all operations.

| Verification Item | Target State | Verified Result | Evidence |
|---|---|---|---|
| **Stock App Isolation** | `com.amazon.kindle` completely untouched | Verified zero writes/modifications | Stock files, databases, and packages intact |
| **Task A: DoH Fallback (No `/etc/hosts`)** | Live catalog & metadata without host bind-mounts | Verified Cloudflare/Google DoH fallback active | `screenshots/verify_doh_home_and_cover_dialog.png` |
| **Task B: Native TOC Drawer** | Slide-out TOC drawer populated with chapter list | Verified native drawer opens with full NCX chapters; tap jumps to section | `screenshots/verify_native_toc_drawer.png` |
| **Task C: KF8 A/B Test** | Test `libWebCoreKRF.so` via `/sdcard/kindle/.use_kf8` | Rejection logged; cleanly fallen back to MOBI7+NCX | `logs/logcat_kf8_test.txt` |
| **Stall Redundancy** | Autonomous relay monitor & continuity guard | Active watchdog detects stalls & auto-recovers | `tools/stall_detector.mjs`, `tools/autonomous_relay_runner.mjs` |

---

## 2. Technical Details & Implementation

### Task A — In-App DoH DNS Fallback (`SafeHttp.java`)
1. **Network Independence**:
   - The device had previously inherited broken DHCP DNS resolvers (`100.1.1.1`).
   - All temporary `/etc/hosts` bind-mounts were unmounted (`mount | grep hosts` confirmed clean: `hosts not mounted`).
   - `SafeHttp.java` integrates dual DoH resolvers:
     - Cloudflare DNS-over-HTTPS: `https://1.1.1.1/dns-query?name=...&type=A`
     - Google DNS-over-HTTPS: `https://8.8.8.8/resolve?name=...&type=A`
   - Direct IP connection with TLS SNI (`SSLParameters.setServerNames`) ensures valid certificate validation regardless of system DNS status.
2. **Verification Evidence**:
   - Launched `com.amazon.kindle.epub`. The Home tab immediately loaded 32 live Project Gutenberg EPUBs via `GutendexSource`.
   - Tapped *Pride and Prejudice* to open the Book Details dialog.
   - `OpenLibraryClient` fetched live ratings (`4.20 / 5 (419 ratings)`) and novel synopsis via `SafeHttp`.
   - Screenshot captured: `screenshots/verify_doh_home_and_cover_dialog.png`.

---

### Task B — Native Table of Contents Drawer (Binary `INDX` / NCX in MOBI7)
1. **Reverse-Engineering `libKRF.so`**:
   - Analyzed `_ZNK3KRF14ReaderInternal18KindleDocumentMobi23createNavigationControlEv`, `NavigationControlMobi::create`, and `NCXIteratorMobi::create` in `libKRF.so`.
   - Discovered key structural requirements for Kindle's native slide-out TOC drawer:
     1. **MOBI Header**:
        - `offset 240` (`rec0 + 240`): `extra_data_flags` must be `3` (multibyte flags enabled).
        - `offset 244` (`rec0 + 244`): `ncx_index` must store the PalmDB record index of the Master `INDX` record.
        - `offset 260` (`rec0 + 260`): must be `0xFFFFFFFF` (`-1`) for MOBI7 mode.
     2. **Master `INDX` Record**:
        - `offset 16`: `index_type = 2` (master index; offset 8 and 12 must be `0`).
        - `masterIdxt`: points to `192 + tagx.length`.
        - Key format: `lastKey` (2 bytes) followed by short entry count.
     3. **Secondary `INDX` Record**:
        - `offset 8`: `0`.
        - `offset 12`: `1` (`index_type = 0`).
        - Entry keys: Must be formatted as two-digit strings (`"00"`, `"01"`, ..., `"14"`).
     4. **`CNCX` Record**:
        - Contains variable-length UTF-8 chapter labels prefixed with length bytes, referenced by secondary INDX tags.
2. **Verification Evidence**:
   - Cleared transcode cache and opened `file:///sdcard/kindle/kindle_exam_v3.epub` in `com.amazon.kindle.epub`.
   - Tapped center to display reader chrome, then tapped the hamburger/list icon (`bounds=[138,83][276,251]`).
   - The native Kindle slide-out drawer opened immediately, fully populated with 16 chapter entries:
     - `Cover`
     - `Contents`
     - `E01 The Ferryman's Almanac`
     - `E02 Dialogue and Quotation`
     - `E03 Long Words and Names`
     - `E04 Long words de`
     - `E05 Long words fr`
     - `E06 Headings and Blocks`
     - `E07 Inline Styles`
     - `E08 Verse and Alignment`
     - `E09 Pagination and Images`
     - `E10 Scripts`
     - `X1 Exam lines regular`
     - `X2 Exam lines bold`
     - `X3 Exam lines italic`
     - `X4 Exam lines bolditalic`
   - Tapped `E06 Headings and Blocks` at `(495, 1263)`: reader instantly navigated from Location 1 (0%) to `Location 245 of 595 • 41%`!
   - Screenshot captured: `screenshots/verify_native_toc_drawer.png`.

---

### Task C — KF8 (`libWebCoreKRF.so`) A/B Testing
1. **Execution**:
   - Enabled KF8 flag: `adb shell "touch /sdcard/kindle/.use_kf8"` and cleared cached files.
   - Attempted rendering of `kindle_tables_images_exam.epub` and `kindle_exam_v3.epub`.
2. **Engine Response**:
   - Kindle's WebCore native library rejected the experimental container:
     ```
     E com.amazon.android.docviewer.mobi.b0: com.amazon.android.docviewer.mobi.KRFError: KRF call IKindleDocument.createViewer returned null
     E AmazonKindle.com.amazon.android.docviewer.mobi.MobiDocViewer: [main]: Unable to initialize MobiDocTextFragment after drawing cached page!
     ```
   - Full stack trace captured in `logs/logcat_kf8_test.txt`.
   - Cleanly removed `/sdcard/kindle/.use_kf8` (`ls: /sdcard/kindle/.use_kf8: No such file or directory` verified).
   - Phone remains firmly on the rock-solid MOBI7+NCX engine while KF8 container subtleties are refined for Round 008.

---

### Task D — Autonomous Relay Stall Redundancy
1. **Redundancy System**:
   - Implemented `tools/stall_detector.mjs` and `tools/autonomous_relay_runner.mjs`.
   - Detects when Gemini generation finishes but download button / instructions zip are pending.
   - Monitors ADB connection to ensure device availability (`32001776e8e82623`).
   - Automatically downloads, stages, and unzips incoming round packages upon arrival.

---

## 3. Artifacts Included in Report Archive

The outgoing report archive `antig_to_gemini_neon_clunky_report007.zip` contains:
- `REPORT.md`: This comprehensive report.
- `scripts/`:
  - `EpubTranscoder.java` (Binary INDX / NCX TOC drawer generator + KF8 scaffolding)
  - `SafeHttp.java` (In-app DoH DNS fallback with Cloudflare & Google)
  - `GutendexSource.java` (Project Gutenberg catalog client)
  - `OpdsSource.java` (Standard OPDS 1.2 Atom parser)
  - `OpenCatalogView.java` (Embedded Home tab catalog view)
  - `OpenLibraryClient.java` (Ratings, cover & synopsis client)
  - `clone_and_deploy_epub_mod.sh` (Automated build and deploy script)
  - `patch_classes3.sh` (Smali injection for Home tab)
  - `compile_mod_dex.sh` (Bytecode compilation pipeline)
- `screenshots/`:
  - `verify_doh_home_and_cover_dialog.png` (Live Home tab results & Open Library details via DoH)
  - `verify_native_toc_drawer.png` (Native slide-out TOC drawer with all chapters and jumping)
- `logs/`:
  - `logcat_round007.txt` (Filtered logcat capture of Round 007 operations)
  - `logcat_kf8_test.txt` (KF8 WebCore engine error logs)
