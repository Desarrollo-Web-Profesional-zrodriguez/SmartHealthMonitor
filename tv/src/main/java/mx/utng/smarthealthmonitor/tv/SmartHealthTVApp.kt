package mx.utng.smarthealthmonitor.tv

import android.app.Application
import mx.utng.smarthealthmonitor.data.models.SmartHealthRepository

class SmartHealthTVApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar el repositorio para que el DAO no sea nulo
        SmartHealthRepository.init(this)
    }
}