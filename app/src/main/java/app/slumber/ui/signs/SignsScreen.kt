package app.slumber.ui.signs

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.slumber.R
import app.slumber.container
import app.slumber.data.OwnSign
import app.slumber.ui.Book
import app.slumber.ui.Go
import app.slumber.ui.T
import app.slumber.ui.components.SectionTitle
import app.slumber.ui.components.SoftButton
import app.slumber.ui.components.TopBar
import app.slumber.ui.ctx
import app.slumber.ui.theme.U
import app.slumber.ui.theme.UType

/**
 * Your dream signs, found in the journal: what keeps happening in your dreams. One of them is tonight's,
 * for the ritual. Add your own in your own words.
 */
@Composable
fun SignsScreen(book: Book, go: Go) {
    val c = ctx()
    var phrase by rememberSaveable { mutableStateOf("") }
    val tonight = book.tonight?.key
    fun pick(key: String) = c.container.write { settings.update { it.copy(sign = key) } }

    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding().imePadding()) {
        TopBar(stringResource(R.string.signs), go::back)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 24.dp)) {
            item { Text(stringResource(R.string.signs_sub), style = UType.Sub, modifier = Modifier.padding(horizontal = 4.dp)) }
            if (book.yours.isEmpty()) item {
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.signs_empty), style = UType.Muted, modifier = Modifier.padding(horizontal = 4.dp))
            } else item { SectionTitle(stringResource(R.string.signs_yours)) }
            items(book.yours, key = { it.first.key }) { (s, n) ->
                val ownId = s.key.removePrefix("own:").toLongOrNull()?.takeIf { s.key.startsWith("own:") }
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(U.RMd).background(U.c.surface)
                        .border(1.dp, if (s.key == tonight) U.c.lamp.copy(alpha = 0.6f) else U.c.line, U.RMd)
                        .clickable { pick(s.key) }.padding(start = 4.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(s.key == tonight, { pick(s.key) }, colors = RadioButtonDefaults.colors(selectedColor = U.c.lamp, unselectedColor = U.c.faint))
                    Column(Modifier.weight(1f)) {
                        Text(s.label(book.ru), style = UType.Strong)
                        Text(T.plural(c, R.plurals.in_dreams, n), style = UType.Muted)
                    }
                    if (ownId != null) IconButton(onClick = {
                        c.container.write {
                            db.dao().deleteOwnSign(ownId)
                            if (settings.now().sign == s.key) settings.update { it.copy(sign = "") }
                        }
                    }) { Icon(Icons.Rounded.Close, stringResource(R.string.signs_remove), tint = U.c.faint) }
                }
            }
            item {
                SectionTitle(stringResource(R.string.signs_own))
                OutlinedTextField(phrase, { phrase = it.take(40) }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text(stringResource(R.string.signs_own_hint)) })
                Spacer(Modifier.height(8.dp))
                Row {
                    SoftButton(stringResource(R.string.signs_add), {
                        val p = phrase.trim()
                        if (p.isNotEmpty()) {
                            c.container.write { db.dao().insert(OwnSign(phrase = p)) }
                            phrase = ""
                        }
                    }, enabled = phrase.isNotBlank())
                    Spacer(Modifier.width(8.dp))
                }
            }
        }
    }
}
