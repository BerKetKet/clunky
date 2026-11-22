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

TMP_DIR="$(mktemp -d /tmp/c5_smali_patch_XXXXXX)"
trap 'rm -rf "${TMP_DIR}"' EXIT

echo "Disassembling ${INPUT_DEX}..."
java -cp "${APKTOOL_JAR}" com.android.tools.smali.baksmali.Main d "${INPUT_DEX}" -o "${TMP_DIR}"

python3 - << PYEOF
import sys, re

# 1. SearchResultsKt.smali: suppress store header section (b4 -> false)
sr_path = "${TMP_DIR}/com/amazon/kcp/search/v2/views/components/page/SearchResultsKt.smali"
with open(sr_path, "r", encoding="utf-8") as f:
    sr_content = f.read()

b4_target = """.method public static final b4(ZZZZZ)Z
    .registers 5

    if-nez p0, :cond_d

    if-nez p1, :cond_d

    if-nez p2, :cond_d

    if-eqz p3, :cond_b

    if-nez p4, :cond_b

    goto :goto_d

    :cond_b
    const/4 p0, 0x0

    return p0

    :cond_d
    :goto_d
    const/4 p0, 0x1

    return p0
.end method"""

b4_replacement = """.method public static final b4(ZZZZZ)Z
    .registers 5

    const/4 p0, 0x0

    return p0
.end method"""

if b4_target in sr_content:
    sr_content = sr_content.replace(b4_target, b4_replacement, 1)
    with open(sr_path, "w", encoding="utf-8") as f:
        f.write(sr_content)
    print("1. Patched SearchResultsKt.smali (b4 -> false)")
else:
    print("Warning: b4 target not found or already patched")

# 2. b.smali: return emptyList for store results
b_path = "${TMP_DIR}/com/amazon/kcp/search/v2/models/b.smali"
with open(b_path, "r", encoding="utf-8") as f:
    b_content = f.read()

e_target = """.method public final e()Ljava/util/List;
    .registers 1

    iget-object p0, p0, Lcom/amazon/kcp/search/v2/models/b;->storeResults:Ljava/util/List;

    return-object p0
.end method"""

e_replacement = """.method public final e()Ljava/util/List;
    .registers 1

    sget-object p0, Ljava/util/Collections;->EMPTY_LIST:Ljava/util/List;

    return-object p0
.end method"""

if e_target in b_content:
    b_content = b_content.replace(e_target, e_replacement, 1)
    with open(b_path, "w", encoding="utf-8") as f:
        f.write(b_content)
    print("2. Patched b.smali (e() -> emptyList())")
else:
    print("Warning: e() target not found or already patched")

# 3. SearchProvider.smali: N() -> EMPTY_LIST, queryStore -> false
sp_path = "${TMP_DIR}/com/amazon/kcp/search/SearchProvider.smali"
with open(sp_path, "r", encoding="utf-8") as f:
    sp_content = f.read()

n_target = """.method public N()Ljava/util/List;
    .registers 1

    iget-object p0, p0, Lcom/amazon/kcp/search/SearchProvider;->storeResults:Ljava/util/List;

    if-nez p0, :cond_6

    sget-object p0, Ljava/util/Collections;->EMPTY_LIST:Ljava/util/List;

    :cond_6
    return-object p0
.end method"""

n_replacement = """.method public N()Ljava/util/List;
    .registers 1

    sget-object p0, Ljava/util/Collections;->EMPTY_LIST:Ljava/util/List;

    return-object p0
.end method"""

if n_target in sp_content:
    sp_content = sp_content.replace(n_target, n_replacement, 1)
    print("3a. Patched SearchProvider.smali N() -> EMPTY_LIST")

qs_target = """    iget-boolean v5, p0, Lcom/amazon/kcp/search/SearchProvider;->queryStore:Z"""
qs_replacement = """    const/4 v5, 0x0"""
if qs_target in sp_content:
    sp_content = sp_content.replace(qs_target, qs_replacement, 1)
    print("3b. Patched SearchProvider.smali queryStore read -> false")

