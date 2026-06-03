package mx.utng.smarthealthmonitor.wear

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import mx.utng.myapplication.presentation.theme.SartHealthMonitorTheme

class WearMainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.BODY_SENSORS] == true) {
            registrarHealthServices()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        )

        setContent {
            WearApp()
        }
    }

    private fun registrarHealthServices() {
        lifecycleScope.launch {
            try {
                HealthDataService.registrar(applicationContext)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

@Composable
fun WearApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var bpmValue by remember { mutableFloatStateOf(80f) }
    var pasosValue by remember { mutableFloatStateOf(2000f) }

    SartHealthMonitorTheme {
        AppScaffold {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(
                scrollState = listState,
            ) { contentPadding ->
                TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
                    item {
                        ListHeader(
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(text = "Prueba de Sensores")
                        }
                    }
                    
                    // --- SECCIÓN RITMO CARDÍACO ---
                    item {
                        Text(
                            text = "Ritmo: ${bpmValue.toInt()} BPM",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    item {
                        Slider(
                            value = bpmValue.toInt(),
                            onValueChange = { bpmValue = it.toFloat() },
                            valueProgression = 40..180,
                            modifier = Modifier.fillMaxWidth(),
                            decreaseIcon = { Icon(Icons.Default.Remove, "Menos") },
                            increaseIcon = { Icon(Icons.Default.Add, "Más") },
                            segmented = true
                        )
                    }
                    item {
                        Button(
                            onClick = {
                                scope.launch {
                                    WearDataSender(context).enviarFC(bpmValue.toInt())
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Enviar FC")
                        }
                    }

                    // --- SECCIÓN PASOS ---
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    }
                    item {
                        Text(
                            text = "Pasos: ${pasosValue.toInt()}",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    item {
                        Slider(
                            value = pasosValue.toInt(),
                            onValueChange = { pasosValue = it.toFloat() },
                            valueProgression = 0..10000,
                            modifier = Modifier.fillMaxWidth(),
                            decreaseIcon = { Icon(Icons.Default.Remove, "Menos") },
                            increaseIcon = { Icon(Icons.Default.Add, "Más") },
                        )
                    }
                    item {
                        Button(
                            onClick = {
                                scope.launch {
                                    WearDataSender(context).enviarPasos(pasosValue.toInt())
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Enviar Pasos")
                        }
                    }
                }
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun DefaultPreview() {
    WearApp()
}
