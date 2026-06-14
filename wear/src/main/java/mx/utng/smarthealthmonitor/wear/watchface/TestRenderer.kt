package mx.utng.smarthealthmonitor.wear.watchface

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.SurfaceHolder
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import java.time.ZonedDateTime
import java.util.Locale

class TestRenderer(
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    complicationSlotsManager: ComplicationSlotsManager,
    currentUserStyleRepository: CurrentUserStyleRepository,
    interactiveDrawModeUpdateDelayMillis: Long
) : Renderer.CanvasRenderer2<Renderer.SharedAssets>(
    surfaceHolder, currentUserStyleRepository, watchState,
    CanvasType.HARDWARE, interactiveDrawModeUpdateDelayMillis,
    false
) {

    private val textPaint = Paint().apply {
        color = Color.CYAN
        textSize = 60f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val subTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 30f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    override suspend fun createSharedAssets(): SharedAssets =
        object : SharedAssets { override fun onDestroy() {} }

    override fun render(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime, sharedAssets: SharedAssets) {
        // Fondo Gris Oscuro
        canvas.drawColor(Color.parseColor("#121212"))

        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()

        // Hora
        val time = String.format(Locale.getDefault(), "%02d:%02d", zonedDateTime.hour, zonedDateTime.minute)
        canvas.drawText(time, cx, cy, textPaint)

        // Etiqueta de prueba
        canvas.drawText("TEST MODE", cx, cy + 50f, subTextPaint)
    }

    override fun renderHighlightLayer(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime, sharedAssets: SharedAssets) {
        canvas.drawColor(renderParameters.highlightLayer!!.backgroundTint)
    }
}
