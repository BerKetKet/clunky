package com.amazon.kindle.epub.ingest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Backfills folio_metadata.db from all existing local books in kindle_library.db,
 * enriching missing author, pub_date, publisher, genres, ISBN, and missing cover
 * thumbnails (local sidecars + Open Library Covers API fallback) on a background thread.
 */
public final class LibraryMetadataBackfiller {

    private static final String TAG = "FolioBackfiller";

    public interface ProgressListener {
        void onProgress(int done, int total, String currentTitle);
        void onFinished(int newlyAdded, int totalInDb);
    }

    private static final class Candidate {
        String title;
        String author;
        String filePath;
    }

    private LibraryMetadataBackfiller() {}

    public static void syncAllAsync(final Context ctx, final ProgressListener listener) {
        new Thread(() -> {
            int added = 0;
            int totalInDb = 0;
            try {
                List<Candidate> candidates = readKindleLibraryBooks(ctx);
                FolioMetadataDb metaDb = new FolioMetadataDb(ctx);
                try {
                    List<FolioMetadataDb.BookRecord> existingRecords = metaDb.getAllBooks();
                    Set<String> existingPaths = new HashSet<>();
                    for (FolioMetadataDb.BookRecord r : existingRecords) {
                        if (r.kfxPath != null) existingPaths.add(r.kfxPath);
                    }

                    // Pass 1: Backfill any new books not yet in folio_metadata.db
                    List<Candidate> todo = new ArrayList<>();
                    for (Candidate c : candidates) {
                        if (c.filePath != null && !existingPaths.contains(c.filePath)) {
                            File f = new File(c.filePath);
                            if (f.exists() && f.length() > 128) {
                                todo.add(c);
                            }
                        }
                    }

                    // Pass 2: Collect existing records missing a valid cover thumbnail
                    List<FolioMetadataDb.BookRecord> missingCovers = new ArrayList<>();
                    for (FolioMetadataDb.BookRecord r : existingRecords) {
                        if (r.coverPath == null || r.coverPath.trim().isEmpty()
                                || !new File(r.coverPath).exists()) {
                            missingCovers.add(r);
                        }
                    }

                    // Pass 3: Collect existing records missing genres or pub_date
                    List<FolioMetadataDb.BookRecord> missingMeta = new ArrayList<>();
                    for (FolioMetadataDb.BookRecord r : existingRecords) {
                        boolean noGenres = r.genres == null || r.genres.trim().isEmpty();
                        boolean noDate = r.pubDate == null || r.pubDate.trim().isEmpty();
                        if (noGenres || noDate) {
                            missingMeta.add(r);
                        }
                    }

                    int totalWork = todo.size() + missingCovers.size() + missingMeta.size();
                    int done = 0;

                    for (Candidate c : todo) {
                        done++;
                        if (listener != null) {
                            listener.onProgress(done, totalWork, c.title);
                        }
                        File kfxFile = new File(c.filePath);

                        EpubMetadataExtractor.BookMeta meta = new EpubMetadataExtractor.BookMeta();
                        meta.title = cleanTitleForLookup(c.title);
                        String originalAuthor = (c.author != null && !c.author.trim().isEmpty())
                                ? c.author.trim() : "Unknown Author";
                        meta.author = cleanAuthorForLookup(originalAuthor, meta.title);

                        MetadataEnricher.enrichInPlace(meta);

                        if (meta.author == null || meta.author.isEmpty()
                                || "Unknown Author".equalsIgnoreCase(meta.author)) {
                            meta.author = originalAuthor;
                        }
                        if (c.title != null && !c.title.trim().isEmpty()) {
                            meta.title = c.title.trim();
                        }

                        String bookKey = FolioMetadataDb.computeBookKey(meta.title, kfxFile.length());
                        File coverFile = resolveOrFetchCover(ctx, kfxFile, bookKey, meta.isbn);
                        metaDb.upsertBook(bookKey, kfxFile, coverFile, kfxFile.getName(), meta);
                        added++;
                    }

                    for (FolioMetadataDb.BookRecord r : missingCovers) {
                        done++;
                        if (listener != null) {
                            listener.onProgress(done, totalWork, r.title);
                        }
                        File kfxFile = new File(r.kfxPath);
                        File repaired = resolveOrFetchCover(ctx, kfxFile, r.bookKey, r.isbn);
                        if (repaired != null && repaired.exists() && repaired.length() > 512) {
                            ContentValues cv = new ContentValues();
                            cv.put("cover_path", repaired.getAbsolutePath());
                            metaDb.getWritableDatabase().update(
                                    "books_metadata", cv, "id = ?", new String[] { String.valueOf(r.id) });
                            added++;
                        }
                    }

                    for (FolioMetadataDb.BookRecord r : missingMeta) {
                        done++;
                        if (listener != null) {
                            listener.onProgress(done, totalWork, r.title);
                        }
                        EpubMetadataExtractor.BookMeta meta = new EpubMetadataExtractor.BookMeta();
                        meta.title = cleanTitleForLookup(r.title);
                        meta.author = cleanAuthorForLookup(r.author, meta.title);
                        meta.isbn = r.isbn != null ? r.isbn : "";
                        meta.pubDate = r.pubDate != null ? r.pubDate : "";
                        meta.publisher = r.publisher != null ? r.publisher : "";
                        meta.openLibraryKey = r.openLibraryKey != null ? r.openLibraryKey : "";
                        meta.genres.addAll(r.genreList());

                        MetadataEnricher.enrichInPlace(meta);

                        ContentValues cv = new ContentValues();
                        boolean changed = false;
                        if (r.genreList().isEmpty() && !meta.genres.isEmpty()) {
                            java.util.List<String> cleanedGenres = EpubMetadataExtractor.cleanAndRankGenres(
                                    meta.genres, null, meta.author);
                            cv.put("genres", EpubMetadataExtractor.joinGenresForDisplay(cleanedGenres));
                            cv.put("genres_json", EpubMetadataExtractor.genresToJson(cleanedGenres));
                            changed = true;
                        }
                        if ((r.pubDate == null || r.pubDate.trim().isEmpty()) && !EpubMetadataExtractor.isBlank(meta.pubDate)) {
                            cv.put("pub_date", EpubMetadataExtractor.normalizePubDate(meta.pubDate));
                            changed = true;
                        }
                        if ((r.publisher == null || r.publisher.trim().isEmpty()) && !EpubMetadataExtractor.isBlank(meta.publisher)) {
                            cv.put("publisher", meta.publisher);
                            changed = true;
                        }
                        if ((r.isbn == null || r.isbn.trim().isEmpty()) && !EpubMetadataExtractor.isBlank(meta.isbn)) {
                            cv.put("isbn", meta.isbn);
                            changed = true;
                        }
                        if ((r.openLibraryKey == null || r.openLibraryKey.trim().isEmpty()) && !EpubMetadataExtractor.isBlank(meta.openLibraryKey)) {
                            cv.put("open_library_key", meta.openLibraryKey);
                            changed = true;
                        }
                        if (changed) {
                            metaDb.getWritableDatabase().update(
                                    "books_metadata", cv, "id = ?", new String[] { String.valueOf(r.id) });
                            added++;
                        }
                    }

                    totalInDb = metaDb.getAllBooks().size();
                } finally {
                    metaDb.close();
                }
            } catch (Throwable t) {
                Log.e(TAG, "Backfill failed", t);
            }
            if (listener != null) {
                listener.onFinished(added, totalInDb);
            }
        }, "folio-meta-backfill").start();
    }