with open(sp_path, "w", encoding="utf-8") as f:
    f.write(sp_content)

# 4. SearchViewModel.smali: r() and z() silencer
svm_path = "${TMP_DIR}/com/amazon/kcp/search/v2/models/SearchViewModel.smali"
with open(svm_path, "r", encoding="utf-8") as f:
    svm_content = f.read()

r_target = """.method private final r()V
    .registers 7

    invoke-static {p0}, Landroidx/lifecycle/q0;->a(Landroidx/lifecycle/p0;)Lkotlinx/coroutines/CoroutineScope;

    move-result-object v0

    new-instance v3, Lcom/amazon/kcp/search/v2/models/SearchViewModel$getStoreResults$1;

    const/4 v1, 0x0

    invoke-direct {v3, p0, v1}, Lcom/amazon/kcp/search/v2/models/SearchViewModel$getStoreResults$1;-><init>(Lcom/amazon/kcp/search/v2/models/SearchViewModel;Lkotlin/coroutines/Continuation;)V

    const/4 v4, 0x3

    const/4 v5, 0x0

    const/4 v2, 0x0

    invoke-static/range {v0 .. v5}, Lkotlinx/coroutines/BuildersKt;->launch$default(Lkotlinx/coroutines/CoroutineScope;Lkotlin/coroutines/d;Lkotlinx/coroutines/CoroutineStart;Ls01/p;ILjava/lang/Object;)Lkotlinx/coroutines/Job;

    return-void
.end method"""

r_replacement = """.method private final r()V
    .registers 1

    return-void
.end method"""

if r_target in svm_content:
    svm_content = svm_content.replace(r_target, r_replacement, 1)
    print("4a. Patched SearchViewModel.smali r() -> nop")

z_regex = re.compile(r'\.method public final z\(Lcom/amazon/kcp/search/store/SearchNodeCompleteEvent;\)V.*?\n\.end method', re.DOTALL)
m = z_regex.search(svm_content)
if m is not None:
    z_replacement = """.method public final z(Lcom/amazon/kcp/search/store/SearchNodeCompleteEvent;)V
    .registers 2

    return-void
.end method"""
    svm_content = z_regex.sub(z_replacement, svm_content, count=1)
    print("4b. Patched SearchViewModel.smali z() -> nop")

with open(svm_path, "w", encoding="utf-8") as f:
    f.write(svm_content)

# 5. SearchActivity.smali: setShouldShowStoreResults forced false
sa_path = "${TMP_DIR}/com/amazon/kcp/search/SearchActivity.smali"
with open(sa_path, "r", encoding="utf-8") as f:
    sa_content = f.read()

store_target = """.method protected setShouldShowStoreResults(Z)V
    .registers 2

    iput-boolean p1, p0, Lcom/amazon/kcp/search/SearchActivity;->shouldShowStoreResults:Z

    return-void
.end method"""

store_replacement = """.method protected setShouldShowStoreResults(Z)V
    .registers 2

    const/4 p1, 0x0

    iput-boolean p1, p0, Lcom/amazon/kcp/search/SearchActivity;->shouldShowStoreResults:Z

    return-void
.end method"""

if store_target in sa_content:
    sa_content = sa_content.replace(store_target, store_replacement, 1)
    with open(sa_path, "w", encoding="utf-8") as f:
        f.write(sa_content)
    print("5. Patched SearchActivity.smali (setShouldShowStoreResults forced false)")
PYEOF

echo "Reassembling patched classes5.dex..."
java -cp "${APKTOOL_JAR}" com.android.tools.smali.smali.Main a "${TMP_DIR}" -o "${INPUT_DEX}"
echo "Patched classes5.dex written to ${INPUT_DEX} ($(stat -c%s "${INPUT_DEX}") bytes)"
