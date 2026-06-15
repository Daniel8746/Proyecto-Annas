import com.android.build.api.dsl.ManagedVirtualDevice
import com.android.build.api.dsl.TestExtension

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.baselineprofile)
}

configure<TestExtension> {
    namespace = "com.annas.baselineprofile"
    compileSdk = 37

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"

    // This code creates the gradle managed device used to generate baseline profiles.
    // To use GMD please invoke generation through the command line:
    // ./gradlew :app:generateBaselineProfile
    testOptions.managedDevices.allDevices {
        create<ManagedVirtualDevice>("pixel8Api37Atd") {
            device = "Pixel 8"
            sdkVersion = 37
            systemImageSource = "google"
            pageAlignment = ManagedVirtualDevice.PageAlignment.FORCE_16KB_PAGES
            testedAbi = "x86_64"
        }
    }
}

// This is the configuration block for the Baseline Profile plugin.
// You can specify to run the generators on a managed devices or connected devices.
baselineProfile {
    managedDevices += "pixel8Api37Atd"
    useConnectedDevices = false
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.kotlinx.collections.immutable)
}
