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

nav_vm_smali = "${TMP_DIR}/com/amazon/kcp/oob/navigation/state/NavigationControllerViewModel.smali"
with open(nav_vm_smali, "r", encoding="utf-8") as f:
    nv_content = f.read()
nv_target = """    invoke-interface {v1}, Lcom/amazon/android/navigation/tab/LandingScreenTab;->getId()Ljava/lang/String;

    move-result-object v1

    new-instance v2, Ljava/lang/StringBuilder;"""
nv_replace = """    invoke-interface {v1}, Lcom/amazon/android/navigation/tab/LandingScreenTab;->getId()Ljava/lang/String;

    move-result-object v1

    invoke-static {v1}, Lcom/amazon/kindle/epub/catalog/FolioTheme;->onTabIdSelected(Ljava/lang/String;)V

    new-instance v2, Ljava/lang/StringBuilder;"""
if nv_target in nv_content:
    nv_content = nv_content.replace(nv_target, nv_replace, 1)
    with open(nav_vm_smali, "w", encoding="utf-8") as f:
        f.write(nv_content)
    print("Successfully patched NavigationControllerViewModel.smali with FolioTheme.onTabIdSelected(v1)")
elif "FolioTheme;->onTabIdSelected" in nv_content:
    print("NavigationControllerViewModel.smali already patched with FolioTheme.onTabIdSelected")
else:
    print("ERROR: Target E0 snippet not found in NavigationControllerViewModel.smali", file=sys.stderr)
    sys.exit(1)

lib_screenlet_smali = "${TMP_DIR}/com/amazon/kcp/library/LibraryScreenlet.smali"
with open(lib_screenlet_smali, "r", encoding="utf-8") as f:
    ls_content = f.read()
ls_target = """    invoke-virtual {p1, p0, p2, v0}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;

    move-result-object p0

    const-string p1, "inflate(...)"

    invoke-static {p0, p1}, Lkotlin/jvm/internal/o;->h(Ljava/lang/Object;Ljava/lang/String;)V

    return-object p0"""
ls_replace = """    invoke-virtual {p1, p0, p2, v0}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;

    move-result-object p0

    const-string p1, "inflate(...)"

    invoke-static {p0, p1}, Lkotlin/jvm/internal/o;->h(Ljava/lang/Object;Ljava/lang/String;)V

    invoke-static {p0}, Lcom/amazon/kindle/epub/catalog/FolioTheme;->wrapLibraryScreenlet(Landroid/view/View;)Landroid/view/View;

    move-result-object p0

    return-object p0"""
if ls_target in ls_content:
    ls_content = ls_content.replace(ls_target, ls_replace, 1)
    with open(lib_screenlet_smali, "w", encoding="utf-8") as f:
        f.write(ls_content)
    print("Successfully patched LibraryScreenlet.smali with FolioTheme.wrapLibraryScreenlet(p0)")
elif "FolioTheme;->wrapLibraryScreenlet" in ls_content:
    print("LibraryScreenlet.smali already patched with FolioTheme.wrapLibraryScreenlet")
else:
    print("ERROR: Target f() snippet not found in LibraryScreenlet.smali", file=sys.stderr)
    sys.exit(1)

search_bar_smali = "${TMP_DIR}/com/amazon/kcp/oob/z.smali"
with open(search_bar_smali, "r", encoding="utf-8") as f:
    sb_content = f.read()
sb_target = """    invoke-virtual {p1, p0}, Landroid/view/View;->setFocusable(Z)V

    invoke-static {p1}, Lkotlin/jvm/internal/o;->f(Ljava/lang/Object;)V

    return-object p1"""
sb_replace = """    invoke-virtual {p1, p0}, Landroid/view/View;->setFocusable(Z)V

    invoke-static {p1}, Lkotlin/jvm/internal/o;->f(Ljava/lang/Object;)V

    invoke-static {p1}, Lcom/amazon/kindle/epub/catalog/FolioTheme;->wrapLegacySearchBar(Landroid/view/View;)Landroid/view/View;

    move-result-object p1

    return-object p1"""
if sb_target in sb_content:
    sb_content = sb_content.replace(sb_target, sb_replace, 1)
    with open(search_bar_smali, "w", encoding="utf-8") as f:
        f.write(sb_content)
    print("Successfully patched oob/z.smali with FolioTheme.wrapLegacySearchBar(p1)")
elif "FolioTheme;->wrapLegacySearchBar" in sb_content:
    print("oob/z.smali already patched with FolioTheme.wrapLegacySearchBar")
else:
    print("ERROR: Target snippet not found in oob/z.smali", file=sys.stderr)
    sys.exit(1)
PYEOF

echo "Reassembling patched classes3.dex..."
java -cp "${APKTOOL_JAR}" com.android.tools.smali.smali.Main a "${TMP_DIR}" -o "${INPUT_DEX}"
echo "Patched classes3.dex written to ${INPUT_DEX} ($(stat -c%s "${INPUT_DEX}") bytes)"
