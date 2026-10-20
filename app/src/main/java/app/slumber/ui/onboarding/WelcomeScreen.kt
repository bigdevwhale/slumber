package app.slumber.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.slumber.R
import app.slumber.container
import app.slumber.notify.Notes
import app.slumber.ui.T
import app.slumber.ui.art.SpinningTop
import app.slumber.ui.art.rememberTopSpin
import app.slumber.ui.components.Chip
import app.slumber.ui.components.PrimaryButton
import app.slumber.ui.ctx
import app.slumber.ui.settings.BEDTIMES
import app.slumber.ui.settings.WAKE_TIMES
import app.slumber.ui.theme.U
import app.slumber.ui.theme.UType

/** Three pages: the top that never falls in a dream; the day's checks; when you sleep and wake. */
@Composable
fun WelcomeScreen(onDone: () -> Unit) {
    val c = ctx()
    var page by rememberSaveable { mutableIntStateOf(0) }
    var wake by rememberSaveable { mutableIntStateOf(7 * 60 + 30) }
    var bed by rememberSaveable { mutableIntStateOf(23 * 60) }
    val spin = rememberTopSpin()

    fun finish() {
        c.container.write {
            settings.update { it.copy(onboarded = true, wake = wake, bedtime = bed) }
            Notes.reschedule(c)
        }
        onDone()
    }

    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { finish() }

    Column(Modifier.fillMaxSize().background(U.c.bg).navigationBarsPadding()) {
        Box(Modifier.fillMaxWidth().weight(if (page == 2) 0.7f else 1f)) {
            // The first page's top never falls; the next ones run down like any other.
            SpinningTop(spin, 30f, endless = page == 0, rings = 2, modifier = Modifier.fillMaxSize())
        }
        AnimatedContent(page, transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(200)) }, label = "page", modifier = Modifier.padding(horizontal = 24.dp)) { p ->
            Column {
                Text(stringResource(listOf(R.string.w1_title, R.string.w2_title, R.string.w3_title)[p]), style = UType.Hero)
                Spacer(Modifier.height(10.dp))
                Text(stringResource(listOf(R.string.w1_text, R.string.w2_text, R.string.w3_text)[p]), style = UType.Sub)
                if (p == 2) {
                    Spacer(Modifier.height(14.dp))
                    Text(stringResource(R.string.set_bedtime), style = UType.Small)
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        BEDTIMES.forEach { m -> Chip(T.time(m), bed == m, { bed = m }) }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(stringResource(R.string.set_wake), style = UType.Small)
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        WAKE_TIMES.forEach { m -> Chip(T.time(m), wake == m, { wake = m }) }
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { i -> Box(Modifier.size(8.dp).clip(CircleShape).background(if (i == page) U.c.lamp else U.c.line)) }
        }
        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            stringResource(if (page < 2) R.string.next else R.string.start),
            {
                if (page < 2) { page++; spin.spin() }
                else if (Build.VERSION.SDK_INT >= 33 && !Notes.canNotify(c)) ask.launch(Manifest.permission.POST_NOTIFICATIONS)
                else finish()
            },
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(16.dp))
    }
}
