package app.slumber.ui.atlas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.slumber.R
import app.slumber.data.Spot
import app.slumber.domain.Bed
import app.slumber.ui.Book
import app.slumber.ui.Go
import app.slumber.ui.T
import app.slumber.ui.art.medallion
import app.slumber.ui.art.walkingBed
import app.slumber.ui.components.SectionTitle
import app.slumber.ui.components.UCard
import app.slumber.ui.ctx
import app.slumber.ui.theme.Ink
import app.slumber.ui.theme.Paper
import app.slumber.ui.theme.U
import app.slumber.ui.theme.UType
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** Where a new place goes on the atlas: out along a sunflower spiral, so places never pile up. */
fun placeFor(spots: List<Spot>): Pair<Float, Float> {
    val n = spots.size
    if (n == 0) return 0f to 0f
    val a = n * 2.39996f
    val r = 0.62f * sqrt(n.toFloat())
    return (cos(a) * r) to (sin(a) * r)
}

/**
 * The atlas of your own dreamland: the places you named, as medallions, and doors where a dream walked
 * from one into another. The bed stands where you dreamt last; fog hides the edges until it walks further.
 */
@Composable
fun AtlasTab(book: Book, go: Go) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 24.dp)) {
        if (book.spots.isEmpty()) {
            item {
                UCard {
                    Text(stringResource(R.string.atlas_empty_title), style = UType.Title)
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(R.string.atlas_empty_sub), style = UType.Sub)
                }
            }
            return@LazyColumn
        }
        item { AtlasMap(book, go, Modifier.fillMaxWidth().height(380.dp)) }
        item {
            Text(stringResource(R.string.atlas_hint), style = UType.Small, modifier = Modifier.padding(top = 6.dp, start = 4.dp))
            SectionTitle(stringResource(R.string.atlas_places))
        }
        items(book.spots, key = { it.id }) { s -> SpotRow(s, book, go) }
    }
}

@Composable
private fun SpotRow(s: Spot, book: Book, go: Go) {
    val c = ctx()
    val n = book.dreamsAt(s.id).size
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(U.RMd).background(U.c.surface).border(1.dp, U.c.line, U.RMd).clickable { go.spot(s.id) }.padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(Modifier.size(52.dp)) { medallion(s.sceneOf, book.moodAt(s.id), s.id.toInt()) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(s.name, style = UType.Strong)
            Text(T.scene(c, s.sceneOf) + " · " + T.plural(c, R.plurals.dreams_here, n), style = UType.Muted)
        }
    }
}

@Composable
fun AtlasMap(book: Book, go: Go, modifier: Modifier = Modifier) {
    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current
    val reach = Bed.reach(book.mornings)
    BoxWithConstraints(
        modifier.clip(U.RLg).background(Paper).border(2.dp, Ink, U.RLg)
            .pointerInput(Unit) {
                detectTransformGestures { _, p, z, _ ->
                    zoom = (zoom * z).coerceIn(0.5f, 2.5f)
                    pan += p
                }
            },
    ) {
        val wpx = with(density) { maxWidth.toPx() }
        val hpx = with(density) { maxHeight.toPx() }
        // A fixed scale, so places keep their distance however many there are; a big atlas is panned.
        val unit = with(density) { 125.dp.toPx() }
        fun at(s: Spot) = Offset(wpx / 2 + s.x * unit, hpx / 2 + s.y * unit)
        val medal = with(density) { 60.dp.toPx() }

        Box(Modifier.fillMaxSize().graphicsLayer { scaleX = zoom; scaleY = zoom; translationX = pan.x; translationY = pan.y }) {
            Canvas(Modifier.fillMaxSize()) {
                // An old map's ground: faint meridians, a compass rose.
                for (k in -6..6) {
                    drawLine(Ink.copy(alpha = 0.06f), Offset(size.width / 2 + k * unit * 0.5f, 0f), Offset(size.width / 2 + k * unit * 0.5f, size.height), 1f)
                    drawLine(Ink.copy(alpha = 0.06f), Offset(0f, size.height / 2 + k * unit * 0.5f), Offset(size.width, size.height / 2 + k * unit * 0.5f), 1f)
                }
                val rose = Offset(size.width - 44f, 44f)
                for (k in 0 until 8) {
                    val a = Math.PI * k / 4
                    val r = if (k % 2 == 0) 30f else 16f
                    drawLine(Ink.copy(alpha = 0.5f), rose, rose + Offset((cos(a) * r).toFloat(), (sin(a) * r).toFloat()), 2f, StrokeCap.Round)
                }
                // Doors: dashed paths, heavier the more dreams walked them.
                for ((pair, n) in book.doors) {
                    val a = book.spot(pair.first) ?: continue
                    val b = book.spot(pair.second) ?: continue
                    drawLine(
                        Color(0xFFB8372A).copy(alpha = 0.8f), at(a), at(b), (2.5f + n * 1.5f).coerceAtMost(9f),
                        StrokeCap.Round, PathEffect.dashPathEffect(floatArrayOf(14f, 10f)),
                    )
                }
            }
            for (s in book.spots) {
                val p = at(s)
                Column(
                    Modifier.offset { IntOffset((p.x - medal).roundToInt(), (p.y - medal / 2).roundToInt()) }.width(with(density) { (medal * 2).toDp() }).clickable { go.spot(s.id) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Canvas(Modifier.size(60.dp)) { medallion(s.sceneOf, book.moodAt(s.id), s.id.toInt()) }
                    Text(s.name, style = UType.Small, color = Ink, textAlign = TextAlign.Center, maxLines = 2, modifier = Modifier.background(Paper.copy(alpha = 0.8f)).padding(horizontal = 4.dp))
                }
            }
            book.bedAt?.let(book::spot)?.let { s ->
                val p = at(s)
                val bedH = with(density) { 40.dp.toPx() }
                Canvas(Modifier.offset { IntOffset((p.x + medal * 0.15f).roundToInt(), (p.y - medal * 0.5f - bedH * 0.9f).roundToInt()) }.size(48.dp, 40.dp)) {
                    walkingBed(book.stage, 0.25f)
                }
            }
        }
        // The fog: the bed hasn't walked out that far yet.
        Canvas(Modifier.fillMaxSize()) {
            val r = maxOf(size.width, size.height) * (0.45f + reach * 0.6f)
            drawRect(Brush.radialGradient(listOf(Color.Transparent, Color.Transparent, Paper.copy(alpha = 0.92f)), Offset(size.width / 2, size.height / 2), r))
        }
    }
}
