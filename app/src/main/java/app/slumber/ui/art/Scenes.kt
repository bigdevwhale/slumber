package app.slumber.ui.art

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import app.slumber.domain.Scene
import app.slumber.ui.theme.Ink
import kotlin.random.Random

/** Where feet stand in every scene, as a fraction of the panel's height. */
const val GROUND = 0.88f

private val Wood = Color(0xFF9A6A42)
private val Cream = Color(0xFFF4EAD5)
private val Brick = Color(0xFFB0563C)
private val Stone = Color(0xFFB9B2A4)
private val Snow = Color(0xFFFFFCF4)
private val Leaf = Color(0xFF5D8C4A)
private val Metal = Color(0xFFA9B0B8)
private val Glass = Color(0xFFBFDCE6)

/**
 * The places, each a handful of flat shapes in the panel's mood colours. They're archetypes on purpose:
 * "a corridor" has to be every endless corridor anyone has dreamt.
 */
fun Pen.scene(s: Scene, seed: Int) {
    val r = Random(seed)
    when (s) {
        Scene.HOUSE -> house(r)
        Scene.ROOM -> plainRoom(r)
        Scene.CORRIDOR -> corridor()
        Scene.STAIRS -> stairs()
        Scene.KITCHEN -> kitchen()
        Scene.BATHROOM -> bathroom()
        Scene.SCHOOL -> school()
        Scene.OFFICE -> office()
        Scene.HOSPITAL -> hospital()
        Scene.SHOP -> shop(r)
        Scene.STATION -> station()
        Scene.TRAIN -> train()
        Scene.ELEVATOR -> elevator()
        Scene.CAR -> road()
        Scene.STREET -> street(r)
        Scene.CITY -> city(r)
        Scene.ROOF -> roof(r)
        Scene.BRIDGE -> bridge()
        Scene.GARDEN -> garden(r)
        Scene.FOREST -> forest(r)
        Scene.FIELD -> field(r)
        Scene.MOUNTAIN -> mountain()
        Scene.CAVE -> cave()
        Scene.BEACH -> beach()
        Scene.SEA -> sea()
        Scene.UNDERWATER -> underwater(r)
        Scene.SKY -> sky(r)
        Scene.SPACE -> space(r)
        Scene.CASTLE -> castle()
        Scene.TEMPLE -> temple()
        Scene.VOID -> void(r)
    }
}

private fun Pen.outdoors(horizon: Float = 0.62f) {
    grad(t.sky, t.sky2, 0f, horizon, 0f, horizon + 0.01f)
}

private fun Pen.house(r: Random) {
    outdoors()
    cloud(0.2f, 0.2f, 0.05f)
    hills(0.66f, t.ground.mix(Color.White, 0.25f), 0.05f, 0.3f)
    rect(0f, 0.7f, 1f, 1f, t.ground, line = false)
    line(0f, 0.7f, 1f, 0.7f)
    // The house: walls, gable, door, two windows, chimney.
    rect(0.62f, 0.12f, 0.67f, 0.3f, Brick)
    rect(0.3f, 0.36f, 0.8f, 0.76f, t.wall)
    poly(Brick, 0.25f, 0.38f, 0.55f, 0.1f, 0.85f, 0.38f)
    window(0.36f, 0.46f, 0.47f, 0.6f, t.sky.mix(Color.Yellow, 0.4f))
    window(0.63f, 0.46f, 0.74f, 0.6f, t.sky.mix(Color.Yellow, 0.4f))
    door(0.51f, 0.55f, 0.59f, 0.76f, t.accent)
    poly(Stone, 0.51f, 0.76f, 0.59f, 0.76f, 0.66f, 1f, 0.44f, 1f, line = false)
    if (r.nextBoolean()) tree(0.13f, 0.74f, 0.32f, Leaf)
}

private fun Pen.plainRoom(r: Random) {
    room(0.18f, 0.16f, 0.82f, 0.66f)
    window(0.28f, 0.24f, 0.46f, 0.48f)
    rect(0.58f, 0.26f, 0.72f, 0.42f, t.accent.mix(Cream, 0.4f))
    rect(0.61f, 0.29f, 0.69f, 0.39f, t.sky2)
    // A lamp hanging from the ceiling, and a rug.
    line(0.5f, 0f, 0.5f, 0.1f, 0.7f)
    poly(t.accent, 0.45f, 0.14f, 0.55f, 0.14f, 0.53f, 0.09f, 0.47f, 0.09f)
    oval(0.26f, 0.8f, 0.74f, 0.95f, t.accent.mix(Cream, 0.3f))
    if (r.nextBoolean()) rect(0.76f, 0.5f, 0.8f, 0.66f, Wood)
}

