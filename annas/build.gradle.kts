// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.devtools.ksp) apply false
    alias(libs.plugins.com.google.dagger) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.baselineprofile) apply false
}

tasks.register("annasDebug") {
    group = "annas"
    description = "Compatibilidad: instala debug desde la raiz."
    dependsOn(":app:appDebug")
}

tasks.register("annasRelease") {
    group = "annas"
    description = "Compatibilidad: genera baseline profile y compila release desde la raiz."
    dependsOn(":app:appRelease")
}

tasks.register("annasInstallDebug") {
    group = "annas"
    description = "Compatibilidad: instala debug desde la raiz."
    dependsOn(":app:appDebug")
}

tasks.register("appDebug") {
    group = "annas"
    description = "Instala la variante debug desde la raiz."
    dependsOn(":app:appDebug")
}

tasks.register("appRelease") {
    group = "annas"
    description = "Genera baseline profile y compila release desde la raiz."
    dependsOn(":app:appRelease")
}

tasks.register("appReleaseFast") {
    group = "annas"
    description = "Compila release sin regenerar baseline profile desde la raiz."
    dependsOn(":app:appReleaseFast")
}

tasks.register("appBundleRelease") {
    group = "annas"
    description = "Genera el bundle release desde la raiz."
    dependsOn(":app:appBundleRelease")
}

tasks.register("appBaselineProfile") {
    group = "annas"
    description = "Regenera el baseline profile desde la raiz."
    dependsOn(":app:appBaselineProfile")
}
