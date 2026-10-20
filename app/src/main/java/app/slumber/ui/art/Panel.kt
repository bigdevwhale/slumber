package app.slumber.ui.art

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import app.slumber.domain.Act
import app.slumber.domain.Base
import app.slumber.domain.Doodle
import app.slumber.domain.Hair
import app.slumber.domain.Kind
import app.slumber.domain.Look
import app.slumber.domain.Mood
import app.slumber.domain.Scene
import app.slumber.ui.theme.Ink
import app.slumber.ui.theme.Paper
import app.slumber.ui.theme.Tints
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Someone as a panel draws them: how they look, and whether they're a whole crowd or only a voice. */
@Immutable
data class Body(val look: Look, val kind: Kind? = null) {
    val voice get() = kind == Kind.VOICE
    val crowd get() = kind == Kind.CROWD
}

/** Everything one panel needs to be drawn. [seed] keeps its little randomness (clouds, books on shelves) the same every time. */
@Immutable
data class Shot(
    val scene: Scene,
    val act: Act,
    val mood: Mood,
    val bodies: List<Body>,
    val doodle: String = "",
    val seed: Int = 0,
)

/** The panel's outline: its shape says how the dream felt before the colour does. */
fun framePath(size: Size, mood: Mood, seed: Int, inset: Float): Path {
    val w = size.width
    val h = size.height
    val i = inset
    val r = Random(seed)
    val m = minOf(w, h)
    return Path().apply {
        when (mood) {
            Mood.JOY -> addRoundRect(androidx.compose.ui.geometry.RoundRect(i, i, w - i, h - i, m * 0.07f, m * 0.07f))
            Mood.WONDER -> {
                moveTo(i, h * 0.07f); quadraticTo(w / 2, -h * 0.05f + i, w - i, h * 0.07f)
                lineTo(w - i, h - i); lineTo(i, h - i); close()
            }
            Mood.FEAR -> {
                val j = { m * (0.01f + r.nextFloat() * 0.035f) }
                moveTo(i + j(), i + j()); lineTo(w - i - j(), i + j()); lineTo(w - i - j(), h - i - j()); lineTo(i + j(), h - i - j()); close()
            }
            Mood.ANXIETY -> {
                val s = m * 0.03f
                moveTo(i + s, i); lineTo(w - i, i); lineTo(w - i - s, h - i); lineTo(i, h - i); close()
            }
            Mood.ANGER -> {
                val n = 14
                val a = m * 0.014f
                moveTo(i, i)
                for (k in 1..n) lineTo(i + (w - 2 * i) * k / n, i + if (k % 2 == 1) a else 0f)
                for (k in 1..n) lineTo(w - i - if (k % 2 == 1) a else 0f, i + (h - 2 * i) * k / n)
                for (k in 1..n) lineTo(w - i - (w - 2 * i) * k / n, h - i - if (k % 2 == 1) a else 0f)
                for (k in 1..n) lineTo(i + if (k % 2 == 1) a else 0f, h - i - (h - 2 * i) * k / n)
                close()
            }
            Mood.STRANGE -> {
                val a = m * 0.012f
                val n = 48
                fun wv(k: Int) = a * sin(k * PI.toFloat() / 4f)
                moveTo(i, i)
                for (k in 1..n) lineTo(i + (w - 2 * i) * k / n, i + a + wv(k))
                for (k in 1..n) lineTo(w - i - a - wv(k), i + (h - 2 * i) * k / n)
                for (k in 1..n) lineTo(w - i - (w - 2 * i) * k / n, h - i - a - wv(k))
                for (k in 1..n) lineTo(i + a + wv(k), h - i - (h - 2 * i) * k / n)
                close()
            }
            else -> addRect(androidx.compose.ui.geometry.Rect(i, i, w - i, h - i))
        }
    }
}

/** A whole panel: scene, mood, figures, what they do, the finger drawing, the frame. */
fun DrawScope.panel(s: Shot, dim: Boolean = false) {
    val t = Tints.of(s.mood)
    val pen = Pen(this, t)
    val frame = framePath(size, s.mood, s.seed, pen.lw)
    clipPath(frame) {
        val p = Pen(this, t)
        drawRect(Paper)
        p.scene(s.scene, s.seed)
        p.moodBehind(s.mood, s.seed)
        p.cast(s)
        p.doodle(s.doodle)
        p.moodOver(s.mood, s.seed)
        if (dim) drawRect(Paper.copy(alpha = 0.55f))
    }
    drawPath(frame, Ink, style = Stroke(pen.lw * 2.2f))
}

