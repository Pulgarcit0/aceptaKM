package com.mavacode.aceptakm.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.mavacode.aceptakm.R
import com.mavacode.aceptakm.data.remote.EstadoSuscripcion
import com.mavacode.aceptakm.data.remote.actualizarConfiguracionEnFirestore
import com.mavacode.aceptakm.data.remote.observarEstadoSuscripcion
import com.mavacode.aceptakm.feature.asistente.FloatingService
import com.mavacode.aceptakm.feature.asistente.PlataformaManager
import com.mavacode.aceptakm.feature.permisos.PermisosManager
import com.mavacode.aceptakm.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    isServiceRunning: Boolean,
    onSetServiceRunning: (Boolean) -> Unit,
    onNavigateToPlataformas: () -> Unit,
    onNavigateToZonas: () -> Unit,
    onNavigateToSuscripcion: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val sharedPreferences = context.getSharedPreferences("aceptakmPrefs", Context.MODE_PRIVATE)

    // --- PERMISOS ---
    var tieneSuperposicion by remember { mutableStateOf(PermisosManager.tienePermisoSuperposicion(context)) }
    var tieneBateria by remember { mutableStateOf(PermisosManager.tienePermisoBateria(context)) }
    var tieneNotificaciones by remember { mutableStateOf(PermisosManager.tienePermisoNotificaciones(context)) }
    var mostrarDialogoBateria by remember { mutableStateOf(false) }

    var mostrarGuiaPermisos by remember {
        mutableStateOf(
            !PermisosManager.tienePermisoSuperposicion(context) ||
                    !PermisosManager.tienePermisoBateria(context) ||
                    !PermisosManager.tienePermisoNotificaciones(context)
        )
    }

    // --- PLATAFORMAS / ANCLA ---
    var plataformaDefault by remember {
        mutableStateOf(PlataformaManager.obtenerPlataformaDefault(context))
    }
    var plataformasActivas by remember {
        mutableStateOf(PlataformaManager.obtenerPlataformasActivas(context))
    }

    val textoPlataforma = when {
        plataformasActivas.isEmpty() -> "Ninguna activa"
        plataformasActivas.size == PlataformaManager.TODAS_LAS_PLATAFORMAS.size ->
            "Todas · Default: $plataformaDefault"
        plataformasActivas.size == 1 ->
            plataformasActivas.first()
        else ->
            "${plataformasActivas.size} activas · Default: $plataformaDefault"
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
                    kotlinx.coroutines.delay(400)
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

    // --- FILTROS ---
    var tarifaMinima by remember { mutableStateOf(sharedPreferences.getFloat("tarifaMin", 25f).toString()) }
    var tarifaImpuesto by remember { mutableStateOf(sharedPreferences.getFloat("impuesto", 10.1f).toString()) }
    var distanciaMaxima by remember { mutableStateOf(sharedPreferences.getFloat("distMax", 12f).toString()) }
    var gananciaNeta by remember { mutableStateOf(sharedPreferences.getFloat("ganancia", 7f).toString()) }

    // --- SUSCRIPCIÓN ---
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

    if (mostrarDialogoBateria) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoBateria = false },
            icon = { Icon(Icons.Default.BatteryAlert, contentDescription = null, tint = MainBlue) },
            title = {
                Text(text = "¡Aviso Importante!", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            },
            text = {
                Text(
                    text = "Tu teléfono es muy eficiente ahorrando energía, por lo que si bloqueas la pantalla, el sistema detendrá el asistente para ahorrar batería.\n\nMantén la pantalla encendida mientras esperas un viaje para que AceptaKm funcione al 100%.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoBateria = false
                        onNavigateToPlataformas()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MainBlue)
                ) {
                    Text("Entendido, encender")
                }
            },
            containerColor = WhiteBackground
        )
    }

    // ====================== CONTENIDO + GUÍA ======================
    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackground)
                .padding(16.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WhiteBackground),
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
                        color = MainTextDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isServiceRunning) "Analizando la pantalla…" else "Elige plataforma y actívala para empezar",
                        fontSize = 16.sp,
                        color = MainTextGray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (isServiceRunning) "Si bloqueas el teléfono, la lectura se pausa. Toca la oruga para reactivar."
                        else "Desde Plataformas enciendes DiDi, Uber, etc.",
                        fontSize = 12.sp,
                        color = MainTextGray,
                        modifier = Modifier.padding(top = 4.dp),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Plataforma: $textoPlataforma",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MainBlue
                    )
                    Text(
                        text = "Cámbiala en la pestaña Plataformas",
                        fontSize = 12.sp,
                        color = MainTextGray
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val textoBoton = when {
                        isServiceRunning -> "APAGAR ASISTENTE"
                        !tieneSuperposicion -> "1. PERMISO DE SUPERPOSICIÓN"
                        !tieneBateria -> "2. PERMISO DE BATERÍA"
                        !tieneNotificaciones -> "3. PERMISO DE NOTIFICACIONES"
                        else -> "IR A PLATAFORMAS / ENCENDER"
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
                                context.stopService(Intent(context, FloatingService::class.java))
                                onSetServiceRunning(false)
                                Toast.makeText(context, "Asistente apagado", Toast.LENGTH_SHORT).show()
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
                                    else -> mostrarDialogoBateria = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isServiceRunning) TextRedColor else MainBlue
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
                CircularProgressIndicator(color = MainBlue)
            } else if (!tieneAcceso) {
                LockedFeatureCard(onUpgradeClick = onNavigateToSuscripcion)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = MainBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Configuraciones del Filtro",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MainTextDark
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = dividerColor,
                    thickness = 1.dp
                )

                val textFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = WhiteBackground,
                    unfocusedContainerColor = WhiteBackground,
                    focusedBorderColor = MainBlue,
                    unfocusedBorderColor = MainOutline,
                    focusedLabelColor = MainBlue,
                    unfocusedLabelColor = MainTextDark,
                    focusedTextColor = MainTextDark,
                    unfocusedTextColor = MainTextDark
                )
                val textFieldShape = RoundedCornerShape(12.dp)

                OutlinedTextField(
                    value = tarifaMinima,
                    onValueChange = { tarifaMinima = it },
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
                    onValueChange = { tarifaImpuesto = it },
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
                    onValueChange = { distanciaMaxima = it },
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
                    onValueChange = { gananciaNeta = it },
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
                        val tarifaMin = tarifaMinima.toDoubleOrNull() ?: 0.0
                        val impuesto = tarifaImpuesto.toDoubleOrNull() ?: 0.0
                        val distMax = distanciaMaxima.toDoubleOrNull() ?: 0.0
                        val ganancia = gananciaNeta.toDoubleOrNull() ?: 0.0

                        guardarConfiguracionFiltros(context, tarifaMin, impuesto, distMax, ganancia)

                        actualizarConfiguracionEnFirestore(tarifaMin, impuesto, distMax, ganancia) { exito ->
                            if (exito) {
                                Toast.makeText(context, "¡Configuración guardada en la nube!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Error al guardar en internet", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MainBlue),
                    shape = CircleShape
                ) {
                    Text(
                        "GUARDAR CONFIGURACIÓN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = dividerColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onNavigateToZonas,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MainBlue)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "ADMINISTRAR ZONAS PELIGROSAS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // ====================== GUÍA DE PERMISOS ======================
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
fun LockedFeatureCard(onUpgradeClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = Color(0xFF9CA3AF)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Función Premium",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MainTextDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tu periodo de prueba terminó o no tienes una suscripción activa.\n\nSuscríbete para seguir usando los filtros y zonas peligrosas.",
                fontSize = 14.sp,
                color = MainTextGray,
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
                colors = ButtonDefaults.buttonColors(containerColor = MainBlue)
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