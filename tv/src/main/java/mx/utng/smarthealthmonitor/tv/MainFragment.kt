package mx.utng.smarthealthmonitor.tv

import android.os.Bundle
import android.view.View
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import mx.utng.smarthealthmonitor.data.models.MockData
import mx.utng.smarthealthmonitor.data.db.LecturaFC

class MainFragment : BrowseSupportFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración del BrowseFragment
        title        = "SmartHealth TV"
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        // Color de la marca en el sidebar
        brandColor = resources.getColor(R.color.sh_primary, null)

        cargarFilas()
    }

    private fun cargarFilas() {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

        // ── Fila 1: Estado actual (FC + Pasos) ───────────
        val estadoAdapter = ArrayObjectAdapter(FCCardPresenter())
        // Datos simulados — en Ej.03 vendrán de Room
        estadoAdapter.add(LecturaFC(id=0, valorBpm=88, hora="Ahora"))
        estadoAdapter.add(LecturaFC(id=1, valorBpm=4250, hora="Pasos"))
        rowsAdapter.add(ListRow(HeaderItem("Estado actual"), estadoAdapter))

        // ── Fila 2: Historial de FC ────────────────────
        val histAdapter = ArrayObjectAdapter(FCCardPresenter())
        MockData.historialFC.forEach { histAdapter.add(it) }
        rowsAdapter.add(ListRow(HeaderItem("Historial FC"), histAdapter))

        // ── Fila 3: Alertas recientes ──────────────────
        val alertAdapter = ArrayObjectAdapter(FCCardPresenter())
        alertAdapter.add(LecturaFC(valorBpm = 115, hora = "08:20", esNormal = false))
        alertAdapter.add(LecturaFC(valorBpm = 55, hora = "10:45", esNormal = false))
        alertAdapter.add(LecturaFC(valorBpm = 120, hora = "14:10", esNormal = false))
        rowsAdapter.add(ListRow(HeaderItem("Alertas recientes"), alertAdapter))

        this.adapter = rowsAdapter
    }
}