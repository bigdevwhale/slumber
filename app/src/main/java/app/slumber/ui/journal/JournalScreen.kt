package app.slumber.ui.journal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.slumber.R
import app.slumber.container
import app.slumber.data.Dream
import app.slumber.domain.Clarity
import app.slumber.ui.Book
import app.slumber.ui.Go
import app.slumber.ui.T
import app.slumber.ui.components.SectionTitle
import app.slumber.ui.components.Segmented
import app.slumber.ui.components.TopBar
import app.slumber.ui.ctx
import app.slumber.ui.theme.U
import app.slumber.ui.theme.UType

/** A brass ring for a lucid dream, a thin one for a glimmer, nothing for an ordinary dream. */
@Composable
fun ClarityMark(c: Clarity, gold: Color = U.c.gold) {
    if (c == Clarity.NONE) return
    Canvas(Modifier.size(18.dp)) {
        val w = if (c == Clarity.LUCID) 4f else 2f
        drawCircle(gold.copy(alpha = if (c == Clarity.LUCID) 1f else 0.6f), size.minDimension / 2 - w, style = Stroke(w))
    }
}

/** Every dream, newest first, by month. */
@Composable
fun JournalScreen(book: Book, go: Go) {
    val c = ctx()
    val months = remember(book) { book.dreams.groupBy { it.day(book.zone).withDayOfMonth(1) } }
    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding()) {
        TopBar(stringResource(R.string.journal), go::back)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 24.dp)) {
            if (book.dreams.isEmpty()) item {
                Text(stringResource(R.string.journal_empty), style = UType.Sub, modifier = Modifier.padding(4.dp))
            }
            for ((m, ds) in months) {
                item(key = "m$m") { SectionTitle(T.month(c, m)) }
                items(ds, key = { it.id }) { d -> DreamRow(d, book, go) }
            }
        }
    }
}

@Composable
private fun DreamRow(d: Dream, book: Book, go: Go) {
    val c = ctx()
    Column(
        Modifier.fillMaxWidth().padding(vertical = 5.dp).clip(U.RMd).background(U.c.surface).border(1.dp, U.c.line, U.RMd)
            .clickable { go.dream(d.id) }.padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(T.weekday(c, d.day(book.zone)), style = UType.Small, modifier = Modifier.weight(1f))
            ClarityMark(d.clarityOf)
        }
        Spacer(Modifier.height(4.dp))
        Text(d.text, style = UType.Told.copy(fontSize = 16.5.sp), maxLines = 3, overflow = TextOverflow.Ellipsis)
        val signs = book.found(d)
        if (signs.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(signs.joinToString(" · ") { it.label(book.ru) }, style = UType.Small, color = U.c.lamp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** One dream: the words, the signs in it, how clear it was. */
@Composable
fun DreamScreen(id: Long, book: Book, go: Go) {
    val c = ctx()
    val d = book.dream(id)
    var forgetting by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding()) {
        TopBar(d?.let { T.weekday(c, it.day(book.zone)) }.orEmpty(), go::back) {
            if (d != null) IconButton(onClick = { forgetting = true }) { Icon(Icons.Rounded.DeleteOutline, stringResource(R.string.forget), tint = U.c.muted) }
        }
        if (d == null) return@Column
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).navigationBarsPadding()) {
            Text(d.text, style = UType.Told)
            val signs = book.found(d)
            if (signs.isNotEmpty()) {
                SectionTitle(stringResource(R.string.dream_signs))
                Text(signs.joinToString(" · ") { it.label(book.ru) }, style = UType.Body, color = U.c.lamp)
            }
            SectionTitle(stringResource(R.string.dream_clarity))
            Segmented(
                Clarity.entries, d.clarityOf,
                { stringResource(when (it) { Clarity.NONE -> R.string.cl_none; Clarity.GLIMMER -> R.string.cl_glimmer; Clarity.LUCID -> R.string.cl_lucid }) },
                { cl -> c.container.write { db.dao().update(d.copy(clarity = cl.ordinal)) } },
            )
            Spacer(Modifier.height(32.dp))
        }
    }
    if (forgetting && d != null) AlertDialog(
        onDismissRequest = { forgetting = false },
        title = { Text(stringResource(R.string.forget_title)) },
        text = { Text(stringResource(R.string.forget_sub)) },
        confirmButton = {
            TextButton({
                forgetting = false
                c.container.write { db.dao().deleteDream(d.id) }
                go.back()
            }) { Text(stringResource(R.string.forget), color = U.c.lamp) }
        },
        dismissButton = { TextButton({ forgetting = false }) { Text(stringResource(R.string.cancel)) } },
        containerColor = U.c.surface,
    )
}
