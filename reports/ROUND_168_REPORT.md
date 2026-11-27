# Round 168 Stability & Polish Report (`Azath v18.9 KFX`)

## 1. Scope, Environment & Preserved Baseline
- **Round 168 Branch**: `round-168-stability` (`79784e216afee723ea96708b9445bf658ecff131`, branched from cleaned `master` `c42042e`; 9 commits `b4f67af..79784e2`).
- **Backup Preservation**: `release/v18.8-kfx-stable` (`515c02b`) and tag `v18.8-kfx` untouched; `master` untouched at `c42042e`.
- **Device Under Test**: Physical Samsung Galaxy M30 (`32001776e8e82623`, TrebleDroid Android 13 GSI `arm64-v8a`), exclusive test phone.
- **Stock `com.amazon.kindle` Invariant**: `versionName=8.160.1.0(2.0.124621.0)`, `lastUpdateTime=2026-11-19 21:59:23` — strictly untouched & read-only before and after all diagnostic runs.
- **Zero `ct.reader`**: Verified absent (`ZERO_CT_READER`).
- **Unmodified Native Libraries**: `libKindleAndroidNativeBundlerJNI.so` (`BuildId: ba7121ff131750b4cacf2bdfcfc1562a0efb8a20`) and all native `.so` libraries left 100% unmodified.
- **Host JVM Unit Tests (Round 168 Closeout HEAD `79784e2`)**: `./scripts/run_unit_tests.sh` -> `AzathUnitTests: passed=166 failed=0 total=166`.

---

## 2. Native Crash Diagnosis — Full 30-Run M30 Matrix

All 30 runs below were executed sequentially on M30 (`32001776e8e82623`), each starting from `am force-stop`, cleared logcat, clean launch, and up to 120.0s idle observation. Raw TSV outputs are committed alongside this report under `reports/data/*.tsv`.