private fun Pen.corridor() {
    // One-point perspective to a far small door, doors on both sides growing toward you.
    room(0.44f, 0.36f, 0.56f, 0.56f)
    door(0.465f, 0.4f, 0.535f, 0.56f, t.accent.mix(Ink, 0.2f))
    for (i in 0..3) {
        val f = 0.08f + i * 0.09f
        val x0 = f
        val x1 = f + 0.05f + i * 0.004f
        val top0 = 0.36f - (0.44f - x0) * 0.75f
        val bot0 = 0.56f + (0.44f - x0) * 0.85f
        val top1 = 0.36f - (0.44f - x1) * 0.75f
        val bot1 = 0.56f + (0.44f - x1) * 0.85f
        poly(t.accent.mix(Wood, 0.5f), x0, top0 + 0.08f, x1, top1 + 0.08f, x1, bot1 - 0.01f, x0, bot0 - 0.01f)
        val rx0 = 1f - x1
        val rx1 = 1f - x0
        poly(t.accent.mix(Wood, 0.5f), rx0, top1 + 0.08f, rx1, top0 + 0.08f, rx1, bot0 - 0.01f, rx0, bot1 - 0.01f)
    }
    for (i in 0..2) {
        val y = 0.04f + i * 0.1f
        oval(0.47f - i * 0.01f, y, 0.53f + i * 0.01f, y + 0.03f, Color(0xFFFFF1C2))
    }
}

private fun Pen.stairs() {
    room(0.1f, 0.1f, 0.9f, 0.7f)
    // Steps rising left to right, a banister along them, a door at the top.
    val n = 7
    for (i in 0 until n) {
        val x0 = 0.12f + i * 0.1f
        val y0 = 0.9f - i * 0.1f
        rect(x0, y0 - 0.1f, 1f, y0, t.wall.mix(Wood, 0.5f + 0.05f * (i % 2)))
    }
    door(0.8f, 0.0f, 0.95f, 0.2f, t.accent)
    line(0.08f, 0.66f, 0.82f, -0.04f, 1.4f, Wood)
    for (i in 0 until n) line(0.12f + i * 0.1f, 0.8f - i * 0.1f, 0.12f + i * 0.1f, 0.62f - i * 0.1f, 0.7f)
}

private fun Pen.kitchen() {
    room(0.14f, 0.14f, 0.86f, 0.64f)
    window(0.42f, 0.2f, 0.6f, 0.4f)
    rect(0.16f, 0.18f, 0.36f, 0.32f, Wood.mix(Cream, 0.3f))
    rect(0.66f, 0.18f, 0.84f, 0.32f, Wood.mix(Cream, 0.3f))
    // Counter with stove, a kettle on it.
    rect(0.14f, 0.46f, 0.86f, 0.64f, Wood.mix(Cream, 0.2f))
    rect(0.14f, 0.44f, 0.86f, 0.47f, Stone)
    rect(0.6f, 0.49f, 0.78f, 0.62f, Ink.mix(Metal, 0.6f))
    circle(0.68f, 0.4f, 0.04f, t.accent)
    line(0.71f, 0.38f, 0.75f, 0.35f, 0.9f)
    for (i in 0..2) rect(0.2f + i * 0.12f, 0.5f, 0.28f + i * 0.12f, 0.6f, Wood)
}

private fun Pen.bathroom() {
    room(0.16f, 0.12f, 0.84f, 0.64f, Glass.mix(Cream, 0.5f), Stone)
    for (i in 1..6) line(0.16f + i * 0.097f, 0.12f, 0.16f + i * 0.097f, 0.64f, 0.3f)
    for (i in 1..4) line(0.16f, 0.12f + i * 0.104f, 0.84f, 0.12f + i * 0.104f, 0.3f)
    oval(0.36f, 0.18f, 0.56f, 0.4f, Glass)
    // The tub, its tap.
    rect(0.2f, 0.5f, 0.8f, 0.68f, Cream)
    oval(0.2f, 0.46f, 0.8f, 0.55f, Snow)
    oval(0.24f, 0.48f, 0.76f, 0.54f, t.sky.mix(Glass, 0.5f), line = false)
    line(0.74f, 0.4f, 0.74f, 0.48f, 1.2f, Metal)
}

private fun Pen.school() {
    room(0.12f, 0.12f, 0.88f, 0.62f)
    rect(0.24f, 0.18f, 0.76f, 0.42f, Color(0xFF2F4A3C))
    line(0.3f, 0.26f, 0.48f, 0.26f, 0.6f, Snow)
    line(0.3f, 0.32f, 0.62f, 0.32f, 0.6f, Snow)
    line(0.3f, 0.38f, 0.42f, 0.38f, 0.6f, Snow)
    circle(0.82f, 0.22f, 0.035f, Cream)
    line(0.82f, 0.22f, 0.82f, 0.19f, 0.6f)
    line(0.82f, 0.22f, 0.835f, 0.22f, 0.6f)
    // Desks in rows, growing toward you.
    for (row in 0..1) for (col in 0..2) {
        val s = 0.13f + row * 0.05f
        val x = 0.22f + col * 0.28f - (col - 1) * row * 0.05f
        val y = 0.66f + row * 0.13f
        rect(x - s / 2, y, x + s / 2, y + s * 0.3f, Wood)
        line(x - s / 2.4f, y + s * 0.3f, x - s / 2.4f, y + s * 0.65f, 0.8f)
        line(x + s / 2.4f, y + s * 0.3f, x + s / 2.4f, y + s * 0.65f, 0.8f)
    }
}

