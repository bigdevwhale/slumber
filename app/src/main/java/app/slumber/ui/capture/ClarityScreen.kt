package app.slumber.ui.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.slumber.R
import app.slumber.container
import app.slumber.domain.Clarity
import app.slumber.ui.Book
import app.slumber.ui.Go
import app.slumber.ui.ctx
import app.slumber.ui.theme.Dawn
import app.slumber.ui.theme.Sans
import app.slumber.ui.theme.Serif

/**
 * One question after telling a dream, still in the dark: did you know it was a dream? And the signs that
 * were in it, the things to catch next time.
 */
@Composable
fun ClarityScreen(id: Long, book: Book, go: Go) {
    val c = ctx()
    val dream = book.dream(id)
    val signs = dream?.let(book::found).orEmpty()

    fun answer(cl: Clarity) {
        if (dream != null) c.container.write { db.dao().update(dream.copy(clarity = cl.ordinal)) }
        go.home()
    }

    Column(
        Modifier.fillMaxSize().background(Dawn.bg).statusBarsPadding().navigationBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(R.string.clarity_q), fontFamily = Serif, fontSize = 27.sp, lineHeight = 35.sp, color = Dawn.text)
        Spacer(Modifier.height(10.dp))
        Text(stringResource(R.string.clarity_sub), fontFamily = Sans, fontSize = 15.sp, color = Dawn.dim)
        Spacer(Modifier.height(28.dp))
        Option(stringResource(R.string.clarity_none)) { answer(Clarity.NONE) }
        Option(stringResource(R.string.clarity_glimmer)) { answer(Clarity.GLIMMER) }
        Option(stringResource(R.string.clarity_lucid)) { answer(Clarity.LUCID) }
        if (signs.isNotEmpty()) {
            Spacer(Modifier.height(28.dp))
            Text(stringResource(R.string.clarity_signs), fontFamily = Sans, fontSize = 14.sp, color = Dawn.dim)
            Spacer(Modifier.height(4.dp))
            Text(signs.joinToString(" · ") { it.label(book.ru) }, fontFamily = Serif, fontSize = 18.sp, color = Dawn.text)
        }
    }
}

@Composable
private fun Option(label: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Text(
        label, fontFamily = Sans, fontSize = 18.sp, color = Dawn.text,
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).clip(shape).background(Dawn.field).border(1.dp, Dawn.dim, shape)
            .clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 16.dp),
    )
}
