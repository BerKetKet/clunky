# Round 160 Report — Read-Only Storage Diagnostic (`com.azath.reader` 3+ GB Investigation)

## 1. Target Hardware & Policy Compliance
- **Hardware**: Physical **Samsung Galaxy M30** (`32001776e8e82623`), TrebleDroid Android 13 GSI (`arm64-v8a`).
- **Active Package**: `com.azath.reader` (`Azath v17.9 KFX`).
- **Safety Invariant Verified**: Stock `com.amazon.kindle` (`versionName=8.160.1.0(2.0.124621.0)`) remains **100% UNTOUCHED and READ-ONLY** (`lastUpdateTime=2026-11-19 21:59:23`). Zero files deleted, zero APK builds, zero `ct.reader` references.

## 2. Root Cause Summary
- **`/data/user/0/com.azath.reader/files/epub_cache` is `2.9G`** (out of `2.9G` total in `/data/user/0/com.azath.reader`).
- It contains hundreds of pre-converted `.epub.kfx` + `.cover.jpg` + `.sdr` entries (timestamps `2026-11-25 07:51–08:01`), including 40–147 MB illustrated EPUBs from `/sdcard/en/0/`.
- `/data/app/.../com.azath.reader-...` is `259M` (`base.apk` `133M`, `lib` `126M`), `/sdcard/Android/data/com.azath.reader` is `33M` (`appexpan` `30M`), `/data/user/0/com.azath.reader/files/folio_library` is `4.3M`, and shared `/sdcard/kindle` is `202,894 KB` (~198 MB).

## 3. Verbatim Storage Diagnostic Outputs

### 3.1 `pm path` & `/data/app` Size
```text
pm path: /data/app/~~BqeFHjOMVC4qg1Kcqzi7yw==/com.azath.reader-7qiOYTsPtbSHfUo7KVrDJQ==/base.apk
259M	/data/app/~~BqeFHjOMVC4qg1Kcqzi7yw==/com.azath.reader-7qiOYTsPtbSHfUo7KVrDJQ==
133M	/data/app/~~BqeFHjOMVC4qg1Kcqzi7yw==/com.azath.reader-7qiOYTsPtbSHfUo7KVrDJQ==/base.apk
126M	/data/app/~~BqeFHjOMVC4qg1Kcqzi7yw==/com.azath.reader-7qiOYTsPtbSHfUo7KVrDJQ==/lib
```

### 3.2 `/data/user/0/com.azath.reader/` Breakdown
```text
2.9G	/data/user/0/com.azath.reader
13K	/data/user/0/com.azath.reader/app_MINERVA
2.8M	/data/user/0/com.azath.reader/app_kindleforandroid
165K	/data/user/0/com.azath.reader/app_webview
5.5M	/data/user/0/com.azath.reader/cache
1.1M	/data/user/0/com.azath.reader/databases
2.9G	/data/user/0/com.azath.reader/files
556K	/data/user/0/com.azath.reader/no_backup
64K	/data/user/0/com.azath.reader/shared_prefs
2.9G	/data/user/0/com.azath.reader/files/epub_cache
4.3M	/data/user/0/com.azath.reader/files/folio_library
5.2M	/data/user/0/com.azath.reader/files/out_of_band_assets
1.2M	/data/user/0/com.azath.reader/files/res_android.dat
3.2M	/data/user/0/com.azath.reader/cache/WebView
888K	/data/user/0/com.azath.reader/cache/folio_cover_cache
840K	/data/user/0/com.azath.reader/cache/font-cache
160K	/data/user/0/com.azath.reader/cache/folio_meta_cache
```

