package com.amazon.kindle.epub.ingest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Persistent local metadata store (folio_metadata.db) recording rich EPUB
 * details (title, author, publisher, pub_date, genres, language, isbn,
 * description, open_library_key, app-private kfx_path, cover_path), duplicate
 * lookup, genre aggregation, and schema-safe registration into kindle_library.db.
 */
public final class FolioMetadataDb extends SQLiteOpenHelper {

    private static final String TAG = "FolioMetadataDb";
    private static final String DB_NAME = "folio_metadata.db";
    private static final int DB_VERSION = 1;

    public static final class BookRecord {
        public long id;
        public String bookKey = "";
        public String kfxPath = "";
        public String coverPath = "";
        public String sourceName = "";
        public String title = "";
        public String author = "";
        public String publisher = "";
        public String pubDate = "";
        public String genres = "";
        public String language = "";
        public String isbn = "";
        public String description = "";
        public String openLibraryKey = "";
        public String contentHash = "";
        public long sourceSize;
        public long addedAt;

        public boolean hasGenre(String target) {
            if (target == null || target.isEmpty() || "All".equalsIgnoreCase(target)) return true;
            if (genres == null || genres.isEmpty()) return false;
            String needle = target.trim().toLowerCase(Locale.ROOT);
            for (String part : genres.split(",")) {
                if (part.trim().toLowerCase(Locale.ROOT).equals(needle)) return true;
            }
            return false;
        }
    }

    public FolioMetadataDb(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS books_metadata ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "book_key TEXT UNIQUE NOT NULL, "
                + "kfx_path TEXT UNIQUE NOT NULL, "
                + "cover_path TEXT, "
                + "source_name TEXT, "
                + "title TEXT NOT NULL, "
                + "author TEXT, "
                + "publisher TEXT, "
                + "pub_date TEXT, "
                + "genres TEXT, "
                + "language TEXT, "
                + "isbn TEXT, "
                + "description TEXT, "
                + "open_library_key TEXT, "
                + "added_at INTEGER NOT NULL"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void upsertBook(String bookKey, File kfxFile, File coverFile,
                           String sourceName, EpubMetadataExtractor.BookMeta meta) {
        upsertBook(bookKey, kfxFile, coverFile, sourceName, meta, null, 0L);
    }

    private static String nonBlankOrPrior(String incoming, String priorVal) {
        if (incoming != null && !incoming.trim().isEmpty()) return incoming;
        return priorVal != null ? priorVal : "";
    }

    public void upsertBook(String bookKey, File kfxFile, File coverFile,
                           String sourceName, EpubMetadataExtractor.BookMeta meta,
                           String contentHash, long sourceSize) {
        String kfxPath = kfxFile.getAbsolutePath();
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            java.util.Map<String, String> cols = tableColumns(db, "books_metadata");
            BookRecord prior = null;
            Cursor c = null;
            try {
                c = db.rawQuery(
                        "SELECT id, book_key, kfx_path, cover_path, source_name, title, author, "
                        + "publisher, pub_date, genres, language, isbn, description, open_library_key, added_at "
                        + "FROM books_metadata WHERE book_key = ? OR kfx_path = ? LIMIT 1",
                        new String[] { bookKey != null ? bookKey : "", kfxPath });
                if (c.moveToFirst()) {
                    prior = readRow(c);
                }
            } finally {
                if (c != null) c.close();
            }

            String incomingCover = (coverFile != null && coverFile.exists()) ? coverFile.getAbsolutePath() : "";
            String coverPath = nonBlankOrPrior(incomingCover, prior != null ? prior.coverPath : "");
            String genres = nonBlankOrPrior(meta.genresCsv(), prior != null ? prior.genres : "");
            String publisher = nonBlankOrPrior(meta.publisher, prior != null ? prior.publisher : "");
            String pubDate = nonBlankOrPrior(meta.pubDate, prior != null ? prior.pubDate : "");
            String isbn = nonBlankOrPrior(meta.isbn, prior != null ? prior.isbn : "");
            String olKey = nonBlankOrPrior(meta.openLibraryKey, prior != null ? prior.openLibraryKey : "");
            String description = nonBlankOrPrior(meta.description, prior != null ? prior.description : "");
            String language = nonBlankOrPrior(meta.language, prior != null ? prior.language : "");
            long addedAt = (prior != null && prior.addedAt > 0) ? prior.addedAt : System.currentTimeMillis();

            ContentValues cv = new ContentValues();
            cv.put("book_key", prior != null && !prior.bookKey.isEmpty() ? prior.bookKey : bookKey);
            cv.put("kfx_path", kfxPath);
            cv.put("cover_path", coverPath);
            cv.put("source_name", sourceName != null ? sourceName : kfxFile.getName());
            cv.put("title", meta.title != null && !meta.title.trim().isEmpty()
                    ? meta.title : (prior != null && !prior.title.isEmpty() ? prior.title : kfxFile.getName()));
            cv.put("author", meta.author != null && !meta.author.trim().isEmpty()
                    ? meta.author : (prior != null && !prior.author.isEmpty() ? prior.author : "Unknown Author"));
            cv.put("publisher", publisher);
            cv.put("pub_date", pubDate);
            cv.put("genres", genres);
            cv.put("language", language);
            cv.put("isbn", isbn);
            cv.put("description", description);
            cv.put("open_library_key", olKey);
            if (cols.containsKey("CONTENT_HASH") && contentHash != null && !contentHash.isEmpty()) {
                cv.put("content_hash", contentHash);
            }
            if (cols.containsKey("SOURCE_SIZE") && sourceSize > 0) {
                cv.put("source_size", sourceSize);
            }
            cv.put("added_at", addedAt);

            if (prior != null) {
                db.update("books_metadata", cv, "id = ?", new String[] { String.valueOf(prior.id) });
            } else {
                db.insertOrThrow("books_metadata", null, cv);
            }
            db.setTransactionSuccessful();
        } finally {
            if (db.inTransaction()) db.endTransaction();
        }
    }

