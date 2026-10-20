package app.slumber.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import app.slumber.ui.theme.Ink
import app.slumber.ui.theme.Tints
import kotlin.math.min

/**
 * McCay's way of drawing, as a tiny pen: flat colour, then a firm black outline round it. Coordinates are
 * fractions of the panel (0..1 across, 0..1 down), so every scene draws the same at any size.
 */
class Pen(val d: DrawScope, val t: Tints) {
    val w = d.size.width
    val h = d.size.height

    /** Outline weight, a little heavier on bigger panels. */
    val lw = (min(w, h) * 0.011f).coerceAtLeast(1.1f)

    fun o(x: Float, y: Float) = Offset(x * w, y * h)

    private val stroke get() = Stroke(width = lw, cap = StrokeCap.Round, join = StrokeJoin.Round)

    fun fill(c: Color) = d.drawRect(c)

    /** Sky or wall from one colour at the top to another at [to] (and beyond). */
    fun grad(top: Color, bottom: Color, from: Float = 0f, to: Float = 1f, y0: Float = 0f, y1: Float = 1f) =
        d.drawRect(Brush.verticalGradient(listOf(top, bottom), startY = from * h, endY = to * h), topLeft = o(0f, y0), size = Size(w, (y1 - y0) * h))

    fun rect(x0: Float, y0: Float, x1: Float, y1: Float, c: Color, line: Boolean = true) {
        d.drawRect(c, o(x0, y0), Size((x1 - x0) * w, (y1 - y0) * h))
        if (line) d.drawRect(Ink, o(x0, y0), Size((x1 - x0) * w, (y1 - y0) * h), style = stroke)
    }

    fun poly(c: Color, vararg xy: Float, line: Boolean = true) = shape(c, line) {
        moveTo(xy[0] * w, xy[1] * h)
        var i = 2
        while (i < xy.size) { lineTo(xy[i] * w, xy[i + 1] * h); i += 2 }
        close()
    }

    /** A circle; [r] is a fraction of the width so it stays round. */
    fun circle(x: Float, y: Float, r: Float, c: Color, line: Boolean = true) {
        d.drawCircle(c, r * w, o(x, y))
        if (line) d.drawCircle(Ink, r * w, o(x, y), style = stroke)
    }

    fun oval(x0: Float, y0: Float, x1: Float, y1: Float, c: Color, line: Boolean = true) {
        d.drawOval(c, o(x0, y0), Size((x1 - x0) * w, (y1 - y0) * h))
        if (line) d.drawOval(Ink, o(x0, y0), Size((x1 - x0) * w, (y1 - y0) * h), style = stroke)
    }

    fun line(x0: Float, y0: Float, x1: Float, y1: Float, weight: Float = 1f, c: Color = Ink) =
        d.drawLine(c, o(x0, y0), o(x1, y1), strokeWidth = lw * weight, cap = StrokeCap.Round)

