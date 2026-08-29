package com.mavacode.aceptakm.feature.asistente

import android.content.Context
import androidx.core.content.edit

object PlataformaManager {

    private const val PREFS_NAME = "aceptakmPrefs"
    private const val KEY_PLATAFORMAS = "plataformas_activas"
    private const val KEY_PLATAFORMA_VIEJA = "plataforma_activa"
    private const val KEY_PLATAFORMA_DEFAULT = "plataforma_default"

    private const val DEFAULT_PLATAFORMA = "Didi Moto/Auto"

    /** Lista canónica (mismo orden/nombres que Switch + Parser + Listener) */
    val TODAS_LAS_PLATAFORMAS = listOf(
        "Didi Moto/Auto",
        "Uber Moto/Auto",
        "inDrive",
        "Didi Food",
        "Uber Eats",
        "Rappi",
        "Cabify",
        "Lalamove"
    )

    // ---------- Multi-select ----------

    fun guardarPlataformasActivas(context: Context, plataformas: Set<String>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            // toSet() evita el bug de SharedPreferences con la misma instancia del Set
            putStringSet(KEY_PLATAFORMAS, plataformas.toSet())
        }
    }

    fun obtenerPlataformasActivas(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val plataformas = prefs.getStringSet(KEY_PLATAFORMAS, null)

        if (plataformas != null && plataformas.isNotEmpty()) {
            return plataformas
        }

        // Migración desde clave vieja (1 sola plataforma)
        val vieja = prefs.getString(KEY_PLATAFORMA_VIEJA, null)
        if (vieja != null) {
            val migrado = setOf(vieja)
            guardarPlataformasActivas(context, migrado)
            prefs.edit { remove(KEY_PLATAFORMA_VIEJA) }
            return migrado
        }

        // Nada activo por defecto: el usuario elige
        return emptySet()
    }

    fun estaActiva(context: Context, plataforma: String): Boolean {
        return obtenerPlataformasActivas(context).contains(plataforma)
    }

    fun estanTodasActivas(context: Context): Boolean {
        val activas = obtenerPlataformasActivas(context)
        return TODAS_LAS_PLATAFORMAS.all { activas.contains(it) }
    }

    fun activarTodas(context: Context) {
        guardarPlataformasActivas(context, TODAS_LAS_PLATAFORMAS.toSet())
    }

    fun desactivarTodas(context: Context) {
        guardarPlataformasActivas(context, emptySet())
    }

    // ---------- Ancla / default para Home ----------

    fun guardarPlataformaDefault(context: Context, plataforma: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_PLATAFORMA_DEFAULT, plataforma)
        }
    }

    fun obtenerPlataformaDefault(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val guardada = prefs.getString(KEY_PLATAFORMA_DEFAULT, null)
        if (!guardada.isNullOrBlank()) return guardada

        // Si no hay ancla, usa la primera activa o DiDi
        val activas = obtenerPlataformasActivas(context)
        return activas.firstOrNull() ?: DEFAULT_PLATAFORMA
    }

    // ---------- Compatibilidad (código viejo) ----------

    fun obtenerPlataformaActiva(context: Context): String? {
        return obtenerPlataformasActivas(context).firstOrNull()
            ?: obtenerPlataformaDefault(context)
    }

    fun guardarPlataformaActiva(context: Context, plataforma: String?) {
        if (plataforma != null) {
            guardarPlataformasActivas(context, setOf(plataforma))
            guardarPlataformaDefault(context, plataforma)
        } else {
            desactivarTodas(context)
        }
    }
}