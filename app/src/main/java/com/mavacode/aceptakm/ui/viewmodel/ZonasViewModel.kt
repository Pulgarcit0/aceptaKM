package com.mavacode.aceptakm.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mavacode.aceptakm.data.local.AppDatabase
import com.mavacode.aceptakm.domain.model.ZonaPeligrosa
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ZonasViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).zonasDao()

    // Lista observable que se actualiza solita en la UI
    val listaZonas: StateFlow<List<ZonaPeligrosa>> = dao.obtenerTodasLasZonas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun agregarZona(palabra: String) {
        val palabraLimpia = palabra.trim()
        if (palabraLimpia.isNotBlank()) {
            // Verificamos si ya existe en la lista actual ignorando mayúsculas/minúsculas
            val yaExiste = listaZonas.value.any {
                it.palabraClave.equals(palabraLimpia, ignoreCase = true)
            }

            if (!yaExiste) {
                viewModelScope.launch {
                    dao.insertarZona(ZonaPeligrosa(palabraClave = palabraLimpia))
                }
            }
        }
    }

    fun eliminarZona(zona: ZonaPeligrosa) {
        viewModelScope.launch {
            dao.eliminarZona(zona)
        }
    }

}