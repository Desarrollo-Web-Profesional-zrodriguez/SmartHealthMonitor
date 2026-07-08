package mx.utng.smarthealthmonitor.wear

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mx.utng.smarthealthmonitor.data.models.SmartHealthRepository

class WearSyncListenerService : WearableListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        const val PATH_HEALTH_DATA = "/smarthealth/data"
        private const val TAG = "WearSync"
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == PATH_HEALTH_DATA) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val bpm = dataMap.getInt("fc", 0)
                val pasos = dataMap.getInt("pasos", 0)

                Log.d(TAG, "Sincronización recibida desde Data Layer: FC=$bpm, Pasos=$pasos")

                scope.launch {
                    if (bpm > 0) SmartHealthRepository.actualizarFC(bpm, guardarEnBD = false)
                    if (pasos > 0) SmartHealthRepository.actualizarPasos(pasos)
                }
            }
        }
    }
}