package com.mavacode.aceptakm.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mavacode.aceptakm.feature.asistente.FloatingService
import com.mavacode.aceptakm.ui.theme.AceptaTheme
import com.mavacode.aceptakm.ui.theme.TextOrangeColor
import com.mavacode.aceptakm.ui.theme.TextRedColor
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun SettingsScreen(
    isLoggedIn: Boolean = false,
    onSetServiceRunning: (Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    onManageSubscriptionClick: () -> Unit,
    onUpgradePlanClick: () -> Unit,
    onHelpClick: () -> Unit,
) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme
    val app = AceptaTheme.colors

    var userName by remember { mutableStateOf("Cargando perfil...") }
    var userPhotoUrl by remember { mutableStateOf<String?>(null) }
    var calificacionMostrada by remember { mutableStateOf("4.95") }

    var tipoPlan by remember { mutableStateOf("cargando") }
    var esPremium by remember { mutableStateOf(false) }
    var fechaVencimientoTexto by remember { mutableStateOf("") }
    var diasRestantes by remember { mutableStateOf(0) }
    var tieneAcceso by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            userName = "Conductor AceptaKm"
            tipoPlan = "gratis"
            return@LaunchedEffect
        }

        userPhotoUrl = currentUser.photoUrl?.toString()
        userName = currentUser.displayName?.trim().orEmpty().ifBlank { "Cargando perfil..." }

        FirebaseFirestore.getInstance()
            .collection("Usuarios")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                val nombreFirestore = document.getString("nombre")?.trim().orEmpty()
                val nombreAuth = currentUser.displayName?.trim().orEmpty()
                userName = when {
                    nombreFirestore.isNotBlank() -> nombreFirestore
                    nombreAuth.isNotBlank() -> nombreAuth
                    else -> "Conductor AceptaKm"
                }

                if (!document.exists()) {
                    tipoPlan = "gratis"
                    return@addOnSuccessListener
                }

                tipoPlan = document.getString("tipoPlan") ?: "gratis"
                esPremium = document.getBoolean("premium") ?: false

                val timestamp = document.getTimestamp("fechaVencimiento")
                if (timestamp != null) {
                    val fecha = timestamp.toDate()
                    fechaVencimientoTexto = SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale("es", "MX")
                    ).format(fecha)

                    val diff = fecha.time - System.currentTimeMillis()
                    diasRestantes = if (diff > 0) {
                        TimeUnit.MILLISECONDS.toDays(diff).toInt()
                    } else {
                        0
                    }
                }

                tieneAcceso = esPremium || (tipoPlan == "prueba" && diasRestantes > 0)
            }
            .addOnFailureListener {
                userName = currentUser.displayName?.trim().orEmpty().ifBlank { "Conductor AceptaKm" }
                tipoPlan = "gratis"
                tieneAcceso = false
            }
    }

    val tituloMembresia: String
    val mensajeMembresia: String
    val colorMembresia: Color
    val textoBoton: String

    when {
        esPremium -> {
            tituloMembresia = "MEMBRESÍA PREMIUM"
            mensajeMembresia = "Próxima renovación: $fechaVencimientoTexto"
            colorMembresia = cs.primary
            textoBoton = "Gestionar Suscripción"
        }
        tipoPlan == "prueba" && diasRestantes > 0 -> {
            tituloMembresia = "PRUEBA GRATUITA"
            mensajeMembresia = "Te quedan $diasRestantes días restantes."
            colorMembresia = TextOrangeColor
            textoBoton = "Adquirir Premium"
        }
        tipoPlan == "cargando" -> {
            tituloMembresia = "VERIFICANDO PLAN..."
            mensajeMembresia = "Consultando estado de tu cuenta."
            colorMembresia = app.textSecondary
            textoBoton = "Cargando..."
        }
        else -> {
            tituloMembresia = "PLAN EXPIRADO"
            mensajeMembresia = "Tu acceso ha concluido. Actualiza a Premium para seguir usando filtros y zonas."
            colorMembresia = TextRedColor
            textoBoton = "Renovar Membresía"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = userPhotoUrl,
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(cs.surfaceVariant),
                contentScale = ContentScale.Crop,
                fallback = rememberVectorPainter(Icons.Default.AccountCircle),
                error = rememberVectorPainter(Icons.Default.AccountCircle)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = userName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = cs.onBackground
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Calificación",
                        tint = cs.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = calificacionMostrada,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = cs.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(Calificación)",
                        fontSize = 14.sp,
                        color = app.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = app.card),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.WorkspacePremium,
                        contentDescription = null,
                        tint = colorMembresia,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tituloMembresia,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorMembresia,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = mensajeMembresia,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = app.textPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (esPremium) {
                            onManageSubscriptionClick()
                        } else {
                            onUpgradePlanClick()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorMembresia,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = textoBoton, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = app.cardAlt),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(app.iconBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CardGiftcard,
                            contentDescription = null,
                            tint = Color(0xFF4ADE80)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Recomendar Aplicación",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = app.textPrimary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Invita a tus amigos a usar AceptaKm y obtén descuentos exclusivos en tu próxima renovación.",
                    fontSize = 14.sp,
                    color = app.textSecondary,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "¡Prueba AceptaKm! La mejor herramienta para optimizar tus viajes. Descárgala ya."
                            )
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Compartir con..."))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        tint = cs.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Compartir Código",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = cs.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection(
            items = listOf(
                SettingsItem("Notificaciones", Icons.Outlined.Notifications) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                SettingsItem("Idioma (Español)", Icons.Outlined.Language) {
                    Toast.makeText(context, "El idioma se gestiona desde el sistema", Toast.LENGTH_SHORT).show()
                },
                SettingsItem("Seguridad", Icons.Outlined.Security) {
                    Toast.makeText(context, "Opciones de seguridad en construcción", Toast.LENGTH_SHORT).show()
                }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(
            items = listOf(
                SettingsItem("Centro de Ayuda", Icons.Outlined.HelpOutline) {
                    onHelpClick()
                },
                SettingsItem("Términos y Condiciones", Icons.Outlined.Description) {
                    val url = "https://sites.google.com/d/1N_CkqobmSghpmG-DuonFwEreDTtXeDF9/p/1eXK4EwRZenuxm67aNoHJByWykUcI0n_8/edit"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = {
                val stopIntent = Intent(context, FloatingService::class.java)
                context.stopService(stopIntent)
                onSetServiceRunning(false)
                FirebaseAuth.getInstance().signOut()
                onLogoutClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, app.outline),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextRedColor)
        ) {
            Icon(
                imageVector = Icons.Outlined.Logout,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Cerrar Sesión", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

data class SettingsItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit = {}
)

@Composable
fun SettingsSection(items: List<SettingsItem>) {
    val app = AceptaTheme.colors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = app.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { item.onClick() }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = app.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = item.title,
                            fontSize = 16.sp,
                            color = app.textPrimary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Ir",
                        tint = app.textSecondary
                    )
                }

                if (index < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = app.divider,
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}