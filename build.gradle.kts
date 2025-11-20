plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false

    // Compose Compiler – versión explícita oficial
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
}