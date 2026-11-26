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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    public static final int MAX_GENRES_PER_BOOK = 6;
    private static final Pattern ISO_DATETIME_PREFIX =
            Pattern.compile("^(\\d{4}-\\d{2}-\\d{2})(?:[T ]\\d{2}:\\d{2}.*)?$");
    private static final Pattern YEAR_4_PATTERN =
            Pattern.compile("(?:^|\\D)((?:1\\d|20)\\d{2})(?:\\D|$)");
    private static final Pattern PAREN_QUALIFIER =
            Pattern.compile("\\s*\\([^()]*\\)");
    private static final Pattern PURE_DATE_OR_RANGE =
            Pattern.compile("^(?:ca\\.?\\s*)?(?:\\d{3,4}s?|\\d{3,4}\\s*[-\\u2013\\u2014/]\\s*(?:\\d{2,4})?|[-\\u2013\\u2014]\\s*\\d{3,4})$",
                    Pattern.CASE_INSENSITIVE);
    private static final Pattern PERSONAL_NAME_WITH_DATES =
            Pattern.compile("^[^,]{1,60},\\s*[^,]{1,60}(?:,\\s*[^,]{1,30})?,\\s*(?:(?:b\\.|d\\.|fl\\.|ca\\.?)\\s*)?\\d{3,4}(?:\\s*[-\\u2013\\u2014]\\s*(?:\\d{2,4})?)?\\.?$",
                    Pattern.CASE_INSENSITIVE);
    private static final Pattern SURNAME_PART_PATTERN =
            Pattern.compile("^[A-Z\\u00C0-\\u024F][\\p{L}'\\u2019.-]{1,30}(?:\\s+(?:de|la|du|van|von|der|del|da|di|[A-Z\\u00C0-\\u024F][\\p{L}'\\u2019.-]{1,30})){0,2}$");
    private static final Pattern FORENAME_PART_PATTERN =
            Pattern.compile("^[A-Z\\u00C0-\\u024F][\\p{L}'\\u2019.-]*(?:\\s+[A-Z\\u00C0-\\u024F][\\p{L}'\\u2019.-]*){0,2}\\.?$");
    private static final Set<String> NON_PERSON_TOPIC_WORDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "fiction", "nonfiction", "non-fiction", "poetry", "drama", "essays", "letters",
            "literature", "history", "philosophy", "biography", "autobiography", "criticism",
            "science", "horror", "mystery", "fantasy", "humor", "humorous", "satire",
            "utopias", "dystopias", "short stories", "authors", "poets", "novelists",
            "women", "men", "children", "classic", "classics", "modern", "ancient",
            "medieval", "contemporary", "general", "correspondence", "translations",
            "translations into english", "early works", "civilization", "politics",
            "religion", "social", "economic", "war", "world war", "world",
            "american", "british", "english", "french", "german", "russian",
            "japanese", "spanish", "italian", "greek", "latin", "chinese", "indian",
            "england", "london", "france", "paris", "germany", "russia", "japan",
            "greece", "rome", "italy", "spain", "america", "united states",
            "great britain", "europe", "new york", "california", "textbooks"
    )));

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
        List<String> rawSubjects = new ArrayList<>();
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
                                if (!isBlank(val)) {
                                    rawSubjects.add(val);
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
            meta.genres.clear();
            meta.genres.addAll(cleanAndRankGenres(rawSubjects, null, meta.author));
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

    public static boolean isPureDateOrRange(String raw) {
        if (raw == null) return false;
        String s = cleanSpace(raw);
        return !s.isEmpty() && PURE_DATE_OR_RANGE.matcher(s).matches();
    }

    private static boolean isNonPersonTopic(String segment) {
        if (segment == null) return true;
        String s = cleanSpace(segment).toLowerCase(Locale.ROOT);
        if (s.isEmpty() || NON_PERSON_TOPIC_WORDS.contains(s)) return true;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isDigit(s.charAt(i))) return true;
        }
        String[] parts = s.split("\\s+");
        for (String p : parts) {
            if (NON_PERSON_TOPIC_WORDS.contains(p)) return true;
        }
        return false;
    }

    /**
     * Detects personal-name subjects such as "Bukowski, Charles, 1921-1994" or
     * "Bukowski, Charles" without false-positiving on topical comma headings.
     */
    public static boolean isPersonalNameSubject(String raw) {
        if (raw == null) return false;
        String s = cleanSpace(raw);
        if (s.isEmpty() || s.indexOf(',') < 0) return false;
        if (PERSONAL_NAME_WITH_DATES.matcher(s).matches()) {
            String[] segs = s.split(",");
            if (segs.length >= 2) {
                String p0 = cleanSpace(segs[0]);
                String p1 = cleanSpace(segs[1]);
                if (!p0.isEmpty() && !p1.isEmpty()
                        && p0.split("\\s+").length <= 3
                        && p1.split("\\s+").length <= 4
                        && !isNonPersonTopic(p0)
                        && !isNonPersonTopic(p1)) {
                    return true;
                }
            }
        }
        String stripped = cleanSubject(s);
        if (stripped.indexOf(',') >= 0) {
            String[] segs = stripped.split(",");
            if (segs.length == 2) {
                String left = cleanSpace(segs[0]);
                String right = cleanSpace(segs[1]);
                if (SURNAME_PART_PATTERN.matcher(left).matches()
                        && FORENAME_PART_PATTERN.matcher(right).matches()
                        && !isNonPersonTopic(left)
                        && !isNonPersonTopic(right)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Strips parenthetical qualifiers iteratively (e.g.
     * "Poetry (poetic works by one author)" -> "Poetry") without splitting commas.
     */
    public static String cleanSubject(String raw) {
        if (raw == null) return "";
        String cur = cleanSpace(raw);
        for (int pass = 0; pass < 4; pass++) {
            String next = cleanSpace(PAREN_QUALIFIER.matcher(cur).replaceAll(" "));
            if (next.equals(cur)) break;
            cur = next;
        }
        cur = cur.replaceAll("[,;:]+$", "").replaceAll("^[,;:]+", "").trim();
        return cleanSpace(cur);
    }

    public static boolean isNoiseTag(String tag) {
        if (tag == null) return true;
        String k = cleanSpace(tag).toLowerCase(Locale.ROOT);
        if (k.length() < 2 || k.length() > 80) return true;
        if (isPureDateOrRange(k)) return true;
        if ("read".equals(k) || "unread".equals(k)
                || "to read".equals(k) || "to-read".equals(k)
                || "currently reading".equals(k) || "currently-reading".equals(k)
                || "ebook".equals(k) || "e-book".equals(k)
                || "ebooks".equals(k) || "e-books".equals(k)
                || "kindle".equals(k) || "calibre".equals(k)
                || "epub".equals(k) || "pdf".equals(k) || "mobi".equals(k) || "kfx".equals(k)
                || "audiobook".equals(k) || "audiobooks".equals(k)
                || "general".equals(k)
                || "fiction, general".equals(k)
                || "fiction - general".equals(k)
                || "fiction / general".equals(k)
                || "nonfiction, general".equals(k)
                || "non-fiction, general".equals(k)
                || "accessible book".equals(k) || "accessible books".equals(k)
                || "protected daisy".equals(k) || "daisy".equals(k)
                || "in library".equals(k) || "overdrive".equals(k)
                || "large type books".equals(k) || "large type".equals(k)
                || "large print books".equals(k) || "large print".equals(k)
                || "open library staff picks".equals(k) || "staff picks".equals(k)
                || "internet archive".equals(k) || "internet archive wishlist".equals(k)
                || "lending library".equals(k) || "lending library books".equals(k)
                || "public domain".equals(k) || "gutenberg".equals(k)
                || "project gutenberg".equals(k) || "standard ebooks".equals(k)
                || "open library".equals(k) || "archive.org".equals(k)) {
            return true;
        }
        return k.startsWith("early works")
                || k.startsWith("translations into")
                || k.startsWith("nyt:")
                || k.startsWith("award:")
                || k.startsWith("series:")
                || k.startsWith("ddc:")
                || k.startsWith("lcc:")
                || k.startsWith("bisac")
                || k.contains("protected daisy")
                || k.contains("accessible book")
                || k.contains("lending library")
                || k.contains("internet archive")
                || k.contains("open library staff pick")
                || k.contains("large type book")
                || k.contains("textbooks for")
                || (k.contains("in library") && k.length() <= 24)
                || (k.contains("overdrive") && k.length() <= 30);
    }

    private static boolean matchesBookAuthor(String candidate, String authorName) {
        if (candidate == null || authorName == null) return false;
        String candLower = cleanSpace(candidate).toLowerCase(Locale.ROOT);
        String authLower = cleanSpace(authorName).toLowerCase(Locale.ROOT);
        if (candLower.isEmpty() || authLower.isEmpty() || "unknown author".equals(authLower)) {
            return false;
        }
        if (candLower.equals(authLower)) return true;
        String[] parts = authLower.split("\\s+");
        if (parts.length >= 2) {
            StringBuilder reversed = new StringBuilder(parts[parts.length - 1]).append(", ");
            for (int i = 0; i < parts.length - 1; i++) {
                if (i > 0) reversed.append(' ');
                reversed.append(parts[i]);
            }
            if (candLower.equals(reversed.toString())) return true;
        }
        return false;
    }

    /**
     * Treats each subject entry as a complete unit (never splitting on commas),
     * drops personal-name subjects, pure date ranges, and library/noise tags,
     * strips parenthetical qualifiers, deduplicates case-insensitively, and
     * caps at MAX_GENRES_PER_BOOK (6), preferring higher global frequency then shorter labels.
     */
    public static List<String> cleanAndRankGenres(Collection<String> rawSubjects,
                                                    Map<String, Integer> globalFreq) {
        return cleanAndRankGenres(rawSubjects, globalFreq, null, MAX_GENRES_PER_BOOK);
    }

    public static List<String> cleanAndRankGenres(Collection<String> rawSubjects,
                                                    Map<String, Integer> globalFreq,
                                                    String authorName) {
        return cleanAndRankGenres(rawSubjects, globalFreq, authorName, MAX_GENRES_PER_BOOK);
    }

    public static List<String> cleanAndRankGenres(Collection<String> rawSubjects,
                                                    Map<String, Integer> globalFreq,
                                                    String authorName,
                                                    int limit) {
        if (rawSubjects == null || rawSubjects.isEmpty() || limit <= 0) {
            return new ArrayList<>();
        }
        final Map<String, String> unique = new LinkedHashMap<>();
        final Map<String, Integer> origOrder = new HashMap<>();
        int idx = 0;
        for (String raw : rawSubjects) {
            if (raw == null) continue;
            String s = cleanSpace(raw);
            if (s.isEmpty() || isNoiseTag(s) || isPersonalNameSubject(s)
                    || isPureDateOrRange(s) || matchesBookAuthor(s, authorName)) {
                continue;
            }
            String cleaned = cleanSubject(s);
            if (cleaned.length() < 2 || cleaned.length() > 60
                    || isNoiseTag(cleaned) || isPersonalNameSubject(cleaned)
                    || isPureDateOrRange(cleaned) || matchesBookAuthor(cleaned, authorName)) {
                continue;
            }
            String key = cleaned.toLowerCase(Locale.ROOT);
            if (!unique.containsKey(key)) {
                unique.put(key, cleaned);
                origOrder.put(key, idx++);
            }
        }
        List<String> keys = new ArrayList<>(unique.keySet());
        Collections.sort(keys, new Comparator<String>() {
            @Override
            public int compare(String ka, String kb) {
                int fa = (globalFreq != null && globalFreq.containsKey(ka)) ? globalFreq.get(ka) : 1;
                int fb = (globalFreq != null && globalFreq.containsKey(kb)) ? globalFreq.get(kb) : 1;
                if (fa != fb) return Integer.compare(fb, fa);
                int la = unique.get(ka).length();
                int lb = unique.get(kb).length();
                if (la != lb) return Integer.compare(la, lb);
                return Integer.compare(origOrder.get(ka), origOrder.get(kb));
            }
        });
        int cap = Math.min(limit, keys.size());
        List<String> out = new ArrayList<>(cap);
        for (int i = 0; i < cap; i++) {
            out.add(unique.get(keys.get(i)));
        }
        return out;
    }

    public static String joinGenresForDisplay(Collection<String> genres) {
        if (genres == null || genres.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String g : genres) {
            if (g == null) continue;
            String t = cleanSpace(g);
            if (t.isEmpty()) continue;
            if (!first) sb.append(", ");
            sb.append(t);
            first = false;
        }
        return sb.toString();
    }

    /** Pure-Java JSON array serializer safe for both Android and headless JVM tests. */
    public static String genresToJson(Collection<String> genres) {
        if (genres == null || genres.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("[");
        boolean any = false;
        for (String g : genres) {
            if (g == null) continue;
            String t = cleanSpace(g);
            if (t.isEmpty()) continue;
            if (any) sb.append(',');
            appendJsonString(sb, t);
            any = true;
        }
        if (!any) return "";
        sb.append(']');
        return sb.toString();
    }

    private static void appendJsonString(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format(Locale.ROOT, "\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }

    /** Pure-Java JSON string-array parser; never splits entries on commas. */
    public static List<String> parseGenresJson(String json) {
        List<String> out = new ArrayList<>();
        if (json == null) return out;
        String s = json.trim();
        if (s.length() < 2 || s.charAt(0) != '[') return out;
        int i = 1;
        int n = s.length();
        while (i < n) {
            char c = s.charAt(i);
            if (c == ']') break;
            if (c == ',' || Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (c != '"') {
                return new ArrayList<>();
            }
            i++;
            StringBuilder item = new StringBuilder();
            boolean closed = false;
            while (i < n) {
                char ch = s.charAt(i++);
                if (ch == '"') {
                    closed = true;
                    break;
                }
                if (ch == '\\' && i < n) {
                    char esc = s.charAt(i++);
                    switch (esc) {
                        case '"': item.append('"'); break;
                        case '\\': item.append('\\'); break;
                        case '/': item.append('/'); break;
                        case 'b': item.append('\b'); break;
                        case 'f': item.append('\f'); break;
                        case 'n': item.append('\n'); break;
                        case 'r': item.append('\r'); break;
                        case 't': item.append('\t'); break;
                        case 'u':
                            if (i + 4 <= n) {
                                try {
                                    item.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                                    i += 4;
                                } catch (NumberFormatException ignored) {}
                            }
                            break;
                        default: item.append(esc); break;
                    }
                } else {
                    item.append(ch);
                }
            }
            if (!closed) return new ArrayList<>();
            String cleaned = cleanSpace(item.toString());
            if (!cleaned.isEmpty()) out.add(cleaned);
        }
        return out;
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
