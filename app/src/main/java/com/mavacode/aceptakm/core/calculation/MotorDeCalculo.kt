package com.mavacode.aceptakm.core.calculation

import kotlin.math.round


data class ResultadoViaje(
    val esRentable: Boolean,
    val gananciaNeta: Double,
    val pagoPorKm: Double,
    val distanciaTotal: Double,
    val motivoRechazo: String? = null 
)

object MotorDeCalculo {

    /**
     * @param tarifaBrutaPantalla Lo que cobró Uber/DiDi (ej. 157.65)
     * @param kmRecogida Distancia hacia el pasajero (ej. 0.3)
     * @param kmViaje Distancia del viaje en sí (ej. 5.9)
     * @param impuestoRetencion Tu configuración de HomeScreen (ej. 10.1%)
     * @param gananciaNetaDeseada Tu configuración de HomeScreen (ej. 7.0 $/km)
     * @param distanciaMaxima Tu configuración (ej. 12.0 km)
     * @param tarifaMinima Tu configuración (ej. 25.0 $)
     */
    fun evaluarViaje(
        tarifaBrutaPantalla: Double,
        kmRecogida: Double,
        kmViaje: Double,
        impuestoRetencion: Double,
        gananciaNetaDeseada: Double,
        distanciaMaxima: Double,
        tarifaMinima: Double
    ): ResultadoViaje {

        
        val distanciaTotal = kmRecogida + kmViaje

        
        if (distanciaTotal <= 0.0) {
            return ResultadoViaje(false, 0.0, 0.0, 0.0, "Error: Distancia 0")
        }

        
        val descuentoImpuesto = tarifaBrutaPantalla * (impuestoRetencion / 100)
        val gananciaNeta = tarifaBrutaPantalla - descuentoImpuesto

        
        val pagoPorKm = gananciaNeta / distanciaTotal

        
        var esRentable = true
        var motivoRechazo: String? = null

        if (distanciaTotal > distanciaMaxima) {
            esRentable = false
            motivoRechazo = "Excede distancia máx"
        } else if (gananciaNeta < tarifaMinima) {
            esRentable = false
            motivoRechazo = "No llega a tarifa mín"
        } else if (pagoPorKm < gananciaNetaDeseada) {
            esRentable = false
            motivoRechazo = "Paga muy poco por km"
        }

        
        return ResultadoViaje(
            esRentable = esRentable,
            gananciaNeta = redondear(gananciaNeta),
            pagoPorKm = redondear(pagoPorKm),
            distanciaTotal = redondear(distanciaTotal),
            motivoRechazo = motivoRechazo
        )
    }

    
    private fun redondear(valor: Double): Double {
        return round(valor * 100) / 100
    }
}