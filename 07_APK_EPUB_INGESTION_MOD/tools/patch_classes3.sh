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

TMP_DIR="$(mktemp -d /tmp/c3_smali_patch_XXXXXX)"
trap 'rm -rf "${TMP_DIR}"' EXIT

echo "Disassembling ${INPUT_DEX}..."
java -cp "${APKTOOL_JAR}" com.android.tools.smali.baksmali.Main d "${INPUT_DEX}" -o "${TMP_DIR}"

SMALI_FILE="${TMP_DIR}/com/amazon/kcp/library/fragments/RubyHomeFragment.smali"
if [[ ! -f "${SMALI_FILE}" ]]; then
    echo "ERROR: ${SMALI_FILE} not found after disassembly."
    exit 1
fi

python3 - << PYEOF
import sys

smali_path = "${SMALI_FILE}"
with open(smali_path, "r", encoding="utf-8") as f:
    content = f.read()

target = """    :cond_9a
    return-object p1"""

replacement = """    :cond_9a
    invoke-static {p1}, Lcom/amazon/kindle/epub/catalog/OpenCatalogView;->wrap(Landroid/view/View;)Landroid/view/View;

    move-result-object p1

    return-object p1"""

if target in content:
    new_content = content.replace(target, replacement, 1)
    with open(smali_path, "w", encoding="utf-8") as f:
        f.write(new_content)
    print("Successfully patched RubyHomeFragment.smali with OpenCatalogView.wrap(p1)")
elif "OpenCatalogView;->wrap" in content:
    print("RubyHomeFragment.smali already patched with OpenCatalogView.wrap(p1)")
else:
    print("ERROR: Target smali snippet not found in RubyHomeFragment.smali", file=sys.stderr)
    sys.exit(1)

reader_smali = "${TMP_DIR}/com/amazon/kcp/reader/StandAloneBookReaderActivity.smali"
with open(reader_smali, "r", encoding="utf-8") as f:
    rcontent = f.read()

rtarget = """    invoke-super {p0}, Lcom/amazon/kcp/reader/BookReaderActivity;->onResume()V"""
rreplace = """    invoke-super {p0}, Lcom/amazon/kcp/reader/BookReaderActivity;->onResume()V

    invoke-static {p0}, Lcom/amazon/kindle/epub/CoverOverlayView;->showForCurrentBook(Landroid/app/Activity;)Z"""

if rtarget in rcontent:
    rnew_content = rcontent.replace(rtarget, rreplace, 1)
    with open(reader_smali, "w", encoding="utf-8") as f:
        f.write(rnew_content)
    print("Successfully patched StandAloneBookReaderActivity.smali with CoverOverlayView.showForCurrentBook(p0)")
elif "CoverOverlayView;->showForCurrentBook" in rcontent:
    print("StandAloneBookReaderActivity.smali already patched with CoverOverlayView.showForCurrentBook(p0)")
else:
    print("ERROR: Target onResume smali snippet not found in StandAloneBookReaderActivity.smali", file=sys.stderr)
    sys.exit(1)

reader_act_smali = "${TMP_DIR}/com/amazon/kcp/reader/ReaderActivity.smali"
with open(reader_act_smali, "r", encoding="utf-8") as f:
    racontent = f.read()

ratarget = """    :cond_56
    invoke-interface {v0}, Lcom/amazon/android/docviewer/q0;->navigateToCover()V

    return-void"""

rareplace = """    :cond_56
    invoke-interface {v0}, Lcom/amazon/android/docviewer/q0;->navigateToCover()V

    invoke-static {p0}, Lcom/amazon/kindle/epub/CoverOverlayView;->showForCurrentBookForced(Landroid/app/Activity;)Z

    return-void"""

if ratarget in racontent:
    racontent = racontent.replace(ratarget, rareplace, 1)
    with open(reader_act_smali, "w", encoding="utf-8") as f:
        f.write(racontent)
    print("Successfully patched ReaderActivity.smali with CoverOverlayView.showForCurrentBookForced(p0)")

import os
more_smali = "${TMP_DIR}/com/amazon/kindle/setting/layout/MoreItemsFragment.smali"
if os.path.exists(more_smali):
    with open(more_smali, "r", encoding="utf-8") as f:
        mcontent = f.read()
    mtarget = """    invoke-interface {p1, p0}, Lsw/j;->c(Lsw/e;)V

    return-object v0"""
    mreplace = """    invoke-interface {p1, p0}, Lsw/j;->c(Lsw/e;)V

    invoke-static {v0}, Lcom/amazon/kindle/epub/catalog/FolioMoreView;->wrap(Landroid/view/View;)Landroid/view/View;

    move-result-object v0

    return-object v0"""
    if mtarget in mcontent:
        mcontent = mcontent.replace(mtarget, mreplace, 1)
        with open(more_smali, "w", encoding="utf-8") as f:
            f.write(mcontent)
        print("Successfully patched MoreItemsFragment.smali with FolioMoreView.wrap(v0)")
    elif "FolioMoreView;->wrap" in mcontent:
        print("MoreItemsFragment.smali already patched with FolioMoreView.wrap(v0)")
    else:
        print("WARNING: Target snippet not found in MoreItemsFragment.smali, trying fallback pattern")
        if "return-object v0\n.end method" in mcontent:
            mcontent = mcontent.replace("return-object v0\n.end method", "invoke-static {v0}, Lcom/amazon/kindle/epub/catalog/FolioMoreView;->wrap(Landroid/view/View;)Landroid/view/View;\n\n    move-result-object v0\n\n    return-object v0\n.end method", 1)
            with open(more_smali, "w", encoding="utf-8") as f:
                f.write(mcontent)
            print("Successfully patched MoreItemsFragment.smali with FolioMoreView.wrap via fallback")
        else:
            print("ERROR: Could not patch MoreItemsFragment.smali", file=sys.stderr)
            sys.exit(1)
else:
    print("ERROR: MoreItemsFragment.smali not found!", file=sys.stderr)
    sys.exit(1)
PYEOF

echo "Reassembling patched classes3.dex..."
java -cp "${APKTOOL_JAR}" com.android.tools.smali.smali.Main a "${TMP_DIR}" -o "${INPUT_DEX}"
echo "Patched classes3.dex written to ${INPUT_DEX} ($(stat -c%s "${INPUT_DEX}") bytes)"