/** Where everyone stands, and the props and marks that go with what they do. */
private fun Pen.cast(s: Shot) {
    val people = s.bodies.filter { !it.voice }
    val voice = s.bodies.any { it.voice }
    val base = if (w / h > 1.4f) 0.58f else 0.5f
    val main = people.firstOrNull()
    val n = people.size
    val xs = when (s.act) {
        Act.CHASED -> listOf(0.3f, 0.66f, 0.84f)
        Act.HUG -> listOf(0.44f, 0.58f, 0.8f)
        Act.FIGHT -> listOf(0.38f, 0.64f, 0.84f)
        else -> when (n) { 1 -> listOf(0.46f); 2 -> listOf(0.36f, 0.66f); else -> listOf(0.28f, 0.54f, 0.78f) }
    }
    // The dreamer faces the others; chased, everyone runs the same way.
    val facing = { i: Int -> if (s.act == Act.CHASED) -1 else if (i == 0) 1 else -1 }
    val ground = when (s.scene) { Scene.SKY -> 0.9f; else -> GROUND }
    val mainPose = Poses.of(s.act)
    val otherPose = Poses.of(Poses.others(s.act))
    val sizeOf = { i: Int -> if (i == 0) when (s.act) { Act.GROW -> 1.8f; Act.SHRINK -> 0.42f; else -> 1f } else 1f }

    // Props that stand behind people.
    if (main != null) when (s.act) {
        Act.SIT -> rect(xs[0] - 0.12f, ground - base * 0.26f, xs.take(n).last() + 0.12f, ground, Color(0xFF9A6A42))
        Act.OPEN -> door(xs[0] + 0.1f, ground - base * 1.15f, xs[0] + 0.26f, ground, t.accent, open = true)
        Act.FALL -> speed(true, s.seed)
        Act.FLY, Act.RUN, Act.CHASED -> speed(false, s.seed)
        else -> {}
    }

    people.forEachIndexed { i, b ->
        val tall = b.look.height(base) * sizeOf(i)
        val pose = if (i == 0) mainPose else otherPose
        val x = xs[i.coerceAtMost(xs.size - 1)]
        if (b.crowd) {
            for ((k, dx) in listOf(-0.07f, 0.07f, 0f).withIndex()) {
                figure(b.look.copy(coat = (b.look.coat + k * 3) % Coats.size, hair = listOf(Hair.HAT, Hair.SHORT, Hair.BUN)[k]), pose, x + dx, ground - 0.02f * (2 - k), tall * (0.8f + k * 0.06f), facing(i), s.mood)
            }
        } else {
            // In the air, the others fly a little higher so bodies don't pile up.
            val up = if (!pose.grounded && i > 0) i * 0.16f else 0f
            figure(b.look, pose, x, ground - up, tall, facing(i), s.mood)
        }
    }

    if (main == null) {
        if (voice) bubble(0.78f, 0.2f, 0.12f, false, toEdge = true)
        return
    }
    val mt = main.look.height(base) * sizeOf(0)
    val headY = (ground - mt * if (s.act == Act.SIT || s.act == Act.DRIVE) 0.78f else 1.02f).coerceAtLeast(0.12f)
    val hx = xs[0]
    when (s.act) {
        Act.DRIVE -> car(hx, ground, base)
        Act.SWIM, Act.SINK -> if (s.scene != Scene.UNDERWATER) waves(ground - mt * (if (s.act == Act.SINK) 0.72f else 0.3f), t.accent.mix(Color(0xFFBFDCE6), 0.4f).copy(alpha = 0.72f), 7, 0.02f)
        Act.TALK -> bubble(hx + 0.12f, headY - 0.1f, 0.1f, false)
        Act.SHOUT -> bubble(hx + 0.13f, headY - 0.1f, 0.12f, true)
        Act.SLEEP -> zzz(hx + 0.1f, ground - mt * 0.4f)
        Act.LOST -> { ask(hx - 0.1f, headY - 0.06f, 0.05f); ask(hx + 0.1f, headY - 0.1f, 0.06f) }
        Act.SEARCH -> { ask(hx + 0.12f, headY - 0.06f, 0.05f); lamp(hx + 0.17f, headY + mt * 0.42f) }
        Act.CRY -> tears(hx, headY + mt * 0.08f, mt)
        Act.LAUGH -> burst(hx, headY, mt * 0.4f, 5, 0.8f)
        Act.FROZEN -> { shiver(hx - mt * 0.4f, ground - mt * 0.5f, mt); shiver(hx + mt * 0.4f, ground - mt * 0.5f, mt) }
        Act.FIGHT -> if (n > 1) star((xs[0] + xs[1]) / 2, ground - mt * 0.7f, 0.06f, Color(0xFFFFD166))
        Act.HUG -> heart((xs[0] + xs[1]) / 2, headY - 0.06f, 0.035f)
        Act.DANCE -> { note(hx - 0.14f, headY, 0.04f); note(hx + 0.16f, headY - 0.08f, 0.035f) }
        Act.GROW, Act.SHRINK -> burst(hx, headY, mt * 0.3f, 6, 0.6f)
        else -> {}
    }
    if (voice) bubble(0.86f, 0.16f, 0.09f, false, toEdge = true)
}

