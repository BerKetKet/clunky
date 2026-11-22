#!/usr/bin/env python3
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
    books = [
        {
            "key": "AMZNID0/ARISTOTLE001DELPHI/0/",
            "asin": "CR!ARISTOTLE001DELPHI",
            "title": "Delphi Complete Works of Aristotle (Illustrated) (Delphi Ancient Classics Book 11)",
            "author": "Aristotle",
            "sort_title": "delphi complete works of aristotle illustrated delphi ancient classics book 11",
            "sort_author": "aristotle",
            "file_path": "/storage/emulated/0/kindle/Aristotle - Delphi Complete Works.kfx",
            "file_size": 23926724,
            "last_accessed": 1795360000000,
        },
        {
            "key": "AMZNID0/HERODOTUS001DELPHI/0/",
            "asin": "CR!HERODOTUS001DELPHI",
            "title": "Complete Works of Herodotus (Illustrated) (Delphi Ancient Classics)",
            "author": "Herodotus",
            "sort_title": "complete works of herodotus illustrated delphi ancient classics",
            "sort_author": "herodotus",
            "file_path": "/storage/emulated/0/kindle/Herodotus - Delphi Complete Works.kfx",
            "file_size": 9301863,
            "last_accessed": 1795350000000,
        },
        {
            "key": "AMZNID0/SENECA001DELPHI/0/",
            "asin": "CR!SENECA001DELPHI",
            "title": "Delphi Complete Works of Seneca the Younger (Illustrated) (Delphi Ancient Classics)",
            "author": "Lucius Annaeus Seneca",
            "sort_title": "delphi complete works of seneca the younger illustrated delphi ancient classics",
            "sort_author": "lucius annaeus seneca",
            "file_path": "/storage/emulated/0/kindle/Seneca - Delphi Complete Works.kfx",
            "file_size": 8798052,
            "last_accessed": 1795340000000,
        }
    ]

    sql = "BEGIN TRANSACTION;\n"
    for b in books:
        sql += f"""
INSERT OR REPLACE INTO KindleContent (
    ID, STATE, ERROR, ARCHIVABLE, HAS_READ_ALONG, TYPE, KEPT, CONTENT_TYPE, TITLE, AUTHOR, PUBLICATION_DATE, HAS_XRAY, LANGUAGE, SORTABLE_TITLE, SORTABLE_AUTHOR, DICTIONARY_TYPE, TITLE_ORDER, AUTHOR_ORDER, AUTHOR_ORDER_REVERSED, READ_STATE, READ_STATE_ORIGIN, HAS_LOADED_COVER, IS_HIDDEN, IS_HIDDEN_BY_USER
) VALUES (
    '{b["key"]}', 'LOCAL', -1, 0, 0, 'BT_EBOOK', 0, 'application/x-kfx-ebook', '{b["title"]}', '{b["author"]}', 1370217600000, 0, 'en', '{b["sort_title"]}', '{b["sort_author"]}', 'NONE', -1, -1, -1, 'UNREAD', 'OTHER', 1, 0, 0
);
INSERT OR REPLACE INTO LocalContent (
    KEY, FILE_PATH, GUID, LAST_MODIFIED, OWNERSHIP_TYPE, FILE_SIZE
) VALUES (
    '{b["key"]}', '{b["file_path"]}', '{b["asin"]}', {b["last_accessed"]}, 'Unknown', {b["file_size"]}
);
INSERT OR REPLACE INTO UserContent (
    KEY, USER_ID, LAST_ACCESSED, LPR, MRPR, FPR, MLT_LASTUPDATED, READING_PROGRESS, IS_IN_CAROUSEL, CLIPPING_BALANCE, HAS_UNMIGRATED_ANNOTATION
) VALUES (
    '{b["key"]}', 'MASTER', {b["last_accessed"]}, 0, 0, 0, -1, 0, 1, 0, 0
);
"""
    sql += "COMMIT;\n"

    with open("/tmp/register_omnibuses.sql", "w") as f:
        f.write(sql)

    print("Pushing and applying SQL registration...")
    run("adb push /tmp/register_omnibuses.sql /data/local/tmp/register_omnibuses.sql")
    run("adb shell \"su -c 'sqlite3 /data/data/com.amazon.kindle.epub/databases/kindle_library.db < /data/local/tmp/register_omnibuses.sql'\"")
    print("Registration complete!")

if __name__ == '__main__':
    main()
