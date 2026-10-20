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
import app.slumber.notify.Morning
import app.slumber.ui.Book
import app.slumber.ui.Go
import app.slumber.ui.Language
import app.slumber.ui.T
import app.slumber.ui.components.Chip
import app.slumber.ui.components.InfoRow
import app.slumber.ui.components.SectionTitle
import app.slumber.ui.components.Toggle
import app.slumber.ui.components.TopBar
import app.slumber.ui.components.UCard
import app.slumber.ui.ctx
import app.slumber.ui.theme.U
import app.slumber.ui.theme.UType

/** Waking times to choose from, every half hour from five to ten. */
val WAKE_TIMES = (10..20).map { it * 30 }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(book: Book, go: Go) {
    val c = ctx()
    val s = book.settings

    fun set(change: (Settings) -> Settings) = c.container.write {
        settings.update(change)
        Morning.reschedule(c)
    }

    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok -> if (ok) Morning.show(c) }

    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding()) {
        TopBar(stringResource(R.string.settings), go::back)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).navigationBarsPadding()) {
            SectionTitle(stringResource(R.string.set_morning))
            UCard {
                Toggle(stringResource(R.string.set_morning_on), stringResource(R.string.set_morning_on_sub), s.morning) { v -> set { it.copy(morning = v) } }
                if (s.morning) {
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.set_wake), style = UType.Strong)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        WAKE_TIMES.forEach { m -> Chip(T.time(m), s.wake == m, { set { it.copy(wake = m) } }) }
                    }
                }
                InfoRow(Icons.Rounded.NotificationsActive, stringResource(R.string.set_try_sub), stringResource(R.string.set_try)) {
                    if (Morning.canNotify(c)) Morning.show(c)
                    else if (Build.VERSION.SDK_INT >= 33) ask.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                InfoRow(Icons.Rounded.BatteryAlert, stringResource(R.string.set_battery_sub), stringResource(R.string.set_battery)) {
                    val app = Intent(SystemSettings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", c.packageName, null))
                    try {
                        c.startActivity(Intent(SystemSettings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                    } catch (_: ActivityNotFoundException) {
                        c.startActivity(app)
                    }
                }
            }

            SectionTitle(stringResource(R.string.set_telling))
            UCard {
                Toggle(stringResource(R.string.set_fade), stringResource(R.string.set_fade_sub), s.fade) { v -> set { it.copy(fade = v) } }
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
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.set_credit), style = UType.Muted)
            Spacer(Modifier.height(28.dp))
        }
    }
}
