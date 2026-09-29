package com.mavacode.aceptakm.ui.screens

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.edit
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mavacode.aceptakm.data.BillingManager
import com.mavacode.aceptakm.data.remote.actualizarConfiguracionEnFirestore
import com.mavacode.aceptakm.data.remote.crearUsuarioEnFirestore
import com.mavacode.aceptakm.data.remote.obtenerEstadoSuscripcion
import com.mavacode.aceptakm.feature.asistente.FloatingService
import com.mavacode.aceptakm.feature.permisos.rememberScreenCaptureLauncher
import com.mavacode.aceptakm.ui.theme.AceptaTheme
import com.mavacode.aceptakm.ui.theme.ThemeMode
import com.mavacode.aceptakm.ui.theme.ThemePrefs
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AceptaKm(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {}
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val sharedPreferences = context.getSharedPreferences("aceptakmPrefs", Context.MODE_PRIVATE)
    var tabSeleccionada by remember { mutableIntStateOf(0) }

    val cs = MaterialTheme.colorScheme
    val app = AceptaTheme.colors

    val mediaProjectionManager =
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
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

    val iconoOscuro = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    var menuTema by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val pantallasConMenu = listOf("home", "switch", "settings")
    val mostrarMenus = currentRoute in pantallasConMenu

    LaunchedEffect(currentRoute) {
        tabSeleccionada = when (currentRoute) {
            "home" -> 0
            "switch" -> 1
            "settings" -> 2
            else -> tabSeleccionada
        }
    }

    val itemColors = NavigationBarItemDefaults.colors(
        indicatorColor = cs.primary.copy(alpha = 0.18f),
        selectedIconColor = cs.primary,
        selectedTextColor = cs.primary,
        unselectedIconColor = app.textSecondary,
        unselectedTextColor = app.textSecondary
    )

    fun necesitaVerificarCorreo(): Boolean {
        val user = FirebaseAuth.getInstance().currentUser ?: return false
        val esCorreo = !user.email.isNullOrBlank() &&
                user.providerData.any { it.providerId == "password" }
        return esCorreo && !user.isEmailVerified
    }

    fun irSegunConfiguracion(desde: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            navController.navigate("login") { popUpTo(desde) { inclusive = true } }
            return
        }
        if (necesitaVerificarCorreo()) {
            navController.navigate("verify_email") {
                popUpTo(desde) { inclusive = true }
            }
            return
        }
        FirebaseFirestore.getInstance()
            .collection("Usuarios")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                val yaConfiguro = doc.getDouble("tarifaMinima") != null
                val destino = if (yaConfiguro) "home" else "google_success"
                navController.navigate(destino) {
                    popUpTo(desde) { inclusive = true }
                }
            }
            .addOnFailureListener {
                navController.navigate("google_success")
            }
    }

    Scaffold(
        containerColor = cs.background,
        topBar = {
            if (mostrarMenus) {
                TopAppBar(
                    title = {
                        Text("AceptaKm", fontWeight = FontWeight.Bold, color = cs.primary)
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { menuTema = true }) {
                                Icon(
                                    imageVector = if (iconoOscuro) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = "Tema",
                                    tint = cs.onSurface
                                )
                            }
                            DropdownMenu(
                                expanded = menuTema,
                                onDismissRequest = { menuTema = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Claro") },
                                    leadingIcon = { Icon(Icons.Default.LightMode, contentDescription = null) },
                                    onClick = {
                                        ThemePrefs.set(context, ThemeMode.LIGHT)
                                        onThemeModeChange(ThemeMode.LIGHT)
                                        menuTema = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Oscuro") },
                                    leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                                    onClick = {
                                        ThemePrefs.set(context, ThemeMode.DARK)
                                        onThemeModeChange(ThemeMode.DARK)
                                        menuTema = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Igual que el sistema") },
                                    leadingIcon = { Icon(Icons.Default.BrightnessAuto, contentDescription = null) },
                                    onClick = {
                                        ThemePrefs.set(context, ThemeMode.SYSTEM)
                                        onThemeModeChange(ThemeMode.SYSTEM)
                                        menuTema = false
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = cs.surface,
                        titleContentColor = cs.primary,
                        actionIconContentColor = cs.onSurface
                    )
                )
            }
        },
        bottomBar = {
            if (mostrarMenus) {
                NavigationBar(containerColor = cs.surface) {
                    NavigationBarItem(
                        selected = tabSeleccionada == 0,
                        onClick = {
                            tabSeleccionada = 0
                            navController.navigate("home") { popUpTo("home") { inclusive = true } }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = itemColors
                    )
                    NavigationBarItem(
                        selected = tabSeleccionada == 1,
                        onClick = {
                            tabSeleccionada = 1
                            navController.navigate("switch") { popUpTo("home") }
                        },
                        icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "Switch") },
                        label = { Text("Switch") },
                        colors = itemColors
                    )
                    NavigationBarItem(
                        selected = tabSeleccionada == 2,
                        onClick = {
                            tabSeleccionada = 2
                            navController.navigate("settings") { popUpTo("home") }
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        colors = itemColors
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
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user == null) {
                        navController.navigate("login") { popUpTo("splash") { inclusive = true } }
                    } else {
                        irSegunConfiguracion("splash")
                    }
                }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = cs.primary)
                }
            }

            composable("login") {
                LoginScreen(
                    onLoginSuccessClick = { irSegunConfiguracion("login") },
                    onNeedEmailVerification = {
                        navController.navigate("verify_email") {
                            popUpTo("login") { inclusive = false }
                        }
                    }
                )
            }

            composable("verify_email") {
                VerifyEmailScreen(
                    onVerified = { irSegunConfiguracion("verify_email") },
                    onBackToLogin = {
                        navController.navigate("login") { popUpTo(0) }
                    }
                )
            }

            composable("google_success") {
                GoogleSuccessScreen(
                    onContinueClick = {
                        crearUsuarioEnFirestore { exito ->
                            if (exito) {
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
                        sharedPreferences.edit { putFloat("tarifaMin", tarifa.toFloatOrNull() ?: 0f) }
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
                        sharedPreferences.edit { putFloat("distMax", distancia.toFloatOrNull() ?: 0f) }
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
                        sharedPreferences.edit { putFloat("impuesto", impuesto.toFloatOrNull() ?: 0f) }
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

                        actualizarConfiguracionEnFirestore(
                            sharedPreferences.getFloat("tarifaMin", 0f).toDouble(),
                            sharedPreferences.getFloat("impuesto", 0f).toDouble(),
                            sharedPreferences.getFloat("distMax", 0f).toDouble(),
                            gananciaFloat.toDouble()
                        ) { exito ->
                            Toast.makeText(
                                context,
                                if (exito) "¡Configuración guardada en la nube!" else "Guardado local. Hubo un error de red.",
                                Toast.LENGTH_SHORT
                            ).show()
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
                    obtenerEstadoSuscripcion { estado ->
                        if (estado.tipoPlan != "prueba") {
                            val billingManager = BillingManager(
                                context = context,
                                coroutineScope = scope,
                                onPurchaseSuccess = { },
                                getCurrentUserId = { uid },
                                onPurchaseError = { }
                            )
                            billingManager.iniciarConexion { billingManager.restaurarCompras() }
                        }
                    }
                }

                HomeScreen(
                    isServiceRunning = isServiceRunning,
                    onSetServiceRunning = { isServiceRunning = it },
                    onNavigateToPlataformas = {
                        tabSeleccionada = 1
                        navController.navigate("switch") { popUpTo("home") }
                    },
                    onNavigateToZonas = { navController.navigate("zonas_peligrosas") },
                    onNavigateToSuscripcion = { navController.navigate("planes") }
                )
            }

            composable("switch") {
                SwitchScreen(
                    isServiceRunning = isServiceRunning,
                    onSetServiceRunning = { isRunning ->
                        isServiceRunning = isRunning
                        if (!isRunning) {
                            context.stopService(Intent(context, FloatingService::class.java))
                        }
                    },
                    onSolicitarPermiso = {
                        screenCaptureLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
                    },
                    onNavigateToSuscripcion = { navController.navigate("planes") }
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
                CentroAyudaScreen(onBackClick = { navController.popBackStack() })
            }

            composable("planes") {
                PlanesSuscripcionScreen(onBackClick = { navController.popBackStack() })
            }

            composable("detalle_suscripcion") {
                var tipoPlan by remember { mutableStateOf("mensual") }
                var fechaRenovacionTexto by remember { mutableStateOf("Cargando...") }
                var cargando by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        FirebaseFirestore.getInstance()
                            .collection("Usuarios")
                            .document(uid)
                            .get()
                            .addOnSuccessListener { document ->
                                tipoPlan = document.getString("tipoPlan") ?: "mensual"
                                val timestamp = document.getTimestamp("fechaVencimiento")
                                fechaRenovacionTexto = if (timestamp != null) {
                                    SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "MX"))
                                        .format(timestamp.toDate())
                                } else {
                                    "Fecha no disponible"
                                }
                                cargando = false
                            }
                            .addOnFailureListener { cargando = false }
                    } else {
                        cargando = false
                    }
                }

                if (cargando) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = cs.primary)
                    }
                } else {
                    DetalleSuscripcionScreen(
                        estado = SuscripcionEstado(
                            isActive = true,
                            precioMes = if (tipoPlan == "anual") "$699.00" else "$99.00",
                            fechaRenovacion = fechaRenovacionTexto,
                            productId = if (tipoPlan == "anual") "aceptakm_anual" else "aceptakm_mensual"
                        ),
                        onBackClick = { navController.popBackStack() },
                        onCambiarPlanClick = { navController.navigate("planes") },
                        onCancelarClick = { productId ->
                            val url =
                                "https://play.google.com/store/account/subscriptions?sku=$productId&package=com.mavacode.aceptakm"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
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