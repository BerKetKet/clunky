#!/usr/bin/env bash
set -euo pipefail

TARGET_SERIAL="32001776e8e82623"
APK_PATH="/home/bgg/projects/clunky/07_APK_EPUB_INGESTION_MOD/release/folio_v10.8_kfx/base.apk"

echo "=== Round 108 Live Hardware Deployment & Verification ==="

echo "1. Checking connected ADB devices..."
DEVICES=$(adb devices | grep -w "${TARGET_SERIAL}" || true)
if [ -z "${DEVICES}" ]; then
  echo "Target device ${TARGET_SERIAL} (Samsung Galaxy M32) not currently detected."
  echo "Waiting for device connection..."
  adb wait-for-device
fi

CURRENT_SERIAL=$(adb get-serialno)
if [ "${CURRENT_SERIAL}" != "${TARGET_SERIAL}" ]; then
  echo "ERROR: Connected device serial '${CURRENT_SERIAL}' does not match target '${TARGET_SERIAL}'!"
  exit 1
fi
echo "Verified target device connected: ${CURRENT_SERIAL}"

echo "2. Verifying Stock Kindle package invariant..."
KINDLE_DUMP=$(adb shell dumpsys package com.amazon.kindle | grep -E "versionName|lastUpdateTime")
echo "${KINDLE_DUMP}"
if ! echo "${KINDLE_DUMP}" | grep -q "2026-11-19 21:59:23"; then
  echo "WARNING: Stock Kindle update timestamp has changed!"
fi

echo "3. Installing Folio v10.8 KFX base.apk..."
adb install -r -d "${APK_PATH}"
echo "Folio v10.8 installed successfully."

echo "4. Clearing stale Fight Club KFX from device..."
adb shell su -c "rm -rf /data/data/com.amazon.kindle.epub/files/folio_library/Palahniuk_Chuck_FOLIO_7313DDB45B1B.*" || true

echo "5. Launching Folio v10.8 KFX..."
adb shell monkey -p com.amazon.kindle.epub -c android.intent.category.LAUNCHER 1
sleep 4

echo "Ready for Fight Club re-conversion and Task B visual parity capture."
