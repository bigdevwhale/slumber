package app.slumber.data

import app.slumber.AppContainer
import app.slumber.domain.Act
import app.slumber.domain.Age
import app.slumber.domain.Base
import app.slumber.domain.Draft
import app.slumber.domain.Fig
import app.slumber.domain.Hair
import app.slumber.domain.Kind
import app.slumber.domain.Look
import app.slumber.domain.Mood
import app.slumber.domain.Scene
import app.slumber.domain.Trait
import app.slumber.ui.atlas.placeFor
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Six weeks of made-up dreams for the `-Pdemo` build and the screenshots: a dreamland of six places,
 * a cast of five, a chase that got a new ending, two lucid nights, and a nameless teacher who keeps
 * coming back.
 */
object DemoSeed {
    private class P(
        val scene: Scene, val act: Act, val mood: Mood, val who: String,
        val ru: String, val en: String, val spot: String? = null, val doodle: String = "",
    )

    private class D(
        val ago: Int, val titleRu: String, val titleEn: String, val panels: List<P>,
        val lucid: Boolean = false, val rewrite: List<P>? = null,
    )

    // Who: m = me, mom, boss, dog, shadow, anya; x:KIND or x:KIND:label for the nameless.
    private val dreams = listOf(
        D(1, "Билет на море", "A ticket to the sea", listOf(
            P(Scene.HOUSE, Act.WALK, Mood.WONDER, "m", "Я в бабушкином доме, но комнат в нём гораздо больше, чем было.", "I’m in Grandma’s house, but it has far more rooms than it used to.", "house"),
            P(Scene.CORRIDOR, Act.OPEN, Mood.STRANGE, "m", "В конце коридора дверь, которой раньше не было. Открываю её.", "At the end of the corridor there’s a door that was never there. I open it.", "house"),
            P(Scene.SEA, Act.FALL, Mood.WONDER, "m", "За дверью сразу море, и я падаю в него, но не мокну.", "Behind the door is the sea, and I fall in, but I don’t get wet.", "sea"),
            P(Scene.SEA, Act.TALK, Mood.JOY, "m,boss", "А на плоту начальник продаёт билеты на это же море.", "And on a raft my boss is selling tickets to this very sea.", "sea"),
        )),
        D(2, "", "", listOf(
            P(Scene.SCHOOL, Act.SEARCH, Mood.ANXIETY, "m,x:SOMEONE:учитель физики", "Снова школа, контрольная, а я не помню ни одной формулы.", "School again, a test, and I can’t remember a single formula.", "school"),
            P(Scene.SCHOOL, Act.LOST, Mood.ANXIETY, "m,x:CROWD", "Все уже сдали, а мой листок пустой и всё время меняется.", "Everyone has handed theirs in; my sheet is blank and keeps changing.", "school"),
        )),
        D(4, "Кровать на крышах", "The bed over the roofs", listOf(
            P(Scene.ROOM, Act.SLEEP, Mood.CALM, "m", "Засыпаю у себя в комнате, и кровать вдруг встаёт.", "I fall asleep in my room, and suddenly the bed stands up."),
            P(Scene.ROOF, Act.FLY, Mood.WONDER, "m", "Летим над крышами, я понимаю, что это сон, и не боюсь.", "We fly over the roofs; I know it’s a dream, and I’m not afraid.", "roofs"),
            P(Scene.CITY, Act.FLOAT, Mood.JOY, "m,dog", "Бублик летит рядом и лает на луну.", "Bublik flies alongside, barking at the moon.", "roofs"),
        ), lucid = true),
        D(6, "Коридор", "The corridor", listOf(
            P(Scene.CORRIDOR, Act.WALK, Mood.ANXIETY, "m", "Длинный коридор, двери с обеих сторон, все заперты.", "A long corridor, doors on both sides, all locked.", "house"),
            P(Scene.CORRIDOR, Act.CHASED, Mood.FEAR, "m,shadow", "За мной идёт человек без лица. Я бегу, а коридор не кончается.", "The faceless man is following me. I run, and the corridor never ends.", "house"),
            P(Scene.CORRIDOR, Act.FROZEN, Mood.FEAR, "m,shadow", "Ноги не слушаются, я не могу пошевелиться.", "My legs won’t work; I can’t move.", "house"),
        ), rewrite = listOf(
            P(Scene.CORRIDOR, Act.WALK, Mood.ANXIETY, "m", "Длинный коридор, двери с обеих сторон, все заперты.", "A long corridor, doors on both sides, all locked.", "house"),
            P(Scene.CORRIDOR, Act.LOOK, Mood.CALM, "m,shadow", "Я оборачиваюсь и смотрю на него. Это просто пальто на вешалке.", "I turn round and look at him. It’s just a coat on a hook.", "house"),
            P(Scene.GARDEN, Act.WALK, Mood.JOY, "m,mom", "Ближайшая дверь открывается в бабушкин сад, там мама.", "The nearest door opens into Grandma’s garden, and Mum is there.", "house"),
        )),
        D(8, "", "", listOf(
            P(Scene.STATION, Act.RUN, Mood.ANXIETY, "m,x:CROWD", "Опаздываю на поезд, вокзал бесконечный, табло на незнакомом языке.", "I’m late for a train; the station goes on forever, the board is in a language I don’t know.", "station"),
            P(Scene.TRAIN, Act.SIT, Mood.CALM, "m,anya", "Успеваю. В вагоне Аня, она говорит, что поезд идёт в детство.", "I make it. Anya is in the carriage; she says the train goes to childhood.", "station"),
        )),
        D(11, "Лес у дачи", "The woods by the dacha", listOf(
            P(Scene.FOREST, Act.SEARCH, Mood.STRANGE, "m,dog", "Ищем с Бубликом грибы, а деревья шепчутся.", "Bublik and I look for mushrooms, and the trees whisper.", "forest"),
            P(Scene.FOREST, Act.TALK, Mood.WONDER, "m,x:BIRD", "Сова спрашивает, который час, и сама же отвечает.", "An owl asks what time it is, and answers itself.", "forest"),
            P(Scene.FIELD, Act.LAUGH, Mood.JOY, "m,dog", "Выходим в поле, и я смеюсь, не знаю чему.", "We come out into a field, and I laugh, I don’t know why.", "forest"),
        )),
        D(13, "", "", listOf(
            P(Scene.OFFICE, Act.TALK, Mood.ANGER, "m,boss", "Начальник требует отчёт, которого у меня никогда не было.", "My boss wants a report I never wrote.", null),
            P(Scene.OFFICE, Act.SHOUT, Mood.ANGER, "m,boss", "Я кричу, но изо рта выходят только мыльные пузыри.", "I shout, but only soap bubbles come out of my mouth.", null),
        )),
        D(16, "Мама на кухне", "Mum in the kitchen", listOf(
            P(Scene.KITCHEN, Act.HUG, Mood.CALM, "m,mom", "Мама печёт блины, как в детстве. Обнимаю её.", "Mum is making pancakes, like when I was small. I hug her.", "house"),
            P(Scene.GARDEN, Act.SIT, Mood.SADNESS, "m,mom", "Сидим в саду, и я знаю, что скоро проснусь.", "We sit in the garden, and I know I’ll wake up soon.", "house"),
        )),
        D(19, "", "", listOf(
            P(Scene.SCHOOL, Act.TALK, Mood.STRANGE, "m,x:SOMEONE:учитель физики", "Учитель физики объясняет, что гравитация сегодня отменена.", "The physics teacher explains that gravity is cancelled today.", "school"),
            P(Scene.SCHOOL, Act.FLOAT, Mood.JOY, "m,x:CHILD", "Весь класс всплывает к потолку.", "The whole class floats up to the ceiling.", "school"),
        )),
        D(22, "Под водой", "Under the water", listOf(
            P(Scene.BEACH, Act.WALK, Mood.CALM, "m", "Иду по пляжу, вода тёплая.", "I walk along the beach; the water is warm.", "sea"),
            P(Scene.UNDERWATER, Act.SWIM, Mood.WONDER, "m,x:CREATURE", "Ныряю и могу дышать. Внизу светится огромное существо.", "I dive and I can breathe. Below, a huge creature glows.", "sea", doodle = "300,700 340,640 420,610 520,600 610,630 660,690 600,740 500,760 400,750 330,730 300,700"),
        ), lucid = true),
        D(26, "", "", listOf(
            P(Scene.STATION, Act.LOST, Mood.ANXIETY, "m", "Тот же вокзал. Ищу свой перрон, перроны всё время переставляют.", "The same station. I look for my platform; they keep moving the platforms.", "station"),
            P(Scene.STAIRS, Act.CLIMB, Mood.STRANGE, "m", "Лестница ведёт вверх, на крышу.", "A staircase leads up, to the roof.", "station"),
            P(Scene.ROOF, Act.LOOK, Mood.WONDER, "m", "С крыши виден весь город, он светится.", "From the roof I can see the whole city, glowing.", "roofs"),
        )),
        D(30, "", "", listOf(
            P(Scene.VOID, Act.FALL, Mood.FEAR, "m", "Падаю в темноту, долго, без дна.", "I fall into the dark, for a long time, no bottom.", null),
        )),
        D(34, "Пёс-великан", "The giant dog", listOf(
            P(Scene.STREET, Act.WALK, Mood.JOY, "m,dog", "Гуляем с Бубликом по улице детства.", "Bublik and I walk down the street I grew up on.", null),
            P(Scene.STREET, Act.SHRINK, Mood.STRANGE, "m,dog", "Я становлюсь крошкой, а Бублик великаном.", "I go tiny, and Bublik goes huge.", null),
        )),
        D(38, "", "", listOf(
            P(Scene.HOUSE, Act.STAND, Mood.CALM, "m,mom", "Бабушкин дом, все живы, пьём чай на веранде.", "Grandma’s house, everyone alive, tea on the veranda.", "house"),
        )),
    )

