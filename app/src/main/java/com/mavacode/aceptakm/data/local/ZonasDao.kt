package com.mavacode.aceptakm.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mavacode.aceptakm.domain.model.ZonaPeligrosa
import kotlinx.coroutines.flow.Flow

@Dao
interface ZonasDao {

    // Esta es la que ya tenías y usa tu pantalla (devuelve Flow)
    @Query("SELECT * FROM zonas_peligrosas")
    fun obtenerTodasLasZonas(): Flow<List<ZonaPeligrosa>>

    // 👇 ESTA ES LA NUEVA QUE NECESITA EL OCR 👇
    @Query("SELECT * FROM zonas_peligrosas")
    suspend fun obtenerZonasParaServicio(): List<ZonaPeligrosa>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarZona(zona: ZonaPeligrosa)

    @Delete
    suspend fun eliminarZona(zona: ZonaPeligrosa)
}