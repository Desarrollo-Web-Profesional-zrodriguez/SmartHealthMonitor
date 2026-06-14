package mx.utng.smarthealthmonitor.wear.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import mx.utng.smarthealthmonitor.wear.HealthDataService
import mx.utng.smarthealthmonitor.wear.presentation.theme.SmartHealthWearTheme

class WearMainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[android.Manifest.permission.BODY_SENSORS] == true) {
            Log.d("WearMainActivity", "Permisos concedidos, registrando HealthDataService")
            registrarServicioSalud()
        } else {
            Log.e("WearMainActivity", "Permisos denegados")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pedir permisos en tiempo de ejecución
        permissionLauncher.launch(arrayOf(
            android.Manifest.permission.BODY_SENSORS,
            android.Manifest.permission.ACTIVITY_RECOGNITION
        ))

        setContent {
            SmartHealthWearTheme {
                SmartHealthWearNavGraph()
            }
        }
    }

    private fun registrarServicioSalud() {
        lifecycleScope.launch {
            try {
                HealthDataService.registrar(this@WearMainActivity)
                Log.d("WearMainActivity", "Servicio registrado en Health Services")
            } catch (e: Exception) {
                Log.e("WearMainActivity", "Error al registrar servicio", e)
            }
        }
    }
}
