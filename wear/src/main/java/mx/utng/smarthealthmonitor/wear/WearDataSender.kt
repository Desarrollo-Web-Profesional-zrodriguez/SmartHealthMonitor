package mx.utng.smarthealthmonitor.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class WearDataSender(private val context: Context) {

    suspend fun enviarFC(bpm: Int) {
        Log.d("WearDataSender", "Intentando enviar FC: $bpm")
        enviarMensaje("/smarthealthmonitor/fc", bpm.toString())
    }

    private suspend fun enviarMensaje(path: String, data: String) {
        try {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            Log.d("WearDataSender", "Nodos conectados encontrados: ${nodes.size}")
            nodes.forEach { node ->
                Log.d("WearDataSender", "Enviando mensaje a nodo: ${node.displayName} (id: ${node.id})")
                val result = Wearable.getMessageClient(context).sendMessage(
                    node.id,
                    path,
                    data.toByteArray()
                ).await()
                Log.d("WearDataSender", "Resultado de envío: $result")
            }
            if (nodes.isEmpty()) {
                Log.w("WearDataSender", "No hay nodos conectados para enviar el mensaje.")
            }
        } catch (e: Exception) {
            Log.e("WearDataSender", "Error al enviar mensaje", e)
        }
    }
}
