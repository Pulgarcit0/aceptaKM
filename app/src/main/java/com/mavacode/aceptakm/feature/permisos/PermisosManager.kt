package com.mavacode.aceptakm.feature.permisos

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.mavacode.aceptakm.feature.asistente.FloatingService

object PermisosManager {

    // 1. Permiso de Superposición (Overlay) - Para dibujar la burbuja
    fun tienePermisoSuperposicion(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun solicitarPermisoSuperposicion(context: Context) {
        Toast.makeText(context, "Concede el permiso para mostrar sobre otras apps", Toast.LENGTH_LONG).show()
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            "package:${context.packageName}".toUri()
        )
        context.startActivity(intent)
    }

    // 2. Permiso de Batería - Para que el OCR no se cierre en segundo plano
    fun tienePermisoBateria(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun solicitarPermisoBateria(context: Context) {
        if (!tienePermisoBateria(context)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = "package:${context.packageName}".toUri()
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    // 3. Permiso para mostrar notificaciones (Requisito para Foreground Service en Android 13+)
    fun tienePermisoNotificaciones(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // En versiones anteriores a Android 13, se concede al instalar
        }
    }
}

// 4. Lanzador para la Captura de Pantalla (OCR)
@Composable
fun rememberScreenCaptureLauncher(
    context: Context,
    onSuccess: () -> Unit,
    onError: () -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val intent = Intent(context, FloatingService::class.java).apply {
                putExtra("RESULT_CODE", result.resultCode)
                putExtra("DATA_INTENT", result.data)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            onSuccess()
        } else {
            onError()
        }
    }
}