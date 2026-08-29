package com.mavacode.aceptakm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mavacode.aceptakm.ui.screens.AceptaKm
import com.mavacode.aceptakm.ui.theme.AceptaKMTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AceptaKMTheme {
                
                AceptaKm()
            }
        }
    }
}