package mx.utng.smarthealthmonitor.wear

import android.content.Context
import androidx.health.services.client.HealthServices
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.guava.await

class HealthDataService : PassiveListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var wearDataSender: WearDataSender

    override fun onCreate() {
        super.onCreate()
        wearDataSender = WearDataSender(this)  // S6: MessageClient
    }

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        // Procesar Frecuencia Cardíaca
        val fcDataPoints = dataPoints.getData(DataType.HEART_RATE_BPM)
        fcDataPoints.forEach { dataPoint ->
            if (dataPoint is SampleDataPoint<Double>) {
                val bpm = dataPoint.value.toInt()
                scope.launch { wearDataSender.enviarFC(bpm) }
            }
        }

        // Procesar Pasos Diarios
        val pasosDataPoints = dataPoints.getData(DataType.STEPS_DAILY)
        pasosDataPoints.forEach { dataPoint ->
            if (dataPoint is IntervalDataPoint<Long>) {
                val pasos = dataPoint.value.toInt()
                scope.launch { wearDataSender.enviarPasos(pasos) }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
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