private fun Pen.office() {
    room(0.1f, 0.1f, 0.9f, 0.62f, t.wall.mix(Metal, 0.3f))
    for (i in 0..2) window(0.16f + i * 0.25f, 0.16f, 0.34f + i * 0.25f, 0.4f)
    // Two desks with screens, a plant.
    for (x in listOf(0.24f, 0.66f)) {
        rect(x - 0.13f, 0.6f, x + 0.13f, 0.64f, Wood)
        line(x - 0.11f, 0.64f, x - 0.11f, 0.76f, 0.8f)
        line(x + 0.11f, 0.64f, x + 0.11f, 0.76f, 0.8f)
        rect(x - 0.06f, 0.48f, x + 0.06f, 0.57f, Ink.mix(Glass, 0.3f))
        line(x, 0.57f, x, 0.6f, 0.9f)
    }
    rect(0.86f, 0.62f, 0.92f, 0.7f, Brick)
    oval(0.83f, 0.5f, 0.95f, 0.64f, Leaf)
}

private fun Pen.hospital() {
    room(0.12f, 0.12f, 0.88f, 0.62f, Color(0xFFD5E6D8), Stone.mix(Snow, 0.4f))
    rect(0.44f, 0.18f, 0.56f, 0.3f, Snow)
    rect(0.485f, 0.2f, 0.515f, 0.28f, t.accent, line = false)
    rect(0.455f, 0.225f, 0.545f, 0.255f, t.accent, line = false)
    // The bed with its rails, a drip on a stand.
    rect(0.2f, 0.56f, 0.68f, 0.66f, Snow)
    rect(0.2f, 0.52f, 0.3f, 0.58f, Cream)
    line(0.2f, 0.5f, 0.2f, 0.76f, 1.2f, Metal)
    line(0.68f, 0.54f, 0.68f, 0.76f, 1.2f, Metal)
    line(0.78f, 0.28f, 0.78f, 0.74f, 0.9f, Metal)
    oval(0.75f, 0.28f, 0.81f, 0.36f, Glass)
}

private fun Pen.shop(r: Random) {
    room(0.08f, 0.1f, 0.92f, 0.62f)
    for (shelf in 0..2) {
        val y = 0.22f + shelf * 0.13f
        rect(0.1f, y + 0.08f, 0.9f, y + 0.1f, Wood)
        var x = 0.12f
        while (x < 0.86f) {
            val bw = 0.04f + r.nextFloat() * 0.04f
            val bh = 0.04f + r.nextFloat() * 0.04f
            rect(x, y + 0.08f - bh, x + bw, y + 0.08f, listOf(t.accent, Brick, Leaf, Glass, Color(0xFFE3B23C))[r.nextInt(5)])
            x += bw + 0.012f
        }
    }
    rect(0.0f, 0.66f, 0.42f, 0.8f, Wood.mix(Cream, 0.2f))
    rect(0.3f, 0.6f, 0.4f, 0.66f, Metal)
}

private fun Pen.station() {
    grad(t.sky, t.sky2, 0f, 0.5f, 0f, 0.5f)
    // The great iron roof arch, a clock, the platform and rails running away.
    shape(t.deep.mix(Metal, 0.3f)) {
        moveTo(0f, 0f); lineTo(w, 0f); lineTo(w, 0.5f * h)
        cubicTo(w * 0.85f, 0.05f * h, w * 0.15f, 0.05f * h, 0f, 0.5f * h); close()
    }
    for (i in 1..5) line(i / 6f, 0.13f, i / 6f, 0.5f, 0.5f)
    rect(0f, 0.5f, 1f, 0.56f, Stone)
    circle(0.5f, 0.24f, 0.05f, Cream)
    line(0.5f, 0.24f, 0.5f, 0.2f, 0.7f)
    line(0.5f, 0.24f, 0.525f, 0.24f, 0.7f)
    poly(t.ground.mix(Stone, 0.5f), 0f, 0.56f, 1f, 0.56f, 1f, 1f, 0f, 1f)
    poly(Ink.mix(Wood, 0.4f), 0.42f, 0.56f, 0.5f, 0.56f, 0.32f, 1f, 0.02f, 1f, line = false)
    line(0.43f, 0.56f, 0.06f, 1f, 1f, Metal)
    line(0.49f, 0.56f, 0.28f, 1f, 1f, Metal)
    line(0.52f, 0.62f, 1f, 0.62f, 0.8f, Color(0xFFE3B23C))
}

