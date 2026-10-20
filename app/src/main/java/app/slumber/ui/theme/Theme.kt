package app.slumber.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.slumber.R
import app.slumber.domain.Mood

/**
 * A Sunday supplement from 1906: newsprint, black ink outlines, flat bright colour, and the carmine red
 * of the title banner. The morning capture screen is the exception: nearly black, so it doesn't wake you.
 */
@Immutable
data class Palette(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val line: Color,
    val ink: Color,
    val muted: Color,
    val faint: Color,
    /** Banner red: for the one thing to do. */
    val red: Color,
    val redSoft: Color,
    val onRed: Color,
    /** Night blue, for the dream side of things. */
    val night: Color,
    val nightSoft: Color,
    val gold: Color,
)

val Newsprint = Palette(
    bg = Color(0xFFF2E8D3),
    surface = Color(0xFFFAF4E6),
    surface2 = Color(0xFFE9DDC3),
    line = Color(0xFFD8C9A8),
    ink = Color(0xFF1B1A24),
    muted = Color(0xFF5C5547),
    faint = Color(0xFF978C74),
    red = Color(0xFFB8372A),
    redSoft = Color(0xFFF1D3C6),
    onRed = Color(0xFFFFF8EC),
    night = Color(0xFF26305A),
    nightSoft = Color(0xFFDCDCEB),
    gold = Color(0xFFD9A33A),
)

/** The capture screen at dawn: dark red on black, dim enough to stay half asleep. */
object Dawn {
    val bg = Color(0xFF0E0504)
    val field = Color(0xFF1C0907)
    val text = Color(0xFFC0503C)
    val dim = Color(0xFF6E2A20)
    val glow = Color(0xFF8E2A1E)
}

/** The ink every outline is drawn in. */
val Ink = Color(0xFF1B1A24)
val Paper = Color(0xFFF4EAD5)

/** A panel's colours by how it felt: sky top and bottom, walls, ground, an accent and a deep shade. */
@Immutable
data class Tints(val sky: Color, val sky2: Color, val wall: Color, val ground: Color, val accent: Color, val deep: Color) {
    companion object {
        fun of(m: Mood): Tints = when (m) {
            Mood.CALM -> Tints(Color(0xFF8FC3CF), Color(0xFFDDEBE0), Color(0xFFE9DDBE), Color(0xFFB9C99A), Color(0xFF3F8A93), Color(0xFF2E5A66))
            Mood.JOY -> Tints(Color(0xFFF7C66A), Color(0xFFFCE7B0), Color(0xFFF6DDB0), Color(0xFFC9D27A), Color(0xFFE0703A), Color(0xFF9A4A22))
            Mood.WONDER -> Tints(Color(0xFF6F5DB3), Color(0xFFD6B9E6), Color(0xFFE3D2F0), Color(0xFF9C8BC9), Color(0xFFF0C24A), Color(0xFF3C2D6E))
            Mood.FEAR -> Tints(Color(0xFF1E2742), Color(0xFF3E4A70), Color(0xFF4B4F6A), Color(0xFF2B2F45), Color(0xFFC2412D), Color(0xFF10131F))
            Mood.ANXIETY -> Tints(Color(0xFFB9A55B), Color(0xFFDCD3A0), Color(0xFFCFC594), Color(0xFF9C976C), Color(0xFFB4543A), Color(0xFF5A5232))
            Mood.SADNESS -> Tints(Color(0xFF5F7A9E), Color(0xFFB7C3D0), Color(0xFFAFBACB), Color(0xFF7F8FA3), Color(0xFF3E5675), Color(0xFF2A3A52))
            Mood.ANGER -> Tints(Color(0xFFB23A26), Color(0xFFEE9A6E), Color(0xFFE7A884), Color(0xFFA0522D), Color(0xFFFFD166), Color(0xFF5E1A10))
            Mood.STRANGE -> Tints(Color(0xFF5DB39A), Color(0xFFF0B1CF), Color(0xFFC9E6D7), Color(0xFFE9A0C0), Color(0xFF7A4FA0), Color(0xFF2F4A44))
        }

        /** The waking panel: always the same pale dawn. */
        val Morning = Tints(Color(0xFFF3C9A6), Color(0xFFFBE9D2), Color(0xFFF1E1C4), Color(0xFFC9A77E), Color(0xFFB8372A), Color(0xFF6B4A33))
    }
}

