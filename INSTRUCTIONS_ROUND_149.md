# Round 149 Instructions — Azath v16.3 KFX (Crash-Free In-Place Stone Theme Hot-Swap, Zero `Activity.recreate()`, & Final GitHub Push)

**Target Device**: Physical **Samsung Galaxy M30** (`32001776e8e82623`, TrebleDroid Android 13 GSI).  
**Active Package**: `com.azath.reader` (`Azath v16.3 KFX`).  
**Safety Invariant**: Stock `com.amazon.kindle` MUST remain **100% UNTOUCHED and READ-ONLY**.  
**Strict Policy**: **DO NOT launch, inspect, or reference Play Store Folio (`ct.reader`).**

## Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."

## Stability Fix in Round 149 (`Azath v16.3 KFX`)
Eliminates the native `SIGSYS` (`SYS_SECCOMP` in `libKindleAndroidNativeBundlerJNI.so`) triggered by calling `MainActivity.recreate()` twice in Report 148:
1. **`FolioTheme.java`**: Added weak-referenced `registerThemeListener(Runnable)` and `notifyThemeChanged()` registry.
2. **`FolioMoreView.java` & `OpenCatalogView.java`**:
   - Both `wrap(originalView)` entry points now return a persistent `FrameLayout` host container that registers an in-place rebuilder (`host.removeAllViews(); host.addView(build(ctx));`) tied to the host lifecycle (`host.setTag(rebuilder)`).
   - Replaced `((Activity) ctx).recreate()` in the `Granite` / `Limestone` pill with `FolioTheme.notifyThemeChanged()`, hot-swapping BOTH Control Center and Home instantaneously with zero native Activity teardown!
   - Bumped to **`Azath v16.3 KFX`**.

## Task A: Build, Deploy, Stage & Final Stone Edition GitHub Push
1. Copy `FolioTheme.java`, `FolioMoreView.java`, and `OpenCatalogView.java` into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/catalog/`.
2. Rebuild via `rebuild_fast.sh` (`azath_v16.3_kfx`), deploy `com.azath.reader` to **Samsung Galaxy M30**, stage `07_APK_EPUB_INGESTION_MOD/release/azath_v16.3_kfx/base.apk` with SHA256, and push **`Azath v16.3 KFX`** to GitHub (`origin master`).

## Task B: Rapid Toggle Stress-Test & Verification (Samsung Galaxy M30)
1. In a single uninterrupted process (no force-stop!), toggle `Granite -> Limestone -> Granite -> Limestone -> Granite` (4 consecutive toggles) inside Control Center, switching to Home after toggling to Limestone and again after returning to Granite.
2. Confirm **ZERO `SIGSYS` / `FATAL EXCEPTION` / tombstone crashes** in `logcat` across all 4 consecutive toggles!
3. Capture `verify_r149_azath_hotswap_granite.png` (Control Center `v16.3 KFX` cleanly back in Granite after the 4-toggle stress test) and confirm `com.amazon.kindle` remains untouched.
