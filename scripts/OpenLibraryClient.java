package com.amazon.kindle.epub.catalog;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Queries Open Library API for rich book metadata (ratings, synopsis, year, subjects)
 * and fine-grained personalized recommendations seeded from ANY book in the user's Library,
 * regardless of whether a free EPUB download exists.
 */
public final class OpenLibraryClient {
    public static final class BookMeta {
        public String workKey = "";
        public String isbn = "";
        public int firstPublishYear = 0;
        public double avgRating = 0.0;
        public int ratingsCount = 0;
        public String description = "";
        public List<String> subjects = new ArrayList<String>();
        public List<String> similarTitles = new ArrayList<String>();
        public String coverUrl = "";
    }

    public static BookMeta fetchMeta(String title, String author) {
        BookMeta meta = new BookMeta();
        try {
            String q = URLEncoder.encode(title + (author != null && !author.isEmpty() ? (" " + author) : ""), "UTF-8");
            String searchJson = GutendexSource.httpGet(
                    "https://openlibrary.org/search.json?q=" + q +
                    "&limit=1&fields=key,title,author_name,cover_i,isbn,subject,first_publish_year,ratings_average,ratings_count"
            );
            JSONObject root = new JSONObject(searchJson);
            JSONArray docs = root.optJSONArray("docs");
            if (docs == null || docs.length() == 0) return meta;
            JSONObject doc = docs.optJSONObject(0);
            if (doc == null) return meta;

            meta.workKey = doc.optString("key", "");
            meta.firstPublishYear = doc.optInt("first_publish_year", 0);
            meta.avgRating = doc.optDouble("ratings_average", 0.0);
            meta.ratingsCount = doc.optInt("ratings_count", 0);
            int coverI = doc.optInt("cover_i", 0);
            if (coverI > 0) {
                meta.coverUrl = "https://covers.openlibrary.org/b/id/" + coverI + "-M.jpg";
            }
            JSONArray isbns = doc.optJSONArray("isbn");
            if (isbns != null && isbns.length() > 0) {
                meta.isbn = isbns.optString(0, "");
            }

            // Collect clean human-readable subjects
            JSONArray subjArr = doc.optJSONArray("subject");
            if (subjArr != null) {
                for (int i = 0; i < subjArr.length() && meta.subjects.size() < 6; i++) {
                    String raw = subjArr.optString(i, "").trim();
                    if (!raw.isEmpty() && !isGenericOrNoisySubject(raw) && raw.length() <= 40) {
                        meta.subjects.add(raw);
                    }
                }
            }

            if (!meta.workKey.isEmpty()) {
                if (meta.ratingsCount == 0) {
                    try {
                        String rJson = GutendexSource.httpGet("https://openlibrary.org" + meta.workKey + "/ratings.json");
                        JSONObject rRoot = new JSONObject(rJson);
                        JSONObject summary = rRoot.optJSONObject("summary");
                        if (summary != null) {
                            meta.avgRating = summary.optDouble("average", 0.0);
                            meta.ratingsCount = summary.optInt("count", 0);
                        }
                    } catch (Throwable ignored) {}
                }

                try {
                    String wJson = GutendexSource.httpGet("https://openlibrary.org" + meta.workKey + ".json");
                    JSONObject wRoot = new JSONObject(wJson);
                    Object descObj = wRoot.opt("description");
                    if (descObj instanceof String) {
                        meta.description = (String) descObj;
                    } else if (descObj instanceof JSONObject) {
                        meta.description = ((JSONObject) descObj).optString("value", "");
                    }
                    // Also harvest curated work-level subjects if search subjects were sparse
                    if (meta.subjects.size() < 3) {
                        JSONArray wSubj = wRoot.optJSONArray("subjects");
                        if (wSubj != null) {
                            for (int i = 0; i < wSubj.length() && meta.subjects.size() < 6; i++) {
                                String raw = wSubj.optString(i, "").trim();
                                if (!raw.isEmpty() && !isGenericOrNoisySubject(raw) && raw.length() <= 40 && !meta.subjects.contains(raw)) {
                                    meta.subjects.add(raw);
                                }
                            }
                        }
                    }
                } catch (Throwable ignored) {}
            }

            // Populate similarTitles from fine-grained recommendations
            List<BookResult> sims = fetchRecommendations(title, author, 4);
            for (BookResult br : sims) {
                if (br.title != null && !br.title.isEmpty() && !meta.similarTitles.contains(br.title)) {
                    meta.similarTitles.add(br.title);
                }
            }
        } catch (Throwable t) {
            android.util.Log.e("OpenLibraryClient", "fetchMeta error", t);
        }
        return meta;
    }

