package com.mavacode.aceptakm.feature.asistente

import android.content.Intent
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class aceptakmNotificationListener : NotificationListenerService() {

    private val tag = "aceptakm_Listener"

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(tag, "🎧 Oído biónico conectado. Escuchando notificaciones...")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        sbn?.let {
            val paquete = it.packageName?.lowercase() ?: ""

            // 1. Obtenemos la lista actual de plataformas encendidas en la app
            val plataformas = PlataformaManager.obtenerPlataformasActivas(this)

            // 2. Evaluamos si la notificación pertenece EXACTAMENTE a la app seleccionada
            val esRelevante = when {
                // Uber Eats (Revisamos 'eats' en el paquete)
                paquete.contains("ubercab.eats") && plataformas.contains("Uber Eats") -> true

                // Uber Moto/Auto (Asegurándonos de que NO sea Eats)
                paquete.contains("ubercab") && !paquete.contains("eats") && plataformas.contains("Uber Moto/Auto") -> true

                // Didi Food
                paquete.contains("didi.food") && plataformas.contains("Didi Food") -> true

                // Didi Moto/Auto (Asegurándonos de que NO sea Food)
                paquete.contains("didi") && !paquete.contains("food") && plataformas.contains("Didi Moto/Auto") -> true

                // inDrive
                paquete.contains("indriver") && plataformas.contains("inDrive") -> true

                // Rappi
                paquete.contains("rappi") && plataformas.contains("Rappi") -> true

                // Cabify
                paquete.contains("cabify") && plataformas.contains("Cabify") -> true

                // Lalamove
                paquete.contains("lalamove") && plataformas.contains("Lalamove") -> true

                else -> false
            }

            // 3. Solo despertamos al sistema si la notificación es relevante
            if (esRelevante) {
                Log.d(tag, "🔔 ALERTA DE VIAJE DETECTADA (Plataforma activa): $paquete")

                encenderPantalla()

                // Enviamos la señal para despertar al FloatingService (y por ende al OCR)
                val intent = Intent("com.mavacode.aceptakm.DESPERTAR_OCR")
                sendBroadcast(intent)
                Log.d(tag, "📢 Señal de despertar enviada al FloatingService")
            } else {
                // Solo registramos si es una app de viajes conocida pero ignorada
                if (paquete.contains("uber") || paquete.contains("didi") || paquete.contains("indriver") || paquete.contains("rappi")) {
                    Log.d(tag, "💤 Notificación de $paquete ignorada. El switch de esa plataforma está apagado.")
                }
            }
        }
    }

    private fun encenderPantalla() {
        try {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "aceptakm::AlertaViajeWakeLock"
            )

            wakeLock.acquire(10 * 1000L) // 10 segundos es suficiente
            Log.d(tag, "💡 Pantalla encendida exitosamente")
        } catch (e: Exception) {
            Log.e(tag, "❌ Error al intentar encender la pantalla: ${e.message}")
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(tag, "🔇 Oído desconectado.")
    }
}