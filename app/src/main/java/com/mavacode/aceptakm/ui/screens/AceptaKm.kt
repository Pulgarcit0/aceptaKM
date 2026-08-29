package com.mavacode.aceptakm.ui.screens

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.edit
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mavacode.aceptakm.feature.asistente.FloatingService
import com.mavacode.aceptakm.data.remote.actualizarConfiguracionEnFirestore
import com.mavacode.aceptakm.data.remote.crearUsuarioEnFirestore
import com.mavacode.aceptakm.data.remote.verificarSiUsuarioExisteEnFirebase
import com.mavacode.aceptakm.feature.permisos.rememberScreenCaptureLauncher
import com.mavacode.aceptakm.data.BillingManager // <--- Importación añadida para el verificador
import com.mavacode.aceptakm.data.remote.obtenerEstadoSuscripcion
import java.text.SimpleDateFormat
import java.util.Locale // <--- Importación añadida para formatear la fecha correctamente

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AceptaKm() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val sharedPreferences = context.getSharedPreferences("aceptakmPrefs", Context.MODE_PRIVATE)
    var tabSeleccionada by remember { mutableIntStateOf(0) }

    val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    var isServiceRunning by remember { mutableStateOf(false) }

    val screenCaptureLauncher = rememberScreenCaptureLauncher(
        context = context,
        onSuccess = {
            isServiceRunning = true
            Toast.makeText(context, "Asistente y OCR listos", Toast.LENGTH_SHORT).show()
        },
        onError = {
            isServiceRunning = false
            Toast.makeText(context, "Permiso de grabación denegado", Toast.LENGTH_SHORT).show()
        }
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val pantallasConMenu = listOf("home", "switch", "settings")
    val mostrarMenus = currentRoute in pantallasConMenu

    Scaffold(
        topBar = {
            if (mostrarMenus) {
                TopAppBar(
                    title = { Text("AceptaKm", fontWeight = FontWeight.Bold, color = Color(0xFF0052CC)) },
                    actions = {
                        IconButton(onClick = { /* Acción de notificaciones */ }) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = "Notificaciones")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        },
        bottomBar = {
            if (mostrarMenus) {
                NavigationBar(containerColor = Color.White) {
                    NavigationBarItem(
                        selected = tabSeleccionada == 0,
                        onClick = {
                            tabSeleccionada = 0
                            navController.navigate("home") { popUpTo("home") { inclusive = true } }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = tabSeleccionada == 1,
                        onClick = {
                            tabSeleccionada = 1
                            navController.navigate("switch") { popUpTo("home") }
                        },
                        icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "Switch") },
                        label = { Text("Switch") },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFF0052CC).copy(alpha = 0.2f),
                            selectedIconColor = Color(0xFF0052CC),
                            selectedTextColor = Color(0xFF0052CC)
                        )
                    )
                    NavigationBarItem(
                        selected = tabSeleccionada == 2,
                        onClick = {
                            tabSeleccionada = 2
                            navController.navigate("settings") { popUpTo("home") }
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("splash") {
                LaunchedEffect(Unit) {
                    verificarSiUsuarioExisteEnFirebase { existe ->
                        if (existe) {
                            navController.navigate("home") { popUpTo("splash") { inclusive = true } }
                        } else {
                            navController.navigate("login") { popUpTo("splash") { inclusive = true } }
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF0052CC))
                }
            }

            composable("login") {
                LoginScreen(
                    onLoginSuccessClick = {
                        verificarSiUsuarioExisteEnFirebase { existe ->
                            if (existe) {
                                navController.navigate("home") { popUpTo("login") { inclusive = true } }
                            } else {
                                navController.navigate("google_success")
                            }
                        }
                    }
                )
            }

            composable("google_success") {
                GoogleSuccessScreen(
                    onContinueClick = {
                        crearUsuarioEnFirestore { exito ->
                            if(exito) {
                                navController.navigate("step1_tariff") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, "Error al guardar el perfil", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }

            composable("step1_tariff") {
                TariffConfigScreen(
                    onNextClick = { tarifa ->
                        val tarifaFloat = tarifa.toFloatOrNull() ?: 0f
                        sharedPreferences.edit { putFloat("tarifaMin", tarifaFloat) }
                        navController.navigate("step2_distance")
                    }
                )
            }

            composable("step2_distance") {
                FilterConfigStepScreen(
                    stepNumber = 2,
                    title = "¿Distancia máxima por viaje?",
                    description = "Ingresa el máximo de kilómetros que estás dispuesto a recorrer por un solo servicio.",
                    placeholder = "Ej: 15",
                    suffixText = "km",
                    onNextClick = { distancia ->
                        val distanciaFloat = distancia.toFloatOrNull() ?: 0f
                        sharedPreferences.edit { putFloat("distMax", distanciaFloat) }
                        navController.navigate("step3_tax")
                    }
                )
            }

            composable("step3_tax") {
                FilterConfigStepScreen(
                    stepNumber = 3,
                    title = "Retención de Impuestos",
                    description = "Ingresa el porcentaje que la plataforma te retiene para calcular tu ganancia real.",
                    placeholder = "Ej: 10.1",
                    suffixText = "%",
                    onNextClick = { impuesto ->
                        val impuestoFloat = impuesto.toFloatOrNull() ?: 0f
                        sharedPreferences.edit { putFloat("impuesto", impuestoFloat) }
                        navController.navigate("step4_gain")
                    }
                )
            }

            composable("step4_gain") {
                FilterConfigStepScreen(
                    stepNumber = 4,
                    title = "Ganancia Neta",
                    description = "Ingresa cuánto esperas ganar como mínimo por cada kilómetro recorrido.",
                    placeholder = "Ej: 7.00",
                    prefixText = "$",
                    onNextClick = { ganancia ->
                        val gananciaFloat = ganancia.toFloatOrNull() ?: 0f
                        sharedPreferences.edit { putFloat("ganancia", gananciaFloat) }

                        val tarifaMin = sharedPreferences.getFloat("tarifaMin", 0f).toDouble()
                        val distMax = sharedPreferences.getFloat("distMax", 0f).toDouble()
                        val impuesto = sharedPreferences.getFloat("impuesto", 0f).toDouble()
                        val gananciaNeta = gananciaFloat.toDouble()

                        actualizarConfiguracionEnFirestore(tarifaMin, impuesto, distMax, gananciaNeta) { exito ->
                            if (exito) {
                                Toast.makeText(context, "¡Configuración guardada en la nube!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Guardado local. Hubo un error de red.", Toast.LENGTH_SHORT).show()
                            }
                            navController.navigate("setup_complete") { popUpTo("login") { inclusive = true } }
                        }
                    }
                )
            }

            composable("setup_complete") {
                SetupCompleteScreen(
                    onGoToHomeClick = {
                        navController.navigate("home") { popUpTo("login") { inclusive = true } }
                    }
                )
            }

            composable("home") {
                val scope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect

                    // Primero leemos el estado actual
                    obtenerEstadoSuscripcion { estado ->
                        // Solo restauramos compras si YA NO está en prueba
                        // (usuario que reinstaló o que ya había comprado antes)
                        if (estado.tipoPlan != "prueba") {
                            val billingManager = BillingManager(
                                context = context,
                                coroutineScope = scope,
                                onPurchaseSuccess = { },
                                getCurrentUserId = { uid },
                                onPurchaseError = { }
                            )

                            billingManager.iniciarConexion {
                                billingManager.restaurarCompras()
                            }
                        }
                    }
                }
                // ========================================

                HomeScreen(
                    isServiceRunning = isServiceRunning,
                    onSetServiceRunning = { isServiceRunning = it },
                    onNavigateToPlataformas = {
                        tabSeleccionada = 1
                        navController.navigate("switch") {
                            popUpTo("home")
                        }
                    },
                    onNavigateToZonas = {
                        navController.navigate("zonas_peligrosas")
                    },
                    onNavigateToSuscripcion = {
                        navController.navigate("planes")
                    }
                )
            }

            composable("switch") {
                SwitchScreen(
                    isServiceRunning = isServiceRunning,
                    onSetServiceRunning = { isRunning ->
                        isServiceRunning = isRunning
                        if (!isRunning) {
                            val intent = Intent(context, FloatingService::class.java)
                            context.stopService(intent)
                        }
                    },
                    onSolicitarPermiso = {
                        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
                        screenCaptureLauncher.launch(captureIntent)
                    },
                    onNavigateToSuscripcion = {
                        navController.navigate("planes")
                    } // <--- Esta es la línea que faltaba conectar aquí
                )
            }

            composable("settings") {
                SettingsScreen(
                    isLoggedIn = true,
                    onSetServiceRunning = { isServiceRunning = it },
                    onLogoutClick = { navController.navigate("login") { popUpTo(0) } },
                    onUpgradePlanClick = { navController.navigate("planes") },
                    onManageSubscriptionClick = { navController.navigate("detalle_suscripcion") },
                    onHelpClick = { navController.navigate("centro_ayuda") }
                )
            }

            composable("centro_ayuda") {
                CentroAyudaScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("planes") {
                PlanesSuscripcionScreen(onBackClick = { navController.popBackStack() })
            }

            composable("detalle_suscripcion") {
                // 1. Estados para guardar los datos reales de Firebase
                var tipoPlan by remember { mutableStateOf("mensual") }
                var fechaRenovacionTexto by remember { mutableStateOf("Cargando...") }
                var cargando by remember { mutableStateOf(true) }

                // 2. Consultamos Firestore al entrar a la pantalla
                LaunchedEffect(Unit) {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        FirebaseFirestore.getInstance()
                            .collection("Usuarios")
                            .document(uid)
                            .get()
                            .addOnSuccessListener { document ->
                                // Leemos si es anual o mensual
                                tipoPlan = document.getString("tipoPlan") ?: "mensual"

                                // Formateamos la fecha para que se vea bonita (ej: 12 de Agosto, 2026)
                                val timestamp = document.getTimestamp("fechaVencimiento")
                                if (timestamp != null) {
                                    val fecha = timestamp.toDate()
                                    fechaRenovacionTexto = SimpleDateFormat(
                                        "dd 'de' MMMM, yyyy",
                                        Locale("es", "MX")
                                    ).format(fecha)
                                } else {
                                    fechaRenovacionTexto = "Fecha no disponible"
                                }
                                cargando = false
                            }
                            .addOnFailureListener {
                                cargando = false
                            }
                    } else {
                        cargando = false
                    }
                }

                // 3. Mostramos un circulito de carga mientras trae los datos
                if (cargando) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF0052CC))
                    }
                } else {
                    // 4. Asignamos el precio y el ID según lo que dijo Firebase
                    // (Ojo: Cambia el $899.00 por el precio real que le pusiste a tu plan anual en Google Play)
                    val precio = if (tipoPlan == "anual") "$899.00" else "$99.00"
                    val idProducto = if (tipoPlan == "anual") "aceptakm_anual" else "aceptakm_mensual"

                    DetalleSuscripcionScreen(
                        estado = SuscripcionEstado(
                            isActive = true,
                            precioMes = precio,
                            fechaRenovacion = fechaRenovacionTexto,
                            productId = idProducto // ¡Ahora pasará el ID correcto a Google Play si quieren cancelar!
                        ),
                        onBackClick = { navController.popBackStack() },
                        onCambiarPlanClick = { navController.navigate("planes") },
                        onCancelarClick = { productId ->
                            val url = "https://play.google.com/store/account/subscriptions?sku=$productId&package=com.mavacode.aceptakm"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    )
                }
            }

            composable("zonas_peligrosas") {
                ZonasPeligrosasScreen()
            }
        }
    }
}