#!/usr/bin/env bash
# ==============================================================================
# Clone & Deploy Kindle v8.159.0.100 as a Separate Package (com.amazon.kindle.epub)
# Keeps original com.amazon.kindle intact and adds local EPUB library tracking!
# ==============================================================================
set -euo pipefail

export PATH="/opt/android-sdk/build-tools/36.0.0:/opt/android-sdk/build-tools/34.0.0:$PATH"

NEW_PKG="${1:-com.amazon.kindle.epub}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MOD_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
WORK_DIR="/tmp/kindle_clone_build"
rm -rf "${WORK_DIR}"
mkdir -p "${WORK_DIR}/orig_splits" "${WORK_DIR}/signed_splits"

if [[ -f "${MOD_ROOT}/../handoff_assets/com.amazon.kindle_v8.159.0.100_base_original.apk" ]]; then
    echo "[1/6] Using verified v8.159.0.100 base APK from handoff_assets..."
    cp "${MOD_ROOT}/../handoff_assets/com.amazon.kindle_v8.159.0.100_base_original.apk" "${WORK_DIR}/orig_splits/base.apk"
else
    echo "[1/6] Pulling all split APKs for com.amazon.kindle from device..."
    for p in $(adb shell pm path com.amazon.kindle | sed 's/^package://;s/\r$//'); do
        bn="$(basename "$p")"
        adb pull "$p" "${WORK_DIR}/orig_splits/${bn}"
    done
fi

echo "[2/6] Decoding base.apk manifest with apktool (no-src)..."
apktool d -s -f "${WORK_DIR}/orig_splits/base.apk" -o "${WORK_DIR}/base_dec"

echo "[3/6] Rewriting package name (${NEW_PKG}), removing sharedUserId, and renaming conflicting authorities/permissions..."
python3 - "${WORK_DIR}/base_dec/AndroidManifest.xml" "${NEW_PKG}" <<'PYEOF'
import re, sys
manifest_path, new_pkg = sys.argv[1], sys.argv[2]
with open(manifest_path, "r", encoding="utf-8") as f:
    xml = f.read()

# 1. Remove android:sharedUserId="com.amazon"
xml = re.sub(r'\s*android:sharedUserId="[^"]*"', '', xml)

# 2. Change package="com.amazon.kindle" -> package="<new_pkg>"
xml = re.sub(r'package="com\.amazon\.kindle"', f'package="{new_pkg}"', xml, count=1)

# 3. Rename all android:authorities="..." so they never collide with stock com.amazon.kindle
def repl_auth(m):
    auths = m.group(1).split(";")
    out = []
    for a in auths:
        if "com.amazon.kindle" in a:
            out.append(a.replace("com.amazon.kindle", new_pkg))
        else:
            out.append(f"{a}.epub")
    return f'android:authorities="{";".join(out)}"'
xml = re.sub(r'android:authorities="([^"]+)"', repl_auth, xml)

# 4. Rename custom <permission android:name="..."/> declarations to avoid INSTALL_FAILED_DUPLICATE_PERMISSION
custom_perms = set(re.findall(r'<permission[^>]+android:name="([^"]+)"', xml))
for p in custom_perms:
    new_p = p.replace("com.amazon.kindle", new_pkg) if "com.amazon.kindle" in p else f"{new_pkg}.perm.{p}"
    xml = xml.replace(f'"{p}"', f'"{new_p}"')

# 5. Update app label so you can tell the cloned app apart in launcher
xml = re.sub(r'android:label="[^"]*"', 'android:label="Kindle EPUB"', xml, count=1)

# 6. Add MANAGE_EXTERNAL_STORAGE permission for Android 11+ scoped storage access
if 'android.permission.MANAGE_EXTERNAL_STORAGE' not in xml:
    xml = re.sub(r'(<application\b)', r'<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"/>\n    \1', xml, count=1)

with open(manifest_path, "w", encoding="utf-8") as f:
    f.write(xml)
print(f"Patched AndroidManifest.xml: {len(custom_perms)} custom permissions & all authorities renamed for {new_pkg}")
PYEOF