private fun Pen.train() {
    room(0.06f, 0.14f, 0.94f, 0.62f, t.wall.mix(Wood, 0.35f))
    for (i in 0..2) {
        val x = 0.12f + i * 0.28f
        rect(x, 0.2f, x + 0.22f, 0.42f, Wood)
        d.drawRect(Brush.verticalGradient(listOf(t.sky, t.sky2), startY = 0.22f * h, endY = 0.4f * h), o(x + 0.015f, 0.22f), Size((0.19f) * w, 0.18f * h))
        line(x + 0.02f, 0.36f, x + 0.2f, 0.33f, 0.4f, t.ground)
    }
    // Seat backs in rows.
    for (i in 0..2) {
        val x = 0.1f + i * 0.3f
        rect(x, 0.5f, x + 0.22f, 0.7f, t.accent.mix(Ink, 0.15f))
        rect(x, 0.68f, x + 0.22f, 0.74f, t.accent.mix(Ink, 0.3f))
    }
    rect(0.06f, 0.44f, 0.94f, 0.46f, Metal)
}

private fun Pen.elevator() {
    room(0.24f, 0.08f, 0.76f, 0.7f, Metal.mix(Color(0xFFD9A33A), 0.25f), Metal.mix(Ink, 0.2f))
    // Doors with their slit, the floor dial above, the button panel.
    rect(0.3f, 0.2f, 0.7f, 0.7f, Metal)
    line(0.5f, 0.2f, 0.5f, 0.7f, 1.1f)
    shape(Cream) {
        moveTo(0.4f * w, 0.17f * h)
        arcTo(Rect(0.4f * w, 0.09f * h, 0.6f * w, 0.25f * h), 180f, 180f, false)
        close()
    }
    line(0.5f, 0.17f, 0.56f, 0.12f, 0.8f, t.accent)
    rect(0.71f, 0.38f, 0.75f, 0.54f, Ink.mix(Metal, 0.5f))
    for (i in 0..3) circle(0.73f, 0.41f + i * 0.035f, 0.008f, if (i == 1) t.accent else Cream, line = false)
}

private fun Pen.road() {
    outdoors(0.5f)
    hills(0.52f, t.ground.mix(Color.White, 0.2f), 0.05f)
    rect(0f, 0.55f, 1f, 1f, t.ground, line = false)
    poly(Ink.mix(Stone, 0.45f), 0.47f, 0.55f, 0.53f, 0.55f, 0.95f, 1f, 0.05f, 1f)
    for (i in 0..3) {
        val y0 = 0.58f + i * i * 0.03f + i * 0.04f
        line(0.5f, y0, 0.5f, y0 + 0.02f + i * 0.015f, 0.8f + i * 0.3f, Snow)
    }
    pine(0.12f, 0.62f, 0.2f, Leaf)
    pine(0.86f, 0.64f, 0.24f, Leaf.mix(Ink, 0.2f))
}

private fun Pen.street(r: Random) {
    outdoors(0.5f)
    // Houses on both sides running to a point, lamps, cobbles.
    val left = listOf(0f to 0.05f, 0.18f to 0.18f, 0.3f to 0.3f, 0.38f to 0.38f)
    for ((i, p) in left.withIndex()) {
        val (x0, top) = p
        val x1 = if (i < left.size - 1) left[i + 1].first else 0.44f
        poly(listOf(Brick, t.wall, t.accent.mix(Cream, 0.4f), Stone)[i % 4], x0, top + r.nextFloat() * 0.04f, x1, top + 0.08f, x1, 0.6f + (0.44f - x1) * 0.6f, x0, 0.6f + (0.44f - x0) * 0.9f)
        val wx = (x0 + x1) / 2
        rect(wx - 0.02f, top + 0.16f, wx + 0.02f, top + 0.24f, Color(0xFFFFE9A8))
        val rx0 = 1f - x1
        val rx1 = 1f - x0
        poly(listOf(Stone, Brick, t.wall, t.accent.mix(Cream, 0.4f))[i % 4], rx0, top + 0.08f, rx1, top + r.nextFloat() * 0.04f, rx1, 0.6f + (0.44f - x0) * 0.9f, rx0, 0.6f + (0.44f - x1) * 0.6f)
        rect(1f - wx - 0.02f, top + 0.16f, 1f - wx + 0.02f, top + 0.24f, Color(0xFFFFE9A8))
    }
    poly(Stone.mix(t.ground, 0.4f), 0.44f, 0.6f, 0.56f, 0.6f, 1f, 1f, 0f, 1f)
    for (i in 0..5) line(0.5f - i * 0.09f, 0.62f + i * 0.07f, 0.5f + i * 0.09f, 0.62f + i * 0.07f, 0.35f)
    line(0.2f, 0.3f, 0.2f, 0.9f, 1.1f, Ink)
    circle(0.2f, 0.29f, 0.025f, Color(0xFFFFE9A8))
}

