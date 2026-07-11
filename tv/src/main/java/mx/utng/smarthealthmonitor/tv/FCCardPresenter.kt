package mx.utng.smarthealthmonitor.tv

import android.graphics.Color
import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import mx.utng.smarthealthmonitor.data.db.LecturaFC

class FCCardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            // CRÍTICO: sin estas dos líneas,
            // el D-pad no puede navegar a este card
            isFocusable           = true
            isFocusableInTouchMode = true
            setMainImageDimensions(240, 180)
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val card    = viewHolder.view as ImageCardView
        val lectura = item as LecturaFC

        if (lectura.hora == "Pasos") {
            card.titleText = "${lectura.valorBpm} pasos"
        } else {
            card.titleText = "${lectura.valorBpm} bpm"
        }
        card.contentText = lectura.hora

        // Color de fondo según si FC es normal (o si son pasos)
        val isHealthy = if (lectura.hora == "Pasos") {
            lectura.valorBpm >= 0 // Siempre azul para pasos
        } else {
            lectura.esNormal
        }

        val bgColor = if (isHealthy) {
            Color.parseColor("#1B4F8A")  // primary (azul)
        } else {
            Color.parseColor("#B3261E")  // error (rojo)
        }
        card.setBackgroundColor(bgColor)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        (viewHolder.view as ImageCardView).mainImage = null
    }
}