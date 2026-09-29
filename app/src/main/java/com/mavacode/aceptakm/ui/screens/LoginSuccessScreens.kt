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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mavacode.aceptakm.ui.theme.AceptaTheme
import kotlinx.coroutines.launch

@Composable
fun CodeValidatedScreen(
    onContinueClick: () -> Unit,
    onDetailsClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    SuccessScreenTemplate(
        title = "¡Código Validado!",
        subtitle = "Descuento del 10% aplicado a tu\npróxima moto.",
        showDivider = true,
        primaryButtonText = if (isLoading) "Conectando con Google..." else "Continuar al Dashboard",
        secondaryButtonText = "Ver detalles del beneficio",
        extraContent = null,
        onPrimaryClick = {
            if (!isLoading) {
                coroutineScope.launch {
                    isLoading = true
                    val exito = iniciarSesionConGoogle(context)
                    isLoading = false
                    if (exito) {
                        onContinueClick()
                    } else {
                        Toast.makeText(context, "No se pudo iniciar sesión con Google", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        },
        onSecondaryClick = onDetailsClick
    )
}

@Composable
fun GoogleSuccessScreen(
    onContinueClick: () -> Unit
) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    var nombre by remember {
        mutableStateOf(user?.displayName?.trim().orEmpty())
    }
    var guardando by remember { mutableStateOf(false) }
    val pideNombre = nombre.trim().length < 2

    fun guardarYContinuar() {
        val nombreLimpio = nombre.trim()
        if (nombreLimpio.length < 2) {
            Toast.makeText(context, "Escribe tu nombre para continuar", Toast.LENGTH_SHORT).show()
            return
        }
        guardando = true

        val perfil = UserProfileChangeRequest.Builder()
            .setDisplayName(nombreLimpio)
            .build()

        val uid = user?.uid
        val seguir = {
            if (uid == null) {
                guardando = false
                onContinueClick()
            } else {
                FirebaseFirestore.getInstance()
                    .collection("Usuarios")
                    .document(uid)
                    .set(mapOf("nombre" to nombreLimpio), SetOptions.merge())
                    .addOnCompleteListener {
                        guardando = false
                        onContinueClick()
                    }
            }
        }

        if (user != null) {
            user.updateProfile(perfil).addOnCompleteListener { seguir() }
        } else {
            seguir()
        }
    }

    SuccessScreenTemplate(
        title = "¡Conectado con éxito!",
        subtitle = if (pideNombre) {
            "¿Cómo te llamas? Lo usamos en tu perfil."
        } else {
            "Estamos preparando tu ruta,\n${nombre.trim()}..."
        },
        showDivider = false,
        primaryButtonText = if (guardando) "Guardando..." else "Continuar a Configuración",
        secondaryButtonText = null,
        extraContent = {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Tu nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        },
        onPrimaryClick = { if (!guardando) guardarYContinuar() },
        onSecondaryClick = {}
    )
}

@Composable
private fun SuccessScreenTemplate(
    title: String,
    subtitle: String,
    showDivider: Boolean,
    primaryButtonText: String,
    secondaryButtonText: String?,
    extraContent: (@Composable () -> Unit)?,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val app = AceptaTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = app.card),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(app.iconBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(cs.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Éxito",
                            tint = cs.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = title,
                    color = cs.primary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = subtitle,
                    color = app.textSecondary,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )

                if (extraContent != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    extraContent()
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (showDivider) {
                    HorizontalDivider(
                        modifier = Modifier
                            .width(64.dp)
                            .padding(bottom = 32.dp),
                        color = app.divider,
                        thickness = 3.dp
                    )
                }

                Button(
                    onClick = onPrimaryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = cs.primary)
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

                if (secondaryButtonText != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = onSecondaryClick) {
                        Text(
                            text = secondaryButtonText,
                            color = cs.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

suspend fun iniciarSesionConGoogle(context: Context): Boolean {
    return try {
        val credentialManager = CredentialManager.create(context)
        val webClientId = "573327644880-duj2m9n7tijk589nqm5fcvq9tanb1q8f.apps.googleusercontent.com"

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        credentialManager.getCredential(context, request)
        Log.d("AuthGoogle", "Credencial obtenida exitosamente")
        true
    } catch (e: Exception) {
        Log.e("AuthGoogle", "Error al iniciar sesión con Google", e)
        false
    }
}