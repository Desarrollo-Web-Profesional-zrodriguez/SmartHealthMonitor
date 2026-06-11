package mx.utng.smarthealthmonitor.wear

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*
import mx.utng.smarthealthmonitor.data.models.SmartHealthRepository

class HealthDataService : Service(), SensorEventListener {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    private lateinit var wearDataSender: WearDataSender

    override fun onCreate() {
        super.onCreate()
        wearDataSender = WearDataSender(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        if (heartRateSensor != null) {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d("HealthDataService", "Sensor de FC registrado correctamente")
        } else {
            Log.e("HealthDataService", "El dispositivo no tiene sensor de frecuencia cardíaca")
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
            val bpm = event.values[0].toInt()
            if (bpm > 0) {
                Log.d("HealthDataService", "Nueva lectura de FC: $bpm")
                // 1. Enviar al teléfono
                scope.launch { wearDataSender.enviarFC(bpm) }
                // 2. Actualizar la UI del reloj
                scope.launch {
                    SmartHealthRepository.actualizarFC(bpm)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        scope.cancel()
    }

    companion object {
        fun iniciar(context: Context) {
            val intent = Intent(context, HealthDataService::class.java)
            context.startService(intent)
        }
    }
}
