package app.slumber.ui.atlas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.slumber.R
import app.slumber.container
import app.slumber.ui.Book
import app.slumber.ui.Go
import app.slumber.ui.T
import app.slumber.ui.art.medallion
import app.slumber.ui.comic.Cover
import app.slumber.ui.components.Chip
import app.slumber.ui.components.SectionTitle
import app.slumber.ui.ctx
import app.slumber.ui.theme.Ink
import app.slumber.ui.theme.U
import app.slumber.ui.theme.UType

/** One place of your dreamland: what it's like, every dream set there, the doors out of it. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpotScreen(id: Long, book: Book, go: Go) {
    val c = ctx()
    val s = book.spot(id)
    var forgetting by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding()) {
        app.slumber.ui.components.TopBar(s?.name.orEmpty(), go::back) {
            if (s != null) IconButton(onClick = { forgetting = true }) { Icon(Icons.Rounded.DeleteOutline, stringResource(R.string.spot_forget), tint = U.c.muted) }
        }
        if (s == null) return@Column
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).navigationBarsPadding()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(Modifier.size(120.dp)) { medallion(s.sceneOf, book.moodAt(s.id), s.id.toInt()) }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    var name by remember(s.id) { mutableStateOf(s.name) }
                    OutlinedTextField(name, {
                        name = it.take(40)
                        val n = name.trim()
                        if (n.isNotEmpty()) c.container.write { db.dao().update(s.copy(name = n)) }
                    }, singleLine = true, textStyle = UType.Strong)
                    Spacer(Modifier.height(4.dp))
                    Text(T.scene(c, s.sceneOf) + " · " + T.mood(c, book.moodAt(s.id)), style = UType.Muted)
                }
            }
            val doors = book.doors.filterKeys { it.first == id || it.second == id }
            if (doors.isNotEmpty()) {
                SectionTitle(stringResource(R.string.spot_doors))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for ((pair, _) in doors) {
                        val other = book.spot(if (pair.first == id) pair.second else pair.first) ?: continue
                        Chip(other.name, false, { go.spot(other.id) }, U.c.night)
                    }
                }
            }
            SectionTitle(stringResource(R.string.spot_dreams))
            val ds = book.dreamsAt(id)
            if (ds.isEmpty()) Text(stringResource(R.string.spot_none), style = UType.Muted)
            for (d in ds) {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(U.RMd).background(U.c.surface).clickable { go.dream(d.id) }.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Cover(book.cover(d.id), Modifier.size(52.dp).border(1.5.dp, Ink))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(T.weekday(c, d.day(book.zone)), style = UType.Small)
                        Text(d.title.ifBlank { d.text }, style = UType.Body, maxLines = 2)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
    if (forgetting && s != null) AlertDialog(
        onDismissRequest = { forgetting = false },
        title = { Text(stringResource(R.string.spot_forget_title, s.name)) },
        text = { Text(stringResource(R.string.spot_forget_sub)) },
        confirmButton = {
            TextButton({
                forgetting = false
                c.container.write { db.dao().forgetSpot(id) }
                go.back()
            }) { Text(stringResource(R.string.spot_forget), color = U.c.red) }
        },
        dismissButton = { TextButton({ forgetting = false }) { Text(stringResource(R.string.cancel)) } },
    )
}
