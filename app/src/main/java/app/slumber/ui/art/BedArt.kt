package app.slumber.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import app.slumber.ui.theme.Ink
import app.slumber.ui.theme.Tints
import kotlin.math.sin

/**
 * Nemo's bed, which in one famous Sunday page grew long legs and walked out over the city. Here it grows
 * them with mornings remembered: [stage] 0 sleeps on stubs, 4 strides on stilts. [phase] 0..1 swings the
 * legs. Drawn into the whole draw scope, bed at the bottom.
 */
fun DrawScope.walkingBed(stage: Int, phase: Float, asleep: Boolean = stage == 0) {
    val p = Pen(this, Tints.Morning)
    val w = size.width
    val h = size.height
    val legLen = listOf(0.04f, 0.14f, 0.24f, 0.34f, 0.46f)[stage.coerceIn(0, 4)] * h
    val bedH = h * 0.32f
    val bedW = w * 0.7f
    val x0 = (w - bedW) / 2
    val top = h - legLen - bedH - h * 0.03f
    val base = top + bedH * 0.72f
    val wood = Color(0xFF9A6A42)
    val swing = if (stage == 0) 0f else sin(phase * 2 * Math.PI).toFloat()

    // Legs: two back ones paler, two front ones; each a thigh and shin with a little foot.
    val hips = listOf(x0 + bedW * 0.12f to true, x0 + bedW * 0.86f to true, x0 + bedW * 0.2f to false, x0 + bedW * 0.94f to false)
    for ((i, hp) in hips.withIndex()) {
        val (hx, back) = hp
        val s = if (i % 2 == 0) swing else -swing
        val knee = Offset(hx + s * legLen * 0.25f + legLen * 0.08f, base + legLen * 0.5f)
        val foot = Offset(hx + s * legLen * 0.35f, h - h * 0.03f)
        val c = if (back) wood.mix(Ink, 0.3f) else wood
        val lw = (w * 0.028f).coerceAtLeast(3f)
        if (stage == 0) {
            drawLine(Ink, Offset(hx, base), Offset(hx, h - h * 0.03f), lw + p.lw * 2, StrokeCap.Round)
            drawLine(c, Offset(hx, base), Offset(hx, h - h * 0.03f), lw, StrokeCap.Round)
            continue
        }
        for ((a, b) in listOf(Offset(hx, base) to knee, knee to foot)) {
            drawLine(Ink, a, b, lw + p.lw * 2, StrokeCap.Round)
            drawLine(c, a, b, lw, StrokeCap.Round)
        }
        drawOval(Ink, Offset(foot.x - lw * 0.6f, foot.y - lw * 0.7f), androidx.compose.ui.geometry.Size(lw * 2.4f, lw * 1.2f))
    }

    // Headboard with its brass knobs, mattress, pillow, the red blanket, footboard.
    val hb = Path().apply {
        moveTo(x0, base)
        lineTo(x0, top + bedH * 0.1f)
        quadraticTo(x0 + bedW * 0.09f, top - bedH * 0.2f, x0 + bedW * 0.18f, top + bedH * 0.1f)
        lineTo(x0 + bedW * 0.18f, base)
        close()
    }
    p.shape(wood) { addPath(hb) }
    p.d.drawCircle(Color(0xFFD9A33A), w * 0.022f, Offset(x0 + bedW * 0.09f, top - bedH * 0.08f))
    p.rect((x0 + bedW * 0.16f) / w, (top + bedH * 0.42f) / h, (x0 + bedW) / w, (base) / h, Color(0xFFFFFCF4))
    p.oval((x0 + bedW * 0.18f) / w, (top + bedH * 0.26f) / h, (x0 + bedW * 0.38f) / w, (top + bedH * 0.5f) / h, Color(0xFFFFFCF4))
    p.shape(Color(0xFFB8372A)) {
        moveTo(x0 + bedW * 0.34f, top + bedH * 0.36f)
        quadraticTo(x0 + bedW * 0.7f, top + bedH * 0.2f, x0 + bedW * 1.0f, top + bedH * 0.4f)
        lineTo(x0 + bedW * 1.0f, base + bedH * 0.08f)
        lineTo(x0 + bedW * 0.34f, base + bedH * 0.08f)
        close()
    }
    p.rect((x0 + bedW * 0.94f) / w, (top + bedH * 0.3f) / h, (x0 + bedW * 1.02f) / w, base / h, wood)
    p.d.drawCircle(Color(0xFFD9A33A), w * 0.016f, Offset(x0 + bedW * 0.98f, top + bedH * 0.26f))
    if (asleep) {
        val zx = x0 + bedW * 0.32f
        for (k in 0..2) {
            val s = w * (0.03f + k * 0.012f)
            val cx = zx + k * w * 0.06f
            val cy = top - k * h * 0.08f
            p.polyline(Ink, 1f, cx / w, cy / h, (cx + s) / w, cy / h, cx / w, (cy + s) / h, (cx + s) / w, (cy + s) / h)
        }
    }
}
