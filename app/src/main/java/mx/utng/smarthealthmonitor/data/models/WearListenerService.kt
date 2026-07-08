package mx.utng.smarthealthmonitor.data.models

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WearListenerService : WearableListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val PATH_HEALTH_DATA = "/smarthealth/data"
        private const val TAG = "WearListener"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WearListenerService creado")
        SmartHealthRepository.init(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d(TAG, "onDataChanged: ${dataEvents.count} eventos recibidos")
        dataEvents.forEach { event ->
            Log.d(TAG, "Evento: tipo=${event.type}, path=${event.dataItem.uri.path}")
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == PATH_HEALTH_DATA) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val bpm = dataMap.getInt("fc", 0)
                val pasos = dataMap.getInt("pasos", 0)

                Log.i(TAG, "Sincronización Data Layer RECIBIDA: FC=$bpm, Pasos=$pasos")

                scope.launch {
                    if (bpm > 0) SmartHealthRepository.actualizarFC(bpm)
                    if (pasos > 0) SmartHealthRepository.actualizarPasos(pasos)
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "Mensaje recibido: path=${messageEvent.path}")
        val data = String(messageEvent.data)
        
        scope.launch {
            when (messageEvent.path) {
                "/smarthealthmonitor/fc" -> {
                    val bpm = data.toIntOrNull() ?: 0
                    Log.i(TAG, "Mensaje de FC recibido: $bpm")
                    if (bpm > 0) SmartHealthRepository.actualizarFC(bpm)
                }
                "/smarthealthmonitor/pasos" -> {
                    val pasos = data.toIntOrNull() ?: 0
                    Log.i(TAG, "Mensaje de Pasos recibido: $pasos")
                    if (pasos > 0) SmartHealthRepository.actualizarPasos(pasos)
                }
                "/smarthealth/data" -> {
                    // Soporte para el mismo path que Data Layer pero vía mensaje
                    Log.i(TAG, "Mensaje de data genérico recibido")
                }
            }
        }
    }
}
