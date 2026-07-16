package mx.utng.smarthealthmonitor.wear.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.utng.smarthealthmonitor.data.db.LecturaFC
import mx.utng.smarthealthmonitor.data.models.SmartHealthRepository
import mx.utng.smarthealthmonitor.wear.mqtt.MqttWearPublisher

class WearDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val mqttPublisher = MqttWearPublisher(application)

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            mqttPublisher.connect()
            // Esperar conexión y forzar envío inicial
            kotlinx.coroutines.delay(5000)
            publicarValorActual()
        }
        viewModelScope.launch {
            SmartHealthRepository.fcFlow.collect { bpm ->
                if (bpm > 0) {
                    publicarValorActual(bpm)
                }
            }
        }
    }

    private fun publicarValorActual(bpmActual: Int? = null) {
        val valor = bpmActual ?: SmartHealthRepository.fcFlow.value
        if (valor > 0) {
            val estado = when {
                valor < 60 -> "FC Baja"
                valor > 100 -> "FC Alta"
                else -> "Normal"
            }
            mqttPublisher.publishFC(valor, estado)
        }
    }

    override fun onCleared() {
        super.onCleared()
        mqttPublisher.disconnect()
    }

    // Reutiliza el mismo Repository del módulo shared
    val fc: StateFlow<Int> = SmartHealthRepository.fcFlow
        .map { if (it == 0) 72 else it }  // valor por defecto
        .stateIn(viewModelScope,
            SharingStarted.WhileSubscribed(5_000), 72)

    // Flow de pasos para la UI
    val pasos: StateFlow<Int> = SmartHealthRepository.pasosFlow
        .stateIn(viewModelScope,
            SharingStarted.WhileSubscribed(5_000), 0)
    val historial: StateFlow<List<LecturaFC>> =
        SmartHealthRepository.obtenerHistorial()
            .stateIn(viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList())
}
