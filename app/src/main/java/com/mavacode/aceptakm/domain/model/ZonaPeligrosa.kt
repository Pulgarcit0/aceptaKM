package com.mavacode.aceptakm.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zonas_peligrosas")
data class ZonaPeligrosa(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val palabraClave: String
)