package app.slumber.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import app.slumber.domain.Act
import app.slumber.domain.Age
import app.slumber.domain.Base
import app.slumber.domain.Hair
import app.slumber.domain.Look
import app.slumber.domain.Mood
import app.slumber.domain.Trait
import app.slumber.ui.theme.Ink
import kotlin.math.cos
import kotlin.math.sin

/** Clothes colours a look can pick from (Look.coat). */
val Coats = listOf(
    Color(0xFFC2412D), Color(0xFF2F5D9A), Color(0xFF4E8B4A), Color(0xFFE3B23C), Color(0xFF7A4FA0),
    Color(0xFFF2EEE4), Color(0xFF8A5A3B), Color(0xFF8D8F96), Color(0xFF2A2A33), Color(0xFFE08AA6),
)
val Skins = listOf(Color(0xFFF4CFA8), Color(0xFFDDA77A), Color(0xFF9C6A45), Color(0xFF3A3540))
val HairTones = listOf(Color(0xFF3A2A22), Color(0xFF9A5A2E), Color(0xFFE2C178), Color(0xFFD9D6D0))

/**
 * A pose as joint angles, in degrees from straight down, forward positive. Arms and legs come in a back
 * one (further from you) and a front one; the lower joint is relative to the upper. [rot] turns the whole
 * figure about the hip (a fall is upside down, a flight lies along the air); [lift] raises it off the
 * ground as a share of its height; [grounded] off means it hangs where it's put.
 */
data class Pose(
    val lean: Float = 0f,
    val head: Float = 0f,
    val bu: Float = -8f, val bl: Float = 4f,
    val fu: Float = 8f, val fl: Float = 4f,
    val bt: Float = -4f, val bs: Float = 0f,
    val ft: Float = 4f, val fs: Float = 0f,
    val rot: Float = 0f,
    val lift: Float = 0f,
    val grounded: Boolean = true,
    /** Looking back over the shoulder, as when chased. */
    val back: Boolean = false,
    /** Eyes shut. */
    val shut: Boolean = false,
)

object Poses {
    val stand = Pose()

