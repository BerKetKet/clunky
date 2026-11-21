#!/usr/bin/env bash
set -euo pipefail

INPUT_DEX="${1:-}"
if [[ -z "${INPUT_DEX}" || ! -f "${INPUT_DEX}" ]]; then
    echo "Usage: $0 <path_to_classes2.dex>"
    exit 1
fi

APKTOOL_JAR="/usr/share/android-apktool/apktool.jar"
if [[ ! -f "${APKTOOL_JAR}" ]]; then
    echo "ERROR: ${APKTOOL_JAR} not found."
    exit 1
fi

TMP_DIR="$(mktemp -d /tmp/c2_smali_patch_XXXXXX)"
trap 'rm -rf "${TMP_DIR}"' EXIT

echo "Disassembling ${INPUT_DEX}..."
java -cp "${APKTOOL_JAR}" com.android.tools.smali.baksmali.Main d "${INPUT_DEX}" -o "${TMP_DIR}"

MOBI_SMALI="${TMP_DIR}/com/amazon/android/docviewer/mobi/MobiDocViewer.smali"
if [[ ! -f "${MOBI_SMALI}" ]]; then
    echo "ERROR: ${MOBI_SMALI} not found after disassembly."
    exit 1
fi

python3 - << PYEOF
import sys
import os

smali_path = "${MOBI_SMALI}"
with open(smali_path, "r", encoding="utf-8") as f:
    content = f.read()

target = """.method public navigateToCover()V
    .registers 1

    iget-object p0, p0, Lcom/amazon/android/docviewer/mobi/MobiDocViewer;->m_currentModeNavigator:Lcom/amazon/android/docviewer/mobi/q0;

    if-eqz p0, :cond_7

    invoke-virtual {p0}, Lcom/amazon/android/docviewer/mobi/d;->o0()V

    :cond_7
    return-void
.end method"""

replacement = """.method public navigateToCover()V
    .registers 3

    iget-object v0, p0, Lcom/amazon/android/docviewer/mobi/MobiDocViewer;->m_currentModeNavigator:Lcom/amazon/android/docviewer/mobi/q0;

    if-eqz v0, :cond_7

    invoke-virtual {v0}, Lcom/amazon/android/docviewer/mobi/d;->o0()V

    :cond_7
    iget-object v0, p0, Lcom/amazon/android/docviewer/KindleDocViewerImpl;->context:Landroid/content/Context;

    instance-of v1, v0, Landroid/app/Activity;

    if-eqz v1, :cond_12

    check-cast v0, Landroid/app/Activity;

    invoke-static {v0}, Lcom/amazon/kindle/epub/CoverOverlayView;->showForCurrentBookForced(Landroid/app/Activity;)Z

    :cond_12
    return-void
.end method"""

if target in content:
    content = content.replace(target, replacement, 1)
    with open(smali_path, "w", encoding="utf-8") as f:
        f.write(content)
    print("Successfully patched MobiDocViewer.navigateToCover with CoverOverlayView.showForCurrentBookForced(activity)")
elif "showForCurrentBookForced" in content:
    print("MobiDocViewer.navigateToCover already hooked with showForCurrentBookForced")
else:
    print("WARNING: Target navigateToCover pattern not matched; leaving MobiDocViewer intact")

# 2. Patch BaseTableOfContentsProvider.m so tapping Cover (position 0) in TOC drawer displays fullscreen cover!
toc_smali = "${TMP_DIR}/com/amazon/android/tableofcontents/BaseTableOfContentsProvider.smali"
if os.path.exists(toc_smali):
    with open(toc_smali, "r", encoding="utf-8") as f:
        tc = f.read()
    btarget = """.method private static final m(Lcom/amazon/android/tableofcontents/BaseTableOfContentsProvider;ILandroid/view/View;)V
    .registers 8

    iget-object p2, p0, Lcom/amazon/android/tableofcontents/BaseTableOfContentsProvider;->docViewer:Lcom/amazon/android/docviewer/q0;"""

    breplacement = """.method private static final m(Lcom/amazon/android/tableofcontents/BaseTableOfContentsProvider;ILandroid/view/View;)V
    .registers 8

    if-nez p1, :cond_check_cover_skip

    iget-object v0, p0, Lcom/amazon/android/tableofcontents/BaseTableOfContentsProvider;->context:Landroid/content/Context;

    instance-of v1, v0, Landroid/app/Activity;

    if-eqz v1, :cond_check_cover_skip

    check-cast v0, Landroid/app/Activity;

    invoke-static {v0}, Lcom/amazon/kindle/epub/CoverOverlayView;->showForCurrentBookForced(Landroid/app/Activity;)Z

    :cond_check_cover_skip
    iget-object p2, p0, Lcom/amazon/android/tableofcontents/BaseTableOfContentsProvider;->docViewer:Lcom/amazon/android/docviewer/q0;"""

    if btarget in tc:
        tc = tc.replace(btarget, breplacement, 1)
        with open(toc_smali, "w", encoding="utf-8") as f:
            f.write(tc)
        print("Successfully patched BaseTableOfContentsProvider.m with CoverOverlayView.showForCurrentBookForced")
    elif "showForCurrentBookForced" in tc:
        print("BaseTableOfContentsProvider.m already hooked with showForCurrentBookForced")
    else:
        print("WARNING: Target BaseTableOfContentsProvider.m pattern not matched", file=sys.stderr)
PYEOF

echo "Reassembling patched classes2.dex..."
java -cp "${APKTOOL_JAR}" com.android.tools.smali.smali.Main a "${TMP_DIR}" -o "${INPUT_DEX}"
echo "Patched classes2.dex written to ${INPUT_DEX} ($(stat -c%s "${INPUT_DEX}") bytes)"
