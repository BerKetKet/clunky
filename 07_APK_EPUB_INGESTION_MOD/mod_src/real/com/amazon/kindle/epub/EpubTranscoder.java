package com.amazon.kindle.epub;

import android.app.ActivityThread;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import com.amazon.kcp.application.j4;
import com.amazon.kcp.util.Utils;
import com.amazon.kindle.content.ILibraryService;
import com.amazon.kindle.content.a;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Full-featured on-device EPUB -> Kindle Container Transcoder for com.amazon.kindle.epub.
 * Round 007 additions:
 * - Binary INDX (NCX) Table of Contents records (Master INDX + Secondary INDX + CNCX)
 *   parsed from toc.ncx / EPUB3 nav / spine headings so Kindle's native "Go To -> Table of Contents" drawer works!
 * - Dual output mode:
 *     * Default: MOBI7 (header_length=248 + NCX INDX) for rock-solid compatibility
 *     * KF8 mode (enabled when /sdcard/kindle/.use_kf8 exists): file_version=8, header_length=264,
 *       FDST + SKEL + FRAG + NCX + CSS Flow 1 + kindle:embed: image URIs for libWebCoreKRF.so CSS3 fidelity!
 */
public final class EpubTranscoder {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    public static volatile String lastCoverPath = null;

    private static final class ChapterToc {
        final String label;
        final int startOffset;
        int length;

        ChapterToc(String label, int startOffset) {
            this.label = label;
            this.startOffset = startOffset;
            this.length = 0;
        }
    }

    private static final class NavAnchor {
        final String label;
        final String fragId;

        NavAnchor(String label, String fragId) {
            this.label = label;
            this.fragId = fragId;
        }
    }

    private static final class Kf8Section {
        final int fileNum;
        final int skelStart;
        final int skelLen;
        final int insertPos;
        final int fragStart;
        final int fragLen;
        final String aid;

        Kf8Section(int fileNum, int skelStart, int skelLen, int insertPos, int fragStart, int fragLen, String aid) {
            this.fileNum = fileNum;
            this.skelStart = skelStart;
            this.skelLen = skelLen;
            this.insertPos = insertPos;
            this.fragStart = fragStart;
            this.fragLen = fragLen;
            this.aid = aid;
        }
    }

    public static boolean isKf8Enabled() {
        try {
            return !new File("/sdcard/kindle/.use_mobi7").exists();
        } catch (Throwable ignored) {
            return true;
        }
    }

    public static File getCacheDir() {
        try {
            Application app = ActivityThread.currentApplication();
            if (app != null) {
                File filesDir = app.getFilesDir();
                if (filesDir != null) {
                    File cache = new File(filesDir, "epub_cache");
                    cache.mkdirs();
                    return cache;
                }
            }
        } catch (Throwable ignored) {
        }
        File fallback = new File("/data/local/tmp/epub_cache");
        fallback.mkdirs();
        return fallback;
    }

    public static String ensureKindlePath(String path) {
        if (path == null) return null;
        return ensureKindleFile(new File(path)).getAbsolutePath();
    }

    public static File ensureKindleFile(File file) {
        if (!isEpubFile(file)) return file;
        try {
            File cacheDir = getCacheDir();
            String ext = isKf8Enabled() ? ".azw3" : ".prc";
            File outFile = new File(cacheDir, file.getName() + ext);
            if (!outFile.exists() || outFile.length() < 512 || outFile.lastModified() < file.lastModified()) {
                transcode(file, outFile, isKf8Enabled());
            }
            if (outFile.exists() && outFile.length() >= 512) {
                File coverSidecar = new File(outFile.getAbsolutePath() + ".cover.jpg");
                if (coverSidecar.exists()) {
                    lastCoverPath = coverSidecar.getAbsolutePath();
                    CoverOverlayView.resetSession();
                }
                return outFile;
            }
        } catch (Throwable ignored) {
        }
        return file;
    }