/** Lettering for the banner. */
val Banner = FontFamily(Font(R.font.prata, FontWeight.Normal))

/** Alegreya: an old book face, for captions and the dream's own words. */
val Serif = FontFamily(
    Font(R.font.alegreya, FontWeight.Normal),
    Font(R.font.alegreya_italic, FontWeight.Normal, FontStyle.Italic),
)

val Sans = FontFamily(
    Font(R.font.alegreyasans_regular, FontWeight.Normal),
    Font(R.font.alegreyasans_medium, FontWeight.Medium),
    Font(R.font.alegreyasans_bold, FontWeight.Bold),
)

val LocalPalette = staticCompositionLocalOf { Newsprint }

object U {
    val c: Palette @Composable get() = LocalPalette.current

    val RXl = RoundedCornerShape(24.dp)
    val RLg = RoundedCornerShape(18.dp)
    val RMd = RoundedCornerShape(14.dp)
    val RSm = RoundedCornerShape(10.dp)
}

object UType {
    val Hero: TextStyle @Composable get() = TextStyle(fontFamily = Banner, fontSize = 34.sp, lineHeight = 42.sp, color = U.c.ink)
    val Display: TextStyle @Composable get() = TextStyle(fontFamily = Banner, fontSize = 26.sp, lineHeight = 33.sp, color = U.c.ink)
    val Title: TextStyle @Composable get() = TextStyle(fontFamily = Banner, fontSize = 21.sp, lineHeight = 27.sp, color = U.c.ink)
    /** The dream in its own words. */
    val Told: TextStyle @Composable get() = TextStyle(fontFamily = Serif, fontSize = 18.sp, lineHeight = 26.sp, color = U.c.ink)
    val Caption: TextStyle @Composable get() = TextStyle(fontFamily = Serif, fontSize = 13.sp, lineHeight = 17.sp, color = Ink)
    val Sub: TextStyle @Composable get() = TextStyle(fontFamily = Sans, fontSize = 16.sp, lineHeight = 23.sp, color = U.c.muted)
    val Eyebrow: TextStyle @Composable get() = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 0.14.em, color = U.c.faint)
    val Strong: TextStyle @Composable get() = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = U.c.ink)
    val Body: TextStyle @Composable get() = TextStyle(fontFamily = Sans, fontSize = 16.5.sp, lineHeight = 23.sp, color = U.c.ink)
    val Muted: TextStyle @Composable get() = TextStyle(fontFamily = Sans, fontSize = 14.5.sp, lineHeight = 20.sp, color = U.c.muted)
    val Small: TextStyle @Composable get() = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = U.c.faint)
    val Button: TextStyle @Composable get() = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 16.5.sp)
}

@Composable
fun SlumberTheme(content: @Composable () -> Unit) {
    val p = Newsprint
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = p.red,
                onPrimary = p.onRed,
                primaryContainer = p.redSoft,
                onPrimaryContainer = p.ink,
                secondary = p.night,
                background = p.bg,
                surface = p.surface,
                surfaceContainer = p.surface,
                surfaceContainerHigh = p.surface2,
                surfaceContainerHighest = p.surface2,
                surfaceContainerLow = p.surface,
                onBackground = p.ink,
                onSurface = p.ink,
                onSurfaceVariant = p.muted,
                outline = p.line,
                outlineVariant = p.line,
            ),
            typography = MaterialTheme.typography.copy(
                bodyLarge = UType.Body,
                bodyMedium = UType.Body.copy(fontSize = 15.sp),
                labelLarge = UType.Button,
            ),
            content = content,
        )
    }
}
