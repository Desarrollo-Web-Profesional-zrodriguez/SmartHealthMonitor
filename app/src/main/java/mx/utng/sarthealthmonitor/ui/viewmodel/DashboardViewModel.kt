package mx.utng.smarthealthmonitor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import mx.utng.smarthealthmonitor.data.models.SmartHealthRepository
import mx.utng.smarthealthmonitor.data.models.MockData

class DashboardViewModel : ViewModel() {

    // FC: viene del wearable real vía Repository.
    val fc: StateFlow<Int> = SmartHealthRepository.fcFlow
        .map { if (it == 0) MockData.fcActual else it }
        .stateIn(
            scope          = viewModelScope,
            started        = SharingStarted.WhileSubscribed(5_000),
            initialValue   = MockData.fcActual
        )

    val pasos: StateFlow<Int> = SmartHealthRepository.pasosFlow
        .map { if (it == 0) MockData.pasosActual else it }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = MockData.pasosActual
        )

    val spO2: StateFlow<Int> = SmartHealthRepository.spO2Flow
        .map { if (it == 0) 98 else it } // 98% valor default simulado
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = 98
        )

    val historial = MockData.historialFC
}
