package com.mavacode.aceptakm.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mavacode.aceptakm.domain.model.ZonaBloqueada
import com.mavacode.aceptakm.domain.model.ZonaPeligrosa

// MODIFICADO: Agregamos ZonaBloqueada::class al arreglo y subimos la versión a 2
@Database(
    entities = [ZonaPeligrosa::class, ZonaBloqueada::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun zonasDao(): ZonasDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aceptakm_db"
                )
                    // MODIFICADO: Añadimos esta línea para evitar errores si la base de datos ya existía en tu teléfono/emulador con la versión 1
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}