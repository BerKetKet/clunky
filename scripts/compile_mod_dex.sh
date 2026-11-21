#!/usr/bin/env bash
# Compiles mod_src/real (dp.g, EpubBookBuilder, EpubTranscoder, and catalog/*)
# into classes9.dex (injected as classes4.dex) using javac + d8.
set -euo pipefail

export PATH="/opt/android-sdk/build-tools/36.0.0:/opt/android-sdk/build-tools/34.0.0:$PATH"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MOD_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
SRC_STUBS="${MOD_ROOT}/mod_src/stubs"
SRC_REAL="${MOD_ROOT}/mod_src/real"
OUT_DEX="${1:-${MOD_ROOT}/dex/classes9.dex}"

ANDROID_JAR="$(ls -1 /opt/android-sdk/platforms/android-*/android.jar 2>/dev/null | sort -V | tail -n1 || true)"
if [[ -z "${ANDROID_JAR}" ]]; then
    echo "ERROR: Could not locate android.jar under /opt/android-sdk/platforms/"
    exit 1
fi
echo "Using Android SDK platform jar: ${ANDROID_JAR}"

BUILD_DIR="/tmp/kindle_mod_dex_compile"
rm -rf "${BUILD_DIR}"
mkdir -p "${BUILD_DIR}/stubs_cls" "${BUILD_DIR}/real_cls" "${BUILD_DIR}/dex_out"

echo "[1/3] Compiling compile-time stubs (Kindle interfaces + ActivityThread)..."
STUB_FILES=$(find "${SRC_STUBS}" -name "*.java" ! -path "*/android/graphics/*" ! -path "*/android/app/Application.java")
javac --release 8 -cp "${ANDROID_JAR}" -d "${BUILD_DIR}/stubs_cls" ${STUB_FILES}

echo "[2/3] Compiling real mod + OpenCatalog classes..."
javac --release 8 -cp "${ANDROID_JAR}:${BUILD_DIR}/stubs_cls" -d "${BUILD_DIR}/real_cls" $(find "${SRC_REAL}" -name "*.java")

echo "[3/3] Converting real_cls to Dalvik DEX via d8..."
d8 --min-api 26 --lib "${ANDROID_JAR}" --output "${BUILD_DIR}/dex_out" $(find "${BUILD_DIR}/real_cls" -name "*.class")

mkdir -p "$(dirname "${OUT_DEX}")"
cp "${BUILD_DIR}/dex_out/classes.dex" "${OUT_DEX}"
echo "Successfully built ${OUT_DEX} ($(stat -c%s "${OUT_DEX}") bytes)"
