package com.mavacode.aceptakm.core.parser

import android.content.Context
import android.util.Log
import com.mavacode.aceptakm.feature.asistente.NivelRentabilidad
import com.mavacode.aceptakm.feature.asistente.TripData

object ViajeParser {

    fun analizarTexto(
        context: Context,
        texto: String,
        plataformasActivas: Set<String>,
        zonasBloqueadas: List<String>
    ): TripData? {

        if (texto.isBlank()) return null

        val t = texto.lowercase()

        // --- 0. FILTRO ANTI-ESPEJO E HISTORIAL (Basura) ---
        if (
            t.contains("ganancia neta") ||
            t.contains("aceptakm") ||
            t.contains("recibo") ||
            t.contains("factura") ||
            t.contains("viaje uberx con")
        ) {
            return null // Ignorar nuestra propia UI o historiales de viajes pasados
        }

        // --- 1. ¿Qué plataformas encendió el usuario? ---
        val uberActivo = plataformasActivas.any { it.equals("Uber Moto/Auto", true) }
        val didiActivo = plataformasActivas.any { it.equals("Didi Moto/Auto", true) }
        val inDriveActivo = plataformasActivas.any { it.equals("inDrive", true) }
        val didiFoodActivo = plataformasActivas.any { it.equals("Didi Food", true) }
        val uberEatsActivo = plataformasActivas.any { it.equals("Uber Eats", true) }
        val rappiActivo = plataformasActivas.any { it.equals("Rappi", true) }
        val cabifyActivo = plataformasActivas.any { it.equals("Cabify", true) }
        val lalamoveActivo = plataformasActivas.any { it.equals("Lalamove", true) }

        // --- 2. DICCIONARIO: Detectar qué app es (Señales Fuertes y depuradas) ---

        // Delivery
        val pareceEats = listOf("uber eats", "eats", "entrega", "garantizado", "ubereats", "agrega una entrega").any { t.contains(it) }
        val pareceDidiFood = listOf("didi food", "didifood", "aceptar este pedido", "tienda-usuario").any { t.contains(it) }
        val pareceRappi = t.contains("rappi")
        val pareceLalamove = t.contains("lalamove")

        // Transporte
        val pareceInDrive = listOf("indrive", "indriver", "pon tu precio", "punto(s)", "oferta del pasajero").any { t.contains(it) }
        val pareceCabify = t.contains("cabify")

        // Lógica precisa UBER Moto/Auto
        val pareceUber = (
                listOf("uberx", "uber xl", "uber moto", "interurbano", "comfort").any { t.contains(it) } ||
                        (t.contains("uber") && !t.contains("eats") && !t.contains("didi")) ||
                        (t.contains("viaje:") && (t.contains("a ") || t.contains("min")))
                ) && !pareceEats && !pareceInDrive && !t.contains("didi food")

        // Lógica precisa DIDI Moto/Auto
        val pareceDidi = (
                t.contains("didi") ||
                        Regex("""\d+\s*min\s*\(\s*[\d.,]+\s*(km|m)\s*\)""").containsMatchIn(t)
                ) && !pareceDidiFood && !pareceUber && !pareceInDrive


        // --- 3. PUERTA DE ENTRADA ESTRICTA (Early Exit) ---

        val plataformaDetectada: String
        val esDelivery: Boolean

        when {
            uberActivo && pareceUber -> { plataformaDetectada = "Uber Moto/Auto"; esDelivery = false }
            didiActivo && pareceDidi -> { plataformaDetectada = "Didi Moto/Auto"; esDelivery = false }
            inDriveActivo && pareceInDrive -> { plataformaDetectada = "inDrive"; esDelivery = false }
            uberEatsActivo && pareceEats -> { plataformaDetectada = "Uber Eats"; esDelivery = true }
            didiFoodActivo && pareceDidiFood -> { plataformaDetectada = "Didi Food"; esDelivery = true }
            rappiActivo && pareceRappi -> { plataformaDetectada = "Rappi"; esDelivery = true }
            cabifyActivo && pareceCabify -> { plataformaDetectada = "Cabify"; esDelivery = false }
            lalamoveActivo && pareceLalamove -> { plataformaDetectada = "Lalamove"; esDelivery = true }
            else -> return null // No coincide lo que hay en pantalla con lo que el usuario prendió
        }

        try {
            val montoDinamica = extraerMontoDinamica(texto)
            val factorDinamica = extraerFactorDinamica(texto)
            val precioBruto = extraerPrecio(texto, montoDinamica) ?: return null
            var distanciaTotal = 0.0
            var tiempoTotal = 0

            // --- 4. EXTRACCIÓN PRECISA POR PLATAFORMA ---
            when (plataformaDetectada) {

                "Uber Moto/Auto" -> {
                    // Ida: "A 10 min (3.0 km)" / "A 10 min y (3.0 km)"
                    val regexIda = Regex("""A\s+(\d+)\s*min(?:\s*y)?\s*\(?\s*([\d.,]+)\s*km\s*\)?""", RegexOption.IGNORE_CASE)
                    // Viaje: "Viaje: 22 min (10.1 km)" o "Viaje: 1 h 15 min (32.0 km)"
                    val regexViaje = Regex("""Viaje:\s*(?:(\d+)\s*h\s*)?(\d+)\s*min\s*\(?\s*([\d.,]+)\s*km\s*\)?""", RegexOption.IGNORE_CASE)

                    regexIda.find(texto)?.let {
                        tiempoTotal += it.groupValues[1].toInt()
                        distanciaTotal += it.groupValues[2].replace(",", ".").toDouble()
                    }

                    regexViaje.find(texto)?.let {
                        val horas = it.groupValues[1].toIntOrNull() ?: 0
                        tiempoTotal += (horas * 60) + it.groupValues[2].toInt()
                        distanciaTotal += it.groupValues[3].replace(",", ".").toDouble()
                    }

                    // Fallback
                    if (distanciaTotal == 0.0) distanciaTotal = extraerSumaKm(texto)
                    if (tiempoTotal == 0) tiempoTotal = extraerSumaMin(texto)
                }

                "Didi Moto/Auto" -> {
                    val regexDidi = Regex("""(\d+)\s*min\s*\(?\s*(\d+[.,]?\d*)\s*(km|m)\s*\)?""", RegexOption.IGNORE_CASE)
                    val matches = regexDidi.findAll(texto).toList()

                    if (matches.isNotEmpty()) {
                        for (match in matches) {
                            tiempoTotal += match.groupValues[1].toInt()
                            val unidad = match.groupValues[3].lowercase()
                            val valorDistancia = match.groupValues[2].replace(",", ".").toDouble()

                            if (unidad == "m") {
                                distanciaTotal += valorDistancia / 1000.0
                            } else {
                                distanciaTotal += valorDistancia
                            }
                        }
                    } else {
                        // Fallback
                        distanciaTotal = extraerSumaKm(texto)
                        tiempoTotal = extraerSumaMin(texto)
                    }
                }

                else -> {
                    // Genérica para inDrive, Cabify, Eats, Food, Rappi, Lalamove
                    distanciaTotal = extraerSumaKm(texto)
                    tiempoTotal = extraerSumaMin(texto)
                }
            }

            // --- 5. VALIDACIONES POST-EXTRACCIÓN ---

            // Si es delivery, perdonamos que la distancia sea 0 (le ponemos 1 km simbólico para no dividir entre 0)
            if (esDelivery && distanciaTotal <= 0.0) {
                distanciaTotal = 1.0
            } else if (!esDelivery && distanciaTotal <= 0.0) {
                return null
            }

            val dinamicaLog = when {
                montoDinamica != null && factorDinamica != null ->
                    " | dinámica +$$montoDinamica x$factorDinamica"
                montoDinamica != null -> " | dinámica +$$montoDinamica"
                factorDinamica != null -> " | dinámica x$factorDinamica"
                else -> ""
            }
            Log.d("aceptakm_OCR", "✅ Detectado [$plataformaDetectada] | $$precioBruto | ${distanciaTotal}km | ${tiempoTotal}min$dinamicaLog")

            // --- 6. CÁLCULOS FINANCIEROS Y DE CONFIGURACIÓN ---
            val prefs = context.getSharedPreferences("aceptakmPrefs", Context.MODE_PRIVATE)
            val tarifaMinConf = prefs.getFloat("tarifaMin", 35f).toDouble()
            val impuestoConf = prefs.getFloat("impuesto", 10.1f).toDouble() / 100.0 // Alineado a 10.1
            val distMaxConf = prefs.getFloat("distMax", 60f).toDouble()
            val gananciaPorKmConf = prefs.getFloat("ganancia", 8.5f).toDouble()

            val impuesto = precioBruto * impuestoConf
            val gananciaNeta = precioBruto - impuesto
            val pagoPorKm = gananciaNeta / distanciaTotal

            // --- 7. ZONAS PELIGROSAS ---
            val zonaPeligrosaDetectada = zonasBloqueadas.firstOrNull { zonaCruda ->
                val zonaLimpia = zonaCruda
                    .replace("Colonia:", "", ignoreCase = true)
                    .replace("Municipio/Viaje:", "", ignoreCase = true)
                    .trim()
                zonaLimpia.isNotBlank() && t.contains(zonaLimpia)
            }

            // --- 8. DECISIÓN DEL MOTOR ---
            val nivel: NivelRentabilidad
            val sugerenciaTexto: String

            when {
                zonaPeligrosaDetectada != null -> {
                    nivel = NivelRentabilidad.RECHAZAR
                    val nombreBonito = zonaPeligrosaDetectada
                        .replace("Colonia:", "", ignoreCase = true)
                        .replace("Municipio/Viaje:", "", ignoreCase = true)
                        .trim()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    sugerenciaTexto = "¡PELIGRO! $nombreBonito"
                }
                !esDelivery && distanciaTotal > distMaxConf -> {
                    nivel = NivelRentabilidad.RECHAZAR
                    sugerenciaTexto = "MUY LEJOS"
                }
                gananciaNeta < tarifaMinConf -> {
                    nivel = NivelRentabilidad.RECHAZAR
                    sugerenciaTexto = "TARIFA MUY BAJA"
                }
                pagoPorKm >= gananciaPorKmConf -> {
                    nivel = NivelRentabilidad.ALTA
                    sugerenciaTexto = "¡TÓMALO!"
                }
                pagoPorKm >= gananciaPorKmConf * 0.75 -> {
                    nivel = NivelRentabilidad.MEDIA
                    sugerenciaTexto = "PIÉNSALO"
                }
                else -> {
                    nivel = NivelRentabilidad.BAJA
                    sugerenciaTexto = "RECHAZAR"
                }
            }

            fun Double.redondear2Dec(): Double = Math.round(this * 100.0) / 100.0
            fun Double.redondear1Dec(): Double = Math.round(this * 10.0) / 10.0

            return TripData(
                distanciaTotal = distanciaTotal.redondear1Dec(),
                tiempoTotal = tiempoTotal,
                pagoBruto = precioBruto.redondear2Dec(),
                gananciaNeta = gananciaNeta.redondear2Dec(),
                pagoPorKm = pagoPorKm.redondear2Dec(),
                nivelRentabilidad = nivel,
                sugerencia = sugerenciaTexto,
                montoDinamica = montoDinamica?.redondear2Dec(),
                factorDinamica = factorDinamica
            )

        } catch (e: Exception) {
            Log.e("aceptakm_OCR", "Error calculando viaje: ${e.message}")
            return null
        }
    }