| Run | Build | Enrichment | Sweep | Uptime at Death / Survived 120s | Signals |
| :---: | :---: | :---: | :---: | :---: | :--- |
| 1 | `v18.9` | `on` | `on` | Survived 120s (`120.0s`, pid `4267`) | `none` |
| 2 | `v18.9` | `on` | `on` | Survived 120s (`120.0s`, pid `6255`) | `none` |
| 3 | `v18.9` | `on` | `on` | **Died `37.7s`** (pid `7962`, `tid 8153 Thread-20`) | `SIGSEGV (SEGV_ACCERR)`, fault `0x73b41c0784`, `#00 pc 0x0386a1c0` (`libKindleAndroidNativeBundlerJNI.so`), `#01 pc 1400037a72a6a42c <unknown>` |
| 4 | `v18.9` | `on` | `on` | Survived 120s (`120.0s`, pid `8892`) | `none` |
| 5 | `v18.9` | `on` | `on` | Survived 120s (`120.0s`, pid `10124`) | `none` |
| 1 | `v18.9` (`.disable_launch_sweep`) | `on` | `off` | **Died `37.1s`** (pid `11557`, `tid 11756 Thread-20`) | `SIGILL (ILL_ILLOPC)`, fault `0x74e13bab70`, `#00 pc 0x03868b70` (`libKindleAndroidNativeBundlerJNI.so`), `#01 pc 1400037a72a6a42c <unknown>` |
| 2 | `v18.9` (`.disable_launch_sweep`) | `on` | `off` | Survived 120s (`120.0s`, pid `12718`) | `none` |
| 3 | `v18.9` (`.disable_launch_sweep`) | `on` | `off` | **Died `37.8s`** (pid `13971`, `tid 14192 Thread-21`) | `SIGILL (ILL_ILLOPC)`, fault `0x74e5c7db70`, `#00 pc 0x03868b70` (`libKindleAndroidNativeBundlerJNI.so`), `#01 pc 1400037a72a6a42c <unknown>` |
| 4 | `v18.9` (`.disable_launch_sweep`) | `on` | `off` | **Died `37.2s`** (pid `14903`, `tid 15096 Thread-20`) | `SIGILL (ILL_ILLOPC)`, fault `0x74ec871b70`, `#00 pc 0x03868b70` (`libKindleAndroidNativeBundlerJNI.so`), `#01 pc 1400037a72a6a42c <unknown>` |
| 5 | `v18.9` (`.disable_launch_sweep`) | `on` | `off` | **Died `37.2s`** (pid `15777`, `tid 15962 Thread-20`) | `SIGSEGV (SEGV_ACCERR)`, fault `0x73e19e5798`, `#00 pc 0x038689a4` (`libKindleAndroidNativeBundlerJNI.so`), `#01 pc 1400037a72a6a42c <unknown>` |
| 1 | `v18.9` (`.disable_online_enrichment`) | `off` | `off` | Survived 120s (`120.0s`, pid `16750`) | `none` |
| 2 | `v18.9` (`.disable_online_enrichment`) | `off` | `off` | Survived 120s (`120.0s`, pid `17933`) | `none` |
| 3 | `v18.9` (`.disable_online_enrichment`) | `off` | `off` | Survived 120s (`120.0s`, pid `19114`) | `none` |
| 4 | `v18.9` (`.disable_online_enrichment`) | `off` | `off` | Survived 120s (`120.0s`, pid `20510`) | `none` |
| 5 | `v18.9` (`.disable_online_enrichment`) | `off` | `off` | Survived 120s (`120.0s`, pid `21993`) | `none` |
| 1 | `v18.5` | `on` | `on` | **Died `37.5s`** (pid `23630`, `tid 23843 Thread-21`) | `SIGSEGV (SEGV_ACCERR)`, fault `0x75ae10db1c`, `#00 pc 0x03868b34` (`libKindleAndroidNativeBundlerJNI.so`), `#01 pc 1400037a72a6a42c <unknown>` |
| 2 | `v18.5` | `on` | `on` | **Died `37.2s`** (pid `24912`, `tid 25094 Thread-21`) | `SIGSYS (SYS_SECCOMP)`, syscall `-1116284382`, `#00 pc 0x03869f20` (`libKindleAndroidNativeBundlerJNI.so`), `#01 pc 1400037a72a6a42c <unknown>` |
| 3 | `v18.5` | `on` | `on` | **Died `37.1s`** (pid `25796`, `tid 25981 Thread-20`) | `SIGSEGV (SEGV_ACCERR)`, fault `0x73ced7c798`, `#00 pc 0x038689a4` (`libKindleAndroidNativeBundlerJNI.so`), `#01 pc 1400037a72a6a42c <unknown>` |
| 1 | `v18.6` | `on` | `on` | Survived 120s (`120.0s`, pid `27225`) | `none` |
| 2 | `v18.6` | `on` | `on` | **Died `36.7s`** (pid `28781`, `tid 28969 Thread-20`) | `SIGSEGV (SEGV_ACCERR)`, fault `0x746e48ae18`, `#00 pc 0x03869bac` (`libKindleAndroidNativeBundlerJNI.so`), `#01 pc 1400037a72a6a42c <unknown>` |
| 3 | `v18.6` | `on` | `on` | **Died `71.9s`** (pid `29672`, `tid 29858 Thread-20`) | `SIGILL (ILL_ILLOPC)`, fault `0x74ee48db70`, `#00 pc 0x03868b70` (`libKindleAndroidNativeBundlerJNI.so`), `#01 pc 1400037a72a6a42c <unknown>` |
| 1 | `v18.7` | `on` | `on` | Survived 120s (`120.0s`, pid `31367`) | `none` |
| 2 | `v18.7` | `on` | `on` | Survived 120s (`120.0s`, pid `347`) | `none` |
| 3 | `v18.7` | `on` | `on` | Survived 120s (`120.0s`, pid `1587`) | `none` |
| 1 | `v18.8` | `on` | `on` | Survived 120s (`120.0s`, pid `3096`) | `none` |
| 2 | `v18.8` | `on` | `on` | Survived 120s (`120.0s`, pid `4959`) | `none` |
| 3 | `v18.8` | `on` | `on` | **Died `37.2s`** (pid `6734`, `tid 6937 Thread-19`) | `SIGSEGV (SEGV_ACCERR)`, fault `0x75e2521a10`, `#00 pc 0x038333c0`, `#01 pc 0x0386a048` (`libKindleAndroidNativeBundlerJNI.so`) |
| 1 | Stock `com.amazon.kindle` (idle control) | `n/a` | `n/a` | Survived 120s (`120.0s`, pid `7736`) | `none` |
| 2 | Stock `com.amazon.kindle` (idle control) | `n/a` | `n/a` | Survived 120s (`120.0s`, pid `9338`) | `none` |
| 3 | Stock `com.amazon.kindle` (idle control) | `n/a` | `n/a` | Survived 120s (`120.0s`, pid `10947`) | `none` |

