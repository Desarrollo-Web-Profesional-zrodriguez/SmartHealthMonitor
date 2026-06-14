package mx.utng.smarthealthmonitor.wear

import android.content.Context
import android.util.Log
import androidx.health.services.client.HealthServices
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.guava.await
import mx.utng.smarthealthmonitor.data.models.SmartHealthRepository

class HealthDataService : PassiveListenerService() {

    private var ultimoBpm = 0

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        val app = application as SmartHealthWearApp
        val scope = app.applicationScope
        val wearDataSender = app.wearDataSender

        Log.d("HealthDataService", "onNewDataPointsReceived")
        
        // 1. Procesar Frecuencia Cardíaca
        val fcDataPoints = dataPoints.getData(DataType.HEART_RATE_BPM)
        if (fcDataPoints.isNotEmpty()) {
            val ultimoDataPoint = fcDataPoints.last()
            if (ultimoDataPoint is SampleDataPoint<Double>) {
                val bpm = ultimoDataPoint.value.toInt()
                
                if (bpm != ultimoBpm && bpm > 0) {
                    ultimoBpm = bpm
                    Log.d("HealthDataService", "BPM recibido: $bpm. Enviando con AppScope...")
                    
                    scope.launch {
                        try {
                            wearDataSender.enviarFC(bpm)
                            SmartHealthRepository.actualizarFC(bpm)
                            Log.d("HealthDataService", "FC enviada y guardada exitosamente")
                        } catch (e: Exception) {
                            Log.e("HealthDataService", "Error en proceso de FC", e)
                        }
                    }
                }
            }
        }

        // 2. Procesar Pasos Diarios
        val pasosDataPoints = dataPoints.getData(DataType.STEPS_DAILY)
        if (pasosDataPoints.isNotEmpty()) {
            val ultimoDato = pasosDataPoints.last()
            if (ultimoDato is IntervalDataPoint<Long>) {
                val pasos = ultimoDato.value.toInt()
                Log.d("HealthDataService", "Pasos recibidos: $pasos. Enviando con AppScope...")
                
                scope.launch {
                    try {
                        wearDataSender.enviarPasos(pasos)
                        SmartHealthRepository.actualizarPasos(pasos)
                        Log.d("HealthDataService", "Pasos enviados y guardados exitosamente")
                    } catch (e: Exception) {
                        Log.e("HealthDataService", "Error en proceso de Pasos", e)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        Log.d("HealthDataService", "Service onDestroy (la tarea seguirá en AppScope)")
        super.onDestroy()
    }

    companion object {
        suspend fun registrar(context: Context) {
            val hsClient = HealthServices.getClient(context)
            val passiveClient = hsClient.passiveMonitoringClient

            val config = PassiveListenerConfig.builder()
                .setDataTypes(setOf(
                    DataType.HEART_RATE_BPM,
                    DataType.STEPS_DAILY
                ))
                .setShouldUserActivityInfoBeRequested(true)
                .build()

            passiveClient.setPassiveListenerServiceAsync(
                HealthDataService::class.java,
                config
            ).await()
        }
    }
}