    fun polyline(c: Color = Ink, weight: Float = 1f, vararg xy: Float) {
        val p = Path().apply {
            moveTo(xy[0] * w, xy[1] * h)
            var i = 2
            while (i < xy.size) { lineTo(xy[i] * w, xy[i + 1] * h); i += 2 }
        }
        d.drawPath(p, c, style = Stroke(lw * weight, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }

    /** Any path, in pixels, filled and outlined. */
    fun shape(c: Color, line: Boolean = true, build: Path.() -> Unit) {
        val p = Path().apply(build)
        d.drawPath(p, c)
        if (line) d.drawPath(p, Ink, style = stroke)
    }

    fun outline(p: Path, weight: Float = 1f, c: Color = Ink) =
        d.drawPath(p, c, style = Stroke(lw * weight, cap = StrokeCap.Round, join = StrokeJoin.Round))

    /** A puffy cloud: three bumps on a flat bottom. */
    fun cloud(x: Float, y: Float, s: Float, c: Color = Color(0xFFFFFBF2), line: Boolean = true) = shape(c, line) {
        val r = s * w
        moveTo(x * w - r * 1.6f, y * h)
        cubicTo(x * w - r * 1.9f, y * h - r * 0.9f, x * w - r * 0.9f, y * h - r * 1.3f, x * w - r * 0.6f, y * h - r * 0.9f)
        cubicTo(x * w - r * 0.4f, y * h - r * 1.9f, x * w + r * 0.9f, y * h - r * 1.9f, x * w + r * 0.8f, y * h - r * 0.9f)
        cubicTo(x * w + r * 1.4f, y * h - r * 1.4f, x * w + r * 2.1f, y * h - r * 0.4f, x * w + r * 1.6f, y * h)
        close()
    }

    /** A tree: trunk and a round crown. */
    fun tree(x: Float, base: Float, tall: Float, crown: Color, trunk: Color = Color(0xFF7A5234)) {
        rect(x - tall * 0.05f, base - tall * 0.55f, x + tall * 0.05f, base, trunk)
        oval(x - tall * 0.28f, base - tall, x + tall * 0.28f, base - tall * 0.42f, crown)
    }

    /** A pine: three stacked triangles. */
    fun pine(x: Float, base: Float, tall: Float, c: Color) {
        rect(x - tall * 0.04f, base - tall * 0.2f, x + tall * 0.04f, base, Color(0xFF6A4630))
        for (i in 0..2) {
            val top = base - tall + i * tall * 0.22f
            val wd = tall * (0.16f + i * 0.07f)
            poly(c, x, top, x + wd, top + tall * 0.42f, x - wd, top + tall * 0.42f)
        }
    }

    /** Sea waves: a band of scallops at [y]. */
    fun waves(y: Float, c: Color, n: Int = 8, amp: Float = 0.025f, bottom: Float = 1f) = shape(c) {
        moveTo(0f, bottom * h)
        lineTo(0f, y * h)
        val step = w / n
        for (i in 0 until n) {
            val x0 = i * step
            quadraticTo(x0 + step / 2, (y - amp * 2) * h, x0 + step, y * h)
        }
        lineTo(w, bottom * h)
        close()
    }

    /** Rolling hills along [y]. */
    fun hills(y: Float, c: Color, amp: Float = 0.06f, phase: Float = 0f) = shape(c) {
        moveTo(0f, h)
        lineTo(0f, y * h)
        val n = 40
        for (i in 0..n) {
            val f = i / n.toFloat()
            lineTo(f * w, (y - amp * kotlin.math.sin((f * 2.2f + phase) * Math.PI).toFloat().let { it * it }) * h)
        }
        lineTo(w, h)
        close()
    }

    fun stars(seed: Int, n: Int, top: Float = 0f, bottom: Float = 0.6f, c: Color = Color(0xFFFFF4C8)) {
        val r = kotlin.random.Random(seed)
        repeat(n) {
            val x = r.nextFloat()
            val y = top + r.nextFloat() * (bottom - top)
            val s = (0.004f + r.nextFloat() * 0.006f) * w
            d.drawCircle(c, s, o(x, y))
        }
    }

    /** A five-point star, McCay's favourite sky ornament. */
    fun star(x: Float, y: Float, r: Float, c: Color, line: Boolean = true) = shape(c, line) {
        for (i in 0 until 10) {
            val a = Math.PI * i / 5 - Math.PI / 2
            val rr = if (i % 2 == 0) r * w else r * w * 0.45f
            val px = x * w + (rr * kotlin.math.cos(a)).toFloat()
            val py = y * h + (rr * kotlin.math.sin(a)).toFloat()
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }

    /** A door, with its knob; [open] shows the dark beyond and the door swung in. */
    fun door(x0: Float, y0: Float, x1: Float, y1: Float, c: Color, open: Boolean = false, beyond: Color = t.deep) {
        if (open) {
            rect(x0, y0, x1, y1, beyond)
            poly(c, x0, y0, x0 + (x1 - x0) * 0.35f, y0 + (y1 - y0) * 0.06f, x0 + (x1 - x0) * 0.35f, y1 - (y1 - y0) * 0.04f, x0, y1)
        } else {
            rect(x0, y0, x1, y1, c)
            rect(x0 + (x1 - x0) * 0.18f, y0 + (y1 - y0) * 0.1f, x1 - (x1 - x0) * 0.18f, y0 + (y1 - y0) * 0.42f, c.mix(Color.Black, 0.12f))
            circle(x1 - (x1 - x0) * 0.2f, y0 + (y1 - y0) * 0.55f, (x1 - x0) * 0.06f, Color(0xFFD9A33A))
        }
    }

    /** A window onto [sky], with its cross. */
    fun window(x0: Float, y0: Float, x1: Float, y1: Float, sky: Color = t.sky, frame: Color = Color(0xFFF4EAD5)) {
        rect(x0, y0, x1, y1, frame)
        val m = (x1 - x0) * 0.08f
        d.drawRect(Brush.verticalGradient(listOf(sky, t.sky2), startY = y0 * h, endY = y1 * h), o(x0 + m, y0 + m), Size((x1 - x0 - 2 * m) * w, (y1 - y0 - 2 * m) * h))
        line((x0 + x1) / 2, y0 + m, (x0 + x1) / 2, y1 - m, 0.8f)
        line(x0 + m, (y0 + y1) / 2, x1 - m, (y0 + y1) / 2, 0.8f)
    }

    /**
     * A room seen from its open side: back wall [bx0..bx1]×[by0..by1], side walls, floor and ceiling
     * running out to the panel's edges.
     */
    fun room(bx0: Float, by0: Float, bx1: Float, by1: Float, wall: Color = t.wall, floor: Color = t.ground) {
        poly(wall.mix(Color.Black, 0.18f), 0f, 0f, bx0, by0, bx0, by1, 0f, 1f)
        poly(wall.mix(Color.Black, 0.12f), 1f, 0f, bx1, by0, bx1, by1, 1f, 1f)
        poly(wall.mix(Color.White, 0.15f), 0f, 0f, 1f, 0f, bx1, by0, bx0, by0)
        poly(floor, 0f, 1f, bx0, by1, bx1, by1, 1f, 1f)
        rect(bx0, by0, bx1, by1, wall)
    }
}

fun Color.mix(o: Color, f: Float): Color = lerp(this, o, f)

/** Runs [block] with a [Pen] over this whole draw scope. */
inline fun DrawScope.pen(t: Tints, block: Pen.() -> Unit) = Pen(this, t).block()