    private fun looks(ru: Boolean) = listOf(
        "mom" to Person(name = if (ru) "Мама" else "Mum", look = Look(hair = Hair.BUN, coat = 0, hairTone = 1, age = Age.OLD).encode()),
        "boss" to Person(name = if (ru) "Начальник" else "Boss", look = Look(hair = Hair.HAT, coat = 7, tall = 2, traits = setOf(Trait.GLASSES)).encode()),
        "dog" to Person(name = if (ru) "Бублик" else "Bublik", look = Look(base = Base.ANIMAL, coat = 3).encode()),
        "shadow" to Person(name = if (ru) "Человек без лица" else "The faceless man", look = Look(hair = Hair.HAT, coat = 8, skin = 3, tall = 2, traits = setOf(Trait.FACELESS)).encode()),
        "anya" to Person(name = if (ru) "Аня" else "Anya", look = Look(hair = Hair.LONG, coat = 9, hairTone = 2).encode()),
    )

    private fun places(ru: Boolean) = listOf(
        "house" to ((if (ru) "Бабушкин дом" else "Grandma’s house") to Scene.HOUSE),
        "sea" to ((if (ru) "Море за дверью" else "The sea behind the door") to Scene.SEA),
        "school" to ((if (ru) "Та самая школа" else "That school") to Scene.SCHOOL),
        "roofs" to ((if (ru) "Город крыш" else "The city of roofs") to Scene.ROOF),
        "station" to ((if (ru) "Бесконечный вокзал" else "The endless station") to Scene.STATION),
        "forest" to ((if (ru) "Лес у дачи" else "The woods by the dacha") to Scene.FOREST),
    )

