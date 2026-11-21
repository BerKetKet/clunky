package com.amazon.kindle.epub.catalog;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Queries Open Library API for book ratings, ISBN, description, similar books,
 * and personalized cover-illustrated recommendations seeded from the user's Library.
 */
public final class OpenLibraryClient {
    public static final class BookMeta {
        public String workKey = "";
        public String isbn = "";
        public double avgRating = 0.0;
        public int ratingsCount = 0;
        public String description = "";
        public List<String> similarTitles = new ArrayList<String>();
        public String coverUrl = "";
    }

    public static BookMeta fetchMeta(String title, String author) {
        BookMeta meta = new BookMeta();
        try {
            String q = URLEncoder.encode(title + " " + author, "UTF-8");
            String searchJson = GutendexSource.httpGet("https://openlibrary.org/search.json?q=" + q + "&limit=1&fields=key,title,author_name,cover_i,isbn,subject");
            JSONObject root = new JSONObject(searchJson);
            JSONArray docs = root.optJSONArray("docs");
            if (docs == null || docs.length() == 0) return meta;
            JSONObject doc = docs.optJSONObject(0);
            if (doc == null) return meta;
            meta.workKey = doc.optString("key", "");
            int coverI = doc.optInt("cover_i", 0);
            if (coverI > 0) {
                meta.coverUrl = "https://covers.openlibrary.org/b/id/" + coverI + "-M.jpg";
            }
            JSONArray isbns = doc.optJSONArray("isbn");
            if (isbns != null && isbns.length() > 0) {
                meta.isbn = isbns.optString(0, "");
            }

            if (!meta.workKey.isEmpty()) {
                try {
                    String rJson = GutendexSource.httpGet("https://openlibrary.org" + meta.workKey + "/ratings.json");
                    JSONObject rRoot = new JSONObject(rJson);
                    JSONObject summary = rRoot.optJSONObject("summary");
                    if (summary != null) {
                        meta.avgRating = summary.optDouble("average", 0.0);
                        meta.ratingsCount = summary.optInt("count", 0);
                    }
                } catch (Throwable t) { android.util.Log.e("OpenLibraryClient", "Ratings error", t); }

                try {
                    String wJson = GutendexSource.httpGet("https://openlibrary.org" + meta.workKey + ".json");
                    JSONObject wRoot = new JSONObject(wJson);
                    Object descObj = wRoot.opt("description");
                    if (descObj instanceof String) {
                        meta.description = (String) descObj;
                    } else if (descObj instanceof JSONObject) {
                        meta.description = ((JSONObject) descObj).optString("value", "");
                    }
                } catch (Throwable t) { android.util.Log.e("OpenLibraryClient", "Work error", t); }
            }

            JSONArray subjects = doc.optJSONArray("subject");
            if (subjects != null) {
                for (int si = 0; si < Math.min(12, subjects.length()) && meta.similarTitles.size() < 4; si++) {
                    String subj = normalizeSubject(subjects.optString(si, ""));
                    if (subj.isEmpty() || subj.length() > 36) continue;
                    try {
                        String sJson = GutendexSource.httpGet("https://openlibrary.org/subjects/" + subj + ".json?limit=8");
                        JSONObject sRoot = new JSONObject(sJson);
                        JSONArray works = sRoot.optJSONArray("works");
                        if (works != null) {
                            for (int i = 0; i < works.length() && meta.similarTitles.size() < 4; i++) {
                                JSONObject w = works.optJSONObject(i);
                                if (w == null) continue;
                                String wt = w.optString("title", "");
                                if (!wt.isEmpty() && !wt.equalsIgnoreCase(title) && !meta.similarTitles.contains(wt)) {
                                    meta.similarTitles.add(wt);
                                }
                            }
                        }
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable t) {
            android.util.Log.e("OpenLibraryClient", "Outer error", t);
        }
        return meta;
    }

    /**
     * Fetches rich cover-illustrated recommendations from Open Library based on a seed book
     * in the user's local Library.
     */
    public static List<BookResult> fetchRecommendations(String seedTitle, String seedAuthor, int limit) {
        List<BookResult> recs = new ArrayList<BookResult>();
        Set<String> seen = new HashSet<String>();
        seen.add(seedTitle.toLowerCase(Locale.US));
        try {
            String q = URLEncoder.encode(seedTitle + " " + seedAuthor, "UTF-8");
            String searchJson = GutendexSource.httpGet("https://openlibrary.org/search.json?q=" + q + "&limit=1&fields=key,title,subject");
            JSONObject root = new JSONObject(searchJson);
            JSONArray docs = root.optJSONArray("docs");
            if (docs == null || docs.length() == 0) return recs;
            JSONObject doc = docs.optJSONObject(0);
            if (doc == null) return recs;
            JSONArray subjects = doc.optJSONArray("subject");
            if (subjects == null) return recs;

            for (int si = 0; si < Math.min(18, subjects.length()) && recs.size() < limit; si++) {
                String subj = normalizeSubject(subjects.optString(si, ""));
                if (subj.isEmpty() || subj.length() > 32) continue;
                try {
                    String sJson = GutendexSource.httpGet("https://openlibrary.org/subjects/" + subj + ".json?limit=14");
                    JSONObject sRoot = new JSONObject(sJson);
                    JSONArray works = sRoot.optJSONArray("works");
                    if (works == null) continue;
                    for (int i = 0; i < works.length() && recs.size() < limit; i++) {
                        JSONObject w = works.optJSONObject(i);
                        if (w == null) continue;
                        String wt = w.optString("title", "").trim();
                        int coverId = w.optInt("cover_id", 0);
                        if (wt.isEmpty() || coverId <= 0) continue;
                        String keyLow = wt.toLowerCase(Locale.US);
                        if (seen.contains(keyLow)) continue;
                        seen.add(keyLow);

                        String auth = "Unknown Author";
                        JSONArray authors = w.optJSONArray("authors");
                        if (authors != null && authors.length() > 0) {
                            JSONObject a0 = authors.optJSONObject(0);
                            if (a0 != null) auth = a0.optString("name", auth);
                        }
                        String coverUrl = "https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg";
                        recs.add(new BookResult(
                                "ol_" + coverId,
                                "Open Library",
                                wt,
                                auth,
                                "", // Will resolve via Gutendex/OPDS when user taps Download & Read
                                coverUrl,
                                "",
                                0
                        ));
                    }
                } catch (Throwable ignored) {}
            }
            // Tier 2 fallback: same-author books from Open Library (e.g. Metro 2033 / Metro 2035 for Metro 2034!)
            if (recs.size() < 6 && seedAuthor != null && !seedAuthor.isEmpty() && !seedAuthor.equalsIgnoreCase("Unknown Author")) {
                try {
                    String aq = URLEncoder.encode(seedAuthor, "UTF-8");
                    String aJson = GutendexSource.httpGet("https://openlibrary.org/search.json?author=" + aq + "&limit=12&fields=title,author_name,cover_i");
                    JSONObject aRoot = new JSONObject(aJson);
                    JSONArray aDocs = aRoot.optJSONArray("docs");
                    if (aDocs != null) {
                        for (int i = 0; i < aDocs.length() && recs.size() < limit; i++) {
                            JSONObject ad = aDocs.optJSONObject(i);
                            if (ad == null) continue;
                            String wt = ad.optString("title", "").trim();
                            int coverId = ad.optInt("cover_i", 0);
                            if (wt.isEmpty() || coverId <= 0) continue;
                            String keyLow = wt.toLowerCase(Locale.US);
                            if (seen.contains(keyLow)) continue;
                            seen.add(keyLow);
                            String auth = seedAuthor;
                            JSONArray an = ad.optJSONArray("author_name");
                            if (an != null && an.length() > 0) auth = an.optString(0, auth);
                            String coverUrl = "https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg";
                            recs.add(new BookResult("ol_" + coverId, "Open Library", wt, auth, "", coverUrl, "", 0));
                        }
                    }
                } catch (Throwable ignored) {}
            }

            // Tier 3 fallback: sci-fi / speculative / adventure subject shelf if still < 4
            if (recs.size() < 4) {
                String fallbackSubj = "science_fiction";
                String lt = seedTitle.toLowerCase(Locale.US);
                if (lt.contains("wonderland") || lt.contains("oz") || lt.contains("tower")) fallbackSubj = "fantasy";
                else if (lt.contains("moby") || lt.contains("treasure")) fallbackSubj = "adventure";
                else if (lt.contains("pride") || lt.contains("jane")) fallbackSubj = "romance";
                else if (lt.contains("frankenstein") || lt.contains("dracula")) fallbackSubj = "horror";
                try {
                    String sJson = GutendexSource.httpGet("https://openlibrary.org/subjects/" + fallbackSubj + ".json?limit=12");
                    JSONObject sRoot = new JSONObject(sJson);
                    JSONArray works = sRoot.optJSONArray("works");
                    if (works != null) {
                        for (int i = 0; i < works.length() && recs.size() < limit; i++) {
                            JSONObject w = works.optJSONObject(i);
                            if (w == null) continue;
                            String wt = w.optString("title", "").trim();
                            int coverId = w.optInt("cover_id", 0);
                            if (wt.isEmpty() || coverId <= 0) continue;
                            String keyLow = wt.toLowerCase(Locale.US);
                            if (seen.contains(keyLow)) continue;
                            seen.add(keyLow);
                            String auth = "Unknown Author";
                            JSONArray authors = w.optJSONArray("authors");
                            if (authors != null && authors.length() > 0) {
                                JSONObject a0 = authors.optJSONObject(0);
                                if (a0 != null) auth = a0.optString("name", auth);
                            }
                            String coverUrl = "https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg";
                            recs.add(new BookResult("ol_" + coverId, "Open Library", wt, auth, "", coverUrl, "", 0));
                        }
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable t) {
            android.util.Log.e("OpenLibraryClient", "Recs error", t);
        }
        return recs;
    }

    private static String normalizeSubject(String raw) {
        if (raw == null) return "";
        String s = raw.toLowerCase(Locale.US).trim();
        if (s.contains("accessible book") || s.contains("protected daisy") || s.contains("in library")
                || s.contains("lending") || s.contains("staff picks") || s.contains("bestseller")
                || s.contains("translation") || s.contains("reading level") || s.contains("large type")) {
            return "";
        }
        s = s.replace("-", "_").replace(" ", "_").replaceAll("[^a-z0-9_]", "");
        while (s.contains("__")) s = s.replace("__", "_");
        // Skip overly generic subjects that produce off-genre recommendations (e.g. Wizard of Oz for Metro 2034)
        String[] generic = new String[]{
            "fiction", "novel", "novels", "literature", "general", "stories", "books",
            "english", "classic", "classics", "juvenile_fiction", "childrens_stories",
            "open_library_staff_picks", "fiction_general", "popular_works"
        };
        for (String g : generic) {
            if (s.equals(g)) return "";
        }
        return s;
    }
}