    fun of(a: Act): Pose = when (a) {
        Act.STAND -> stand
        Act.WALK -> Pose(lean = 4f, bu = -28f, bl = 10f, fu = 26f, fl = 20f, bt = -22f, bs = -10f, ft = 24f, fs = -24f)
        Act.RUN -> Pose(lean = 18f, bu = -55f, bl = 80f, fu = 60f, fl = 70f, bt = -45f, bs = -50f, ft = 55f, fs = -70f)
        Act.CHASED -> of(Act.RUN).copy(back = true)
        Act.FALL -> Pose(rot = 160f, bu = -150f, bl = -20f, fu = 150f, fl = 20f, bt = -20f, bs = -20f, ft = 30f, fs = -40f, lift = 0.32f, grounded = false)
        Act.FLY -> Pose(rot = 80f, bu = 165f, bl = 0f, fu = 175f, fl = 0f, bt = -5f, bs = 0f, ft = 5f, fs = 0f, lift = 0.55f, grounded = false)
        Act.FLOAT -> Pose(bu = -70f, bl = -30f, fu = 70f, fl = 30f, bt = -6f, bs = -10f, ft = 12f, fs = -20f, lift = 0.28f, grounded = false)
        Act.SWIM -> Pose(rot = 75f, bu = 40f, bl = 20f, fu = 170f, fl = 0f, bt = -15f, bs = 0f, ft = 10f, fs = 0f, lift = 0.18f, grounded = false)
        Act.SINK -> Pose(head = -15f, bu = -170f, bl = -10f, fu = 170f, fl = 10f, bt = -5f, bs = -5f, ft = 5f, fs = -10f, lift = 0.05f, grounded = false)
        Act.CLIMB -> Pose(lean = 5f, bu = 150f, bl = 30f, fu = 165f, fl = 10f, bt = -5f, bs = 0f, ft = 60f, fs = -90f, lift = 0.15f)
        Act.HIDE -> Pose(lean = 35f, head = 10f, bu = 80f, bl = 100f, fu = 100f, fl = 90f, bt = 70f, bs = -130f, ft = 80f, fs = -140f)
        Act.SEARCH -> Pose(lean = 18f, head = 15f, bu = -10f, bl = 20f, fu = 85f, fl = -10f, bt = -10f, bs = 0f, ft = 15f, fs = -10f)
        Act.LOST -> Pose(head = -10f, bu = -40f, bl = -20f, fu = 40f, fl = 20f)
        Act.TALK -> Pose(bu = -5f, bl = 5f, fu = 50f, fl = 40f)
        Act.SHOUT -> Pose(head = -20f, bu = -150f, bl = -20f, fu = 150f, fl = 20f, bt = -12f, ft = 12f)
        Act.SIT -> Pose(bu = 10f, bl = 40f, fu = 5f, fl = 40f, bt = 90f, bs = -90f, ft = 90f, fs = -90f)
        Act.SLEEP -> Pose(rot = 90f, bu = -5f, bl = 0f, fu = 10f, fl = 0f, bt = -3f, ft = 3f, shut = true)
        Act.CRY -> Pose(lean = 8f, head = 20f, bu = 30f, bl = 120f, fu = 40f, fl = 110f, shut = true)
        Act.LAUGH -> Pose(head = -20f, bu = -10f, bl = 90f, fu = 20f, fl = 70f)
        Act.HUG -> Pose(lean = 8f, bu = 70f, bl = 40f, fu = 80f, fl = 30f)
        Act.FIGHT -> Pose(lean = 10f, bu = 40f, bl = 100f, fu = 90f, fl = 0f, bt = -30f, bs = 0f, ft = 30f, fs = -10f)
        Act.DANCE -> Pose(lean = -5f, bu = -120f, bl = 40f, fu = 160f, fl = -30f, bt = -10f, bs = 0f, ft = 50f, fs = -80f)
        Act.OPEN -> Pose(lean = 6f, bu = -15f, bl = 10f, fu = 80f, fl = 0f, bt = -12f, ft = 14f, fs = -8f)
        Act.DRIVE -> Pose(bu = 55f, bl = 30f, fu = 60f, fl = 30f, bt = 90f, bs = -90f, ft = 90f, fs = -90f)
        Act.FROZEN -> Pose(bu = -2f, bl = 0f, fu = 2f, fl = 0f, bt = -2f, ft = 2f)
        Act.GROW, Act.SHRINK -> Pose(head = -8f, bu = -25f, bl = -10f, fu = 25f, fl = 10f)
        Act.LOOK -> Pose(head = -5f, fu = 150f, fl = 100f)
    }

    /** What everyone else in the panel does while the dreamer does [a]. */
    fun others(a: Act): Act = when (a) {
        Act.CHASED -> Act.RUN
        Act.RUN, Act.WALK, Act.FLY, Act.FLOAT, Act.SWIM, Act.SIT, Act.DANCE, Act.CLIMB, Act.SINK, Act.FIGHT, Act.HUG, Act.FALL -> a
        Act.DRIVE -> Act.SIT
        Act.TALK -> Act.TALK
        else -> Act.STAND
    }
}

private class Joints(
    val hip: Offset, val neck: Offset, val head: Offset,
    val bs: Offset, val be: Offset, val bh: Offset,
    val fs: Offset, val fe: Offset, val fh: Offset,
    val bk: Offset, val bf: Offset, val fk: Offset, val ff: Offset,
) {
    fun all() = listOf(hip, neck, head, be, bh, fe, fh, bk, bf, fk, ff)
    fun map(f: (Offset) -> Offset) = Joints(f(hip), f(neck), f(head), f(bs), f(be), f(bh), f(fs), f(fe), f(fh), f(bk), f(bf), f(fk), f(ff))
}

/** How tall a figure of this look stands, as a share of the panel. */
fun Look.height(base: Float): Float {
    val t = listOf(0.88f, 1f, 1.1f)[tall]
    val a = when (age) { Age.CHILD -> 0.64f; Age.ADULT -> 1f; Age.OLD -> 0.95f }
    val g = if (has(Trait.GIANT)) 1.45f else 1f
    return base * t * a * g
}

