"""Draws the README logo, the launcher icon's walking bed under a crescent moon, as docs/logo.png.

python tools/icon.py
"""
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
S = 1024
K = 4  # supersampling

INK = (0x1B, 0x1A, 0x24)
WOOD = (0x9A, 0x6A, 0x42)
WOOD_DARK = (0x6E, 0x4A, 0x30)
SHEET = (0xFF, 0xFC, 0xF4)
RED = (0xB8, 0x37, 0x2A)
GOLD = (0xD9, 0xA3, 0x3A)


def main():
    n = S * K
    im = Image.new("RGBA", (n, n), (0x26, 0x30, 0x5A, 255))
    d = ImageDraw.Draw(im)
    u = n / 108

    def p(x, y):
        return (x * u, y * u)

    def w(v):
        return int(v * u)

    # Night, a far hill, stars, the moon (a disc with a bite taken out).
    d.polygon([p(x, 78 - 4 * (x / 108) + 6 * ((x / 54 - 1) ** 2)) for x in range(0, 109)] + [p(108, 108), p(0, 108)], fill=(0x3A, 0x46, 0x77))
    for x, y, r in [(28, 30, 1.2), (80, 24, 1), (86, 44, 1.2), (22, 50, 0.9), (50, 18, 0.8)]:
        d.ellipse([*p(x - r, y - r), *p(x + r, y + r)], fill=(0xFF, 0xF4, 0xC8))
    d.ellipse([*p(60, 26), *p(80, 46)], fill=(0xFF, 0xD9, 0x8A))
    d.ellipse([*p(65, 23), *p(83, 41)], fill=(0x26, 0x30, 0x5A))

    # Four legs mid-stride, the back pair darker.
    for legs, c in [([(36, 60), (31, 70), (35, 80)], WOOD_DARK), ([(70, 60), (76, 70), (72, 80)], WOOD_DARK),
                    ([(42, 60), (46, 70), (42, 80)], WOOD), ([(76, 60), (71, 70), (79, 80)], WOOD)]:
        pts = [p(*q) for q in legs]
        d.line(pts, fill=INK, width=w(4.4), joint="curve")
        d.line(pts, fill=c, width=w(2.6), joint="curve")
        fx, fy = pts[-1]
        d.ellipse([fx - w(2), fy - w(1), fx + w(2.6), fy + w(1.2)], fill=INK)

    # Headboard, knob, mattress, pillow, blanket, footboard.
    out = w(1.4)
    d.rectangle([*p(30, 42), *p(40, 61)], fill=WOOD, outline=INK, width=out)
    d.pieslice([*p(30, 36), *p(40, 48)], 180, 360, fill=WOOD, outline=INK, width=out)
    d.ellipse([*p(33, 35.5), *p(37, 39.5)], fill=GOLD, outline=INK, width=w(1))
    d.rectangle([*p(39, 50), *p(79, 61)], fill=SHEET, outline=INK, width=out)
    d.ellipse([*p(40, 44), *p(52, 52)], fill=SHEET, outline=INK, width=out)
    d.polygon([p(50, 49), p(64, 46), p(79, 48), p(79, 62), p(50, 62)], fill=RED, outline=INK, width=out)
    d.rectangle([*p(77, 46), *p(81, 61)], fill=WOOD, outline=INK, width=out)

    # Rounded square.
    mask = Image.new("L", (n, n), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, n - 1, n - 1], radius=int(n * 0.22), fill=255)
    result = Image.new("RGBA", (n, n), (0, 0, 0, 0))
    result.paste(im, (0, 0), mask)
    result = result.resize((S, S), Image.LANCZOS)
    (ROOT / "docs").mkdir(exist_ok=True)
    result.save(ROOT / "docs/logo.png")


if __name__ == "__main__":
    main()
