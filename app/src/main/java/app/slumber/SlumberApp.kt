package app.slumber

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import app.slumber.data.DemoSeed
import app.slumber.data.Person
import app.slumber.data.SettingsStore
import app.slumber.data.SlumberDatabase
import app.slumber.domain.Look
import app.slumber.notify.Morning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AppContainer(context: Context) {
    val db = SlumberDatabase.create(context)
    val settings = SettingsStore(context)
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Where a tap from outside wants to go ("capture" from the morning note or the widget) and when. */
    val open = MutableStateFlow<Pair<String, Long>?>(null)

    /** Fire-and-forget so a write survives the screen being closed right after tapping. */
    fun write(block: suspend AppContainer.() -> Unit) {
        scope.launch { block() }
    }

    private val meLock = Mutex()

    /** The dreamer is always in the cast, from the first launch (and only once, however many ask at once). */
    suspend fun ensureMe(name: String) {
        meLock.withLock {
            if (db.dao().me() == null) db.dao().insert(Person(name = name, look = Look.Dreamer.encode(), me = true))
        }
    }
}

class SlumberApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Morning.ensureChannel(this)
        container.scope.launch {
            container.ensureMe(localized().getString(R.string.me))
            if (BuildConfig.DEMO) DemoSeed.seedIfEmpty(container, this@SlumberApp.ru)
            Morning.reschedule(this@SlumberApp)
        }
    }
}

val Context.container: AppContainer get() = (applicationContext as SlumberApp).container

/** Resources in the in-app language, for notifications, which AppCompat doesn't localise before Android 13. */
fun Context.localized(): Context {
    val tags = AppCompatDelegate.getApplicationLocales()
    if (tags.isEmpty) return this
    val config = Configuration(resources.configuration)
    config.setLocale(tags[0])
    return createConfigurationContext(config)
}

/** Is this context in Russian? */
val Context.ru: Boolean get() = resources.configuration.locales[0].language == "ru"
