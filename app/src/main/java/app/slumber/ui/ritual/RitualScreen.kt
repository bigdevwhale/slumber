package app.slumber.ui.ritual

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.slumber.R
import app.slumber.container
import app.slumber.data.Ritual
import app.slumber.ui.Book
import app.slumber.ui.Go
import app.slumber.ui.art.Art
import app.slumber.ui.art.desk
import app.slumber.ui.art.pose
import app.slumber.ui.art.top
import app.slumber.ui.components.PrimaryButton
import app.slumber.ui.components.SoftButton
import app.slumber.ui.components.TopBar
import app.slumber.ui.ctx
import app.slumber.ui.theme.U
import app.slumber.ui.theme.Walnut
import app.slumber.ui.theme.UType

/** How long to hold the top while repeating the intention. */
private const val HOLD_S = 20f

/**
 * Before sleep (MILD, the best-tested way into lucid dreams): recall a dream, look at tonight's sign, then
 * hold the top spinning while you repeat the intention to yourself. Then the lamp goes down.
 */
@Composable
fun RitualScreen(book: Book, go: Go) {
    val c = ctx()
    var step by rememberSaveable { mutableIntStateOf(0) }
    val sign = book.tonight?.label(book.ru)

    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding().navigationBarsPadding()) {
        TopBar(stringResource(R.string.ritual), go::back)
        AnimatedContent(step, Modifier.weight(1f), transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(250)) }, label = "step") { s ->
            Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                when (s) {
                    0 -> {
                        Spacer(Modifier.height(24.dp))
                        Text(stringResource(R.string.r1_title), style = UType.Display)
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(R.string.r1_text), style = UType.Sub)
                        Spacer(Modifier.height(20.dp))
                        Text(book.lastDream?.text ?: stringResource(R.string.r1_none), style = UType.Told, maxLines = 9)
                        Spacer(Modifier.weight(1f))
                        Still()
                    }
                    1 -> {
                        Spacer(Modifier.height(24.dp))
                        Text(stringResource(R.string.r2_title), style = UType.Display)
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(R.string.r2_text), style = UType.Sub)
                        Spacer(Modifier.height(24.dp))
                        Text(sign ?: stringResource(R.string.r2_none), style = UType.Prompt, color = U.c.lamp)
                        Spacer(Modifier.height(16.dp))
                        SoftButton(stringResource(R.string.r2_change), go::signs)
                        Spacer(Modifier.weight(1f))
                        Still()
                    }
                    2 -> Hold(sign, onDone = {
                        c.container.write { db.dao().insert(Ritual()) }
                        step = 3
                    })
                    else -> {
                        Spacer(Modifier.weight(1f))
                        Text(stringResource(R.string.r4_title), style = UType.Hero)
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(R.string.r4_text), style = UType.Sub)
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        when (step) {
            0, 1 -> PrimaryButton(stringResource(R.string.next), { step++ }, Modifier.fillMaxWidth().padding(16.dp))
            3 -> PrimaryButton(stringResource(R.string.r4_done), go::home, Modifier.fillMaxWidth().padding(16.dp))
            else -> Spacer(Modifier.height(88.dp))
        }
    }
}

/** The top waiting on the desk, upright and still, while you recall and look. */
@Composable
private fun Still() {
    Canvas(Modifier.fillMaxWidth().height(220.dp).clip(U.RXl)) {
        desk(table = 0.62f)
        top(pose(0.6f, 60f, false, 0).copy(turn = 0.4f), cy = 0.84f, tall = 0.62f)
    }
}

/** Hold the top: it spins while your finger is on it, and a lamplit ring fills as you repeat the words. */
@Composable
private fun Hold(sign: String?, onDone: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    var held by remember { mutableFloatStateOf(if (Art.still) HOLD_S * 0.6f else 0f) }
    var spun by remember { mutableFloatStateOf(1.2f) }
    LaunchedEffect(pressed) {
        var last = 0L
        while (pressed) withFrameMillis { t ->
            if (last != 0L) {
                val dt = (t - last) / 1000f
                held += dt
                spun += dt
                if (held >= HOLD_S) { pressed = false; onDone() }
            }
            last = t
        }
    }
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.r3_title), style = UType.Display, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        Text(
            if (sign != null) stringResource(R.string.intention, sign) else stringResource(R.string.intention_plain),
            style = UType.Prompt, modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Box(
            Modifier.fillMaxWidth().aspectRatio(1f).clip(U.RXl).pointerInput(Unit) {
                detectTapGestures(onPress = {
                    pressed = true
                    tryAwaitRelease()
                    pressed = false
                })
            },
        ) {
            Canvas(Modifier.fillMaxSize()) {
                desk(table = 0.68f)
                // Held, it spins steadily; let go, and it waits upright where it was.
                top(pose(spun, 60f, true, 0).copy(glow = held / HOLD_S * 0.8f), cy = 0.8f, tall = 0.5f)
                val r = size.minDimension * 0.46f
                drawArc(
                    Walnut.lamp.copy(alpha = 0.85f), -90f, 360f * (held / HOLD_S).coerceAtMost(1f), false,
                    Offset(size.width / 2 - r, size.height / 2 - r), Size(r * 2, r * 2), style = Stroke(6f, cap = StrokeCap.Round),
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(stringResource(if (pressed) R.string.r3_holding else R.string.r3_hold), style = UType.Muted)
    }
}
