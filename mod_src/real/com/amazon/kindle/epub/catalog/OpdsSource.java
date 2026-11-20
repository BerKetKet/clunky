package com.amazon.kindle.epub.catalog;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generic OPDS 1.2 Atom XML catalog source.
 * Works with any public OPDS catalog (Standard Ebooks, Internet Archive, ManyBooks,
 * or a self-hosted Calibre-Web / Kavita server).
 */
public final class OpdsSource implements BookSource {
    private final String name;
    private final String feedUrl;
    private final String searchTemplate;

    public OpdsSource(String name, String feedUrl, String searchTemplate) {
        this.name = name;
        this.feedUrl = feedUrl;
        this.searchTemplate = searchTemplate;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<BookResult> search(String query) throws Exception {
        String url = feedUrl;
        if (query != null && !query.trim().isEmpty() && searchTemplate != null && !searchTemplate.isEmpty()) {
            url = searchTemplate.replace("{searchTerms}", URLEncoder.encode(query.trim(), "UTF-8"));
        }
        String xml = GutendexSource.httpGet(url);
        List<BookResult> out = new ArrayList<BookResult>();
        Matcher entryM = Pattern.compile("<entry\\b[^>]*>(.*?)</entry>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(xml);
        int idx = 0;
        while (entryM.find()) {
            String entry = entryM.group(1);
            String title = extractTag(entry, "title", "Untitled");
            String author = extractTag(entry, "name", "Unknown Author");
            String epubUrl = extractLinkByType(entry, "application/epub+zip");
            if (epubUrl.isEmpty()) continue;
            if (epubUrl.startsWith("/")) {
                // Resolve root-relative URL against feed origin
                int schemeEnd = feedUrl.indexOf("://");
                int hostEnd = feedUrl.indexOf('/', schemeEnd + 3);
                String origin = (hostEnd > 0) ? feedUrl.substring(0, hostEnd) : feedUrl;
                epubUrl = origin + epubUrl;
            }
            String coverUrl = extractLinkByType(entry, "image/jpeg");
            if (coverUrl.isEmpty()) coverUrl = extractLinkByType(entry, "image/png");
            if (coverUrl.startsWith("/")) {
                int schemeEnd = feedUrl.indexOf("://");
                int hostEnd = feedUrl.indexOf('/', schemeEnd + 3);
                String origin = (hostEnd > 0) ? feedUrl.substring(0, hostEnd) : feedUrl;
                coverUrl = origin + coverUrl;
            }
            out.add(new BookResult("opds_" + (++idx), name, title, author, epubUrl, coverUrl, "", 0));
        }
        return out;
    }

    private static String extractTag(String xml, String tag, String def) {
        Matcher m = Pattern.compile("<" + tag + "\\b[^>]*>(.*?)</" + tag + ">", Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(xml);
        if (m.find()) {
            String s = m.group(1).replaceAll("<[^>]+>", "").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").trim();
            if (!s.isEmpty()) return s;
        }
        return def;
    }

    private static String extractLinkByType(String entry, String mimeType) {
        Matcher m = Pattern.compile("<link\\b[^>]*>", Pattern.CASE_INSENSITIVE).matcher(entry);
        while (m.find()) {
            String tag = m.group(0);
            if (tag.toLowerCase().contains(mimeType.toLowerCase())) {
                Matcher hm = Pattern.compile("\\bhref=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(tag);
                if (hm.find()) {
                    return hm.group(1).replace("&amp;", "&");
                }
            }
        }
        return "";
    }
}
