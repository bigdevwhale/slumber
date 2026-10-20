package app.slumber.ui.cast

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.slumber.R
import app.slumber.container
import app.slumber.data.Person
import app.slumber.domain.Age
import app.slumber.domain.Base
import app.slumber.domain.Hair
import app.slumber.domain.Look
import app.slumber.domain.Trait
import app.slumber.ui.Book
import app.slumber.ui.Go
import app.slumber.ui.T
import app.slumber.ui.art.Coats
import app.slumber.ui.art.HairTones
import app.slumber.ui.art.Skins
import app.slumber.ui.comic.Cover
import app.slumber.ui.components.Chip
import app.slumber.ui.components.PrimaryButton
import app.slumber.ui.components.SectionTitle
import app.slumber.ui.components.TopBar
import app.slumber.ui.ctx
import app.slumber.ui.theme.Ink
import app.slumber.ui.theme.U
import app.slumber.ui.theme.UType
import kotlinx.coroutines.delay

/**
 * Someone from the cast, built once from a few choices so they're recognisable in every dream after.
 * [id] 0 is someone new; [label] is what they were called while nameless.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonScreen(id: Long, label: String, book: Book, go: Go) {
    val c = ctx()
    val existing = if (id != 0L) book.person(id) else null
    var name by rememberSaveable { mutableStateOf(existing?.name ?: label) }
    var look by remember { mutableStateOf(existing?.lookOf ?: Look(coat = (book.people.size * 3) % Look.COATS)) }
    var dismissing by remember { mutableStateOf(false) }

    // Someone already in the cast is kept as you change them.
    if (existing != null) LaunchedEffect(name, look) {
        delay(400)
        val n = name.trim()
        if (n.isNotEmpty()) c.container.write { db.dao().update(existing.copy(name = n, look = look.encode())) }
    }

    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding().imePadding()) {
        TopBar(if (existing == null) stringResource(R.string.person_new) else name, go::back) {
            if (existing != null && !existing.me) IconButton(onClick = { dismissing = true }) { Icon(Icons.Rounded.DeleteOutline, stringResource(R.string.person_dismiss), tint = U.c.muted) }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(Modifier.size(130.dp, 160.dp).clip(U.RMd).border(2.dp, Ink, U.RMd)) { portrait(look) }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    OutlinedTextField(name, { name = it.take(30) }, singleLine = true, placeholder = { Text(stringResource(R.string.person_name_hint)) }, textStyle = UType.Strong)
                    if (existing?.me == true) Text(stringResource(R.string.person_me), style = UType.Small, modifier = Modifier.padding(top = 4.dp))
                }
            }

            SectionTitle(stringResource(R.string.person_base))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Base.entries.forEach { b -> Chip(T.base(c, b), look.base == b, { look = look.copy(base = b) }) }
            }
            if (look.base == Base.HUMAN) {
                SectionTitle(stringResource(R.string.person_age))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Age.entries.forEach { a -> Chip(T.age(c, a), look.age == a, { look = look.copy(age = a) }) }
                }
                SectionTitle(stringResource(R.string.person_tall))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(R.string.tall_0, R.string.tall_1, R.string.tall_2).forEachIndexed { i, s -> Chip(stringResource(s), look.tall == i, { look = look.copy(tall = i) }) }
                }
                SectionTitle(stringResource(R.string.person_hair))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Hair.entries.forEach { h -> Chip(T.hair(c, h), look.hair == h, { look = look.copy(hair = h) }) }
                }
                Spacer(Modifier.height(10.dp))
                Swatches(HairTones, look.hairTone) { look = look.copy(hairTone = it) }
                SectionTitle(stringResource(R.string.person_skin))
                Swatches(Skins, look.skin) { look = look.copy(skin = it) }
            }
            SectionTitle(stringResource(if (look.base == Base.HUMAN) R.string.person_coat else R.string.person_colour))
            Swatches(Coats, look.coat) { look = look.copy(coat = it) }
            SectionTitle(stringResource(R.string.person_traits))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Trait.entries.forEach { t -> Chip(T.trait(c, t), look.has(t), { look = look.toggle(t) }) }
            }

            if (existing != null) {
                val ds = book.appearances(existing.id)
                SectionTitle(T.plural(c, R.plurals.appearances, ds.size))
                for (d in ds) {
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(U.RMd).background(U.c.surface).clickable { go.dream(d.id) }.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Cover(book.cover(d.id), Modifier.size(48.dp).border(1.5.dp, Ink))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(T.weekday(c, d.day(book.zone)), style = UType.Small)
                            Text(d.title.ifBlank { d.text }, style = UType.Body, maxLines = 1)
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
        if (existing == null) {
            PrimaryButton(
                stringResource(R.string.person_keep),
                {
                    val n = name.trim()
                    c.container.write { db.dao().adopt(Person(name = n, look = look.encode()), label) }
                    go.back()
                },
                Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(),
                enabled = name.isNotBlank(),
            )
        }
    }
    if (dismissing && existing != null) AlertDialog(
        onDismissRequest = { dismissing = false },
        title = { Text(stringResource(R.string.person_dismiss_title, existing.name)) },
        text = { Text(stringResource(R.string.person_dismiss_sub)) },
        confirmButton = {
            TextButton({
                dismissing = false
                c.container.write { db.dao().dismiss(existing.id) }
                go.back()
            }) { Text(stringResource(R.string.person_dismiss), color = U.c.red) }
        },
        dismissButton = { TextButton({ dismissing = false }) { Text(stringResource(R.string.cancel)) } },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Swatches(colors: List<Color>, selected: Int, onPick: (Int) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        colors.forEachIndexed { i, col ->
            Canvas(
                Modifier.size(36.dp).clip(CircleShape).border(if (i == selected) 3.dp else 1.dp, if (i == selected) U.c.red else Ink, CircleShape).clickable { onPick(i) },
            ) { drawCircle(col) }
        }
    }
}
