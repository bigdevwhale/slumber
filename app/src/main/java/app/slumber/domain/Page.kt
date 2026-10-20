package app.slumber.domain

import kotlin.math.roundToInt

/** A finger drawing in a panel: strokes of points, each 0..1 across the panel. */
object Doodle {
    data class P(val x: Float, val y: Float)

    fun encode(strokes: List<List<P>>): String = strokes.filter { it.size > 1 }.joinToString(";") { s ->
        s.joinToString(" ") { "${(it.x * 1000).roundToInt()},${(it.y * 1000).roundToInt()}" }
    }

    fun decode(s: String): List<List<P>> = if (s.isBlank()) emptyList() else s.split(';').map { stroke ->
        stroke.split(' ').mapNotNull { p ->
            val (x, y) = p.split(',').takeIf { it.size == 2 } ?: return@mapNotNull null
            val fx = x.toIntOrNull() ?: return@mapNotNull null
            val fy = y.toIntOrNull() ?: return@mapNotNull null
            P(fx / 1000f, fy / 1000f)
        }
    }.filter { it.size > 1 }

    /** A stroke that ends near where it began is a shape, and gets filled. */
    fun closed(s: List<P>): Boolean {
        if (s.size < 6) return false
        val a = s.first()
        val b = s.last()
        val dx = a.x - b.x
        val dy = a.y - b.y
        return dx * dx + dy * dy < 0.08f * 0.08f
    }
}

/** How a page's panels sit in rows, McCay-fashion: wide ones and pairs. The waking panel comes after. */
object Layout {
    fun rows(n: Int): List<Int> = when (n) {
        0 -> emptyList()
        1 -> listOf(1)
        2 -> listOf(2)
        3 -> listOf(1, 2)
        4 -> listOf(2, 2)
        5 -> listOf(2, 1, 2)
        6 -> listOf(2, 2, 2)
        else -> List(n / 2) { 2 } + if (n % 2 == 1) listOf(1) else emptyList()
    }
}

/**
 * Nemo's bed: the more mornings you bring a dream back, the more it wakes up, grows legs and walks out
 * across the atlas. It never loses them; a week without dreams and it just sleeps.
 */
object Bed {
    val steps = listOf(0, 3, 7, 14, 30)

    fun stage(mornings: Int): Int = steps.indexOfLast { mornings >= it }.coerceAtLeast(0)

    /** Mornings until the next stage, or null at the last one. */
    fun toNext(mornings: Int): Int? = steps.getOrNull(stage(mornings) + 1)?.let { it - mornings }

    /** How much of the atlas is out of the fog, 0..1. */
    fun reach(mornings: Int): Float = (0.35f + 0.13f * stage(mornings)).coerceAtMost(1f)
}
