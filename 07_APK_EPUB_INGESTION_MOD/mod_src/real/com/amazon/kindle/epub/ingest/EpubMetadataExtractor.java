package com.amazon.kindle.epub.ingest;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Pure-Java EPUB OPF metadata extractor (zero external dependencies).
 * Reads META-INF/container.xml -> rootfile OPF and extracts title, author,
 * publisher, publication date, language, ISBN/identifier, genres (dc:subject),
 * and description cleanly in <25ms.
 */
public final class EpubMetadataExtractor {

    public static final class BookMeta {
        public String title = "";
        public String author = "";
        public String publisher = "";
        public String pubDate = "";
        public String language = "";
        public String isbn = "";
        public String description = "";
        public String openLibraryKey = "";
        public final List<String> genres = new ArrayList<>();

        public String genresCsv() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < genres.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(genres.get(i));
            }
            return sb.toString();
        }
    }

    private EpubMetadataExtractor() {}

    public static BookMeta extract(File epubFile, String fallbackDisplayName) {
        BookMeta meta = new BookMeta();
        if (epubFile != null && epubFile.exists() && epubFile.length() > 0) {
            ZipFile zip = null;
            try {
                zip = new ZipFile(epubFile);
                String opfPath = findOpfPath(zip);
                if (opfPath != null) {
                    ZipEntry opfEntry = zip.getEntry(opfPath);
                    if (opfEntry == null) {
                        opfEntry = findEntryIgnoreCase(zip, opfPath);
                    }
                    if (opfEntry != null) {
                        InputStream opfIs = null;
                        try {
                            opfIs = zip.getInputStream(opfEntry);
                            parseOpf(opfIs, meta);
                        } finally {
                            if (opfIs != null) {
                                try { opfIs.close(); } catch (Throwable ignored) {}
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {
                // Graceful fallback to filename below
            } finally {
                if (zip != null) {
                    try { zip.close(); } catch (Throwable ignored) {}
                }
            }
        }

        if (isBlank(meta.title)) {
            meta.title = cleanTitleFromFilename(fallbackDisplayName != null
                    ? fallbackDisplayName
                    : (epubFile != null ? epubFile.getName() : "Untitled EPUB"));
        }
        if (isBlank(meta.author)) {
            meta.author = "Unknown Author";
        }
        return meta;
    }

    private static String findOpfPath(ZipFile zip) {
        ZipEntry container = zip.getEntry("META-INF/container.xml");
        if (container == null) {
            container = findEntryIgnoreCase(zip, "META-INF/container.xml");
        }
        if (container != null) {
            InputStream is = null;
            try {
                is = zip.getInputStream(container);
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new InputStreamReader(is, StandardCharsets.UTF_8));
                int event = parser.getEventType();
                while (event != XmlPullParser.END_DOCUMENT) {
                    if (event == XmlPullParser.START_TAG) {
                        String name = localName(parser.getName());
                        if ("rootfile".equalsIgnoreCase(name)) {
                            String fullPath = parser.getAttributeValue(null, "full-path");
                            if (isBlank(fullPath)) {
                                for (int i = 0; i < parser.getAttributeCount(); i++) {
                                    if ("full-path".equalsIgnoreCase(localName(parser.getAttributeName(i)))) {
                                        fullPath = parser.getAttributeValue(i);
                                        break;
                                    }
                                }
                            }
                            if (!isBlank(fullPath)) {
                                return fullPath.trim();
                            }
                        }
                    }
                    event = parser.next();
                }
            } catch (Throwable ignored) {
            } finally {
                if (is != null) {
                    try { is.close(); } catch (Throwable ignored) {}
                }
            }
        }
        // Fallback: first .opf entry in ZIP
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            if (!e.isDirectory() && e.getName().toLowerCase(Locale.ROOT).endsWith(".opf")) {
                return e.getName();
            }
        }
        return null;
    }

    private static ZipEntry findEntryIgnoreCase(ZipFile zip, String path) {
        String target = path.replace('\\', '/').toLowerCase(Locale.ROOT);
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            if (e.getName().replace('\\', '/').toLowerCase(Locale.ROOT).equals(target)) {
                return e;
            }
        }
        return null;
    }

    private static void parseOpf(InputStream is, BookMeta meta) {
        Set<String> genreSet = new LinkedHashSet<>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new InputStreamReader(is, StandardCharsets.UTF_8));

            int event = parser.getEventType();
            boolean inMetadata = false;
            String currentTag = null;
            StringBuilder textBuf = new StringBuilder();

            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    String name = localName(parser.getName()).toLowerCase(Locale.ROOT);
                    if ("metadata".equals(name)) {
                        inMetadata = true;
                    } else if (inMetadata) {
                        currentTag = name;
                        textBuf.setLength(0);
                    }
                } else if (event == XmlPullParser.TEXT) {
                    if (inMetadata && currentTag != null) {
                        textBuf.append(parser.getText());
                    }
                } else if (event == XmlPullParser.END_TAG) {
                    String name = localName(parser.getName()).toLowerCase(Locale.ROOT);
                    if ("metadata".equals(name)) {
                        break;
                    }
                    if (inMetadata && name.equals(currentTag)) {
                        String val = cleanSpace(decodeXmlEntities(textBuf.toString()));
                        if (!isBlank(val)) {
                            if ("title".equals(name) && isBlank(meta.title)) {
                                meta.title = val;
                            } else if ("creator".equals(name)) {
                                if (isBlank(meta.author)) {
                                    meta.author = val;
                                } else if (!meta.author.contains(val)) {
                                    meta.author = meta.author + "; " + val;
                                }
                            } else if ("publisher".equals(name) && isBlank(meta.publisher)) {
                                meta.publisher = val;
                            } else if ("date".equals(name) && isBlank(meta.pubDate)) {
                                meta.pubDate = normalizePubDate(val);
                            } else if ("language".equals(name) && isBlank(meta.language)) {
                                meta.language = val;
                            } else if ("subject".equals(name)) {
                                if (!isBlank(val) && !isNoiseTag(val) && val.length() <= 60 && genreSet.size() < 8) {
                                    genreSet.add(val);
                                }
                            } else if ("description".equals(name) && isBlank(meta.description)) {
                                meta.description = stripHtml(val);
                            } else if ("identifier".equals(name)) {
                                String isbn = normalizeIsbn(val);
                                if (!isBlank(isbn)) {
                                    if (isbn.length() == 13 || isBlank(meta.isbn)) {
                                        meta.isbn = isbn;
                                    }
                                }
                            }
                        }
                        currentTag = null;
                        textBuf.setLength(0);
                    }
                }
                event = parser.next();
            }
        } catch (Throwable ignored) {
        } finally {
            try { is.close(); } catch (Throwable ignored) {}
        }
        meta.genres.addAll(genreSet);
    }

    private static String localName(String qName) {
        if (qName == null) return "";
        int idx = qName.indexOf(':');
        return idx >= 0 ? qName.substring(idx + 1) : qName;
    }

    /**
     * Pure, Android-free ISBN normalizer & checksum validator (ISBN-13 978/979 and ISBN-10).
     * Rejects UUIDs, Calibre IDs, URLs, and invalid checksums.
     */
    public static String normalizeIsbn(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        String lower = s.toLowerCase(Locale.ROOT);
        if (lower.contains("http://") || lower.contains("https://")
                || lower.contains("uuid") || lower.contains("calibre")) {
            return "";
        }
        s = s.replaceAll("(?i)^urn:isbn:", "")
                .replaceAll("(?i)^isbn[:\\s-]*", "")
                .replaceAll("[\\s-]+", "")
                .toUpperCase(Locale.ROOT);
        if (s.length() == 13) {
            if (!s.startsWith("978") && !s.startsWith("979")) return "";
            int sum = 0;
            for (int i = 0; i < 13; i++) {
                char c = s.charAt(i);
                if (c < '0' || c > '9') return "";
                int d = c - '0';
                sum += (i % 2 == 0) ? d : (d * 3);
            }
            return (sum % 10 == 0) ? s : "";
        }
        if (s.length() == 10) {
            int sum = 0;
            for (int i = 0; i < 10; i++) {
                char c = s.charAt(i);
                int val;
                if (i == 9 && c == 'X') {
                    val = 10;
                } else if (c >= '0' && c <= '9') {
                    val = c - '0';
                } else {
                    return "";
                }
                sum += (10 - i) * val;
            }
            return (sum % 11 == 0) ? s : "";
        }
        return "";
    }

    public static String decodeXmlEntities(String s) {
        if (s == null || s.indexOf('&') < 0) return s == null ? "" : s;
        StringBuilder sb = new StringBuilder(s.length());
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '&') {
                int semi = s.indexOf(';', i + 1);
                if (semi > i + 1 && semi - i <= 10) {
                    String ent = s.substring(i + 1, semi);
                    if ("amp".equals(ent)) { sb.append('&'); i = semi + 1; continue; }
                    if ("lt".equals(ent)) { sb.append('<'); i = semi + 1; continue; }
                    if ("gt".equals(ent)) { sb.append('>'); i = semi + 1; continue; }
                    if ("quot".equals(ent)) { sb.append('"'); i = semi + 1; continue; }
                    if ("apos".equals(ent)) { sb.append('\''); i = semi + 1; continue; }
                    if (ent.startsWith("#x") || ent.startsWith("#X")) {
                        try {
                            int cp = Integer.parseInt(ent.substring(2), 16);
                            if (cp > 0 && cp <= 0x10FFFF) {
                                sb.append(Character.toChars(cp));
                                i = semi + 1;
                                continue;
                            }
                        } catch (Throwable ignored) {}
                    } else if (ent.startsWith("#")) {
                        try {
                            int cp = Integer.parseInt(ent.substring(1), 10);
                            if (cp > 0 && cp <= 0x10FFFF) {
                                sb.append(Character.toChars(cp));
                                i = semi + 1;
                                continue;
                            }
                        } catch (Throwable ignored) {}
                    }
                }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    private static String stripHtml(String s) {
        if (s == null) return "";
        String plain = decodeXmlEntities(s).replaceAll("<[^>]+>", " ");
        return cleanSpace(plain);
    }

    private static String cleanSpace(String s) {
        if (s == null) return "";
        return s.replaceAll("[\\r\\n\\t ]+", " ").trim();
    }

    public static String cleanTitleFromFilename(String filename) {
        if (filename == null) return "Untitled";
        String name = filename;
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0) name = name.substring(slash + 1);
        if (name.toLowerCase(Locale.ROOT).endsWith(".epub")) {
            name = name.substring(0, name.length() - 5);
        }
        name = name.replace('_', ' ').replaceAll("\\s+", " ").trim();
        return name.isEmpty() ? "Untitled" : name;
    }

    public static String normalizePubDate(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        // Trim ISO timestamps like "2000-07-15 00:00:00+01:00" or "2014-06-06T00:00:00Z"
        int tIdx = s.indexOf('T');
        if (tIdx >= 4) s = s.substring(0, tIdx);
        int spIdx = s.indexOf(' ');
        if (spIdx >= 4) s = s.substring(0, spIdx);
        return s.trim();
    }

    public static boolean isNoiseTag(String tag) {
        if (tag == null) return true;
        String k = tag.trim().toLowerCase(Locale.ROOT);
        return k.isEmpty() || "read".equals(k) || "unread".equals(k)
                || "to read".equals(k) || "to-read".equals(k)
                || "ebook".equals(k) || "e-book".equals(k)
                || "kindle".equals(k) || "calibre".equals(k)
                || "general".equals(k) || "fiction - general".equals(k);
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