    /**
     * Fine-grained, completely general recommendations from Open Library for ANY book:
     * 1. Looks up the seed work's curated subjects, author_key, and publication era.
     * 2. Intersects the top 2 specific subjects via Open Library Search API (not broad /subjects/ dumps)
     *    filtered by publication era proximity so modern sci-fi recommends modern sci-fi,
     *    not 19th-century generic classics.
     * 3. Merges same-author works from Open Library (e.g. sequels and companion novels).
     * 4. Works identically for commercial books with no free EPUB download.
     */
    public static List<BookResult> fetchRecommendations(String seedTitle, String seedAuthor, int limit) {
        List<BookResult> recs = new ArrayList<BookResult>();
        Set<String> seenTitles = new HashSet<String>();
        String normSeed = normalizeTitleForDedup(seedTitle);
        seenTitles.add(normSeed);

        try {
            String titleQ = URLEncoder.encode(seedTitle == null ? "" : seedTitle.trim(), "UTF-8");
            String authorQ = URLEncoder.encode(seedAuthor == null ? "" : seedAuthor.trim(), "UTF-8");
            String lookupUrl = (seedAuthor != null && !seedAuthor.trim().isEmpty())
                    ? ("https://openlibrary.org/search.json?title=" + titleQ + "&author=" + authorQ + "&limit=1&fields=key,title,author_name,author_key,subject,first_publish_year")
                    : ("https://openlibrary.org/search.json?q=" + titleQ + "&limit=1&fields=key,title,author_name,author_key,subject,first_publish_year");

            String searchJson = GutendexSource.httpGet(lookupUrl);
            JSONObject root = new JSONObject(searchJson);
            JSONArray docs = root.optJSONArray("docs");
            if ((docs == null || docs.length() == 0) && seedAuthor != null && !seedAuthor.trim().isEmpty()) {
                // Retry title-only lookup if author formatting differed
                searchJson = GutendexSource.httpGet(
                        "https://openlibrary.org/search.json?title=" + titleQ + "&limit=1&fields=key,title,author_name,author_key,subject,first_publish_year"
                );
                root = new JSONObject(searchJson);
                docs = root.optJSONArray("docs");
            }
            if (docs == null || docs.length() == 0) return recs;
            JSONObject doc = docs.optJSONObject(0);
            if (doc == null) return recs;

            String workKey = doc.optString("key", "");
            int seedYear = doc.optInt("first_publish_year", 0);
            String authorKey = "";
            JSONArray akArr = doc.optJSONArray("author_key");
            if (akArr != null && akArr.length() > 0) authorKey = akArr.optString(0, "");
            String resolvedAuthor = seedAuthor;
            JSONArray anArr = doc.optJSONArray("author_name");
            if (anArr != null && anArr.length() > 0) resolvedAuthor = anArr.optString(0, seedAuthor);

            // Gather specific (non-generic) subjects from both work record and search doc
            List<String> specificSubjects = new ArrayList<String>();
            if (!workKey.isEmpty()) {
                try {
                    String wJson = GutendexSource.httpGet("https://openlibrary.org" + workKey + ".json");
                    JSONObject wRoot = new JSONObject(wJson);
                    JSONArray wSubj = wRoot.optJSONArray("subjects");
                    collectSpecificSubjects(wSubj, specificSubjects, 10);
                } catch (Throwable ignored) {}
            }
            collectSpecificSubjects(doc.optJSONArray("subject"), specificSubjects, 12);

            // Rank subjects by specificity (multi-word specific genre/theme tags first, avoiding huge single-word buckets)
            Collections.sort(specificSubjects, new Comparator<String>() {
                @Override
                public int compare(String a, String b) {
                    int sa = subjectSpecificityScore(a);
                    int sb = subjectSpecificityScore(b);
                    return Integer.compare(sb, sa);
                }
            });

            // Signal 1: Same-author works (up to 3 — great for series/sequels & same-voice works)
            if (!authorKey.isEmpty()) {
                try {
                    String aUrl = "https://openlibrary.org/search.json?author=" + URLEncoder.encode(resolvedAuthor, "UTF-8")
                            + "&sort=editions&limit=10&fields=key,title,author_name,cover_i,first_publish_year";
                    parseSearchDocsIntoRecs(GutendexSource.httpGet(aUrl), recs, seenTitles, Math.min(3, limit), 0);
                } catch (Throwable ignored) {}
            }

            // Signal 2: Multi-subject intersection search (top 2 specific subjects combined!)
            if (specificSubjects.size() >= 2 && recs.size() < limit) {
                try {
                    String s1 = URLEncoder.encode(specificSubjects.get(0), "UTF-8");
                    String s2 = URLEncoder.encode(specificSubjects.get(1), "UTF-8");
                    String qUrl = "https://openlibrary.org/search.json?q=subject:%22" + s1 + "%22+AND+subject:%22" + s2 + "%22"
                            + "&sort=rating&limit=16&fields=key,title,author_name,cover_i,first_publish_year";
                    parseSearchDocsIntoRecs(GutendexSource.httpGet(qUrl), recs, seenTitles, limit, seedYear);
                } catch (Throwable ignored) {}
            }

            // Signal 3: Individual specific subjects via Search API sorted by rating + era proximity
            for (int i = 0; i < Math.min(4, specificSubjects.size()) && recs.size() < limit; i++) {
                try {
                    String s = URLEncoder.encode(specificSubjects.get(i), "UTF-8");
                    String qUrl = "https://openlibrary.org/search.json?subject=" + s
                            + "&sort=rating&limit=16&fields=key,title,author_name,cover_i,first_publish_year";
                    parseSearchDocsIntoRecs(GutendexSource.httpGet(qUrl), recs, seenTitles, limit, seedYear);
                } catch (Throwable ignored) {}
            }
        } catch (Throwable t) {
            android.util.Log.e("OpenLibraryClient", "fetchRecommendations error", t);
        }
        return recs;
    }

