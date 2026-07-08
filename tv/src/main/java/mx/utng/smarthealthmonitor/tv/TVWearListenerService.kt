package mx.utng.smarthealthmonitor.tv

import android.content.Intent
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
import mx.utng.smarthealthmonitor.data.models.SmartHealthRepository

class TVWearListenerService : WearableListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val PATH_HEALTH_DATA = "/smarthealth/data"
        private const val TAG = "TVWearListener"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "TVWearListenerService creado")
        SmartHealthRepository.init(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val action = it.action
            val dataUri = it.data
            val extraData = it.getStringExtra("android.intent.extra.DATA")
            
            if (action == "com.google.android.gms.wearable.MESSAGE_RECEIVED" && dataUri != null && extraData != null) {
                val path = dataUri.path
                Log.d(TAG, "Simulación ADB recibida: path=$path, data=$extraData")
                
                scope.launch {
                    when (path) {
                        "/smarthealthmonitor/fc" -> {
                            val bpm = extraData.toIntOrNull() ?: 0
                            if (bpm > 0) SmartHealthRepository.actualizarFC(bpm, guardarEnBD = true)
                        }
                        "/smarthealthmonitor/pasos" -> {
                            val pasos = extraData.toIntOrNull() ?: 0
                            if (pasos > 0) SmartHealthRepository.actualizarPasos(pasos)
                        }
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d(TAG, "onDataChanged: ${dataEvents.count} eventos recibidos")
        dataEvents.forEach { event ->
            Log.d(TAG, "Evento: tipo=${event.type}, path=${event.dataItem.uri.path}")
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == PATH_HEALTH_DATA) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val bpm = dataMap.getInt("fc", 0)
                val pasos = dataMap.getInt("pasos", 0)

                Log.i(TAG, "Sincronización Data Layer en TV: FC=$bpm, Pasos=$pasos")

                scope.launch {
                    if (bpm > 0) SmartHealthRepository.actualizarFC(bpm, guardarEnBD = true)
                    if (pasos > 0) SmartHealthRepository.actualizarPasos(pasos)
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "Mensaje recibido en TV: path=${messageEvent.path}")
        val data = String(messageEvent.data)
        
        scope.launch {
            when (messageEvent.path) {
                "/smarthealthmonitor/fc" -> {
                    val bpm = data.toIntOrNull() ?: 0
                    Log.i(TAG, "Mensaje de FC recibido en TV: $bpm")
                    if (bpm > 0) SmartHealthRepository.actualizarFC(bpm, guardarEnBD = true)
                }
                "/smarthealthmonitor/pasos" -> {
                    val pasos = data.toIntOrNull() ?: 0
                    Log.i(TAG, "Mensaje de Pasos recibido en TV: $pasos")
                    if (pasos > 0) SmartHealthRepository.actualizarPasos(pasos)
                }
            }
        }
    }
}
