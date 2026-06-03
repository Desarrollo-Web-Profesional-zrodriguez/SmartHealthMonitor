package mx.utng.smarthealthmonitor.data.models

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WearListenerService : WearableListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val PATH_FC    = "/smarthealthmonitor/fc"
        const val PATH_PASOS = "/smarthealthmonitor/pasos"
        const val PATH_SPO2  = "/smarthealthmonitor/spo2"
        private const val TAG = "WearListener"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WearListenerService creado")
        SmartHealthRepository.init(this)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val data   = String(messageEvent.data)
        val path   = messageEvent.path
        Log.i(TAG, "¡MENSAJE RECIBIDO! path=$path, data=$data")

        when (path) {
            PATH_FC -> {
                val bpm = data.toIntOrNull() ?: return
                Log.d(TAG, "Actualizando FC en repositorio: $bpm")
                scope.launch {
                    SmartHealthRepository.actualizarFC(bpm)
                }
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
