package app.slumber.ui.check

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.slumber.R
import app.slumber.container
import app.slumber.data.Check
import app.slumber.domain.Checks
import app.slumber.ui.Book
import app.slumber.ui.Go
import app.slumber.ui.components.PrimaryButton
import app.slumber.ui.components.TopBar
import app.slumber.ui.ctx
import app.slumber.ui.theme.Banner
import app.slumber.ui.theme.U
import app.slumber.ui.theme.UType

/** Short words to read twice: awake they stay put, in a dream they rarely do. */
private val WORDS_EN = listOf("BRASS LAMP 4", "SEVEN OWLS", "WINDOW 12", "OAK TABLE 9", "BLUE KEY 3", "NIGHT TRAIN 8")
private val WORDS_RU = listOf("ЛАТУННАЯ ЛАМПА 4", "СЕМЬ СОВ", "ОКНО 12", "ДУБОВЫЙ СТОЛ 9", "СИНИЙ КЛЮЧ 3", "НОЧНОЙ ПОЕЗД 8")

/**
 * "Are you dreaming?" A check to do with your body, a line to read twice, and tonight's sign, so the
 * habit of asking carries over into sleep.
 */
@Composable
fun CheckScreen(book: Book, go: Go) {
    val c = ctx()
    val seed = remember { System.currentTimeMillis() / 60_000 }
    val check = Checks.pick(seed).text(book.ru)
    val word = (if (book.ru) WORDS_RU else WORDS_EN)[(seed % WORDS_EN.size).toInt()]

    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding().navigationBarsPadding()) {
        TopBar("", go::back)
        Column(Modifier.weight(1f).padding(horizontal = 24.dp), verticalArrangement = Arrangement.Center) {
            Text(stringResource(R.string.check_title), style = UType.Hero)
            Spacer(Modifier.height(16.dp))
            Text(check, style = UType.Prompt)
            Spacer(Modifier.height(28.dp))
            Text(stringResource(R.string.check_read), style = UType.Muted)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().border(1.dp, U.c.line, U.RMd).background(U.c.surface, U.RMd).padding(vertical = 18.dp), contentAlignment = Alignment.Center) {
                Text(word, fontFamily = Banner, fontSize = 24.sp, color = U.c.lamp, textAlign = TextAlign.Center)
            }
            book.tonight?.let { s ->
                Spacer(Modifier.height(24.dp))
                Text(stringResource(R.string.check_sign, s.label(book.ru)), style = UType.Muted)
            }
        }
        PrimaryButton(
            stringResource(R.string.check_awake),
            {
                c.container.write { db.dao().insert(Check()) }
                go.back()
            },
            Modifier.fillMaxWidth().padding(16.dp),
        )
    }
}
