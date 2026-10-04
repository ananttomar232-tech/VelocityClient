#!/usr/bin/env python3
"""Generates Velocity's built-in resource packs (all art is drawn here, nothing copied from Minecraft).

  velocity_icons        clean flat hearts, hunger, armor, air, hotbar, XP bar and crosshair
  velocity_clear_glass  borderless-looking clear glass and stained glass

Run:  pip install pillow && python3 tools/generate_packs.py
"""
import json
import os
import shutil
from PIL import Image

ROOT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "resourcepacks")
ICON = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "velocity", "icon.png")
PACK_FORMAT = 34  # Minecraft 1.21 / 1.21.1


def hexa(h, a=255):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


def from_map(rows, palette):
    img = Image.new("RGBA", (len(rows[0]), len(rows)), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        assert len(row) == len(rows[0]), row
        for x, ch in enumerate(row):
            if ch in palette and palette[ch] is not None:
                img.putpixel((x, y), palette[ch])
    return img


def save(img, pack, path):
    out = os.path.join(ROOT, pack, "assets", "minecraft", "textures", path)
    os.makedirs(os.path.dirname(out), exist_ok=True)
    img.save(out)


def write_meta(pack, description):
    os.makedirs(os.path.join(ROOT, pack), exist_ok=True)
    with open(os.path.join(ROOT, pack, "pack.mcmeta"), "w") as f:
        json.dump({"pack": {"pack_format": PACK_FORMAT, "description": description}}, f, indent=2)
    shutil.copy(ICON, os.path.join(ROOT, pack, "pack.png"))


# ------------------------------------------------------------------ icons (HD)
#
# Icons are drawn as smooth vector shapes at 8x supersampling and saved at 4x resolution
# (a 9x9 heart becomes 36x36). Minecraft draws GUI sprites at their GUI size, so the extra
# pixels show up as crisp, anti-aliased icons on any GUI scale.

from PIL import ImageChops, ImageDraw, ImageFilter
import math

OUT = 4   # saved pixels per GUI pixel
SS = 8    # drawing pixels per GUI pixel (downsampled to OUT for anti-aliasing)


def mask_canvas(w, h):
    return Image.new("L", (round(w * SS), round(h * SS)), 0)


def pts(points):
    return [(x * SS, y * SS) for x, y in points]


def heart_mask(w=9, h=9, pad=0.35):
    m = mask_canvas(w, h)
    raw = []
    for i in range(400):
        t = i / 400 * 2 * math.pi
        x = 16 * math.sin(t) ** 3
        y = 13 * math.cos(t) - 5 * math.cos(2 * t) - 2 * math.cos(3 * t) - math.cos(4 * t)
        raw.append((x, -y))
    xs = [p[0] for p in raw]
    ys = [p[1] for p in raw]
    sx = (w - 2 * pad) / (max(xs) - min(xs))
    sy = (h - 2 * pad) / (max(ys) - min(ys))
    sc = min(sx, sy)
    ox = w / 2 - (max(xs) + min(xs)) / 2 * sc
    oy = h / 2 - (max(ys) + min(ys)) / 2 * sc + 0.15
    ImageDraw.Draw(m).polygon(pts([(x * sc + ox, y * sc + oy) for x, y in raw]), fill=255)
    return m


def rotated_ellipse(draw, cx, cy, rx, ry, angle_deg):
    a = math.radians(angle_deg)
    poly = []
    for i in range(120):
        t = i / 120 * 2 * math.pi
        x, y = rx * math.cos(t), ry * math.sin(t)
        poly.append((cx + x * math.cos(a) - y * math.sin(a), cy + x * math.sin(a) + y * math.cos(a)))
    draw.polygon(pts(poly), fill=255)


def thick_line(draw, x1, y1, x2, y2, width):
    draw.line(pts([(x1, y1), (x2, y2)]), fill=255, width=round(width * SS))
    r = width / 2 * SS
    for x, y in ((x1, y1), (x2, y2)):
        draw.ellipse((x * SS - r, y * SS - r, x * SS + r, y * SS + r), fill=255)


def meat_mask():
    m = mask_canvas(9, 9)
    rotated_ellipse(ImageDraw.Draw(m), 5.55, 3.45, 3.25, 2.45, -45)
    return m


def bone_mask():
    m = mask_canvas(9, 9)
    d = ImageDraw.Draw(m)
    thick_line(d, 4.2, 4.8, 2.0, 7.0, 1.25)
    for cx, cy in ((1.05, 6.75), (2.25, 7.95)):
        r = 0.85 * SS
        d.ellipse((cx * SS - r, cy * SS - r, cx * SS + r, cy * SS + r), fill=255)
    return m


def armor_mask():
    m = mask_canvas(9, 9)
    ImageDraw.Draw(m).polygon(pts([
        (0.55, 1.35), (2.5, 0.55), (3.35, 1.55), (5.65, 1.55), (6.5, 0.55), (8.45, 1.35),
        (8.55, 3.75), (7.15, 4.05), (7.15, 8.45), (1.85, 8.45), (1.85, 4.05), (0.45, 3.75)]), fill=255)
    return m


def circle_mask(w, h, cx, cy, r):
    m = mask_canvas(w, h)
    ImageDraw.Draw(m).ellipse(((cx - r) * SS, (cy - r) * SS, (cx + r) * SS, (cy + r) * SS), fill=255)
    return m


def rrect_mask(w, h, r, inset=0.0):
    m = mask_canvas(w, h)
    ImageDraw.Draw(m).rounded_rectangle((inset * SS, inset * SS, (w - inset) * SS - 1, (h - inset) * SS - 1),
                                        radius=r * SS, fill=255)
    return m


def grow(mask, gui_px):
    k = max(1, round(gui_px * SS))
    return mask.filter(ImageFilter.MaxFilter(2 * k + 1))


def shrink(mask, gui_px):
    # Pad first: the filter treats outside pixels as "inside", so edges touching the border would not erode.
    k = max(1, round(gui_px * SS))
    padded = Image.new("L", (mask.size[0] + 2 * k, mask.size[1] + 2 * k), 0)
    padded.paste(mask, (k, k))
    return padded.filter(ImageFilter.MinFilter(2 * k + 1)).crop((k, k, k + mask.size[0], k + mask.size[1]))


def vgradient(size, top, bottom):
    w, h = size
    g = Image.new("RGBA", size)
    px = g.load()
    for y in range(h):
        t = y / max(1, h - 1)
        c = tuple(round(top[i] + (bottom[i] - top[i]) * t) for i in range(4))
        for x in range(w):
            px[x, y] = c
    return g


def hgradient(size, left, right):
    w, h = size
    g = Image.new("RGBA", size)
    px = g.load()
    for x in range(w):
        t = x / max(1, w - 1)
        c = tuple(round(left[i] + (right[i] - left[i]) * t) for i in range(4))
        for y in range(h):
            px[x, y] = c
    return g


def paint(base, mask, color_or_image):
    """Composite a colour (or image) onto base through mask (both supersampled)."""
    if isinstance(color_or_image, tuple):
        layer = Image.new("RGBA", base.size, color_or_image)
    else:
        layer = color_or_image
    a = layer.getchannel("A")
    layer = layer.copy()
    layer.putalpha(ImageChops.multiply(a, mask))
    base.alpha_composite(layer)


def left_half(mask, frac=0.5):
    clip = Image.new("L", mask.size, 0)
    ImageDraw.Draw(clip).rectangle((0, 0, mask.size[0] * frac, mask.size[1]), fill=255)
    return ImageChops.multiply(mask, clip)


def right_part(mask, frac):
    clip = Image.new("L", mask.size, 0)
    ImageDraw.Draw(clip).rectangle((mask.size[0] * frac, 0, mask.size[0], mask.size[1]), fill=255)
    return ImageChops.multiply(mask, clip)


def shine(mask, cx, cy, rx, ry, alpha=120):
    """A soft glossy highlight inside the shape."""
    s = mask_canvas(mask.size[0] / SS, mask.size[1] / SS)
    ImageDraw.Draw(s).ellipse(((cx - rx) * SS, (cy - ry) * SS, (cx + rx) * SS, (cy + ry) * SS), fill=alpha)
    s = s.filter(ImageFilter.GaussianBlur(SS * 0.35))
    return ImageChops.multiply(s, shrink(mask, 0.5))


def finish(big, w, h):
    return big.resize((w * OUT, h * OUT), Image.LANCZOS)


OUTLINE_W = 0.6
EMPTY_FILL = hexa("#2A3040", 165)


def draw_icon(shape, top, bottom, outline, fill_part=None, shine_spec=None, empty=False, extra=None):
    """Generic icon: outline, empty backing, gradient fill (optionally partial) and a glossy shine."""
    big = Image.new("RGBA", shape.size, (0, 0, 0, 0))
    paint(big, grow(shape, OUTLINE_W), outline)
    paint(big, shape, EMPTY_FILL)
    if not empty:
        fill = shape if fill_part is None else fill_part
        paint(big, fill, vgradient(big.size, top, bottom))
        if shine_spec:
            paint(big, ImageChops.multiply(shine(shape, *shine_spec), fill), (255, 255, 255, 255))
        if extra:
            extra(big, fill)
    return big


HEART_KINDS = {
    "": ("#FF8A95", "#E0213A"),
    "poisoned_": ("#B7F07A", "#3F9A1E"),
    "withered_": ("#8C8C96", "#2E2E35"),
    "absorbing_": ("#FFF08A", "#E0A21A"),
    "frozen_": ("#E6F8FF", "#5FB4E6"),
}


def hearts():
    shape = heart_mask()
    half_fill = left_half(shape, 0.5)
    heart_shine = (2.7, 2.6, 1.35, 0.95)
    for prefix, (top, bottom) in HEART_KINDS.items():
        for hardcore in (False, True):
            for blink in (False, True):
                outline = hexa("#FFFFFF") if blink else hexa("#1A0408" if prefix != "withered_" else "#000000")
                t, b = hexa(top), hexa(bottom)

                def hardcore_mark(big, fill, _hc=hardcore):
                    if not _hc:
                        return
                    # Hardcore: a gold inner rim so the hearts look "armoured".
                    rim = ImageChops.subtract(shape, shrink(shape, 0.55))
                    paint(big, ImageChops.multiply(rim, fill), vgradient(big.size, hexa("#FFF3B0"), hexa("#E0A21A")))

                hc = "hardcore_" if hardcore else ""
                bl = "_blinking" if blink else ""
                full = draw_icon(shape, t, b, outline, None, heart_shine, extra=hardcore_mark)
                save(finish(full, 9, 9), "velocity_icons", f"gui/sprites/hud/heart/{prefix}{hc}full{bl}.png")
                half = draw_icon(shape, t, b, outline, half_fill, heart_shine, extra=hardcore_mark)
                save(finish(half, 9, 9), "velocity_icons", f"gui/sprites/hud/heart/{prefix}{hc}half{bl}.png")
    for hardcore in (False, True):
        for blink in (False, True):
            outline = hexa("#FFFFFF") if blink else hexa("#05060A", 235)
            name = "container" + ("_hardcore" if hardcore else "") + ("_blinking" if blink else "")
            save(finish(draw_icon(shape, None, None, outline, empty=True), 9, 9), "velocity_icons", f"gui/sprites/hud/heart/{name}.png")
    t, b, o = hexa("#FFB3CF"), hexa("#E0457F"), hexa("#22050F")
    save(finish(draw_icon(shape, t, b, o, None, heart_shine), 9, 9), "velocity_icons", "gui/sprites/hud/heart/vehicle_full.png")
    save(finish(draw_icon(shape, t, b, o, half_fill, heart_shine), 9, 9), "velocity_icons", "gui/sprites/hud/heart/vehicle_half.png")
    save(finish(draw_icon(shape, None, None, hexa("#05060A", 235), empty=True), 9, 9), "velocity_icons", "gui/sprites/hud/heart/vehicle_container.png")


def food():
    meat = meat_mask()
    bone = bone_mask()
    shape = ImageChops.lighter(meat, bone)
    for suffix, (mt, mb) in (("", ("#FFC27A", "#C25A22")), ("_hunger", ("#C9E58C", "#5E8A2A"))):
        for kind in ("full", "half", "empty"):
            big = Image.new("RGBA", shape.size, (0, 0, 0, 0))
            paint(big, grow(shape, OUTLINE_W), hexa("#2A1204") if kind != "empty" else hexa("#05060A", 235))
            paint(big, shape, EMPTY_FILL)
            if kind != "empty":
                meat_part = meat if kind == "full" else right_part(meat, 0.5)
                paint(big, meat_part, vgradient(big.size, hexa(mt), hexa(mb)))
                paint(big, ImageChops.multiply(shine(meat, 6.3, 2.4, 1.2, 0.7, 140), meat_part), (255, 255, 255, 255))
                if kind == "full":
                    paint(big, bone, vgradient(big.size, hexa("#FFFFFF"), hexa("#D8D0C0")))
            save(finish(big, 9, 9), "velocity_icons", f"gui/sprites/hud/food_{kind}{suffix}.png")


def armor():
    shape = armor_mask()

    def details(big, fill):
        line = mask_canvas(9, 9)
        ImageDraw.Draw(line).line(pts([(4.5, 2.2), (4.5, 8.0)]), fill=255, width=round(0.35 * SS))
        paint(big, ImageChops.multiply(line, fill), hexa("#7C8BA3", 200))

    o = hexa("#161A22")
    t, b = hexa("#FFFFFF"), hexa("#97A8C0")
    save(finish(draw_icon(shape, t, b, o, None, (3.0, 2.6, 1.3, 0.8), extra=details), 9, 9), "velocity_icons", "gui/sprites/hud/armor_full.png")
    save(finish(draw_icon(shape, t, b, o, left_half(shape), (3.0, 2.6, 1.3, 0.8), extra=details), 9, 9), "velocity_icons", "gui/sprites/hud/armor_half.png")
    save(finish(draw_icon(shape, None, None, hexa("#05060A", 235), empty=True), 9, 9), "velocity_icons", "gui/sprites/hud/armor_empty.png")


def air():
    shape = circle_mask(9, 9, 4.5, 4.5, 3.9)
    big = Image.new("RGBA", shape.size, (0, 0, 0, 0))
    paint(big, grow(shape, 0.45), hexa("#D9F3FF", 235))
    paint(big, shape, vgradient(big.size, hexa("#9BE0FF", 190), hexa("#2F86D9", 200)))
    paint(big, shine(shape, 3.2, 3.0, 1.2, 0.9, 200), (255, 255, 255, 255))
    save(finish(big, 9, 9), "velocity_icons", "gui/sprites/hud/air.png")
    # Bursting bubble: a broken ring
    ring = ImageChops.subtract(grow(shape, 0.45), shape)
    cut = mask_canvas(9, 9)
    d = ImageDraw.Draw(cut)
    for ang in range(0, 360, 45):
        a = math.radians(ang)
        d.line(pts([(4.5, 4.5), (4.5 + 6 * math.cos(a), 4.5 + 6 * math.sin(a))]), fill=255, width=round(0.9 * SS))
    pop = Image.new("RGBA", shape.size, (0, 0, 0, 0))
    paint(pop, ImageChops.subtract(ring, cut), hexa("#D9F3FF", 230))
    save(finish(pop, 9, 9), "velocity_icons", "gui/sprites/hud/air_bursting.png")


def hotbar():
    w, h = 182, 22
    shape = rrect_mask(w, h, 4.5)
    big = Image.new("RGBA", shape.size, (0, 0, 0, 0))
    paint(big, shape, hexa("#FFFFFF", 70))                                  # border
    inner = shrink(shape, 0.5)
    paint(big, inner, vgradient(big.size, hexa("#1C2130", 165), hexa("#0B0D13", 175)))
    sep = mask_canvas(w, h)
    d = ImageDraw.Draw(sep)
    for i in range(1, 9):
        x = 1 + i * 20
        d.line(pts([(x, 5), (x, 17)]), fill=255, width=round(0.4 * SS))
    paint(big, sep, hexa("#FFFFFF", 26))
    save(finish(big, w, h), "velocity_icons", "gui/sprites/hud/hotbar.png")

    w, h = 24, 23
    shape = rrect_mask(w, h, 5)
    big = Image.new("RGBA", shape.size, (0, 0, 0, 0))
    glow = grow(shape, 0.01).filter(ImageFilter.GaussianBlur(SS * 0.6))
    ring = ImageChops.subtract(shape, shrink(shape, 1.25))
    paint(big, shrink(shape, 1.25), hexa("#FFFFFF", 30))
    paint(big, ring, vgradient(big.size, hexa("#FFFFFF", 255), hexa("#D6DCEA", 245)))
    save(finish(big, w, h), "velocity_icons", "gui/sprites/hud/hotbar_selection.png")


def xp_bar():
    w, h = 182, 5
    shape = rrect_mask(w, h, 2.5)
    big = Image.new("RGBA", shape.size, (0, 0, 0, 0))
    paint(big, shape, hexa("#0A0D13", 175))
    paint(big, ImageChops.subtract(shape, shrink(shape, 0.4)), hexa("#FFFFFF", 35))
    save(finish(big, w, h), "velocity_icons", "gui/sprites/hud/experience_bar_background.png")

    big = Image.new("RGBA", shape.size, (0, 0, 0, 0))
    paint(big, shape, hgradient(big.size, hexa("#5CF29A"), hexa("#2CC6EE")))
    top = mask_canvas(w, h)
    ImageDraw.Draw(top).rectangle((0, 0.6 * SS, w * SS, 1.9 * SS), fill=110)
    paint(big, ImageChops.multiply(top, shape), (255, 255, 255, 255))
    bottom = mask_canvas(w, h)
    ImageDraw.Draw(bottom).rectangle((0, 3.6 * SS, w * SS, h * SS), fill=70)
    paint(big, ImageChops.multiply(bottom, shape), (0, 40, 30, 255))
    save(finish(big, w, h), "velocity_icons", "gui/sprites/hud/experience_bar_progress.png")


def crosshair():
    w = h = 15
    m = mask_canvas(w, h)
    d = ImageDraw.Draw(m)
    c, gap, arm, th = 7.5, 1.6, 5.6, 1.0
    for x1, y1, x2, y2 in ((c - gap - arm, c, c - gap, c), (c + gap, c, c + gap + arm, c),
                           (c, c - gap - arm, c, c - gap), (c, c + gap, c, c + gap + arm)):
        thick_line(d, x1, y1, x2, y2, th)
    r = 0.55 * SS
    d.ellipse((c * SS - r, c * SS - r, c * SS + r, c * SS + r), fill=255)
    big = Image.new("RGBA", m.size, (0, 0, 0, 0))
    paint(big, m, (255, 255, 255, 255))
    save(finish(big, w, h), "velocity_icons", "gui/sprites/hud/crosshair.png")


# ------------------------------------------------------------------ clear glass

STAINED = {
    "white": "#F9FFFE", "orange": "#F9801D", "magenta": "#C74EBD", "light_blue": "#3AB3DA",
    "yellow": "#FED83D", "lime": "#80C71F", "pink": "#F38BAA", "gray": "#474F52",
    "light_gray": "#9D9D97", "cyan": "#169C9C", "purple": "#8932B8", "blue": "#3C44AA",
    "brown": "#835432", "green": "#5E7C16", "red": "#B02E26", "black": "#1D1D21",
}


def glass_tile(edge, inner):
    img = Image.new("RGBA", (16, 16), inner)
    for i in range(16):
        for p in ((i, 0), (i, 15), (0, i), (15, i)):
            img.putpixel(p, edge)
    return img


def clear_glass():
    # Plain glass is "cutout": pixels are either solid or invisible, so keep a thin frame and an empty middle.
    save(glass_tile(hexa("#D6ECF2"), (0, 0, 0, 0)), "velocity_clear_glass", "block/glass.png")
    save(Image.new("RGBA", (16, 16), hexa("#D6ECF2")), "velocity_clear_glass", "block/glass_pane_top.png")
    for name, color in STAINED.items():
        save(glass_tile(hexa(color, 175), hexa(color, 60)), "velocity_clear_glass", f"block/{name}_stained_glass.png")
        save(Image.new("RGBA", (16, 16), hexa(color, 175)), "velocity_clear_glass", f"block/{name}_stained_glass_pane_top.png")


def main():
    for pack in ("velocity_icons", "velocity_clear_glass"):
        shutil.rmtree(os.path.join(ROOT, pack), ignore_errors=True)
    write_meta("velocity_icons", "Velocity's smooth HD HUD icons")
    write_meta("velocity_clear_glass", "Clear glass without streaks")
    hearts()
    food()
    armor()
    air()
    hotbar()
    xp_bar()
    crosshair()
    clear_glass()
    print("packs generated in", os.path.normpath(ROOT))


if __name__ == "__main__":
    main()
