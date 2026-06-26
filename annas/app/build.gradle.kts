import com.android.build.api.dsl.ApplicationExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.com.google.dagger)
    alias(libs.plugins.kotlinx.serialization)
    id("kotlin-parcelize")
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.baselineprofile)
}

configure<ApplicationExtension> {
    namespace = "com.annas"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.annas"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "bizcocho"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

composeCompiler {
    metricsDestination = layout.buildDirectory.dir("compose_metrics")
    reportsDestination = layout.buildDirectory.dir("compose_reports")
}

dependencies {
    implementation(project(":core:data"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.composeBundle)

    implementation(libs.bundles.hiltBundle)
    ksp(libs.hilt.compiler)
    ksp(libs.kotlin.metadata)

    implementation(libs.bundles.uiUtilsBundle)

    releaseImplementation(libs.androidx.profileinstaller)
    baselineProfile(project(":baselineprofile"))

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.bundles.testingBundle)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.bundles.debugBundle)
}
