package app.slumber.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

data class Settings(
    val onboarded: Boolean = false,
    /** A soft "What did you dream?" around the time you wake. */
    val morning: Boolean = true,
    /** When you usually wake, minutes after midnight. */
    val wake: Int = 7 * 60 + 30,
    /** The words you're typing slowly pale while you pause, as a dream does. They never actually go. */
    val fade: Boolean = true,
    /** Kept while telling, so a closed app never loses a dream half told. */
    val draft: String = "",
)

class SettingsStore(context: Context) {
    private val store = context.applicationContext.dataStore

    val settings: Flow<Settings> = store.data.map { it.read() }

    private fun Preferences.read() = Settings(
        onboarded = this[ONBOARDED] ?: false,
        morning = this[MORNING] ?: true,
        wake = this[WAKE] ?: (7 * 60 + 30),
        fade = this[FADE] ?: true,
        draft = this[DRAFT] ?: "",
    )

    suspend fun now(): Settings = settings.first()

    suspend fun update(change: (Settings) -> Settings) {
        store.edit {
            val s = change(it.read())
            it[ONBOARDED] = s.onboarded
            it[MORNING] = s.morning
            it[WAKE] = s.wake
            it[FADE] = s.fade
            it[DRAFT] = s.draft
        }
    }

    private companion object {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val MORNING = booleanPreferencesKey("morning")
        val WAKE = intPreferencesKey("wake")
        val FADE = booleanPreferencesKey("fade")
        val DRAFT = stringPreferencesKey("draft")
    }
}
