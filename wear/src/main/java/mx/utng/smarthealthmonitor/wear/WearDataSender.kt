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
            // Buscamos nodos que tengan la capacidad de recibir datos
            val capabilityInfo = Wearable.getCapabilityClient(context)
                .getCapability("health_monitor_receiver", CapabilityClient.FILTER_ALL)
                .await()
            
            val nodes = capabilityInfo.nodes
            Log.d("WearDataSender", "Nodos con capacidad encontrados: ${nodes.size}")
            
            nodes.forEach { node ->
                Log.d("WearDataSender", "Enviando mensaje a nodo: ${node.displayName} (id: ${node.id})")
                val result = Wearable.getMessageClient(context).sendMessage(
                    node.id,
                    path,
                    data.toByteArray()
                ).await()
                Log.d("WearDataSender", "Resultado de envío: $result")
            }

            // Intento de respaldo - Listar TODOS los nodos para diagnóstico
            val allNodes = Wearable.getNodeClient(context).connectedNodes.await()
            Log.d("WearDataSender", "DIAGNÓSTICO: Total de nodos detectados por el sistema: ${allNodes.size}")
            allNodes.forEach { node ->
                Log.d("WearDataSender", "Nodo detectado: ID=${node.id}, Nombre=${node.displayName}, ¿Está cerca?=${node.isNearby}")
                // Intentamos enviar a todos, sin importar capacidades
                Wearable.getMessageClient(context).sendMessage(node.id, path, data.toByteArray()).await()
            }

            if (nodes.isEmpty() && allNodes.isEmpty()) {
                Log.w("WearDataSender", "FALLO CRÍTICO: El reloj no detecta NINGÚN dispositivo conectado (teléfono).")
            }
        } catch (e: Exception) {
            Log.e("WearDataSender", "Error al enviar mensaje", e)
        }
    }
}
