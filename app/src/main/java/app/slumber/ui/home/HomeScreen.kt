package app.slumber.ui.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.slumber.R
import app.slumber.data.Dream
import app.slumber.domain.Bed
import app.slumber.ui.Book
import app.slumber.ui.Go
import app.slumber.ui.T
import app.slumber.ui.art.Art
import app.slumber.ui.art.walkingBed
import app.slumber.ui.atlas.AtlasTab
import app.slumber.ui.cast.CastTab
import app.slumber.ui.comic.Cover
import app.slumber.ui.comic.LucidStar
import app.slumber.ui.components.PrimaryButton
import app.slumber.ui.components.SectionTitle
import app.slumber.ui.components.UCard
import app.slumber.ui.ctx
import app.slumber.ui.theme.Banner
import app.slumber.ui.theme.Ink
import app.slumber.ui.theme.U
import app.slumber.ui.theme.UType
import java.time.LocalTime

@Composable
fun HomeScreen(book: Book, go: Go, startTab: Int = 0) {
    var tab by rememberSaveable { mutableIntStateOf(startTab) }
    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().height(60.dp).padding(start = 20.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.app_name), fontFamily = Banner, fontSize = 26.sp, color = U.c.ink, modifier = Modifier.weight(1f))
            IconButton(onClick = go::settings) { Icon(Icons.Rounded.Settings, stringResource(R.string.settings), tint = U.c.muted) }
        }
        Box(Modifier.weight(1f)) {
            when (tab) {
                0 -> DreamsTab(book, go)
                1 -> AtlasTab(book, go)
                else -> CastTab(book, go)
            }
        }
        Row(
            Modifier.fillMaxWidth().background(U.c.surface).border(1.dp, U.c.line).navigationBarsPadding().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            TabItem(Icons.Rounded.AutoStories, stringResource(R.string.tab_dreams), tab == 0) { tab = 0 }
            TabItem(Icons.Rounded.Map, stringResource(R.string.tab_atlas), tab == 1) { tab = 1 }
            TabItem(Icons.Rounded.Groups, stringResource(R.string.tab_cast), tab == 2) { tab = 2 }
        }
    }
}

@Composable
private fun TabItem(icon: ImageVector, label: String, on: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.clip(U.RMd).clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, null, tint = if (on) U.c.red else U.c.faint)
        Text(label, style = UType.Small, color = if (on) U.c.red else U.c.faint)
    }
}

@Composable
private fun DreamsTab(book: Book, go: Go) {
    val c = ctx()
    val months = remember(book) { book.dreams.groupBy { it.day(book.zone).withDayOfMonth(1) } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 24.dp)) {
        item { BedCard(book, go) }
        val rehearse = book.toRehearse
        if (rehearse != null && LocalTime.now().hour >= 19) item {
            Spacer(Modifier.height(12.dp))
            UCard(onClick = { go.dream(rehearse.id) }) {
                Text(stringResource(R.string.rehearse_title), style = UType.Strong)
                Text(stringResource(R.string.rehearse_sub), style = UType.Muted)
            }
        }
        book.returning.entries.firstOrNull()?.let { (label, ds) ->
            item {
                Spacer(Modifier.height(12.dp))
                UCard(onClick = { go.person(0, label) }) {
                    Text(stringResource(R.string.returning_title, label), style = UType.Strong)
                    Text(T.plural(c, R.plurals.returning_sub, ds.size), style = UType.Muted)
                }
            }
        }
        if (book.dreams.isEmpty()) item {
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.empty_title), style = UType.Title)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.empty_sub), style = UType.Sub)
        }
        for ((month, ds) in months) {
            item(key = "m$month") { SectionTitle(T.month(c, month)) }
            items(ds, key = { it.id }) { d -> DreamRow(d, book, go) }
        }
    }
}

/** The night card: Nemo's bed at its stage, how many mornings, the button to tell a dream. */
@Composable
private fun BedCard(book: Book, go: Go) {
    val c = ctx()
    val phase = if (Art.still || book.stage == 0) 0.25f else {
        val t = rememberInfiniteTransition(label = "bed")
        t.animateFloat(0f, 1f, infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart), label = "walk").value
    }
    Column(Modifier.fillMaxWidth().clip(U.RLg).background(U.c.night).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(Modifier.size(120.dp, 96.dp)) { walkingBed(book.stage, phase) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(T.plural(c, R.plurals.mornings, book.mornings), fontFamily = Banner, fontSize = 20.sp, color = Color(0xFFF4EAD5))
                Spacer(Modifier.height(4.dp))
                val next = Bed.toNext(book.mornings)
                Text(
                    c.resources.getStringArray(R.array.bed_stages)[book.stage] + (next?.let { " " + T.plural(c, R.plurals.bed_next, it) } ?: ""),
                    style = UType.Muted, color = Color(0xFFC9CBE0),
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        PrimaryButton(stringResource(R.string.tell), go::capture, Modifier.fillMaxWidth(), icon = Icons.Rounded.Bedtime)
    }
}

@Composable
private fun DreamRow(d: Dream, book: Book, go: Go) {
    val c = ctx()
    val panels = book.panels(d.id, book.showing(d.id))
    Row(
        Modifier.fillMaxWidth().padding(vertical = 5.dp).clip(U.RMd).background(U.c.surface).border(1.dp, U.c.line, U.RMd)
            .clickable { go.dream(d.id) }.padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Cover(book.cover(d.id), Modifier.size(64.dp).border(1.5.dp, Ink))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(T.weekday(c, d.day(book.zone)), style = UType.Small, modifier = Modifier.weight(1f, fill = false))
                if (d.lucid) LucidStar(14f)
            }
            Text(
                d.title.ifBlank { panels.firstOrNull()?.caption ?: d.text },
                style = UType.Body, maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
            val places = panels.map { p -> book.spot(p.spotId)?.name ?: T.scene(c, p.sceneOf) }.distinct().take(3)
            Text(
                (listOf(T.mood(c, d.moodOf)) + places).joinToString(" · ") + if (book.hasAlt(d.id)) " · " + stringResource(R.string.rewritten_mark) else "",
                style = UType.Small, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
