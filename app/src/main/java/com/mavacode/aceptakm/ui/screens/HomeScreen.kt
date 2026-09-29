package com.mavacode.aceptakm.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mavacode.aceptakm.R
import com.mavacode.aceptakm.data.remote.EstadoSuscripcion
import com.mavacode.aceptakm.data.remote.actualizarConfiguracionEnFirestore
import com.mavacode.aceptakm.data.remote.observarEstadoSuscripcion
import com.mavacode.aceptakm.feature.asistente.FloatingService
import com.mavacode.aceptakm.feature.asistente.PlataformaManager
import com.mavacode.aceptakm.feature.permisos.PermisosManager
import com.mavacode.aceptakm.ui.theme.AceptaTheme
import com.mavacode.aceptakm.ui.theme.TextRedColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object OverlayPrefs {
    const val ACTION_ALPHA = "com.mavacode.aceptakm.OVERLAY_ALPHA"
    private const val PREFS = "aceptakmPrefs"
    private const val KEY = "overlayAlpha"

    fun getAlpha(context: Context): Float =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getFloat(KEY, 0.92f)
            .coerceIn(0.40f, 1f)

    fun setAlpha(context: Context, alpha: Float) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit { putFloat(KEY, alpha.coerceIn(0.40f, 1f)) }
        context.sendBroadcast(Intent(ACTION_ALPHA))
    }
}

