package mx.utng.smarthealthmonitor.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import kotlinx.coroutines.launch
import mx.utng.smarthealthmonitor.BuildConfig
import mx.utng.smarthealthmonitor.data.models.AppDataSender
import mx.utng.smarthealthmonitor.ui.viewmodel.DashboardViewModel
import mx.utng.smarthealthmonitor.FilaHistorial
import mx.utng.smarthealthmonitor.data.models.MockData
import mx.utng.smarthealthmonitor.data.models.SmartHealthRepository
import mx.utng.smarthealthmonitor.ui.components.TarjetaDato
import mx.utng.smarthealthmonitor.ui.theme.SmartHealthMonitorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onHistorialClick: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val fc        by viewModel.fc.collectAsState()
    val pasos     by viewModel.pasos.collectAsState()
    val spO2      by viewModel.spO2.collectAsState()
    val historial by viewModel.historial.collectAsState()

    // ── Estado del diálogo y Snackbar ──────────────────────
    var mostrarAlerta by remember { mutableStateOf(false) }
    val snackbarHost  = remember { SnackbarHostState() }
    val scope         = rememberCoroutineScope()

    // ── Diálogo condicional ────────────────────────────────
    if (mostrarAlerta) {
        AlertaScreen(
            fc          = fc,
            onDismiss   = { mostrarAlerta = false },
            onConfirmar = { nota ->
                mostrarAlerta = false
                scope.launch {
                    val mensaje = if (nota.isBlank()) {
                        "✅ Alerta enviada a tus contactos de emergencia"
                    } else {
                        "✅ Alerta enviada con nota: $nota"
                    }
                    val result = snackbarHost.showSnackbar(
                        message     = mensaje,
                        actionLabel = "Deshacer",
                        duration    = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        snackbarHost.showSnackbar(
                            message = "❌ Alerta cancelada",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        )
    }

    SmartHealthMonitorTheme {
        Scaffold(
            // ── Snackbar host en el Scaffold ───────────────
            snackbarHost = { SnackbarHost(hostState = snackbarHost) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "SmartHealth",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    actions = {
                        // CastButton: AndroidView que envuelve MediaRouteButton
                        AndroidView(
                            factory = { context ->
                                MediaRouteButton(context).apply {
                                    CastButtonFactory.setUpMediaRouteButton(context, this)
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    colors = TopAppBarDefaults
                        .topAppBarColors(
                            containerColor    = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick        = { mostrarAlerta = true },
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(Icons.Default.Warning,
                        contentDescription = "Enviar alerta de emergencia",
                        tint = MaterialTheme.colorScheme.onError)
                }
            }
        ) { paddingValues ->
            LazyColumn(Modifier.padding(paddingValues)) {

                // ── Tarjeta FC ────────────────────────────
                item {
                    val esNormal = fc in 60..100
                    val colorAlerta = if (esNormal) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    TarjetaDato(
                        valor      = "$fc",
                        unidad     = "bpm",
                        label      = "Frecuencia cardíaca",
                        colorValor = colorAlerta,
                        icono      = Icons.Default.Favorite,
                        estado      = if (esNormal) "Normal" else "Consulta al médico",
                        colorEstado = colorAlerta
                    )
                }
                
                // ── Tarjeta SpO2 ──────────────────────────
                item {
                    val esNormalSpO2 = spO2 >= 95
                    TarjetaDato(
                        valor      = "$spO2",
                        unidad     = "%",
                        label      = "Saturación de Oxígeno",
                        colorValor = MaterialTheme.colorScheme.tertiary,
                        icono      = Icons.Default.Bloodtype,
                        estado      = if (esNormalSpO2) "Normal" else "Bajo (revisar)",
                        colorEstado = if (esNormalSpO2) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    )
                }

                // ── Tarjeta Pasos ─────────────────────────
                item {
                    TarjetaDato(
                        valor      = "%,d".format(pasos),
                        unidad     = "pasos",
                        label      = "Pasos del día",
                        colorValor = MaterialTheme.colorScheme.primary,
                        icono      = Icons.AutoMirrored.Filled.DirectionsWalk
                    )
                }
                
                // ── Encabezado historial ──────────────────
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("Historial reciente",
                            style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = onHistorialClick) {
                            Text("Ver todo")
                        }
                    }
                }
                // ── Lista del historial ───────────────────
                items(historial, key = { it.id }) { lectura ->
                    FilaHistorial(lectura = lectura)
                }
                item {
                    // Botón de simulación — SOLO PARA DEBUG
                    val scope = rememberCoroutineScope()
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val dataSender = remember { AppDataSender(context) }
                    
                    if (BuildConfig.DEBUG) {
                        OutlinedButton (
                            onClick = {
                                // Simular lectura del wearable
                                val fcSimulado = (60..110).random()
                                val pasosSimulados = (3000..8000).random()
                                scope.launch {
                                    SmartHealthRepository.actualizarFC(fcSimulado)
                                    SmartHealthRepository.actualizarPasos(pasosSimulados)
                                    // Sincronizar vía Data Layer (Estado Global)
                                    SmartHealthRepository.sincronizarConDataLayer(
                                        context,
                                        fcSimulado,
                                        pasosSimulados
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Simular y Sincronizar (DEBUG)")
                        }
                    }
                }

            }
        }
    }
}

@Preview(showBackground = true, name = "Dashboard - Light",
    showSystemUi = true, device = "id:pixel_6")
@Preview(showBackground = true, name = "Dashboard - Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DashboardScreenPreview() {
    SmartHealthMonitorTheme {
        DashboardScreen()
    }
}
