package mx.utng.smarthealthmonitor.config

import mx.utng.smarthealthmonitor.BuildConfig

/**
 * Configuración de MQTT obtenida desde local.properties a través de BuildConfig.
 */
object MqttConfig {
    const val BROKER_URL: String = BuildConfig.MQTT_BROKER_URL
    const val PORT: String = BuildConfig.MQTT_PORT
    const val USER: String = BuildConfig.MQTT_USER
    const val PASSWORD: String = BuildConfig.MQTT_PASSWORD
}
