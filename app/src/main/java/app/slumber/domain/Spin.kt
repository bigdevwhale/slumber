package app.slumber.domain

import java.time.LocalDate

/** How clear a dream was: not at all, a glimmer of "is this a dream?", or fully knowing. */
enum class Clarity {
    NONE, GLIMMER, LUCID;

    companion object {
        fun of(i: Int): Clarity = entries.getOrElse(i) { NONE }
    }
}

/** One day of practice, as the top sees it. */
data class Practice(val day: LocalDate, val dreams: Int, val checks: Int, val ritual: Boolean, val best: Clarity)

/**
 * The top's wind-up. Each of the last seven days adds to it: a dream remembered, the day's checks (up to
 * three count), the bedtime ritual, and most of all a glimmer or a lucid dream. Days further back count
 * less, so a missed day loosens the top a little and nothing more. A wound-up top spins for longer.
 */
object Spin {
    const val MIN_S = 5f
    const val MAX_S = 75f

    fun windUp(days: List<Practice>, today: LocalDate): Float {
        var sum = 0f
        for (p in days) {
            val ago = today.toEpochDay() - p.day.toEpochDay()
            if (ago !in 0..6) continue
            val weight = 1f - ago / 8f
            var s = 0f
            if (p.dreams > 0) s += 1f
            s += minOf(p.checks, 3) / 3f
            if (p.ritual) s += 1f
            s += when (p.best) { Clarity.LUCID -> 2f; Clarity.GLIMMER -> 1f; Clarity.NONE -> 0f }
            sum += s * weight
        }
        // A full week of dreams, checks and rituals winds it nearly all the way; glimmers and lucid dreams finish it.
        return (sum / 14f).coerceIn(0f, 1f)
    }

    /** How long the top spins, in seconds. */
    fun seconds(windUp: Float): Float = MIN_S + (MAX_S - MIN_S) * windUp * windUp.coerceAtLeast(0.35f)

    /**
     * After a lucid dream the top keeps spinning all day without falling, the way it never falls in a
     * dream. Until the next morning.
     */
    fun endless(lastLucid: LocalDate?, today: LocalDate): Boolean = lastLucid == today
}
