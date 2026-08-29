package com.mavacode.aceptakm.feature.asistente

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.mavacode.aceptakm.ui.overlay.FloatingOverlay


class VentanaFlotanteManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var composeView: ComposeView? = null

    fun mostrarOActualizar(tripData: TripData) {
        if (composeView == null) {
            
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                y = 120 
            }

            
            composeView = ComposeView(context).apply {
                
                val lifecycleOwner = MyLifecycleOwner()
                lifecycleOwner.performRestore(null)
                lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
                lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)

                val viewModelStore = ViewModelStore()
                setViewTreeViewModelStoreOwner(object : ViewModelStoreOwner {
                    override val viewModelStore: ViewModelStore get() = viewModelStore
                })

                
                setContent {
                    FloatingOverlay(
                        tripData = tripData,
                        onDrag = { dragX, dragY ->
                            
                            params.x += dragX.toInt()
                            params.y += dragY.toInt()
                            windowManager.updateViewLayout(this, params)
                        },
                        onClose = {
                            
                            ocultar()
                        }
                    )
                }
            }

            
            try {
                windowManager.addView(composeView, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            
            composeView?.setContent {
                FloatingOverlay(
                    tripData = tripData,
                    onDrag = { dragX, dragY ->
                        val layoutParams = composeView?.layoutParams as? WindowManager.LayoutParams
                        if (layoutParams != null) {
                            layoutParams.x += dragX.toInt()
                            layoutParams.y += dragY.toInt()
                            windowManager.updateViewLayout(composeView, layoutParams)
                        }
                    },
                    onClose = {
                        ocultar()
                    }
                )
            }
        }
    }

    fun ocultar() {
        composeView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                
            }
            composeView = null
        }
    }

    
    
    
    private class MyLifecycleOwner : SavedStateRegistryOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val savedStateRegistryController = SavedStateRegistryController.create(this)

        override val lifecycle: Lifecycle get() = lifecycleRegistry
        override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

        fun performRestore(savedState: android.os.Bundle?) {
            savedStateRegistryController.performRestore(savedState)
        }

        fun handleLifecycleEvent(event: Lifecycle.Event) {
            lifecycleRegistry.handleLifecycleEvent(event)
        }
    }
}