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

if target not in content:
    print("ERROR: Target smali snippet not found in RubyHomeFragment.smali", file=sys.stderr)
    sys.exit(1)

new_content = content.replace(target, replacement, 1)
with open(smali_path, "w", encoding="utf-8") as f:
    f.write(new_content)
print("Successfully patched RubyHomeFragment.smali with OpenCatalogView.wrap(p1)")

reader_smali = "${TMP_DIR}/com/amazon/kcp/reader/StandAloneBookReaderActivity.smali"
with open(reader_smali, "r", encoding="utf-8") as f:
    rcontent = f.read()

rtarget = """    invoke-super {p0}, Lcom/amazon/kcp/reader/BookReaderActivity;->onResume()V"""
rreplace = """    invoke-super {p0}, Lcom/amazon/kcp/reader/BookReaderActivity;->onResume()V

    invoke-static {p0}, Lcom/amazon/kindle/epub/CoverOverlayView;->showForCurrentBook(Landroid/app/Activity;)Z"""

if rtarget not in rcontent:
    print("ERROR: Target onResume smali snippet not found in StandAloneBookReaderActivity.smali", file=sys.stderr)
    sys.exit(1)

rnew_content = rcontent.replace(rtarget, rreplace, 1)
with open(reader_smali, "w", encoding="utf-8") as f:
    f.write(rnew_content)
print("Successfully patched StandAloneBookReaderActivity.smali with CoverOverlayView.showForCurrentBook(p0)")
PYEOF

echo "Reassembling patched classes3.dex..."
java -cp "${APKTOOL_JAR}" com.android.tools.smali.smali.Main a "${TMP_DIR}" -o "${INPUT_DEX}"
echo "Patched classes3.dex written to ${INPUT_DEX} ($(stat -c%s "${INPUT_DEX}") bytes)"
