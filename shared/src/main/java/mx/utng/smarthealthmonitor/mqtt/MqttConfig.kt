package mx.utng.smarthealthmonitor.mqtt

import mx.utng.smarthealthmonitor.shared.BuildConfig

/**
 * Configuración centralizada de MQTT.
 * Se utiliza BuildConfig para mantener las credenciales seguras (desde local.properties).
 */
object MqttConfig {
    // Datos dinámicos desde local.properties
    val BROKER_URL: String = "ssl://${BuildConfig.MQTT_BROKER_URL}:${BuildConfig.MQTT_PORT}"
    val USERNAME: String = BuildConfig.MQTT_USER
    val PASSWORD: String = BuildConfig.MQTT_PASSWORD

    // Topics del proyecto
    const val TOPIC_FC    = "utng/smarthealthmonitor/fc"
    const val TOPIC_TV    = "utng/smarthealthmonitor/tv"
    const val TOPIC_ALERT = "utng/smarthealthmonitor/alerta"

    // QoS: 0=best effort, 1=at least once, 2=exactly once
    const val QOS = 1

    // Client IDs únicos por dispositivo
    const val CLIENT_WEAR = "smarthealthmonitor-wear"
    const val CLIENT_APP  = "smarthealthmonitor-app"
    const val CLIENT_TV   = "smarthealthmonitor-tv"
}
