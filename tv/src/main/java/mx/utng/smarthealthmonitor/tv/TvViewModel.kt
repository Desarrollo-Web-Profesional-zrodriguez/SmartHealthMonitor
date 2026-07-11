package mx.utng.smarthealthmonitor.tv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import mx.utng.smarthealthmonitor.data.models.SmartHealthRepository
import mx.utng.smarthealthmonitor.data.db.LecturaFC

class TvViewModel : ViewModel() {

    // FC actual del wearable
    val fc: StateFlow<Int> = SmartHealthRepository.fcFlow

    // Pasos actuales
    val pasos: StateFlow<Int> = SmartHealthRepository.pasosFlow

    // Historial de lecturas desde Room DAO
    val historial: StateFlow<List<LecturaFC>> =
        SmartHealthRepository.obtenerHistorial()
            .stateIn(viewModelScope,
                     SharingStarted.WhileSubscribed(5_000),
                     emptyList())
}