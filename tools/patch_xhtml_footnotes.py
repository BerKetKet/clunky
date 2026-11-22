#!/usr/bin/env python3
import re

with open("kfx_converter/src/epub/xhtml.rs", "r") as f:
    code = f.read()

# 1. Add current_block_id
code = code.replace(
    "    let mut current_anchor: Option<String> = None;\n",
    "    let mut current_anchor: Option<String> = None;\n    let mut current_block_id: Option<String> = None;\n"
)

# 2. Update flush_current_span definition
old_flush_def = """    let flush_current_span = |text: &mut String,
                              spans: &mut Vec<TextSpan>,
                              b: bool,
                              i: bool,
                              u: bool,
                              c: bool,
                              stk: bool,
                              sp: bool,
                              sb: bool,
                              href: &Option<String>,
                              anchor: &Option<String>,
                              cs: &Option<String>| {
        if !text.is_empty() || (anchor.is_some() && href.is_none()) {
            let mut style_parts = Vec::new();
            if b {
                style_parts.push("bold");
            }
            if i {
                style_parts.push("italic");
            }
            if u {
                style_parts.push("underline");
            }
            if c {
                style_parts.push("code");
            }
            if stk {
                style_parts.push("strike");
            }
            if sp {
                style_parts.push("sup");
            }
            if sb {
                style_parts.push("sub");
            }
            if let Some(s) = cs {
                style_parts.push(s.as_str());
            }
            let style = if style_parts.is_empty() {
                None
            } else {
                Some(style_parts.join(" "))
            };

            let normalized_text: String = text.nfc().collect();
            text.clear();
            spans.push(TextSpan {
                text: normalized_text,
                style,
                anchor_id: anchor.clone(),
                href: href.clone(),
                ruby: None,
            });
        }
    };"""

new_flush_def = """    let mut flush_current_span = |text: &mut String,
                                  spans: &mut Vec<TextSpan>,
                                  b: bool,
                                  i: bool,
                                  u: bool,
                                  c: bool,
                                  stk: bool,
                                  sp: bool,
                                  sb: bool,
                                  href: &Option<String>,
                                  anchor: &mut Option<String>,
                                  cs: &Option<String>| {
        if !text.is_empty() || (anchor.is_some() && href.is_none()) {
            let mut style_parts = Vec::new();
            if b {
                style_parts.push("bold");
            }
            if i {
                style_parts.push("italic");
            }
            if u {
                style_parts.push("underline");
            }
            if c {
                style_parts.push("code");
            }
            if stk {
                style_parts.push("strike");
            }
            if sp {
                style_parts.push("sup");
            }
            if sb {
                style_parts.push("sub");
            }
            if let Some(s) = cs {
                style_parts.push(s.as_str());
            }
            let style = if style_parts.is_empty() {
                None
            } else {
                Some(style_parts.join(" "))
            };

            let normalized_text: String = text.nfc().collect();
            text.clear();
            let anchor_id = anchor.take();
            spans.push(TextSpan {
                text: normalized_text,
                style,
                anchor_id,
                href: href.clone(),
                ruby: None,
            });
        }
    };"""

assert old_flush_def in code, "old_flush_def not found in code"
code = code.replace(old_flush_def, new_flush_def)

# 3. Replace all call sites &current_anchor -> &mut current_anchor
code = code.replace("&current_anchor", "&mut current_anchor")

# 4. In start element parsing, update current_anchor and current_block_id
old_elem_id = """                        if elem_id.is_some() && current_anchor.is_none() {
                            current_anchor = elem_id.clone();
                        }"""
new_elem_id = """                        if let Some(ref id) = elem_id {
                            current_anchor = Some(id.clone());
                            current_block_id = Some(id.clone());
                        }"""
assert old_elem_id in code, "old_elem_id not found"
code = code.replace(old_elem_id, new_elem_id)

# 5. In Block::Paragraph creation, use current_block_id.take()
# We can replace: id: format!("p_{}", blocks.len() + 1),
# with: id: current_block_id.take().unwrap_or_else(|| format!("p_{}", blocks.len() + 1)),
code = code.replace(
    'id: format!("p_{}", blocks.len() + 1),',
    'id: current_block_id.take().unwrap_or_else(|| format!("p_{}", blocks.len() + 1)),'
)

# 6. At aside/div/section end tag, clear current_anchor and current_block_id
old_end_aside = """                            if local_bytes == b"blockquote" {
                                in_blockquote = false;
                            }"""
new_end_aside = """                            if local_bytes == b"blockquote" {
                                in_blockquote = false;
                            }
                            if local_bytes == b"aside" || local_bytes == b"div" || local_bytes == b"section" {
                                current_anchor = None;
                                current_block_id = None;
                            }"""
assert old_end_aside in code, "old_end_aside not found"
code = code.replace(old_end_aside, new_end_aside)

with open("kfx_converter/src/epub/xhtml.rs", "w") as f:
    f.write(code)

print("Successfully patched kfx_converter/src/epub/xhtml.rs")
