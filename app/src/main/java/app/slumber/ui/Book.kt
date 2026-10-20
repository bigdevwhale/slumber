package app.slumber.ui

import androidx.compose.runtime.Immutable
import app.slumber.data.Dream
import app.slumber.data.Panel
import app.slumber.data.Person
import app.slumber.data.Settings
import app.slumber.data.Spot
import app.slumber.domain.Bed
import app.slumber.domain.Fig
import app.slumber.domain.Kind
import app.slumber.domain.Look
import app.slumber.domain.Mood
import app.slumber.domain.Named
import app.slumber.ui.art.Body
import app.slumber.ui.art.Shot
import java.time.LocalDate
import java.time.ZoneId

/** Everything stored, plus what is derived from it. Screens read through here. */
@Immutable
class Book(
    val dreams: List<Dream>,
    panels: List<Panel>,
    val spots: List<Spot>,
    val people: List<Person>,
    val settings: Settings,
    val ru: Boolean,
) {
    val zone: ZoneId = ZoneId.systemDefault()
    val today: LocalDate = LocalDate.now(zone)

    private val byDream = panels.groupBy { it.dreamId }
    private val byPerson = people.associateBy { it.id }
    private val bySpot = spots.associateBy { it.id }

    val me: Person? = people.firstOrNull { it.me }
    val meLook: Look get() = me?.lookOf ?: Look.Dreamer
    val cast: List<Person> get() = people.filter { !it.me }

    fun dream(id: Long) = dreams.firstOrNull { it.id == id }
    fun spot(id: Long) = bySpot[id]
    fun person(id: Long) = byPerson[id]

    /** A page's panels: the new ending if there is one (unless [original]), else the dream as it was. */
    fun panels(dreamId: Long, alt: Int): List<Panel> = byDream[dreamId].orEmpty().filter { it.alt == alt }.sortedBy { it.idx }
    fun hasAlt(dreamId: Long) = byDream[dreamId].orEmpty().any { it.alt == 1 }
    fun showing(dreamId: Long) = if (hasAlt(dreamId)) 1 else 0

    fun body(f: Fig): Body = when (f) {
        is Fig.Cast -> byPerson[f.id]?.let { Body(it.lookOf) } ?: Body(Look.of(Kind.SOMEONE), Kind.SOMEONE)
        is Fig.Extra -> Body(Look.of(f.kind), f.kind)
    }

    fun shot(p: Panel) = Shot(p.sceneOf, p.actOf, p.moodOf, p.figsOf.map(::body), p.doodle, (p.dreamId * 31 + p.idx * 7 + p.alt).toInt())

    /** The first panel, for a cover in lists. */
    fun cover(dreamId: Long): Shot? = panels(dreamId, showing(dreamId)).firstOrNull()?.let(::shot)

    // The bed and the fog.
    val mornings: Int = dreams.map { it.day(zone) }.distinct().size
    val stage: Int get() = Bed.stage(mornings)

    // The atlas.
    private val allPanels = panels
    fun dreamsAt(spotId: Long): List<Dream> = allPanels.filter { it.spotId == spotId }.map { it.dreamId }.distinct().mapNotNull(::dream)

    /** The mood that place is most often dreamt in. */
    fun moodAt(spotId: Long): Mood = allPanels.filter { it.spotId == spotId }.groupingBy { it.moodOf }.eachCount().maxByOrNull { it.value }?.key ?: Mood.WONDER

    /** Doors: one dream walking from one named place into another. Counted both ways as one. */
    val doors: Map<Pair<Long, Long>, Int> = buildMap {
        for ((_, ps) in byDream) {
            val seq = ps.filter { it.alt == 0 }.sortedBy { it.idx }.map { it.spotId }.filter { it != 0L }
            for ((a, b) in seq.zipWithNext()) {
                if (a == b) continue
                val k = if (a < b) a to b else b to a
                put(k, (get(k) ?: 0) + 1)
            }
        }
    }

    /** Where the bed stands: the place of the latest dream that had one. */
    val bedAt: Long? = dreams.firstNotNullOfOrNull { d -> byDream[d.id].orEmpty().lastOrNull { it.spotId != 0L }?.spotId }

    // The cast.
    fun appearances(personId: Long): List<Dream> = allPanels.filter { Fig.Cast(personId) in it.figsOf }.map { it.dreamId }.distinct().mapNotNull(::dream)

    /**
     * Someone nameless given the same label in two dreams or more ("my old teacher") who isn't in the cast
     * yet: they've come back, and could be kept. Label → the dreams.
     */
    val returning: Map<String, List<Dream>> = run {
        val names = people.map { it.name.trim().lowercase() }.toSet()
        allPanels.flatMap { p -> p.figsOf.filterIsInstance<Fig.Extra>().map { it.label.trim() to p.dreamId } }
            .filter { it.first.isNotEmpty() && it.first.lowercase() !in names }
            .groupBy({ it.first.lowercase() }, { it })
            .mapNotNull { (_, hits) ->
                val ds = hits.map { it.second }.distinct()
                if (ds.size >= 2) hits.first().first to ds.mapNotNull(::dream) else null
            }.toMap()
    }

    val named: List<Named> get() = people.map { Named(it.id, it.name) }
    val places: List<Named> get() = spots.map { Named(it.id, it.name, it.sceneOf) }

    /** A new ending drawn in the last two weeks: read it again at night, before sleep. */
    val toRehearse: Dream? get() = dreams.firstOrNull { it.rewritten > System.currentTimeMillis() - 14L * 86_400_000 && hasAlt(it.id) }
}
