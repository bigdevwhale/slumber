package app.slumber.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/** Screenshots and renders want a still picture: the top holds one moment of its spin. */
object Art {
    var still: Boolean = app.slumber.BuildConfig.SHOTS
}

/**
 * One moment of the top. [tilt] from upright in degrees, [precession] the direction it leans (radians),
 * [turn] how far it has spun (radians), [rings] brass rings for lucid dreams, [glow] the gold light of a
 * day after a lucid dream.
 */
data class TopPose(
    val tilt: Float = 3f,
    val precession: Float = 0f,
    val turn: Float = 0f,
    val rings: Int = 0,
    val glow: Float = 0f,
)

private val Maple = Color(0xFFD9A86C)
private val MapleDark = Color(0xFF9C6B3C)
private val Paint1 = Color(0xFFB8432E)
private val Paint2 = Color(0xFF2E6E73)
private val Brass = Color(0xFFF1C66C)
private val Steel = Color(0xFFB9B7B0)
private val Line = Color(0xFF1A120C)

/** The top's side profile: radius (as a share of its height) at a point along its axis, tip 0 to stem 1. */
private fun radius(u: Float): Float = when {
    u < 0.07f -> 0.03f * u / 0.07f + 0.01f
    u < 0.6f -> { val f = (u - 0.07f) / 0.53f; 0.04f + 0.31f * sin(f * PI.toFloat() / 2).let { it * sqrt(it) } }
    u < 0.78f -> { val f = (u - 0.6f) / 0.18f; 0.35f * sqrt(1 - f * f * 0.75f) - 0.08f * f }
    else -> 0.065f
}

/**
 * The night desk: a dim wall, a window with the moon, the lamp's warm pool on dark walnut. The top stands
 * at ([cx], [table]) as shares of the canvas.
 */
fun DrawScope.desk(cx: Float = 0.5f, table: Float = 0.7f, seed: Int = 3) {
    val w = size.width
    val h = size.height
    drawRect(Brush.verticalGradient(listOf(Color(0xFF120D0A), Color(0xFF1F1610)), endY = table * h), size = Size(w, table * h))
    // A window, top right: night blue, a moon, a few stars, the frame's cross.
    val wx0 = w * 0.64f
    val wy0 = h * 0.08f
    val ww = w * 0.27f
    val wh = h * 0.3f
    drawRect(Color(0xFF2A1E15), Offset(wx0 - 6f, wy0 - 6f), Size(ww + 12f, wh + 12f))
    drawRect(Brush.verticalGradient(listOf(Color(0xFF1B2741), Color(0xFF2E3D5E)), startY = wy0, endY = wy0 + wh), Offset(wx0, wy0), Size(ww, wh))
    val r = Random(seed)
    repeat(9) { drawCircle(Color(0xFFF4E9C8), 1.4f + r.nextFloat() * 1.6f, Offset(wx0 + r.nextFloat() * ww, wy0 + r.nextFloat() * wh * 0.7f)) }
    val moon = Offset(wx0 + ww * 0.68f, wy0 + wh * 0.3f)
    drawCircle(Color(0xFFF4E2B0), ww * 0.11f, moon)
    drawCircle(Color(0xFF1F2B47), ww * 0.1f, moon + Offset(ww * 0.05f, -ww * 0.03f))
    drawLine(Color(0xFF2A1E15), Offset(wx0 + ww / 2, wy0), Offset(wx0 + ww / 2, wy0 + wh), 5f)
    drawLine(Color(0xFF2A1E15), Offset(wx0, wy0 + wh / 2), Offset(wx0 + ww, wy0 + wh / 2), 5f)

    // The tabletop and its grain.
    drawRect(Brush.verticalGradient(listOf(Color(0xFF4A3220), Color(0xFF2A1B10)), startY = table * h, endY = h), Offset(0f, table * h), Size(w, h - table * h))
    val g = Random(seed + 1)
    repeat(14) {
        val y = table * h + g.nextFloat() * (h - table * h)
        val p = Path().apply {
            moveTo(0f, y)
            cubicTo(w * 0.3f, y - 6f + g.nextFloat() * 12f, w * 0.6f, y - 6f + g.nextFloat() * 12f, w, y + g.nextFloat() * 8f)
        }
        drawPath(p, Color(0xFF1E130A).copy(alpha = 0.35f), style = Stroke(1.2f + g.nextFloat()))
    }
    drawLine(Color(0xFF5E4029), Offset(0f, table * h), Offset(w, table * h), 2.5f)

    // The lamp's pool of light, from off the top left.
    drawRect(Brush.radialGradient(listOf(Color(0xFFF2B35E).copy(alpha = 0.32f), Color.Transparent), Offset(w * 0.28f, table * h), w * 0.75f))
    drawRect(Brush.radialGradient(listOf(Color(0xFFF2B35E).copy(alpha = 0.18f), Color.Transparent), Offset(w * 0.05f, 0f), w * 0.9f))
    // A dark edge so the eye rests on the middle.
    drawRect(Brush.radialGradient(listOf(Color.Transparent, Color(0xFF0A0705).copy(alpha = 0.7f)), Offset(w * 0.5f, h * 0.55f), maxOf(w, h) * 0.75f))
}

