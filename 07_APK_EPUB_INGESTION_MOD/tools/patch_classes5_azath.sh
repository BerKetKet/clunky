#!/usr/bin/env bash
set -euo pipefail

INPUT_DEX="${1:-}"
if [[ -z "${INPUT_DEX}" || ! -f "${INPUT_DEX}" ]]; then
    echo "Usage: $0 <path_to_classes5.dex>"
    exit 1
fi

APKTOOL_JAR="/usr/share/android-apktool/apktool.jar"
if [[ ! -f "${APKTOOL_JAR}" ]]; then
    echo "ERROR: ${APKTOOL_JAR} not found."
    exit 1
fi

TMP_DIR="$(mktemp -d /tmp/c5_azath_patch_XXXXXX)"
trap 'rm -rf "${TMP_DIR}"' EXIT

java -cp "${APKTOOL_JAR}" com.android.tools.smali.baksmali.Main d "${INPUT_DEX}" -o "${TMP_DIR}"

python3 - "${TMP_DIR}" << 'PYEOF'
import os
import sys

tmp_dir = sys.argv[1]
changed = False

# 1. l4.smali: LibraryTopBar.r
l4_path = os.path.join(tmp_dir, "com/amazon/kindle/library/ui/topbar/l4.smali")
with open(l4_path, "r", encoding="utf-8") as f:
    src = f.read()

old_l4 = """    move-object/from16 v24, v6

    move-object/from16 v6, v27

    invoke-virtual/range {v8 .. v23}, Landroidx/compose/material3/pf;->h(JJJJJJLandroidx/compose/runtime/p;II)Landroidx/compose/material3/nf;"""
new_l4 = """    move-object/from16 v24, v6

    move-object/from16 v6, v27

    invoke-static {}, Lcom/amazon/kindle/epub/catalog/FolioTheme;->composeSurfaceLong()J

    move-result-wide v9

    move-wide v11, v9

    invoke-static {}, Lcom/amazon/kindle/epub/catalog/FolioTheme;->composeOnSurfaceLong()J

    move-result-wide v13

    move-wide v15, v13

    move-wide/from16 v17, v13

    const/16 v23, 0x20

    invoke-virtual/range {v8 .. v23}, Landroidx/compose/material3/pf;->h(JJJJJJLandroidx/compose/runtime/p;II)Landroidx/compose/material3/nf;"""
if old_l4 in src:
    src = src.replace(old_l4, new_l4, 1)
    with open(l4_path, "w", encoding="utf-8") as f:
        f.write(src)
    print("Patched l4.smali LibraryTopBar TopAppBar colors -> FolioTheme live tokens.")
    changed = True
elif "FolioTheme;->composeOnSurfaceLong" in src:
    print("l4.smali already patched.")
else:
    raise SystemExit("ERROR: Target pf.h snippet not found in l4.smali")

# 2. m1.smali
m1_path = os.path.join(tmp_dir, "com/amazon/kindle/library/ui/topbar/m1.smali")
if os.path.isfile(m1_path):
    with open(m1_path, "r", encoding="utf-8") as f:
        m1_src = f.read()
    old_m1 = """    move/from16 v23, v24

    invoke-virtual/range {v7 .. v22}, Landroidx/compose/material3/pf;->h(JJJJJJLandroidx/compose/runtime/p;II)Landroidx/compose/material3/nf;"""
    new_m1 = """    move/from16 v23, v24

    invoke-static {}, Lcom/amazon/kindle/epub/catalog/FolioTheme;->composeSurfaceLong()J

    move-result-wide v8

    move-wide v10, v8

    invoke-static {}, Lcom/amazon/kindle/epub/catalog/FolioTheme;->composeOnSurfaceLong()J

    move-result-wide v12

    move-wide v14, v12

    move-wide/from16 v16, v12

    const/16 v22, 0x20

    invoke-virtual/range {v7 .. v22}, Landroidx/compose/material3/pf;->h(JJJJJJLandroidx/compose/runtime/p;II)Landroidx/compose/material3/nf;"""
    if old_m1 in m1_src:
        m1_src = m1_src.replace(old_m1, new_m1, 1)
        with open(m1_path, "w", encoding="utf-8") as f:
            f.write(m1_src)
        print("Patched m1.smali TopAppBar colors -> FolioTheme live tokens.")
        changed = True
    elif "FolioTheme;->composeOnSurfaceLong" in m1_src:
        print("m1.smali already patched.")

# 3. q2.smali
q2_path = os.path.join(tmp_dir, "com/amazon/kindle/library/ui/topbar/q2.smali")
if os.path.isfile(q2_path):
    with open(q2_path, "r", encoding="utf-8") as f:
        q2_src = f.read()
    old_q2 = """    const/4 v0, 0x1

    invoke-virtual/range {v8 .. v23}, Landroidx/compose/material3/pf;->h(JJJJJJLandroidx/compose/runtime/p;II)Landroidx/compose/material3/nf;"""
    new_q2 = """    const/4 v0, 0x1

    invoke-static {}, Lcom/amazon/kindle/epub/catalog/FolioTheme;->composeSurfaceLong()J

    move-result-wide v9

    move-wide v11, v9

    invoke-static {}, Lcom/amazon/kindle/epub/catalog/FolioTheme;->composeOnSurfaceLong()J

    move-result-wide v13

    move-wide v15, v13

    move-wide/from16 v17, v13

    const/16 v23, 0x20

    invoke-virtual/range {v8 .. v23}, Landroidx/compose/material3/pf;->h(JJJJJJLandroidx/compose/runtime/p;II)Landroidx/compose/material3/nf;"""
    if old_q2 in q2_src:
        q2_src = q2_src.replace(old_q2, new_q2, 1)
        with open(q2_path, "w", encoding="utf-8") as f:
            f.write(q2_src)
        print("Patched q2.smali TopAppBar colors -> FolioTheme live tokens.")
        changed = True
    elif "FolioTheme;->composeOnSurfaceLong" in q2_src:
        print("q2.smali already patched.")

if not changed:
    print("All target classes5 TopAppBars already patched.")
PYEOF

java -cp "${APKTOOL_JAR}" com.android.tools.smali.smali.Main a "${TMP_DIR}" -o "${INPUT_DEX}"
echo "Reassembled patched ${INPUT_DEX}."
