# Kindle Android v8.159.0.100 — Local EPUB Ingestion & Native Display Modification

## 1. Exact Root Cause in Stock `com.amazon.kindle` v8.159.0.100
In the stock Kindle Android APK (`v8.159.0.100`):
1. `AndroidManifest.xml` already registers `com.amazon.kindle.ThirdPartyUpgradePage` for `android.intent.action.VIEW` with `file://` and `content://` schemes and `mimeType="application/epub+zip"`.
2. However, `ThirdPartyUpgradePage` explicitly diverts `.epub` files to Amazon's Send-to-Kindle cloud upload (`com.amazon.kindle.s2k.SendToKindleActivity`) instead of opening them locally:
   - `ThirdPartyUpgradePage.handleContentScheme()` (`classes4.dex`, `code_off=0x250140`): calls `ContentSchemeBookOpenHelperActivity.getContentTypeFromUri(this, uri)`. When `contentType == ContentType.c` (`EPUB`), it calls `routeEpubToSendToKindle(uri)` instead of `openDocumentForContent(uri)`.
   - `ThirdPartyUpgradePage.handleFileScheme()` (`classes4.dex`, `code_off=0x250248`): checks `BookContentType.a(file)` and if `EPUB`, calls `routeEpubToSendToKindle(uri)`; if not in `SUPPORTED_FILE_EXTENSIONS`, shows an unsupported-file `AlertActivity`; only `.mobi`/`.prc`/`.azw3`/`.pdf` reach `BookOpenManagerBuilderFactory.a().build().f(path, this, EntryPoint.FILE_SCHEME)`.
3. `ContentSchemeBookOpenHelperActivity.getContentTypeFromFilePath()` (`0x24cd7c`) and `getContentTypeFromUri()` (`0x24cdac`) only map `.prc`/`.mobi` to `.prc` and fall back to `.pdf` (`ContentType.a`) for any non-MOBI extension, which causes `copyContentToRead()` to name the temp file `content.pdf` and route it to `PdfReaderActivity`.
4. `KRF4ContentModule.f()` (`classes4.dex`, `0x3eebcc`) registers `[".prc", ".pdb", ".azw", ".azw1", ".mobi", ".tpz", ".azw3"]` into `Ldp/g;` and `Ldp/f;` (`LocalContentFactory`), omitting `".epub"`.
5. Native `libKRF.so` (`KindleDocumentFactory` -> `EBookPDB` / `KindleDocumentBuilderMobi` / `KindleDocumentBuilderWebCore`) and `libKindleAndroidNativeBundlerJNI.so` (`BookYJ::createBook`) only parse PalmDB (`BOOKMOBI` / `TEXtREAd`), Topaz (`TPZ`), or KFX/KDF containers, not raw `.epub` ZIP archives.

## 2. Binary Bytecode Patches Applied (`classes4.dex` + `classes9.dex`)

### A. `classes4.dex` (`8,505,460` bytes)
| Target Class & Method | Code Offset | Exact Dalvik Patch | Effect |
|---|---|---|---|
| `ThirdPartyUpgradePage.handleContentScheme()` | `0x250140` | NOPed units `0005..000c` (`if (type == EPUB) routeEpubToSendToKindle`) | All `content://` EPUB URIs flow directly into `openDocumentForContent(uri)` |
| `ThirdPartyUpgradePage.handleFileScheme()` | `0x250248` | Replaced unit `000c` (`6201 6f36`) with `283c 0000` (`goto +0x3c -> 0048`) | All non-null `file://` paths bypass `routeEpubToSendToKindle` and `AlertActivity` and invoke `BookOpenManager.f(path, this, EntryPoint.FILE_SCHEME)` |
| `ThirdPartyUpgradePage.routeEpubToSendToKindle(Uri)` | `0x250450` | Replaced body with `invoke-virtual {v4, v5}, BaseUpgradePage;->openDocumentForContent(Uri)V; return-void` | Safety net redirecting any remaining Send-to-Kindle EPUB call to local reader |
| `ContentSchemeBookOpenHelperActivity.getContentTypeFromFilePath()` & `getContentTypeFromUri()` | `0x24cd7c`, `0x24cdac` | Swapped `ContentType.b` (`0x249e`, `MOBI`) and `ContentType.a` (`0x249d`, `PDF`) | Only explicit `.pdf` / `application/pdf` gets `.pdf`; `.epub` and `application/epub+zip` get `.prc` temp files routed to `dp.g` |
| `KRF4ContentModule.f()` | `0x3eebcc` | Changed `const-string v3, ".azw1"` (`0x11f9`) to `const-string v3, ".epub"` (`0x1211`) | Registers `".epub"` in `LocalContentFactory` (`dp.f`) at app startup |
| `SendToKindleActivity.continueBuildActivity()` | `0x4737e4` | Replaced cloud upload UI setup with `readIntentExtras(intent)` + `BookOpenManager.f(m_file.getAbsolutePath(), this, EntryPoint.FILE_SCHEME)` + `finish()` | Sharing an `.epub` via Android's `ACTION_SEND` "Share to Kindle" opens it locally without cloud upload or sign-in |
| `Ldp/g;` (`class_def #8367`) | `class_defs` | Removed `Ldp/g;` from `classes4.dex` (`class_defs_size: 10561 -> 10560`) | Allows MultiDex `PathClassLoader` to load the enhanced `Ldp/g;` from `classes9.dex` |

### B. `classes9.dex` (`6,820` bytes)
- **`Ldp/g;`**: Extends `Ldp/b;`, pre-populates `".epub"` and `".azw1"` in its supported extension set, wraps the `Lji/j;` (`LibraryCachedKRFBookBuilder`) instance in `Lcom/amazon/kindle/epub/EpubBookBuilder;`, and overrides `a(String, File)` and `e(String, File, ContentState, xt.a)` to pass `File` through `EpubTranscoder.ensureKindleFile(File)`.
- **`Lcom/amazon/kindle/epub/EpubBookBuilder;`**: Implements `Lji/j;` (`a`, `b`, `c`), passing every file path through `EpubTranscoder.ensureKindlePath(String)` before delegating to `Lji/p;`.
- **`Lcom/amazon/kindle/epub/EpubTranscoder;`**: Detects `.epub` files (both by `.epub` extension and by `PK\x03\x04` ZIP magic header on `content://` temp `.prc` files), extracts OPF metadata (`<dc:title>`, `<dc:creator>`), extracts and normalizes XHTML chapter bodies with `<mbp:pagebreak/>`, and writes a spec-compliant multi-record 4096-byte PalmDB `BOOKMOBI` container into `/data/data/com.amazon.kindle/files/epub_cache/<filename>.prc` with a deterministic 10-character ASIN (`B0%08X`).