### 3.3 Top 25 Largest Files in `/data/user/0/com.azath.reader/` (KB)
```text
147048	/data/user/0/com.azath.reader/files/epub_cache/The Madman's Library_ The Greatest Curiosities of Literature -- Edward Brooke-Hitching -- 2020 -- Simon & Schuster UK -- 1471166910 -- 11c07fff6cd23d2f86677e6cc52e46d0.epub.kfx
123428	/data/user/0/com.azath.reader/files/epub_cache/Carla Emery - The Encyclopedia of Country Living, 50th Anniversary Edition_ The Original Manual for Living off the Land & Doing It Yourself (3 Dec 2019, Sasquatch Books).epub.kfx
110184	/data/user/0/com.azath.reader/files/epub_cache/Subpar Parks_ America's Most Extraordinary National Parks -- Amber Share -- 2021 -- Plume -- 0593185544 -- 491a5eb7d72803f57a62103205034014.epub.kfx
106784	/data/user/0/com.azath.reader/files/epub_cache/American Medical Association Family Medical Guide - American Medical Association.epub.kfx
106704	/data/user/0/com.azath.reader/files/epub_cache/American Medical Association Family Medical Guide -- American Medical Association -- 2011 -- John Wiley & Sons, Ltd_ -- 9781118039748 -- 403e4d1eaf61bf139115552fdc3250c8.epub.kfx
98252	/data/user/0/com.azath.reader/files/epub_cache/Arcanum Unbounded_ The Cosmere Collection (Edgedancer; The -- Sanderson, Brandon -- Cosmere, 2016 -- Tom Doherty Associates -- 0765391163 -- 948fcc8eaf5c963d38696eabba31b6b8.epub.kfx
88492	/data/user/0/com.azath.reader/files/epub_cache/[Delphi Classics ] Dickens, Charles - Complete Works of Charles Dickens (2011, Delphi Classics).epub.kfx
82896	/data/user/0/com.azath.reader/files/epub_cache/Lemony Snicket, - A Series of Unfortunate Events Collection_ Books 1-13 with Bonus Material (2016, HarperCollins).epub.kfx
81784	/data/user/0/com.azath.reader/files/epub_cache/The Green Beret Survival Guide_ Advice on Situational -- Brian M_ Morris -- 2019 -- Skyhorse Publishing Company -- 9781510740754 -- 01131554a42c230d562b065361cea3c4.epub.kfx
75076	/data/user/0/com.azath.reader/files/epub_cache/The disaster survival guide_ how to prepare for and survive -- Jones, Marie D -- 2018 -- Visible Ink Press -- 9781578596850 -- d198dfff60563d7fa68305113d515b4a.epub.kfx
68048	/data/user/0/com.azath.reader/files/epub_cache/Prepper's Medical Manual The Ultimate Readiness Guide for -- Jones, James C_; -- 2023 -- Skyhorse Publishing Company, Incorporated -- f577e7a3d75a98bb5f9f95d932a280e5.epub.kfx
57596	/data/user/0/com.azath.reader/files/epub_cache/Clint Emerson - The Rugged Life _ The Modern Guide to Self-Reliance (2022, Harmony_Rodale).epub.kfx
56108	/data/user/0/com.azath.reader/files/epub_cache/DK - A History of Magic, Witchcraft, and the Occult (2020, DK).epub.kfx
55016	/data/user/0/com.azath.reader/files/epub_cache/Michael Finkel - The Art Thief_ A True Story of Love, Crime, and a Dangerous Obsession.epub.kfx
54332	/data/user/0/com.azath.reader/files/epub_cache/Savage, Adam - Every Tool's a Hammer_ Lessons from a Lifetime of Making (t) (2019, Atria Books_Touchstone).epub.kfx
53364	/data/user/0/com.azath.reader/files/epub_cache/Arthur Conan Doyle - The Complete Sherlock Holmes (Illustrated)_ All 4 Novels and 56 Stories with More Than 480 Illustrations (2014, Top Five Books).epub.kfx
52320	/data/user/0/com.azath.reader/files/epub_cache/The food of Italy _ region by region -- Roden, Claudia -- [New ed_], 1998 -- Random House;Vintage -- 9780099273257 -- 2f42f1c6792292fc6306ab06eac56853.epub.kfx
52264	/data/user/0/com.azath.reader/files/epub_cache/(A History of Ancient Egypt  2) John Romer - A History of Ancient Egypt Volume 2-Thomas Dunne Books (2017).epub.kfx
51652	/data/user/0/com.azath.reader/files/epub_cache/Hamilton, Edith _ Tierney, Jim - Mythology_ Timeless Tales of Gods and Heroes (75 th Anniversary Illustrated ed) (2017, Black Dog & Leventhal Publishers).epub.kfx
47116	/data/user/0/com.azath.reader/files/epub_cache/Max Miller, Ann Volkwein - Tasting History_ Explore the Past through 4,000 Years of Recipes (A Cookbook) (2023, S&S_Simon Element).epub.kfx
44192	/data/user/0/com.azath.reader/files/epub_cache/The Twenty-Ninth Day _ Surviving a Grizzly Attack in the -- Messenger Alex (author) -- First edition, Ashland, OR, 2019 -- Blackstone Publishing -- 9781094091242 -- 49da58f127bc008f979c23e681e0c16c .epub.kfx
44192	/data/user/0/com.azath.reader/files/epub_cache/Messenger, Alex - The twenty-ninth day_ surviving a grizzly attack in the Canadian tundra-Blackstone Publishing (2019).epub.kfx
44068	/data/user/0/com.azath.reader/files/epub_cache/C.S. Lewis - The Chronicles of Narnia Books 1–7-HarperCollins (2013).epub.kfx
43956	/data/user/0/com.azath.reader/files/epub_cache/Richard Rhodes - The Making of the Atomic Bomb (2012, Simon & Schuster).epub.kfx
43892	/data/user/0/com.azath.reader/files/epub_cache/The Chronicles of Narnia Comple - C.S. Lewis.epub.kfx
```

