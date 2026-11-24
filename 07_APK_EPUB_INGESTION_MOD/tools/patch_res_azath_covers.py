#!/usr/bin/env python3
"""
Themes stock Kindle Library generic book cover placeholders into Azath's
Dark Granite-Limestone palette (#1C1E22 card, #32353B border, #8A857D open-book
emblem + subtle #524B41 hairline rule, #ECE8E0 title, #8A857D author).
Idempotent: safe to run repeatedly on base_dec/res.
"""
import os
import re
import sys
from PIL import Image, ImageDraw

if len(sys.argv) < 2:
    print("Usage: patch_res_azath_covers.py <base_dec/res>")
    sys.exit(1)

res_dir = sys.argv[1]


def make_granite_cover_webp(out_path: str, w: int, h: int) -> None:
    scale = 4
    sw, sh = w * scale, h * scale
    bg = (28, 30, 34, 255)        # #1C1E22 Honed Granite surface
    border = (50, 53, 59, 255)    # #32353B Stone seam border
    crease = (21, 23, 26, 255)    # #15171A Modal dark crease
    ash = (138, 133, 125, 255)    # #8A857D Muted Limestone ash
    hairline = (82, 75, 65, 255)  # #524B41 Stone border hairline

    img = Image.new("RGBA", (sw, sh), bg)
    draw = ImageDraw.Draw(img)

    bw = max(scale, int(round(min(w, h) * 0.005 * scale)))
    draw.rectangle([0, 0, sw - 1, sh - 1], outline=border, width=bw)

    spine_x = int(sw * 0.055)
    draw.line([(spine_x, 0), (spine_x, sh)], fill=crease, width=max(scale, int(sw * 0.006)))

    cx = sw * 0.5
    top_y = sh * 0.155
    bot_y = sh * 0.315
    half_w = sw * 0.175
    gap = sw * 0.014
    lw = max(scale * 2, int(sw * 0.018))

    left_outer = [
        (cx - gap, bot_y + sh * 0.012),
        (cx - half_w, bot_y),
        (cx - half_w, top_y),
        (cx - gap, top_y + sh * 0.012),
    ]
    right_outer = [
        (cx + gap, bot_y + sh * 0.012),
        (cx + half_w, bot_y),
        (cx + half_w, top_y),
        (cx + gap, top_y + sh * 0.012),
    ]
    draw.polygon(left_outer, outline=ash, width=lw)
    draw.polygon(right_outer, outline=ash, width=lw)

    for frac in (0.34, 0.54, 0.74):
        ly = top_y + (bot_y - top_y) * frac + sh * 0.005
        draw.line([(cx - half_w * 0.76, ly), (cx - gap * 2.1, ly + sh * 0.004)], fill=ash, width=max(scale, lw // 2))
        draw.line([(cx + gap * 2.1, ly + sh * 0.004), (cx + half_w * 0.76, ly)], fill=ash, width=max(scale, lw // 2))

    rule_y = int(sh * 0.355)
    rule_hw = int(sw * 0.085)
    draw.line([(cx - rule_hw, rule_y), (cx + rule_hw, rule_y)], fill=hairline, width=max(scale, int(sh * 0.006)))

    out = img.resize((w, h), Image.Resampling.LANCZOS).convert("RGB")
    out.save(out_path, "WEBP", quality=95)


cover_variants = [
    ("drawable/generic_book_cover_list.webp", 400, 600),
    ("drawable-large/generic_book_cover_list.webp", 800, 1200),
    ("drawable-xlarge/generic_book_cover_list.webp", 960, 1440),
]
for rel, w, h in cover_variants:
    p = os.path.join(res_dir, rel)
    if os.path.exists(p):
        make_granite_cover_webp(p, w, h)
        print(f"Patched {rel} ({w}x{h}) -> Dark Granite generic cover (#1C1E22)")

colors_path = os.path.join(res_dir, "values", "colors.xml")
with open(colors_path, "r", encoding="utf-8") as f:
    c_xml = f.read()

c_xml, n1 = re.subn(
    r'(<color name="generated_cover_text_color">)#[0-9a-fA-F]+(</color>)',
    r'\g<1>#ece8e0\g<2>',
    c_xml,
)
c_xml, n2 = re.subn(
    r'(<color name="library_default_cover_color_dark">)#[0-9a-fA-F]+(</color>)',
    r'\g<1>#1c1e22\g<2>',
    c_xml,
)
c_xml, n3 = re.subn(
    r'(<color name="library_default_cover_color_light">)#[0-9a-fA-F]+(</color>)',
    r'\g<1>#1c1e22\g<2>',
    c_xml,
)
with open(colors_path, "w", encoding="utf-8") as f:
    f.write(c_xml)
print(f"Patched colors.xml tokens (generated_cover_text_color={n1}, dark_bg={n2}, light_bg={n3})")

overlay_path = os.path.join(res_dir, "layout", "badgeable_cover_rich_placeholder_overlay.xml")
if os.path.exists(overlay_path):
    with open(overlay_path, "r", encoding="utf-8") as f:
        ov = f.read()
    ov_new = re.sub(
        r'(<TextView\s+android:textColor=")[^"]+("[^>]*android:id="@id/rich_placeholder_author")',
        r'\g<1>#8a857d\g<2>',
        ov,
    )
    if ov_new != ov:
        with open(overlay_path, "w", encoding="utf-8") as f:
            f.write(ov_new)
        print("Patched badgeable_cover_rich_placeholder_overlay.xml author text -> #8a857d")

bds_dark = os.path.join(res_dir, "drawable", "bds_book_placeholder_dark.xml")
if os.path.exists(bds_dark):
    with open(bds_dark, "r", encoding="utf-8") as f:
        bd = f.read()
    bd = (bd.replace("#cad3db", "#1c1e22")
            .replace("#211c1b", "#1c1e22")
            .replace("#7abdff", "#524b41")
            .replace("#5c2d26", "#524b41"))
    with open(bds_dark, "w", encoding="utf-8") as f:
        f.write(bd)
    print("Patched bds_book_placeholder_dark.xml -> Granite #1C1E22 + #524B41 hairline")
