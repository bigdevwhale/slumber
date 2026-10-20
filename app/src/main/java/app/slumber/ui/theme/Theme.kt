package app.slumber.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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

/**
 * A desk at night: dark walnut, one warm lamp, a brass-ringed wooden top. Everything is dim and amber
 * so the app can be opened in bed without waking anyone, yourself included.
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
    /** Lamplight: for the one thing to do. */
    val lamp: Color,
    val lampSoft: Color,
    val onLamp: Color,
    /** Brass, for lucid dreams. */
    val gold: Color,
    /** Moonlight blue, for the checks of the day. */
    val moon: Color,
    val moonSoft: Color,
)

val Walnut = Palette(
    bg = Color(0xFF15100C),
    surface = Color(0xFF211912),
    surface2 = Color(0xFF2B2118),
    line = Color(0xFF3A2D22),
    ink = Color(0xFFF2E4CF),
    muted = Color(0xFFBCA78C),
    faint = Color(0xFF85725D),
    lamp = Color(0xFFE9A54B),
    lampSoft = Color(0xFF3B2A17),
    onLamp = Color(0xFF1D130A),
    gold = Color(0xFFF1C66C),
    moon = Color(0xFF9DB4D8),
    moonSoft = Color(0xFF232C3B),
)

/** The capture screen at dawn: dim red on near black, so it doesn't wake you. */
object Dawn {
    val bg = Color(0xFF0E0504)
    val field = Color(0xFF1C0907)
    val text = Color(0xFFC0503C)
    val dim = Color(0xFF6E2A20)
    val glow = Color(0xFF8E2A1E)
}

val Banner = FontFamily(Font(R.font.prata, FontWeight.Normal))

/** Alegreya: an old book face, for the dream's own words. */
val Serif = FontFamily(
    Font(R.font.alegreya, FontWeight.Normal),
    Font(R.font.alegreya_italic, FontWeight.Normal, FontStyle.Italic),
)

val Sans = FontFamily(
    Font(R.font.alegreyasans_regular, FontWeight.Normal),
    Font(R.font.alegreyasans_medium, FontWeight.Medium),
    Font(R.font.alegreyasans_bold, FontWeight.Bold),
)

val LocalPalette = staticCompositionLocalOf { Walnut }

object U {
    val c: Palette @Composable get() = LocalPalette.current

    val RXl = RoundedCornerShape(24.dp)
    val RLg = RoundedCornerShape(18.dp)
    val RMd = RoundedCornerShape(14.dp)
    val RSm = RoundedCornerShape(10.dp)
}

object UType {
    val Hero: TextStyle @Composable get() = TextStyle(fontFamily = Banner, fontSize = 32.sp, lineHeight = 40.sp, color = U.c.ink)
    val Display: TextStyle @Composable get() = TextStyle(fontFamily = Banner, fontSize = 25.sp, lineHeight = 32.sp, color = U.c.ink)
    val Title: TextStyle @Composable get() = TextStyle(fontFamily = Banner, fontSize = 21.sp, lineHeight = 27.sp, color = U.c.ink)
    /** The dream in its own words, and the intention at night. */
    val Told: TextStyle @Composable get() = TextStyle(fontFamily = Serif, fontSize = 18.sp, lineHeight = 26.sp, color = U.c.ink)
    val Prompt: TextStyle @Composable get() = TextStyle(fontFamily = Serif, fontStyle = FontStyle.Italic, fontSize = 25.sp, lineHeight = 33.sp, color = U.c.ink)
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
    val p = Walnut
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = p.lamp,
                onPrimary = p.onLamp,
                primaryContainer = p.lampSoft,
                onPrimaryContainer = p.ink,
                secondary = p.moon,
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
