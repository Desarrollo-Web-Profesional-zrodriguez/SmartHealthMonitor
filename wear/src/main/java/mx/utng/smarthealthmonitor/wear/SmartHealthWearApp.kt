package mx.utng.smarthealthmonitor.wear

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import mx.utng.smarthealthmonitor.data.models.SmartHealthRepository

class SmartHealthWearApp : Application() {
    
    // Ámbito global que persiste mientras la app esté en memoria
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    lateinit var wearDataSender: WearDataSender
        private set

    override fun onCreate() {
        super.onCreate()
        SmartHealthRepository.init(this)
        wearDataSender = WearDataSender(this)
    }
}
