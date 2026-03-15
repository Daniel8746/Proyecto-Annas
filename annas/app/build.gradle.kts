plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.com.google.dagger)
    alias(libs.plugins.kotlinx.serialization)
    id("kotlin-parcelize")
}

android {
    namespace = "com.pmdm.annas"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pmdm.annas"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.pmdm.ies.balmis.components)
    implementation(libs.pmdm.ies.balmis.utilities)

    implementation(libs.dagger.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.animation)
    ksp(libs.dagger.hilt.android.compiler)
    kspAndroidTest(libs.dagger.hilt.android.compiler)

    implementation(libs.compose.navigation)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

//    implementation(libs.com.squareup.retrofit2.converter.gson)
//    implementation(libs.com.squareup.retrofit2.retrofit)
//    implementation(platform(libs.com.squareup.okhttp3.okhttp.bom))
//    implementation(libs.com.squareup.okhttp3.okhttp)
//    implementation(libs.com.squareup.okhttp3.logging.interceptor)

    implementation(libs.androidx.webkit)
    implementation(libs.material3)

    implementation(libs.coil.compose)

    implementation(libs.lottie.compose)

    implementation(libs.jsoup)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}