private fun Pen.car(x: Float, ground: Float, base: Float) {
    val top = ground - base * 0.42f
    shape(t.accent) {
        moveTo((x - 0.26f) * w, ground * h - base * 0.08f * h)
        lineTo((x - 0.26f) * w, (top + base * 0.12f) * h)
        lineTo((x + 0.1f) * w, (top + base * 0.12f) * h)
        lineTo((x + 0.2f) * w, (top + base * 0.2f) * h)
        lineTo((x + 0.28f) * w, (top + base * 0.22f) * h)
        lineTo((x + 0.28f) * w, ground * h - base * 0.08f * h)
        close()
    }
    line(x + 0.12f, top - base * 0.05f, x + 0.18f, top + base * 0.18f, 1.2f)
    circle(x - 0.16f, ground - base * 0.07f, 0.05f, Ink.mix(Color.Gray, 0.3f))
    circle(x + 0.18f, ground - base * 0.07f, 0.05f, Ink.mix(Color.Gray, 0.3f))
}

/** A speech bubble, round for talk, spiky for a shout; [toEdge]: the speaker is outside the panel. */
private fun Pen.bubble(x: Float, y: Float, r: Float, shout: Boolean, toEdge: Boolean = false) {
    val c = o(x, y)
    val rr = r * w
    val p = Path()
    if (shout) {
        for (k in 0 until 18) {
            val a = 2 * PI * k / 18
            val rad = if (k % 2 == 0) rr else rr * 0.72f
            val px = c.x + (cos(a) * rad * 1.25f).toFloat()
            val py = c.y + (sin(a) * rad * 0.8f).toFloat()
            if (k == 0) p.moveTo(px, py) else p.lineTo(px, py)
        }
        p.close()
    } else {
        p.addOval(androidx.compose.ui.geometry.Rect(c.x - rr * 1.25f, c.y - rr * 0.8f, c.x + rr * 1.25f, c.y + rr * 0.8f))
        val tip = if (toEdge) Offset(w, c.y + rr * 0.4f) else Offset(c.x - rr * 1.1f, c.y + rr * 1.3f)
        p.moveTo(c.x - rr * 0.3f, c.y + rr * 0.6f)
        p.lineTo(tip.x, tip.y)
        p.lineTo(c.x + rr * 0.2f, c.y + rr * 0.7f)
    }
    d.drawPath(p, Color.White)
    outline(p)
    for (k in -1..1) d.drawCircle(Ink, rr * 0.1f, Offset(c.x + k * rr * 0.4f, c.y))
}

private fun Pen.ask(x: Float, y: Float, s: Float) {
    val c = o(x, y)
    val r = s * w * 0.5f
    val p = Path().apply {
        moveTo(c.x - r, c.y - r * 0.3f)
        cubicTo(c.x - r, c.y - r * 1.6f, c.x + r * 1.1f, c.y - r * 1.6f, c.x + r, c.y - r * 0.5f)
        cubicTo(c.x + r * 0.9f, c.y + r * 0.2f, c.x, c.y + r * 0.2f, c.x, c.y + r)
    }
    d.drawPath(p, Ink, style = Stroke(lw * 1.6f, cap = StrokeCap.Round))
    d.drawCircle(Ink, lw * 1.1f, Offset(c.x, c.y + r * 1.6f))
}

private fun Pen.zzz(x: Float, y: Float) {
    for (k in 0..2) {
        val s = (0.025f + k * 0.012f)
        val cx = x + k * 0.05f
        val cy = y - k * 0.07f
        polyline(Ink, 1f, cx, cy, cx + s, cy, cx, cy + s * w / h, cx + s, cy + s * w / h)
    }
}

private fun Pen.tears(x: Float, y: Float, tall: Float) {
    for (dx in listOf(-0.03f, 0.03f)) {
        val c = o(x + dx, y + tall * 0.06f)
        val r = tall * h * 0.025f
        val p = Path().apply {
            moveTo(c.x, c.y - r * 2)
            quadraticTo(c.x + r * 1.2f, c.y, c.x, c.y + r)
            quadraticTo(c.x - r * 1.2f, c.y, c.x, c.y - r * 2)
        }
        d.drawPath(p, Color(0xFF8FC3E6))
        outline(p, 0.7f)
    }
}

