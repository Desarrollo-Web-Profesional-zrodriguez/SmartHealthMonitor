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
                    Log.d("HealthDataService", "BPM recibido: $bpm. Sincronizando...")
                    
                    scope.launch {
                        try {
                            SmartHealthRepository.actualizarFC(bpm)
                            // Sincronización primaria (Data Layer)
                            SmartHealthRepository.sincronizarConDataLayer(
                                this@HealthDataService,
                                bpm,
                                SmartHealthRepository.pasosFlow.value
                            )
                            // Sincronización secundaria/respaldo (Messages)
                            wearDataSender.enviarFC(bpm)
                            
                            Log.d("HealthDataService", "FC sincronizada exitosamente")
                        } catch (e: Exception) {
                            Log.e("HealthDataService", "Error en sincronización de FC", e)
                        }
                    }
                }
            }
        }

        // 2. Procesar Pasos Diarios (Acumulados)
        val pasosDataPoints = dataPoints.getData(DataType.STEPS_DAILY)
        if (pasosDataPoints.isNotEmpty()) {
            val ultimoDato = pasosDataPoints.last()
            if (ultimoDato is IntervalDataPoint<Long>) {
                val pasos = ultimoDato.value.toInt()
                Log.d("HealthDataService", "Pasos acumulados hoy: $pasos")
                if (pasos > 0) {
                    actualizarPasosEnRepo(pasos, scope, wearDataSender)
                }
            }
        }

        // 3. Procesar Pasos Instantáneos (Deltas) - Útil para el emulador
        val pasosDeltaPoints = dataPoints.getData(DataType.STEPS)
        if (pasosDeltaPoints.isNotEmpty()) {
            val delta = pasosDeltaPoints.sumOf { (it as? IntervalDataPoint<Long>)?.value ?: 0L }.toInt()
            if (delta > 0) {
                val pasosActuales = SmartHealthRepository.pasosFlow.value
                val nuevosPasos = pasosActuales + delta
                Log.d("HealthDataService", "Delta de pasos: $delta. Total calculado: $nuevosPasos")
                actualizarPasosEnRepo(nuevosPasos, scope, wearDataSender)
            }
        }
    }

    private fun actualizarPasosEnRepo(pasos: Int, scope: CoroutineScope, wearDataSender: WearDataSender) {
        scope.launch {
            try {
                SmartHealthRepository.actualizarPasos(pasos)
                // Sincronización primaria (Data Layer)
                SmartHealthRepository.sincronizarConDataLayer(
                    this@HealthDataService,
                    SmartHealthRepository.fcFlow.value,
                    pasos
                )
                // Sincronización secundaria/respaldo (Messages)
                wearDataSender.enviarPasos(pasos)

                Log.d("HealthDataService", "Pasos sincronizados: $pasos")
            } catch (e: Exception) {
                Log.e("HealthDataService", "Error sincronizando pasos", e)
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
                    DataType.STEPS_DAILY,
                    DataType.STEPS // Añadimos pasos instantáneos como respaldo
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
