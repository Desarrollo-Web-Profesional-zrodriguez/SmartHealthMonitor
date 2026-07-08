package mx.utng.smarthealthmonitor.tv

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.flow.combine
import mx.utng.smarthealthmonitor.data.db.LecturaFC

class MainFragment : BrowseSupportFragment() {

    private val viewModel: TvViewModel by viewModels()
    private lateinit var histAdapter: ArrayObjectAdapter
    private lateinit var estadoAdapter: ArrayObjectAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = "SmartHealth TV"
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        brandColor = resources.getColor(R.color.sh_primary, null)

        cargarFilas()
        observarDatos()
    }

    private fun observarDatos() {
        // 1. Observar historial de Room
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.historial.collect { lecturas ->
                    histAdapter.clear()
                    lecturas.forEach { histAdapter.add(it) }
                }
            }
        }

        // 2. Observar datos actuales (FC y Pasos) combinados para evitar parpadeos
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(viewModel.fc, viewModel.pasos) { fc, pasos ->
                    Pair(fc, pasos)
                }.collect { (fc, pasos) ->
                    Log.d("MainFragment", "Actualizando UI TV: FC=$fc, Pasos=$pasos")
                    actualizarEstado(fc, pasos)
                }
            }
        }
    }

    private fun actualizarEstado(fc: Int, pasos: Int) {
        estadoAdapter.clear()
        // Card de Frecuencia Cardíaca actual
        estadoAdapter.add(LecturaFC(valorBpm = fc, hora = "Ahora"))
        // Card de Pasos
        estadoAdapter.add(LecturaFC(valorBpm = pasos, hora = "Pasos"))
    }

    private fun cargarFilas() {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

        // Fila 1: Estado actual
        estadoAdapter = ArrayObjectAdapter(FCCardPresenter())
        rowsAdapter.add(ListRow(HeaderItem("Estado actual"), estadoAdapter))

        // Fila 2: Historial FC
        histAdapter = ArrayObjectAdapter(FCCardPresenter())
        rowsAdapter.add(ListRow(HeaderItem("Historial FC"), histAdapter))

        this.adapter = rowsAdapter
    }
}