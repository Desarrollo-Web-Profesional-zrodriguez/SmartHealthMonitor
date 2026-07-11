package mx.utng.smarthealthmonitor.tv.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.utng.smarthealthmonitor.data.models.SmartHealthRepository
import mx.utng.smarthealthmonitor.tv.domain.model.TvUiState

class TvViewModel(
    private val repository: SmartHealthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TvUiState())
    val state: StateFlow<TvUiState> = _state.asStateFlow()

    init {
        // Inicializar repositorio explícitamente para asegurar que el contexto esté disponible
        // SmartHealthRepository.init(...) ya debería haber sido llamado en la App class
        
        // Observar historial reactivo del Room DAO
        viewModelScope.launch {
            repository.obtenerHistorial()
                .catch { e -> 
                    android.util.Log.e("TvViewModel", "Error cargando historial", e)
                    _state.update { it.copy(error = e.message, isLoading = false) } 
                }
                .collect { lecturas ->
                    android.util.Log.d("TvViewModel", "Historial actualizado: ${lecturas.size} lecturas")
                    _state.update { it.copy(lecturas = lecturas, isLoading = false) }
                }
        }
        // Observar FC actual (StateFlow del sensor)
        viewModelScope.launch {
            repository.fcFlow.collect { bpm ->
                android.util.Log.d("TvViewModel", "FC actualizada en UI: $bpm")
                _state.update { it.copy(fcActual = bpm) }
            }
        }
    }
}
