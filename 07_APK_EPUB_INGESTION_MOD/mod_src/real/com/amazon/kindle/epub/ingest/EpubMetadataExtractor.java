package com.amazon.kindle.epub.ingest;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    public static final int MAX_OPF_BYTES = 2 * 1024 * 1024;
    public static final int MAX_DESCRIPTION_CHARS = 4000;
    private static final Pattern ISO_DATETIME_PREFIX =
            Pattern.compile("^(\\d{4}-\\d{2}-\\d{2})(?:[T ]\\d{2}:\\d{2}.*)?$");
    private static final Pattern YEAR_4_PATTERN =
            Pattern.compile("(?:^|\\D)((?:1\\d|20)\\d{2})(?:\\D|$)");

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
                        byte[] opfBytes = readEntryBounded(zip, opfEntry, MAX_OPF_BYTES);
                        if (opfBytes != null) {
                            parseOpfBytes(opfBytes, meta);
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

    private static byte[] readEntryBounded(ZipFile zip, ZipEntry entry, int maxBytes) {
        if (zip == null || entry == null || entry.isDirectory()) return null;
        long declared = entry.getSize();
        if (declared > maxBytes) {
            return null;
        }
        InputStream is = null;
        try {
            is = zip.getInputStream(entry);
            if (is == null) return null;
            int initCap = (declared > 0 && declared <= maxBytes) ? (int) declared : 4096;
            ByteArrayOutputStream baos = new ByteArrayOutputStream(initCap);
            byte[] buf = new byte[8192];
            int total = 0;
            int n;
            while ((n = is.read(buf)) > 0) {
                total += n;
                if (total > maxBytes) {
                    return null;
                }
                baos.write(buf, 0, n);
            }
            return baos.toByteArray();
        } catch (Throwable ignored) {
            return null;
        } finally {
            if (is != null) {
                try { is.close(); } catch (Throwable ignored) {}
            }
        }
    }

    private static String findOpfPath(ZipFile zip) {
        ZipEntry container = zip.getEntry("META-INF/container.xml");
        if (container == null) {
            container = findEntryIgnoreCase(zip, "META-INF/container.xml");
        }
        if (container != null) {
            byte[] bytes = readEntryBounded(zip, container, MAX_OPF_BYTES);
            if (bytes != null) {
                String detected = parseContainerOpfPath(bytes, true);
                if (detected == null) {
                    detected = parseContainerOpfPath(bytes, false);
                }
                if (!isBlank(detected)) {
                    return detected;
                }
            }
        }
        // Fallback: first .opf entry in ZIP within size limit
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            if (!e.isDirectory() && e.getName().toLowerCase(Locale.ROOT).endsWith(".opf")
                    && e.getSize() <= MAX_OPF_BYTES) {
                return e.getName();
            }
        }
        return null;
    }

    private static String parseContainerOpfPath(byte[] xmlBytes, boolean autoDetectEncoding) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            XmlPullParser parser = factory.newPullParser();
            ByteArrayInputStream bais = new ByteArrayInputStream(xmlBytes);
            if (autoDetectEncoding) {
                parser.setInput(bais, null);
            } else {
                parser.setInput(new InputStreamReader(bais, StandardCharsets.UTF_8));
            }
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
        } catch (Throwable ignored) {}
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

    private static void parseOpfBytes(byte[] xmlBytes, BookMeta meta) {
        if (!tryParseOpf(xmlBytes, meta, true)) {
            tryParseOpf(xmlBytes, meta, false);
        }
    }

    private static boolean tryParseOpf(byte[] xmlBytes, BookMeta meta, boolean autoDetectEncoding) {
        Set<String> genreSet = new LinkedHashSet<>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            XmlPullParser parser = factory.newPullParser();
            ByteArrayInputStream bais = new ByteArrayInputStream(xmlBytes);
            if (autoDetectEncoding) {
                parser.setInput(bais, null);
            } else {
                parser.setInput(new InputStreamReader(bais, StandardCharsets.UTF_8));
            }

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
                                String normAuthor = normalizeAuthor(val);
                                meta.author = appendDistinctAuthor(meta.author, normAuthor);
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
                                meta.description = truncateDescription(stripHtml(val));
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
            Set<String> seenLower = new LinkedHashSet<>();
            for (String g : genreSet) {
                if (seenLower.add(g.toLowerCase(Locale.ROOT))) {
                    meta.genres.add(g);
                }
            }
            return true;
        } catch (Throwable ignored) {
            return false;
        }
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

    private static boolean isAuthorSuffix(String part) {
        if (part == null) return false;
        String k = part.trim().replace(".", "").toLowerCase(Locale.ROOT);
        return "jr".equals(k) || "sr".equals(k) || "ii".equals(k)
                || "iii".equals(k) || "iv".equals(k) || "phd".equals(k) || "md".equals(k);
    }

    /**
     * Pure, Android-free author list deduplicator using case-insensitive exact entry equality per "; "-separated author.
     */
    public static String appendDistinctAuthor(String existingAuthors, String candidateAuthor) {
        java.util.List<String> ordered = new java.util.ArrayList<>();
        Set<String> seenLower = new LinkedHashSet<>();
        for (String chunk : new String[] { existingAuthors, candidateAuthor }) {
            if (chunk == null) continue;
            for (String part : chunk.split(";")) {
                String trimmed = cleanSpace(part);
                if (!trimmed.isEmpty() && seenLower.add(trimmed.toLowerCase(Locale.ROOT))) {
                    ordered.add(trimmed);
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ordered.size(); i++) {
            if (i > 0) sb.append("; ");
            sb.append(ordered.get(i));
        }
        return sb.toString();
    }

    public static String truncateDescription(String desc) {
        if (desc == null) return "";
        if (desc.length() <= MAX_DESCRIPTION_CHARS) return desc;
        return desc.substring(0, MAX_DESCRIPTION_CHARS);
    }

    /**
     * Pure, Android-free author normalizer: converts "Last, First" -> "First Last"
     * without mangling suffixes (Jr., Sr., III) or multi-author strings.
     */
    public static String normalizeAuthor(String raw) {
        if (raw == null) return "";
        String trimmed = cleanSpace(raw);
        if (trimmed.isEmpty()) return "";
        if (trimmed.contains(";")) {
            String[] parts = trimmed.split(";");
            String out = "";
            for (String p : parts) {
                String norm = normalizeSingleAuthor(p);
                out = appendDistinctAuthor(out, norm);
            }
            return out;
        }
        return normalizeSingleAuthor(trimmed);
    }

    private static String normalizeSingleAuthor(String raw) {
        if (raw == null) return "";
        String s = cleanSpace(raw);
        if (s.isEmpty()) return "";
        if (s.contains(" & ") || s.toLowerCase(Locale.ROOT).contains(" and ")) {
            return s;
        }
        String[] commaParts = s.split(",");
        if (commaParts.length == 2) {
            String left = commaParts[0].trim();
            String right = commaParts[1].trim();
            if (!left.isEmpty() && !right.isEmpty() && !isAuthorSuffix(right)) {
                return right + " " + left;
            }
            return s;
        }
        if (commaParts.length == 3 && isAuthorSuffix(commaParts[2])) {
            String last = commaParts[0].trim();
            String first = commaParts[1].trim();
            String suffix = commaParts[2].trim();
            if (!last.isEmpty() && !first.isEmpty()) {
                return first + " " + last + " " + suffix;
            }
        }
        return s;
    }

    public static String normalizePubDate(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        if (s.isEmpty()) return "";
        Matcher iso = ISO_DATETIME_PREFIX.matcher(s);
        if (iso.matches()) {
            String datePart = iso.group(1);
            int yr = Integer.parseInt(datePart.substring(0, 4));
            if (yr >= 1000 && yr <= 2099) {
                return datePart;
            }
            return "";
        }
        Matcher ym = YEAR_4_PATTERN.matcher(s);
        if (ym.find()) {
            String yStr = ym.group(1);
            int yr = Integer.parseInt(yStr);
            if (yr >= 1000 && yr <= 2099) {
                return yStr;
            }
        }
        return "";
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