echo "[4/6] Shifting DEX files, patching classes4 -> classes5, injecting enhanced classes4.dex, and rebuilding..."
mv "${WORK_DIR}/base_dec/classes8.dex" "${WORK_DIR}/base_dec/classes9.dex"
mv "${WORK_DIR}/base_dec/classes7.dex" "${WORK_DIR}/base_dec/classes8.dex"
mv "${WORK_DIR}/base_dec/classes6.dex" "${WORK_DIR}/base_dec/classes7.dex"
python3 "${MOD_ROOT}/tools/patch_classes5.py" "${WORK_DIR}/base_dec/classes5.dex" "${WORK_DIR}/base_dec/classes6.dex"
rm -f "${WORK_DIR}/base_dec/classes5.dex"

python3 "${MOD_ROOT}/tools/patch_classes4.py" "${WORK_DIR}/base_dec/classes4.dex" "${WORK_DIR}/base_dec/classes5.dex"
cp "${MOD_ROOT}/dex/classes9.dex" "${WORK_DIR}/base_dec/classes4.dex"

apktool b "${WORK_DIR}/base_dec" -o "${WORK_DIR}/base_cloned_raw.apk"

echo "[5/6] Signing base_cloned.apk + all split APKs with persistent keystore..."
KEYSTORE="${MOD_ROOT}/../handoff_assets/debug.keystore"
ALIAS="androiddebugkey"
if [[ ! -f "${KEYSTORE}" ]]; then
    KEYSTORE="${MOD_ROOT}/epub_mod.jks"
    ALIAS="epubmod"
    if [[ ! -f "${KEYSTORE}" ]]; then
        keytool -genkeypair -v -keystore "${KEYSTORE}" -alias "${ALIAS}" -keyalg RSA -keysize 2048 -validity 10000 \
            -storepass android -keypass android -dname "CN=KindleEPUB,O=Mod,C=US" >/dev/null 2>&1
    fi
fi

zipalign -f -p 4 "${WORK_DIR}/base_cloned_raw.apk" "${WORK_DIR}/signed_splits/base.apk"
apksigner sign --ks "${KEYSTORE}" --ks-pass pass:android --ks-key-alias "${ALIAS}" --key-pass pass:android "${WORK_DIR}/signed_splits/base.apk"

for f in "${WORK_DIR}/orig_splits/"*.apk; do
    bn="$(basename "$f")"
    if [[ "$bn" != "base.apk" ]]; then
        zipalign -f -p 4 "$f" "${WORK_DIR}/signed_splits/${bn}"
        apksigner sign --ks "${KEYSTORE}" --ks-pass pass:android --ks-key-alias "${ALIAS}" --key-pass pass:android "${WORK_DIR}/signed_splits/${bn}"
    fi
done

echo "[6/6] Installing ${NEW_PKG} alongside original com.amazon.kindle..."
if [[ $(ls -1 "${WORK_DIR}/signed_splits/"*.apk | wc -l) -gt 1 ]]; then
    adb install-multiple -r -d "${WORK_DIR}/signed_splits/"*.apk
else
    adb install -r -d "${WORK_DIR}/signed_splits/base.apk"
fi

adb shell appops set "${NEW_PKG}" MANAGE_EXTERNAL_STORAGE allow || true
adb shell pm grant "${NEW_PKG}" android.permission.READ_EXTERNAL_STORAGE || true

echo "Pushing test EPUBs to /sdcard/kindle/ and opening kindle_exam_v3.epub..."
adb shell "mkdir -p /sdcard/kindle /sdcard/Download"
adb push "${MOD_ROOT}/test_containers/kindle_exam_v3.epub" /sdcard/kindle/kindle_exam_v3.epub
adb push "${MOD_ROOT}/test_containers/kindle_tables_images_exam.epub" /sdcard/kindle/kindle_tables_images_exam.epub
adb shell am start -n "${NEW_PKG}/com.amazon.kindle.ThirdPartyUpgradePage" \
    -a android.intent.action.VIEW \
    -d "file:///sdcard/kindle/kindle_exam_v3.epub" \
    -t "application/epub+zip"
