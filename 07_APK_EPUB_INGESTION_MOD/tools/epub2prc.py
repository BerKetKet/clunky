#!/usr/bin/env python3
"""
Full-featured EPUB-to-PalmDB-MOBI (BOOKMOBI .prc) container compiler targeting
libKRF.so's EBookPDB::get_book_info (0x3b7f10) and Record 0 / EXTH parser (0x3b9128).
Supports:
- OPF metadata (<dc:title>, <dc:creator>, <dc:publisher>, <dc:language>, <dc:date>)
- Spine-ordered XHTML chapter concatenation with <mbp:pagebreak/>
- Embedded image extraction (JPEG/PNG/GIF) and <img src="..."/> -> <img recindex="00001"/> rewriting
- HTML table normalization (<table border="1">)
- Multi-record 4096-byte uncompressed UTF-8 text record slicing + FLIS/FCIS/EOF records
"""
import os
import re
import sys
import struct
import hashlib
import zipfile
import posixpath
import xml.etree.ElementTree as ET

def convert_epub_to_prc(epub_path, out_prc_path):
    with zipfile.ZipFile(epub_path, 'r') as zf:
        names = zf.namelist()
        opf_path = None
        if 'META-INF/container.xml' in names:
            try:
                c_root = ET.fromstring(zf.read('META-INF/container.xml'))
                for elem in c_root.iter():
                    if elem.tag.endswith('rootfile') and 'full-path' in elem.attrib:
                        opf_path = elem.attrib['full-path']
                        break
            except Exception:
                pass
        if not opf_path:
            for n in names:
                if n.lower().endswith('.opf'):
                    opf_path = n
                    break

        title = os.path.splitext(os.path.basename(epub_path))[0]
        author = "Local EPUB Author"
        publisher = "Kindle Local EPUB"
        lang = "en"
        pub_date = "2026-01-01"
        manifest = {}
        spine_ids = []
        opf_dir = posixpath.dirname(opf_path) if opf_path else ""

        if opf_path and opf_path in names:
            try:
                root = ET.fromstring(zf.read(opf_path))
                for elem in root.iter():
                    t = elem.tag.split('}')[-1]
                    txt = (elem.text or '').strip()
                    if t == 'title' and txt:
                        title = txt
                    elif t == 'creator' and txt:
                        author = txt
                    elif t == 'publisher' and txt:
                        publisher = txt
                    elif t == 'language' and txt:
                        lang = txt
                    elif t == 'date' and txt:
                        pub_date = txt[:10]
                    elif t == 'item' and 'id' in elem.attrib and 'href' in elem.attrib:
                        href = posixpath.normpath(posixpath.join(opf_dir, elem.attrib['href'])) if opf_dir else elem.attrib['href']
                        manifest[elem.attrib['id']] = (href, elem.attrib.get('media-type', ''))
                    elif t == 'itemref' and 'idref' in elem.attrib:
                        spine_ids.append(elem.attrib['idref'])
            except Exception:
                pass

        # Collect ordered HTML files and image files
        html_files = []
        for sid in spine_ids:
            if sid in manifest and manifest[sid][0] in names:
                html_files.append(manifest[sid][0])
        if not html_files:
            html_files = sorted([n for n in names if n.lower().endswith(('.xhtml', '.html', '.htm'))])

        image_files = []
        image_rec_map = {} # norm_zip_path -> 1-based image recindex
        for n in names:
            if n.lower().endswith(('.jpg', '.jpeg', '.png', '.gif')):
                image_files.append(n)
                image_rec_map[n] = len(image_files)
                image_rec_map[os.path.basename(n)] = len(image_files)

        body_parts = []
        for hf in html_files:
            raw_html = zf.read(hf).decode('utf-8', errors='replace')
            h_dir = posixpath.dirname(hf)
            # Extract <body>...</body>
            m = re.search(r'<body[^>]*>(.*)</body>', raw_html, flags=re.DOTALL | re.IGNORECASE)
            chunk = m.group(1) if m else raw_html
            chunk = re.sub(r'<(style|script)[^>]*>.*?</\1>', '', chunk, flags=re.DOTALL | re.IGNORECASE)
            # Rewrite <img ... src="..." ...> to <img recindex="00001"/>
            def repl_img(match):
                tag = match.group(0)
                sm = re.search(r'src=["\']([^"\']+)["\']', tag, flags=re.IGNORECASE)
                if not sm:
                    return tag
                src = sm.group(1).split('#')[0].split('?')[0]
                norm = posixpath.normpath(posixpath.join(h_dir, src)) if h_dir else src
                ridx = image_rec_map.get(norm) or image_rec_map.get(os.path.basename(src))
                if ridx:
                    return f'<img recindex="{ridx:05d}"/>'
                return tag
            chunk = re.sub(r'<img\b[^>]*>', repl_img, chunk, flags=re.IGNORECASE)
            chunk = re.sub(r'<table\b(?![^>]*border=)', '<table border="1"', chunk, flags=re.IGNORECASE)
            body_parts.append(chunk.strip())

        full_html = "<html><head></head><body>" + "<mbp:pagebreak/>".join(body_parts) + "</body></html>"
        html_bytes = full_html.encode('utf-8')

        # Slice html_bytes into <= 4096-byte records without splitting UTF-8 multi-byte sequences
        text_records = []
        pos = 0
        total_len = len(html_bytes)
        while pos < total_len:
            end = min(pos + 4096, total_len)
            while end < total_len and end > pos and (html_bytes[end] & 0xC0) == 0x80:
                end -= 1
            text_records.append(html_bytes[pos:end])
            pos = end
        if not text_records:
            text_records.append(b"<html><head></head><body></body></html>")

        image_records = [zf.read(imf) for imf in image_files]

    num_text = len(text_records)
    num_imgs = len(image_records)
    first_img_idx = (num_text + 1) if num_imgs > 0 else 0xFFFFFFFF
    first_non_book_idx = num_text + 1
    flis_idx = num_text + num_imgs + 1
    fcis_idx = num_text + num_imgs + 2
    eof_idx = num_text + num_imgs + 3

    # Build EXTH Header
    asin = "B0" + hashlib.md5(os.path.basename(epub_path).encode('utf-8')).hexdigest()[:8].upper()
    exth_recs = [
        (100, author.encode('utf-8')),
        (101, publisher.encode('utf-8')),
        (106, pub_date.encode('utf-8')),
        (113, asin.encode('utf-8')),
        (501, b"EBOK"),
        (503, title.encode('utf-8')),
        (524, lang.encode('utf-8')),
    ]
    if num_imgs > 0:
        exth_recs.append((201, struct.pack('>I', 0)))
        exth_recs.append((202, struct.pack('>I', 0)))
        exth_recs.append((203, struct.pack('>I', 0)))

    exth_body = bytearray()
    for rtype, rdata in exth_recs:
        exth_body.extend(struct.pack('>II', rtype, 8 + len(rdata)))
        exth_body.extend(rdata)
    while (12 + len(exth_body)) % 4 != 0:
        exth_body.append(0)
    exth_header = struct.pack('>4sII', b'EXTH', 12 + len(exth_body), len(exth_recs)) + bytes(exth_body)

    title_bytes = title.encode('utf-8')
    full_name_off = 16 + 232 + len(exth_header)
    full_name_len = len(title_bytes)

    palmdoc = struct.pack('>HHIHHHH', 1, 0, sum(len(r) for r in text_records), num_text, 4096, 0, 0)
    mobi = bytearray(232)
    mobi[0:4] = b'MOBI'
    struct.pack_into('>IIIII', mobi, 4, 232, 2, 65001, 0x12345678, 6)
    for off in range(24, 64, 4):
        struct.pack_into('>I', mobi, off, 0xFFFFFFFF)
    struct.pack_into('>IIIIII', mobi, 64, first_non_book_idx, full_name_off, full_name_len, 9, 0, 0)
    struct.pack_into('>II', mobi, 88, 6, first_img_idx)
    struct.pack_into('>I', mobi, 112, 0x50)
    for off in range(148, 176, 4):
        struct.pack_into('>I', mobi, off, 0xFFFFFFFF)
    struct.pack_into('>HHI', mobi, 176, 1, num_text, 1)
    struct.pack_into('>IIII', mobi, 184, fcis_idx, 1, flis_idx, 1)

    rec0 = bytearray(palmdoc + bytes(mobi) + exth_header + title_bytes + b'\x00\x00')
    while len(rec0) % 4 != 0:
        rec0.append(0)

    flis_rec = b'FLIS\x00\x00\x00\x08\x00\x41\x00\x00\x00\x00\x00\x00\xff\xff\xff\xff\x00\x01\x00\x03\x00\x00\x00\x03\x00\x00\x00\x01\xff\xff\xff\xff'
    fcis_rec = struct.pack('>4sIIIIIIIIIIHHI', b'FCIS', 20, 16, 1, 0, sum(len(r) for r in text_records), 0, 32, 8, 1, 1, 0, 0, 0)
    eof_rec = b'\xe9\x8e\r\n'

    all_records = [bytes(rec0)] + text_records + image_records + [flis_rec, fcis_rec, eof_rec]
    num_records = len(all_records)

    pdb_header = bytearray(78)
    safe_name = re.sub(r'[^A-Za-z0-9_-]', '_', title)[:31].encode('ascii', errors='replace')
    pdb_header[0:len(safe_name)] = safe_name
    pdb_header[60:68] = b'BOOKMOBI'
    struct.pack_into('>H', pdb_header, 76, num_records)

    rec_list = bytearray()
    cur_off = 78 + 8 * num_records + 2
    for idx, rbytes in enumerate(all_records):
        rec_list.extend(struct.pack('>II', cur_off, idx * 2))
        cur_off += len(rbytes)

    with open(out_prc_path, 'wb') as f:
        f.write(pdb_header)
        f.write(rec_list)
        f.write(b'\x00\x00')
        for rbytes in all_records:
            f.write(rbytes)

    return {
        "title": title,
        "author": author,
        "asin": asin,
        "num_text_records": num_text,
        "num_image_records": num_imgs,
        "total_bytes": cur_off,
    }

if __name__ == '__main__':
    if len(sys.argv) >= 3:
        info = convert_epub_to_prc(sys.argv[1], sys.argv[2])
        print(f"Converted {sys.argv[1]} -> {sys.argv[2]}: {info}")
