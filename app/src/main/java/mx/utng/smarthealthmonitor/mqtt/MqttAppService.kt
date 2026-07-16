package mx.utng.smarthealthmonitor.mqtt

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mx.utng.smarthealthmonitor.mqtt.MqttConfig
import mx.utng.smarthealthmonitor.mqtt.FcMessage
import mx.utng.smarthealthmonitor.mqtt.TvMessage
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.text.SimpleDateFormat
import java.util.Date
 
class MqttAppService(
    private val context: Context,
    private val onFcReceived: (Int, String) -> Unit
) {
    private var client: MqttAsyncClient? = null
 
    fun connect() {
        android.util.Log.d("MQTT_APP", "Intentando conectar a: ${MqttConfig.BROKER_URL}")
        client = MqttAsyncClient(
            MqttConfig.BROKER_URL,
            MqttConfig.CLIENT_APP,
            MemoryPersistence()
        )
 
        val options = MqttConnectOptions().apply {
            userName = MqttConfig.USERNAME
            password = MqttConfig.PASSWORD.toCharArray()
            isCleanSession = true
            socketFactory = javax.net.ssl.SSLSocketFactory.getDefault()
            setAutomaticReconnect(true)
            connectionTimeout = 30
        }
 
        // Callback de mensajes entrantes
        client?.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String, msg: MqttMessage) {
                android.util.Log.d("MQTT_APP", "Mensaje recibido en topic: $topic")
                when (topic) {
                    MqttConfig.TOPIC_FC -> handleFcMessage(msg)
                }
            }
            override fun connectionLost(cause: Throwable?) {
                android.util.Log.w("MQTT_APP","Conexión perdida: ${cause?.message}")
            }
            override fun deliveryComplete(token: IMqttDeliveryToken?) {}
        })
 
        client?.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(token: IMqttToken?) {
                // Suscribirse al topic de FC del reloj
                client?.subscribe(MqttConfig.TOPIC_FC, MqttConfig.QOS)
                android.util.Log.d("MQTT_APP","✅ App conectada y suscrita a ${MqttConfig.TOPIC_FC}")
            }
            override fun onFailure(token: IMqttToken?, ex: Throwable?) {
                android.util.Log.e("MQTT_APP", "❌ Error al conectar: ${ex?.message}")
                ex?.printStackTrace()
            }
        })
    }
 
    private fun handleFcMessage(msg: MqttMessage) {
        try {
            val fcMsg = Json.decodeFromString<FcMessage>(String(msg.payload))
            // Actualizar el Repository mediante el callback
            onFcReceived(fcMsg.bpm, fcMsg.estado)
            // Re-publicar al topic TV (usando pasos actuales del repo si están disponibles)
            val pasosActuales = mx.utng.smarthealthmonitor.data.models.SmartHealthRepository.pasosFlow.value
            republicarATv(fcMsg.bpm, pasosActuales, fcMsg.estado)
        } catch (e: Exception) {
            android.util.Log.e("MQTT_APP", "Error procesando mensaje: ${e.message}")
        }
    }

    /**
     * Re-publica los datos a la TV. Puede ser llamado desde el callback MQTT o
     * localmente cuando llega un dato por Data Layer.
     */
    fun republicarATv(bpm: Int, pasos: Int = 0, estado: String = "Normal") {
        if (client?.isConnected != true) {
            return
        }

        val hora = SimpleDateFormat("HH:mm:ss").format(Date())
        val tvMsg = TvMessage(bpm = bpm, pasos = pasos, estado = estado, hora = hora)
        val tvPayload = Json.encodeToString(tvMsg).toByteArray()
        val tvMqtt = MqttMessage(tvPayload).apply {
            qos = MqttConfig.QOS
            isRetained = true 
        }
        
        try {
            client?.publish(MqttConfig.TOPIC_TV, tvMqtt)
            android.util.Log.d("MQTT_APP","🔁 Re-publicado a la TV: $bpm bpm")
        } catch (e: Exception) {
            android.util.Log.e("MQTT_APP", "Error al publicar a TV: ${e.message}")
        }
    }
 
    fun disconnect() { client?.disconnect() }
}
