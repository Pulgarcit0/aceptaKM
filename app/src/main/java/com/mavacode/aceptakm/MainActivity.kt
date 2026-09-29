package com.mavacode.aceptakm

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mavacode.aceptakm.ui.screens.AceptaKm
import com.mavacode.aceptakm.ui.theme.AceptaKMTheme
import com.mavacode.aceptakm.ui.theme.ThemePrefs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            )
        )

        setContent {
            val context = this
            var themeMode by remember { mutableStateOf(ThemePrefs.get(context)) }
            AceptaKMTheme(themeMode = themeMode) {
                AceptaKm(themeMode = themeMode, onThemeModeChange = { themeMode = it })
            }
        }
    }
}