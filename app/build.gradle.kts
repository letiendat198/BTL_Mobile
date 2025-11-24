plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.ptit.btl_mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ptit.btl_mobile"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    sourceSets {
        getByName("main") {
            assets {
                srcDirs("src\\main\\assets", "src\\main\\assets")
            }
        }
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
    implementation(files("libs/taglib.aar"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    val room_version = "2.8.2"

    implementation("androidx.room:room-runtime:$room_version")

    // If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)
    // See Add the KSP plugin to your project
    ksp("androidx.room:room-compiler:$room_version")

    val nav_version = "2.9.5"

    // Jetpack Compose integration
    implementation("androidx.navigation:navigation-compose:$nav_version")

    // Views/Fragments integration
    implementation("androidx.navigation:navigation-fragment:$nav_version")
    implementation("androidx.navigation:navigation-ui:$nav_version")

    // Feature module support for Fragments
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$nav_version")

    // Testing Navigation
    androidTestImplementation("androidx.navigation:navigation-testing:$nav_version")

    // JSON serialization library, works with the Kotlin serialization plugin
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    val media3_version = "1.8.0"

    // For media playback using ExoPlayer
    implementation("androidx.media3:media3-exoplayer:$media3_version")


    // For building media playback UIs using Jetpack Compose
    implementation("androidx.media3:media3-ui-compose:${media3_version}")

    // For exposing and controlling media sessions
    implementation("androidx.media3:media3-session:${media3_version}")
    // For extracting data from media containers
    implementation("androidx.media3:media3-extractor:${media3_version}")

    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")

    // Preference DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // Thư viện để chạy model AI
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

}