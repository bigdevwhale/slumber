package app.slumber.ui.settings

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings as SystemSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.slumber.R
import app.slumber.container
import app.slumber.data.Settings
import app.slumber.notify.Notes
import app.slumber.ui.Book
import app.slumber.ui.Go
import app.slumber.ui.Language
import app.slumber.ui.T
import app.slumber.ui.components.Chip
import app.slumber.ui.components.InfoRow
import app.slumber.ui.components.SectionTitle
import app.slumber.ui.components.Segmented
import app.slumber.ui.components.Toggle
import app.slumber.ui.components.TopBar
import app.slumber.ui.components.UCard
import app.slumber.ui.ctx
import app.slumber.ui.theme.U
import app.slumber.ui.theme.UType

/** Waking times, every half hour from five to ten. */
val WAKE_TIMES = (10..20).map { it * 30 }
/** Bedtimes, every half hour from nine in the evening to one at night. */
val BEDTIMES = (42..50).map { it * 30 }
val CHECK_FROM = listOf(8, 9, 10, 11, 12)
val CHECK_TO = listOf(18, 19, 20, 21, 22)
val PER_DAY = listOf(3, 5, 7, 10)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(book: Book, go: Go) {
    val c = ctx()
    val s = book.settings

    fun set(change: (Settings) -> Settings) = c.container.write {
        settings.update(change)
        Notes.reschedule(c)
    }

    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok -> if (ok) Notes.check(c) }

    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding()) {
        TopBar(stringResource(R.string.settings), go::back)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).navigationBarsPadding()) {
            SectionTitle(stringResource(R.string.set_checks))
            UCard {
                Toggle(stringResource(R.string.set_checks_on), stringResource(R.string.set_checks_on_sub), s.checks) { v -> set { it.copy(checks = v) } }
                if (s.checks) {
                    Spacer(Modifier.height(14.dp))
                    Text(stringResource(R.string.set_per_day), style = UType.Strong)
                    Spacer(Modifier.height(8.dp))
                    Segmented(PER_DAY, s.perDay, { it.toString() }, { n -> set { it.copy(perDay = n) } })
                    Spacer(Modifier.height(14.dp))
                    Text(stringResource(R.string.set_from), style = UType.Strong)
                    Spacer(Modifier.height(8.dp))
                    Segmented(CHECK_FROM, s.from / 60, { T.time(it * 60) }, { h -> set { it.copy(from = h * 60) } })
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.set_to), style = UType.Strong)
                    Spacer(Modifier.height(8.dp))
                    Segmented(CHECK_TO, s.to / 60, { T.time(it * 60) }, { h -> set { it.copy(to = h * 60) } })
                }
                InfoRow(Icons.Rounded.NotificationsActive, stringResource(R.string.set_try_sub), stringResource(R.string.set_try)) {
                    if (Notes.canNotify(c)) Notes.check(c)
                    else if (Build.VERSION.SDK_INT >= 33) ask.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            SectionTitle(stringResource(R.string.set_night))
            UCard {
                Toggle(stringResource(R.string.set_bedtime_on), stringResource(R.string.set_bedtime_on_sub), s.bedtimeNote) { v -> set { it.copy(bedtimeNote = v) } }
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.set_bedtime), style = UType.Strong)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BEDTIMES.forEach { m -> Chip(T.time(m), s.bedtime == m, { set { it.copy(bedtime = m) } }) }
                }
            }

            SectionTitle(stringResource(R.string.set_morning))
            UCard {
                Toggle(stringResource(R.string.set_morning_on), stringResource(R.string.set_morning_on_sub), s.morning) { v -> set { it.copy(morning = v) } }
                if (s.morning) {
                    Spacer(Modifier.height(10.dp))
                    Text(stringResource(R.string.set_wake), style = UType.Strong)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        WAKE_TIMES.forEach { m -> Chip(T.time(m), s.wake == m, { set { it.copy(wake = m) } }) }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Toggle(stringResource(R.string.set_fade), stringResource(R.string.set_fade_sub), s.fade) { v -> set { it.copy(fade = v) } }
                InfoRow(Icons.Rounded.BatteryAlert, stringResource(R.string.set_battery_sub), stringResource(R.string.set_battery)) {
                    val app = Intent(SystemSettings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", c.packageName, null))
                    try {
                        c.startActivity(Intent(SystemSettings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                    } catch (_: ActivityNotFoundException) {
                        c.startActivity(app)
                    }
                }
            }

            SectionTitle(stringResource(R.string.set_language))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val cur = Language.current()
                Chip(stringResource(R.string.set_lang_system), cur == Language.SYSTEM, { Language.set(Language.SYSTEM) })
                Chip("English", cur == Language.EN, { Language.set(Language.EN) })
                Chip("Русский", cur == Language.RU, { Language.set(Language.RU) })
            }

            SectionTitle(stringResource(R.string.set_about))
            Text(stringResource(R.string.set_privacy), style = UType.Sub)
            Spacer(Modifier.height(28.dp))
        }
    }
}
