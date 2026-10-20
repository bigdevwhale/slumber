package app.slumber.data

import app.slumber.AppContainer
import app.slumber.domain.Clarity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.random.Random

/**
 * Three weeks of practice for the `-Pdemo` build and the screenshots: dreams with signs that keep coming
 * back (school, being late, the childhood home, water), checks most days, the ritual most nights, a few
 * glimmers and two lucid dreams, and a sign of the dreamer's own.
 */
object DemoSeed {
    private class D(val ago: Int, val clarity: Clarity, val ru: String, val en: String)

    private val dreams = listOf(
        D(0, Clarity.GLIMMER, "Снова школа, контрольная, а я опаздываю. В коридоре свет не включается, и на секунду мелькнуло: так ведь уже было.", "School again, a test, and I’m late. The corridor lights won’t turn on, and for a second I thought: this has happened before."),
        D(1, Clarity.NONE, "Бабушкин дом, но в нём лишние комнаты. За синей дверью море.", "Grandma’s house, but with extra rooms. Behind a blue door, the sea."),
        D(3, Clarity.LUCID, "Я стою на вокзале и вижу, что надпись на табло меняется. Понимаю, что сплю! Поднимаюсь в воздух и лечу над крышами.", "I’m at the station and the board keeps changing. I realise I’m dreaming! I rise into the air and fly over the roofs."),
        D(4, Clarity.NONE, "Экзамен по физике, я ничего не помню, а листок пустой.", "A physics exam; I remember nothing and the sheet is blank."),
        D(5, Clarity.GLIMMER, "Пытаюсь позвонить маме, но номер на телефоне всё время другой.", "I try to phone Mum, but the number keeps changing."),
        D(7, Clarity.NONE, "Опаздываю на поезд, бегу, а ноги ватные.", "I’m late for a train, I run, but my legs are like cotton wool."),
        D(8, Clarity.NONE, "Дом детства, дедушка жив и чинит велосипед во дворе.", "My childhood home; Grandpa is alive and mending a bike in the yard."),
        D(10, Clarity.NONE, "Плаваю в море, вода тёплая, под водой светятся рыбы.", "I swim in the sea; the water is warm and fish glow below."),
        D(11, Clarity.GLIMMER, "Школьный коридор без конца. Синяя дверь, за ней снова коридор.", "An endless school corridor. A blue door, and behind it, more corridor."),
        D(13, Clarity.NONE, "Работа, начальник требует отчёт, которого у меня нет.", "Work; my boss wants a report I don’t have."),
        D(15, Clarity.LUCID, "За мной гонится собака. Смотрю на руки, а пальцев шесть. Это сон! Останавливаюсь, и собака садится рядом.", "A dog is chasing me. I look at my hands: six fingers. It’s a dream! I stop, and the dog sits beside me."),
        D(17, Clarity.NONE, "Падаю с лестницы в бабушкином доме и просыпаюсь.", "I fall down the stairs in Grandma’s house and wake up."),
        D(19, Clarity.NONE, "Опаздываю на экзамен, а автобус едет не туда.", "I’m late for an exam and the bus goes the wrong way."),
    )

    private val lock = Mutex()

    suspend fun seedIfEmpty(c: AppContainer, ru: Boolean = java.util.Locale.getDefault().language == "ru") = lock.withLock {
        val dao = c.db.dao()
        if (dao.allDreams().isNotEmpty()) return@withLock
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        fun at(day: LocalDate, h: Int, m: Int) = day.atTime(LocalTime.of(h, m)).atZone(zone).toInstant().toEpochMilli()

        dao.insertDreams(dreams.map { d ->
            Dream(at = at(today.minusDays(d.ago.toLong()), 7, 5 + d.ago % 40), text = if (ru) d.ru else d.en, clarity = d.clarity.ordinal)
        })
        val r = Random(11)
        val checks = mutableListOf<Check>()
        val rituals = mutableListOf<Ritual>()
        for (ago in 1..20) {
            val day = today.minusDays(ago.toLong())
            repeat(if (ago % 6 == 0) 0 else 2 + r.nextInt(3)) { checks += Check(at = at(day, 10 + r.nextInt(10), r.nextInt(60))) }
            if (ago % 5 != 2) rituals += Ritual(at = at(day, 22, 40 + r.nextInt(15)))
        }
        // Today so far: two checks.
        checks += Check(at = at(today, 10, 12))
        checks += Check(at = at(today, 12, 40))
        dao.insertChecks(checks)
        dao.insertRituals(rituals)
        dao.insert(OwnSign(phrase = if (ru) "синяя дверь" else "blue door"))
        c.settings.update { it.copy(onboarded = true) }
    }
}
