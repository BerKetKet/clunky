#!/usr/bin/env bash
set -euo pipefail
export PATH="/opt/android-sdk/build-tools/36.0.0:${PATH}"

MOD_ROOT="/home/bgg/projects/clunky/07_APK_EPUB_INGESTION_MOD"
WORK_DIR="/tmp/kindle_clone_build"
NEW_PKG="com.azath.reader"

echo "[0/5] Compiling mod DEX (classes9.dex)..."
bash "${MOD_ROOT}/tools/compile_mod_dex.sh" "${MOD_ROOT}/dex/classes9.dex"

echo "[1/5] Copying compiled mod DEX to base_dec/classes4.dex, libkfxconv.so, and azath_icons..."
cp "${MOD_ROOT}/dex/classes9.dex" "${WORK_DIR}/base_dec/classes4.dex"
python3 "${MOD_ROOT}/tools/patch_classes2_azath.py" "${WORK_DIR}/base_dec/classes2.dex"
bash "${MOD_ROOT}/tools/patch_classes3_azath.sh" "${WORK_DIR}/base_dec/classes3.dex"
mkdir -p "${WORK_DIR}/base_dec/lib/arm64-v8a"
cp "${MOD_ROOT}/lib/arm64-v8a/libkfxconv.so" "${WORK_DIR}/base_dec/lib/arm64-v8a/libkfxconv.so"
mkdir -p "${WORK_DIR}/base_dec/assets/azath_icons"
cp -a "${MOD_ROOT}/assets/azath_icons/"*.png "${WORK_DIR}/base_dec/assets/azath_icons/"
mkdir -p "${WORK_DIR}/base_dec/assets/azath_fonts"
cp -a "${MOD_ROOT}/assets/azath_fonts/"* "${WORK_DIR}/base_dec/assets/azath_fonts/"

echo "[1b/5] Patching strings.xml & bottom nav Ember Crimson resources..."
find "${WORK_DIR}/base_dec/res" -name "strings.xml" -exec sed -i \
    -e 's/in your Kindle library\./in your Azath library\./g' \
    -e 's/in your Folio library\./in your Azath library\./g' \
    -e 's/your Kindle library/your Azath library/g' \
    -e 's/your Folio library/your Azath library/g' \
    -e 's/>Search Folio</\>Search Azath</g' \
    -e 's/>Search Kindle</\>Search Azath</g' {} +
sed -i 's/#ff2491ff/#ffe05a47/gI' \
    "${WORK_DIR}/base_dec/res/values-night/colors.xml" \
    "${WORK_DIR}/base_dec/res/values/colors.xml" \
    "${WORK_DIR}/base_dec/res/values/styles.xml"
sed -i 's/android:color="@color\/accent_3_dark"/android:color="#ffe05a47"/g' \
    "${WORK_DIR}/base_dec/res/drawable/bottom_bar_label_color_dark.xml" || true

echo "[1c/5] Updating launcher icons from dropin_res..."
python3 -c '
import os, shutil
from PIL import Image

res_dir = "/tmp/kindle_clone_build/base_dec/res"
dropin_res = "/home/bgg/projects/clunky/dropin_res"

crimson = (26, 8, 8)
bg_512 = Image.new("RGBA", (512, 512), crimson + (255,))
bg_512.save(os.path.join(res_dir, "drawable", "folio_icon_bg.png"))

bg_432 = Image.new("RGB", (432, 432), crimson)
bg_432.save(os.path.join(res_dir, "drawable-xxxhdpi", "launcher_bg_adaptive.jpg"), "JPEG")

shutil.copyfile(os.path.join(dropin_res, "ic_launcher_foreground.png"),
                os.path.join(res_dir, "drawable", "folio_icon_fg.png"))
shutil.copyfile(os.path.join(dropin_res, "ic_launcher_foreground.png"),
                os.path.join(res_dir, "drawable-xxxhdpi", "launcher_icon_adaptive.png"))

for dens in ["mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi"]:
    src_dens_dir = os.path.join(dropin_res, f"mipmap-{dens}")
    dst_dens_dir = os.path.join(res_dir, f"mipmap-{dens}")
    os.makedirs(dst_dens_dir, exist_ok=True)
    shutil.copyfile(os.path.join(src_dens_dir, "ic_launcher.png"),
                    os.path.join(dst_dens_dir, "folio_launcher_icon.png"))
    shutil.copyfile(os.path.join(src_dens_dir, "ic_launcher_round.png"),
                    os.path.join(dst_dens_dir, "folio_launcher_icon_round.png"))
    im = Image.open(os.path.join(src_dens_dir, "ic_launcher.png"))
    im.save(os.path.join(dst_dens_dir, "ic_launcher.webp"), "WEBP")
    im_r = Image.open(os.path.join(src_dens_dir, "ic_launcher_round.png"))
    im_r.save(os.path.join(dst_dens_dir, "ic_launcher_round.webp"), "WEBP")
'

echo "[2/5] Building APK with apktool..."
apktool b "${WORK_DIR}/base_dec" -o "${WORK_DIR}/base_cloned_raw.apk"

echo "[3/5] Aligning and signing..."
KEYSTORE="${MOD_ROOT}/../handoff_assets/debug.keystore"
ALIAS="androiddebugkey"
zipalign -f -p 4 "${WORK_DIR}/base_cloned_raw.apk" "${WORK_DIR}/signed_splits/base.apk"
apksigner sign --ks "${KEYSTORE}" --ks-pass pass:android --ks-key-alias "${ALIAS}" --key-pass pass:android "${WORK_DIR}/signed_splits/base.apk"

echo "[4/5] Streamed install to device..."
if adb get-state >/dev/null 2>&1; then
    if [[ $(ls -1 "${WORK_DIR}/signed_splits/"*.apk | wc -l) -gt 1 ]]; then
        adb install-multiple -r -d "${WORK_DIR}/signed_splits/"*.apk
    else
        adb install -r -d "${WORK_DIR}/signed_splits/base.apk"
    fi
    adb shell appops set "${NEW_PKG}" MANAGE_EXTERNAL_STORAGE allow >/dev/null 2>&1 || true
    adb shell pm grant "${NEW_PKG}" android.permission.READ_EXTERNAL_STORAGE >/dev/null 2>&1 || true
else
    echo "Device not currently attached via adb; skipping install for now."
fi

echo "[5/5] Staging Azath v14.7 KFX release..."
mkdir -p "${MOD_ROOT}/release/azath_v14.7_kfx"
cp -a "${WORK_DIR}/signed_splits/base.apk" "${MOD_ROOT}/release/azath_v14.7_kfx/base.apk"
if [[ -f "${WORK_DIR}/signed_splits/base.apk.idsig" ]]; then
    cp -a "${WORK_DIR}/signed_splits/base.apk.idsig" "${MOD_ROOT}/release/azath_v14.7_kfx/base.apk.idsig"
fi

echo "Fast rebuild, deploy, and Azath v14.7 KFX staging successful!"