private fun Pen.city(r: Random) {
    outdoors(0.62f)
    // A skyline in two depths, lit windows, a square in front.
    var x = -0.02f
    while (x < 1f) {
        val bw = 0.08f + r.nextFloat() * 0.08f
        val top = 0.18f + r.nextFloat() * 0.25f
        rect(x, top, x + bw, 0.64f, t.deep.mix(t.sky, 0.45f))
        x += bw
    }
    x = -0.03f
    while (x < 1f) {
        val bw = 0.1f + r.nextFloat() * 0.1f
        val top = 0.32f + r.nextFloat() * 0.2f
        rect(x, top, x + bw, 0.66f, t.wall.mix(t.deep, 0.25f))
        var wy = top + 0.04f
        while (wy < 0.6f) {
            var wx = x + 0.02f
            while (wx < x + bw - 0.03f) {
                if (r.nextFloat() < 0.55f) rect(wx, wy, wx + 0.015f, wy + 0.03f, Color(0xFFFFE9A8), line = false)
                wx += 0.035f
            }
            wy += 0.06f
        }
        x += bw
    }
    rect(0f, 0.66f, 1f, 1f, Stone.mix(t.ground, 0.4f))
    oval(0.3f, 0.7f, 0.7f, 0.8f, Glass)
}

private fun Pen.roof(r: Random) {
    grad(t.deep.mix(t.sky, 0.4f), t.sky2, 0f, 0.7f, 0f, 1f)
    stars(r.nextInt(), 18, 0f, 0.4f)
    circle(0.8f, 0.16f, 0.06f, Color(0xFFFFF0B8))
    // Roofs beyond, then the ridge you stand on, its chimney.
    for (i in 0..3) {
        val x = i * 0.27f
        poly(t.deep.mix(t.wall, 0.3f), x - 0.04f, 0.62f, x + 0.12f, 0.46f + r.nextFloat() * 0.05f, x + 0.28f, 0.62f)
        rect(x + 0.1f, 0.5f, x + 0.13f, 0.58f, Color(0xFFFFE9A8), line = false)
    }
    rect(0f, 0.62f, 1f, 0.7f, t.deep.mix(t.wall, 0.4f), line = false)
    poly(Brick, 0f, 0.86f, 1f, 0.84f, 1f, 1f, 0f, 1f)
    for (i in 0..10) line(i / 10f, 0.86f, i / 10f - 0.04f, 1f, 0.4f)
    rect(0.74f, 0.66f, 0.84f, 0.86f, Brick.mix(Ink, 0.15f))
}

private fun Pen.bridge() {
    outdoors(0.62f)
    hills(0.6f, t.ground.mix(Color.White, 0.2f), 0.05f)
    waves(0.66f, t.accent.mix(Glass, 0.5f), 10, 0.012f)
    // An arch under the deck, rails along it.
    shape(Stone) {
        moveTo(0f, 0.74f * h); lineTo(w, 0.74f * h); lineTo(w, h)
        lineTo(0.82f * w, h); cubicTo(0.8f * w, 0.8f * h, 0.2f * w, 0.8f * h, 0.18f * w, h)
        lineTo(0f, h); close()
    }
    rect(0f, 0.7f, 1f, 0.76f, Stone.mix(Ink, 0.1f))
    line(0f, 0.6f, 1f, 0.6f, 1.2f, Wood)
    for (i in 0..10) line(i / 10f, 0.6f, i / 10f, 0.7f, 0.8f, Wood)
}

private fun Pen.garden(r: Random) {
    outdoors(0.56f)
    tree(0.82f, 0.62f, 0.5f, Leaf)
    rect(0f, 0.56f, 1f, 1f, t.ground, line = false)
    // A hedge, a fence, flowers all over.
    rect(0f, 0.5f, 0.6f, 0.62f, Leaf.mix(Ink, 0.15f))
    for (i in 0..10) {
        val x = i * 0.1f
        poly(Cream, x, 0.66f, x + 0.03f, 0.62f, x + 0.06f, 0.66f, x + 0.06f, 0.74f, x, 0.74f)
    }
    line(0f, 0.69f, 1f, 0.69f, 0.9f)
    repeat(26) {
        val x = r.nextFloat()
        val y = 0.76f + r.nextFloat() * 0.22f
        circle(x, y, 0.012f, listOf(t.accent, Color(0xFFE3B23C), Snow, Color(0xFFE08AA6))[r.nextInt(4)])
    }
}

