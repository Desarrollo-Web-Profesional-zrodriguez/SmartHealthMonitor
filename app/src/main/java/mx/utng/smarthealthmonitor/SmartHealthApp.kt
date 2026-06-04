package mx.utng.smarthealthmonitor

import android.app.Application
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mx.utng.smarthealthmonitor.data.models.SmartHealthRepository

class SmartHealthApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar Repositorio (y base de datos)
        SmartHealthRepository.init(this)

        // Limpiar historial antiguo al iniciar (más de 7 días)
        MainScope().launch {
            SmartHealthRepository.limpiarHistorialAntiguo()
        }
    }
}
