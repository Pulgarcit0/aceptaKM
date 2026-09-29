package com.mavacode.aceptakm.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

// =====================================================
// MODELO DE ESTADO DE SUSCRIPCIÓN
// =====================================================
data class EstadoSuscripcion(
    val isActive: Boolean = false,
    val tipoPlan: String = "gratis",          // "prueba", "mensual", "anual", "gratis"
    val fechaVencimiento: Date? = null,
    val diasRestantes: Int = 0,
    val premium: Boolean = false
)

// =====================================================
// FUNCIONES EXISTENTES (se mantienen)
// =====================================================

fun verificarSiUsuarioExisteEnFirebase(onResultado: (Boolean) -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user == null) {
        onResultado(false)
        return
    }

    FirebaseFirestore.getInstance()
        .collection("Usuarios")
        .document(user.uid)
        .get()
        .addOnSuccessListener { document ->
            onResultado(document.exists())
        }
        .addOnFailureListener {
            onResultado(false)
        }
}

fun validarCodigoEnFirebase(codigoIngresado: String, onResultado: (Boolean, String) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("cupones")
        .whereEqualTo("codigo", codigoIngresado.uppercase())
        .whereEqualTo("activo", true)
        .get()
        .addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                onResultado(false, "Ups... Código inválido")
                return@addOnSuccessListener
            }

            val document = documents.documents[0]
            val id = document.id
            val limite = document.getLong("limiteUso") ?: 50L
            val actuales = document.getLong("usosActuales") ?: 0L

            if (actuales < limite) {
                db.collection("cupones").document(id)
                    .update("usosActuales", actuales + 1)
                    .addOnSuccessListener {
                        if (actuales + 1 >= limite) {
                            db.collection("cupones").document(id).update("activo", false)
                        }
                        onResultado(true, "¡Código aplicado con éxito! Tienes 20% de descuento.")
                    }
                    .addOnFailureListener {
                        onResultado(false, "Error de red al registrar el cupón.")
                    }
            } else {
                onResultado(false, "Lo sentimos, este cupón ya alcanzó el límite de los 50 usuarios.")
            }
        }
        .addOnFailureListener {
            onResultado(false, "Error al conectar con el servidor.")
        }
}

fun crearUsuarioEnFirestore(onComplete: (Boolean) -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
        onComplete(false)
        return
    }

    val db = FirebaseFirestore.getInstance()
    val usuarioRef = db.collection("Usuarios").document(currentUser.uid)
    val nombre = currentUser.displayName?.trim().orEmpty().ifBlank { "Socio" }

    val datosPerfil = mapOf(
        "uid" to currentUser.uid,
        "nombre" to nombre,
        "email" to (currentUser.email ?: ""),
        "telefono" to (currentUser.phoneNumber ?: ""),
        "fotoUrl" to (currentUser.photoUrl?.toString() ?: "")
    )

    usuarioRef.get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                usuarioRef.set(datosPerfil, SetOptions.merge())
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(true) }
                return@addOnSuccessListener
            }

            val fechaActual = Date()
            val calendar = Calendar.getInstance().apply {
                time = fechaActual
                add(Calendar.DAY_OF_YEAR, 7)
            }

            val nuevoUsuario = datosPerfil + mapOf(
                "fechaRegistro" to fechaActual,
                "fechaVencimiento" to calendar.time,
                "tipoPlan" to "prueba",
                "premium" to false
            )

            usuarioRef.set(nuevoUsuario)
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        }
        .addOnFailureListener {
            onComplete(false)
        }
}
fun actualizarConfiguracionEnFirestore(
    tarifaMin: Double,
    impuesto: Double,
    distMax: Double,
    ganancia: Double,
    onComplete: (Boolean) -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
        onComplete(false)
        return
    }

    val updates = mapOf(
        "tarifaMinima" to tarifaMin,
        "retencionImpuestos" to impuesto,
        "distanciaMaxima" to distMax,
        "gananciaNetaPorKm" to ganancia
    )

    FirebaseFirestore.getInstance()
        .collection("Usuarios")
        .document(currentUser.uid)
        .set(updates, SetOptions.merge())
        .addOnSuccessListener { onComplete(true) }
        .addOnFailureListener { onComplete(false) }
}

