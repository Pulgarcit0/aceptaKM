package com.mavacode.aceptakm.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mavacode.aceptakm.feature.permisos.PermisosManager
import com.mavacode.aceptakm.ui.theme.AceptaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class PermissionStep { OVERLAY, BATTERY, NOTIFICATION }

private data class PermissionCardData(
    val step: PermissionStep,
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val description: String
)

@Composable
fun PermissionGuideOverlay(onAllGranted: () -> Unit = {}) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var missingSteps by remember { mutableStateOf(getMissingSteps(context)) }

    fun refrescarPermisosConDelay() {
        missingSteps = getMissingSteps(context)
        scope.launch {
            delay(400)
            missingSteps = getMissingSteps(context)
            delay(800)
            missingSteps = getMissingSteps(context)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refrescarPermisosConDelay()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(missingSteps) {
        if (missingSteps.isEmpty()) onAllGranted()
    }

    if (missingSteps.isEmpty()) return

    val currentStep = missingSteps.first()

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refrescarPermisosConDelay()
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        refrescarPermisosConDelay()
    }

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F1115).copy(alpha = 0.82f)),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    (fadeIn(tween(280)) + slideInVertically { it / 6 }) togetherWith
                            (fadeOut(tween(200)) + slideOutVertically { -it / 6 })
                },
                label = "permission_card"
            ) { step ->
                PermissionCard(
                    data = getCardData(step),
                    onGrantClick = {
                        when (step) {
                            PermissionStep.OVERLAY -> {
                                settingsLauncher.launch(
                                    Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        "package:${context.packageName}".toUri()
                                    )
                                )
                            }
                            PermissionStep.BATTERY -> {
                                try {
                                    settingsLauncher.launch(
                                        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                            data = "package:${context.packageName}".toUri()
                                        }
                                    )
                                } catch (_: Exception) {
                                    settingsLauncher.launch(
                                        Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                    )
                                }
                            }
                            PermissionStep.NOTIFICATION -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    settingsLauncher.launch(
                                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PermissionGuideScreen(onAllGranted: () -> Unit = {}) {
    PermissionGuideOverlay(onAllGranted = onAllGranted)
}

@Composable
private fun PermissionCard(
    data: PermissionCardData,
    onGrantClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val app = AceptaTheme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = app.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(app.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = null,
                    tint = cs.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = data.subtitle.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = cs.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = data.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = app.textPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = data.description,
                fontSize = 14.sp,
                color = app.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onGrantClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cs.primary,
                    contentColor = cs.onPrimary
                )
            ) {
                Text("Conceder permiso", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun getCardData(step: PermissionStep) = when (step) {
    PermissionStep.OVERLAY -> PermissionCardData(
        step,
        Icons.Rounded.Layers,
        "Para mostrar la ventana de AceptaKm",
        "Permiso de superposición",
        "Necesario para mostrar la ventana flotante sobre las apps de viaje."
    )
    PermissionStep.BATTERY -> PermissionCardData(
        step,
        Icons.Rounded.BatteryChargingFull,
        "Dar permiso para mantener encendida AceptaKm",
        "Permiso de batería",
        "Evita que el sistema cierre la app para ahorrar batería."
    )
    PermissionStep.NOTIFICATION -> PermissionCardData(
        step,
        Icons.Rounded.Notifications,
        "Para que veas que AceptaKm sí está funcionando",
        "Permiso de notificación",
        "Muestra el estado del asistente mientras analiza viajes."
    )
}

private fun getMissingSteps(context: Context): List<PermissionStep> {
    val missing = mutableListOf<PermissionStep>()
    if (!PermisosManager.tienePermisoSuperposicion(context)) missing.add(PermissionStep.OVERLAY)
    if (!PermisosManager.tienePermisoBateria(context)) missing.add(PermissionStep.BATTERY)
    if (!PermisosManager.tienePermisoNotificaciones(context)) missing.add(PermissionStep.NOTIFICATION)
    return missing
}