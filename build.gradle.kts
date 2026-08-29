// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Plugin de servicios de Google para Firebase
    id("com.google.gms.google-services") version "4.5.0" apply false

    // Plugin KSP para el procesador de anotaciones de Room
    id("com.google.devtools.ksp") version "2.0.20-1.0.25" apply false
}
