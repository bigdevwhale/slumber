package app.slumber.domain

/** A panel before it's stored: what [Guess] made of one piece of the dream. */
data class Draft(
    val caption: String,
    val scene: Scene,
    val spotId: Long = 0,
    val figs: List<Fig>,
    val act: Act,
    val mood: Mood,
    val doodle: String = "",
)

/** A named place in the atlas, or a named member of the cast, as [Guess] needs them. */
data class Named(val id: Long, val name: String, val scene: Scene = Scene.ROOM)

/**
 * A first guess at each panel from the words, so a dream told in thirty seconds already has a page.
 * Nothing clever: word stems in Russian and English, the atlas's place names and the cast's names. Every
 * guess is a suggestion the dreamer can change with a tap.
 *
 * A stem matches a word that starts with it; a stem ending in '$' must be the whole word; one with a
 * space is looked for as a phrase.
 */
object Guess {
    private val scenes: List<Pair<Scene, List<String>>> = listOf(
        Scene.UNDERWATER to listOf("под водой", "под воду", "на дне", "underwater", "under water"),
        Scene.CORRIDOR to listOf("коридор", "прихож", "холл", "corridor", "hallway", "hall$"),
        Scene.STAIRS to listOf("лестниц", "ступен", "stair", "steps$"),
        Scene.KITCHEN to listOf("кухн", "kitchen"),
        Scene.BATHROOM to listOf("ванн", "туалет", "душ$", "душе$", "bathroom", "toilet", "shower"),
        Scene.SCHOOL to listOf("школ", "класс", "урок", "экзамен", "универ", "институт", "school", "class", "exam", "lesson", "universit", "college"),
        Scene.OFFICE to listOf("офис", "работ", "office", "work$", "job$"),
        Scene.HOSPITAL to listOf("больниц", "врач", "госпитал", "палат", "hospital", "doctor", "clinic"),
        Scene.SHOP to listOf("магазин", "рынок", "рынк", "супермаркет", "shop", "store", "market", "mall"),
        Scene.STATION to listOf("вокзал", "станци", "перрон", "аэропорт", "метро", "station", "platform", "airport", "subway"),
        Scene.TRAIN to listOf("поезд", "вагон", "электрич", "train", "carriage"),
        Scene.ELEVATOR to listOf("лифт", "elevator", "lift$"),
        Scene.CAR to listOf("машин", "автомоб", "автобус", "такси", "car$", "bus$", "taxi"),
        Scene.ROOF to listOf("крыш", "балкон", "roof", "balcony"),
        Scene.BRIDGE to listOf("мост", "bridge"),
        Scene.GARDEN to listOf("сад$", "саду$", "сада$", "парк", "garden", "park$"),
        Scene.FOREST to listOf("лес$", "леса$", "лесу$", "лесом$", "чащ", "деревь", "forest", "wood", "trees"),
        Scene.FIELD to listOf("поле$", "полю$", "поля$", "луг", "степ", "field", "meadow"),
        Scene.MOUNTAIN to listOf("гора$", "горы$", "гору$", "горах$", "скал", "вершин", "mountain", "cliff", "hill"),
        Scene.CAVE to listOf("пещер", "подвал", "тоннел", "туннел", "cave", "basement", "tunnel", "cellar"),
        Scene.BEACH to listOf("пляж", "берег", "песок", "песке$", "beach", "shore", "sand$"),
        Scene.SEA to listOf("море$", "моря$", "морю$", "морем$", "океан", "озер", "река$", "реке$", "реку$", "волн", "sea$", "ocean", "lake", "river", "wave"),
        Scene.SKY to listOf("неб", "облак", "sky", "cloud"),
        Scene.SPACE to listOf("космос", "звезд", "планет", "луна$", "луну$", "луне$", "space$", "star", "planet", "moon"),
        Scene.CASTLE to listOf("замок$", "замке$", "замка$", "дворц", "дворец", "castle", "palace"),
        Scene.TEMPLE to listOf("церк", "храм", "собор", "church", "temple", "cathedral"),
        Scene.STREET to listOf("улиц", "переул", "двор$", "дворе$", "street", "alley", "yard"),
        Scene.CITY to listOf("город", "площад", "city", "town", "square"),
        Scene.ROOM to listOf("комнат", "спальн", "гостин", "room", "bedroom"),
        Scene.HOUSE to listOf("дом$", "доме$", "дома$", "домой$", "квартир", "дач", "house", "home", "flat$", "apartment"),
        Scene.VOID to listOf("пустот", "темнот", "ничего не было", "void", "darkness", "nothing"),
    )

