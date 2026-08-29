package com.mavacode.aceptakm.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zonas_bloqueadas")
data class ZonaBloqueada(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val palabraClave: String
)