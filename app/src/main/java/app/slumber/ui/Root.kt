package app.slumber.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.slumber.MainActivity
import app.slumber.container
import app.slumber.data.Settings
import app.slumber.notify.Notes
import app.slumber.ru
import app.slumber.ui.capture.CaptureScreen
import app.slumber.ui.capture.ClarityScreen
import app.slumber.ui.check.CheckScreen
import app.slumber.ui.components.rememberNow
import app.slumber.ui.home.HomeScreen
import app.slumber.ui.journal.DreamScreen
import app.slumber.ui.journal.JournalScreen
import app.slumber.ui.onboarding.WelcomeScreen
import app.slumber.ui.ritual.RitualScreen
import app.slumber.ui.settings.SettingsScreen
import app.slumber.ui.signs.SignsScreen
import app.slumber.ui.theme.U

/** Everything a screen can open. */
class Go(private val nav: NavHostController) {
    fun capture() = nav.navigate("capture") { launchSingleTop = true }
    /** After telling a dream: one question, instead of the capture screen. */
    fun clarity(id: Long) = nav.navigate("clarity/$id") { popUpTo("main") }
    fun journal() = nav.navigate("journal") { launchSingleTop = true }
    fun dream(id: Long) = nav.navigate("dream/$id") { launchSingleTop = true }
    fun signs() = nav.navigate("signs") { launchSingleTop = true }
    fun check() = nav.navigate("check") { launchSingleTop = true }
    fun ritual() = nav.navigate("ritual") { launchSingleTop = true }
    fun settings() = nav.navigate("settings") { launchSingleTop = true }
    fun back() { nav.popBackStack() }
    fun home() { nav.popBackStack("main", false) }
}

@Composable
fun SlumberRoot(settings: Settings) {
    val context = LocalContext.current
    val container = context.container
    val nav = rememberNavController()
    val start = remember { if (settings.onboarded) "main" else "welcome" }

    val dao = container.db.dao()
    val dreams by dao.dreams().collectAsStateWithLifecycle(initialValue = null)
    val checks by dao.checks().collectAsStateWithLifecycle(initialValue = null)
    val rituals by dao.rituals().collectAsStateWithLifecycle(initialValue = null)
    val own by dao.ownSigns().collectAsStateWithLifecycle(initialValue = null)
    // Past four in the morning the day rolls over.
    val tick = rememberNow(60_000) / 3_600_000
    val ru = context.ru
    val book = remember(dreams, checks, rituals, own, settings, tick, ru) {
        Book(dreams.orEmpty(), checks.orEmpty(), rituals.orEmpty(), own.orEmpty(), settings, ru)
    }
    val go = remember(nav) { Go(nav) }

    // Onboarded elsewhere (the demo seed): leave the welcome screens.
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    LaunchedEffect(settings.onboarded, route) {
        if (settings.onboarded && route == "welcome") nav.navigate("main") { popUpTo("welcome") { inclusive = true } }
    }
    val open by container.open.collectAsStateWithLifecycle()
    LaunchedEffect(open, settings.onboarded) {
        val (what, _) = open ?: return@LaunchedEffect
        if (!settings.onboarded) return@LaunchedEffect
        container.open.value = null
        Notes.clear(context)
        nav.popBackStack("main", false)
        when (what) {
            MainActivity.OPEN_CHECK -> go.check()
            MainActivity.OPEN_RITUAL -> go.ritual()
            else -> go.capture()
        }
    }

    Box(Modifier.fillMaxSize().background(U.c.bg)) {
        NavHost(
            navController = nav,
            startDestination = start,
            modifier = Modifier.background(U.c.bg),
            enterTransition = { fadeIn(tween(260)) },
            exitTransition = { fadeOut(tween(180)) },
            popEnterTransition = { fadeIn(tween(260)) },
            popExitTransition = { fadeOut(tween(180)) },
        ) {
            composable("welcome") { WelcomeScreen { nav.navigate("main") { popUpTo("welcome") { inclusive = true } } } }
            composable("main") { HomeScreen(book, go) }
            composable("capture") { CaptureScreen(book, go) }
            composable("clarity/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e ->
                ClarityScreen(e.arguments?.getLong("id") ?: 0L, book, go)
            }
            composable("journal") { JournalScreen(book, go) }
            composable("dream/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e ->
                DreamScreen(e.arguments?.getLong("id") ?: 0L, book, go)
            }
            composable("signs") { SignsScreen(book, go) }
            composable("check") { CheckScreen(book, go) }
            composable("ritual") { RitualScreen(book, go) }
            composable("settings") { SettingsScreen(book, go) }
        }
    }
}