    /** Most telling first: being chased says more than walking. */
    private val acts: List<Pair<Act, List<String>>> = listOf(
        Act.CHASED to listOf("за мной", "гнал", "гонял", "погон", "преслед", "убега", "chased", "chasing me", "after me", "running from"),
        Act.FROZEN to listOf("не мог пошевел", "не могу пошевел", "не могу двиг", "не мог двиг", "парализ", "застыл", "оцепен", "couldn't move", "could not move", "can't move", "frozen", "paraly"),
        Act.FALL to listOf("пада", "упал", "упад", "падени", "сорвал", "fall", "fell"),
        Act.SINK to listOf("тону", "тонул", "утону", "захлеб", "drown", "sink", "sank"),
        Act.FLY to listOf("лета", "летел", "лечу", "лететь", "полет", "взлет", "fly", "flew", "flying"),
        Act.FLOAT to listOf("парил", "парю", "парила", "невесом", "float", "hover"),
        Act.SWIM to listOf("плава", "плыл", "плыв", "нырн", "swim", "swam", "dive", "dived"),
        Act.FIGHT to listOf("дрался", "драл", "драк", "удар", "бой$", "fight", "fought", "punch", "hit$"),
        Act.HUG to listOf("обня", "объят", "hug", "embrace"),
        Act.SHOUT to listOf("крич", "крик", "орал", "scream", "shout", "yell"),
        Act.CRY to listOf("плака", "плач", "слез", "cry", "cried", "tears"),
        Act.LAUGH to listOf("смея", "смех", "смеял", "хохот", "laugh"),
        Act.DANCE to listOf("танц", "dance", "danc"),
        Act.HIDE to listOf("прята", "спрят", "прячу", "hide", "hid$", "hiding"),
        Act.LOST to listOf("заблуд", "не мог найти выход", "не могла найти", "lost$", "got lost"),
        Act.SEARCH to listOf("искал", "ищу", "искала", "найти", "search", "look for", "looking for"),
        Act.CLIMB to listOf("лез", "карабк", "подним", "climb"),
        Act.DRIVE to listOf("ехал", "еду$", "едем", "вел машин", "за рулем", "drive", "drove", "driving"),
        Act.OPEN to listOf("открыва", "открыл", "двер", "open", "door"),
        Act.RUN to listOf("бег", "беж", "run$", "ran$", "running"),
        Act.TALK to listOf("говор", "сказал", "разговар", "спроси", "ответ", "прода", "talk", "said", "told", "ask", "sell", "sold"),
        Act.SIT to listOf("сидел", "сидим", "сижу", "сел$", "села$", "sit", "sat$"),
        Act.SLEEP to listOf("спал", "сплю", "засып", "лежал", "sleep", "slept", "lying"),
        Act.GROW to listOf("огромн", "вырос", "великан", "giant", "huge", "grew"),
        Act.SHRINK to listOf("крошечн", "уменьш", "маленьким", "маленькой", "tiny", "shrank", "shrunk"),
        Act.LOOK to listOf("смотр", "увидел", "вижу", "гляд", "watch", "saw$", "look"),
        Act.WALK to listOf("шел", "шла", "иду", "идет", "идем", "гулял", "ходил", "walk", "went", "going"),
        Act.STAND to listOf("стоял", "стою", "stand", "stood"),
    )

    private val moods: List<Pair<Mood, List<String>>> = listOf(
        Mood.FEAR to listOf("страш", "боял", "боюс", "ужас", "жутк", "кошмар", "без лица", "монстр", "scared", "afraid", "fear", "terrif", "horror", "nightmare", "monster", "faceless"),
        Mood.ANXIETY to listOf("тревож", "тревог", "волнов", "опазд", "не успе", "паник", "спеш", "нервн", "экзамен", "late$", "anxious", "worried", "panic", "hurry", "exam"),
        Mood.ANGER to listOf("злил", "злость", "злой", "бесил", "ярост", "ругал", "angry", "rage", "furious"),
        Mood.SADNESS to listOf("грус", "печал", "тоск", "плака", "умер", "похорон", "sad$", "cried", "grief", "funeral", "missed"),
        Mood.JOY to listOf("рад$", "рада$", "счаст", "весел", "смеял", "happy", "joy", "glad", "fun$", "laugh"),
        Mood.WONDER to listOf("удивит", "красив", "волшеб", "чудес", "сияющ", "светил", "beautiful", "magic", "wonder", "glow", "amazing"),
        Mood.CALM to listOf("спокой", "тихо", "умиротвор", "calm", "quiet", "peaceful"),
        Mood.STRANGE to listOf("странн", "непонятн", "абсурд", "почему-то", "weird", "strange", "odd$", "bizarre", "somehow"),
    )