    /**
     * Finds an existing imported book strictly by content_hash OR book_key ONLY
     * whose kfx_path still exists on disk.
     */
    public BookRecord findExisting(String bookKey, String contentHash) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = null;
        try {
            java.util.Map<String, String> cols = tableColumns(db, "books_metadata");
            boolean hasHashCol = cols.containsKey("CONTENT_HASH");
            if (hasHashCol && contentHash != null && !contentHash.isEmpty()) {
                c = db.rawQuery(
                        "SELECT id, book_key, kfx_path, cover_path, source_name, title, author, "
                        + "publisher, pub_date, genres, language, isbn, description, open_library_key, added_at "
                        + "FROM books_metadata "
                        + "WHERE content_hash = ? OR book_key = ? "
                        + "ORDER BY added_at DESC LIMIT 1",
                        new String[] { contentHash, bookKey != null ? bookKey : "" });
            } else {
                c = db.rawQuery(
                        "SELECT id, book_key, kfx_path, cover_path, source_name, title, author, "
                        + "publisher, pub_date, genres, language, isbn, description, open_library_key, added_at "
                        + "FROM books_metadata "
                        + "WHERE book_key = ? "
                        + "ORDER BY added_at DESC LIMIT 1",
                        new String[] { bookKey != null ? bookKey : "" });
            }
            if (c.moveToFirst()) {
                BookRecord rec = readRow(c);
                if (rec.kfxPath != null && new File(rec.kfxPath).exists()) {
                    return rec;
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "findExisting query failed", t);
        } finally {
            if (c != null) c.close();
        }
        return null;
    }

