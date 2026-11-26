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
    public static final long MAX_BACKUP_FILE_BYTES = 16L * 1024L * 1024L;
    public static final int MAX_BACKUP_BOOKS = 10000;
    public static final int MAX_BOOK_KEY_LEN = 64;
    public static final int MAX_TEXT_FIELD_LEN = 2000;
    public static final int MAX_DESC_FIELD_LEN = 4000;
    public static final int MAX_READING_GOAL = 1000;

    private FolioMetadataBackup() {}

    public static boolean isValidBackupBookKey(String bookKey) {
        if (bookKey == null) return false;
        String s = bookKey.trim();
        if (s.isEmpty() || s.length() > MAX_BOOK_KEY_LEN) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c <= 0x20 || c == 0x7F || c == '/' || c == '\\') return false;
        }
        return !s.contains("..");
    }

    public static boolean isValidBackupBookPath(String rawPath) {
        if (rawPath == null) return false;
        String s = rawPath.trim();
        if (s.isEmpty() || s.length() > 1024 || !s.startsWith("/")) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < 0x20 || c == 0x7F) return false;
        }
        String norm = s.replace('\\', '/');
        for (String seg : norm.split("/")) {
            if ("..".equals(seg)) return false;
        }
        String lower = norm.toLowerCase(java.util.Locale.ROOT);
        if (lower.contains("/databases/") || lower.contains("/shared_prefs/")) return false;
        return lower.endsWith(".kfx");
    }

    public static boolean isValidBackupCoverPath(String rawPath) {
        if (rawPath == null || rawPath.trim().isEmpty()) return false;
        String s = rawPath.trim();
        if (s.length() > 1024 || !s.startsWith("/")) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < 0x20 || c == 0x7F) return false;
        }
        String norm = s.replace('\\', '/');
        for (String seg : norm.split("/")) {
            if ("..".equals(seg)) return false;
        }
        String lower = norm.toLowerCase(java.util.Locale.ROOT);
        if (lower.contains("/databases/") || lower.contains("/shared_prefs/")) return false;
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp");
    }

    public static int clampReadingGoal(int g) {
        if (g <= 0) return 0;
        return Math.min(g, MAX_READING_GOAL);
    }

    public static int clampStarRating(int r) {
        if (r <= 0) return 0;
        return Math.min(Math.max(r, 1), 5);
    }

    private static String boundString(String s, int maxLen) {
        if (s == null) return "";
        String t = s.trim();
        return t.length() <= maxLen ? t : t.substring(0, maxLen);
    }

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
                String finDt = FolioMetadataCatalogDialog.getFinishedDate(ctx, r.kfxPath);
                if (!finDt.isEmpty()) obj.put("finished_date", finDt);
                arr.put(obj);
            }
            root.put("books", arr);

            File out = getBackupFile(ctx);
            writeAtomicUtf8(out, root.toString(2));
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
        if (!inFile.exists() || !inFile.isFile()) return -1;
        if (inFile.length() > MAX_BACKUP_FILE_BYTES) {
            Log.e(TAG, "Backup validation failed: file exceeds 16 MB size cap (" + inFile.length() + " B)");
            return -1;
        }

        FolioMetadataDb db = new FolioMetadataDb(ctx);
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(inFile), StandardCharsets.UTF_8));
            try {
                char[] cbuf = new char[8192];
                int n;
                long totalChars = 0L;
                while ((n = br.read(cbuf)) != -1) {
                    totalChars += n;
                    if (totalChars > MAX_BACKUP_FILE_BYTES) {
                        Log.e(TAG, "Backup stream exceeded 16 MB character limit");
                        return -1;
                    }
                    sb.append(cbuf, 0, n);
                }
            } finally {
                br.close();
            }

            JSONObject root = new JSONObject(sb.toString());
            int ver = root.optInt("version", 1);
            if (ver < 1 || ver > 10) {
                Log.w(TAG, "Backup validation failed: unsupported schema version=" + ver);
                return -1;
            }
            JSONArray arr = root.optJSONArray("books");
            if (arr == null || arr.length() == 0) {
                Log.w(TAG, "Backup validation failed: missing or empty 'books' array");
                return -1;
            }
            if (arr.length() > MAX_BACKUP_BOOKS) {
                Log.w(TAG, "Backup validation: capping restore count from " + arr.length() + " to " + MAX_BACKUP_BOOKS);
            }
            boolean hasValidEntry = false;
            int limit = Math.min(arr.length(), MAX_BACKUP_BOOKS);
            for (int i = 0; i < limit; i++) {
                JSONObject obj = arr.optJSONObject(i);
                if (obj != null
                        && isValidBackupBookKey(obj.optString("book_key", ""))
                        && isValidBackupBookPath(obj.optString("kfx_path", ""))) {
                    hasValidEntry = true;
                    break;
                }
            }
            if (!hasValidEntry) {
                Log.w(TAG, "Backup validation failed: no valid book_key/kfx_path entries");
                return -1;
            }

            int restoredGoal = clampReadingGoal(root.optInt("reading_goal_target", 0));
            if (restoredGoal > 0) {
                ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
                        .edit().putInt("reading_goal_target", restoredGoal).apply();
            }

            int restored = 0;
            for (int i = 0; i < limit; i++) {
                JSONObject obj = arr.optJSONObject(i);
                if (obj == null) continue;
                String bookKey = obj.optString("book_key", "").trim();
                String kfxPath = obj.optString("kfx_path", "").trim();
                if (!isValidBackupBookKey(bookKey) || !isValidBackupBookPath(kfxPath)) {
                    Log.w(TAG, "Skipping invalid backup row at index=" + i);
                    continue;
                }

                EpubMetadataExtractor.BookMeta meta = new EpubMetadataExtractor.BookMeta();
                meta.title = boundString(obj.optString("title", ""), MAX_TEXT_FIELD_LEN);
                String rawAuthor = boundString(obj.optString("author", "Unknown Author"), MAX_TEXT_FIELD_LEN);
                meta.author = rawAuthor.isEmpty() ? "Unknown Author" : rawAuthor;
                meta.publisher = boundString(obj.optString("publisher", ""), MAX_TEXT_FIELD_LEN);
                String rawPubDate = boundString(obj.optString("pub_date", ""), 64);
                String normPubDate = EpubMetadataExtractor.normalizePubDate(rawPubDate);
                meta.pubDate = normPubDate.isEmpty() ? rawPubDate : normPubDate;
                meta.language = boundString(obj.optString("language", ""), 32);
                String rawIsbn = boundString(obj.optString("isbn", ""), 64);
                String normIsbn = EpubMetadataExtractor.normalizeIsbn(rawIsbn);
                meta.isbn = normIsbn.isEmpty() ? rawIsbn : normIsbn;
                meta.description = EpubMetadataExtractor.truncateDescription(obj.optString("description", ""));
                meta.openLibraryKey = boundString(obj.optString("open_library_key", ""), 128);
                String genresStr = boundString(obj.optString("genres", ""), MAX_TEXT_FIELD_LEN);
                if (!genresStr.isEmpty()) {
                    for (String g : genresStr.split(",")) {
                        String c = g.trim();
                        if (!c.isEmpty() && c.length() <= 60 && meta.genres.size() < 12) {
                            meta.genres.add(c);
                        }
                    }
                }

                String coverPath = obj.optString("cover_path", "").trim();
                File validCover = isValidBackupCoverPath(coverPath) ? new File(coverPath) : null;
                String sourceName = boundString(obj.optString("source_name", ""), 256);
                db.upsertBook(
                        bookKey,
                        new File(kfxPath),
                        validCover,
                        sourceName,
                        meta);
                if (obj.optBoolean("is_favorite", false)) {
                    FolioMetadataCatalogDialog.setFavorite(ctx, kfxPath, true);
                }
                String pNote = boundString(obj.optString("personal_note", ""), MAX_DESC_FIELD_LEN);
                if (!pNote.isEmpty()) {
                    FolioMetadataCatalogDialog.setNote(ctx, kfxPath, pNote);
                }
                int sRate = clampStarRating(obj.optInt("star_rating", 0));
                if (sRate > 0) {
                    FolioMetadataCatalogDialog.setRating(ctx, kfxPath, sRate);
                }
                String shName = boundString(obj.optString("shelf", ""), 128);
                if (!shName.isEmpty()) {
                    FolioMetadataCatalogDialog.setShelf(ctx, kfxPath, shName);
                }
                if (obj.optBoolean("in_queue", false)) {
                    FolioMetadataCatalogDialog.setInQueue(ctx, kfxPath, true);
                }
                String finDt = boundString(obj.optString("finished_date", ""), 64);
                if (!finDt.isEmpty()) {
                    FolioMetadataCatalogDialog.setFinishedDate(ctx, kfxPath, finDt);
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
        return buildReadingLogMarkdown(ctx, false);
    }

    public static String buildReadingLogMarkdown(Context ctx, boolean curatedOnly) {
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
            md.append(curatedOnly ? "# Azath Curated Reading Log (" : "# Azath Reading Log (").append(dateStr).append(")\n\n");
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

            for (FolioMetadataDb.BookRecord r : all) {
                boolean isFav = FolioMetadataCatalogDialog.isFavorite(ctx, r.kfxPath);
                int stars = FolioMetadataCatalogDialog.getRating(ctx, r.kfxPath);
                String note = FolioMetadataCatalogDialog.getNote(ctx, r.kfxPath);
                String shCheck = FolioMetadataCatalogDialog.getShelf(ctx, r.kfxPath);
                boolean inQ = FolioMetadataCatalogDialog.isInQueue(ctx, r.kfxPath);
                String yr = EpubMetadataExtractor.normalizePubDate(r.pubDate);
                String cleanAuthor = FolioMetadataCatalogDialog.formatAuthorForDisplay(r.author);
                Integer pv = (r.kfxPath != null) ? progMap.get(r.kfxPath) : null;
                int pct = pv != null ? pv : 0;
                if (curatedOnly && pct <= 0 && !isFav && stars <= 0 && note.isEmpty() && shCheck.isEmpty() && !inQ) {
                    continue;
                }

                md.append("## ");
                if (isFav) md.append("★ ");
                md.append(r.title != null ? r.title : "Untitled").append("\n");
                md.append("- **Author:** ").append(!cleanAuthor.isEmpty() ? cleanAuthor : "Unknown");
                if (!yr.isEmpty()) md.append(" (").append(yr).append(")");
                md.append("\n");
                if (pct >= 100) {
                    String finDt = FolioMetadataCatalogDialog.getFinishedDate(ctx, r.kfxPath);
                    md.append("- **Status:** ✓ Finished");
                    if (!finDt.isEmpty()) md.append(" (").append(finDt).append(")");
                    md.append("\n");
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
            return md.toString();
        } catch (Throwable t) {
            Log.e(TAG, "Failed to build reading log markdown", t);
            return "# Folio Reading Log\n\nError generating reading log.";
        } finally {
            db.close();
        }
    }

    public static File exportReadingLogMarkdown(Context ctx) {
        return exportReadingLogMarkdown(ctx, false);
    }

    public static File exportReadingLogMarkdown(Context ctx, boolean curatedOnly) {
        try {
            String md = buildReadingLogMarkdown(ctx, curatedOnly);
            File out = getReadingLogFile(ctx);
            writeAtomicUtf8(out, md);
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
        String clean = v.trim();
        if (clean.contains(",") || clean.contains("\"") || clean.contains("\n") || clean.contains("\r")) {
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
            csv.append("Title,Author,Year,Publisher,ISBN,Status,Finished Date,Shelf,Rating,Favorite,In Queue,Note\n");
            for (FolioMetadataDb.BookRecord r : all) {
                String cleanAuthor = FolioMetadataCatalogDialog.formatAuthorForDisplay(r.author);
                String yr = EpubMetadataExtractor.normalizePubDate(r.pubDate);
                Integer pv = (r.kfxPath != null) ? progMap.get(r.kfxPath) : null;
                int pct = pv != null ? pv : 0;
                String status = pct >= 100 ? "Finished" : (pct > 0 ? pct + "%" : "Unread");
                String finDt = FolioMetadataCatalogDialog.getFinishedDate(ctx, r.kfxPath);
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
                   .append(csvEsc(finDt)).append(",")
                   .append(csvEsc(shelf)).append(",")
                   .append(rating > 0 ? rating : "").append(",")
                   .append(fav ? "Yes" : "").append(",")
                   .append(inQ ? "Yes" : "").append(",")
                   .append(csvEsc(note)).append("\n");
            }
            File out = getCsvFile(ctx);
            writeAtomicUtf8(out, csv.toString());
            Log.i(TAG, "Exported CSV library (" + all.size() + " rows) to " + out.getAbsolutePath());
            return out;
        } catch (Throwable t) {
            Log.e(TAG, "CSV export failed", t);
            return null;
        } finally {
            db.close();
        }
    }

    public static File getHtmlFile(Context ctx) {
        return new File(getBackupFile(ctx).getParentFile(), "folio_library.html");
    }

    private static String htmlEsc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    public static File exportLibraryHtml(Context ctx) {
        FolioMetadataDb db = new FolioMetadataDb(ctx);
        try {
            List<FolioMetadataDb.BookRecord> all = db.getAllBooks();
            java.util.Map<String, Integer> progMap = FolioMetadataCatalogDialog.loadProgressMap(ctx);
            int finished = 0, reading = 0, favs = 0, queued = 0;
            for (FolioMetadataDb.BookRecord r : all) {
                Integer pv = (r.kfxPath != null) ? progMap.get(r.kfxPath) : null;
                int p = pv != null ? pv : 0;
                if (p >= 100) finished++;
                else if (p > 0) reading++;
                if (FolioMetadataCatalogDialog.isFavorite(ctx, r.kfxPath)) favs++;
                if (FolioMetadataCatalogDialog.isInQueue(ctx, r.kfxPath)) queued++;
            }
            int goalTarget = ctx.getSharedPreferences("folio_ui_prefs", Context.MODE_PRIVATE)
                    .getInt("reading_goal_target", 12);
            if (goalTarget <= 0) goalTarget = 12;
            int goalPct = Math.min(100, Math.round((finished * 100f) / goalTarget));
            String dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    .format(new java.util.Date());

            StringBuilder h = new StringBuilder();
            h.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\">");
            h.append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">");
            h.append("<title>Azath Offline Library Catalog</title>");
            h.append("<style>body{margin:0;padding:24px;background:#0E0F11;color:#ECE8E0;font-family:-apple-system,BlinkMacSystemFont,sans-serif;}");
            h.append(".wrap{max-width:860px;margin:0 auto;}h1{margin:0 0 6px;font-size:26px;font-family:Georgia,'Times New Roman',serif;font-weight:700;}");
            h.append(".sub{color:#8A857D;font-size:13px;margin-bottom:16px;}");
            h.append(".bar{height:5px;background:#32353B;border-radius:4px;overflow:hidden;margin:8px 0 18px;}");
            h.append(".fill{height:100%;background:#D4C9B8;width:").append(goalPct).append("%;}");
            h.append("#q{width:100%;box-sizing:border-box;padding:10px 14px;border-radius:8px;border:1px solid #32353B;background:#1C1E22;color:#ECE8E0;font-size:14px;margin-bottom:14px;}");
            h.append(".card{background:#1C1E22;border:1px solid #32353B;border-radius:10px;padding:14px 16px;margin-bottom:10px;}");
            h.append(".t{font-weight:700;font-size:15.5px;font-family:Georgia,'Times New Roman',serif;margin-bottom:3px;}.a{color:#8A857D;font-size:12.5px;}");
            h.append(".tags{margin-top:7px;display:flex;flex-wrap:wrap;gap:6px;font-size:11px;font-weight:600;}");
            h.append(".tag{padding:2px 9px;border-radius:7px;background:#25282D;border:1px solid #32353B;color:#BDB8AE;}");
            h.append(".done,.rd,.q,.sh{background:#282520;border-color:#524B41;color:#D4C9B8;}");
            h.append(".note{margin-top:9px;padding:9px 12px;background:#1C1E22;border:1px solid #524B41;border-left:3px solid #D4C9B8;color:#ECE8E0;font-size:12.5px;border-radius:6px;}");
            h.append("</style></head><body><div class=\"wrap\">");
            h.append("<h1>Azath Offline Library Catalog</h1>");
            h.append("<div class=\"sub\">Exported ").append(dateStr).append(" · ")
             .append(all.size()).append(" books · ").append(reading).append(" reading · ")
             .append(finished).append(" finished · ").append(favs).append(" fav · ")
             .append(queued).append(" queued · Goal ").append(finished).append("/").append(goalTarget)
             .append(" (").append(goalPct).append("%)</div>");
            h.append("<div class=\"bar\"><div class=\"fill\"></div></div>");
            h.append("<input id=\"q\" placeholder=\"Filter books by title, author, shelf, or note...\" oninput=\"f()\">");
            h.append("<div id=\"list\">");

            for (FolioMetadataDb.BookRecord r : all) {
                String t = r.title != null ? r.title : "Untitled";
                String a = FolioMetadataCatalogDialog.formatAuthorForDisplay(r.author);
                String yr = EpubMetadataExtractor.normalizePubDate(r.pubDate);
                Integer pv = (r.kfxPath != null) ? progMap.get(r.kfxPath) : null;
                int pct = pv != null ? pv : 0;
                boolean fav = FolioMetadataCatalogDialog.isFavorite(ctx, r.kfxPath);
                boolean inQ = FolioMetadataCatalogDialog.isInQueue(ctx, r.kfxPath);
                int stars = FolioMetadataCatalogDialog.getRating(ctx, r.kfxPath);
                String sh = FolioMetadataCatalogDialog.getShelf(ctx, r.kfxPath);
                String finDt = FolioMetadataCatalogDialog.getFinishedDate(ctx, r.kfxPath);
                String note = FolioMetadataCatalogDialog.getNote(ctx, r.kfxPath);

                h.append("<div class=\"card\">");
                h.append("<div class=\"t\">").append(fav ? "★ " : "").append(htmlEsc(t)).append("</div>");
                h.append("<div class=\"a\">").append(htmlEsc(a.isEmpty() ? "Unknown Author" : a));
                if (!yr.isEmpty()) h.append(" · ").append(htmlEsc(yr));
                if (r.publisher != null && !r.publisher.isEmpty()) h.append(" · ").append(htmlEsc(r.publisher));
                h.append("</div><div class=\"tags\">");
                if (pct >= 100) {
                    h.append("<span class=\"tag done\">✓ Finished").append(finDt.isEmpty() ? "" : " " + htmlEsc(finDt)).append("</span>");
                } else if (pct > 0) {
                    h.append("<span class=\"tag rd\">").append(pct).append("% read</span>");
                }
                if (inQ) h.append("<span class=\"tag q\">📌 Up Next</span>");
                if (stars > 0) h.append("<span class=\"tag\" style=\"color:#D4C9B8\">★ ").append(stars).append("/5</span>");
                if (!sh.isEmpty()) h.append("<span class=\"tag sh\">🏷 ").append(htmlEsc(sh)).append("</span>");
                h.append("</div>");
                if (!note.isEmpty()) {
                    h.append("<div class=\"note\">").append(htmlEsc(note)).append("</div>");
                }
                h.append("</div>");
            }
            h.append("</div></div><script>function f(){var v=document.getElementById('q').value.toLowerCase();");
            h.append("document.querySelectorAll('.card').forEach(function(c){c.style.display=c.textContent.toLowerCase().indexOf(v)>=0?'':'none';});}</script>");
            h.append("</body></html>");

            File out = getHtmlFile(ctx);
            writeAtomicUtf8(out, h.toString());
            Log.i(TAG, "Exported HTML catalog (" + all.size() + " books) to " + out.getAbsolutePath());
            return out;
        } catch (Throwable t) {
            Log.e(TAG, "HTML export failed", t);
            return null;
        } finally {
            db.close();
        }
    }

    private static void writeAtomicUtf8(File out, String content) throws Exception {
        File parent = out.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        File tmp = new File(parent, out.getName() + ".tmp");
        boolean ok = false;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(tmp);
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            fos.flush();
            fos.close();
            fos = null;
            if (out.exists() && !tmp.renameTo(out)) {
                out.delete();
                if (!tmp.renameTo(out)) {
                    throw new java.io.IOException("Atomic rename failed: " + tmp.getAbsolutePath());
                }
            } else if (!out.exists() && !tmp.renameTo(out)) {
                throw new java.io.IOException("Atomic rename failed: " + tmp.getAbsolutePath());
            }
            ok = true;
        } finally {
            if (fos != null) {
                try { fos.close(); } catch (Throwable ignored) {}
            }
            if (!ok && tmp.exists()) {
                tmp.delete();
            }
        }
    }
}
