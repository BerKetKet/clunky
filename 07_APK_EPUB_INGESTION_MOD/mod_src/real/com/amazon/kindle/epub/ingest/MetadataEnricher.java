package com.amazon.kindle.epub.ingest;

import android.app.ActivityThread;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Lightweight, non-blocking metadata enricher using Open Library Search API.
 * Tries ISBN first (when present), and automatically falls back to title+author
 * search if the ebook-specific ISBN returns 0 docs or no subjects — ensuring
 * modern ebook ISBN-13s still get rich genres, pub dates, and work keys.
 * Uses 15s timeouts for high-latency mobile TLS handshakes and fails gracefully offline.
 */
public final class MetadataEnricher {

    private static final String TAG = "MetadataEnricher";
    private static final String PREFS_UI = "folio_ui_prefs";
    private static final String KEY_ONLINE_ENRICHMENT = "online_enrichment_enabled";
    private static final String KEY_DISABLE_LAUNCH_SWEEP_DEBUG = "disable_launch_sweep_debug";
    private static final String DEBUG_DISABLE_ENRICH_FILE = "/sdcard/kindle/.disable_online_enrichment";
    private static final String DEBUG_DISABLE_SWEEP_FILE = "/sdcard/kindle/.disable_launch_sweep";
    private static final int CONNECT_TIMEOUT_MS = 8000;
    private static final int READ_TIMEOUT_MS = 10000;
    private static final int MAX_JSON_BYTES = 256 * 1024;
    private static final long MIN_REQUEST_INTERVAL_MS = 1000L;
    private static final int MAX_HTTP_RETRIES = 2;
    private static final long MAX_RETRY_AFTER_MS = 30_000L;
    public static final int MAX_COVER_BYTES = 2 * 1024 * 1024;

    private static final Object RATE_LOCK = new Object();
    private static long sLastRequestMs = 0L;

    public static String buildUserAgent(Context ctx) {
        String ver = "unknown";
        try {
            Context c = (ctx != null) ? ctx : ActivityThread.currentApplication();
            if (c != null && c.getPackageManager() != null) {
                PackageInfo pi = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
                if (pi != null && pi.versionName != null && !pi.versionName.trim().isEmpty()) {
                    ver = pi.versionName.trim();
                }
            }
        } catch (Throwable ignored) {}
        return "AzathReader/" + ver + " (Android; https://github.com/BerKetKet/clunky)";
    }

