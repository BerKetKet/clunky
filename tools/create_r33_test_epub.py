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
        <dc:identifier id="pub-id">urn:uuid:folio-r33-verification-001</dc:identifier>
        <dc:title>Folio v3.0 HTML Entities &amp; Typography Test</dc:title>
        <dc:creator>Folio Architecture Team</dc:creator>
        <dc:language>en</dc:language>
        <meta property="dcterms:modified">2026-11-22T13:30:00Z</meta>
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
        <li><a href="chapter1.xhtml">Folio v3.0 Verification</a></li>
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
    margin: 0.6em 0;
    font-size: 1.05em;
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
    <title>Folio v3.0 Verification</title>
    <link rel="stylesheet" type="text/css" href="style.css"/>
</head>
<body>
    <h1>Folio v3.0 Verification</h1>
    
    <h2>Decoded HTML4/HTML5 Character Entities</h2>
    <p>Chemical reaction equation: 2H<sub>2</sub> + O<sub>2</sub> &rarr; 2H<sub>2</sub>O and atmospheric CO<sub>2</sub>.</p>
    <p>Math symbols: 5 &times; 10 &plusmn; 2 &le; 100 &ne; &infin; &hellip; QED.</p>
    <p>Typography and Accents: &mdash;caf&eacute; au lait&mdash; in &Omega;hm realm &copy; 2026.</p>
    <p>Numeric entities: Decimal &#8594; and Hex &#x2192; arrow directions.</p>

    <hr/>

    <h2>Baseline Proportions &amp; Verified Inlines</h2>
    <p>Einstein formula: <em>E</em> = <em>mc</em><sup>2</sup> sitting cleanly on the baseline.</p>
    <p>Water molecule: H<sub>2</sub>O and glucose C<sub>6</sub>H<sub>12</sub>O<sub>6</sub> with proportional subscripts.</p>
    <p>Greek alphabet span: &alpha;, &beta;, &gamma;, &Delta;, &Theta;, &pi;, &sigma;, &omega;, &Omega;.</p>
    <p>Fractions and currency: &frac12; liter of milk for &euro;1.50 and &pound;1.25.</p>
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
        
    print(f"Created {output_path} ({os.path.getsize(output_path)} bytes)")

if __name__ == "__main__":
    import sys
    out = sys.argv[1] if len(sys.argv) > 1 else "logs/r33_html_entities.epub"
    create_epub(out)
