import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinx.serialization)
    id("kotlin-parcelize")
}

configure<LibraryExtension> {
    namespace = "com.annas.core.model"
    compileSdk = 37

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    api(project(":core:common"))
    
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    
    api(libs.kotlinx.collections.immutable)
    api(libs.kotlinx.serialization.json)
}
