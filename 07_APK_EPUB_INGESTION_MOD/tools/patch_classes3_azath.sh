#!/usr/bin/env bash
set -euo pipefail

INPUT_DEX="${1:-}"
if [[ -z "${INPUT_DEX}" || ! -f "${INPUT_DEX}" ]]; then
    echo "Usage: $0 <path_to_classes3.dex>"
    exit 1
fi

APKTOOL_JAR="/usr/share/android-apktool/apktool.jar"
if [[ ! -f "${APKTOOL_JAR}" ]]; then
    echo "ERROR: ${APKTOOL_JAR} not found."
    exit 1
fi

TMP_DIR="$(mktemp -d /tmp/c3_azath_patch_XXXXXX)"
trap 'rm -rf "${TMP_DIR}"' EXIT

java -cp "${APKTOOL_JAR}" com.android.tools.smali.baksmali.Main d "${INPUT_DEX}" -o "${TMP_DIR}"

HOME_SMALI="${TMP_DIR}/com/amazon/kcp/home/a.smali"
if [[ ! -f "${HOME_SMALI}" ]]; then
    echo "ERROR: ${HOME_SMALI} not found."
    exit 1
fi

python3 - "${HOME_SMALI}" << 'PYEOF'
import sys

path = sys.argv[1]
with open(path, "r", encoding="utf-8") as f:
    src = f.read()

old_j = """.method public j()Lcom/amazon/android/navigation/screenlet/HeaderMode;
    .registers 1

    sget-object p0, Lcom/amazon/android/navigation/screenlet/HeaderMode;->d:Lcom/amazon/android/navigation/screenlet/HeaderMode;

    return-object p0
.end method"""

new_j = """.method public j()Lcom/amazon/android/navigation/screenlet/HeaderMode;
    .registers 1

    sget-object p0, Lcom/amazon/android/navigation/screenlet/HeaderMode;->c:Lcom/amazon/android/navigation/screenlet/HeaderMode;

    return-object p0
.end method"""

if new_j in src:
    print("HomeScreenlet (com/amazon/kcp/home/a.smali) HeaderMode already patched to c (NoHeader).")
    sys.exit(0)

assert old_j in src, "Missing HomeScreenlet.j() HeaderMode target in a.smali"
src = src.replace(old_j, new_j, 1)
with open(path, "w", encoding="utf-8") as f:
    f.write(src)
print("Patched HomeScreenlet.j() HeaderMode: d -> c (collapses Compose Home TopAppBar).")
PYEOF

java -cp "${APKTOOL_JAR}" com.android.tools.smali.smali.Main a "${TMP_DIR}" -o "${INPUT_DEX}"
echo "Reassembled patched ${INPUT_DEX}."
