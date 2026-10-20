package app.slumber

import androidx.test.core.app.ApplicationProvider
import app.slumber.domain.Act
import app.slumber.domain.Age
import app.slumber.domain.Base
import app.slumber.domain.Bed
import app.slumber.domain.Doodle
import app.slumber.domain.Fig
import app.slumber.domain.Guess
import app.slumber.domain.Hair
import app.slumber.domain.Kind
import app.slumber.domain.Layout
import app.slumber.domain.Look
import app.slumber.domain.Mood
import app.slumber.domain.Named
import app.slumber.domain.Scene
import app.slumber.domain.Split
import app.slumber.domain.Trait
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

class SplitTest {
    @Test fun `a breathless sentence becomes three panels`() {
        val s = "Я в бабушкином доме, но там бесконечный коридор, за мной идёт кто-то без лица, я открываю дверь и падаю в море, а там мой бывший начальник продаёт билеты."
        val chunks = Split.chunks(s)
        assertEquals(3, chunks.size)
        assertTrue(chunks.last().startsWith("А там"))
        // Every word survives, in order.
        assertEquals(s.replace(Regex("[^\\p{L}]"), "").lowercase(), chunks.joinToString("").replace(Regex("[^\\p{L}]"), "").lowercase())
    }

    @Test fun `sentences and connectors cut, scraps join`() {
        val c = Split.chunks("I was at school. Then suddenly the walls fell away and I was flying. Odd!")
        assertTrue(c.size in 2..3)
        assertTrue(c.none { it.split(' ').size < 3 })
    }

    @Test fun `never more than six`() {
        val long = (1..20).joinToString(" ") { "Sentence number $it is here." }
        assertEquals(Split.MAX, Split.chunks(long).size)
    }

    @Test fun `empty is empty`() = assertEquals(emptyList<String>(), Split.chunks("   "))
}

class GuessTest {
    @Test fun `places, acts and moods from words`() {
        assertEquals(Scene.SEA, Guess.scene("я открываю дверь и падаю в море"))
        assertEquals(Act.FALL, Guess.act("я открываю дверь и падаю в море"))
        assertEquals(Act.CHASED, Guess.act("за мной идёт кто-то без лица"))
        assertEquals(Mood.FEAR, Guess.mood("за мной идёт кто-то без лица"))
        assertEquals(listOf(Kind.SHADOW), Guess.kinds("за мной идёт кто-то без лица"))
        assertEquals(Scene.STATION, Guess.scene("I was late at the station"))
        assertEquals(Scene.STATION, Guess.scene("I was at the station, the train was leaving"))
        assertEquals(Scene.TRAIN, Guess.scene("I sat in a train"))
        assertEquals(Mood.ANXIETY, Guess.mood("I was late for an exam"))
        // "город" is a city, not a mountain.
        assertEquals(Scene.CITY, Guess.scene("гуляю по городу"))
    }

    @Test fun `the dreamer in every panel, the cast and atlas by name`() {
        val cast = listOf(Named(1, "Me"), Named(2, "Начальник"))
        val spots = listOf(Named(10, "Бабушкин дом", Scene.HOUSE))
        val d = Guess.panels(
            "Я в бабушкином доме, но там бесконечный коридор, за мной идёт кто-то без лица, я открываю дверь и падаю в море, а там мой бывший начальник продаёт билеты.",
            me = 1, cast = cast, spots = spots,
        )
        assertEquals(3, d.size)
        assertTrue(d.all { it.figs.first() == Fig.Cast(1) })
        assertEquals(10L, d[0].spotId)
        assertEquals(Scene.SEA, d[2].scene)
        assertTrue(Fig.Cast(2) in d[2].figs)
        assertEquals(Act.CHASED, d[1].act)
    }

    @Test fun `a dream that never says where is nowhere`() {
        assertTrue(Guess.panels("Something was wrong. I couldn't move at all. Then I laughed a lot.", 1, emptyList(), emptyList()).all { it.scene == Scene.VOID })
    }
}

class CodingTest {
    @Test fun `figures round trip`() {
        val f = listOf(Fig.Cast(3), Fig.Extra(Kind.SHADOW, "the man: at | door"), Fig.Extra(Kind.VOICE))
        val back = Fig.decode(Fig.encode(f))
        assertEquals(Fig.Cast(3), back[0])
        assertEquals(Kind.SHADOW, (back[1] as Fig.Extra).kind)
        assertEquals("the man  at   door", (back[1] as Fig.Extra).label)
        assertEquals(Fig.Extra(Kind.VOICE), back[2])
    }

    @Test fun `looks round trip`() {
        val l = Look(Base.HUMAN, 2, Age.OLD, Hair.BUN, 7, 2, 3, setOf(Trait.GLOWS, Trait.BEARD))
        assertEquals(l, Look.decode(l.encode()))
        assertEquals(Look(), Look.decode("garbage"))
    }

    @Test fun `doodles round trip and close`() {
        val loop = (0..20).map { val a = it / 20.0 * 2 * Math.PI; Doodle.P(0.5f + 0.2f * kotlin.math.cos(a).toFloat(), 0.5f + 0.2f * kotlin.math.sin(a).toFloat()) }
        val back = Doodle.decode(Doodle.encode(listOf(loop, listOf(Doodle.P(0f, 0f), Doodle.P(1f, 1f)))))
        assertEquals(2, back.size)
        assertTrue(Doodle.closed(back[0]))
        assertTrue(!Doodle.closed(back[1]))
    }

    @Test fun `pages and the bed`() {
        for (n in 1..8) assertEquals(n, Layout.rows(n).sum())
        assertEquals(0, Bed.stage(0))
        assertEquals(3, Bed.stage(14))
        assertEquals(4, Bed.stage(100))
        assertEquals(1, Bed.toNext(6))
        assertEquals(null, Bed.toNext(30))
    }
}

/** The names of the layers are string arrays in enum order, in both languages. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NamesTest {
    private fun check(lang: String) {
        val app = ApplicationProvider.getApplicationContext<SlumberApp>()
        val conf = android.content.res.Configuration(app.resources.configuration).apply { setLocale(java.util.Locale.forLanguageTag(lang)) }
        val r = app.createConfigurationContext(conf).resources
        assertEquals(Scene.entries.size, r.getStringArray(R.array.scenes).size)
        assertEquals(Act.entries.size, r.getStringArray(R.array.acts).size)
        assertEquals(Mood.entries.size, r.getStringArray(R.array.moods).size)
        assertEquals(Kind.entries.size, r.getStringArray(R.array.kinds).size)
        assertEquals(Base.entries.size, r.getStringArray(R.array.bases).size)
        assertEquals(Age.entries.size, r.getStringArray(R.array.ages).size)
        assertEquals(Hair.entries.size, r.getStringArray(R.array.hairs).size)
        assertEquals(Trait.entries.size, r.getStringArray(R.array.traits).size)
        assertEquals(Bed.steps.size, r.getStringArray(R.array.bed_stages).size)
    }

    @Test fun english() = check("en")
    @Test fun russian() = check("ru")
}
