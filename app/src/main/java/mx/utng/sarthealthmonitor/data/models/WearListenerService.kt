package mx.utng.smarthealthmonitor.data.models

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class WearListenerService : WearableListenerService() {

    companion object {
        const val PATH_FC    = "/smarthealthmonitor/fc"
        const val PATH_PASOS = "/smarthealthmonitor/pasos"
        const val PATH_SPO2  = "/smarthealthmonitor/spo2"
        private const val TAG = "WearListener"
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val data   = String(messageEvent.data)
        val path   = messageEvent.path
        Log.d(TAG, "Mensaje recibido: path=$path, data=$data")

        when (path) {
            PATH_FC -> {
                val bpm = data.toIntOrNull() ?: return
                SmartHealthRepository.actualizarFC(bpm)
            }
            PATH_PASOS -> {
                val pasos = data.toIntOrNull() ?: return
                SmartHealthRepository.actualizarPasos(pasos)
            }
            PATH_SPO2 -> {
                val spo2 = data.toIntOrNull() ?: return
                SmartHealthRepository.actualizarSpO2(spo2)
            }
            else -> Log.w(TAG, "Path desconocido: $path")
        }
    }
}
