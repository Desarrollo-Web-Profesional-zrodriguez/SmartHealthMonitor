package mx.utng.smarthealthmonitor.data.models

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import mx.utng.smarthealthmonitor.data.db.LecturaFC
import mx.utng.smarthealthmonitor.data.db.LecturaFCDao
import mx.utng.smarthealthmonitor.data.db.SmartHealthDB

/**
 * Repositorio singleton que centraliza los datos de salud.
 * El WearListenerService escribe aquí.
 * El ViewModel lee de aquí.
 */
object SmartHealthRepository {

    // FC actual del wearable (bpm)
    private val _fcFlow = MutableStateFlow(0)
    val fcFlow: StateFlow<Int> = _fcFlow.asStateFlow()

    // Pasos del día actual
    private val _pasosFlow = MutableStateFlow(0)
    val pasosFlow: StateFlow<Int> = _pasosFlow.asStateFlow()

    // SpO2 (Saturación de oxígeno %)
    private val _spO2Flow = MutableStateFlow(0)
    val spO2Flow: StateFlow<Int> = _spO2Flow.asStateFlow()

    private var dao: LecturaFCDao? = null

    fun init(context: Context) {
        dao = SmartHealthDB.getDatabase(context).lecturaDao()
    }

    suspend fun actualizarFC(bpm: Int) {
        _fcFlow.value = bpm
        // Persistir en Room automáticamente
        dao?.insertar(LecturaFC(valorBpm = bpm))
    }

    fun actualizarPasos(pasos: Int) {
        _pasosFlow.value = pasos
    }

    fun actualizarSpO2(spo2: Int) {
        _spO2Flow.value = spo2
    }

    // Flow del historial desde Room
    fun obtenerHistorial(): Flow<List<LecturaFC>> =
        dao?.obtenerUltimas() ?: emptyFlow()

    suspend fun limpiarHistorialAntiguo(umbralManual: Long? = null) {
        val umbral = if (umbralManual != null) {
            umbralManual
        } else {
            // 7 días por defecto
            val sieteDiasMs = 7 * 24 * 60 * 60 * 1000L
            System.currentTimeMillis() - sieteDiasMs
        }
        dao?.limpiarViejos(umbral)
    }
}
