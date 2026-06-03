package mx.utng.smarthealthmonitor

import android.app.Application
import mx.utng.smarthealthmonitor.data.models.SmartHealthRepository

class SmartHealthApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar Repositorio (y base de datos)
        SmartHealthRepository.init(this)
    }
}