    private static List<Candidate> readKindleLibraryBooks(Context ctx) {
        List<Candidate> out = new ArrayList<>();
        File dbFile = ctx.getDatabasePath("kindle_library.db");
        if (dbFile == null || !dbFile.exists()) return out;

        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            String sql = "SELECT k.TITLE, k.AUTHOR, l.FILE_PATH "
                    + "FROM KindleContent k "
                    + "INNER JOIN LocalContent l ON l.KEY = k.ID "
                    + "WHERE l.FILE_PATH IS NOT NULL AND l.FILE_PATH != '' "
                    + "AND l.FILE_PATH NOT LIKE '%/dictionaries/%' "
                    + "GROUP BY l.FILE_PATH";
            c = db.rawQuery(sql, null);
            while (c.moveToNext()) {
                Candidate item = new Candidate();
                item.title = c.getString(0);
                item.author = c.getString(1);
                item.filePath = c.getString(2);
                if (item.title != null && !item.title.trim().isEmpty()) {
                    out.add(item);
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Primary join failed, trying fallback query", t);
            try {
                if (c != null) c.close();
                if (db != null) {
                    c = db.rawQuery("SELECT FILE_PATH FROM LocalContent WHERE FILE_PATH IS NOT NULL", null);
                    while (c.moveToNext()) {
                        String fp = c.getString(0);
                        if (fp != null && !fp.isEmpty()) {
                            Candidate item = new Candidate();
                            item.filePath = fp;
                            item.title = EpubMetadataExtractor.cleanTitleFromFilename(fp);
                            item.author = "";
                            out.add(item);
                        }
                    }
                }
            } catch (Throwable ignored) {}
        } finally {
            if (c != null) try { c.close(); } catch (Throwable ignored) {}
            if (db != null) try { db.close(); } catch (Throwable ignored) {}
        }
        return out;
    }

    private static File resolveOrFetchCover(Context ctx, File bookFile, String bookKey, String isbn) {
        // 1. Direct sidecar: <bookFile>.cover.jpg
        File sidecar = new File(bookFile.getAbsolutePath() + ".cover.jpg");
        if (sidecar.exists() && sidecar.length() > 512) return sidecar;

        // 2. Sibling sidecar variants (<base>.cover.jpg, <base>.azw3.cover.jpg, <base>.kfx.cover.jpg)
        File parent = bookFile.getParentFile();
        String name = bookFile.getName();
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        if (parent != null) {
            String[] variants = new String[] {
                base + ".cover.jpg",
                base + ".azw3.cover.jpg",
                base + ".kfx.cover.jpg",
                base + ".jpg"
            };
            for (String v : variants) {
                File f = new File(parent, v);
                if (f.exists() && f.length() > 512) return f;
            }
        }

        // 3. Cached Folio cover in app storage
        File cacheDir = new File(ctx.getFilesDir(), "folio_covers");
        if (!cacheDir.exists()) cacheDir.mkdirs();
        File cached = new File(cacheDir, bookKey + ".jpg");
        if (cached.exists() && cached.length() > 512) return cached;

        // 4. Fallback: fetch cover from Open Library Covers API by ISBN
        if (isbn != null && !isbn.trim().isEmpty()) {
            String cleanIsbn = isbn.replace("-", "").replace(" ", "").trim();
            if (downloadCover("https://covers.openlibrary.org/b/isbn/" + cleanIsbn + "-M.jpg?default=false", cached)) {
                return cached;
            }
        }
        return null;
    }

    private static boolean downloadCover(String urlStr, File dest) {
        HttpURLConnection conn = null;
        InputStream in = null;
        FileOutputStream out = null;
        try {
            URL url = new URL(urlStr);
            int redirects = 0;
            while (redirects < 5) {
                conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(12000);
                conn.setRequestProperty("User-Agent", "FolioReader/3.7 (Android; Local-First EPUB)");
                int code = conn.getResponseCode();
                if (code == 301 || code == 302 || code == 303 || code == 307) {
                    String loc = conn.getHeaderField("Location");
                    conn.disconnect();
                    if (loc == null || loc.isEmpty()) return false;
                    url = new URL(loc);
                    redirects++;
                    continue;
                }
                if (code != 200) return false;
                break;
            }
            in = conn.getInputStream();
            out = new FileOutputStream(dest);
            byte[] buf = new byte[8192];
            int n;
            int total = 0;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
                total += n;
            }
            out.flush();
            if (total > 1024) return true;
            dest.delete();
        } catch (Throwable ignored) {
            if (dest.exists()) dest.delete();
        } finally {
            if (in != null) try { in.close(); } catch (Throwable ignored) {}
            if (out != null) try { out.close(); } catch (Throwable ignored) {}
            if (conn != null) try { conn.disconnect(); } catch (Throwable ignored) {}
        }
        return false;
    }

    private static String cleanTitleForLookup(String raw) {
        if (raw == null) return "";
        String s = raw.replaceAll("\\([^)]*\\)", " ")
                .replaceAll("(?i)Delphi Complete Works of\\s*", "")
                .replaceAll("(?i)Complete Works of\\s*", "")
                .replaceAll("(?i)-\\s*Delphi Complete Works.*", "")
                .replaceAll("\\s+", " ")
                .trim();
        return s.isEmpty() ? raw.trim() : s;
    }

    private static String cleanAuthorForLookup(String author, String cleanedTitle) {
        if (author == null || author.trim().isEmpty() || "Unknown Author".equalsIgnoreCase(author.trim())) {
            return cleanedTitle;
        }
        String a = author.trim();
        if (a.contains("HOMER")) return "Homer";
        if (a.contains(",")) {
            String[] parts = a.split(",");
            if (parts.length == 2 && !parts[0].trim().isEmpty() && !parts[1].trim().isEmpty()) {
                return parts[1].trim() + " " + parts[0].trim();
            }
            return parts[0].trim();
        }
        return a;
    }
}
