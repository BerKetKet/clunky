#!/usr/bin/env python3
import os
import zipfile

def create_epub(output_path):
    os.makedirs(os.path.dirname(os.path.abspath(output_path)), exist_ok=True)
    
    mimetype = b"application/epub+zip"
    
    container_xml = """<?xml version="1.0" encoding="UTF-8"?>
<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
    <rootfiles>
        <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
    </rootfiles>
</container>
"""

    content_opf = """<?xml version="1.0" encoding="UTF-8"?>
<package xmlns="http://www.idpf.org/2007/opf" unique-identifier="pub-id" version="3.0">
    <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
        <dc:identifier id="pub-id">urn:uuid:folio-r35-verification-001</dc:identifier>
        <dc:title>Folio v3.2 Text-Transform &amp; RTL</dc:title>
        <dc:creator>Folio Architecture Team</dc:creator>
        <dc:language>mul</dc:language>
        <meta property="dcterms:modified">2026-11-22T15:00:00Z</meta>
    </metadata>
    <manifest>
        <item id="nav" href="nav.xhtml" media-type="application/xhtml+xml" properties="nav"/>
        <item id="chapter1" href="chapter1.xhtml" media-type="application/xhtml+xml"/>
        <item id="style" href="style.css" media-type="text/css"/>
    </manifest>
    <spine>
        <itemref idref="chapter1"/>
    </spine>
</package>
"""

    nav_xhtml = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops">
<head><title>Navigation</title></head>
<body>
<nav epub:type="toc" id="toc">
    <h1>Table of Contents</h1>
    <ol>
        <li><a href="chapter1.xhtml">Folio v3.2 Typography &amp; RTL</a></li>
    </ol>
</nav>
</body>
</html>
"""

    style_css = """
body {
    font-family: serif;
    margin: 4%;
    line-height: 1.4;
}
h1 {
    font-size: 1.45em;
    font-weight: bold;
    margin-top: 0.8em;
    margin-bottom: 0.4em;
    color: #111827;
}
h2 {
    font-size: 1.25em;
    font-weight: bold;
    margin-top: 0.7em;
    margin-bottom: 0.3em;
    color: #1F2937;
}
.uppercase {
    text-transform: uppercase;
}
.lowercase {
    text-transform: lowercase;
}
.capitalize {
    text-transform: capitalize;
}
.small-caps {
    font-variant: small-caps;
}
.rtl-block {
    direction: rtl;
    text-align: right;
}
"""

    chapter1_xhtml = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Folio v3.2 Typography &amp; RTL</title>
    <link rel="stylesheet" type="text/css" href="style.css"/>
</head>
<body>
    <h1>Folio v3.2 Typography &amp; RTL</h1>
    
    <h2>1. CSS Text-Transform</h2>
    <p class="uppercase">This whole paragraph is transformed to uppercase via css class.</p>
    <p class="capitalize">every word in this title-cased sentence begins with an uppercase letter.</p>
    <p>Inline styling: <span style="text-transform: uppercase;">uppercase inline span</span> and <span style="text-transform: capitalize;">capitalized inline span</span> render cleanly.</p>

    <h2>2. Small-Caps Presentation</h2>
    <p class="small-caps">Small-caps typography provides balanced visual weight for leads and acronyms like <abbr>unesco</abbr> and <abbr>html</abbr>.</p>

    <h2>3. Right-to-Left (RTL) Multilingual</h2>
    <div dir="rtl" class="rtl-block">
        <h2 dir="rtl">اللغة العربية (Arabic)</h2>
        <p dir="rtl">مرحبا بكم في قارئ فوليو. هذا نص تجريبي باللغة العربية مع اتجاه كتابة من اليمين إلى اليسار وتنسيق متقدم في كيندل.</p>
        
        <h2 dir="rtl">עברית (Hebrew)</h2>
        <p dir="rtl">ברוכים הבאים לקורא פוליו. טקסט זה כתוב בעברית ומוצג מימין לשמאל בדיוק רב עם תמיכה מלאה בטיפוגרפיה.</p>
    </div>
</body>
</html>
"""

    with zipfile.ZipFile(output_path, "w") as zf:
        zf.writestr("mimetype", mimetype, compress_type=zipfile.ZIP_STORED)
        zf.writestr("META-INF/container.xml", container_xml, compress_type=zipfile.ZIP_DEFLATED)
        zf.writestr("OEBPS/content.opf", content_opf, compress_type=zipfile.ZIP_DEFLATED)
        zf.writestr("OEBPS/nav.xhtml", nav_xhtml, compress_type=zipfile.ZIP_DEFLATED)
        zf.writestr("OEBPS/style.css", style_css, compress_type=zipfile.ZIP_DEFLATED)
        zf.writestr("OEBPS/chapter1.xhtml", chapter1_xhtml, compress_type=zipfile.ZIP_DEFLATED)

    print(f"Created EPUB: {output_path} ({os.path.getsize(output_path)} bytes)")

if __name__ == "__main__":
    create_epub("logs/r35_text_transform_rtl.epub")
