package com.amazon.kindle.epub.ingest;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Backfills folio_metadata.db from all existing local books in kindle_library.db,
 * enriching missing author, pub_date, publisher, genres, and ISBN via Open Library
 * on a background thread without blocking the UI.
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
        String coverPath;
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
                    Set<String> existingPaths = new HashSet<>();
                    for (FolioMetadataDb.BookRecord r : metaDb.getAllBooks()) {
                        if (r.kfxPath != null) existingPaths.add(r.kfxPath);
                    }

                    List<Candidate> todo = new ArrayList<>();
                    for (Candidate c : candidates) {
                        if (c.filePath != null && !existingPaths.contains(c.filePath)) {
                            File f = new File(c.filePath);
                            if (f.exists() && f.length() > 128) {
                                todo.add(c);
                            }
                        }
                    }

                    int total = todo.size();
                    for (int i = 0; i < total; i++) {
                        Candidate c = todo.get(i);
                        if (listener != null) {
                            listener.onProgress(i + 1, total, c.title);
                        }
                        File kfxFile = new File(c.filePath);
                        File coverFile = resolveCover(kfxFile, c.coverPath);

                        EpubMetadataExtractor.BookMeta meta = new EpubMetadataExtractor.BookMeta();
                        meta.title = cleanTitleForLookup(c.title);
                        String originalAuthor = (c.author != null && !c.author.trim().isEmpty())
                                ? c.author.trim() : "Unknown Author";
                        meta.author = cleanAuthorForLookup(originalAuthor, meta.title);

                        // Enrich via Open Library (gracefully offline-safe)
                        MetadataEnricher.enrichInPlace(meta);

                        // Restore sensible author if Open Library did not find an author
                        if (meta.author == null || meta.author.isEmpty() || "Unknown Author".equalsIgnoreCase(meta.author)) {
                            meta.author = originalAuthor;
                        }

                        // Restore full display title while keeping enriched author/date/genres/ISBN
                        if (c.title != null && !c.title.trim().isEmpty()) {
                            meta.title = c.title.trim();
                        }

                        String bookKey = FolioMetadataDb.computeBookKey(meta.title, kfxFile.length());
                        metaDb.upsertBook(bookKey, kfxFile, coverFile, kfxFile.getName(), meta);
                        added++;
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
            // Join KindleContent with LocalContent on ID = KEY
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
                item.coverPath = null;
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
                            item.coverPath = "";
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

    private static File resolveCover(File kfxFile, String dbCoverPath) {
        File sidecar = new File(kfxFile.getAbsolutePath() + ".cover.jpg");
        if (sidecar.exists()) return sidecar;
        if (dbCoverPath != null && !dbCoverPath.isEmpty()) {
            File f = new File(dbCoverPath);
            if (f.exists()) return f;
        }
        return null;
    }

    /**
     * Strips verbose omnibus suffixes like "(Illustrated) (Delphi Ancient Classics Book 11)"
     * strictly for Open Library title lookup so hits succeed cleanly.
     */
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
