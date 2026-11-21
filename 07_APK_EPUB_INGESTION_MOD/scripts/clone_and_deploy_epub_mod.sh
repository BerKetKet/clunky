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
HAS_CUSTOM_ICON=0
ICON_SRC="${MOD_ROOT}/assets/folio_launcher_icon.png"
ICON_BG="${MOD_ROOT}/assets/folio_icon_bg.png"
ICON_FG="${MOD_ROOT}/assets/folio_icon_fg.png"
if [[ -f "${ICON_SRC}" ]]; then
    mkdir -p "${WORK_DIR}/base_dec/res/drawable" "${WORK_DIR}/base_dec/res/mipmap-xxxhdpi" "${WORK_DIR}/base_dec/res/mipmap-anydpi-v26"
    cp "${ICON_SRC}" "${WORK_DIR}/base_dec/res/mipmap-xxxhdpi/folio_launcher_icon.png"
    if [[ -f "${ICON_BG}" && -f "${ICON_FG}" ]]; then
        cp "${ICON_BG}" "${WORK_DIR}/base_dec/res/drawable/folio_icon_bg.png"
        cp "${ICON_FG}" "${WORK_DIR}/base_dec/res/drawable/folio_icon_fg.png"
        cat > "${WORK_DIR}/base_dec/res/mipmap-anydpi-v26/folio_launcher_icon.xml" <<'XMLEND'
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/folio_icon_bg"/>
    <foreground android:drawable="@drawable/folio_icon_fg"/>
</adaptive-icon>
XMLEND
        echo "Bundled Android Adaptive Icon (@mipmap/folio_launcher_icon)"
    fi
    HAS_CUSTOM_ICON=1
fi
APP_LABEL="${APP_LABEL:-Folio}" HAS_CUSTOM_ICON="${HAS_CUSTOM_ICON}" python3 - "${WORK_DIR}/base_dec/AndroidManifest.xml" "${NEW_PKG}" <<'PYEOF'
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

# 5. Update app label ("Folio") and custom launcher icon (@mipmap/folio_launcher_icon)
import os
app_label = os.environ.get("APP_LABEL", "Folio")
has_custom_icon = os.environ.get("HAS_CUSTOM_ICON", "0") == "1"

# Patch <application> label, icon, roundIcon
def patch_app_tag(m):
    tag = m.group(0)
    if 'android:label=' in tag:
        tag = re.sub(r'android:label="[^"]*"', f'android:label="{app_label}"', tag)
    else:
        tag = tag.replace('<application', f'<application android:label="{app_label}"', 1)
    if has_custom_icon:
        if 'android:icon=' in tag:
            tag = re.sub(r'android:icon="[^"]*"', 'android:icon="@mipmap/folio_launcher_icon"', tag)
        else:
            tag = tag.replace('<application', '<application android:icon="@mipmap/folio_launcher_icon"', 1)
        if 'android:roundIcon=' in tag:
            tag = re.sub(r'android:roundIcon="[^"]*"', 'android:roundIcon="@mipmap/folio_launcher_icon"', tag)
        else:
            tag = tag.replace('<application', '<application android:roundIcon="@mipmap/folio_launcher_icon"', 1)
    if 'android:extractNativeLibs=' in tag:
        tag = re.sub(r'android:extractNativeLibs="[^"]*"', 'android:extractNativeLibs="true"', tag)
    else:
        tag = tag.replace('<application', '<application android:extractNativeLibs="true"', 1)
    return tag
xml = re.sub(r'<application\b[^>]*>', patch_app_tag, xml, count=1)

# Also patch any <activity> or <activity-alias> containing CATEGORY_LAUNCHER so launcher displays new name & icon
def patch_launcher_elem(m):
    block = m.group(0)
    if 'android.intent.category.LAUNCHER' not in block:
        return block
    open_tag_m = re.match(r'<(activity(?:-alias)?)\b[^>]*>', block)
    if not open_tag_m:
        return block
    ot = open_tag_m.group(0)
    if 'android:label=' in ot:
        ot = re.sub(r'android:label="[^"]*"', f'android:label="{app_label}"', ot)
    else:
        ot = ot[:-1] + f' android:label="{app_label}">'
    if has_custom_icon:
        if 'android:icon=' in ot:
            ot = re.sub(r'android:icon="[^"]*"', 'android:icon="@mipmap/folio_launcher_icon"', ot)
        else:
            ot = ot[:-1] + ' android:icon="@mipmap/folio_launcher_icon">'
        if 'android:roundIcon=' in ot:
            ot = re.sub(r'android:roundIcon="[^"]*"', 'android:roundIcon="@mipmap/folio_launcher_icon"', ot)
    return ot + block[open_tag_m.end():]
xml = re.sub(r'<(activity(?:-alias)?)\b[^>]*>.*?</\1>', patch_launcher_elem, xml, flags=re.DOTALL)