// =====================================================
// FUNCIONES DE SUSCRIPCIÓN (CORREGIDAS)
// =====================================================

/**
 * Consulta UNA vez el estado actual de la suscripción.
 */
fun obtenerEstadoSuscripcion(onResultado: (EstadoSuscripcion) -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user == null) {
        onResultado(EstadoSuscripcion(isActive = false, tipoPlan = "gratis"))
        return
    }

    FirebaseFirestore.getInstance()
        .collection("Usuarios")
        .document(user.uid)
        .get()
        .addOnSuccessListener { document ->
            if (!document.exists()) {
                onResultado(EstadoSuscripcion(isActive = false, tipoPlan = "gratis"))
                return@addOnSuccessListener
            }

            val tipoPlan = document.getString("tipoPlan") ?: "gratis"
            val premium = document.getBoolean("premium") ?: false
            val timestamp = document.getTimestamp("fechaVencimiento")
            val fecha = timestamp?.toDate()

            val diasRestantes = if (fecha != null && fecha.time > System.currentTimeMillis()) {
                TimeUnit.MILLISECONDS.toDays(fecha.time - System.currentTimeMillis()).toInt()
            } else {
                0
            }

            // ✅ Lógica correcta restaurada: La fecha manda siempre.
            val isActive = when {
                tipoPlan == "prueba" -> diasRestantes > 0
                tipoPlan == "mensual" || tipoPlan == "anual" -> {
                    fecha != null && fecha.time > System.currentTimeMillis()
                }
                else -> false
            }

            onResultado(
                EstadoSuscripcion(
                    isActive = isActive,
                    tipoPlan = tipoPlan,
                    fechaVencimiento = fecha,
                    diasRestantes = diasRestantes,
                    premium = premium
                )
            )
        }
        .addOnFailureListener {
            onResultado(EstadoSuscripcion(isActive = false, tipoPlan = "gratis"))
        }
}

/**
 * Observa en tiempo real el estado de la suscripción.
 */
fun observarEstadoSuscripcion(onUpdate: (EstadoSuscripcion) -> Unit): ListenerRegistration? {
    val user = FirebaseAuth.getInstance().currentUser
    if (user == null) {
        onUpdate(EstadoSuscripcion(isActive = false, tipoPlan = "gratis"))
        return null
    }

    return FirebaseFirestore.getInstance()
        .collection("Usuarios")
        .document(user.uid)
        .addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) {
                onUpdate(EstadoSuscripcion(isActive = false, tipoPlan = "gratis"))
                return@addSnapshotListener
            }

            val tipoPlan = snapshot.getString("tipoPlan") ?: "gratis"
            val premium = snapshot.getBoolean("premium") ?: false
            val timestamp = snapshot.getTimestamp("fechaVencimiento")
            val fecha = timestamp?.toDate()

            val diasRestantes = if (fecha != null && fecha.time > System.currentTimeMillis()) {
                TimeUnit.MILLISECONDS.toDays(fecha.time - System.currentTimeMillis()).toInt()
            } else {
                0
            }

            // ✅ Lógica correcta restaurada: La fecha manda siempre.
            val isActive = when {
                tipoPlan == "prueba" -> diasRestantes > 0
                tipoPlan == "mensual" || tipoPlan == "anual" -> {
                    fecha != null && fecha.time > System.currentTimeMillis()
                }
                else -> false
            }

            onUpdate(
                EstadoSuscripcion(
                    isActive = isActive,
                    tipoPlan = tipoPlan,
                    fechaVencimiento = fecha,
                    diasRestantes = diasRestantes,
                    premium = premium
                )
            )
        }
}
