package app.slumber

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import app.slumber.data.DemoSeed
import app.slumber.domain.Act
import app.slumber.domain.Kind
import app.slumber.domain.Look
import app.slumber.domain.Mood
import app.slumber.domain.Scene
import app.slumber.ui.Book
import app.slumber.ui.Go
import app.slumber.ui.art.Art
import app.slumber.ui.art.Body
import app.slumber.ui.art.Shot
import app.slumber.ui.art.panel
import app.slumber.ui.atlas.SpotScreen
import app.slumber.ui.capture.CaptureScreen
import app.slumber.ui.cast.PersonScreen
import app.slumber.ui.dream.DreamScreen
import app.slumber.ui.edit.EditScreen
import app.slumber.ui.home.HomeScreen
import app.slumber.ui.onboarding.WelcomeScreen
import app.slumber.ui.settings.SettingsScreen
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
        Book(dao.allDreams(), dao.allPanels(), dao.allSpots(), dao.allPeople(), c.settings.settings.first(), app.ru)
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

    private fun Book.byTitle(n: Int) = dreams.sortedBy { it.at }.reversed()[n].id

    @Test fun welcome() = book().let { shot("0-welcome", it) { _, _ -> WelcomeScreen {} } }
    @Test fun home() = book().let { shot("1-home", it) { b, go -> HomeScreen(b, go) } }
    @Test fun capture() = book().let { b ->
        val told = if (b.ru) "Я в бабушкином доме, но там бесконечный коридор, за мной идёт кто-то без лица…" else "I’m in Grandma’s house, but there’s an endless corridor, someone without a face is following me…"
        shot("2-capture", Book(b.dreams, emptyList(), b.spots, b.people, b.settings.copy(draft = told), b.ru)) { bb, go -> CaptureScreen(bb, go) }
    }
    @Test fun dream() = book().let { shot("3-dream", it) { b, go -> DreamScreen(b.byTitle(0), b, go) } }
    @Test fun rewritten() = book().let { b -> shot("4-rewritten", b) { bb, go -> DreamScreen(bb.dreams.first { bb.hasAlt(it.id) }.id, bb, go) } }
    @Test fun edit() = book().let { shot("5-edit", it) { b, go -> EditScreen(b.byTitle(0), 0, b, go) } }
    @Test fun atlas() = book().let { shot("6-atlas", it) { b, go -> HomeScreen(b, go, startTab = 1) } }
    @Test fun spot() = book().let { shot("7-spot", it) { b, go -> SpotScreen(b.spots.first().id, b, go) } }
    @Test fun cast() = book().let { shot("8-cast", it) { b, go -> HomeScreen(b, go, startTab = 2) } }
    @Test fun person() = book().let { shot("9-person", it) { b, go -> PersonScreen(b.cast.first().id, "", b, go) } }
    @Test fun settings() = book().let { shot("10-settings", it) { b, go -> SettingsScreen(b, go) } }

    /** Every place in every mood's colours, and every act: the whole vocabulary on two sheets. */
    private val me = Body(Look.Dreamer)
    private val other = Body(Look.of(Kind.SOMEONE), Kind.SOMEONE)

    @Test fun sheetScenes() {
        shot("sheet-scenes", book(), 500) { _, _ ->
            Column(Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Scene.entries.chunked(5).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        row.forEach { s ->
                            Column {
                                Canvas(Modifier.size(78.dp)) { panel(Shot(s, Act.STAND, Mood.entries[s.ordinal % Mood.entries.size], listOf(me), seed = s.ordinal)) }
                                Text(s.name, fontSize = 8.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    @Test fun sheetActs() {
        shot("sheet-acts", book(), 500) { _, _ ->
            Column(Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Act.entries.chunked(5).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        row.forEach { a ->
                            Column {
                                Canvas(Modifier.size(78.dp)) { panel(Shot(Scene.FIELD, a, Mood.CALM, listOf(me, other), seed = a.ordinal)) }
                                Text(a.name, fontSize = 8.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

class RenderArtScreensEn : ScreensBase() {
    override val lang = "en"
}

@Config(qualifiers = "ru-w411dp-h891dp-xxhdpi")
class RenderArtScreensRu : ScreensBase() {
    override val lang = "ru"
}
