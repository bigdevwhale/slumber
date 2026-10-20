"""Draws the README logo, the launcher icon's spinning top in lamplight, as docs/logo.png.

python tools/icon.py
"""
import math
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parent.parent
S = 1024
K = 4  # supersampling

MAPLE = (0xD9, 0xA8, 0x6C)
MAPLE_DARK = (0x9C, 0x6B, 0x3C)
RED = (0xB8, 0x43, 0x2E)
TEAL = (0x2E, 0x6E, 0x73)
BRASS = (0xF1, 0xC6, 0x6C)
STEEL = (0xB9, 0xB7, 0xB0)


def bez(p0, p1, p2, p3, n=24):
    return [tuple((1 - t) ** 3 * a + 3 * (1 - t) ** 2 * t * b + 3 * (1 - t) * t ** 2 * c + t ** 3 * d for a, b, c, d in zip(p0, p1, p2, p3))
            for t in [i / n for i in range(n + 1)]]


def main():
    n = S * K
    u = n / 108
    im = Image.new("RGB", (n, n), (0x15, 0x10, 0x0C))

    # The lamp's warm pool.
    glow = Image.new("RGB", (n, n), (0, 0, 0))
    ImageDraw.Draw(glow).ellipse([-10 * u, 20 * u, 100 * u, 120 * u], fill=(0x7A, 0x51, 0x30))
    glow = glow.filter(ImageFilter.GaussianBlur(26 * u))
    im = Image.blend(im, glow, 0.8)
    d = ImageDraw.Draw(im)
    d.rectangle([0, 76 * u, n, n], fill=(0x22, 0x17, 0x0E))

    rot = math.radians(7)

    def p(x, y):
        dx, dy = x - 54, y - 78
        return ((54 + dx * math.cos(rot) - dy * math.sin(rot)) * u, (78 + dx * math.sin(rot) + dy * math.cos(rot)) * u)

    d.ellipse([42 * u, 76 * u, 66 * u, 81 * u], fill=(0x0A, 0x07, 0x05))
    # Stem.
    d.polygon([p(51.6, 27), p(56.4, 27), p(56.4, 41), p(51.6, 41)], fill=MAPLE_DARK)
    # Body.
    left = bez((50.5, 72), (40, 66), (34, 57), (35, 48))
    top_ = [(35 + (73 - 35) * t, 48 - 11 * 4 * t * (1 - t) * 0.5) for t in [i / 24 for i in range(25)]]
    right = bez((73, 48), (74, 57), (68, 66), (57.5, 72))
    body = [(54, 79)] + left + top_ + right
    d.polygon([p(*q) for q in body], fill=MAPLE)
    shade = [(57, 41.6), (66, 42.6), (73, 48)] + right + [(54, 79), (56, 72)] + bez((56, 72), (64, 64), (67, 55), (63, 44))
    d.polygon([p(*q) for q in shade], fill=(0xB8, 0x86, 0x52))
    # Stripes and brass rings.
    for pts, c, wd in [(bez((45, 47), (44, 56), (48, 65), (52.5, 71)), RED, 3), (bez((56, 46), (57, 55), (56, 64), (55, 71)), TEAL, 2.4),
                       (bez((66, 47.5), (66, 55), (63, 63), (57.5, 70.5)), RED, 1.6)]:
        d.line([p(*q) for q in pts], fill=c, width=int(wd * u), joint="curve")
    for y in (47.5, 44.5):
        x0, x1 = (36, 72) if y > 46 else (37.5, 70.5)
        ring = [(x0 + (x1 - x0) * t, y + 6.5 * 4 * t * (1 - t) * 0.5) for t in [i / 30 for i in range(31)]]
        d.line([p(*q) for q in ring], fill=BRASS, width=int(1.6 * u), joint="curve")
    d.polygon([p(54, 79.5), p(51.6, 74.5), p(56.4, 74.5)], fill=STEEL)

    mask = Image.new("L", (n, n), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, n - 1, n - 1], radius=int(n * 0.22), fill=255)
    out = Image.new("RGBA", (n, n), (0, 0, 0, 0))
    out.paste(im, (0, 0), mask)
    out = out.resize((S, S), Image.LANCZOS)
    (ROOT / "docs").mkdir(exist_ok=True)
    out.save(ROOT / "docs/logo.png")


if __name__ == "__main__":
    main()
