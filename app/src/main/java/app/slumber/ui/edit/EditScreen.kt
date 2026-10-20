package app.slumber.ui.edit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.slumber.R
import app.slumber.container
import app.slumber.data.Panel
import app.slumber.data.Spot
import app.slumber.domain.Act
import app.slumber.domain.Doodle
import app.slumber.domain.Fig
import app.slumber.domain.Guess
import app.slumber.domain.Kind
import app.slumber.domain.Mood
import app.slumber.domain.Scene
import app.slumber.domain.Split
import app.slumber.ui.Book
import app.slumber.ui.Go
import app.slumber.ui.T
import app.slumber.ui.art.medallion
import app.slumber.ui.art.panel
import app.slumber.ui.atlas.placeFor
import app.slumber.ui.components.Chip
import app.slumber.ui.components.SoftButton
import app.slumber.ui.components.TopBar
import app.slumber.ui.ctx
import app.slumber.ui.theme.Ink
import app.slumber.ui.theme.Tints
import app.slumber.ui.theme.U
import app.slumber.ui.theme.UType
import kotlinx.coroutines.delay

private enum class Layer { WHERE, WHO, WHAT, FEEL, WORDS, DRAW }

/**
 * Building the page: pick a panel, then its four layers: where, who, what happens, how it felt; its
 * words; and, when nothing in the lists will do, a drawing with a finger. Every change is kept at once.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditScreen(id: Long, alt: Int, book: Book, go: Go) {
    val c = ctx()
    val dream = book.dream(id)
    val source = book.panels(id, alt)
    val list = remember { mutableStateListOf<Panel>() }
    var loaded by remember { mutableStateOf(false) }
    var sel by rememberSaveable { mutableIntStateOf(0) }
    var layer by rememberSaveable { mutableStateOf(Layer.WHERE) }
    var version by remember { mutableIntStateOf(0) }
    var naming by remember { mutableStateOf(false) }
    var labelling by remember { mutableStateOf<Int?>(null) }
    var resplit by remember { mutableStateOf(false) }

    // A new ending's panels are copied in just before this opens: wait for them.
    LaunchedEffect(source.size) {
        if (!loaded && source.isNotEmpty()) {
            list.addAll(source)
            loaded = true
        }
    }
    LaunchedEffect(Unit) {
        delay(1500)
        if (!loaded) loaded = true
    }
    LaunchedEffect(version) {
        if (version == 0) return@LaunchedEffect
        delay(350)
        val keep = list.toList()
        c.container.write { db.dao().replacePanels(id, alt, keep) }
    }

    fun change(p: Panel) {
        if (sel !in list.indices) return
        list[sel] = p
        version++
    }

    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding().imePadding()) {
        TopBar(stringResource(if (alt == 1) R.string.edit_title_new else R.string.edit_title), go::back)
        if (dream == null) return@Column
        if (alt == 1) Text(stringResource(R.string.edit_new_hint), style = UType.Muted, modifier = Modifier.padding(horizontal = 18.dp))

        // The strip of panels.
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            list.forEachIndexed { i, p ->
                Box(
                    Modifier.size(60.dp).border(if (i == sel) 3.dp else 1.dp, if (i == sel) U.c.red else Ink).clickable { sel = i },
                ) {
                    Canvas(Modifier.fillMaxSize()) { panel(book.shot(p)) }
                    Text("${i + 1}", style = UType.Small, color = Ink, modifier = Modifier.align(Alignment.TopStart).background(U.c.surface).padding(horizontal = 3.dp))
                }
            }
            if (list.size < Split.MAX + 2) {
                Box(
                    Modifier.size(60.dp).clip(U.RSm).background(U.c.surface2).clickable {
                        val base = list.getOrNull(sel) ?: Panel(dreamId = id, idx = 0, alt = alt, figs = book.me?.let { Fig.encode(listOf(Fig.Cast(it.id))) }.orEmpty())
                        list.add((sel + 1).coerceAtMost(list.size), base.copy(id = 0, caption = "", doodle = ""))
                        sel = (sel + 1).coerceAtMost(list.size - 1)
                        version++
                    },
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Rounded.Add, stringResource(R.string.edit_add), tint = U.c.muted) }
            }
        }

        val p = list.getOrNull(sel)
        if (p == null) {
            if (loaded) Text(stringResource(R.string.edit_empty), style = UType.Sub, modifier = Modifier.padding(20.dp))
            return@Column
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState(), enabled = layer != Layer.DRAW).padding(horizontal = 16.dp)) {
            // The panel, big. In drawing mode a finger draws on it.
            val strokes = remember(sel, p.doodle) { Doodle.decode(p.doodle).toMutableList() }
            var live by remember(sel) { mutableStateOf<List<Doodle.P>>(emptyList()) }
            Box(Modifier.fillMaxWidth().aspectRatio(1.3f)) {
                Canvas(
                    Modifier.fillMaxSize().then(
                        if (layer == Layer.DRAW) Modifier.pointerInput(sel) {
                            detectDragGestures(
                                onDragStart = { o -> live = listOf(Doodle.P(o.x / size.width, o.y / size.height)) },
                                onDrag = { ch, _ -> live = live + Doodle.P((ch.position.x / size.width).coerceIn(0f, 1f), (ch.position.y / size.height).coerceIn(0f, 1f)) },
                                onDragEnd = {
                                    if (live.size > 1) change(p.copy(doodle = Doodle.encode(strokes + listOf(live))))
                                    live = emptyList()
                                },
                            )
                        } else Modifier
                    ),
                ) {
                    val shot = book.shot(p)
                    panel(if (live.isEmpty()) shot else shot.copy(doodle = Doodle.encode(strokes + listOf(live))))
                }
            }
            Spacer(Modifier.height(6.dp))
            if (p.caption.isNotBlank()) Text(p.caption, style = UType.Caption, color = U.c.ink)

            // Which layer to change.
            Spacer(Modifier.height(10.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val names = listOf(R.string.l_where, R.string.l_who, R.string.l_what, R.string.l_feel, R.string.l_words, R.string.l_draw)
                Layer.entries.forEach { l -> Chip(stringResource(names[l.ordinal]), layer == l, { layer = l }) }
            }
            Spacer(Modifier.height(12.dp))

            when (layer) {
                Layer.WHERE -> {
                    if (book.spots.isNotEmpty()) {
                        Text(stringResource(R.string.where_atlas), style = UType.Small)
                        Spacer(Modifier.height(6.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            book.spots.forEach { s ->
                                Chip(s.name, p.spotId == s.id, { change(p.copy(spotId = if (p.spotId == s.id) 0 else s.id, scene = s.scene)) }, U.c.night)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                    SoftButton(
                        stringResource(if (p.spotId != 0L) R.string.where_rename else R.string.where_name),
                        { naming = true }, Modifier.fillMaxWidth(), color = U.c.night, soft = U.c.nightSoft, icon = Icons.Rounded.Place,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.where_kinds), style = UType.Small)
                    Spacer(Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Scene.entries.forEach { s ->
                            Column(Modifier.width(74.dp).clip(U.RSm).clickable { change(p.copy(scene = s.name, spotId = 0)) }, horizontalAlignment = Alignment.CenterHorizontally) {
                                Canvas(Modifier.size(62.dp).then(if (p.sceneOf == s && p.spotId == 0L) Modifier.border(3.dp, U.c.red, CircleShape) else Modifier)) {
                                    medallion(s, p.moodOf, s.ordinal)
                                }
                                Text(T.scene(c, s), style = UType.Small, color = if (p.sceneOf == s) U.c.red else U.c.muted, textAlign = TextAlign.Center, maxLines = 2)
                            }
                        }
                    }
                }
                Layer.WHO -> {
                    val figs = p.figsOf
                    Text(stringResource(R.string.who_here), style = UType.Small)
                    Spacer(Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        figs.forEachIndexed { i, f ->
                            val name = when (f) {
                                is Fig.Cast -> book.person(f.id)?.name ?: T.kind(c, Kind.SOMEONE)
                                is Fig.Extra -> f.label.ifBlank { T.kind(c, f.kind) }
                            }
                            Row(
                                Modifier.clip(U.RSm).background(U.c.redSoft).clickable { if (f is Fig.Extra) labelling = i }.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(name, style = UType.Body, color = U.c.red)
                                IconButton(onClick = { change(p.copy(figs = Fig.encode(figs.filterIndexed { k, _ -> k != i }))) }, modifier = Modifier.size(30.dp)) {
                                    Icon(Icons.Rounded.Close, stringResource(R.string.who_remove), tint = U.c.red, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    if (figs.any { it is Fig.Extra }) Text(stringResource(R.string.who_label_hint), style = UType.Small, modifier = Modifier.padding(top = 6.dp))
                    if (figs.size < 3) {
                        Spacer(Modifier.height(14.dp))
                        Text(stringResource(R.string.who_cast), style = UType.Small)
                        Spacer(Modifier.height(6.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            book.people.filter { pp -> Fig.Cast(pp.id) !in figs }.forEach { pp ->
                                Chip(pp.name, false, { change(p.copy(figs = Fig.encode(figs + Fig.Cast(pp.id)))) }, U.c.night)
                            }
                        }
                        SoftButton(stringResource(R.string.who_new), { go.person(0) }, Modifier.padding(top = 8.dp), color = U.c.night, soft = U.c.nightSoft, icon = Icons.Rounded.PersonAdd)
                        Spacer(Modifier.height(14.dp))
                        Text(stringResource(R.string.who_extras), style = UType.Small)
                        Spacer(Modifier.height(6.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Kind.entries.forEach { k -> Chip(T.kind(c, k), false, { change(p.copy(figs = Fig.encode(figs + Fig.Extra(k)))) }) }
                        }
                    }
                }
                Layer.WHAT -> FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Act.entries.forEach { a -> Chip(T.act(c, a), p.actOf == a, { change(p.copy(act = a.name)) }) }
                }
                Layer.FEEL -> FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Mood.entries.forEach { m ->
                        val t = Tints.of(m)
                        Row(
                            Modifier.clip(U.RSm).border(if (p.moodOf == m) 2.5.dp else 1.dp, if (p.moodOf == m) U.c.red else U.c.line, U.RSm)
                                .clickable { change(p.copy(mood = m.name)) }.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Canvas(Modifier.size(22.dp)) {
                                drawCircle(t.sky)
                                drawCircle(t.accent, size.minDimension / 4)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(T.mood(c, m), style = UType.Body)
                        }
                    }
                }
                Layer.WORDS -> OutlinedTextField(
                    p.caption, { change(p.copy(caption = it)) }, Modifier.fillMaxWidth(),
                    textStyle = UType.Told, minLines = 3, placeholder = { Text(stringResource(R.string.words_hint)) },
                )
                Layer.DRAW -> {
                    Text(stringResource(R.string.draw_hint), style = UType.Muted)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SoftButton(stringResource(R.string.draw_undo), { if (strokes.isNotEmpty()) change(p.copy(doodle = Doodle.encode(strokes.dropLast(1)))) }, icon = Icons.AutoMirrored.Rounded.Undo, enabled = strokes.isNotEmpty())
                        SoftButton(stringResource(R.string.draw_clear), { change(p.copy(doodle = "")) }, enabled = strokes.isNotEmpty())
                    }
                }
            }

            // Order and removal, and starting over from the words.
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (sel > 0) { val x = list.removeAt(sel); list.add(sel - 1, x); sel--; version++ } }, enabled = sel > 0) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.edit_left), tint = U.c.muted)
                }
                IconButton(onClick = { if (sel < list.size - 1) { val x = list.removeAt(sel); list.add(sel + 1, x); sel++; version++ } }, enabled = sel < list.size - 1) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, stringResource(R.string.edit_right), tint = U.c.muted)
                }
                IconButton(onClick = { if (list.size > 1) { list.removeAt(sel); sel = sel.coerceAtMost(list.size - 1); version++ } }, enabled = list.size > 1) {
                    Icon(Icons.Rounded.DeleteOutline, stringResource(R.string.edit_remove), tint = U.c.muted)
                }
                Spacer(Modifier.weight(1f))
                TextButton({ resplit = true }) { Text(stringResource(R.string.edit_resplit), color = U.c.muted) }
            }
            Spacer(Modifier.height(24.dp))
            Spacer(Modifier.navigationBarsPadding())
        }

        if (naming) {
            val current = book.spot(p.spotId)
            var name by remember { mutableStateOf(current?.name.orEmpty()) }
            AlertDialog(
                onDismissRequest = { naming = false },
                title = { Text(stringResource(R.string.where_name_title)) },
                text = {
                    Column {
                        Text(stringResource(R.string.where_name_sub), style = UType.Muted)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(name, { name = it.take(40) }, singleLine = true, placeholder = { Text(stringResource(R.string.where_name_hint)) })
                    }
                },
                confirmButton = {
                    TextButton({
                        val n = name.trim()
                        naming = false
                        if (n.isEmpty()) return@TextButton
                        val panelAt = sel
                        c.container.write {
                            val dao = db.dao()
                            val spotId = if (current != null) {
                                dao.update(current.copy(name = n, scene = p.scene)); current.id
                            } else {
                                val (x, y) = placeFor(dao.allSpots())
                                dao.insert(Spot(name = n, scene = p.scene, x = x, y = y))
                            }
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                if (panelAt in list.indices) { list[panelAt] = list[panelAt].copy(spotId = spotId); version++ }
                            }
                        }
                    }) { Text(stringResource(R.string.ok)) }
                },
                dismissButton = { TextButton({ naming = false }) { Text(stringResource(R.string.cancel)) } },
            )
        }
        labelling?.let { i ->
            val f = p.figsOf.getOrNull(i) as? Fig.Extra
            if (f == null) { labelling = null; return@let }
            var label by remember(i) { mutableStateOf(f.label) }
            AlertDialog(
                onDismissRequest = { labelling = null },
                title = { Text(T.kind(c, f.kind)) },
                text = { OutlinedTextField(label, { label = it.take(30) }, singleLine = true, placeholder = { Text(stringResource(R.string.who_label_placeholder)) }) },
                confirmButton = {
                    TextButton({
                        change(p.copy(figs = Fig.encode(p.figsOf.mapIndexed { k, x -> if (k == i) f.copy(label = label.trim()) else x })))
                        labelling = null
                    }) { Text(stringResource(R.string.ok)) }
                },
                dismissButton = { TextButton({ labelling = null }) { Text(stringResource(R.string.cancel)) } },
            )
        }
        if (resplit) AlertDialog(
            onDismissRequest = { resplit = false },
            title = { Text(stringResource(R.string.edit_resplit)) },
            text = { Text(stringResource(R.string.edit_resplit_sub)) },
            confirmButton = {
                TextButton({
                    resplit = false
                    val me = book.me?.id ?: 0L
                    val fresh = Guess.panels(dream.text, me, book.named, book.places).mapIndexed { i, d -> Panel.of(id, i, d, alt) }
                    list.clear()
                    list.addAll(fresh)
                    sel = 0
                    version++
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton({ resplit = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}
