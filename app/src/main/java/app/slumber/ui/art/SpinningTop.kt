package app.slumber.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/** The spin in progress: when it was set going, and the clock it's read against. */
class TopSpin(start: Long) {
    var started by mutableLongStateOf(start)
    var now by mutableLongStateOf(start)
    val elapsed: Float get() = (now - started) / 1000f
    fun spin() { started = now }
}

@Composable
fun rememberTopSpin(): TopSpin {
    val spin = remember { TopSpin(System.currentTimeMillis()) }
    if (!Art.still) LaunchedEffect(spin) {
        while (true) withFrameMillis { spin.now = System.currentTimeMillis() }
    }
    return spin
}

private const val RISE_S = 0.5f
private const val FALL_S = 0.8f
private const val FALLEN_TILT = 76f

/** How the top stands [t] seconds into a spin that lasts [seconds]: rising, spinning down, wobbling wider, falling, resting. */
fun pose(t: Float, seconds: Float, endless: Boolean, rings: Int): TopPose {
    val turnSpeed = 2 * PI.toFloat() * 3.2f
    if (t < RISE_S) {
        val f = t / RISE_S
        return TopPose(tilt = FALLEN_TILT * (1 - f).pow(2) + 3f * f, precession = 0f, turn = turnSpeed * t, rings = rings, glow = if (endless) f * 0.7f else 0f)
    }
    if (endless) {
        return TopPose(tilt = 3f + 1.5f * sin(t * 1.3f), precession = t * 0.6f, turn = turnSpeed * t, rings = rings, glow = 0.6f + 0.2f * sin(t * 0.9f))
    }
    val s = (t - RISE_S).coerceAtMost(seconds)
    val f = s / seconds
    // It slows evenly, so its turn is the area under a falling speed; the wobble widens as it slows.
    val turn = turnSpeed * (RISE_S + s - 0.85f * s * s / (2 * seconds))
    val prec = 0.5f * s + 2.2f * s * f
    if (t - RISE_S < seconds) return TopPose(tilt = 3f + 25f * f.pow(2.5f), precession = prec, turn = turn, rings = rings)
    val g = min(1f, (t - RISE_S - seconds) / FALL_S)
    val ease = 1 - (1 - g).pow(3)
    return TopPose(tilt = 28f + (FALLEN_TILT - 28f) * ease, precession = prec + 0.6f * ease, turn = turn + 2f * ease, rings = rings)
}

/** Is the spin over and the top lying on its side? */
fun fallen(t: Float, seconds: Float, endless: Boolean) = !endless && t > RISE_S + seconds

/** The desk and the top on it. A tap sets it spinning again. */
@Composable
fun SpinningTop(spin: TopSpin, seconds: Float, endless: Boolean, rings: Int, modifier: Modifier = Modifier) {
    val t = if (Art.still) RISE_S + seconds * 0.35f else spin.elapsed
    val p = pose(t, seconds, endless, rings)
    Canvas(modifier.clickable(remember { MutableInteractionSource() }, null) { spin.spin() }) {
        desk()
        top(p)
    }
}
