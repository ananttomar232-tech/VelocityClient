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


# ------------------------------------------------------------------ icons

HEART = [
    ".##...##.",
    "#hh#.#oo#",
    "#hoo#oos#",
    "#ooooooo#",
    "#oooooos#",
    ".#oooos#.",
    "..#oos#..",
    "...#s#...",
    "....#....",
]

FOOD = [
    ".....###.",
    "....#hmm#",
    "...#mmmm#",
    "...#mmmm#",
    "...#mmm#.",
    "..#b###..",
    ".#bb#....",
    "#bb#.....",
    ".##......",
]

ARMOR = [
    "##.....##",
    "#a#...#a#",
    "#aa###aa#",
    "#aaaaaaa#",
    ".#aahaa#.",
    ".#aaaaa#.",
    ".#aaaaa#.",
    ".#aaaaa#.",
    ".#######.",
]

BUBBLE = [
    "..#####..",
    ".#ccccc#.",
    "#cwccccc#",
    "#cwccccc#",
    "#ccccccc#",
    "#ccccccc#",
    "#ccccccc#",
    ".#ccccc#.",
    "..#####..",
]

BUBBLE_POP = [
    "..#...#..",
    ".#.....#.",
    "#.......#",
    ".........",
    "#.......#",
    ".........",
    "#.......#",
    ".#.....#.",
    "..#...#..",
]

EMPTY_FILL = hexa("#141821", 170)


def half(rows, keep_cols, fill_chars, empty="e"):
    """Replace fill characters outside keep_cols with the empty fill."""
    out = []
    for row in rows:
        out.append("".join(ch if (x in keep_cols or ch not in fill_chars) else empty for x, ch in enumerate(row)))
    return out


def container(rows, fill_chars):
    return ["".join("e" if ch in fill_chars else ch for ch in row) for row in rows]


def hearts():
    kinds = {
        "": ("#FF4D5E", "#FFA3AC", "#C4263A"),
        "poisoned_": ("#7FD84C", "#C6F5A6", "#4E9A26"),
        "withered_": ("#5A5A63", "#9A9AA4", "#34343A"),
        "absorbing_": ("#FFD54A", "#FFF0A8", "#D19A12"),
        "frozen_": ("#8FD8FF", "#E4F6FF", "#4FA6D6"),
    }
    fill = "hos"
    for prefix, (o, h, s) in kinds.items():
        for hardcore in (False, True):
            for blink in (False, True):
                outline = hexa("#FFFFFF") if blink else hexa("#22070C" if prefix != "withered_" else "#000000")
                pal = {"#": outline, "o": hexa(o), "h": hexa(h), "s": hexa(s), "e": EMPTY_FILL}
                if hardcore:
                    # Hardcore hearts get a bright shine stripe so they stand out.
                    pal["h"] = hexa("#FFFFFF")
                    pal["s"] = hexa(s if prefix else "#8E1022")
                hc = "hardcore_" if hardcore else ""
                bl = "_blinking" if blink else ""
                save(from_map(HEART, pal), "velocity_icons", f"gui/sprites/hud/heart/{prefix}{hc}full{bl}.png")
                save(from_map(half(HEART, range(0, 5), fill), pal), "velocity_icons", f"gui/sprites/hud/heart/{prefix}{hc}half{bl}.png")
    for hardcore in (False, True):
        for blink in (False, True):
            pal = {"#": hexa("#FFFFFF") if blink else hexa("#05060A", 220), "e": EMPTY_FILL}
            name = "container" + ("_hardcore" if hardcore else "") + ("_blinking" if blink else "")
            save(from_map(container(HEART, fill), pal), "velocity_icons", f"gui/sprites/hud/heart/{name}.png")
    # Mount (horse) health
    pal = {"#": hexa("#2A0712"), "o": hexa("#FF7AA8"), "h": hexa("#FFC2D8"), "s": hexa("#C4407A"), "e": EMPTY_FILL}
    save(from_map(HEART, pal), "velocity_icons", "gui/sprites/hud/heart/vehicle_full.png")
    save(from_map(half(HEART, range(0, 5), fill), pal), "velocity_icons", "gui/sprites/hud/heart/vehicle_half.png")
    save(from_map(container(HEART, fill), {"#": hexa("#05060A", 220), "e": EMPTY_FILL}), "velocity_icons", "gui/sprites/hud/heart/vehicle_container.png")


