package app.slumber.domain

/**
 * Cuts a dream as it was told into a few panels: at sentence ends, then before "and then", "suddenly",
 * "а там"; a long run-on sentence (dreams are told that way) is cut at the comma nearest its middle.
 * The words stay exactly as written: they become the captions.
 */
object Split {
    const val MAX = 6

    private val connectors = listOf(
        "и тут", "а потом", "потом", "вдруг", "а там", "и вот", "затем", "после этого", "в итоге", "а дальше",
        "and then", "then", "suddenly", "after that", "next thing", "and there",
    )

    private fun words(s: String) = s.split(Regex("\\s+")).count { it.any(Char::isLetterOrDigit) }

    fun chunks(text: String, max: Int = MAX): List<String> {
        val clean = text.replace('\r', '\n').trim()
        if (clean.isEmpty()) return emptyList()
        val sentences = clean.split(Regex("(?<=[.!?…])\\s+|\\n+")).map { it.trim() }.filter { it.isNotEmpty() }
        var out = sentences.flatMap(::atConnectors).toMutableList()

        // Too few panels from one breathless sentence: halve the longest at a comma.
        while (out.size < 3) {
            val i = out.indices.filter { words(out[it]) >= 12 && out[it].contains(',') }.maxByOrNull { words(out[it]) } ?: break
            val halves = atMiddleComma(out[i]) ?: break
            out.removeAt(i)
            out.addAll(i, halves)
        }
        // Scraps of one or two words join the panel before them.
        var i = 1
        while (i < out.size) {
            if (words(out[i]) < 3) { out[i - 1] = out[i - 1] + " " + out[i]; out.removeAt(i) } else i++
        }
        if (out.size > 1 && words(out[0]) < 3) { out[1] = out[0] + " " + out[1]; out.removeAt(0) }
        // Too many: the shortest neighbours share a panel.
        while (out.size > max) {
            val j = (0 until out.size - 1).minBy { out[it].length + out[it + 1].length }
            out[j] = out[j] + " " + out[j + 1]
            out.removeAt(j + 1)
        }
        out = out.map { s -> s.trim().trimEnd(',', ';', '—', '-').trim().replaceFirstChar { it.uppercase() } }.toMutableList()
        return out
    }

    /** "…a corridor, and then a door" → before "and then". Only where both sides keep three words. */
    private fun atConnectors(s: String): List<String> {
        val low = s.lowercase()
        for (c in connectors) {
            val m = Regex("(^|[\\s,;—-])" + Regex.escape(c) + "(?=[\\s,])").findAll(low).firstOrNull { it.range.first > 0 } ?: continue
            val at = m.range.first + m.value.indexOfFirst { it.isLetter() }
            val a = s.substring(0, at)
            val b = s.substring(at)
            if (words(a) >= 3 && words(b) >= 3) return atConnectors(a) + atConnectors(b)
        }
        return listOf(s)
    }

    private fun atMiddleComma(s: String): List<String>? {
        val commas = s.indices.filter { s[it] == ',' }
        if (commas.isEmpty()) return null
        val mid = s.length / 2
        val at = commas.minBy { kotlin.math.abs(it - mid) }
        val a = s.substring(0, at + 1).trim()
        val b = s.substring(at + 1).trim()
        if (words(a) < 3 || words(b) < 3) return null
        return listOf(a, b)
    }
}
