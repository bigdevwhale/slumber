package app.slumber.domain

/**
 * Dream signs: what happens in your dreams and hardly ever when you're awake. Spotting one in a dream is
 * the most reliable way to realise you're dreaming. They're found in the journal by word stems, Russian
 * and English; nothing has to be tagged. A stem matches a word starting with it, a stem ending in '$' must
 * be the whole word, and one with a space is looked for as a phrase.
 */
data class Sign(val key: String, val ru: String, val en: String, val stems: List<String>) {
    fun label(ru: Boolean) = if (ru) this.ru else en
}

object Signs {
    val all = listOf(
        Sign("school", "Школа или экзамен", "School or an exam", listOf("школ", "урок", "экзамен", "контрольн", "одноклассн", "school", "exam", "lesson", "classmate", "test$")),
        Sign("late", "Опаздываю", "Running late", listOf("опазд", "не успе", "спешу", "спешил", "late$", "hurry", "missed")),
        Sign("flying", "Полёт", "Flying", listOf("летаю", "летал", "летел", "лечу", "лететь", "полет", "взлет", "fly", "flew", "flying")),
        Sign("falling", "Падение", "Falling", listOf("пада", "упал", "упаду", "fall", "fell")),
        Sign("chase", "Погоня", "Being chased", listOf("за мной", "гнал", "гонял", "погон", "преслед", "chased", "chasing", "after me")),
        Sign("teeth", "Зубы", "Teeth", listOf("зуб", "teeth", "tooth")),
        Sign("stuck", "Не могу двигаться или бежать", "Can’t move or run", listOf("не могу двиг", "не мог двиг", "пошевел", "ватны", "не могу бежать", "не мог бежать", "can't move", "couldn't move", "can't run", "couldn't run", "slow motion")),
        Sign("lights", "Свет не включается", "Lights won’t turn on", listOf("выключател", "свет не", "лампа не", "light switch", "lights won't", "light wouldn't")),
        Sign("phone", "Телефон не слушается", "The phone won’t work", listOf("телефон", "позвонить", "номер", "phone", "dial", "call$")),
        Sign("text", "Текст меняется", "Text that changes", listOf("текст", "буквы", "надпис", "прочитать", "читаю", "text", "letters", "read$", "reading")),
        Sign("rooms", "Лишние комнаты", "Rooms that weren’t there", listOf("лишн", "новая комната", "новые комнаты", "которой не было", "которых не было", "extra room", "new room", "never there", "wasn't there")),
        Sign("childhood", "Дом детства", "A childhood home", listOf("бабушк", "дедушк", "детств", "старой квартир", "старый дом", "grandma", "grandmother", "grandpa", "childhood", "old house")),
        Sign("gone", "Ушедшие снова рядом", "Someone gone is there again", listOf("умер", "покойн", "умерш", "died", "dead", "passed away")),
        Sign("ex", "Бывшие", "An ex", listOf("бывш", "ex$", "former")),
        Sign("water", "Вода", "Water", listOf("вода", "воды", "воде", "море", "моря", "морю", "океан", "волн", "потоп", "река", "реке", "water", "sea$", "ocean", "wave", "flood", "river")),
        Sign("naked", "Без одежды на людях", "Undressed in public", listOf("голый", "голая", "голым", "без одежды", "раздет", "naked", "undressed")),
        Sign("toilet", "Ищу туалет", "Looking for a toilet", listOf("туалет", "toilet")),
        Sign("lost", "Не найти дорогу", "Can’t find the way", listOf("заблуд", "не могу найти", "не мог найти", "не могла найти", "потерял", "lost$", "can't find", "couldn't find")),
        Sign("travel", "Поезда и вокзалы", "Trains and stations", listOf("поезд", "вокзал", "автобус", "самолет", "аэропорт", "train", "station", "bus$", "plane", "airport")),
        Sign("animals", "Звери", "Animals", listOf("собак", "кошк", "кот$", "кота$", "зверь", "волк", "медвед", "dog", "cat$", "wolf", "animal", "bear$")),
        Sign("work", "Работа", "Work", listOf("работ", "начальн", "офис", "коллег", "work$", "boss", "office", "colleague")),
    )

    fun of(key: String): Sign? = all.firstOrNull { it.key == key }

    /** Your own sign from a phrase ("the blue door"): every word's stem has to be there. */
    fun own(id: Long, phrase: String): Sign = Sign("own:$id", phrase, phrase, stems(phrase))

    private fun norm(s: String) = s.lowercase().replace('ё', 'е')

    /** A word's own stem, so endings don't matter: "синяя дверь" also finds "за синей дверью". */
    fun stems(phrase: String): List<String> = norm(phrase).split(Regex("[^\\p{L}\\p{N}']+")).filter { it.length >= 2 }.map { w ->
        if (w.length <= 3) w else w.take(maxOf(3, w.length - 2))
    }

    private fun words(text: String): Pair<String, List<String>> {
        val t = " " + norm(text).replace(Regex("[^\\p{L}\\p{N}'-]+"), " ") + " "
        return t to t.split(' ').filter { it.isNotEmpty() }
    }

    private fun hit(t: String, ws: List<String>, stem: String): Boolean {
        if (' ' in stem) return t.contains(" $stem")
        val st = stem.trimEnd('$')
        return if (stem.endsWith('$')) ws.any { it == st } else ws.any { it.startsWith(st) }
    }

    /** Does [text] show [sign]? An own sign needs all its words; a built-in one, any stem. */
    fun shows(sign: Sign, text: String): Boolean {
        val (t, ws) = words(text)
        if (sign.stems.isEmpty()) return false
        return if (sign.key.startsWith("own:")) sign.stems.all { hit(t, ws, it) } else sign.stems.any { hit(t, ws, it) }
    }

    fun found(text: String, signs: List<Sign> = all): List<Sign> = signs.filter { shows(it, text) }

    /** Your signs, most frequent first: the sign and how many dreams show it. Only signs seen twice or more count as yours. */
    fun yours(texts: List<String>, signs: List<Sign> = all, atLeast: Int = 2): List<Pair<Sign, Int>> =
        signs.map { s -> s to texts.count { shows(s, it) } }.filter { it.second >= atLeast }.sortedByDescending { it.second }
}

/** The quick tests that tell waking from dreaming. Each works because dreams get it wrong. */
object Checks {
    data class Check(val ru: String, val en: String) {
        fun text(ru: Boolean) = if (ru) this.ru else en
    }

    val all = listOf(
        Check("Посмотри на руки. Сосчитай пальцы.", "Look at your hands. Count your fingers."),
        Check("Прочитай любую надпись, отведи взгляд и прочитай снова. Она та же?", "Read some text, look away, read it again. Is it the same?"),
        Check("Зажми нос и попробуй вдохнуть.", "Pinch your nose shut and try to breathe through it."),
        Check("Посмотри на часы, отвернись и посмотри ещё раз.", "Look at a clock, look away, and look again."),
        Check("Щёлкни выключателем. Свет послушался?", "Flip a light switch. Did the light obey?"),
        Check("Вспомни, как начался этот час. Всё складывается?", "Remember how this hour began. Does it all add up?"),
        Check("Надави пальцем на ладонь. Палец остался снаружи?", "Push a finger into your palm. Did it stay out?"),
        Check("Подпрыгни. Ты опускаешься как обычно?", "Jump. Do you come down the way you always do?"),
    )

    fun pick(seed: Long): Check = all[((seed % all.size) + all.size).toInt() % all.size]
}
