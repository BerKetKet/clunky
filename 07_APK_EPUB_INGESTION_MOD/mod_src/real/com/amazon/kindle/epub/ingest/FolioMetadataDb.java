package com.amazon.kindle.epub.ingest;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Persistent local metadata store (folio_metadata.db) recording rich EPUB
 * details (title, author, publisher, pub_date, genres, language, isbn,
 * description, open_library_key, app-private kfx_path, cover_path), duplicate
 * lookup, genre aggregation, and schema-safe registration into kindle_library.db.
 */
public final class FolioMetadataDb extends SQLiteOpenHelper {

    private static final String TAG = "FolioMetadataDb";
    private static final String DB_NAME = "folio_metadata.db";
    public static final int DB_VERSION = 3;
    private static final String PREF_MIGRATIONS = "folio_db_migrations";
    private static final String KEY_GENRES_CLEANUP_V1 = "genres_cleanup_v1_done";
    private static volatile boolean sGenresMigratedInProcess = false;

    private final Context appContext;

    public static final class GenreMigrationRow {
        public final long id;
        public final String author;
        public final String genres;
        public final String genresJson;

        public GenreMigrationRow(long id, String author, String genres, String genresJson) {
            this.id = id;
            this.author = author != null ? author : "";
            this.genres = genres != null ? genres : "";
            this.genresJson = genresJson != null ? genresJson : "";
        }
    }

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
        public String genresJson = "";
        public String language = "";
        public String isbn = "";
        public String description = "";
        public String openLibraryKey = "";
        public String contentHash = "";
        public long sourceSize;
        public long addedAt;
        public long enrichAttemptedAt;
        public String enrichStatus = "";

        public List<String> genreList() {
            if (genresJson != null && !genresJson.trim().isEmpty()) {
                List<String> parsed = EpubMetadataExtractor.parseGenresJson(genresJson);
                if (!parsed.isEmpty()) return parsed;
            }
            List<String> out = new ArrayList<>();
            if (genres != null && !genres.trim().isEmpty()) {
                for (String part : genres.split(",")) {
                    String g = part.trim();
                    if (!g.isEmpty()) out.add(g);
                }
            }
            return out;
        }

