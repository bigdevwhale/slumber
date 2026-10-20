package app.slumber.ui

import androidx.compose.runtime.Immutable
import app.slumber.data.Check
import app.slumber.data.Dream
import app.slumber.data.OwnSign
import app.slumber.data.Ritual
import app.slumber.data.Settings
import app.slumber.data.morningOf
import app.slumber.domain.Clarity
import app.slumber.domain.Practice
import app.slumber.domain.Sign
import app.slumber.domain.Signs
import app.slumber.domain.Spin
import java.time.LocalDate
import java.time.ZoneId

/** Everything stored, plus what is derived from it. Screens read through here. */
@Immutable
class Book(
    val dreams: List<Dream>,
    val checks: List<Check>,
    val rituals: List<Ritual>,
    val ownSigns: List<OwnSign>,
    val settings: Settings,
    val ru: Boolean,
    now: Long = System.currentTimeMillis(),
) {
    val zone: ZoneId = ZoneId.systemDefault()
    val today: LocalDate = morningOf(now, zone)

    fun dream(id: Long) = dreams.firstOrNull { it.id == id }

    // Signs: the built-in ones and your own.
    val own: List<Sign> = ownSigns.map { Signs.own(it.id, it.phrase) }
    val signs: List<Sign> = Signs.all + own
    private val texts = dreams.map { it.text }

    /** Your dream signs, most frequent first. Your own phrases count from the first dream that shows them. */
    val yours: List<Pair<Sign, Int>> = (Signs.yours(texts, Signs.all) + own.map { s -> s to texts.count { Signs.shows(s, it) } })
        .sortedByDescending { it.second }

    fun found(d: Dream): List<Sign> = Signs.found(d.text, signs)
    fun dreamsWith(s: Sign): List<Dream> = dreams.filter { Signs.shows(s, it.text) }
    fun sign(key: String): Sign? = signs.firstOrNull { it.key == key }

    /** Tonight's sign for the ritual: the one you picked, or else your most frequent. */
    val tonight: Sign? = sign(settings.sign) ?: yours.firstOrNull()?.first

    // Practice, day by day.
    private val dreamsByDay = dreams.groupBy { it.day(zone) }
    private val checksByDay = checks.groupBy { morningOf(it.at, zone) }
    private val ritualDays = rituals.map { morningOf(it.at, zone) }.toSet()

    fun practice(day: LocalDate) = Practice(
        day,
        dreamsByDay[day].orEmpty().size,
        checksByDay[day].orEmpty().size,
        day in ritualDays,
        dreamsByDay[day].orEmpty().maxOfOrNull { it.clarityOf } ?: Clarity.NONE,
    )

    val week: List<Practice> = (6 downTo 0).map { practice(today.minusDays(it.toLong())) }
    val windUp: Float = Spin.windUp(week, today)
    val seconds: Float = Spin.seconds(windUp)

    val lucid: Int = dreams.count { it.clarityOf == Clarity.LUCID }
    val glimmers: Int = dreams.count { it.clarityOf == Clarity.GLIMMER }
    private val lastLucid: LocalDate? = dreams.filter { it.clarityOf == Clarity.LUCID }.maxOfOrNull { it.day(zone) }

    /** A lucid dream this morning: today the top doesn't fall. */
    val endless: Boolean = Spin.endless(lastLucid, today)

    val dreamtToday: Boolean get() = dreamsByDay[today].orEmpty().isNotEmpty()
    val ritualToday: Boolean get() = today in ritualDays
    val checksToday: Int get() = checksByDay[today].orEmpty().size

    /** The last dream before today, to recall at bedtime. */
    val lastDream: Dream? = dreams.firstOrNull()
}
