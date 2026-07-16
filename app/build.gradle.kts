import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "mx.utng.smarthealthmonitor"
    compileSdk = 37

    defaultConfig {
        applicationId = "mx.utng.smarthealthmonitor"
        minSdk = 26
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // MQTT Config from local.properties
        buildConfigField("String", "MQTT_BROKER_URL", "\"${localProperties.getProperty("mqtt.broker.url") ?: ""}\"")
        buildConfigField("String", "MQTT_PORT", "\"${localProperties.getProperty("mqtt.port") ?: "8883"}\"")
        buildConfigField("String", "MQTT_USER", "\"${localProperties.getProperty("mqtt.user") ?: ""}\"")
        buildConfigField("String", "MQTT_PASSWORD", "\"${localProperties.getProperty("mqtt.password") ?: ""}\"")
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
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    // Wearable Data Layer API
    implementation(libs.play.services.wearable.v1820)
    // Coroutines para await()
    implementation(libs.kotlinx.coroutines.play.services.v173)
    // Coroutines await() para Guava ListenableFuture
    implementation(libs.guava.v3300android)
    implementation(libs.kotlinx.coroutines.guava.v173)

    // MediaRouter and Cast
    implementation(libs.androidx.mediarouter)
    implementation(libs.play.services.cast.framework)

    // Eclipse Paho MQTT para Android
    implementation(libs.paho.mqtt)
    implementation(libs.paho.android.service)
    // Kotlinx Serialization para JSON
    implementation(libs.kotlinx.serialization.json)

    // Room
    val roomVersion = "2.7.0-alpha11"
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(project(":shared"))

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
