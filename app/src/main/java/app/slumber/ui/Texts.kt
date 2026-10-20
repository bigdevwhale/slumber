package app.slumber.ui

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import app.slumber.R
import app.slumber.domain.Act
import app.slumber.domain.Age
import app.slumber.domain.Base
import app.slumber.domain.Hair
import app.slumber.domain.Kind
import app.slumber.domain.Mood
import app.slumber.domain.Scene
import app.slumber.domain.Trait
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Words and dates in the app's language. The names of the layers live in string arrays in the same order
 * as their enums (a test holds them to it).
 */
object T {
    private fun locale(c: Context): Locale = c.resources.configuration.locales[0]

    fun plural(c: Context, id: Int, n: Int): String = c.resources.getQuantityString(id, n, n)

    /** "Tuesday, 20 October". */
    fun weekday(c: Context, d: LocalDate): String =
        d.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", locale(c))).replaceFirstChar { it.titlecase(locale(c)) }

    /** "20 October 2026", under the banner. */
    fun date(c: Context, d: LocalDate): String = d.format(DateTimeFormatter.ofPattern("d MMMM yyyy", locale(c)))

    /** "October 2026", over a month of the album. */
    fun month(c: Context, d: LocalDate): String =
        d.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale(c))).replaceFirstChar { it.titlecase(locale(c)) }

    fun time(minutes: Int): String = "%02d:%02d".format(minutes / 60, minutes % 60)

    private fun arr(c: Context, id: Int, i: Int) = c.resources.getStringArray(id)[i]

    fun scene(c: Context, s: Scene) = arr(c, R.array.scenes, s.ordinal)
    fun act(c: Context, a: Act) = arr(c, R.array.acts, a.ordinal)
    fun mood(c: Context, m: Mood) = arr(c, R.array.moods, m.ordinal)
    fun kind(c: Context, k: Kind) = arr(c, R.array.kinds, k.ordinal)
    fun base(c: Context, b: Base) = arr(c, R.array.bases, b.ordinal)
    fun age(c: Context, a: Age) = arr(c, R.array.ages, a.ordinal)
    fun hair(c: Context, h: Hair) = arr(c, R.array.hairs, h.ordinal)
    fun trait(c: Context, t: Trait) = arr(c, R.array.traits, t.ordinal)
}

/** In-app language switching (persisted by AppCompat; shows up in Android 13+ per-app language settings). */
object Language {
    const val SYSTEM = ""
    const val EN = "en"
    const val RU = "ru"

    fun current(): String = AppCompatDelegate.getApplicationLocales().toLanguageTags().substringBefore('-').substringBefore(',')

    fun set(tag: String) {
        AppCompatDelegate.setApplicationLocales(if (tag == SYSTEM) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag))
    }
}

@Composable
fun ctx(): Context = LocalContext.current