    // ==================== HELPERS DE EXTRACCIÓN ====================

    /** Parse money string supporting MX / EU decimal styles. */
    private fun parseNumero(raw: String): Double? {
        return when {
            raw.contains(",") && raw.contains(".") ->
                raw.replace(",", "").toDoubleOrNull()
            raw.contains(".") && raw.lastIndexOf(",") > raw.lastIndexOf(".") ->
                raw.replace(".", "").replace(",", ".").toDoubleOrNull()
            raw.contains(",") && !raw.contains(".") ->
                raw.replace(",", ".").toDoubleOrNull()
            else -> raw.toDoubleOrNull()
        }
    }

    /**
     * Pesos pill: "Tarifa base dinámica de $6.20", "Aumento de $9.00 incluido", etc.
     * Does NOT match factor form "Dinámica x1.3".
     */
    private fun extraerMontoDinamica(texto: String): Double? {
        val patterns = listOf(
            Regex("""(?i)tarifa\s+base\s+din[aá]mica\s+de\s*\$?\s*([\d.,]+)"""),
            Regex("""(?i)base\s+din[aá]mica\s+de\s*\$?\s*([\d.,]+)"""),
            Regex("""(?i)din[aá]mica\s+de\s*\$?\s*([\d.,]+)"""),
            Regex("""(?i)aumento\s+de\s*\$?\s*([\d.,]+)\s+incluido""")
        )
        for (p in patterns) {
            p.find(texto)?.let { match ->
                val v = parseNumero(match.groupValues[1])
                if (v != null && v > 0.0) return v
            }
        }
        return null
    }

