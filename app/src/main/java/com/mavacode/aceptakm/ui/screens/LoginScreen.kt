package com.mavacode.aceptakm.ui.screens

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.mavacode.aceptakm.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

enum class LoginStep {
    CHOOSE_METHOD, ENTER_PHONE, ENTER_EMAIL, ENTER_CODE
}

@Composable
fun LoginScreen(
    onLoginSuccessClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val coroutineScope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }

    var isLoading by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(LoginStep.CHOOSE_METHOD) }
    var inputValue by remember { mutableStateOf("") }      // teléfono o email
    var password by remember { mutableStateOf("") }       // solo correo
    var verificationCode by remember { mutableStateOf("") }

    var storedVerificationId by remember { mutableStateOf("") }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }

    val callbacks = remember {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                coroutineScope.launch {
                    try {
                        auth.signInWithCredential(credential).await()
                        isLoading = false
                        onLoginSuccessClick()
                    } catch (e: Exception) {
                        isLoading = false
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                isLoading = false
                Log.e("AceptaKmAuth", "Error enviando SMS", e)
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                isLoading = false
                storedVerificationId = verificationId
                resendToken = token
                currentStep = LoginStep.ENTER_CODE
            }
        }
    }

    val webClientId = "573327644880-duj2m9n7tijk589nqm5fcvq9tanb1q8f.apps.googleusercontent.com"
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = false
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                coroutineScope.launch {
                    try {
                        val credential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(credential).await()
                        onLoginSuccessClick()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error Firebase: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(context, "No se obtuvo idToken", Toast.LENGTH_LONG).show()
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Error Google: ${e.statusCode}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun loginConCorreo() {
        val email = inputValue.trim()
        val pass = password
        if (email.isBlank() || pass.length < 6) {
            Toast.makeText(context, "Correo y contraseña (mín. 6 caracteres)", Toast.LENGTH_SHORT).show()
            return
        }
        isLoading = true
        coroutineScope.launch {
            try {
                // Intenta iniciar sesión
                auth.signInWithEmailAndPassword(email, pass).await()
                isLoading = false
                onLoginSuccessClick()
            } catch (e: Exception) {
                // Si no existe, crea la cuenta
                try {
                    auth.createUserWithEmailAndPassword(email, pass).await()
                    // Opcional: enviar verificación
                    auth.currentUser?.sendEmailVerification()
                    isLoading = false
                    Toast.makeText(context, "Cuenta creada. Revisa tu correo si pedimos verificación.", Toast.LENGTH_LONG).show()
                    onLoginSuccessClick()
                } catch (e2: Exception) {
                    isLoading = false
                    Toast.makeText(context, "Error: ${e2.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

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
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (currentStep != LoginStep.CHOOSE_METHOD) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        IconButton(onClick = {
                            currentStep = LoginStep.CHOOSE_METHOD
                            inputValue = ""
                            password = ""
                            verificationCode = ""
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextGray)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(primaryBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TwoWheeler,
                        contentDescription = "aceptakm Logo",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "AceptaKm",
                    color = primaryBlue,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                val subtitleText = when (currentStep) {
                    LoginStep.CHOOSE_METHOD -> "Tu compañero de ruta confiable.\nElige cómo iniciar sesión."
                    LoginStep.ENTER_PHONE -> "Ingresa tu número de 10 dígitos (México).\nTe enviaremos un código SMS."
                    LoginStep.ENTER_EMAIL -> "Ingresa tu correo y contraseña.\nSi no tienes cuenta, se creará automáticamente."
                    LoginStep.ENTER_CODE -> "Ingresa el código de 6 dígitos\nenviado a:\n$inputValue"
                }

                Text(
                    text = subtitleText,
                    color = TextGray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                when (currentStep) {
                    LoginStep.CHOOSE_METHOD -> {
                        AuthButton(
                            text = "Continuar con Google",
                            icon = "G",
                            iconColor = Color(0xFF4285F4),
                            isLoading = isLoading,
                            onClick = {
                                isLoading = true
                                googleSignInClient.signOut().addOnCompleteListener {
                                    launcher.launch(googleSignInClient.signInIntent)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        AuthButton(
                            text = "Continuar con Teléfono",
                            iconVector = Icons.Outlined.Phone,
                            isLoading = false,
                            onClick = { currentStep = LoginStep.ENTER_PHONE }
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        AuthButton(
                            text = "Continuar con Correo",
                            iconVector = Icons.Outlined.Email,
                            isLoading = false,
                            onClick = { currentStep = LoginStep.ENTER_EMAIL }
                        )
                    }

                    LoginStep.ENTER_PHONE -> {
                        OutlinedTextField(
                            value = inputValue,
                            onValueChange = {
                                if (it.length <= 10 && it.all { c -> c.isDigit() }) inputValue = it
                            },
                            label = { Text("Número a 10 dígitos (sin +52)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBlue,
                                focusedLabelColor = primaryBlue
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                isLoading = true
                                // México: +52 + 10 dígitos
                                val phoneNumber = "+52$inputValue"
                                val options = PhoneAuthOptions.newBuilder(auth)
                                    .setPhoneNumber(phoneNumber)
                                    .setTimeout(60L, TimeUnit.SECONDS)
                                    .setActivity(activity)
                                    .setCallbacks(callbacks)
                                    .build()
                                PhoneAuthProvider.verifyPhoneNumber(options)
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = inputValue.length == 10 && !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                        ) {
                            Text("Enviar código SMS")
                        }
                    }

                    LoginStep.ENTER_EMAIL -> {
                        OutlinedTextField(
                            value = inputValue,
                            onValueChange = { inputValue = it },
                            label = { Text("Correo electrónico") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBlue,
                                focusedLabelColor = primaryBlue
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña (mín. 6 caracteres)") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBlue,
                                focusedLabelColor = primaryBlue
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { loginConCorreo() },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = inputValue.isNotBlank() && password.length >= 6 && !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Entrar / Crear cuenta")
                            }
                        }
                    }

                    LoginStep.ENTER_CODE -> {
                        OutlinedTextField(
                            value = verificationCode,
                            onValueChange = {
                                if (it.length <= 6 && it.all { c -> c.isDigit() }) verificationCode = it
                            },
                            label = { Text("Código de 6 dígitos") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBlue,
                                focusedLabelColor = primaryBlue
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                isLoading = true
                                val credential = PhoneAuthProvider.getCredential(storedVerificationId, verificationCode)
                                coroutineScope.launch {
                                    try {
                                        auth.signInWithCredential(credential).await()
                                        isLoading = false
                                        onLoginSuccessClick()
                                    } catch (e: Exception) {
                                        isLoading = false
                                        Toast.makeText(context, "Código incorrecto", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = verificationCode.length == 6 && !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                        ) {
                            Text("Verificar y Entrar")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                val annotatedString = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = TextGray)) { append("Al continuar, aceptas nuestros ") }
                    pushStringAnnotation(tag = "TOS", annotation = "TOS")
                    withStyle(style = SpanStyle(color = primaryBlue, fontWeight = FontWeight.Bold)) {
                        append("Términos de\nServicio")
                    }
                    pop()
                    withStyle(style = SpanStyle(color = TextGray)) { append(" y ") }
                    pushStringAnnotation(tag = "PRIVACY", annotation = "PRIVACY")
                    withStyle(style = SpanStyle(color = primaryBlue, fontWeight = FontWeight.Bold)) {
                        append("Política de Privacidad")
                    }
                    pop()
                    withStyle(style = SpanStyle(color = TextGray)) { append(".") }
                }

                ClickableText(
                    text = annotatedString,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    ),
                    onClick = { /* TODO: abrir TOS / Privacy */ }
                )
            }
        }
    }
}

@Composable
fun AuthButton(
    text: String,
    icon: String? = null,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconColor: Color = textDark,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, outlineGray.copy(alpha = 0.5f)),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = primaryBlue, strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Procesando...", color = textDark, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Text(icon, color = iconColor, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                } else if (iconVector != null) {
                    Icon(iconVector, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text, color = textDark, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}