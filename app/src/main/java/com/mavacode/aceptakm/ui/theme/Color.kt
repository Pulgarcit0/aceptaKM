package com.mavacode.aceptakm.ui.theme

import androidx.compose.ui.graphics.Color

// --- 1. COLORES BASE (Modo Claro) ---
val MainBlue = Color(0xFF0058BE)
val SecondaryBlue = Color(0xFF0C5FCD)

val MainBackground = Color(0xFFF5F7FA) // Actualizado con tu último valor
val WhiteBackground = Color(0xFFFFFFFF)

val MainTextDark = Color(0xFF111827)   // Actualizado con tu último valor
val MainTextGray = Color(0xFF6B7280)   // Actualizado con tu último valor

val MainOutline = Color(0xFFC2C6D6)

// --- 2. COLORES BASE (Modo Oscuro - Preparación) ---
val DarkBackground = Color(0xFF0F1115)
val DarkSurface = Color(0xFF1A1D24)
val DarkCard = Color(0xFF22262E)
val DarkText = Color(0xFFF3F4F6)
val DarkTextGray = Color(0xFF9CA3AF)
val DarkOutline = Color(0xFF3F4651)

// --- 3. COLORES DE ACENTO Y ESTADOS ---
val ActiveGreen = Color(0xFF4ADE80)
val LightGreenIconBg = Color(0xFFE6F4EA)
val DarkGreenIcon = Color(0xFF137333)
val TextRedColor = Color(0xFFE02424)
val TextOrangeColor = Color(0xFFD97706)

// --- 4. COMPATIBILIDAD CON TUS PANTALLAS ---
// Azules
val primaryBlue = MainBlue
val PrimaryColor = MainBlue
val colorPrimary = MainBlue
val MainBlueColor = SecondaryBlue
val iconBlue = Color(0xFF0052CC)

// Fondos
val bgSurface = MainBackground
val BackgroundColor = MainBackground
val colorBackground = MainBackground
val CardBackgroundColor = WhiteBackground
val cardBackground = WhiteBackground
val cardBg = Color(0xFFF4F7FB)
val LightBlueCardColor = Color(0xFFF1F5F9)
val LightBackground = Color(0xFFF8FAFC)
val headerBackground = Color(0xFFF8F9FA)
val colorIconBg = Color(0xFFE5EEFF)

// Textos
val textDark = MainTextDark
val OnSurfaceColor = MainTextDark
val colorTextPrimary = MainTextDark
val textPrimary = MainTextDark // Unificado con la variable maestra

val TextGray = MainTextGray
val textSecondary = MainTextGray
val colorTextSecondary = MainTextGray
val OnSurfaceVariantColor = Color(0xFF424754)

// Bordes y Divisores
val OutlineVariantColor = MainOutline
val outlineGray = MainOutline
val dividerColor = Color(0xFFE5E7EB)

// --- 5. COLORES POR DEFECTO DE ANDROID ---
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)