# 6. Add MANAGE_EXTERNAL_STORAGE permission for Android 11+ scoped storage access
if 'android.permission.MANAGE_EXTERNAL_STORAGE' not in xml:
    xml = re.sub(r'(<application\b)', r'<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"/>\n    \1', xml, count=1)

# 7. Ensure android:usesCleartextTraffic="true" on <application> and remove networkSecurityConfig
if 'android:usesCleartextTraffic' not in xml:
    xml = re.sub(r'(<application\b)', r'\1 android:usesCleartextTraffic="true"', xml, count=1)
xml = re.sub(r'\s*android:networkSecurityConfig="[^"]*"', '', xml)

# 8. Register OpenCatalogActivity inside <application> (no LAUNCHER filter, embedded via Home tab)
catalog_activity = '''
        <activity android:name="com.amazon.kindle.epub.catalog.OpenCatalogActivity" android:exported="true" android:theme="@android:style/Theme.Material.NoActionBar"/>
    </application>'''
if 'com.amazon.kindle.epub.catalog.OpenCatalogActivity' not in xml:
    xml = xml.replace('</application>', catalog_activity)

with open(manifest_path, "w", encoding="utf-8") as f:
    f.write(xml)
print(f"Patched AndroidManifest.xml: registered OpenCatalogActivity, {len(custom_perms)} custom permissions & all authorities renamed for {new_pkg}")
PYEOF

if [[ -f "${WORK_DIR}/base_dec/res/xml/network_security_config.xml" ]]; then
    sed -i 's/cleartextTrafficPermitted="false"/cleartextTrafficPermitted="true"/g' "${WORK_DIR}/base_dec/res/xml/network_security_config.xml"
    echo "Patched network_security_config.xml to allow cleartext traffic"
fi

echo "[3.5/6] Compiling mod DEX (EpubTranscoder, EpubBookBuilder, dp.g, OpenCatalogView) with javac + d8..."
bash "${MOD_ROOT}/tools/compile_mod_dex.sh" "${MOD_ROOT}/dex/classes9.dex"

echo "[4/6] Patching classes2 (Spacing gates), classes3 (RubyHomeFragment), shifting DEX files, patching classes4 -> classes5, classes5 -> classes6, injecting enhanced classes4.dex, and rebuilding..."
python3 "${MOD_ROOT}/tools/patch_classes2.py" "${WORK_DIR}/base_dec/classes2.dex"
bash "${MOD_ROOT}/tools/patch_classes3.sh" "${WORK_DIR}/base_dec/classes3.dex"
mv "${WORK_DIR}/base_dec/classes8.dex" "${WORK_DIR}/base_dec/classes9.dex"
mv "${WORK_DIR}/base_dec/classes7.dex" "${WORK_DIR}/base_dec/classes8.dex"
mv "${WORK_DIR}/base_dec/classes6.dex" "${WORK_DIR}/base_dec/classes7.dex"
python3 "${MOD_ROOT}/tools/patch_classes5.py" "${WORK_DIR}/base_dec/classes5.dex" "${WORK_DIR}/base_dec/classes6.dex"
rm -f "${WORK_DIR}/base_dec/classes5.dex"

python3 "${MOD_ROOT}/tools/patch_classes4.py" "${WORK_DIR}/base_dec/classes4.dex" "${WORK_DIR}/base_dec/classes5.dex"
cp "${MOD_ROOT}/dex/classes9.dex" "${WORK_DIR}/base_dec/classes4.dex"

if [[ -f "${MOD_ROOT}/lib/arm64-v8a/libkfxconv.so" ]]; then
    mkdir -p "${WORK_DIR}/base_dec/lib/arm64-v8a"
    cp "${MOD_ROOT}/lib/arm64-v8a/libkfxconv.so" "${WORK_DIR}/base_dec/lib/arm64-v8a/libkfxconv.so"
    echo "Bundled libkfxconv.so into base_dec/lib/arm64-v8a/"
fi

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

RELEASE_V22="${MOD_ROOT}/release/folio_v2.2_kfx"
RELEASE_V23="${MOD_ROOT}/release/folio_v2.3_kfx"
mkdir -p "${RELEASE_V22}" "${RELEASE_V23}"
cp "${WORK_DIR}/signed_splits/base.apk" "${RELEASE_V22}/base.apk"
cp "${WORK_DIR}/signed_splits/base.apk" "${RELEASE_V23}/base.apk"
if [[ -f "${WORK_DIR}/signed_splits/base.apk.idsig" ]]; then
    cp "${WORK_DIR}/signed_splits/base.apk.idsig" "${RELEASE_V22}/base.apk.idsig"
    cp "${WORK_DIR}/signed_splits/base.apk.idsig" "${RELEASE_V23}/base.apk.idsig"
fi
echo "Staged Folio v2.3 in ${RELEASE_V23}/base.apk"

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
