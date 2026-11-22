package com.amazon.kindle.epub.ingest;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Portable JSON backup & restore for folio_metadata.db so enriched authors,
 * publishing dates, publishers, genres, ISBNs, and manual edits survive reinstalls.
 */
public final class FolioMetadataBackup {

    private static final String TAG = "FolioMetaBackup";
    private static final String BACKUP_DIR = "/sdcard/kindle/backups";
    private static final String BACKUP_FILE = "folio_metadata_backup.json";

    private FolioMetadataBackup() {}

    public static File getBackupFile(Context ctx) {
        File extDir = new File(BACKUP_DIR);
        if (extDir.exists() || extDir.mkdirs()) {
            return new File(extDir, BACKUP_FILE);
        }
        File fallback = new File(ctx.getFilesDir(), "backups");
        if (!fallback.exists()) fallback.mkdirs();
        return new File(fallback, BACKUP_FILE);
    }

    public static int exportToJson(Context ctx) {
        FolioMetadataDb db = new FolioMetadataDb(ctx);
        try {
            List<FolioMetadataDb.BookRecord> all = db.getAllBooks();
            JSONObject root = new JSONObject();
            root.put("version", 2);
            root.put("exported_at", System.currentTimeMillis());
            root.put("count", all.size());
            int goalTarget = ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
                    .getInt("reading_goal_target", 12);
            root.put("reading_goal_target", goalTarget);

            JSONArray arr = new JSONArray();
            for (FolioMetadataDb.BookRecord r : all) {
                JSONObject obj = new JSONObject();
                obj.put("book_key", r.bookKey);
                obj.put("kfx_path", r.kfxPath);
                obj.put("cover_path", r.coverPath);
                obj.put("source_name", r.sourceName);
                obj.put("title", r.title);
                obj.put("author", r.author);
                obj.put("publisher", r.publisher);
                obj.put("pub_date", r.pubDate);
                obj.put("genres", r.genres);
                obj.put("language", r.language);
                obj.put("isbn", r.isbn);
                obj.put("description", r.description);
                obj.put("open_library_key", r.openLibraryKey);
                obj.put("is_favorite", FolioMetadataCatalogDialog.isFavorite(ctx, r.kfxPath));
                String pNote = FolioMetadataCatalogDialog.getNote(ctx, r.kfxPath);
                if (!pNote.isEmpty()) obj.put("personal_note", pNote);
                int sRate = FolioMetadataCatalogDialog.getRating(ctx, r.kfxPath);
                if (sRate > 0) obj.put("star_rating", sRate);
                String shName = FolioMetadataCatalogDialog.getShelf(ctx, r.kfxPath);
                if (!shName.isEmpty()) obj.put("shelf", shName);
                if (FolioMetadataCatalogDialog.isInQueue(ctx, r.kfxPath)) obj.put("in_queue", true);
                arr.put(obj);
            }
            root.put("books", arr);

            File out = getBackupFile(ctx);
            FileOutputStream fos = new FileOutputStream(out);
            try {
                fos.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
                fos.flush();
            } finally {
                fos.close();
            }
            Log.i(TAG, "Exported " + all.size() + " books to " + out.getAbsolutePath());
            return all.size();
        } catch (Throwable t) {
            Log.e(TAG, "Metadata export failed", t);
            return -1;
        } finally {
            db.close();
        }
    }

