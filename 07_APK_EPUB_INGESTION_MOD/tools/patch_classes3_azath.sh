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

python3 - "${TMP_DIR}" << 'PYEOF'
import os
import sys

tmp_dir = sys.argv[1]
targets = [
    ("HomeScreenlet", "com/amazon/kcp/home/a.smali"),
    ("MoreScreenlet", "com/amazon/kcp/more/w.smali"),
]

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

changed = False
for label, rel in targets:
    path = os.path.join(tmp_dir, rel)
    if not os.path.isfile(path):
        raise SystemExit(f"ERROR: {path} not found.")
    with open(path, "r", encoding="utf-8") as f:
        src = f.read()
    if new_j in src:
        print(f"{label} ({rel}) HeaderMode already patched to c (NoHeader).")
        continue
    assert old_j in src, f"Missing {label}.j() HeaderMode target in {rel}"
    src = src.replace(old_j, new_j, 1)
    with open(path, "w", encoding="utf-8") as f:
        f.write(src)
    print(f"Patched {label}.j() ({rel}) HeaderMode: d -> c (collapses Compose TopAppBar).")
    changed = True

if not changed:
    print("All target Screenlets already patched.")
PYEOF

java -cp "${APKTOOL_JAR}" com.android.tools.smali.smali.Main a "${TMP_DIR}" -o "${INPUT_DEX}"
echo "Reassembled patched ${INPUT_DEX}."
