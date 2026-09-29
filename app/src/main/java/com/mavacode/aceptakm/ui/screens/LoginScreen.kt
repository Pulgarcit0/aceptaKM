package com.mavacode.aceptakm.ui.screens

import android.app.Activity
import android.util.Log
import android.util.Patterns
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
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
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.mavacode.aceptakm.ui.theme.AceptaTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

enum class LoginStep {
    CHOOSE_METHOD, ENTER_PHONE, ENTER_EMAIL, ENTER_CODE
}

/** Mensajes en español por tipo de error de FirebaseAuth para el flujo de correo. */
private fun mensajeErrorAuthCorreo(e: Exception, registro: Boolean): String = when (e) {
    is FirebaseNetworkException -> "Sin conexión. Revisa tu internet."
    is FirebaseTooManyRequestsException -> "Demasiados intentos. Espera unos minutos."
    is FirebaseAuthUserCollisionException -> "Ese correo ya tiene cuenta. Inicia sesión."
    // WeakPassword hereda de InvalidCredentials: debe ir antes.
    is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil. Usa al menos 6 caracteres."
    is FirebaseAuthInvalidUserException ->
        if (e.errorCode == "ERROR_USER_DISABLED") "Esta cuenta está deshabilitada."
        else "No existe una cuenta con ese correo. Usa 'Crear cuenta'."
    is FirebaseAuthInvalidCredentialsException ->
        // Con la protección de enumeración de correos, un usuario inexistente llega también aquí,
        // por eso el mensaje de login es neutral.
        if (registro) "El correo no es válido." else "Correo o contraseña incorrectos."
    else -> "No pudimos completar la operación. Inténtalo de nuevo."
}

