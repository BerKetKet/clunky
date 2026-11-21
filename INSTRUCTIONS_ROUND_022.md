# Round 022 Instructions — On-Device Local EPUB → KFX (`KRIFDocViewer`) Pipeline (100% Native Stock Kindle Enhanced Typesetting Parity!)

**Target Package:** `com.amazon.kindle.epub` ("Folio")  
**HARD SAFETY INVARIANT:** Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY** (never write, modify, clear data, force-stop, or uninstall stock `com.amazon.kindle`).  
**Autonomous Relay Mode:** Execute all tasks end-to-end and upload `antig_to_gemini_neon_clunky_report022.zip` (`< 100 MB`) directly to this chat via Chrome Dev Bridge without pausing.

---

## Why Round 022 Is the Game-Changer

Round 021 proved that **Folio (`com.amazon.kindle.epub`) natively opens local unencrypted `.kfx` files directly in `KRIFDocViewer` / `KRIFView`** (`libKindleAndroidNativeBundlerJNI.so`), unlocking:
- **100% Native Amazon Enhanced Typesetting** (hyphenation, kerning, ligatures, Bookerly metrics)
- **100% Native Continuous Scrolling** (`Aa → Layout → Continuous Scrolling`)
- **100% Native `Aa → Font → Spacing`** (all 4 live sliders in C++ YellowJersey)
- **100% Native Fullscreen `#000000` Black Cover Page** (with zero overlays or hacks!)

Now we replicate Amazon's Send-to-Kindle EPUB → KFX server locally **on-device** inside Folio!

---

## Tasks

### Task A — Clone, Build & Test `hwanjongyu/kfx_converter` (Pure-Rust EPUB → KFX) Against Folio's `KRIFDocViewer`
1. On your Linux host PC, clone **`https://github.com/hwanjongyu/kfx_converter`** (pure Rust EPUB → Amazon KFX `CONT` v2 + Ion 1.0 binary converter, zero Python/Calibre/Kindle Previewer dependencies).
2. Build the host CLI binary (`cargo build --release`) and test converting **2 distinct EPUBs**:
   - `kindle_exam_v3.epub` → `/tmp/kfx_test_exam.kfx`
   - One real book from `/sdcard/epub/` (e.g. Hesiod or Sophocles or Cato) → `/tmp/kfx_test_classic.kfx`
3. Push both locally-generated `.kfx` files to `/sdcard/` and open each in **Folio (`com.amazon.kindle.epub`)** via `ThirdPartyUpgradePage` (`-t "application/x-kfx"`).
4. **Verify in Folio (`KRIFDocViewer`)**:
   - Capture `logs/local_kfx_gen_logcat.txt`.
   - If `KRIFDocViewer` renders them cleanly, capture:
     * **`screenshots/verify_r22_local_kfx_cover.png`** (native KFX fullscreen cover!)
     * **`screenshots/verify_r22_local_kfx_prose.png`** (Enhanced Typesetting prose with native hyphenation!)
     * **`screenshots/verify_r22_local_kfx_toc.png`** (native slide-out TOC drawer!)
     * **`screenshots/verify_r22_local_kfx_spacing.png`** (live `Aa → Font → Spacing` + Continuous Scrolling!)
   - **If `KRIFDocViewer` rejects or errors on `kfx_converter`'s `.kfx` output**:
     * Capture the exact `KRIFView` / `KindleNDKLog` error in `logs/local_kfx_gen_logcat.txt`, and diff the generated `.kfx` header/Ion fragments against the unencrypted stock `CR!ZTD3FX31BX225B7KRMNC6MN5NFHZ.kfx` (read-only!) in `logs/kfx_format_diff.txt` so we can patch `kfx_converter` directly!

### Task B — Cross-Compile `kfx-conv` for Android `arm64-v8a` & Bundle On-Device Inside Folio (`lib/arm64-v8a/libkfxconv.so`)
1. Copy the updated `CoverOverlayView.java` from this package (skips `.kfx` books so `KRIFDocViewer` owns KFX covers natively without flashing a stale cover from a previous book!).
2. **If Task A's generated `.kfx` renders cleanly in Folio's `KRIFDocViewer`**:
   - Cross-compile the Rust CLI binary for **`aarch64-linux-android`** using Android NDK (`rustup target add aarch64-linux-android` + NDK clang linker, e.g. `aarch64-linux-android26-clang`).
   - Bundle the compiled ARM64 binary into Folio's APK at `lib/arm64-v8a/libkfxconv.so` (ensure `android:extractNativeLibs="true"` in `AndroidManifest.xml` so Android extracts it with `+x` permissions into `ctx.getApplicationInfo().nativeLibraryDir + "/libkfxconv.so"`).
   - Wire `EpubBookBuilder` / `EpubTranscoder` so when opening any `.epub`:
     1. Try running `nativeLibraryDir + "/libkfxconv.so"` (`ProcessBuilder`) to transcode `.epub` → `epub_cache/<name>.kfx`.
     2. If exit code == 0 and output `.kfx` starts with `"CONT"`, open that `.kfx` directly in `KRIFDocViewer`!
     3. Otherwise, fall back seamlessly to our rock-solid Java KF8 `EpubTranscoder` (`.azw3`) so ZERO books ever fail!
   - Test opening a raw `.epub` directly from `/sdcard/epub/` in Folio and verify end-to-end on-device `.epub` → `.kfx` → `KRIFDocViewer`! Capture **`screenshots/verify_r22_ondevice_epub_to_kfx.png`**.

### Task C — Package & Autonomous Upload
Confirm stock `com.amazon.kindle` is 100% untouched, package `antig_to_gemini_neon_clunky_report022.zip` (`< 100 MB`) containing `REPORT.md`, `scripts/`, `screenshots/`, and `logs/`, and upload directly to this chat via Chrome Dev Bridge.
