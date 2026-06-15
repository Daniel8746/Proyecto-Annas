import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.com.google.dagger)
    alias(libs.plugins.ksp)
}

configure<LibraryExtension> {
    namespace = "com.annas.core.data"
    compileSdk = 37

    defaultConfig {
        minSdk = 31
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    api(project(":core:model"))
    
    api(platform(libs.firebase.bom))
    api(libs.firebase.ai)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.kotlin.metadata)

    implementation(libs.bundles.networkingBundle)

    testImplementation(libs.junit)
}
