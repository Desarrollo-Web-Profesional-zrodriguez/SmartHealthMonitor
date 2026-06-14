package mx.utng.smarthealthmonitor.wear.watchface

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.view.SurfaceHolder
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import mx.utng.smarthealthmonitor.data.models.SmartHealthRepository
import java.time.ZonedDateTime
import java.util.Locale

class SmartHealthRenderer(
    private val context: Context,
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    complicationSlotsManager: ComplicationSlotsManager,
    currentUserStyleRepository: CurrentUserStyleRepository,
    interactiveDrawModeUpdateDelayMillis: Long
) : Renderer.CanvasRenderer2<Renderer.SharedAssets>(
    surfaceHolder,
    currentUserStyleRepository,
    watchState,
    CanvasType.SOFTWARE, // MUST BE SOFTWARE
    interactiveDrawModeUpdateDelayMillis,
    false
) {
    private val paintHora = Paint().apply {
        color     = Color.WHITE
        textSize  = 72f
        isAntiAlias = true
        typeface  = Typeface.DEFAULT_BOLD
    }
    private val paintFC = Paint().apply {
        color     = Color.RED
        textSize  = 30f
        isAntiAlias = true
    }
    private val paintPasos = Paint().apply {
        color     = Color.CYAN
        textSize  = 26f
        isAntiAlias = true
    }
    private val paintSub = Paint().apply {
        color     = Color.GRAY
        textSize  = 22f
        isAntiAlias = true
    }

    override suspend fun createSharedAssets(): SharedAssets =
        object : SharedAssets { override fun onDestroy() {} }

    override fun render(canvas: Canvas, bounds: Rect,
                        zonedDateTime: ZonedDateTime,
                        sharedAssets: SharedAssets) {

        val isAmbient = renderParameters.drawMode == DrawMode.AMBIENT

        // Configurar AntiAlias según el modo (Ahorro de batería en AOD)
        paintHora.isAntiAlias = !isAmbient
        paintSub.isAntiAlias = !isAmbient
        paintFC.isAntiAlias = !isAmbient

        // Fondo negro — ahorra batería en modo AOD
        canvas.drawColor(Color.BLACK)

        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()

        // Hora digital centrada
        val hora = String.format(Locale.getDefault(), "%02d:%02d",
            zonedDateTime.hour, zonedDateTime.minute)
        val tw = paintHora.measureText(hora)

        // En modo AOD, centramos la hora un poco mejor verticalmente ya que no hay más elementos
        val yPos = if (isAmbient) cy + (paintHora.textSize / 3f) else cy - 10f
        canvas.drawText(hora, cx - tw / 2, yPos, paintHora)

        // Elementos extras solo si NO estamos en Ambient Mode
        if (!isAmbient) {
            paintPasos.isAntiAlias = true
            
            // Segundos (pequeño debajo)
            val seg = String.format(Locale.getDefault(), "%02d", zonedDateTime.second)
            canvas.drawText(seg, cx - 18f, cy + 30f, paintSub)

            // FC desde SmartHealthRepository
            val fc = try {
                SmartHealthRepository.fcFlow.value
            } catch (e: Exception) {
                0
            }
            if (fc > 0) {
                val fcStr = "❤ $fc bpm"
                val fcW = paintFC.measureText(fcStr)
                canvas.drawText(fcStr, cx - fcW/2, cy + 70f, paintFC)
            }

            // Pasos desde SmartHealthRepository
            val pasos = try {
                SmartHealthRepository.pasosFlow.value
            } catch (e: Exception) {
                0
            }
            val pasosStr = "👣 $pasos"
            val pasosW = paintPasos.measureText(pasosStr)
            canvas.drawText(pasosStr, cx - pasosW/2, cy + 105f, paintPasos)
        }
    }

    override fun renderHighlightLayer(canvas: Canvas, bounds: Rect,
                                      zonedDateTime: ZonedDateTime, sharedAssets: SharedAssets) {
        canvas.drawColor(renderParameters.highlightLayer!!.backgroundTint)
    }
}