/**
 * Draws someone standing with their feet at ([x], [y]) (fractions of the panel), [tall] high (a share of
 * the panel's height), facing right when [facing] is 1.
 */
fun Pen.figure(look: Look, pose: Pose, x: Float, y: Float, tall: Float, facing: Int, mood: Mood) {
    if (look.has(Trait.DOUBLE)) {
        d.drawIntoCanvas { c ->
            c.saveLayer(Rect(0f, 0f, w, h), Paint().apply { alpha = 0.4f })
            single(look, pose, x - facing * tall * 0.22f * h / w, y, tall, facing, mood)
            c.restore()
        }
    }
    single(look, pose, x, y, tall, facing, mood)
}

private fun Pen.single(look: Look, pose: Pose, x: Float, y: Float, tall: Float, facing: Int, mood: Mood) {
    val hp = tall * h
    when (look.base) {
        Base.HUMAN -> human(look, pose, x * w, y * h, hp, facing.toFloat(), mood)
        Base.ANIMAL -> beast(look, pose, x * w, y * h, hp * 0.62f, facing.toFloat())
        Base.BIRD -> bird(look, pose, x * w, y * h, hp * 0.5f, facing.toFloat())
        Base.THING -> thing(look, pose, x * w, y * h, hp * 0.8f, facing.toFloat())
        Base.BLOB -> blob(look, pose, x * w, y * h, hp * 0.75f, facing.toFloat())
    }
}

private fun dir(deg: Float, f: Float): Offset {
    val a = Math.toRadians(deg.toDouble())
    return Offset((sin(a) * f).toFloat(), cos(a).toFloat())
}

private fun rotate(p: Offset, about: Offset, deg: Float): Offset {
    val a = Math.toRadians(deg.toDouble())
    val dx = p.x - about.x
    val dy = p.y - about.y
    return Offset((about.x + dx * cos(a) - dy * sin(a)).toFloat(), (about.y + dx * sin(a) + dy * cos(a)).toFloat())
}

private fun Pen.human(look: Look, pose: Pose, fx: Float, fy: Float, H: Float, f: Float, mood: Mood) {
    val child = look.age == Age.CHILD
    val headR = H * if (child) 0.11f else 0.085f
    val leg = H * if (child) 0.4f else 0.47f
    val torso = H * if (child) 0.3f else 0.31f
    val upper = H * 0.16f
    val fore = H * 0.15f

    // Lay the skeleton out from the hip at the origin.
    val hip = Offset.Zero
    val neck = hip + dir(180f - pose.lean, f) * torso
    val head = neck + dir(180f - pose.lean - pose.head, f) * (headR * 1.05f)
    val sh = neck + dir(-pose.lean, f) * (H * 0.03f)
    val be = sh + dir(pose.bu, f) * upper
    val bh = be + dir(pose.bu + pose.bl, f) * fore
    val fe = sh + dir(pose.fu, f) * upper
    val fh = fe + dir(pose.fu + pose.fl, f) * fore
    val bk = hip + dir(pose.bt, f) * (leg * 0.5f)
    val bf = bk + dir(pose.bt + pose.bs, f) * (leg * 0.5f)
    val fk = hip + dir(pose.ft, f) * (leg * 0.5f)
    val ff = fk + dir(pose.ft + pose.fs, f) * (leg * 0.5f)
    var j = Joints(hip, neck, head, sh, be, bh, sh, fe, fh, bk, bf, fk, ff)
    if (pose.rot != 0f) j = j.map { rotate(it, hip, pose.rot * f) }

    // Then stand it on the ground (or hang it in the air).
    val lowest = j.all().maxOf { it.y }.coerceAtLeast(j.head.y + headR)
    val dy = if (pose.grounded) fy - lowest - pose.lift * H else fy - pose.lift * H * 1.6f
    val placed = j.map { it + Offset(fx, dy) }

    if (look.has(Trait.GLOWS)) {
        val c = Offset(placed.neck.x, placed.neck.y)
        d.drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF1B0).copy(alpha = 0.85f), Color.Transparent), c, H * 0.75f), H * 0.75f, c)
    }

    val coat = Coats[look.coat]
    val skin = if (look.has(Trait.FACELESS)) coat.mix(Ink, 0.5f) else Skins[look.skin]
    val legs = if (look.coat == 5) skin else coat.mix(Ink, 0.4f)
    val limb = H * 0.075f

    limbPair(placed.bs, placed.be, placed.bh, limb, coat.mix(Ink, 0.15f), skin)
    leg(placed.hip, placed.bk, placed.bf, limb * 1.15f, legs.mix(Ink, 0.15f), f)
    body(placed, H, coat, look)
    leg(placed.hip, placed.fk, placed.ff, limb * 1.15f, legs, f)
    limbPair(placed.fs, placed.fe, placed.fh, limb, coat, skin)
    headOf(look, pose, placed.head, headR, f, mood, skin)
}