    /** Factor form: "Dinámica x1.3" / "⚡ Dinámica x1.6". */
    private fun extraerFactorDinamica(texto: String): Double? {
        val patterns = listOf(
            Regex("""(?i)⚡\s*din[aá]mica\s*x\s*([\d.,]+)"""),
            Regex("""(?i)din[aá]mica\s*x\s*([\d.,]+)""")
        )
        for (p in patterns) {
            p.find(texto)?.let { match ->
                val v = parseNumero(match.groupValues[1])
                if (v != null && v > 0.0) return v
            }
        }
        return null
    }

    /** Spans of dinámica-pesos phrases so fare extraction can skip those `$` amounts. */
    private fun spansMontoDinamica(texto: String): List<IntRange> {
        val patterns = listOf(
            Regex("""(?i)(?:tarifa\s+)?base\s+din[aá]mica\s+de\s*\$?\s*[\d.,]+"""),
            Regex("""(?i)din[aá]mica\s+de\s*\$?\s*[\d.,]+"""),
            Regex("""(?i)aumento\s+de\s*\$?\s*[\d.,]+\s+incluido""")
        )
        return patterns.flatMap { it.findAll(texto).map { m -> m.range }.toList() }
    }

    /**
     * Large fare (MXN). Never pick the dinámica pesos pill when a larger total exists.
     * Skips `$` amounts inside dinámica-pesos phrases; prefers max among remaining candidates.
     */
    private fun extraerPrecio(texto: String, montoDinamica: Double? = null): Double? {
        val skipSpans = spansMontoDinamica(texto)
        val regex = Regex("""(?:\+?\s*MXN\$?|\$)\s*([\d.,]+)""", RegexOption.IGNORE_CASE)

        val candidatos = regex.findAll(texto).mapNotNull { match ->
            if (skipSpans.any { span -> match.range.first in span }) return@mapNotNull null
            val valor = parseNumero(match.groupValues[1]) ?: return@mapNotNull null
            if (valor <= 0.0) return@mapNotNull null
            // Extra guard: same numeric value as dinámica monto near "dinámica"
            if (montoDinamica != null && kotlin.math.abs(valor - montoDinamica) < 0.001) {
                val start = match.range.first
                val ctx = texto.substring(maxOf(0, start - 48), start).lowercase()
                if (ctx.contains("dinám") || ctx.contains("dinam") || ctx.contains("aumento")) return@mapNotNull null
            }
            valor
        }.toList()

        if (candidatos.isEmpty()) return null
        // Prefer the largest reasonable fare (total vs dinámica pill)
        return candidatos.maxOrNull()
    }

    private fun extraerSumaKm(texto: String): Double {
        val regex = Regex("""(\d+[.,]?\d*)\s*km""", RegexOption.IGNORE_CASE)
        return regex.findAll(texto).sumOf { it.groupValues[1].replace(",", ".").toDoubleOrNull() ?: 0.0 }
    }

    private fun extraerSumaMin(texto: String): Int {
        val regex = Regex("""(\d+)\s*min""", RegexOption.IGNORE_CASE)
        return regex.findAll(texto).sumOf { it.groupValues[1].toIntOrNull() ?: 0 }
    }
}