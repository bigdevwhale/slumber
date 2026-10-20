package app.slumber.ui.comic

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.slumber.R
import app.slumber.data.Dream
import app.slumber.data.Panel
import app.slumber.domain.Layout
import app.slumber.domain.Look
import app.slumber.ui.T
import app.slumber.ui.art.Shot
import app.slumber.ui.art.panel
import app.slumber.ui.art.waking
import app.slumber.ui.ctx
import app.slumber.ui.theme.Banner
import app.slumber.ui.theme.Ink
import app.slumber.ui.theme.Paper
import app.slumber.ui.theme.U
import app.slumber.ui.theme.UType

/** One panel's picture. */
@Composable
fun PanelArt(shot: Shot, modifier: Modifier = Modifier, dim: Boolean = false) {
    Canvas(modifier) { panel(shot, dim) }
}

/**
 * A dream as a Sunday page: the banner, the panels in rows with the dreamer's own words under each, and
 * the waking panel at the foot. [onPanel]: a tap on a panel, by index.
 */
@Composable
fun ComicPage(
    dream: Dream,
    panels: List<Panel>,
    shot: (Panel) -> Shot,
    me: Look,
    modifier: Modifier = Modifier,
    dim: Boolean = false,
    onPanel: ((Int) -> Unit)? = null,
) {
    val c = ctx()
    Column(
        modifier
            .fillMaxWidth()
            .background(Paper)
            .border(2.dp, Ink)
            .padding(8.dp),
    ) {
        Masthead(T.date(c, dream.day()), dream.title, dream.lucid)
        Spacer(Modifier.height(8.dp))
        var i = 0
        val rows = Layout.rows(panels.size)
        for (n in rows) {
            val row = panels.subList(i, i + n)
            val start = i
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEachIndexed { k, p ->
                    Cell(
                        shot(p), p.caption, if (n == 1) 1.75f else 1f, dim,
                        Modifier.weight(1f).fillMaxHeight().then(if (onPanel != null) Modifier.clickable { onPanel(start + k) } else Modifier),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            i += n
        }
        // Always the same last panel: awake, in your own bed.
        Column(Modifier.fillMaxWidth()) {
            Canvas(Modifier.fillMaxWidth().aspectRatio(2.6f).alpha(if (dim) 0.5f else 1f)) { waking(me, dream.moodOf.dark) }
            Text(stringResource(R.string.woke), style = UType.Caption.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), modifier = Modifier.padding(top = 4.dp, start = 2.dp))
        }
    }
}

@Composable
private fun Cell(shot: Shot, caption: String, ratio: Float, dim: Boolean, modifier: Modifier) {
    Column(modifier) {
        PanelArt(shot, Modifier.fillMaxWidth().aspectRatio(ratio), dim)
        if (caption.isNotBlank()) {
            Text(caption, style = UType.Caption, color = if (dim) Ink.copy(alpha = 0.45f) else Ink, modifier = Modifier.padding(top = 4.dp, start = 2.dp, end = 2.dp))
        }
    }
}

/** The banner across the top of the page, red with cream lettering. */
@Composable
fun Masthead(date: String, title: String, lucid: Boolean) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(U.c.red)
            .border(2.dp, Ink)
            .drawBehind {
                // Thin cream rules above and below the lettering, as on the old mastheads.
                val y1 = size.height * 0.12f
                val y2 = size.height * 0.88f
                drawLine(Paper.copy(alpha = 0.7f), Offset(12f, y1), Offset(size.width - 12f, y1), 2f)
                drawLine(Paper.copy(alpha = 0.7f), Offset(12f, y2), Offset(size.width - 12f, y2), 2f)
            }
            .padding(vertical = 12.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (lucid) LucidStar()
            Text(
                stringResource(R.string.masthead).uppercase(),
                fontFamily = Banner, fontSize = 22.sp, letterSpacing = 0.08.em, color = Paper, textAlign = TextAlign.Center,
            )
            if (lucid) LucidStar()
        }
        if (title.isNotBlank()) Text(title, fontFamily = Banner, fontSize = 16.sp, color = Paper, textAlign = TextAlign.Center)
        Text(date, style = UType.Small, color = Paper.copy(alpha = 0.85f))
    }
}

@Composable
fun LucidStar(size: Float = 18f, color: Color = Color(0xFFFFD166)) {
    Canvas(Modifier.padding(horizontal = 6.dp).size(size.dp)) {
        val r = this.size.minDimension / 2
        val c = Offset(this.size.width / 2, this.size.height / 2)
        val p = androidx.compose.ui.graphics.Path()
        for (k in 0 until 10) {
            val a = Math.PI * k / 5 - Math.PI / 2
            val rr = if (k % 2 == 0) r else r * 0.45f
            val x = c.x + (rr * kotlin.math.cos(a)).toFloat()
            val y = c.y + (rr * kotlin.math.sin(a)).toFloat()
            if (k == 0) p.moveTo(x, y) else p.lineTo(x, y)
        }
        p.close()
        drawPath(p, color)
        drawPath(p, Ink, style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
    }
}

/** A small cover for lists: the first panel in a frame. */
@Composable
fun Cover(shot: Shot?, modifier: Modifier = Modifier) {
    Box(modifier.background(Paper)) {
        if (shot != null) PanelArt(shot, Modifier.fillMaxWidth().aspectRatio(1f))
    }
}
