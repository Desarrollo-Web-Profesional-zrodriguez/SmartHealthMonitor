package mx.utng.smarthealthmonitor.tv

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.utng.smarthealthmonitor.data.models.SmartHealthRepository
import mx.utng.smarthealthmonitor.data.db.LecturaFC
import mx.utng.smarthealthmonitor.mqtt.TvMessage
import mx.utng.smarthealthmonitor.tv.mqtt.MqttTvSubscriber

data class TvUiState(
    val fcActual: Int = 0,
    val fcEstado: String = "Normal",
    val ultimaHora: String = "--:--:--",
    val isLoading: Boolean = true
)

class TvViewModel(application: Application) : AndroidViewModel(application) {

    // Reutiliza el mismo Repository del módulo shared
    val fc: StateFlow<Int> = SmartHealthRepository.fcFlow

    // Pasos actuales
    val pasos: StateFlow<Int> = SmartHealthRepository.pasosFlow

    // Historial de lecturas desde Room DAO
    val historial: StateFlow<List<LecturaFC>> =
        SmartHealthRepository.obtenerHistorial()
            .stateIn(viewModelScope,
                     SharingStarted.WhileSubscribed(5_000),
                     emptyList())

    private val _state = MutableStateFlow(TvUiState())
    val state: StateFlow<TvUiState> = _state.asStateFlow()

    // Flow de mensajes MQTT entrantes
    private val mqttFlow = MutableStateFlow<TvMessage?>(null)
    private val mqttSubscriber = MqttTvSubscriber(application, mqttFlow)

    init {
        mqttSubscriber.connect()

        // Observar mensajes MQTT y actualizar el estado de la UI
        viewModelScope.launch {
            mqttFlow.collect { tvMsg ->
                tvMsg ?: return@collect
                
                // Actualizar repositorio local para persistir lectura
                SmartHealthRepository.actualizarFC(tvMsg.bpm)

                _state.update { it.copy(
                    fcActual = tvMsg.bpm,
                    fcEstado = tvMsg.estado,
                    ultimaHora = tvMsg.hora,
                    isLoading = false
                )}
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mqttSubscriber.disconnect()
    }
}