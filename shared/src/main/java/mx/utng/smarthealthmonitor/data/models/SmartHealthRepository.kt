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
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

/**
 * Repositorio singleton que centraliza los datos de salud.
 */
object SmartHealthRepository {
    private const val TAG = "SmartHealthRepository"
    private const val PATH_HEALTH_DATA = "/smarthealth/data"
    private const val KEY_FC = "fc"
    private const val KEY_PASOS = "pasos"

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
        if (dao == null) {
            android.util.Log.d(TAG, "Initializing DB")
            dao = SmartHealthDB.getDatabase(context).lecturaDao()
        }
    }

    /**
     * Sincroniza los datos con el Data Layer de Wear OS.
     * Esto hace que el estado sea global y persistente en todos los dispositivos.
     */
    suspend fun sincronizarConDataLayer(context: Context, bpm: Int, pasos: Int) {
        try {
            val request = PutDataMapRequest.create(PATH_HEALTH_DATA).apply {
                dataMap.putInt(KEY_FC, bpm)
                dataMap.putInt(KEY_PASOS, pasos)
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }.asPutDataRequest().setUrgent()

            Wearable.getDataClient(context).putDataItem(request).await()
            android.util.Log.d(TAG, "Data Layer sincronizado: FC=$bpm, Pasos=$pasos")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error al sincronizar con Data Layer", e)
        }
    }

    suspend fun actualizarFC(bpm: Int, guardarEnBD: Boolean = true) {
        android.util.Log.d(TAG, "actualizarFC: $bpm")
        _fcFlow.value = bpm
        if (guardarEnBD) {
            try {
                dao?.let {
                    it.insertar(LecturaFC(valorBpm = bpm))
                    android.util.Log.d(TAG, "FC guardada en BD")
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error al guardar FC en BD", e)
            }
        }
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
