package app.slumber.ui.capture

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.slumber.R
import app.slumber.container
import app.slumber.data.Dream
import app.slumber.domain.Guess
import app.slumber.domain.Mood
import app.slumber.ui.Book
import app.slumber.ui.Go
import app.slumber.ui.T
import app.slumber.ui.art.Art
import app.slumber.ui.components.rememberNow
import app.slumber.ui.ctx
import app.slumber.ui.theme.Dawn
import app.slumber.ui.theme.Sans
import app.slumber.ui.theme.Serif
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Words start paling after this long without a keystroke, and are faintest this much later. */
private const val FADE_AFTER_MS = 15_000L
private const val FADE_OVER_MS = 45_000L

/**
 * Telling a dream, still half asleep: nearly black, dim red letters, the screen turned right down. Type
 * or speak; fragments are fine. Pausing too long and the words begin to pale, the way the dream itself
 * does. They never actually go, and one keystroke brings them back.
 */
@Composable
fun CaptureScreen(book: Book, go: Go) {
    val c = ctx()
    val activity = c as? Activity
    var text by rememberSaveable { mutableStateOf(book.settings.draft) }
    var edited by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val now = rememberNow(1000)
    val scope = rememberCoroutineScope()
    val focus = remember { FocusRequester() }

    // The screen dims for as long as this is open.
    DisposableEffect(activity) {
        val win = activity?.window
        val before = win?.attributes?.screenBrightness
        win?.let { it.attributes = it.attributes.apply { screenBrightness = 0.05f } }
        win?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            win?.let { it.attributes = it.attributes.apply { screenBrightness = before ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE } }
            win?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    // Kept as it's told, so nothing is lost if the phone is put down.
    LaunchedEffect(text) {
        delay(600)
        c.container.settings.update { it.copy(draft = text) }
    }

    val idle = now - edited
    val fade = when {
        Art.still || !book.settings.fade || text.isEmpty() -> 1f
        idle < FADE_AFTER_MS -> 1f
        else -> (1f - (idle - FADE_AFTER_MS).toFloat() / FADE_OVER_MS * 0.72f).coerceAtLeast(0.28f)
    }

    val listen = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        val said = r.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!said.isNullOrBlank()) {
            text = if (text.isBlank()) said.replaceFirstChar { it.uppercase() } else text.trimEnd() + " " + said
            edited = System.currentTimeMillis()
        }
    }
    val speech = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (book.ru) "ru-RU" else "en-US")
            .putExtra(RecognizerIntent.EXTRA_PROMPT, c.getString(R.string.capture_speak))
    }
    val canSpeak = remember { speech.resolveActivity(c.packageManager) != null }

    fun save() {
        val told = text.trim()
        if (told.isEmpty()) return
        val container = c.container
        scope.launch {
            val id = withContext(Dispatchers.IO) {
                val me = container.db.dao().me()?.id ?: 0L
                val drafts = Guess.panels(told, me, book.named, book.places)
                val mood = drafts.groupingBy { it.mood }.eachCount().maxByOrNull { it.value }?.key ?: Mood.WONDER
                val id = container.db.dao().tell(Dream(text = told, mood = mood.name), drafts)
                container.settings.update { it.copy(draft = "") }
                id
            }
            go.dream(id, fresh = true)
        }
    }

    Column(Modifier.fillMaxSize().background(Dawn.bg).statusBarsPadding().navigationBarsPadding().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(T.weekday(c, book.today), fontFamily = Sans, fontSize = 14.sp, color = Dawn.dim, modifier = Modifier.weight(1f))
            IconButton(onClick = go::back) { Icon(Icons.Rounded.Close, stringResource(R.string.cancel), tint = Dawn.dim) }
        }
        Box(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 12.dp)) {
            if (text.isEmpty()) Text(stringResource(R.string.capture_hint), fontFamily = Serif, fontSize = 24.sp, lineHeight = 32.sp, color = Dawn.dim)
            BasicTextField(
                value = text,
                onValueChange = { text = it; edited = System.currentTimeMillis() },
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Serif, fontSize = 24.sp, lineHeight = 32.sp, color = Dawn.text.copy(alpha = fade)),
                cursorBrush = SolidColor(Dawn.text),
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
            )
        }
        if (fade < 1f) Text(stringResource(R.string.capture_fading), fontFamily = Sans, fontSize = 13.sp, color = Dawn.dim, modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            if (canSpeak) {
                Box(
                    Modifier.size(64.dp).clip(CircleShape).background(Dawn.field).border(1.5.dp, Dawn.glow, CircleShape).clickable {
                        runCatching { listen.launch(speech) }
                    },
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Rounded.Mic, stringResource(R.string.capture_speak), tint = Dawn.text, modifier = Modifier.size(30.dp)) }
            }
            Box(
                Modifier.weight(1f).height(64.dp).clip(CircleShape).background(if (text.isBlank()) Dawn.field else Dawn.glow)
                    .clickable(enabled = text.isNotBlank()) { save() },
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.capture_keep), fontFamily = Sans, fontSize = 18.sp, color = if (text.isBlank()) Dawn.dim else Dawn.bg)
            }
        }
    }
}