/** A limb: ink first a little wider, then the colour, so it reads as outlined. */
private fun Pen.limb(a: Offset, b: Offset, width: Float, c: Color) {
    d.drawLine(Ink, a, b, strokeWidth = width + lw * 2f, cap = StrokeCap.Round)
    d.drawLine(c, a, b, strokeWidth = width, cap = StrokeCap.Round)
}

private fun Pen.limbPair(s: Offset, e: Offset, hand: Offset, width: Float, sleeve: Color, skin: Color) {
    limb(s, e, width, sleeve)
    limb(e, hand, width * 0.9f, sleeve)
    d.drawCircle(Ink, width * 0.62f + lw, hand)
    d.drawCircle(skin, width * 0.62f, hand)
}

private fun Pen.leg(hip: Offset, knee: Offset, foot: Offset, width: Float, c: Color, f: Float) {
    limb(hip, knee, width, c)
    limb(knee, foot, width * 0.92f, c)
    val toe = Offset(foot.x + f * width * 0.9f, foot.y)
    d.drawLine(Ink, foot, toe, strokeWidth = width * 0.9f, cap = StrokeCap.Round)
}

/** The torso: a shirt that flares a little to the hem. */
private fun Pen.body(j: Joints, H: Float, coat: Color, look: Look) {
    val up = (j.neck - j.hip).let { it / it.getDistance().coerceAtLeast(0.001f) }
    val side = Offset(-up.y, up.x)
    val sw = H * 0.085f
    val hw = H * 0.075f
    val hem = H * if (look.coat == 5) 0.16f else 0.05f
    val hemW = hw * if (look.coat == 5) 1.5f else 1.1f
    val p = Path().apply {
        val a = j.neck + side * sw
        val b = j.neck - side * sw
        val c = j.hip - side * hemW - up * hem
        val e = j.hip + side * hemW - up * hem
        moveTo(a.x, a.y); lineTo(b.x, b.y); lineTo(c.x, c.y); lineTo(e.x, e.y); close()
    }
    d.drawPath(p, coat)
    outline(p)
}

