#!/usr/bin/env python3
"""
Themes stock Kindle Library generic book cover placeholders into Azath's
dark Obsidian-Ivory-Ash palette (#211C1B card, #2A2423 border, #8E867E open-book
icon + subtle #5C2D26 rule, #F2EFE9 title, #8E867E author).
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


def make_obsidian_cover_webp(out_path: str, w: int, h: int) -> None:
    scale = 4
    sw, sh = w * scale, h * scale
    bg = (33, 28, 27, 255)        # #211C1B Obsidian
    border = (42, 36, 35, 255)    # #2A2423 subtle rim
    ash = (142, 134, 126, 255)    # #8E867E Warm Ash
    crimson = (92, 45, 38, 255)   # #5C2D26 muted crimson spine rule

    img = Image.new("RGBA", (sw, sh), bg)
    draw = ImageDraw.Draw(img)

    # Outer border (subtle 1.5dp-equivalent at target resolution)
    bw = max(scale, int(round(min(w, h) * 0.005 * scale)))
    draw.rectangle([0, 0, sw - 1, sh - 1], outline=border, width=bw)

    # Subtle vertical book-spine crease near left edge
    spine_x = int(sw * 0.055)
    draw.line([(spine_x, 0), (spine_x, sh)], fill=(24, 21, 21, 255), width=max(scale, int(sw * 0.006)))

    # Crisp geometric open-book emblem in upper-center (y ~ 15%..33%)
    cx = sw * 0.5
    top_y = sh * 0.155
    bot_y = sh * 0.315
    half_w = sw * 0.175
    gap = sw * 0.014
    lw = max(scale * 2, int(sw * 0.018))

    # Left cover + page block
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

    # Inner curved/offset page lines on left & right pages
    for frac in (0.34, 0.54, 0.74):
        ly = top_y + (bot_y - top_y) * frac + sh * 0.005
        draw.line([(cx - half_w * 0.76, ly), (cx - gap * 2.1, ly + sh * 0.004)], fill=ash, width=max(scale, lw // 2))
        draw.line([(cx + gap * 2.1, ly + sh * 0.004), (cx + half_w * 0.76, ly)], fill=ash, width=max(scale, lw // 2))

    # Subtle crimson spine hairline beneath book emblem
    rule_y = int(sh * 0.355)
    rule_hw = int(sw * 0.085)
    draw.line([(cx - rule_hw, rule_y), (cx + rule_hw, rule_y)], fill=crimson, width=max(scale, int(sh * 0.006)))

    out = img.resize((w, h), Image.Resampling.LANCZOS).convert("RGB")
    out.save(out_path, "WEBP", quality=95)


# 1. Generic book cover bitmaps across all present density buckets
cover_variants = [
    ("drawable/generic_book_cover_list.webp", 400, 600),
    ("drawable-large/generic_book_cover_list.webp", 800, 1200),
    ("drawable-xlarge/generic_book_cover_list.webp", 960, 1440),
]
for rel, w, h in cover_variants:
    p = os.path.join(res_dir, rel)
    if os.path.exists(p):
        make_obsidian_cover_webp(p, w, h)
        print(f"Patched {rel} ({w}x{h}) -> Dark Obsidian generic cover")

# 2. Color tokens in values/colors.xml
colors_path = os.path.join(res_dir, "values", "colors.xml")
with open(colors_path, "r", encoding="utf-8") as f:
    c_xml = f.read()

c_xml, n1 = re.subn(
    r'(<color name="generated_cover_text_color">)#[0-9a-fA-F]+(</color>)',
    r'\g<1>#f2efe9\g<2>',
    c_xml,
)
c_xml, n2 = re.subn(
    r'(<color name="library_default_cover_color_dark">)#[0-9a-fA-F]+(</color>)',
    r'\g<1>#211c1b\g<2>',
    c_xml,
)
c_xml, n3 = re.subn(
    r'(<color name="library_default_cover_color_light">)#[0-9a-fA-F]+(</color>)',
    r'\g<1>#211c1b\g<2>',
    c_xml,
)
with open(colors_path, "w", encoding="utf-8") as f:
    f.write(c_xml)
print(f"Patched colors.xml tokens (generated_cover_text_color={n1}, dark_bg={n2}, light_bg={n3})")

# 3. Rich placeholder overlay author color -> Warm Ash (#8E867E)
overlay_path = os.path.join(res_dir, "layout", "badgeable_cover_rich_placeholder_overlay.xml")
if os.path.exists(overlay_path):
    with open(overlay_path, "r", encoding="utf-8") as f:
        ov = f.read()
    ov_new = re.sub(
        r'(<TextView\s+android:textColor=")[^"]+("[^>]*android:id="@id/rich_placeholder_author")',
        r'\g<1>#8e867e\g<2>',
        ov,
    )
    if ov_new != ov:
        with open(overlay_path, "w", encoding="utf-8") as f:
            f.write(ov_new)
        print("Patched badgeable_cover_rich_placeholder_overlay.xml author text -> #8e867e")

# 4. BDS vector placeholders (if displayed)
bds_dark = os.path.join(res_dir, "drawable", "bds_book_placeholder_dark.xml")
if os.path.exists(bds_dark):
    with open(bds_dark, "r", encoding="utf-8") as f:
        bd = f.read()
    bd = bd.replace("#cad3db", "#211c1b").replace("#7abdff", "#5c2d26")
    with open(bds_dark, "w", encoding="utf-8") as f:
        f.write(bd)
    print("Patched bds_book_placeholder_dark.xml -> Obsidian + muted crimson bookmark")
