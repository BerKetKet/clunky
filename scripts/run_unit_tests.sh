#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MOD_SRC="${ROOT_DIR}/07_APK_EPUB_INGESTION_MOD/mod_src"
ANDROID_JAR="/opt/android-sdk/platforms/android-36/android.jar"
OUT_DIR="/tmp/azath_unit_tests"

rm -rf "${OUT_DIR}"
mkdir -p "${OUT_DIR}/stubs" "${OUT_DIR}/classes"

javac -encoding UTF-8 -source 8 -target 8 -Xlint:none \
    -cp "${ANDROID_JAR}" \
    -d "${OUT_DIR}/stubs" \
    $(find "${MOD_SRC}/stubs" -name "*.java")

javac -encoding UTF-8 -source 8 -target 8 -Xlint:none \
    -cp "${ANDROID_JAR}:${OUT_DIR}/stubs" \
    -d "${OUT_DIR}/classes" \
    $(find "${MOD_SRC}/real" -name "*.java") \
    "${ROOT_DIR}/07_APK_EPUB_INGESTION_MOD/tests/AzathUnitTests.java"

java -cp "${OUT_DIR}/classes:${ANDROID_JAR}" com.amazon.kindle.epub.tests.AzathUnitTests