private fun Pen.headOf(look: Look, pose: Pose, c: Offset, r: Float, f: Float, mood: Mood, skin: Color) {
    val hair = HairTones[if (look.age == Age.OLD) 3 else look.hairTone]
    if (look.hair == Hair.LONG) {
        d.drawRoundRect(Ink, Offset(c.x - r * 1.1f - (if (f > 0) r * 0.2f else -r * 0.2f), c.y - r * 0.4f), androidx.compose.ui.geometry.Size(r * 2.2f, r * 2f), androidx.compose.ui.geometry.CornerRadius(r))
        d.drawRoundRect(hair, Offset(c.x - r * 1.0f - (if (f > 0) r * 0.2f else -r * 0.2f), c.y - r * 0.35f), androidx.compose.ui.geometry.Size(r * 2f, r * 1.85f), androidx.compose.ui.geometry.CornerRadius(r))
    }
    d.drawCircle(skin, r, c)
    d.drawCircle(Ink, r, c, style = Stroke(lw))
    when (look.hair) {
        Hair.NONE -> {}
        Hair.SHORT, Hair.LONG -> cap(c, r, hair, 0.55f)
        Hair.BUN -> { cap(c, r, hair, 0.5f); d.drawCircle(hair, r * 0.42f, c + Offset(-f * r * 0.5f, -r * 0.95f)); d.drawCircle(Ink, r * 0.42f, c + Offset(-f * r * 0.5f, -r * 0.95f), style = Stroke(lw)) }
        Hair.CURLY -> for (i in 0..6) {
            val a = Math.PI * (1.05 + i * 0.15)
            val p = c + Offset((cos(a) * r).toFloat(), (sin(a) * r * 0.95f).toFloat())
            d.drawCircle(hair, r * 0.36f, p)
            d.drawCircle(Ink, r * 0.36f, p, style = Stroke(lw * 0.8f))
        }
        Hair.HAT -> {
            val top = Path().apply {
                moveTo(c.x - r * 0.85f, c.y - r * 0.45f)
                cubicTo(c.x - r * 0.85f, c.y - r * 1.7f, c.x + r * 0.85f, c.y - r * 1.7f, c.x + r * 0.85f, c.y - r * 0.45f)
                close()
            }
            d.drawPath(top, Ink.mix(Coats[look.coat], 0.25f))
            d.drawLine(Ink, Offset(c.x - r * 1.3f, c.y - r * 0.45f), Offset(c.x + r * 1.3f, c.y - r * 0.45f), strokeWidth = lw * 2f, cap = StrokeCap.Round)
        }
    }
    if (look.has(Trait.FACELESS)) return
    // The face looks the way the body faces, or back over the shoulder.
    val look2 = if (pose.back) -f else f
    val eye = c + Offset(look2 * r * 0.38f, -r * 0.05f)
    if (pose.shut) {
        d.drawLine(Ink, eye - Offset(r * 0.16f, 0f), eye + Offset(r * 0.16f, 0f), strokeWidth = lw * 0.9f, cap = StrokeCap.Round)
    } else {
        val wide = mood == Mood.FEAR || pose.back
        d.drawCircle(Ink, r * if (wide) 0.14f else 0.1f, eye)
        if (wide) d.drawCircle(Color.White, r * 0.05f, eye)
    }
    if (look.has(Trait.GLASSES)) {
        d.drawCircle(Ink, r * 0.24f, eye, style = Stroke(lw * 0.8f))
        d.drawLine(Ink, eye + Offset(-look2 * r * 0.24f, 0f), c + Offset(-look2 * r * 0.25f, -r * 0.1f), strokeWidth = lw * 0.7f)
    }
    if (look.has(Trait.BEARD)) {
        val b = Path().apply {
            moveTo(c.x - r * 0.8f, c.y + r * 0.1f)
            cubicTo(c.x - r * 0.7f, c.y + r * 1.4f, c.x + r * 0.7f, c.y + r * 1.4f, c.x + r * 0.8f, c.y + r * 0.1f)
            close()
        }
        d.drawPath(b, hair)
        outline(b, 0.8f)
        return
    }
    val m = c + Offset(look2 * r * 0.45f, r * 0.45f)
    val mw = r * 0.22f
    when (mood) {
        Mood.JOY, Mood.WONDER, Mood.CALM -> d.drawArc(Ink, 10f, 160f, false, Offset(m.x - mw, m.y - mw * 1.2f), androidx.compose.ui.geometry.Size(mw * 2, mw * 1.6f), style = Stroke(lw * 0.9f, cap = StrokeCap.Round))
        Mood.FEAR -> d.drawOval(Ink, Offset(m.x - mw * 0.5f, m.y - mw * 0.6f), androidx.compose.ui.geometry.Size(mw, mw * 1.3f))
        Mood.SADNESS -> d.drawArc(Ink, 200f, 140f, false, Offset(m.x - mw, m.y - mw * 0.2f), androidx.compose.ui.geometry.Size(mw * 2, mw * 1.6f), style = Stroke(lw * 0.9f, cap = StrokeCap.Round))
        else -> d.drawLine(Ink, m - Offset(mw, 0f), m + Offset(mw, 0f), strokeWidth = lw * 0.9f, cap = StrokeCap.Round)
    }
    if (mood == Mood.ANGER) d.drawLine(Ink, eye + Offset(-r * 0.2f, -r * 0.28f), eye + Offset(r * 0.2f * look2, -r * 0.16f), strokeWidth = lw, cap = StrokeCap.Round)
}