@Composable
fun HomeScreen(
    isServiceRunning: Boolean,
    onSetServiceRunning: (Boolean) -> Unit,
    onNavigateToPlataformas: () -> Unit,
    onNavigateToZonas: () -> Unit,
    onNavigateToSuscripcion: () -> Unit
) {
    val context = LocalContext.current
    var mostrarTelegram by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        mostrarTelegram = debeMostrarAvisoTelegram(context)
    }

    if (mostrarTelegram) {
        TelegramInviteDialog(onDismiss = { mostrarTelegram = false })
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val sharedPreferences = context.getSharedPreferences("aceptakmPrefs", Context.MODE_PRIVATE)

    val cs = MaterialTheme.colorScheme
    val app = AceptaTheme.colors

    var tieneSuperposicion by remember { mutableStateOf(PermisosManager.tienePermisoSuperposicion(context)) }
    var tieneBateria by remember { mutableStateOf(PermisosManager.tienePermisoBateria(context)) }
    var tieneNotificaciones by remember { mutableStateOf(PermisosManager.tienePermisoNotificaciones(context)) }
    var mostrarDialogoBateria by remember { mutableStateOf(false) }
    var seccionAvanzadaAbierta by remember { mutableStateOf(false) }

    var mostrarGuiaPermisos by remember {
        mutableStateOf(
            !PermisosManager.tienePermisoSuperposicion(context) ||
                    !PermisosManager.tienePermisoBateria(context) ||
                    !PermisosManager.tienePermisoNotificaciones(context)
        )
    }

    var plataformaDefault by remember {
        mutableStateOf(PlataformaManager.obtenerPlataformaDefault(context))
    }
    var plataformasActivas by remember {
        mutableStateOf(PlataformaManager.obtenerPlataformasActivas(context))
    }

    val textoPlataforma = when {
        isServiceRunning && plataformasActivas.size == PlataformaManager.TODAS_LAS_PLATAFORMAS.size ->
            "Todas · $plataformaDefault"
        isServiceRunning && plataformasActivas.size == 1 ->
            plataformasActivas.first()
        isServiceRunning && plataformasActivas.isNotEmpty() ->
            "${plataformasActivas.size} plataformas"
        plataformaDefault.isNotBlank() -> plataformaDefault
        else -> "Sin plataforma"
    }

    val scope = rememberCoroutineScope()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                tieneSuperposicion = PermisosManager.tienePermisoSuperposicion(context)
                tieneNotificaciones = PermisosManager.tienePermisoNotificaciones(context)
                tieneBateria = PermisosManager.tienePermisoBateria(context)
                mostrarGuiaPermisos = !tieneSuperposicion || !tieneBateria || !tieneNotificaciones
                plataformaDefault = PlataformaManager.obtenerPlataformaDefault(context)
                plataformasActivas = PlataformaManager.obtenerPlataformasActivas(context)
                scope.launch {
                    delay(400)
                    tieneBateria = PermisosManager.tienePermisoBateria(context)
                    mostrarGuiaPermisos = !tieneSuperposicion || !tieneBateria || !tieneNotificaciones
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val requestNotificationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        tieneNotificaciones = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Se necesita el permiso para mostrar el estado", Toast.LENGTH_LONG).show()
        }
    }

    var tarifaMinima by remember { mutableStateOf(sharedPreferences.getFloat("tarifaMin", 25f).toString()) }
    var tarifaImpuesto by remember { mutableStateOf(sharedPreferences.getFloat("impuesto", 10.1f).toString()) }
    var distanciaMaxima by remember { mutableStateOf(sharedPreferences.getFloat("distMax", 12f).toString()) }
    var gananciaNeta by remember { mutableStateOf(sharedPreferences.getFloat("ganancia", 7f).toString()) }

    var estadoSuscripcion by remember { mutableStateOf(EstadoSuscripcion()) }
    var cargandoUsuario by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val listener = observarEstadoSuscripcion { nuevoEstado ->
            estadoSuscripcion = nuevoEstado
            cargandoUsuario = false
        }
        onDispose { listener?.remove() }
    }

    val tieneAcceso = estadoSuscripcion.isActive
    val camposEditables = tieneAcceso && !cargandoUsuario

    fun apagarAsistente() {
        context.stopService(Intent(context, FloatingService::class.java))
        PlataformaManager.desactivarTodas(context)
        plataformasActivas = emptySet()
        onSetServiceRunning(false)
        Toast.makeText(context, "Asistente apagado", Toast.LENGTH_SHORT).show()
    }

    fun irAPlataformas() {
        val yaVisto = sharedPreferences.getBoolean("batteryTipShown", false)
        if (yaVisto) {
            onNavigateToPlataformas()
        } else {
            mostrarDialogoBateria = true
        }
    }

    if (mostrarDialogoBateria) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoBateria = false },
            icon = { Icon(Icons.Default.BatteryAlert, contentDescription = null, tint = cs.primary) },
            title = {
                Text(text = "Mantén la pantalla encendida", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            },
            text = {
                Text(
                    text = "Si bloqueas el teléfono, el sistema pausa la lectura. Déjala encendida mientras esperas un viaje.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        sharedPreferences.edit { putBoolean("batteryTipShown", true) }
                        mostrarDialogoBateria = false
                        onNavigateToPlataformas()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = cs.primary)
                ) {
                    Text("Entendido")
                }
            },
            containerColor = cs.surface
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(cs.background)
                .padding(16.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = app.card),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isServiceRunning) "Estado: Encendido" else "Estado: Apagado",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = cs.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isServiceRunning) {
                            "Leyendo viajes. No bloquees la pantalla."
                        } else {
                            "Elige una plataforma para empezar"
                        },
                        fontSize = 15.sp,
                        color = app.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = textoPlataforma,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = cs.primary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val textoBoton = when {
                        isServiceRunning -> "APAGAR ASISTENTE"
                        !tieneSuperposicion -> "1. PERMISO DE SUPERPOSICIÓN"
                        !tieneBateria -> "2. PERMISO DE BATERÍA"
                        !tieneNotificaciones -> "3. PERMISO DE NOTIFICACIONES"
                        else -> "IR A PLATAFORMAS"
                    }

                    val iconoBoton = when {
                        isServiceRunning -> Icons.Default.PowerSettingsNew
                        !tieneSuperposicion -> Icons.Default.Layers
                        !tieneBateria -> Icons.Default.BatteryChargingFull
                        !tieneNotificaciones -> Icons.Default.Notifications
                        else -> Icons.Default.DirectionsCar
                    }

                    Button(
                        onClick = {
                            if (isServiceRunning) {
                                apagarAsistente()
                            } else {
                                when {
                                    !tieneSuperposicion -> PermisosManager.solicitarPermisoSuperposicion(context)
                                    !tieneBateria -> PermisosManager.solicitarPermisoBateria(context)
                                    !tieneNotificaciones -> {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            tieneNotificaciones = true
                                        }
                                    }
                                    else -> irAPlataformas()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isServiceRunning) TextRedColor else cs.primary
                        )
                    ) {
                        Icon(imageVector = iconoBoton, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = textoBoton, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (cargandoUsuario) {
                CircularProgressIndicator(color = cs.primary)
            } else {
                if (!tieneAcceso) {
                    LockedFeatureCard(onUpgradeClick = onNavigateToSuscripcion)
                    Spacer(modifier = Modifier.height(20.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = cs.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Configuraciones del Filtro",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = cs.onBackground
                    )
                    if (!camposEditables) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Lock, contentDescription = null, tint = app.textSecondary, modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = app.divider,
                    thickness = 1.dp
                )

                val textFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = app.card,
                    unfocusedContainerColor = app.card,
                    disabledContainerColor = app.card,
                    focusedBorderColor = cs.primary,
                    unfocusedBorderColor = app.outline,
                    disabledBorderColor = app.outline,
                    focusedLabelColor = cs.primary,
                    unfocusedLabelColor = app.textPrimary,
                    disabledLabelColor = app.textSecondary,
                    focusedTextColor = app.textPrimary,
                    unfocusedTextColor = app.textPrimary,
                    disabledTextColor = app.textSecondary,
                    cursorColor = cs.primary
                )
                val textFieldShape = RoundedCornerShape(12.dp)

                OutlinedTextField(
                    value = tarifaMinima,
                    onValueChange = { if (camposEditables) tarifaMinima = it },
                    enabled = camposEditables,
                    readOnly = !camposEditables,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(id = R.drawable.ic_payments), null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tarifa Mínima por Viaje ($)")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = textFieldShape,
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = tarifaImpuesto,
                    onValueChange = { if (camposEditables) tarifaImpuesto = it },
                    enabled = camposEditables,
                    readOnly = !camposEditables,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(id = R.drawable.ic_request_quote), null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retención de Impuestos (%)")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = textFieldShape,
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = distanciaMaxima,
                    onValueChange = { if (camposEditables) distanciaMaxima = it },
                    enabled = camposEditables,
                    readOnly = !camposEditables,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(id = R.drawable.ic_route), null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Distancia Máxima (km)")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = textFieldShape,
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = gananciaNeta,
                    onValueChange = { if (camposEditables) gananciaNeta = it },
                    enabled = camposEditables,
                    readOnly = !camposEditables,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(id = R.drawable.ic_trending), null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ganancia Neta por km")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = textFieldShape,
                    colors = textFieldColors
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (!camposEditables) {
                            onNavigateToSuscripcion()
                            return@Button
                        }
                        val tarifaMin = tarifaMinima.toDoubleOrNull() ?: 0.0
                        val impuesto = tarifaImpuesto.toDoubleOrNull() ?: 0.0
                        val distMax = distanciaMaxima.toDoubleOrNull() ?: 0.0
                        val ganancia = gananciaNeta.toDoubleOrNull() ?: 0.0
                        guardarConfiguracionFiltros(context, tarifaMin, impuesto, distMax, ganancia)
                        actualizarConfiguracionEnFirestore(tarifaMin, impuesto, distMax, ganancia) { exito ->
                            Toast.makeText(
                                context,
                                if (exito) "¡Configuración guardada en la nube!" else "Error al guardar en internet",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    enabled = camposEditables,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = cs.primary),
                    shape = CircleShape
                ) {
                    Text(
                        if (camposEditables) "GUARDAR CONFIGURACIÓN" else "SUSCRÍBETE PARA EDITAR",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = cs.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { seccionAvanzadaAbierta = !seccionAvanzadaAbierta },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Avanzado", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = app.textSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (seccionAvanzadaAbierta) {
                            Icons.Default.KeyboardDoubleArrowUp
                        } else {
                            Icons.Default.KeyboardDoubleArrowDown
                        },
                        contentDescription = null,
                        tint = app.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                AnimatedVisibility(
                    visible = seccionAvanzadaAbierta,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                if (camposEditables) onNavigateToZonas() else onNavigateToSuscripcion()
                            },
                            enabled = camposEditables,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB72025))
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Administrar zonas peligrosas", fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        OverlayTransparencyPanel(enabled = camposEditables)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (mostrarGuiaPermisos) {
            PermissionGuideScreen(
                onAllGranted = {
                    mostrarGuiaPermisos = false
                    tieneSuperposicion = PermisosManager.tienePermisoSuperposicion(context)
                    tieneBateria = PermisosManager.tienePermisoBateria(context)
                    tieneNotificaciones = PermisosManager.tienePermisoNotificaciones(context)
                }
            )
        }
    }
}

