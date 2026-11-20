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
 * Full-featured on-device EPUB -> PalmDB BOOKMOBI (.prc) transcoder for Kindle Android v8.159.0.100.
 * Features:
 * - Spine-ordered OPF parsing (container.xml -> .opf -> manifest + spine + metadata + cover)
 * - Embedded image extraction with PNG->JPEG conversion & MOBI7 recindex="00001" rewriting
 * - Cover art embedding (EXTH 201/202/203) so real covers show in the Kindle Library grid
 * - Correct last_content_record = num_text + num_images so libKRF.so renders all image records
 * - Clean HTML normalization (<ol> 65535-bug fix, <table border="1">, style/script stripping)
 */
public final class EpubTranscoder {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String CACHE_PREFIX = "v4_";

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
        if (path == null) {
            return null;
        }
        return ensureKindleFile(new File(path)).getAbsolutePath();
    }

    public static File ensureKindleFile(File file) {
        if (!isEpubFile(file)) {
            return file;
        }
        try {
            File cacheDir = getCacheDir();
            File outFile = new File(cacheDir, CACHE_PREFIX + file.getName() + ".prc");
            if (!outFile.exists() || outFile.length() < 512 || outFile.lastModified() < file.lastModified()) {
                transcodeToPrc(file, outFile);
            }
            if (outFile.exists() && outFile.length() >= 512) {
                return outFile;
            }
        } catch (Throwable ignored) {
        }
        return file;
    }

    public static boolean isEpubFile(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return false;
        }
        if (file.getName().toLowerCase(Locale.US).endsWith(".epub")) {
            return true;
        }
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
        if (metadata == null || !isEpubFile(origFile)) {
            return;
        }
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

    public static void transcodeToPrc(File inFile, File outFile) throws Exception {
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

        // 1. Locate OPF path from META-INF/container.xml or fallback scan
        String opfPath = null;
        byte[] containerBytes = zipEntries.get("META-INF/container.xml");
        if (containerBytes != null) {
            String cxml = new String(containerBytes, UTF8);
            Matcher m = Pattern.compile("full-path=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(cxml);
            if (m.find()) {
                opfPath = normalizeZipPath(m.group(1));
            }
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
            if (rawDate.length() >= 10) {
                pubDate = rawDate.substring(0, 10);
            }

            // Cover meta tag (EPUB2): <meta name="cover" content="cover-img"/>
            Matcher cm1 = Pattern.compile("<meta[^>]+name=[\"']cover[\"'][^>]+content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(opfXml);
            if (cm1.find()) {
                coverId = cm1.group(1);
            } else {
                Matcher cm2 = Pattern.compile("<meta[^>]+content=[\"']([^\"']+)[\"'][^>]+name=[\"']cover[\"']", Pattern.CASE_INSENSITIVE).matcher(opfXml);
                if (cm2.find()) {
                    coverId = cm2.group(1);
                }
            }

            // Parse <item ...> tags in manifest
            Matcher itemM = Pattern.compile("<item\\b([^>]+)>", Pattern.CASE_INSENSITIVE).matcher(opfXml);
            while (itemM.find()) {
                String attrs = itemM.group(1);
                String id = extractAttr(attrs, "id");
                String href = extractAttr(attrs, "href");
                String props = extractAttr(attrs, "properties");
                if (id != null && href != null) {
                    String resolved = resolveRelPath(opfDir, href);
                    manifestIdToPath.put(id, resolved);
                    if (props != null && props.contains("cover-image")) {
                        coverHref = resolved;
                    }
                }
            }
            if (coverHref == null && coverId != null) {
                coverHref = manifestIdToPath.get(coverId);
            }

            // Parse <itemref idref="..."/> in spine order
            Matcher spineM = Pattern.compile("<itemref\\b[^>]+idref=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(opfXml);
            while (spineM.find()) {
                String p = manifestIdToPath.get(spineM.group(1));
                if (p != null && zipEntries.containsKey(p)) {
                    spinePaths.add(p);
                }
            }
        }

        // Fallback if spine was empty
        if (spinePaths.isEmpty()) {
            for (String k : zipEntries.keySet()) {
                String kl = k.toLowerCase(Locale.US);
                if (kl.endsWith(".xhtml") || kl.endsWith(".html") || kl.endsWith(".htm")) {
                    spinePaths.add(k);
                }
            }
            Collections.sort(spinePaths);
        }

        // 2. Collect images (place cover image first at recindex=1 if present)
        List<String> imagePaths = new ArrayList<String>();
        if (coverHref != null && zipEntries.containsKey(coverHref) && isImagePath(coverHref)) {
            imagePaths.add(coverHref);
        }
        for (String k : zipEntries.keySet()) {
            if (isImagePath(k) && !imagePaths.contains(k)) {
                imagePaths.add(k);
            }
        }
        // If no explicit cover was set, promote any filename containing "cover" to index 0
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
            byte[] rawImg = zipEntries.get(ip);
            byte[] mobiImg = ensureMobiCompatibleImage(ip, rawImg, i == 0);
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

        // 3. Build spine-ordered HTML body
        StringBuilder html = new StringBuilder(65536);
        html.append("<html><head></head><body>");
        for (int i = 0; i < spinePaths.size(); i++) {
            String hp = spinePaths.get(i);
            String rawHtml = new String(zipEntries.get(hp), UTF8);
            String body = extractAndNormalizeBody(rawHtml, getDirName(hp), imgRecMap);
            if (i > 0) {
                html.append("<mbp:pagebreak/>");
            }
            html.append(body);
        }
        html.append("</body></html>");
        byte[] htmlBytes = html.toString().getBytes(UTF8);

        // 4. Deterministic 10-char ASIN
        String asin = String.format(Locale.US, "B0%08X", Integer.valueOf(inFile.getName().hashCode()));
        byte[] prcBytes = buildPalmDbMobi(title, author, publisher, lang, pubDate, asin, htmlBytes, imageRecords, hasCover);

        File tmpOut = new File(outFile.getParentFile(), outFile.getName() + ".tmp");
        FileOutputStream fos = new FileOutputStream(tmpOut);
        try {
            fos.write(prcBytes);
        } finally {
            try { fos.close(); } catch (Throwable ignored) {}
        }
        if (outFile.exists()) {
            outFile.delete();
        }
        tmpOut.renameTo(outFile);
    }

    private static byte[] ensureMobiCompatibleImage(String path, byte[] raw, boolean isCover) {
        if (raw == null || raw.length < 4) return raw;
        boolean isJpeg = ((raw[0] & 0xFF) == 0xFF && (raw[1] & 0xFF) == 0xD8);
        if (isJpeg && (!isCover || raw.length <= 58000)) {
            return raw;
        }
        try {
            Bitmap bmp = BitmapFactory.decodeByteArray(raw, 0, raw.length);
            if (bmp != null) {
                int w = bmp.getWidth();
                int h = bmp.getHeight();
                if (isCover && (w > 450 || h > 600)) {
                    float scale = Math.min(450f / w, 600f / h);
                    int nw = Math.max(1, (int) (w * scale));
                    int nh = Math.max(1, (int) (h * scale));
                    Bitmap scaled = Bitmap.createScaledBitmap(bmp, nw, nh, true);
                    if (scaled != bmp) {
                        bmp.recycle();
                        bmp = scaled;
                    }
                    w = nw;
                    h = nh;
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
                if (jpg.length > 0) {
                    return jpg;
                }
            }
        } catch (Throwable ignored) {
        }
        return raw;
    }

    private static String extractAndNormalizeBody(String rawHtml, String htmlDir, Map<String, Integer> imgRecMap) {
        String s = rawHtml;
        int bIdx = indexOfIgnoreCase(s, "<body");
        if (bIdx >= 0) {
            int gt = s.indexOf('>', bIdx);
            if (gt >= 0) {
                int end = indexOfIgnoreCase(s, "</body>", gt + 1);
                s = (end >= 0) ? s.substring(gt + 1, end) : s.substring(gt + 1);
            }
        }
        // Strip <style> and <script>
        s = Pattern.compile("<(style|script)\\b[^>]*>.*?</\\1>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(s).replaceAll("");
        // Fix MOBI7 <ol> 65535 counter underflow bug: convert <ol>...</ol> into numbered paragraphs
        s = normalizeOrderedLists(s);
        // Rewrite <img ... src="..."> -> <img recindex="00001"/>
        Matcher imgM = Pattern.compile("<img\\b[^>]*>", Pattern.CASE_INSENSITIVE).matcher(s);
        StringBuffer sb = new StringBuffer();
        while (imgM.find()) {
            String tag = imgM.group(0);
            String src = extractAttr(tag, "src");
            if (src != null) {
                int q = src.indexOf('?');
                if (q >= 0) src = src.substring(0, q);
                int h = src.indexOf('#');
                if (h >= 0) src = src.substring(0, h);
                String resolved = resolveRelPath(htmlDir, src);
                Integer ridx = imgRecMap.get(resolved);
                if (ridx == null) ridx = imgRecMap.get(resolved.toLowerCase(Locale.US));
                if (ridx == null) ridx = imgRecMap.get(getBaseName(src));
                if (ridx == null) ridx = imgRecMap.get(getBaseName(src).toLowerCase(Locale.US));
                if (ridx != null) {
                    String replacement = String.format(Locale.US, "<img recindex=\"%05d\"/>", ridx);
                    imgM.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                    continue;
                }
            }
            imgM.appendReplacement(sb, Matcher.quoteReplacement(tag));
        }
        imgM.appendTail(sb);
        s = sb.toString();

        // Ensure tables have visible borders in MOBI7
        s = Pattern.compile("<table\\b(?![^>]*border=)", Pattern.CASE_INSENSITIVE).matcher(s).replaceAll("<table border=\"1\"");
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

    private static byte[] buildPalmDbMobi(
            String title,
            String author,
            String publisher,
            String lang,
            String pubDate,
            String asin,
            byte[] htmlBytes,
            List<byte[]> imageRecords,
            boolean hasCover
    ) throws Exception {
        // 1. Slice HTML into <= 4096-byte UTF-8 records without splitting multibyte chars
        List<byte[]> textRecords = new ArrayList<byte[]>();
        int pos = 0;
        int totalLen = htmlBytes.length;
        while (pos < totalLen) {
            int end = Math.min(pos + 4096, totalLen);
            while (end < totalLen && end > pos && (htmlBytes[end] & 0xC0) == 0x80) {
                end--;
            }
            if (end == pos) {
                end = Math.min(pos + 4096, totalLen);
            }
            byte[] chunk = new byte[end - pos];
            System.arraycopy(htmlBytes, pos, chunk, 0, chunk.length);
            textRecords.add(chunk);
            pos = end;
        }
        if (textRecords.isEmpty()) {
            textRecords.add("<html><head></head><body></body></html>".getBytes(UTF8));
        }

        int numText = textRecords.size();
        int numImgs = imageRecords.size();
        int firstNonBookIdx = numText + 1;
        int firstImgIdx = (numImgs > 0) ? (numText + 1) : -1;
        int lastContentRec = numText + numImgs; // CRITICAL: includes image records so libKRF.so renders <img recindex="..."/>!
        int flisIdx = numText + numImgs + 1;
        int fcisIdx = numText + numImgs + 2;

        // 2. Build EXTH header
        List<int[]> exthTypes = new ArrayList<int[]>();
        List<byte[]> exthPayloads = new ArrayList<byte[]>();
        addExth(exthTypes, exthPayloads, 100, author.getBytes(UTF8));
        addExth(exthTypes, exthPayloads, 101, publisher.getBytes(UTF8));
        addExth(exthTypes, exthPayloads, 106, pubDate.getBytes(UTF8));
        addExth(exthTypes, exthPayloads, 113, asin.getBytes(UTF8));
        addExth(exthTypes, exthPayloads, 501, "EBOK".getBytes(UTF8));
        addExth(exthTypes, exthPayloads, 503, title.getBytes(UTF8));
        addExth(exthTypes, exthPayloads, 524, lang.getBytes(UTF8));
        if (numImgs > 0) {
            byte[] zero4 = new byte[]{0, 0, 0, 0};
            addExth(exthTypes, exthPayloads, 201, zero4); // CoverOffset = 0
            addExth(exthTypes, exthPayloads, 202, zero4); // ThumbOffset = 0
            addExth(exthTypes, exthPayloads, 203, zero4); // HasFakeCover = 0
        }

        ByteArrayOutputStream exthBody = new ByteArrayOutputStream();
        for (int i = 0; i < exthTypes.size(); i++) {
            int rtype = exthTypes.get(i)[0];
            byte[] data = exthPayloads.get(i);
            writeIntBE(exthBody, rtype);
            writeIntBE(exthBody, 8 + data.length);
            exthBody.write(data);
        }
        int exthTotalLen = 12 + exthBody.size();
        while (exthTotalLen % 4 != 0) {
            exthBody.write(0);
            exthTotalLen++;
        }
        ByteArrayOutputStream exthOut = new ByteArrayOutputStream();
        exthOut.write("EXTH".getBytes(UTF8));
        writeIntBE(exthOut, exthTotalLen);
        writeIntBE(exthOut, exthTypes.size());
        exthOut.write(exthBody.toByteArray());
        byte[] exthBytes = exthOut.toByteArray();

        // 3. Build Record 0 (16B PalmDOC + 232B MOBI + EXTH + FullName + pad)
        byte[] titleBytes = title.getBytes(UTF8);
        int fullNameOff = 16 + 232 + exthBytes.length;
        int rec0Len = fullNameOff + titleBytes.length + 2;
        while (rec0Len % 4 != 0) rec0Len++;
        byte[] rec0 = new byte[rec0Len];

        // PalmDOC (16B)
        putShortBE(rec0, 0, 1); // uncompressed
        putIntBE(rec0, 4, totalLen);
        putShortBE(rec0, 8, numText);
        putShortBE(rec0, 10, 4096);

        // MOBI (232B at offset 16)
        putIntBE(rec0, 16, 0x4D4F4249); // "MOBI"
        putIntBE(rec0, 20, 232);
        putIntBE(rec0, 24, 2);
        putIntBE(rec0, 28, 65001);
        putIntBE(rec0, 32, 0x12345678);
        putIntBE(rec0, 36, 6);
        for (int off = 40; off < 80; off += 4) {
            putIntBE(rec0, off, -1);
        }
        putIntBE(rec0, 80, firstNonBookIdx);
        putIntBE(rec0, 84, fullNameOff);
        putIntBE(rec0, 88, titleBytes.length);
        putIntBE(rec0, 92, 9);
        putIntBE(rec0, 104, 6);
        putIntBE(rec0, 108, firstImgIdx);
        putIntBE(rec0, 128, 0x50);
        for (int off = 164; off < 192; off += 4) {
            putIntBE(rec0, off, -1);
        }
        putShortBE(rec0, 192, 1);
        putShortBE(rec0, 194, lastContentRec);
        putIntBE(rec0, 196, 1);
        putIntBE(rec0, 200, fcisIdx);
        putIntBE(rec0, 204, 1);
        putIntBE(rec0, 208, flisIdx);
        putIntBE(rec0, 212, 1);

        System.arraycopy(exthBytes, 0, rec0, 248, exthBytes.length);
        System.arraycopy(titleBytes, 0, rec0, fullNameOff, titleBytes.length);

        // 4. FLIS, FCIS, EOF records
        byte[] flisRec = new byte[]{
            'F','L','I','S', 0,0,0,8, 0,0x41,0,0, 0,0,0,0,
            (byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF, 0,1,0,3, 0,0,0,3, 0,0,0,1,
            (byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF
        };
        byte[] fcisRec = new byte[44];
        fcisRec[0]='F'; fcisRec[1]='C'; fcisRec[2]='I'; fcisRec[3]='S';
        putIntBE(fcisRec, 4, 20);
        putIntBE(fcisRec, 8, 16);
        putIntBE(fcisRec, 12, 1);
        putIntBE(fcisRec, 20, totalLen);
        putIntBE(fcisRec, 28, 32);
        putIntBE(fcisRec, 32, 8);
        putShortBE(fcisRec, 36, 1);
        putShortBE(fcisRec, 38, 1);
        byte[] eofRec = new byte[]{(byte)0xE9, (byte)0x8E, 0x0D, 0x0A};

        List<byte[]> allRecords = new ArrayList<byte[]>();
        allRecords.add(rec0);
        allRecords.addAll(textRecords);
        allRecords.addAll(imageRecords);
        allRecords.add(flisRec);
        allRecords.add(fcisRec);
        allRecords.add(eofRec);

        int numRecords = allRecords.size();
        byte[] pdbHeader = new byte[78];
        String safeTitle = title.replaceAll("[^A-Za-z0-9_-]", "_");
        if (safeTitle.length() > 31) safeTitle = safeTitle.substring(0, 31);
        byte[] safeBytes = safeTitle.getBytes(UTF8);
        System.arraycopy(safeBytes, 0, pdbHeader, 0, Math.min(31, safeBytes.length));
        putIntBE(pdbHeader, 60, 0x424F4F4B); // "BOOK"
        putIntBE(pdbHeader, 64, 0x4D4F4249); // "MOBI"
        putShortBE(pdbHeader, 76, numRecords);

        ByteArrayOutputStream out = new ByteArrayOutputStream(totalLen + 16384);
        out.write(pdbHeader);
        int curOff = 78 + 8 * numRecords + 2;
        for (int i = 0; i < numRecords; i++) {
            writeIntBE(out, curOff);
            writeIntBE(out, i * 2);
            curOff += allRecords.get(i).length;
        }
        out.write(0);
        out.write(0);
        for (byte[] rec : allRecords) {
            out.write(rec);
        }
        return out.toByteArray();
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
            if ("..".equals(part)) {
                if (!stack.isEmpty()) stack.remove(stack.size() - 1);
            } else {
                stack.add(part);
            }
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

    private static int indexOfIgnoreCase(String s, String sub) {
        return indexOfIgnoreCase(s, sub, 0);
    }

    private static int indexOfIgnoreCase(String s, String sub, int from) {
        return s.toLowerCase(Locale.US).indexOf(sub.toLowerCase(Locale.US), from);
    }

    private static void writeIntBE(ByteArrayOutputStream out, int v) {
        out.write((v >> 24) & 0xFF);
        out.write((v >> 16) & 0xFF);
        out.write((v >> 8) & 0xFF);
        out.write(v & 0xFF);
    }

    private static void putIntBE(byte[] b, int off, int val) {
        b[off] = (byte) (val >> 24);
        b[off + 1] = (byte) (val >> 16);
        b[off + 2] = (byte) (val >> 8);
        b[off + 3] = (byte) val;
    }

    private static void putShortBE(byte[] b, int off, int val) {
        b[off] = (byte) (val >> 8);
        b[off + 1] = (byte) val;
    }
}