    private val lock = Mutex()

    suspend fun seedIfEmpty(c: AppContainer, ru: Boolean) = lock.withLock { seed(c, ru) }

    private suspend fun seed(c: AppContainer, ru: Boolean) {
        val dao = c.db.dao()
        if (dao.allDreams().isNotEmpty()) return
        c.ensureMe(if (ru) "Я" else "Me")
        val me = dao.me()!!.id
        val people = looks(ru).associate { (k, p) -> k to dao.insert(p) }
        val spots = mutableMapOf<String, Long>()
        for ((k, v) in places(ru)) {
            val (x, y) = placeFor(dao.allSpots())
            spots[k] = dao.insert(Spot(name = v.first, scene = v.second.name, x = x, y = y))
        }

        fun figs(who: String): List<Fig> = who.split(',').map { w ->
            when {
                w == "m" -> Fig.Cast(me)
                w.startsWith("x:") -> {
                    val b = w.split(':')
                    val label = b.getOrNull(2).orEmpty().let { if (ru || it.isEmpty()) it else "physics teacher" }
                    Fig.Extra(Kind.of(b[1]), label)
                }
                else -> Fig.Cast(people.getValue(w))
            }
        }

        fun drafts(ps: List<P>) = ps.map { p ->
            Draft(if (ru) p.ru else p.en, p.scene, p.spot?.let(spots::getValue) ?: 0, figs(p.who), p.act, p.mood, p.doodle)
        }

        val zone = ZoneId.systemDefault()
        for (d in dreams) {
            val at = LocalDate.now(zone).minusDays(d.ago.toLong()).atTime(LocalTime.of(7, 10 + d.ago % 40)).atZone(zone).toInstant().toEpochMilli()
            val ds = drafts(d.panels)
            val mood = ds.groupingBy { it.mood }.eachCount().maxBy { it.value }.key
            val text = ds.joinToString(" ") { it.caption }
            val id = dao.tell(Dream(at = at, title = if (ru) d.titleRu else d.titleEn, text = text, mood = mood.name, lucid = d.lucid, rewritten = if (d.rewrite != null) at + 86_400_000L * 3 else 0), ds)
            d.rewrite?.let { r -> dao.insertPanels(drafts(r).mapIndexed { i, p -> Panel.of(id, i, p, alt = 1) }) }
        }
        c.settings.update { it.copy(onboarded = true) }
    }
}
