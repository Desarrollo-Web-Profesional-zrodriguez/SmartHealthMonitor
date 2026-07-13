plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "mx.utng.smarthealthmonitor.shared"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
        buildConfigField("String", "MQTT_BROKER_URL", "\"${project.findProperty("mqtt.broker.url") ?: ""}\"")
        buildConfigField("String", "MQTT_PORT", "\"${project.findProperty("mqtt.port") ?: "8883"}\"")
        buildConfigField("String", "MQTT_USER", "\"${project.findProperty("mqtt.user") ?: ""}\"")
        buildConfigField("String", "MQTT_PASSWORD", "\"${project.findProperty("mqtt.password") ?: ""}\"")
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
