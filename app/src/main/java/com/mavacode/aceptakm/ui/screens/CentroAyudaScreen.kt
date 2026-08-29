package com.mavacode.aceptakm.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CentroAyudaScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("aceptaKM Centro de Ayuda", color = Color(0xFF0052CC), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color(0xFF0052CC))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF9FAFB))
            )
        },
        containerColor = Color(0xFFF9FAFB)
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

            // --- HEADER Y BÚSQUEDA ---
            Text(
                text = "¿Cómo podemos ayudarte hoy?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0052CC),
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
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tendencias
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Text("Tendencias: ", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                ChipAyuda("Activar GPS")
                Spacer(modifier = Modifier.width(4.dp))
                ChipAyuda("Métodos de pago")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- CATEGORÍAS ---
            Text(
                text = "Explorar por categorías",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
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

            // --- PREGUNTAS FRECUENTES ---
            Text(
                text = "Preguntas Frecuentes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Encuentra respuestas rápidas a las dudas más comunes.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            FAQItem("¿Cómo activo el asistente por voz durante la ruta?")
            FAQItem("¿Por qué se rechazó mi código de invitación?")
            FAQItem("¿Cómo cambio mi método de pago predeterminado?")

            Spacer(modifier = Modifier.height(32.dp))

            // --- BANNER DE SOPORTE (TELEGRAM / WHATSAPP) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2563EB)) // Azul fuerte
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¿Necesitas más ayuda?",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nuestro equipo de soporte está disponible 24/7 para ayudarte con cualquier inconveniente técnico o duda sobre el servicio.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón WhatsApp
                    Button(
                        onClick = {
                            val url = "https://chat.whatsapp.com/JiSCGFLZFshAQqY9KVaDcB?s=cl&p=a&ilr=0&amv=0" // <-- CAMBIA ESTO
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            try { context.startActivity(intent) } catch (e: Exception) {
                                Toast.makeText(context, "WhatsApp no instalado", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF2563EB))
                    ) {
                        Icon(Icons.Outlined.Chat, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Comunidad de WhatsApp", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón Telegram
                    OutlinedButton(
                        onClick = {
                            val url = "https://t.me/+uyhcKsrmErBhZmMx" // <-- CAMBIA ESTO
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            try { context.startActivity(intent) } catch (e: Exception) {
                                Toast.makeText(context, "Telegram no instalado", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
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
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFE0E7FF))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = texto, color = Color(0xFF4338CA), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun CategoriaCard(icono: ImageVector, titulo: String, subtitulo: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icono, contentDescription = null, tint = Color(0xFF0052CC))
            Spacer(modifier = Modifier.height(12.dp))
            Text(titulo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitulo, fontSize = 12.sp, color = Color.Gray, lineHeight = 14.sp)
        }
    }
}

@Composable
fun FAQItem(pregunta: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(pregunta, fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.weight(1f))
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Expandir", tint = Color(0xFF0052CC))
        }
    }
}