private fun Pen.cap(c: Offset, r: Float, hair: Color, depth: Float) {
    val p = Path().apply {
        moveTo(c.x - r * 1.02f, c.y - r * (1f - depth * 1.2f))
        cubicTo(c.x - r * 1.1f, c.y - r * 1.5f, c.x + r * 1.1f, c.y - r * 1.5f, c.x + r * 1.02f, c.y - r * (1f - depth * 1.2f))
        quadraticTo(c.x, c.y - r * 0.55f, c.x - r * 1.02f, c.y - r * (1f - depth * 1.2f))
        close()
    }
    d.drawPath(p, hair)
    outline(p, 0.8f)
}

/** A four-legged someone: dog, cat, wolf, horse, whatever it was. */
private fun Pen.beast(look: Look, pose: Pose, fx: Float, fy: Float, H: Float, f: Float) {
    val c = Coats[look.coat]
    val lying = pose.rot != 0f || pose == Poses.of(Act.SLEEP)
    val run = pose.lean > 10f
    val legH = if (lying) 0f else H * 0.42f
    val lift = if (pose.grounded) 0f else H * 0.8f
    val by = fy - legH - H * 0.18f - lift
    val bl = H * 0.62f
    val spread = if (run) H * 0.22f else H * 0.06f
    if (!lying) for ((dx, back) in listOf(-bl * 0.32f to true, bl * 0.3f to false)) {
        val hipX = fx + f * dx
        limb(Offset(hipX, by), Offset(hipX - f * spread, fy - lift), H * 0.09f, c.mix(Ink, if (back) 0.2f else 0f))
        limb(Offset(hipX, by), Offset(hipX + f * spread, fy - lift), H * 0.09f, c.mix(Ink, if (back) 0.2f else 0f))
    }
    // Tail, body, head with ears.
    val tail = Path().apply {
        moveTo(fx - f * bl * 0.48f, by - H * 0.05f)
        quadraticTo(fx - f * bl * 0.8f, by - H * 0.1f, fx - f * bl * 0.72f, by - H * 0.36f)
    }
    d.drawPath(tail, Ink, style = Stroke(H * 0.07f + lw * 2, cap = StrokeCap.Round))
    d.drawPath(tail, c, style = Stroke(H * 0.07f, cap = StrokeCap.Round))
    oval((fx - bl / 2) / w, (by - H * 0.17f) / h, (fx + bl / 2) / w, (by + H * 0.17f) / h, c)
    val hc = Offset(fx + f * bl * 0.5f, by - H * 0.2f)
    val hr = H * 0.17f
    poly(c, (hc.x - f * hr * 0.6f) / w, (hc.y - hr * 0.6f) / h, (hc.x - f * hr * 0.2f) / w, (hc.y - hr * 1.5f) / h, (hc.x + f * hr * 0.2f) / w, (hc.y - hr * 0.7f) / h)
    d.drawCircle(c, hr, hc)
    d.drawCircle(Ink, hr, hc, style = Stroke(lw))
    oval((hc.x + f * hr * 0.4f - hr * 0.5f) / w, (hc.y) / h, (hc.x + f * hr * 0.4f + hr * 0.5f) / w, (hc.y + hr * 0.7f) / h, c.mix(Color.White, 0.3f))
    d.drawCircle(Ink, hr * 0.14f, hc + Offset(f * hr * 0.25f, -hr * 0.2f))
    d.drawCircle(Ink, hr * 0.16f, hc + Offset(f * hr * 0.9f, hr * 0.25f))
}