private fun Pen.burst(x: Float, y: Float, r: Float, n: Int, weight: Float) {
    for (k in 0 until n) {
        val a = -PI / 2 + (k - (n - 1) / 2f) * 0.45
        val c = o(x, y)
        val r0 = r * h * 0.55f
        val r1 = r * h * 0.85f
        d.drawLine(Ink, Offset(c.x + (cos(a) * r0).toFloat(), c.y + (sin(a) * r0).toFloat()), Offset(c.x + (cos(a) * r1).toFloat(), c.y + (sin(a) * r1).toFloat()), lw * weight * 1.4f, StrokeCap.Round)
    }
}

private fun Pen.shiver(x: Float, y: Float, tall: Float) {
    for (k in -1..1) {
        val c = o(x, y + k * tall * 0.12f)
        val s = tall * h * 0.05f
        val p = Path().apply { moveTo(c.x - s, c.y); quadraticTo(c.x - s / 2, c.y - s, c.x, c.y); quadraticTo(c.x + s / 2, c.y + s, c.x + s, c.y) }
        d.drawPath(p, Ink, style = Stroke(lw * 0.9f, cap = StrokeCap.Round))
    }
}

private fun Pen.heart(x: Float, y: Float, s: Float) = shape(t.accent.mix(Color(0xFFE08AA6), 0.5f)) {
    val c = o(x, y)
    val r = s * w
    moveTo(c.x, c.y + r)
    cubicTo(c.x - r * 1.6f, c.y - r * 0.2f, c.x - r * 0.6f, c.y - r * 1.4f, c.x, c.y - r * 0.4f)
    cubicTo(c.x + r * 0.6f, c.y - r * 1.4f, c.x + r * 1.6f, c.y - r * 0.2f, c.x, c.y + r)
    close()
}

private fun Pen.note(x: Float, y: Float, s: Float) {
    val c = o(x, y)
    val r = s * w
    d.drawOval(Ink, Offset(c.x - r * 0.5f, c.y), Size(r, r * 0.7f))
    d.drawLine(Ink, Offset(c.x + r * 0.45f, c.y + r * 0.3f), Offset(c.x + r * 0.45f, c.y - r * 1.4f), lw * 1.2f)
    d.drawLine(Ink, Offset(c.x + r * 0.45f, c.y - r * 1.4f), Offset(c.x + r * 1.1f, c.y - r * 1f), lw * 1.2f)
}

private fun Pen.lamp(x: Float, y: Float) {
    val c = o(x, y)
    d.drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF1B0), Color.Transparent), c, w * 0.12f), w * 0.12f, c)
    circle(x, y, 0.022f, Color(0xFFFFE58A))
}

/** Before the figures: rain, a menace in the dark, rays. */
private fun Pen.moodBehind(m: Mood, seed: Int) {
    val r = Random(seed + 7)
    when (m) {
        Mood.ANGER -> for (k in 0 until 16) {
            val a = 2 * PI * k / 16
            line(0.5f, 0.45f, 0.5f + (cos(a) * 0.9f).toFloat(), 0.45f + (sin(a) * 0.9f).toFloat(), 0.6f, t.accent.copy(alpha = 0.35f))
        }
        Mood.WONDER -> repeat(6) { star(r.nextFloat(), r.nextFloat() * 0.5f, 0.014f + r.nextFloat() * 0.01f, t.accent, line = false) }
        Mood.STRANGE -> repeat(3) {
            val x = 0.1f + r.nextFloat() * 0.8f
            val y = 0.08f + r.nextFloat() * 0.3f
            oval(x - 0.04f, y - 0.025f, x + 0.04f, y + 0.025f, Color.White)
            circle(x, y, 0.012f, t.accent)
        }
        Mood.ANXIETY -> {
            circle(0.88f, 0.12f, 0.06f, Paper)
            line(0.88f, 0.12f, 0.88f, 0.07f)
            line(0.88f, 0.12f, 0.92f, 0.14f)
        }
        else -> {}
    }
}

