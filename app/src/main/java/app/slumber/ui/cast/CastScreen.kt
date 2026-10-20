package app.slumber.ui.cast

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.slumber.R
import app.slumber.domain.Base
import app.slumber.domain.Look
import app.slumber.domain.Mood
import app.slumber.ui.Book
import app.slumber.ui.Go
import app.slumber.ui.T
import app.slumber.ui.art.Pen
import app.slumber.ui.art.Poses
import app.slumber.ui.art.figure
import app.slumber.ui.art.height
import app.slumber.ui.components.UCard
import app.slumber.ui.ctx
import app.slumber.ui.theme.Ink
import app.slumber.ui.theme.Paper
import app.slumber.ui.theme.Tints
import app.slumber.ui.theme.U
import app.slumber.ui.theme.UType

/** Someone standing in a round frame of dream sky. */
fun DrawScope.portrait(look: Look) {
    val t = Tints.of(Mood.WONDER)
    val p = Pen(this, t)
    p.grad(t.sky2, Paper, 0f, 1f)
    p.rect(0f, 0.9f, 1f, 1f, t.ground.copy(alpha = 0.5f), line = false)
    val tall = look.height(0.78f).coerceAtMost(0.86f) * if (look.base == Base.HUMAN) 1f else 1.25f
    p.figure(look, Poses.stand, 0.5f, 0.93f, tall, 1, Mood.CALM)
}

/** The cast: the dreamer first, then everyone who keeps coming back. */
@Composable
fun CastTab(book: Book, go: Go) {
    val c = ctx()
    LazyVerticalGrid(
        GridCells.Fixed(3), Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(stringResource(R.string.cast_sub), style = UType.Muted, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
        }
        for ((label, ds) in book.returning) item(span = { GridItemSpan(maxLineSpan) }) {
            UCard(onClick = { go.person(0, label) }) {
                Text(stringResource(R.string.returning_title, label), style = UType.Strong)
                Text(T.plural(c, R.plurals.returning_sub, ds.size), style = UType.Muted)
            }
        }
        items(book.people, key = { it.id }) { p ->
            Column(Modifier.clip(U.RMd).clickable { go.person(p.id) }, horizontalAlignment = Alignment.CenterHorizontally) {
                Canvas(Modifier.fillMaxWidth().aspectRatio(0.8f).clip(U.RMd).border(2.dp, Ink, U.RMd)) { portrait(p.lookOf) }
                Spacer(Modifier.height(4.dp))
                Text(p.name, style = UType.Strong, textAlign = TextAlign.Center, maxLines = 2)
                Text(T.plural(c, R.plurals.appearances, book.appearances(p.id).size), style = UType.Small)
            }
        }
        item {
            Box(
                Modifier.fillMaxWidth().aspectRatio(0.8f).clip(U.RMd).background(U.c.surface2).clickable { go.person(0) },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Add, null, tint = U.c.muted)
                    Text(stringResource(R.string.cast_add), style = UType.Small, textAlign = TextAlign.Center)
                }
            }
        }
    }
}
