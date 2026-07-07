package mx.utng.smarthealthmonitor.tv

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment

/**
 * Fragmento principal para Android TV usando Leanback.
 */
class MainFragment : BrowseSupportFragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        @Suppress("DEPRECATION")
        super.onActivityCreated(savedInstanceState)
        setupUIElements()
    }

    private fun setupUIElements() {
        title = "SmartHealth TV"
        // Aquí se configurarán las filas y el adaptador más adelante
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        brandColor = ContextCompat.getColor(requireContext(), R.color.sh_primary)
    }
}