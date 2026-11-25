# Round 163 Report — Azath v18.2 KFX (`com.azath.reader`)

## 1. Target Hardware & Safety Invariants
- **Device**: Physical Samsung Galaxy M30 (`32001776e8e82623`), TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Active Package**: `com.azath.reader` (`versionName=8.159.0.100(2.0.115180.0)`, `lastUpdateTime=2026-11-25 19:00:10`).
- **Stock Kindle Safety**: `com.amazon.kindle` (`versionName=8.160.1.0(2.0.124621.0)`) verified **100% UNTOUCHED** (`lastUpdateTime=2026-11-19 21:59:23`).
- **Play Store Folio (`ct.reader`)**: Zero launches, zero inspections, zero references.

---

## 2. Task A — Build, Deploy & Stage (`Azath v18.2 KFX`)
- Copied Round 163 drop-ins (`FolioMoreView.java`, `AzathLibrarySync.java`, `FolioFolderBrowserDialog.java`, `FolioMetadataDb.java`) verbatim into `07_APK_EPUB_INGESTION_MOD/mod_src/real/com/amazon/kindle/epub/`.
- Updated `07_APK_EPUB_INGESTION_MOD/tools/rebuild_fast.sh` to stage `azath_v18.2_kfx`.
- Built `classes9.dex` (`498,356` bytes), signed, installed on Galaxy M30, and staged `07_APK_EPUB_INGESTION_MOD/release/azath_v18.2_kfx/base.apk` (`138,856,551` bytes, SHA256 `616357fcbd75cf257e4331b557a004801be8c7b54365be3ca24691652ccd9658`).

---

## 3. Task B — `REFRESH LIBRARY NOW` Foreground Relaunch Test (HONEST FAILURE)
Per STOP-AND-REPORT, no local workaround was attempted; exact hardware evidence is reported below:
- **Repro Flow**: Imported `Aldous Huxley - Island.epub` via `Control Center -> Import EPUBs from Storage` -> tapped `Refresh Library Now` in `FolioFolderBrowserDialog` (`refresh_prompt.xml`) -> waited 6s without manual `adb` launch.
- **Observed Result**: **FAIL** — `com.azath.reader` did **not** relaunch into the foreground. Instead, it crashed with `SIGSYS` and dropped to the Android Launcher (`verify_r163_refresh_foreground.png`):
  - `mCurrentFocus=Window{4272dc u0 com.android.launcher3/com.android.launcher3.uioverrides.QuickstepLauncher}`
- **Exact `logcat` / Tombstone Evidence**:
```text
11-25 19:01:29.680  1335  2100 I AzathLibrarySync: Ingested OK: /sdcard/kindle/Island_FOLIO_8CE251A8A07B.kfx (1223991 B)
11-25 19:01:29.727  1335  2100 I AzathLibrarySync: reconcileLibrary complete: activeKfx=28 changes=0
11-25 19:01:37.698  1335  1516 F libc    : Fatal signal 31 (SIGSYS), code 1 (SYS_SECCOMP) in tid 1516 (Thread-22), pid 1335 (om.azath.reader)
11-25 19:01:38.540  2330  2330 F DEBUG   : *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** ***
11-25 19:01:38.540  2330  2330 F DEBUG   : Process uptime: 40s
11-25 19:01:38.540  2330  2330 F DEBUG   : Cmdline: com.azath.reader
11-25 19:01:38.540  2330  2330 F DEBUG   : pid: 1335, tid: 1516, name: Thread-22  >>> com.azath.reader <<<
11-25 19:01:38.540  2330  2330 F DEBUG   : signal 31 (SIGSYS), code 1 (SYS_SECCOMP), fault addr --------
11-25 19:01:38.540  2330  2330 F DEBUG   : Cause: seccomp prevented call to disallowed arm64 system call -241514045
11-25 19:01:38.540  2330  2330 F DEBUG   : backtrace:
11-25 19:01:38.540  2330  2330 F DEBUG   :       #00 pc 000000000386a098  /data/app/~~Hr1XTa_qj3eJO2e7-T0Kqw==/com.azath.reader-IGhr4vRs1QhWrGwXp2x0RQ==/lib/arm64/libKindleAndroidNativeBundlerJNI.so (BuildId: ba7121ff131750b4cacf2bdfcfc1562a0efb8a20)
11-25 19:01:38.540  2330  2330 F DEBUG   :       #01 pc 1400037a72a6a42c  <unknown>
```
- **Screenshot Included**: `verify_r163_refresh_foreground.png` (and `/tmp/r163/{import_list.xml,refresh_prompt.xml}` captured during the run).

---

## 4. Mandatory Cover Architecture Directive (Project Lead)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
