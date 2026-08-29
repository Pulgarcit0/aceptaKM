package com.mavacode.aceptakm.feature.asistente

import android.app.Service
import android.content.Intent
import android.os.IBinder

class aceptakmService : Service() {

    
    private lateinit var ventanaManager: VentanaFlotanteManager

    override fun onCreate() {
        super.onCreate()
        
        ventanaManager = VentanaFlotanteManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        
        

        return START_NOT_STICKY
    }

    
    fun alDetectarViaje(tripData: TripData?) {
        if (tripData != null) {
            
            ventanaManager.mostrarOActualizar(tripData)
        } else {
            
            ventanaManager.ocultar()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        
        ventanaManager.ocultar()
    }

    override fun onBind(intent: Intent?): IBinder? {
        
        return null
    }
}