private fun Pen.forest(r: Random) {
    grad(t.sky, t.deep, 0f, 1f)
    // Far trunks pale, near ones dark and big, a canopy over all.
    repeat(9) {
        val x = r.nextFloat()
        rect(x, 0f, x + 0.03f, 0.72f, t.deep.mix(t.sky, 0.55f), line = false)
    }
    rect(0f, 0.72f, 1f, 1f, t.ground.mix(Leaf, 0.4f), line = false)
    line(0f, 0.72f, 1f, 0.72f, 0.8f)
    for (x in listOf(0.06f, 0.3f, 0.7f, 0.92f)) {
        val wd = 0.05f + r.nextFloat() * 0.04f
        poly(Wood.mix(Ink, 0.3f), x - wd, 0f, x + wd, 0f, x + wd * 0.9f, 0.9f, x + wd * 1.6f, 0.96f, x - wd * 1.6f, 0.96f, x - wd * 0.9f, 0.9f)
    }
    repeat(5) {
        val x = r.nextFloat()
        oval(x - 0.22f, -0.12f, x + 0.22f, 0.16f, Leaf.mix(t.deep, 0.35f))
    }
}

private fun Pen.field(r: Random) {
    outdoors(0.66f)
    cloud(0.25f, 0.24f, 0.06f)
    cloud(0.72f, 0.14f, 0.045f)
    hills(0.66f, t.ground.mix(Color.White, 0.25f), 0.07f, 0.5f)
    hills(0.74f, t.ground, 0.04f, 1.2f)
    repeat(30) {
        val x = r.nextFloat()
        val y = 0.78f + r.nextFloat() * 0.2f
        line(x, y, x - 0.01f, y - 0.03f, 0.6f, Leaf.mix(Ink, 0.2f))
        line(x, y, x + 0.01f, y - 0.03f, 0.6f, Leaf.mix(Ink, 0.2f))
    }
}

private fun Pen.mountain() {
    outdoors(0.7f)
    poly(t.deep.mix(t.sky, 0.5f), -0.1f, 0.75f, 0.2f, 0.25f, 0.45f, 0.75f)
    poly(t.deep.mix(Stone, 0.4f), 0.15f, 0.8f, 0.55f, 0.08f, 0.95f, 0.8f)
    poly(Snow, 0.47f, 0.22f, 0.55f, 0.08f, 0.63f, 0.22f, 0.58f, 0.2f, 0.55f, 0.24f, 0.51f, 0.2f)
    poly(t.deep.mix(t.sky, 0.3f), 0.6f, 0.8f, 0.85f, 0.38f, 1.1f, 0.8f)
    // The rock in front you stand on.
    poly(Stone.mix(t.ground, 0.3f), 0f, 0.82f, 0.3f, 0.78f, 0.7f, 0.8f, 1f, 0.84f, 1f, 1f, 0f, 1f)
}

private fun Pen.cave() {
    fill(t.deep)
    // Light at the mouth behind; rock all round; teeth from the roof.
    oval(0.25f, 0.2f, 0.75f, 0.85f, t.sky2.mix(t.accent, 0.2f), line = false)
    d.drawCircle(Brush.radialGradient(listOf(t.sky2.copy(alpha = 0.6f), Color.Transparent), center = o(0.5f, 0.5f), radius = 0.4f * w), 0.4f * w, o(0.5f, 0.5f))
    shape(Stone.mix(t.deep, 0.55f)) {
        moveTo(0f, 0f); lineTo(w, 0f); lineTo(w, h); lineTo(0f, h); close()
        moveTo(0.5f * w, 0.18f * h)
        cubicTo(0.9f * w, 0.18f * h, 0.85f * w, 0.9f * h, 0.8f * w, h)
        lineTo(0.2f * w, h)
        cubicTo(0.15f * w, 0.9f * h, 0.1f * w, 0.18f * h, 0.5f * w, 0.18f * h); close()
        fillType = PathFillType.EvenOdd
    }
    for (i in 0..5) {
        val x = 0.22f + i * 0.11f
        poly(Stone.mix(t.deep, 0.4f), x - 0.03f, 0.15f, x + 0.03f, 0.15f, x, 0.26f + (i % 3) * 0.04f)
    }
    rect(0f, 0.86f, 1f, 1f, Stone.mix(t.deep, 0.35f))
}

private fun Pen.beach() {
    outdoors(0.55f)
    circle(0.78f, 0.22f, 0.08f, Color(0xFFFFD98A))
    waves(0.55f, t.accent.mix(Glass, 0.45f), 8, 0.01f, 0.72f)
    waves(0.68f, Snow, 6, 0.012f, 0.74f)
    poly(Color(0xFFEBD3A1), 0f, 0.72f, 1f, 0.7f, 1f, 1f, 0f, 1f)
    line(0.16f, 0.4f, 0.22f, 0.9f, 1.1f, Wood)
    shape(t.accent) {
        moveTo(0.04f * w, 0.44f * h)
        quadraticTo(0.15f * w, 0.26f * h, 0.3f * w, 0.36f * h)
        close()
    }
}

