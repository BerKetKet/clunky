package com.amazon.kindle.epub.ingest;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
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
    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int READ_TIMEOUT_MS = 15000;

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

    /**
     * Enqueues asynchronous Open Library enrichment onto a bounded daemon executor,
     * deduplicated per bookKey, persisting updates non-destructively via FolioMetadataDb.upsertBook.
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
        if (!IN_FLIGHT_KEYS.add(bookKey)) {
            Log.d(TAG, "Enrichment already queued for book_key=" + bookKey);
            return;
        }
        final Context appCtx = ctx.getApplicationContext();
        ENRICH_EXECUTOR.execute(() -> {
            try {
                enrichInPlace(meta);
                FolioMetadataDb db = new FolioMetadataDb(appCtx);
                try {
                    db.upsertBook(bookKey, kfxFile, coverFile, sourceName, meta, contentHash, sourceSize);
                } finally {
                    db.close();
                }
            } catch (Throwable t) {
                Log.w(TAG, "Async enrichment failed for " + bookKey + ": " + t.getMessage());
            } finally {
                IN_FLIGHT_KEYS.remove(bookKey);
            }
        });
    }

    public static void enrichInPlace(EpubMetadataExtractor.BookMeta meta) {
        if (meta == null) return;
        boolean needsAuthor = EpubMetadataExtractor.isBlank(meta.author)
                || "Unknown Author".equalsIgnoreCase(meta.author.trim());
        boolean needsPubDate = EpubMetadataExtractor.isBlank(meta.pubDate);
        boolean needsGenres = meta.genres.isEmpty();
        boolean needsPublisher = EpubMetadataExtractor.isBlank(meta.publisher);

        if (!needsAuthor && !needsPubDate && !needsGenres && !needsPublisher) {
            return;
        }
        if (EpubMetadataExtractor.isBlank(meta.title) && EpubMetadataExtractor.isBlank(meta.isbn)) {
            return;
        }

        // Attempt 1: ISBN lookup if available
        if (!EpubMetadataExtractor.isBlank(meta.isbn)) {
            try {
                String q = "isbn=" + URLEncoder.encode(meta.isbn.trim(), "UTF-8");
                JSONObject doc = queryFirstDoc(q);
                if (doc != null && validateAndApplyDoc(meta, doc)) {
                    // matched
                }
            } catch (Throwable ignored) {}
        }

        // Attempt 2: Fallback to title + author if genres or openLibraryKey still missing
        if ((meta.genres.isEmpty() || EpubMetadataExtractor.isBlank(meta.openLibraryKey)
                || EpubMetadataExtractor.isBlank(meta.pubDate))
                && !EpubMetadataExtractor.isBlank(meta.title)) {
            try {
                StringBuilder q = new StringBuilder();
                q.append("title=").append(URLEncoder.encode(meta.title.trim(), "UTF-8"));
                if (!EpubMetadataExtractor.isBlank(meta.author)
                        && !"Unknown Author".equalsIgnoreCase(meta.author.trim())) {
                    String primaryAuthor = meta.author.split(";")[0].trim();
                    if (!primaryAuthor.isEmpty()) {
                        q.append("&author=").append(URLEncoder.encode(primaryAuthor, "UTF-8"));
                    }
                }
                JSONObject doc = queryFirstDoc(q.toString());
                if (doc != null) {
                    validateAndApplyDoc(meta, doc);
                }
            } catch (Throwable ignored) {}
        }
    }

    private static JSONObject queryFirstDoc(String queryParams) {
        HttpURLConnection conn = null;
        try {
            String urlStr = "https://openlibrary.org/search.json?" + queryParams
                    + "&fields=key,title,author_name,first_publish_year,subject,publisher,isbn&limit=1";
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestProperty("User-Agent", "FolioReader/3.9 (Android; Local-First EPUB)");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) return null;

            StringBuilder body = new StringBuilder();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) body.append(line);
            br.close();

            JSONObject root = new JSONObject(body.toString());
            JSONArray docs = root.optJSONArray("docs");
            if (docs == null || docs.length() == 0) return null;
            return docs.getJSONObject(0);
        } catch (Throwable ignored) {
            return null;
        } finally {
            if (conn != null) {
                try { conn.disconnect(); } catch (Throwable ignored) {}
            }
        }
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

    private static boolean authorMatches(String epubAuthor, String olAuthor) {
        if (epubAuthor == null || epubAuthor.trim().isEmpty()
                || "Unknown Author".equalsIgnoreCase(epubAuthor.trim())) {
            return true;
        }
        if (olAuthor == null || olAuthor.trim().isEmpty()) return false;
        String na = epubAuthor.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
        String nb = olAuthor.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
        if (na.equals(nb)) return true;
        Set<String> ta = tokens(na);
        Set<String> tb = tokens(nb);
        for (String t : ta) {
            if (t.length() >= 3 && tb.contains(t)) return true;
        }
        return false;
    }

    /**
     * Pure, Android-free Open Library match predicate for JVM unit testing and runtime validation.
     */
    public static boolean isAcceptableMatch(
            String epubTitle, String epubAuthor, String epubIsbn,
            String olTitle, String olAuthor, String olIsbn) {
        String nEpubIsbn = normalizeIsbn(epubIsbn);
        String nOlIsbn = normalizeIsbn(olIsbn);
        if (!nEpubIsbn.isEmpty() && !nOlIsbn.isEmpty() && nEpubIsbn.equals(nOlIsbn)) {
            return true;
        }

        String ntEpub = normalizeTitle(epubTitle);
        String ntOl = normalizeTitle(olTitle);
        if (ntEpub.isEmpty() || ntOl.isEmpty()) return false;

        boolean titleOk;
        if (ntEpub.equals(ntOl)) {
            titleOk = true;
        } else {
            Set<String> ta = tokens(ntEpub);
            Set<String> tb = tokens(ntOl);
            int inter = 0;
            for (String t : ta) {
                if (tb.contains(t)) inter++;
            }
            int union = ta.size() + tb.size() - inter;
            double jaccard = union > 0 ? ((double) inter / (double) union) : 0.0;
            titleOk = jaccard >= 0.75;
        }
        return titleOk && authorMatches(epubAuthor, olAuthor);
    }

    private static boolean validateAndApplyDoc(EpubMetadataExtractor.BookMeta meta, JSONObject doc) {
        String olTitle = doc.optString("title", "").trim();
        String olAuthor = "";
        JSONArray authors = doc.optJSONArray("author_name");
        if (authors != null && authors.length() > 0) {
            olAuthor = authors.optString(0, "").trim();
        }
        String olIsbn = "";
        JSONArray isbns = doc.optJSONArray("isbn");
        if (isbns != null && !EpubMetadataExtractor.isBlank(meta.isbn)) {
            String targetIsbn = normalizeIsbn(meta.isbn);
            for (int i = 0; i < isbns.length(); i++) {
                String cand = isbns.optString(i, "").trim();
                if (normalizeIsbn(cand).equals(targetIsbn)) {
                    olIsbn = cand;
                    break;
                }
            }
        }
        if (olIsbn.isEmpty() && isbns != null && isbns.length() > 0) {
            olIsbn = isbns.optString(0, "").trim();
        }

        if (!isAcceptableMatch(meta.title, meta.author, meta.isbn, olTitle, olAuthor, olIsbn)) {
            Log.i(TAG, "Rejected mismatched Open Library candidate: epub=('" + meta.title
                    + "', '" + meta.author + "') vs ol=('" + olTitle + "', '" + olAuthor + "')");
            return false;
        }
        applyDoc(meta, doc);
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
                for (int i = 0; i < subjects.length() && meta.genres.size() < 5; i++) {
                    String s = subjects.optString(i, "").trim();
                    if (!s.isEmpty() && s.length() <= 45 && !meta.genres.contains(s)) {
                        meta.genres.add(s);
                    }
                }
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