/** After the figures: the dark closing in, rain over everything. */
private fun Pen.moodOver(m: Mood, seed: Int) {
    val r = Random(seed + 11)
    when (m) {
        Mood.FEAR -> {
            d.drawRect(Brush.radialGradient(listOf(Color.Transparent, t.deep.copy(alpha = 0.75f)), center = o(0.5f, 0.55f), radius = maxOf(w, h) * 0.7f))
            for (x in listOf(0.08f, 0.9f)) {
                val y = 0.2f + r.nextFloat() * 0.3f
                d.drawCircle(Color(0xFFFFD166), w * 0.008f, o(x - 0.012f, y))
                d.drawCircle(Color(0xFFFFD166), w * 0.008f, o(x + 0.012f, y))
            }
        }
        Mood.SADNESS -> repeat(28) {
            val x = r.nextFloat()
            val y = r.nextFloat()
            line(x, y, x - 0.015f, y + 0.06f, 0.5f, Color(0xFFDDE6F0).copy(alpha = 0.8f))
        }
        Mood.JOY -> repeat(10) {
            val x = r.nextFloat()
            val y = r.nextFloat() * 0.6f
            rect(x, y, x + 0.012f, y + 0.012f, listOf(t.accent, Color(0xFF2F5D9A), Color(0xFF4E8B4A))[r.nextInt(3)], line = false)
        }
        else -> {}
    }
}

/** The finger drawing, inked over: smooth curves, a firm line, shapes filled. */
fun Pen.doodle(s: String, color: Color = t.accent.mix(Paper, 0.25f)) {
    for (stroke in Doodle.decode(s)) {
        val p = Path()
        val pts = stroke.map { o(it.x, it.y) }
        p.moveTo(pts[0].x, pts[0].y)
        for (k in 1 until pts.size) {
            val a = pts[k - 1]
            val b = pts[k]
            p.quadraticTo(a.x, a.y, (a.x + b.x) / 2, (a.y + b.y) / 2)
        }
        p.lineTo(pts.last().x, pts.last().y)
        if (Doodle.closed(stroke)) {
            p.close()
            d.drawPath(p, color)
        }
        d.drawPath(p, Ink, style = Stroke(lw * 1.5f, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    }
}

/**
 * The last panel of every page, as in Slumberland: you in your own bed, awake. After a dark dream you sit
 * bolt upright; after a good one you stretch.
 */
fun DrawScope.waking(me: Look, dark: Boolean) {
    val t = Tints.Morning
    val pen = Pen(this, t)
    val frame = framePath(size, Mood.CALM, 0, pen.lw)
    clipPath(frame) {
        val p = Pen(this, t)
        drawRect(Paper)
        p.bedroom()
        val bx0 = 0.14f
        val bx1 = 0.86f
        // Headboard, the figure sitting up, the blanket over the legs.
        p.rect(bx0 - 0.02f, 0.3f, bx0 + 0.04f, 0.94f, Color(0xFF9A6A42))
        p.oval(bx0 + 0.03f, 0.5f, bx0 + 0.2f, 0.64f, Color(0xFFFFFCF4))
        val pose = if (dark) Poses.of(Act.SIT).copy(bu = -20f, bl = 20f, fu = 40f, fl = 30f) else Poses.of(Act.SIT).copy(bu = -160f, bl = -10f, fu = 165f, fl = 10f, shut = true)
        p.figure(me.copy(base = Base.HUMAN), pose, bx0 + 0.2f, 0.84f, 0.78f, 1, if (dark) Mood.FEAR else Mood.JOY)
        p.rect(bx0 + 0.14f, 0.68f, bx1, 0.88f, t.accent.mix(Paper, 0.35f))
        p.rect(bx0 - 0.02f, 0.87f, bx1 + 0.02f, 0.94f, Color(0xFF9A6A42))
        p.rect(bx1 - 0.02f, 0.6f, bx1 + 0.03f, 0.94f, Color(0xFF9A6A42))
        p.line(bx0 + 0.34f, 0.72f, bx1 - 0.04f, 0.76f, 0.5f)
        if (dark) { p.burst(bx0 + 0.2f, 0.2f, 0.25f, 5, 0.8f) }
        else p.circle(0.72f, 0.24f, 0.035f, Color(0xFFFFD98A))
    }
    drawPath(frame, Ink, style = Stroke(pen.lw * 2.2f))
}

/** Scene alone in a round medallion, for the atlas. */
fun DrawScope.medallion(scene: Scene, mood: Mood, seed: Int) {
    val t = Tints.of(mood)
    val c = Offset(size.width / 2, size.height / 2)
    val r = minOf(size.width, size.height) / 2
    val circle = Path().apply { addOval(androidx.compose.ui.geometry.Rect(c, r)) }
    clipPath(circle) {
        drawRect(Paper)
        Pen(this, t).scene(scene, seed)
    }
    drawCircle(Ink, r - 1.5f, c, style = Stroke(3f))
}
