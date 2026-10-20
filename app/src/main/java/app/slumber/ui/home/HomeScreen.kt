package app.slumber.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.BackHand
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.slumber.R
import app.slumber.ui.Book
import app.slumber.ui.Go
import app.slumber.ui.T
import app.slumber.ui.art.Art
import app.slumber.ui.art.SpinningTop
import app.slumber.ui.art.fallen
import app.slumber.ui.art.rememberTopSpin
import app.slumber.ui.components.PrimaryButton
import app.slumber.ui.ctx
import app.slumber.ui.theme.Banner
import app.slumber.ui.theme.U
import app.slumber.ui.theme.UType
import java.time.LocalTime
import kotlin.math.ceil

/** What the day wants next: the dream in the morning, the ritual at night, a check in between. */
private enum class Next { DREAM, CHECK, RITUAL }

/**
 * The desk and the top. It spins as long as your practice has wound it up; tap and it spins again. Below,
 * the day: a dream, the checks, the ritual.
 */
@Composable
fun HomeScreen(book: Book, go: Go) {
    val c = ctx()
    val spin = rememberTopSpin()
    val s = book.settings
    val minute = LocalTime.now().let { it.hour * 60 + it.minute }
    val next = when {
        !book.dreamtToday && minute < 13 * 60 && minute >= 4 * 60 -> Next.DREAM
        !book.ritualToday && (minute >= s.bedtime - 120 || minute < 4 * 60) -> Next.RITUAL
        else -> Next.CHECK
    }

    Column(Modifier.fillMaxSize().background(U.c.bg)) {
        Box(Modifier.fillMaxWidth().weight(1f)) {
            SpinningTop(spin, book.seconds, book.endless, book.lucid, Modifier.fillMaxSize())
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = 20.dp, end = 4.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.app_name), fontFamily = Banner, fontSize = 26.sp, color = U.c.ink, modifier = Modifier.weight(1f))
                IconButton(onClick = go::journal) { Icon(Icons.Rounded.AutoStories, stringResource(R.string.journal), tint = U.c.muted) }
                IconButton(onClick = go::settings) { Icon(Icons.Rounded.Settings, stringResource(R.string.settings), tint = U.c.muted) }
            }
            val t = spin.elapsed
            val status = when {
                book.endless -> stringResource(R.string.top_endless)
                fallen(t, book.seconds, false) && !Art.still -> stringResource(R.string.top_fallen)
                else -> stringResource(R.string.top_spins, T.seconds(c, ceil(book.seconds).toInt()))
            }
            Text(
                status, style = UType.Muted, color = if (book.endless) U.c.gold else U.c.muted, textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Tile(stringResource(R.string.day_dream), if (book.dreamtToday) "✓" else "—", book.dreamtToday, go::capture)
                Tile(stringResource(R.string.day_checks), "${book.checksToday}/${s.perDay}", book.checksToday >= s.perDay, go::check)
                Tile(stringResource(R.string.day_ritual), if (book.ritualToday) "✓" else "—", book.ritualToday, go::ritual)
            }
            Spacer(Modifier.height(12.dp))
            when (next) {
                Next.DREAM -> PrimaryButton(stringResource(R.string.tell), go::capture, Modifier.fillMaxWidth(), icon = Icons.Rounded.NightsStay)
                Next.RITUAL -> PrimaryButton(stringResource(R.string.ritual), go::ritual, Modifier.fillMaxWidth(), icon = Icons.Rounded.Bedtime)
                Next.CHECK -> PrimaryButton(stringResource(R.string.check_now), go::check, Modifier.fillMaxWidth(), icon = Icons.Rounded.BackHand)
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth().clip(U.RMd).clickable(onClick = go::signs).padding(vertical = 10.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.home_sign), style = UType.Small)
                    Text(book.tonight?.label(book.ru) ?: stringResource(R.string.home_sign_none), style = UType.Body, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (book.lucid + book.glimmers > 0) Column(horizontalAlignment = Alignment.End) {
                    Text(T.plural(c, R.plurals.lucid_count, book.lucid), style = UType.Small, color = U.c.gold)
                    Text(T.plural(c, R.plurals.glimmer_count, book.glimmers), style = UType.Small)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RowScope.Tile(label: String, value: String, done: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.weight(1f).clip(U.RMd).background(U.c.surface).border(1.dp, if (done) U.c.lamp.copy(alpha = 0.5f) else U.c.line, U.RMd)
            .clickable(onClick = onClick).padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = UType.Title.copy(fontSize = 20.sp), color = if (done) U.c.lamp else U.c.muted)
        Text(label, style = UType.Small, color = if (done) U.c.ink else U.c.faint)
    }
}
