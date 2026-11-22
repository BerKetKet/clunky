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
        <dc:identifier id="pub-id">urn:uuid:folio-r34-verification-001</dc:identifier>
        <dc:title>Folio v3.1 Nested Lists &amp; Table Spans</dc:title>
        <dc:creator>Folio Architecture Team</dc:creator>
        <dc:language>en</dc:language>
        <meta property="dcterms:modified">2026-11-22T14:00:00Z</meta>
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
        <li><a href="chapter1.xhtml">Folio v3.1 Verification</a></li>
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
    font-size: 1.5em;
    margin-bottom: 0.5em;
}
h2 {
    font-size: 1.2em;
    margin-top: 1em;
    margin-bottom: 0.4em;
    border-bottom: 1px solid #ccc;
}
p {
    margin: 0.5em 0;
    font-size: 1.0em;
}
table {
    width: 100%;
    border-collapse: collapse;
    margin: 1em 0;
}
th, td {
    border: 1px solid #888;
    padding: 0.4em 0.6em;
    text-align: left;
}
th {
    background-color: #f0f0f0;
    font-weight: bold;
    text-align: center;
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
    <title>Folio v3.1 Verification</title>
    <link rel="stylesheet" type="text/css" href="style.css"/>
</head>
<body>
    <h1>Folio v3.1 KFX Verification</h1>
    
    <h2>Multi-Level Nested Lists</h2>
    <ul>
        <li>Classical Philosophy
            <ul>
                <li>Pre-Socratics (Ionian &amp; Eleatic)
                    <ul>
                        <li>Heraclitus (Doctrine of Flux &amp; Logos)</li>
                        <li>Parmenides (The Unchanging One)</li>
                    </ul>
                </li>
                <li>Athenian School (Golden Age)
                    <ul>
                        <li>Socrates (Dialectic inquiry &amp; Elenchus)</li>
                        <li>Plato (Theory of Forms &amp; Republic)</li>
                        <li>Aristotle (Hylomorphism &amp; Organon)</li>
                    </ul>
                </li>
            </ul>
        </li>
        <li>Hellenistic &amp; Roman Thought
            <ul>
                <li>Stoicism
                    <ul>
                        <li>Zeno of Citium (Stoa Poikile)</li>
                        <li>Marcus Aurelius (Meditations)</li>
                    </ul>
                </li>
            </ul>
        </li>
    </ul>

    <hr/>

    <h2>Native KFX Table with Thead &amp; Spans</h2>
    <table>
        <thead>
            <tr>
                <th>Tradition</th>
                <th colspan="2">Temporal Span (BCE)</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td rowspan="2">Hellenic</td>
                <td>Early Archaic Epoch</td>
                <td>c. 800–600</td>
            </tr>
            <tr>
                <td>Classical Era</td>
                <td>c. 500–323</td>
            </tr>
            <tr>
                <td>Hellenistic</td>
                <td>Alexandrian Period</td>
                <td>c. 323–31</td>
            </tr>
        </tbody>
    </table>

    <hr/>

    <h2>Ordered Sub-Lists</h2>
    <ol>
        <li>Foundational Axioms
            <ol>
                <li>Principle of Non-Contradiction</li>
                <li>Law of the Excluded Middle</li>
            </ol>
        </li>
    </ol>
</body>
</html>
"""

    with zipfile.ZipFile(output_path, 'w') as zf:
        zf.writestr('mimetype', mimetype, compress_type=zipfile.ZIP_STORED)
        zf.writestr('META-INF/container.xml', container_xml.encode('utf-8'), compress_type=zipfile.ZIP_DEFLATED)
        zf.writestr('OEBPS/content.opf', content_opf.encode('utf-8'), compress_type=zipfile.ZIP_DEFLATED)
        zf.writestr('OEBPS/nav.xhtml', nav_xhtml.encode('utf-8'), compress_type=zipfile.ZIP_DEFLATED)
        zf.writestr('OEBPS/style.css', style_css.encode('utf-8'), compress_type=zipfile.ZIP_DEFLATED)
        zf.writestr('OEBPS/chapter1.xhtml', chapter1_xhtml.encode('utf-8'), compress_type=zipfile.ZIP_DEFLATED)

    print(f"Created EPUB: {output_path} ({os.path.getsize(output_path)} bytes)")

if __name__ == '__main__':
    create_epub("logs/r34_nested_lists_tables.epub")
