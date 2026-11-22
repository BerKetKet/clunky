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
        <dc:identifier id="pub-id">urn:uuid:folio-r32-verification-001</dc:identifier>
        <dc:title>Folio v2.9 Code Hyphenation &amp; Sup Sub Test</dc:title>
        <dc:creator>Folio Architecture Team</dc:creator>
        <dc:language>en</dc:language>
        <meta property="dcterms:modified">2026-11-22T11:45:00Z</meta>
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
        <li><a href="chapter1.xhtml">Folio v2.9 Verification</a></li>
    </ol>
</nav>
</body>
</html>
"""

    style_css = """
body {
    font-family: serif;
    margin: 4%;
}
pre {
    background-color: #f4f4f4;
    padding: 0.5em;
    border-left: 3px solid #ccc;
    font-family: monospace;
}
code {
    font-family: monospace;
}
hr {
    border: none;
    border-top: 1px solid #888;
    margin: 1.2em 0;
}
"""

    chapter1_xhtml = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops">
<head>
    <title>Folio v2.9 Verification</title>
    <link rel="stylesheet" type="text/css" href="style.css"/>
</head>
<body>
    <h1>Folio v2.9 Verification</h1>
    
    <h2>Scientific &amp; Mathematical Typography</h2>
    <p>Mass-energy equivalence formula: <em>E</em> = <em>mc</em><sup>2</sup> sitting cleanly on the baseline.</p>
    <p>Chemical reaction equation: 2H<sub>2</sub> + O<sub>2</sub> &rarr; 2H<sub>2</sub>O and atmospheric CO<sub>2</sub>.</p>
    <p>Pythagorean theorem in geometry: <em>a</em><sup>2</sup> + <em>b</em><sup>2</sup> = <em>c</em><sup>2</sup>.</p>
    <p>Clean proportional superscript: Gauss celebrated theorem<sup>[1]</sup> with proportional sizing and zero baseline collision.</p>

    <hr/>

    <h2>Zero-Hyphenation Code Block</h2>
    <p>Preformatted Python code block with long identifiers preserved verbatim without hyphenation:</p>
<pre><code>def calculate_engine_execution_duration(books_processed, total_duration_seconds):
    # Verbatim indentation and code formatting without hyphenation
    processing_throughput_rate = books_processed / total_duration_seconds
    print(f"Throughput rate: {processing_throughput_rate:.2f} books/sec")
    return processing_throughput_rate

measured_result = calculate_engine_execution_duration(14, 2.85)
print(f"Execution duration metrics verified verbatim: {measured_result}")</code></pre>

    <hr/>

    <p>Inline code identifiers like <code>calculate_engine_execution_duration</code> and <code>processing_throughput_rate</code> are also rendered with <code>hyphens: none</code>.</p>
</body>
</html>
"""

    with zipfile.ZipFile(output_path, 'w') as zf:
        zf.writestr("mimetype", mimetype, compress_type=zipfile.ZIP_STORED)
        zf.writestr("META-INF/container.xml", container_xml, compress_type=zipfile.ZIP_DEFLATED)
        zf.writestr("OEBPS/content.opf", content_opf, compress_type=zipfile.ZIP_DEFLATED)
        zf.writestr("OEBPS/nav.xhtml", nav_xhtml, compress_type=zipfile.ZIP_DEFLATED)
        zf.writestr("OEBPS/style.css", style_css, compress_type=zipfile.ZIP_DEFLATED)
        zf.writestr("OEBPS/chapter1.xhtml", chapter1_xhtml, compress_type=zipfile.ZIP_DEFLATED)

    print(f"Generated EPUB at {output_path} ({os.path.getsize(output_path)} bytes)")

if __name__ == "__main__":
    create_epub("logs/r32_code_sup_sub.epub")
