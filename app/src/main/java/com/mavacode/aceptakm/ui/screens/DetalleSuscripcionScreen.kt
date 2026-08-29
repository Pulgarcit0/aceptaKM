package com.mavacode.aceptakm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Motorcycle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mavacode.aceptakm.ui.theme.*

// --- 1. CLASE DE ESTADO (Para recibir los datos reales) ---
data class SuscripcionEstado(
    val isActive: Boolean = false,
    val precioMes: String = "$0.00",
    val fechaRenovacion: String = "No disponible",
    val productId: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleSuscripcionScreen(
    estado: SuscripcionEstado,
    onBackClick: () -> Unit,
    onCambiarPlanClick: () -> Unit,
    onCancelarClick: (String) -> Unit
) {
    Scaffold(
        containerColor = LightBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi Suscripción",
                        color = MainBlueColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.DarkGray)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Notificaciones */ }) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notificaciones", tint = Color.DarkGray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // --- TARJETA AZUL PRINCIPAL ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MainBlueColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Etiqueta de Activo / Inactivo dinámica
                        Row(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (estado.isActive) ActiveGreen else TextGray)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (estado.isActive) "Activo" else "Inactivo",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Ícono de moto
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Motorcycle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Membresía\nPremium",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        // Precio dinámico
                        Text(
                            text = estado.precioMes,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        // Cambiamos el texto dinámicamente dependiendo del producto
                        val peridoTexto = if (estado.productId == "aceptakm_anual") " /año" else " /mes"
                        Text(
                            text = peridoTexto,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- DETALLES DE FACTURACIÓN ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "DETALLES DE FACTURACIÓN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Próxima renovación dinámica
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(LightBackground, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Event, contentDescription = null, tint = MainBlueColor)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = "Próxima renovación", fontSize = 12.sp, color = TextGray)
                            Text(text = estado.fechaRenovacion, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- BENEFICIOS PREMIUM ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "TUS BENEFICIOS PREMIUM",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    BeneficioItem(
                        titulo = "Zonas Peligrosas",
                        descripcion = "Alertas y bloqueo de colonias o municipios de alto riesgo."
                    )
                    BeneficioItem(
                        titulo = "Filtros Personalizados",
                        descripcion = "Define tu tarifa mínima, distancia y ganancia exacta por viaje."
                    )
                    BeneficioItem(
                        titulo = "Cálculo Inteligente",
                        descripcion = "Conoce tu ganancia real restando los impuestos al instante."
                    )
                    BeneficioItem(
                        titulo = "Recepción en Segundo Plano",
                        descripcion = "La oruga lee tus viajes incluso mientras usas otras apps."
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- BOTONES INFERIORES CONECTADOS ---
            Button(
                onClick = { onCambiarPlanClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MainBlueColor)
            ) {
                Text(text = "Cambiar Plan", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { onCancelarClick(estado.productId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Cancelar Suscripción",
                    color = Color(0xFFDC2626), // Rojo
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun BeneficioItem(titulo: String, descripcion: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(LightGreenIconBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = DarkGreenIcon,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = titulo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = descripcion, fontSize = 13.sp, color = TextGray)
        }
    }
}