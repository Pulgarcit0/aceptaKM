package com.mavacode.aceptakm.ui.screens
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mavacode.aceptakm.ui.theme.colorBackground
import com.mavacode.aceptakm.ui.theme.colorIconBg
import com.mavacode.aceptakm.ui.theme.colorPrimary
import com.mavacode.aceptakm.ui.theme.colorTextPrimary
import com.mavacode.aceptakm.ui.theme.colorTextSecondary
import com.mavacode.aceptakm.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleBeneficioScreen(
    onEntendidoClick: () -> Unit
) {


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalles del Beneficio",
                        fontWeight = FontWeight.Bold,
                        color = colorTextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorBackground)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorBackground)
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = onEntendidoClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Entendido", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
        },
        containerColor = colorBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta Principal con Gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFE0F7FA), Color(0xFFE8EAF6))
                        )
                    )
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Contenedor de la imagen y el badge del 10%
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_background), // Reemplazar por tu imagen de moto
                            contentDescription = "Moto",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White),
                            contentScale = ContentScale.Crop
                        )
                        // Badge 10%
                        Box(
                            modifier = Modifier
                                .offset(x = 12.dp, y = 12.dp)
                                .size(48.dp)
                                .background(colorPrimary, CircleShape)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "10%",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "10% de descuento",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "en tu próxima moto o suscripción a través de la plataforma.",
                        fontSize = 15.sp,
                        color = colorTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Lista de Detalles
            BeneficioItem(
                icon = Icons.Default.CalendarToday,
                title = "Vigencia",
                description = "Válido por 30 días a partir de hoy.",
                iconBgColor = colorIconBg,
                iconColor = colorPrimary
            )

            // Corrección del padding aquí
            HorizontalDivider(
                modifier = Modifier
                    .padding(start = 64.dp)
                    .padding(vertical = 8.dp),
                color = Color(0xFFE0E0E0)
            )

            BeneficioItem(
                icon = Icons.Default.Stars,
                title = "Aplicación",
                description = "El descuento se aplicará automáticamente en tu siguiente compra o suscripción de vehículo a través de la plataforma.",
                iconBgColor = colorIconBg,
                iconColor = colorPrimary
            )

            // Corrección del padding aquí también
            HorizontalDivider(
                modifier = Modifier
                    .padding(start = 64.dp)
                    .padding(vertical = 8.dp),
                color = Color(0xFFE0E0E0)
            )

            BeneficioItem(
                icon = Icons.Default.Info,
                title = "Condiciones",
                description = "No acumulable con otras promociones. Válido solo para conductores activos.",
                iconBgColor = colorIconBg,
                iconColor = colorPrimary
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun BeneficioItem(icon: ImageVector, title: String, description: String, iconBgColor: Color, iconColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(iconBgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0B1C30))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 14.sp, color = Color(0xFF5E656D), lineHeight = 20.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetalleBeneficioPreview() {
    DetalleBeneficioScreen(onEntendidoClick = {})
}