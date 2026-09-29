package com.mavacode.aceptakm.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.mavacode.aceptakm.ui.theme.AceptaTheme

@Composable
fun VerifyEmailScreen(
    onVerified: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme
    val app = AceptaTheme.colors
    var cargando by remember { mutableStateOf(false) }
    val email = FirebaseAuth.getInstance().currentUser?.email.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Confirma tu correo",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = app.textPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Te enviamos un enlace a:\n$email\n\nÁbrelo y luego pulsa Ya confirmé.",
            fontSize = 15.sp,
            color = app.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = {
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    onBackToLogin()
                    return@Button
                }
                cargando = true
                user.reload().addOnCompleteListener {
                    cargando = false
                    if (user.isEmailVerified) {
                        onVerified()
                    } else {
                        Toast.makeText(
                            context,
                            "Aún no está confirmado. Revisa la bandeja o spam.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            },
            enabled = !cargando,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (cargando) "Revisando..." else "Ya confirmé")
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = {
            FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
            Toast.makeText(context, "Correo reenviado", Toast.LENGTH_SHORT).show()
        }) {
            Text("Reenviar correo")
        }
        TextButton(onClick = {
            FirebaseAuth.getInstance().signOut()
            onBackToLogin()
        }) {
            Text("Usar otra cuenta")
        }
    }
}