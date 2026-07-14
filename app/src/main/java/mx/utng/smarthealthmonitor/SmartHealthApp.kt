package mx.utng.smarthealthmonitor

import android.app.Application
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mx.utng.smarthealthmonitor.data.models.SmartHealthRepository
import mx.utng.smarthealthmonitor.mqtt.MqttAppService

class SmartHealthApp : Application() {
    lateinit var mqttService: MqttAppService

    override fun onCreate() {
        super.onCreate()
        // Inicializar Repositorio (y base de datos)
        SmartHealthRepository.init(this)

        // Inicializar MQTT con callback de actualización al repositorio
        mqttService = MqttAppService(
            context = this,
            onFcReceived = { bpm, _ ->
                MainScope().launch {
                    SmartHealthRepository.actualizarFC(bpm)
                }
            }
        )
        mqttService.connect()

        // Limpiar historial antiguo al iniciar (más de 7 días)
        MainScope().launch {
            SmartHealthRepository.limpiarHistorialAntiguo()
        }
    }
}
