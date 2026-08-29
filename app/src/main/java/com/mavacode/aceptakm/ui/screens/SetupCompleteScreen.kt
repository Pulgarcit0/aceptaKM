package com.mavacode.aceptakm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mavacode.aceptakm.ui.theme.*

@Composable
fun SetupCompleteScreen(
    onGoToHomeClick: () -> Unit
) {


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgSurface)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // --- ILUSTRACIÓN CENTRAL CON INSIGNIAS FLOTANTES ---
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Círculo de fondo (azul muy claro)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(Color(0xFFE5EEFF).copy(alpha = 0.5f), CircleShape)
            )

            // Círculo azul principal
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(primaryBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Listo",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }

            // Insignia Superior Derecha: "Ruta Lista"
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 10.dp, y = 10.dp)
                    .background(Color(0xFFE5EEFF), RoundedCornerShape(percent = 50))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Route, contentDescription = null, tint = primaryBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ruta Lista", color = textDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            // Insignia Inferior Izquierda: "Validado"
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-10).dp, y = (-20).dp)
                    .background(Color(0xFF4ADE80), RoundedCornerShape(percent = 50)) // Verde brillante
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Color(0xFF006947), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Validado", color = Color(0xFF006947), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- TEXTOS PRINCIPALES ---
        Text(
            text = "¡Felicidades, la\nconfiguración está\nlista!",
            color = textDark,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ya puedes iniciar el asistente y\nempezar a recibir las mejores rutas\npersonalizadas para tu estilo de\nconducción.",
            color = TextGray,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- BOTÓN IR AL INICIO ---
        Button(
            onClick = onGoToHomeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Ir al Inicio", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Ir al inicio",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- SECCIÓN aceptakm CORE ---
        Text(
            text = "aceptakm CORE V2.4",
            color = Color(0xFFC2C6D6),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tarjetas de estado
        CoreStatusCard(
            icon = Icons.Outlined.Person,
            title = "Perfil",
            subtitle = "Configurado",
            iconBgColor = Color(0xFFE5EEFF),
            iconColor = primaryBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        CoreStatusCard(
            icon = Icons.Outlined.Map,
            title = "Mapas",
            subtitle = "Offline Activos",
            iconBgColor = Color(0xFFE6F4EA),
            iconColor = Color(0xFF0F9D58)
        )

        Spacer(modifier = Modifier.height(12.dp))

        CoreStatusCard(
            icon = Icons.Outlined.Notifications,
            title = "Alertas",
            subtitle = "En Tiempo Real",
            iconBgColor = Color(0xFFFCE8E6),
            iconColor = Color(0xFFD93025)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// --- COMPOSABLE PARA LAS TARJETAS DE CORE ---
@Composable
fun CoreStatusCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconBgColor: Color,
    iconColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    color = Color(0xFF585F67),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF0B1C30),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}