        public boolean hasGenre(String target) {
            if (target == null || target.isEmpty() || "All".equalsIgnoreCase(target)) return true;
            String needle = target.trim().toLowerCase(Locale.ROOT);
            for (String part : genreList()) {
                if (part.trim().toLowerCase(Locale.ROOT).equals(needle)) return true;
            }
            return false;
        }
    }

    public FolioMetadataDb(Context context) {
        super(context != null ? context.getApplicationContext() : null, DB_NAME, null, DB_VERSION);
        this.appContext = context != null ? context.getApplicationContext() : null;
    }

    private static final String SELECT_COLS =
            "id, book_key, kfx_path, cover_path, source_name, title, author, "
            + "publisher, pub_date, genres, language, isbn, description, open_library_key, "
            + "added_at, content_hash, source_size, genres_json, enrich_attempted_at, enrich_status";

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
                + "genres_json TEXT, "
                + "language TEXT, "
                + "isbn TEXT, "
                + "description TEXT, "
                + "open_library_key TEXT, "
                + "content_hash TEXT, "
                + "source_size INTEGER, "
                + "added_at INTEGER NOT NULL, "
                + "enrich_attempted_at INTEGER DEFAULT 0, "
                + "enrich_status TEXT DEFAULT ''"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            java.util.Map<String, String> cols = tableColumns(db, "books_metadata");
            if (!cols.containsKey("CONTENT_HASH")) {
                db.execSQL("ALTER TABLE books_metadata ADD COLUMN content_hash TEXT");
            }
            if (!cols.containsKey("SOURCE_SIZE")) {
                db.execSQL("ALTER TABLE books_metadata ADD COLUMN source_size INTEGER");
            }
            if (!cols.containsKey("GENRES_JSON")) {
                db.execSQL("ALTER TABLE books_metadata ADD COLUMN genres_json TEXT");
            }
            // Best-effort migration of legacy CSV genres -> genres_json
            Cursor c = null;
            try {
                c = db.rawQuery("SELECT id, genres, genres_json FROM books_metadata", null);
                while (c.moveToNext()) {
                    long id = c.getLong(0);
                    String csv = c.getString(1);
                    String existingJson = c.getString(2);
                    if ((existingJson == null || existingJson.trim().isEmpty())
                            && csv != null && !csv.trim().isEmpty()) {
                        String json = genresCsvToJson(csv);
                        if (!json.isEmpty()) {
                            ContentValues cv = new ContentValues();
                            cv.put("genres_json", json);
                            db.update("books_metadata", cv, "id = ?", new String[]{ String.valueOf(id) });
                        }
                    }
                }
            } catch (Throwable t) {
                Log.w(TAG, "onUpgrade v2 genres_json migration warning", t);
            } finally {
                if (c != null) c.close();
            }
        }
        if (oldVersion < 3) {
            java.util.Map<String, String> cols = tableColumns(db, "books_metadata");
            if (!cols.containsKey("ENRICH_ATTEMPTED_AT")) {
                db.execSQL("ALTER TABLE books_metadata ADD COLUMN enrich_attempted_at INTEGER DEFAULT 0");
            }
            if (!cols.containsKey("ENRICH_STATUS")) {
                db.execSQL("ALTER TABLE books_metadata ADD COLUMN enrich_status TEXT DEFAULT ''");
            }
        }
    }

    public static String genresCsvToJson(String csv) {
        if (csv == null || csv.trim().isEmpty()) return "";
        List<String> raw = new ArrayList<>();
        for (String part : csv.split(",")) {
            String g = part.trim();
            if (!g.isEmpty()) raw.add(g);
        }
        return EpubMetadataExtractor.genresToJson(
                EpubMetadataExtractor.cleanAndRankGenres(raw, null));
    }

    public static String dedupeGenresCsv(String csv) {
        if (csv == null || csv.trim().isEmpty()) return "";
        List<String> raw = new ArrayList<>();
        for (String part : csv.split(",")) {
            String g = part.trim();
            if (!g.isEmpty()) raw.add(g);
        }
        return EpubMetadataExtractor.joinGenresForDisplay(
                EpubMetadataExtractor.cleanAndRankGenres(raw, null));
    }

    private static List<String> repairLegacyCommaSplitFragments(List<String> tokens, String author) {
        if (tokens == null || tokens.isEmpty()) return new ArrayList<>();
        List<String> out = new ArrayList<>(tokens.size());
        int n = tokens.size();
        for (int i = 0; i < n; ) {
            String t0 = tokens.get(i) != null ? tokens.get(i).trim() : "";
            if (i + 2 < n) {
                String t1 = tokens.get(i + 1) != null ? tokens.get(i + 1).trim() : "";
                String t2 = tokens.get(i + 2) != null ? tokens.get(i + 2).trim() : "";
                if (EpubMetadataExtractor.isPureDateOrRange(t2)) {
                    String joined = t0 + ", " + t1 + ", " + t2;
                    if (EpubMetadataExtractor.isPersonalNameSubject(joined)) {
                        out.add(joined);
                        i += 3;
                        continue;
                    }
                }
            }
            if (i + 1 < n) {
                String t1 = tokens.get(i + 1) != null ? tokens.get(i + 1).trim() : "";
                if ("Authors".equalsIgnoreCase(t0) && "correspondence".equalsIgnoreCase(t1)) {
                    out.add("Authors, correspondence");
                    i += 2;
                    continue;
                }
                if (EpubMetadataExtractor.isPersonalNameSubject(t0 + ", " + t1)) {
                    out.add(t0 + ", " + t1);
                    i += 2;
                    continue;
                }
            }
            out.add(t0);
            i++;
        }
        return out;
    }

    /**
     * Pure Android-free genre migration pass across all library rows:
     * preserves comma-containing JSON entries, repairs legacy comma-split personal-name triples,
     * drops noise/personal names/date ranges, strips parentheticals, counts global library
     * frequencies, and caps each book at 6 genres preferring shorter and more frequent tags.
     */
    public static Map<Long, List<String>> computeMigratedGenres(List<GenreMigrationRow> rows) {
        Map<Long, List<String>> out = new LinkedHashMap<>();
        if (rows == null || rows.isEmpty()) return out;

        Map<Long, List<String>> repairedByRow = new LinkedHashMap<>(rows.size());
        Map<String, Integer> globalFreq = new HashMap<>();
        for (GenreMigrationRow row : rows) {
            List<String> raw;
            if (!row.genresJson.trim().isEmpty()) {
                raw = repairLegacyCommaSplitFragments(
                        EpubMetadataExtractor.parseGenresJson(row.genresJson), row.author);
            } else if (!row.genres.trim().isEmpty()) {
                List<String> csvParts = new ArrayList<>();
                for (String part : row.genres.split(",")) {
                    String g = part.trim();
                    if (!g.isEmpty()) csvParts.add(g);
                }
                raw = repairLegacyCommaSplitFragments(csvParts, row.author);
            } else {
                raw = new ArrayList<>();
            }
            repairedByRow.put(row.id, raw);
            List<String> cleanedAll = EpubMetadataExtractor.cleanAndRankGenres(
                    raw, null, row.author, Integer.MAX_VALUE);
            for (String g : cleanedAll) {
                String k = g.toLowerCase(Locale.ROOT);
                globalFreq.put(k, (globalFreq.containsKey(k) ? globalFreq.get(k) : 0) + 1);
            }
        }

        for (GenreMigrationRow row : rows) {
            List<String> repaired = repairedByRow.get(row.id);
            List<String> ranked = EpubMetadataExtractor.cleanAndRankGenres(
                    repaired, globalFreq, row.author);
            out.put(row.id, ranked);
        }
        return out;
    }

    /**
     * One-time idempotent migration cleaning existing genres and genres_json rows,
     * guarded by SharedPreferences key genres_cleanup_v1_done.
     */
    public void ensureGenresMigrated() {
        if (sGenresMigratedInProcess || appContext == null) return;
        synchronized (FolioMetadataDb.class) {
            if (sGenresMigratedInProcess) return;
            SharedPreferences sp = appContext.getSharedPreferences(PREF_MIGRATIONS, Context.MODE_PRIVATE);
            if (sp.getBoolean(KEY_GENRES_CLEANUP_V1, false)) {
                sGenresMigratedInProcess = true;
                return;
            }
            SQLiteDatabase db = getWritableDatabase();
            Cursor c = null;
            int total = 0;
            int updated = 0;
            try {
                java.util.Map<String, String> cols = tableColumns(db, "books_metadata");
                boolean hasGenresJsonCol = cols.containsKey("GENRES_JSON");
                String sql = hasGenresJsonCol
                        ? "SELECT id, author, genres, genres_json FROM books_metadata"
                        : "SELECT id, author, genres, '' FROM books_metadata";
                c = db.rawQuery(sql, null);
                List<GenreMigrationRow> rows = new ArrayList<>();
                while (c.moveToNext()) {
                    rows.add(new GenreMigrationRow(
                            c.getLong(0),
                            safeStr(c.getString(1)),
                            safeStr(c.getString(2)),
                            safeStr(c.getString(3))));
                }
                c.close();
                c = null;
                total = rows.size();
                Map<Long, List<String>> cleaned = computeMigratedGenres(rows);
                db.beginTransaction();
                try {
                    for (GenreMigrationRow row : rows) {
                        List<String> cleanList = cleaned.get(row.id);
                        String newCsv = EpubMetadataExtractor.joinGenresForDisplay(cleanList);
                        String newJson = EpubMetadataExtractor.genresToJson(cleanList);
                        boolean csvChanged = !newCsv.equals(row.genres);
                        boolean jsonChanged = hasGenresJsonCol && !newJson.equals(row.genresJson);
                        if (csvChanged || jsonChanged) {
                            ContentValues cv = new ContentValues();
                            cv.put("genres", newCsv);
                            if (hasGenresJsonCol) {
                                cv.put("genres_json", newJson);
                            }
                            db.update("books_metadata", cv, "id = ?",
                                    new String[] { String.valueOf(row.id) });
                            updated++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    if (db.inTransaction()) db.endTransaction();
                }
                sp.edit().putBoolean(KEY_GENRES_CLEANUP_V1, true).apply();
                sGenresMigratedInProcess = true;
                Log.i(TAG, "Genres cleanup migration v1 complete: total=" + total + " updated=" + updated);
            } catch (Throwable t) {
                Log.w(TAG, "Genres cleanup migration v1 failed", t);
            } finally {
                if (c != null) c.close();
            }
        }
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
                        "SELECT " + SELECT_COLS
                        + " FROM books_metadata WHERE book_key = ? OR kfx_path = ? LIMIT 1",
                        new String[] { bookKey != null ? bookKey : "", kfxPath });
                if (c.moveToFirst()) {
                    prior = readRow(c);
                }
            } finally {
                if (c != null) c.close();
            }

            String incomingCover = (coverFile != null && coverFile.exists()) ? coverFile.getAbsolutePath() : "";
            String coverPath = nonBlankOrPrior(incomingCover, prior != null ? prior.coverPath : "");
            String resolvedAuthor = (meta.author != null && !meta.author.trim().isEmpty())
                    ? meta.author : (prior != null && !prior.author.isEmpty() ? prior.author : "Unknown Author");
            List<String> rawGenres = (meta.genres != null && !meta.genres.isEmpty())
                    ? meta.genres
                    : (prior != null ? prior.genreList() : new ArrayList<String>());
            List<String> cleanGenres = EpubMetadataExtractor.cleanAndRankGenres(rawGenres, null, resolvedAuthor);
            String genres = EpubMetadataExtractor.joinGenresForDisplay(cleanGenres);
            String genresJson = EpubMetadataExtractor.genresToJson(cleanGenres);
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
            cv.put("author", resolvedAuthor);
            cv.put("publisher", publisher);
            cv.put("pub_date", pubDate);
            cv.put("genres", genres);
            if (cols.containsKey("GENRES_JSON")) {
                cv.put("genres_json", genresJson);
            }
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
                        "SELECT " + SELECT_COLS
                        + " FROM books_metadata "
                        + "WHERE content_hash = ? OR book_key = ? "
                        + "ORDER BY added_at DESC LIMIT 1",
                        new String[] { contentHash, bookKey != null ? bookKey : "" });
            } else {
                c = db.rawQuery(
                        "SELECT " + SELECT_COLS
                        + " FROM books_metadata "
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
                    "SELECT " + SELECT_COLS
                    + " FROM books_metadata "
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
                    "SELECT " + SELECT_COLS
                    + " FROM books_metadata ORDER BY added_at DESC", null);
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
                for (String g : r.genreList()) {
                    String clean = g.trim();
                    if (!clean.isEmpty() && clean.length() <= 32) {
                        set.add(clean);
                    }
                }
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
        if (c.getColumnCount() > 15) r.contentHash = safeStr(c.getString(15));
        if (c.getColumnCount() > 16 && !c.isNull(16)) r.sourceSize = c.getLong(16);
        if (c.getColumnCount() > 17) r.genresJson = safeStr(c.getString(17));
        if (c.getColumnCount() > 18 && !c.isNull(18)) r.enrichAttemptedAt = c.getLong(18);
        if (c.getColumnCount() > 19) r.enrichStatus = safeStr(c.getString(19));
        return r;
    }

    /** Records the latest Open Library enrichment timestamp & outcome ("matched", "no_match", or "error"). */
    public void recordEnrichAttempt(String bookKey, long attemptedAtMs, String status) {
        if (bookKey == null || bookKey.isEmpty()) return;
        try {
            SQLiteDatabase db = getWritableDatabase();
            java.util.Map<String, String> cols = tableColumns(db, "books_metadata");
            if (!cols.containsKey("ENRICH_ATTEMPTED_AT") || !cols.containsKey("ENRICH_STATUS")) {
                return;
            }
            ContentValues cv = new ContentValues();
            cv.put("enrich_attempted_at", attemptedAtMs);
            cv.put("enrich_status", status != null ? status : "");
            db.update("books_metadata", cv, "book_key = ?", new String[]{ bookKey });
        } catch (Throwable t) {
            Log.w(TAG, "recordEnrichAttempt warning for " + bookKey + ": " + t.getMessage());
        }
    }

    private static String safeStr(String s) {
        return s == null ? "" : s;
    }

    private static volatile boolean sBackedUpThisProcess = false;
    private static final Object BACKUP_LOCK = new Object();

    private static void copyFileAtomic(File src, File dst) throws Exception {
        File tmp = new File(dst.getParentFile(), dst.getName() + ".part");
        if (tmp.exists()) tmp.delete();
        boolean ok = false;
        try (FileInputStream fis = new FileInputStream(src);
             FileOutputStream fos = new FileOutputStream(tmp)) {
            byte[] buf = new byte[16384];
            int n;
            while ((n = fis.read(buf)) > 0) {
                fos.write(buf, 0, n);
            }
            fos.flush();
            ok = true;
        } finally {
            if (!ok && tmp.exists()) tmp.delete();
        }
        if (dst.exists()) dst.delete();
        if (!tmp.renameTo(dst)) {
            if (tmp.exists()) tmp.delete();
            throw new java.io.IOException("Atomic rename failed: " + tmp.getAbsolutePath() + " -> " + dst.getAbsolutePath());
        }
    }

    private static boolean backupKindleLibraryDbOnce(Context ctx, File dbFile) {
        if (ctx == null || dbFile == null || !dbFile.isFile()) return false;
        if (sBackedUpThisProcess) return true;
        synchronized (BACKUP_LOCK) {
            if (sBackedUpThisProcess) return true;
            try {
                File backupDir = new File(ctx.getFilesDir(), "db_backups");
                if (!backupDir.exists()) backupDir.mkdirs();
                if (!backupDir.isDirectory()) {
                    Log.e(TAG, "Backup directory is invalid: " + backupDir.getAbsolutePath());
                    return false;
                }
                File[] existing = backupDir.listFiles((dir, name) ->
                        name.startsWith("kindle_library_") && name.endsWith(".db"));
                long now = System.currentTimeMillis();
                if (existing != null && existing.length > 0) {
                    Arrays.sort(existing, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
                    if (now - existing[0].lastModified() < 5 * 60_000L
                            && existing[0].isFile() && existing[0].length() > 0) {
                        sBackedUpThisProcess = true;
                        Log.i(TAG, "Verified recent kindle_library.db backup (< 5m): " + existing[0].getName());
                        return true;
                    }
                }

                File dest = new File(backupDir, "kindle_library_" + now + ".db");
                File destPart = new File(backupDir, "kindle_library_" + now + ".db.part");
                if (destPart.exists()) destPart.delete();

                boolean vacuumOk = false;
                SQLiteDatabase srcDb = null;
                try {
                    srcDb = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
                    String escaped = destPart.getAbsolutePath().replace("'", "''");
                    srcDb.execSQL("VACUUM INTO '" + escaped + "'");
                    vacuumOk = destPart.isFile() && destPart.length() > 0;
                } catch (Throwable vt) {
                    Log.w(TAG, "VACUUM INTO failed (" + vt.getMessage() + "), falling back to atomic DB+WAL/SHM copy");
                    if (destPart.exists()) destPart.delete();
                } finally {
                    if (srcDb != null) {
                        try { srcDb.close(); } catch (Throwable ignored) {}
                    }
                }

                if (vacuumOk) {
                    if (dest.exists()) dest.delete();
                    if (!destPart.renameTo(dest)) {
                        if (destPart.exists()) destPart.delete();
                        Log.e(TAG, "Failed atomic rename of VACUUM INTO backup");
                        return false;
                    }
                } else {
                    copyFileAtomic(dbFile, dest);
                    File walSrc = new File(dbFile.getAbsolutePath() + "-wal");
                    if (walSrc.isFile()) {
                        copyFileAtomic(walSrc, new File(dest.getAbsolutePath() + "-wal"));
                    }
                    File shmSrc = new File(dbFile.getAbsolutePath() + "-shm");
                    if (shmSrc.isFile()) {
                        copyFileAtomic(shmSrc, new File(dest.getAbsolutePath() + "-shm"));
                    }
                }

                if (!dest.isFile() || dest.length() <= 0) {
                    Log.e(TAG, "Backup verification failed (missing or empty): " + dest.getAbsolutePath());
                    return false;
                }

                sBackedUpThisProcess = true;
                Log.i(TAG, "Backed up kindle_library.db (" + (vacuumOk ? "VACUUM INTO" : "atomic+WAL")
                        + ", " + dest.length() + " B) -> " + dest.getAbsolutePath());

                File[] after = backupDir.listFiles((dir, name) ->
                        name.startsWith("kindle_library_") && name.endsWith(".db"));
                if (after != null && after.length > 3) {
                    Arrays.sort(after, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
                    for (int i = 3; i < after.length; i++) {
                        File old = after[i];
                        new File(old.getAbsolutePath() + "-wal").delete();
                        new File(old.getAbsolutePath() + "-shm").delete();
                        if (old.delete()) {
                            Log.i(TAG, "Pruned old backup: " + old.getName());
                        }
                    }
                }
                return true;
            } catch (Throwable t) {
                Log.e(TAG, "Failed to backup kindle_library.db", t);
                return false;
            }
        }
    }

    private static boolean isLockOrBusy(Throwable t) {
        if (t instanceof SQLiteDatabaseLockedException) return true;
        String msg = t.getMessage();
        if (msg == null) return false;
        String lower = msg.toLowerCase(Locale.ROOT);
        return lower.contains("locked") || lower.contains("busy");
    }

    private static final Pattern ISO_DATE_PREFIX =
            Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2})(?:$|[T\\s].*)");
    private static final Pattern YEAR_ONLY = Pattern.compile("^(\\d{4})$");

    /**
     * Pure, Android-free publication date parser returning UTC epoch milliseconds
     * for ISO YYYY-MM-DD (including ISO timestamps) or 4-digit YYYY, or null if unparseable.
     */
    public static Long parsePubDateToEpochMs(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;
        try {
            Matcher mIso = ISO_DATE_PREFIX.matcher(s);
            int year;
            int month;
            int day;
            if (mIso.matches()) {
                year = Integer.parseInt(mIso.group(1));
                month = Integer.parseInt(mIso.group(2));
                day = Integer.parseInt(mIso.group(3));
            } else {
                Matcher mYear = YEAR_ONLY.matcher(s);
                if (!mYear.matches()) return null;
                year = Integer.parseInt(mYear.group(1));
                month = 1;
                day = 1;
            }
            if (year < 1000 || year > 2099) return null;
            Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
            cal.setLenient(false);
            cal.clear();
            cal.set(year, month - 1, day, 0, 0, 0);
            return Long.valueOf(cal.getTimeInMillis());
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static String toSortableText(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
    }

    public static final class OrderRow {
        public final String id;
        public final String sortKey;
        public final long orderValue;

        public OrderRow(String id, String sortKey, long orderValue) {
            this.id = id != null ? id : "";
            this.sortKey = toSortableText(sortKey);
            this.orderValue = orderValue;
        }
    }

    public static final class OrderPlan {
        public final long assignedOrder;
        public final Map<String, Long> updatedNeighborOrders;

        public OrderPlan(long assignedOrder, Map<String, Long> updatedNeighborOrders) {
            this.assignedOrder = assignedOrder;
            this.updatedNeighborOrders = updatedNeighborOrders != null
                    ? updatedNeighborOrders : Collections.<String, Long>emptyMap();
        }
    }

    /**
     * Pure Android-free order-key allocator guaranteeing strictly increasing, unique
     * TITLE_ORDER / AUTHOR_ORDER / AUTHOR_ORDER_REVERSED values across consecutive inserts
     * between the same neighbors (midpoint fast-path with local neighborhood re-spread when
     * next - prev <= 1 or on collision).
     */
    public static OrderPlan planOrderInsertion(
            List<OrderRow> existingRows, String rawSortKey, final boolean reversed) {
        final String targetKey = toSortableText(rawSortKey);
        final long defaultBase = 1_000_000_000_000L;
        if (existingRows == null || existingRows.isEmpty()) {
            return new OrderPlan(defaultBase, Collections.<String, Long>emptyMap());
        }

        List<OrderRow> rows = new ArrayList<>(existingRows.size());
        Set<Long> used = new HashSet<>();
        for (OrderRow r : existingRows) {
            if (r != null && !r.id.isEmpty() && r.orderValue > 0L) {
                rows.add(r);
                used.add(r.orderValue);
            }
        }
        if (rows.isEmpty()) {
            return new OrderPlan(defaultBase, Collections.<String, Long>emptyMap());
        }

        Collections.sort(rows, new Comparator<OrderRow>() {
            @Override
            public int compare(OrderRow a, OrderRow b) {
                int c = Long.compare(a.orderValue, b.orderValue);
                if (c != 0) return c;
                int ks = a.sortKey.compareTo(b.sortKey);
                if (reversed) ks = -ks;
                if (ks != 0) return ks;
                return a.id.compareTo(b.id);
            }
        });

        int n = rows.size();
        int insIdx = 0;
        for (int i = 0; i < n; i++) {
            String rowSort = rows.get(i).sortKey;
            if (rowSort.isEmpty()) {
                insIdx = i + 1;
                continue;
            }
            int cmp = rowSort.compareTo(targetKey);
            if (reversed) cmp = -cmp;
            if (cmp <= 0) {
                insIdx = i + 1;
            } else {
                break;
            }
        }

        if (insIdx > 0 && insIdx < n) {
            long prev = rows.get(insIdx - 1).orderValue;
            long next = rows.get(insIdx).orderValue;
            if (next > prev + 1L) {
                long mid = prev + ((next - prev) >>> 1);
                if (mid > prev && mid < next && !used.contains(mid)) {
                    return new OrderPlan(mid, Collections.<String, Long>emptyMap());
                }
            }
        } else if (insIdx == 0) {
            long next = rows.get(0).orderValue;
            if (next > 1L) {
                long cand = next >>> 1;
                if (cand > 0L && cand < next && !used.contains(cand)) {
                    return new OrderPlan(cand, Collections.<String, Long>emptyMap());
                }
            }
        } else {
            long prev = rows.get(n - 1).orderValue;
            if (prev < Long.MAX_VALUE - 1L) {
                long cand = prev + ((Long.MAX_VALUE - prev) >>> 1);
                if (cand > prev && !used.contains(cand)) {
                    return new OrderPlan(cand, Collections.<String, Long>emptyMap());
                }
            }
        }

        // Tight-gap / boundary / collision fallback: re-spread minimal local window [L..R]
        int L = Math.max(0, insIdx - 1);
        int R = Math.min(n - 1, insIdx);
        if (insIdx == 0) { L = 0; R = 0; }
        else if (insIdx == n) { L = n - 1; R = n - 1; }

        final long targetPerSlotGap = 64L;
        long lowBound;
        long highBound;
        int slots;
        boolean expandRightNext = true;
        while (true) {
            lowBound = (L == 0) ? 0L : rows.get(L - 1).orderValue;
            if (R == n - 1) {
                BigInteger tailBase = BigInteger.valueOf(Math.max(rows.get(n - 1).orderValue, lowBound));
                BigInteger grow = tailBase.add(BigInteger.valueOf(1_000_000L * (long) (R - L + 4)));
                BigInteger maxCap = BigInteger.valueOf(Long.MAX_VALUE);
                highBound = (grow.compareTo(maxCap) > 0 ? maxCap : grow).longValue();
            } else {
                highBound = rows.get(R + 1).orderValue;
            }
            slots = (R - L + 1) + 1;
            BigInteger span = BigInteger.valueOf(highBound).subtract(BigInteger.valueOf(lowBound));
            BigInteger desired = BigInteger.valueOf((long) (slots + 1) * targetPerSlotGap);
            BigInteger minimum = BigInteger.valueOf(slots + 1L);
            if (span.compareTo(desired) >= 0) break;
            if (L == 0 && R == n - 1) {
                if (span.compareTo(minimum) >= 0) break;
                highBound = Long.MAX_VALUE;
                break;
            }
            if (expandRightNext && R < n - 1) {
                R++;
            } else if (!expandRightNext && L > 0) {
                L--;
            } else if (R < n - 1) {
                R++;
            } else if (L > 0) {
                L--;
            }
            expandRightNext = !expandRightNext;
        }

        BigInteger lo = BigInteger.valueOf(lowBound);
        BigInteger sp = BigInteger.valueOf(highBound).subtract(lo);
        BigInteger denom = BigInteger.valueOf(slots + 1L);
        long assigned = -1L;
        Map<String, Long> updates = new LinkedHashMap<>();
        int slotIdx = 1;
        for (int j = L; j <= R; j++) {
            if (j == insIdx) {
                assigned = lo.add(sp.multiply(BigInteger.valueOf(slotIdx++)).divide(denom)).longValue();
            }
            long newVal = lo.add(sp.multiply(BigInteger.valueOf(slotIdx++)).divide(denom)).longValue();
            OrderRow orig = rows.get(j);
            if (newVal != orig.orderValue) {
                updates.put(orig.id, newVal);
            }
        }
        if (insIdx == R + 1) {
            assigned = lo.add(sp.multiply(BigInteger.valueOf(slotIdx)).divide(denom)).longValue();
        }
        return new OrderPlan(assigned, updates);
    }

    private static long computeOrderPosition(
            SQLiteDatabase db,
            java.util.Map<String, String> cols,
            String excludeId,
            String sortCol,
            String orderCol,
            String sortKey,
            boolean reversed) {
        if (db == null || cols == null || sortKey == null || sortKey.isEmpty()) return -1L;
        String actualId = cols.get("ID");
        String actualSort = cols.get(sortCol.toUpperCase(Locale.ROOT));
        String actualOrder = cols.get(orderCol.toUpperCase(Locale.ROOT));
        if (actualId == null || actualSort == null || actualOrder == null) return -1L;

        List<OrderRow> rows = new ArrayList<>();
        Cursor c = null;
        try {
            c = db.rawQuery(
                    "SELECT " + actualId + ", " + actualSort + ", " + actualOrder + " FROM KindleContent "
                            + "WHERE " + actualId + " != ? AND " + actualOrder + " > 0 AND " + actualSort + " IS NOT NULL "
                            + "ORDER BY " + actualOrder + " ASC",
                    new String[] { excludeId });
            while (c.moveToNext()) {
                String id = safeStr(c.getString(0));
                String rowSort = toSortableText(c.getString(1));
                long rowOrder = c.getLong(2);
                if (!id.isEmpty() && !rowSort.isEmpty() && rowOrder > 0L) {
                    rows.add(new OrderRow(id, rowSort, rowOrder));
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "computeOrderPosition(" + orderCol + ") warning: " + t.getMessage());
            return -1L;
        } finally {
            if (c != null) c.close();
        }

        OrderPlan plan = planOrderInsertion(rows, sortKey, reversed);
        if (!plan.updatedNeighborOrders.isEmpty()) {
            for (Map.Entry<String, Long> upd : plan.updatedNeighborOrders.entrySet()) {
                ContentValues cvUpd = new ContentValues();
                cvUpd.put(actualOrder, upd.getValue().longValue());
                db.update("KindleContent", cvUpd, actualId + " = ?", new String[] { upd.getKey() });
            }
        }
        return plan.assignedOrder;
    }

    /**
     * Schema-adaptive transactional registration into kindle_library.db aligned
     * with real M30 PRAGMA table_info(KindleContent/LocalContent/UserContent) columns.
     */
    public static boolean registerInKindleLibraryDb(
            Context ctx, String bookKey, File kfxFile, File coverFile,
            EpubMetadataExtractor.BookMeta meta) {
        File dbFile = ctx.getDatabasePath("kindle_library.db");
        if (dbFile == null || !dbFile.exists()) {
            Log.w(TAG, "kindle_library.db not found yet at " + dbFile);
            return false;
        }
        if (!backupKindleLibraryDbOnce(ctx, dbFile)) {
            Log.e(TAG, "Aborting registerInKindleLibraryDb: kindle_library.db backup failed");
            return false;
        }

        final int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            SQLiteDatabase db = null;
            try {
                db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
                db.beginTransaction();
                long now = System.currentTimeMillis();
                String path = kfxFile.getAbsolutePath();
                String coverPath = (coverFile != null && coverFile.exists()) ? coverFile.getAbsolutePath() : "";
                String title = !EpubMetadataExtractor.isBlank(meta.title) ? meta.title.trim() : kfxFile.getName();
                String author = !EpubMetadataExtractor.isBlank(meta.author) ? meta.author.trim() : "Unknown Author";
                String sortableTitle = toSortableText(title);
                String sortableAuthor = toSortableText(author);
                String asin = "CR!" + bookKey.replace("FOLIO_", "") + "KFX";
                String amznId = "AMZNID0/" + asin + "/0/";
                int hasCover = coverPath.isEmpty() ? 0 : 1;

                // Remove any prior row pointing to the exact same path to prevent duplicates
                try {
                    db.delete("LocalContent", "FILE_PATH = ?", new String[] { path });
                } catch (Throwable t) {
                    if (isLockOrBusy(t)) throw t;
                    Log.w(TAG, "LocalContent pre-delete warning", t);
                }

                // 1. KindleContent (aligned to real M30 PRAGMA table_info columns)
                java.util.Map<String, String> kcCols = tableColumns(db, "KindleContent");
                if (!kcCols.isEmpty()) {
                    Long pubDateEpochMs = parsePubDateToEpochMs(meta != null ? meta.pubDate : null);
                    long titleOrder = computeOrderPosition(db, kcCols, amznId, "SORTABLE_TITLE", "TITLE_ORDER", sortableTitle, false);
                    long authorOrder = computeOrderPosition(db, kcCols, amznId, "SORTABLE_AUTHOR", "AUTHOR_ORDER", sortableAuthor, false);
                    long authorOrderRev = computeOrderPosition(db, kcCols, amznId, "SORTABLE_AUTHOR", "AUTHOR_ORDER_REVERSED", sortableAuthor, true);

                    ContentValues kc = new ContentValues();
                    putIfPresent(kc, kcCols, "ID", amznId);
                    putIfPresent(kc, kcCols, "STATE", "LOCAL");
                    putIfPresent(kc, kcCols, "ARCHIVABLE", 0);
                    putIfPresent(kc, kcCols, "HAS_READ_ALONG", 0);
                    putIfPresent(kc, kcCols, "TYPE", "BT_EBOOK_PDOC");
                    putIfPresent(kc, kcCols, "KEPT", 0);
                    putIfPresent(kc, kcCols, "CONTENT_TYPE", "application/x-kfx-ebook");
                    putIfPresent(kc, kcCols, "TITLE", title);
                    putIfPresent(kc, kcCols, "AUTHOR", author);
                    if (pubDateEpochMs != null) {
                        putIfPresent(kc, kcCols, "PUBLICATION_DATE", pubDateEpochMs.longValue());
                    }
                    if (meta != null && !EpubMetadataExtractor.isBlank(meta.publisher)) {
                        putIfPresent(kc, kcCols, "PUBLISHER", meta.publisher.trim());
                    }
                    putIfPresent(kc, kcCols, "HAS_XRAY", 0);
                    if (meta != null && !EpubMetadataExtractor.isBlank(meta.language)) {
                        putIfPresent(kc, kcCols, "LANGUAGE", meta.language.trim());
                    }
                    putIfPresent(kc, kcCols, "SORTABLE_TITLE", sortableTitle);
                    putIfPresent(kc, kcCols, "SORTABLE_AUTHOR", sortableAuthor);
                    putIfPresent(kc, kcCols, "DICTIONARY_TYPE", "NONE");
                    putIfPresent(kc, kcCols, "DELIVERY_DATE", now);
                    if (titleOrder > 0L) {
                        putIfPresent(kc, kcCols, "TITLE_ORDER", titleOrder);
                    }
                    if (authorOrder > 0L) {
                        putIfPresent(kc, kcCols, "AUTHOR_ORDER", authorOrder);
                    }
                    if (authorOrderRev > 0L) {
                        putIfPresent(kc, kcCols, "AUTHOR_ORDER_REVERSED", authorOrderRev);
                    }
                    putIfPresent(kc, kcCols, "IS_COMIC", 0);
                    putIfPresent(kc, kcCols, "READ_STATE", "UNREAD");
                    putIfPresent(kc, kcCols, "READ_STATE_ORIGIN", "OTHER");
                    putIfPresent(kc, kcCols, "IS_FALKOR_EPISODE", 0);
                    putIfPresent(kc, kcCols, "HAS_LOADED_COVER", hasCover);
                    putIfPresent(kc, kcCols, "IS_HIDDEN", 0);
                    putIfPresent(kc, kcCols, "IS_HIDDEN_BY_USER", 0);
                    putIfPresent(kc, kcCols, "IS_WEBCOMIC", 0);
                    if (!fillMissingNotNullDefaults(db, "KindleContent", kc)) {
                        return false;
                    }
                    db.insertWithOnConflict("KindleContent", null, kc, SQLiteDatabase.CONFLICT_REPLACE);
                }

                // 2. LocalContent
                java.util.Map<String, String> lcCols = tableColumns(db, "LocalContent");
                if (!lcCols.isEmpty()) {
                    ContentValues lc = new ContentValues();
                    putIfPresent(lc, lcCols, "KEY", amznId);
                    putIfPresent(lc, lcCols, "FILE_PATH", path);
                    putIfPresent(lc, lcCols, "GUID", asin);
                    putIfPresent(lc, lcCols, "FILE_SIZE", kfxFile.length());
                    putIfPresent(lc, lcCols, "LAST_MODIFIED", now);
                    putIfPresent(lc, lcCols, "OWNERSHIP_TYPE", "Unknown");
                    if (!fillMissingNotNullDefaults(db, "LocalContent", lc)) {
                        return false;
                    }
                    db.insertWithOnConflict("LocalContent", null, lc, SQLiteDatabase.CONFLICT_REPLACE);
                }

                // 3. UserContent
                java.util.Map<String, String> ucCols = tableColumns(db, "UserContent");
                if (!ucCols.isEmpty()) {
                    ContentValues uc = new ContentValues();
                    putIfPresent(uc, ucCols, "KEY", amznId);
                    putIfPresent(uc, ucCols, "USER_ID", "MASTER");
                    putIfPresent(uc, ucCols, "LAST_ACCESSED", now);
                    putIfPresent(uc, ucCols, "IS_IN_CAROUSEL", 1);
                    putIfPresent(uc, ucCols, "CLIPPING_BALANCE", 0);
                    putIfPresent(uc, ucCols, "READING_PROGRESS", 0);
                    if (!fillMissingNotNullDefaults(db, "UserContent", uc)) {
                        return false;
                    }
                    db.insertWithOnConflict("UserContent", null, uc, SQLiteDatabase.CONFLICT_REPLACE);
                }

                db.setTransactionSuccessful();
                return true;
            } catch (Throwable t) {
                if (isLockOrBusy(t) && attempt < maxAttempts) {
                    Log.w(TAG, "kindle_library.db locked/busy (attempt " + attempt + "/" + maxAttempts
                            + "), retrying in 200ms: " + t.getMessage());
                    try { Thread.sleep(200L); } catch (InterruptedException ignored) {}
                    continue;
                }
                Log.e(TAG, "Failed to register book in kindle_library.db (attempt " + attempt + ")", t);
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
        return false;
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

    private static boolean fillMissingNotNullDefaults(SQLiteDatabase db, String table, ContentValues cv) {
        Cursor c = null;
        int forgedCount = 0;
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
                    forgedCount++;
                    Log.w(TAG, "fillMissingNotNullDefaults forging column " + table + "." + n + " (#" + forgedCount + ")");
                    if (forgedCount > 3) {
                        Log.e(TAG, "Schema drift detected in " + table + ": >3 NOT NULL columns would be forged; aborting write");
                        return false;
                    }
                    String tp = typeIdx >= 0 && c.getString(typeIdx) != null
                            ? c.getString(typeIdx).toUpperCase(Locale.ROOT) : "";
                    if (tp.contains("INT") || tp.contains("REAL") || tp.contains("NUM") || tp.contains("BOOL")) {
                        cv.put(n, 0);
                    } else {
                        cv.put(n, "");
                    }
                }
            }
            return true;
        } catch (Throwable t) {
            Log.e(TAG, "fillMissingNotNullDefaults failed for " + table, t);
            return false;
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
     * Deletes a single book metadata row by book_key and returns true only when >=1 row was deleted.
     */
    public boolean deleteBook(String bookKey) {
        if (bookKey == null || bookKey.isEmpty()) return false;
        try {
            int deleted = getWritableDatabase().delete("books_metadata", "book_key = ?", new String[]{ bookKey });
            return deleted > 0;
        } catch (Throwable t) {
            Log.e(TAG, "deleteBook failed for book_key=" + bookKey, t);
            return false;
        }
    }

    /**
     * Removes legacy synthetic AMZNID0/CR!...KFX/0/ rows written prior to Round 158
     * so books in /sdcard/kindle/ appear exactly once (indexed by Kindle's native scanner).
     * Verified against M30 kindle_library.db PRAGMA table_info: FILE_PATH lives on LocalContent
     * (cid=1, not KindleContent or UserContent), while deletions key on KindleContent.ID,
     * LocalContent.KEY, and UserContent.KEY.
     */
    public static int cleanupLegacyDuplicateRows(Context ctx) {
        if (ctx == null) return 0;
        File dbFile = ctx.getDatabasePath("kindle_library.db");
        if (dbFile == null || !dbFile.exists()) return 0;
        SQLiteDatabase db = null;
        int removed = 0;
        try {
            db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
            db.beginTransaction();

            List<String> keysList = new ArrayList<>();
            Cursor lcCur = null;
            try {
                lcCur = db.rawQuery(
                        "SELECT KEY FROM LocalContent WHERE KEY LIKE 'AMZNID0/CR!%KFX/0/' AND FILE_PATH LIKE '/sdcard/kindle/%'",
                        null);
                while (lcCur.moveToNext()) {
                    String k = lcCur.getString(0);
                    if (k != null && !k.isEmpty()) keysList.add(k);
                }
            } catch (Throwable t) {
                Log.w(TAG, "cleanupLegacyDuplicateRows LocalContent query failed", t);
            } finally {
                if (lcCur != null) lcCur.close();
            }

            if (!keysList.isEmpty()) {
                final int chunkSize = 450;
                for (int start = 0; start < keysList.size(); start += chunkSize) {
                    int end = Math.min(start + chunkSize, keysList.size());
                    List<String> sub = keysList.subList(start, end);
                    StringBuilder placeholders = new StringBuilder();
                    for (int i = 0; i < sub.size(); i++) {
                        if (i > 0) placeholders.append(',');
                        placeholders.append('?');
                    }
                    String[] args = sub.toArray(new String[0]);
                    try {
                        removed += db.delete("KindleContent",
                                "ID IN (" + placeholders + ")", args);
                    } catch (Throwable t) {
                        Log.w(TAG, "cleanupLegacyDuplicateRows KindleContent delete failed", t);
                    }
                    try {
                        removed += db.delete("LocalContent",
                                "KEY IN (" + placeholders + ")", args);
                    } catch (Throwable t) {
                        Log.w(TAG, "cleanupLegacyDuplicateRows LocalContent delete failed", t);
                    }
                    try {
                        removed += db.delete("UserContent",
                                "KEY IN (" + placeholders + ")", args);
                    } catch (Throwable t) {
                        Log.w(TAG, "cleanupLegacyDuplicateRows UserContent delete failed", t);
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
            if (suffix.startsWith("FOLIO_") && suffix.length() >= 12) {
                String hex = suffix.substring(6);
                if (hex.length() >= 6 && hex.length() <= 32) {
                    for (int i = 0; i < hex.length(); i++) {
                        char c = hex.charAt(i);
                        boolean isHex = (c >= '0' && c <= '9')
                                || (c >= 'a' && c <= 'f')
                                || (c >= 'A' && c <= 'F');
                        if (!isHex) return null;
                    }
                    return "FOLIO_" + hex.toUpperCase(Locale.ROOT);
                }
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
