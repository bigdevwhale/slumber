package app.slumber

import app.slumber.domain.Checks
import app.slumber.domain.Clarity
import app.slumber.domain.Plan
import app.slumber.domain.Practice
import app.slumber.domain.Signs
import app.slumber.domain.Spin
import app.slumber.ui.art.fallen
import app.slumber.ui.art.pose
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SignsTest {
    @Test fun `signs are found in either language`() {
        val ru = "Снова школа, контрольная, а я опаздываю. В коридоре свет не включается."
        val keys = Signs.found(ru).map { it.key }
        assertTrue("school" in keys)
        assertTrue("late" in keys)
        assertTrue("lights" in keys)
        assertEquals(listOf("flying"), Signs.found("Then I was flying over the city").map { it.key })
    }

    @Test fun `only signs seen twice are yours, most frequent first`() {
        val texts = listOf("I was late for school", "Late again, the train left", "Running late, an exam at school", "I flew")
        val yours = Signs.yours(texts)
        assertEquals("late", yours[0].first.key)
        assertTrue(yours.all { it.second >= 2 })
        assertTrue(yours.none { it.first.key == "flying" })
    }

    @Test fun `an own sign needs all its words`() {
        val s = Signs.own(1, "синяя дверь")
        assertTrue(Signs.shows(s, "За синей дверью море"))
        assertFalse(Signs.shows(s, "Красная дверь"))
    }

    @Test fun `checks rotate`() {
        assertNotEquals(Checks.pick(0), Checks.pick(1))
        assertEquals(Checks.pick(3), Checks.pick(3 + Checks.all.size.toLong()))
        assertEquals(Checks.pick(-1), Checks.pick(Checks.all.size - 1L))
    }
}

class SpinTest {
    private val today = LocalDate.of(2026, 10, 20)

    @Test fun `practice winds the top up, missed days loosen it`() {
        val none = Spin.windUp(emptyList(), today)
        val week = (0..6).map { Practice(today.minusDays(it.toLong()), 1, 3, true, if (it == 2) Clarity.LUCID else Clarity.NONE) }
        val full = Spin.windUp(week, today)
        val old = Spin.windUp(week.map { it.copy(day = it.day.minusDays(10)) }, today)
        assertEquals(0f, none)
        assertTrue(full > 0.9f)
        assertEquals(0f, old)
        assertTrue(Spin.seconds(full) > Spin.seconds(0.5f))
        assertEquals(Spin.MIN_S, Spin.seconds(0f))
    }

    @Test fun `a lucid morning keeps it spinning all day`() {
        assertTrue(Spin.endless(today, today))
        assertFalse(Spin.endless(today.minusDays(1), today))
    }

    @Test fun `the top spins, wobbles wider, falls and rests`() {
        val s = 20f
        val early = pose(2f, s, false, 0)
        val late = pose(18f, s, false, 0)
        val down = pose(30f, s, false, 0)
        assertTrue(early.tilt < late.tilt)
        assertTrue(down.tilt > 70f)
        assertTrue(fallen(30f, s, false))
        assertFalse(fallen(30f, s, true))
        assertTrue(pose(300f, s, true, 0).tilt < 6f)
    }

    @Test fun `checks fall inside the window`() {
        val times = Plan.of(today, 600, 1260, 5, 42)
        assertEquals(5, times.size)
        assertTrue(times.all { it in 600 until 1260 })
    }
}
