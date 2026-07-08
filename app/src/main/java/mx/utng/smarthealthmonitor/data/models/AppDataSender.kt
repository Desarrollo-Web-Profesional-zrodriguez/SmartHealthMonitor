package mx.utng.smarthealthmonitor.data.models

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class AppDataSender(private val context: Context) {

    suspend fun enviarFC(bpm: Int) {
        enviarMensaje("/smarthealthmonitor/fc", bpm.toString())
    }

    suspend fun enviarPasos(pasos: Int) {
        enviarMensaje("/smarthealthmonitor/pasos", pasos.toString())
    }

    private suspend fun enviarMensaje(path: String, data: String) {
        try {
            // Buscamos todos los nodos (Reloj y TV)
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            nodes.forEach { node ->
                Log.d("AppDataSender", "Sincronizando con: ${node.displayName}")
                Wearable.getMessageClient(context).sendMessage(
                    node.id, path, data.toByteArray()
                ).await()
            }
        } catch (e: Exception) {
            Log.e("AppDataSender", "Error al sincronizar datos", e)
        }
    }
}