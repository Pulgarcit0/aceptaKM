package com.mavacode.aceptakm.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mavacode.aceptakm.ui.theme.AceptaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CentroAyudaScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme
    val app = AceptaTheme.colors
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "AceptaKm Centro de Ayuda",
                        color = cs.primary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = cs.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cs.background,
                    titleContentColor = cs.primary
                )
            )
        },
        containerColor = cs.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "¿Cómo podemos ayudarte hoy?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = cs.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Busca rutas, pagos, problemas...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = app.card,
                    focusedContainerColor = app.card,
                    focusedBorderColor = cs.primary,
                    unfocusedBorderColor = app.outline,
                    focusedTextColor = app.textPrimary,
                    unfocusedTextColor = app.textPrimary,
                    cursorColor = cs.primary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tendencias: ", fontSize = 12.sp, color = app.textSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                ChipAyuda("Activar GPS")
                Spacer(modifier = Modifier.width(4.dp))
                ChipAyuda("Métodos de pago")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Explorar por categorías",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = cs.onBackground,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CategoriaCard(Icons.Outlined.Person, "Mi Cuenta", "Perfil, seguridad y preferencias.", Modifier.weight(1f))
                CategoriaCard(Icons.Outlined.CreditCard, "Pagos y Facturación", "Historial, tarjetas y cupones.", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CategoriaCard(Icons.Outlined.Map, "Rutas y Mapas", "Navegación y modo offline.", Modifier.weight(1f))
                CategoriaCard(Icons.Outlined.Build, "Soporte Técnico", "Errores de app y dispositivos.", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Preguntas Frecuentes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = cs.onBackground,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Encuentra respuestas rápidas a las dudas más comunes.",
                fontSize = 14.sp,
                color = app.textSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            FAQItem("¿Cómo activo el asistente por voz durante la ruta?")
            FAQItem("¿Por qué se rechazó mi código de invitación?")
            FAQItem("¿Cómo cambio mi método de pago predeterminado?")

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cs.primary)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¿Necesitas más ayuda?",
                        color = cs.onPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nuestro equipo de soporte está disponible 24/7 para ayudarte con cualquier inconveniente técnico o duda sobre el servicio.",
                        color = cs.onPrimary.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val url = "https://chat.whatsapp.com/JiSCGFLZFshAQqY9KVaDcB?s=cl&p=a&ilr=0&amv=0"
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            } catch (e: Exception) {
                                Toast.makeText(context, "WhatsApp no instalado", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cs.onPrimary,
                            contentColor = cs.primary
                        )
                    ) {
                        Icon(Icons.Outlined.Chat, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Comunidad de WhatsApp", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            val url = "https://t.me/+uyhcKsrmErBhZmMx"
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Telegram no instalado", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.onPrimary),
                        border = BorderStroke(1.dp, cs.onPrimary.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Outlined.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Soporte en Telegram", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ChipAyuda(texto: String) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(cs.primary.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = texto,
            color = cs.primary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun CategoriaCard(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val app = AceptaTheme.colors
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = app.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icono, contentDescription = null, tint = cs.primary)
            Spacer(modifier = Modifier.height(12.dp))
            Text(titulo, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = app.textPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitulo, fontSize = 12.sp, color = app.textSecondary, lineHeight = 14.sp)
        }
    }
}

@Composable
fun FAQItem(pregunta: String) {
    val cs = MaterialTheme.colorScheme
    val app = AceptaTheme.colors
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = app.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, app.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                pregunta,
                fontSize = 14.sp,
                color = app.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Expandir", tint = cs.primary)
        }
    }
}