private fun Pen.sea() {
    outdoors(0.5f)
    cloud(0.7f, 0.2f, 0.05f)
    waves(0.5f, t.accent.mix(Glass, 0.3f), 9, 0.01f)
    waves(0.66f, t.accent.mix(t.deep, 0.2f), 7, 0.018f)
    waves(0.84f, t.accent.mix(t.deep, 0.45f), 5, 0.025f)
    // A raft to stand on.
    poly(Wood, 0.12f, 0.86f, 0.88f, 0.86f, 0.84f, 0.94f, 0.16f, 0.94f)
    for (i in 1..5) line(0.12f + i * 0.13f, 0.86f, 0.12f + i * 0.13f, 0.94f, 0.5f)
}

private fun Pen.underwater(r: Random) {
    grad(t.accent.mix(Glass, 0.5f), t.deep, 0f, 1f)
    // Light from above, weed, bubbles, a fish or two.
    for (i in 0..3) poly(Color.White.copy(alpha = 0.12f), 0.1f + i * 0.25f, 0f, 0.2f + i * 0.25f, 0f, 0.05f + i * 0.25f, 1f, -0.05f + i * 0.25f, 1f, line = false)
    poly(Color(0xFFD8C49A).mix(t.deep, 0.3f), 0f, 0.9f, 0.3f, 0.86f, 0.7f, 0.9f, 1f, 0.86f, 1f, 1f, 0f, 1f)
    for (x in listOf(0.08f, 0.16f, 0.84f, 0.92f)) {
        shape(Leaf.mix(t.deep, 0.2f)) {
            moveTo(x * w, 0.92f * h)
            cubicTo((x - 0.06f) * w, 0.7f * h, (x + 0.06f) * w, 0.6f * h, x * w, 0.42f * h)
            cubicTo((x + 0.05f) * w, 0.6f * h, (x - 0.02f) * w, 0.7f * h, (x + 0.03f) * w, 0.92f * h)
            close()
        }
    }
    repeat(12) { circle(r.nextFloat(), r.nextFloat() * 0.8f, 0.006f + r.nextFloat() * 0.01f, Color.White.copy(alpha = 0.5f)) }
    fish(0.72f, 0.3f, 0.06f, Color(0xFFE3B23C))
    fish(0.3f, 0.2f, 0.04f, t.accent.mix(Snow, 0.3f))
}

private fun Pen.fish(x: Float, y: Float, s: Float, c: Color) {
    oval(x - s, y - s * 0.5f, x + s, y + s * 0.5f, c)
    poly(c, x + s * 0.8f, y, x + s * 1.5f, y - s * 0.5f, x + s * 1.5f, y + s * 0.5f)
    circle(x - s * 0.5f, y - s * 0.1f, s * 0.12f, Ink, line = false)
}

private fun Pen.sky(r: Random) {
    grad(t.sky, t.sky2, 0f, 1f)
    cloud(0.2f, 0.25f, 0.05f)
    cloud(0.78f, 0.4f, 0.07f)
    repeat(3) { star(0.1f + r.nextFloat() * 0.8f, 0.05f + r.nextFloat() * 0.2f, 0.018f, Color(0xFFFFE58A)) }
    // The cloud underfoot.
    cloud(0.5f, 0.95f, 0.16f)
}

private fun Pen.space(r: Random) {
    grad(Color(0xFF0B0E22), t.deep.mix(Color(0xFF2A1F55), 0.5f), 0f, 1f)
    stars(r.nextInt(), 60, 0f, 0.8f)
    // A ringed planet, a far moon, the grey ground of somewhere.
    circle(0.74f, 0.3f, 0.12f, t.accent.mix(Color(0xFFE3B23C), 0.4f))
    d.drawOval(Ink, o(0.56f, 0.27f), Size(0.36f * w, 0.08f * h), style = Stroke(lw * 1.2f))
    circle(0.2f, 0.2f, 0.035f, Snow)
    shape(Stone.mix(t.deep, 0.2f)) {
        moveTo(0f, h); lineTo(0f, 0.86f * h)
        quadraticTo(0.5f * w, 0.74f * h, w, 0.86f * h)
        lineTo(w, h); close()
    }
    oval(0.2f, 0.9f, 0.32f, 0.94f, Stone.mix(Ink, 0.3f))
    oval(0.7f, 0.86f, 0.78f, 0.89f, Stone.mix(Ink, 0.3f))
}

