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
import app.slumber.container
import app.slumber.data.Settings
import app.slumber.notify.Morning
import app.slumber.ru
import app.slumber.ui.atlas.SpotScreen
import app.slumber.ui.capture.CaptureScreen
import app.slumber.ui.cast.PersonScreen
import app.slumber.ui.components.rememberNow
import app.slumber.ui.dream.DreamScreen
import app.slumber.ui.edit.EditScreen
import app.slumber.ui.home.HomeScreen
import app.slumber.ui.onboarding.WelcomeScreen
import app.slumber.ui.settings.SettingsScreen
import app.slumber.ui.theme.U

/** Everything a screen can open. */
class Go(private val nav: NavHostController) {
    fun capture() = nav.navigate("capture") { launchSingleTop = true }
    fun dream(id: Long, fresh: Boolean = false) = nav.navigate("dream/$id") {
        launchSingleTop = true
        if (fresh) popUpTo("main")
    }
    fun edit(id: Long, alt: Int) = nav.navigate("edit/$id/$alt") { launchSingleTop = true }
    fun spot(id: Long) = nav.navigate("spot/$id") { launchSingleTop = true }
    /** [id] 0 makes someone new; [label] fills in their name. */
    fun person(id: Long, label: String = "") = nav.navigate("person/$id?label=${android.net.Uri.encode(label)}") { launchSingleTop = true }
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
    val panels by dao.panels().collectAsStateWithLifecycle(initialValue = null)
    val spots by dao.spots().collectAsStateWithLifecycle(initialValue = null)
    val people by dao.people().collectAsStateWithLifecycle(initialValue = null)
    // Past midnight the day rolls over.
    val tick = rememberNow(60_000) / 3_600_000
    val ru = context.ru
    val book = remember(dreams, panels, spots, people, settings, tick, ru) {
        Book(dreams.orEmpty(), panels.orEmpty(), spots.orEmpty(), people.orEmpty(), settings, ru)
    }
    val go = remember(nav) { Go(nav) }

    // Onboarded elsewhere (the demo seed): leave the welcome screens.
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    LaunchedEffect(settings.onboarded, route) {
        if (settings.onboarded && route == "welcome") nav.navigate("main") { popUpTo("welcome") { inclusive = true } }
    }
    val open by container.open.collectAsStateWithLifecycle()
    LaunchedEffect(open, settings.onboarded) {
        if (open == null || !settings.onboarded) return@LaunchedEffect
        container.open.value = null
        Morning.clear(context)
        nav.popBackStack("main", false)
        go.capture()
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
            composable("dream/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e ->
                DreamScreen(e.arguments?.getLong("id") ?: 0L, book, go)
            }
            composable(
                "edit/{id}/{alt}",
                arguments = listOf(navArgument("id") { type = NavType.LongType }, navArgument("alt") { type = NavType.IntType }),
            ) { e ->
                EditScreen(e.arguments?.getLong("id") ?: 0L, e.arguments?.getInt("alt") ?: 0, book, go)
            }
            composable("spot/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e ->
                SpotScreen(e.arguments?.getLong("id") ?: 0L, book, go)
            }
            composable(
                "person/{id}?label={label}",
                arguments = listOf(navArgument("id") { type = NavType.LongType }, navArgument("label") { type = NavType.StringType; defaultValue = "" }),
            ) { e ->
                PersonScreen(e.arguments?.getLong("id") ?: 0L, e.arguments?.getString("label").orEmpty(), book, go)
            }
            composable("settings") { SettingsScreen(book, go) }
        }
    }
}