    public static int restoreFromJson(Context ctx) {
        File inFile = getBackupFile(ctx);
        if (!inFile.exists()) return -1;

        FolioMetadataDb db = new FolioMetadataDb(ctx);
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(inFile), StandardCharsets.UTF_8));
            try {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            } finally {
                br.close();
            }

            JSONObject root = new JSONObject(sb.toString());
            int restoredGoal = root.optInt("reading_goal_target", 0);
            if (restoredGoal > 0) {
                ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
                        .edit().putInt("reading_goal_target", restoredGoal).apply();
            }
            JSONArray arr = root.optJSONArray("books");
            if (arr == null) return 0;

            int restored = 0;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.optJSONObject(i);
                if (obj == null) continue;
                String bookKey = obj.optString("book_key", "");
                String kfxPath = obj.optString("kfx_path", "");
                if (bookKey.isEmpty() || kfxPath.isEmpty()) continue;

                EpubMetadataExtractor.BookMeta meta = new EpubMetadataExtractor.BookMeta();
                meta.title = obj.optString("title", "");
                meta.author = obj.optString("author", "Unknown Author");
                meta.publisher = obj.optString("publisher", "");
                meta.pubDate = obj.optString("pub_date", "");
                meta.language = obj.optString("language", "");
                meta.isbn = obj.optString("isbn", "");
                meta.description = obj.optString("description", "");
                meta.openLibraryKey = obj.optString("open_library_key", "");
                String genresStr = obj.optString("genres", "");
                if (!genresStr.isEmpty()) {
                    for (String g : genresStr.split(",")) {
                        String c = g.trim();
                        if (!c.isEmpty()) meta.genres.add(c);
                    }
                }

                String coverPath = obj.optString("cover_path", "");
                String sourceName = obj.optString("source_name", "");
                db.upsertBook(
                        bookKey,
                        new File(kfxPath),
                        coverPath.isEmpty() ? null : new File(coverPath),
                        sourceName,
                        meta);
                if (obj.optBoolean("is_favorite", false)) {
                    FolioMetadataCatalogDialog.setFavorite(ctx, kfxPath, true);
                }
                String pNote = obj.optString("personal_note", "");
                if (!pNote.isEmpty()) {
                    FolioMetadataCatalogDialog.setNote(ctx, kfxPath, pNote);
                }
                int sRate = obj.optInt("star_rating", 0);
                if (sRate > 0) {
                    FolioMetadataCatalogDialog.setRating(ctx, kfxPath, sRate);
                }
                String shName = obj.optString("shelf", "");
                if (!shName.isEmpty()) {
                    FolioMetadataCatalogDialog.setShelf(ctx, kfxPath, shName);
                }
                if (obj.optBoolean("in_queue", false)) {
                    FolioMetadataCatalogDialog.setInQueue(ctx, kfxPath, true);
                }
                restored++;
            }
            Log.i(TAG, "Restored " + restored + " metadata records from " + inFile.getAbsolutePath());
            return restored;
        } catch (Throwable t) {
            Log.e(TAG, "Metadata restore failed", t);
            return -1;
        } finally {
            db.close();
        }
    }

    public static File getReadingLogFile(Context ctx) {
        File extDir = new File(BACKUP_DIR);
        if (extDir.exists() || extDir.mkdirs()) {
            return new File(extDir, "folio_reading_log.md");
        }
        File fallback = new File(ctx.getFilesDir(), "backups");
        if (!fallback.exists()) fallback.mkdirs();
        return new File(fallback, "folio_reading_log.md");
    }

    public static String buildReadingLogMarkdown(Context ctx) {
        FolioMetadataDb db = new FolioMetadataDb(ctx);
        try {
            List<FolioMetadataDb.BookRecord> all = db.getAllBooks();
            java.util.Collections.sort(all, (a, b) -> {
                int ra = FolioMetadataCatalogDialog.getRating(ctx, a.kfxPath);
                int rb = FolioMetadataCatalogDialog.getRating(ctx, b.kfxPath);
                if (ra != rb) return Integer.compare(rb, ra);
                boolean fa = FolioMetadataCatalogDialog.isFavorite(ctx, a.kfxPath);
                boolean fb = FolioMetadataCatalogDialog.isFavorite(ctx, b.kfxPath);
                if (fa != fb) return fa ? -1 : 1;
                return Long.compare(b.addedAt, a.addedAt);
            });

            java.util.Map<String, Integer> progMap = FolioMetadataCatalogDialog.loadProgressMap(ctx);
            int favs = 0, rated = 0, noted = 0, reading = 0, finished = 0;
            for (FolioMetadataDb.BookRecord r : all) {
                if (FolioMetadataCatalogDialog.isFavorite(ctx, r.kfxPath)) favs++;
                if (FolioMetadataCatalogDialog.getRating(ctx, r.kfxPath) > 0) rated++;
                if (!FolioMetadataCatalogDialog.getNote(ctx, r.kfxPath).isEmpty()) noted++;
                Integer pv = (r.kfxPath != null) ? progMap.get(r.kfxPath) : null;
                int p = pv != null ? pv : 0;
                if (p >= 100) finished++;
                else if (p > 0) reading++;
            }

            String dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    .format(new java.util.Date());
            StringBuilder md = new StringBuilder();
            md.append("# Folio Reading Log (").append(dateStr).append(")\n\n");
            int goalTarget = ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
                    .getInt("reading_goal_target", 12);
            if (goalTarget <= 0) goalTarget = 12;
            int goalPct = Math.min(100, Math.round((finished * 100f) / goalTarget));
            int yrNow = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            md.append("**Library:** ").append(all.size()).append(" books · ")
              .append(reading).append(" reading · ")
              .append(finished).append(" finished · ")
              .append(favs).append(" favorited · ")
              .append(rated).append(" rated · ")
              .append(noted).append(" annotated\n");
            md.append("**").append(yrNow).append(" Reading Goal:** ")
              .append(finished).append(" / ").append(goalTarget)
              .append(" books finished (").append(goalPct).append("%)\n\n");

            for (FolioMetadataDb.BookRecord r : all) {
                boolean isFav = FolioMetadataCatalogDialog.isFavorite(ctx, r.kfxPath);
                int stars = FolioMetadataCatalogDialog.getRating(ctx, r.kfxPath);
                String note = FolioMetadataCatalogDialog.getNote(ctx, r.kfxPath);
                String yr = EpubMetadataExtractor.normalizePubDate(r.pubDate);
                String cleanAuthor = FolioMetadataCatalogDialog.formatAuthorForDisplay(r.author);
                Integer pv = (r.kfxPath != null) ? progMap.get(r.kfxPath) : null;
                int pct = pv != null ? pv : 0;

                md.append("## ");
                if (isFav) md.append("★ ");
                md.append(r.title != null ? r.title : "Untitled").append("\n");
                md.append("- **Author:** ").append(!cleanAuthor.isEmpty() ? cleanAuthor : "Unknown");
                if (!yr.isEmpty()) md.append(" (").append(yr).append(")");
                md.append("\n");
                if (pct >= 100) {
                    md.append("- **Status:** ✓ Finished\n");
                } else if (pct > 0) {
                    md.append("- **Status:** ").append(pct).append("% read\n");
                }
                if (stars > 0) {
                    md.append("- **Rating:** ");
                    for (int s = 0; s < 5; s++) md.append(s < stars ? "★" : "☆");
                    md.append(" (").append(stars).append("/5)\n");
                }
                String shName = FolioMetadataCatalogDialog.getShelf(ctx, r.kfxPath);
                if (!shName.isEmpty()) {
                    md.append("- **Shelf:** ").append(shName).append("\n");
                }
                if (r.isbn != null && !r.isbn.isEmpty()) {
                    md.append("- **ISBN:** ").append(r.isbn).append("\n");
                }
                if (!note.isEmpty()) {
                    md.append("> ").append(note.replace("\n", "\n> ")).append("\n");
                }
                md.append("\n");
            }
            List<FolioMetadataDb.BookRecord> qBooks = new ArrayList<>();
            for (FolioMetadataDb.BookRecord r : all) {
                if (r.kfxPath != null && FolioMetadataCatalogDialog.isInQueue(ctx, r.kfxPath)) {
                    qBooks.add(r);
                }
            }
            if (!qBooks.isEmpty()) {
                md.append("## 📌 Up Next Queue (").append(qBooks.size()).append(")\n\n");
                for (FolioMetadataDb.BookRecord r : qBooks) {
                    String ca = FolioMetadataCatalogDialog.formatAuthorForDisplay(r.author);
                    md.append("- **").append(r.title != null ? r.title : "Untitled").append("**");
                    if (!ca.isEmpty()) md.append(" — ").append(ca);
                    md.append("\n");
                }
                md.append("\n");
            }
            return md.toString();
        } catch (Throwable t) {
            Log.e(TAG, "Failed to build reading log markdown", t);
            return "# Folio Reading Log\n\nError generating reading log.";
        } finally {
            db.close();
        }
    }

    public static File exportReadingLogMarkdown(Context ctx) {
        try {
            String md = buildReadingLogMarkdown(ctx);
            File out = getReadingLogFile(ctx);
            FileOutputStream fos = new FileOutputStream(out);
            try {
                fos.write(md.getBytes(StandardCharsets.UTF_8));
                fos.flush();
            } finally {
                fos.close();
            }
            Log.i(TAG, "Exported reading log to " + out.getAbsolutePath());
            return out;
        } catch (Throwable t) {
            Log.e(TAG, "Reading log export failed", t);
            return null;
        }
    }

    public static File getCsvFile(Context ctx) {
        File extDir = new File(BACKUP_DIR);
        if (extDir.exists() || extDir.mkdirs()) {
            return new File(extDir, "folio_library.csv");
        }
        File fallback = new File(ctx.getFilesDir(), "backups");
        if (!fallback.exists()) fallback.mkdirs();
        return new File(fallback, "folio_library.csv");
    }

    private static String csvEsc(String v) {
        if (v == null) return "";
        String clean = v.replace("\r\n", " ").replace("\n", " ").trim();
        if (clean.contains(",") || clean.contains("\"")) {
            return "\"" + clean.replace("\"", "\"\"") + "\"";
        }
        return clean;
    }

    public static File exportLibraryCsv(Context ctx) {
        FolioMetadataDb db = new FolioMetadataDb(ctx);
        try {
            List<FolioMetadataDb.BookRecord> all = db.getAllBooks();
            java.util.Map<String, Integer> progMap = FolioMetadataCatalogDialog.loadProgressMap(ctx);
            StringBuilder csv = new StringBuilder();
            csv.append("Title,Author,Year,Publisher,ISBN,Status,Shelf,Rating,Favorite,In Queue,Note\n");
            for (FolioMetadataDb.BookRecord r : all) {
                String cleanAuthor = FolioMetadataCatalogDialog.formatAuthorForDisplay(r.author);
                String yr = EpubMetadataExtractor.normalizePubDate(r.pubDate);
                Integer pv = (r.kfxPath != null) ? progMap.get(r.kfxPath) : null;
                int pct = pv != null ? pv : 0;
                String status = pct >= 100 ? "Finished" : (pct > 0 ? pct + "%" : "Unread");
                String shelf = FolioMetadataCatalogDialog.getShelf(ctx, r.kfxPath);
                int rating = FolioMetadataCatalogDialog.getRating(ctx, r.kfxPath);
                boolean fav = FolioMetadataCatalogDialog.isFavorite(ctx, r.kfxPath);
                boolean inQ = FolioMetadataCatalogDialog.isInQueue(ctx, r.kfxPath);
                String note = FolioMetadataCatalogDialog.getNote(ctx, r.kfxPath);

                csv.append(csvEsc(r.title)).append(",")
                   .append(csvEsc(cleanAuthor)).append(",")
                   .append(csvEsc(yr)).append(",")
                   .append(csvEsc(r.publisher)).append(",")
                   .append(csvEsc(r.isbn)).append(",")
                   .append(csvEsc(status)).append(",")
                   .append(csvEsc(shelf)).append(",")
                   .append(rating > 0 ? rating : "").append(",")
                   .append(fav ? "Yes" : "").append(",")
                   .append(inQ ? "Yes" : "").append(",")
                   .append(csvEsc(note)).append("\n");
            }
            File out = getCsvFile(ctx);
            FileOutputStream fos = new FileOutputStream(out);
            try {
                fos.write(csv.toString().getBytes(StandardCharsets.UTF_8));
                fos.flush();
            } finally {
                fos.close();
            }
            Log.i(TAG, "Exported CSV library (" + all.size() + " rows) to " + out.getAbsolutePath());
            return out;
        } catch (Throwable t) {
            Log.e(TAG, "CSV export failed", t);
            return null;
        } finally {
            db.close();
        }
    }
}
