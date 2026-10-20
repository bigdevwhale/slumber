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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import app.slumber.domain.Act
import app.slumber.domain.Kind
import app.slumber.domain.Look
import app.slumber.domain.Mood
import app.slumber.domain.Scene
import app.slumber.notify.Morning
import app.slumber.ui.T
import app.slumber.ui.art.Body
import app.slumber.ui.art.Shot
import app.slumber.ui.art.panel
import app.slumber.ui.art.walkingBed
import app.slumber.ui.components.Chip
import app.slumber.ui.components.PrimaryButton
import app.slumber.ui.ctx
import app.slumber.ui.settings.WAKE_TIMES
import app.slumber.ui.theme.Ink
import app.slumber.ui.theme.Newsprint
import app.slumber.ui.theme.U
import app.slumber.ui.theme.UType

/** Three pages: a dream becomes a page; your dreamland and its people; when you wake. */
@Composable
fun WelcomeScreen(onDone: () -> Unit) {
    val c = ctx()
    var page by rememberSaveable { mutableIntStateOf(0) }
    var wake by rememberSaveable { mutableIntStateOf(7 * 60 + 30) }

    fun finish() {
        c.container.write {
            settings.update { it.copy(onboarded = true, wake = wake, morning = true) }
            Morning.reschedule(c)
        }
        onDone()
    }

    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { finish() }

    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding().navigationBarsPadding()) {
        Box(Modifier.fillMaxWidth().weight(if (page == 2) 0.75f else 1f).padding(16.dp).clip(U.RLg).border(2.dp, Ink, U.RLg)) {
            Canvas(Modifier.fillMaxSize()) {
                when (page) {
                    0 -> panel(Shot(Scene.CITY, Act.FLY, Mood.WONDER, listOf(Body(Look.Dreamer), Body(Look.of(Kind.BIRD), Kind.BIRD)), seed = 4))
                    1 -> { drawRect(Newsprint.night); walkingBed(3, 0.3f) }
                    else -> panel(Shot(Scene.CORRIDOR, Act.CHASED, Mood.FEAR, listOf(Body(Look.Dreamer), Body(Look.of(Kind.SHADOW), Kind.SHADOW)), seed = 2))
                }
            }
        }
        AnimatedContent(page, transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(200)) }, label = "page", modifier = Modifier.padding(horizontal = 24.dp)) { p ->
            Column {
                Text(stringResource(listOf(R.string.w1_title, R.string.w2_title, R.string.w3_title)[p]), style = UType.Hero)
                Spacer(Modifier.height(10.dp))
                Text(stringResource(listOf(R.string.w1_text, R.string.w2_text, R.string.w3_text)[p]), style = UType.Sub)
                if (p == 2) {
                    Spacer(Modifier.height(14.dp))
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
            repeat(3) { i -> Box(Modifier.size(8.dp).clip(CircleShape).background(if (i == page) U.c.red else U.c.line)) }
        }
        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            stringResource(if (page < 2) R.string.next else R.string.start),
            {
                if (page < 2) page++
                else if (Build.VERSION.SDK_INT >= 33 && !Morning.canNotify(c)) ask.launch(Manifest.permission.POST_NOTIFICATIONS)
                else finish()
            },
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(16.dp))
    }
}

