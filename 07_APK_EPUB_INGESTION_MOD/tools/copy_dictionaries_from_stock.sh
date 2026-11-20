#!/usr/bin/env bash
# Copies offline Kindle dictionary files & DB metadata from stock com.amazon.kindle (READ-ONLY)
# into cloned com.amazon.kindle.epub so long-press word lookup works offline.
set -euo pipefail

TARGET_PKG="${1:-com.amazon.kindle.epub}"
STOCK_DB="/data/data/com.amazon.kindle/databases/kindle_library.db"
TARGET_DB="/data/data/${TARGET_PKG}/databases/kindle_library.db"
TARGET_DICT_DIR="/data/data/${TARGET_PKG}/files/dictionaries"

echo "[1/4] Resolving UID of ${TARGET_PKG}..."
TARGET_UID="$(adb shell su -c "stat -c %u /data/data/${TARGET_PKG}" | tr -d '\r\n')"
echo "      ${TARGET_PKG} UID = ${TARGET_UID}"

echo "[2/4] Inspecting stock com.amazon.kindle dictionaries (READ-ONLY)..."
echo "SELECT d.ID, d.SHORT_TITLE, l.FILE_PATH FROM DictionaryInfo d JOIN LocalContent l ON d.ID = l.KEY;" | adb shell su -c "sqlite3 '${STOCK_DB}'" | tee /tmp/stock_dicts.txt

if [[ ! -s /tmp/stock_dicts.txt ]]; then
    echo "No local dictionaries found in stock com.amazon.kindle. Searching /sdcard/Android/data/com.amazon.kindle/files/..."
    adb shell su -c "find /sdcard/Android/data/com.amazon.kindle/files /data/data/com.amazon.kindle/files -iname '*dict*' -o -iname '*B00*' 2>/dev/null | head -n 40"
    exit 0
fi

echo "[3/4] Copying dictionary files & metadata into ${TARGET_PKG}..."
adb shell "su -c 'mkdir -p \"${TARGET_DICT_DIR}\" && chown ${TARGET_UID}:${TARGET_UID} \"${TARGET_DICT_DIR}\" && chmod 755 \"${TARGET_DICT_DIR}\"'"

# Pull stock DB read-only to host /tmp for safe SQL generation (never touches stock DB)
adb shell "su -c 'cp \"${STOCK_DB}\" /data/local/tmp/stock_lib_ro.db && chmod 644 /data/local/tmp/stock_lib_ro.db'"
adb pull /data/local/tmp/stock_lib_ro.db /tmp/stock_lib_ro.db >/dev/null
adb shell "su -c 'rm -f /data/local/tmp/stock_lib_ro.db'"

python3 - /tmp/stock_lib_ro.db "${TARGET_DICT_DIR}" "${TARGET_UID}" <<'PYEOF'
import sqlite3, sys, os, subprocess

stock_db, target_dict_dir, target_uid = sys.argv[1], sys.argv[2], sys.argv[3]
conn = sqlite3.connect(stock_db)
conn.row_factory = sqlite3.Row
cur = conn.cursor()

dicts = cur.execute("SELECT d.ID, l.FILE_PATH FROM DictionaryInfo d JOIN LocalContent l ON d.ID = l.KEY").fetchall()
sql_lines = ["BEGIN TRANSACTION;"]

for row in dicts:
    did = row["ID"]
    src_path = row["FILE_PATH"]
    bn = os.path.basename(src_path)
    dst_path = f"{target_dict_dir}/{bn}"
    print(f"  Copying dictionary {did}: {src_path} -> {dst_path}")
    subprocess.run(["adb", "shell", f"su -c 'cp \"{src_path}\" \"{dst_path}\" && chown {target_uid}:{target_uid} \"{dst_path}\" && chmod 644 \"{dst_path}\"'"], check=True)

    # Copy KindleContent row
    kc = cur.execute("SELECT * FROM KindleContent WHERE ID = ?", (did,)).fetchone()
    if kc:
        cols = list(kc.keys())
        vals = [kc[c] for c in cols]
        def sql_lit(v):
            if v is None: return "NULL"
            if isinstance(v, (int, float)): return str(v)
            if isinstance(v, bytes): return "X'" + v.hex() + "'"
            return "'" + str(v).replace("'", "''") + "'"
        sql_lines.append(f"INSERT OR REPLACE INTO KindleContent ({','.join(cols)}) VALUES ({','.join(sql_lit(v) for v in vals)});")

    # Copy LocalContent row with updated FILE_PATH
    lc = cur.execute("SELECT * FROM LocalContent WHERE KEY = ?", (did,)).fetchone()
    if lc:
        cols = list(lc.keys())
        vals = [dst_path if c == "FILE_PATH" else lc[c] for c in cols]
        sql_lines.append(f"INSERT OR REPLACE INTO LocalContent ({','.join(cols)}) VALUES ({','.join(sql_lit(v) for v in vals)});")

    # Copy DictionaryInfo row
    di = cur.execute("SELECT * FROM DictionaryInfo WHERE ID = ?", (did,)).fetchone()
    if di:
        cols = list(di.keys())
        vals = [di[c] for c in cols]
        sql_lines.append(f"INSERT OR REPLACE INTO DictionaryInfo ({','.join(cols)}) VALUES ({','.join(sql_lit(v) for v in vals)});")

    # Copy DictionaryLocale rows
    for dl in cur.execute("SELECT * FROM DictionaryLocale WHERE ID = ?", (did,)).fetchall():
        cols = list(dl.keys())
        vals = [dl[c] for c in cols]
        sql_lines.append(f"INSERT OR REPLACE INTO DictionaryLocale ({','.join(cols)}) VALUES ({','.join(sql_lit(v) for v in vals)});")

    # Ensure UserContent row for MASTER
    sql_lines.append(f"INSERT OR REPLACE INTO UserContent (KEY, USER_ID, LAST_ACCESSED, LPR, MRPR, FPR, MLT_LASTUPDATED, READING_PROGRESS, IS_IN_CAROUSEL, CLIPPING_BALANCE, HAS_UNMIGRATED_ANNOTATION) VALUES ('{did}', 'MASTER', 0, -1, -1, -1, -1, 0, 0, 0, 0);")

sql_lines.append("COMMIT;")
with open("/tmp/insert_dicts.sql", "w") as f:
    f.write("\n".join(sql_lines) + "\n")
print(f"Generated /tmp/insert_dicts.sql for {len(dicts)} dictionary(s)")
PYEOF

echo "[4/4] Applying dictionary metadata to ${TARGET_PKG}..."
adb push /tmp/insert_dicts.sql /data/local/tmp/insert_dicts.sql >/dev/null
adb shell su -c "am force-stop '${TARGET_PKG}'"
adb shell su -c "sqlite3 '${TARGET_DB}' < /data/local/tmp/insert_dicts.sql"
adb shell su -c "rm -f /data/local/tmp/insert_dicts.sql"
adb shell "su -c 'chown ${TARGET_UID}:${TARGET_UID} \"${TARGET_DB}\"*'"
echo "Dictionaries installed in ${TARGET_PKG}:"
echo "SELECT ID, SHORT_TITLE FROM DictionaryInfo;" | adb shell su -c "sqlite3 '${TARGET_DB}'"
