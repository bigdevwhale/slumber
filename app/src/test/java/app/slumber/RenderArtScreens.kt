package app.slumber

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import app.slumber.data.DemoSeed
import app.slumber.domain.Clarity
import app.slumber.ui.Book
import app.slumber.ui.Go
import app.slumber.ui.art.Art
import app.slumber.ui.capture.CaptureScreen
import app.slumber.ui.capture.ClarityScreen
import app.slumber.ui.check.CheckScreen
import app.slumber.ui.home.HomeScreen
import app.slumber.ui.journal.DreamScreen
import app.slumber.ui.journal.JournalScreen
import app.slumber.ui.onboarding.WelcomeScreen
import app.slumber.ui.ritual.RitualScreen
import app.slumber.ui.settings.SettingsScreen
import app.slumber.ui.signs.SignsScreen
import app.slumber.ui.theme.SlumberTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Every main screen over the demo weeks, drawn to PNGs (run with `-Prender`), English and Russian: a look without a phone. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
abstract class ScreensBase {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    abstract val lang: String
    private val out by lazy { File(System.getProperty("render.out") ?: "build/art", "screens-$lang").apply { mkdirs() } }

    @Before fun still() {
        Art.still = true
    }

    private fun book(): Book = runBlocking {
        val app = ApplicationProvider.getApplicationContext<SlumberApp>()
        val c = app.container
        DemoSeed.seedIfEmpty(c, app.ru)
        val dao = c.db.dao()
        Book(dao.allDreams(), dao.allChecks(), dao.allRituals(), dao.allOwnSigns(), c.settings.settings.first(), app.ru)
    }

    private fun shot(name: String, b: Book, settle: Long = 3_000, content: @Composable (Book, Go) -> Unit) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            val nav = rememberNavController()
            SlumberTheme { content(b, Go(nav)) }
        }
        rule.mainClock.advanceTimeBy(settle)
        rule.runOnUiThread {
            val view = rule.activity.window.decorView
            val image = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(image))
            File(out, "$name.png").outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    @Test fun welcome() = book().let { shot("0-welcome", it) { _, _ -> WelcomeScreen {} } }
    @Test fun home() = book().let { shot("1-home", it) { b, go -> HomeScreen(b, go) } }
    @Test fun lucidMorning() = book().let { b ->
        // The morning after a lucid dream: the top glows and doesn't fall.
        val d = b.dreams.map { if (it.day() == b.today) it.copy(clarity = Clarity.LUCID.ordinal) else it }
        shot("2-lucid", Book(d, b.checks, b.rituals, b.ownSigns, b.settings, b.ru)) { bb, go -> HomeScreen(bb, go) }
    }
    @Test fun capture() = book().let { b ->
        val told = if (b.ru) "Снова школа, контрольная, а я опаздываю…" else "School again, a test, and I’m late…"
        shot("3-capture", Book(b.dreams, b.checks, b.rituals, b.ownSigns, b.settings.copy(draft = told), b.ru)) { bb, go -> CaptureScreen(bb, go) }
    }
    @Test fun clarity() = book().let { shot("4-clarity", it) { b, go -> ClarityScreen(b.dreams.first().id, b, go) } }
    @Test fun journal() = book().let { shot("5-journal", it) { b, go -> JournalScreen(b, go) } }
    @Test fun dream() = book().let { shot("6-dream", it) { b, go -> DreamScreen(b.dreams.first { d -> d.clarityOf == Clarity.LUCID }.id, b, go) } }
    @Test fun signs() = book().let { shot("7-signs", it) { b, go -> SignsScreen(b, go) } }
    @Test fun check() = book().let { shot("8-check", it) { b, go -> CheckScreen(b, go) } }
    @Test fun ritual() = book().let { shot("9-ritual", it) { b, go -> RitualScreen(b, go) } }
    @Test fun settings() = book().let { shot("10-settings", it) { b, go -> SettingsScreen(b, go) } }
}

class RenderArtScreensEn : ScreensBase() {
    override val lang = "en"
}

@Config(qualifiers = "ru-w411dp-h891dp-xxhdpi")
class RenderArtScreensRu : ScreensBase() {
    override val lang = "ru"
}
