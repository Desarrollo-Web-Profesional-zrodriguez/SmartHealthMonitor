import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
    println("DEBUG: MQTT_BROKER_URL loaded as ${localProperties.getProperty("mqtt.broker.url")}")
} else {
    println("DEBUG: local.properties NOT FOUND in shared module")
}

android {
    namespace = "mx.utng.smarthealthmonitor.shared"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
        buildConfigField("String", "MQTT_BROKER_URL", "\"${localProperties.getProperty("mqtt.broker.url") ?: ""}\"")
        buildConfigField("String", "MQTT_PORT", "\"${localProperties.getProperty("mqtt.port") ?: "8883"}\"")
        buildConfigField("String", "MQTT_USER", "\"${localProperties.getProperty("mqtt.user") ?: ""}\"")
        buildConfigField("String", "MQTT_PASSWORD", "\"${localProperties.getProperty("mqtt.password") ?: ""}\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.play.services.wearable)
    // Kotlinx Serialization para JSON
    implementation(libs.kotlinx.serialization.json)
}