def food():
    fill = "hmb"
    for suffix, meat, light in (("", "#E58B3A", "#FFC27D"), ("_hunger", "#8DB04A", "#C8E08A")):
        pal = {"#": hexa("#2E1606"), "m": hexa(meat), "h": hexa(light), "b": hexa("#F4EFE6"), "e": EMPTY_FILL}
        save(from_map(FOOD, pal), "velocity_icons", f"gui/sprites/hud/food_full{suffix}.png")
        # Half: only the meaty right side stays.
        save(from_map(half(FOOD, range(4, 9), fill), pal), "velocity_icons", f"gui/sprites/hud/food_half{suffix}.png")
        save(from_map(container(FOOD, fill), {"#": hexa("#05060A", 220), "e": EMPTY_FILL}), "velocity_icons", f"gui/sprites/hud/food_empty{suffix}.png")


def armor():
    fill = "ah"
    pal = {"#": hexa("#1C212B"), "a": hexa("#C9D4E3"), "h": hexa("#FFFFFF"), "e": EMPTY_FILL}
    save(from_map(ARMOR, pal), "velocity_icons", "gui/sprites/hud/armor_full.png")
    save(from_map(half(ARMOR, range(0, 5), fill), pal), "velocity_icons", "gui/sprites/hud/armor_half.png")
    save(from_map(container(ARMOR, fill), {"#": hexa("#05060A", 220), "e": EMPTY_FILL}), "velocity_icons", "gui/sprites/hud/armor_empty.png")


def air():
    pal = {"#": hexa("#CFEFFF"), "c": hexa("#3FA9F5", 150), "w": hexa("#FFFFFF")}
    save(from_map(BUBBLE, pal), "velocity_icons", "gui/sprites/hud/air.png")
    save(from_map(BUBBLE_POP, {"#": hexa("#CFEFFF", 200)}), "velocity_icons", "gui/sprites/hud/air_bursting.png")


def rounded_box(w, h, r, fill, border=None, border_width=1):
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    for y in range(h):
        for x in range(w):
            # Distance outside the rounded rectangle core.
            cx = min(max(x + 0.5, r), w - r)
            cy = min(max(y + 0.5, r), h - r)
            d = ((x + 0.5 - cx) ** 2 + (y + 0.5 - cy) ** 2) ** 0.5
            if d > r:
                continue
            on_edge = d > r - border_width or x < border_width or y < border_width \
                or x >= w - border_width or y >= h - border_width
            img.putpixel((x, y), border if (border and on_edge) else fill)
    return img


def hotbar():
    bar = rounded_box(182, 22, 4, hexa("#10131B", 150), hexa("#FFFFFF", 45))
    # Subtle slot separators (vanilla slots are 20px apart starting at x=1).
    for i in range(1, 9):
        x = 1 + i * 20
        for y in range(4, 18):
            bar.putpixel((x, y), hexa("#FFFFFF", 22))
    save(bar, "velocity_icons", "gui/sprites/hud/hotbar.png")
    sel = rounded_box(24, 23, 5, hexa("#FFFFFF", 28), hexa("#FFFFFF", 235), border_width=2)
    save(sel, "velocity_icons", "gui/sprites/hud/hotbar_selection.png")


def xp_bar():
    bg = rounded_box(182, 5, 2, hexa("#0B0E14", 170))
    save(bg, "velocity_icons", "gui/sprites/hud/experience_bar_background.png")
    prog = Image.new("RGBA", (182, 5), (0, 0, 0, 0))
    a, b = hexa("#4BE38A"), hexa("#2BC4E6")
    mask = rounded_box(182, 5, 2, (255, 255, 255, 255))
    for x in range(182):
        t = x / 181
        col = tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))
        for y in range(5):
            if mask.getpixel((x, y))[3]:
                shade = 1.15 if y == 1 else (0.85 if y == 4 else 1.0)
                prog.putpixel((x, y), tuple(min(255, int(c * shade)) for c in col) + (255,))
    save(prog, "velocity_icons", "gui/sprites/hud/experience_bar_progress.png")


def crosshair():
    img = Image.new("RGBA", (15, 15), (0, 0, 0, 0))
    white = hexa("#FFFFFF")
    for i in range(15):
        if abs(i - 7) >= 2:  # small gap in the middle
            img.putpixel((7, i), white)
            img.putpixel((i, 7), white)
    img.putpixel((7, 7), white)
    save(img, "velocity_icons", "gui/sprites/hud/crosshair.png")


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
    write_meta("velocity_icons", "Velocity's clean flat HUD icons")
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
