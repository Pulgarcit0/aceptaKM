package com.mavacode.aceptakm.feature.asistente

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.mavacode.aceptakm.ui.overlay.FloatingOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@Suppress("DEPRECATION")
class FloatingService : LifecycleService() {

    companion object {
        var isRunning = false
        private const val ACTION_DESPERTAR = "com.mavacode.aceptakm.DESPERTAR_OCR"
        private const val CHANNEL_ID = "aceptakmChannel"
        private const val NOTIFICATION_ID = 1
    }

    private lateinit var windowManager: WindowManager
    private lateinit var composeView: ComposeView
    private lateinit var layoutParams: WindowManager.LayoutParams
    private val customLifecycleOwner = ServiceLifecycleOwner()
    private val currentTripState = mutableStateOf<TripData?>(null)
    private val capturaActivaState = mutableStateOf(false)

    private lateinit var ocrManager: OcrCaptureManager
    private lateinit var windowParams: WindowManager.LayoutParams
    private val mainHandler = Handler(Looper.getMainLooper())
    private val resetRunnable = Runnable { currentTripState.value = null }
    private val TAG = "aceptakm_Service"

    private var sleepJob: Job? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        isRunning = true
        super.onCreate()
        customLifecycleOwner.onCreate()
        customLifecycleOwner.onStart()

        ocrManager = OcrCaptureManager(
            context = this,
            onTripDetected = { viajeDetectado ->
                if (viajeDetectado != null) {
                    currentTripState.value = viajeDetectado
                    mainHandler.removeCallbacks(resetRunnable)
                    mainHandler.postDelayed(resetRunnable, 8_000)

                    // Tarjeta de datos → siempre centro arriba (fija)
                    centrarVentanaArriba()
                } else if (currentTripState.value != null) {
                    currentTripState.value = null
                    mainHandler.removeCallbacks(resetRunnable)
                }
            },
            onCapturaDetenida = {
                mainHandler.post {
                    capturaActivaState.value = false
                    Log.d(TAG, "Captura detenida → Toca para leer")
                }
            }
        )

        setupFloatingWindow()

        ContextCompat.registerReceiver(
            this,
            internalWakeUpReceiver,
            IntentFilter(ACTION_DESPERTAR),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        val screenIntentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        ContextCompat.registerReceiver(
            this,
            systemScreenReceiver,
            screenIntentFilter,
            ContextCompat.RECEIVER_EXPORTED
        )

        ocrManager.isScannerPaused = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "🟢 SERVICIO INICIADO / REINICIADO")
        super.onStartCommand(intent, flags, startId)
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AceptaKm")
            .setContentText("Analizando viajes… Toca la oruga para reactivar si se pausó")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .build()
        startForeground(NOTIFICATION_ID, notification)

        val resultCode = intent?.getIntExtra("RESULT_CODE", Activity.RESULT_CANCELED)
            ?: Activity.RESULT_CANCELED

        val dataIntent: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("DATA_INTENT", Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra("DATA_INTENT")
        }

        if (resultCode == Activity.RESULT_OK && dataIntent != null) {
            try { ocrManager.detenerCaptura() } catch (_: Exception) {}
            ocrManager.iniciarCaptura(resultCode, dataIntent)
            ocrManager.isScannerPaused = false
            capturaActivaState.value = true
            Log.d(TAG, "✅ Captura activa → Listo")
        }

        return START_STICKY
    }

    private fun centrarVentanaArriba() {
        mainHandler.post {
            try {
                if (!::composeView.isInitialized || !::windowParams.isInitialized) return@post
                windowParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                windowParams.x = 0
                windowParams.y = 30
                windowManager.updateViewLayout(composeView, windowParams)
            } catch (e: Exception) {
                Log.e(TAG, "Error centrando ventana: ${e.message}")
            }
        }
    }

    private fun setupFloatingWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        windowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            x = 0
            y = 30
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(customLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(customLifecycleOwner)
            setViewTreeViewModelStoreOwner(customLifecycleOwner)
            setContent {
                val currentTrip by currentTripState
                val capturaActiva by capturaActivaState

                FloatingOverlay(
                    tripData = currentTrip,
                    capturaActiva = capturaActiva,
                    onDrag = { deltaX, deltaY ->
                        windowParams.x += deltaX.roundToInt()
                        windowParams.y += deltaY.roundToInt()
                        windowManager.updateViewLayout(composeView, windowParams)
                    },
                    onClose = {
                        currentTripState.value = null
                        sleepJob?.cancel()
                        sleepJob = CoroutineScope(Dispatchers.Main).launch {
                            ocrManager.isScannerPaused = true
                            delay(3000.milliseconds)
                            ocrManager.isScannerPaused = false
                        }
                    },
                    onReactivar = {
                        if (!capturaActivaState.value) {
                            startActivity(
                                Intent(this@FloatingService, ReactivarCapturaActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                }
                            )
                        } else {
                            ocrManager.isScannerPaused = false
                            sleepJob?.cancel()
                            sleepJob = CoroutineScope(Dispatchers.Main).launch {
                                delay(15_000)
                                ocrManager.isScannerPaused = true
                            }
                        }
                    }
                )
            }
        }
        windowManager.addView(composeView, windowParams)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AceptaKm OCR",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        Log.e(TAG, "🔴 SERVICIO DESTRUIDO")
        isRunning = false
        super.onDestroy()
        sleepJob?.cancel()
        mainHandler.removeCallbacks(resetRunnable)

        try { unregisterReceiver(internalWakeUpReceiver) } catch (_: Exception) {}
        try { unregisterReceiver(systemScreenReceiver) } catch (_: Exception) {}
        try {
            if (::composeView.isInitialized) {
                windowManager.removeView(composeView)
            }
        } catch (_: Exception) {}
        try { ocrManager.detenerCaptura() } catch (_: Exception) {}
        try { customLifecycleOwner.onDestroy() } catch (_: Exception) {}
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private val internalWakeUpReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_DESPERTAR) {
                Log.d(TAG, "⏰ Despertador recibido. Reactivando OCR…")
                ocrManager.isScannerPaused = false
                sleepJob?.cancel()
                sleepJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(15_000.milliseconds)
                    ocrManager.isScannerPaused = true
                    Log.d(TAG, "😴 Modo ahorro: OCR en pausa")
                }
            }
        }
    }

    private val systemScreenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    ocrManager.isScannerPaused = true
                    capturaActivaState.value = false
                    sleepJob?.cancel()
                }
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                    Log.d(TAG, "📱 Pantalla encendida. Despertando OCR 15s")
                    ocrManager.isScannerPaused = false
                    sleepJob?.cancel()
                    sleepJob = CoroutineScope(Dispatchers.Main).launch {
                        delay(15_000.milliseconds)
                        ocrManager.isScannerPaused = true
                        Log.d(TAG, "😴 OCR en pausa por inactividad")
                    }
                }
            }
        }
    }
}