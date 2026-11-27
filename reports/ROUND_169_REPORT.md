# Round 169 Closeout Report (`Azath v19.0 KFX`)

## 1. Branch, Scope & Safety Invariants
- **Branch**: `round-169-closeout` (branched from `round-168-stability` at `79784e2`; **not merged to `master`** pending Project Lead approval).
- **Target Version**: `Azath v19.0 KFX` (`07_APK_EPUB_INGESTION_MOD/release/azath_v19.0_kfx/base.apk`).
- **Canonical Source Tree**: `07_APK_EPUB_INGESTION_MOD/mod_src/real/` ONLY.
- **Stock `com.amazon.kindle` Invariant**: `versionName=8.160.1.0(2.0.124621.0)`, `lastUpdateTime=2026-11-19 21:59:23` — strictly untouched & read-only. Zero `ct.reader` packages/references. Stock `.so` binaries (`libKindleAndroidNativeBundlerJNI.so`) untouched.

---

## 2. Task 3 Conditional Determination (Driven by Task 1 Matrix)

Per Task 3 rules, we evaluated whether disabling only the launch sweep (`v18.9` with `/sdcard/kindle/.disable_launch_sweep`) eliminates the native `~37s` crash observed across `v18.5`–`v18.9`.

- **Result**: **The crash does NOT disappear when the launch sweep is disabled.**
  - `v18_9_no_sweep` (`enrich=on`, `sweep=off`, verified by logcat `MetadataEnricher: Launch-time enrichment sweep disabled (debug switch)`) died **`4/5` times** at `37.1s–37.8s` (`SIGILL ILL_ILLOPC` × 3 at `#00 pc 0x03868b70`; `SIGSEGV SEGV_ACCERR` × 1 at `#00 pc 0x038689a4` in `libKindleAndroidNativeBundlerJNI.so`).
  - `v18_9_default` (`enrich=on`, `sweep=on`) died **`1/5` times** (`37.7s`, `SIGSEGV` at `#00 pc 0x0386a1c0`).
  - Earlier historical releases sharing the exact same unmodified native `libKindleAndroidNativeBundlerJNI.so` (`BuildId: ba7121ff131750b4cacf2bdfcfc1562a0efb8a20`) reproduce the exact same failure signature: `v18.5` died **`3/3` times** (`37.1s–37.5s`: `SIGSEGV`/`SIGSYS`/`SIGSEGV`), `v18.6` died **`2/3` times** (`36.7s`, `71.9s`), and `v18.8` died **`1/3` times** (`37.2s`).
  - Idle stock `com.amazon.kindle` control survived **`3/3` times** (`120.0s`) untouched (`lastUpdateTime=2026-11-19 21:59:23`).
- **Decision (Task 3 Branch B)**: The crash is **environmental / unmodified stock-native** (`libKindleAndroidNativeBundlerJNI.so` KSDK background `Thread-19`/`20`/`21`/`22` faults under unregistered `com.azath.reader`), independent of Java-side launch sweep or `v18.5`–`v18.9` changes. Sweep bisect `(a)/(b)/(c)` is **not applicable** (`N/A`), and native investigation stops here without modifying stock native libraries.

---

## 3. Mandatory Cover Architecture Directive (Verbatim — Preserved)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
