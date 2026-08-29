package com.mavacode.aceptakm.model

data class UsuarioaceptaKM(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val fotoUrl: String = "",
    val fechaRegistro: String = "",

    // Suscripción
    val isPremium: Boolean = false,
    val tipoPlan: String = "gratis",
    val fechaVencimiento: String = "",

    // Configuración
    val tarifaMinima: Double = 0.0,
    val retencionImpuestos: Double = 0.0,
    val distanciaMaxima: Double = 0.0,
    val gananciaNetaPorKm: Double = 0.0,

    // --- CAMBIO CLAVE: Ahora soporta múltiples plataformas ---
    val plataformasActivas: List<String> = listOf("Didi Moto/Auto"),

    // Referidos
    val codigoReferido: String = "",
    val referidosExitosos: Int = 0,
    val telefono: String = "" // Es buena práctica tener un valor por defecto ("") aquí
)