package com.mavacode.aceptakm.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AceptaKMTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = MainBlue,
            onPrimary = Color.White,
            background = DarkBackground,
            onBackground = DarkText,
            surface = DarkSurface,
            onSurface = DarkText,
            surfaceVariant = DarkCard,
            onSurfaceVariant = DarkTextGray,
            outline = DarkOutline
        )
    } else {
        lightColorScheme(
            primary = MainBlue,
            onPrimary = Color.White,
            background = MainBackground,
            onBackground = MainTextDark,
            surface = WhiteBackground,
            onSurface = MainTextDark,
            surfaceVariant = Color(0xFFF3F4F6),
            onSurfaceVariant = MainTextGray,
            outline = Color(0xFFC2C6D6)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Asegúrate de tener tu archivo Type.kt con esta variable
        content = content
    )
}