    private static void parseSearchDocsIntoRecs(
            String json, List<BookResult> recs, Set<String> seenTitles, int maxTotal, int seedYear
    ) {
        if (json == null || json.isEmpty()) return;
        try {
            JSONObject root = new JSONObject(json);
            JSONArray docs = root.optJSONArray("docs");
            if (docs == null) return;
            for (int i = 0; i < docs.length() && recs.size() < maxTotal; i++) {
                JSONObject d = docs.optJSONObject(i);
                if (d == null) continue;
                String wt = d.optString("title", "").trim();
                int coverId = d.optInt("cover_i", 0);
                if (wt.isEmpty() || coverId <= 0) continue;

                // Era sanity filter: if seed book is modern (>= 1950), skip pre-1920 public-domain defaults
                // unless they matched a multi-subject intersection
                int yr = d.optInt("first_publish_year", 0);
                if (seedYear >= 1950 && yr > 0 && yr < 1920) continue;

                String norm = normalizeTitleForDedup(wt);
                if (seenTitles.contains(norm)) continue;
                seenTitles.add(norm);

                String auth = "Unknown Author";
                JSONArray an = d.optJSONArray("author_name");
                if (an != null && an.length() > 0) auth = an.optString(0, auth);
                String coverUrl = "https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg";
                recs.add(new BookResult("ol_" + coverId, "Open Library", wt, auth, "", coverUrl, "", 0));
            }
        } catch (Throwable ignored) {}
    }

    private static void collectSpecificSubjects(JSONArray arr, List<String> out, int maxCount) {
        if (arr == null) return;
        for (int i = 0; i < arr.length() && out.size() < maxCount; i++) {
            String raw = arr.optString(i, "").trim();
            if (raw.isEmpty() || raw.length() > 42 || isGenericOrNoisySubject(raw)) continue;
            boolean exists = false;
            for (String e : out) {
                if (e.equalsIgnoreCase(raw)) { exists = true; break; }
            }
            if (!exists) out.add(raw);
        }
    }

    private static int subjectSpecificityScore(String s) {
        String l = s.toLowerCase(Locale.US);
        int score = 10;
        if (l.contains(" ") || l.contains("-")) score += 15; // multi-word themes are far more specific
        if (l.contains("apocalyp") || l.contains("dystop") || l.contains("cyberpunk") || l.contains("noir")
                || l.contains("detective") || l.contains("space opera") || l.contains("survival")
                || l.contains("gothic") || l.contains("steampunk") || l.contains("epic")
                || l.contains("historical") || l.contains("thriller") || l.contains("psychological")) {
            score += 20;
        }
        if (l.equals("science fiction") || l.equals("fantasy") || l.equals("horror") || l.equals("mystery")) {
            score += 5; // good fallback, below more specific sub-genres
        }
        return score;
    }

    private static boolean isGenericOrNoisySubject(String raw) {
        String s = raw.toLowerCase(Locale.US).trim();
        if (s.contains("accessible book") || s.contains("protected daisy") || s.contains("in library")
                || s.contains("lending") || s.contains("staff picks") || s.contains("bestseller")
                || s.contains("translation") || s.contains("reading level") || s.contains("large type")
                || s.contains("open library") || s.contains("nyt:") || s.contains("award")
                || s.contains("audiobook") || s.contains("juvenile") || s.contains("children")) {
            return true;
        }
        String[] generic = new String[]{
            "fiction", "novel", "novels", "literature", "general", "stories", "books",
            "english", "classic", "classics", "popular works", "fiction, general",
            "american fiction", "english fiction", "literary", "contemporary"
        };
        for (String g : generic) {
            if (s.equals(g)) return true;
        }
        return false;
    }

    private static String normalizeTitleForDedup(String t) {
        if (t == null) return "";
        return t.toLowerCase(Locale.US).replaceAll("[^a-z0-9]", "");
    }
}
