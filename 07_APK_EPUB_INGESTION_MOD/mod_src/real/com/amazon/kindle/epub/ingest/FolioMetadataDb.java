package com.amazon.kindle.epub.ingest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Persistent local metadata store (folio_metadata.db) recording rich EPUB
 * details (title, author, publisher, pub_date, genres, language, isbn,
 * description, open_library_key, app-private kfx_path, cover_path), plus
 * schema-safe transactional registration into kindle_library.db.
 */
public final class FolioMetadataDb extends SQLiteOpenHelper {

    private static final String TAG = "FolioMetadataDb";
    private static final String DB_NAME = "folio_metadata.db";
    private static final int DB_VERSION = 1;

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
        // Forward-compatible additive migrations go here
    }

    public void upsertBook(String bookKey, File kfxFile, File coverFile,
                           String sourceName, EpubMetadataExtractor.BookMeta meta) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("book_key", bookKey);
        cv.put("kfx_path", kfxFile.getAbsolutePath());
        cv.put("cover_path", (coverFile != null && coverFile.exists()) ? coverFile.getAbsolutePath() : "");
        cv.put("source_name", sourceName != null ? sourceName : kfxFile.getName());
        cv.put("title", meta.title != null ? meta.title : kfxFile.getName());
        cv.put("author", meta.author != null ? meta.author : "Unknown Author");
        cv.put("publisher", meta.publisher != null ? meta.publisher : "");
        cv.put("pub_date", meta.pubDate != null ? meta.pubDate : "");
        cv.put("genres", meta.genresCsv());
        cv.put("language", meta.language != null ? meta.language : "");
        cv.put("isbn", meta.isbn != null ? meta.isbn : "");
        cv.put("description", meta.description != null ? meta.description : "");
        cv.put("open_library_key", meta.openLibraryKey != null ? meta.openLibraryKey : "");
        cv.put("added_at", System.currentTimeMillis());
        db.insertWithOnConflict("books_metadata", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Schema-adaptive registration into Kindle's kindle_library.db so the book
     * appears immediately in Library and Continue Reading with its real cover
     * and BT_EBOOK_PDOC classification (zero Amazon Store stubs).
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

            String asin = bookKey.startsWith("CR!") ? bookKey : ("CR!" + Math.abs(bookKey.hashCode()) + "KFX");
            String key = "AMZNID0/" + asin + "/0/";

            // 1. KindleContent
            Set<String> kcCols = tableColumns(db, "KindleContent");
            if (!kcCols.isEmpty()) {
                ContentValues kc = new ContentValues();
                putIfPresent(kc, kcCols, "ID", key);
                putIfPresent(kc, kcCols, "KEY", key);
                putIfPresent(kc, kcCols, "CONTENT_ID", key);
                putIfPresent(kc, kcCols, "ASIN", asin);
                putIfPresent(kc, kcCols, "STATE", "LOCAL");
                putIfPresent(kc, kcCols, "ARCHIVABLE", 0);
                putIfPresent(kc, kcCols, "HAS_READ_ALONG", 0);
                putIfPresent(kc, kcCols, "TYPE", "BT_EBOOK");
                putIfPresent(kc, kcCols, "CONTENT_TYPE", "application/x-kfx-ebook");
                putIfPresent(kc, kcCols, "MIME_TYPE", "application/x-kfx-ebook");
                putIfPresent(kc, kcCols, "TITLE", title);
                putIfPresent(kc, kcCols, "TITLE_SORT", title.toLowerCase(Locale.ROOT));
                putIfPresent(kc, kcCols, "AUTHOR", author);
                putIfPresent(kc, kcCols, "AUTHORS", author);
                putIfPresent(kc, kcCols, "AUTHOR_SORT", author.toLowerCase(Locale.ROOT));
                putIfPresent(kc, kcCols, "HAS_XRAY", 0);
                putIfPresent(kc, kcCols, "DICTIONARY_TYPE", "NONE");
                putIfPresent(kc, kcCols, "TITLE_ORDER", -1);
                putIfPresent(kc, kcCols, "AUTHOR_ORDER", -1);
                putIfPresent(kc, kcCols, "AUTHOR_ORDER_REVERSED", -1);
                putIfPresent(kc, kcCols, "READ_STATE", "UNREAD");
                putIfPresent(kc, kcCols, "READ_STATE_ORIGIN", "OTHER");
                putIfPresent(kc, kcCols, "HAS_LOADED_COVER", 1);
                putIfPresent(kc, kcCols, "IS_HIDDEN", 0);
                putIfPresent(kc, kcCols, "IS_HIDDEN_BY_USER", 0);
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
                db.insertWithOnConflict("KindleContent", null, kc, SQLiteDatabase.CONFLICT_REPLACE);
            }

            // 2. LocalContent
            Set<String> lcCols = tableColumns(db, "LocalContent");
            if (!lcCols.isEmpty()) {
                ContentValues lc = new ContentValues();
                putIfPresent(lc, lcCols, "KEY", key);
                putIfPresent(lc, lcCols, "CONTENT_ID", key);
                putIfPresent(lc, lcCols, "FILE_PATH", path);
                putIfPresent(lc, lcCols, "GUID", asin);
                putIfPresent(lc, lcCols, "COVER_PATH", coverPath);
                putIfPresent(lc, lcCols, "FILE_SIZE", kfxFile.length());
                putIfPresent(lc, lcCols, "LAST_MODIFIED", now);
                putIfPresent(lc, lcCols, "OWNERSHIP_TYPE", "Unknown");
                putIfPresent(lc, lcCols, "MIME_TYPE", "application/x-kfx-ebook");
                db.insertWithOnConflict("LocalContent", null, lc, SQLiteDatabase.CONFLICT_REPLACE);
            }

            // 3. UserContent
            Set<String> ucCols = tableColumns(db, "UserContent");
            if (!ucCols.isEmpty()) {
                ContentValues uc = new ContentValues();
                putIfPresent(uc, ucCols, "KEY", key);
                putIfPresent(uc, ucCols, "CONTENT_ID", key);
                putIfPresent(uc, ucCols, "USER_ID", "MASTER");
                putIfPresent(uc, ucCols, "LAST_ACCESSED", now);
                putIfPresent(uc, ucCols, "LAST_ACCESS_TIME", now);
                putIfPresent(uc, ucCols, "PERCENT_COMPLETE", 0.0);
                putIfPresent(uc, ucCols, "READING_PROGRESS", 0);
                putIfPresent(uc, ucCols, "IS_IN_CAROUSEL", 1);
                putIfPresent(uc, ucCols, "CLIPPING_BALANCE", 0);
                putIfPresent(uc, ucCols, "HAS_UNMIGRATED_ANNOTATION", 0);
                putIfPresent(uc, ucCols, "IS_READ", 0);
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

    private static Set<String> tableColumns(SQLiteDatabase db, String table) {
        Set<String> cols = new HashSet<>();
        Cursor c = null;
        try {
            c = db.rawQuery("PRAGMA table_info(" + table + ")", null);
            int nameIdx = c.getColumnIndex("name");
            while (c.moveToNext()) {
                if (nameIdx >= 0) cols.add(c.getString(nameIdx));
            }
        } catch (Throwable ignored) {
        } finally {
            if (c != null) c.close();
        }
        return cols;
    }

    private static void putIfPresent(ContentValues cv, Set<String> cols, String col, String val) {
        if (cols.contains(col) && val != null) cv.put(col, val);
    }

    private static void putIfPresent(ContentValues cv, Set<String> cols, String col, long val) {
        if (cols.contains(col)) cv.put(col, val);
    }

    private static void putIfPresent(ContentValues cv, Set<String> cols, String col, int val) {
        if (cols.contains(col)) cv.put(col, val);
    }

    private static void putIfPresent(ContentValues cv, Set<String> cols, String col, double val) {
        if (cols.contains(col)) cv.put(col, val);
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