/** The top itself, its tip at ([cx], [cy]) (shares of the canvas), [tall] a share of the canvas height. */
fun DrawScope.top(p: TopPose, cx: Float = 0.5f, cy: Float = 0.8f, tall: Float = 0.38f) {
    val w = size.width
    val h = size.height
    val pivot = Offset(cx * w, cy * h)
    val H = tall * h
    val t = Math.toRadians(p.tilt.toDouble()).toFloat()
    // The axis, leaning by tilt toward precession; leaning to or from you foreshortens it a little.
    val ax = sin(t) * cos(p.precession)
    val ay = -cos(t) + sin(t) * sin(p.precession) * 0.28f
    val al = sqrt(ax * ax + ay * ay)
    val a = Offset(ax / al, ay / al)
    val n = Offset(-a.y, a.x)
    fun at(u: Float, v: Float) = pivot + a * (u * H) + n * (v * H)

    // Shadow on the table, thrown away from the lamp.
    val sw = H * (0.42f + 0.6f * sin(t))
    drawOval(Color.Black.copy(alpha = 0.45f), Offset(pivot.x - sw * 0.3f, pivot.y - H * 0.03f), Size(sw, H * 0.08f))

    // The day after a lucid dream: a warm halo, and a ring of light on the table round the tip.
    if (p.glow > 0f) {
        val c = at(0.45f, 0f)
        drawCircle(Brush.radialGradient(listOf(Brass.copy(alpha = 0.75f * p.glow), Brass.copy(alpha = 0.25f * p.glow), Color.Transparent), c, H * 1.25f), H * 1.25f, c)
        drawOval(Brush.radialGradient(listOf(Brass.copy(alpha = 0.6f * p.glow), Color.Transparent), pivot, H * 0.7f), Offset(pivot.x - H * 0.7f, pivot.y - H * 0.12f), Size(H * 1.4f, H * 0.24f))
    }

    val us = (0..60).map { it / 60f }
    val outline = Path().apply {
        us.forEachIndexed { i, u -> val q = at(u, -radius(u)); if (i == 0) moveTo(q.x, q.y) else lineTo(q.x, q.y) }
        us.reversed().forEach { u -> val q = at(u, radius(u)); lineTo(q.x, q.y) }
        close()
    }
    // Body in maple, lit from the left.
    val l = at(0.5f, -0.35f)
    val rgt = at(0.5f, 0.35f)
    drawPath(outline, Brush.linearGradient(listOf(lerp(Maple, Color.White, 0.18f), Maple, MapleDark), l, rgt))

    // Painted stripes running tip to crown: the ones facing you, wider the more they face you. Their
    // sideways drift is what makes the top read as spinning.
    for (k in 0 until 8) {
        val th = p.turn + k * (PI.toFloat() / 4)
        val facing = cos(th)
        if (facing <= 0.05f) continue
        val path = Path()
        var first = true
        for (u in us) {
            if (u < 0.1f || u > 0.6f) continue
            val q = at(u, sin(th) * radius(u))
            if (first) { path.moveTo(q.x, q.y); first = false } else path.lineTo(q.x, q.y)
        }
        drawPath(path, (if (k % 2 == 0) Paint1 else Paint2).copy(alpha = 0.55f + 0.45f * facing), style = Stroke(H * 0.035f * facing, cap = StrokeCap.Round))
    }
    // Shade over the stripes too, so they turn round the body.
    drawPath(outline, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.12f), Color.Transparent, Color.Black.copy(alpha = 0.35f)), l, rgt))

    // Rings round the shoulder: a dark groove, then a brass ring for each lucid dream.
    fun band(u: Float, c: Color, weight: Float) {
        val r = radius(u)
        val e = at(u, -r)
        val f = at(u, r)
        val mid = at(u, 0f) - a * (r * H * 0.22f)
        val b = Path().apply { moveTo(e.x, e.y); quadraticTo(2 * mid.x - (e.x + f.x) / 2, 2 * mid.y - (e.y + f.y) / 2, f.x, f.y) }
        drawPath(b, c, style = Stroke(weight, cap = StrokeCap.Round))
    }
    band(0.6f, Line.copy(alpha = 0.6f), H * 0.012f)
    val rings = p.rings.coerceAtMost(7)
    for (i in 0 until rings) band(0.62f + i * 0.022f, Brass, H * 0.012f)

    // Steel tip, the stem with its knob.
    val tip = Path().apply {
        val q0 = at(0f, 0f); val q1 = at(0.08f, -0.04f); val q2 = at(0.08f, 0.04f)
        moveTo(q0.x, q0.y); lineTo(q1.x, q1.y); lineTo(q2.x, q2.y); close()
    }
    drawPath(tip, Steel)
    drawLine(MapleDark, at(0.77f, 0f), at(0.98f, 0f), H * 0.12f, StrokeCap.Round)
    drawLine(lerp(Maple, Color.White, 0.2f), at(0.78f, -0.02f), at(0.97f, -0.02f), H * 0.03f, StrokeCap.Round)
    drawPath(outline, Line.copy(alpha = 0.55f), style = Stroke(H * 0.008f))
}
