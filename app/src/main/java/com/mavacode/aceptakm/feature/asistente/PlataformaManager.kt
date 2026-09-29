package com.mavacode.aceptakm.feature.asistente

import android.content.Context
import androidx.core.content.edit

object PlataformaManager {

    private const val PREFS_NAME = "aceptakmPrefs"
    private const val KEY_PLATAFORMAS = "plataformas_activas"
    private const val KEY_PLATAFORMA_VIEJA = "plataforma_activa"
    private const val KEY_PLATAFORMA_DEFAULT = "plataforma_default"

    private const val DEFAULT_PLATAFORMA = "Didi Moto/Auto"

    val TODAS_LAS_PLATAFORMAS = listOf(
        "Didi Moto/Auto",
        "Uber Moto/Auto",
        "Didi Food",
        "Uber Eats"
        // "inDrive",
        // "Rappi",
        // "Cabify",
        // "Lalamove"
    )

    private val PERMITIDAS = TODAS_LAS_PLATAFORMAS.toSet()

    // ---------- Multi-select ----------

    fun guardarPlataformasActivas(context: Context, plataformas: Set<String>) {
        val limpias = plataformas.filter { it in PERMITIDAS }.toSet()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putStringSet(KEY_PLATAFORMAS, limpias.toSet())
        }
    }

    fun obtenerPlataformasActivas(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val plataformas = prefs.getStringSet(KEY_PLATAFORMAS, null)

        if (plataformas != null && plataformas.isNotEmpty()) {
            val limpias = plataformas.filter { it in PERMITIDAS }.toSet()
            if (limpias.size != plataformas.size) {
                guardarPlataformasActivas(context, limpias)
            }
            return limpias
        }

        val vieja = prefs.getString(KEY_PLATAFORMA_VIEJA, null)
        if (vieja != null && vieja in PERMITIDAS) {
            val migrado = setOf(vieja)
            guardarPlataformasActivas(context, migrado)
            prefs.edit { remove(KEY_PLATAFORMA_VIEJA) }
            return migrado
        }
        if (vieja != null) {
            prefs.edit { remove(KEY_PLATAFORMA_VIEJA) }
        }

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
        val valor = if (plataforma in PERMITIDAS) plataforma else DEFAULT_PLATAFORMA
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_PLATAFORMA_DEFAULT, valor)
        }
    }

    fun obtenerPlataformaDefault(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val guardada = prefs.getString(KEY_PLATAFORMA_DEFAULT, null)
        if (!guardada.isNullOrBlank() && guardada in PERMITIDAS) return guardada

        val activas = obtenerPlataformasActivas(context)
        return activas.firstOrNull() ?: DEFAULT_PLATAFORMA
    }

    // ---------- Compatibilidad ----------

    fun obtenerPlataformaActiva(context: Context): String? {
        return obtenerPlataformasActivas(context).firstOrNull()
            ?: obtenerPlataformaDefault(context)
    }

    fun guardarPlataformaActiva(context: Context, plataforma: String?) {
        if (plataforma != null && plataforma in PERMITIDAS) {
            guardarPlataformasActivas(context, setOf(plataforma))
            guardarPlataformaDefault(context, plataforma)
        } else {
            desactivarTodas(context)
        }
    }
}