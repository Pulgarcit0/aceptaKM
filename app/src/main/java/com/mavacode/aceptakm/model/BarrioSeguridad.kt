package com.mavacode.aceptakm.model

data class BarrioSeguridad(
    val id: String = "", // Un ID único
    val nombre: String = "", // Ej: "Santa Cruz Xoxocotlán" o "San Martín"
    val votosSeguro: Int = 0,
    val votosPrecaucion: Int = 0,
    val votosPeligroso: Int = 0,
    val nivelRiesgo: String = "DESCONOCIDO" // Puede ser "SEGURO", "PRECAUCION", o "PELIGROSO"
)