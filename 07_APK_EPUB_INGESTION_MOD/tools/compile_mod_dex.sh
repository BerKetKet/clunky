#!/usr/bin/env bash
# Compiles mod_src/real (dp.g, EpubBookBuilder, EpubTranscoder) into a clean classes4.dex
# using javac + Android SDK d8 (excluding compile-time stubs).
set -euo pipefail

export PATH="/opt/android-sdk/build-tools/36.0.0:/opt/android-sdk/build-tools/34.0.0:$PATH"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MOD_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
SRC_STUBS="${MOD_ROOT}/mod_src/stubs"
SRC_REAL="${MOD_ROOT}/mod_src/real"
OUT_DEX="${1:-${MOD_ROOT}/dex/classes9.dex}"

BUILD_DIR="/tmp/kindle_mod_dex_compile"
rm -rf "${BUILD_DIR}"
mkdir -p "${BUILD_DIR}/stubs_cls" "${BUILD_DIR}/real_cls" "${BUILD_DIR}/dex_out"

echo "[1/3] Compiling compile-time stubs..."
javac --release 8 -d "${BUILD_DIR}/stubs_cls" $(find "${SRC_STUBS}" -name "*.java")

echo "[2/3] Compiling real mod classes (dp.g, EpubBookBuilder, EpubTranscoder)..."
javac --release 8 -cp "${BUILD_DIR}/stubs_cls" -d "${BUILD_DIR}/real_cls" $(find "${SRC_REAL}" -name "*.java")

echo "[3/3] Converting real_cls to Dalvik DEX via d8..."
d8 --min-api 26 --output "${BUILD_DIR}/dex_out" $(find "${BUILD_DIR}/real_cls" -name "*.class")

mkdir -p "$(dirname "${OUT_DEX}")"
cp "${BUILD_DIR}/dex_out/classes.dex" "${OUT_DEX}"
echo "Successfully built ${OUT_DEX} ($(stat -c%s "${OUT_DEX}") bytes)"
