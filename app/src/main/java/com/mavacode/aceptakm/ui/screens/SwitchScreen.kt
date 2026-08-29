package com.mavacode.aceptakm.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.mavacode.aceptakm.data.remote.EstadoSuscripcion
import com.mavacode.aceptakm.data.remote.observarEstadoSuscripcion
import com.mavacode.aceptakm.feature.asistente.FloatingService
import com.mavacode.aceptakm.feature.asistente.PlataformaManager
import com.mavacode.aceptakm.feature.permisos.PermisosManager
import com.mavacode.aceptakm.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SwitchScreen(
    isServiceRunning: Boolean,
    onSetServiceRunning: (Boolean) -> Unit,
    onSolicitarPermiso: () -> Unit,
    onNavigateToSuscripcion: () -> Unit,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var plataformasSeleccionadas by remember {
        mutableStateOf(PlataformaManager.obtenerPlataformasActivas(context).toMutableSet())
    }

    var plataformaDefault by remember {
        mutableStateOf(PlataformaManager.obtenerPlataformaDefault(context))
    }

    var mostrarInstrucciones by remember { mutableStateOf(false) }

    // Pendiente hasta que el usuario acepte compartir pantalla
    var pendienteActivacion by remember { mutableStateOf<Set<String>?>(null) }
    var esperandoCaptura by remember { mutableStateOf(false) }

    // --- PERMISOS ---
    var tieneSuperposicion by remember { mutableStateOf(PermisosManager.tienePermisoSuperposicion(context)) }
    var tieneBateria by remember { mutableStateOf(PermisosManager.tienePermisoBateria(context)) }
    var tieneNotificaciones by remember { mutableStateOf(PermisosManager.tienePermisoNotificaciones(context)) }

    val scope = rememberCoroutineScope()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                tieneSuperposicion = PermisosManager.tienePermisoSuperposicion(context)
                tieneNotificaciones = PermisosManager.tienePermisoNotificaciones(context)
                tieneBateria = PermisosManager.tienePermisoBateria(context)
                scope.launch {
                    kotlinx.coroutines.delay(400)
                    tieneBateria = PermisosManager.tienePermisoBateria(context)
                }

                // Volvió del diálogo de captura
                if (esperandoCaptura) {
                    scope.launch {
                        // Dar tiempo a que el padre arranque el servicio si aceptó
                        kotlinx.coroutines.delay(500)
                        val pendiente = pendienteActivacion
                        if (FloatingService.isRunning || isServiceRunning) {
                            if (pendiente != null) {
                                PlataformaManager.guardarPlataformasActivas(context, pendiente)
                                plataformasSeleccionadas = pendiente.toMutableSet()
                                if (pendiente.size == 1) {
                                    PlataformaManager.guardarPlataformaDefault(
                                        context,
                                        pendiente.first()
                                    )
                                    plataformaDefault = pendiente.first()
                                }
                            }
                        } else {
                            // Canceló compartir pantalla → switch vuelve a OFF
                            pendienteActivacion = null
                            Toast.makeText(
                                context,
                                "No se activó: debes permitir capturar pantalla",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        pendienteActivacion = null
                        esperandoCaptura = false
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Si el servicio arranca mientras estamos esperando, confirma
    LaunchedEffect(isServiceRunning) {
        if (isServiceRunning && esperandoCaptura) {
            val pendiente = pendienteActivacion
            if (pendiente != null) {
                PlataformaManager.guardarPlataformasActivas(context, pendiente)
                plataformasSeleccionadas = pendiente.toMutableSet()
                if (pendiente.size == 1) {
                    PlataformaManager.guardarPlataformaDefault(context, pendiente.first())
                    plataformaDefault = pendiente.first()
                }
            }
            pendienteActivacion = null
            esperandoCaptura = false
        }
    }

    val permisosCompletos = tieneSuperposicion && tieneBateria && tieneNotificaciones

    // --- SUSCRIPCIÓN ---
    var estadoSuscripcion by remember { mutableStateOf(EstadoSuscripcion()) }
    var cargandoSuscripcion by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val listener = observarEstadoSuscripcion { nuevoEstado ->
            estadoSuscripcion = nuevoEstado
            cargandoSuscripcion = false
        }
        onDispose { listener?.remove() }
    }

    val tieneAcceso = estadoSuscripcion.isActive

    val todasActivas = PlataformaManager.TODAS_LAS_PLATAFORMAS.all {
        plataformasSeleccionadas.contains(it)
    }

    // --- DIÁLOGO DE CAPTURA ---
    if (mostrarInstrucciones) {
        val pendientes = pendienteActivacion.orEmpty()
        val multiples = pendientes.size > 1
        AlertDialog(
            onDismissRequest = {
                // Cerrar sin activar
                mostrarInstrucciones = false
                pendienteActivacion = null
            },
            icon = { Icon(Icons.Default.ScreenShare, contentDescription = null, tint = PrimaryColor) },
            title = {
                Text(
                    text = "Instrucción Importante",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                if (multiples) {
                    Text(
                        "Has elegido todas las plataformas.\n\nCuando Android te pregunte, elige:\n\n👉 Compartir Toda la Pantalla\n\nSi eliges una sola app, el asistente no podrá leer las demás.",
                        textAlign = TextAlign.Center
                    )
                } else {
                    val appUnica = pendientes.firstOrNull() ?: "tu app de viajes"
                    Text(
                        "Has elegido solo $appUnica.\n\nCuando Android te pregunte, te recomendamos:\n\n👉 Compartir una sola App\n(y seleccionas $appUnica)\n\nAsí ahorras batería y cuidas tu privacidad.",
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarInstrucciones = false
                        esperandoCaptura = true
                        onSolicitarPermiso()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("Entendido, Iniciar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarInstrucciones = false
                        pendienteActivacion = null
                    }
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Plataformas de Viaje",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Activa Todas, o elige una sola plataforma. El ancla marca cuál se muestra como default en Home.",
            fontSize = 16.sp,
            color = OnSurfaceVariantColor,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(32.dp))

        when {
            cargandoSuscripcion -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            }

            !tieneAcceso -> {
                CardBloqueo(
                    titulo = "Suscripción Requerida",
                    mensaje = "Tu periodo de prueba o membresía ha concluido. Adquiere Premium para activar el asistente.",
                    textoBoton = "Ver Planes Premium",
                    onClick = onNavigateToSuscripcion
                )
            }

            !permisosCompletos -> {
                val (titulo, mensaje, accion) = when {
                    !tieneSuperposicion -> Triple(
                        "Falta permiso de Superposición",
                        "Necesitas permitir que AceptaKm se muestre sobre otras apps.",
                        { PermisosManager.solicitarPermisoSuperposicion(context) }
                    )
                    !tieneBateria -> Triple(
                        "Falta permiso de Batería",
                        "Desactiva las restricciones de batería para que no se apague.",
                        { PermisosManager.solicitarPermisoBateria(context) }
                    )
                    else -> Triple(
                        "Falta permiso de Notificaciones",
                        "Permiso necesario para mostrar el estado del asistente.",
                        {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Toast.makeText(
                                    context,
                                    "Ve a Home y otorga el permiso de notificaciones",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )
                }
                CardBloqueo(
                    titulo = titulo,
                    mensaje = mensaje,
                    textoBoton = "Otorgar permiso",
                    onClick = accion
                )
            }

            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    PlataformaCardHtml(
                        nombre = "Todas las plataformas",
                        icono = Icons.Default.Apps,
                        colorTema = PrimaryColor,
                        isActivo = todasActivas,
                        switchEnabled = true,
                        mostrarAncla = false,
                        onCheckedChange = { activo ->
                            if (activo) {
                                if (isServiceRunning) {
                                    // Ya hay captura: solo cambiar plataformas
                                    PlataformaManager.activarTodas(context)
                                    plataformasSeleccionadas =
                                        PlataformaManager.TODAS_LAS_PLATAFORMAS.toMutableSet()
                                    Toast.makeText(
                                        context,
                                        "Todas las plataformas activadas",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    // Aún no hay captura → NO guardar hasta que acepte
                                    pendienteActivacion =
                                        PlataformaManager.TODAS_LAS_PLATAFORMAS.toSet()
                                    mostrarInstrucciones = true
                                }
                            } else {
                                PlataformaManager.desactivarTodas(context)
                                plataformasSeleccionadas = mutableSetOf()
                                context.stopService(Intent(context, FloatingService::class.java))
                                onSetServiceRunning(false)
                                pendienteActivacion = null
                                esperandoCaptura = false
                                Toast.makeText(
                                    context,
                                    "Todas desactivadas. Ahora puedes elegir una sola.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val plataformasData = listOf(
                        Triple("Didi Moto/Auto", Icons.Default.DirectionsCar, Color(0xFFFF7D00)),
                        Triple("Uber Moto/Auto", Icons.Default.LocalTaxi, Color(0xFF000000)),
                        Triple("inDrive", Icons.Default.ElectricRickshaw, Color(0xFF00D166)),
                        Triple("Didi Food", Icons.Default.Restaurant, Color(0xFFFF7D00)),
                        Triple("Uber Eats", Icons.Default.DeliveryDining, Color(0xFF06C167)),
                        Triple("Rappi", Icons.Default.TwoWheeler, Color(0xFFFF441F)),
                        Triple("Cabify", Icons.Default.AirportShuttle, Color(0xFF7145D6)),
                        Triple("Lalamove", Icons.Default.LocalShipping, Color(0xFFF15A24))
                    )

                    plataformasData.forEach { (nombre, icono, color) ->
                        val isSeleccionado = plataformasSeleccionadas.contains(nombre)

                        PlataformaCardHtml(
                            nombre = nombre,
                            icono = icono,
                            colorTema = color,
                            isActivo = isSeleccionado,
                            switchEnabled = !todasActivas,
                            isAnclada = plataformaDefault == nombre,
                            mostrarAncla = true,
                            onAnclaClick = {
                                PlataformaManager.guardarPlataformaDefault(context, nombre)
                                plataformaDefault = nombre
                                Toast.makeText(
                                    context,
                                    "$nombre es ahora el default en Home",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onCheckedChange = { activo ->
                                if (todasActivas) {
                                    Toast.makeText(
                                        context,
                                        "Primero desactiva \"Todas las plataformas\" para elegir una sola app.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@PlataformaCardHtml
                                }

                                if (activo) {
                                    if (isServiceRunning) {
                                        // Servicio ya corre: cambiar a esta sola app
                                        val nuevo = setOf(nombre)
                                        PlataformaManager.guardarPlataformasActivas(context, nuevo)
                                        PlataformaManager.guardarPlataformaDefault(context, nombre)
                                        plataformasSeleccionadas = nuevo.toMutableSet()
                                        plataformaDefault = nombre
                                        Toast.makeText(
                                            context,
                                            "Ahora solo $nombre. Si antes compartiste otra app, reinicia el asistente.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        // Primera activación: esperar captura
                                        pendienteActivacion = setOf(nombre)
                                        mostrarInstrucciones = true
                                    }
                                } else {
                                    PlataformaManager.desactivarTodas(context)
                                    plataformasSeleccionadas = mutableSetOf()
                                    context.stopService(
                                        Intent(context, FloatingService::class.java)
                                    )
                                    onSetServiceRunning(false)
                                    pendienteActivacion = null
                                    esperandoCaptura = false
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        BannerProximamenteHtml()
        Spacer(modifier = Modifier.height(32.dp))
    }
}