    /**
     * Advisory-only title+author match for import dialog hints.
     * Never triggers when author is blank or "Unknown Author", and never merges automatically.
     */
    public BookRecord findPossibleDuplicate(String title, String author) {
        if (title == null || title.trim().isEmpty()) return null;
        if (author == null || author.trim().isEmpty()
                || "Unknown Author".equalsIgnoreCase(author.trim())) {
            return null;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = null;
        try {
            c = db.rawQuery(
                    "SELECT id, book_key, kfx_path, cover_path, source_name, title, author, "
                    + "publisher, pub_date, genres, language, isbn, description, open_library_key, added_at "
                    + "FROM books_metadata "
                    + "WHERE LOWER(TRIM(title)) = LOWER(TRIM(?)) AND LOWER(TRIM(author)) = LOWER(TRIM(?)) "
                    + "ORDER BY added_at DESC LIMIT 1",
                    new String[] { title, author });
            if (c.moveToFirst()) {
                BookRecord rec = readRow(c);
                if (rec.kfxPath != null && new File(rec.kfxPath).exists()) {
                    return rec;
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "findPossibleDuplicate query failed", t);
        } finally {
            if (c != null) c.close();
        }
        return null;
    }

    public List<BookRecord> getAllBooks() {
        List<BookRecord> out = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = null;
        try {
            c = db.rawQuery(
                    "SELECT id, book_key, kfx_path, cover_path, source_name, title, author, "
                    + "publisher, pub_date, genres, language, isbn, description, open_library_key, added_at "
                    + "FROM books_metadata ORDER BY added_at DESC", null);
            while (c.moveToNext()) {
                out.add(readRow(c));
            }
        } catch (Throwable t) {
            Log.w(TAG, "getAllBooks failed", t);
        } finally {
            if (c != null) c.close();
        }
        return out;
    }

    public List<String> getDistinctGenres(List<BookRecord> books) {
        Set<String> set = new LinkedHashSet<>();
        set.add("All");
        if (books != null) {
            for (BookRecord r : books) {
                if (r.genres == null || r.genres.isEmpty()) continue;
                for (String g : r.genres.split(",")) {
                    String clean = g.trim();
                    if (!clean.isEmpty() && clean.length() <= 32) {
                        set.add(clean);
                        if (set.size() >= 12) break;
                    }
                }
                if (set.size() >= 12) break;
            }
        }
        return new ArrayList<>(set);
    }

    private static BookRecord readRow(Cursor c) {
        BookRecord r = new BookRecord();
        r.id = c.getLong(0);
        r.bookKey = safeStr(c.getString(1));
        r.kfxPath = safeStr(c.getString(2));
        r.coverPath = safeStr(c.getString(3));
        r.sourceName = safeStr(c.getString(4));
        r.title = safeStr(c.getString(5));
        r.author = safeStr(c.getString(6));
        r.publisher = safeStr(c.getString(7));
        r.pubDate = safeStr(c.getString(8));
        r.genres = safeStr(c.getString(9));
        r.language = safeStr(c.getString(10));
        r.isbn = safeStr(c.getString(11));
        r.description = safeStr(c.getString(12));
        r.openLibraryKey = safeStr(c.getString(13));
        r.addedAt = c.getLong(14);
        return r;
    }

    private static String safeStr(String s) {
        return s == null ? "" : s;
    }

    /**
     * Schema-adaptive transactional registration into kindle_library.db aligned
     * with Kindle v8.159 NOT NULL columns so imported books appear immediately
     * in Library and Continue Reading with real covers.
     */
    public static boolean registerInKindleLibraryDb(
            Context ctx, String bookKey, File kfxFile, File coverFile,
            EpubMetadataExtractor.BookMeta meta) {
        File dbFile = ctx.getDatabasePath("kindle_library.db");
        if (dbFile == null || !dbFile.exists()) {
            Log.w(TAG, "kindle_library.db not found yet at " + dbFile);
            return false;
        }
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
            db.beginTransaction();
            long now = System.currentTimeMillis();
            String path = kfxFile.getAbsolutePath();
            String coverPath = (coverFile != null && coverFile.exists()) ? coverFile.getAbsolutePath() : "";
            String title = !EpubMetadataExtractor.isBlank(meta.title) ? meta.title : kfxFile.getName();
            String author = !EpubMetadataExtractor.isBlank(meta.author) ? meta.author : "Unknown Author";
            String asin = "CR!" + bookKey.replace("FOLIO_", "") + "KFX";
            String amznId = "AMZNID0/" + asin + "/0/";
            int hasCover = coverPath.isEmpty() ? 0 : 1;

            // Remove any prior row pointing to the exact same path to prevent duplicates
            try {
                db.delete("LocalContent", "FILE_PATH = ?", new String[] { path });
            } catch (Throwable ignored) {}

            // 1. KindleContent
            java.util.Map<String, String> kcCols = tableColumns(db, "KindleContent");
            if (!kcCols.isEmpty()) {
                ContentValues kc = new ContentValues();
                putIfPresent(kc, kcCols, "ID", amznId);
                putIfPresent(kc, kcCols, "KEY", bookKey);
                putIfPresent(kc, kcCols, "CONTENT_ID", bookKey);
                putIfPresent(kc, kcCols, "ASIN", asin);
                putIfPresent(kc, kcCols, "TITLE", title);
                putIfPresent(kc, kcCols, "TITLE_SORT", title.toLowerCase(Locale.ROOT));
                putIfPresent(kc, kcCols, "AUTHOR", author);
                putIfPresent(kc, kcCols, "AUTHORS", author);
                putIfPresent(kc, kcCols, "AUTHOR_SORT", author.toLowerCase(Locale.ROOT));
                putIfPresent(kc, kcCols, "TYPE", "BT_EBOOK_PDOC");
                putIfPresent(kc, kcCols, "CONTENT_TYPE", "application/x-kfx-ebook");
                putIfPresent(kc, kcCols, "MIME_TYPE", "application/x-kfx-ebook");
                putIfPresent(kc, kcCols, "STATE", "LOCAL");
                putIfPresent(kc, kcCols, "ARCHIVABLE", 0);
                putIfPresent(kc, kcCols, "HAS_READ_ALONG", 0);
                putIfPresent(kc, kcCols, "HAS_LOADED_COVER", hasCover);
                putIfPresent(kc, kcCols, "FILE_PATH", path);
                putIfPresent(kc, kcCols, "COVER_URL", coverPath);
                putIfPresent(kc, kcCols, "PUBLISHER", meta.publisher);
                putIfPresent(kc, kcCols, "PUBLICATION_DATE", meta.pubDate);
                putIfPresent(kc, kcCols, "LANGUAGE", meta.language);
                putIfPresent(kc, kcCols, "FILE_SIZE", kfxFile.length());
                putIfPresent(kc, kcCols, "LAST_ACCESS_TIME", now);
                putIfPresent(kc, kcCols, "PURCHASE_DATE", now);
                putIfPresent(kc, kcCols, "IS_LOCAL", 1);
                putIfPresent(kc, kcCols, "DOWNLOAD_STATE", 1);
                fillMissingNotNullDefaults(db, "KindleContent", kc);
                db.insertWithOnConflict("KindleContent", null, kc, SQLiteDatabase.CONFLICT_REPLACE);
            }

            // 2. LocalContent
            java.util.Map<String, String> lcCols = tableColumns(db, "LocalContent");
            if (!lcCols.isEmpty()) {
                ContentValues lc = new ContentValues();
                putIfPresent(lc, lcCols, "KEY", amznId);
                putIfPresent(lc, lcCols, "CONTENT_ID", amznId);
                putIfPresent(lc, lcCols, "FILE_PATH", path);
                putIfPresent(lc, lcCols, "GUID", asin);
                putIfPresent(lc, lcCols, "COVER_PATH", coverPath);
                putIfPresent(lc, lcCols, "FILE_SIZE", kfxFile.length());
                putIfPresent(lc, lcCols, "LAST_MODIFIED", now);
                putIfPresent(lc, lcCols, "OWNERSHIP_TYPE", "Unknown");
                putIfPresent(lc, lcCols, "MIME_TYPE", "application/x-kfx-ebook");
                fillMissingNotNullDefaults(db, "LocalContent", lc);
                db.insertWithOnConflict("LocalContent", null, lc, SQLiteDatabase.CONFLICT_REPLACE);
            }

            // 3. UserContent
            java.util.Map<String, String> ucCols = tableColumns(db, "UserContent");
            if (!ucCols.isEmpty()) {
                ContentValues uc = new ContentValues();
                putIfPresent(uc, ucCols, "KEY", amznId);
                putIfPresent(uc, ucCols, "USER_ID", "MASTER");
                putIfPresent(uc, ucCols, "LAST_ACCESSED", now);
                putIfPresent(uc, ucCols, "LAST_ACCESS_TIME", now);
                putIfPresent(uc, ucCols, "IS_IN_CAROUSEL", 1);
                putIfPresent(uc, ucCols, "CLIPPING_BALANCE", 0);
                putIfPresent(uc, ucCols, "READING_PROGRESS", 0);
                fillMissingNotNullDefaults(db, "UserContent", uc);
                db.insertWithOnConflict("UserContent", null, uc, SQLiteDatabase.CONFLICT_REPLACE);
            }

            db.setTransactionSuccessful();
            return true;
        } catch (Throwable t) {
            Log.e(TAG, "Failed to register book in kindle_library.db", t);
            return false;
        } finally {
            if (db != null) {
                try {
                    if (db.inTransaction()) db.endTransaction();
                    db.close();
                } catch (Throwable ignored) {}
            }
        }
    }

    private static java.util.Map<String, String> tableColumns(SQLiteDatabase db, String table) {
        java.util.Map<String, String> cols = new java.util.HashMap<>();
        Cursor c = null;
        try {
            c = db.rawQuery("PRAGMA table_info(" + table + ")", null);
            int nameIdx = c.getColumnIndex("name");
            while (c.moveToNext()) {
                if (nameIdx >= 0) {
                    String n = c.getString(nameIdx);
                    if (n != null) cols.put(n.toUpperCase(Locale.ROOT), n);
                }
            }
        } catch (Throwable ignored) {
        } finally {
            if (c != null) c.close();
        }
        return cols;
    }

    private static void fillMissingNotNullDefaults(SQLiteDatabase db, String table, ContentValues cv) {
        Cursor c = null;
        try {
            c = db.rawQuery("PRAGMA table_info(" + table + ")", null);
            int nameIdx = c.getColumnIndex("name");
            int typeIdx = c.getColumnIndex("type");
            int nnIdx = c.getColumnIndex("notnull");
            int dfltIdx = c.getColumnIndex("dflt_value");
            int pkIdx = c.getColumnIndex("pk");
            while (c.moveToNext()) {
                String n = nameIdx >= 0 ? c.getString(nameIdx) : null;
                int nn = nnIdx >= 0 ? c.getInt(nnIdx) : 0;
                int pk = pkIdx >= 0 ? c.getInt(pkIdx) : 0;
                String dflt = (dfltIdx >= 0 && !c.isNull(dfltIdx)) ? c.getString(dfltIdx) : null;
                if (n != null && nn == 1 && pk == 0 && dflt == null && !cv.containsKey(n)) {
                    String tp = typeIdx >= 0 && c.getString(typeIdx) != null
                            ? c.getString(typeIdx).toUpperCase(Locale.ROOT) : "";
                    if (tp.contains("INT") || tp.contains("REAL") || tp.contains("NUM") || tp.contains("BOOL")) {
                        cv.put(n, 0);
                    } else {
                        cv.put(n, "");
                    }
                }
            }
        } catch (Throwable ignored) {
        } finally {
            if (c != null) c.close();
        }
    }

    private static void putIfPresent(ContentValues cv, java.util.Map<String, String> cols, String col, String val) {
        String actual = cols.get(col.toUpperCase(Locale.ROOT));
        if (actual != null && val != null) cv.put(actual, val);
    }

    private static void putIfPresent(ContentValues cv, java.util.Map<String, String> cols, String col, long val) {
        String actual = cols.get(col.toUpperCase(Locale.ROOT));
        if (actual != null) cv.put(actual, val);
    }

    private static void putIfPresent(ContentValues cv, java.util.Map<String, String> cols, String col, int val) {
        String actual = cols.get(col.toUpperCase(Locale.ROOT));
        if (actual != null) cv.put(actual, val);
    }

    private static void putIfPresent(ContentValues cv, java.util.Map<String, String> cols, String col, double val) {
        String actual = cols.get(col.toUpperCase(Locale.ROOT));
        if (actual != null) cv.put(actual, val);
    }

    /**
     * Removes legacy synthetic AMZNID0/CR!...KFX/0/ rows written prior to Round 158
     * so books in /sdcard/kindle/ appear exactly once (indexed by Kindle's native scanner).
     */
    public void deleteBook(String bookKey) {
        if (bookKey == null || bookKey.isEmpty()) return;
        try {
            getWritableDatabase().delete("books_metadata", "book_key = ?", new String[]{ bookKey });
        } catch (Throwable ignored) {}
    }

    public static int cleanupLegacyDuplicateRows(Context ctx) {
        if (ctx == null) return 0;
        File dbFile = ctx.getDatabasePath("kindle_library.db");
        if (dbFile == null || !dbFile.exists()) return 0;
        SQLiteDatabase db = null;
        int removed = 0;
        try {
            db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
            db.beginTransaction();

            Set<String> keysToDelete = new LinkedHashSet<>();
            Cursor kcCur = null;
            try {
                kcCur = db.rawQuery(
                        "SELECT ID FROM KindleContent WHERE ID LIKE 'AMZNID0/CR!%KFX/0/' AND FILE_PATH LIKE '/sdcard/kindle/%'",
                        null);
                while (kcCur.moveToNext()) {
                    String id = kcCur.getString(0);
                    if (id != null && !id.isEmpty()) keysToDelete.add(id);
                }
            } catch (Throwable t) {
                Log.w(TAG, "cleanupLegacyDuplicateRows KindleContent query failed", t);
            } finally {
                if (kcCur != null) kcCur.close();
            }

            Cursor lcCur = null;
            try {
                lcCur = db.rawQuery(
                        "SELECT KEY FROM LocalContent WHERE KEY LIKE 'AMZNID0/CR!%KFX/0/' AND FILE_PATH LIKE '/sdcard/kindle/%'",
                        null);
                while (lcCur.moveToNext()) {
                    String k = lcCur.getString(0);
                    if (k != null && !k.isEmpty()) keysToDelete.add(k);
                }
            } catch (Throwable t) {
                Log.w(TAG, "cleanupLegacyDuplicateRows LocalContent query failed", t);
            } finally {
                if (lcCur != null) lcCur.close();
            }

            if (!keysToDelete.isEmpty()) {
                try {
                    removed += db.delete("KindleContent",
                            "ID LIKE 'AMZNID0/CR!%KFX/0/' AND FILE_PATH LIKE '/sdcard/kindle/%'", null);
                } catch (Throwable t) {
                    Log.w(TAG, "cleanupLegacyDuplicateRows KindleContent delete failed", t);
                }
                try {
                    removed += db.delete("LocalContent",
                            "KEY LIKE 'AMZNID0/CR!%KFX/0/' AND FILE_PATH LIKE '/sdcard/kindle/%'", null);
                } catch (Throwable t) {
                    Log.w(TAG, "cleanupLegacyDuplicateRows LocalContent delete failed", t);
                }
                for (String key : keysToDelete) {
                    try {
                        removed += db.delete("UserContent", "KEY = ?", new String[]{ key });
                    } catch (Throwable t) {
                        Log.w(TAG, "cleanupLegacyDuplicateRows UserContent delete failed for key=" + key, t);
                    }
                }
            }
            db.setTransactionSuccessful();
        } catch (Throwable t) {
            Log.e(TAG, "cleanupLegacyDuplicateRows failed", t);
        } finally {
            if (db != null) {
                try {
                    if (db.inTransaction()) db.endTransaction();
                    db.close();
                } catch (Throwable t) {
                    Log.w(TAG, "cleanupLegacyDuplicateRows close failed", t);
                }
            }
        }
        return removed;
    }

    public static String computeContentHash(File file) {
        if (file == null || !file.isFile()) return "";
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[16384];
            int n;
            while ((n = fis.read(buf)) > 0) {
                md.update(buf, 0, n);
            }
            byte[] h = md.digest();
            StringBuilder sb = new StringBuilder(64);
            for (byte b : h) {
                sb.append(String.format(Locale.ROOT, "%02x", b));
            }
            return sb.toString();
        } catch (Throwable t) {
            Log.w(TAG, "computeContentHash failed for " + file.getName(), t);
            return "";
        }
    }

