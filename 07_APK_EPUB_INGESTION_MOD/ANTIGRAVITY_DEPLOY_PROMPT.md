# Antigravity Local Agent Prompt: Deploy & Verify Kindle v8.159.0.100 Local EPUB Ingestion Mod

Copy and paste the prompt below directly to your local Antigravity agent (which has `adb su` access to your rooted phone):

```text
You have access to the rooted Samsung phone over ADB (`adb shell su -c ...`) and the extracted `07_APK_EPUB_INGESTION_MOD` directory from `kindle_apk_epub_mod_v8.159.0.100.zip` (or `kindle_rendering_engine_master_dataset_epub_mod.zip`).

Your task is to deploy the binary-patched `classes4.dex` and new `classes9.dex` into the installed `com.amazon.kindle` (v8.159.0.100) APK on the phone, re-compile/clear the ART oat cache, and verify that `.epub` files open directly in the native Kindle reader without triggering Send-to-Kindle cloud upload.

Execute these exact steps:

1. Locate the installed Kindle APK path on the phone:
   APK_PATH=$(adb shell pm path com.amazon.kindle | head -n1 | sed 's/^package://;s/$//')
    APK_DIR=$(dirname "$APK_PATH")

2. Pull the original `base.apk` from `$APK_PATH` to `/tmp/kindle_base.apk`, make a backup `/tmp/kindle_base.apk.bak`, and update `/tmp/kindle_base.apk` with the provided `dex/classes4.dex` and `dex/classes9.dex` stored uncompressed (`zip -0`):
   cp 07_APK_EPUB_INGESTION_MOD/dex/classes4.dex /tmp/classes4.dex
   cp 07_APK_EPUB_INGESTION_MOD/dex/classes9.dex /tmp/classes9.dex
   (cd /tmp && zip -0 -u kindle_base.apk classes4.dex classes9.dex)

3. Deploy the patched `base.apk` over root (`su -c`) and rebuild the oat cache:
   adb push /tmp/kindle_base.apk /data/local/tmp/kindle_base.apk
   adb shell su -c "am force-stop com.amazon.kindle"
   adb shell su -c "cp /data/local/tmp/kindle_base.apk '$APK_PATH' && chown system:system '$APK_PATH' && chmod 644 '$APK_PATH'"
   adb shell su -c "rm -rf '$APK_DIR/oat' /data/dalvik-cache/arm64/*com.amazon.kindle*"
   adb shell su -c "mkdir -p /data/data/com.amazon.kindle/files/epub_cache && chmod 777 /data/data/com.amazon.kindle/files/epub_cache"
   adb shell su -c "cmd package compile -m speed-profile -f com.amazon.kindle || true"

   (Note: If Android's PackageManager signature check rejects an unsigned zip modification on reboot, either disable APK signature verification via Magisk/CorePatch or resign all split APKs in `$APK_DIR/*.apk` with the same v2/v3 key using `apksigner` and install via `pm install-create` / `pm install-write` / `pm install-commit`.)

4. Push the test `.epub` files and verify all three local EPUB ingestion pathways:
   a) `file://` VIEW intent:
      adb push 07_APK_EPUB_INGESTION_MOD/test_containers/kindle_exam_v3.epub /sdcard/Download/kindle_exam_v3.epub
      adb shell am start -n com.amazon.kindle/com.amazon.kindle.ThirdPartyUpgradePage -a android.intent.action.VIEW -d "file:///sdcard/Download/kindle_exam_v3.epub" -t "application/epub+zip"
   b) Rich HTML tables + embedded images EPUB (`kindle_tables_images_exam.epub`):
      For full high-resolution embedded image records and multi-record tables, you can also run `python3 07_APK_EPUB_INGESTION_MOD/tools/epub2prc.py <input.epub> /data/data/com.amazon.kindle/files/epub_cache/<input.epub>.prc` or push `07_APK_EPUB_INGESTION_MOD/test_containers/kindle_tables_images_exam.prc` to `/data/data/com.amazon.kindle/files/epub_cache/kindle_tables_images_exam.epub.prc` and open `file:///sdcard/Download/kindle_tables_images_exam.epub`.
   c) Local sideload folder scan:
      Copy `.epub` files into `/sdcard/kindle/` or `/sdcard/Android/data/com.amazon.kindle/files/kindle/` and verify they appear in the Kindle Library (`Downloaded` filter) and open in `MobiDocViewer`.

5. Capture `adb logcat -d | grep -iE "ThirdPartyUpgradePage|ContentScheme|EpubTranscoder|LoadContent|MobiDocViewer"` and a screenshot (`screencap -p`) of the rendered EPUB page to confirm local rendering.
```