    public static boolean isNetworkConnected(Context ctx) {
        try {
            Context c = (ctx != null) ? ctx : ActivityThread.currentApplication();
            if (c == null) return true;
            ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            if (Build.VERSION.SDK_INT >= 23) {
                Network nw = cm.getActiveNetwork();
                if (nw == null) return false;
                NetworkCapabilities nc = cm.getNetworkCapabilities(nw);
                return nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.isConnected();
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void throttleOpenLibraryRequest() {
        synchronized (RATE_LOCK) {
            long now = System.currentTimeMillis();
            long waitMs = (sLastRequestMs + MIN_REQUEST_INTERVAL_MS) - now;
            if (waitMs > 0) {
                try {
                    Thread.sleep(waitMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
            sLastRequestMs = System.currentTimeMillis();
        }
    }

    public static long parseRetryAfterMs(String headerVal, int retryAttempt) {
        if (headerVal != null && !headerVal.trim().isEmpty()) {
            try {
                long secs = Long.parseLong(headerVal.trim());
                if (secs > 0) {
                    return Math.min(secs * 1000L, MAX_RETRY_AFTER_MS);
                }
            } catch (NumberFormatException ignored) {}
        }
        return Math.min(1000L << retryAttempt, MAX_RETRY_AFTER_MS);
    }

    public static boolean isOnlineEnrichmentEnabled(Context ctx) {
        try {
            if (new File(DEBUG_DISABLE_ENRICH_FILE).exists()) {
                return false;
            }
        } catch (Throwable ignored) {}
        if (ctx == null) return true;
        try {
            SharedPreferences sp = ctx.getSharedPreferences(PREFS_UI, Context.MODE_PRIVATE);
            return sp.getBoolean(KEY_ONLINE_ENRICHMENT, true);
        } catch (Throwable ignored) {
            return true;
        }
    }

    public static boolean isLaunchSweepDisabledDebug(Context ctx) {
        try {
            if (new File(DEBUG_DISABLE_SWEEP_FILE).exists()) {
                return true;
            }
        } catch (Throwable ignored) {}
        if (ctx == null) return false;
        try {
            SharedPreferences sp = ctx.getSharedPreferences(PREFS_UI, Context.MODE_PRIVATE);
            return sp.getBoolean(KEY_DISABLE_LAUNCH_SWEEP_DEBUG, false);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void setLaunchSweepDisabledDebug(Context ctx, boolean disabled) {
        if (ctx == null) return;
        try {
            ctx.getSharedPreferences(PREFS_UI, Context.MODE_PRIVATE)
                    .edit().putBoolean(KEY_DISABLE_LAUNCH_SWEEP_DEBUG, disabled).apply();
        } catch (Throwable ignored) {}
    }

    public static boolean shouldRunLaunchSweep(boolean onlineEnabled, boolean debugDisabled) {
        return onlineEnabled && !debugDisabled;
    }

    public static void setOnlineEnrichmentEnabled(Context ctx, boolean enabled) {
        if (ctx == null) return;
        try {
            ctx.getSharedPreferences(PREFS_UI, Context.MODE_PRIVATE)
                    .edit().putBoolean(KEY_ONLINE_ENRICHMENT, enabled).apply();
        } catch (Throwable ignored) {}
    }

    private static final Set<String> IN_FLIGHT_KEYS = ConcurrentHashMap.newKeySet();
    private static final ExecutorService ENRICH_EXECUTOR = Executors.newFixedThreadPool(2, new ThreadFactory() {
        private int count = 0;
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "azath-enrich-" + (++count));
            t.setDaemon(true);
            return t;
        }
    });

    private MetadataEnricher() {}

    private static boolean isPrivateLibraryFile(Context ctx, File kfxFile) {
        if (kfxFile == null) return false;
        String path = kfxFile.getAbsolutePath();
        if (ctx != null) {
            try {
                File privDir = new File(ctx.getFilesDir(), "folio_library");
                if (ExternalEpubActivity.isPathInsideOrEqual(
                        kfxFile.getCanonicalFile().getPath(),
                        privDir.getCanonicalFile().getPath())) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }
        return !path.startsWith("/sdcard/kindle");
    }

    /**
     * Launch-time sweep: queues online enrichment for existing library books missing genres/genres_json or open_library_key.
     */
    public static int sweepUnenrichedAsync(final Context ctx) {
        if (ctx == null) {
            return 0;
        }
        if (isLaunchSweepDisabledDebug(ctx)) {
            Log.i(TAG, "Launch-time enrichment sweep disabled (debug switch)");
            return 0;
        }
        if (!shouldRunLaunchSweep(isOnlineEnrichmentEnabled(ctx), false) || !isNetworkConnected(ctx)) {
            return 0;
        }
        int queued = 0;
        FolioMetadataDb db = new FolioMetadataDb(ctx);
        try {
            List<FolioMetadataDb.BookRecord> all = db.getAllBooks();
            for (FolioMetadataDb.BookRecord r : all) {
                if (r.bookKey == null || r.bookKey.isEmpty() || r.kfxPath == null || r.kfxPath.isEmpty()) {
                    continue;
                }
                boolean missingGenres = r.genreList().isEmpty();
                boolean missingOlKey = EpubMetadataExtractor.isBlank(r.openLibraryKey);
                if (!missingGenres && !missingOlKey) continue;

                File kfxFile = ExternalEpubActivity.resolveExistingKfxFile(ctx, new File(r.kfxPath));
                if (kfxFile == null || !kfxFile.exists()) continue;

                EpubMetadataExtractor.BookMeta meta = new EpubMetadataExtractor.BookMeta();
                meta.title = r.title;
                meta.author = r.author;
                meta.publisher = r.publisher;
                meta.pubDate = r.pubDate;
                meta.language = r.language;
                meta.isbn = r.isbn;
                meta.description = r.description;
                meta.openLibraryKey = r.openLibraryKey;
                meta.genres.addAll(r.genreList());

                File cov = (r.coverPath != null && !r.coverPath.isEmpty())
                        ? new File(r.coverPath)
                        : new File(kfxFile.getAbsolutePath() + ".cover.jpg");
                enqueueAsync(ctx, r.bookKey, kfxFile, cov, r.sourceName, meta, r.contentHash, r.sourceSize);
                queued++;
            }
        } catch (Throwable t) {
            Log.w(TAG, "sweepUnenrichedAsync failed: " + t.getMessage());
        } finally {
            db.close();
        }
        if (queued > 0) {
            Log.i(TAG, "Launch-time enrichment sweep queued " + queued + " book(s)");
        }
        return queued;
    }

    /**
     * Enqueues asynchronous Open Library enrichment onto a bounded daemon executor,
     * deduplicated per bookKey, persisting updates non-destructively via FolioMetadataDb.upsertBook
     * and re-registering private-dir books in kindle_library.db when enriched.
     */
    public static void enqueueAsync(
            final Context ctx,
            final String bookKey,
            final File kfxFile,
            final File coverFile,
            final String sourceName,
            final EpubMetadataExtractor.BookMeta meta,
            final String contentHash,
            final long sourceSize) {
        if (ctx == null || bookKey == null || bookKey.isEmpty() || kfxFile == null || meta == null) {
            return;
        }
        if (!isOnlineEnrichmentEnabled(ctx)) {
            Log.i(TAG, "Online enrichment disabled by user preference; skipping network lookup for " + bookKey);
            return;
        }
        if (!isNetworkConnected(ctx)) {
            Log.i(TAG, "Offline (ConnectivityManager); skipping network lookup for " + bookKey);
            return;
        }
        if (!IN_FLIGHT_KEYS.add(bookKey)) {
            Log.d(TAG, "Enrichment already queued for book_key=" + bookKey);
            return;
        }
        final Context appCtx = ctx.getApplicationContext();
        ENRICH_EXECUTOR.execute(() -> {
            try {
                boolean updated = enrichInPlace(appCtx, meta);
                FolioMetadataDb db = new FolioMetadataDb(appCtx);
                try {
                    db.upsertBook(bookKey, kfxFile, coverFile, sourceName, meta, contentHash, sourceSize);
                } finally {
                    db.close();
                }
                if (updated && isPrivateLibraryFile(appCtx, kfxFile)) {
                    FolioMetadataDb.registerInKindleLibraryDb(appCtx, bookKey, kfxFile, coverFile, meta);
                }
            } catch (Throwable t) {
                Log.w(TAG, "Async enrichment failed for " + bookKey + ": " + t.getMessage());
            } finally {
                IN_FLIGHT_KEYS.remove(bookKey);
            }
        });
    }

    public static void enrichInPlace(EpubMetadataExtractor.BookMeta meta) {
        enrichInPlace(null, meta);
    }

    public static boolean enrichInPlace(Context ctx, EpubMetadataExtractor.BookMeta meta) {
        if (meta == null) return false;
        boolean needsAuthor = isUnknownAuthor(meta.author);
        boolean needsPubDate = EpubMetadataExtractor.isBlank(meta.pubDate);
        boolean needsGenres = meta.genres.isEmpty();
        boolean needsPublisher = EpubMetadataExtractor.isBlank(meta.publisher);
        boolean needsOlKey = EpubMetadataExtractor.isBlank(meta.openLibraryKey);

        if (!needsAuthor && !needsPubDate && !needsGenres && !needsPublisher && !needsOlKey) {
            return false;
        }
        if (EpubMetadataExtractor.isBlank(meta.title) && EpubMetadataExtractor.isBlank(meta.isbn)) {
            return false;
        }
        if (!isNetworkConnected(ctx)) {
            Log.i(TAG, "Offline; skipping Open Library enrichment for '" + meta.title + "'");
            return false;
        }

        boolean enriched = false;
        // Attempt 1: ISBN lookup if available (limit=3)
        if (!EpubMetadataExtractor.isBlank(meta.isbn)) {
            try {
                String q = "isbn=" + URLEncoder.encode(meta.isbn.trim(), "UTF-8");
                JSONArray docs = queryDocs(ctx, q, 3);
                if (docs != null && validateAndApplyDocs(meta, docs)) {
                    enriched = true;
                }
            } catch (Throwable ignored) {}
        }

        // Attempt 2: Fallback to title + author if genres, pubDate, or openLibraryKey still missing
        if ((meta.genres.isEmpty() || EpubMetadataExtractor.isBlank(meta.openLibraryKey)
                || EpubMetadataExtractor.isBlank(meta.pubDate)
                || isUnknownAuthor(meta.author))
                && !EpubMetadataExtractor.isBlank(meta.title)) {
            try {
                StringBuilder q = new StringBuilder();
                q.append("title=").append(URLEncoder.encode(meta.title.trim(), "UTF-8"));
                if (!isUnknownAuthor(meta.author)) {
                    String primaryAuthor = meta.author.split(";")[0].trim();
                    if (!primaryAuthor.isEmpty()) {
                        q.append("&author=").append(URLEncoder.encode(primaryAuthor, "UTF-8"));
                    }
                }
                JSONArray docs = queryDocs(ctx, q.toString(), 3);
                if (docs != null && validateAndApplyDocs(meta, docs)) {
                    enriched = true;
                }
            } catch (Throwable ignored) {}
        }
        return enriched;
    }

    private static void logWarn(String msg) {
        try { Log.w(TAG, msg); } catch (Throwable ignored) {}
    }

    private static void logDebug(String msg) {
        try { Log.d(TAG, msg); } catch (Throwable ignored) {}
    }

    private static JSONArray queryDocs(Context ctx, String queryParams, int limit) {
        if (!isNetworkConnected(ctx)) return null;
        String ua = buildUserAgent(ctx);
        String urlStr = "https://openlibrary.org/search.json?" + queryParams
                + "&fields=key,title,author_name,first_publish_year,subject,publisher,isbn&limit=" + limit;
        return queryDocsUrl(urlStr, ua);
    }

    public static JSONArray queryDocsUrl(String urlStr, String ua) {
        String body = queryDocsRawJsonUrl(urlStr, ua);
        if (body == null) return null;
        try {
            JSONObject root = new JSONObject(body);
            JSONArray docs = root.optJSONArray("docs");
            if (docs == null || docs.length() == 0) return null;
            return docs;
        } catch (Throwable t) {
            logDebug("Open Library JSON parse failed: " + t.getMessage());
            return null;
        }
    }

    /** Executes HTTP GET with Open Library rate-limiting and 429/503 Retry-After backoff; testable on host JVM. */
    public static String queryDocsRawJsonUrl(String urlStr, String ua) {
        if (urlStr == null || urlStr.isEmpty()) return null;

        for (int attempt = 0; attempt <= MAX_HTTP_RETRIES; attempt++) {
            throttleOpenLibraryRequest();
            HttpURLConnection conn = null;
            InputStream in = null;
            try {
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
                conn.setReadTimeout(READ_TIMEOUT_MS);
                conn.setRequestProperty("User-Agent", ua);
                conn.setRequestProperty("Accept", "application/json");

                int code = conn.getResponseCode();
                if (code == 429 || code == 503) {
                    if (attempt < MAX_HTTP_RETRIES) {
                        long sleepMs = parseRetryAfterMs(conn.getHeaderField("Retry-After"), attempt);
                        logWarn("Open Library HTTP " + code + "; backing off " + sleepMs + "ms");
                        try { Thread.sleep(sleepMs); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return null; }
                        continue;
                    }
                    return null;
                }
                if (code != 200) return null;

                in = conn.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int total = 0;
                int n;
                while ((n = in.read(buf)) > 0) {
                    total += n;
                    if (total > MAX_JSON_BYTES) {
                        logWarn("Aborting Open Library JSON response exceeding " + MAX_JSON_BYTES + " bytes");
                        return null;
                    }
                    baos.write(buf, 0, n);
                }

                return new String(baos.toByteArray(), StandardCharsets.UTF_8);
            } catch (Throwable t) {
                logDebug("Open Library query failed: " + t.getMessage());
                return null;
            } finally {
                if (in != null) {
                    try { in.close(); } catch (Throwable ignored) {}
                }
                if (conn != null) {
                    try { conn.disconnect(); } catch (Throwable ignored) {}
                }
            }
        }
        return null;
    }

    private static String normalizeTitle(String raw) {
        if (raw == null) return "";
        String s = raw.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (s.startsWith("the ")) s = s.substring(4).trim();
        else if (s.startsWith("an ")) s = s.substring(3).trim();
        else if (s.startsWith("a ")) s = s.substring(2).trim();
        return s;
    }

    private static String normalizeIsbn(String raw) {
        if (raw == null) return "";
        return raw.replaceAll("[^0-9Xx]", "").toUpperCase(Locale.ROOT);
    }

    private static Set<String> tokens(String normalized) {
        if (normalized == null || normalized.isEmpty()) return new HashSet<>();
        Set<String> out = new HashSet<>();
        for (String tok : normalized.split("\\s+")) {
            if (!tok.isEmpty()) out.add(tok);
        }
        return out;
    }

    private static boolean isUnknownAuthor(String author) {
        return author == null || author.trim().isEmpty()
                || "Unknown Author".equalsIgnoreCase(author.trim());
    }

    private static String normalizeAuthorKey(String raw) {
        if (raw == null) return "";
        String norm = EpubMetadataExtractor.normalizeAuthor(raw);
        return norm.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static boolean isTitleMatch(String epubTitle, String olTitle) {
        String ntEpub = normalizeTitle(epubTitle);
        String ntOl = normalizeTitle(olTitle);
        if (ntEpub.isEmpty() || ntOl.isEmpty()) return false;
        if (ntEpub.equals(ntOl)) return true;
        Set<String> ta = tokens(ntEpub);
        Set<String> tb = tokens(ntOl);
        int inter = 0;
        for (String t : ta) {
            if (tb.contains(t)) inter++;
        }
        int union = ta.size() + tb.size() - inter;
        double jaccard = union > 0 ? ((double) inter / (double) union) : 0.0;
        return jaccard >= 0.75;
    }

    private static boolean authorMatches(String epubAuthor, String olAuthor) {
        if (isUnknownAuthor(epubAuthor)) {
            return false;
        }
        if (olAuthor == null || olAuthor.trim().isEmpty()) return false;
        String na = normalizeAuthorKey(epubAuthor);
        String nb = normalizeAuthorKey(olAuthor);
        if (na.isEmpty() || nb.isEmpty()) return false;
        if (na.equals(nb)) return true;
        Set<String> ta = tokens(na);
        Set<String> tb = tokens(nb);
        for (String t : ta) {
            if (t.length() >= 3 && tb.contains(t)) return true;
        }
        return false;
    }

    /**
     * Pure, Android-free Open Library single-candidate predicate.
     * When epubAuthor is blank or "Unknown Author", ONLY exact normalized ISBN matches are accepted here;
     * title-only unknown-author matches require multi-doc consensus via selectAcceptedCandidateIndex(...).
     */
    public static boolean isAcceptableMatch(
            String epubTitle, String epubAuthor, String epubIsbn,
            String olTitle, String olAuthor, String olIsbn) {
        String nEpubIsbn = normalizeIsbn(epubIsbn);
        String nOlIsbn = normalizeIsbn(olIsbn);
        if (!nEpubIsbn.isEmpty() && !nOlIsbn.isEmpty() && nEpubIsbn.equals(nOlIsbn)) {
            return true;
        }
        if (isUnknownAuthor(epubAuthor)) {
            return false;
        }
        return isTitleMatch(epubTitle, olTitle) && authorMatches(epubAuthor, olAuthor);
    }

    /**
     * Pure, Android-free limit=3 consensus validator.
     * For unknown/blank authors without an ISBN match, accepts index 0 ONLY when ALL returned results
     * agree on the exact same normalized first author AND candidate 0 passes title matching.
     */
    public static int selectAcceptedCandidateIndex(
            String epubTitle, String epubAuthor, String epubIsbn,
            List<String> olTitles, List<String> olFirstAuthors, List<String> olIsbns) {
        if (olTitles == null || olFirstAuthors == null || olTitles.isEmpty()
                || olTitles.size() != olFirstAuthors.size()) {
            return -1;
        }
        int n = olTitles.size();
        for (int i = 0; i < n; i++) {
            String olIsbn = (olIsbns != null && i < olIsbns.size()) ? olIsbns.get(i) : "";
            if (!isUnknownAuthor(epubAuthor)) {
                if (isAcceptableMatch(epubTitle, epubAuthor, epubIsbn, olTitles.get(i), olFirstAuthors.get(i), olIsbn)) {
                    return i;
                }
            } else {
                String nEpubIsbn = normalizeIsbn(epubIsbn);
                String nOlIsbn = normalizeIsbn(olIsbn);
                if (!nEpubIsbn.isEmpty() && !nOlIsbn.isEmpty() && nEpubIsbn.equals(nOlIsbn)) {
                    return i;
                }
            }
        }
        if (isUnknownAuthor(epubAuthor)) {
            String consensusAuthor = normalizeAuthorKey(olFirstAuthors.get(0));
            if (consensusAuthor.isEmpty()) return -1;
            for (int i = 1; i < n; i++) {
                if (!consensusAuthor.equals(normalizeAuthorKey(olFirstAuthors.get(i)))) {
                    return -1;
                }
            }
            if (isTitleMatch(epubTitle, olTitles.get(0))) {
                return 0;
            }
        }
        return -1;
    }

    private static boolean validateAndApplyDocs(EpubMetadataExtractor.BookMeta meta, JSONArray docs) {
        int n = docs.length();
        List<String> titles = new ArrayList<>(n);
        List<String> authors = new ArrayList<>(n);
        List<String> isbns = new ArrayList<>(n);
        String targetIsbn = !EpubMetadataExtractor.isBlank(meta.isbn) ? normalizeIsbn(meta.isbn) : "";

        for (int i = 0; i < n; i++) {
            JSONObject doc = docs.optJSONObject(i);
            if (doc == null) {
                titles.add("");
                authors.add("");
                isbns.add("");
                continue;
            }
            titles.add(doc.optString("title", "").trim());
            JSONArray aArr = doc.optJSONArray("author_name");
            authors.add(aArr != null && aArr.length() > 0 ? aArr.optString(0, "").trim() : "");

            String matchedIsbn = "";
            JSONArray iArr = doc.optJSONArray("isbn");
            if (iArr != null && !targetIsbn.isEmpty()) {
                for (int j = 0; j < iArr.length(); j++) {
                    String cand = iArr.optString(j, "").trim();
                    if (normalizeIsbn(cand).equals(targetIsbn)) {
                        matchedIsbn = cand;
                        break;
                    }
                }
            }
            if (matchedIsbn.isEmpty() && iArr != null && iArr.length() > 0) {
                matchedIsbn = iArr.optString(0, "").trim();
            }
            isbns.add(matchedIsbn);
        }

        int chosen = selectAcceptedCandidateIndex(meta.title, meta.author, meta.isbn, titles, authors, isbns);
        if (chosen < 0) {
            Log.i(TAG, "Rejected mismatched Open Library candidates (count=" + n + ") for epub=('"
                    + meta.title + "', '" + meta.author + "')");
            return false;
        }
        JSONObject chosenDoc = docs.optJSONObject(chosen);
        if (chosenDoc == null) return false;
        applyDoc(meta, chosenDoc);
        return true;
    }

    private static void applyDoc(EpubMetadataExtractor.BookMeta meta, JSONObject doc) {
        boolean needsAuthor = EpubMetadataExtractor.isBlank(meta.author)
                || "Unknown Author".equalsIgnoreCase(meta.author.trim());
        if (needsAuthor) {
            JSONArray authors = doc.optJSONArray("author_name");
            if (authors != null && authors.length() > 0) {
                String a = authors.optString(0, "").trim();
                if (!a.isEmpty()) meta.author = a;
            }
        }

        if (EpubMetadataExtractor.isBlank(meta.pubDate)) {
            int year = doc.optInt("first_publish_year", 0);
            if (year > 0) meta.pubDate = String.valueOf(year);
        }

        if (meta.genres.isEmpty()) {
            JSONArray subjects = doc.optJSONArray("subject");
            if (subjects != null) {
                List<String> rawSubjects = new ArrayList<>(subjects.length());
                for (int i = 0; i < subjects.length(); i++) {
                    String s = subjects.optString(i, "").trim();
                    if (!s.isEmpty()) {
                        rawSubjects.add(s);
                    }
                }
                meta.genres.addAll(
                        EpubMetadataExtractor.cleanAndRankGenres(rawSubjects, null, meta.author));
            }
        }

        if (EpubMetadataExtractor.isBlank(meta.publisher)) {
            JSONArray pubs = doc.optJSONArray("publisher");
            if (pubs != null && pubs.length() > 0) {
                String p = pubs.optString(0, "").trim();
                if (!p.isEmpty()) meta.publisher = p;
            }
        }

        if (EpubMetadataExtractor.isBlank(meta.isbn)) {
            JSONArray isbns = doc.optJSONArray("isbn");
            if (isbns != null && isbns.length() > 0) {
                String id = isbns.optString(0, "").trim();
                if (!id.isEmpty()) meta.isbn = id;
            }
        }

        if (EpubMetadataExtractor.isBlank(meta.openLibraryKey)) {
            String key = doc.optString("key", "").trim();
            if (!key.isEmpty()) meta.openLibraryKey = key;
        }
    }
}