    private val kinds: List<Pair<Kind, List<String>>> = listOf(
        Kind.SHADOW to listOf("тень", "тени$", "без лица", "силуэт", "shadow", "faceless", "figure"),
        Kind.CROWD to listOf("толп", "люди$", "людей", "народ", "crowd", "people"),
        Kind.CHILD to listOf("ребен", "дети$", "детей", "мальчик", "девочк", "child", "kid$", "kids", "boy$", "girl$"),
        Kind.ANIMAL to listOf("собак", "пес$", "кот$", "кота$", "кошк", "волк", "медвед", "зверь", "лошад", "dog", "cat$", "wolf", "bear$", "horse", "animal"),
        Kind.BIRD to listOf("птиц", "ворон", "сова$", "сову$", "голуб", "bird", "crow", "owl"),
        Kind.CREATURE to listOf("чудовищ", "монстр", "существ", "дракон", "monster", "creature", "dragon"),
        Kind.VOICE to listOf("голос", "voice"),
        Kind.SOMEONE to listOf("кто-то", "кого-то", "кем-то", "мужчин", "женщин", "незнаком", "someone", "somebody", "stranger", "man$", "woman$"),
    )

    private fun norm(s: String) = s.lowercase().replace('ё', 'е')

    private class Text(raw: String) {
        val text = " " + norm(raw).replace(Regex("[^\\p{L}\\p{N}'-]+"), " ") + " "
        val words = text.split(' ').filter { it.isNotEmpty() }

        /** Where in the text a stem first matches, or -1. */
        fun find(stem: String): Int {
            if (' ' in stem) return text.indexOf(" $stem").let { if (it < 0) -1 else it }
            val exact = stem.endsWith('$')
            val st = stem.trimEnd('$')
            val i = words.indexOfFirst { if (exact) it == st else it.startsWith(st) }
            return if (i < 0) -1 else text.indexOf(" ${words[i]}")
        }

        fun has(stems: List<String>) = stems.any { find(it) >= 0 }
        fun last(stems: List<String>) = stems.maxOfOrNull { find(it) } ?: -1
    }

    /** A name's own stem: "Мама" also finds "маму", "Начальник" finds "начальника". */
    private fun nameStems(name: String): List<String> = norm(name).split(Regex("[^\\p{L}\\p{N}]+")).filter { it.length >= 2 }.map { w ->
        when {
            w.length <= 3 -> w
            w.length <= 5 -> w.dropLast(1)
            else -> w.dropLast(2)
        }
    }

    private fun names(t: Text, name: String): Int {
        val st = nameStems(name)
        if (st.isEmpty()) return -1
        val at = st.map { t.find(it) }
        return if (at.all { it >= 0 }) at.max() else -1
    }

    /** The last place mentioned wins: dreams move, and the panel shows where it ended up. */
    fun scene(raw: String): Scene? {
        val t = Text(raw)
        return scenes.map { (s, st) -> s to t.last(st) }.filter { it.second >= 0 }.maxByOrNull { it.second }?.first
    }

    fun act(raw: String): Act? {
        val t = Text(raw)
        return acts.firstOrNull { (_, st) -> t.has(st) }?.first
    }

    fun mood(raw: String): Mood? {
        val t = Text(raw)
        val hits = moods.map { (m, st) -> m to st.count { t.find(it) >= 0 } }.filter { it.second > 0 }
        return hits.maxByOrNull { it.second }?.first
    }

    fun kinds(raw: String): List<Kind> {
        val t = Text(raw)
        val found = kinds.filter { (_, st) -> t.has(st) }.map { it.first }
        // A creature without a face is a shadow, not two figures.
        return if (Kind.SHADOW in found) found - Kind.SOMEONE else found
    }

    /**
     * The whole dream as panels. [me] is the dreamer, in every panel; then anyone from [cast] named in that
     * piece, then the nameless extras, three figures at most.
     */
    fun panels(text: String, me: Long, cast: List<Named>, spots: List<Named>): List<Draft> {
        val chunks = Split.chunks(text)
        val overall = mood(text) ?: Mood.WONDER
        var scene = Scene.ROOM
        var spot = 0L
        var known = false
        val out = chunks.map { chunk ->
            val t = Text(chunk)
            val place = spots.map { it to names(t, it.name) }.filter { it.second >= 0 }.maxByOrNull { it.second }?.first
            val arch = scene(chunk)
            when {
                place != null -> { scene = place.scene; spot = place.id; known = true }
                arch != null -> { scene = arch; spot = 0; known = true }
            }
            val people = cast.filter { it.id != me && names(t, it.name) >= 0 }.map { Fig.Cast(it.id) }
            val extras = kinds(chunk).map { Fig.Extra(it) }
            val figs = (listOf(Fig.Cast(me)) + people + extras).take(3)
            Draft(chunk, scene, spot, figs, act(chunk) ?: Act.STAND, mood(chunk) ?: overall)
        }
        // A dream that never says where it is: wherever the first place mentioned later was, or an empty dreamscape.
        if (!known) return out.map { it.copy(scene = Scene.VOID) }
        val first = out.indexOfFirst { scene(it.caption) != null || spots.any { s -> names(Text(it.caption), s.name) >= 0 } }
        return out.mapIndexed { i, d -> if (i < first) d.copy(scene = out[first].scene, spotId = out[first].spotId) else d }
    }
}
