package app.slumber.ui.dream

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DriveFileRenameOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.slumber.R
import app.slumber.container
import app.slumber.ui.Book
import app.slumber.ui.Go
import app.slumber.ui.T
import app.slumber.ui.comic.ComicPage
import app.slumber.ui.components.SectionTitle
import app.slumber.ui.components.Segmented
import app.slumber.ui.components.SoftButton
import app.slumber.ui.components.Toggle
import app.slumber.ui.components.TopBar
import app.slumber.ui.components.UCard
import app.slumber.ui.ctx
import app.slumber.ui.theme.U
import app.slumber.ui.theme.UType

/**
 * A dream's page. With a new ending drawn, it shows that one, and the dream as it came is a tap away,
 * faded, like the back of the sheet.
 */
@Composable
fun DreamScreen(id: Long, book: Book, go: Go) {
    val c = ctx()
    val dream = book.dream(id)
    if (dream == null) {
        Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding()) { TopBar("", go::back) }
        return
    }
    val hasAlt = book.hasAlt(id)
    var side by rememberSaveable { mutableIntStateOf(if (hasAlt) 1 else 0) }
    val alt = if (hasAlt) side else 0
    val panels = book.panels(id, alt)
    var naming by remember { mutableStateOf(false) }
    var forgetting by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding()) {
        TopBar(T.date(c, dream.day(book.zone)), go::back) {
            IconButton(onClick = { naming = true }) { Icon(Icons.Rounded.DriveFileRenameOutline, stringResource(R.string.dream_name), tint = U.c.muted) }
            IconButton(onClick = { forgetting = true }) { Icon(Icons.Rounded.DeleteOutline, stringResource(R.string.dream_forget), tint = U.c.muted) }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 12.dp).navigationBarsPadding()) {
            if (hasAlt) {
                Segmented(listOf(1, 0), side, { stringResource(if (it == 1) R.string.side_new else R.string.side_old) }, { side = it })
                Spacer(Modifier.height(10.dp))
            }
            ComicPage(dream, panels, book::shot, book.meLook, dim = hasAlt && side == 0, onPanel = { go.edit(id, alt) })
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SoftButton(stringResource(R.string.dream_edit), { go.edit(id, alt) }, Modifier.weight(1f), icon = Icons.Rounded.Edit)
                SoftButton(
                    stringResource(if (hasAlt) R.string.dream_rewrite_again else R.string.dream_rewrite),
                    {
                        c.container.write {
                            val dao = db.dao()
                            if (!hasAlt) dao.replacePanels(id, 1, dao.panelsOf(id, 0))
                            dao.update(dream.copy(rewritten = System.currentTimeMillis()))
                        }
                        go.edit(id, 1)
                    },
                    Modifier.weight(1f),
                    color = U.c.night, soft = U.c.nightSoft, icon = Icons.Rounded.AutoFixHigh,
                )
            }
            if (dream.moodOf.dark && !hasAlt) {
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.dream_rewrite_why), style = UType.Muted, modifier = Modifier.padding(horizontal = 4.dp))
            }
            Spacer(Modifier.height(8.dp))
            UCard {
                Toggle(stringResource(R.string.lucid), stringResource(R.string.lucid_sub), dream.lucid) { v ->
                    c.container.write { db.dao().update(dream.copy(lucid = v)) }
                }
            }
            SectionTitle(stringResource(R.string.dream_told))
            Text(dream.text, style = UType.Told, modifier = Modifier.padding(horizontal = 4.dp))
            Spacer(Modifier.height(32.dp))
        }
    }

    if (naming) {
        var title by remember { mutableStateOf(dream.title) }
        AlertDialog(
            onDismissRequest = { naming = false },
            title = { Text(stringResource(R.string.dream_name)) },
            text = { OutlinedTextField(title, { title = it.take(60) }, singleLine = true, placeholder = { Text(stringResource(R.string.dream_name_hint)) }) },
            confirmButton = {
                TextButton({
                    c.container.write { db.dao().update(dream.copy(title = title.trim())) }
                    naming = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton({ naming = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
    if (forgetting) {
        AlertDialog(
            onDismissRequest = { forgetting = false },
            title = { Text(stringResource(R.string.dream_forget_title)) },
            text = { Text(stringResource(R.string.dream_forget_sub)) },
            confirmButton = {
                TextButton({
                    forgetting = false
                    c.container.write { db.dao().forget(id) }
                    go.back()
                }) { Text(stringResource(R.string.dream_forget), color = U.c.red) }
            },
            dismissButton = { TextButton({ forgetting = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}
