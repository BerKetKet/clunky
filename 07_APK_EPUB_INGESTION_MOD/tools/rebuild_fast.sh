#!/usr/bin/env bash
set -euo pipefail
export PATH="/opt/android-sdk/build-tools/36.0.0:${PATH}"

MOD_ROOT="/home/bgg/projects/clunky/07_APK_EPUB_INGESTION_MOD"
WORK_DIR="/tmp/kindle_clone_build"
NEW_PKG="com.amazon.kindle.epub"

echo "[1/4] Copying compiled mod DEX to base_dec/classes4.dex..."
cp "${MOD_ROOT}/dex/classes9.dex" "${WORK_DIR}/base_dec/classes4.dex"

echo "[2/4] Building APK with apktool..."
apktool b "${WORK_DIR}/base_dec" -o "${WORK_DIR}/base_cloned_raw.apk"

echo "[3/4] Aligning and signing..."
KEYSTORE="${MOD_ROOT}/../handoff_assets/debug.keystore"
ALIAS="androiddebugkey"
zipalign -f -p 4 "${WORK_DIR}/base_cloned_raw.apk" "${WORK_DIR}/signed_splits/base.apk"
apksigner sign --ks "${KEYSTORE}" --ks-pass pass:android --ks-key-alias "${ALIAS}" --key-pass pass:android "${WORK_DIR}/signed_splits/base.apk"

echo "[4/4] Streamed install to device..."
if [[ $(ls -1 "${WORK_DIR}/signed_splits/"*.apk | wc -l) -gt 1 ]]; then
    adb install-multiple -r -d "${WORK_DIR}/signed_splits/"*.apk
else
    adb install -r -d "${WORK_DIR}/signed_splits/base.apk"
fi
echo "Fast rebuild and deploy successful!"
