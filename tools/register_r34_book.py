#!/usr/bin/env python3
import os
import subprocess

def run(cmd):
    p = subprocess.run(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    if p.returncode != 0:
        print(f"FAILED: {cmd}\nSTDOUT: {p.stdout}\nSTDERR: {p.stderr}")
    else:
        if p.stdout.strip():
            print(p.stdout.strip())
    return p.returncode

def main():
    kfx_path = "logs/r34_nested_lists_tables.kfx"
    epub_path = "logs/r34_nested_lists_tables.epub"
    kfx_size = os.path.getsize(kfx_path)
    
    print("[1/3] Pushing KFX and EPUB to device...")
    run(f"adb push {kfx_path} /sdcard/kindle/r34_nested_lists_tables.kfx")
    run(f"adb push {epub_path} /sdcard/epub/r34_nested_lists_tables.epub")
    
    key = "AMZNID0/R34NESTED001/0/"
    asin = "CR!R34NESTED001"
    title = "Folio v3.1 Nested Lists & Table Spans"
    author = "Folio Architecture Team"
    file_path = "/storage/emulated/0/kindle/r34_nested_lists_tables.kfx"
    last_accessed = 1795360000000

    print("[2/3] Generating SQLite statements...")
    sql = f"""
BEGIN TRANSACTION;
INSERT OR REPLACE INTO KindleContent (
    ID, STATE, ARCHIVABLE, HAS_READ_ALONG, TYPE, KEPT, CONTENT_TYPE, TITLE, AUTHOR, HAS_XRAY, DICTIONARY_TYPE, TITLE_ORDER, AUTHOR_ORDER, AUTHOR_ORDER_REVERSED, READ_STATE, READ_STATE_ORIGIN, CONTENT_TAGS, HAS_LOADED_COVER, IS_HIDDEN, IS_HIDDEN_BY_USER
) VALUES (
    '{key}', 'DOWNLOADED', 1, 0, 'BT_EBOOK_PDOC', 1, 'application/x-kfx-ebook', '{title}', '{author}', -1, 'NONE', -1, -1, -1, 'UNREAD', 'OTHER', 'CONTENT_TAGS', 0, 0, 0
);
INSERT OR REPLACE INTO LocalContent (
    KEY, FILE_PATH, GUID, LAST_MODIFIED, OWNERSHIP_TYPE, FILE_SIZE
) VALUES (
    '{key}', '{file_path}', '{asin}', {last_accessed}, 'Unknown', {kfx_size}
);
INSERT OR REPLACE INTO UserContent (
    KEY, USER_ID, LAST_ACCESSED, LPR, MRPR, FPR, MLT_LASTUPDATED, READING_PROGRESS, IS_IN_CAROUSEL, CLIPPING_BALANCE, HAS_UNMIGRATED_ANNOTATION
) VALUES (
    '{key}', 'MASTER', {last_accessed}, -1, -1, -1, -1, 0, 0, 0, 0
);
COMMIT;
"""
    with open("/tmp/r34_register.sql", "w") as f:
        f.write(sql)
        
    print("[3/3] Executing SQLite registration on device...")
    run("adb push /tmp/r34_register.sql /data/local/tmp/r34_register.sql")
    run("adb shell \"su -c 'sqlite3 /data/data/com.amazon.kindle.epub/databases/kindle_library.db < /data/local/tmp/r34_register.sql'\"")
    print("Registered successfully!")

if __name__ == '__main__':
    main()