    public static boolean isEpubFile(File file) {
        if (file == null || !file.exists() || file.isDirectory()) return false;
        if (file.getName().toLowerCase(Locale.US).endsWith(".epub")) return true;
        byte[] magic = new byte[4];
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            int read = fis.read(magic);
            return read == 4 && magic[0] == 0x50 && magic[1] == 0x4B;
        } catch (Throwable ignored) {
            return false;
        } finally {
            if (fis != null) {
                try { fis.close(); } catch (Throwable ignored) {}
            }
        }
    }

    public static void registerInLibrary(String userId, File origFile, a metadata) {
        if (metadata == null || !isEpubFile(origFile)) return;
        try {
            j4 factory = Utils.getFactory();
            if (factory == null) return;
            ILibraryService lib = factory.getLibraryService();
            if (lib == null) return;
            List<a> single = Collections.singletonList(metadata);
            lib.addContentMetadata(single);
            if (userId != null) {
                String path = metadata.e1();
                if (path == null || lib.W0(userId, path) == null) {
                    lib.W(userId, single, false);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    public static void transcode(File inFile, File outFile, boolean kf8Mode) throws Exception {
        Map<String, byte[]> zipEntries = new LinkedHashMap<String, byte[]>();
        ZipInputStream zis = new ZipInputStream(new FileInputStream(inFile));
        try {
            byte[] buf = new byte[8192];
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = normalizeZipPath(entry.getName());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int n;
                while ((n = zis.read(buf)) > 0) {
                    baos.write(buf, 0, n);
                }
                zipEntries.put(name, baos.toByteArray());
            }
        } finally {
            try { zis.close(); } catch (Throwable ignored) {}
        }

        // 1. Locate OPF
        String opfPath = null;
        byte[] containerBytes = zipEntries.get("META-INF/container.xml");
        if (containerBytes != null) {
            String cxml = new String(containerBytes, UTF8);
            Matcher m = Pattern.compile("full-path=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(cxml);
            if (m.find()) opfPath = normalizeZipPath(m.group(1));
        }
        if (opfPath == null || !zipEntries.containsKey(opfPath)) {
            for (String k : zipEntries.keySet()) {
                if (k.toLowerCase(Locale.US).endsWith(".opf")) {
                    opfPath = k;
                    break;
                }
            }
        }

        String defaultTitle = inFile.getName();
        if (defaultTitle.toLowerCase(Locale.US).endsWith(".epub")) {
            defaultTitle = defaultTitle.substring(0, defaultTitle.length() - 5);
        }
        String title = defaultTitle;
        String author = "Local EPUB Author";
        String publisher = "Kindle Local EPUB";
        String lang = "en";
        String pubDate = "2026-01-01";
        String coverId = null;
        String coverHref = null;
        String ncxPath = null;
        String navPath = null;

        Map<String, String> manifestIdToPath = new HashMap<String, String>();
        List<String> spinePaths = new ArrayList<String>();
        String opfDir = getDirName(opfPath);

        if (opfPath != null && zipEntries.containsKey(opfPath)) {
            String opfXml = new String(zipEntries.get(opfPath), UTF8);
            title = unescapeXml(extractXmlTag(opfXml, "<dc:title", "</dc:title>", title));
            author = unescapeXml(extractXmlTag(opfXml, "<dc:creator", "</dc:creator>", author));
            publisher = unescapeXml(extractXmlTag(opfXml, "<dc:publisher", "</dc:publisher>", publisher));
            lang = unescapeXml(extractXmlTag(opfXml, "<dc:language", "</dc:language>", lang));
            String rawDate = extractXmlTag(opfXml, "<dc:date", "</dc:date>", pubDate);
            if (rawDate.length() >= 10) pubDate = rawDate.substring(0, 10);

            Matcher cm1 = Pattern.compile("<meta[^>]+name=[\"']cover[\"'][^>]+content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(opfXml);
            if (cm1.find()) coverId = cm1.group(1);
            else {
                Matcher cm2 = Pattern.compile("<meta[^>]+content=[\"']([^\"']+)[\"'][^>]+name=[\"']cover[\"']", Pattern.CASE_INSENSITIVE).matcher(opfXml);
                if (cm2.find()) coverId = cm2.group(1);
            }

            Matcher itemM = Pattern.compile("<item\\b([^>]+)>", Pattern.CASE_INSENSITIVE).matcher(opfXml);
            while (itemM.find()) {
                String attrs = itemM.group(1);
                String id = extractAttr(attrs, "id");
                String href = extractAttr(attrs, "href");
                String props = extractAttr(attrs, "properties");
                String mtype = extractAttr(attrs, "media-type");
                if (id != null && href != null) {
                    String resolved = resolveRelPath(opfDir, href);
                    manifestIdToPath.put(id, resolved);
                    if (props != null && props.contains("cover-image")) coverHref = resolved;
                    if (props != null && (props.equals("nav") || props.contains(" nav") || props.contains("nav "))) navPath = resolved;
                    if ("application/x-dtbncx+xml".equalsIgnoreCase(mtype) || resolved.toLowerCase(Locale.US).endsWith(".ncx")) {
                        ncxPath = resolved;
                    }
                }
            }
            if (coverHref == null && coverId != null) coverHref = manifestIdToPath.get(coverId);

            Matcher spineM = Pattern.compile("<itemref\\b[^>]+idref=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(opfXml);
            while (spineM.find()) {
                String p = manifestIdToPath.get(spineM.group(1));
                if (p != null && zipEntries.containsKey(p)) spinePaths.add(p);
            }
        }

        if (spinePaths.isEmpty()) {
            for (String k : zipEntries.keySet()) {
                String kl = k.toLowerCase(Locale.US);
                if (kl.endsWith(".xhtml") || kl.endsWith(".html") || kl.endsWith(".htm")) spinePaths.add(k);
            }
            Collections.sort(spinePaths);
        }

        // 2. Parse ALL NCX chapter labels & #fragment anchors (supports nested navPoints & multi-chapter spine files)
        Map<String, List<NavAnchor>> fileToNavAnchors = new HashMap<String, List<NavAnchor>>();
        if (ncxPath != null && zipEntries.containsKey(ncxPath)) {
            String ncxXml = new String(zipEntries.get(ncxPath), UTF8);
            String ncxDir = getDirName(ncxPath);
            Matcher npM = Pattern.compile(
                    "<navLabel\\b[^>]*>.*?<text\\b[^>]*>(.*?)</text>.*?</navLabel>\\s*<content\\b[^>]+src=[\"']([^\"']+)[\"']",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE
            ).matcher(ncxXml);
            while (npM.find()) {
                String lbl = unescapeXml(npM.group(1)).replaceAll("\\s+", " ").trim();
                String src = npM.group(2);
                if (!lbl.isEmpty() && src != null && !src.isEmpty()) {
                    String filePart = src;
                    String fragId = "";
                    int hash = src.indexOf('#');
                    if (hash >= 0) {
                        filePart = src.substring(0, hash);
                        fragId = src.substring(hash + 1);
                    }
                    String resolved = resolveRelPath(ncxDir, filePart);
                    List<NavAnchor> list = fileToNavAnchors.get(resolved);
                    if (list == null) {
                        list = new ArrayList<NavAnchor>();
                        fileToNavAnchors.put(resolved, list);
                    }
                    list.add(new NavAnchor(lbl, fragId));
                }
            }
        }

        // 2b. EPUB3 nav.xhtml fallback if NCX was absent or yielded no entries
        if (fileToNavAnchors.isEmpty() && navPath != null && zipEntries.containsKey(navPath)) {
            String navXml = new String(zipEntries.get(navPath), UTF8);
            String navDir = getDirName(navPath);
            String tocSection = navXml;
            Matcher navTocM = Pattern.compile("<nav\\b[^>]*epub:type=[\"'][^\"']*toc[^\"']*[\"'][^>]*>(.*?)</nav>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(navXml);
            if (navTocM.find()) {
                tocSection = navTocM.group(1);
            }
            Matcher aTocM = Pattern.compile("<a\\b[^>]+href=[\"']([^\"']+)[\"'][^>]*>(.*?)</a>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(tocSection);
            while (aTocM.find()) {
                String src = aTocM.group(1);
                String lbl = unescapeXml(aTocM.group(2).replaceAll("<[^>]+>", " ")).replaceAll("\\s+", " ").trim();
                if (!lbl.isEmpty() && src != null && !src.isEmpty()) {
                    String filePart = src;
                    String fragId = "";
                    int hash = src.indexOf('#');
                    if (hash >= 0) {
                        filePart = src.substring(0, hash);
                        fragId = src.substring(hash + 1);
                    }
                    String resolved = filePart.isEmpty() ? navPath : resolveRelPath(navDir, filePart);
                    List<NavAnchor> list = fileToNavAnchors.get(resolved);
                    if (list == null) {
                        list = new ArrayList<NavAnchor>();
                        fileToNavAnchors.put(resolved, list);
                    }
                    list.add(new NavAnchor(lbl, fragId));
                }
            }
        }

        // 3. Collect & sanitize CSS (for KF8 Flow 1) so Kindle's native Aa theme/spacing controls work cleanly
        StringBuilder cssBuf = new StringBuilder();
        if (kf8Mode) {
            cssBuf.append("body { margin: 0; padding: 0; }\n");
            cssBuf.append("p { margin-top: 0; margin-bottom: 0.35em; }\n");
            cssBuf.append("table { border-collapse: collapse; }\n");
            cssBuf.append("img { max-width: 100%; height: auto; }\n");
            for (Map.Entry<String, byte[]> e : zipEntries.entrySet()) {
                if (e.getKey().toLowerCase(Locale.US).endsWith(".css")) {
                    cssBuf.append(sanitizeEpubCssForKindle(new String(e.getValue(), UTF8))).append("\n");
                }
            }
        }
        byte[] cssBytes = cssBuf.toString().getBytes(UTF8);

        // 4. Collect images (cover first at recindex=1)
        List<String> imagePaths = new ArrayList<String>();
        if (coverHref != null && zipEntries.containsKey(coverHref) && isImagePath(coverHref)) {
            imagePaths.add(coverHref);
        }
        for (String k : zipEntries.keySet()) {
            if (isImagePath(k) && !imagePaths.contains(k)) imagePaths.add(k);
        }
        boolean hasCover = (coverHref != null && !imagePaths.isEmpty());
        if (!hasCover && !imagePaths.isEmpty()) {
            for (int i = 0; i < imagePaths.size(); i++) {
                if (imagePaths.get(i).toLowerCase(Locale.US).contains("cover")) {
                    String cp = imagePaths.remove(i);
                    imagePaths.add(0, cp);
                    hasCover = true;
                    break;
                }
            }
        }

        Map<String, Integer> imgRecMap = new HashMap<String, Integer>();
        List<byte[]> imageRecords = new ArrayList<byte[]>();
        for (int i = 0; i < imagePaths.size(); i++) {
            String ip = imagePaths.get(i);
            byte[] mobiImg = ensureMobiCompatibleImage(ip, zipEntries.get(ip), i == 0);
            imageRecords.add(mobiImg);
            int recIndex1Based = i + 1;
            imgRecMap.put(ip, recIndex1Based);
            imgRecMap.put(ip.toLowerCase(Locale.US), recIndex1Based);
            String base = getBaseName(ip);
            if (!imgRecMap.containsKey(base)) {
                imgRecMap.put(base, recIndex1Based);
                imgRecMap.put(base.toLowerCase(Locale.US), recIndex1Based);
            }
        }
        // Save standalone high-res cover image sidecar (<outFile>.cover.jpg) for native fullscreen CoverOverlayView
        if (hasCover && !imageRecords.isEmpty()) {
            try {
                File coverSidecar = new File(outFile.getAbsolutePath() + ".cover.jpg");
                FileOutputStream cfos = new FileOutputStream(coverSidecar);
                try {
                    cfos.write(imageRecords.get(0));
                } finally {
                    cfos.close();
                }
                lastCoverPath = coverSidecar.getAbsolutePath();
                CoverOverlayView.resetSession();
            } catch (Throwable ignored) {}
        }

        // 5. Assemble spine HTML and record exact UTF-8 byte offsets for NCX INDX (and SKEL/FRAG in KF8)
        ByteArrayOutputStream htmlBaos = new ByteArrayOutputStream(65536);
        List<ChapterToc> chapters = new ArrayList<ChapterToc>();
        List<Kf8Section> kf8Sections = new ArrayList<Kf8Section>();

        Map<String, Integer> spineIndexMap = new HashMap<String, Integer>();
        for (int i = 0; i < spinePaths.size(); i++) {
            String sp = spinePaths.get(i);
            spineIndexMap.put(sp, Integer.valueOf(i));
            spineIndexMap.put(sp.toLowerCase(Locale.US), Integer.valueOf(i));
            String sbName = getBaseName(sp);
            if (!spineIndexMap.containsKey(sbName)) {
                spineIndexMap.put(sbName, Integer.valueOf(i));
                spineIndexMap.put(sbName.toLowerCase(Locale.US), Integer.valueOf(i));
            }
        }

        if (kf8Mode) {
            for (int i = 0; i < spinePaths.size(); i++) {
                String hp = spinePaths.get(i);
                String rawHtml = new String(zipEntries.get(hp), UTF8);
                String aid = String.format(Locale.US, "%04X", i + 1);
                String skel = "<html><head><link rel=\"stylesheet\" type=\"text/css\" href=\"kindle:flow:0001?mime=text/css\"/></head><body aid=\"0\"><div aid=\"" + aid + "\"></div></body></html>\n";
                byte[] skelBytes = skel.getBytes(UTF8);
                int skelStart = htmlBaos.size();
                int insertPos = skelStart + skel.indexOf("</div>");
                htmlBaos.write(skelBytes);
                int skelLen = skelBytes.length;

                String body = extractAndNormalizeBody(rawHtml, getDirName(hp), i, imgRecMap, spineIndexMap, true);
                String fragHtml = (body == null || body.isEmpty()) ? "<p>&#160;</p>" : body;
                byte[] fragBytes = fragHtml.getBytes(UTF8);
                int fragStart = htmlBaos.size();
                htmlBaos.write(fragBytes);
                int fragLen = fragBytes.length;

                kf8Sections.add(new Kf8Section(i, skelStart, skelLen, insertPos, fragStart, fragLen, aid));

                List<NavAnchor> anchors = fileToNavAnchors.get(hp);
                if (anchors != null && !anchors.isEmpty()) {
                    for (int a = 0; a < anchors.size(); a++) {
                        NavAnchor na = anchors.get(a);
                        int relOff = findAnchorByteOffset(fragHtml, na.fragId);
                        ChapterToc ct = new ChapterToc(na.label, fragStart + relOff);
                        ct.length = Math.max(1, fragLen - relOff);
                        chapters.add(ct);
                    }
                } else {
                    String chapLabel = extractHeadingOrTitle(rawHtml, "Section " + (i + 1));
                    ChapterToc ct = new ChapterToc(chapLabel, fragStart);
                    ct.length = Math.max(1, fragLen);
                    chapters.add(ct);
                }
            }
        } else {
            htmlBaos.write("<html><head></head><body>".getBytes(UTF8));
            for (int i = 0; i < spinePaths.size(); i++) {
                String hp = spinePaths.get(i);
                String rawHtml = new String(zipEntries.get(hp), UTF8);
                if (i > 0) {
                    htmlBaos.write("<mbp:pagebreak/>".getBytes(UTF8));
                }
                int chapStart = htmlBaos.size();
                String body = extractAndNormalizeBody(rawHtml, getDirName(hp), i, imgRecMap, spineIndexMap, false);
                byte[] bodyBytes = body.getBytes(UTF8);
                htmlBaos.write(bodyBytes);
                int bodyLen = bodyBytes.length;

                List<NavAnchor> anchors = fileToNavAnchors.get(hp);
                if (anchors != null && !anchors.isEmpty()) {
                    for (int a = 0; a < anchors.size(); a++) {
                        NavAnchor na = anchors.get(a);
                        int relOff = findAnchorByteOffset(body, na.fragId);
                        ChapterToc ct = new ChapterToc(na.label, chapStart + relOff);
                        ct.length = Math.max(1, bodyLen - relOff);
                        chapters.add(ct);
                    }
                } else {
                    String chapLabel = extractHeadingOrTitle(rawHtml, "Section " + (i + 1));
                    ChapterToc ct = new ChapterToc(chapLabel, chapStart);
                    ct.length = Math.max(1, bodyLen);
                    chapters.add(ct);
                }
            }
            htmlBaos.write("</body></html>".getBytes(UTF8));
        }
        // Normalize chapter lengths so each chapter spans cleanly up to the next chapter
        for (int i = 0; i + 1 < chapters.size(); i++) {
            int diff = chapters.get(i + 1).startOffset - chapters.get(i).startOffset;
            if (diff > 0) chapters.get(i).length = diff;
        }
        byte[] htmlBytes = htmlBaos.toByteArray();

        String asin = String.format(Locale.US, "B0%08X", Integer.valueOf(inFile.getName().hashCode()));
        byte[] containerBytesOut = kf8Mode
                ? buildPalmDbKf8(title, author, publisher, lang, pubDate, asin, htmlBytes, cssBytes, imageRecords, chapters, kf8Sections, hasCover)
                : buildPalmDbMobi7(title, author, publisher, lang, pubDate, asin, htmlBytes, imageRecords, chapters, hasCover);

        File tmpOut = new File(outFile.getParentFile(), outFile.getName() + ".tmp");
        FileOutputStream fos = new FileOutputStream(tmpOut);
        try {
            fos.write(containerBytesOut);
        } finally {
            try { fos.close(); } catch (Throwable ignored) {}
        }
        if (outFile.exists()) outFile.delete();
        tmpOut.renameTo(outFile);
    }

    private static String extractHeadingOrTitle(String html, String fallback) {
        for (String tag : new String[]{"h1", "h2", "h3", "title"}) {
            Matcher m = Pattern.compile("<" + tag + "\\b[^>]*>(.*?)</" + tag + ">", Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(html);
            if (m.find()) {
                String t = unescapeXml(m.group(1).replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim());
                if (!t.isEmpty() && t.length() <= 80) return t;
            }
        }
        return fallback;
    }

    // ==========================================================================
    // Binary NCX INDX Generator (Master INDX + Secondary INDX + CNCX)
    // ==========================================================================
    private static int findAnchorByteOffset(String html, String fragId) {
        if (html == null || fragId == null || fragId.isEmpty()) return 0;
        String[] needles = new String[]{
            "id=\"" + fragId + "\"",
            "id='" + fragId + "'",
            "name=\"" + fragId + "\"",
            "name='" + fragId + "'"
        };
        int idx = -1;
        for (String needle : needles) {
            idx = html.indexOf(needle);
            if (idx >= 0) break;
        }
        if (idx < 0) return 0;
        int tagStart = html.lastIndexOf('<', idx);
        if (tagStart < 0) tagStart = idx;
        return html.substring(0, tagStart).getBytes(UTF8).length;
    }

    private static List<byte[]> buildNcxIndxRecords(List<ChapterToc> chapters) throws Exception {
        List<byte[]> out = new ArrayList<byte[]>();
        if (chapters == null || chapters.isEmpty()) return out;

        // 1. Build CNCX string table
        ByteArrayOutputStream cncxBaos = new ByteArrayOutputStream();
        int[] labelOffsets = new int[chapters.size()];
        for (int i = 0; i < chapters.size(); i++) {
            labelOffsets[i] = cncxBaos.size();
            byte[] lb = chapters.get(i).label.getBytes(UTF8);
            if (lb.length > 120) {
                byte[] trimmed = new byte[120];
                System.arraycopy(lb, 0, trimmed, 0, 120);
                lb = trimmed;
            }
            writeVwiForward(cncxBaos, lb.length);
            cncxBaos.write(lb);
        }
        while (cncxBaos.size() % 4 != 0) cncxBaos.write(0);
        byte[] cncxRec = cncxBaos.toByteArray();

        // 2. Build Secondary INDX record (all chapter entries)
        ByteArrayOutputStream secEntries = new ByteArrayOutputStream();
        int[] secOffs = new int[chapters.size()];
        String lastKey = "0000";
        for (int i = 0; i < chapters.size(); i++) {
            secOffs[i] = 192 + secEntries.size();
            String key = String.format(Locale.US, "%04d", i);
            lastKey = key;
            byte[] kb = key.getBytes(UTF8);
            secEntries.write(kb.length);
            secEntries.write(kb);
            secEntries.write(0x0F); // control byte: tags 1 (offset), 2 (len), 3 (label), 4 (depth)
            writeVwiForward(secEntries, chapters.get(i).startOffset);
            writeVwiForward(secEntries, chapters.get(i).length);
            writeVwiForward(secEntries, labelOffsets[i]);
            writeVwiForward(secEntries, 0); // depth = 0
        }
        while (secEntries.size() % 4 != 0) secEntries.write(0);
        int secIdxtOff = 192 + secEntries.size();

        ByteArrayOutputStream secIdxt = new ByteArrayOutputStream();
        secIdxt.write("IDXT".getBytes(UTF8));
        for (int off : secOffs) writeShortBE(secIdxt, off);
        while (secIdxt.size() % 4 != 0) secIdxt.write(0);

        byte[] secHdr = new byte[192];
        System.arraycopy("INDX".getBytes(UTF8), 0, secHdr, 0, 4);
        putIntBE(secHdr, 4, 192);
        putIntBE(secHdr, 8, 0);          // index_type = 0 (leaf)
        putIntBE(secHdr, 12, 1);
        putIntBE(secHdr, 16, 0);         // idxt_encoding
        putIntBE(secHdr, 20, secIdxtOff);
        putIntBE(secHdr, 24, chapters.size());
        putIntBE(secHdr, 28, -1);
        putIntBE(secHdr, 32, -1);
        ByteArrayOutputStream secRec = new ByteArrayOutputStream();
        secRec.write(secHdr);
        secRec.write(secEntries.toByteArray());
        secRec.write(secIdxt.toByteArray());

        // 3. Build Master INDX record (header + TAGX + 1 entry pointing to Secondary INDX + IDXT)
        byte[] tagx = new byte[]{
            'T','A','G','X', 0,0,0,32, 0,0,0,1,
            1, 1, 0x01, 0, // Tag 1: Pos (offset)
            2, 1, 0x02, 0, // Tag 2: Len
            3, 1, 0x04, 0, // Tag 3: Label CNCX offset
            4, 1, 0x08, 0, // Tag 4: Depth
            0, 0, 0x00, 1  // End of control byte 1
        };
        int masterEntryOff = 192 + tagx.length;
        ByteArrayOutputStream masterEntries = new ByteArrayOutputStream();
        byte[] lkb = lastKey.getBytes(UTF8);
        masterEntries.write(lkb.length);
        masterEntries.write(lkb);
        writeShortBE(masterEntries, chapters.size());
        while ((192 + tagx.length + masterEntries.size()) % 4 != 0) masterEntries.write(0);
        int masterIdxtOff = masterEntryOff + masterEntries.size();

        ByteArrayOutputStream masterIdxt = new ByteArrayOutputStream();
        masterIdxt.write("IDXT".getBytes(UTF8));
        writeShortBE(masterIdxt, masterEntryOff);
        while ((tagx.length + masterEntries.size() + masterIdxt.size()) % 4 != 0) masterIdxt.write(0);

        byte[] masterHdr = new byte[192];
        System.arraycopy("INDX".getBytes(UTF8), 0, masterHdr, 0, 4);
        putIntBE(masterHdr, 4, 192);
        putIntBE(masterHdr, 8, 0);
        putIntBE(masterHdr, 12, 0);
        putIntBE(masterHdr, 16, 2);             // index_type = 2 (master)
        putIntBE(masterHdr, 20, masterIdxtOff);
        putIntBE(masterHdr, 24, 1);            // 1 secondary record
        putIntBE(masterHdr, 28, 65001);        // UTF-8
        putIntBE(masterHdr, 32, -1);           // language
        putIntBE(masterHdr, 36, chapters.size());
        putIntBE(masterHdr, 52, 1);            // cncx_count = 1
        putIntBE(masterHdr, 180, 192);         // tagx_offset = 192
        putIntBE(masterHdr, 184, 0);

        ByteArrayOutputStream masterRec = new ByteArrayOutputStream();
        masterRec.write(masterHdr);
        masterRec.write(tagx);
        masterRec.write(masterEntries.toByteArray());
        masterRec.write(masterIdxt.toByteArray());

        out.add(masterRec.toByteArray());
        out.add(secRec.toByteArray());
        out.add(cncxRec);
        return out;
    }

    // ==========================================================================
    // MOBI7 Builder (header_length = 248 + NCX INDX)
    // ==========================================================================
    private static byte[] buildPalmDbMobi7(
            String title, String author, String publisher, String lang, String pubDate, String asin,
            byte[] htmlBytes, List<byte[]> imageRecords, List<ChapterToc> chapters, boolean hasCover
    ) throws Exception {
        List<byte[]> textRecords = sliceUtf8Records(htmlBytes);
        List<byte[]> ncxRecords = buildNcxIndxRecords(chapters);

        int numText = textRecords.size();
        int numImgs = imageRecords.size();
        int firstNonBookIdx = numText + 1;
        int firstImgIdx = (numImgs > 0) ? (numText + 1) : -1;
        int lastContentRec = numText + numImgs;
        int ncxIdx = !ncxRecords.isEmpty() ? (numText + numImgs + 1) : -1;
        int flisIdx = numText + numImgs + ncxRecords.size() + 1;
        int fcisIdx = flisIdx + 1;

        byte[] exthBytes = buildExth(title, author, publisher, lang, pubDate, asin, numImgs, false, hasCover);
        byte[] titleBytes = title.getBytes(UTF8);
        int mobiHdrLen = 248;
        int fullNameOff = 16 + mobiHdrLen + exthBytes.length;
        int rec0Len = fullNameOff + titleBytes.length + 2;
        while (rec0Len % 4 != 0) rec0Len++;
        byte[] rec0 = new byte[rec0Len];

        putShortBE(rec0, 0, 1);
        putIntBE(rec0, 4, htmlBytes.length);
        putShortBE(rec0, 8, numText);
        putShortBE(rec0, 10, 4096);

        putIntBE(rec0, 16, 0x4D4F4249);
        putIntBE(rec0, 20, mobiHdrLen);
        putIntBE(rec0, 24, 2);
        putIntBE(rec0, 28, 65001);
        putIntBE(rec0, 32, 0x12345678);
        putIntBE(rec0, 36, 6);
        for (int off = 40; off < 80; off += 4) putIntBE(rec0, off, -1);
        putIntBE(rec0, 80, firstNonBookIdx);
        putIntBE(rec0, 84, fullNameOff);
        putIntBE(rec0, 88, titleBytes.length);
        putIntBE(rec0, 92, 9);
        putIntBE(rec0, 104, 6);
        putIntBE(rec0, 108, firstImgIdx);
        putIntBE(rec0, 128, 0x50);
        for (int off = 164; off < 192; off += 4) putIntBE(rec0, off, -1);
        putShortBE(rec0, 192, 1);
        putShortBE(rec0, 194, lastContentRec);
        putIntBE(rec0, 196, 1);
        putIntBE(rec0, 200, fcisIdx);
        putIntBE(rec0, 204, 1);
        putIntBE(rec0, 208, flisIdx);
        putIntBE(rec0, 212, 1);
        putIntBE(rec0, 240, 3);      // extra_data_flags (MOBI+224)
        putIntBE(rec0, 244, ncxIdx); // ncx_index (MOBI+228 & MOBI+230)
        putIntBE(rec0, 256, 0);      // MOBI+240
        putIntBE(rec0, 260, -1);     // MOBI+244 (-1 for MOBI7, KF8 uses offset 244)

        System.arraycopy(exthBytes, 0, rec0, 16 + mobiHdrLen, exthBytes.length);
        System.arraycopy(titleBytes, 0, rec0, fullNameOff, titleBytes.length);

        List<byte[]> allRecords = new ArrayList<byte[]>();
        allRecords.add(rec0);
        allRecords.addAll(textRecords);
        allRecords.addAll(imageRecords);
        allRecords.addAll(ncxRecords);
        allRecords.add(makeFlisRecord());
        allRecords.add(makeFcisRecord(htmlBytes.length));
        allRecords.add(new byte[]{(byte)0xE9, (byte)0x8E, 0x0D, 0x0A});
        return packPalmDb(title, allRecords);
    }

    // ==========================================================================
    // KF8 (MOBI8 / libWebCoreKRF.so) Builder (Round 008: fixed rec0 offsets, INDX headers & per-chapter SKEL/FRAG)
    // ==========================================================================
    private static byte[] buildPalmDbKf8(
            String title, String author, String publisher, String lang, String pubDate, String asin,
            byte[] htmlBytes, byte[] cssBytes, List<byte[]> imageRecords,
            List<ChapterToc> chapters, List<Kf8Section> kf8Sections, boolean hasCover
    ) throws Exception {
        byte[] combinedText = new byte[htmlBytes.length + cssBytes.length];
        System.arraycopy(htmlBytes, 0, combinedText, 0, htmlBytes.length);
        System.arraycopy(cssBytes, 0, combinedText, htmlBytes.length, cssBytes.length);

        List<byte[]> textRecords = sliceUtf8RecordsWithOverlap(combinedText);
        List<byte[]> fragRecords = buildKf8FragIndx(kf8Sections);
        List<byte[]> skelRecords = buildKf8SkelIndx(kf8Sections);
        List<byte[]> ncxRecords = buildNcxIndxRecords(chapters);
        byte[] fdstRec = buildFdstRecord(htmlBytes.length, cssBytes.length);

        int numText = textRecords.size();
        int numImgs = imageRecords.size();
        int curIdx = numText + 1;
        int firstNonBookIdx = curIdx; // Points directly to FRAG Master INDX (matches working AZW3)
        int fragIdx = curIdx; curIdx += fragRecords.size();
        int skelIdx = curIdx; curIdx += skelRecords.size();
        int ncxIdx = !ncxRecords.isEmpty() ? curIdx : -1;
        curIdx += ncxRecords.size();
        int firstImgIdx = (numImgs > 0) ? curIdx : -1;
        curIdx += numImgs;
        int fdstIdx = curIdx++;
        int flisIdx = curIdx++;
        int fcisIdx = curIdx++;

        byte[] exthBytes = buildExth(title, author, publisher, lang, pubDate, asin, numImgs, true, hasCover);
        byte[] titleBytes = title.getBytes(UTF8);
        int mobiHdrLen = 248; // 16 + 248 = 264 bytes total (rec0 + 0 .. rec0 + 263)
        int fullNameOff = 16 + mobiHdrLen + exthBytes.length;
        int rec0Len = fullNameOff + titleBytes.length + 2;
        while (rec0Len % 4 != 0) rec0Len++;
        byte[] rec0 = new byte[rec0Len];

        putShortBE(rec0, 0, 1);
        putIntBE(rec0, 4, combinedText.length);
        putShortBE(rec0, 8, numText);
        putShortBE(rec0, 10, 4096);

        putIntBE(rec0, 16, 0x4D4F4249);
        putIntBE(rec0, 20, mobiHdrLen);
        putIntBE(rec0, 24, 2);
        putIntBE(rec0, 28, 65001);
        putIntBE(rec0, 32, 0x12345678);
        putIntBE(rec0, 36, 8); // file_version = 8 (KF8)
        for (int off = 40; off < 80; off += 4) putIntBE(rec0, off, -1);
        putIntBE(rec0, 80, firstNonBookIdx);
        putIntBE(rec0, 84, fullNameOff);
        putIntBE(rec0, 88, titleBytes.length);
        putIntBE(rec0, 92, 9);
        putIntBE(rec0, 104, 8); // min_version = 8
        putIntBE(rec0, 108, firstImgIdx);
        putIntBE(rec0, 128, 0x50);
        for (int off = 164; off < 192; off += 4) putIntBE(rec0, off, -1);
        putIntBE(rec0, 192, fdstIdx); // MOBI+176 (rec0+192): fdst_record
        putIntBE(rec0, 196, 2);       // MOBI+180 (rec0+196): fdst_flow_count (HTML + CSS)
        putIntBE(rec0, 200, fcisIdx);
        putIntBE(rec0, 204, 1);
        putIntBE(rec0, 208, flisIdx);
        putIntBE(rec0, 212, 1);
        putIntBE(rec0, 224, -1);
        putIntBE(rec0, 228, 0);
        putIntBE(rec0, 232, -1);
        putIntBE(rec0, 236, -1);
        putIntBE(rec0, 240, 1);       // MOBI+224 (rec0+240): extra_data_flags = 1 (multibyte overlap trailer)
        putIntBE(rec0, 244, ncxIdx);  // MOBI+228 (rec0+244): ncx_index
        putIntBE(rec0, 248, fragIdx); // MOBI+232 (rec0+248): frag_index
        putIntBE(rec0, 252, skelIdx); // MOBI+236 (rec0+252): skel_index
        putIntBE(rec0, 256, -1);      // MOBI+240 (rec0+256): datp_index
        putIntBE(rec0, 260, -1);      // MOBI+244 (rec0+260): guide_index

        System.arraycopy(exthBytes, 0, rec0, 16 + mobiHdrLen, exthBytes.length);
        System.arraycopy(titleBytes, 0, rec0, fullNameOff, titleBytes.length);

        List<byte[]> allRecords = new ArrayList<byte[]>();
        allRecords.add(rec0);
        allRecords.addAll(textRecords);
        allRecords.addAll(fragRecords);
        allRecords.addAll(skelRecords);
        allRecords.addAll(ncxRecords);
        allRecords.addAll(imageRecords);
        allRecords.add(fdstRec);
        allRecords.add(makeFlisRecord());
        allRecords.add(makeFcisRecord(combinedText.length));
        allRecords.add(new byte[]{(byte)0xE9, (byte)0x8E, 0x0D, 0x0A});
        return packPalmDb(title, allRecords);
    }

    private static byte[] buildFdstRecord(int htmlLen, int cssLen) {
        byte[] fdst = new byte[12 + 16];
        System.arraycopy("FDST".getBytes(UTF8), 0, fdst, 0, 4);
        putIntBE(fdst, 4, 12);
        putIntBE(fdst, 8, 2);
        putIntBE(fdst, 12, 0);
        putIntBE(fdst, 16, htmlLen);
        putIntBE(fdst, 20, htmlLen);
        putIntBE(fdst, 24, htmlLen + cssLen);
        return fdst;
    }

    private static List<byte[]> buildKf8SkelIndx(List<Kf8Section> sections) throws Exception {
        List<byte[]> out = new ArrayList<byte[]>();
        int count = sections.size();
        ByteArrayOutputStream secE = new ByteArrayOutputStream();
        int[] secOffs = new int[count];
        String lastKey = "SKEL0000000000";
        for (int i = 0; i < count; i++) {
            Kf8Section sec = sections.get(i);
            secOffs[i] = 192 + secE.size();
            String key = String.format(Locale.US, "SKEL%010d", i);
            lastKey = key;
            byte[] kb = key.getBytes(UTF8);
            secE.write(kb.length);
            secE.write(kb);
            secE.write(0x03); // Tag 1 (chunk_count=1) + Tag 6 (start, len)
            writeVwiForward(secE, 1);
            writeVwiForward(secE, sec.skelStart);
            writeVwiForward(secE, sec.skelLen);
        }
        while (secE.size() % 4 != 0) secE.write(0);
        int secIdxtOff = 192 + secE.size();

        ByteArrayOutputStream secIdxt = new ByteArrayOutputStream();
        secIdxt.write("IDXT".getBytes(UTF8));
        for (int off : secOffs) writeShortBE(secIdxt, off);
        while (secIdxt.size() % 4 != 0) secIdxt.write(0);

        byte[] secHdr = new byte[192];
        System.arraycopy("INDX".getBytes(UTF8), 0, secHdr, 0, 4);
        putIntBE(secHdr, 4, 192);
        putIntBE(secHdr, 8, 0);
        putIntBE(secHdr, 12, 1); // Secondary INDX (verified in libKRF.so)
        putIntBE(secHdr, 16, 0);
        putIntBE(secHdr, 20, secIdxtOff);
        putIntBE(secHdr, 24, count);
        putIntBE(secHdr, 28, -1);
        putIntBE(secHdr, 32, -1);
        ByteArrayOutputStream secRec = new ByteArrayOutputStream();
        secRec.write(secHdr);
        secRec.write(secE.toByteArray());
        secRec.write(secIdxt.toByteArray());

        byte[] tagx = new byte[]{
            'T','A','G','X', 0,0,0,24, 0,0,0,1,
            1, 1, 0x01, 0, // Tag 1: chunk_count
            6, 2, 0x02, 0, // Tag 6: start, length
            0, 0, 0x00, 1
        };
        int mOff = 192 + tagx.length;
        ByteArrayOutputStream mE = new ByteArrayOutputStream();
        byte[] lkb = lastKey.getBytes(UTF8);
        mE.write(lkb.length);
        mE.write(lkb);
        writeShortBE(mE, count);
        while ((mOff + mE.size()) % 4 != 0) mE.write(0);
        int mIdxtOff = mOff + mE.size();

        ByteArrayOutputStream mIdxt = new ByteArrayOutputStream();
        mIdxt.write("IDXT".getBytes(UTF8));
        writeShortBE(mIdxt, mOff);
        while ((tagx.length + mE.size() + mIdxt.size()) % 4 != 0) mIdxt.write(0);

        byte[] mHdr = new byte[192];
        System.arraycopy("INDX".getBytes(UTF8), 0, mHdr, 0, 4);
        putIntBE(mHdr, 4, 192);
        putIntBE(mHdr, 8, 0);
        putIntBE(mHdr, 12, 0);
        putIntBE(mHdr, 16, 2); // Master INDX (verified in libKRF.so)
        putIntBE(mHdr, 20, mIdxtOff);
        putIntBE(mHdr, 24, 1);
        putIntBE(mHdr, 28, 65001);
        putIntBE(mHdr, 32, -1);
        putIntBE(mHdr, 36, count);
        putIntBE(mHdr, 52, 0);
        putIntBE(mHdr, 180, 192);

        ByteArrayOutputStream mRec = new ByteArrayOutputStream();
        mRec.write(mHdr);
        mRec.write(tagx);
        mRec.write(mE.toByteArray());
        mRec.write(mIdxt.toByteArray());

        out.add(mRec.toByteArray());
        out.add(secRec.toByteArray());
        return out;
    }

    private static List<byte[]> buildKf8FragIndx(List<Kf8Section> sections) throws Exception {
        List<byte[]> out = new ArrayList<byte[]>();
        int count = sections.size();

        ByteArrayOutputStream cncx = new ByteArrayOutputStream();
        int[] cncxOffs = new int[count];
        for (int i = 0; i < count; i++) {
            cncxOffs[i] = cncx.size();
            String selector = "P-//*[@aid='" + sections.get(i).aid + "']";
            byte[] aidBytes = selector.getBytes(UTF8);
            writeVwiForward(cncx, aidBytes.length);
            cncx.write(aidBytes);
        }
        while (cncx.size() % 4 != 0) cncx.write(0);

        ByteArrayOutputStream secE = new ByteArrayOutputStream();
        int[] secOffs = new int[count];
        String lastKey = "0000000000";
        for (int i = 0; i < count; i++) {
            Kf8Section sec = sections.get(i);
            secOffs[i] = 192 + secE.size();
            String key = String.format(Locale.US, "%010d", sec.insertPos);
            lastKey = key;
            byte[] kb = key.getBytes(UTF8);
            secE.write(kb.length);
            secE.write(kb);
            secE.write(0x0F); // Tags 2 (0x01), 3 (0x02), 4 (0x04), 6 (0x08) -> 0x0F!
            writeVwiForward(secE, cncxOffs[i]);  // Tag 2: cncx offset
            writeVwiForward(secE, sec.fileNum);  // Tag 3: file_num
            writeVwiForward(secE, 0);            // Tag 4: seq_num
            writeVwiForward(secE, 0);            // Tag 6a: relative start offset after skeleton end (0 = exact start of fragment!)
            writeVwiForward(secE, sec.fragLen);  // Tag 6b: length
        }
        while (secE.size() % 4 != 0) secE.write(0);
        int secIdxtOff = 192 + secE.size();

        ByteArrayOutputStream secIdxt = new ByteArrayOutputStream();
        secIdxt.write("IDXT".getBytes(UTF8));
        for (int off : secOffs) writeShortBE(secIdxt, off);
        while (secIdxt.size() % 4 != 0) secIdxt.write(0);

        byte[] secHdr = new byte[192];
        System.arraycopy("INDX".getBytes(UTF8), 0, secHdr, 0, 4);
        putIntBE(secHdr, 4, 192);
        putIntBE(secHdr, 8, 0);
        putIntBE(secHdr, 12, 1); // Secondary INDX (verified in libKRF.so)
        putIntBE(secHdr, 16, 0);
        putIntBE(secHdr, 20, secIdxtOff);
        putIntBE(secHdr, 24, count);
        putIntBE(secHdr, 28, -1);
        putIntBE(secHdr, 32, -1);
        ByteArrayOutputStream secRec = new ByteArrayOutputStream();
        secRec.write(secHdr);
        secRec.write(secE.toByteArray());
        secRec.write(secIdxt.toByteArray());

        byte[] tagx = new byte[]{
            'T','A','G','X', 0,0,0,32, 0,0,0,1,
            2, 1, 0x01, 0,
            3, 1, 0x02, 0,
            4, 1, 0x04, 0,
            6, 2, 0x08, 0,
            0, 0, 0x00, 1
        };
        int mOff = 192 + tagx.length;
        ByteArrayOutputStream mE = new ByteArrayOutputStream();
        byte[] lkb = lastKey.getBytes(UTF8);
        mE.write(lkb.length);
        mE.write(lkb);
        writeShortBE(mE, count);
        while ((mOff + mE.size()) % 4 != 0) mE.write(0);
        int mIdxtOff = mOff + mE.size();

        ByteArrayOutputStream mIdxt = new ByteArrayOutputStream();
        mIdxt.write("IDXT".getBytes(UTF8));
        writeShortBE(mIdxt, mOff);
        while ((tagx.length + mE.size() + mIdxt.size()) % 4 != 0) mIdxt.write(0);

        byte[] mHdr = new byte[192];
        System.arraycopy("INDX".getBytes(UTF8), 0, mHdr, 0, 4);
        putIntBE(mHdr, 4, 192);
        putIntBE(mHdr, 8, 0);
        putIntBE(mHdr, 12, 0);
        putIntBE(mHdr, 16, 2); // Master INDX (verified in libKRF.so)
        putIntBE(mHdr, 20, mIdxtOff);
        putIntBE(mHdr, 24, 1);
        putIntBE(mHdr, 28, 65001);
        putIntBE(mHdr, 32, -1);
        putIntBE(mHdr, 36, count);
        putIntBE(mHdr, 52, 1);
        putIntBE(mHdr, 180, 192);

        ByteArrayOutputStream mRec = new ByteArrayOutputStream();
        mRec.write(mHdr);
        mRec.write(tagx);
        mRec.write(mE.toByteArray());
        mRec.write(mIdxt.toByteArray());

        out.add(mRec.toByteArray());
        out.add(secRec.toByteArray());
        out.add(cncx.toByteArray());
        return out;
    }

    private static byte[] buildExth(String title, String author, String publisher, String lang, String pubDate, String asin, int numImgs, boolean kf8, boolean hasCover) throws Exception {
        List<int[]> types = new ArrayList<int[]>();
        List<byte[]> payloads = new ArrayList<byte[]>();
        addExth(types, payloads, 100, author.getBytes(UTF8));
        addExth(types, payloads, 101, publisher.getBytes(UTF8));
        addExth(types, payloads, 106, pubDate.getBytes(UTF8));
        addExth(types, payloads, 113, asin.getBytes(UTF8));
        if (kf8) {
            byte[] ffff = new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
            addExth(types, payloads, 121, ffff);
            byte[] cnt = new byte[4];
            putIntBE(cnt, 0, numImgs);
            addExth(types, payloads, 125, cnt);
            if (hasCover && numImgs > 0) {
                addExth(types, payloads, 129, "kindle:embed:0001?mime=image/jpeg".getBytes(UTF8));
            }
            addExth(types, payloads, 131, new byte[]{0, 0, 0, 0});
        }
        addExth(types, payloads, 501, "EBOK".getBytes(UTF8));
        addExth(types, payloads, 503, title.getBytes(UTF8));
        addExth(types, payloads, 524, lang.getBytes(UTF8));
        if (numImgs > 0) {
            byte[] zero4 = new byte[]{0, 0, 0, 0};
            addExth(types, payloads, 201, zero4);
            addExth(types, payloads, 202, zero4);
            addExth(types, payloads, 203, zero4);
        }

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        for (int i = 0; i < types.size(); i++) {
            writeIntBE(body, types.get(i)[0]);
            writeIntBE(body, 8 + payloads.get(i).length);
            body.write(payloads.get(i));
        }
        int totalLen = 12 + body.size();
        while (totalLen % 4 != 0) {
            body.write(0);
            totalLen++;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write("EXTH".getBytes(UTF8));
        writeIntBE(out, totalLen);
        writeIntBE(out, types.size());
        out.write(body.toByteArray());
        return out.toByteArray();
    }

    private static List<byte[]> sliceUtf8Records(byte[] data) {
        List<byte[]> recs = new ArrayList<byte[]>();
        int pos = 0, total = data.length;
        while (pos < total) {
            int end = Math.min(pos + 4096, total);
            while (end < total && end > pos && (data[end] & 0xC0) == 0x80) end--;
            if (end == pos) end = Math.min(pos + 4096, total);
            byte[] chunk = new byte[end - pos];
            System.arraycopy(data, pos, chunk, 0, chunk.length);
            recs.add(chunk);
            pos = end;
        }
        if (recs.isEmpty()) recs.add("<html><head></head><body></body></html>".getBytes(UTF8));
        return recs;
    }

    private static List<byte[]> sliceUtf8RecordsWithOverlap(byte[] data) {
        List<byte[]> recs = new ArrayList<byte[]>();
        int pos = 0, total = data.length;
        while (pos < total) {
            int end = Math.min(pos + 4096, total);
            while (end < total && end > pos && (data[end] & 0xC0) == 0x80) end--;
            if (end == pos) end = Math.min(pos + 4096, total);
            int len = end - pos;
            byte[] chunk = new byte[len + 1];
            System.arraycopy(data, pos, chunk, 0, len);
            chunk[len] = 0x00; // 0 multibyte overlap bytes (extra_data_flags = 1)
            recs.add(chunk);
            pos = end;
        }
        if (recs.isEmpty()) {
            byte[] empty = "<html><head></head><body></body></html>\0".getBytes(UTF8);
            recs.add(empty);
        }
        return recs;
    }

    private static byte[] makeFlisRecord() {
        return new byte[]{
            'F','L','I','S', 0,0,0,8, 0,0x41,0,0, 0,0,0,0,
            (byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF, 0,1,0,3, 0,0,0,3, 0,0,0,1,
            (byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF
        };
    }

    private static byte[] makeFcisRecord(int textLen) {
        byte[] fcis = new byte[44];
        fcis[0]='F'; fcis[1]='C'; fcis[2]='I'; fcis[3]='S';
        putIntBE(fcis, 4, 20);
        putIntBE(fcis, 8, 16);
        putIntBE(fcis, 12, 1);
        putIntBE(fcis, 20, textLen);
        putIntBE(fcis, 28, 32);
        putIntBE(fcis, 32, 8);
        putShortBE(fcis, 36, 1);
        putShortBE(fcis, 38, 1);
        return fcis;
    }

    private static byte[] packPalmDb(String title, List<byte[]> allRecords) throws Exception {
        int numRecords = allRecords.size();
        byte[] pdbHeader = new byte[78];
        String safeTitle = title.replaceAll("[^A-Za-z0-9_-]", "_");
        if (safeTitle.length() > 31) safeTitle = safeTitle.substring(0, 31);
        byte[] safeBytes = safeTitle.getBytes(UTF8);
        System.arraycopy(safeBytes, 0, pdbHeader, 0, Math.min(31, safeBytes.length));
        putIntBE(pdbHeader, 60, 0x424F4F4B);
        putIntBE(pdbHeader, 64, 0x4D4F4249);
        putShortBE(pdbHeader, 76, numRecords);

        ByteArrayOutputStream out = new ByteArrayOutputStream(65536);
        out.write(pdbHeader);
        int curOff = 78 + 8 * numRecords + 2;
        for (int i = 0; i < numRecords; i++) {
            writeIntBE(out, curOff);
            writeIntBE(out, i * 2);
            curOff += allRecords.get(i).length;
        }
        out.write(0);
        out.write(0);
        for (byte[] rec : allRecords) out.write(rec);
        return out.toByteArray();
    }

    private static String toBase32Four(int val) {
        final String alpha = "0123456789ABCDEFGHIJKLMNOPQRSTUV";
        char[] c = new char[4];
        int v = Math.max(0, val);
        for (int i = 3; i >= 0; i--) {
            c[i] = alpha.charAt(v & 31);
            v >>>= 5;
        }
        return new String(c);
    }

    private static String toBase32Ten(int val) {
        final String alpha = "0123456789ABCDEFGHIJKLMNOPQRSTUV";
        char[] c = new char[10];
        long v = Math.max(0L, (long) val);
        for (int i = 9; i >= 0; i--) {
            c[i] = alpha.charAt((int) (v & 31L));
            v >>>= 5;
        }
        return new String(c);
    }

    private static void writeVwiForward(ByteArrayOutputStream out, int val) {
        byte[] tmp = new byte[5];
        int n = 0;
        do {
            tmp[n++] = (byte) (val & 0x7F);
            val >>>= 7;
        } while (val != 0);
        for (int i = n - 1; i >= 0; i--) {
            out.write(i == 0 ? (tmp[i] | 0x80) : tmp[i]);
        }
    }

    private static byte[] ensureMobiCompatibleImage(String path, byte[] raw, boolean isCover) {
        if (raw == null || raw.length < 4) return raw;
        boolean isJpeg = ((raw[0] & 0xFF) == 0xFF && (raw[1] & 0xFF) == 0xD8);
        if (isJpeg && (!isCover || raw.length <= 350000)) return raw;
        try {
            Bitmap bmp = BitmapFactory.decodeByteArray(raw, 0, raw.length);
            if (bmp != null) {
                int w = bmp.getWidth(), h = bmp.getHeight();
                if (isCover && (w > 1080 || h > 1600)) {
                    float scale = Math.min(1080f / w, 1600f / h);
                    int nw = Math.max(1, (int) (w * scale));
                    int nh = Math.max(1, (int) (h * scale));
                    Bitmap scaled = Bitmap.createScaledBitmap(bmp, nw, nh, true);
                    if (scaled != bmp) { bmp.recycle(); bmp = scaled; }
                    w = nw; h = nh;
                }
                Bitmap rgb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(rgb);
                canvas.drawColor(0xFFFFFFFF);
                canvas.drawBitmap(bmp, 0f, 0f, null);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                rgb.compress(Bitmap.CompressFormat.JPEG, isCover ? 80 : 88, baos);
                rgb.recycle();
                bmp.recycle();
                byte[] jpg = baos.toByteArray();
                if (jpg.length > 0) return jpg;
            }
        } catch (Throwable ignored) {}
        return raw;
    }

    private static boolean isLikelyCoverSpineItem(String path, String rawHtml) {
        String p = (path == null) ? "" : path.toLowerCase(Locale.US);
        if (p.contains("cover") || p.contains("titlepage")) return true;
        if (rawHtml != null) {
            String stripped = rawHtml.replaceAll("(?s)<head\\b[^>]*>.*?</head>", "")
                    .replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
            boolean hasImg = rawHtml.toLowerCase(Locale.US).contains("<img") || rawHtml.toLowerCase(Locale.US).contains("<image");
            if (hasImg && stripped.length() < 40) return true;
        }
        return false;
    }

    private static String sanitizeEpubCssForKindle(String css) {
        if (css == null || css.isEmpty()) return "";
        // Strip rigid line-height and fixed body/paragraph font-size declarations that crush Kindle's Aa theme spacing
        String s = css.replaceAll("(?i)line-height\\s*:[^;\\}]+;?", "");
        s = s.replaceAll("(?i)letter-spacing\\s*:[^;\\}]+;?", "");
        s = s.replaceAll("(?i)word-spacing\\s*:[^;\\}]+;?", "");
        // Avoid fixed px/pt font-size locking body prose (keep em/% headings intact)
        s = s.replaceAll("(?i)font-size\\s*:\\s*[0-9.]+(px|pt)\\s*;?", "");
        return s;
    }

    private static String extractAndNormalizeBody(String rawHtml, String htmlDir, int currentSpineIdx, Map<String, Integer> imgRecMap, Map<String, Integer> spineIndexMap, boolean kf8Mode) {
        String s = rawHtml;
        int bIdx = indexOfIgnoreCase(s, "<body");
        if (bIdx >= 0) {
            int gt = s.indexOf('>', bIdx);
            if (gt >= 0) {
                int end = indexOfIgnoreCase(s, "</body>", gt + 1);
                s = (end >= 0) ? s.substring(gt + 1, end) : s.substring(gt + 1);
            }
        }
        s = Pattern.compile("<(style|script)\\b[^>]*>.*?</\\1>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(s).replaceAll("");
        if (!kf8Mode) {
            s = normalizeOrderedLists(s);
        }
        Matcher imgM = Pattern.compile("<img\\b[^>]*>", Pattern.CASE_INSENSITIVE).matcher(s);
        StringBuffer sb = new StringBuffer();
        while (imgM.find()) {
            String tag = imgM.group(0);
            String src = extractAttr(tag, "src");
            if (src != null) {
                int q = src.indexOf('?'); if (q >= 0) src = src.substring(0, q);
                int h = src.indexOf('#'); if (h >= 0) src = src.substring(0, h);
                String resolved = resolveRelPath(htmlDir, src);
                Integer ridx = imgRecMap.get(resolved);
                if (ridx == null) ridx = imgRecMap.get(resolved.toLowerCase(Locale.US));
                if (ridx == null) ridx = imgRecMap.get(getBaseName(src));
                if (ridx == null) ridx = imgRecMap.get(getBaseName(src).toLowerCase(Locale.US));
                if (ridx != null) {
                    String replacement = kf8Mode
                            ? "<img src=\"kindle:embed:" + toBase32Four(ridx.intValue()) + "?mime=image/jpeg\"/>"
                            : String.format(Locale.US, "<img recindex=\"%05d\"/>", ridx);
                    imgM.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                    continue;
                }
            }
            imgM.appendReplacement(sb, Matcher.quoteReplacement(tag));
        }
        imgM.appendTail(sb);
        s = sb.toString();
        if (kf8Mode && spineIndexMap != null && !spineIndexMap.isEmpty()) {
            Matcher aM = Pattern.compile("<a\\b[^>]*>", Pattern.CASE_INSENSITIVE).matcher(s);
            StringBuffer aSb = new StringBuffer();
            while (aM.find()) {
                String tag = aM.group(0);
                String href = extractAttr(tag, "href");
                if (href != null && !href.isEmpty()) {
                    String lowerH = href.toLowerCase(Locale.US);
                    if (!lowerH.startsWith("http://") && !lowerH.startsWith("https://") && !lowerH.startsWith("mailto:") && !lowerH.startsWith("kindle:")) {
                        String filePart = href;
                        String fragId = "";
                        int hIdx = filePart.indexOf('#');
                        if (hIdx >= 0) {
                            fragId = filePart.substring(hIdx + 1);
                            filePart = filePart.substring(0, hIdx);
                        }
                        Integer fidx = null;
                        int offBytes = 0;
                        if (filePart.isEmpty() && !fragId.isEmpty() && currentSpineIdx >= 0) {
                            fidx = Integer.valueOf(currentSpineIdx);
                            offBytes = findAnchorByteOffset(s, fragId);
                        } else if (!filePart.isEmpty()) {
                            String resolved = resolveRelPath(htmlDir, filePart);
                            fidx = spineIndexMap.get(resolved);
                            if (fidx == null) fidx = spineIndexMap.get(resolved.toLowerCase(Locale.US));
                            if (fidx == null) fidx = spineIndexMap.get(getBaseName(filePart));
                            if (fidx == null) fidx = spineIndexMap.get(getBaseName(filePart).toLowerCase(Locale.US));
                            if (fidx != null && fidx.intValue() == currentSpineIdx && !fragId.isEmpty()) {
                                offBytes = findAnchorByteOffset(s, fragId);
                            }
                        }
                        if (fidx != null) {
                            String kf8Href = "kindle:pos:fid:" + toBase32Four(fidx.intValue()) + ":off:" + toBase32Ten(offBytes);
                            String replacedTag = tag.replaceFirst("(?i)href\\s*=\\s*(\"[^\"]*\"|'[^']*')", "href=\"" + kf8Href + "\"");
                            aM.appendReplacement(aSb, Matcher.quoteReplacement(replacedTag));
                            continue;
                        }
                    }
                }
                aM.appendReplacement(aSb, Matcher.quoteReplacement(tag));
            }
            aM.appendTail(aSb);
            s = aSb.toString();
        }
        if (!kf8Mode) {
            s = Pattern.compile("<table\\b(?![^>]*border=)", Pattern.CASE_INSENSITIVE).matcher(s).replaceAll("<table border=\"1\"");
        }
        return s.trim();
    }

    private static String normalizeOrderedLists(String html) {
        Matcher olM = Pattern.compile("<ol\\b[^>]*>(.*?)</ol>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(html);
        StringBuffer out = new StringBuffer();
        while (olM.find()) {
            String inner = olM.group(1);
            Matcher liM = Pattern.compile("<li\\b[^>]*>(.*?)</li>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(inner);
            StringBuilder listHtml = new StringBuilder("<div>");
            int idx = 1;
            while (liM.find()) {
                listHtml.append("<p>").append(idx++).append(". ").append(liM.group(1).trim()).append("</p>");
            }
            listHtml.append("</div>");
            olM.appendReplacement(out, Matcher.quoteReplacement(listHtml.toString()));
        }
        olM.appendTail(out);
        return out.toString();
    }

    private static void addExth(List<int[]> types, List<byte[]> payloads, int type, byte[] data) {
        types.add(new int[]{type});
        payloads.add(data);
    }

    private static boolean isImagePath(String p) {
        String l = p.toLowerCase(Locale.US);
        return l.endsWith(".jpg") || l.endsWith(".jpeg") || l.endsWith(".png") || l.endsWith(".gif");
    }

    private static String normalizeZipPath(String p) {
        if (p == null) return "";
        p = p.replace('\\', '/');
        while (p.startsWith("/")) p = p.substring(1);
        return p;
    }

    private static String getDirName(String p) {
        if (p == null) return "";
        int idx = p.lastIndexOf('/');
        return (idx >= 0) ? p.substring(0, idx) : "";
    }

    private static String getBaseName(String p) {
        if (p == null) return "";
        int idx = p.lastIndexOf('/');
        return (idx >= 0) ? p.substring(idx + 1) : p;
    }

    private static String resolveRelPath(String baseDir, String rel) {
        rel = rel.replace("%20", " ");
        String combined = (baseDir == null || baseDir.isEmpty()) ? rel : (baseDir + "/" + rel);
        String[] parts = combined.replace('\\', '/').split("/");
        List<String> stack = new ArrayList<String>();
        for (String part : parts) {
            if (part.isEmpty() || ".".equals(part)) continue;
            if ("..".equals(part)) { if (!stack.isEmpty()) stack.remove(stack.size() - 1); }
            else stack.add(part);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stack.size(); i++) {
            if (i > 0) sb.append('/');
            sb.append(stack.get(i));
        }
        return sb.toString();
    }

    private static String extractAttr(String tagAttrs, String attrName) {
        Matcher m = Pattern.compile("\\b" + attrName + "\\s*=\\s*[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(tagAttrs);
        return m.find() ? m.group(1) : null;
    }

    private static String extractXmlTag(String xml, String openPrefix, String closeTag, String defaultVal) {
        String localName = openPrefix.replace("<dc:", "").replace("<", "").trim();
        Matcher m = Pattern.compile("<(?:[a-zA-Z0-9_-]+:)?" + localName + "\\b[^>]*>(.*?)</(?:[a-zA-Z0-9_-]+:)?" + localName + ">", Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(xml);
        if (m.find()) {
            String val = m.group(1).replaceAll("<[^>]+>", "").trim();
            if (!val.isEmpty()) return val;
        }
        return defaultVal;
    }

    private static String unescapeXml(String s) {
        if (s == null) return "";
        return s.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'").trim();
    }

    private static int indexOfIgnoreCase(String s, String sub) { return indexOfIgnoreCase(s, sub, 0); }
    private static int indexOfIgnoreCase(String s, String sub, int from) { return s.toLowerCase(Locale.US).indexOf(sub.toLowerCase(Locale.US), from); }

    private static void writeIntBE(ByteArrayOutputStream out, int v) {
        out.write((v >> 24) & 0xFF); out.write((v >> 16) & 0xFF); out.write((v >> 8) & 0xFF); out.write(v & 0xFF);
    }
    private static void writeShortBE(ByteArrayOutputStream out, int v) {
        out.write((v >> 8) & 0xFF); out.write(v & 0xFF);
    }
    private static void putIntBE(byte[] b, int off, int val) {
        b[off] = (byte) (val >> 24); b[off + 1] = (byte) (val >> 16); b[off + 2] = (byte) (val >> 8); b[off + 3] = (byte) val;
    }
    private static void putShortBE(byte[] b, int off, int val) {
        b[off] = (byte) (val >> 8); b[off + 1] = (byte) val;
    }
}