@Composable
private fun OverlayTransparencyPanel(enabled: Boolean = true) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme
    val app = AceptaTheme.colors

    val guardado = remember { OverlayPrefs.getAlpha(context) }
    var alpha by remember { mutableFloatStateOf(guardado) }
    var original by remember { mutableFloatStateOf(guardado) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = app.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Opacity, contentDescription = null, tint = cs.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ventana flotante", fontWeight = FontWeight.SemiBold, color = app.textPrimary)
                Spacer(modifier = Modifier.weight(1f))
                Text("${(alpha * 100).toInt()}%", color = cs.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .background(cs.surfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.alpha(alpha),
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🍏", fontSize = 16.sp)
                        Text(
                            "Toca para leer",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = alpha,
                onValueChange = { if (enabled) alpha = it },
                enabled = enabled,
                valueRange = 0.40f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = cs.primary,
                    activeTrackColor = cs.primary
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { if (enabled) alpha = original },
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        if (!enabled) return@Button
                        OverlayPrefs.setAlpha(context, alpha)
                        original = alpha
                        Toast.makeText(context, "Transparencia guardada", Toast.LENGTH_SHORT).show()
                    },
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = cs.primary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Listo")
                }
            }
        }
    }
}

@Composable
fun LockedFeatureCard(onUpgradeClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val app = AceptaTheme.colors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = app.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(56.dp), tint = app.textSecondary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Función Premium", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = app.textPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tu periodo de prueba terminó o no tienes una suscripción activa.\n\nPuedes ver la app, pero no modificar filtros, zonas ni el overlay.",
                fontSize = 14.sp,
                color = app.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onUpgradeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = cs.primary)
            ) {
                Text("Desbloquear ahora", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun guardarConfiguracionFiltros(
    context: Context,
    tarifaMin: Double,
    impuesto: Double,
    distMax: Double,
    ganancia: Double
) {
    val sharedPreferences = context.getSharedPreferences("aceptakmPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit {
        putFloat("tarifaMin", tarifaMin.toFloat())
        putFloat("impuesto", impuesto.toFloat())
        putFloat("distMax", distMax.toFloat())
        putFloat("ganancia", ganancia.toFloat())
    }
}