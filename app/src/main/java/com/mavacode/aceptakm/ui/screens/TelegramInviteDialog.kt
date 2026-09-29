package com.mavacode.aceptakm.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mavacode.aceptakm.ui.theme.AceptaTheme

private const val PREFS = "aceptakmPrefs"
private const val KEY_ABRIO = "telegramGrupoAbierto"
private const val KEY_ULTIMO_AVISO = "telegramGrupoUltimoAviso"
private const val SIETE_DIAS_MS = 24L * 60 * 60 * 1000
private const val LINK = "https://t.me/soporte_Mava"

fun debeMostrarAvisoTelegram(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    if (prefs.getBoolean(KEY_ABRIO, false)) return false
    val ultimo = prefs.getLong(KEY_ULTIMO_AVISO, 0L)
    if (ultimo == 0L) return true
    return System.currentTimeMillis() - ultimo >= SIETE_DIAS_MS
}

@Composable
fun TelegramInviteDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme
    val app = AceptaTheme.colors

    fun marcarMostrado() {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_ULTIMO_AVISO, System.currentTimeMillis())
            .apply()
    }

    fun abrirTelegram() {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ABRIO, true)
            .apply()
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(LINK)))
        onDismiss()
    }

    Dialog(onDismissRequest = {
        marcarMostrado()
        onDismiss()
    }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = app.card)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(cs.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Celebration,
                        contentDescription = null,
                        tint = cs.primary,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "¡Qué bueno tenerte aquí!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = app.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Únete al grupo de conductores de AceptaKm.\nAhí nos dices qué mejorar y qué quieres ver en la app.",
                    fontSize = 15.sp,
                    color = app.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { abrirTelegram() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Unirme al grupo", fontWeight = FontWeight.Bold)
                }
                TextButton(
                    onClick = {
                        marcarMostrado()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ahora no", color = app.textSecondary)
                }
            }
        }
    }
}