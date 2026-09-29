package com.mavacode.aceptakm.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemeMode { SYSTEM, LIGHT, DARK }

object ThemePrefs {
    private const val PREFS = "aceptakmPrefs"
    private const val KEY = "themeMode"

    fun get(context: Context): ThemeMode =
        runCatching {
            ThemeMode.valueOf(
                context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .getString(KEY, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
            )
        }.getOrDefault(ThemeMode.SYSTEM)

    fun set(context: Context, mode: ThemeMode) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, mode.name)
            .apply()
    }
}

private val LightScheme = lightColorScheme(
    primary = MainBlue,
    onPrimary = Color.White,
    secondary = SecondaryBlue,
    onSecondary = Color.White,
    background = MainBackground,
    onBackground = MainTextDark,
    surface = WhiteBackground,
    onSurface = MainTextDark,
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = MainTextGray,
    outline = MainOutline,
    error = TextRedColor,
    onError = Color.White
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF5B9BFF),
    onPrimary = Color(0xFF001B3D),
    secondary = Color(0xFF7AB0FF),
    onSecondary = Color(0xFF001B3D),
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkCard,
    onSurfaceVariant = DarkTextGray,
    outline = DarkOutline,
    error = Color(0xFFFF8A80),
    onError = Color(0xFF3B0000)
)

data class AceptaColors(
    val card: Color,
    val cardAlt: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val outline: Color,
    val divider: Color,
    val iconBg: Color
)

val LocalAceptaColors = staticCompositionLocalOf {
    AceptaColors(
        card = WhiteBackground,
        cardAlt = LightBlueCardColor,
        textPrimary = MainTextDark,
        textSecondary = MainTextGray,
        outline = MainOutline,
        divider = dividerColor,
        iconBg = colorIconBg
    )
}

private val LightAppColors = AceptaColors(
    card = WhiteBackground,
    cardAlt = LightBlueCardColor,
    textPrimary = MainTextDark,
    textSecondary = MainTextGray,
    outline = MainOutline,
    divider = dividerColor,
    iconBg = colorIconBg
)

private val DarkAppColors = AceptaColors(
    card = DarkCard,
    cardAlt = DarkSurface,
    textPrimary = DarkText,
    textSecondary = DarkTextGray,
    outline = DarkOutline,
    divider = DarkOutline,
    iconBg = Color(0xFF1E3A5F)
)

@Composable
fun AceptaKMTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val scheme = if (darkTheme) DarkScheme else LightScheme
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    CompositionLocalProvider(LocalAceptaColors provides appColors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography,
            content = content
        )
    }
}

object AceptaTheme {
    val colors: AceptaColors
        @Composable
        get() = LocalAceptaColors.current
}