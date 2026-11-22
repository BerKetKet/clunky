#!/usr/bin/env bash
set -euo pipefail
export PATH="/opt/android-sdk/build-tools/36.0.0:${PATH}"

MOD_ROOT="/home/bgg/projects/clunky/07_APK_EPUB_INGESTION_MOD"
WORK_DIR="/tmp/kindle_clone_build"
NEW_PKG="com.amazon.kindle.epub"

echo "[0/5] Compiling mod DEX (classes9.dex)..."
bash "${MOD_ROOT}/tools/compile_mod_dex.sh" "${MOD_ROOT}/dex/classes9.dex"

echo "[1/5] Copying compiled mod DEX to base_dec/classes4.dex and libkfxconv.so..."
cp "${MOD_ROOT}/dex/classes9.dex" "${WORK_DIR}/base_dec/classes4.dex"
mkdir -p "${WORK_DIR}/base_dec/lib/arm64-v8a"
cp "${MOD_ROOT}/lib/arm64-v8a/libkfxconv.so" "${WORK_DIR}/base_dec/lib/arm64-v8a/libkfxconv.so"

echo "[1b/5] Patching strings.xml Folio library branding..."
find "${WORK_DIR}/base_dec/res" -name "strings.xml" -exec sed -i 's/in your Kindle library\./in your Folio library\./g' {} +
find "${WORK_DIR}/base_dec/res" -name "strings.xml" -exec sed -i 's/your Kindle library/your Folio library/g' {} +

echo "[2/5] Building APK with apktool..."
apktool b "${WORK_DIR}/base_dec" -o "${WORK_DIR}/base_cloned_raw.apk"

echo "[3/5] Aligning and signing..."
KEYSTORE="${MOD_ROOT}/../handoff_assets/debug.keystore"
ALIAS="androiddebugkey"
zipalign -f -p 4 "${WORK_DIR}/base_cloned_raw.apk" "${WORK_DIR}/signed_splits/base.apk"
apksigner sign --ks "${KEYSTORE}" --ks-pass pass:android --ks-key-alias "${ALIAS}" --key-pass pass:android "${WORK_DIR}/signed_splits/base.apk"

echo "[4/5] Streamed install to device..."
if [[ $(ls -1 "${WORK_DIR}/signed_splits/"*.apk | wc -l) -gt 1 ]]; then
    adb install-multiple -r -d "${WORK_DIR}/signed_splits/"*.apk
else
    adb install -r -d "${WORK_DIR}/signed_splits/base.apk"
fi

echo "[5/5] Staging Folio v3.8 KFX release..."
mkdir -p "${MOD_ROOT}/release/folio_v3.8_kfx"
cp -a "${WORK_DIR}/signed_splits/base.apk" "${MOD_ROOT}/release/folio_v3.8_kfx/base.apk"
if [[ -f "${WORK_DIR}/signed_splits/base.apk.idsig" ]]; then
    cp -a "${WORK_DIR}/signed_splits/base.apk.idsig" "${MOD_ROOT}/release/folio_v3.8_kfx/base.apk.idsig"
fi

echo "Fast rebuild, deploy, and Folio v3.8 KFX staging successful!"