### 3.4 `/sdcard/Android/data/com.azath.reader/`
```text
33M	/sdcard/Android/data/com.azath.reader
33M	/sdcard/Android/data/com.azath.reader/files
30M	/sdcard/Android/data/com.azath.reader/files/appexpan
1.2M	/sdcard/Android/data/com.azath.reader/files/195449F1DD7278C070D511BADA1ABB7D
931K	/sdcard/Android/data/com.azath.reader/files/09CA146605C45E9D0B2DC8B0C8AAB59C
```

### 3.5 `/sdcard/kindle/` (KB)
```text
202894	/sdcard/kindle
32496	/sdcard/kindle/Homer - Delphi Complete Works.kfx
26888	/sdcard/kindle/Cicero - Delphi Complete Works.kfx
25152	/sdcard/kindle/Plutarch - Delphi Complete Works.kfx
23392	/sdcard/kindle/Aristotle - Delphi Complete Works.kfx
10432	/sdcard/kindle/Livy - Delphi Complete Works.kfx
9092	/sdcard/kindle/Herodotus - Delphi Complete Works.kfx
8600	/sdcard/kindle/Seneca - Delphi Complete Works.kfx
7516	/sdcard/kindle/Tacitus - Delphi Complete Works.kfx
6184	/sdcard/kindle/Julius Caesar - Delphi Complete Works.kfx
5628	/sdcard/kindle/Aristophanes - Delphi Complete Works.kfx
4592	/sdcard/kindle/Pliny - Delphi Complete Works.kfx
4096	/sdcard/kindle/sophocles.epub
3952	/sdcard/kindle/sophocles.kfx
3952	/sdcard/kindle/Sophocles - Delphi Complete Works.kfx
2936	/sdcard/kindle/Aeschylus - Delphi Complete Works.kfx
2380	/sdcard/kindle/Sallust - Delphi Complete Works.kfx
1860	/sdcard/kindle/Cato the Elder - Delphi Complete Works.kfx
1272	/sdcard/kindle/Eyeless_In_Gaza_FOLIO_83848DCC3B03.kfx
1208	/sdcard/kindle/Hesiod - Delphi Complete Works.kfx
1196	/sdcard/kindle/hesiod.kfx
1116	/sdcard/kindle/Moksha_FOLIO_72C0BAE5934F.kfx
1088	/sdcard/kindle/Chambers_s_Twentieth_Century_Dictionary__part_1_of_37683.epub
968	/sdcard/kindle/Brave_New_World_FOLIO_621B293A3B8F.kfx
960	/sdcard/kindle/The_Works_of_William_Shakespeare__Cambridge_Editio_49297.epub
924	/sdcard/kindle/the_210th_day.azw3
856	/sdcard/kindle/Confessions_of_Zeno_opds_1.epub
708	/sdcard/kindle/Moby_Dick__Or__The_Whale_2701.epub
700	/sdcard/kindle/backups
664	/sdcard/kindle/The_50th_Law_FOLIO_9F2166AF9585.kfx
```

## 4. Project Lead Architectural Directive (Mandatory Reiteration)
> "The approach to display the cover as part of the book is a mistake. The correct path to approach the cover to render properly reliably is the following: when parsing the EPUB for the first time and converting it, remove the cover page from the reflowable spine and take the image for the cover. It is then processed separately and kept in its own class/structure and inserted when displaying the book separately (as a dedicated UI cover view) rather than as a reflowable spine page."