private fun Pen.castle() {
    outdoors(0.66f)
    cloud(0.15f, 0.18f, 0.04f)
    hills(0.7f, t.ground.mix(Leaf, 0.3f), 0.1f, 0.4f)
    // Towers with pointed roofs and flags.
    rect(0.3f, 0.32f, 0.7f, 0.68f, Stone)
    for (i in 0..4) rect(0.3f + i * 0.085f, 0.28f, 0.34f + i * 0.085f, 0.32f, Stone)
    for (x in listOf(0.24f, 0.76f)) {
        rect(x - 0.06f, 0.2f, x + 0.06f, 0.7f, Stone.mix(Snow, 0.15f))
        poly(t.accent, x - 0.08f, 0.2f, x, 0.04f, x + 0.08f, 0.2f)
        line(x, 0.04f, x, -0.02f, 0.8f)
        poly(Color(0xFFE3B23C), x, -0.02f, x + 0.06f, 0.0f, x, 0.02f)
    }
    shape(t.deep) {
        moveTo(0.44f * w, 0.68f * h); lineTo(0.44f * w, 0.52f * h)
        quadraticTo(0.5f * w, 0.42f * h, 0.56f * w, 0.52f * h); lineTo(0.56f * w, 0.68f * h); close()
    }
    poly(Stone.mix(t.ground, 0.5f), 0.44f, 0.68f, 0.56f, 0.68f, 0.7f, 1f, 0.3f, 1f, line = false)
}

private fun Pen.temple() {
    fill(t.wall.mix(t.deep, 0.35f))
    // A rose window, arches on columns, a long floor.
    circle(0.5f, 0.2f, 0.12f, t.accent)
    for (i in 0 until 8) {
        val a = Math.PI * i / 4
        line(0.5f, 0.2f, 0.5f + 0.12f * kotlin.math.cos(a).toFloat(), 0.2f + 0.12f * kotlin.math.sin(a).toFloat() * w / h, 0.7f)
    }
    circle(0.5f, 0.2f, 0.04f, Color(0xFFE3B23C))
    for (i in 0..3) {
        val x = 0.04f + i * 0.3f
        rect(x, 0.36f, x + 0.04f, 0.8f, Stone)
        if (i < 3) shape(Color.Transparent) {
            moveTo((x + 0.04f) * w, 0.46f * h)
            quadraticTo((x + 0.17f) * w, 0.3f * h, (x + 0.3f) * w, 0.46f * h)
        }
    }
    poly(Stone.mix(t.ground, 0.4f), 0f, 0.8f, 1f, 0.8f, 1f, 1f, 0f, 1f)
    for (i in 0..6) line(i / 6f, 0.8f, 0.5f + (i / 6f - 0.5f) * 1.6f, 1f, 0.4f)
}

private fun Pen.void(r: Random) {
    grad(t.deep, t.sky, 0f, 1f)
    // A checker floor that fades away, a door standing on nothing, stairs to nowhere.
    for (row in 0..5) {
        val y0 = 0.62f + row * row * 0.012f + row * 0.03f
        val y1 = 0.62f + (row + 1) * (row + 1) * 0.012f + (row + 1) * 0.03f
        for (col in -6..6) {
            if ((row + col) % 2 != 0) continue
            val sx0 = 0.5f + col * (0.06f + row * 0.03f)
            val sx1 = 0.5f + (col + 1) * (0.06f + row * 0.03f)
            val tx0 = 0.5f + col * (0.06f + (row + 1) * 0.03f)
            val tx1 = 0.5f + (col + 1) * (0.06f + (row + 1) * 0.03f)
            poly(t.wall.copy(alpha = 0.25f + row * 0.1f), sx0, y0, sx1, y0, tx1, y1, tx0, y1, line = false)
        }
    }
    door(0.12f, 0.36f, 0.24f, 0.62f, t.accent, open = r.nextBoolean())
    for (i in 0..3) rect(0.7f + i * 0.05f, 0.5f - i * 0.08f, 0.82f + i * 0.05f, 0.54f - i * 0.08f, t.wall.mix(t.deep, 0.2f))
    circle(0.5f, 0.18f, 0.05f, t.accent.mix(Snow, 0.5f))
}

/** The last panel of every page: your own bed, the window going pale. */
fun Pen.bedroom() {
    room(0.1f, 0.1f, 0.9f, 0.6f)
    window(0.62f, 0.16f, 0.82f, 0.42f, Color(0xFFF7C9A0))
    rect(0.16f, 0.2f, 0.32f, 0.36f, t.accent.mix(Cream, 0.5f))
    oval(0.2f, 0.24f, 0.28f, 0.32f, t.sky2)
}

/** Mid-air speed lines, a fall's or a flight's. */
fun Pen.speed(vertical: Boolean, seed: Int) {
    val r = Random(seed)
    repeat(9) {
        val a = r.nextFloat()
        val b = r.nextFloat()
        if (vertical) line(a, b * 0.6f, a, b * 0.6f + 0.15f, 0.6f, Ink.copy(alpha = 0.55f))
        else line(b * 0.6f, a, b * 0.6f + 0.15f, a, 0.6f, Ink.copy(alpha = 0.55f))
    }
}

