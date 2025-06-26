plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.serialization") version "1.8.21"

}

android {
    namespace = "com.example.babyphotoapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.babyphotoapp"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.7"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation(libs.androidx.navigation.runtime.android)
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    // Material Components (for your XML theme)
    implementation("com.google.android.material:material:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    // Compose UI
    implementation("androidx.compose.ui:ui:1.4.3")
    implementation("androidx.compose.material:material:1.4.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.3")
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.3")

    // Compose Foundation (includes LazyVerticalGrid)
    implementation("androidx.compose.foundation:foundation:1.4.3")

    // Lifecycle Compose (for LocalLifecycleOwner)
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")

    // Activity Compose (permissions launcher)
    implementation("androidx.activity:activity-compose:1.7.2")

    // CameraX
    implementation("androidx.camera:camera-core:1.2.0")
    implementation("androidx.camera:camera-camera2:1.2.0")
    implementation("androidx.camera:camera-lifecycle:1.2.0")
    implementation("androidx.camera:camera-view:1.2.0")

    // Instrumented test dependencies
    androidTestImplementation("androidx.test:core:1.5.0")              // ApplicationProvider
    androidTestImplementation("androidx.test.ext:junit:1.1.5")         // JUnit4 rules
    androidTestImplementation("androidx.test:runner:1.5.2")            // AndroidJUnitRunner
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")


    // Coil
    implementation("io.coil-kt:coil-compose:2.2.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.5.3")

    // Icons
    implementation("androidx.compose.material:material-icons-extended:1.4.3")
}
kapt {
    javacOptions {
        option("-source", "17")
        option("-target", "17")
    }
}

configurations.all {
    resolutionStrategy {
        // force both core & json to 1.5.1 so you never pick up 1.7.3 transitively
        force(
            "org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.1",
            "org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1"
        )
    }
}
