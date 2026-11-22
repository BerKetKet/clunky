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
        <dc:identifier id="pub-id">urn:uuid:folio-r31-verification-001</dc:identifier>
        <dc:title>YellowJersey Formatting &amp; Pop-up Footnotes</dc:title>
        <dc:creator>Folio Architecture Team</dc:creator>
        <dc:language>en</dc:language>
        <meta property="dcterms:modified">2026-11-22T10:00:00Z</meta>
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
        <li><a href="chapter1.xhtml">YellowJersey Verification</a></li>
    </ol>
</nav>
</body>
</html>
"""

    style_css = """
body {
    font-family: serif;
    margin: 5%;
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
    margin: 1.5em 0;
}
aside[epub\\:type~="footnote"] {
    font-size: 0.9em;
    color: #444;
}
"""

    chapter1_xhtml = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops">
<head>
    <title>YellowJersey Verification</title>
    <link rel="stylesheet" type="text/css" href="style.css"/>
</head>
<body>
    <h1>YellowJersey Verification</h1>
    
    <p>Folio v2.8 KFX introduces authentic Amazon YellowJersey floating pop-up footnotes<a href="#fn1" epub:type="noteref" class="noteref" id="ref1"><sup>[1]</sup></a> that render cleanly at the bottom of the reading pane without soft keyboard interruption.</p>
    
    <p>Enhanced Typesetting supports bidirectional footnote linking<a href="#fn2" epub:type="noteref" class="noteref" id="ref2"><sup>[2]</sup></a> with full preview card navigation.</p>

    <hr/>

    <h2>Rich Inline Typography</h2>
    <p>This paragraph demonstrates <sup>superscript formula</sup>, <sub>subscript chemical (H<sub>2</sub>O)</sub>, <u>underlined prose</u>, and <s>strikethrough text</s> rendered natively in YellowJersey KFX.</p>

    <hr/>

    <h2>Preformatted Code Block</h2>
    <p>Below is a preformatted Python code block with preserved indentation and monospace font:</p>
<pre><code>def calculate_engine_metrics(books, duration):
    # Preserves indentation and newlines verbatim
    rate = books / duration
    print(f"Conversion rate: {rate:.2f} books/sec")
    return rate

result = calculate_engine_metrics(7, 1.45)
print(f"Engine performance: {result}")</code></pre>

    <hr/>

    <h2>Notes &amp; References</h2>
    <aside id="fn1" epub:type="footnote">
        <p><strong>[1]</strong> Native pop-up footnotes in KFX utilize Amazon's YellowJersey floating preview card at the bottom of the reading pane with full body text, "Go to Footnotes" link, and tap-outside dismiss.</p>
    </aside>
    <aside id="fn2" epub:type="footnote">
        <p><strong>[2]</strong> Bidirectional anchors link the reference span ($179/$616:$617) to the classified footnote story entry ($615:$281).</p>
    </aside>
</body>
</html>
"""

    with zipfile.ZipFile(output_path, 'w') as zf:
        # mimetype must be first and uncompressed
        zf.writestr("mimetype", mimetype, compress_type=zipfile.ZIP_STORED)
        zf.writestr("META-INF/container.xml", container_xml, compress_type=zipfile.ZIP_DEFLATED)
        zf.writestr("OEBPS/content.opf", content_opf, compress_type=zipfile.ZIP_DEFLATED)
        zf.writestr("OEBPS/nav.xhtml", nav_xhtml, compress_type=zipfile.ZIP_DEFLATED)
        zf.writestr("OEBPS/style.css", style_css, compress_type=zipfile.ZIP_DEFLATED)
        zf.writestr("OEBPS/chapter1.xhtml", chapter1_xhtml, compress_type=zipfile.ZIP_DEFLATED)

    print(f"Generated EPUB at {output_path} ({os.path.getsize(output_path)} bytes)")

if __name__ == "__main__":
    create_epub("logs/r31_rich_formatting.epub")
