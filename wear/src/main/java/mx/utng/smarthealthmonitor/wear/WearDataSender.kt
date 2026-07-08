package mx.utng.smarthealthmonitor.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class WearDataSender(private val context: Context) {

    suspend fun enviarFC(bpm: Int) {
        Log.d("WearDataSender", "Intentando enviar FC: $bpm")
        enviarMensaje("/smarthealthmonitor/fc", bpm.toString())
    }

    suspend fun enviarPasos(pasos: Int) {
        Log.d("WearDataSender", "Intentando enviar Pasos: $pasos")
        enviarMensaje("/smarthealthmonitor/pasos", pasos.toString())
    }

    private suspend fun enviarMensaje(path: String, data: String) {
        try {
            // 1. Buscamos nodos que tengan la capacidad de recibir datos
            val capabilityInfo = Wearable.getCapabilityClient(context)
                .getCapability("health_monitor_receiver", CapabilityClient.FILTER_ALL) // Cambiado a FILTER_ALL
                .await()
            
            val targetNodes = capabilityInfo.nodes.toMutableSet()
            Log.d("WearDataSender", "Nodos con capacidad encontrados: ${targetNodes.size}")
            
            // 2. Si no hay nodos con la capacidad, intentamos con todos los nodos conectados como respaldo
            if (targetNodes.isEmpty()) {
                Log.w("WearDataSender", "No se encontraron nodos con capacidad. Intentando con todos los nodos conectados...")
                val allNodes = Wearable.getNodeClient(context).connectedNodes.await()
                targetNodes.addAll(allNodes)
            }

            if (targetNodes.isEmpty()) {
                Log.e("WearDataSender", "FALLO CRÍTICO: No se detectaron dispositivos (Phone o TV) conectados.")
                return
            }

            // 3. Enviamos el mensaje a cada nodo único
            targetNodes.forEach { node ->
                Log.d("WearDataSender", "Enviando data ($path) a: ${node.displayName} (id: ${node.id})")
                try {
                    Wearable.getMessageClient(context).sendMessage(
                        node.id,
                        path,
                        data.toByteArray()
                    ).await()
                    Log.i("WearDataSender", "Mensaje enviado exitosamente a ${node.displayName}")
                } catch (e: Exception) {
                    Log.e("WearDataSender", "Error al enviar mensaje a nodo ${node.id}", e)
                }
            }
        } catch (e: Exception) {
            Log.e("WearDataSender", "Error general en WearDataSender", e)
        }
    }
}