private fun Pen.bird(look: Look, pose: Pose, fx: Float, fy: Float, H: Float, f: Float) {
    val c = Coats[look.coat]
    val flying = !pose.grounded || pose.lean > 10f
    val by = fy - H * 0.45f - if (flying) H * 1.2f else 0f
    if (!flying) {
        d.drawLine(Ink, Offset(fx - H * 0.08f, by + H * 0.2f), Offset(fx - H * 0.08f, fy), lw * 1.2f)
        d.drawLine(Ink, Offset(fx + H * 0.08f, by + H * 0.2f), Offset(fx + H * 0.08f, fy), lw * 1.2f)
    }
    oval((fx - H * 0.3f) / w, (by - H * 0.22f) / h, (fx + H * 0.3f) / w, (by + H * 0.24f) / h, c)
    val wy = if (flying) -H * 0.5f else H * 0.05f
    poly(c.mix(Ink, 0.2f), (fx - f * H * 0.05f) / w, (by - H * 0.05f) / h, (fx - f * H * 0.45f) / w, (by + wy) / h, (fx + f * H * 0.12f) / w, (by + H * 0.08f) / h)
    val hc = Offset(fx + f * H * 0.28f, by - H * 0.24f)
    d.drawCircle(c, H * 0.15f, hc)
    d.drawCircle(Ink, H * 0.15f, hc, style = Stroke(lw))
    poly(Color(0xFFE3B23C), (hc.x + f * H * 0.12f) / w, (hc.y - H * 0.04f) / h, (hc.x + f * H * 0.32f) / w, (hc.y + H * 0.02f) / h, (hc.x + f * H * 0.12f) / w, (hc.y + H * 0.07f) / h)
    d.drawCircle(Ink, H * 0.03f, hc + Offset(f * H * 0.04f, -H * 0.03f))
}

/** A thing that came alive: a tall clock-cupboard with eyes and spindly legs. */
private fun Pen.thing(look: Look, pose: Pose, fx: Float, fy: Float, H: Float, f: Float) {
    val c = Color(0xFF9A6A42)
    val lift = if (pose.grounded) 0f else H * 0.6f
    val top = fy - H - lift
    d.drawLine(Ink, Offset(fx - H * 0.12f, fy - H * 0.25f - lift), Offset(fx - H * 0.18f, fy - lift), lw * 1.4f)
    d.drawLine(Ink, Offset(fx + H * 0.12f, fy - H * 0.25f - lift), Offset(fx + H * 0.18f, fy - lift), lw * 1.4f)
    rect((fx - H * 0.2f) / w, top / h, (fx + H * 0.2f) / w, (fy - H * 0.22f - lift) / h, c)
    circle(fx / w, (top + H * 0.2f) / h, H * 0.13f / w, Color(0xFFF4EAD5))
    d.drawLine(Ink, Offset(fx, top + H * 0.2f), Offset(fx + f * H * 0.07f, top + H * 0.14f), lw)
    d.drawCircle(Ink, H * 0.03f, Offset(fx - H * 0.07f, top + H * 0.5f))
    d.drawCircle(Ink, H * 0.03f, Offset(fx + H * 0.07f, top + H * 0.5f))
}

/** Something without a name: a soft heap of colour with eyes. */
private fun Pen.blob(look: Look, pose: Pose, fx: Float, fy: Float, H: Float, f: Float) {
    val c = Coats[look.coat]
    val lift = if (pose.grounded) 0f else H * 0.6f
    val p = Path().apply {
        moveTo(fx - H * 0.5f, fy - lift)
        cubicTo(fx - H * 0.6f, fy - H * 0.6f - lift, fx - H * 0.3f, fy - H * 1.05f - lift, fx, fy - H - lift)
        cubicTo(fx + H * 0.35f, fy - H * 0.98f - lift, fx + H * 0.62f, fy - H * 0.5f - lift, fx + H * 0.5f, fy - lift)
        quadraticTo(fx + H * 0.25f, fy - H * 0.1f - lift, fx, fy - lift)
        quadraticTo(fx - H * 0.25f, fy - H * 0.1f - lift, fx - H * 0.5f, fy - lift)
        close()
    }
    d.drawPath(p, c)
    outline(p)
    for (dx in listOf(-0.14f, 0.14f)) {
        val e = Offset(fx + f * H * 0.1f + dx * H, fy - H * 0.62f - lift)
        d.drawCircle(Color.White, H * 0.09f, e)
        d.drawCircle(Ink, H * 0.09f, e, style = Stroke(lw))
        d.drawCircle(Ink, H * 0.04f, e + Offset(f * H * 0.03f, 0f))
    }
}
