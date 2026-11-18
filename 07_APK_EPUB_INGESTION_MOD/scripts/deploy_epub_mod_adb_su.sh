#!/usr/bin/env bash
# ==============================================================================
# Automated Rooted Phone & APK Repackaging Deployment Script
# Target: Amazon Kindle Android v8.159.0.100 (com.amazon.kindle, arm64-v8a)
# Device: Samsung Galaxy M30/M32 (1080x2340 @ 420 dpi, Android 13, adb su)
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MOD_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
DEX4="${MOD_ROOT}/dex/classes4.dex"
DEX9="${MOD_ROOT}/dex/classes9.dex"

echo "[1/5] Locating installed com.amazon.kindle base.apk on device..."
APK_PATH="$(adb shell pm path com.amazon.kindle | head -n1 | sed 's/^package://;s/$//')"
if [[ -z "${APK_PATH}" ]]; then
    echo "ERROR: com.amazon.kindle is not installed on the connected device."
    exit 1
fi
echo "      Found base.apk at: ${APK_PATH}"

WORK_DIR="/tmp/kindle_epub_mod_build"
rm -rf "${WORK_DIR}"
mkdir -p "${WORK_DIR}"

echo "[2/5] Pulling base.apk from device..."
adb pull "${APK_PATH}" "${WORK_DIR}/base_orig.apk"
cp "${WORK_DIR}/base_orig.apk" "${WORK_DIR}/base_patched.apk"

echo "[3/5] Injecting patched classes4.dex and new classes9.dex into base_patched.apk..."
cp "${DEX4}" "${WORK_DIR}/classes4.dex"
cp "${DEX9}" "${WORK_DIR}/classes9.dex"
(
    cd "${WORK_DIR}"
    zip -0 -u base_patched.apk classes4.dex classes9.dex
)

echo "[4/5] Deploying patched APK and clearing oat/art cache via adb su..."
adb push "${WORK_DIR}/base_patched.apk" /data/local/tmp/base_patched.apk
adb shell su -c "am force-stop com.amazon.kindle"
adb shell su -c "mkdir -p /data/data/com.amazon.kindle/files/epub_cache && chmod 777 /data/data/com.amazon.kindle/files/epub_cache"

# Option A: Direct root bind-mount / in-place replacement (preserves original app data & signature state)
adb shell su -c "cp /data/local/tmp/base_patched.apk '${APK_PATH}' && chmod 644 '${APK_PATH}' && chown system:system '${APK_PATH}'"
APK_DIR="$(dirname "${APK_PATH}")"
adb shell su -c "rm -rf '${APK_DIR}/oat' /data/dalvik-cache/arm64/*com.amazon.kindle*"

echo "[5/5] Pushing test EPUBs and launching com.amazon.kindle..."
adb push "${MOD_ROOT}/test_containers/kindle_exam_v3.epub" /sdcard/Download/kindle_exam_v3.epub
adb push "${MOD_ROOT}/test_containers/kindle_tables_images_exam.epub" /sdcard/Download/kindle_tables_images_exam.epub
adb shell su -c "mkdir -p /sdcard/kindle && cp /sdcard/Download/kindle_exam_v3.epub /sdcard/kindle/"

# Trigger direct file:// EPUB open in Kindle reader
adb shell am start -n com.amazon.kindle/com.amazon.kindle.ThirdPartyUpgradePage     -a android.intent.action.VIEW     -d "file:///sdcard/Download/kindle_exam_v3.epub"     -t "application/epub+zip"

echo "Done! Kindle is now opening kindle_exam_v3.epub locally."
