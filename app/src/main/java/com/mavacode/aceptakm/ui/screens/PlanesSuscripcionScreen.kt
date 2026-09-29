package com.mavacode.aceptakm.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mavacode.aceptakm.data.BillingManager
import com.mavacode.aceptakm.ui.theme.AceptaTheme
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanesSuscripcionScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme
    val app = AceptaTheme.colors

    var planSeleccionado by remember { mutableStateOf("mensual") }
    var isLoading by remember { mutableStateOf(false) }

    var currentPlan by remember { mutableStateOf<String?>(null) }
    var oldPurchaseToken by remember { mutableStateOf<String?>(null) }
    var fechaVencimiento by remember { mutableStateOf<Date?>(null) }
    var cargandoDatos by remember { mutableStateOf(true) }

    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }

    val billingManager = remember {
        BillingManager(
            context = context,
            coroutineScope = coroutineScope,
            onPurchaseSuccess = {
                Toast.makeText(context, "¡Suscripción activada/actualizada!", Toast.LENGTH_LONG).show()
                isLoading = false
                onBackClick()
            },
            getCurrentUserId = { FirebaseAuth.getInstance().currentUser?.uid },
            onPurchaseError = { msg ->
                isLoading = false
                if (msg != "cancelado") {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        billingManager.iniciarConexion {}

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance()
                .collection("Usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    currentPlan = document.getString("tipoPlan")
                    oldPurchaseToken = document.getString("purchaseToken")
                    fechaVencimiento = document.getTimestamp("fechaVencimiento")?.toDate()
                    if (currentPlan == "anual") {
                        planSeleccionado = "anual"
                    }
                    cargandoDatos = false
                }
                .addOnFailureListener { cargandoDatos = false }
        } else {
            cargandoDatos = false
        }
    }

    DisposableEffect(Unit) {
        onDispose { billingManager.cerrar() }
    }

    val planActivo = remember(currentPlan, fechaVencimiento) {
        val fecha = fechaVencimiento
        when {
            currentPlan == "prueba" || currentPlan == "gratis" || currentPlan == null -> false
            fecha == null -> false
            else -> fecha.time > System.currentTimeMillis()
        }
    }

    val esMismoPlan = currentPlan == planSeleccionado && planActivo
    val esCambioDePlan = planActivo && currentPlan != planSeleccionado

    if (mostrarDialogoConfirmacion) {
        val esUpgrade = planSeleccionado == "anual"

        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmacion = false },
            title = {
                Text(
                    text = "Confirmar Cambio de Plan",
                    fontWeight = FontWeight.Bold,
                    color = app.textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = if (esUpgrade) {
                            "Al cambiar al plan Anual, Google Play calculará los días que no usaste de tu mes actual y te los descontará del nuevo cobro. El cambio será inmediato."
                        } else {
                            "Al cambiar al plan Mensual, seguirás disfrutando de tu plan Anual hasta que termine el periodo ya pagado. Después comenzarás a pagar la tarifa mensual."
                        },
                        fontSize = 14.sp,
                        color = app.textSecondary,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = {
                            val uri = Uri.parse("https://sites.google.com/view/aceptakm?usp=sharing")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Más información sobre cobros", fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoConfirmacion = false
                        if (activity != null) {
                            val productId =
                                if (planSeleccionado == "mensual") "aceptakm_mensual" else "aceptakm_anual"
                            isLoading = true
                            billingManager.lanzarCobroSuscripcion(
                                activity = activity,
                                productId = productId,
                                oldPurchaseToken = if (esCambioDePlan) oldPurchaseToken else null
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = cs.primary)
                ) {
                    Text("Aceptar y Continuar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoConfirmacion = false }) {
                    Text("Cancelar", color = app.textSecondary)
                }
            },
            containerColor = app.card
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Elige tu Plan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cs.background,
                    titleContentColor = cs.onBackground,
                    navigationIconContentColor = cs.onBackground
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cs.surface)
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = {
                        if (activity == null) return@Button

                        if (esCambioDePlan) {
                            mostrarDialogoConfirmacion = true
                        } else {
                            val productId =
                                if (planSeleccionado == "mensual") "aceptakm_mensual" else "aceptakm_anual"
                            isLoading = true
                            billingManager.lanzarCobroSuscripcion(
                                activity = activity,
                                productId = productId,
                                oldPurchaseToken = null
                            )
                        }
                    },
                    enabled = !isLoading && !esMismoPlan && !cargandoDatos,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (esMismoPlan) app.textSecondary else cs.primary
                    )
                ) {
                    if (isLoading || cargandoDatos) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = cs.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else if (esMismoPlan) {
                        Text("Tu plan actual", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text(
                            text = when {
                                esCambioDePlan -> "Cambiar a ${if (planSeleccionado == "mensual") "Mensual" else "Anual"}"
                                planSeleccionado == "mensual" -> "Iniciar prueba gratis (Mensual)"
                                else -> "Pagar \$699 / año"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        containerColor = cs.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = "Premium",
                tint = Color(0xFFFFB300),
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Desbloquea todo el poder",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = cs.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Disfruta de 7 días gratis en tu plan mensual. Filtros ilimitados y cálculo de ganancias en tiempo real.",
                fontSize = 14.sp,
                color = app.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            PlanCard(
                title = "Mensual (7 días gratis)",
                price = "$99.00",
                period = "/ mes",
                isSelected = planSeleccionado == "mensual",
                onClick = { planSeleccionado = "mensual" },
                badge = "PRUEBA GRATIS"
            )

            Spacer(modifier = Modifier.height(16.dp))

            PlanCard(
                title = "Anual (Ahorras 25%)",
                price = "$699",
                period = "/ año",
                isSelected = planSeleccionado == "anual",
                onClick = { planSeleccionado = "anual" },
                badge = "MEJOR VALOR"
            )

            Spacer(modifier = Modifier.height(32.dp))

            BeneficioRow("Escaneo de viajes ilimitado")
            BeneficioRow("Cálculo de impuestos automático")
            BeneficioRow("Filtro por zona y distancia")
            BeneficioRow("Sin anuncios")
        }
    }
}

@Composable
fun PlanCard(
    title: String,
    price: String,
    period: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    badge: String? = null
) {
    val cs = MaterialTheme.colorScheme
    val app = AceptaTheme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) cs.primary.copy(alpha = 0.12f) else app.card
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) cs.primary else app.outline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = if (isSelected) cs.primary else Color.Transparent,
                        shape = CircleShape
                    )
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = cs.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(app.card, CircleShape)
                            .border(2.dp, app.outline, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = app.textPrimary
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF0F9D58), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = price,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = cs.primary
                    )
                    Text(
                        text = " $period",
                        fontSize = 14.sp,
                        color = app.textSecondary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BeneficioRow(texto: String) {
    val app = AceptaTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            tint = Color(0xFF4ADE80),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = texto, fontSize = 14.sp, color = app.textSecondary)
    }
}