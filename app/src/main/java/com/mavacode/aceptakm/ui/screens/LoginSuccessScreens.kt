package com.mavacode.aceptakm.ui.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.mavacode.aceptakm.ui.theme.TextGray
import com.mavacode.aceptakm.ui.theme.bgSurface
import com.mavacode.aceptakm.ui.theme.primaryBlue
import kotlinx.coroutines.launch

// --- PANTALLA 1: CÓDIGO VALIDADO ---

@Composable
fun CodeValidatedScreen(
    onContinueClick: () -> Unit, // Esto se ejecutará DESPUÉS de que el login de Google sea exitoso
    onDetailsClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    // Agregamos un estado de carga opcional para que el usuario sepa que está conectando
    var isLoading by remember { mutableStateOf(false) }

    SuccessScreenTemplate(
        title = "¡Código Validado!",
        subtitle = "Descuento del 10% aplicado a tu\npróxima moto.",
        showDivider = true,
        // Cambiamos un poco el texto si está cargando
        primaryButtonText = if (isLoading) "Conectando con Google..." else "Continuar al Dashboard",
        secondaryButtonText = "Ver detalles del beneficio",
        onPrimaryClick = {
            // Evitamos múltiples clics seguidos
            if (!isLoading) {
                coroutineScope.launch {
                    isLoading = true
                    // 1. Lanzamos el inicio de sesión con Google
                    val exito = iniciarSesionConGoogle(context)
                    isLoading = false

                    // 2. Si es exitoso, navegamos al Dashboard
                    if (exito) {
                        onContinueClick()
                    } else {
                        // Opcional: Mostrar un mensaje si falla el login o si el usuario cancela
                        Toast.makeText(context, "No se pudo iniciar sesión con Google", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        },
        onSecondaryClick = onDetailsClick
    )
}

// --- PANTALLA 2: CONECTADO CON GOOGLE ---
@Composable
fun GoogleSuccessScreen(
    userName: String = "Valentín", // Puedes pasar el nombre que recuperes de Google aquí
    onContinueClick: () -> Unit
) {
    SuccessScreenTemplate(
        title = "¡Conectado con éxito!",
        subtitle = "Estamos preparando tu ruta,\n$userName...",
        showDivider = false,
        primaryButtonText = "Continuar a Configuración", // Agregado como pediste
        secondaryButtonText = null,
        onPrimaryClick = onContinueClick,
        onSecondaryClick = {}
    )
}

// --- PLANTILLA BASE PARA AMBAS PANTALLAS ---
// Como ambas pantallas son casi idénticas visualmente, usamos una plantilla reutilizable
@Composable
private fun SuccessScreenTemplate(
    title: String,
    subtitle: String,
    showDivider: Boolean,
    primaryButtonText: String,
    secondaryButtonText: String?,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgSurface)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- CÍRCULO AZUL CON PALOMITA ---
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFE5EEFF), CircleShape), // Fondo azul claro exterior
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(primaryBlue, CircleShape), // Círculo azul fuerte interior
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Éxito",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- TÍTULO ---
                Text(
                    text = title,
                    color = primaryBlue,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- SUBTÍTULO ---
                Text(
                    text = subtitle,
                    color = TextGray,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // --- DIVIDER OPCIONAL ---
                if (showDivider) {
                    HorizontalDivider(
                        modifier = Modifier
                            .width(64.dp)
                            .padding(bottom = 32.dp),
                        color = Color(0xFFC2C6D6).copy(alpha = 0.5f),
                        thickness = 3.dp
                    )
                }

                // --- BOTÓN PRINCIPAL ---
                Button(
                    onClick = onPrimaryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp), // Botón más redondeado (estilo pastilla)
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(primaryButtonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Continuar",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // --- BOTÓN SECUNDARIO (OPCIONAL) ---
                if (secondaryButtonText != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = onSecondaryClick) {
                        Text(
                            text = secondaryButtonText,
                            color = primaryBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// --- FUNCIÓN DE AUTENTICACIÓN CON GOOGLE (Credential Manager API) ---
suspend fun iniciarSesionConGoogle(context: Context): Boolean {
    return try {
        val credentialManager = CredentialManager.create(context)

        // IMPORTANTE: Aquí deberás poner tu Web Client ID de la consola de Google Cloud/Firebase
        // Por ahora pon un string vacío o tu ID real si ya lo tienes para que compile.
        val webClientId = "TU_WEB_CLIENT_ID_AQUI"

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // Lanza el diálogo nativo de Android para elegir la cuenta de Google
        val result = credentialManager.getCredential(context, request)

        Log.d("AuthGoogle", "Credencial obtenida exitosamente")

        // Aquí es donde tomaríamos result.credential y se lo pasaríamos a Firebase
        // Pero con esto, la función ya hace el trabajo real y devuelve true
        true

    } catch (e: Exception) {
        Log.e("AuthGoogle", "Error al iniciar sesión con Google", e)
        false
    }
}