package com.mavacode.aceptakm.core.parser


data class DatosExtraidos(
    val tarifaBruta: Double,
    val kmRecogida: Double,
    val kmViaje: Double
)

object ExtractorUber {

    
    private val regexPrecio = "\\\$([0-9]+(?:\\.[0-9]{1,2})?)".toRegex()

    
    private val regexRecogida = "A\\s+\\d+\\s+min\\s+y\\s+\\(([0-9.]+)\\s+km\\)".toRegex()

    
    private val regexViaje = "Viaje:\\s+\\d+\\s+min\\s+\\(([0-9.]+)\\s+km\\)".toRegex()

    /**
     * @param textoPantalla Todo el texto crudo que el servicio de accesibilidad logre leer
     * @return Los 3 datos limpios, o 'null' si no detectó que sea una alerta de viaje válida
     */
    fun extraerDatos(textoPantalla: String): DatosExtraidos? {
        try {
            
            val matchPrecio = regexPrecio.find(textoPantalla)
            val matchRecogida = regexRecogida.find(textoPantalla)
            val matchViaje = regexViaje.find(textoPantalla)

            
            if (matchPrecio != null && matchRecogida != null && matchViaje != null) {

                
                val precio = matchPrecio.groupValues[1].toDouble()
                val kmRecogida = matchRecogida.groupValues[1].toDouble()
                val kmViaje = matchViaje.groupValues[1].toDouble()

                return DatosExtraidos(
                    tarifaBruta = precio,
                    kmRecogida = kmRecogida,
                    kmViaje = kmViaje
                )
            }
        } catch (e: Exception) {
            
            e.printStackTrace()
        }

        
        return null
    }
}