### What the Table Shows (Plain Objective Reading)
1. **Disabling the launch sweep does NOT stop the crash**: `v18.9` with `/sdcard/kindle/.disable_launch_sweep` (`enrich=on, sweep=off`, confirmed by `MetadataEnricher: Launch-time enrichment sweep disabled (debug switch)` in run logs) died **`4/5` times** between `37.1s` and `37.8s` (`SIGILL` × 3, `SIGSEGV` × 1), worse in that sample than `v18.9` default (`1/5` died at `37.7s`).
2. **Not a regression from `v18.6`–`v18.9`**: `v18.5` died **`3/3` times** (`37.1s–37.5s`: `SIGSEGV`, `SIGSYS`, `SIGSEGV`), `v18.6` died **`2/3` times** (`36.7s`, `71.9s`), `v18.8` died **`1/3` times** (`37.2s`), and `v18.7` happened to survive `3/3` in this sample (after dying `1/2` at `40.7s` in Round 168's earlier probe). Notice also `v18_9_no_enrich` survived `5/5` here, whereas Round 168's `task1a_no_enrich` (`v18.8` with enrichment & sweep off + `ThreadSnap`) hit `1/5` death at `41.4s` (`SIGSEGV` in `Thread-22`) with `ThreadSnap` showing `syncLocked=false` and zero `azath-*` worker threads active `0.55s` before death.
3. **Common failure signature across all crashing Azath builds**: Every fatal crash lands on native threads (`Thread-19`, `Thread-20`, `Thread-21`, `Thread-22`) at ~`37s` (or ~`72s`) inside the unmodified stock native library `libKindleAndroidNativeBundlerJNI.so` (`BuildId: ba7121ff131750b4cacf2bdfcfc1562a0efb8a20`, protected/JIT code region around `0x038689a4`–`0x0386a1c0` / `0x038333c0`), producing mixed `SIGSEGV (SEGV_ACCERR)`, `SIGILL (ILL_ILLOPC)`, and `SIGSYS (SYS_SECCOMP)` immediately after `KSDKDatamateClient` / `KSDKDeviceMessaging` auth errors under the cloned `com.azath.reader` identity.
4. **Idle stock `com.amazon.kindle` control**: Survived `3/3` (`120.0s` each) with zero crashes and unchanged `lastUpdateTime=2026-11-19 21:59:23`.
5. **Conclusion**: The crash is environmental / inside unmodified stock `libKindleAndroidNativeBundlerJNI.so` when running unregistered as `com.azath.reader`, independent of Round 168's Java sweep or `v18.5`–`v18.9` changes.

---

## 3. Round 168 Per-Task Outcomes (`b4f67af..79784e2`)
| Task | Commit | Outcome | Summary & Honest Notes |
| :---: | :---: | :---: | :--- |
| **Task 1** | `b4f67af` | **PASS** | Added 120s diagnostic switches + `ThreadSnap` telemetry; isolated crash to unmodified `libKindleAndroidNativeBundlerJNI.so` (full 30-run verification above). |
| **Task 2** | `b05c093` | **PASS** | Unsplit comma subjects (`Authors, correspondence`), personal-name/noise filters, frequency-ranked cap `<= 6`, `genres_json` round-trip, and idempotent migration (`13/35` rows updated on M30). |
| **Task 3** | `f3763bb` | **PARTIAL** | Added size-based timeout (`computeConversionTimeoutSec`), subtitle ETA, and per-attempt timing logs. Verified live with `Cookbook_165MB.epub` (`164.3 MB` -> `CONT` KFX in `3,221 ms`; *Madman's Library* EPUB absent; `inproc` already calls `libkfxconv.so`). |
| **Task 4** | `c50b4d9` | **PASS** | Logged `reconcileLibrary` breakdown; traced earlier `changes=27` to `kar-sharedSchedule-0` background transcoding `/sdcard/kindle/*.epub` into `files/epub_cache/`, blocked raw `.epub` crawl (`cache_pruned=0`). |
| **Task 5** | `dfd354a` | **PASS** | Imported 5 books across all 5 ingestion paths, ran 3x force-stop + `adb reboot`; `activeKfx=43` and all KFX/cover/DB records intact (`.sdr` created lazily on open). |
| **Task 6** | `2f8a2dd` | **PASS** | Normalized `standardebooks.org` `.epub` links with `?source=download`; verified live `Ivanov` import (`893,241 B` `CONT` KFX) opened in reader. |
| **Task 7** | `4c4c3f2` | **PASS** | Audited `SafeHttp.getBytes` callers (OPDS cover images only; zero EPUB callers); added `8 MiB` `getBytes(url, maxBytes)` overload for primary OPDS cover fetch. |
| **Task 8** | `1b76454` | **PASS** | Implemented `BigInteger` midpoint + local re-spread (`planOrderInsertion`) for `TITLE_ORDER` / `AUTHOR_ORDER` / `AUTHOR_ORDER_REVERSED`, verified via 4 JVM tests. |
| **Task 9** | `79784e2` | **PARTIAL** | `166/166` unit tests pass, staged `azath_v18.9_kfx/base.apk`; M30 active PID `946` ran `118.7s` (badge, theme toggle, open book, live `Collected Poems` import, `61.8s` reader hold) after cold PID `32466` hit the native `KSDK` `Thread-20` `SIGSEGV`. |

---

## 4. Mandatory Cover Architecture Directive (Verbatim — Preserved)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
