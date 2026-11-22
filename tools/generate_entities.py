import html.entities
import os

def rust_str_literal(s):
    res = []
    for c in s:
        code = ord(c)
        if c == '"':
            res.append('\\"')
        elif c == '\\':
            res.append('\\\\')
        elif c == '\n':
            res.append('\\n')
        elif c == '\r':
            res.append('\\r')
        elif c == '\t':
            res.append('\\t')
        elif 32 <= code < 127:
            res.append(c)
        else:
            res.append(f'\\u{{{code:x}}}')
    return '"' + ''.join(res) + '"'

out = []
out.append('//! Complete HTML4 and HTML5 character entity decoder.\n')
out.append('//!\n')
out.append('//! Provides full coverage of all W3C HTML5 named character references\n')
out.append('//! as well as decimal and hexadecimal numeric character references.\n\n')

out.append('/// Look up an HTML named entity (without leading & or trailing ;).\n')
out.append('pub fn lookup_html_entity(name: &str) -> Option<&\'static str> {\n')
out.append('    match name {\n')

seen = set()
# Ensure XML standard entities are prioritized and present
xml_entities = [
    ("amp", "&"),
    ("lt", "<"),
    ("gt", ">"),
    ("quot", "\""),
    ("apos", "'"),
]
for name, val in xml_entities:
    seen.add(name)
    out.append(f'        "{name}" => Some({rust_str_literal(val)}),\n')

# Add all HTML5 entities
for k, v in html.entities.html5.items():
    name = k[:-1] if k.endswith(';') else k
    if name in seen:
        continue
    seen.add(name)
    val = "" if name == "shy" else v
    out.append(f'        "{name}" => Some({rust_str_literal(val)}),\n')

out.append('        _ => None,\n')
out.append('    }\n')
out.append('}\n\n')

out.append('''/// Decodes XML and HTML named and numeric entities in a single linear pass.
///
/// If the string contains no `&`, it returns the original string without allocation.
pub fn decode_html_entities(s: &str) -> String {
    if !s.contains('&') {
        return s.to_string();
    }
    let mut out = String::with_capacity(s.len());
    let mut chars = s.char_indices().peekable();

    while let Some((_, c)) = chars.next() {
        if c == '&' {
            if let Some(&(_, '#')) = chars.peek() {
                chars.next(); // consume '#'
                let is_hex = matches!(chars.peek(), Some(&(_, 'x')) | Some(&(_, 'X')));
                if is_hex {
                    chars.next(); // consume 'x'/'X'
                }
                let mut num_str = String::with_capacity(8);
                while let Some(&(_, nc)) = chars.peek() {
                    if (is_hex && nc.is_ascii_hexdigit()) || (!is_hex && nc.is_ascii_digit()) {
                        num_str.push(nc);
                        chars.next();
                    } else {
                        break;
                    }
                }
                if let Some(&(_, ';')) = chars.peek() {
                    chars.next(); // consume ';'
                    let parsed = if is_hex {
                        u32::from_str_radix(&num_str, 16).ok()
                    } else {
                        num_str.parse::<u32>().ok()
                    };
                    if let Some(ch) = parsed.and_then(char::from_u32) {
                        out.push(ch);
                        continue;
                    } else {
                        out.push('&');
                        out.push('#');
                        if is_hex {
                            out.push('x');
                        }
                        out.push_str(&num_str);
                        out.push(';');
                        continue;
                    }
                } else {
                    out.push('&');
                    out.push('#');
                    if is_hex {
                        out.push('x');
                    }
                    out.push_str(&num_str);
                    continue;
                }
            } else {
                let mut name = String::with_capacity(16);
                let mut found_semi = false;
                while let Some(&(_, nc)) = chars.peek() {
                    if nc.is_ascii_alphanumeric() {
                        name.push(nc);
                        chars.next();
                        if name.len() > 32 {
                            break;
                        }
                    } else if nc == ';' {
                        chars.next();
                        found_semi = true;
                        break;
                    } else {
                        break;
                    }
                }
                if found_semi && !name.is_empty() {
                    if let Some(replacement) = lookup_html_entity(&name) {
                        out.push_str(replacement);
                        continue;
                    } else {
                        out.push('&');
                        out.push_str(&name);
                        out.push(';');
                        continue;
                    }
                } else {
                    out.push('&');
                    out.push_str(&name);
                    continue;
                }
            }
        }
        out.push(c);
    }
    out
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_decode_html_entities() {
        assert_eq!(decode_html_entities("Hello World"), "Hello World");
        assert_eq!(decode_html_entities("AT&T and Tom & Jerry"), "AT&T and Tom & Jerry");
        assert_eq!(decode_html_entities("2H₂ + O₂ &rarr; 2H₂O"), "2H₂ + O₂ → 2H₂O");
        assert_eq!(decode_html_entities("&times; &plusmn; &hellip; &mdash; &eacute; &Omega;"), "× ± … — é Ω");
        assert_eq!(decode_html_entities("Decimal &#8594; and Hex &#x2192; and upper &#X2192;"), "Decimal → and Hex → and upper →");
        assert_eq!(decode_html_entities("&copy; 2026 &reg; &trade; &pound;100 &euro;50"), "© 2026 ® ™ £100 €50");
        assert_eq!(decode_html_entities("&alpha;&beta;&gamma; &Alpha;&Beta;&Gamma;"), "αβγ ΑΒΓ");
        assert_eq!(decode_html_entities("&frac12; &frac14; &frac34;"), "½ ¼ ¾");
        assert_eq!(decode_html_entities("&sub; &sup;"), "⊂ ⊃");
        assert_eq!(decode_html_entities("&unknown; stays unknown"), "&unknown; stays unknown");
    }
}
''')

target = os.path.join(os.path.dirname(__file__), '..', 'kfx_converter', 'src', 'epub', 'entities.rs')
with open(target, 'w', encoding='utf-8') as f:
    f.writelines(out)

print(f"Successfully generated {target} with {len(seen)} entities.")