@Composable
fun LoginScreen(
    onLoginSuccessClick: () -> Unit,
    onNeedEmailVerification: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as Activity
    val coroutineScope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val cs = MaterialTheme.colorScheme
    val app = AceptaTheme.colors

    var isLoading by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(LoginStep.CHOOSE_METHOD) }
    var inputValue by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    var verificationCode by remember { mutableStateOf("") }

    var storedVerificationId by remember { mutableStateOf("") }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }

    val onSuccess by rememberUpdatedState(onLoginSuccessClick)
    val onNeedVerify by rememberUpdatedState(onNeedEmailVerification)

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = cs.primary,
        unfocusedBorderColor = app.outline,
        focusedLabelColor = cs.primary,
        unfocusedLabelColor = app.textSecondary,
        focusedTextColor = app.textPrimary,
        unfocusedTextColor = app.textPrimary,
        focusedContainerColor = app.card,
        unfocusedContainerColor = app.card,
        cursorColor = cs.primary
    )

    val callbacks = remember {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                coroutineScope.launch {
                    try {
                        auth.signInWithCredential(credential).await()
                        isLoading = false
                        onSuccess()
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
                        onSuccess()
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

    // Causa raíz: el flujo Entrar/Crear cuenta estaba fusionado en un solo try/catch; cualquier
    // fallo de signIn (usuario inexistente, typo, contraseña incorrecta, sin red, too many requests)
    // caía a createUserWithEmailAndPassword, creando cuentas basura y mostrando errores engañosos
    // de UserCollision. Ahora son dos acciones explícitas: iniciarSesionConCorreo() NUNCA crea
    // cuentas y registrarConCorreo() es la única que llama a createUserWithEmailAndPassword.
    // Además, onSuccess()/onNeedVerify() se ejecutan FUERA del try/catch de autenticación.

    // Devuelve el correo normalizado (trim + lowercase) o null (y avisa) si el formato no es válido.
    fun correoValidoONull(): String? {
        val email = inputValue.trim().lowercase()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Escribe un correo válido.", Toast.LENGTH_SHORT).show()
            return null
        }
        return email
    }

    // Tras autenticarse: si el correo no está verificado, envía verificación y va a verify_email.
    // No se bloquea la navegación esperando el envío; solo se avisa si el envío falla.
    fun continuarTrasAutenticar(recienRegistrado: Boolean) {
        val user = auth.currentUser
        if (user != null && !user.isEmailVerified) {
            user.sendEmailVerification().addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("AceptaKmAuth", "Error enviando verificación", task.exception)
                    Toast.makeText(
                        context,
                        "No pudimos enviar el correo de verificación. Inténtalo de nuevo más tarde.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            Toast.makeText(
                context,
                if (recienRegistrado) "Te enviamos un enlace. Confirma el correo para entrar."
                else "Confirma tu correo para continuar",
                Toast.LENGTH_LONG
            ).show()
            onNeedVerify()
        } else {
            onSuccess()
        }
    }

    fun iniciarSesionConCorreo() {
        val email = correoValidoONull() ?: return
        val pass = password
        if (pass.length < 6) {
            Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show()
            return
        }
        isLoading = true
        coroutineScope.launch {
            // Solo la llamada de autenticación va dentro del try/catch. Aquí NUNCA se crea usuario.
            val error: Exception? = try {
                auth.signInWithEmailAndPassword(email, pass).await()
                null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("AceptaKmAuth", "Error en signIn con correo", e)
                e
            }
            isLoading = false
            if (error != null) {
                Toast.makeText(context, mensajeErrorAuthCorreo(error, registro = false), Toast.LENGTH_LONG).show()
                return@launch
            }
            continuarTrasAutenticar(recienRegistrado = false)
        }
    }

    fun registrarConCorreo() {
        val email = correoValidoONull() ?: return
        val pass = password
        if (pass.length < 6) {
            Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show()
            return
        }
        if (pass != confirmPassword) {
            Toast.makeText(context, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show()
            return
        }
        isLoading = true
        coroutineScope.launch {
            val error: Exception? = try {
                auth.createUserWithEmailAndPassword(email, pass).await()
                null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("AceptaKmAuth", "Error en registro con correo", e)
                e
            }
            isLoading = false
            if (error != null) {
                Toast.makeText(context, mensajeErrorAuthCorreo(error, registro = true), Toast.LENGTH_LONG).show()
                return@launch
            }
            continuarTrasAutenticar(recienRegistrado = true)
        }
    }

    fun recuperarContrasena() {
        val email = correoValidoONull() ?: return
        isLoading = true
        coroutineScope.launch {
            val error: Exception? = try {
                auth.sendPasswordResetEmail(email).await()
                null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("AceptaKmAuth", "Error enviando restablecimiento", e)
                e
            }
            isLoading = false
            // Mensaje neutral para no revelar si el correo tiene cuenta.
            val msg = when (error) {
                null -> "Si el correo tiene cuenta, te enviamos un enlace para restablecer la contraseña."
                is FirebaseNetworkException -> "Sin conexión. Revisa tu internet."
                is FirebaseTooManyRequestsException -> "Demasiados intentos. Espera unos minutos."
                is FirebaseAuthInvalidUserException,
                is FirebaseAuthInvalidCredentialsException ->
                    "Si el correo tiene cuenta, te enviamos un enlace para restablecer la contraseña."
                else -> "No pudimos enviar el enlace. Inténtalo de nuevo."
            }
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

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
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (currentStep != LoginStep.CHOOSE_METHOD) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        IconButton(onClick = {
                            currentStep = LoginStep.CHOOSE_METHOD
                            inputValue = ""
                            password = ""
                            confirmPassword = ""
                            isRegisterMode = false
                            verificationCode = ""
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = app.textSecondary
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(cs.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TwoWheeler,
                        contentDescription = "AceptaKm Logo",
                        tint = cs.onPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "AceptaKm",
                    color = cs.primary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                val subtitleText = when (currentStep) {
                    LoginStep.CHOOSE_METHOD -> "Tu compañero de ruta confiable.\nElige cómo iniciar sesión."
                    LoginStep.ENTER_PHONE -> "Ingresa tu número de 10 dígitos (México).\nTe enviaremos un código SMS."
                    LoginStep.ENTER_EMAIL ->
                        if (isRegisterMode) "Crea tu cuenta con correo y contraseña.\nTe enviaremos un enlace para confirmar tu correo."
                        else "Ingresa tu correo y contraseña para iniciar sesión.\nSi aún no tienes cuenta, elige 'Crear cuenta'."
                    LoginStep.ENTER_CODE -> "Ingresa el código de 6 dígitos\nenviado a:\n$inputValue"
                }

                Text(
                    text = subtitleText,
                    color = app.textSecondary,
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
                            colors = fieldColors
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                isLoading = true
                                val phoneNumber = "+52$inputValue"
                                val options = PhoneAuthOptions.newBuilder(auth)
                                    .setPhoneNumber(phoneNumber)
                                    .setTimeout(60L, TimeUnit.SECONDS)
                                    .setActivity(activity)
                                    .setCallbacks(callbacks)
                                    .build()
                                PhoneAuthProvider.verifyPhoneNumber(options)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = inputValue.length == 10 && !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = cs.primary)
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
                            colors = fieldColors
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
                            colors = fieldColors
                        )

                        if (isRegisterMode) {
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirmar contraseña") },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = fieldColors
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (isRegisterMode) registrarConCorreo() else iniciarSesionConCorreo()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = inputValue.isNotBlank() && password.length >= 6 &&
                                (!isRegisterMode || confirmPassword.isNotBlank()) && !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = cs.primary)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = cs.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(if (isRegisterMode) "Crear cuenta" else "Iniciar sesión")
                            }
                        }

                        if (!isRegisterMode) {
                            TextButton(
                                onClick = { recuperarContrasena() },
                                enabled = !isLoading
                            ) {
                                Text("¿Olvidaste tu contraseña?", color = cs.primary)
                            }
                        }

                        TextButton(
                            onClick = {
                                isRegisterMode = !isRegisterMode
                                confirmPassword = ""
                            },
                            enabled = !isLoading
                        ) {
                            Text(
                                if (isRegisterMode) "¿Ya tienes cuenta? Iniciar sesión"
                                else "¿No tienes cuenta? Crear cuenta",
                                color = cs.primary
                            )
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
                            colors = fieldColors
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                isLoading = true
                                val credential = PhoneAuthProvider.getCredential(
                                    storedVerificationId,
                                    verificationCode
                                )
                                coroutineScope.launch {
                                    try {
                                        auth.signInWithCredential(credential).await()
                                        isLoading = false
                                        onSuccess()
                                    } catch (e: Exception) {
                                        isLoading = false
                                        Toast.makeText(context, "Código incorrecto", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = verificationCode.length == 6 && !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = cs.primary)
                        ) {
                            Text("Verificar y Entrar")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                val annotatedString = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = app.textSecondary)) { append("Al continuar, aceptas nuestros ") }
                    pushStringAnnotation(tag = "TOS", annotation = "TOS")
                    withStyle(style = SpanStyle(color = cs.primary, fontWeight = FontWeight.Bold)) {
                        append("Términos de\nServicio")
                    }
                    pop()
                    withStyle(style = SpanStyle(color = app.textSecondary)) { append(" y ") }
                    pushStringAnnotation(tag = "PRIVACY", annotation = "PRIVACY")
                    withStyle(style = SpanStyle(color = cs.primary, fontWeight = FontWeight.Bold)) {
                        append("Política de Privacidad")
                    }
                    pop()
                    withStyle(style = SpanStyle(color = app.textSecondary)) { append(".") }
                }

                ClickableText(
                    text = annotatedString,
                    style = TextStyle(
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    ),
                    onClick = { }
                )
            }
        }
    }
}

@Composable
fun AuthButton(
    text: String,
    icon: String? = null,
    iconVector: ImageVector? = null,
    iconColor: Color = MaterialTheme.colorScheme.onSurface,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val app = AceptaTheme.colors

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, app.outline),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = app.card),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = cs.primary, strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Procesando...", color = app.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
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
                Text(text, color = app.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}