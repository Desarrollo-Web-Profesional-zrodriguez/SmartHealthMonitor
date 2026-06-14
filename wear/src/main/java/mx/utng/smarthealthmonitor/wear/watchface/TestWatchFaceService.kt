package mx.utng.smarthealthmonitor.wear.watchface

import android.view.SurfaceHolder
import androidx.wear.watchface.*
import androidx.wear.watchface.style.*

class TestWatchFaceService : WatchFaceService() {

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        val renderer = TestRenderer(
            surfaceHolder              = surfaceHolder,
            watchState                 = watchState,
            complicationSlotsManager   = complicationSlotsManager,
            currentUserStyleRepository = currentUserStyleRepository,
            interactiveDrawModeUpdateDelayMillis = 1_000L
        )
        return WatchFace(WatchFaceType.DIGITAL, renderer)
    }
}
