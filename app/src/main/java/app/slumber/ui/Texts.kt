package app.slumber.ui

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import app.slumber.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Words and dates in the app's language. Plain functions over a [Context] so notifications can use them too. */
object T {
    private fun locale(c: Context): Locale = c.resources.configuration.locales[0]

    fun plural(c: Context, id: Int, n: Int): String = c.resources.getQuantityString(id, n, n)

    /** "Tuesday, 20 October". */
    fun weekday(c: Context, d: LocalDate): String =
        d.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", locale(c))).replaceFirstChar { it.titlecase(locale(c)) }

    /** "20 October". */
    fun date(c: Context, d: LocalDate): String = d.format(DateTimeFormatter.ofPattern("d MMMM", locale(c)))

    /** "October 2026", over a month of the journal. */
    fun month(c: Context, d: LocalDate): String =
        d.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale(c))).replaceFirstChar { it.titlecase(locale(c)) }

    fun time(minutes: Int): String = "%02d:%02d".format((minutes / 60) % 24, minutes % 60)

    fun seconds(c: Context, n: Int): String = plural(c, R.plurals.seconds, n)
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