    public static String computeBookKeyFromHash(String contentHash) {
        if (contentHash != null && contentHash.length() >= 12) {
            return "FOLIO_" + contentHash.substring(0, 12).toUpperCase(Locale.ROOT);
        }
        return "FOLIO_" + Long.toHexString(System.currentTimeMillis()).toUpperCase(Locale.ROOT);
    }

    public static String extractBookKeyFromFilename(String filename) {
        if (filename == null) return null;
        String base = filename;
        if (base.toLowerCase(Locale.ROOT).endsWith(".kfx")) {
            base = base.substring(0, base.length() - 4);
        }
        int idx = base.lastIndexOf("_FOLIO_");
        if (idx >= 0) {
            String suffix = base.substring(idx + 1);
            if (suffix.startsWith("FOLIO_") && suffix.length() >= 10) {
                return suffix;
            }
        }
        return null;
    }

    public static String computeBookKey(String title, long sizeBytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest((title + "|" + sizeBytes).getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder("FOLIO_");
            for (int i = 0; i < 6; i++) {
                sb.append(String.format(Locale.ROOT, "%02X", h[i]));
            }
            return sb.toString();
        } catch (Throwable t) {
            return "FOLIO_" + Math.abs((title + sizeBytes).hashCode());
        }
    }
}
