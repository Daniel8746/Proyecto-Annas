import com.android.build.api.dsl.ApplicationExtension
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.devtools.ksp)
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
        minSdk = 31
        targetSdk = 37
        versionCode = 1
        versionName = "bizcocho"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "ANNAS_BUILD_VARIANT", "\"debug\"")
            buildConfigField("Boolean", "ANNAS_IS_RELEASE", "false")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("String", "ANNAS_BUILD_VARIANT", "\"release\"")
            buildConfigField("Boolean", "ANNAS_IS_RELEASE", "true")
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

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:model"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.dagger.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.animation)
    implementation(libs.androidx.compose.material3)
    ksp(libs.dagger.hilt.android.compiler)
    ksp(libs.kotlin.metadata.jvm)

    implementation(libs.compose.navigation)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.coil.compose)

    implementation(libs.lottie.compose)

    implementation(libs.okhttp)

    implementation(libs.androidx.compose.material.icons.extended.android)
    implementation(libs.readmore.material3)

    implementation(libs.qrose)

    implementation(libs.kotlinx.collections.immutable)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.ai)

    releaseImplementation(libs.androidx.profileinstaller)
    baselineProfile(project(":baselineprofile"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.matching { it.name == "assembleRelease" }.configureEach {
    mustRunAfter("generateBaselineProfile")
}

fun adbExecutable(): File {
    val properties = Properties()
    val localProperties = rootProject.file("local.properties")
    if (localProperties.isFile) {
        localProperties.inputStream().use(properties::load)
    }

    val sdkDir = properties.getProperty("sdk.dir")
        ?: System.getenv("ANDROID_HOME")
        ?: System.getenv("ANDROID_SDK_ROOT")
        ?: error("No se encontro Android SDK. Revisa local.properties o ANDROID_HOME.")

    val adbName = if (System.getProperty("os.name").contains("Windows", ignoreCase = true)) {
        "adb.exe"
    } else {
        "adb"
    }

    return file(sdkDir).resolve("platform-tools").resolve(adbName)
}

tasks.register<Exec>("appDebug") {
    group = "annas"
    description = "Instala debug y lanza Annas en un dispositivo conectado."
    dependsOn("installDebug")
    doFirst {
        commandLine(
            adbExecutable().absolutePath,
            "shell",
            "am",
            "start",
            "-n",
            "com.annas/com.annas.ui.view.MainActivity"
        )
    }
}

tasks.register("appRelease") {
    group = "annas"
    description = "Genera baseline profile y compila la APK release optimizada."
    dependsOn("generateBaselineProfile", "assembleRelease")
}

tasks.register("appReleaseFast") {
    group = "annas"
    description = "Compila release usando el baseline profile ya guardado, sin regenerarlo."
    dependsOn("assembleRelease")
}

tasks.register("appBundleRelease") {
    group = "annas"
    description = "Genera el bundle release usando el baseline profile ya guardado."
    dependsOn("bundleRelease")
}

tasks.register("appBaselineProfile") {
    group = "annas"
    description = "Regenera y guarda el baseline profile de produccion."
    dependsOn("generateBaselineProfile")
}
