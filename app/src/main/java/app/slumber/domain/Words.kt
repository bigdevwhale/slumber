package app.slumber.domain

/**
 * The four layers a panel is made of. Each is a short list on purpose: dreams are endless, but the
 * picture only has to say where, who, doing what, and how it felt. The words carry the rest.
 */

/** Where: a place archetype. [open] places have sky and ground; indoor ones have walls. */
enum class Scene(val open: Boolean = false) {
    HOUSE(true), ROOM, CORRIDOR, STAIRS, KITCHEN, BATHROOM, SCHOOL, OFFICE, HOSPITAL, SHOP,
    STATION, TRAIN, ELEVATOR, CAR,
    STREET(true), CITY(true), ROOF(true), BRIDGE(true), GARDEN(true), FOREST(true), FIELD(true),
    MOUNTAIN(true), CAVE, BEACH(true), SEA(true), UNDERWATER, SKY(true), SPACE(true),
    CASTLE(true), TEMPLE, VOID;

    val wet: Boolean get() = this == SEA || this == UNDERWATER

    companion object {
        fun of(s: String?): Scene = entries.firstOrNull { it.name == s } ?: ROOM
    }
}

/** What happens: the pose of the dreamer, and how the others answer it. */
enum class Act {
    STAND, WALK, RUN, CHASED, FALL, FLY, FLOAT, SWIM, SINK, CLIMB, HIDE, SEARCH, LOST, TALK, SHOUT,
    SIT, SLEEP, CRY, LAUGH, HUG, FIGHT, DANCE, OPEN, DRIVE, FROZEN, GROW, SHRINK, LOOK;

    companion object {
        fun of(s: String?): Act = entries.firstOrNull { it.name == s } ?: STAND
    }
}

/** How it felt: the colour of the panel and the shape of its frame. */
enum class Mood {
    CALM, JOY, WONDER, FEAR, ANXIETY, SADNESS, ANGER, STRANGE;

    /** A dream like this can be given a new ending. */
    val dark: Boolean get() = this == FEAR || this == ANXIETY

    companion object {
        fun of(s: String?): Mood = entries.firstOrNull { it.name == s } ?: WONDER
    }
}

/** Nameless figures: they come and go and aren't kept. [VOICE] has no body, only a bubble. */
enum class Kind { SOMEONE, CROWD, SHADOW, CHILD, ANIMAL, BIRD, CREATURE, VOICE;

    companion object {
        fun of(s: String?): Kind = entries.firstOrNull { it.name == s } ?: SOMEONE
    }
}

/** Who is in a panel: someone from the cast, or a nameless extra with an optional label ("a man at the door"). */
sealed interface Fig {
    data class Cast(val id: Long) : Fig
    data class Extra(val kind: Kind, val label: String = "") : Fig

    companion object {
        fun encode(figs: List<Fig>): String = figs.joinToString("|") {
            when (it) {
                is Cast -> "c:${it.id}"
                is Extra -> "x:${it.kind.name}:${it.label.replace(Regex("[|:]"), " ").trim()}"
            }
        }

        fun decode(s: String): List<Fig> = s.split('|').mapNotNull { part ->
            val bits = part.split(':', limit = 3)
            when (bits.getOrNull(0)) {
                "c" -> bits.getOrNull(1)?.toLongOrNull()?.let(::Cast)
                "x" -> Extra(Kind.of(bits.getOrNull(1)), bits.getOrNull(2).orEmpty())
                else -> null
            }
        }
    }
}

enum class Base { HUMAN, ANIMAL, BIRD, THING, BLOB }
enum class Age { CHILD, ADULT, OLD }
enum class Hair { NONE, SHORT, LONG, BUN, CURLY, HAT }
enum class Trait { FACELESS, GLOWS, GIANT, DOUBLE, GLASSES, BEARD }

/**
 * What someone from the cast looks like, built once from a few choices and recognisable in every dream
 * after. [tall] 0..2, [coat] a clothes colour, [skin] and [hairTone] 0..3.
 */
data class Look(
    val base: Base = Base.HUMAN,
    val tall: Int = 1,
    val age: Age = Age.ADULT,
    val hair: Hair = Hair.SHORT,
    val coat: Int = 1,
    val skin: Int = 0,
    val hairTone: Int = 0,
    val traits: Set<Trait> = emptySet(),
) {
    fun encode(): String = listOf(base.name, tall, age.name, hair.name, coat, skin, hairTone, traits.joinToString(",") { it.name }).joinToString(";")

    fun has(t: Trait) = t in traits
    fun toggle(t: Trait) = copy(traits = if (t in traits) traits - t else traits + t)

    companion object {
        const val COATS = 10

        fun decode(s: String?): Look {
            val b = s?.split(';') ?: return Look()
            fun <T : Enum<T>> pick(all: Array<T>, i: Int, def: T) = all.firstOrNull { it.name == b.getOrNull(i) } ?: def
            return Look(
                base = pick(Base.entries.toTypedArray(), 0, Base.HUMAN),
                tall = b.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 2) ?: 1,
                age = pick(Age.entries.toTypedArray(), 2, Age.ADULT),
                hair = pick(Hair.entries.toTypedArray(), 3, Hair.SHORT),
                coat = b.getOrNull(4)?.toIntOrNull()?.coerceIn(0, COATS - 1) ?: 1,
                skin = b.getOrNull(5)?.toIntOrNull()?.coerceIn(0, 3) ?: 0,
                hairTone = b.getOrNull(6)?.toIntOrNull()?.coerceIn(0, 3) ?: 0,
                traits = b.getOrNull(7).orEmpty().split(',').mapNotNull { t -> Trait.entries.firstOrNull { it.name == t } }.toSet(),
            )
        }

        /** How a nameless extra looks. */
        fun of(k: Kind): Look = when (k) {
            Kind.SOMEONE -> Look(hair = Hair.HAT, coat = 7)
            Kind.CROWD -> Look(coat = 7, hair = Hair.SHORT)
            Kind.SHADOW -> Look(coat = 8, hair = Hair.NONE, skin = 3, traits = setOf(Trait.FACELESS))
            Kind.CHILD -> Look(age = Age.CHILD, coat = 3, hair = Hair.CURLY, hairTone = 2)
            Kind.ANIMAL -> Look(base = Base.ANIMAL, coat = 6)
            Kind.BIRD -> Look(base = Base.BIRD, coat = 8)
            Kind.CREATURE -> Look(base = Base.BLOB, coat = 4)
            Kind.VOICE -> Look(base = Base.THING)
        }

        /** The dreamer before they've dressed themselves: Nemo's white nightshirt. */
        val Dreamer = Look(hair = Hair.CURLY, coat = 5, hairTone = 1)
    }
}
