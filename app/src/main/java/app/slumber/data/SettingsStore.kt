package app.slumber.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
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
    val wake: Int = 7 * 60 + 30,
    /** Reality checks during the day, at moments you can't guess. */
    val checks: Boolean = true,
    val from: Int = 10 * 60,
    val to: Int = 21 * 60,
    val perDay: Int = 5,
    /** The bedtime ritual, and a reminder for it this long before [bedtime]. */
    val bedtimeNote: Boolean = true,
    val bedtime: Int = 23 * 60,
    /** The words you're typing slowly pale while you pause, as a dream does. They never actually go. */
    val fade: Boolean = true,
    /** Kept while telling, so a closed app never loses a dream half told. */
    val draft: String = "",
    /** Tonight's sign for the ritual: a [app.slumber.domain.Sign] key, or "" for your most frequent. */
    val sign: String = "",
    /** Mixed into the check plan so no two phones are nudged at the same moments. */
    val salt: Long = 0,
)

class SettingsStore(context: Context) {
    private val store = context.applicationContext.dataStore

    val settings: Flow<Settings> = store.data.map { it.read() }

    private fun Preferences.read() = Settings(
        onboarded = this[ONBOARDED] ?: false,
        morning = this[MORNING] ?: true,
        wake = this[WAKE] ?: (7 * 60 + 30),
        checks = this[CHECKS] ?: true,
        from = this[FROM] ?: (10 * 60),
        to = this[TO] ?: (21 * 60),
        perDay = this[PER_DAY] ?: 5,
        bedtimeNote = this[BEDTIME_NOTE] ?: true,
        bedtime = this[BEDTIME] ?: (23 * 60),
        fade = this[FADE] ?: true,
        draft = this[DRAFT] ?: "",
        sign = this[SIGN] ?: "",
        salt = this[SALT] ?: 0,
    )

    suspend fun now(): Settings = settings.first()

    suspend fun update(change: (Settings) -> Settings) {
        store.edit {
            val s = change(it.read())
            it[ONBOARDED] = s.onboarded
            it[MORNING] = s.morning
            it[WAKE] = s.wake
            it[CHECKS] = s.checks
            it[FROM] = s.from
            it[TO] = s.to
            it[PER_DAY] = s.perDay
            it[BEDTIME_NOTE] = s.bedtimeNote
            it[BEDTIME] = s.bedtime
            it[FADE] = s.fade
            it[DRAFT] = s.draft
            it[SIGN] = s.sign
            it[SALT] = s.salt
        }
    }

    /** A salt is picked once, on the first launch. */
    suspend fun ensureSalt() {
        if (now().salt == 0L) update { it.copy(salt = (1L..Long.MAX_VALUE / 2).random()) }
    }

    private companion object {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val MORNING = booleanPreferencesKey("morning")
        val WAKE = intPreferencesKey("wake")
        val CHECKS = booleanPreferencesKey("checks")
        val FROM = intPreferencesKey("from")
        val TO = intPreferencesKey("to")
        val PER_DAY = intPreferencesKey("per_day")
        val BEDTIME_NOTE = booleanPreferencesKey("bedtime_note")
        val BEDTIME = intPreferencesKey("bedtime")
        val FADE = booleanPreferencesKey("fade")
        val DRAFT = stringPreferencesKey("draft")
        val SIGN = stringPreferencesKey("sign")
        val SALT = longPreferencesKey("salt")
    }
}
